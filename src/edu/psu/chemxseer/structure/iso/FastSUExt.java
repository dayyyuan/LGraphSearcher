package edu.psu.chemxseer.structure.iso;

import java.util.BitSet;

import de.parmol.graph.Graph;

/**
 * This is an extension of FastSU It support complete embedding mining &
 * embedding growing
 * 
 * @author dayuyuan
 * 
 */
public class FastSUExt {
	private int[][] maps; // The mappings records between the small graph and
							// the big graph
	private int mapsNum;
	// mapsStatus[i] = 3, means maps[i] is valid 0, 1, 2, 3
	private int[] mapsStatus; // Records status of each mapping
	// mapsEffectiveRound[i] = 3, means maps[i] is valid 3, 4, 5, ...
	// before node m, maps[i] is duplicated with some other maps[j]
	private int[] mapsEffectiveRound;

	private FastSUStateExt state;
	// Number of nodes in current mapping
	// normally equals to mapsStatus + 1
	private int nodeCountS;
	private boolean mappingShrink;

	/**
	 * Given two graphs small and big Find all complete embeddings between them
	 * 
	 * @param small
	 * @param big
	 * @return
	 */
	public FastSUState completeEmbedding(Graph small, Graph big) {
		FastSUState fastSuState = new FastSUState(small, big);
		this.mapsNum = 0;
		this.maps = new int[2][fastSuState.getNodeCountS()];
		this.mapsStatus = new int[2];
		for (int i = 0; i < mapsStatus.length; i++)
			mapsStatus[i] = -1;

		this.mapsEffectiveRound = new int[2];
		for (int i = 0; i < mapsEffectiveRound.length; i++)
			mapsEffectiveRound[i] = -1;

		completeEmbedding(fastSuState);
		this.nodeCountS = small.getNodeCount();
		return fastSuState;
	}

	/**
	 * Given two graphs small and big Find all complete embeddings between them
	 * This complete embeddings can be used as start point of further embedding
	 * extension [middle, big]
	 * 
	 * @param small
	 * @param big
	 * @return
	 */
	public FastSUStateExt startCompleteEmbeddingExt(Graph small, Graph big) {
		FastSUStateExt ext = new FastSUStateExt(small, big);
		this.mapsNum = 0;
		this.maps = new int[2][ext.getNodeCountS()];
		this.mapsStatus = new int[2];
		for (int i = 0; i < mapsStatus.length; i++)
			mapsStatus[i] = -1;
		this.mapsEffectiveRound = new int[2];
		for (int i = 0; i < mapsEffectiveRound.length; i++)
			mapsEffectiveRound[i] = -1;

		completeEmbedding(ext);
		state = ext;
		this.nodeCountS = state.nodeCountS;
		return ext;
	}

	/**
	 * For finding embeddings only
	 * 
	 * @param fastSU
	 */
	private void completeEmbedding(FastSUState fastSU) {
		int SeedcandidateS = fastSU.getCandidateS();
		if (SeedcandidateS == -1)
			return;
		int[] SeedcandidateB = fastSU.getCandidatesB();
		if (SeedcandidateB == null || SeedcandidateB.length == 0)
			return;
		for (int i = 0; i < SeedcandidateB.length; i++) {
			findCompleteEmbedding(fastSU, SeedcandidateS, SeedcandidateB[i]);
		}
	}

	private void findCompleteEmbedding(FastSUState currentState,
			int SeedcandidateS, int SeedcandidateB) {
		// System.out.println(currentState.getDepth());
		currentState.expand(SeedcandidateS, SeedcandidateB);

		if (currentState.isGoal()) {
			// successfully find a mapping
			this.saveCurrentMatch(currentState.getMap(),
					currentState.getNodeCountS());
			currentState.backTrack();
		} else if (currentState.isDead()) {
			currentState.backTrack();
		} else {
			int[] candidateB = currentState.getCandidatesB();
			int candidateS = currentState.getCandidateS();
			for (int i = 0; i < candidateB.length; i++) {
				findCompleteEmbedding(currentState, candidateS, candidateB[i]);
			}
			currentState.backTrack();
		}
	}

