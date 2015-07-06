package edu.psu.chemxseer.structure.iso;

import java.util.ArrayList;

import de.parmol.graph.Graph;

/**
 * A implementation of FastSUState, where the smaller graph can be expanded to a
 * middle graph or shrink to a smaller graph
 * 
 * @author dayuyuan
 * 
 */
public class FastSUStateExpandable extends FastSUState {
	protected int edgeNumS;
	protected int edgeNumB;
	protected int minDepth;

	public FastSUStateExpandable() {
		// dummy
	}

	public FastSUStateExpandable(Graph small, Graph big) {
		super();// dummy
		// Initial internal representation of graph small and graph big
		graphInitialization(small, big);
		datamemberInitial();
		super.findInitialPair();
	}

	public FastSUStateExpandable(int[][] GVCodes, Graph big) {
		super(); // dummy
		graphInitialization(GVCodes, big);
		datamemberInitial();
		super.findInitialPair();
	}

	/**
	 * Construct a FastSU State, sharing the memory storing small graphs
	 * 
	 * @param smallConnectivity
	 * @param smallVerticies
	 * @param big
	 */
	public FastSUStateExpandable(int[][] smallConnectivity,
			int[][] smallVertices, Graph big) {
		super();// dummy
		// Initial internal representation of graph small and graph big
		graphInitial(smallConnectivity, smallVertices, big);
		datamemberInitial();
		super.findInitialPair();
	}

	public FastSUStateExpandable(FastSUStateExpandable state) {
		super(state);
		this.edgeNumB = state.edgeNumB;
		this.edgeNumS = state.edgeNumS;
		this.minDepth = state.minDepth;
	}

	/**
	 * Initialize internal representation of the small graph and the big graph
	 * The internal big graph representation can be shared among all
	 * FastSUStateExpandable
	 * 
	 * @param small
	 * @param big
	 */
	protected void graphInitialization(Graph small, Graph big) {
		nodeCountS = small.getNodeCount();
		nodeCountB = big.getNodeCount();
		edgeNumS = small.getEdgeCount();
		edgeNumB = big.getEdgeCount();
		// First initialize the big graph
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
		// Then initialize the small graph as big as the big graph
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
	}

	/**
	 * Initialize internal representation of the small graph and the big graph
	 * The internal big graph representation can be shared among all
	 * FastSUStateExpandable
	 * 
	 * @param GVCodes
	 *            []
	 * @param big
	 */
	protected void graphInitialization(int[][] GVCodes, Graph big) {
		nodeCountS = GVCodes[0][0];
		for (int i = 0; i < GVCodes.length; i++) {
			if (GVCodes[i][1] > nodeCountS)
				nodeCountS = GVCodes[i][1];
			if (GVCodes[i][0] > nodeCountS)
				nodeCountS = GVCodes[i][0];
		}
		nodeCountS++;

		nodeCountB = big.getNodeCount();
		edgeNumS = GVCodes.length;
		edgeNumB = big.getEdgeCount();
		// First initialize the big graph
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
		// Then initialize the small graph as big as the big graph
		this.verticesS = new int[nodeCountS][];
		this.connectivityS = new int[nodeCountS][nodeCountS];

		for (int i = 0; i < nodeCountS; i++)
			for (int j = 0; j < nodeCountS; j++)
				this.connectivityS[i][j] = NULL_EDGE;

		for (int i = 0; i < GVCodes.length; i++) {
			int nodeA = GVCodes[i][0];
			int nodeB = GVCodes[i][1];
			this.connectivityS[nodeA][nodeA] = GVCodes[i][2];
			this.connectivityS[nodeB][nodeB] = GVCodes[i][4];
			this.connectivityS[nodeA][nodeB] = this.connectivityS[nodeB][nodeA] = GVCodes[i][3];
		}

		for (int i = 0; i < nodeCountS; i++) {
			int count = 0;
			for (int j = 0; j < nodeCountS; j++) {
				if (j == i)
					continue;
				if (this.connectivityS[i][j] != NULL_EDGE)
					count++;
			}
			this.verticesS[i] = new int[count];
			int index = 0;
			for (int j = 0; j < nodeCountS; j++) {
				if (j == i)
					continue;
				if (this.connectivityS[i][j] != NULL_EDGE)
					this.verticesS[i][index++] = j;
			}
		}
	}

