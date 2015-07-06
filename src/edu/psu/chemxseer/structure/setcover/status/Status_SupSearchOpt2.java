package edu.psu.chemxseer.structure.setcover.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.util.PartialOrderedIntSets;

public class Status_SupSearchOpt2 implements ICoverStatus_Swap_InvertedIndex {
	// For each of the database graph, the uncovered queries
	private int[][] uncoveredItems;
	private int[] uncoveredItemSize;

	private int[][] onlyCoveredItems;
	private int[] onlyCoveredItemSize;

	private ICoverStatus_SwapInternal status;

	public Status_SupSearchOpt2(ICoverStatus_Swap status) {
		// construct the uncoveredItems
		int gSize = ((ICoverStatus_SwapInternal) status).getGCount();
		this.uncoveredItems = new int[gSize][];
		this.uncoveredItemSize = new int[gSize];
		this.onlyCoveredItems = new int[gSize][];
		this.onlyCoveredItemSize = new int[gSize];

		Arrays.fill(uncoveredItemSize, 0);
		Arrays.fill(onlyCoveredItemSize, 0);
		this.status = (ICoverStatus_SwapInternal) status;
	}

	private void construct(ICoverStatus_SwapInternal status) {
		for (int gIndex = 0; gIndex < uncoveredItemSize.length; gIndex++) {
			int[][] temp = status.getUnOnlyCoveredItemForGID(gIndex);
			uncoveredItems[gIndex] = temp[0];
			uncoveredItemSize[gIndex] = temp[0].length;
			onlyCoveredItems[gIndex] = temp[1];
			onlyCoveredItemSize[gIndex] = temp[1].length;
		}
	}

	/**
	 * After this update, the unCoveredItems[qID] is not sorted anymore
	 * 
	 * @param items
	 * @param itemSize
	 * @param gID
	 * @param position
	 */
	private void removeItems(int[][] items, int[] itemSize, int gID,
			int[] position) {
		for (int i = position.length - 1; i >= 0; i--) {
			int pos = position[i];
			int newPos = --itemSize[gID];
			items[gID][pos] = items[gID][newPos];
		}
	}

	/**
	 * After this update, the unCoveredItems[qID] is not sorted anymore
	 * 
	 * @param items
	 * @param itemSize
	 * @param gID
	 * @param value
	 */
	private void appendItems(int[][] items, int[] itemSize, int gID, int[] value) {
		if (itemSize[gID] + value.length > items[gID].length) {
			items[gID] = Arrays.copyOf(items[gID], itemSize[gID] + 2
					* value.length);
		}
		for (int i = 0; i < value.length; i++)
			items[gID][itemSize[gID]++] = value[i];
	}

	// add the sorted value to items[qID], but because sortedValue my overlap
	// with items[qID], need to remove those overlaps.
	private void addItems(int[][] items, int[] itemSize, int gID,
			int[] sortedValue) {
		if (sortedValue.length == 0)
			return;
		int count = sortedValue.length;
		int[] duplicate = Arrays.copyOf(sortedValue, sortedValue.length);
		for (int i = 0; i < itemSize[gID]; i++) {
			int pos = Arrays.binarySearch(sortedValue, items[gID][i]);
			if (pos >= 0) {
				duplicate[pos] = -1;
				count--;
			}
		}
		int[] temp = new int[count];
		for (int index = 0, i = 0; i < sortedValue.length; i++) {
			if (duplicate[i] >= 0)
				temp[index++] = duplicate[i];
		}
		this.appendItems(items, itemSize, gID, temp);
	}

