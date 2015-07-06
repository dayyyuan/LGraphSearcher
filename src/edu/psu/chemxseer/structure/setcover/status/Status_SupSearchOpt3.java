package edu.psu.chemxseer.structure.setcover.status;

import java.util.Map;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;

/**
 * An wrapper of normal status This class is designed to make sure that all the
 * input set are guaranteed to be of class: Set_Pair_Index, so that it can run
 * relatively faster.
 * 
 * @author dayuyuan
 * 
 */
public class Status_SupSearchOpt3 implements ICoverStatus_Swap_InvertedIndex {
	ICoverStatus_SwapInternal status;

	public Status_SupSearchOpt3(ICoverStatus_Swap status) {
		this.status = (ICoverStatus_SwapInternal) status;
	}

	@Override
	public Map<Integer, Integer> getOnlyCoveredSetPosIDs(ISet minSet) {
		if (!(minSet instanceof Set_Pair_Index))
			throw new ClassCastException();
		else if (!(status instanceof ICoverStatus_Swap_InvertedIndex))
			throw new UnsupportedOperationException();
		return ((ICoverStatus_Swap_InvertedIndex) status)
				.getOnlyCoveredSetPosIDs(minSet);
	}

	@Override
	public int getOnlyCoveredSetPosID(int qID, int gID) {
		return ((ICoverStatus_Swap_InvertedIndex) this.status)
				.getOnlyCoveredSetPosID(qID, gID);
	}

	@Override
	public long getUncoveredItemCount(ISet newSet) {
		if (!(newSet instanceof Set_Pair_Index))
			throw new ClassCastException();
		return this.status.getUncoveredItemCount(newSet);

	}

	@Override
	public long getUncoveredItemCount(Item_Group newGroup) {
		return this.status.getUncoveredItemCount(newGroup);
	}

	@Override
	public long getOnlyCoveredItemCount(ISet selectSet) {
		if (!(selectSet instanceof Set_Pair_Index))
			throw new ClassCastException();
		return this.status.getOnlyCoveredItemCount(selectSet);
	}

	@Override
	public long getOnlyCoveredItemCount(Item_Group group) {
		return this.getOnlyCoveredItemCount(group);
	}

	@Override
	public long getCoveredItemCountForQ(int qID) {
		return this.getCoveredItemCountForQ(qID);
	}

	@Override
	public long getCoveredItemCountForG(int gID) {
		return this.getCoveredItemCountForG(gID);
	}

	// **********************The Rest Functions keep the Same
	// *********************/

	@Override
	public long getCoveredItemCount() {
		return status.getCoveredItemCount();
	}

	@Override
	public void clear() {
		this.status.clear();
	}

	@Override
	public void removeFromPos(SetwithScore minSet) {
		this.status.removeFromPos(minSet);
	}

	@Override
	public void addToPos(ISet newSet, int pos) {
		this.status.addToPos(newSet, pos);
	}

	@Override
	public void addToPos(ISet[] selectedSet) {
		this.status.addToPos(selectedSet);
	}

	@Override
	public int[][] getUnOnlyCoveredItemForQID(int qIndex) {
		return status.getUnOnlyCoveredItemForQID(qIndex);
	}

	@Override
	public int[][] getUnOnlyCoveredItemForGID(int gIndex) {
		return status.getUnOnlyCoveredItemForGID(gIndex);
	}

	@Override
	public boolean isCovered(int qID, int gID) {
		return status.isCovered(qID, gID);
	}

	@Override
	public boolean isOnlyCovered(int qID, int gID) {
		return status.isOnlyCovered(qID, gID);
	}

	@Override
	public int getQCount() {
		return status.getQCount();
	}

	@Override
	public int getGCount() {
		return status.getGCount();
	}
}
