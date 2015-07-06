package edu.psu.chemxseer.structure.iterative.FeatureGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iterative.CandidateInfo;
import edu.psu.chemxseer.structure.iterative.IndexFeatureInfo;
import edu.psu.chemxseer.structure.iterative.QueryInfo;
import edu.psu.chemxseer.structure.iterative.LindexUpdate.SubgraphSearch_LindexUpdate;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.query.TestQuery;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;
import edu.psu.chemxseer.structure.util.SelfImplementSet;

/**
 * This class is in charge of "generating" features that are suitable for
 * subgraph search The feature selection is an incremental procedure, given a
 * current available Lindex, find a new feature that can be added into the index
 * in order to benefit the overall effectiveness of the subgraph search using
 * the new index
 * 
 * @author dayuyuan
 * 
 */
public class FeatureFinder {
	private SubgraphSearch_LindexUpdate lindex;
	private List<IndexFeatureInfo> featureArray; // each index feature is
													// associated with on
													// FeatureInfo object

	private QueryInfo[] queryArray; // all the queries

	private int maxNonSelectDepth = 10;
	private double minFreq;
	private boolean estimated; // estimate the gain function of the candidate
								// feature or not
	private double gamma; // gamma if for lower bound
	private double alpha; // alpha is for the combination of the upper boudn and
							// lower bound

	/**
	 * Given the lindex and a set of query logs, construct a new FeatureFinder
	 * object The procedure takes the following stpes: 1. Construct an array of
	 * featureInfos, each of them corresponds to one of the LindexTerm feature
	 * 2. Construct an array of queryInfos, each of them corresponds to one of
	 * the Query in the queryLogs 3. Each feature is associated with a set of
	 * maximum queries, add this information to the FeatureInfo objects that are
	 * created in step one
	 * 
	 * @param lindex
	 * @param queryGraphs
	 * @throws Exception
	 */
	public FeatureFinder(SubgraphSearch_LindexUpdate lindex,
			ArrayList<TestQuery> queries, double minFreq, boolean estimateGain,
			double gamma, double alpha) {
		this.lindex = lindex;
		this.minFreq = minFreq;
		this.estimated = estimateGain;
		this.gamma = gamma;
		this.alpha = alpha;
		// 1. For each index feature, construct a FeatureInfo object: the
		// maxQueries will be set latter
		int featureCount = lindex.getFeatureNum();
		featureArray = new ArrayList<IndexFeatureInfo>();
		for (int i = 0; i < featureCount; i++) {
			featureArray.add(new IndexFeatureInfo(i));
		}
		// 2. For each queryGraph, construct a QueryInfo object:
		// we assume if two queries are of the same ID, we treat them as one
		// query
		List<QueryInfo> queriesList = new ArrayList<QueryInfo>();
		long[] temp = new long[4];
		for (int i = 0; i < queries.size(); i++) {
			// Construct the queryInfo Object
			Graph q = queries.get(i).getG();
			List<Integer> maxSubsRaw = this.lindex.indexSearcher.maxSubgraphs(
					q, temp);
			List<Integer> maxSubs = this.lindex.indexSearcher
					.getRealMaxSubgraphs(maxSubsRaw);
			List<Integer> subs = this.lindex.indexSearcher.subgraphs(q, temp);
			// 2.2 Use the maximum subgraphs to find the real frequency of the
			// query
			IGraphFetcher candidateFetcher = this.lindex.getCandidate(maxSubs);
			int[] gaps = this.lindex.getGap(q, candidateFetcher);
			int support = candidateFetcher.size() - gaps.length;
			// 2.3 Construct a new QueryInfo
			QueryInfo oneQueryInfo = new QueryInfo(q, i, gaps, support, queries
					.get(i).getFrequency());
			queriesList.add(oneQueryInfo);
			// 3 Add this new queryInfo object to the list of terms that are
			// maximum subgraph isomorphic to the queryInfo
			for (int subTermID : maxSubs) {
				featureArray.get(subTermID).addOneMaxQuery(oneQueryInfo);
			}
			for (int subTermID : subs) {
				featureArray.get(subTermID).addOneMaxQuery(oneQueryInfo);
			}
		}
		this.queryArray = new QueryInfo[queriesList.size()];
		queriesList.toArray(this.queryArray);
	}

