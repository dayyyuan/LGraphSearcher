package edu.psu.chemxseer.structure.setcover.IO.interfaces;

/**
 * An InputBucket is a collection of Buckets, Unlike other input that support
 * iteration over all sets, the IInputBucket support iteration of all the
 * buckets.
 * 
 * @author dayuyuan
 * 
 */
public interface IInputBucket extends IInput, Iterable<IBucket> {

	/**
	 * Return the threshold (lowerBound) for a bucket given the bucket ID For
	 * example, the last bucket bucket should return 0
	 * 
	 * @param bID
	 * @return
	 */
	public int getBucketThreshold(int bID);

	/**
	 * Return the total number of buckets
	 * 
	 * @return
	 */
	public int getBucketCount();

	/**
	 * Given the gain, append the feature to the end of a bucket containing the
	 * gain
	 * 
	 * @param feature
	 * @param gain
	 * @return
	 */
	public boolean append(ISet set, long gain);

	/**
	 * This function if specific for the in-memory buckets, for on-disk bucket,
	 * this function is currently not supported, will support later.
	 * 
	 * @param theSet
	 * @param gain
	 */
	public boolean appendWithOrder(ISet theSet, long gain);

	public int getEnumeratedSetCount();

}
