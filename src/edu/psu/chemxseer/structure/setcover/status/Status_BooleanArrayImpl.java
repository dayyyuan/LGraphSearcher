package edu.psu.chemxseer.structure.setcover.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Array;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

/**
 * Implementation of the Status with Boolean Array.
 * 
 * @author dayuyuan
 * 
 */
public class Status_BooleanArrayImpl implements ICoverStatus {
	protected boolean[][] coverStatus;

	public static Status_BooleanArrayImpl newInstance(int qCount, int gCount) {
		return new Status_BooleanArrayImpl(qCount, gCount);
	}

	public static ICoverStatus newInstance(int qCount, int gCount,
			ISet[] edgeSets) {
		Status_BooleanArrayImpl imp = newInstance(qCount, gCount);
		for (ISet oneSet : edgeSets) {
			imp.updateCoverage(oneSet);
		}
		return imp;
	}

	private Status_BooleanArrayImpl(int qCount, int gCount) {
		this.coverStatus = new boolean[qCount][gCount];
		for (int i = 0; i < qCount; i++) {
			Arrays.fill(coverStatus[i], false);
		}
	}

	@Override
	// Since the status is implemented in Boolean Array, which assumes that each
	// set can be stored in-memory
	// therefore when the input is set_pair, what we returned is a itemlist
	// iterator.
	public ISet updateCoverageWithSetReturn(ISet set) {
		List<int[]> items = new ArrayList<int[]>(set.size());
		int qID = 0, gID = 0;
		for (int[] item : set) {
			qID = item[0];
			gID = item[1];
			if (!this.coverStatus[qID][gID]) {
				coverStatus[qID][gID] = true;
				items.add(item);
			}
		}
		return new Set_Pair_Array(items, set.getSetID(), set.getFeature());
	}

	@Override
	public void updateCoverage(ISet oneSet) {
		int qID = 0, gID = 0;
		for (int[] item : oneSet) {
			qID = item[0];
			gID = item[1];
			if (!this.coverStatus[qID][gID])
				coverStatus[qID][gID] = true;
		}
	}

	@Override
	public long getUncoveredItemCount(ISet set) {
		long counter = 0;
		for (int[] item : set) {
			if (!this.coverStatus[item[0]][item[1]])
				counter++;
		}
		return counter;
	}

	@Override
	public long getCoveredItemCount() {
		long counter = 0;
		for (int i = 0; i < coverStatus.length; i++)
			for (int j = 0; j < coverStatus[i].length; j++)
				if (coverStatus[i][j])
					counter++;
		return counter;
	}

	boolean isCovered(int[] item) {
		return this.coverStatus[item[0]][item[1]];
	}

	boolean isCovered(int qID, int gID) {
		return this.coverStatus[qID][gID];
	}

	@Override
	public int getQCount() {
		return this.coverStatus.length;
	}

	@Override
	public int getGCount() {
		return this.coverStatus[0].length;
	}
	/*
	 * @Override public int[] getUnCoveredItemForQID(int qIndex) { int gSize =
	 * this.getGCount(); int[] result = new int[gSize]; int index = 0; for(int i
	 * = 0; i< gSize; i++) if(coverStatus[qIndex][i]) result[index++] = i;
	 * return Arrays.copyOf(result, index); }
	 * 
	 * @Override public int[] getUnCoveredItemForGID(int gIndex) { int qSize =
	 * this.getQCount(); int[] result = new int[qSize]; int index = 0; for(int j
	 * = 0; j< qSize; j++){ if(coverStatus[j][gIndex]) result[index++] = j; }
	 * return Arrays.copyOf(result, index); }
	 */

}
