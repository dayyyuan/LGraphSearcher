/*
 * Created on Dec 11, 2004
 * 
 * Copyright 2004, 2005 Marc Wörlein
 * 
 * This file is part of ParMol.
 * ParMol is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * ParMol is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ParMol; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
package edu.psu.chemxseer.structure.iterative.FeatureGenerator;

import java.util.*;

import de.parmol.graph.Graph;
import de.parmol.graph.MutableGraph;
import de.parmol.parsers.GraphParser;
import de.parmol.util.Debug;
import de.parmol.util.FrequentFragment;
import edu.psu.chemxseer.structure.parmol.gSpanInduced.DataBaseModified;
import edu.psu.chemxseer.structure.parmol.gSpanInduced.GSpanGraph;
import edu.psu.chemxseer.structure.parmol.gSpanInduced.GraphSet;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * This class represents a DFSCode (list of GSpanEdges) for an created SubGraph
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 * @author duy113: edit to support mining based on common subgraphs
 * 
 *         Dayu: Change all data member into protected
 * 
 *         Change A Little Bit:
 * 
 *         string from the first edge: edge.next is the next edge added into the
 *         code for each rootGraph string: edge = edge.next.prev for newly added
 *         rootGraph string: edge.prev = previous edge that was attached to
 *         brunch And it equals to null, if it is the first edge attached to
 *         this brunch
 */
public class DFSCode2 implements Comparable<DFSCode2> {
	protected int rootNodeCount; // rootNodeCount -1 = lastrootNode
	protected int totalNodeCount;
	protected int node0Label;

	protected GSpanEdgeFeatureSelection first;
	protected GSpanEdgeFeatureSelection rootLastEdge;
	protected GSpanEdgeFeatureSelection last;

	// Each of the nodes in the root can have a LastNodeEdge
	// Otherwise it is null;
	protected GSpanEdgeFeatureSelection lasts[];

	protected int[] lastNodes; // The right most node: of each root node brunch
	protected GSpanEdgeFeatureSelection[] parents; // Denote the parent of each
													// node
	// If the node[i] is a rootNode, then parents[i] is just the edge connecting
	// this node
	// If the node[i] is a newly grown node, then parents[i] is the edge growing
	// this node,
	// Stop searching,

	protected DataBaseModified dataBase;
	protected MutableGraph graph;
	protected Map childEdges = null; // Children Edges that can grow to
	protected DFSCode2 me = this;
	protected GraphSet myset; // The set of supporting graphs

	HashSet<String> allGeneratedPattern;;