	/**
	 * Based on the start point obtained from stateCompleteEmbeddingExt A
	 * complete embedding between a middle graph (an extension of the small
	 * graph) and the big graph can be found by growEmbeddingExt(extension)
	 * 
	 * @param oriState
	 * @param extension
	 * @return
	 */
	public FastSUStateExt growCompleteEmbeddingExt(int[][] extension) {
		this.mapsNum = 0;
		FastSUStateExt ext = null;
		this.mappingShrink = false;
		boolean extendable = false;
		int currentNodeCountS = this.nodeCountS;
		for (int i = 0; i < this.maps.length; i++) {
			if (mapsStatus[i] == -1 || mapsStatus[i] != currentNodeCountS - 1)
				continue; // this map is not valid to index
							// state.getNodeCountS() -1
			else if (mapsEffectiveRound[i] >= currentNodeCountS)
				continue; // this map is not yet valid untill
							// state.getNodeCountS
			// Grow the ith mapping
			else
				ext = growEmbeddingExt(extension, i);
			if (ext == null)
				this.mappingShrink = true;
			// mapsStatus[i] is unchanged
			else {
				extendable = true;
				state = ext;
			}
		}
		if (extendable)
			return state; // return a extended state
		else
			return null; // return null if can not extend
	}

	private FastSUStateExt growEmbeddingExt(int[][] extension, int mapIndex) {
		state.replaceMapping(maps[mapIndex], mapsStatus[mapIndex]);
		// Grow the state to include middle graph
		state = new FastSUStateExt(state, extension);
		this.nodeCountS = state.nodeCountS;
		// If expendable, return the extended state
		if (completeEmbeddingExt(mapIndex))
			return state;
		// else return null as a indication of not expendable
		else
			return null;
	}

	private boolean completeEmbeddingExt(int mapIndex) {
		int SeedcandidateS = state.getCandidateS();
		if (SeedcandidateS == -1)
			return true;
		int[] SeedcandidateB = state.getCandidatesB();
		int oriMapStatus = this.mapsStatus[mapIndex];
		if (SeedcandidateB == null || SeedcandidateB.length == 0)
			return false;
		boolean success = false;
		for (int i = 0; i < SeedcandidateB.length; i++) {
			if (findCompleteEmbedding(SeedcandidateS, SeedcandidateB[i],
					mapIndex, oriMapStatus))
				success = true;
		}
		return success;
	}

	private boolean findCompleteEmbedding(int SeedcandidateS,
			int SeedcandidateB, int mapIndex, int oriMapStatus) {
		// System.out.println(currentState.getDepth());
		state.expand(SeedcandidateS, SeedcandidateB);
		boolean success = false;
		if (state.isGoal()) {
			// first time successfully find a mapping
			if (oriMapStatus == this.mapsStatus[mapIndex])
				this.extendCurrentMatch(state.coreS, mapIndex, oriMapStatus,
						state.getNodeCountS());
			else
				// second time find a mapping: duplicate
				this.duplicateMatch(state.coreS, oriMapStatus,
						state.getNodeCountS());
			state.backTrack();
			success = true;
		} else if (state.isDead()) {
			state.backTrack();
		} else {
			int[] candidateB = state.getCandidatesB();
			int candidateS = state.getCandidateS();
			for (int i = 0; i < candidateB.length; i++) {
				findCompleteEmbedding(candidateS, candidateB[i], mapIndex,
						oriMapStatus);
			}
			state.backTrack();
		}
		return success;
	}

	/**
	 * Set all mapping that is larger than nodeCount to the size nodeCount Set
	 * all mapping that is not valid untill nodeCount to be invalid
	 * 
	 * @param nodeCount
	 */
	public void traceBackEmbeddingGrowth(int nodeCount) {
		for (int i = 0; i < maps.length; i++)
			if (mapsStatus[i] != -1)
				backupMatch(i, nodeCount - 1);
	}

	/**
	 * The first time saves a match into maps array the maps array grows when
	 * its available capacity = 0
	 * 
	 * @param match
	 */
	private void saveCurrentMatch(int[] match, int nodeIndexUpper) {
		saveCurrentMatch(match, 0, nodeIndexUpper);
	}

