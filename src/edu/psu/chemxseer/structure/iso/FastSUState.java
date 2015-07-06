package edu.psu.chemxseer.structure.iso;

import de.parmol.graph.Graph;

public class FastSUState {
	// Internal representation of graphS and graphB
	// verticesS and verticesB are linked-list representation of graphS and
	// graphB
	protected int[][] verticesS, verticesB;
	// connectivityS and connectivityB are matrix representation of graphS and
	// graphB
	// connectivityS[i][i] = label(vertex i)
	// connectivityS[i][j] = label(edge(i,j)) or -1 if no edge exists;
	protected int[][] connectivityS, connectivityB;
	// coreA and coreB are used to record the partial mapping of current state
	// coreS[i] = j if node i in small graph maps with node j in big graph,
	// coreB[j]=i
	// coreS[i] = -1(NULL_NODE) if no node is mapped
	protected int nodeCountS, nodeCountB;
	protected int[] coreS, coreB;
	// vertices not in current match, but connects to vertices of current match
	// 1. In current state match:-1 [coreS!=NULL_NODE]
	// 2. Adjacent to vertices of current match:
	// The depth in the SSR tree of the state in which the node added
	// 3. None of above: -1
	protected int[] connectS, connectB;

	// 2. these data member are used and duplicated for each state
	protected int currentDepth; // also the number of pairs in the current
								// mapping
	protected int[] newlyAddedS; // the pair of nodes that were added to the
									// current state
	protected int[] newlyAddedB;
	// saving generated candidate pair of vertices
	protected int[][] candidatesB;
	protected int[] candidateS;
	protected static int NULL_NODE = -1;
	protected static int NULL_EDGE = -1;

	/**
	 * Copy Constructor
	 * 
	 * @param state
	 */
	public FastSUState(FastSUState state) {
		this.verticesS = state.verticesS;
		this.verticesB = state.verticesB;
		this.connectivityS = state.connectivityS;
		this.connectivityB = state.connectivityB;
		this.nodeCountS = state.nodeCountS;
		this.nodeCountB = state.nodeCountB;
		this.coreS = state.coreS;
		this.coreB = state.coreB;
		this.connectS = state.connectS;
		this.connectB = state.connectB;
		this.currentDepth = state.currentDepth;
		this.newlyAddedS = state.newlyAddedS;
		this.newlyAddedB = state.newlyAddedB;
		this.candidatesB = state.candidatesB;
		this.candidateS = state.candidateS;
	}

	/**
	 * Construct a FastSUState from null
	 * 
	 * @param small
	 * @param big
	 */
	public FastSUState(Graph small, Graph big) {
		nodeCountS = small.getNodeCount();
		nodeCountB = big.getNodeCount();
		// Initial internal representation of graph small and graph big
		graphInitial(small, big);
		datamemberInitial();
		this.findInitialPair();
	}

	public FastSUState() {
	}

