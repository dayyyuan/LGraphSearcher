package edu.psu.chemxseer.structure.setcover.status;

import java.util.Arrays;
import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;

public class SupSearchBB implements IBranchBound {
	private int[] gQCount; // for each query "g", what is the number of graphs
							// "q", not containing $g$.

	// need to be updated
	private int[] gCoveredCount; // for each query "g" what is the number of "q"
									// has already been covered

	public SupSearchBB(int[] gQCount, int qCount) {
		this.gQCount = new int[gQCount.length];
		for (int i = 0; i < gQCount.length; i++)
			this.gQCount[i] = qCount - gQCount[i];
		this.gCoveredCount = new int[gQCount.length];
		Arrays.fill(gCoveredCount, 0);
	}

	/**
	 * Given G(p), find the branch upper bound
	 * 
	 * @param Qp
	 * @return
	 */
	@Override
	public boolean isUppBoundSmaller(ISet set, double threshold) {
		if (!(set instanceof Set_Pair))
			throw new ClassCastException();

		int[] Gp = ((Set_Pair) set).getValueG();
		long score = 0;
		for (int gID : Gp) {
			score += gQCount[gID] - gCoveredCount[gID];
			if (score > threshold)
				return false;
			// because of the application, it is guaranteed that
			// gQCount[gID]-gCoveredCount[gID] > 0
		}
		return true;
	}

	@Override
	public void updateAfterInsert(ISet newSet, ICoverStatus_Swap status) {
		if (!(newSet instanceof Set_Pair))
			throw new ClassCastException();

		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			long onlyCoveredCount = status.getOnlyCoveredItemCount(group);
			this.gCoveredCount[group.getItemA()] += onlyCoveredCount;
		}
	}

	@Override
	public void initialize(ICoverStatus_Swap status) {
		for (int gID = 0; gID < this.gCoveredCount.length; gID++) {
			gCoveredCount[gID] = (int) status.getCoveredItemCountForG(gID);
		}
	}

	@Override
	public void updateAfterDelete(ISet oldSet, ICoverStatus_Swap status) {
		if (!(oldSet instanceof Set_Pair))
			throw new ClassCastException();
		Iterator<Item_Group> it = ((Set_Pair) oldSet).getItemGroupIterator();
		while (it.hasNext()) {
			Item_Group group = it.next();
			long uncoveredCount = status.getUncoveredItemCount(group);
			gCoveredCount[group.getItemA()] -= uncoveredCount;
		}
	}

	// @Override
	public int getCoveredCount() {
		int score = 0;
		for (int temp : this.gCoveredCount)
			score += temp;
		return score;
	}

	@Override
	public void clear() {
		Arrays.fill(gCoveredCount, 0);
	}
}
