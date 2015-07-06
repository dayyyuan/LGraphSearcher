package edu.psu.chemxseer.structure.setcover.status.invertedindex;

import java.util.Arrays;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

public class InvertedIndexArray implements IInvertedIndex {
	private int[][][] invertedIndex; // invertedIndex[item][] = sets containing
										// the item
	private int[][] invertedIndexSize;

	/**
	 * An inverted index with an entry -10, means that this entry is covered by
	 * some fixed sets
	 * 
	 * @param universeSize
	 */
	private InvertedIndexArray(int qCount, int gCount) {
		this.invertedIndex = new int[qCount][gCount][];
		this.invertedIndexSize = new int[qCount][gCount];
		for (int i = 0; i < invertedIndexSize.length; i++)
			Arrays.fill(invertedIndexSize[i], 0);
	}

	/**
	 * Create an empty inverted index
	 * 
	 * @param universeSize
	 * @return
	 */
	public static InvertedIndexArray newEmptyInstance(int qCount, int gCount) {
		return new InvertedIndexArray(qCount, gCount);
	}

	/**
	 * Given an input of sets, create an inverted index of those sets
	 * 
	 * @param input
	 * @return
	 */
	public static InvertedIndexArray newInstance(IInputSequential input) {
		InvertedIndexArray index = new InvertedIndexArray(input.getQCount(),
				input.getGCount());
		for (ISet set : input)
			index.addSet(set, set.getSetID());
		index.saveSpace();
		return index;
	}

	@Override
	public void addSet(ISet set, int posID) {
		for (int[] item : set) {
			this.insertValue(item, posID);
		}
	}

	@Override
	public void removeSet(ISet set, int posID) {
		for (int[] item : set) {
			this.removeValue(item, posID);
		}
	}

	@Override
	public int[] getCoveredSetPosIDs(int qID, int gID, int exceptID) {
		if (this.invertedIndexSize[qID][gID] == 0)
			return new int[0]; // no inverted index
		else if (this.invertedIndexSize[qID][gID] == 1
				&& invertedIndex[qID][gID][0] == exceptID)
			return new int[0];

		assert (invertedIndex[qID][gID] != null); // if the inverted index is
													// maintained correctly
		int[] result = null;
		if (exceptID < 0)
			result = Arrays.copyOf(this.invertedIndex[qID][gID],
					invertedIndexSize[qID][gID]);
		else {
			int pos = this.linearSearch(invertedIndex[qID][gID],
					invertedIndexSize[qID][gID], exceptID);
			if (pos >= 0) {
				result = new int[invertedIndexSize[qID][gID]];
				for (int i = 0; i < pos; i++)
					result[i] = invertedIndex[qID][gID][i];
				for (int i = pos + 1; i < invertedIndexSize[qID][gID]; i++)
					result[i - 1] = invertedIndex[qID][gID][i];

			} else
				result = Arrays.copyOf(this.invertedIndex[qID][gID],
						invertedIndexSize[qID][gID]); // does not found exceptID
		}

		if (result.length > 0 && result[0] < 0)
			return Arrays.copyOfRange(result, 1, result.length);
		else
			return result;
	}

