package edu.psu.chemxseer.structure.postings.Interface;

/**
 * GraphResult interface, with prefix
 * 
 * @author dayuyuan
 * 
 */
public interface IGraphResultPref extends IGraphResult {

	/**
	 * Return the Feature ID of the Prefix Feature The Prefix FeatureID may be
	 * -1
	 * 
	 * @return
	 */
	public int getPrefixFeatureID();

	/**
	 * The Suffix String is represented in DFS code format Return null if the
	 * database graph is exactly the same as its prefix
	 * 
	 * @return
	 */
	public int[][] getSuffix();
}