	protected void graphInitial(int[][] smallConnectivity,
			int[][] smallVertices, Graph big) {
		this.nodeCountS = smallConnectivity.length;
		this.nodeCountB = big.getNodeCount();
		this.verticesS = smallVertices;
		this.connectivityS = smallConnectivity;

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
		this.edgeNumB = big.getEdgeCount();
		for (int i = 0; i < smallVertices.length; i++)
			this.edgeNumS += smallVertices[i].length;
		edgeNumS = edgeNumS / 2;

	}

	/**
	 * All those internal data member are large enough to be shared among all
	 * state
	 */
	@Override
	protected void datamemberInitial() {

		// sharable status
		this.coreS = new int[nodeCountB];
		this.connectS = new int[nodeCountB];
		this.newlyAddedS = new int[nodeCountB];
		this.newlyAddedB = new int[nodeCountB];
		this.coreB = new int[nodeCountB];
		this.connectB = new int[nodeCountB];
		for (int i = 0; i < coreB.length; i++) {
			coreB[i] = NULL_NODE;
			connectB[i] = NULL_NODE;
			newlyAddedS[i] = NULL_NODE;
			newlyAddedB[i] = NULL_NODE;
			coreS[i] = NULL_NODE;
			connectS[i] = NULL_NODE;
		}
		this.currentDepth = 0;
		this.candidateS = new int[nodeCountB];
		this.candidatesB = new int[nodeCountB][];
		this.minDepth = 0;
	}

	/**
	 * Expand the small graph to a middle graph
	 * 
	 * @param extension
	 */
	public FastSUStateExpandable expandToNewState(int[][] extension) {
		FastSUStateExpandable newState = new FastSUStateExpandable();
		boolean success = newState.graphExtension(this, extension);
		if (!success)
			return null;
		newState.dataMemberInitialization(this);
		return newState;
	}

	public FastSUStateExpandable expandToNewState(Graph middleGraph) {
		FastSUStateExpandable newState = new FastSUStateExpandable();
		boolean success = newState.graphExtension(this, middleGraph);
		if (!success)
			return null;
		newState.dataMemberInitialization(this);
		return newState;
	}

