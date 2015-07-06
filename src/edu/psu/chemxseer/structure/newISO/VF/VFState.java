package edu.psu.chemxseer.structure.newISO.VF;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

public class VFState {
	static public int NULL_NODE = -1;
	// 1. Data members that are shared among all VFState
	protected ISOGraph graphS;
	protected ISOGraph graphB;
	// The mapping between graphS and graphB
	// coreS[i] = j if node i in graphS maps with node j in graphB, coreB[j]=i
	// coreS[i] = -1(NULL_NODE) if no node is mapped
	protected int[] coreS, coreB;
	// Describe the membership of vertices
	// a. In current state match:
	// b. Adjacent to vertices in current match:
	// The depth in the SSR tree of the state in which the node entered the
	// corresponding set
	// either set a or set b
	// c. None of above: -1
	protected int[] connectS, connectB;
	// the order of search
	protected int[] searchOrder;
	protected int connectSNum, connectBNum;

	// 2. Data members that are used and duplicated for each state
	protected int currentDepth; // also the number of pairs in the current
								// mapping
	protected int newlyAddedS; // the pair of nodes that were added to the
								// current state
	protected int newlyAddedB;

	public VFState() {

	}

	public VFState(ISOGraph graphSmall, ISOGraph graphBig) {
		// Initialize common shared data member
		this.graphS = graphSmall;
		this.graphB = graphBig;
		coreS = new int[graphS.getNodeCount()];
		coreB = new int[graphB.getNodeCount()];
		connectS = new int[coreS.length];
		connectB = new int[coreB.length];
		for (int i = 0; i < coreS.length; i++) {
			coreS[i] = NULL_NODE;
			connectS[i] = NULL_NODE;
		}
		for (int i = 0; i < coreB.length; i++) {
			coreB[i] = NULL_NODE;
			connectB[i] = NULL_NODE;
		}
		// Pre-compute the search order of VF algorithm
		searchOrder = new int[graphS.getNodeCount()];
		findSearchOrder();

		// Initialize state owned data member
		currentDepth = 0;
		newlyAddedS = NULL_NODE;
		newlyAddedB = NULL_NODE;
		connectSNum = 0;
		connectBNum = 0;
	}

	public VFState(VFState prestate, int candidateS, int candidateB) {
		// Copy common shared data member
		graphS = prestate.graphS;
		graphB = prestate.graphB;
		coreS = prestate.coreS;
		coreB = prestate.coreB;
		connectS = prestate.connectS;
		connectB = prestate.connectB;
		searchOrder = prestate.searchOrder;
		// Initialize state owned data member
		if (currentDepth == graphS.getNodeCount())
			return;
		currentDepth = prestate.currentDepth + 1;
		newlyAddedS = candidateS;
		newlyAddedB = candidateB;
		// Update common shared data member after adding the new
		// pair(candidateS, candidateB)
		coreS[newlyAddedS] = newlyAddedB;
		coreB[newlyAddedB] = newlyAddedS;
		connectSNum--;
		connectBNum--;
		connectS[newlyAddedS] = currentDepth;
		connectS[newlyAddedB] = currentDepth;
		for (int i = 0; i < graphS.getDegree(newlyAddedS); i++) {
			int adjNodeS = graphS.getAdjacentNode(newlyAddedS, i);
			if (coreS[adjNodeS] == NULL_NODE && connectS[adjNodeS] == NULL_NODE) {
				connectS[adjNodeS] = currentDepth;
				connectSNum++;
			}
		}
		for (int j = 0; j < graphB.getDegree(newlyAddedB); j++) {
			int adjNodeB = graphS.getAdjacentNode(newlyAddedB, j);
			if (coreB[adjNodeB] == NULL_NODE && connectB[adjNodeB] == NULL_NODE) {
				connectB[adjNodeB] = currentDepth;
				connectBNum++;
			}
		}
	}

	public boolean restore() {
		if (currentDepth == 0)
			return false;
		connectSNum++;
		connectBNum++;
		for (int i = 0; i < graphS.getDegree(newlyAddedS); i++) {
			int adjNodeS = graphS.getAdjacentNode(newlyAddedS, i);
			if (coreS[adjNodeS] == NULL_NODE
					&& connectS[adjNodeS] == currentDepth) {
				connectS[adjNodeS] = NULL_NODE;
				connectSNum--;
			}
		}
		for (int j = 0; j < graphB.getDegree(newlyAddedB); j++) {
			int adjNodeB = graphB.getAdjacentNode(newlyAddedB, j);
			if (coreB[adjNodeB] == NULL_NODE
					&& connectB[adjNodeB] == currentDepth) {
				connectB[adjNodeB] = NULL_NODE;
				connectBNum--;
			}
		}
		coreS[newlyAddedS] = NULL_NODE;
		coreB[newlyAddedB] = NULL_NODE;

		currentDepth--;
		connectS[newlyAddedS] = currentDepth;
		connectB[newlyAddedB] = currentDepth;
		return true;
	}

