package edu.psu.chemxseer.structure.iterative;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Implementation of indexing features: Each index feature is associated with a
 * list of maximal queries The starting point score can be calculated and
 * returned for each indexing features
 * 
 * @author dayuyuan
 * 
 */
public class IndexFeatureInfo implements Comparable<IndexFeatureInfo> {
	private List<QueryInfo> maxQueries;
	private List<QueryInfo> queries; // for generating new feature's maxQueries
	private int internalID;

	// Some Simple Statistics of this indexing feature
	private double avgGain;
	private double medianGain;
	private double quartiles;
	private int stPointScore;

	/**
	 * The maxQueries and queries are not initialized
	 * 
	 * @param iternalID
	 *            : the ID of the underling feature
	 */
	public IndexFeatureInfo(int iternalID) {
		this.maxQueries = new ArrayList<QueryInfo>();
		this.queries = new ArrayList<QueryInfo>();
		this.stPointScore = -1;
		this.avgGain = -1;
		this.medianGain = -1;
		this.quartiles = -1;
	}

	/**
	 * 
	 * @param internalID
	 *            : the ID of the underlying feature
	 * @param maxQueries
	 * @param queries
	 */
	public IndexFeatureInfo(int internalID, List<QueryInfo> maxQueries,
			List<QueryInfo> queries) {
		this.maxQueries = maxQueries;
		this.queries = queries;
		this.stPointScore = -1;
		this.avgGain = -1;
		this.medianGain = -1;
		this.quartiles = -1;
	}

	/**
	 * This must be called before any getUpperBound Calculate the upper bound
	 * according to the formula The upper bound is calculated over all queries
	 */
	private void calculateStPointScore() {
		int total = 0;
		int maxAnswerSize = 0;
		for (QueryInfo oneQ : this.maxQueries) {
			total += oneQ.getFrequency() * oneQ.getGapCount();
			int tempSize = oneQ.getSupport() * oneQ.getGapCount();
			if (tempSize > maxAnswerSize)
				maxAnswerSize = tempSize;
		}
		total += maxAnswerSize;
		this.stPointScore = total;
	}

	public int getStPointScore() {
		if (this.stPointScore == -1)
			this.calculateStPointScore();
		return this.stPointScore;
	}

	/**
	 * This must be called before any getAvg or getMedian or getQuatile or
	 * isGoodRoot Given the featureInfo, recalculate the average and median of
	 * the "gain upper bound" function for each maxQueryInfo For now the
	 * "gain upper bound" = gaps.size();
	 */
	private void calculateAvg() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (QueryInfo oneQ : this.maxQueries) {
			stats.addValue(oneQ.getGapCount() * oneQ.getFrequency());
		}
		this.avgGain = stats.getMean();
		this.medianGain = stats.getPercentile(50);
		this.quartiles = stats.getPercentile(75);
	}

	/**
	 * Get the average possible gain
	 * 
	 * @return
	 */
	public double getAvgGain() {
		if (avgGain == -1)
			this.calculateAvg();
		return avgGain;
	}

	public double getMedian() {
		if (this.medianGain == -1)
			this.calculateAvg();
		return this.medianGain;
	}

	/**
	 * Add one maximal query to this feature
	 * 
	 * @param queryInfo
	 */
	public void addOneMaxQuery(QueryInfo queryInfo) {
		this.maxQueries.add(queryInfo);
	}

	/**
	 * Add one super query to this feature
	 * 
	 * @param queryInfo
	 */
	public void addOneQuery(QueryInfo queryInfo) {
		this.maxQueries.add(queryInfo);
	}

	/**
	 * Return the # of queries has this index feature, multiply with
	 * "frequencies"
	 * 
	 * @return
	 */
	public int getQueriesSize() {
		int result = 0;
		for (QueryInfo oneQ : this.maxQueries) {
			result += oneQ.getFrequency();
		}
		return result;
	}

	public boolean isGoodRoot() {
		if (this.avgGain < this.quartiles)
			return true;
		else
			return false;
	}

	@Override
	/**
	 * Reverse order of the spoint value
	 */
	public int compareTo(IndexFeatureInfo arg0) {
		int other = arg0.getStPointScore();
		int thisScore = arg0.getStPointScore();
		if (thisScore < other)
			return 1;
		else if (thisScore == other)
			return 0;
		else
			return -1;
	}

	public int getID() {
		return this.internalID;
	}

	public List<QueryInfo> getMaxQueries() {
		return this.maxQueries;
	}

	public List<QueryInfo> getQueries() {
		return this.queries;
	}

}
