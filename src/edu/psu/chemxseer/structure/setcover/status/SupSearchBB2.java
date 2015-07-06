package edu.psu.chemxseer.structure.setcover.status;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

public class SupSearchBB2 implements IBranchBound {

	private int[] gQCount; // for each query "g", what is the number of graphs
							// "q", not containing $g$.
	private Status_SupSearchOpt2 status;

	public SupSearchBB2(int[] gQCount, Status_SupSearchOpt2 status) {
		int qCount = status.getQCount();
		this.gQCount = new int[gQCount.length];
		for (int i = 0; i < gQCount.length; i++)
			this.gQCount[i] = qCount - gQCount[i];
		this.status = status;
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
			score += gQCount[gID] - this.status.getCoveredItemCountForG(gID);
			if (score > threshold)
				return false;
		}
		return true;
	}

	@Override
	public void updateAfterInsert(ISet newSet, ICoverStatus_Swap status) {
		// Do nothing
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
