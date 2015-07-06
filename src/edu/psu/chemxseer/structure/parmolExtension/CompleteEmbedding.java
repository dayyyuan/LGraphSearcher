package edu.psu.chemxseer.structure.parmolExtension;

/*
 * Created on May 17, 2004
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.parmol.graph.DirectedGraph;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphEmbedding;
import de.parmol.graph.NodeEmbedding;
import de.parmol.graph.SimpleEdgeComparator;
import de.parmol.graph.SimpleGraphComparator;
import de.parmol.graph.SimpleNodeComparator;
import de.parmol.graph.UndirectedGraph;

/**
 * This class stores a complete embedding of the subgraph into its supergraph,
 * i.e. it contains mappings between nodes and edges. It also implements
 * UndirectedGraph so that it represents a subgraph by itself.
 * 
 * @author Thorsten Meinl <Thorsten.Meinl@informatik.uni-erlangen.de>
 * 
 */
public class CompleteEmbedding implements GraphEmbedding, Graph, DirectedGraph,
		UndirectedGraph {
	// protected final int[] m_nodeMap, m_edgeMap;
	public final int[] m_nodeMap, m_edgeMap;
	protected int m_nodeCount, m_edgeCount;
	protected Graph m_superGraph;

	/**
	 * Create a new CompleteEmbedding given a supergraph and a subgraph and
	 * their corresponding mapping It is important to notice that this mapping
	 * do not ncessarily of being complete
	 * 
	 * @param supergraph
	 * @param subgraph
	 * @param nodeMapping
	 */
	public CompleteEmbedding(Graph supergraph, Graph subgraph, int[] nodeMapping) {
		m_superGraph = supergraph;
		m_nodeMap = nodeMapping.clone();
		m_nodeCount = subgraph.getNodeCount();
		m_edgeCount = subgraph.getEdgeCount();
		m_edgeMap = new int[m_edgeCount];
		for (int i = 0; i < m_edgeCount; i++) {
			int supergraphEdge = supergraph.getEdge(subgraph.getNodeA(i),
					subgraph.getNodeB(i));
			if (supergraphEdge != -1)
				m_edgeMap[i] = supergraphEdge;
		}
	}

	/**
	 * Creates a new CompleteEmbedding consisting of a single node.
	 * 
	 * @param supergraph
	 *            the supergraph in which the embedding occurs
	 * @param node
	 *            the node of the <b>super </b>graph that is part of the
	 *            embedding
	 */
	public CompleteEmbedding(Graph supergraph, int node) {
		m_superGraph = supergraph;
		m_nodeMap = new int[] { node, -1, -1, -1 };
		m_edgeMap = new int[] { -1, -1, -1, -1 };
		m_nodeCount = 1;
		m_edgeCount = 0;
	}

	/**
	 * Creates a new CompleteEmbedding that is an extension of the given
	 * embedding by a new edge and/or a new node. Either the edge or the node
	 * can be NO_EDGE or NO_NODE if only a new node or a new edge should be
	 * added.
	 * 
	 * @param embedding
	 *            the embeddings that should be extended
	 * @param newEdge
	 *            the new edge in the <b>supergraph </b> or NO_EDGE if no edge
	 *            should be added
	 * @param newNode
	 *            the new node in the <b>supergraph </b> or NO_NODE if no node
	 *            should be added
	 */
	public CompleteEmbedding(CompleteEmbedding embedding, int newEdge,
			int newNode) {
		if (newNode != NO_NODE) {
			m_nodeCount = embedding.m_nodeCount + 1;
			m_nodeMap = new int[(m_nodeCount % 4 == 0) ? m_nodeCount
					: (m_nodeCount + 4) & ~3]; // take multiples of four to
												// aid the object pool
			System.arraycopy(embedding.m_nodeMap, 0, m_nodeMap, 0,
					embedding.m_nodeCount);
			m_nodeMap[m_nodeCount - 1] = newNode;
		} else {
			m_nodeMap = embedding.m_nodeMap.clone();
			m_nodeCount = embedding.m_nodeCount;
		}

		if (newEdge != NO_EDGE) {
			m_edgeCount = embedding.m_edgeCount + 1;
			m_edgeMap = new int[(m_edgeCount % 4 == 0) ? m_edgeCount
					: (m_edgeCount + 4) & ~3]; // take multiples of four to
												// aid the object pool
			System.arraycopy(embedding.m_edgeMap, 0, m_edgeMap, 0,
					embedding.m_edgeCount);
			m_edgeMap[m_edgeCount - 1] = newEdge;
		} else {
			m_edgeMap = embedding.m_edgeMap.clone();
			m_edgeCount = embedding.m_edgeCount;
		}

		m_superGraph = embedding.m_superGraph;
	}

	/**
	 * Creates a new CompleteEmbedding that is an extension of the given
	 * embedding by new edges and/or new nodes. If no nodes or edges should be
	 * added the corresponding arrays must be of length 0 but not
	 * <code>null</code>!
	 * 
	 * @param embedding
	 *            the embeddings that should be extended
	 * @param newEdges
	 *            the new edges in the <b>supergraph </b>
	 * @param newNodes
	 *            the new nodes in the <b>supergraph </b>
	 */
	public CompleteEmbedding(CompleteEmbedding embedding, int[] newEdges,
			int[] newNodes) {
		m_nodeCount = embedding.m_nodeCount + newNodes.length;
		m_nodeMap = new int[(m_nodeCount % 4 == 0) ? m_nodeCount
				: (m_nodeCount + 4) & ~3]; // take multiples of four to aid
											// the object pool
		System.arraycopy(embedding.m_nodeMap, 0, m_nodeMap, 0,
				embedding.m_nodeCount);
		System.arraycopy(newNodes, 0, m_nodeMap, embedding.m_nodeCount,
				newNodes.length);

		m_edgeCount = embedding.m_edgeCount + newEdges.length;
		m_edgeMap = new int[(m_edgeCount % 4 == 0) ? m_edgeCount
				: (m_edgeCount + 4) & ~3]; // take multiples of four to aid
											// the object pool
		System.arraycopy(embedding.m_edgeMap, 0, m_edgeMap, 0,
				embedding.m_edgeCount);
		System.arraycopy(newEdges, 0, m_edgeMap, embedding.m_edgeCount,
				newEdges.length);

		m_superGraph = embedding.m_superGraph;
	}

	/**
	 * Creates a new CompleteEmbedding from the given node map.
	 * 
	 * @param nodeMap
	 *            a two dimensional array that hold the nodes of the <b>sub
	 *            </b>graph in <code>nodeMap[0][i]</code> and the corresponding
	 *            nodes of the <b>super </b>graph in <code>nodeMap[1][i]</code>
	 * @param subgraph
	 *            the <b>sub </b>graph
	 * @param supergraph
	 *            the <b>super </b>graph
	 */
	public CompleteEmbedding(int[][] nodeMap, Graph subgraph, Graph supergraph) {

		m_nodeMap = new int[nodeMap[0].length];
		m_edgeMap = new int[subgraph.getEdgeCount()];

		for (int i = 0; i < nodeMap[0].length; i++) {
			m_nodeMap[nodeMap[0][i]] = nodeMap[1][i];
		}
		for (int i = subgraph.getEdgeCount() - 1; i >= 0; i--) {
			final int subgraphEdge = subgraph.getEdge(i);
			final int supergraphEdge = supergraph.getEdge(
					m_nodeMap[subgraph.getNodeA(subgraphEdge)],
					m_nodeMap[subgraph.getNodeB(subgraphEdge)]);

			if (supergraphEdge != Graph.NO_EDGE) {
				m_edgeMap[subgraph.getEdgeIndex(subgraphEdge)] = supergraphEdge;
			}
		}

		m_nodeCount = m_nodeMap.length;
		m_edgeCount = m_edgeMap.length;
		m_superGraph = supergraph;
	}

	/**
	 * Creates a new complete embedding based on a node map, a subgraph and the
	 * corresponding supergraph.
	 * 
	 * @param nodeMap
	 *            an array of nodes where index <i>i</i> contains the node from
	 *            the <b>super</b>graph that corresponds to node <i>i</i> from
	 *            the <b>sub </b>graph
	 * @param subgraph
	 *            the subgraph
	 * @param supergraph
	 *            the supergraph
	 */
	public CompleteEmbedding(int[] nodeMap, Graph subgraph, Graph supergraph) {
		m_nodeMap = nodeMap;
		m_edgeMap = new int[subgraph.getEdgeCount()];

		for (int i = subgraph.getEdgeCount() - 1; i >= 0; i--) {
			final int subgraphEdge = subgraph.getEdge(i);
			final int supergraphEdge = supergraph.getEdge(
					m_nodeMap[subgraph.getNodeA(subgraphEdge)],
					m_nodeMap[subgraph.getNodeB(subgraphEdge)]);

			if (supergraphEdge != Graph.NO_EDGE) {
				m_edgeMap[subgraph.getEdgeIndex(subgraphEdge)] = supergraphEdge;
			}
		}

		m_nodeCount = m_nodeMap.length;
		m_edgeCount = m_edgeMap.length;
		m_superGraph = supergraph;
	}

	/**
	 * Creates a new CompleteEmbedding that is a copy of the given template
	 * embedding.
	 * 
	 * @param template
	 *            the embedding to be copied
	 */
	protected CompleteEmbedding(CompleteEmbedding template) {
		m_nodeMap = template.m_nodeMap.clone();
		m_edgeMap = template.m_edgeMap.clone();
		m_nodeCount = template.m_nodeCount;
		m_edgeCount = template.m_edgeCount;
		m_superGraph = template.m_superGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.GraphEmbedding#getSuperGraph()
	 */
	@Override
	public Graph getSuperGraph() {
		return m_superGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.GraphEmbedding#getSubGraph()
	 */
	@Override
	public Graph getSubGraph() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getNodeCount()
	 */
	@Override
	public int getNodeCount() {
		return m_nodeCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		return m_edgeCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getID()
	 */
	@Override
	public String getName() {
		return Integer.toString(hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#copy()
	 */
	@Override
	public Object clone() {
		return new CompleteEmbedding(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getEdge(int, int)
	 */
	@Override
	public int getEdge(int nodeA, int nodeB) {
		int edge = m_superGraph.getEdge(m_nodeMap[nodeA], m_nodeMap[nodeB]);
		if (edge == NO_EDGE)
			return NO_EDGE;

		for (int i = 0; i < m_edgeCount; i++) {
			if (m_edgeMap[i] == edge) {
				return i;
			}
		}

		return NO_EDGE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getNodeLabel(int)
	 */
	@Override
	public int getNodeLabel(int node) {
		return m_superGraph.getNodeLabel(m_nodeMap[node]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getEdgeLabel(int)
	 */
	@Override
	public int getEdgeLabel(int edge) {
		return m_superGraph.getEdgeLabel(m_edgeMap[edge]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getDegree(int)
	 */
	@Override
	public int getDegree(int node) {
		int degree = 0;
		for (int i = m_superGraph.getDegree(m_nodeMap[node]) - 1; i >= 0; i--) {
			int edge = m_superGraph.getNodeEdge(m_nodeMap[node], i);
			for (int k = 0; k < m_edgeCount; k++) {
				if (m_edgeMap[k] == edge) {
					degree++;
					break;
				}
			}
		}
		return degree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getNodeIndex(int)
	 */
	@Override
	public int getNodeIndex(int node) {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getEdgeIndex(int)
	 */
	@Override
	public int getEdgeIndex(int edge) {
		return edge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getNodeA(int)
	 */
	@Override
	public int getNodeA(int edge) {
		int node = m_superGraph.getNodeA(m_edgeMap[edge]);

		for (int i = 0; i < m_nodeCount; i++) {
			if (m_nodeMap[i] == node)
				return i;
		}
		return NO_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getNodeB(int)
	 */
	@Override
	public int getNodeB(int edge) {
		int node = m_superGraph.getNodeB(m_edgeMap[edge]);

		for (int i = 0; i < m_nodeCount; i++) {
			if (m_nodeMap[i] == node)
				return i;
		}
		return NO_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getOtherNode(int, int)
	 */
	@Override
	public int getOtherNode(int edge, int node) {
		int x = getNodeB(edge);
		if (node == x)
			return getNodeA(edge);
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getNodeEdge(int, int)
	 */
	@Override
	public int getNodeEdge(int node, int number) {
		int deg = m_superGraph.getDegree(m_nodeMap[node]);

		for (int k = 0; k < m_edgeCount; k++) {
			for (int i = deg - 1; i >= 0; i--) {
				int edge = m_superGraph.getNodeEdge(m_nodeMap[node], i);
				if ((m_edgeMap[k] == edge) && (number-- == 0)) {
					return k;
				}
			}
		}

		throw new NoSuchElementException("No such edge");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getEdge(int)
	 */
	@Override
	public int getEdge(int number) {
		return number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.UndirectedGraph#getNode(int)
	 */
	@Override
	public int getNode(int number) {
		return number;
	}

	/**
	 * Returns the node in the <b>sub </b>graph that corresponds the the given
	 * node in the <b>super </b>graph.
	 * 
	 * @param supergraphNode
	 *            a node in the supergraph
	 * @return a node in the subgraph or Graph.NO_NODE if no mapping exists
	 */
	@Override
	public int getSubgraphNode(int supergraphNode) {
		int node = Graph.NO_EDGE;
		for (int i = 0; i < m_nodeCount; i++) {
			if (m_nodeMap[i] == supergraphNode) {
				node = i;
				break;
			}
		}
		return node;
	}

	/**
	 * Returns the edge in the <b>sub </b>graph that corresponds the the given
	 * edge in the <b>super </b>graph.
	 * 
	 * @param supergraphEdge
	 *            an edge in the supergraph
	 * @return an edge in the subgraph or Graph.NO_EDGE if no mapping exists
	 */
	@Override
	public int getSubgraphEdge(int supergraphEdge) {
		int edge = Graph.NO_EDGE;
		for (int i = 0; i < m_edgeCount; i++) {
			if (m_edgeMap[i] == supergraphEdge) {
				edge = i;
				break;
			}
		}
		return edge;
	}

	/**
	 * Returns the node in the <b>super </b>graph that corresponds the the given
	 * node in the <b>sub </b>graph.
	 * 
	 * @param subgraphNode
	 *            a node in the subgraph
	 * @return a node in the supergraph
	 */
	@Override
	public int getSupergraphNode(int subgraphNode) {
		return m_nodeMap[subgraphNode];
	}

	/**
	 * Returns the edge in the <b>super </b>graph that corresponds the the given
	 * edge in the <b>sub </b>graph.
	 * 
	 * @param subgraphEdge
	 *            an edge in the subgraph
	 * @return an edge in the supergraph
	 */
	@Override
	public int getSupergraphEdge(int subgraphEdge) {
		return m_edgeMap[subgraphEdge];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return SimpleGraphComparator.getHashCode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.DirectedGraph#getInDegree(int)
	 */
	@Override
	public int getInDegree(int node) {
		int degree = 0;
		for (int i = ((DirectedGraph) m_superGraph)
				.getInDegree(m_nodeMap[node]) - 1; i >= 0; i--) {
			int edge = ((DirectedGraph) m_superGraph).getIncomingNodeEdge(
					m_nodeMap[node], i);
			for (int k = 0; k < m_edgeCount; k++) {
				if (m_edgeMap[k] == edge) {
					degree++;
					break;
				}
			}
		}
		return degree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.DirectedGraph#getOutDegree(int)
	 */
	@Override
	public int getOutDegree(int node) {
		int degree = 0;
		for (int i = ((DirectedGraph) m_superGraph)
				.getOutDegree(m_nodeMap[node]) - 1; i >= 0; i--) {
			int edge = ((DirectedGraph) m_superGraph).getOutgoingNodeEdge(
					m_nodeMap[node], i);
			for (int k = 0; k < m_edgeCount; k++) {
				if (m_edgeMap[k] == edge) {
					degree++;
					break;
				}
			}
		}
		return degree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.DirectedGraph#getIncomingNodeEdge(int, int)
	 */
	@Override
	public int getIncomingNodeEdge(int node, int number) {
		int deg = ((DirectedGraph) m_superGraph).getInDegree(m_nodeMap[node]);

		for (int k = 0; k < m_edgeCount; k++) {
			for (int i = deg - 1; i >= 0; i--) {
				int edge = ((DirectedGraph) m_superGraph).getIncomingNodeEdge(
						m_nodeMap[node], i);
				if ((m_edgeMap[k] == edge) && (number-- == 0)) {
					return k;
				}
			}
		}

		throw new NoSuchElementException("No such edge");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.DirectedGraph#getOutgoingNodeEdge(int, int)
	 */
	@Override
	public int getOutgoingNodeEdge(int node, int number) {
		int deg = ((DirectedGraph) m_superGraph).getOutDegree(m_nodeMap[node]);

		for (int k = 0; k < m_edgeCount; k++) {
			for (int i = deg - 1; i >= 0; i--) {
				int edge = ((DirectedGraph) m_superGraph).getOutgoingNodeEdge(
						m_nodeMap[node], i);
				if ((m_edgeMap[k] == edge) && (number-- == 0)) {
					return k;
				}
			}
		}

		throw new NoSuchElementException("No such edge");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.DirectedGraph#getEdgeDirection(int, int)
	 */
	@Override
	public int getEdgeDirection(int edge, int node) {
		if ((edge >= m_edgeCount) || (node >= m_nodeCount))
			return NO_EDGE;

		return ((DirectedGraph) m_superGraph).getEdgeDirection(m_edgeMap[edge],
				m_nodeMap[node]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.GraphEmbedding#isDirectedGraphEmbedding()
	 */
	@Override
	public final boolean isDirectedGraphEmbedding() {
		return (m_superGraph instanceof DirectedGraph);
	}

	/**
	 * Creates all complete embeddings of the subgraph in the supergraph.
	 * 
	 * @param embedding
	 *            the embedding that should be upgraded to a complete embedding
	 * @param completeEmbeddings
	 *            a collection into which the complete embeddings are stored.
	 * @return the number of complete embeddings
	 */
	public static int getCompleteEmbeddings(GraphEmbedding embedding,
			Collection completeEmbeddings) {
		return getCompleteEmbeddings(embedding, completeEmbeddings,
				Factory.instance);
	}

	public static int getCompleteEmbeddings2(Graph superGraph, Graph subGraph,
			Collection completeEmbeddings) {
		return getCompleteEmbeddings2(superGraph, subGraph, completeEmbeddings,
				Factory.instance);
	}

	protected static int getCompleteEmbeddings(GraphEmbedding embedding,
			Collection completeEmbeddings, Factory factory) {
		if (embedding instanceof NodeEmbedding) {
			completeEmbeddings.add(factory.getInstance(
					((NodeEmbedding) embedding).getNodeMapping(),
					embedding.getSubGraph(), embedding.getSuperGraph()));
			return 1;
		} else {
			int size = completeEmbeddings.size();
			if ((embedding.getSubGraph().getEdgeCount() > embedding
					.getSuperGraph().getEdgeCount())
					&& (embedding.getSubGraph().getNodeCount() < embedding
							.getSuperGraph().getNodeCount()))
				return 0;
			if ((embedding.getSubGraph().getEdgeCount() < embedding
					.getSuperGraph().getEdgeCount())
					&& (embedding.getSubGraph().getNodeCount() > embedding
							.getSuperGraph().getNodeCount()))
				return 0;

			int[][] map = new int[2][embedding.getSubGraph().getNodeCount()];
			for (int i = 0; i < map[0].length; i++)
				map[0][i] = -1;
			for (int i = 0; i < map[1].length; i++)
				map[1][i] = -1;

			int[] subgraphNodes = new int[embedding.getSubGraph()
					.getNodeCount()];
			for (int i = 0; i < subgraphNodes.length; i++) {
				subgraphNodes[i] = embedding.getSubGraph().getNode(i);
			}

			int[] supergraphNodes = new int[embedding.getSuperGraph()
					.getNodeCount()];
			for (int i = 0; i < supergraphNodes.length; i++) {
				supergraphNodes[i] = embedding.getSuperGraph().getNode(i);
			}
			for (int t = 0; t < embedding.getSubGraph().getNodeCount(); t++) {
				int node = embedding.getSubGraph().getNode(t);
			}

			getSubgraphIsomorphism(
					new int[][] { subgraphNodes, supergraphNodes }, map,
					embedding.getSubGraph(), embedding.getSuperGraph(),
					new boolean[embedding.getSuperGraph().getNodeCount()], 0,
					completeEmbeddings, factory);
			return completeEmbeddings.size() - size;
		}
	}

	protected static int getCompleteEmbeddings2(Graph superGraph,
			Graph subGraph, Collection completeEmbeddings, Factory factory) {
		/*
		 * if (embedding instanceof NodeEmbedding) {
		 * completeEmbeddings.add(factory.getInstance(((NodeEmbedding)
		 * embedding).getNodeMapping(), embedding.getSubGraph(),
		 * embedding.getSuperGraph())); return 1; } else {
		 */
		int size = completeEmbeddings.size();
		if ((subGraph.getEdgeCount() > superGraph.getEdgeCount())
				&& (subGraph.getNodeCount() < superGraph.getNodeCount()))
			return 0;
		if ((subGraph.getEdgeCount() < superGraph.getEdgeCount())
				&& (subGraph.getNodeCount() > superGraph.getNodeCount()))
			return 0;

		int[][] map = new int[2][subGraph.getNodeCount()];
		for (int i = 0; i < map[0].length; i++)
			map[0][i] = -1;
		for (int i = 0; i < map[1].length; i++)
			map[1][i] = -1;

		int[] subgraphNodes = new int[subGraph.getNodeCount()];
		for (int i = 0; i < subgraphNodes.length; i++) {
			subgraphNodes[i] = subGraph.getNode(i);
		}

		int[] supergraphNodes = new int[superGraph.getNodeCount()];
		for (int i = 0; i < supergraphNodes.length; i++) {
			supergraphNodes[i] = superGraph.getNode(i);
		}

		getSubgraphIsomorphism(new int[][] { subgraphNodes, supergraphNodes },
				map, subGraph, superGraph,
				new boolean[superGraph.getNodeCount()], 0, completeEmbeddings,
				factory);
		return completeEmbeddings.size() - size;
	}

	/**
	 * Creates all complete embeddings for the given subgraph.
	 * 
	 * @param subgraph
	 *            the subgraph
	 * @param graphs
	 *            a collection of graphs in which the embeddings should be
	 *            created
	 * @param completeEmbeddings
	 *            a collection into which the found embeddings are inserted
	 * @param factory
	 *            a factory for embeddings
	 * @return the number of found embeddings
	 */
	protected static int getCompleteEmbeddings(Graph subgraph,
			Collection graphs, Collection completeEmbeddings, Factory factory) {
		final int[][] map = new int[2][subgraph.getNodeCount()];
		final int[] subgraphNodes = new int[subgraph.getNodeCount()];
		for (int i = 0; i < subgraphNodes.length; i++) {
			subgraphNodes[i] = subgraph.getNode(i);
		}

		final int size = completeEmbeddings.size();

		for (Iterator it = graphs.iterator(); it.hasNext();) {
			final Graph supergraph = (Graph) it.next();

			if ((subgraph.getEdgeCount() > supergraph.getEdgeCount())
					&& (subgraph.getNodeCount() < supergraph.getNodeCount()))
				continue;
			if ((subgraph.getEdgeCount() < supergraph.getEdgeCount())
					&& (subgraph.getNodeCount() > supergraph.getNodeCount()))
				continue;

			for (int i = 0; i < map[0].length; i++)
				map[0][i] = -1;
			for (int i = 0; i < map[1].length; i++)
				map[1][i] = -1;

			int[] supergraphNodes = new int[supergraph.getNodeCount()];
			for (int i = 0; i < supergraphNodes.length; i++) {
				supergraphNodes[i] = supergraph.getNode(i);
			}

			getSubgraphIsomorphism(
					new int[][] { subgraphNodes, supergraphNodes }, map,
					subgraph, supergraph,
					new boolean[supergraph.getNodeCount()], 0,
					completeEmbeddings, factory);
		}

		return completeEmbeddings.size() - size;
	}

	/**
	 * Creates all complete embeddings for the given subgraph.
	 * 
	 * @param subgraph
	 *            the subgraph
	 * @param graphs
	 *            a collection of graphs in which the embeddings should be
	 *            created
	 * @param completeEmbeddings
	 *            a collection into which the found embeddings are inserted
	 * @return the number of found embeddings
	 */
	public static int getCompleteEmbeddings(Graph subgraph, Collection graphs,
			Collection completeEmbeddings) {
		return getCompleteEmbeddings(subgraph, graphs, completeEmbeddings,
				Factory.instance);
	}

	protected static void getSubgraphIsomorphism(int[][] nodes, int[][] map,
			Graph subgraph, Graph supergraph, boolean[] used, int count,
			Collection embeddings, Factory factory) {
		if (count >= nodes[0].length) {
			embeddings.add(factory.getInstance(map, subgraph, supergraph));
			return;
		}

		for (int i = 0; i < nodes[1].length; i++) {

			if (!used[i]
					&& canMatch(subgraph, nodes[0][count], supergraph,
							nodes[1][i], map, count - 1)) {
				map[0][count] = nodes[0][count];
				map[1][count] = nodes[1][i];
				used[i] = true;

				getSubgraphIsomorphism(nodes, map, subgraph, supergraph, used,
						count + 1, embeddings, factory);

				map[0][count] = -1;
				map[1][count] = -1;
				used[i] = false;
			}
		}
	}

	protected static CompleteEmbedding createEmbedding(int[][] nodes,
			int[][] map, Graph subgraph, Graph supergraph) {
		return new CompleteEmbedding(map, subgraph, supergraph);
	}

	protected static boolean canMatch(Graph subgraph, int nodeA,
			Graph supergraph, int nodeB, int[][] map, int count) {
		if (SimpleNodeComparator.instance.compare(subgraph, nodeA, supergraph,
				nodeB) != 0)
			return false;

		// if the node in the subgraph has more edges it cannot be mapped onto
		// the supergraph node
		if (subgraph.getDegree(nodeA) > supergraph.getDegree(nodeB))
			return false;
		for (int i = count; i >= 0; i--) {
			int edge1 = subgraph.getEdge(nodeA, map[0][i]);
			int edge2 = supergraph.getEdge(nodeB, map[1][i]);

			if ((edge1 != Graph.NO_EDGE) && (edge2 == Graph.NO_EDGE)) {
				return false;
			} else if ((edge1 != Graph.NO_EDGE)
					&& (SimpleEdgeComparator.instance.compare(subgraph, edge1,
							supergraph, edge2) != 0)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.Graph#isBridge(int)
	 */
	@Override
	public boolean isBridge(int edge) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * A factory that creates CompleteEmbeddings.
	 */
	protected static class Factory {
		/** The only public instance of this factory */
		public final static Factory instance = new Factory();

		protected Factory() {
		}

		/**
		 * Returns a new complete embedding object base on a node map, a
		 * subgraph and the corresponding supergraph.
		 * 
		 * @param nodeMap
		 *            a two dimensional array that hold the nodes of the <b>sub
		 *            </b>graph in <code>nodeMap[0][i]</code> and the
		 *            corresponding nodes of the <b>super </b>graph in
		 *            <code>nodeMap[1][i]</code>
		 * @param subgraph
		 *            the <b>sub </b>graph
		 * @param supergraph
		 *            the <b>super </b>graph
		 * @return a new complete embedding
		 */
		public CompleteEmbedding getInstance(int[][] nodeMap, Graph subgraph,
				Graph supergraph) {
			return new CompleteEmbedding(nodeMap, subgraph, supergraph);
		}

		/**
		 * Returns a new complete embedding object base on a node map, a
		 * subgraph and the corresponding supergraph.
		 * 
		 * @param nodeMap
		 *            an array of nodes where index <i>i</i> contains the node
		 *            from the <b>super</b>graph that corresponds to node
		 *            <i>i</i> from the <b>sub </b>graph
		 * @param subgraph
		 *            the subgraph
		 * @param supergraph
		 *            the supergraph
		 * @return a new complete embedding
		 */
		public CompleteEmbedding getInstance(int[] nodeMap, Graph subgraph,
				Graph supergraph) {
			return new CompleteEmbedding(nodeMap, subgraph, supergraph);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.Graph#setNodeObject(int, java.lang.Object)
	 */
	@Override
	public void setNodeObject(int node, Object o) {
		throw new UnsupportedOperationException(
				"An embedding must not set the node object of the underlying graph");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.Graph#getNodeObject(int)
	 */
	@Override
	public Object getNodeObject(int node) {
		return m_superGraph.getNodeObject(m_nodeMap[node]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.Graph#setEdgeObject(int, java.lang.Object)
	 */
	@Override
	public void setEdgeObject(int edge, Object o) {
		throw new UnsupportedOperationException(
				"An embedding must not set the edge object of the underlying graph");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.Graph#getEdgeObject(int)
	 */
	@Override
	public Object getEdgeObject(int edge) {
		return m_superGraph.getEdgeObject(m_edgeMap[edge]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.Graph#saveMemory()
	 */
	@Override
	public void saveMemory() { /* nothing do here */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.graph.Graph#getID()
	 */
	@Override
	public int getID() {
		// this should in almost all cases create a unique id although
		// System.identityHashCode does not
		return System.identityHashCode(this) ^ m_superGraph.getID()
				^ System.identityHashCode(m_edgeMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.parmol.graph.GraphEmbedding#overlaps(de.parmol.graph.GraphEmbedding)
	 */
	@Override
	public boolean overlaps(GraphEmbedding other) {
		CompleteEmbedding emb = (CompleteEmbedding) other;

		if (emb.m_superGraph != this.m_superGraph)
			return false;

		for (int i = 0; i < this.m_nodeCount; i++) {
			for (int k = 0; k < emb.m_nodeCount; k++) {
				if (this.m_nodeMap[i] == emb.m_nodeMap[k])
					return true;
			}
		}

		return false;
	}

	/**
	 * Extended by Dayu Yuan: current embedding "this" is a embedding between
	 * subgraph and supergraph given another embedding, "to MidleEmbedding" is a
	 * embedding between subgraph and anothergraph return a embedding between
	 * another graph to supergraph if there is any
	 * 
	 * @param toMidleEmbedding
	 * @return
	 */
	public List<CompleteEmbedding> getMidleEmbedding(
			CompleteEmbedding toMidleEmbedding) {
		Graph subGraph = this.getSubGraph(), midleGraph = toMidleEmbedding
				.getSuperGraph(), superGraph = this.getSuperGraph();
		if ((midleGraph.getEdgeCount() > superGraph.getEdgeCount())
				|| (midleGraph.getNodeCount() > superGraph.getNodeCount()))
			return new ArrayList<CompleteEmbedding>(0);

		int count = subGraph.getNodeCount() - 1;
		// map[0][i] = midleGraph.getNode(index i)
		// map[1][i] = corresponding super graph node of map[0][i]
		int[][] map = new int[2][midleGraph.getNodeCount()];
		for (int i = 0; i <= count; i++) {
			map[0][i] = toMidleEmbedding.m_nodeMap[i];
			map[1][i] = this.m_nodeMap[i];
		}
		for (int i = count + 1; i < map[0].length; i++) {
			map[0][i] = -1;
			map[1][i] = -1;
		}
		List embeddings = new ArrayList<CompleteEmbedding>();
		boolean used[] = new boolean[superGraph.getNodeCount()];
		int[] midlegraphNodes = new int[midleGraph.getNodeCount()];
		for (int i = 0; i < midlegraphNodes.length; i++) {
			midlegraphNodes[i] = midleGraph.getNode(i);
		}
		int[] supergraphNodes = new int[superGraph.getNodeCount()];
		for (int i = 0; i < supergraphNodes.length; i++) {
			supergraphNodes[i] = superGraph.getNode(i);
		}
		for (int i = 0; i <= count; i++) {
			int superNode = this.m_nodeMap[i];
			int superNodeIndex = superGraph.getNodeIndex(superNode);
			used[superNodeIndex] = true;
		}
		getSubgraphIsomorphism(
				new int[][] { midlegraphNodes, supergraphNodes }, map,
				midleGraph, superGraph, used, count, embeddings,
				Factory.instance);
		if (embeddings.isEmpty()) {
			return new ArrayList<CompleteEmbedding>(0);
		} else {
			return embeddings;
		}
	}
}
