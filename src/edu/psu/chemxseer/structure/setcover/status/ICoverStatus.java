package edu.psu.chemxseer.structure.setcover.status;

//import edu.psu.chemxseer.structure.setcover.IO.interfaces.IItem;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

/**
 * The Interface for the Status of the max-cover status For In-memory & On-disk
 * Model
 * 
 * @author dayuyuan
 * 
 */
public interface ICoverStatus {
	/**
	 * Given a new set, update the cover status &return items newly covered
	 * 
	 * @param Iterator
	 *            over all the newly covered items
	 * @return
	 */
	public ISet updateCoverageWithSetReturn(ISet newSet);

	/**
	 * Given a new set, update the cover status
	 * 
	 * @param oneSet
	 */
	public void updateCoverage(ISet newSet);

	/**
	 * Given a new set, return # of uncovered items
	 * 
	 * @param set
	 * @return
	 */
	public long getUncoveredItemCount(ISet newSet);

	/**
	 * scan the status again & return the total nubmer of coverd items so far
	 * 
	 * @return
	 */
	public long getCoveredItemCount();

	/**
	 * Return the number of queries
	 * 
	 * @return
	 */
	public int getQCount();

	/**
	 * Return the number of database graphs
	 * 
	 * @return
	 */
	public int getGCount();

}
