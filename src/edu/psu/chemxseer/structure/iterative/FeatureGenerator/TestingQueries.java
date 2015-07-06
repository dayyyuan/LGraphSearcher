package edu.psu.chemxseer.structure.iterative.FeatureGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iterative.LindexUpdate.SubgraphSearch_LindexUpdate;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphs;
import edu.psu.chemxseer.structure.query.InFrequentQueryGenerater;
import edu.psu.chemxseer.structure.query.TestQuery;

/**
 * This class is in charge of generating queries and select queries with very
 * big gap or very big candidate set
 * 
 * @author duy113
 * 
 */
public class TestingQueries {

	/**
	 * Given the generated queryFile, load all the queries
	 * 
	 * @param queryFile
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */
	public HashMap<Graph, Integer> loadQueriesRaw(String queryFile)
			throws IOException, ParseException, MathException {
		InFrequentQueryGenerater gen = new InFrequentQueryGenerater();
		// gen.generateInFrequentQueriesUniform2(4, 10, 1000, gDB, edgeIndex);
		IGraphs[] allQ = gen.loadInfrequentQueries(queryFile);
		// qMaps<String, Frequency>
		HashMap<String, Integer> maps1 = new HashMap<String, Integer>();
		HashMap<String, Graph> maps2 = new HashMap<String, Graph>();
		for (IGraphs Qs : allQ) {
			for (int i = 0; i < Qs.getGraphNum(); i++) {
				String label = Qs.getLabel(i);
				if (maps1.containsKey(label))
					maps1.put(label, maps1.get(label) + 1);
				else {
					maps1.put(label, 1);
					maps2.put(label, Qs.getGraph(i));
				}
			}
		}

		HashMap<Graph, Integer> qMaps = new HashMap<Graph, Integer>();
		for (Entry<String, Integer> entry : maps1.entrySet()) {
			Graph g = maps2.get(entry.getKey());
			qMaps.put(g, entry.getValue());
		}
		return qMaps;
	}

	/**
	 * Filter the queries with small gaps
	 * 
	 * @param qMaps
	 * @param index
	 * @param queryFile
	 * @param byGapSize
	 *            : true then prune by gap size, false then prune by candidate
	 *            size
	 * @throws Exception
	 */
	public void gapsFiltering(HashMap<Graph, Integer> qMaps,
			SubgraphSearch_LindexUpdate index, String queryFile,
			boolean byGapSize) throws Exception {
		List<TestQuery> queries = new ArrayList<TestQuery>();
		long[] TimeComponent = new long[4];
		for (Entry<Graph, Integer> oneEntry : qMaps.entrySet()) {
			Graph g = oneEntry.getKey();
			List<Integer> maxSubs = index.indexSearcher.maxSubgraphs(g,
					TimeComponent);
			if (maxSubs.get(0) == -1)
				continue;
			else {
				IGraphFetcher candidateFetcher = index.getCandidate(maxSubs);
				if (byGapSize) {
					int[] gaps = index.getGap(g, candidateFetcher);
					queries.add(new TestQuery(g, gaps.length, oneEntry
							.getValue()));
				} else
					queries.add(new TestQuery(g, candidateFetcher.size(),
							oneEntry.getValue()));
			}
		}
		Collections.sort(queries);
		this.filterAndWrite(queries, queryFile);
	}

	// Only Choose the Top 20%
	private void filterAndWrite(List<TestQuery> queries, String queryFile)
			throws IOException {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 0; i < queries.size(); i++) {
			stats.addValue(queries.get(i).getScores());
		}
		double threshold = stats.getPercentile(80);
		BufferedWriter writer = new BufferedWriter(new FileWriter(queryFile));
		for (int i = 0; i < queries.size(); i++) {
			TestQuery q = queries.get(i);
			if (q.getScores() < threshold)
				continue;
			else {
				writer.write(q.toString() + "\n");
			}
		}
		writer.close();
	}

	/**
	 * Gaps filtering for the Fgindex, since all the subgraphs > upperBound can
	 * be answered directly by FGindex So here, we discard those kind of index
	 * 
	 * @param qMaps
	 * @param index
	 * @param queryFile
	 * @param uppBound
	 * @param byGapSize
	 *            : true then prune by gap size, false then prune by candidate
	 *            size
	 * @throws Exception
	 */
	public void gapsFiltering(HashMap<Graph, Integer> qMaps,
			SubgraphSearch_LindexUpdate index, String queryFile, int uppBound,
			boolean byGraphSize) throws Exception {
		List<TestQuery> queries = new ArrayList<TestQuery>();
		long[] TimeComponent = new long[4];
		for (Entry<Graph, Integer> oneEntry : qMaps.entrySet()) {
			Graph g = oneEntry.getKey();
			List<Integer> maxSubs = index.indexSearcher.maxSubgraphs(g,
					TimeComponent);
			if (maxSubs.get(0) == -1)
				continue;
			else {
				IGraphFetcher candidateFetcher = index.getCandidate(maxSubs);
				int[] gaps = index.getGap(g, candidateFetcher);
				int support = candidateFetcher.size() - gaps.length;
				if (support >= uppBound)
					continue;
				else if (byGraphSize)
					queries.add(new TestQuery(g, gaps.length, oneEntry
							.getValue()));
				else
					queries.add(new TestQuery(g, candidateFetcher.size(),
							oneEntry.getValue()));
			}
		}
		Collections.sort(queries);
		this.filterAndWrite(queries, queryFile);
	}

	/**
	 * 
	 * @param queryFile
	 * @return
	 * @throws IOException
	 */
	public ArrayList<TestQuery> loadQueries(String queryFile)
			throws IOException {
		ArrayList<TestQuery> qs = new ArrayList<TestQuery>();
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		String aline = reader.readLine();
		while (aline != null) {
			TestQuery oneQ = new TestQuery(aline);
			qs.add(oneQ);
			aline = reader.readLine();
		}
		reader.close();
		return qs;
	}

}
