package edu.psu.chemxseer.structure.iso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.util.SortIntSet;

/**
 * New implementation of the Canonical DFS in April,2,2013 Called by
 * CanonicalDFSImpl
 * 
 * @author dayuyuan
 * 
 */
public class CanonicalDFSImplInternal {
	// Internal representation of the graph
	private int[][] vertices; // vertices[i] = list of adjacent node to j
	private int[][] connectivity;// connectivity[i][i] = label(i),
									// connectivity[i][j] = edge(i,j), edge id
	private int[] edgeLabel; // label of each edge

	// Status of the depth first search
	private boolean[] edgeVisited; // mark on edge visited or not
	private int edgeDepth;
	private int[] nodeVisited;
	private int nodeDepth;
	// private LinkedList<Integer> nodeStack; //node that has been
	// private LinkedList<int[]> nodeStackAdj; // forward adjacent nodes
	private int[] nodeSequence;
	private int[][] nodeAdjSequence;
	private int nodePos;
	private LinkedList<Integer> nodePosStack;
	// Output
	public int[][] minSequence;
	public int[][] oneSequence;

	public void initialize(Graph g) {
		int nodeCount = g.getNodeCount();
		// Initial internal representation of graph small and graph big
		this.vertices = new int[nodeCount][];
		this.connectivity = new int[nodeCount][nodeCount];
		this.edgeLabel = new int[g.getEdgeCount()];
		for (int nodeI = 0; nodeI < nodeCount; nodeI++) {
			vertices[nodeI] = new int[g.getDegree(nodeI)];
			for (int temp = 0; temp < nodeCount; temp++) {
				this.connectivity[nodeI][temp] = -1;
			}
			this.connectivity[nodeI][nodeI] = g.getNodeLabel(nodeI);
			for (int j = 0; j < vertices[nodeI].length; j++) {
				int edge = g.getNodeEdge(nodeI, j);
				int nodeJ = g.getOtherNode(edge, nodeI);
				connectivity[nodeI][nodeJ] = edge;
				edgeLabel[edge] = g.getEdgeLabel(edge);
				vertices[nodeI][j] = nodeJ;
			}
		}
	}

	public void initialize(int[][] graphConnectivity, int[] edgeLabel) {
		// Initial internal representation of graph small and graph big
		this.vertices = new int[graphConnectivity.length][];
		this.connectivity = graphConnectivity;
		this.edgeLabel = edgeLabel;
		// Initialize this.vertices
		for (int nodeI = 0; nodeI < graphConnectivity.length; nodeI++) {
			int degree = 0;
			for (int nodeJ = 0; nodeJ < graphConnectivity.length; nodeJ++)
				if (connectivity[nodeI][nodeJ] != -1 && nodeI != nodeJ)
					degree++;
			vertices[nodeI] = new int[degree];
			for (int nodeJ = 0, j = 0; nodeJ < graphConnectivity.length; nodeJ++)
				if (connectivity[nodeI][nodeJ] != -1 && nodeI != nodeJ) {
					vertices[nodeI][j] = nodeJ;
					++j;
				}
		}
	}

	public boolean dealCornerCase(Graph g) {
		// Dealing with the situation when g has no edge
		if (g == null) {
			this.minSequence = null;
			return false;
		} else if (g.getNodeCount() == 0) {
			this.minSequence = null;
			return false;
		} else if (g.getEdgeCount() == 0)// No edge At all
		{
			if (g.getNodeCount() > 1) {
				System.out
						.println("YDY: Exception in serialize of CanonicalDFS: not connected");
				return false;
			}
			this.minSequence = new int[1][5];
			minSequence[0][0] = 0;
			minSequence[0][1] = -1;
			minSequence[0][2] = g.getNodeLabel(0);
			minSequence[0][3] = -1;
			minSequence[0][4] = -1;
			return false;
		}
		return true;
	}

