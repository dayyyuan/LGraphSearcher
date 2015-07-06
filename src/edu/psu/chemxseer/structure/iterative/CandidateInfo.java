package edu.psu.chemxseer.structure.iterative;

import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iterative.LindexUpdate.SubgraphSearch_LindexUpdate;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * This is a class representing a new candidate feature Both maximal supergraphs
 * and whole supergraphs are used here
 * 
 * One CandidateFeature have one gain scores (w.r.t the objective function) The
 * gain score can be calculated precisely or as a average (some ratio) of the
 * upper and lower bound A branch upper bound can also be derived for branch and
 * bound prunning.
 * 
 * @author dayuyuan
 */
public class CandidateInfo implements Comparable<CandidateInfo> {
	private Graph graph;
	private boolean hitOtherIndexFeature; // true if this candidate feature hit
											// on any indexing feature
	private ArrayList<QueryInfo> maxQueries;
	private ArrayList<QueryInfo> queries;
	// a list of maximum subgraph queries that are covered by this feature
	private QueryInfo hitQuery; // the query that hit on this candidate feature

	// If the gain function is estimated instead of calculated, then the support
	// is not generated
	private int[] supports; // the supports of the feature
	private List<Integer> maxSubgraphs; // the maximum subgraphs of this
										// candidate feature
	private List<Integer> minSupgraphs; // the minimal supergraph of this
										// candidate feature

	// Some statistics
	private int upperbound;
	private int D_maxP; // D_max(p)
	private int lowerBound;
	private int branchUpbound; // used in branch & bound paradigm

	private boolean estimated; // the gain function can be either estimated or
								// calculated
	private double gamma;
	private double alpha;
	private int gain; // the objective function

	// /**
	// * Construct a Candidate Feature
	// * @param featureGraph
	// * @param maxQueries
	// * @param allQueries
	// * @param hitQuery
	// * @param lindex
	// * @param estimated
	// */
	// public CandidateInfo(Graph featureGraph, ArrayList<QueryInfo> maxQueries,
	// ArrayList<QueryInfo> allQueries,
	// QueryInfo hitQuery, SubgraphSearch_LindexUpdate lindex, boolean
	// estimated){
	// this.graph = featureGraph;
	// this.maxQueries = maxQueries;
	// this.queries = allQueries;
	// this.hitQuery = hitQuery;
	// this.estimated = estimated;
	// this.hitOtherIndexFeature = false;
	// // the maxSubgraphs & supports will be found here
	// this.findSupport(lindex);
	// this.gain = -1;
	// this.upperbound = -1;
	// this.lowerbound = -1;
	// this.branchUpbound = -1;
	// }
	/**
	 * Construct a Candidate Feature
	 * 
	 * @param feature
	 * @param maxQueries
	 * @param hitQuery
	 * @param lindex
	 * @param maxSubgraphs
	 * @param minSupgraphs
	 * @param estimated
	 */
	public CandidateInfo(Graph feature, ArrayList<QueryInfo> maxQueries,
			ArrayList<QueryInfo> allQueries, QueryInfo hitQuery,
			SubgraphSearch_LindexUpdate lindex, List<Integer> maxSubgraphs,
			List<Integer> minSupergraphs, boolean estimated, double gamma,
			double alpha) {
		this.graph = feature;
		this.maxQueries = maxQueries;
		this.queries = allQueries;
		this.hitQuery = hitQuery;
		this.estimated = estimated;
		this.hitOtherIndexFeature = false;
		this.minSupgraphs = minSupergraphs;
		// the maxSubgraphs & supports will be found here
		this.findSupport(lindex, maxSubgraphs);
		this.gain = -1;
		this.upperbound = -1;
		this.lowerBound = -1;
		this.D_maxP = -1;
		this.branchUpbound = -1;
		this.gamma = gamma;
		this.alpha = alpha;
	}

	/**
	 * Given the Lindex, find the maximum subgraph indexing terms of the new
	 * feature If estimated, no need for finding the exact support. The lower
	 * bound will be set as the |maxSub(f)| Otherwise, the exact support is
	 * calculated.
	 * 
	 * @param lindex
	 * @return null if the new feature is found to be isomorphic to the any
	 *         current indexing feature
	 * 
	 */
	// private int[] findSupport(SubgraphSearch_LindexUpdate searcher) {
	// long[] TimeComponent = new long[4];
	// // If the maximal subgraphs are generated here, then it may contain falst
	// positives
	// this.maxSubgraphs = searcher.indexSearcher.maxSubgraphs(this.graph,
	// TimeComponent);
	// return findSupport(searcher, maxSubgraphs);
	// }

	private int[] findSupport(SubgraphSearch_LindexUpdate searcher,
			List<Integer> maxSubgraphs) {
		this.maxSubgraphs = maxSubgraphs;
		if (maxSubgraphs != null && maxSubgraphs.get(0) == -1) {// graph g hits
																// on one of the
																// index term
			this.hitOtherIndexFeature = true;
			return null;
		} else if (!estimated) {
			this.supports = searcher.getAnswer(this.graph, maxSubgraphs);
			return supports;
		} else {
			this.D_maxP = searcher.getCandidate(this.maxSubgraphs).size();
			return null;
		}
	}