	/**
	 * Return the only one set covering the item (except for exceptID). If there
	 * are multiple sets covering the item or no set, return -1;
	 * 
	 * @param item
	 * @param exceptID
	 * @return
	 */
	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
		if (this.invertedIndexSize[qID][gID] > 0
				&& invertedIndex[qID][gID][0] == -10)
			return -1; // based on the assumption that the "item" belongs to a
						// new sets.
		else if (this.invertedIndexSize[qID][gID] == 1)
			return invertedIndex[qID][gID][0];
		return -1;
	}

	@Override
	public boolean isCovered(int qID, int gID, int n, int exceptID) {
		if (this.invertedIndexSize[qID][gID] - 1 > n)
			return true;
		int counter = 0;
		for (int i = 0; i < invertedIndexSize[qID][gID]; i++) {
			if (invertedIndex[qID][gID][i] != exceptID) {
				counter++;
				if (counter > n)
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCovered(int qID, int gID, int n) {
		return (this.invertedIndexSize[qID][gID] > n);
	}

	/********************** private algorithms ***********************/

	private void insertValue(int[] item, int sID) {
		int qID = item[0];
		int gID = item[1];
		if (this.invertedIndex[qID][gID] == null) {
			invertedIndex[qID][gID] = new int[2]; // initial size to be 2
			invertedIndexSize[qID][gID] = 0;
		} else if (this.invertedIndex[qID][gID].length == invertedIndexSize[qID][gID]) {
			int[] temp = Arrays.copyOf(invertedIndex[qID][gID],
					invertedIndexSize[qID][gID] * 2);
			invertedIndex[qID][gID] = temp;
		}
		// update the inverted index by appending the fID
		invertedIndex[qID][gID][invertedIndexSize[qID][gID]++] = sID;
	}

	private void removeValue(int[] item, int sID) {
		int qID = item[0];
		int gID = item[1];
		if (invertedIndexSize[qID][gID] == 0) {
			System.out.println("error in InvertedINdex:removeValue"); // TODO:
																		// throw
																		// an
																		// exception
			return;
		}
		// Shrink the size of the array
		else if (2 * invertedIndexSize[qID][gID] == invertedIndex[qID][gID].length
				&& invertedIndex[qID][gID].length > 2) {
			int[] temp = new int[invertedIndexSize[qID][gID]];
			for (int i = 0, iter = 0; i < invertedIndexSize[qID][gID]; i++)
				if (invertedIndex[qID][gID][i] != sID)
					temp[iter++] = invertedIndex[qID][gID][i];
			invertedIndex[qID][gID] = temp;
			invertedIndexSize[qID][gID]--;
		} else {
			// do the deletion
			int pos = linearSearch(invertedIndex[qID][gID],
					invertedIndexSize[qID][gID], sID);
			if (pos == -1) {
				pos = linearSearch(invertedIndex[qID][gID],
						invertedIndexSize[qID][gID], sID);
				System.out.println("error in removevalue, not such value");
				return; // TODO: throw exceptions
			}
			if (pos != invertedIndexSize[qID][gID] - 1) {
				// sID is not the last: swap the fID with the last value, to
				// delete the element sID
				this.invertedIndex[qID][gID][pos] = invertedIndex[qID][gID][invertedIndexSize[qID][gID] - 1];
			}
			invertedIndexSize[qID][gID]--;
			;
		}
	}

	private int linearSearch(int[] array, int boundary, int value) {
		if (array == null)
			return -1;
		else {
			for (int i = 0; i < boundary; i++)
				if (array[i] == value)
					return i;
			return -1;
		}
	}

	@Override
	public void saveSpace() {
		for (int i = 0; i < this.invertedIndexSize.length; i++) {
			for (int j = 0; j < this.invertedIndexSize[i].length; j++) {
				if (this.invertedIndexSize[i][j] == 0)
					this.invertedIndex[i][j] = null;
				else if (this.invertedIndexSize[i][j] < this.invertedIndex[i][j].length) {
					invertedIndex[i][j] = Arrays.copyOf(
							this.invertedIndex[i][j],
							this.invertedIndexSize[i][j]);
				}
			}
		}
	}

	@Override
	public void clear() {
		for (int i = 0; i < this.invertedIndexSize.length; i++) {
			Arrays.fill(invertedIndexSize[i], 0);
		}
		this.saveSpace();
	}

	public long getCoveredItemCount() {
		long count = 0;
		for (int i = 0; i < this.invertedIndexSize.length; i++) {
			for (int j = 0; j < this.invertedIndexSize[i].length; j++)
				if (invertedIndexSize[i][j] > 0)
					count++;
		}
		return count;
	}

	@Override
	public int getQCount() {
		return this.invertedIndexSize.length;
	}

	@Override
	public int getGCount() {
		return this.invertedIndexSize[0].length;
	}

}