	protected boolean graphExtension(FastSUStateExpandable oriSTATE,
			int[][] extension) {
		// share the big graph
		this.verticesB = oriSTATE.verticesB;
		this.connectivityB = oriSTATE.connectivityB;
		// Do extending the smaller graph
		this.nodeCountS = oriSTATE.nodeCountS;
		this.nodeCountB = oriSTATE.nodeCountB;
		this.edgeNumS = oriSTATE.edgeNumS;
		this.edgeNumB = oriSTATE.edgeNumB;

		// First Step: find the maximum node index in the extension
		int maxNodeIndex = oriSTATE.nodeCountS - 1;
		for (int i = 0; i < extension.length; i++) {
			int nodeA = extension[i][0];
			int nodeB = extension[i][1];
			if (nodeA > maxNodeIndex)
				maxNodeIndex = nodeA;
			if (nodeB > maxNodeIndex)
				maxNodeIndex = nodeB;
		}
		int edgeNum = extension.length + oriSTATE.edgeNumS;
		if (edgeNum > oriSTATE.edgeNumB || maxNodeIndex >= oriSTATE.nodeCountB)
			return false;
		// Second step: create matrix and linkedList for small graph
		this.verticesS = new int[maxNodeIndex + 1][];
		this.connectivityS = new int[maxNodeIndex + 1][maxNodeIndex + 1];
		// Third step: copy and extend connectivityS
		for (int i = 0; i < nodeCountS; i++) {
			for (int j = 0; j < nodeCountS; j++)
				this.connectivityS[i][j] = oriSTATE.connectivityS[i][j];
			for (int j = nodeCountS; j < connectivityS[i].length; j++)
				this.connectivityS[i][j] = -1;
		}
		for (int i = nodeCountS; i < connectivityS.length; i++)
			for (int j = 0; j < connectivityS[i].length; j++)
				this.connectivityS[i][j] = -1;

		for (int i = 0; i < extension.length; i++) {
			int nodeA = extension[i][0];
			int nodeB = extension[i][1];
			// extend edge label
			this.connectivityS[nodeA][nodeB] = extension[i][3];
			this.connectivityS[nodeB][nodeA] = extension[i][3];
			// extend node label
			if (nodeA >= nodeCountS)
				connectivityS[nodeA][nodeA] = extension[i][2];
			if (nodeB >= nodeCountS)
				connectivityS[nodeB][nodeB] = extension[i][4];
		}
		// Fourth step: initialize linkedList based on matrix
		ArrayList<Integer> adjNodes = new ArrayList<Integer>();
		for (int i = 0; i < connectivityS.length; i++) {
			// find adjNodes for vertex i
			adjNodes.clear();
			for (int j = 0; j < connectivityS[i].length; j++) {
				if (i == j || connectivityS[i][j] == -1)
					continue;
				else
					adjNodes.add(j);
			}
			this.verticesS[i] = new int[adjNodes.size()];
			for (int t = 0; t < verticesS[i].length; t++)
				this.verticesS[i][t] = adjNodes.get(t);
		}
		// Fifth step: update edgeNum, nodeNum
		this.nodeCountS = maxNodeIndex + 1;
		this.edgeNumS += extension.length;
		return true;
	}

	protected boolean graphExtension(FastSUStateExpandable oriSTATE,
			Graph middleGraph) {
		// share the big graph
		this.verticesB = oriSTATE.verticesB;
		this.connectivityB = oriSTATE.connectivityB;
		// Do extending the smaller graph
		this.nodeCountS = middleGraph.getNodeCount();
		this.nodeCountB = oriSTATE.nodeCountB;
		this.edgeNumS = oriSTATE.edgeNumS;
		this.edgeNumB = middleGraph.getEdgeCount();

		// First Step: find the maximum node index in the extension

		if (edgeNumB > oriSTATE.edgeNumB || nodeCountS > oriSTATE.nodeCountB)
			return false;
		// Second step: create matrix and linkedList for small graph
		this.verticesS = new int[nodeCountS][];
		this.connectivityS = new int[nodeCountS][nodeCountS];

		for (int i = 0; i < nodeCountS; i++) {
			verticesS[i] = new int[middleGraph.getDegree(i)];
			for (int temp = 0; temp < nodeCountS; temp++)
				this.connectivityS[i][temp] = NULL_EDGE;

			this.connectivityS[i][i] = middleGraph.getNodeLabel(i);
			for (int j = 0; j < verticesS[i].length; j++) {
				int edge = middleGraph.getNodeEdge(i, j);
				int anotherNode = middleGraph.getNodeB(edge);
				if (anotherNode == i)
					anotherNode = middleGraph.getNodeA(edge);
				connectivityS[i][anotherNode] = middleGraph.getEdgeLabel(edge);
				verticesS[i][j] = anotherNode;
			}
		}
		return true;
	}

	protected void dataMemberInitialization(FastSUStateExpandable oriSTATE) {

		// share all records
		this.coreS = oriSTATE.coreS;
		this.connectS = oriSTATE.connectS;
		this.newlyAddedS = oriSTATE.newlyAddedS;
		this.newlyAddedB = oriSTATE.newlyAddedB;
		this.coreB = oriSTATE.coreB;
		this.connectB = oriSTATE.connectB;
		this.candidateS = oriSTATE.candidateS;
		this.candidatesB = oriSTATE.candidatesB;
		this.currentDepth = oriSTATE.nodeCountS;

		updateConnects();

		this.minDepth = oriSTATE.nodeCountS;
	}

