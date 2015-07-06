package edu.psu.chemxseer.structure.setcover.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.status.invertedindex.InvertedIndexArray;

/**
 * This is especially effective for the swap based algorithm (especially swap
 * algorithm 2) since instead of finding the minSet in every iteration, we can
 * maintain a score for each selected set and update the score when necessary.
 * 
 * @author dayuyuan
 * 
 */
public class Status_InvertedIndexArray implements ICoverStatus_Swap,
		ICoverStatus_Swap_InvertedIndex {
	private InvertedIndexArray invertedIndex;
	private Status_BooleanArrayImpl fixIndex;

	/**
	 * Construct & return an status_inverted index the underlying structure
	 * depends on the SetType The universeSize is saved, because the input is
	 * inputed directly
	 * 
	 * @param input
	 * @param sType
	 * @return
	 */
	public static Status_InvertedIndexArray newInstance(int qCount, int gCount) {
		return new Status_InvertedIndexArray(
				InvertedIndexArray.newEmptyInstance(qCount, gCount));
	}

	private Status_InvertedIndexArray(InvertedIndexArray invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	public static ICoverStatus_Swap_InvertedIndex newInstance(int qCount,
			int gCount, ISet[] edgeSets) {
		Status_BooleanArrayImpl fixIndex = Status_BooleanArrayImpl.newInstance(
				qCount, gCount);
		for (ISet oneSet : edgeSets)
			fixIndex.updateCoverage(oneSet);
		return new Status_InvertedIndexArray(
				InvertedIndexArray.newEmptyInstance(qCount, gCount), fixIndex);
	}

	private Status_InvertedIndexArray(InvertedIndexArray invertedIndex,
			Status_BooleanArrayImpl fixIndex) {
		this.invertedIndex = invertedIndex;
		this.fixIndex = fixIndex;
	}

	@Override
	public long getUncoveredItemCount(ISet newSet) {
		long counter = 0;
		for (int[] item : newSet) {
			if (!this.isCovered(item[0], item[1]))
				counter++;
		}
		return counter;
	}

	@Override
	public long getOnlyCoveredItemCount(ISet selectSet) {
		long counter = 0;
		for (int[] item : selectSet) {
			if (isOnlyCovered(item[0], item[1]))
				counter++;
		}
		return counter;
	}

	@Override
	public boolean isOnlyCovered(int qID, int gID) {
		if (fixIndex != null && fixIndex.isCovered(qID, gID))
			return false;
		else
			return this.invertedIndex.isCovered(qID, gID, 0)
					&& !this.invertedIndex.isCovered(qID, gID, 1);
	}

	@Override
	public boolean isCovered(int qID, int gID) {
		return (fixIndex != null && fixIndex.isCovered(qID, gID))
				|| this.invertedIndex.isCovered(qID, gID, 0);
	}

	@Override
	public void removeFromPos(SetwithScore minSet) {
		this.invertedIndex.removeSet(minSet, minSet.getPos());
	}

	@Override
	public void addToPos(ISet newSet, int pos) {
		this.invertedIndex.addSet(newSet, pos);
	}

	@Override
	public void addToPos(ISet[] newSets) {
		for (int i = 0; i < newSets.length; i++)
			this.addToPos(newSets[i], i);
	}

	/**
	 * Get the posIDs of sets covering the item, except for "exceptID" Return -1
	 * if not covered or more than once covered
	 * 
	 * @param item
	 * @param exceptID
	 * @return
	 */
	private int getOnlyCoveredSetPosID(int[] item) {
		if (fixIndex != null && fixIndex.isCovered(item))
			return -1;
		else
			return this.invertedIndex.getOnlyCoveredSetPosID(item[0], item[1]);
	}

	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
		if (fixIndex != null && fixIndex.isCovered(qID, gID))
			return -1;
		else
			return this.invertedIndex.getOnlyCoveredSetPosID(qID, gID);
	}

	@Override
	public Map<Integer, Integer> getOnlyCoveredSetPosIDs(ISet set) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (int[] item : set) {
			int setID = this.getOnlyCoveredSetPosID(item);
			if (setID >= 0)
				if (result.containsKey(setID))
					result.put(setID, result.get(setID) + 1);
				else
					result.put(setID, 1);
		}
		return result;
	}

	@Override
	public long getCoveredItemCount() {
		if (fixIndex == null)
			return this.invertedIndex.getCoveredItemCount();
		else {
			long count = 0;
			int qCount = invertedIndex.getQCount();
			int gCount = invertedIndex.getGCount();
			for (int qID = 0; qID < qCount; qID++)
				for (int gID = 0; gID < gCount; gID++)
					if (this.isCovered(qID, gID))
						count++;
			return count;
		}
	}

	@Override
	public void clear() {
		this.invertedIndex.clear();
	}

	@Override
	public long getOnlyCoveredItemCount(Item_Group group) {
		long count = 0;
		if (group.getType() == AppType.subSearch
				|| group.getType() == AppType.supSearch) {
			for (int[] item : group) {
				if (this.isOnlyCovered(item[0], item[1]))
					count++;
			}
		}
		return count;
	}

	@Override
	public long getUncoveredItemCount(Item_Group group) {
		int count = 0;
		if (group.getType() == AppType.subSearch
				|| group.getType() == AppType.supSearch) {
			for (int[] item : group) {
				if (!this.isCovered(item[0], item[1]))
					count++;
			}
		}
		return count;
	}

	@Override
	public long getCoveredItemCountForQ(int qID) {
		int gSize = this.invertedIndex.getGCount();
		long count = 0;
		for (int i = 0; i < gSize; i++)
			if (this.isCovered(qID, i))
				count++;
		return count;
	}

	@Override
	public long getCoveredItemCountForG(int gID) {
		int qSize = this.invertedIndex.getQCount();
		long count = 0;
		for (int j = 0; j < qSize; j++) {
			if (isCovered(j, gID))
				count++;
		}
		return count;
	}

	@Override
	public int[][] getUnOnlyCoveredItemForQID(int qIndex) {
		int gSize = this.invertedIndex.getGCount();
		int[] result = new int[gSize];
		int index = 0;
		int index2 = gSize - 1;
		for (int i = 0; i < gSize; i++)
			if (!isCovered(qIndex, i))
				result[index++] = i;
			else if (isOnlyCovered(qIndex, i))
				result[index2--] = i;
		int[][] finalResult = new int[2][];
		finalResult[0] = Arrays.copyOf(result, index);
		index2 += 1;
		finalResult[1] = new int[gSize - index2];
		for (int i = 0; i < finalResult[1].length; i++)
			finalResult[1][i] = result[gSize - 1 - i];

		return finalResult;
	}

	@Override
	public int[][] getUnOnlyCoveredItemForGID(int gIndex) {
		int qSize = this.invertedIndex.getQCount();
		int[] result = new int[qSize];
		int index = 0;
		int index2 = qSize - 1;
		for (int j = 0; j < qSize; j++) {
			if (!isCovered(j, gIndex))
				result[index++] = j;
			else if (isOnlyCovered(j, gIndex))
				result[index2--] = j;
		}
		int[][] finalResult = new int[2][];
		finalResult[0] = Arrays.copyOf(result, index);
		index2 += 1;
		finalResult[1] = new int[qSize - index2];
		for (int i = 0; i < finalResult[1].length; i++)
			finalResult[1][i] = result[qSize - 1 - i];

		return finalResult;
	}

	@Override
	public int getQCount() {
		return this.invertedIndex.getQCount();
	}

	@Override
	public int getGCount() {
		return this.invertedIndex.getGCount();
	}

	/*
	 * @Override public SetwithScore getUncoveredWithReturn(ISet oneSet, int
	 * exceptID) { List<IItem> result =new ArrayList<IItem>(); for(IItem
	 * item:oneSet){ if(this.invertedIndex.isCovered(item, 0 , exceptID))
	 * continue; else result.add(item); } ISet newSet = new Set_Array(result,
	 * oneSet.getSetID(), oneSet.getFeature()); return new SetwithScore(oneSet,
	 * oneSet.getSetID(), newSet.size()); }
	 */

}