	/**
	 * Create a new DFSCode representing the one-edge graph containing the given
	 * Edge
	 * 
	 * @param rootGraph
	 *            : Important, the rootGrap is only a linked list
	 *            representation, there is no guarantee that this rootGraph is
	 *            in its canonical form, only requirement in one edge, if nodeA
	 *            < nodeB, that means a forward edge
	 * 
	 * @param edge
	 * @param dataBase
	 *            the whole DataBase of this mining run
	 */
	public DFSCode2(int[][] rootGraph, DataBaseModified dataBase, GraphSet myset) {
		// 1. Added by Dayu: given the root Graph, construct the root graph:
		// Add the root graphs as the top K edges of thie DFSCode
		GSpanEdgeFeatureSelection[] root = new GSpanEdgeFeatureSelection[rootGraph.length];
		this.rootNodeCount = 0;
		this.node0Label = rootGraph[0][2];
		for (int i = 0; i < rootGraph.length; i++) {
			// nodeA, nodeB, nodeLabelA, edgeLabel, nodeLabelB
			int nodeA = rootGraph[i][0];
			int nodeB = rootGraph[i][1];
			int labelA = dataBase.getNodeLabel(rootGraph[i][2]);
			int edgeLabel = dataBase.getEdgeLabel(rootGraph[i][3]);
			int labelB = dataBase.getNodeLabel(rootGraph[i][4]);
			root[i] = new GSpanEdgeFeatureSelection(nodeA, nodeB, labelA,
					edgeLabel, labelB, -1);
			if (this.rootNodeCount < rootGraph[i][1])
				rootNodeCount = rootGraph[i][1];
		}
		rootNodeCount++;
		this.totalNodeCount = rootNodeCount;

		for (int i = 0; i < rootGraph.length - 1; i++)
			root[i].next = root[i + 1];
		for (int i = 1; i < rootGraph.length; i++)
			root[i].prev = root[i - 1];
		root[0].prev = root[rootGraph.length - 1].next = null;

		// 2. Old Ones:

		this.first = root[0];
		this.last = root[root.length - 1];
		this.lasts = new GSpanEdgeFeatureSelection[this.rootNodeCount];
		this.rootLastEdge = this.last;
		for (int i = 0; i < this.lasts.length; i++)
			lasts[i] = null;
		this.lastNodes = new int[this.rootNodeCount];
		for (int i = 0; i < lastNodes.length; i++)
			lastNodes[i] = i;
		this.dataBase = dataBase;
		parents = new GSpanEdgeFeatureSelection[dataBase.maxNodeCount];
		for (int i = 0; i < parents.length; i++)
			parents[i] = null;
		// root[0] = null;
		for (int i = 0; i < root.length; i++) {
			GSpanEdgeFeatureSelection oneEdge = root[i];
			if (oneEdge.nodeA < oneEdge.nodeB) // forward edge
				parents[oneEdge.nodeB] = oneEdge;
		}

		if (dataBase != null) {
			this.myset = myset;
			// Build the represented undirected Graph
			this.graph = dataBase.factory.createGraph();
			// this.graph.addNode(dataBase.getRealNodeLabel(edge.labelA));
			// this.graph.addNodeAndEdge(edge.nodeA,dataBase.getRealNodeLabel(edge.labelB),dataBase.getRealEdgeLabel(edge.edgeLabel));
			this.graph.addNode(rootGraph[0][2]);
			for (int i = 0; i < rootGraph.length; i++) {
				int nodeA = rootGraph[i][0];
				int nodeB = rootGraph[i][1];
				int edgeLabel = rootGraph[i][3];
				int labelB = rootGraph[i][4];
				if (nodeA < nodeB) // forward
					this.graph.addNodeAndEdge(nodeA, labelB, edgeLabel);
				else
					this.graph.addEdge(nodeA, nodeB, edgeLabel); // backward
			}
		}
		this.allGeneratedPattern = new HashSet<String>();
	}

	public boolean isMin() {
		String str = MyFactory.getDFSCoder().serialize(this.graph);
		if (this.allGeneratedPattern.contains(str))
			return false;
		else {
			this.allGeneratedPattern.add(str);
			return true;
		}
	}

