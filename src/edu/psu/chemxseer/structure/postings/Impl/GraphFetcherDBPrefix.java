package edu.psu.chemxseer.structure.postings.Impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcherPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResultPref;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class GraphFetcherDBPrefix implements IGraphFetcherPrefix {
	public GraphDatabase_Prefix gDB;
	public IIndexPrefix index;
	public int[] orderedGIDs;
	public int start;

	/**
	 * Assume that the inputGIDs is well sorted
	 * 
	 * @param gDB
	 * @param inputGIDs
	 * @param reverse
	 */
	public GraphFetcherDBPrefix(GraphDatabase_Prefix gDB, int[] inputGIDs,
			boolean reverse, IIndexPrefix prefixIndex) {
		this.gDB = gDB;
		this.index = prefixIndex;
		if (!OrderedIntSets.isOrdered(inputGIDs))
			Arrays.sort(inputGIDs);
		if (reverse) {
			int totalNum = gDB.getTotalNum();
			this.orderedGIDs = OrderedIntSets.getCompleteSet(inputGIDs,
					totalNum);
		} else {
			this.orderedGIDs = inputGIDs;
		}
	}

	@Override
	public int[] getOrderedIDs() {
		return orderedGIDs;
	}

	@Override
	public List<IGraphResultPref> getGraphs(long[] TimeComponent) {
		if (start == orderedGIDs.length)
			return null;
		else {
			long startTime = System.currentTimeMillis();
			int end = Math.min(start + batchCount, orderedGIDs.length);
			List<IGraphResultPref> results = new ArrayList<IGraphResultPref>();
			for (int i = start; i < end; i++) {
				IGraphResultPref temp = new GraphResultNormalPrefix(
						orderedGIDs[i], gDB.getPrefixID(orderedGIDs[i]),
						gDB.getSuffix(orderedGIDs[i]), index);
				results.add(temp);
			}
			start = end;
			TimeComponent[1] += System.currentTimeMillis() - startTime;
			return results;
		}
	}

	@Override
	public int size() {
		return orderedGIDs.length;
	}

	@Override
	public IGraphFetcherPrefix join(IGraphFetcher fetcher) {
		int[] otherIDs = fetcher.getOrderedIDs();
		this.orderedGIDs = OrderedIntSets.join(orderedGIDs, otherIDs);
		return this;
	}

	@Override
	public IGraphFetcherPrefix remove(IGraphFetcher fetcher) {
		int[] otherIDs = fetcher.getOrderedIDs();
		this.orderedGIDs = OrderedIntSets.remove(orderedGIDs, otherIDs);
		return this;
	}

	@Override
	public IGraphFetcherPrefix remove(int[] sortedSet) {
		this.orderedGIDs = OrderedIntSets.remove(orderedGIDs, sortedSet);
		return this;
	}

	@Override
	public List<IGraphResultPref> getAllGraphs(long[] TimeComponent) {
		List<IGraphResultPref> answer = new ArrayList<IGraphResultPref>();
		List<IGraphResultPref> temp = this.getGraphs(TimeComponent);
		while (temp != null) {
			answer.addAll(temp);
			temp = this.getGraphs(TimeComponent);
		}
		Collections.sort(answer);
		return answer;
	}

	public List<IGraphResultPref> getGraphsWithPrefixID(int termPos) {
		List<IGraphResultPref> allGraphs = this.getAllGraphs(new long[4]);
		List<IGraphResultPref> result = new ArrayList<IGraphResultPref>();
		for (IGraphResultPref oneGraph : allGraphs)
			if (oneGraph.getPrefixFeatureID() == termPos)
				result.add(oneGraph);
		return result;
	}
}
