package edu.psu.chemxseer.structure.setcover.status.invertedindex;

import java.util.Arrays;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;

/**
 * The inverted Index in the decomposition form
 * 
 * @author dayuyuan
 * 
 */
public abstract class InvertedIndexDecomp_Abstract implements IInvertedIndex {
	protected int[][] indexQ;
	protected int[] sizeQ;
	protected int[][] indexG;
	protected int[] sizeG;

	// protected int[] qCovered; // qCovered[i] records the number of pairs
	// <qCovered>, covered...
	// protected int qCoveredMinFeatureID;

	protected InvertedIndexDecomp_Abstract(int qCount, int gCount) {
		this.indexQ = new int[qCount][];
		this.sizeQ = new int[qCount];
		Arrays.fill(sizeQ, 0);
		this.indexG = new int[gCount][];
		this.sizeG = new int[gCount];
		Arrays.fill(sizeG, 0);
	}

	public static IInvertedIndex newEmptyInstance(int qCount, int gCount,
			AppType aType) {
		if (aType == AppType.subSearch)
			return new InvertedIndexDecomp_SubSearch(qCount, gCount);
		else if (aType == AppType.supSearch)
			return new InvertedIndexDecomp_SupSearch(qCount, gCount);
		else
			return new InvertedIndexDecomp_Classification(qCount, gCount);

	}

	public static IInvertedIndex newInstance(IInputSequential input,
			AppType aType) {
		IInvertedIndex index = newEmptyInstance(input.getQCount(),
				input.getGCount(), aType);
		for (ISet set : input)
			index.addSet(set, set.getSetID());
		index.saveSpace();
		return index;
	}

	@Override
	public void addSet(ISet set, int posID) {
		if (set instanceof SetwithScore)
			set = ((SetwithScore) set).getSet();
		if (!(set instanceof Set_Pair)) {
			System.out
					.println("Wrong input format of InvertedIndexDecomp:addSet");
			return;
		}
		this.addTheSet((Set_Pair) set, posID);
	}

	protected abstract void addTheSet(Set_Pair set, int posID);

	@Override
	public void removeSet(ISet set, int posID) {
		if (!(set instanceof Set_Pair)) {
			System.out
					.println("Wrong input format of InvertedIndexDecomp:removeSet");
			return;
		}
		this.removeTheSet((Set_Pair) set, posID);
	}

	protected abstract void removeTheSet(Set_Pair set, int posID);

	/********************** Private Functions **************************/
	// The insertion function is different from that of normal inverted index
	// in normal inverted index, the order does not need to be maintained
	// however, in the decomposition algorithm, since we need to intersect/join
	protected void insertValue(int item, int pos, int[][] invertedIndex,
			int[] invertedIndexSize) {
		if (invertedIndex[item] == null) {
			invertedIndex[item] = new int[2]; // initial size = 2
			invertedIndex[item][0] = invertedIndex[item][1] = -1;
			invertedIndex[item][invertedIndexSize[item]++] = pos;
		} else if (invertedIndexSize[item] == invertedIndex[item].length) {
			int[] newEntry = new int[invertedIndexSize[item] * 2];
			Arrays.fill(newEntry, -1);
			int i = 0;
			for (; i < invertedIndexSize[item] && invertedIndex[item][i] < pos; i++)
				newEntry[i] = invertedIndex[item][i];
			newEntry[i] = pos;
			for (; i < invertedIndexSize[item]; i++)
				newEntry[i + 1] = invertedIndex[item][i];
			invertedIndex[item] = newEntry;
			invertedIndexSize[item]++;
		} else {
			// 1. Binary Search the position of "value"
			int index = Arrays.binarySearch(invertedIndex[item], 0,
					invertedIndexSize[item], pos);
			// index of the search key, if it is contained in the array within
			// the specified range; otherwise, (-(insertion point) - 1).
			index = -index - 1;
			// 2. Insert the new value
			if (index >= 0) {
				for (int i = invertedIndexSize[item] - 1; i >= index; i--)
					invertedIndex[item][i + 1] = invertedIndex[item][i];
				invertedIndex[item][index] = pos;
				invertedIndexSize[item]++;
			}
		}
	}

	protected void removeValue(int item, int pos, int[][] invertedIndex,
			int[] invertedIndexSize) {
		if (invertedIndexSize[item] == 1)
			invertedIndexSize[item] = 0;
		else if (invertedIndex[item].length > 2
				&& 2 * invertedIndexSize[item] == invertedIndex[item].length) {
			int[] newEntry = new int[invertedIndexSize[item]];
			Arrays.fill(newEntry, -1);
			int iter = 0;
			for (int i = 0; i < invertedIndexSize[item]; i++) {
				if (invertedIndex[item][i] == pos)
					continue;
				else
					newEntry[iter++] = invertedIndex[item][i];
			}
			invertedIndex[item] = newEntry;
			invertedIndexSize[item]--;
			assert (iter == invertedIndexSize[item]);
		} else {
			// 1. Binary Search the position of "value"
			int index = Arrays.binarySearch(invertedIndex[item], 0,
					invertedIndexSize[item], pos);
			if (index < 0)// the inverted index should contain the value "pos"
				System.out.println("stop");
			// 2. Remove the Value: maintain the order
			for (int i = index + 1; i < invertedIndexSize[item]; i++)
				invertedIndex[item][i - 1] = invertedIndex[item][i];
			invertedIndexSize[item]--;
		}
	}

	/**
	 * reduce the space consumption of the inverted index, when the inverted
	 * index is built stable.
	 */
	@Override
	public void saveSpace() {
		for (int i = 0; i < sizeQ.length; i++) {
			if (sizeQ[i] == 0)
				indexQ[i] = null;
			else if (indexQ[i].length > sizeQ[i])
				indexQ[i] = Arrays.copyOf(indexQ[i], sizeQ[i]);
		}
		for (int i = 0; i < sizeG.length; i++) {
			if (sizeG[i] == 0)
				indexG[i] = null;
			else if (indexG[i].length > sizeG[i]) {
				indexG[i] = Arrays.copyOf(indexG[i], sizeG[i]);
			}
		}
	}

	@Override
	public void clear() {
		Arrays.fill(sizeQ, 0);
		Arrays.fill(sizeG, 0);
		this.saveSpace();
	}

	@Override
	public boolean isCovered(int qID, int gID, int n, int exceptID) {
		int[] pos = this.getCoveredSetPosIDs(qID, gID, exceptID);
		return (pos.length > n);
	}

	@Override
	public int getQCount() {
		return this.indexQ.length;
	}

	@Override
	public int getGCount() {
		return this.indexG.length;
	}
}
