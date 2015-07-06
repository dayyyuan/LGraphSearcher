package edu.psu.chemxseer.structure.setcover.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Array;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.status.invertedindex.IInvertedIndex;
import edu.psu.chemxseer.structure.setcover.status.invertedindex.InvertedIndexDecomp_Abstract;

/**
 * The decomposition implementation of the status Basically an wrapper of the
 * Inverted index
 * 
 * @author dayuyuan
 * 
 */
public class Status_Decomp implements ICoverStatus, ICoverStatus_Swap,
		ICoverStatus_Swap_InvertedIndex {
	private IInvertedIndex invertedIndex; // decomposed index
	private int qCount;
	private int gCount;

	private Status_Decomp(IInvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
		this.qCount = invertedIndex.getQCount();
		this.gCount = invertedIndex.getGCount();
	}

	public static Status_Decomp newInstance(int qCount, int gCount,
			AppType aType) {
		return new Status_Decomp(InvertedIndexDecomp_Abstract.newEmptyInstance(
				qCount, gCount, aType));
	}

	// ICoverStatus
	@Override
	public Set_Pair_Array updateCoverageWithSetReturn(ISet newSet) {
		if (!(newSet instanceof Set_Pair)) {
			System.out
					.println("The input of Status_Decomp:updateCoverageWithSetReturn has wrong format");
			return null;
		}
		Set_Pair newSetPair = (Set_Pair) newSet;
		List<int[]> result = new ArrayList<int[]>();
		for (int[] item : newSet)
			if (this.isCovered(item[0], item[1]))
				continue;
			else
				result.add(item);
		invertedIndex.addSet(newSetPair, newSet.getSetID());
		return new Set_Pair_Array(result, newSet.getSetID(),
				newSet.getFeature());
	}

	@Override
	public void updateCoverage(ISet newSet) {
		if (!(newSet instanceof Set_Pair)) {
			System.out
					.println("The input of Status_Decomp:updateCoverageWithSetReturn has wrong format");
			return;
		}
		Set_Pair newSetPair = (Set_Pair) newSet;
		invertedIndex.addSet(newSetPair, newSet.getSetID());
	}

	@Override
	public long getUncoveredItemCount(ISet newSet) {
		// This function does not utilize the special character of the pair set
		long count = 0;
		for (int[] item : newSet)
			if (this.isCovered(item[0], item[1]))
				continue;
			else
				count++;
		return count;
	}

	@Override
	/**
	 * Does not suggest of frequent use
	 * @param item
	 * @return
	 */
	public boolean isCovered(int qID, int gID) {
		return this.invertedIndex.isCovered(qID, gID, 0);
	}

	// ICoverStatus_Swap
	@Override
	public long getOnlyCoveredItemCount(ISet selectSet) {
		long count = 0;
		for (int[] item : selectSet)
			if (this.invertedIndex.isCovered(item[0], item[1], 1))
				continue;
			else
				count++;
		return count;
	}

	@Override
	public boolean isOnlyCovered(int qID, int gID) {
		// return false if covered more than once
		return this.invertedIndex.isCovered(qID, gID, 0)
				&& !this.invertedIndex.isCovered(qID, gID, 1);
	}

	@Override
	public void removeFromPos(SetwithScore minSet) {
		this.invertedIndex.removeSet(minSet.getSet(), minSet.getPos());

	}

	@Override
	public void addToPos(ISet[] newSets) {
		for (int i = 0; i < newSets.length; i++)
			invertedIndex.addSet(newSets[i], i);
	}

	@Override
	public void addToPos(ISet newSet, int pos) {
		this.invertedIndex.addSet(newSet, pos);
	}

	private int getOnlyCoveredSetPosID(int[] item) {
		return this.invertedIndex.getOnlyCoveredSetPosID(item[0], item[1]);
	}

	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
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
		long counter = 0;
		for (int qID = 0; qID < qCount; qID++)
			for (int gID = 0; gID < gCount; gID++) {
				int[] temp = new int[2];
				temp[0] = qID;
				temp[1] = gID;
				if (this.isCovered(qID, gID))
					counter++;
			}
		return counter;
	}

	@Override
	public void clear() {
		this.invertedIndex.clear();
	}

	@Override
	public int getQCount() {
		return this.qCount;
	}

	@Override
	public int getGCount() {
		return this.gCount;
	}

	@Override
	/**
	 * May take even longer time than item_pair output directly
	 * This is because of each of the q or(g), we need to find noDBGraph, which is an complimentary operation
	 */
	public long getOnlyCoveredItemCount(Item_Group group) {
		long count = 0;
		if (group.getType() == AppType.subSearch
				|| group.getType() == AppType.supSearch) {
			for (int[] item : group)
				if (this.isOnlyCovered(item[0], item[1]))
					count++;
		} else
			throw new UnsupportedOperationException(
					"getOnlyCoveredItemCount(group) method is not supported for classification");
		return count;
	}

	@Override
	public long getUncoveredItemCount(Item_Group group) {
		long count = 0;
		if (group.getType() == AppType.subSearch
				|| group.getType() == AppType.supSearch) {
			for (int[] item : group)
				if (!this.isCovered(item[0], item[1]))
					count++;
		}
		return count;
	}

	@Override
	public int[][] getUnOnlyCoveredItemForQID(int qIndex) {
		int gSize = this.getGCount();
		int[] result = new int[gSize];
		int index = 0;
		int index2 = gSize - 1;
		for (int gID = 0; gID < gSize; gID++)
			if (!isCovered(qIndex, gID))
				result[index++] = gID;
			else if (isOnlyCovered(qIndex, gID))
				result[index2--] = gID;
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
		int qSize = this.getQCount();
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
	public long getCoveredItemCountForQ(int qID) {
		long count = 0;
		for (int gID = 0; gID < gCount; gID++)
			if (this.isCovered(qID, gID))
				count++;
		return count;
	}

	@Override
	public long getCoveredItemCountForG(int gID) {
		long count = 0;
		for (int qID = 0; qID < qCount; qID++)
			if (this.isCovered(qID, gID))
				count++;
		return count;
	}

	// //ICoverStatus_HW
	// @Override
	// public Set_Pair_Array getCoverage(ISet newSet, Set<Integer> uSet) {
	// List<IItem_Pair> items = new ArrayList<IItem_Pair>();
	// for(IItem item:newSet){
	// if(!this.invertedIndex.isCovered(item, 0))
	// continue;
	// else {
	// int[] coveredSets = this.invertedIndex.getCoveredSetPosIDs(item, -1);
	// boolean add = true;
	// for(int i: coveredSets)
	// if(uSet.contains(i)){
	// add=false;
	// break;
	// }
	// if(add)
	// items.add((IItem_Pair) item);
	// }
	// }
	// return new Set_Pair_Array (items, newSet.getSetID());
	// }
	//
	// @Override
	// public void updateCoverage(ISet newSet, Set<Integer> uSet) {
	// this.invertedIndex.addSet(newSet, newSet.getSetID());
	// }
	//
	// @Override
	// public Set_Pair_Array getCoverage(ISet selectSet) {
	// List<IItem_Pair> result = new ArrayList<IItem_Pair>();
	// for(IItem item:selectSet)
	// if(this.invertedIndex.isCovered(item, 1))
	// continue;
	// else result.add((IItem_Pair) item);
	// return new Set_Pair_Array(result, selectSet.getSetID());
	// }

	/*
	 * @Override public SetwithScore getUncoveredWithReturn(ISet oneSet, int
	 * exceptID) { if(!(oneSet instanceof Set_Pair)){ System.out.println(
	 * "The input of Status_Decomp:updateCoverageWithSetReturn has wrong format"
	 * ); return null; } Set_Pair newSetPair = (Set_Pair)oneSet;
	 * List<IItem_Pair> result = new ArrayList<IItem_Pair>(); for(IItem
	 * item:oneSet) if(this.invertedIndex.isCovered(item, 0, exceptID))
	 * continue; else result.add((IItem_Pair)item);
	 * 
	 * Set_Pair_Array resultSet = new Set_Pair_Array(result, oneSet.getSetID(),
	 * oneSet.getFeature()); return new SetwithScore(resultSet,
	 * oneSet.getSetID(), resultSet.size()); }
	 */

}
