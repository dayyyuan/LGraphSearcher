package edu.psu.chemxseer.structure.newISO.Util;

import de.parmol.graph.Graph;

/**
 * A implementation of graph: containing both the Linked list and Matrix
 * representation of an object graph. To distinguish with Graph class in other
 * resources, we name it as ISOGraph Here we treat ISOGraph as the simplest
 * symmetric and labeled undirected graph. For attributed graph, please refer to
 * ISOAttrGraph
 * 
 * @author dayuyuan
 * 
 */
public class ISOGraph {
	// linked-list representation
	protected int[][] vertices;
	// matrix representation
	// connectivity[i][i] = label(vertex i)
	// connectivity[i][j] = label(edge(i,j)) or -1 if no edge exists;
	protected int[][] connectivity;
	protected static int NULL_EDGE = -1;

	public ISOGraph() {
		// Dummy Constructor
	}

	/**
	 * Initialize ISOGraph with a Parmol graph
	 * 
	 * @param g
	 */
	public ISOGraph(Graph g) {
		int nodeCount = g.getNodeCount();
		// Initial internal representation of graph small and graph big
		this.vertices = new int[nodeCount][];
		this.connectivity = new int[nodeCount][nodeCount];

		for (int i = 0; i < nodeCount; i++) {
			vertices[i] = new int[g.getDegree(i)];
			for (int temp = 0; temp < nodeCount; temp++)
				this.connectivity[i][temp] = NULL_EDGE;

			this.connectivity[i][i] = g.getNodeLabel(i);
			for (int j = 0; j < vertices[i].length; j++) {
				int edge = g.getNodeEdge(i, j);
				int anotherNode = g.getNodeB(edge);
				if (anotherNode == i)
					anotherNode = g.getNodeA(edge);
				connectivity[i][anotherNode] = g.getEdgeLabel(edge);
				vertices[i][j] = anotherNode;
			}
		}
	}

	/**
	 * Get the total number of nodes in this graph
	 * 
	 * @return
	 */
	public int getNodeCount() {
		return this.connectivity.length;
	}

	/**
	 * Get node label: can be extended to Attribute getNodeLabel(int nodeID)
	 * 
	 * @param nodeID
	 * @return
	 */
	public int getNodeLabel(int nodeID) {
		return this.connectivity[nodeID][nodeID];
	}

	/**
	 * Get edge label: e(node1, node2) Return -1 if e does not exist
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	public int getEdgeLabel(int node1, int node2) {
		return this.connectivity[node1][node2];
	}

	/**
	 * Get node degree
	 * 
	 * @param nodeID
	 * @return
	 */
	public int getDegree(int nodeID) {
		return this.vertices[nodeID].length;
	}

	/**
	 * Get the "index" th adjacent node of "nodeID" return -1, if index is out
	 * of range
	 * 
	 * @param nodeID
	 * @param index
	 * @return
	 */
	public int getAdjacentNode(int nodeID, int index) {
		if (index >= this.vertices[nodeID].length)
			return -1;
		return this.vertices[nodeID][index];
	}

}
