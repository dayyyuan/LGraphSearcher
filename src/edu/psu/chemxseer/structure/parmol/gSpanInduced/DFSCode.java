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
package edu.psu.chemxseer.structure.parmol.gSpanInduced;

import java.util.*;

import de.parmol.graph.*;
import de.parmol.parsers.*;
import de.parmol.util.*;

/**
 * This class representates a DFSCode (list of GSpanEdges) for an created
 * SubGraph
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 * 
 *         Dayu: Change all data member into protected
 */
public class DFSCode implements Comparable {
	protected GSpanEdge first, last;
	protected int lastNode;
	protected DataBaseModified dataBase;
	protected MutableGraph graph;
	protected GSpanEdge[] parents;
	protected Map childEdges = null;
	protected DFSCode me = this;
	protected GraphSet myset;

	// added by Dayu Yuan
	protected GraphSet coverSet = null;

	/**
	 * creats a new DFSCode representating the one-edge graph containing the
	 * given Edge
	 * 
	 * @param edge
	 * @param dataBase
	 *            the whole DataBase of this mining run
	 */
	public DFSCode(GSpanEdge edge, DataBaseModified dataBase) {
		this.first = this.last = edge;
		this.lastNode = edge.nodeB;
		this.dataBase = dataBase;
		parents = new GSpanEdge[dataBase.maxNodeCount];
		parents[edge.nodeA] = null;
		parents[edge.nodeB] = edge;
		if (dataBase != null) {
			this.myset = dataBase.getContainingGraphs(edge);

			// Build the represented undirected Graph
			this.graph = dataBase.factory.createGraph();
			this.graph.addNode(dataBase.getRealNodeLabel(edge.labelA));
			this.graph.addNodeAndEdge(edge.nodeA,
					dataBase.getRealNodeLabel(edge.labelB),
					dataBase.getRealEdgeLabel(edge.edgeLabel));
			// --
		}
	}

	/**
	 * Recusive search for an minimum DFSCode
	 * 
	 * @param currentBuiltEdge
	 *            the smallest edge for extending current Embedding
	 * @param embeddingNodeCount
	 *            the node count of the current embedding
	 * @param ackNodes
	 *            maps embedding node to real node
	 * @param usedNodes
	 *            maps real node to embedding node (Graph.NO_NODE if unused)
	 * @param usedEdges
	 *            tells if real edge is unused (Graph.NO_EDGE) or used (else)
	 * @param edgeSet
	 *            set of extenstions must add to current embedding
	 * @param localEdge
	 *            the corresponding edge in the main DFSCode checked for minimum
	 * @return false, if a smaller DFSCode is found
	 */
	private boolean findSmallerDFSCode(GSpanEdge currentBuiltEdge,
			int embeddingNodeCount, int[] ackNodes, int[] usedNodes,
			int[] usedEdges, ExtensionSet edgeSet, GSpanEdge localEdge) {
		// boolean doNext=false;
		if (localEdge == null || currentBuiltEdge.compareTo(localEdge) > 0) {
			return true;
		}
		if (currentBuiltEdge.compareTo(localEdge) < 0) {
			return false;
		}
		ArrayList head = (ArrayList) edgeSet.headList().clone();
		for (int j = 0; j < head.size(); j++) {
			// doNext=false;
			Extension e = (Extension) head.get(j);
			edgeSet.remove(e);
			int edge = e.getEdge();
			int node = ackNodes[e.getFromNode()];

			if (usedEdges[edge] == Graph.NO_EDGE) {
				if (e.isNodeExtension()) {
					int nr = embeddingNodeCount + 1;
					int nodeB = graph.getOtherNode(edge, node);
					usedEdges[edge] = 1;
					usedNodes[nodeB] = nr;
					ackNodes[nr] = nodeB;

					// get all extensions for the new Node
					ArrayList subEdges = new ArrayList();
					for (int k = 0; k < graph.getDegree(nodeB); k++) {
						int edgeB = graph.getNodeEdge(nodeB, k);
						if (usedEdges[edgeB] == Graph.NO_EDGE) {
							int nodeC = graph.getOtherNode(edgeB, nodeB);
							if (usedNodes[nodeC] == Graph.NO_NODE) {
								subEdges.add(Extension.newNodeExtension(
										dataBase, graph, nodeB, edgeB, nr));
							} else {
								subEdges.add(Extension.newEdgeExtension(
										dataBase, graph, nodeB, edgeB, nr,
										usedNodes[nodeC]));
							}
						}
					}
					edgeSet.addAll(subEdges);

					if (!findSmallerDFSCode(e.getGSpanEdge(nr), nr, ackNodes,
							usedNodes, usedEdges, edgeSet, localEdge.next))
						return false;

					edgeSet.removeAll(subEdges);
					usedEdges[edge] = Graph.NO_EDGE;
					usedNodes[nodeB] = Graph.NO_NODE;
				} else {// EdgeExtension
					usedEdges[edge] = 1;
					if (!findSmallerDFSCode(e.getGSpanEdge(Graph.NO_NODE),
							embeddingNodeCount, ackNodes, usedNodes, usedEdges,
							edgeSet, localEdge.next))
						return false;
					usedEdges[edge] = Graph.NO_EDGE;
				}
			} else {
				if (!findSmallerDFSCode(currentBuiltEdge, embeddingNodeCount,
						ackNodes, usedNodes, usedEdges, edgeSet, localEdge))
					return false;
			}
			edgeSet.add(e);
		}
		return true;
	}

