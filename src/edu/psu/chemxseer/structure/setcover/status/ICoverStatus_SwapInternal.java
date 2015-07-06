package edu.psu.chemxseer.structure.setcover.status;

interface ICoverStatus_SwapInternal extends ICoverStatus_Swap {

	/****************** The following functions are internal used only, by other status *******************/
	/**
	 * Return the gIDs, such that (qID, gID) is not covered
	 * 
	 * @param qIndex
	 * @return
	 */
	int[][] getUnOnlyCoveredItemForQID(int qIndex);

	/**
	 * Return the qIDs, such that (qID, gID) is not covered
	 * 
	 * @param gIndex
	 * @return
	 */
	int[][] getUnOnlyCoveredItemForGID(int gIndex);

	/**
	 * Test whether the item (qID, gID) is covered or not
	 * 
	 * @param qID
	 * @param gID
	 * @return
	 */
	boolean isCovered(int qID, int gID);

	/**
	 * Test whether the item (qID, gID) is only covered or not
	 * 
	 * @param qID
	 * @param gID
	 * @return
	 */
	boolean isOnlyCovered(int qID, int gID);

	/**
	 * Return the number of queries
	 * 
	 * @return
	 */
	int getQCount();

	/**
	 * Return the number of graphs
	 * 
	 * @return
	 */
	int getGCount();
}