	public boolean dealCornerCase(int[][] graphConnectivity,
			int[] graphEdgeLabel) {
		// Dealing with the situation when g has no edge
		if (graphConnectivity == null || graphEdgeLabel == null)
			return false;
		else if (graphConnectivity.length == 0)
			return false;
		else if (graphEdgeLabel.length == 0)// No edge At all
		{
			if (graphConnectivity.length > 1) {
				System.out
						.println("YDY: Exception in serialize of CanonicalDFS: not connected");
				return false;
			}
			this.minSequence = new int[1][5];
			minSequence[0][0] = 0;
			minSequence[0][1] = -1;
			minSequence[0][2] = graphConnectivity[0][0];
			minSequence[0][3] = -1;
			minSequence[0][4] = -1;
			return false;
		}
		return true;
	}

	public void findSerialization() {
		edgeVisited = new boolean[edgeLabel.length];
		nodeVisited = new int[vertices.length];
		// nodeStack = new LinkedList<Integer>();
		// nodeStackAdj = new LinkedList<int[]>();
		nodeSequence = new int[vertices.length];
		nodeAdjSequence = new int[vertices.length][];
		nodePosStack = new LinkedList<Integer>();
		minSequence = new int[edgeLabel.length][];
		oneSequence = new int[edgeLabel.length][];

		// start the depth-first search
		List<Integer> minNode = this.findMinNodes();
		for (int rootNode : minNode) {
			// 1. Initialization
			Arrays.fill(edgeVisited, false);
			Arrays.fill(nodeVisited, -1);
			nodeDepth = 0;
			edgeDepth = 0;
			nodeVisited[rootNode] = nodeDepth++; // 0
			nodePos = 0;
			nodeSequence[nodePos] = rootNode; // push the smaller node
			nodeAdjSequence[nodePos] = searchAdjNodes(rootNode)[1];
			nodePosStack.push(nodePos);
			nodePos++;
			depthFirstSearch();
		}
	}

	private void depthFirstSearch() {
		if (nodePosStack.isEmpty()) {
			// copy oneSequence to minSequence
			minSequence = oneSequence.clone();
			return;
		}
		int dfsPos = nodePosStack.peek();

		int nodeA = nodeSequence[dfsPos];
		int[] adjNodes = nodeAdjSequence[dfsPos];

		boolean traceBack = true;
		for (int nodeB : adjNodes) {
			if (nodeVisited[nodeB] >= 0)
				continue;
			traceBack = false;
			int[] newNodeEntry = new int[] { dfsPos, nodeDepth,
					connectivity[nodeA][nodeA],
					edgeLabel[connectivity[nodeA][nodeB]],
					connectivity[nodeB][nodeB] };
			boolean goodDecisionToVisitB = false;
			if (minSequence[edgeDepth] == null
					|| compareEntry(newNodeEntry, minSequence[edgeDepth]) <= 0) {
				goodDecisionToVisitB = true;
				int[][] adjNodesB = this.searchAdjNodes(nodeB);
				// 1. try to insert backward entries
				int[][] newEdgeEntries = new int[adjNodesB[0].length - 1][];
				int newEntriesIndex = 0;
				for (int nodeC : adjNodesB[0]) {
					if (nodeC == nodeA)
						continue;
					newEdgeEntries[newEntriesIndex] = new int[] { nodeDepth,
							nodeVisited[nodeC], connectivity[nodeB][nodeB],
							edgeLabel[connectivity[nodeB][nodeC]],
							connectivity[nodeC][nodeC] };
					if (minSequence[edgeDepth + 1 + newEntriesIndex] != null
							&& compareEntry(
									newEdgeEntries[newEntriesIndex],
									minSequence[edgeDepth + 1 + newEntriesIndex]) > 0) {
						goodDecisionToVisitB = false;
						break;
					}
					newEntriesIndex++;
				}
				// 2. Insert node entry & edge entries
				if (goodDecisionToVisitB) {
					oneSequence[edgeDepth++] = newNodeEntry;
					for (int w = 0; w < newEdgeEntries.length; w++) {
						oneSequence[edgeDepth++] = newEdgeEntries[w];
					}
					// 2.2 Update the surrounding information
					nodeVisited[nodeB] = nodeDepth++;
					edgeVisited[connectivity[nodeA][nodeB]] = true;
					for (int nodeC : adjNodesB[0])
						edgeVisited[connectivity[nodeB][nodeC]] = true;
					nodeSequence[nodePos] = nodeB;
					nodeAdjSequence[nodePos] = adjNodesB[1];
					@SuppressWarnings("unchecked")
					LinkedList<Integer> backUp = (LinkedList<Integer>) nodePosStack
							.clone();
					nodePosStack.push(nodePos);
					nodePos++;
					// 3. Keep on Growing
					depthFirstSearch();
					// 4. Recover
					nodePos--;
					nodePosStack = backUp;
					for (int nodeC : adjNodesB[0]) {
						if (nodeC == nodeA)
							continue;
						else
							edgeVisited[connectivity[nodeB][nodeC]] = false;
					}
					edgeVisited[connectivity[nodeA][nodeB]] = false;
					nodeVisited[nodeB] = -1;
					nodeDepth--;
					edgeDepth--;
					edgeDepth -= newEdgeEntries.length;
				}
			}
		}

		if (traceBack) {
			nodePosStack.pop();
			depthFirstSearch();
		}
	}

