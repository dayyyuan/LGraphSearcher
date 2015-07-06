package edu.psu.chemxseer.structure.setcover.status;

import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;

/**
 * The Interface for the stream max-coverage status
 * 
 * @author dayuyuan
 * 
 */
public interface ICoverStatus_Swap {

	/**
	 * Return the number of items covered by the newSet only, but not covered by
	 * current selected sets yet
	 * 
	 * @param newSet
	 * @return
	 */
	public long getUncoveredItemCount(ISet newSet);

	/**
	 * Return the number of items covered by the newGroup only, but not covered
	 * by current selected sets yet
	 * 
	 * @param newGroup
	 * @return
	 */
	public long getUncoveredItemCount(Item_Group newGroup);

	/**
	 * Given a selected Set, find the items that are covered by the selected Set
	 * And also, covered once by the status
	 * 
	 * @param selectSet
	 * @return
	 */
	public long getOnlyCoveredItemCount(ISet selectSet);

	/**
	 * Given a selected group of items, find the items that are covered by the
	 * selected group And also, covered once by the status
	 * 
	 * @param next
	 * @return
	 */
	public long getOnlyCoveredItemCount(Item_Group group);

	/**
	 * Scan the status & return the total number of covered items
	 * 
	 * @return
	 */
	public long getCoveredItemCount();

	/**
	 * Get the number of items (qID, x) currently being covered
	 * 
	 * @param qID
	 * @return
	 */
	public long getCoveredItemCountForQ(int qID);

	/**
	 * Get the number of items(x, gID) currently being covered
	 * 
	 * @param gID
	 * @return
	 */
	public long getCoveredItemCountForG(int gID);

	/**
	 * Clean the status record
	 */
	public void clear();

	/********************** The following functions are used to update the index ************************/
	/**
	 * remove minSet from its position
	 * 
	 * @param newSet
	 * @param minSet
	 */
	public void removeFromPos(SetwithScore minSet);

	/**
	 * Add a newSet to a position pos
	 * 
	 * @param newSet
	 * @param pos
	 */
	public void addToPos(ISet newSet, int pos);

	/**
	 * Add an array of newSet to the status.
	 * 
	 * @param selectedSet
	 */
	public void addToPos(ISet[] selectedSet);
}