	protected void graphInitial(int[][] smallLabels, int[][] bigLabels) {
		this.verticesS = new int[nodeCountS][];
		this.connectivityS = new int[nodeCountS][nodeCountS];

		for (int i = 0; i < nodeCountS; i++) {
			for (int j = 0; j < nodeCountS; j++)
				this.connectivityS[i][j] = NULL_EDGE;
		}
		for (int i = 0; i < smallLabels.length; i++) {
			int nodeA = smallLabels[i][0];
			int nodeB = smallLabels[i][1];
			this.connectivityS[nodeA][nodeA] = smallLabels[i][2];
			this.connectivityS[nodeA][nodeB] = smallLabels[i][3];
			this.connectivityS[nodeB][nodeA] = smallLabels[i][3];
			this.connectivityS[nodeB][nodeB] = smallLabels[i][4];
		}
		for (int i = 0; i < nodeCountS; i++) {
			int count = 0;
			for (int j = 0; j < nodeCountS; j++)
				if (j == i)
					continue;
				else if (this.connectivityS[i][j] != NULL_EDGE)
					count++;
			this.verticesS[i] = new int[count];
			for (int j = 0, iter = 0; j < nodeCountS; j++)
				if (j == i)
					continue;
				else if (this.connectivityS[i][j] != NULL_EDGE)
					this.verticesS[i][iter++] = j;
		}

		this.verticesB = new int[nodeCountB][];
		this.connectivityB = new int[nodeCountB][nodeCountB];
		for (int i = 0; i < nodeCountB; i++) {
			for (int j = 0; j < nodeCountB; j++)
				this.connectivityB[i][j] = NULL_EDGE;
		}
		for (int i = 0; i < bigLabels.length; i++) {
			int nodeA = bigLabels[i][0];
			int nodeB = bigLabels[i][1];
			this.connectivityB[nodeA][nodeA] = bigLabels[i][2];
			this.connectivityB[nodeA][nodeB] = bigLabels[i][3];
			this.connectivityB[nodeB][nodeA] = bigLabels[i][3];
			this.connectivityB[nodeB][nodeB] = bigLabels[i][4];
		}
		for (int i = 0; i < nodeCountB; i++) {
			int count = 0;
			for (int j = 0; j < nodeCountB; j++)
				if (j == i)
					continue;
				else if (this.connectivityB[i][j] != NULL_EDGE)
					count++;
			this.verticesB[i] = new int[count];
			for (int j = 0, iter = 0; j < nodeCountB; j++)
				if (j == i)
					continue;
				else if (this.connectivityB[i][j] != NULL_EDGE)
					this.verticesB[i][iter++] = j;
		}
	}

	protected void graphInitial(int[][] smallLabels, Graph big) {
		this.verticesS = new int[nodeCountS][];
		this.connectivityS = new int[nodeCountS][nodeCountS];

		for (int i = 0; i < nodeCountS; i++) {
			for (int j = 0; j < nodeCountS; j++)
				this.connectivityS[i][j] = NULL_EDGE;
		}
		for (int i = 0; i < smallLabels.length; i++) {
			int nodeA = smallLabels[i][0];
			int nodeB = smallLabels[i][1];
			this.connectivityS[nodeA][nodeA] = smallLabels[i][2];
			this.connectivityS[nodeA][nodeB] = smallLabels[i][3];
			this.connectivityS[nodeB][nodeA] = smallLabels[i][3];
			this.connectivityS[nodeB][nodeB] = smallLabels[i][4];
		}
		for (int i = 0; i < nodeCountS; i++) {
			int count = 0;
			for (int j = 0; j < nodeCountS; j++)
				if (j == i)
					continue;
				else if (this.connectivityS[i][j] != NULL_EDGE)
					count++;
			this.verticesS[i] = new int[count];
			for (int j = 0, iter = 0; j < nodeCountS; j++)
				if (j == i)
					continue;
				else if (this.connectivityS[i][j] != NULL_EDGE)
					this.verticesS[i][iter++] = j;
		}

		this.verticesB = new int[nodeCountB][];
		this.connectivityB = new int[nodeCountB][nodeCountB];

		for (int i = 0; i < nodeCountB; i++) {
			verticesB[i] = new int[big.getDegree(i)];
			for (int temp = 0; temp < nodeCountB; temp++)
				this.connectivityB[i][temp] = NULL_EDGE;

			connectivityB[i][i] = big.getNodeLabel(i);
			for (int j = 0; j < verticesB[i].length; j++) {
				int edge = big.getNodeEdge(i, j);
				int anotherNode = big.getNodeB(edge);
				if (anotherNode == i)
					anotherNode = big.getNodeA(edge);
				connectivityB[i][anotherNode] = big.getEdgeLabel(edge);
				verticesB[i][j] = anotherNode;
			}
		}
	}

