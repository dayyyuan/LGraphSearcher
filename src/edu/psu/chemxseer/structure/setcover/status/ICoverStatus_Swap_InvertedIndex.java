package edu.psu.chemxseer.structure.setcover.status;

import java.util.Map;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

public interface ICoverStatus_Swap_InvertedIndex extends
		ICoverStatus_SwapInternal {

	/**
	 * For each items contained in minSet, return the set position that only
	 * covered the items. Return all these set positions in an array SetID :
	 * Value
	 * 
	 * @param ISet
	 *            minSet
	 * @return
	 */
	public Map<Integer, Integer> getOnlyCoveredSetPosIDs(ISet minSet);

	/**
	 * Return the only covered set positions covering <qID, gID> pairs
	 * 
	 * @param qID
	 * @param gID
	 * @return
	 */
	public int getOnlyCoveredSetPosID(int qID, int gID);
}
