package edu.psu.chemxseer.structure.newISO.Util;

import de.parmol.graph.Graph;

/**
 * An ISO graph which also support edge operation
 * 
 * @author dayuyuan
 * 
 */
public class ISOEdgeGraph extends ISOGraph {
	private int[][] edges; // edges[i][0] = nodeA, edges[i][1] = nodeB
	private int[] edgeLabel;

	// connectivity[i][j]!= edge(i,j), but connectivity[i][j] = index edge(i, j)
	// + 1;

	public ISOEdgeGraph(Graph g) {
		int nodeCount = g.getNodeCount();
		int edgeCount = g.getEdgeCount();
		// Initial internal representation of graph small and graph big
		this.vertices = new int[nodeCount][];
		this.connectivity = new int[nodeCount][nodeCount];
		this.edges = new int[edgeCount][2];
		this.edgeLabel = new int[edgeCount];

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
				connectivity[i][anotherNode] = edge;
				vertices[i][j] = anotherNode;
				if (i < anotherNode) {
					edgeLabel[edge] = g.getEdgeLabel(edge);
					edges[edge][0] = i;
					edges[edge][1] = anotherNode;
				}

			}
		}
	}

	/**
	 * Get edge label: e(node1, node2) Return -1 if e does not exist
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	@Override
	public int getEdgeLabel(int node1, int node2) {
		if (this.connectivity[node1][node2] == NULL_EDGE)
			return -1;
		else
			return this.edgeLabel[this.connectivity[node1][node2]];
	}

	public int getEdgelabel(int edge) {
		if (edge < 0 && edge > this.edgeLabel.length)
			return -1;
		return this.edgeLabel[edge];
	}

	public int[] getEdgeNodes(int edge) {
		if (edge < 0 && edge > this.edgeLabel.length)
			return null;
		else
			return this.edges[edge];
	}

	public int getEdge(int node1, int node2) {
		return this.connectivity[node1][node2];
	}

	public int getEdgeCount() {
		return this.edgeLabel.length;
	}

}