	/**
	 * Sort all the index features according to their spoint value
	 * 
	 * @return
	 */
	private IndexFeatureInfo[] getSortedIndexFeatures() {
		IndexFeatureInfo[] temp = new IndexFeatureInfo[this.featureArray.size()];
		featureArray.toArray(temp);
		Arrays.sort(temp);
		return temp;
	}

	/**
	 * Select multiple features in each iteration of the feature selection:
	 * features generated from different root are are returned The gain function
	 * should be at least alpha * optimumGain
	 * 
	 * @return
	 */
	public List<CandidateInfo> findTopKFeatures(double ratio) {
		List<CandidateInfo> allOpts = new ArrayList<CandidateInfo>();
		// A: root selection:
		// 1. First Step: sort all current features according to their average
		// gain
		IndexFeatureInfo roots[] = this.getSortedIndexFeatures();
		// 2. Star the rootSelection routine : feature selection is embedded
		// into the root selection
		int optimumGain = 0;
		for (int i = 0; i < roots.length; i++) {
			IndexFeatureInfo root = roots[i];
			// 0. early stop
			if (root.getStPointScore() < optimumGain)
				break; // this is an early stop creteria, not that reasonable
						// though
			// a. The starting point is visited according to its gain function:
			// average maximum benefit
			int loweBound = (int) (ratio * optimumGain);
			CandidateInfo newFeatureCandidate = this.findNextFeatureGivenRoot(
					root, loweBound);
			if (newFeatureCandidate == null)
				break; // should be continue, but really don't want to many
						// roots to be examed
			else if (newFeatureCandidate.getGain() > optimumGain) {
				optimumGain = newFeatureCandidate.getGain();
				allOpts.add(newFeatureCandidate);
			} else if (newFeatureCandidate.getGain() > loweBound)
				allOpts.add(newFeatureCandidate);
		}
		List<CandidateInfo> results = new ArrayList<CandidateInfo>();
		for (int i = 0; i < allOpts.size(); i++) {
			CandidateInfo oneC = allOpts.get(i);
			if (oneC.getGain() > ratio * optimumGain)
				results.add(oneC);
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * Find the features using QG algorithm
	 * 
	 * @return
	 */
	public CandidateInfo findFeatureQG() {
		// A: root selection:
		// 1. First Step: sort all current features according to their average
		// gain
		IndexFeatureInfo roots[] = this.getSortedIndexFeatures();
		// 2. Star the rootSelection routine : feature selection is embedded
		// into the root selection
		int optimumGain = 0;
		CandidateInfo optimumFeature = null;
		for (int i = 0; i < roots.length; i++) {
			IndexFeatureInfo root = roots[i];
			// 0. Early Pruning
			if (root.getStPointScore() < optimumGain)
				break; // this is an early stop creteria, not that reasonable
						// though
			// a. The starting point is visited according to its gain function:
			// average maximum benefit
			CandidateInfo newFeatureCandidate = this.findNextFeatureGivenRoot(
					root, optimumGain);
			if (newFeatureCandidate == null)
				if (optimumFeature != null)
					break;
				else
					continue; // still need to find
			else if (newFeatureCandidate.getGain() > optimumGain) {
				optimumGain = newFeatureCandidate.getGain();
				optimumFeature = newFeatureCandidate;
			}
		}
		return optimumFeature;
	}

	/**
	 * Baseline algorithm: this is a brute force algorithm
	 */
	public CandidateInfo findFeatureBB() {
		CandidateInfo gInfo = null;
		try {
			gInfo = ExhaustiveGenerator.genCandidate(minFreq,
					maxNonSelectDepth, this, this.queryArray);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gInfo;
	}

	/**
	 * Given the root graph, find the optimum feature to be added into the
	 * index. If a feature from this root > optimumGain, then this feature is
	 * returned, and the optimumGain of the caller should changed
	 * 
	 * @param root
	 * @param optimumGain
	 * @return
	 */
	private CandidateInfo findNextFeatureGivenRoot(IndexFeatureInfo root,
			int optimumGain) {
		int[][] rootGraph = lindex.indexSearcher.getTermFullLabel(root.getID());
		List<QueryInfo> queries = root.getMaxQueries();
		CandidateInfo gInfo = null;
		try {
			gInfo = GroupQueryGenerator.genCandidate(minFreq,
					maxNonSelectDepth, rootGraph, this, queries, optimumGain);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gInfo;
	}

	/**
	 * /** After finding a new candidate feature, we need to 1. update the index
	 * structure: add this feature into the index, relabel and etc. That is
	 * encapsulate in the index itself 2. Add the new feature into the feature
	 * array, Set the maximum Queries of this feature and also update these
	 * maximum queries's gaps
	 * 
	 * @param newFeature
	 */
	public int addNewFeature(CandidateInfo newFeature) {
		// 1. Update the index structure
		LindexTerm newTerm = this.lindex.indexSearcher
				.insertNewFeature(newFeature);
		// 2. Add the new Feature into the feature array
		// 2.1 Find the maximum Queries of this query
		ArrayList<QueryInfo> maxQueries = newFeature.getMaxQueries();
		// 2.2 Update the queries "gap"
		int[] supports = newFeature.getSupport();
		int count = 0;
		QueryInfo hitQuery = newFeature.getHitQuery();
		for (QueryInfo q : maxQueries) {
			int dif = q.updateGap(supports);
			count += q.getFrequency() * dif;
		}
		if (hitQuery != null) {
			count += hitQuery.getFrequency()
					* (hitQuery.getSupport() + hitQuery.getGapCount());
			hitQuery.setInValid();
		}
		this.featureArray.add(new IndexFeatureInfo(newTerm.getId(), maxQueries,
				newFeature.getQueries()));
		return count;
	}

	/**
	 * Given the new feature graph G, generate one candidateInfo Return null if
	 * codG hit on one of the index Term
	 * 
	 * @param codeG
	 * @param queries
	 *            containing codeG (may not be complete)
	 * @return
	 */
	public CandidateInfo makeCandidateInfo(Graph codeG, Graph[] queries) {
		long[] TimeComponent = new long[4];

		ArrayList<QueryInfo> superQueries = new ArrayList<QueryInfo>(
				queries.length);
		for (int i = 0; i < queries.length; i++) {
			int queryID = Integer.parseInt(queries[i].getName());
			if (queryID > this.queryArray.length)
				System.out.println("It is wired in selector: genCandidate");
			superQueries.add(this.queryArray[queryID]);
		}
		Collections.sort(superQueries);

		// 1. Given the new feature graph, find its maximal subgraph terms
		List<Integer> maxSubs = this.lindex.indexSearcher.maxSubgraphs(codeG,
				TimeComponent);
		List<Integer> minSups = this.lindex.indexSearcher.minimalSupergraphs(
				codeG, TimeComponent, maxSubs);
		List<IndexFeatureInfo> minSupFeature = this.getFeature(minSups);
		ArrayList<QueryInfo> maxQueries = this.getMaxQueriesForCandidate(codeG,
				superQueries, minSupFeature);

		// 2. Remove hit-on queries
		QueryInfo hitQuery = null;
		for (int i = 0; i < maxQueries.size(); i++) {
			QueryInfo oneMaxQuery = maxQueries.get(i);
			if (oneMaxQuery.getQueryGraph().getEdgeCount() == codeG
					.getEdgeCount()) {
				hitQuery = oneMaxQuery;
				maxQueries.remove(i);
				break;
			}
		}
		return new CandidateInfo(codeG, maxQueries, superQueries, hitQuery,
				lindex, maxSubs, minSups, estimated, gamma, alpha);
	}

	private List<IndexFeatureInfo> getFeature(List<Integer> featureIDs) {
		List<IndexFeatureInfo> result = new ArrayList<IndexFeatureInfo>(
				featureIDs.size());
		for (Integer it : featureIDs)
			result.add(this.featureArray.get(it));
		return result;
	}

	/**
	 * Given a candidate graph g, and it's complete set of super queries, find
	 * the set of maximal queries of the graph g
	 * 
	 * @param g
	 * @param superQueries
	 * @param minSupFeatures
	 * @return
	 */
	private ArrayList<QueryInfo> getMaxQueriesForCandidate(Graph g,
			List<QueryInfo> superQueries, List<IndexFeatureInfo> minSupFeatures) {
		SelfImplementSet<QueryInfo> set = new SelfImplementSet<QueryInfo>();
		set.addAll(superQueries);
		for (IndexFeatureInfo oneMinSup : minSupFeatures) {
			set.removeAll(oneMinSup.getQueries());
		}
		return set.getItems();
	}

	/**
	 * Return all the queries
	 * 
	 * @return
	 */
	public QueryInfo[] getQueryInfo() {
		return queryArray;
	}

	public QueryInfo getQueryInfo(int index) {
		return this.queryArray[index];
	}
}
