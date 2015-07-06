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

/**
 * Optimized version of the status: It includes: (1) uncoveredItems for each
 * query (2) onlycoveredItems for each query
 * 
 * @author dayuyuan
 * 
 */
public class Status_SubSearchOpt2 implements ICoverStatus_Swap_InvertedIndex {

	private int[][] uncoveredItems;
	private int[] uncoveredItemSize;
	private int[][] onlyCoveredItems;
	private int[] onlyCoveredItemSize;
	private ICoverStatus_SwapInternal status;

	public Status_SubSearchOpt2(ICoverStatus_Swap status) {
		int qSize = ((ICoverStatus_SwapInternal) status).getQCount();
		this.status = (ICoverStatus_SwapInternal) status;
		this.uncoveredItems = new int[qSize][];
		this.uncoveredItemSize = new int[qSize];
		this.onlyCoveredItems = new int[qSize][];
		this.onlyCoveredItemSize = new int[qSize];
		Arrays.fill(uncoveredItemSize, 0);
		Arrays.fill(onlyCoveredItemSize, 0);
	}

	private void construct(ICoverStatus_SwapInternal status) {
		for (int qIndex = 0; qIndex < uncoveredItemSize.length; qIndex++) {
			int[][] temp = status.getUnOnlyCoveredItemForQID(qIndex);
			uncoveredItems[qIndex] = temp[0];
			uncoveredItemSize[qIndex] = temp[0].length;
			onlyCoveredItems[qIndex] = temp[1];
			onlyCoveredItemSize[qIndex] = temp[1].length;
		}
	}

	/**
	 * After this update, the unCoveredItems[qID] is not sorted anymore
	 * 
	 * @param items
	 * @param itemSize
	 * @param qID
	 * @param position
	 */
	private void removeItems(int[][] items, int[] itemSize, int qID,
			int[] position) {
		for (int i = position.length - 1; i >= 0; i--) {
			int pos = position[i];
			int newPos = --itemSize[qID];
			items[qID][pos] = items[qID][newPos];
		}
	}

	/**
	 * After this update, the unCoveredItems[qID] is not sorted anymore
	 * 
	 * @param items
	 * @param itemSize
	 * @param qID
	 * @param value
	 */
	private void appendItems(int[][] items, int[] itemSize, int qID, int[] value) {
		if (itemSize[qID] + value.length > items[qID].length) {
			items[qID] = Arrays.copyOf(items[qID], itemSize[qID] + 2
					* value.length);
		}
		for (int i = 0; i < value.length; i++)
			items[qID][itemSize[qID]++] = value[i];
	}

	// add the sorted value to items[qID], but because sortedValue my overlap
	// with items[qID], need to remove those overlaps.
	private void addItems(int[][] items, int[] itemSize, int qID,
			int[] sortedValue) {
		if (sortedValue.length == 0)
			return;
		int count = sortedValue.length;
		int[] duplicate = Arrays.copyOf(sortedValue, sortedValue.length);
		for (int i = 0; i < itemSize[qID]; i++) {
			int pos = Arrays.binarySearch(sortedValue, items[qID][i]);
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
		this.appendItems(items, itemSize, qID, temp);
	}

	@Override
	public long getUncoveredItemCount(ISet newSet) {
		long count = 0;
		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			count += getUncoveredItemPositions(group).length;
		}
		// also consider the equal cases
		int[] equal = ((Set_Pair) newSet).getValueQEqual();
		for (int qID : equal)
			count += uncoveredItemSize[qID];
		return count;
	}

	@Override
	public long getUncoveredItemCount(Item_Group newGroup) {
		return this.getUncoveredItemPositions(newGroup).length;
	}

	private int[] getUncoveredItemPositions(Item_Group newGroup) {
		int qID = newGroup.getItemA();
		int[] noGIDs = newGroup.getNoItemsB();
		return PartialOrderedIntSets.removeGetPosition(uncoveredItems[qID],
				uncoveredItemSize[qID], noGIDs);
	}

