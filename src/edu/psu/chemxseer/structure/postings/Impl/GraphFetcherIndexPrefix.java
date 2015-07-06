package edu.psu.chemxseer.structure.postings.Impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcherPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResultPref;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;

public class GraphFetcherIndexPrefix implements IGraphFetcherPrefix {
	private IIndexPrefix gDB;
	private GraphFetcherDB gFetcher;

	/**
	 * Constructor the GraphFetcherDBPrefix.
	 * 
	 * @param gFetcher
	 * @param gDB
	 *            : the database containing all the features
	 */
	public GraphFetcherIndexPrefix(GraphFetcherDB gFetcher, IIndexPrefix gDB) {
		this.gFetcher = gFetcher;
		this.gDB = gDB;
	}

	/**
	 * The Only Different is that here the getGraphs return the
	 * "GraphREsultNormalPrefix" which implements the GraphResultPrefix
	 * interface
	 */
	@Override
	public List<IGraphResultPref> getGraphs(long[] TimeComponent) {
		if (gFetcher.start == gFetcher.orderedGIDs.length)
			return null;
		else {
			long startTime = System.currentTimeMillis();
			int end = Math.min(gFetcher.start + batchCount,
					gFetcher.orderedGIDs.length);
			List<IGraphResultPref> results = new ArrayList<IGraphResultPref>();
			for (int i = gFetcher.start; i < end; i++) {
				int prefixID = this.gDB.getPrefixID(gFetcher.orderedGIDs[i]);
				IGraphResultPref temp = new GraphResultNormalPrefix(
						gFetcher.orderedGIDs[i], prefixID,
						gDB.getExtension(gFetcher.orderedGIDs[i]), gDB);
				results.add(temp);
			}
			gFetcher.start = end;
			TimeComponent[1] += System.currentTimeMillis() - startTime;
			return results;
		}
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

	@Override
	public int[] getOrderedIDs() {
		return gFetcher.getOrderedIDs();
	}

	@Override
	public IGraphFetcherPrefix join(IGraphFetcher fetcher) {
		this.gFetcher.join(fetcher);
		return this;
	}

	@Override
	public IGraphFetcherPrefix remove(IGraphFetcher fetcher) {
		gFetcher.remove(fetcher);
		return this;
	}

	@Override
	public IGraphFetcherPrefix remove(int[] orderedSet) {
		gFetcher.remove(orderedSet);
		return this;
	}

	@Override
	public int size() {
		return gFetcher.size();
	}

}
