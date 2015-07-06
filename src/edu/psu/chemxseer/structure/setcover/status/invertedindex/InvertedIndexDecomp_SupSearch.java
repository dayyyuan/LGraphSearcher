package edu.psu.chemxseer.structure.setcover.status.invertedindex;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.status.Util_IntersectionSet;

public class InvertedIndexDecomp_SupSearch extends InvertedIndexDecomp_Abstract {
	// IndexOne contains all the queryGraph -> feature mapping: if queryGraph
	// contains feature
	// IndexTwo contains all the dbGraph -> feature mapping: if dbGraph contains
	// feature
	// a <q, d> pair is covered by the feature, if the feature is not contained
	// in q, but contained in d
	public InvertedIndexDecomp_SupSearch(int classOneNumber, int classTwoNumber) {
		super(classOneNumber, classTwoNumber);
	}

	@Override
	public boolean isCovered(int qID, int gID, int n) {
		if (n < 0)
			return true;
		else if (sizeQ[qID] == 0 || sizeG[gID] == 0)
			return (sizeG[gID] - sizeQ[qID] > n);
		else
			return (Util_IntersectionSet.contain(indexG[gID], sizeG[gID],
					indexQ[qID], sizeQ[qID], n) > n);
	}

	@Override
	public int[] getCoveredSetPosIDs(int qID, int gID, int exceptID) {
		// test whether there exists a feature contained in the first value
		// (query) but not the second value (database graphs)
		if (indexG[gID] == null || sizeG[gID] == 0)
			return new int[0];
		else if (indexQ[qID] == null)
			return Util_IntersectionSet.retain(indexG[gID], sizeG[gID],
					new int[0], sizeQ[qID], exceptID);
		else
			return Util_IntersectionSet.retain(indexG[gID], sizeG[gID],
					indexQ[qID], sizeQ[qID], exceptID);

	}

	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
		int result = -1;

		if (sizeG[gID] - sizeQ[qID] > 2 || sizeG[gID] == 0)
			result = -1; // multiple-coverage or no coverage
		// if sizeG[gID] ==0, then sizeG[gID] -sizeQ[qID] <=0, thus, sizeG[gID]
		// > 0
		else if (sizeQ[qID] == 0)
			result = Util_IntersectionSet.retainSingleValue(indexG[gID],
					sizeG[gID], new int[0], 0);
		else
			result = Util_IntersectionSet.retainSingleValue(indexG[gID],
					sizeG[gID], indexQ[qID], sizeQ[qID]);

		if (result >= 0)
			return result;
		else
			return -1;
	}

	@Override
	protected void removeTheSet(Set_Pair set, int posID) {
		int[] qIDs = set.getValueQ();
		/*
		 * int[] qEqual = set.getValueQEqual(); if(qEqual!=null && qEqual.length
		 * > 0) qIDs = OrderedIntSets.getUnion(qIDs, qEqual);
		 */

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
		/*
		 * int[] qEquals = set.getValueQEqual(); if(qEquals!=null &&
		 * qEquals.length > 0) qIDs = OrderedIntSets.getUnion(qIDs, qEquals);
		 */

		int[] gIDs = set.getValueG();

		for (int qID : qIDs) {
			this.insertValue(qID, posID, indexQ, sizeQ);
		}
		for (int gID : gIDs)
			this.insertValue(gID, posID, indexG, sizeG);
	}
}
