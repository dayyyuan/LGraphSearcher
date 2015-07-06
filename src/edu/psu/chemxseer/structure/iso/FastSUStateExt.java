package edu.psu.chemxseer.structure.iso;

import de.parmol.graph.Graph;

/**
 * FastSUStateExt is a Extension of FastSUState It support change of the smaller
 * graph Given a FastSUStateExt isomorphism between the small graph and the
 * large graph In addition, given a middle graph in extension representation of
 * the smaller graph The FastSUStateExt can change to an isomorphism between the
 * middle graph and the large graph
 * 
 * @author dayuyuan
 * 
 */
public class FastSUStateExt extends FastSUState {
	protected int edgeNumS;
	protected int edgeNumB;
	protected int minDepth;

	public FastSUStateExt(Graph small, Graph big) {
		super();
		// Initial internal representation of graph small and graph big
		graphInitial(small, big, big.getNodeCount());
		datamemberInitial(big.getNodeCount());
		super.findInitialPair();
	}

	protected void graphInitial(Graph small, Graph big, int looseSize) {
		nodeCountS = small.getNodeCount();
		nodeCountB = big.getNodeCount();
		edgeNumS = small.getEdgeCount();
		edgeNumB = small.getEdgeCount();

		this.verticesS = new int[looseSize][];
		this.connectivityS = new int[looseSize][looseSize];

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
		for (int i = nodeCountS; i < looseSize; i++)
			for (int j = 0; j < looseSize; j++)
				connectivityS[i][j] = NULL_EDGE;
		for (int i = 0; i < nodeCountS; i++)
			for (int j = nodeCountS; j < looseSize; j++)
				connectivityS[i][j] = NULL_EDGE;

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

	protected void datamemberInitial(int looseSize) {
		this.coreS = new int[looseSize];
		this.connectS = new int[looseSize];
		this.newlyAddedS = new int[looseSize];
		this.newlyAddedB = new int[looseSize];
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
		this.candidateS = new int[looseSize];
		this.candidatesB = new int[looseSize][];
		this.minDepth = 0;
	}

	public FastSUStateExt(FastSUStateExt oriSTATE, int[][] extension) {
		super();
		graphInitial(oriSTATE, extension);
		datamemberInitial(oriSTATE, extension);
		if (currentDepth < nodeCountS)
			findMatchableCandidate();
	}

	protected boolean graphInitial(FastSUStateExt oriSTATE, int[][] extension) {
		// The bigger graph is not changed
		this.verticesB = oriSTATE.verticesB;
		this.connectivityB = oriSTATE.connectivityB;
		// Do extending the smaller graph
		this.verticesS = oriSTATE.verticesS.clone();
		this.connectivityS = oriSTATE.connectivityS.clone();
		this.nodeCountS = oriSTATE.nodeCountS;
		this.nodeCountB = oriSTATE.nodeCountB;
		this.edgeNumS = oriSTATE.edgeNumS;
		this.edgeNumB = oriSTATE.edgeNumB;

		for (int i = 0; i < extension.length; i++) {
			int nodeA = extension[i][0];
			int nodeB = extension[i][1];
			if (nodeA >= verticesS.length || nodeB >= verticesS.length)
				return false;
			if (nodeA >= oriSTATE.nodeCountS) {
				this.verticesS[nodeA] = new int[1];
				this.verticesS[nodeA][0] = nodeB;
				++this.nodeCountS;
			} else {
				int[] newVertices = new int[verticesS[nodeA].length + 1];
				for (int t = 0; t < verticesS[nodeA].length; t++)
					newVertices[t] = verticesS[nodeA][t];
				newVertices[verticesS[nodeA].length] = nodeB;
				verticesS[nodeA] = newVertices;
			}
			if (nodeB >= oriSTATE.nodeCountS) {
				this.verticesS[nodeB] = new int[1];
				this.verticesS[nodeB][0] = nodeA;
				++this.nodeCountS;
			} else {
				int[] newVertices = new int[verticesS[nodeB].length + 1];
				for (int t = 0; t < verticesS[nodeB].length; t++)
					newVertices[t] = verticesS[nodeB][t];
				newVertices[verticesS[nodeB].length] = nodeA;
				verticesS[nodeB] = newVertices;
			}
			connectivityS[nodeA][nodeA] = extension[i][2];
			connectivityS[nodeA][nodeB] = extension[i][3];
			connectivityS[nodeB][nodeA] = extension[i][3];
			connectivityS[nodeB][nodeB] = extension[i][4];
			++this.edgeNumS;
		}
		if (edgeNumS > edgeNumB)
			return false;
		return true;
	}

	protected void datamemberInitial(FastSUStateExt oriSTATE, int[][] extension) {
		this.coreS = oriSTATE.coreS;
		this.connectS = oriSTATE.connectS;
		this.newlyAddedS = oriSTATE.newlyAddedS;
		this.newlyAddedB = oriSTATE.newlyAddedB;
		this.coreB = oriSTATE.coreB;
		this.connectB = oriSTATE.connectB;
		this.candidateS = oriSTATE.candidateS;
		this.candidatesB = oriSTATE.candidatesB;
		this.currentDepth = oriSTATE.nodeCountS;
		// update connectS
		for (int i = 0; i < extension.length; i++) {
			int nodeA = extension[i][0];
			int nodeB = extension[i][1];
			if (coreS[nodeA] != NULL_NODE && coreS[nodeB] == NULL_NODE)
				connectS[nodeB] = currentDepth;
		}
		this.minDepth = oriSTATE.nodeCountS;
	}

	public void replaceMapping(int[] newMap, int validLength) {
		for (int i = 0; i < coreB.length; i++)
			coreB[i] = -1;
		for (int i = 0; i <= validLength; i++) {
			coreS[i] = newMap[i];
			coreB[newMap[i]] = i;
		}
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
		if (this.candidatesB[currentDepth].length == minDepth)
			return true;
		else
			return false;
	}
}
