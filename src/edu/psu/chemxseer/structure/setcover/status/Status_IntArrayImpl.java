package edu.psu.chemxseer.structure.setcover.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Array;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;

public class Status_IntArrayImpl implements ICoverStatus,
		ICoverStatus_SwapInternal {
	// for ICoverStatus & ICoverStatus_Swap, status[item] = w denotes the item
	// is covered by w items
	// for ICoverStatus_HW, status[item] = w (w > 0) denote that the set is
	// covered by the set with ID w
	private int[][] status;

	/**
	 * Given the universSize returned by the IInput, construct an
	 * Status_IntArrayImpl object
	 * 
	 * @param universeSize
	 * @return
	 */
	public static Status_IntArrayImpl newInstance(int qCount, int gCount) {
		return new Status_IntArrayImpl(qCount, gCount);
	}

	public static Status_IntArrayImpl newInstance(int qCount, int gCount,
			ISet[] mustSelectSets) {
		Status_IntArrayImpl result = new Status_IntArrayImpl(qCount, gCount);
		for (ISet aSet : mustSelectSets)
			result.addToPos(aSet, -1);
		// System.out.println(result.getCoveredItemCount());
		return result;
	}

	private Status_IntArrayImpl(int qCount, int gCount) {
		this.status = new int[qCount][gCount];
		for (int i = 0; i < qCount; i++)
			Arrays.fill(status[i], 0);
	}

	// ICoverStatus_HW implementation:

	// Due to the fact that we are using integer array for implementation,
	// therefore, the memory should be big enough
	// to hold the input set, therefore, the result returned is a set_array.
	/*
	 * public ISet getCoverage(ISet newSet, Set<Integer> uSet) {
	 * List<IItem_Pair> items = new ArrayList<IItem_Pair>(); for(IItem_Pair
	 * item:newSet){ int theItem = item.getItem(); if(status[theItem]!=0 &&
	 * uSet.contains(status[theItem])) // the item is covered, but by the set
	 * with ID in uSet continue; // then does not count else items.add(item); //
	 * status[theItem] == 0 || theItem is not contained in the uSet } return new
	 * Set_Array(items, newSet.getSetID(), newSet.getFeature()); }
	 */

	/*
	 * public void updateCoverage(ISet newSet, Set<Integer> uSet) { int sID =
	 * newSet.getSetID(); for(IItem_Pair item:newSet){
	 * if(status[item.getQID()][item.getGID()] !=0 &&
	 * uSet.contains(status[item.getQID()][item.getGID()])) // the item is
	 * covered by a set in uSets continue; else
	 * status[item.getQID()][item.getGID()] = sID; } }
	 */

	// Due to the fact that we are using integer array for implementation,
	// therefore, the memory should be big enough
	// to hold the input set, therefore, the result returned is a set_array.
	/*
	 * public ISet getCoverage(ISet selectSet) { List<IItem_Pair> items =new
	 * ArrayList<IItem_Pair>(); int sID = selectSet.getSetID(); for(IItem_Pair
	 * item : selectSet){ if(status[item.getQID()][item.getGID()]!=sID)
	 * continue; else items.add(item); } return new Set_Array(items,
	 * selectSet.getSetID(), selectSet.getFeature()); }
	 */

	// ICover_Swap implementation
	@Override
	public long getUncoveredItemCount(ISet newSet) {
		long score = 0;
		for (int[] item : newSet) {
			if (this.status[item[0]][item[1]] == 0)
				score++;
		}
		return score;
	}

	@Override
	public long getOnlyCoveredItemCount(ISet selectSet) {
		long counter = 0;
		for (int[] item : selectSet) {
			if (this.status[item[0]][item[1]] == 1)
				counter++;
		}
		return counter;
	}

	@Override
	public boolean isOnlyCovered(int qID, int gID) {
		return status[qID][gID] == 1;
	}

	@Override
	public boolean isCovered(int qID, int gID) {
		return status[qID][gID] > 0;
	}

	@Override
	public void removeFromPos(SetwithScore minSet) {
		for (int[] item : minSet)
			this.status[item[0]][item[1]]--;
	}

	@Override
	public void addToPos(ISet[] newSets) {
		for (int i = 0; i < newSets.length; i++)
			this.addToPos(newSets[i], i);
	}

	@Override
	public void addToPos(ISet newSet, int pos) {
		for (int[] item : newSet)
			this.status[item[0]][item[1]]++;
	}

	@Override
	public ISet updateCoverageWithSetReturn(ISet set) {
		List<int[]> items = new ArrayList<int[]>(set.size());
		int qID = 0, gID = 0;
		for (int[] item : set) {
			if (this.status[qID][gID] == 0)
				items.add(item);
			this.status[qID][gID]++;
		}
		return new Set_Pair_Array(items, set.getSetID(), set.getFeature());
	}

	@Override
	public void updateCoverage(ISet oneSet) {
		for (int[] item : oneSet) {
			this.status[item[0]][item[1]]++;
		}
	}

	@Override
	public long getCoveredItemCount() {
		long counter = 0;
		for (int i = 0; i < status.length; i++)
			for (int j = 0; j < status[i].length; j++)
				if (status[i][j] > 0)
					counter++;
		return counter;
	}

	@Override
	public void clear() {
		for (int i = 0; i < status.length; i++)
			Arrays.fill(status[i], 0);
	}

	@Override
	public int getQCount() {
		return this.status.length;
	}

	@Override
	public int getGCount() {
		return this.status[0].length;
	}

	@Override
	public int[][] getUnOnlyCoveredItemForQID(int qIndex) {
		int gSize = this.getGCount();
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
		long count = 0;
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
		long count = 0;
		for (int gID = 0; gID < this.status.length; gID++)
			if (this.isCovered(qID, gID))
				count++;
		return count;
	}

	@Override
	public long getCoveredItemCountForG(int gID) {
		long count = 0;
		for (int qID = 0; qID < this.status.length; qID++)
			if (this.isCovered(qID, gID))
				count++;
		return count;
	}

}