	/**
	 * Given nodeA, return result where result[0] contains all adjacent nodes
	 * that are visited already, ordered according to visit depth and result[1]
	 * contains all adjacent nodes that are un-visited, ordered according to the
	 * edge/node label
	 * 
	 * @param nodeA
	 * @return
	 */
	private int[][] searchAdjNodes(int nodeA) {
		int[] backNodes = new int[vertices[nodeA].length];
		int backNodesIndex = 0;
		int[] forwardNodes = new int[vertices[nodeA].length];
		int forwardNodesIndex = 0;
		for (int nodeB : vertices[nodeA]) {
			if (edgeVisited[connectivity[nodeA][nodeB]])
				continue;
			else if (nodeVisited[nodeB] >= 0)
				backNodes[backNodesIndex++] = nodeB;
			else
				forwardNodes[forwardNodesIndex++] = nodeB;
		}
		int[][] result = new int[2][];
		// Then arrange backNodes with depth from small to large
		int[] backNodesKey = new int[backNodesIndex];
		for (int i = 0; i < backNodesIndex; i++)
			backNodesKey[i] = nodeVisited[backNodes[i]];
		result[0] = SortIntSet.sort(Arrays.copyOf(backNodes, backNodesIndex),
				backNodesKey);

		// arrange forward edge/nodes with label from small to large
		int[] forwardNodesKey = new int[forwardNodesIndex];
		int[] forwardEdgeKey = new int[forwardNodesIndex];
		for (int i = 0; i < forwardNodesIndex; i++) {
			int nodeID = forwardNodes[i];
			forwardNodesKey[i] = connectivity[nodeID][nodeID];
			forwardEdgeKey[i] = edgeLabel[connectivity[nodeA][nodeID]];
		}
		result[1] = SortIntSet.sort(
				Arrays.copyOf(forwardNodes, forwardNodesIndex), forwardEdgeKey,
				forwardNodesKey);
		return result;
	}

	/**
	 * Find the nodes with the minimum labels
	 * 
	 * @return
	 */
	private List<Integer> findMinNodes() {
		int minLabel = Integer.MAX_VALUE;
		List<Integer> result = new ArrayList<Integer>();

		for (int node = 0; node < vertices.length; node++) {
			int nodeLabel = connectivity[node][node];
			if (nodeLabel < minLabel) {
				result.clear();
				result.add(node);
				minLabel = nodeLabel;
			} else if (nodeLabel == minLabel) {
				result.add(node);
			} else
				continue;
		}
		return result;
	}

	private int compareEntry(int[] entryOne, int[] entryTwo) {
		for (int i = 0; i < entryOne.length; i++) {
			if (entryOne[i] < entryTwo[i])
				return -1;
			else if (entryOne[i] > entryTwo[i])
				return 1;
			else
				continue;
		}
		return 0;
	}
}