	@Override
	public long getOnlyCoveredItemCount(ISet newSet) {
		long count = 0;
		if (!(newSet instanceof Set_Pair))
			System.out
					.println("Exception in Status_SubSearchOpt2: getOnlyCoveredItemCount");
		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			count += getOnlycoveredItemPositions(group).length;
		}
		// also consider the equal cases
		int[] equal = ((Set_Pair) newSet).getValueQEqual();
		for (int qID : equal)
			count += onlyCoveredItemSize[qID];
		return count;
	}

	private int[] getOnlycoveredItemPositions(Item_Group newGroup) {
		int qID = newGroup.getItemA();
		int[] noGIDs = newGroup.getNoItemsB();
		return PartialOrderedIntSets.removeGetPosition(onlyCoveredItems[qID],
				onlyCoveredItemSize[qID], noGIDs);
	}

	// Brute force scan all status to locate all only covered items
	private int[] getOnlyCoveredItemValue(Item_Group newGroup) {
		int[] temp = new int[status.getGCount() - newGroup.getNoItemsB().length];
		int index = 0;
		for (int[] item : newGroup) {
			if (this.status.isOnlyCovered(item[0], item[1]))
				temp[index++] = item[1];
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
			int qID = group.getItemA();
			// due to the remove of the minSet, some previous twice covered
			// items will be append to onlyCovered
			int[] onlyCovered = getOnlyCoveredItemValue(group); // sorted
			// due to the remove of the minSet, some previous only covered items
			// will be removed
			int[] removedPosition = PartialOrderedIntSets.removeGetPosition(
					onlyCoveredItems[qID], onlyCoveredItemSize[qID],
					group.getNoItemsB(), onlyCovered);
			int[] removedValue = new int[removedPosition.length];
			for (int w = 0; w < removedPosition.length; w++)
				removedValue[w] = onlyCoveredItems[qID][removedPosition[w]];

			// those only covered items will be append to uncovered.
			this.appendItems(this.uncoveredItems, uncoveredItemSize, qID,
					removedValue);
			// update the onlyCovered
			this.removeItems(onlyCoveredItems, onlyCoveredItemSize, qID,
					removedPosition);
			this.addItems(this.onlyCoveredItems, onlyCoveredItemSize, qID,
					onlyCovered);
		}
		int[] equal = set.getValueQEqual();
		for (int qID : equal) {
			int[][] temp = status.getUnOnlyCoveredItemForQID(qID);
			uncoveredItems[qID] = temp[0];
			uncoveredItemSize[qID] = temp[0].length;
			onlyCoveredItems[qID] = temp[1];
			onlyCoveredItemSize[qID] = temp[1].length;
		}

	}

	@Override
	public void addToPos(ISet newSet, int pos) {
		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			int qID = group.getItemA();
			// add a new set, some only covered items need to be removed, they
			// will be covered twice
			this.removeItems(this.onlyCoveredItems, onlyCoveredItemSize, qID,
					getOnlycoveredItemPositions(group));
			// add a new set, some uncovered items need to be removed
			int[] uncoveredPos = getUncoveredItemPositions(group);
			int[] uncoveredValue = new int[uncoveredPos.length];
			for (int w = 0; w < uncoveredPos.length; w++)
				uncoveredValue[w] = uncoveredItems[qID][uncoveredPos[w]];
			this.removeItems(this.uncoveredItems, uncoveredItemSize, qID,
					uncoveredPos);
			// those uncovered will be moved to onlyCovered
			this.appendItems(this.onlyCoveredItems, onlyCoveredItemSize, qID,
					uncoveredValue);
		}
		// also consider the equal cases
		int[] equal = ((Set_Pair) newSet).getValueQEqual();
		for (int qID : equal) {
			// those previous uncovered will be moved to onlyCovered
			this.onlyCoveredItems[qID] = Arrays.copyOf(uncoveredItems[qID],
					uncoveredItemSize[qID]);
			this.onlyCoveredItemSize[qID] = uncoveredItemSize[qID];
			// those previous uncovered will be remove from unCovered
			this.uncoveredItemSize[qID] = 0;
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
		int gCount = status.getGCount();
		long result = 0;
		for (int qID = 0; qID < this.uncoveredItemSize.length; qID++)
			result += gCount - uncoveredItemSize[qID];
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
		// Different implementation
		return this.status.getGCount() - this.uncoveredItemSize[qID];
	}

	@Override
	public long getCoveredItemCountForG(int gID) {
		return this.status.getCoveredItemCountForG(gID);
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
		// Simple implementation
		int[][] temp = new int[2][];
		temp[0] = Arrays.copyOf(uncoveredItems[qIndex],
				uncoveredItemSize[qIndex]);
		temp[1] = Arrays.copyOf(onlyCoveredItems[qIndex],
				onlyCoveredItemSize[qIndex]);
		return temp;
	}

	@Override
	public int[][] getUnOnlyCoveredItemForGID(int gIndex) {
		return status.getUnOnlyCoveredItemForGID(gIndex);
	}

	@Override
	public Map<Integer, Integer> getOnlyCoveredSetPosIDs(ISet minSet) {
		if (status instanceof ICoverStatus_Swap_InvertedIndex) {
			Map<Integer, Integer> result = new HashMap<Integer, Integer>();
			Iterator<Item_Group> it = ((Set_Pair) minSet)
					.getItemGroupIterator();
			while (it.hasNext()) {
				Item_Group group = it.next();
				int qID = group.getItemA();
				int[] pos = getOnlycoveredItemPositions(group);
				for (int i = 0; i < pos.length; i++) {
					int gID = onlyCoveredItems[qID][pos[i]];
					int setID = this.getOnlyCoveredSetPosID(qID, gID);
					if (setID > 0) {
						if (result.containsKey(setID))
							result.put(setID, result.get(setID) + 1);
						else
							result.put(setID, 1);
					}
				}
			}
			// also consider the equal cases
			int[] equal = ((Set_Pair) minSet).getValueQEqual();
			for (int qID : equal) {
				for (int i = 0; i < onlyCoveredItemSize[qID]; i++) {
					int gID = onlyCoveredItems[qID][i];
					int setID = this.getOnlyCoveredSetPosID(qID, gID);
					if (setID > 0) {
						if (result.containsKey(setID))
							result.put(setID, result.get(setID) + 1);
						else
							result.put(setID, 1);
					}
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

	public int getUnCoveredItemCountForQ(int qID) {
		return this.uncoveredItemSize[qID];
	}

}