	/**
	 * checks if this DFSCode is minimum DFSCode for the represented Graph
	 * 
	 * @return false, if a smaller DFSCode exists
	 */
	public boolean isMin() {
		// create and initilize embedding Arrays
		int[] ackNodes = new int[graph.getNodeCount()];
		int[] usedNodes = new int[graph.getNodeCount()];
		int[] usedEdges = new int[graph.getEdgeCount()];
		for (int i = 0; i < graph.getNodeCount(); i++) {
			ackNodes[i] = Graph.NO_NODE;
			usedNodes[i] = Graph.NO_NODE;
		}
		for (int i = 0; i < graph.getEdgeCount(); i++) {
			usedEdges[i] = Graph.NO_EDGE;
		}
		for (int i = 0; i < graph.getNodeCount(); i++) {
			// check DFSCodes beginning with node i
			int node = graph.getNode(i);
			int label = dataBase.getNodeLabel(graph.getNodeLabel(node));
			if (label < first.labelA)
				return false; // a smaller DFSCode is found
			if (label == first.labelA) { // only if this DFSCode wil searched
											// with starts same as this DFSCode
				ExtensionSet edgeSet = new ExtensionSet();
				usedNodes[node] = 0;
				ackNodes[0] = node;
				for (int j = 0; j < graph.getDegree(node); j++) { // add all
																	// extensions
																	// for the
																	// first
																	// Node
					edgeSet.add(Extension.newNodeExtension(dataBase, graph,
							node, graph.getNodeEdge(node, j), 0));
				}
				ArrayList head = (ArrayList) edgeSet.headList().clone();
				for (int j = 0; j < head.size(); j++) { // try all smallest
														// starting edges
					Extension e = (Extension) head.get(j);
					edgeSet.remove(e);
					int edge = e.getEdge();
					int nodeB = graph.getOtherNode(edge, node);
					usedEdges[edge] = 1;
					usedNodes[nodeB] = 1;
					ackNodes[1] = nodeB;

					// add all extensions for the second Node
					ArrayList subEdges = new ArrayList();
					for (int k = 0; k < graph.getDegree(nodeB); k++) {
						int edgeB = graph.getNodeEdge(nodeB, k);
						if (usedEdges[edgeB] == Graph.NO_EDGE) {
							subEdges.add(Extension.newNodeExtension(dataBase,
									graph, nodeB, edgeB, 1));
						}
					}
					edgeSet.addAll(subEdges);

					// recusive find
					if (!findSmallerDFSCode(e.getGSpanEdge(1), 1, ackNodes,
							usedNodes, usedEdges, edgeSet, first))
						return false;

					// clean edgeSet and embedding Arrays for next run
					edgeSet.removeAll(subEdges);
					usedEdges[edge] = Graph.NO_EDGE;
					usedNodes[nodeB] = Graph.NO_NODE;
					edgeSet.add(e);
				}
				usedNodes[node] = Graph.NO_NODE;
			}
		}
		// no smaller is found
		return true;
	}