	private int saveCurrentMatch(int[] match, int nodeIndexLower,
			int nodeIndexUpper) {

		int availableSlot = -1;
		for (int i = 0; i < this.mapsStatus.length; i++) {
			if (mapsStatus[i] == -1) {
				availableSlot = i;
				break;
			}
		}
		if (availableSlot != -1 && availableSlot < this.mapsStatus.length) {
			saveCurrentMatch(match, availableSlot, nodeIndexLower,
					nodeIndexUpper);
			return availableSlot;
		}
		// No available slots for this new match
		else {
			int[][] newMaps = new int[maps.length * 2][];
			int[] newStatus = new int[newMaps.length];
			int[] newEffectiveRound = new int[newMaps.length];
			for (int i = 0; i < maps.length; i++) {
				newMaps[i] = maps[i];
				newStatus[i] = this.mapsStatus[i];
				newEffectiveRound[i] = this.mapsEffectiveRound[i];
			}
			for (int i = maps.length; i < newMaps.length; i++) {
				newStatus[i] = -1;
				newEffectiveRound[i] = -1;
			}
			maps = newMaps;
			mapsStatus = newStatus;
			mapsEffectiveRound = newEffectiveRound;
			saveCurrentMatch(match, this.mapsNum, nodeIndexLower,
					nodeIndexUpper);
			return this.mapsNum;
		}

	}

	private void saveCurrentMatch(int[] match, int matchIndex,
			int nodeIndexLower, int nodeIndexUpper) {
		if (maps[matchIndex] == null)
			maps[matchIndex] = new int[maps[0].length];
		for (int i = 0; i < nodeIndexUpper; i++) {
			// System.out.println("MapsLengh " + maps[mapsNum].length +
			// "mathLength " + match.length);
			maps[matchIndex][i] = match[i];
		}
		this.mapsEffectiveRound[matchIndex] = nodeIndexLower;
		this.mapsStatus[matchIndex] = nodeIndexUpper - 1;
		++mapsNum;
		// TEST ONLY
		// int count = 0;
		// for(int i = 0; i< this.mapsStatus.length; i++)
		// if(mapsStatus[i] !=-1)
		// count++;
		// if(count!=mapsNum)
		// System.out.println("ERROR in SaveCurrentMatch");
		// FINIDSH TEST
	}

	/**
	 * extend maps[matchIndex], extends it from nodeIndexLower to nodeIndexUpper
	 * 
	 * @param match
	 * @param matchIndex
	 */
	private void extendCurrentMatch(int[] match, int matchIndex,
			int nodeIndexLower, int nodeIndexUpper) {
		for (int i = nodeIndexLower; i < nodeIndexUpper; i++)
			maps[matchIndex][i] = match[i];
		++this.mapsNum;
		// TEST ONLY
		int count = 0;
		for (int i = 0; i < this.mapsStatus.length; i++)
			if (mapsStatus[i] != -1)
				count++;
		if (count != mapsNum)
			System.out.println("ERROR in SaveCurrentMatch");
		// FINIDSH TEST
	}

	private void duplicateMatch(int[] match, int nodeIndexLower,
			int nodeIndexUpper) {
		int matchIndex = saveCurrentMatch(match, 0, nodeIndexUpper);
		this.mapsEffectiveRound[matchIndex] = nodeIndexLower;
	}

	/**
	 * only the partial mapping from 0 to toNodeIndex (not included is
	 * effective)
	 * 
	 * @param matchIndex
	 * @param toNodeIndex
	 */
	private void backupMatch(int matchIndex, int toNodeIndex) {
		if (this.mapsEffectiveRound[matchIndex] >= toNodeIndex)
			this.mapsStatus[matchIndex] = -1; // not effective anymore
		else if (this.mapsStatus[matchIndex] > toNodeIndex - 1) {
			this.mapsStatus[matchIndex] = toNodeIndex - 1;
		}

	}

	public int getMapNumber() {
		return this.mapsNum;
	}

	public int getMapSize() {
		// TODO Auto-generated method stub
		return this.state.getNodeCountS();
	}

	public FastSUStateExt getState() {
		// TODO Auto-generated method stub
		return this.state;
	}

	public boolean isMappingShrink() {
		return this.mappingShrink;
	}

	public BitSet getValidStatus() {
		BitSet validStatus = new BitSet(mapsStatus.length);
		for (int i = 0; i < this.mapsStatus.length; i++)
			if (mapsStatus[i] != -1
					&& mapsStatus[i] >= this.state.nodeCountS - 1)
				validStatus.set(i, true);
			else
				validStatus.set(i, false);
		return validStatus;
	}

	public int[][] getMappings() {
		int[][] results = new int[this.mapsNum][];
		int index = 0;
		for (int i = 0; i < this.mapsStatus.length; i++)
			if (mapsStatus[i] != -1 && mapsStatus[i] >= nodeCountS - 1) {
				results[index] = new int[this.nodeCountS];
				for (int j = 0; j < results[index].length; j++)
					results[index][j] = maps[i][j];
				index++;
			}
		return results;
	}
}