	/**
	 * Return the branch upper bound functin Calculated on all queries
	 * 
	 * @return
	 */
	public int getBranchUpperBound() {
		if (this.branchUpbound == -1) {
			int maxAnswerSize = 0;
			int queryCount = 0;
			for (QueryInfo oneQ : this.queries) {
				queryCount += oneQ.getFrequency();
				this.branchUpbound += oneQ.getFrequency() * oneQ.getGapCount();
				int answerSize = oneQ.getFrequency() * oneQ.getSupport();
				if (maxAnswerSize < answerSize)
					maxAnswerSize = answerSize;
			}
			this.branchUpbound += maxAnswerSize;
			this.branchUpbound = this.branchUpbound / queryCount;
		}
		return this.branchUpbound;
	}

	/**
	 * Return the upper bound function: Calculated on maximal queries
	 * 
	 * @return
	 */
	public int getUpperBound() {
		if (upperbound == -1) {
			upperbound = 0;
			int maxAnswerSize = 0;
			int queryCount = 0;
			for (QueryInfo oneQ : this.maxQueries) {
				queryCount += oneQ.getFrequency();
				upperbound += oneQ.getFrequency() * oneQ.getGapCount();
				int answerSize = oneQ.getFrequency() * oneQ.getSupport();
				if (maxAnswerSize < answerSize)
					maxAnswerSize = answerSize;
			}
			upperbound += maxAnswerSize;
			upperbound = upperbound / queryCount;
		}
		return upperbound;
	}

	/**
	 * For the input gamma, calculate the lower bound The lower bound will
	 * change with gamma
	 * 
	 * @param gamma
	 * @return
	 */
	public int getLowerBound() {
		if (this.lowerBound == -1) {
			lowerBound = 0;
			int temp = (int) (this.D_maxP / gamma);
			int maxAnswerSize = 0;
			int queryCount = 0;
			for (QueryInfo oneQ : this.maxQueries) {
				int oneLowerBound = oneQ.getGapCount() + oneQ.getSupport()
						- temp;
				if (oneLowerBound > 0) {
					lowerBound += oneQ.getFrequency() * oneLowerBound;
					queryCount += oneQ.getFrequency();
				}
				// there are risks that the result is < 0
				int answerSize = oneQ.getFrequency() * oneQ.getSupport();
				if (maxAnswerSize < answerSize)
					maxAnswerSize = answerSize;
			}
			lowerBound += maxAnswerSize;
			lowerBound = lowerBound / queryCount;
		}
		return this.lowerBound;
	}

	/**
	 * Return the estimated calculated gain function The estimated = true;
	 * 
	 * @param lowerBound
	 * @return
	 */
	public int getGain(double alpha) {
		int result = (int) (alpha * this.getUpperBound() + (1 - alpha)
				* this.getLowerBound());
		return result;
	}

	/**
	 * Return the precise calculated gain function: The estimated = false;
	 * 
	 * @return
	 */
	public int getGain() {
		if (gain == -1) {
			gain = 0;
			if (estimated) {
				gain = (int) (alpha * this.getUpperBound() + (1 - alpha)
						* this.getLowerBound());
			} else {
				for (QueryInfo oneQ : this.maxQueries) {
					gain += oneQ.getFrequency()
							* (oneQ.getGapCount() - OrderedIntSets.getJoinSize(
									oneQ.getGaps(), supports));
				}
				if (this.hitQuery != null)
					gain += hitQuery.getFrequency()
							* (hitQuery.getSupport() + hitQuery.getGapCount());
			}
		}
		return gain;
	}

	public boolean hitOneAnyIndex() {
		return this.hitOtherIndexFeature;
	}

	public QueryInfo getHitQuery() {
		return this.hitQuery;
	}

	public Graph getCandidateGraph() {
		return graph;
	}

	/**
	 * The support may be null if the gain is estimated instead of calculated
	 * precisely
	 * 
	 * @return
	 */
	public int[] getSupport() {
		return this.supports;
	}

	/**
	 * Get all the maximum queries
	 * 
	 * @return
	 */
	public ArrayList<QueryInfo> getMaxQueries() {
		return maxQueries;
	}

	/**
	 * Get all the queries
	 * 
	 * @return
	 */
	public ArrayList<QueryInfo> getQueries() {
		return this.queries;
	}

	/**
	 * Return the maximum subgraphs of the feature return [0] = -1 means that a
	 * hiting
	 * 
	 * @return
	 */
	public List<Integer> getMaxSubs() {
		return this.maxSubgraphs;
	}

	public List<Integer> getMinSups() {
		// TODO Auto-generated method stub
		return this.minSupgraphs;
	}

	@Override
	public int compareTo(CandidateInfo o) {
		int thisGain = this.getGain();
		int otherGain = o.getGain();
		if (thisGain < otherGain)
			return -1;
		else if (thisGain == otherGain)
			return 0;
		else
			return 1;
	}

}