	protected void graphInitial(Graph small, Graph big) {
		this.verticesS = new int[nodeCountS][];
		this.connectivityS = new int[nodeCountS][nodeCountS];

		for (int i = 0; i < nodeCountS; i++) {
			verticesS[i] = new int[small.getDegree(i)];
			for (int temp = 0; temp < nodeCountS; temp++)
				this.connectivityS[i][temp] = NULL_EDGE;

			this.connectivityS[i][i] = small.getNodeLabel(i);
			for (int j = 0; j < verticesS[i].length; j++) {
				int edge = small.getNodeEdge(i, j);
				int anotherNode = small.getNodeB(edge);
				if (anotherNode == i)
					anotherNode = small.getNodeA(edge);
				connectivityS[i][anotherNode] = small.getEdgeLabel(edge);
				verticesS[i][j] = anotherNode;
			}
		}

		this.verticesB = new int[nodeCountB][];
		this.connectivityB = new int[nodeCountB][nodeCountB];

		for (int i = 0; i < nodeCountB; i++) {
			verticesB[i] = new int[big.getDegree(i)];
			for (int temp = 0; temp < nodeCountB; temp++)
				this.connectivityB[i][temp] = NULL_EDGE;

			connectivityB[i][i] = big.getNodeLabel(i);
			for (int j = 0; j < verticesB[i].length; j++) {
				int edge = big.getNodeEdge(i, j);
				int anotherNode = big.getNodeB(edge);
				if (anotherNode == i)
					anotherNode = big.getNodeA(edge);
				connectivityB[i][anotherNode] = big.getEdgeLabel(edge);
				verticesB[i][j] = anotherNode;
			}
		}
	}

	protected void datamemberInitial() {
		this.coreS = new int[nodeCountS];
		this.connectS = new int[nodeCountS];
		this.newlyAddedS = new int[nodeCountS];
		this.newlyAddedB = new int[nodeCountS];
		for (int i = 0; i < coreS.length; i++) {
			coreS[i] = NULL_NODE;
			connectS[i] = NULL_NODE;
			newlyAddedS[i] = NULL_NODE;
			newlyAddedB[i] = NULL_NODE;
		}
		this.coreB = new int[nodeCountB];
		this.connectB = new int[nodeCountB];
		for (int i = 0; i < coreB.length; i++) {
			coreB[i] = NULL_NODE;
			connectB[i] = NULL_NODE;
		}
		this.currentDepth = 0;
		this.candidateS = new int[nodeCountS];
		this.candidatesB = new int[nodeCountS][];
	}

	// Create a new state = {oldState + (smallVertex, bigVertex)}
	/**
	 * Expend current FastSU state
	 */
	public void expand(int smallVertex, int bigVertex) {
		newlyAddedS[currentDepth] = smallVertex;
		newlyAddedB[currentDepth] = bigVertex;
		// update coreS/B
		coreS[smallVertex] = bigVertex;
		coreB[bigVertex] = smallVertex;
		// update connectS/B
		for (int i = 0; i < verticesS[smallVertex].length; i++) {
			int adjI = verticesS[smallVertex][i];
			if (connectS[adjI] == NULL_NODE && coreS[adjI] == NULL_NODE)
				connectS[adjI] = currentDepth;
		}
		for (int i = 0; i < verticesB[bigVertex].length; i++) {
			int adjI = verticesB[bigVertex][i];
			if (connectB[adjI] == NULL_NODE && coreB[adjI] == NULL_NODE)
				connectB[adjI] = currentDepth;
		}
		++currentDepth;
		if (currentDepth < nodeCountS)
			findMatchableCandidate();
	}

	/**
	 * This function find all candidate pairs in which both vertices connected
	 * with current match
	 * 
	 * @return
	 */
	protected boolean findMatchableCandidate() {
		// find the vertex of graphS that is unmatched
		// but connected to vertices of current state with minimum depth
		// notation
		int minimumLayer = Integer.MAX_VALUE;
		for (int i = 0; i < nodeCountS; i++) {
			if (coreS[i] == NULL_NODE && connectS[i] != NULL_NODE) {
				if (connectS[i] < minimumLayer) {
					minimumLayer = connectS[i];
					candidateS[currentDepth] = i;
				}
			}
		}

		int end = 0;
		int allCandidates[] = new int[verticesB.length - currentDepth];
		// find possible matches: requirement label(candidateS)=label(i)
		for (int i = 0; i < verticesB.length; i++) {
			if (connectB[i] == NULL_NODE || coreB[i] != NULL_NODE)
				continue;
			else if (connectivityS[candidateS[currentDepth]][candidateS[currentDepth]] != connectivityB[i][i])
				continue;
			else if (!feasibleTest(i))
				continue;
			else {
				allCandidates[end] = i;
				end++;
			}
		}
		this.candidatesB[currentDepth] = new int[end];
		for (int i = 0; i < end; i++)
			candidatesB[currentDepth][i] = allCandidates[i];
		if (end != 0)
			return true;
		else
			return false;
	}

