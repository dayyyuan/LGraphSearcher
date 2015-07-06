package edu.psu.chemxseer.structure.setcover.featureGenerator;

import java.io.Serializable;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * A simple feature wrapper representing one feature
 * 
 * @author dayuyuan
 * 
 */
public class FeatureWrapperSimple implements IFeatureWrapper, Serializable {

	private static final long serialVersionUID = -8803426884427598546L;
	private OneFeatureMultiClass feature;
	private int[][] postings;

	public FeatureWrapperSimple(int fID, Graph g, int[] containedDB,
			int[] equalDB, int[] containedQuery, int[] equalQuery) {
		int[] frequency = new int[] { containedDB.length, equalDB.length,
				containedQuery.length, equalQuery.length };
		long[] shift = new long[4];
		this.feature = new OneFeatureMultiClass(MyFactory.getUnCanDFS()
				.serialize(g), frequency, shift, fID, true);
		this.postings = new int[4][];
		postings[0] = containedDB;
		postings[1] = equalDB;
		postings[2] = containedQuery;
		postings[3] = equalQuery;
	}

	public FeatureWrapperSimple(PostingFeaturesMultiClass features, int fID) {
		this.feature = features.getMultiFeatures().getFeature(fID);
		this.postings = features.getPosting(fID);
	}

	/**
	 * Construct a FeatureWrapper given the "feature" & its four postings Assume
	 * that the postings are sorted
	 * 
	 * @param feature2
	 * @param thePostings
	 *            :
	 */
	public FeatureWrapperSimple(OneFeatureMultiClass feature2,
			int[][] thePostings) {
		this.feature = feature2;
		this.postings = thePostings;
	}

	/*
	 * public FeatureWrapperSimple(FeatureWrapperSimple featureWrapper, int[]
	 * gMap, int[] qMap) { this.feature = featureWrapper.feature; this.postings
	 * = new int[4][]; int[][] oldPostings = featureWrapper.postings; for(int i
	 * = 0; i< 2; i++){ postings[i] = new int[oldPostings[i].length]; for(int j
	 * = 0; j< postings[i].length; j++) postings[i][j] =
	 * gMap[oldPostings[i][j]]; } for(int i = 2; i<4; i++){ postings[i] = new
	 * int[oldPostings[i].length]; for(int j = 0; j< postings[i].length; j++)
	 * postings[i][j] = qMap[oldPostings[i][j]]; } }
	 */

	/**
	 * Return all the database graphs, containing the feature
	 * 
	 * @return
	 */
	@Override
	public int[] containedDatabaseGraphs() {
		return this.postings[0];
	}

	/**
	 * Return all the query graphs, containing the feature
	 * 
	 * @return
	 */
	@Override
	public int[] containedQueryGraphs() {
		return this.postings[2];
	}

	/**
	 * Given the total number of database graphs, return the database graphs not
	 * containing the feature
	 * 
	 * @param totalDBGraphs
	 * @return
	 */
	@Override
	public int[] notContainedDatabaseGraphs(int totalDBGraphs) {
		/*
		 * int[] result = null; if(this.postings[1].length == 0) result =
		 * OrderedIntSets.getCompleteSet(this.postings[0], totalDBGraphs); else{
		 * int[] temp = OrderedIntSets.getUnion(this.postings[0], postings[1]);
		 * result = OrderedIntSets.getCompleteSet(temp, totalDBGraphs); }
		 */
		try {
			int[] result = OrderedIntSets.getCompleteSet(this.postings[0],
					totalDBGraphs);
			return result;
		} catch (Exception e) {
			System.out.println(this.postings[0].length);
			System.out.println(totalDBGraphs);
			for (int i : postings[0])
				System.out.println(i);
		}
		return null;
	}

	/**
	 * Given the total number of queries, return the query graphs not containing
	 * the feature
	 * 
	 * @param totalQueryGraphs
	 * @return
	 */
	@Override
	public int[] notContainedQueryGraphs(int totalQueryGraphs) {
		/*
		 * int[] result =null; if(this.postings[3].length == 0) result =
		 * OrderedIntSets.getCompleteSet(this.postings[2], totalQueryGraphs);
		 * else{ int[] temp = OrderedIntSets.getUnion(postings[2], postings[3]);
		 * result = OrderedIntSets.getCompleteSet(temp, totalQueryGraphs); }
		 */
		int[] result = OrderedIntSets.getCompleteSet(this.postings[2],
				totalQueryGraphs);
		return result;
	}

	/**
	 * Return all the database graphs, isomorphic to the feature
	 * 
	 * @return
	 */
	@Override
	public int[] getEquavalentDatabaseGraphs() {
		return this.postings[1];
	}

	/**
	 * Return all the query graphs, isomorphic to the features
	 * 
	 * @return
	 */
	@Override
	public int[] getEquavalentQueryGraphs() {
		return this.postings[3];
	}

	/**
	 * Return the underlying feature of the coverSet
	 * 
	 * @return
	 */
	@Override
	public OneFeatureMultiClass getFeature() {
		return this.feature;
	}

	@Override
	public int getFetureID() {
		return this.feature.getFeatureId();
	}
}
