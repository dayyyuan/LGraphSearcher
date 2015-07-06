package edu.psu.chemxseer.structure.supersearch.GraphIntegration;

import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;

public class FindSearchOrder {

	/**
	 * Given the input "g", find the sequence of visited nodes TODO: slight
	 * change afterwards
	 * 
	 * @param g
	 * @return
	 */
	public static int[] getNodeSequence(IntegrateGraph intG, Graph g) {
		int[] results = new int[g.getNodeCount()];
		int index = 0;
		boolean[] nodeVisited = new boolean[g.getNodeCount()];
		for (int i = 0; i < nodeVisited.length; i++)
			nodeVisited[i] = false;

		List<OneEdge> surroudingEdge = new ArrayList<OneEdge>();
		while (index < results.length) {
			if (index == 0) {
				// find the edge with maximum frequency to start
				int maxFreq = -1;
				int maxEdge = -1;
				for (int edge = 0; edge < g.getEdgeCount(); edge++) {
					int nodeA = g.getNodeA(edge);
					int nodeB = g.getNodeB(edge);
					OneEdge aEdge = new OneEdge(g.getNodeLabel(nodeA),
							g.getNodeLabel(nodeB), g.getEdgeLabel(edge));
					Integer freq = intG.getHeaderTableFreq(aEdge);
					if (freq == null)
						freq = 0;
					if (freq > maxFreq) {
						maxFreq = freq;
						maxEdge = edge;
					}
				}
				// assign the node sequence
				results[0] = g.getNodeA(maxEdge);
				results[1] = g.getNodeB(maxEdge);
				// update the status
				nodeVisited[results[0]] = nodeVisited[results[1]] = true;
				addSurroudings(g, results[0], surroudingEdge, nodeVisited);
				addSurroudings(g, results[1], surroudingEdge, nodeVisited);
				index = 2;
			} else {
				// find the edge with maximum surrounding scores
				int maxFreq = -1;
				OneEdge maxEdge = null;
				int maxID = -1;
				boolean containued = false;
				for (int i = 0; i < surroudingEdge.size(); i++) {
					OneEdge aEdge = surroudingEdge.get(i);
					Integer freq = intG.getHeaderTableFreq(aEdge);
					if (freq == null)
						freq = 0;
					if (freq > maxFreq) {
						// if both nodeA and nodeB of aEdge is visited, then
						// continue;
						if (nodeVisited[aEdge.getNodeAID()] == true
								&& nodeVisited[aEdge.getNodeBID()] == true) {
							// remove the node
							surroudingEdge.remove(i);
							containued = true;
							break;
						} else {
							maxFreq = freq;
							maxID = i;
							maxEdge = aEdge;
						}
					}
				}
				if (containued)
					continue;
				else {
					// found the edge with maximums surrounding scores
					// TEST
					if (nodeVisited[maxEdge.getNodeBID()] == true)
						System.out
								.println("Excpetion in Integrate Graphs: getNodeSequence");
					// END OF TEST
					surroudingEdge.remove(maxID);
					results[index++] = maxEdge.getNodeBID();
					// update the status
					nodeVisited[maxEdge.getNodeBID()] = true;
					addSurroudings(g, maxEdge.getNodeBID(), surroudingEdge,
							nodeVisited);

				}
			}
		}
		return results;
	}

	private static void addSurroudings(Graph g, int node1,
			List<OneEdge> surroudingEdge, boolean[] nodeVisited) {
		for (int w = 0; w < g.getDegree(node1); w++) {
			int edge = g.getNodeEdge(node1, w);
			int node2 = g.getOtherNode(edge, node1);
			if (nodeVisited[node2])
				continue; // skip the visited node2
			else {
				OneEdge aEdge = new OneEdge(node1, node2, edge,
						g.getNodeLabel(node1), g.getNodeLabel(node2),
						g.getEdgeLabel(edge));
				surroudingEdge.add(aEdge);
			}
		}
	}
}