	/**
	 * Find the first pair of matchable pair
	 * 
	 * @author dayuyuan
	 */
	protected boolean findInitialPair() {
		// but connected to vertices of current state
		candidateS[0] = (2 * nodeCountS - 1) / 2;
		int degreeS = verticesS[candidateS[0]].length;
		int[] allCandidates = new int[verticesB.length];
		int end = 0;
		// find possible matches:
		for (int i = 0; i < verticesB.length; i++) {
			// label(candidateS)=label(i)
			if (connectivityS[candidateS[0]][candidateS[0]] != connectivityB[i][i])
				continue;
			// degree S<=B
			else if (degreeS > verticesB[i].length)
				continue;
			else {
				allCandidates[end] = i;
				end++;
			}
		}
		this.candidatesB[0] = new int[end];
		for (int i = 0; i < end; i++)
			candidatesB[0][i] = allCandidates[i];
		if (end != 0)
			return true;
		else
			return false;
	}

	/**
	 * @author dayuyuan feasible test: 3 rules Two vertices (adjS, adjB) which
	 *         are adjacent to candidateS and candidateB 1. If adjS & adjB are
	 *         in current partial mapping, test branch (adjS, candidateS),
	 *         (adjB, candidateB) 2. Num(verticesConnectedS) <=
	 *         Num(verticesCOnnectedB) 3. Num(s) - Num(verticesConnectedS) <=
	 *         Num(b) - Num(verticesCOnnectedB) the third rule is only for
	 *         edge-reduced subgraph-isomorphism test
	 * @return
	 */
	private boolean feasibleTest(int candidateB) {
		if (verticesS[candidateS[currentDepth]].length > verticesB[candidateB].length)
			return false;
		int T_S = 0;
		int T_B = 0;
		for (int i = 0; i < verticesS[candidateS[currentDepth]].length; i++) {
			int adjS = verticesS[candidateS[currentDepth]][i];
			if (coreS[adjS] == NULL_NODE) {
				if (connectS[adjS] != NULL_NODE) // connected adjacent vertex
					T_S++;
			} else {
				// rule 1:
				// found a adjS that are in current partial mapping
				int adjB = coreS[adjS];
				if (connectivityS[adjS][candidateS[currentDepth]] != connectivityB[adjB][candidateB])
					return false;
			}
		}
		// rule 2:
		for (int i = 0; i < verticesB[candidateB].length; i++) {
			int adjB = verticesB[candidateB][i];
			if (coreB[adjB] == NULL_NODE && connectB[adjB] != NULL_NODE)
				T_B++;
		}
		if (T_S > T_B)
			return false;

		return true;
	}

	/**
	 * @author dayuyuan feasible test: 3 rules when candidate is generated from
	 *         unconnected set Two vertices (adjS, adjB) which are adjacent to
	 *         candidateS and candidateB 1. If adjS & adjB are in current
	 *         partial mapping, test branch (adjS, candidateS), (adjB,
	 *         candidateB) 2. Num(verticesConnectedS) <= Num(verticesCOnnectedB)
	 *         3. Num(s) - Num(verticesConnectedS) <= Num(b) -
	 *         Num(verticesCOnnectedB) the third rule is only for edge-reduced
	 *         subgraph-isomorphism test
	 * @return
	 */
	/*
	 * private boolean feasibleTest2(int candidateB){ // rule 1: No connecting
	 * vertices in partial mapping, skip // rule 2:
	 * Num(verticesConnectedToCurrentState) = 0 , only need to find out // the
	 * number of vertices connected to candidate, guarantee that non of them //
	 * in connected set int T_S = verticesS[candidateS[currentDepth]].length;
	 * int T_B = verticesB[candidateB].length; if(T_S > T_B) return false; else
	 * return true; }
	 */
	/**
	 * @author dayuyuan State of VF algorithm grows in a depth first order When
	 *         some state is dead, we need to backTrack Recover the modification
	 *         of common data member
	 */
	public void backTrack() {
		--currentDepth;
		coreS[newlyAddedS[currentDepth]] = NULL_NODE;
		coreB[newlyAddedB[currentDepth]] = NULL_NODE;

		for (int i = 0; i < verticesS[newlyAddedS[currentDepth]].length; i++) {
			int j = verticesS[newlyAddedS[currentDepth]][i];
			if (connectS[j] == currentDepth)
				connectS[j] = NULL_NODE;
		}
		for (int i = 0; i < verticesB[newlyAddedB[currentDepth]].length; i++) {
			int j = verticesB[newlyAddedB[currentDepth]][i];
			if (connectB[j] == currentDepth)
				connectB[j] = NULL_NODE;
		}
	}

