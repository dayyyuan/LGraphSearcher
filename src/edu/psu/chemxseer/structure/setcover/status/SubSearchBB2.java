package edu.psu.chemxseer.structure.setcover.status;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

/**
 * Branch & Bound Calculator for the Subgraph Search Application: Used together
 * with Decompose Inverted Index
 * 
 * @author dayuyuan
 * 
 */
public class SubSearchBB2 implements IBranchBound {
	private int[] qGCount; // for each query "q", what is the number of graphs
							// "g", not containing $q$.
	private int[] qEquals; // for each query "q", what is the number of other
							// queries that are isomorphic to $q$
	private Status_SubSearchOpt2 status;

	/**
	 * Construct a SubSearch Branch & Bound Calculator
	 * 
	 * @param qGCount
	 * @param qEquals
	 * @param gCount
	 */
	public SubSearchBB2(int[] qGCount, int[] qEquals,
			Status_SubSearchOpt2 status) {
		this.qGCount = new int[qGCount.length];
		int gCount = status.getGCount();

		for (int i = 0; i < qGCount.length; i++)
			this.qGCount[i] = gCount - qGCount[i];

		this.qEquals = new int[qEquals.length];
		for (int i = 0; i < qEquals.length; i++)
			this.qEquals[i] = qEquals[i] + 1;
		this.status = status;
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
		long maxScore = 0;
		int maxScoreQ = -1;
		int gSize = status.getGCount();
		for (int qID : Qp) {
			long tempMaxScore = qEquals[qID]
					* (gSize - status.getCoveredItemCountForQ(qID));
			if (tempMaxScore > maxScore) {
				maxScore = tempMaxScore;
				maxScoreQ = qID;
			}
			score += Math.max(0,
					qGCount[qID] - this.status.getCoveredItemCountForQ(qID));
			if (score > threshold)
				return false;
			// qGCount[qID] may be smaller than qCoveredCount[qID] because qID
			// is covered by a feature isomorphic to it
		}
		// remove double countered
		if (maxScoreQ > 0) {
			score += maxScore;
			score -= qEquals[maxScoreQ]
					* Math.max(0, (qGCount[maxScoreQ] - this.status
							.getCoveredItemCountForQ(maxScoreQ)));
		}
		if (score > threshold)
			return false;
		else
			return true;
	}

	@Override
	public void updateAfterInsert(ISet newSet, ICoverStatus_Swap status) {
		// Do Nothing
	}

	@Override
	public void initialize(ICoverStatus_Swap status) {
		// Do Nothing
	}

	@Override
	public void updateAfterDelete(ISet oldSet, ICoverStatus_Swap status) {
		// Do Nothing
	}

	// @Override
	public long getCoveredCount() {
		return this.status.getCoveredItemCount();
	}

	@Override
	public void clear() {
		// Do Nothing
	}
}
