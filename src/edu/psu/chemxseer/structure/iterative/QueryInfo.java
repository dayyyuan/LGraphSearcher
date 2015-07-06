package edu.psu.chemxseer.structure.iterative;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * Implementation of one Query: It contains: (1) query ID, (2) query graph, (3)
 * the gaps, which is the false positive that has not been filtered yet, or
 * C(q)-D(q). (4) support (5) frequency, how many times this query happens in
 * the query log
 * 
 * @author dayuyuan
 * 
 */
public class QueryInfo implements Comparable<QueryInfo> {
	private int qID;
	private Graph queryGraph;
	private int[] gaps;
	private int support; // # of real support of this feature in the database
	private int frequency; // The # of queries in the query log that = query
							// Graph

	/**
	 * Construct a QueryInfo Object Given the query graph, and its corresponding
	 * supports on the database
	 * 
	 * @param query
	 * @param id
	 * @param gaps
	 * @param supportCount
	 * @param frequency
	 */
	public QueryInfo(Graph query, int id, int[] gaps, int supportCount,
			int frequency) {
		this.queryGraph = query;
		this.qID = id;
		this.gaps = gaps;
		this.support = supportCount;
		this.frequency = frequency;
	}

	/**
	 * Test weather the query graph equals to the graph g, under the assumption
	 * that g is subgraph isomorphic to the query q
	 * 
	 * @param g
	 * @return
	 */
	public boolean isEqualTo(Graph g) {
		if (queryGraph.getEdgeCount() == g.getEdgeCount()
				&& queryGraph.getNodeCount() == g.getNodeCount())
			return true;
		else
			return false;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int[] getGaps() {
		return this.gaps;
	}

	public int getGapCount() {
		return this.gaps.length;
	}

	public int getSupport() {
		return this.support;
	}

	public Graph getQueryGraph() {
		return queryGraph;
	}

	public void setQueryGraph(Graph queryGraph) {
		this.queryGraph = queryGraph;
	}

	public int getID() {
		return this.qID;
	}

	/**
	 * Given the supports of a new feature, find out the new gap as the
	 * intersection of the supports and the original gap
	 * 
	 * @param supports
	 */
	public int updateGap(int[] newFeatureSupport) {
		int oriCount = this.gaps.length;
		this.gaps = OrderedIntSets.join(this.gaps, newFeatureSupport);
		return oriCount - this.gaps.length;

	}

	/**
	 * This query has been changed into invisible
	 */
	public void setInValid() {
		this.gaps = new int[0];
		this.support = 0;
		this.frequency = 0;
	}

	@Override
	/**
	 * Compare with respect to the ID
	 */
	public int compareTo(QueryInfo other) {
		if (this.qID < other.qID)
			return -1;
		else if (this.qID == other.qID)
			return 0;
		else
			return 1;
	}

}
