package edu.psu.chemxseer.structure.postings.Impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class GraphFetcherDB implements IGraphFetcher {
	public IGraphDatabase gDB;
	public int[] orderedGIDs;
	public int start;

	/**
	 * Assume that the inputGIDs is well sorted
	 * 
	 * @param gDB
	 * @param inputGIDs
	 * @param reverse
	 */
	public GraphFetcherDB(IGraphDatabase gDB, int[] inputGIDs, boolean reverse) {
		this.gDB = gDB;
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

	/**
	 * Copy Constructor
	 * 
	 * @param gFetcher
	 */
	public GraphFetcherDB(GraphFetcherDB gFetcher) {
		this.gDB = gFetcher.gDB;
		this.orderedGIDs = gFetcher.orderedGIDs;
	}

	@Override
	public int[] getOrderedIDs() {
		return orderedGIDs;
	}

	@Override
	public List<IGraphResult> getGraphs(long[] TimeComponent) {
		if (start == orderedGIDs.length)
			return null;
		else {
			long startTime = System.currentTimeMillis();
			int end = Math.min(start + batchCount, orderedGIDs.length);
			List<IGraphResult> results = new ArrayList<IGraphResult>();
			for (int i = start; i < end; i++) {
				IGraphResult temp = new GraphResultNormal(orderedGIDs[i],
						gDB.findGraph(orderedGIDs[i]));
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
	public IGraphFetcher join(IGraphFetcher fetcher) {
		int[] otherIDs = fetcher.getOrderedIDs();
		this.orderedGIDs = OrderedIntSets.join(orderedGIDs, otherIDs);
		return this;
	}

	@Override
	public IGraphFetcher remove(IGraphFetcher fetcher) {
		int[] otherIDs = fetcher.getOrderedIDs();
		this.orderedGIDs = OrderedIntSets.remove(this.orderedGIDs, otherIDs);
		return this;
	}

	@Override
	public IGraphFetcher remove(int[] orderedSet) {
		this.orderedGIDs = OrderedIntSets.remove(orderedGIDs, orderedSet);
		return this;
	}

	@Override
	public List<IGraphResult> getAllGraphs(long[] TimeComponent) {
		List<IGraphResult> answer = new ArrayList<IGraphResult>();
		List<IGraphResult> temp = this.getGraphs(TimeComponent);
		while (temp != null) {
			answer.addAll(temp);
			temp = this.getGraphs(TimeComponent);
		}
		Collections.sort(answer);
		return answer;
	}

}
