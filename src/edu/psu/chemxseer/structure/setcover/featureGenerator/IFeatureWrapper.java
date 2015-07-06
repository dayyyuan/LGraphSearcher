package edu.psu.chemxseer.structure.setcover.featureGenerator;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;

public interface IFeatureWrapper {
	/**
	 * Return all the database graphs, containing the feature
	 * 
	 * @return
	 */
	public int[] containedDatabaseGraphs();

	/**
	 * Return all the query graphs, containing the feature
	 * 
	 * @return
	 */
	public int[] containedQueryGraphs();

	/**
	 * Given the total number of database graphs, return the database graphs not
	 * containing the feature
	 * 
	 * @param totalDBGraphs
	 * @return
	 */
	public int[] notContainedDatabaseGraphs(int totalDBGraphs);

	/**
	 * Given the total number of queries, return the query graphs not containing
	 * the feature
	 * 
	 * @param totalQueryGraphs
	 * @return
	 */
	public int[] notContainedQueryGraphs(int totalQueryGraphs);

	/**
	 * Return all the database graphs, isomorphic to the feature
	 * 
	 * @return
	 */
	public int[] getEquavalentDatabaseGraphs();

	/**
	 * Return all the query graphs, isomorphic to the features
	 * 
	 * @return
	 */
	public int[] getEquavalentQueryGraphs();

	/**
	 * Return the underlying feature of the coverSet
	 * 
	 * @return
	 */
	public OneFeatureMultiClass getFeature();

	/**
	 * Return the ID of this set
	 * 
	 * @return
	 */
	public int getFetureID();
}
