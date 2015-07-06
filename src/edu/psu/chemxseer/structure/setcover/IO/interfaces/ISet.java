package edu.psu.chemxseer.structure.setcover.IO.interfaces;

import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;

/**
 * A set is a collection of IItems It support (1) Iteration over items (2)
 * Serialization
 * 
 * @author dayuyuan
 * 
 */
public interface ISet extends Iterable<int[]> {
	/**
	 * Return the ID of the set, pay attention, the SetID have to be a value
	 * strictly greater than 0
	 * 
	 * @return
	 */
	public int getSetID();

	/**
	 * Return the total number of itmes in the set (the size of the set)
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Mark this set to be deleted
	 */
	public void lazyDelete();

	/**
	 * Return true if the set is deleted or false otherwise
	 * 
	 * @return
	 */
	public boolean isDeleted();

	/**
	 * Return the feature of this set
	 * 
	 * @return
	 */
	public IFeatureWrapper getFeature();

	public void setSetID(int i);

}
