package edu.psu.chemxseer.structure.setcover.status.invertedindex;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.status.Util_IntersectionSet;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class InvertedIndexDecomp_Classification extends
		InvertedIndexDecomp_Abstract {

	// IndexOne contains all the queryGraph -> feature mapping: if queryGraph
	// contains feature
	// IndexTwo contains all the dbGraph -> feature mapping: if dbGraph contains
	// feature
	public InvertedIndexDecomp_Classification(int qCount, int gCount) {
		super(qCount, gCount);
	}

	@Override
	public boolean isCovered(int qID, int gID, int n) {
		if (n < 0)
			return true;
		else if (sizeQ[qID] == 0 || sizeG[gID] == 0)
			return (sizeQ[qID] + sizeG[gID] > n);
		else {
			int count = Util_IntersectionSet.contain(indexQ[qID], sizeQ[qID],
					indexG[gID], sizeG[gID], n);
			if (count > n)
				return true;
			n -= count;
			return (Util_IntersectionSet.contain(indexG[gID], sizeG[gID],
					indexQ[qID], sizeQ[qID], n) > n);
		}
	}

	@Override
	public int[] getCoveredSetPosIDs(int qID, int gID, int exceptID) {
		// test whether there exists a set containing the first value but not
		// the second value
		int[] resultOne = null;
		if (sizeQ[qID] == 0)
			resultOne = new int[0];
		else if (indexG[gID] == null)
			resultOne = Util_IntersectionSet.retain(indexQ[qID], sizeQ[qID],
					new int[0], sizeG[gID], exceptID);
		else
			resultOne = Util_IntersectionSet.retain(indexQ[qID], sizeQ[qID],
					indexG[gID], sizeG[gID], exceptID);

		int[] resultTwo = null;
		if (sizeG[gID] == 0)
			resultTwo = new int[0];
		else if (indexQ[qID] == null)
			resultTwo = Util_IntersectionSet.retain(indexG[gID], sizeG[gID],
					new int[0], sizeQ[qID], exceptID);
		else
			resultTwo = Util_IntersectionSet.retain(indexG[gID], sizeG[gID],
					indexQ[qID], sizeQ[qID], exceptID);

		if (resultOne.length == 0)
			return resultTwo;
		else if (resultTwo.length == 0)
			return resultOne;
		else {
			return OrderedIntSets.getUnion(resultOne, resultTwo);
		}

	}

	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
		// test whether there exists a set containing the first value but not
		// the second value
		if (sizeQ[qID] - sizeG[gID] > 2 || sizeG[gID] - sizeQ[qID] > 2)
			return -1; // multiple coverage

		int resultOne = -1;
		if (sizeQ[qID] > 0) {
			if (sizeG[gID] == 0)
				resultOne = Util_IntersectionSet.retainSingleValue(indexQ[qID],
						sizeQ[qID], new int[0], sizeG[gID]);
			else
				resultOne = Util_IntersectionSet.retainSingleValue(indexQ[qID],
						sizeQ[qID], indexG[gID], sizeG[gID]);
		}
		if (resultOne == -2)
			return -1; // multiple appearance

		int resultTwo = -1;
		if (sizeG[gID] > 0) {
			if (sizeQ[qID] == 0)
				resultTwo = Util_IntersectionSet.retainSingleValue(indexG[gID],
						sizeG[gID], new int[0], sizeQ[qID]);
			else
				resultTwo = Util_IntersectionSet.retainSingleValue(indexG[gID],
						sizeG[gID], indexQ[qID], sizeQ[qID]);
		}

		if (resultOne == -1 && resultTwo >= 0)
			return resultTwo;
		else if (resultOne >= 0 && resultTwo == -1)
			return resultOne;
		else
			return -1;
	}

	@Override
	protected void removeTheSet(Set_Pair set, int posID) {
		int[] qIDs = set.getValueQ();
		int[] gIDs = set.getValueG();

		for (int qID : qIDs) {
			this.removeValue(qID, posID, indexQ, sizeQ);
		}
		for (int gID : gIDs)
			this.removeValue(gID, posID, indexG, sizeG);
	}

	@Override
	protected void addTheSet(Set_Pair set, int posID) {
		int[] qIDs = set.getValueQ();
		int[] gIDs = set.getValueG();
		for (int qID : qIDs) {
			this.insertValue(qID, posID, indexQ, sizeQ);
		}
		for (int gID : gIDs)
			this.insertValue(gID, posID, indexG, sizeG);
	}
}
