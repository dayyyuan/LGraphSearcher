package edu.psu.chemxseer.structure.setcover.status;

import java.util.Arrays;
import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;

/**
 * Fast Version of the SubSearchBB3 Basically the same as SubSearchBB, but the
 * input set is Set_Pair_Index, and the input status should be
 * Status_SubSearchOpt3; The status is used as follows: [1]
 * status.getOnlyCoveredItemCount(group); Because the items in the group is
 * largely reduced, thus, this function runs faster [2]
 * status.getUncoveredItemCount(group); Because the items in the group is
 * largely reduce, thus, this function runs faster as well. [3]
 * status.getCoveredItemCountForQ(qID); This can be further optimized according
 * to the query get removes, there is room for //TODO: optimize it However,
 * here, I just keep the simple way.
 * 
 * @author dayuyuan
 * 
 */
public class SubSearchBB3 implements IBranchBound {
	private int[] qGCount; // for each query "q", what is the number of graphs
							// "g", not containing $q$.
	private int[] qEquals; // for each query "q", what is the number of other
							// queries that are isomorphic to $q$

	private int[] qCoveredCount;
	private int gSize;

	/**
	 * Construct a SubSearch Branch & Bound Calculator
	 * 
	 * @param qGCount
	 * @param qEquals
	 * @param gCount
	 */
	public SubSearchBB3(int[] qGCount, int[] qEquals, int gCount) {
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

	@Override
	public void updateAfterInsert(ISet newSet, ICoverStatus_Swap status) {
		if ((newSet instanceof Set_Pair_Index)) {
			throw new UnsupportedOperationException();
		}
		Set_Pair_Index newSetIndex = (Set_Pair_Index) newSet;
		Iterator<Item_Group> it = newSetIndex.getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			long count = status.getOnlyCoveredItemCount(group);
			this.qCoveredCount[group.getItemA()] += count;
		}
		// also consider the equal cases
		int[] equal = newSetIndex.getValueQEqual();
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
		if (!(oldSet instanceof Set_Pair_Index))
			throw new ClassCastException();
		Iterator<Item_Group> it = ((Set_Pair_Index) oldSet)
				.getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			long uncoveredCount = status.getUncoveredItemCount(group);
			// gSize-group.getNoItemB().length, denotes all items that oldSet
			// can filtered out
			// coveredCount is the counter of filtered items after oldSet is
			// deleted.
			this.qCoveredCount[group.getItemA()] -= uncoveredCount;
		}
		int[] equal = ((Set_Pair_Index) oldSet).getValueQEqual();
		for (int qID : equal)
			this.qCoveredCount[qID] = (int) status.getCoveredItemCountForQ(qID);
	}

	@Override
	public boolean isUppBoundSmaller(ISet set, double threshold) {
		if (!(set instanceof Set_Pair_Index))
			throw new ClassCastException();
		int[] Qp = ((Set_Pair_Index) set).getValueQ();
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
		}
		if (maxScoreQ > 0) {
			score += maxScore;
			score -= qEquals[maxScoreQ]
					* (qGCount[maxScoreQ] - qCoveredCount[maxScoreQ]);
		}
		if (score > threshold)
			return false;
		else
			return true;
	}

	@Override
	public void clear() {
		Arrays.fill(qCoveredCount, 0);
	}
}
