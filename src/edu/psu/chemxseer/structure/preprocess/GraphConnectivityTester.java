package edu.psu.chemxseer.structure.preprocess;

import java.util.LinkedList;
import java.util.Queue;

import de.parmol.graph.Graph;

/**
 * Test the Connectivity of graphs
 * 
 * @author duy113
 * 
 */
public class GraphConnectivityTester {

	public static boolean isConnected(Graph g) {
		// visited nodes of graph g in breath first search
		boolean[] visited = new boolean[g.getNodeCount()];
		for (int i = 0; i < visited.length; i++)
			visited[i] = false;
		// starting from the first node
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(0);
		visited[0] = true;
		while (!queue.isEmpty()) {
			int currentNode = queue.poll();
			// add currentNode's adjacent nodes in queue if not visited yet
			for (int j = 0; j < g.getDegree(currentNode); j++) {
				int adjacentEdge = g.getNodeEdge(currentNode, j);
				int adjacentNode = g.getOtherNode(adjacentEdge, currentNode);
				if (visited[adjacentNode] == false) {
					queue.offer(adjacentNode);
					visited[adjacentNode] = true;
				}
			}
		}
		// If these any unvisited node
		for (int i = 0; i < visited.length; i++) {
			if (visited[i] == false)
				return false;
		}
		return true;
	}
}
