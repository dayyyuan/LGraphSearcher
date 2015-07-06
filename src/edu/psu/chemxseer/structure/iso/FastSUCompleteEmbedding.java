package edu.psu.chemxseer.structure.iso;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

public class FastSUCompleteEmbedding {
	protected int[][] maps; // The mappings records between the small graph and
							// the big graph
	protected FastSUStateExpandable state;
	private int mapNum;
	// mapChanged = true if some of the original mappings vanished during the
	// mapping extension
	private boolean mapChanged;

	private void inValidInitialization() {
		this.mapNum = 0;
		this.state = null;
		this.mapChanged = true;

	}

	/**
	 * Construct CompleteEmbedding between the small graph and the big graph
	 * 
	 * @param small
	 * @param big
	 */
	public FastSUCompleteEmbedding(Graph small, Graph big) {
		if (small.getEdgeCount() > big.getEdgeCount()
				|| small.getNodeCount() > big.getNodeCount()) {
			inValidInitialization();
		} else {
			this.mapNum = 0;
			this.state = new FastSUStateExpandable(small, big);
			completeEmbedding(state);
			this.mapChanged = true;
		}
	}

	/**
	 * Construct the CompleteEmbedding between the small graph and the big graph
	 * 
	 * @param smallGVCode
	 * @param big
	 */
	public void construct(int[][] smallGVCode, Graph big) {
		// validate the input:
		int nodeCountS = smallGVCode[0][0];
		int edgeCountS = smallGVCode.length;
		for (int i = 0; i < smallGVCode.length; i++) {
			if (smallGVCode[i][1] > nodeCountS)
				nodeCountS = smallGVCode[i][1];
			if (smallGVCode[i][0] > nodeCountS)
				nodeCountS = smallGVCode[i][0];
		}
		nodeCountS++;
		if (edgeCountS > big.getEdgeCount() || nodeCountS > big.getNodeCount())
			inValidInitialization();
		else {
			this.mapNum = 0;
			state = new FastSUStateExpandable(smallGVCode, big);
			completeEmbedding(state);
			this.mapChanged = true;
		}
	}

	public FastSUCompleteEmbedding(int[][] smallGVCode, Graph big) {
		this.construct(smallGVCode, big);
	}

	/**
	 * Construct the CompleteEmbedding between the small graph and the big graph
	 * 
	 * @param smallDFSString
	 * @param big
	 */
	public FastSUCompleteEmbedding(String smallDFSString, Graph big) {
		int[][] GVCodes = MyFactory.getDFSCoder().parseTextToArray(
				smallDFSString);
		this.construct(GVCodes, big);
	}

	/**
	 * Construct the CompleteEmbedding between the small graph and the big graph
	 * 
	 * @param smallConnectivity
	 * @param smallVertices
	 * @param big
	 */
	public FastSUCompleteEmbedding(int[][] smallConnectivity,
			int[][] smallVertices, Graph big) {
		int edgeCountS = 0;
		for (int i = 0; i < smallVertices.length; i++)
			edgeCountS += smallVertices[i].length;
		edgeCountS /= 2;
		int nodeCountS = smallVertices.length;

		if (edgeCountS > big.getEdgeCount() || nodeCountS > big.getNodeCount())
			inValidInitialization();
		else {
			this.mapNum = 0;
			state = new FastSUStateExpandable(smallConnectivity, smallVertices,
					big);
			completeEmbedding(state);
			this.mapChanged = true;
		}
	}

	/**
	 * If oriEmbedding is expandable with extension, then we build a new
	 * FastSUStateExpandable state and grows the partial mapping to a complete
	 * mapping otherwise, we just assign the internal state to null;
	 * 
	 * @param oriEmbedding
	 * @param extension
	 */
	public FastSUCompleteEmbedding(FastSUCompleteEmbedding oriEmbedding,
			int[][] extension) {
		state = oriEmbedding.state.expandToNewState(extension);
		this.mapChanged = false;
		if (state != null) {
			int[][] oriMap = oriEmbedding.maps;
			for (int i = 0; i < oriEmbedding.getMapNum(); i++) {
				int validMapping = state.replaceMapping(oriMap[i],
						oriEmbedding.state.nodeCountS - 1);
				if (validMapping == 1) {
					boolean success = completeEmbedding(state);
					if (!success)
						this.mapChanged = true;
				} else if (validMapping == 0) { // perfectly full nodes mapping
												// before finding the extension,
												// the extension is usually an
												// edge extension
					break; // nothing to do, since its alreay passed the
							// validation
				} else if (validMapping == 2) { // subgraph isomorphism test:
												// can not grow
					this.saveCurrentMatch(oriMap[i]);
				} else {
					this.mapChanged = true;
					continue;
				}
			}
		} else
			this.mapNum = 0;
	}

