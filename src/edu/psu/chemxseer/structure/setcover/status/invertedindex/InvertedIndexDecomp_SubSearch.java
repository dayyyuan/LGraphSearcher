package edu.psu.chemxseer.structure.setcover.status.invertedindex;

import java.util.Arrays;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.status.Util_IntersectionSet;

public class InvertedIndexDecomp_SubSearch extends InvertedIndexDecomp_Abstract {
	// For each query "q", index the feature (fID) that q == f
	private int[] qfEqual;

	public InvertedIndexDecomp_SubSearch(int qCount, int gCount) {
		super(qCount, gCount);
		this.qfEqual = new int[qCount];
		Arrays.fill(qfEqual, -1);
		// valueOne : queries
		// valueTwo : database graphs
	}

	@Override
	public boolean isCovered(int qID, int gID, int n) {
		// test whether there exists a set containing the first value but not
		// the second value
		if (qfEqual[qID] >= 0)
			n = n - 1;
		if (n < 0)
			return true;
		else if (sizeQ[qID] == 0 || sizeG[gID] == 0)
			return (sizeQ[qID] - sizeG[gID] > n);
		else
			return (Util_IntersectionSet.contain(indexQ[qID], sizeQ[qID],
					indexG[gID], sizeG[gID], n) > n);
	}

	@Override
	public int[] getCoveredSetPosIDs(int qID, int gID, int exceptID) {
		// test whether there exists a set containing the first value but not
		// the second value
		int[] result = null;
		if (sizeQ[qID] == 0)
			result = new int[0];
		else if (indexG[gID] == null)
			result = Util_IntersectionSet.retain(indexQ[qID], sizeQ[qID],
					new int[0], sizeG[gID], exceptID);
		else
			result = Util_IntersectionSet.retain(indexQ[qID], sizeQ[qID],
					indexG[gID], sizeG[gID], exceptID);

		if (this.qfEqual[qID] >= 0 & qfEqual[qID] != exceptID) {
			result = Arrays.copyOf(result, result.length + 1);
			result[result.length - 1] = qfEqual[qID];
		}
		return result;
	}

	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
		// test equivalence:
		int result = -1;
		int equalResult = -1;
		if (this.qfEqual[qID] >= 0)
			equalResult = qfEqual[qID];

		// test whether there exists a set containing the first value but not
		// the second value
		if (equalResult >= 0) {
			if (sizeQ[qID] - sizeG[gID] > 1) // there may be the case that
												// sizeQ[qID]-sizeG[gID] =
												// exceptID
				return -1; // multiple coverage
			else if (sizeQ[qID] == 0)
				return equalResult;
		} else if (sizeQ[qID] == 0 || sizeQ[qID] - sizeG[gID] > 2)
			return -1; // no coverage or multiple coverage

		// sizeQ[qID] > 0 guaranteed
		if (sizeG[gID] == 0)
			result = Util_IntersectionSet.retainSingleValue(indexQ[qID],
					sizeQ[qID], new int[0], sizeG[gID]);
		else
			result = Util_IntersectionSet.retainSingleValue(indexQ[qID],
					sizeQ[qID], indexG[gID], sizeG[gID]);

		if (result == -1 && equalResult != -1)
			return equalResult;
		else if (result >= 0 && equalResult == -1)
			return result;
		else
			return -1;
	}

	@Override
	protected void removeTheSet(Set_Pair set, int posID) {
		int[] qIDs = set.getValueQ();
		int[] gIDs = set.getValueG();
		int[] qEquals = set.getValueQEqual();
		for (int qID : qIDs) {
			this.removeValue(qID, posID, indexQ, sizeQ);
		}
		for (int value3 : qEquals)
			qfEqual[value3] = -1;
		for (int gID : gIDs)
			this.removeValue(gID, posID, indexG, sizeG);
	}

	@Override
	protected void addTheSet(Set_Pair set, int posID) {
		int[] qIDs = set.getValueQ();
		int[] gIDs = set.getValueG();
		int[] qEquals = set.getValueQEqual();
		for (int qID : qIDs) {
			this.insertValue(qID, posID, indexQ, sizeQ);
		}
		for (int value3 : qEquals)
			qfEqual[value3] = posID;
		for (int gID : gIDs)
			this.insertValue(gID, posID, indexG, sizeG);
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(qfEqual, -1);
	}

	/*
	 * public int getOnlyCoveredSetPosID(IItem item, int exceptID) { int temp =
	 * getOnlyCoveredSetPosIDReal(item, exceptID); int[] temp2 =
	 * this.getCoveredSetPosIDs(item, exceptID); if(temp == -1 && temp2.length==
	 * 1){ System.out.println("wan du zi"); temp =
	 * getOnlyCoveredSetPosIDReal(item, exceptID); } else if(temp >= 0 &&
	 * temp2.length !=1) System.out.println("wan du zi 2"); else if(temp >= 0 &&
	 * temp2.length == 1 && temp!=temp2[0]) System.out.println("guile"); return
	 * temp; }
	 */
}