	/**
	 * update connectS and connectB, based on current small graph and big graph
	 */
	private void updateConnects() {
		for (int i = 0; i < connectS.length; i++) {
			this.connectS[i] = NULL_NODE;
			this.connectB[i] = NULL_NODE;
		}
		for (int i = 0; i < verticesS.length; i++) {
			if (coreS[i] == NULL_NODE)
				continue;
			for (int j = 0; j < verticesS[i].length; j++) {
				if (coreS[verticesS[i][j]] != NULL_NODE)
					continue;
				else
					connectS[verticesS[i][j]] = this.currentDepth - 1;
			}
		}
		for (int i = 0; i < verticesB.length; i++) {
			if (coreB[i] == NULL_NODE)
				continue;
			for (int j = 0; j < verticesB[i].length; j++) {
				if (coreB[verticesB[i][j]] != NULL_NODE)
					continue;
				else
					connectB[verticesB[i][j]] = this.currentDepth - 1;
			}
		}
	}

	/**
	 * Replace the mapping with the newCoreS, with "validLength" Return -1: the
	 * replaced mapping is not valid Return 0: the replaced mapping is perfect
	 * mapping. [small = big] Return 1: the replaced mapping is partial mapping.
	 * Return 2: the replaced mapping is full mapping. [All small graphs nodes
	 * are mapped to nodes in the big graph]
	 * 
	 * @param newCoreS
	 * @param validLength
	 * @return
	 */
	public int replaceMapping(int[] newCoreS, int validLength) {
		for (int i = 0; i < coreB.length; i++) {
			coreS[i] = -1;
			coreB[i] = -1;
		}
		for (int i = 0; i <= validLength; i++) {
			coreS[i] = newCoreS[i];
			coreB[newCoreS[i]] = i;
		}
		// Test whether this new mapping is valid
		int isnewMatchValid = validateNewMapping();
		if (isnewMatchValid == -1) // not a valid mapping
			return -1; // this may happen
		else if (isnewMatchValid == 0)
			return 0; // perfect full mapping
		else if (isnewMatchValid == 2)
			return 2; // subgraph full mapping
		updateConnects();
		if (currentDepth < nodeCountS)
			findMatchableCandidate();
		return 1;
	}

	/**
	 * Given the current core mapping between small graph and large graph test
	 * whether it is correct Return -1 if not valid Return 0 if is a full
	 * mapping Return 1 if a partial valid mapping
	 * 
	 * @return
	 */
	private int validateNewMapping() {
		for (int i = 0; i < verticesS.length; i++) {
			int vertexSmall = i;
			if (this.coreS[i] == NULL_NODE)
				continue;
			int vertexBig = coreS[i];
			if (connectivityS[vertexSmall][vertexSmall] != connectivityB[vertexBig][vertexBig])
				return -1;
			// Edge label test
			for (int j = 0; j < verticesS[i].length; j++) {
				int adjNode = verticesS[i][j];
				if (this.coreS[adjNode] == NULL_NODE)
					continue;
				if (connectivityS[vertexSmall][adjNode] != connectivityB[vertexBig][coreS[adjNode]])
					return -1;
			}
		}
		int mappedNum = 0;
		for (int i = 0; i < verticesS.length; i++)
			if (this.coreS[i] != NULL_NODE)
				mappedNum++;
		if (this.edgeNumS == this.edgeNumB
				&& this.nodeCountS == this.nodeCountB
				&& mappedNum == this.nodeCountS)
			return 0; // perfect match, an isomorphism test is found
		else if (mappedNum == this.nodeCountS)
			return 2; // it is already a complete match, but only subgraph
						// isomorphism
		return 1;
	}

	@Override
	public int getEdgeCountS() {
		return this.edgeNumS;
	}

	@Override
	public int getEdgeCountB() {
		return this.edgeNumB;
	}

	@Override
	public boolean isDead() {
		if (this.candidatesB[currentDepth].length == 0)
			return true;
		else
			return false;
	}

}