	/**
	 * If oriEmbedding is expandable with extension, then we build a new
	 * FastSUStateExpandable state and grows the partial mapping to a complete
	 * mapping otherwise, we just assign the internal state to null;
	 * 
	 * @param oriEmbedding
	 * @param middleGraph
	 */
	public FastSUCompleteEmbedding(FastSUCompleteEmbedding oriEmbedding,
			Graph middleGraph) {
		this.state = oriEmbedding.state.expandToNewState(middleGraph);
		this.mapChanged = false;
		if (state != null) {
			int[][] oriMap = oriEmbedding.maps;
			for (int i = 0; i < oriEmbedding.getMapNum(); i++) {
				int validMapping = state.replaceMapping(oriMap[i],
						oriEmbedding.state.nodeCountS - 1);
				if (validMapping == 1) {
					boolean success = completeEmbedding(state);
					if (!success)
						this.mapChanged = true;
				} else if (validMapping == 0) { // perfectly full nodes mapping
												// before finding the extension,
												// the extension is usually an
												// edge extension
					break; // nothing to do, since its alreay passed the
							// validation
				} else if (validMapping == 2) { // subgraph isomorphism test:
												// can not grow
					this.saveCurrentMatch(oriMap[i]);
				} else {
					this.mapChanged = true;
					continue;
				}
			}
		} else {
			this.mapNum = 0;
		}
	}

	public boolean isMappingChanged() {
		return this.mapChanged;
	}

	public int getMapNum() {
		return mapNum;
	}

	public boolean issubIsomorphic() {
		if (mapNum != 0)
			return true;
		else
			return false;
	}

	public boolean isIsomorphic() {
		return issubIsomorphic() && state.edgeNumB == state.edgeNumS
				&& state.nodeCountB == state.nodeCountS;
	}

	/**
	 * Given the current fastSU status, try to extend the fastSU to find
	 * complete embedding.
	 * 
	 * @param fastSU
	 * @return
	 */
	private boolean completeEmbedding(FastSUState fastSU) {
		boolean success = false;
		int SeedcandidateS = fastSU.getCandidateS();
		int[] SeedcandidateB = fastSU.getCandidatesB();
		if (SeedcandidateB == null || SeedcandidateB.length == 0)
			return false; // not success
		for (int i = 0; i < SeedcandidateB.length; i++) {
			boolean status = findCompleteEmbedding(fastSU, SeedcandidateS,
					SeedcandidateB[i]);
			if (status)
				success = true;
		}
		return success;
	}

	/**
	 * Underlying procedures to search for complete embeddings and save the
	 * embedding by calling this.saveCurrentMatch()
	 * 
	 * @param currentState
	 * @param SeedcandidateS
	 * @param SeedcandidateB
	 * @return
	 */
	private boolean findCompleteEmbedding(FastSUState currentState,
			int SeedcandidateS, int SeedcandidateB) {
		// System.out.println(currentState.getDepth());
		currentState.expand(SeedcandidateS, SeedcandidateB);
		boolean success = false; // denote whether we find at least one
									// embedding
		if (currentState.isGoal()) {
			// successfully find a mapping & save the mapping
			this.saveCurrentMatch(currentState.getMap().clone());
			success = true;
			currentState.backTrack();
		} else if (currentState.isDead()) {
			currentState.backTrack();
		} else {
			int[] candidateB = currentState.getCandidatesB();
			int candidateS = currentState.getCandidateS();
			for (int i = 0; i < candidateB.length; i++) {
				boolean status = findCompleteEmbedding(currentState,
						candidateS, candidateB[i]);
				if (status)
					success = status;
			}
			currentState.backTrack();
		}
		return success;
	}

	private void saveCurrentMatch(int[] match) {
		if (this.maps == null) {
			this.maps = new int[1][];
			this.saveCurrentMatch(match);
		} else if (this.maps.length == this.mapNum) {
			int[][] newMap = new int[2 * maps.length][];
			for (int i = 0; i < maps.length; i++)
				newMap[i] = maps[i];
			this.maps = newMap;
			this.saveCurrentMatch(match);
		} else {
			this.maps[mapNum] = match;
			mapNum++;
		}
	}

	/**
	 * Return one-match state
	 * 
	 * @return
	 */
	public FastSUStateExpandable getState() {
		if (this.maps == null || maps.length == 0) {
			return this.state;
		} else {
			FastSUStateExpandable result = new FastSUStateExpandable(this.state);
			result.replaceMapping(this.maps[0], state.nodeCountS - 1);
			return result;
		}
	}

	/**
	 * Return the Mappings. For the mapping[i] = -1, remove the entry.
	 * 
	 * @return
	 */
	public int[][] getMaps() {
		// 1. Calculate the real number of >0 entries in each row

		int[][] results = new int[this.mapNum][];
		for (int i = 0; i < this.mapNum; i++) {
			results[i] = new int[maps[i].length];
			for (int j = 0; j < results[i].length; j++)
				results[i][j] = this.maps[i][j];
		}
		return results;
	}
}