	/**
	 * Test success or Not
	 * 
	 * @return
	 */
	public boolean isGoal() {
		if (nodeCountS == currentDepth)
			return true;
		else
			return false;
	}

	/**
	 * Test further growing is possible
	 * 
	 * @return
	 */
	public boolean isDead() {
		if (this.candidatesB[currentDepth].length == 0)
			return true;
		else
			return false;
	}

	public int[] getCandidatesB() {
		return candidatesB[currentDepth];
	}

	public int getCandidateS() {
		if (currentDepth == this.nodeCountS)
			return -1;
		else
			return candidateS[currentDepth];
	}

	public int[] getMap() {
		return this.coreS;
	}

	public int getDepth() {
		return currentDepth;
	}

	public int getNodeCountS() {
		return nodeCountS;
	}

	public int getNodeCountB() {
		return nodeCountB;
	}

	public void replaceMappingBase(int[] newMap, int validLength) {
		for (int i = 0; i < coreB.length; i++)
			coreB[i] = -1;
		for (int i = 0; i < validLength; i++) {
			coreS[i] = newMap[i];
			coreB[newMap[i]] = i;
		}
	}

	public void reOrderBaseGraphB() {
		// Reorder graphB
		int order[] = new int[verticesB.length];
		int invalidOrder = this.verticesS.length;
		for (int i = 0; i < order.length; i++)
			if (coreB[i] == -1)
				order[i] = invalidOrder++;
			else
				order[i] = coreB[i];

		int[][] newVerticesB = new int[verticesB.length][];
		int[][] newConnectivityB = new int[verticesB.length][verticesB.length];

		for (int nodeB = 0; nodeB < nodeCountB; nodeB++) {
			int reorderB = order[nodeB];
			// System.out.println(reorderB + ", " + nodeB);
			newVerticesB[reorderB] = new int[verticesB[nodeB].length];
			for (int t = 0; t < newConnectivityB.length; t++)
				newConnectivityB[reorderB][t] = -1;
			newConnectivityB[reorderB][reorderB] = connectivityB[nodeB][nodeB];
			for (int i = 0; i < verticesB[nodeB].length; i++) {
				int adjNodeB = verticesB[nodeB][i];
				newConnectivityB[reorderB][order[adjNodeB]] = connectivityB[nodeB][adjNodeB];
				newVerticesB[reorderB][i] = order[adjNodeB];
			}
		}
		this.verticesB = newVerticesB;
		this.connectivityB = newConnectivityB;
	}

	public int[][] getSmallLinkList() {
		return this.verticesS;
	}

	public int[][] getBigLinkList() {
		return this.verticesB;
	}

	public int[][] getSmallMatrix() {
		return this.connectivityS;
	}

	public int[][] getBigMatrix() {
		return this.connectivityB;
	}

	public void setBigMatrix(int[][] connectivityBackUp) {
		this.connectivityB = connectivityBackUp.clone();
	}

	public void setBigLinkList(int[][] verticesBackUp) {
		this.verticesB = verticesBackUp.clone();
	}

	public int getEdgeCountS() {
		int count = 0;
		for (int i = 0; i < this.nodeCountS; i++)
			count += this.verticesS[i].length;
		return count / 2;
	}

	public int getEdgeCountB() {
		int count = 0;
		for (int i = 0; i < this.nodeCountB; i++)
			count += this.verticesB[i].length;
		return count / 2;
	}
}