	/**
	 * searches for children of the embedding in the database graph
	 * 
	 * @param dataBaseGraph
	 * @param ackNodes
	 *            maps embedding node to real node
	 * @param usedNodes
	 *            maps real node to embedding node (lastNode+1 if unused)
	 * @param usedEdges
	 *            tells if real edge is unused (Graph.NO_EDGE) or used (else)
	 * @param findTreesOnly
	 * @param findPathsOnly
	 */
	private void searchChildren(GSpanGraph dataBaseGraph, int[] ackNodes,
			int[] usedNodes, int[] usedEdges, boolean findTreesOnly,
			boolean findPathsOnly) {
		MutableGraph ggraph = dataBaseGraph.me;

		// For brunch starting from each node
		for (int w = 0; w < this.rootNodeCount; w++) {
			int lastNode = this.lastNodes[w];
			int lastNodeLabel = -1;
			if (lastNode == 0)
				lastNodeLabel = this.node0Label;
			else
				lastNodeLabel = this.parents[lastNode].labelB;
			// extensions at the last node: true last Nodes, or starting nodes
			// of each brunch
			int nodeG = ackNodes[lastNode];
			for (int i = 0; i < ggraph.getDegree(nodeG); i++) {
				int edge = ggraph.getNodeEdge(nodeG, i);
				int oNode = ggraph.getOtherNode(edge, nodeG);
				if (usedEdges[edge] == Graph.NO_EDGE) {
					GSpanEdgeFeatureSelection gEdge = null;
					if (usedNodes[oNode] == Graph.NO_NODE)
						gEdge = new GSpanEdgeFeatureSelection(lastNode,
								totalNodeCount, lastNodeLabel,
								ggraph.getEdgeLabel(edge),
								ggraph.getNodeLabel(oNode), w);

					else if (usedNodes[oNode] < lastNode)
						gEdge = new GSpanEdgeFeatureSelection(lastNode,
								usedNodes[oNode], lastNodeLabel,
								ggraph.getEdgeLabel(edge),
								ggraph.getNodeLabel(oNode), w);
					else
						continue;
					if ((parents[lastNode] == null
							|| parents[lastNode].nodeB < this.rootNodeCount || (lasts[w]
							.compareTo(gEdge) < 0))
							&& (!(findTreesOnly || findPathsOnly) || gEdge.nodeA < gEdge.nodeB) // trees
																								// and
																								// paths
																								// have
																								// no
																								// backward
																								// Edges
							&& (!findPathsOnly || graph.getDegree(lastNode) == 1)) // paths
																					// have
																					// max
																					// degree
																					// 2
					{
						GraphSet s = (GraphSet) childEdges.get(gEdge);
						if (s == null) {
							s = new GraphSet();
							childEdges.put(gEdge, s);
						}
						s.add(dataBaseGraph);
					}
				}
			}
			// extensions at the right most path: until meet brunch starting
			// point
			for (GSpanEdgeFeatureSelection ack = parents[lastNode]; ack != null
					&& ack.nodeB >= rootNodeCount; ack = parents[ack.nodeA]) {
				nodeG = ackNodes[ack.nodeA];
				for (int i = 0; i < ggraph.getDegree(nodeG); i++) {
					int edge = ggraph.getNodeEdge(nodeG, i);
					int oNode = ggraph.getOtherNode(edge, nodeG);
					if (usedEdges[edge] == Graph.NO_EDGE
							&& usedNodes[oNode] == Graph.NO_NODE // only forward
																	// edges are
																	// allowed
							&& (!findPathsOnly || graph.getDegree(ack.nodeA) == 1)) // paths
																					// have
																					// max
																					// degree
																					// 2
					{
						GSpanEdgeFeatureSelection gEdge = new GSpanEdgeFeatureSelection(
								ack.nodeA, totalNodeCount, ack.labelA,
								ggraph.getEdgeLabel(edge),
								ggraph.getNodeLabel(oNode), w);
						GraphSet s = (GraphSet) childEdges.get(gEdge);
						if (s == null) {
							s = new GraphSet();
							childEdges.put(gEdge, s);
						}
						s.add(dataBaseGraph);
					}
				}

			}

			// add edge extension inside root graph: for each node W
			int nodewG = ackNodes[w];
			int nodeLabelW = -1;
			if (w == 0)
				nodeLabelW = this.node0Label;
			else
				nodeLabelW = this.parents[lastNode].labelB;
			for (int i = 0; i < ggraph.getDegree(nodewG); i++) {
				int edge = ggraph.getNodeEdge(nodewG, i);
				int oNode = ggraph.getOtherNode(edge, nodewG);
				if (usedEdges[edge] == Graph.NO_EDGE && usedNodes[oNode] < w
						&& usedNodes[oNode] != Graph.NO_NODE) {
					GSpanEdgeFeatureSelection gEdge = new GSpanEdgeFeatureSelection(
							w, usedNodes[oNode], nodeLabelW,
							ggraph.getEdgeLabel(edge),
							ggraph.getNodeLabel(oNode), -1);
					GraphSet s = (GraphSet) childEdges.get(gEdge);
					if (s == null) {
						s = new GraphSet();
						childEdges.put(gEdge, s);
					}
					s.add(dataBaseGraph);
				}
			}
		}

	}