	@Override
	public long getUncoveredItemCount(ISet newSet) {
		long count = 0;
		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			count += getUncoveredItemPositions(group).length;
		}
		return count;
	}

	@Override
	public long getUncoveredItemCount(Item_Group newGroup) {
		return this.getUncoveredItemPositions(newGroup).length;
	}

	private int[] getUncoveredItemPositions(Item_Group newGroup) {
		int gID = newGroup.getItemA();
		int[] noQIDs = newGroup.getNoItemsB();
		return PartialOrderedIntSets.removeGetPosition(uncoveredItems[gID],
				uncoveredItemSize[gID], noQIDs);
	}

	@Override
	public long getOnlyCoveredItemCount(ISet newSet) {
		long count = 0;
		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			count += getOnlycoveredItemPositions(group).length;
		}
		return count;
	}

	private int[] getOnlycoveredItemPositions(Item_Group newGroup) {
		int gID = newGroup.getItemA();
		int[] noQIDs = newGroup.getNoItemsB();
		return PartialOrderedIntSets.removeGetPosition(onlyCoveredItems[gID],
				onlyCoveredItemSize[gID], noQIDs);

	}

	// Brute force scan all status to locate all only covered items
	private int[] getOnlyCoveredItemValue(Item_Group newGroup) {
		int[] temp = new int[status.getQCount() - newGroup.getNoItemsB().length];
		int index = 0;
		for (int[] item : newGroup) {
			if (this.status.isOnlyCovered(item[0], item[1]))
				temp[index++] = item[0];
		}
		return Arrays.copyOf(temp, index);
	}

	@Override
	public long getOnlyCoveredItemCount(Item_Group group) {
		return this.getOnlycoveredItemPositions(group).length;
	}

	@Override
	public void removeFromPos(SetwithScore minSet) {
		this.status.removeFromPos(minSet);

		Set_Pair set = (Set_Pair) minSet.getSet();
		Iterator<Item_Group> it = set.getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			int gID = group.getItemA();
			// due to the remove of the minSet, some previous twice covered
			// items will be append to onlyCovered
			int[] onlyCovered = getOnlyCoveredItemValue(group); // sorted
			// due to the remove of the minSet, some previous only covered items
			// will be removed
			int[] removedPosition = PartialOrderedIntSets.removeGetPosition(
					onlyCoveredItems[gID], onlyCoveredItemSize[gID],
					group.getNoItemsB(), onlyCovered);
			int[] removedValue = new int[removedPosition.length];
			for (int w = 0; w < removedPosition.length; w++)
				removedValue[w] = onlyCoveredItems[gID][removedPosition[w]];

			// those only covered items will be append to uncovered.
			this.appendItems(this.uncoveredItems, uncoveredItemSize, gID,
					removedValue);
			// update the onlyCovered
			this.removeItems(onlyCoveredItems, onlyCoveredItemSize, gID,
					removedPosition);
			this.addItems(this.onlyCoveredItems, onlyCoveredItemSize, gID,
					onlyCovered);
		}
	}

	@Override
	public void addToPos(ISet newSet, int pos) {
		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			int gID = group.getItemA();
			// add a new set, some only covered items need to be removed, they
			// will be covered twice
			this.removeItems(this.onlyCoveredItems, onlyCoveredItemSize, gID,
					getOnlycoveredItemPositions(group));
			// add a new set, some uncovered items need to be removed
			int[] uncoveredPos = getUncoveredItemPositions(group);
			int[] uncoveredValue = new int[uncoveredPos.length];
			for (int w = 0; w < uncoveredPos.length; w++)
				uncoveredValue[w] = uncoveredItems[gID][uncoveredPos[w]];
			this.removeItems(this.uncoveredItems, uncoveredItemSize, gID,
					uncoveredPos);
			// those uncovered will be moved to onlyCovered
			this.appendItems(this.onlyCoveredItems, onlyCoveredItemSize, gID,
					uncoveredValue);
		}

		this.status.addToPos(newSet, pos);
	}

	@Override
	public void addToPos(ISet[] newSets) {
		status.addToPos(newSets);
		this.construct(status);
	}

	@Override
	public long getCoveredItemCount() {
		int qCount = status.getQCount();
		int result = 0;
		for (int gID = 0; gID < this.uncoveredItemSize.length; gID++)
			result += qCount - uncoveredItemSize[gID];
		return result;
	}

	@Override
	public void clear() {
		this.status.clear();
		Arrays.fill(this.uncoveredItemSize, 0);
		Arrays.fill(this.onlyCoveredItemSize, 0);
	}

	@Override
	public long getCoveredItemCountForQ(int qID) {
		return this.status.getCoveredItemCountForQ(qID);
	}

	@Override
	public long getCoveredItemCountForG(int gID) {
		// Different implementation
		return this.status.getGCount() - this.uncoveredItemSize[gID];
	}

	@Override
	public boolean isCovered(int qID, int gID) {
		return this.status.isCovered(qID, gID);
	}

	@Override
	public boolean isOnlyCovered(int qID, int gID) {
		return this.status.isOnlyCovered(qID, gID);
	}

	@Override
	public int getQCount() {
		return this.status.getQCount();
	}

	@Override
	public int getGCount() {
		return this.status.getGCount();
	}

	@Override
	public int[][] getUnOnlyCoveredItemForQID(int qIndex) {
		return status.getUnOnlyCoveredItemForGID(qIndex);
	}

	@Override
	public int[][] getUnOnlyCoveredItemForGID(int gIndex) {
		// Simple implementation
		int[][] temp = new int[2][];
		temp[0] = Arrays.copyOf(uncoveredItems[gIndex],
				uncoveredItemSize[gIndex]);
		temp[1] = Arrays.copyOf(onlyCoveredItems[gIndex],
				onlyCoveredItemSize[gIndex]);
		return temp;
	}

	@Override
	public Map<Integer, Integer> getOnlyCoveredSetPosIDs(ISet minSet) {
		if (status instanceof ICoverStatus_Swap_InvertedIndex) {
			Map<Integer, Integer> result = new HashMap<Integer, Integer>();
			Iterator<Item_Group> it = ((Set_Pair) minSet)
					.getItemGroupIterator();
			while (it.hasNext()) {
				Item_Group group = it.next();
				int gID = group.getItemA();
				int[] pos = getOnlycoveredItemPositions(group);
				for (int i = 0; i < pos.length; i++) {
					int qID = onlyCoveredItems[gID][pos[i]];
					int setID = this.getOnlyCoveredSetPosID(qID, gID);
					if (result.containsKey(setID))
						result.put(setID, result.get(setID) + 1);
					else
						result.put(setID, 1);
				}
			}
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
		if (status instanceof ICoverStatus_Swap_InvertedIndex) {
			return ((ICoverStatus_Swap_InvertedIndex) status)
					.getOnlyCoveredSetPosID(qID, gID);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public int getUnCoveredItemCountForgID(int gID) {
		return this.uncoveredItemSize[gID];
	}
}