	// /**
	// * searches for children of the embedding in the database graph
	// * @param dataBaseGraph
	// * @param ackNodes maps embedding node to real node
	// * @param usedNodes maps real node to embedding node (lastNode+1 if
	// unused)
	// * @param usedEdges tells if real edge is unused (Graph.NO_EDGE) or used
	// (else)
	// * @param findTreesOnly
	// * @param findPathsOnly
	// */
	// private void searchChildren(GSpanGraph dataBaseGraph,
	// int[] ackNodes, int[] usedNodes, int[] usedEdges,
	// boolean findTreesOnly, boolean findPathsOnly){
	// MutableGraph ggraph=dataBaseGraph.me;
	// //extensions at the last node
	// int node=ackNodes[lastNode];
	// for (int i=0;i<ggraph.getDegree(node);i++){
	// int edge=ggraph.getNodeEdge(node,i);
	// int oNode=ggraph.getOtherNode(edge,node);
	// if (usedEdges[edge]==Graph.NO_EDGE){
	// GSpanEdge gEdge=new
	// GSpanEdge(lastNode,usedNodes[oNode],parents[lastNode].labelB,
	// ggraph.getEdgeLabel(edge),ggraph.getNodeLabel(oNode));
	// if ((last.compareTo(gEdge)<0)
	// && (!(findTreesOnly || findPathsOnly) || gEdge.nodeA<gEdge.nodeB) //
	// trees and paths have no backward Edges
	// && (!findPathsOnly || graph.getDegree(lastNode)==1)) // paths have max
	// degree 2
	// {
	// GraphSet s=(GraphSet)childEdges.get(gEdge);
	// if (s==null){
	// s=new GraphSet();
	// childEdges.put(gEdge,s);
	// }
	// s.add(dataBaseGraph);
	// }
	// }
	// }
	// //extensions at the rigth most path
	// for (GSpanEdge ack=parents[lastNode];ack!=null;ack=parents[ack.nodeA]){
	// node=ackNodes[ack.nodeA];
	// for (int i=0;i<ggraph.getDegree(node);i++){
	// int edge=ggraph.getNodeEdge(node,i);
	// int oNode=ggraph.getOtherNode(edge,node);
	// if (usedEdges[edge]==Graph.NO_EDGE
	// && lastNode<usedNodes[oNode] // only forward eges are allowed
	// && (!findPathsOnly || graph.getDegree(ack.nodeA)==1)) // paths have max
	// degree 2
	// {
	// GSpanEdge gEdge=new
	// GSpanEdge(ack.nodeA,usedNodes[oNode],ack.labelA,ggraph.getEdgeLabel(edge),ggraph.getNodeLabel(oNode));
	// GraphSet s=(GraphSet)childEdges.get(gEdge);
	// if (s==null){
	// s=new GraphSet();
	// childEdges.put(gEdge,s);
	// }
	// s.add(dataBaseGraph);
	// }
	// }
	//
	// }
	// }
	/**
	 * 
	 * An extension version of the searchChildren in DFSCode The major change is
	 * the involvement of the "coverEdges" Besides adding the support for each
	 * "code", we also find the cover of each "code". By "cover", we mean graphs
	 * has the code as an edge induced subgraph searches for children of the
	 * embedding in the database graph
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
		// extensions at the last node
		int node = ackNodes[lastNode];
		for (int i = 0; i < ggraph.getDegree(node); i++) {
			int edge = ggraph.getNodeEdge(node, i);
			int oNode = ggraph.getOtherNode(edge, node);
			if (usedEdges[edge] == Graph.NO_EDGE) {
				GSpanEdge gEdge = new GSpanEdge(lastNode, usedNodes[oNode],
						parents[lastNode].labelB, ggraph.getEdgeLabel(edge),
						ggraph.getNodeLabel(oNode));
				if ((last.compareTo(gEdge) < 0)
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

		// extensions at the right most path
		for (GSpanEdge ack = parents[lastNode]; ack != null; ack = parents[ack.nodeA]) {
			node = ackNodes[ack.nodeA];
			for (int i = 0; i < ggraph.getDegree(node); i++) {
				int edge = ggraph.getNodeEdge(node, i);
				int oNode = ggraph.getOtherNode(edge, node);
				if (usedEdges[edge] == Graph.NO_EDGE
						&& lastNode < usedNodes[oNode] // only forward eges are
														// allowed
						&& (!findPathsOnly || graph.getDegree(ack.nodeA) == 1)) // paths
																				// have
																				// max
																				// degree
																				// 2
				{
					GSpanEdge gEdge = new GSpanEdge(ack.nodeA,
							usedNodes[oNode], ack.labelA,
							ggraph.getEdgeLabel(edge),
							ggraph.getNodeLabel(oNode));
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

	private void searchCoverSet(GSpanGraph databaseGraph, int[] ackNodes,
			int[] usedNodes, int[] usedEdges) {
		if (this.coverSet == null)
			coverSet = new GraphSet();
		if (isEdgeReduced(databaseGraph, ackNodes, usedNodes, usedEdges))
			coverSet.add(databaseGraph.getRealGraph());
	}

	/**
	 * Test wehter current code is an edge reduced subgraph of the database
	 * Graph
	 * 
	 * @param dataBaseGraph
	 * @param ackNodes
	 *            maps embedding node to real node: ackNodes[codeNodeID] =
	 *            graphNodeID
	 * @param usedNodes
	 *            maps real node to embedding node (lastNode+1 if unused)
	 *            usedNodes[graphNodeID] = codeNodeID
	 * @param usedEdges
	 *            tells if real edge is unused (Graph.NO_EDGE) or used (else),
	 *            used or unused
	 * @return
	 */
	private boolean isEdgeReduced(GSpanGraph databaseGraph, int[] ackNodes,
			int[] usedNodes, int[] usedEdges) {
		Graph dbGraph = databaseGraph.getRealGraph();
		for (int i = 0; i < usedNodes.length; i++) {
			if (usedNodes[i] > lastNode)
				continue; // unvisited nodes do not count
			// node i in the database graph
			int degree = dbGraph.getDegree(i);
			for (int j = 0; j < degree; j++) {
				int adjEdge = dbGraph.getNodeEdge(i, j);
				int adjNode = dbGraph.getNodeB(adjEdge);
				if (adjNode == i)
					adjNode = dbGraph.getNodeA(adjEdge);
				if (usedNodes[i] > lastNode)
					continue; // unvisited edges do not count

				// both node i and adjNode is mapped to the code nodes
				// the the adjEdge is not
				// then the code is not an edge induced subgraph of the database
				// graph
				else if (usedEdges[adjEdge] == Graph.NO_EDGE)
					return false;
			}
		}
		return true;
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
	private void searchEmbedding(GSpanEdge currentEdge,
			GSpanGraph dataBaseGraph, int[] ackNodes, int[] usedNodes,
			int[] usedEdges, boolean findTreesOnly, boolean findPathsOnly) {
		MutableGraph ggraph = dataBaseGraph.me;
		if (currentEdge == null) { // no more edges to add, so embedding is
									// found
			// Added by Dayu: add cover set
			searchCoverSet(dataBaseGraph, ackNodes, usedNodes, usedEdges);
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
							&& usedNodes[oNode] == lastNode + 1) {
						// only unused, right labeled nodes
						int old = usedNodes[oNode];
						ackNodes[currentEdge.nodeB] = oNode;
						usedNodes[oNode] = currentEdge.nodeB;
						usedEdges[edge] = edge;
						searchEmbedding(currentEdge.next, dataBaseGraph,
								ackNodes, usedNodes, usedEdges, findTreesOnly,
								findPathsOnly);
						usedNodes[oNode] = old;
						usedEdges[edge] = Graph.NO_EDGE;
					}
				}
			}
		} else { // backward Edge
			int edge = ggraph.getEdge(ackNodes[currentEdge.nodeA],
					ackNodes[currentEdge.nodeB]);
			// check if corresponding edge exists and is rigth labeled
			if (edge != -1
					&& ggraph.getEdgeLabel(edge) == currentEdge.edgeLabel) {
				usedEdges[edge] = edge;
				searchEmbedding(currentEdge.next, dataBaseGraph, ackNodes,
						usedNodes, usedEdges, findTreesOnly, findPathsOnly);
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
			childEdges = new TreeMap();
			int[] ackNodes = new int[graph.getNodeCount()];

			for (Iterator ggit = myset.iterator(); ggit.hasNext();) { // search
																		// all
																		// database
																		// graphs
																		// for
																		// children
				GSpanGraph dataBaseGraph = (GSpanGraph) ggit.next();
				MutableGraph ackGraph = dataBaseGraph.me;
				Debug.println(3, "search for childs in " + ackGraph.getID());
				// initialise embedding arrays
				int[] usedNodes = new int[ackGraph.getNodeCount()];
				for (int i = 0; i < ackGraph.getNodeCount(); i++)
					usedNodes[i] = lastNode + 1;
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
						usedNodes[node] = lastNode + 1;
					}
				}
			}
		}

		return new Iterator() { // a tricky iterator, creates no new DFSCodes,
								// but change und restore this DFSCode
			private Map cE = me.childEdges;
			private Iterator it = cE.keySet().iterator();
			private GSpanEdge lastEdge = null;
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
					lastEdge = (GSpanEdge) it.next();
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
	private GraphSet add(GSpanEdge edge, GraphSet dbSubSet) {
		if (last.compareTo(edge) < 0) {
			GraphSet old = myset;
			myset = dbSubSet;
			edge.prev = last;
			last = last.next = edge;
			childEdges = null;
			if (dataBase != null) {
				// Build the represented undirected Graph
				if (edge.nodeA < edge.nodeB) {
					graph.addNodeAndEdge(edge.nodeA,
							dataBase.getRealNodeLabel(edge.labelB),
							dataBase.getRealEdgeLabel(edge.edgeLabel));
					// Debug.println(2,"Graph "+de.parmol.parsers.SimpleUndirectedGraphParser.instance.serialize(graph));
					lastNode = edge.nodeB;
					parents[edge.nodeB] = edge;
				} else {
					graph.addEdge(edge.nodeA, edge.nodeB,
							dataBase.getRealEdgeLabel(edge.edgeLabel));
					// --
				}
			}
			return old;
		} else { // should not happend, if correctly use of childIterator()
			throw new UnsupportedOperationException("no valid extension");
		}
	}

	/**
	 * rebuild an old DFSCode by removing the given edge
	 * 
	 * @param edge
	 * @param set
	 *            the database subset, containing the rebuild DFSCode
	 * @return the database subset, containing the current DFSCode
	 */
	private GraphSet remove(GSpanEdge edge, GraphSet set) {
		if (last.compareTo(edge) != 0) // should not happend, if correctly use
										// of childIterator()
			throw new UnsupportedOperationException(
					"only last edge is removeable");
		GraphSet old = myset;
		myset = set;
		last = last.prev;
		last.next.prev = last.next.next = null;
		last.next = null;
		childEdges = null;
		if (dataBase != null) {
			// Build the represented undirected Graph
			if (edge.nodeA < edge.nodeB) {
				graph.removeNode(edge.nodeB);
				lastNode--;
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
		// Remove Redundancy
		for (Iterator ggit = myset.iterator(); ggit.hasNext();) {
			GSpanGraph gg = (GSpanGraph) ggit.next();
			realSet.add(gg.getRealGraph());
		}

		Set realSet2 = new HashSet();
		for (Iterator ggit = coverSet.iterator(); ggit.hasNext();) {
			GSpanGraph gg = (GSpanGraph) ggit.next();
			realSet.add(gg.getRealGraph());
		}

		return new FrequentFragment((Graph) graph.clone(), realSet, realSet2,
				myset.getFreq());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		DFSCode other = (DFSCode) o;
		GSpanEdge ack1 = this.first;
		GSpanEdge ack2 = other.first;
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