	/**
	 * recursive search for embeddings in the database graph
	 * 
	 * @param currentEdge
	 *            the edge to extend the current embedding
	 * @param dataBaseGraph
	 * @param ackNodes
	 *            maps embedding node to real node
	 * @param usedNodes
	 *            maps real node to embedding node (lastNode+1 if unused)
	 * @param usedEdges
	 *            tells if real edge is unused (Graph.NO_EDGE) or used (else)
	 * @param findTreesOnly
	 * @param findPathsOnly
	 */
	private void searchEmbedding(GSpanEdgeFeatureSelection currentEdge,
			GSpanGraph dataBaseGraph, int[] ackNodes, int[] usedNodes,
			int[] usedEdges, boolean findTreesOnly, boolean findPathsOnly) {
		MutableGraph ggraph = dataBaseGraph.me;
		if (currentEdge == null) { // no more edges to add, so embedding is
									// found
			searchChildren(dataBaseGraph, ackNodes, usedNodes, usedEdges,
					findTreesOnly, findPathsOnly);
		} else if (currentEdge.nodeA < currentEdge.nodeB) { // add forward Edge
			int node = ackNodes[currentEdge.nodeA];
			for (int i = 0; i < ggraph.getDegree(node); i++) { // try all edges
																// of the
																// corresponding
																// node
				int edge = ggraph.getNodeEdge(node, i);
				if (usedEdges[edge] == -1
						&& ggraph.getEdgeLabel(edge) == currentEdge.edgeLabel) {
					// only unused, right labeled edges
					int oNode = ggraph.getOtherNode(edge, node);
					if (ggraph.getNodeLabel(oNode) == currentEdge.labelB
							&& usedNodes[oNode] == Graph.NO_NODE) {
						// only unused, right labeled nodes
						int old = usedNodes[oNode]; // old = lastNode + 1
						ackNodes[currentEdge.nodeB] = oNode;
						usedNodes[oNode] = currentEdge.nodeB;
						usedEdges[edge] = edge;
						searchEmbedding(currentEdge.next, dataBaseGraph,
								ackNodes, usedNodes, usedEdges, findTreesOnly,
								findPathsOnly);
						// recovery:
						usedNodes[oNode] = old;
						usedEdges[edge] = Graph.NO_EDGE;
					}
				}
			}
		} else { // backward Edge
			int edge = ggraph.getEdge(ackNodes[currentEdge.nodeA],
					ackNodes[currentEdge.nodeB]);
			// check if corresponding edge exists and is right labeled
			if (edge != -1
					&& ggraph.getEdgeLabel(edge) == currentEdge.edgeLabel) {
				usedEdges[edge] = edge;
				searchEmbedding(currentEdge.next, dataBaseGraph, ackNodes,
						usedNodes, usedEdges, findTreesOnly, findPathsOnly);
				// recovery
				usedEdges[edge] = Graph.NO_EDGE;
			}
		}
	}

