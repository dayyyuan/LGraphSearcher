package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureWrapperSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class CIndexTreeFeatureSelector {

	private NoPostingFeatures<OneFeatureMultiClass> features;
	private int[][] FGMap; // FGMap[i] = posting[fi]
	private int[][] FQMap; // FQMap[i] = posting[fi]
	private boolean[][] FQCover; // For Look-up, FQ[i][j] = true if feature i is
									// contained in query q

	private int graphCount; // total number of database graphs
	private int queryCount;
	private int minimumQueryThreshold;

	private double startMemory = 0;

	/**
	 * Constructor of the Feature Selector for CIndexTree: basically the same as
	 * FeatureSelector1
	 * 
	 * @param postingFeatures
	 * @param minQueryCount
	 * @throws IOException
	 */
	public CIndexTreeFeatureSelector(PostingFeaturesMultiClass postingFeatures,
			int minQueryCount) throws IOException {
		MemoryConsumptionCal.runGC();
		startMemory = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		this.minimumQueryThreshold = minQueryCount;
		// 1. Step One: load all the features into memory
		this.features = postingFeatures.getMultiFeatures();
		// 2. Step Two: initialize the data member
		this.graphCount = postingFeatures.getClassGraphsCount()[0];

		int featureCount = this.features.getfeatureNum();
		this.FGMap = new int[featureCount][];
		this.FQMap = new int[featureCount][];
		for (int i = 0; i < featureCount; i++) {
			this.FGMap[i] = postingFeatures.getFullPosting(i, 0); // FGMap[i] =
																	// all DB
																	// Graphs
																	// containing
																	// f
		}

		this.queryCount = postingFeatures.getClassGraphsCount()[1];
		this.FQCover = new boolean[featureCount][queryCount];
		for (int i = 0; i < featureCount; i++) {
			for (int j = 0; j < queryCount; j++)
				FQCover[i][j] = false;
			this.FQMap[i] = postingFeatures.getFullPosting(i, 1);
			for (int qID : FQMap[i])
				FQCover[i][qID] = true;
		}
		System.out
				.println("(1) Time for CIndexTreeFeatureSelector Construction:"
						+ (System.currentTimeMillis() - startTime));
	}

	public CIndexTreeFeatureNode constructFeatureTree() {
		long startTime = System.currentTimeMillis();
		int[] wholeQueries = new int[this.queryCount];
		for (int i = 0; i < wholeQueries.length; i++)
			wholeQueries[i] = i;
		CIndexTreeFeatureNode root = CIndexTreeFeatureNode
				.ConstructBinaryFeatureTree(wholeQueries, this);

		// ConsumptionCal.runGC();
		double endMemory = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(2) Space for CIndexFeature Mining: "
				+ (endMemory - startMemory));
		System.out.println("(3) Time for CIndexTreeFeature Mining: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("(4) After Pattern Selection: "
				+ root.getNodeCount() + " nodes out of "
				+ features.getfeatureNum()
				+ " frequent patterns as selected for Indexing");
		return root;
	}

	/**
	 * Return the Minimum Number of Queries that each node should cover
	 * 
	 * @return
	 */
	public int getMinQuerySize() {
		return this.minimumQueryThreshold;
	}

	/**
	 * Given the set of features, return the complement of Union of those
	 * features
	 * 
	 * @param notContainedFeatures
	 */
	public int[] getUnfilteredGraphs(List<IOneFeature> notContainedFeatures) {
		boolean[] graphStatus = new boolean[this.graphCount];
		for (int i = 0; i < graphCount; i++)
			graphStatus[i] = false;
		for (IOneFeature aFeature : notContainedFeatures) {
			int fID = aFeature.getFeatureId();
			int[] fPostings = this.FGMap[fID];
			for (int gID : fPostings)
				graphStatus[gID] = true;
		}
		// return
		int count = 0;
		for (boolean gStatus : graphStatus)
			if (gStatus == false)
				count++;
		int[] results = new int[count];
		for (int i = 0, resultIndex = 0; i < this.graphCount; i++)
			if (graphStatus[i] == false)
				results[resultIndex++] = i;
		return results;
	}

	/**
	 * Return the Total Number of features
	 * 
	 * @return
	 */
	public int getFeatureCount() {
		return this.features.getfeatureNum();
	}

	/**
	 * The the ith features
	 * 
	 * @param i
	 * @return
	 */
	public IOneFeature getFeature(int i) {
		return this.features.getFeature(i);
	}

	/**
	 * get the feature score on query "qID". if feature contained in qID, return
	 * 0; else return the number of graphs get covered in the unFilteredGraphs
	 * 
	 * @param aFeature
	 * @param queries
	 * @param unFilteredGraphs
	 * @return
	 */
	public int getFeatureScore(IOneFeature aFeature, int[] queries,
			int[] unFilteredGraphs) {
		int fID = aFeature.getFeatureId();
		// 1. Calculate how many queries does not contain aFeature, so that it
		// can use aFeature for filtering
		int count = 0;
		for (int i = 0; i < queries.length; i++) {
			int qID = queries[i];
			// if(fID <0 || fID >= FQCover.length || qID <0 || qID >=
			// FQCover[fID].length)
			// System.out.println("find it");
			if (this.FQCover[fID][qID] == false)
				count++;
		}
		// 2. For those features use aFeature for filtering, it have the same
		// score:
		int eachScore = OrderedIntSets.getJoinSize(unFilteredGraphs,
				this.FGMap[fID]);
		return count * eachScore;
	}

	/**
	 * Given the sorted queries, split the query into two arrays results[0] are
	 * the queries containing the feature results[1] are the queries not
	 * containing the feature
	 * 
	 * @param queries
	 * @return
	 */
	public int[][] splitQueries(int[] queries, IOneFeature theFeature) {
		int fID = theFeature.getFeatureId();
		int containedNum = 0;
		for (int i = 0; i < queries.length; i++) {
			int qID = queries[i];
			if (this.FQCover[fID][qID])
				containedNum++;
		}
		int[][] results = new int[2][];
		results[0] = new int[containedNum];
		results[1] = new int[queries.length - containedNum];
		for (int i = 0, containedIndex = 0, unContainedIndex = 0; i < queries.length; i++) {
			int qID = queries[i];
			if (this.FQCover[fID][qID])
				results[0][containedIndex++] = qID;
			else
				results[1][unContainedIndex++] = qID;
		}
		return results;
	}

	public List<IFeatureWrapper> getUnSelectedFeaturesAndRelabel(
			Set<IOneFeature> ancestorFeatures, int[] graphIDs, int[] queryIDs) {
		List<IFeatureWrapper> result = new ArrayList<IFeatureWrapper>();

		for (int i = 0; i < this.features.getfeatureNum(); i++) {
			OneFeatureMultiClass feature = features.getFeature(i);
			if (ancestorFeatures.contains(feature))
				continue;
			else {
				int[][] postings = new int[4][];
				postings[1] = postings[3] = new int[0];
				postings[0] = OrderedIntSets.getJoinPosition(graphIDs, 0,
						graphIDs.length, this.FGMap[i], 0, FGMap[i].length);
				postings[2] = OrderedIntSets.getJoinPosition(queryIDs, 0,
						queryIDs.length, this.FQMap[i], 0, FQMap[i].length);
				result.add(new FeatureWrapperSimple(feature, postings));
			}
		}
		return result;
	}

}