	/**
	 * Generate the search order of subgrpah isomorphism test The first step of
	 * computation of P(s), finding candidateS
	 * 
	 * @param order
	 * @return
	 */
	protected boolean findSearchOrder() {
		// We assume that the input graph is connected and undirected.
		// The only heuristic determining search order is by label
		// First Step: find the minimum label
		int minLabel = Integer.MAX_VALUE;
		int minLabelIndex = 0;
		PriorityQueue<NodeInfoVF> mHeap = new PriorityQueue<NodeInfoVF>();
		boolean[] visited = new boolean[graphS.getNodeCount()];
		int orderSize = 0;
		// Find the vertex with the minimum node label
		for (int i = 0; i < graphS.getNodeCount(); i++) {
			visited[i] = false;
			if (graphS.getNodeLabel(i) < minLabel) {
				minLabel = graphS.getNodeLabel(i);
				minLabelIndex = i;
			}
		}
		visited[minLabelIndex] = true;
		this.searchOrder[0] = minLabelIndex;
		orderSize++;
		// Add all nodes adjacent to the first node into the minHeap
		for (int i = 0; i < graphS.getDegree(minLabelIndex); i++) {
			int node = graphS.getAdjacentNode(minLabelIndex, i);
			visited[node] = true;
			mHeap.offer(new NodeInfoVF(node, graphS.getNodeLabel(node)));
		}
		// keep on findings the next vertices
		while (mHeap.isEmpty() != true) {
			// get the visited nodes with minimum label
			int nodeIndex = mHeap.remove().getNodeIndex();
			searchOrder[orderSize] = nodeIndex;
			orderSize++;
			// add all nodes adjacent to the newly added candidateS into the
			// minHeap
			for (int i = 0; i < graphS.getDegree(nodeIndex); i++) {
				int adjNode = graphS.getAdjacentNode(nodeIndex, i);
				if (visited[adjNode])
					continue; // avoid redundancy
				visited[adjNode] = true;
				mHeap.offer(new NodeInfoVF(adjNode, graphS
						.getNodeLabel(adjNode)));
			}
		}
		return true;
	}

	public int getCandidateS() {
		if (currentDepth == graphS.getNodeCount())
			return NULL_NODE;
		return this.searchOrder[currentDepth];
	}

	/**
	 * The second step of computation of P(s) With a fixed candidateS =
	 * searchorder[currentDepth] Find the set of candidateB
	 * 
	 * @return
	 */
	public int[] getCandidateB() {
		if (currentDepth == graphS.getNodeCount())
			return null;// can not find a next availabel pair

		int candidateS = searchOrder[currentDepth];
		int candidateSLabel = graphS.getNodeLabel(candidateS);
		List<Integer> candidateB = new LinkedList<Integer>();
		// the seed state: candidateB can be any node with the same label of
		// candidateS
		if (currentDepth == 0) {
			for (int j = 0; j < graphB.getNodeCount(); j++) {
				if (graphB.getNodeLabel(j) == candidateSLabel)
					candidateB.add(j);
			}
		}
		// not the seed state
		// Because of the assumption of connected graph
		// candidateS must connected with a node, adjNodeS, in current matching
		// candidateB must connected with the node, adjNodeB, which is the
		// matching node of
		// adjNodeS in graphB.
		else {
			int adjNodeB = NULL_NODE;
			for (int i = 0; i < graphS.getDegree(candidateS); i++) {
				int adjNodeS = graphS.getAdjacentNode(candidateS, i);
				// adjNodeS is in current matching
				if (this.coreS[adjNodeS] != NULL_NODE) {
					// find corresponding adjNodeB
					adjNodeB = this.coreS[adjNodeS];
					break;
				}
			}
			// candidateB should be adjacent to adjNodeB
			for (int i = 0; i < graphB.getDegree(adjNodeB); i++) {
				int oneCandidateB = graphB.getAdjacentNode(adjNodeB, i);
				if (isFeasible(candidateS, oneCandidateB))
					candidateB.add(oneCandidateB);
				else
					continue;
			}
		}
		if (candidateB.isEmpty())
			return null;
		else {
			int[] results = new int[candidateB.size()];
			int i = 0;
			for (Iterator<Integer> it = candidateB.iterator(); it.hasNext();)
				results[i++] = it.next();
			return results;
		}
	}

	/*
	 * FeasibleTest: Two vertices (node1, node2) 1. If adjNode1 & adjNode2 are
	 * in current partial match, test edge (node1, adjNode1), (node2, adjNode2)
	 * 2. Num(verticesConnectedS) <= Num(verticesCOnnectedB) 3. Num(s) -
	 * Num(verticesConnectedS) <= Num(b) - Num(verticesCOnnectedB) the third
	 * rule is only for edge-reduced subgraph-isomorphism test
	 */
	/**
	 * Test if node1 is matchable to node2 before adding node1 and
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	public boolean isGoal() {
		if (currentDepth == graphS.getNodeCount())
			return true;
		else
			return false;
	}

	private boolean isFeasible(int node1, int node2) {
		if (graphS.getNodeLabel(node1) != graphB.getNodeLabel(node2))
			return false;
		if (graphS.getDegree(node1) > graphB.getDegree(node2))
			return false;
		// rule 1
		for (int i = 0; i < graphS.getDegree(node1); i++) {
			int adjNode1 = graphS.getAdjacentNode(node1, i);
			if (coreS[adjNode1] == NULL_NODE)
				continue;
			else {
				int adjNode2 = coreS[adjNode1];
				if (graphS.getEdgeLabel(node1, adjNode1) != graphB
						.getEdgeLabel(node2, adjNode2))
					return false;
			}
		}
		// rule 2
		if (connectSNum > connectBNum)
			return false;
		return true;
	}
}
