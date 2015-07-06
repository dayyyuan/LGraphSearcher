package edu.psu.chemxseer.structure.setcover.status.invertedindex;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

/**
 * The interface for the inverted index for the greedy in-memory algorithm The
 * inverted index is mainly account for (qID, gID) level operations
 * 
 * @author dayuyuan
 * 
 */
public interface IInvertedIndex {

	/**
	 * Add the set to the inverted index, with name = posID
	 * 
	 * @param set
	 * @param posID
	 */
	public void addSet(ISet set, int posID);

	/**
	 * Remove the set (with posID) from the inverted index
	 * 
	 * @param set
	 * @param posID
	 */
	public void removeSet(ISet set, int posID);

	/**
	 * Save space of the inverted index
	 */
	public void saveSpace();

	/**
	 * Clear the inverted index
	 */
	public void clear();

	/**
	 * Test whether the item is covered by any set or not Return true if the
	 * item is covered by strictly more than n time Except for the "excpetID"
	 * 
	 * @param item
	 * @param n
	 * @param exceptID
	 * @return
	 */
	public boolean isCovered(int qID, int gID, int n, int exceptID);

	/**
	 * Test whether the item is covered by any set or not Return true if the
	 * item is covered by strictly more than n time
	 * 
	 * @param item
	 * @return
	 */
	public boolean isCovered(int qID, int gID, int n);

	/**
	 * Given the item, return the positions of the sets covering the item,
	 * except for the exceptID
	 * 
	 * @param item
	 * @return
	 */
	public int[] getCoveredSetPosIDs(int qID, int gID, int exceptID);

	/**
	 * Return one and only one set covering the item, except for the exceptID
	 * set If there are no set or multiple sets covering the item, return -1;
	 * 
	 * @param item
	 * @param exceptID
	 * @return
	 */
	public int getOnlyCoveredSetPosID(int qID, int gID);

	/**
	 * Return the number of queries
	 * 
	 * @return
	 */
	public int getQCount();

	/**
	 * Return the number of data base graphs
	 * 
	 * @return
	 */
	public int getGCount();

}