	/**
	 * @param findTreesOnly
	 * @param findPathsOnly
	 * @return an itererator over all possible child
	 */
	public Iterator childIterator(boolean findTreesOnly, boolean findPathsOnly) { // =enumerate
		if (childEdges == null) {
			// 1. Find Embedding of the current code over all myset graphs
			// 2. Find expandable edges of the current code
			childEdges = new TreeMap();
			int[] ackNodes = new int[graph.getNodeCount() + rootNodeCount];
			for (int i = 0; i < ackNodes.length; i++)
				ackNodes[i] = Graph.NO_NODE;
			for (Iterator ggit = myset.iterator(); ggit.hasNext();) { // search
																		// all
																		// database
																		// graphs
																		// for
																		// children
				GSpanGraph dataBaseGraph = (GSpanGraph) ggit.next();
				MutableGraph ackGraph = dataBaseGraph.me;
				Debug.println(3, "search for childs in " + ackGraph.getID());
				// initialize embedding arrays
				int[] usedNodes = new int[ackGraph.getNodeCount()];
				for (int i = 0; i < ackGraph.getNodeCount(); i++)
					usedNodes[i] = Graph.NO_NODE;
				int[] usedEdges = new int[ackGraph.getEdgeCount()];
				for (int i = 0; i < ackGraph.getEdgeCount(); i++)
					usedEdges[i] = Graph.NO_EDGE;

				for (int i = 0; i < ackGraph.getNodeCount(); i++) { // try all
																	// nodes as
																	// embedding
																	// start
																	// node
					int node = ackGraph.getNode(i);
					if (ackGraph.getNodeLabel(node) == first.labelA) {
						ackNodes[0] = node;
						usedNodes[node] = 0;
						searchEmbedding(first, dataBaseGraph, ackNodes,
								usedNodes, usedEdges, findTreesOnly,
								findPathsOnly);
						// Recover:
						usedNodes[node] = Graph.NO_NODE;
					}
				}
			}

		}
		return new Iterator() { // a tricky iterator, creates no new DFSCodes,
								// but change and restore this DFSCode
			private Map cE = me.childEdges;
			private Iterator it = cE.keySet().iterator();
			private GSpanEdgeFeatureSelection lastEdge = null;
			private GraphSet lastSet = null;

			@Override
			public boolean hasNext() {
				// return false;/*
				if (lastEdge != null) {
					me.remove(lastEdge, lastSet);
					lastEdge = null;
					lastSet = null;
				}
				return it.hasNext();// */
			}

			@Override
			public Object next() {
				if (hasNext()) {
					lastEdge = (GSpanEdgeFeatureSelection) it.next();
					lastSet = me.add(lastEdge, (GraphSet) cE.get(lastEdge));
					return me;
				} else
					throw new NoSuchElementException("No more elements");
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * expands the currents DFSCode with the given edge
	 * 
	 * @param edge
	 * @param dbSubSet
	 *            the database subset, containing the new DFSCode
	 * @return the database subset, containing the old DFSCode
	 */
	private GraphSet add(GSpanEdgeFeatureSelection edge, GraphSet dbSubSet) {
		GraphSet old = myset;
		GSpanEdgeFeatureSelection bLastEdge = null;
		if (edge.attachedNode == -1)
			bLastEdge = this.rootLastEdge;
		else
			bLastEdge = this.lasts[edge.attachedNode];
		// The first time that one edge is attached to the brunch: add this edge
		// without hesitation
		// Or this is an edge extension within the root graph
		if (bLastEdge == null || edge.attachedNode == -1) {
			myset = dbSubSet;
			if (edge.attachedNode == -1) {
				edge.bPrev = rootLastEdge;
				this.rootLastEdge = edge;
			} else {
				edge.bPrev = this.lasts[edge.attachedNode];
				this.lasts[edge.attachedNode] = edge;
			}

			childEdges = null;
			if (dataBase != null) {
				// Build the represented undirected Graph
				if (edge.nodeA < edge.nodeB) {
					graph.addNodeAndEdge(edge.nodeA,
							dataBase.getRealNodeLabel(edge.labelB),
							dataBase.getRealEdgeLabel(edge.edgeLabel));
					// Debug.println(2,"Graph "+de.parmol.parsers.SimpleUndirectedGraphParser.instance.serialize(graph));
					if (edge.attachedNode != -1)
						this.lastNodes[edge.attachedNode] = edge.nodeB;
					else {
						System.out.println("Exception in add");
					}
					parents[edge.nodeB] = edge;
					this.totalNodeCount++;
				} else {
					graph.addEdge(edge.nodeA, edge.nodeB,
							dataBase.getRealEdgeLabel(edge.edgeLabel));
					// --
				}
				edge.prev = this.last;
				edge.next = null;
				this.last.next = edge;
				this.last = edge;
			}
		} else if (bLastEdge.compareTo(edge) < 0) {
			myset = dbSubSet;
			edge.bPrev = bLastEdge;
			edge.bNext = null;
			bLastEdge.bNext = edge;
			bLastEdge = edge;
			if (edge.attachedNode == -1) {
				this.rootLastEdge = bLastEdge;
			} else {
				this.lasts[edge.attachedNode] = bLastEdge;
			}

			childEdges = null;
			if (dataBase != null) {
				// Build the represented undirected Graph
				if (edge.nodeA < edge.nodeB) {
					graph.addNodeAndEdge(edge.nodeA,
							dataBase.getRealNodeLabel(edge.labelB),
							dataBase.getRealEdgeLabel(edge.edgeLabel));
					// Debug.println(2,"Graph "+de.parmol.parsers.SimpleUndirectedGraphParser.instance.serialize(graph));
					this.lastNodes[edge.attachedNode] = edge.nodeB;
					parents[edge.nodeB] = edge;
					this.totalNodeCount++;
				} else {
					graph.addEdge(edge.nodeA, edge.nodeB,
							dataBase.getRealEdgeLabel(edge.edgeLabel));

					// --
				}
				edge.prev = this.last;
				edge.next = null;
				this.last.next = edge;
				this.last = edge;
			}
		} else { // should not happend, if correctly use of childIterator()
			throw new UnsupportedOperationException("no valid extension");
			// return null;
		}
		return old;
	}

	/**
	 * rebuild an old DFSCode by removing the given edge
	 * 
	 * @param edge
	 * @param set
	 *            the database subset, containing the rebuild DFSCode
	 * @return the database subset, containing the current DFSCode
	 */
	private GraphSet remove(GSpanEdgeFeatureSelection edge, GraphSet set) {

		GSpanEdgeFeatureSelection bLastEdge = null;
		int attachedNode = edge.attachedNode;
		if (attachedNode == -1)
			bLastEdge = this.rootLastEdge;
		else
			bLastEdge = this.lasts[attachedNode];
		if (bLastEdge.compareTo(edge) != 0) // should not happend, if correctly
											// use of childIterator()
			throw new UnsupportedOperationException(
					"only last edge is removeable");

		GraphSet old = myset;
		myset = set;
		// Update brunch sequence
		if (bLastEdge.bPrev != null)
			bLastEdge.bPrev.bNext = null;

		if (attachedNode == -1)
			this.rootLastEdge = bLastEdge.bPrev;
		else
			this.lasts[attachedNode] = bLastEdge.bPrev;

		// update whole sequence
		bLastEdge.prev.next = bLastEdge.next;
		if (bLastEdge.compareTo(this.last) == 0)
			this.last = this.last.prev;
		else
			bLastEdge.next.prev = bLastEdge.prev;

		childEdges = null;
		if (dataBase != null) {
			// Build the represented undirected Graph
			if (edge.nodeA < edge.nodeB) {
				graph.removeNode(edge.nodeB);
				// Find the last Nodes, little bit trick
				GSpanEdgeFeatureSelection bpEdge = bLastEdge.bPrev;
				while (bpEdge != null) {
					if (bpEdge.nodeA < bpEdge.nodeB)
						break;
					bpEdge = bpEdge.bPrev;
				}
				if (bpEdge == null)
					this.lastNodes[attachedNode] = attachedNode;
				else
					this.lastNodes[attachedNode] = bpEdge.nodeB;

				this.totalNodeCount--;
				parents[edge.nodeB] = null;
			} else {
				graph.removeEdge(graph.getEdge(edge.nodeA, edge.nodeB));
			}
			// --
		}
		return old;
	}

	/**
	 * @param minFreq
	 * @return true, if current support not smaller then minFreq
	 */
	public final boolean isFrequent(float[] minFreq) {
		float[] freq = myset.getFreq();
		for (int i = 0; i < GraphSet.length; i++)
			if (freq[i] < minFreq[i])
				return false;
		return true;
	}

	/**
	 * @param minFreq
	 * @param maxFreq
	 * @return true, if current support not smaller then minFreq an not greater
	 *         than maxFreq
	 */
	public final boolean isFrequent(float[] minFreq, float[] maxFreq) {
		float[] freq = myset.getFreq();
		for (int i = 0; i < GraphSet.length; i++)
			if ((freq[i] < minFreq[i]) || (freq[i] > maxFreq[i]))
				return false;
		return true;
	}

	/**
	 * @return a FrequentFragment, represented by this DFSCode
	 */
	public FrequentFragment toFragment() {
		Set realSet = new HashSet();
		for (Iterator ggit = myset.iterator(); ggit.hasNext();) {
			GSpanGraph gg = (GSpanGraph) ggit.next();
			realSet.add(gg.getRealGraph());
		}
		return new FrequentFragment((Graph) graph.clone(), realSet,
				myset.getFreq());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DFSCode2 o) {
		DFSCode2 other = o;
		GSpanEdgeFeatureSelection ack1 = this.first;
		GSpanEdgeFeatureSelection ack2 = other.first;
		while (ack1 != null && ack2 != null && ack1.compareTo(ack2) == 0) {
			ack1 = ack1.next;
			ack2 = ack2.next;
		}
		if (ack1 == null)
			if (ack2 == null)
				return 0;
			else
				return -1;
		else if (ack2 == null)
			return 1;
		else
			return ack1.compareTo(ack2);
	}

	/** @return the frequency of this DFS-Code */
	public final float[] getFrequencies() {
		return myset.getFreq();
	}

	/**
	 * @param serializer
	 * @return the graphrepesentation of this DFS-Code corresponding to the
	 *         given parser
	 */
	public final String toString(GraphParser serializer) {
		return serializer.serialize(graph);
	}

	/** @return the graph representing this DFS-Code */
	public Graph getSubgraph() {
		return this.graph;
	}
}
