package edu.psu.chemxseer.structure.setcover.status;

import java.util.Set;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

public interface ICoverStatus_HW {
	/**
	 * Return the items covered by theSet but none of uSet
	 * 
	 * @param theSet
	 * @param uSet
	 * @return theSet/ uSet
	 */
	ISet getCoverage(ISet newSet, Set<Integer> uSet);

	/**
	 * Update the status, for the items covered by newSet & lsSet, update the
	 * inverted index to point to "newSet" instead of "lsSet"
	 * 
	 * @param theSet
	 * @param set
	 */
	void updateCoverage(ISet newSet, Set<Integer> uSet);

	/**
	 * Return the part of the selectSet that are covered by itself so far [some
	 * items in selectSet may by recognized as covered by other sets]
	 * 
	 * @param lsSet
	 * @return
	 */
	ISet getCoverage(ISet selectSet);

	/**
	 * Return the total number of covered items so fat
	 * 
	 * @return
	 */
	int getCoveredItemCount();

}
