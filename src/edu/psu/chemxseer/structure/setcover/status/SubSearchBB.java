package edu.psu.chemxseer.structure.setcover.status;

import java.util.Arrays;
import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;

/**
 * Branch & Bound Calculator for the Subgraph Search Application: Used in
 * accordence with Decompose Inverted Index
 * 
 * @author dayuyuan
 */
public class SubSearchBB implements IBranchBound {
	private int[] qGCount; // for each query "q", what is the number of graphs
							// "g", not containing $q$.
	private int[] qEquals; // for each query "q", what is the number of other
							// queries that are isomorphic to $q$
	private int gSize;

	// need to be updated
	private int[] qCoveredCount; // for each query "q" what is the number of "g"
									// has already been covered

	/**
	 * Construct a SubSearch Branch & Bound Calculator
	 * 
	 * @param qGCount
	 * @param qEquals
	 * @param gCount
	 */
	public SubSearchBB(int[] qGCount, int[] qEquals, int gCount) {
		this.qGCount = new int[qGCount.length];
		for (int i = 0; i < qGCount.length; i++)
			this.qGCount[i] = gCount - qGCount[i];

		this.qEquals = new int[qEquals.length];
		for (int i = 0; i < qEquals.length; i++)
			this.qEquals[i] = qEquals[i] + 1;

		this.gSize = gCount;
		this.qCoveredCount = new int[qGCount.length];
		Arrays.fill(qCoveredCount, 0);
	}

	/**
	 * Given Q(p), find the branch upper bound to computer the upper bound, we
	 * need to know: for the pattern $p$, all queries isomorphic to one of its
	 * super graph $p'$ The maximum number of queries equal to certain $p'$ need
	 * to be known.
	 * 
	 * @param Qp
	 * @return
	 */
	@Override
	public boolean isUppBoundSmaller(ISet set, double threshold) {
		if (!(set instanceof Set_Pair))
			throw new ClassCastException();
		int[] Qp = ((Set_Pair) set).getValueQ();
		long score = 0;
		int maxScore = 0;
		int maxScoreQ = -1;
		for (int qID : Qp) {
			int tempMaxScore = qEquals[qID] * (gSize - qCoveredCount[qID]);
			if (tempMaxScore > maxScore) {
				maxScore = tempMaxScore;
				maxScoreQ = qID;
			}
			score += Math.max(0, qGCount[qID] - qCoveredCount[qID]);
			if (score > threshold)
				return false;
			// qGCount[qID] may be smaller than qCoveredCount[qID] because qID
			// is covered by a feature isomorphic to it
		}
		// remove double countered
		if (maxScoreQ > 0) {
			score += maxScore;
			score -= qEquals[maxScoreQ]
					* (qGCount[maxScoreQ] - qCoveredCount[maxScoreQ]);
		}
		/*
		 * if(score< 0) System.out.println("Must be Something Wrong");
		 */
		if (score > threshold)
			return false;
		else
			return true;
	}

	@Override
	public void updateAfterInsert(ISet newSet, ICoverStatus_Swap status) {
		if (!(newSet instanceof Set_Pair))
			throw new ClassCastException();

		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			long count = status.getOnlyCoveredItemCount(group);
			this.qCoveredCount[group.getItemA()] += count;
		}
		// also consider the equal cases
		int[] equal = ((Set_Pair) newSet).getValueQEqual();
		for (int qID : equal)
			this.qCoveredCount[qID] = this.gSize;
	}

	@Override
	public void initialize(ICoverStatus_Swap status) {
		for (int qID = 0; qID < qCoveredCount.length; qID++) {
			qCoveredCount[qID] = (int) status.getCoveredItemCountForQ(qID);
		}
	}

	@Override
	public void updateAfterDelete(ISet oldSet, ICoverStatus_Swap status) {
		// will be removed items
		if (!(oldSet instanceof Set_Pair))
			throw new ClassCastException();

		Iterator<Item_Group> it = ((Set_Pair) oldSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			int uncoveredCount = (int) status.getUncoveredItemCount(group);
			// gSize-group.getNoItemB().length, denotes all items that oldSet
			// can filtered out
			// coveredCount is the counter of filtered items after oldSet is
			// deleted.
			this.qCoveredCount[group.getItemA()] -= uncoveredCount;
		}
		int[] equal = ((Set_Pair) oldSet).getValueQEqual();
		for (int qID : equal)
			this.qCoveredCount[qID] = (int) status.getCoveredItemCountForQ(qID);
	}

	// @Override
	public int getCoveredCount() {
		int score = 0;
		for (int temp : this.qCoveredCount)
			score += temp;
		return score;
	}

	@Override
	public void clear() {
		Arrays.fill(qCoveredCount, 0);
	}
}
