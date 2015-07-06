package edu.psu.chemxseer.structure.iso;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

public class FastSU {
	protected int[] map;

	/**
	 * Return true if graph small is subgraph isomorphic to graph big
	 * 
	 * @param small
	 * @param big
	 * @return
	 */
	public boolean isIsomorphic(Graph small, Graph big) {
		if (small.getNodeCount() > big.getNodeCount()
				|| small.getEdgeCount() > big.getEdgeCount())
			return false;
		FastSUState fastSUState = new FastSUState(small, big);
		return isIsomorphic(fastSUState);
	}

	public boolean isIsomorphic(FastSUCompleteEmbedding oriEmbedding,
			int[][] extension) {
		FastSUStateExpandable state = oriEmbedding.state
				.expandToNewState(extension); // create an new state
		// The new state maybe : node extension of the oriState, or edge
		// extension of the oriState, or both
		if (state != null) {
			int[][] oriMap = oriEmbedding.maps;
			for (int i = 0; i < oriEmbedding.getMapNum(); i++) {
				int validMapping = state.replaceMapping(oriMap[i],
						oriEmbedding.state.nodeCountS - 1);
				if (validMapping == 1) {
					boolean success = isIsomorphic(state);
					if (success)
						return true;
				} else if (validMapping == 0) { // perfectly full mapping
					return true;
				} else if (validMapping == 2) { // subgraph isomorphism test:
												// can not grow
					return true;
				} else {
					// the mapping is not correct: this is because the (1) the
					// extension is an edge extension (2) the mapping is not
					// valie after
					// adding the edge extension.
					continue;
				}
			}
		}
		return false;
	}

	protected boolean isIsomorphic(FastSUState fastSUState) {
		int SeedcandidateS = fastSUState.getCandidateS();
		int[] SeedcandidateB = fastSUState.getCandidatesB();
		if (SeedcandidateB == null || SeedcandidateB.length == 0)
			return false;
		for (int i = 0; i < SeedcandidateB.length; i++) {
			boolean success = startMatching(fastSUState, SeedcandidateS,
					SeedcandidateB[i]);
			if (success)
				return success;
		}
		return false;
	}

	public int[] getMapping(Graph small, Graph big) {
		if (isIsomorphic(small, big))
			return this.map;
		else
			return null;
	}

	private boolean startMatching(FastSUState currentState, int SeedcandidateS,
			int SeedcandidateB) {
		currentState.expand(SeedcandidateS, SeedcandidateB);

		if (currentState.isGoal()) {
			map = currentState.getMap().clone();
			return true; // successfully find a mapping
		} else if (currentState.isDead()) {
			currentState.backTrack();
			return false;
		} else {
			int[] candidateB = currentState.getCandidatesB();
			int candidateS = currentState.getCandidateS();
			for (int i = 0; i < candidateB.length; i++) {
				boolean match = startMatching(currentState, candidateS,
						candidateB[i]);
				if (match)
					return true;
				else
					continue;
			}
			currentState.backTrack();
			return false;
		}
	}

	/**
	 * Given the small & big graph, return the FastSUStateLabelling
	 * 
	 * @param small
	 * @param big
	 * @return
	 */
	public FastSUStateLabelling graphExtensionLabeling(Graph small, Graph big) {
		FastSUStateLabelling fastSU = new FastSUStateLabelling(small, big);
		boolean isomorphic = this.isIsomorphic(fastSU);
		// small is not a subgraph of big, return null
		if (isomorphic == false)
			return null;
		// else relabel graph big as an extension of small
		else {
			fastSU.relabelGraphB();
			fastSU.reOrderGraphB();
			return fastSU;
		}
	}

	/**
	 * Given the Small Graphs & big Graphs, return the FastSUStateLabelling
	 * 
	 * @param labels
	 * @param big
	 * @return
	 */
	public FastSUStateLabelling graphExtensionLabeling(String labels, Graph big) {
		// TFirst step, convert the labels into int[][]
		int[][] newLabels = MyFactory.getDFSCoder().parseTextToArray(labels);
		return graphExtensionLabeling(newLabels, big);
	}

	/**
	 * Given the small graph labels (represented as in LindexTerm extension),
	 * and the big graph
	 * 
	 * @param labels
	 * @param big
	 * @return
	 */
	public FastSUStateLabelling graphExtensionLabeling(int[][] labels, Graph big) {
		FastSUStateLabelling fastSU = new FastSUStateLabelling(labels, big);
		boolean isomorphic = this.isIsomorphic(fastSU);
		// small is not a subgraph of big, return null
		if (isomorphic == false)
			return null;
		// else relabel graph big as an extension of small
		else {
			fastSU.relabelGraphB();
			fastSU.reOrderGraphB();
			return fastSU;
		}
	}

	public FastSUStateLabelling graphExtensionLabeling(int[][] small,
			int[][] big) {
		FastSUStateLabelling fastSU = new FastSUStateLabelling(small, big);
		boolean isomorphic = this.isIsomorphic(fastSU);
		// small is not a subgraph of big, return null
		if (isomorphic == false)
			return null;
		// else relabel graph big as an extension of small
		else {
			fastSU.relabelGraphB();
			fastSU.reOrderGraphB();
			return fastSU;
		}
	}

	public FastSUStateLabelling graphExtensionLabeling(FastSUState mapedState) {
		FastSUStateLabelling fastSU = new FastSUStateLabelling(mapedState);
		fastSU.relabelGraphB();
		fastSU.reOrderGraphB();
		return fastSU;
	}

	public FastSUStateLabelling graphExtensionLabeling(
			FastSUStateLabelling oriFastSU, Graph big) {
		FastSUStateLabelling fastSU = new FastSUStateLabelling(oriFastSU, big);
		boolean isomorphic = this.isIsomorphic(fastSU);
		// small is not a subgraph of big, return null
		if (isomorphic == false)
			return null;
		// else relabel graph big as an extension of small
		else {
			fastSU.relabelGraphB();
			fastSU.reOrderGraphB();
			return fastSU;
		}
	}

	public FastSUStateLabelling graphExtensionLabeling(
			FastSUStateLabelling oriFastSU, int[][] bigLabel) {
		FastSUStateLabelling fastSU = new FastSUStateLabelling(oriFastSU,
				bigLabel);
		boolean isomorphic = this.isIsomorphic(fastSU);
		// small is not a subgraph of big, return null
		if (isomorphic == false)
			return null;
		// else relabel graph big as an extension of small
		else {
			fastSU.relabelGraphB();
			fastSU.reOrderGraphB();
			return fastSU;
		}
	}

}
