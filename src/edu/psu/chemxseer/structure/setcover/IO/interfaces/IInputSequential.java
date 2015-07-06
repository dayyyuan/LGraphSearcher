package edu.psu.chemxseer.structure.setcover.IO.interfaces;

/**
 * An input that support only sequential read of all the sets
 * 
 * @author dayuyuan
 * 
 */
public interface IInputSequential extends IInput, Iterable<ISet> {

	/**
	 * Given the set of selected features, Store the CoverSet_FeatureWrapper to
	 * Disk
	 * 
	 * @param selectedFeatureIDs
	 * @param selectedFeatureFile
	 */
	public void storeSelected(int[] selectedFeatureIDs,
			String selectedFeatureFile);

	/**
	 * Return the set of distinct edges
	 * 
	 * @return
	 */
	public ISet[] getEdgeSets();

	/**
	 * Return the number of enumerated set
	 * 
	 * @return
	 */
	public long getEnumeratedSetCount();

	/**
	 * Return the time for pattern enumeration
	 * 
	 * @return
	 */
	public long getPatternEnumerationTime();

}
