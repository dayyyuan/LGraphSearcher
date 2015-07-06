package edu.psu.chemxseer.structure.setcover.interfaces;

import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;

/**
 * The interface for the max-coverage solver
 * 
 * @author dayuyuan
 * 
 */
public interface IMaxCoverSolver {
	/**
	 * Given the input "K", return the "K" top set (ID) that covers the maximum
	 * number of items. This algorithm has to return the average current memory
	 * consumption as well
	 * 
	 * @param K
	 * @param stat
	 *            [0] Total Mining Time: T [1] Time for Exclusive Exam [2] Time
	 *            for Update [3] Time for Score Update [4] Space after running
	 *            [5] Average Space [6] Total number of patterns enumerated [7]
	 *            Number of Swaps [8] Before Coverage [9] After Coverage
	 * @return
	 */
	public IFeatureWrapper[] runGreedy(int K, float[] stat);

	/**
	 * Return the total number of covered items by the top K features
	 * 
	 * @return
	 */
	public long coveredItemsCount();

	/**
	 * Re scan the status, and return the count
	 * 
	 * @return
	 */
	public long realCoveredItemCount();

	/**
	 * Get the fixed selected features
	 * 
	 * @return
	 */
	public IFeatureWrapper[] getFixedSelected();

	/**
	 * Return the total number of patterns got enumerated
	 * 
	 * @return
	 */
	public long getEnumeratedPatternNum();
}
