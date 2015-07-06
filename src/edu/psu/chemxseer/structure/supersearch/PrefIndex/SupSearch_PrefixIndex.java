package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherDB;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherIndexPrefix;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherLucenePrefix;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcherPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

public class SupSearch_PrefixIndex implements ISearcher {
	protected SupSearch_PrefixIndex upperLevelIndex;
	protected PrefixIndexSearcher searcher;
	protected IPostingFetcher postingFetcher;
	protected VerifierISOPrefix verifier;

	protected PrefixIndexSearcher lowerLevelSearcher;

	/**
	 * Build One Flat SupSearch_PrefixIndex: Not upper Level Index, Not Lower
	 * Level Index
	 * 
	 * @param indexSearcher
	 * @param postingFetcher
	 * @param verifier
	 */
	public SupSearch_PrefixIndex(PrefixIndexSearcher indexSearcher,
			IPostingFetcher postingFetcher, VerifierISOPrefix verifier) {
		this.searcher = indexSearcher;
		this.postingFetcher = postingFetcher;
		this.verifier = verifier;
		this.upperLevelIndex = null;
		this.lowerLevelSearcher = null;
	}

	/**
	 * Build One SupSearch_PrefIndex with Both UpperLevelIndex & LowerLevelIndex
	 * 
	 * @param indexSearcher
	 * @param postingFetcher
	 * @param verifier
	 * @param upperLevelIndex
	 * @param lowerSearcher
	 */
	public SupSearch_PrefixIndex(PrefixIndexSearcher indexSearcher,
			IPostingFetcher postingFetcher, VerifierISOPrefix verifier,
			SupSearch_PrefixIndex upperLevelIndex,
			PrefixIndexSearcher lowerSearcher) {
		this.searcher = indexSearcher;
		this.postingFetcher = postingFetcher;
		this.verifier = verifier;
		this.upperLevelIndex = upperLevelIndex;
		this.lowerLevelSearcher = lowerSearcher;
	}

	public void addLowerIndexSearcher(PrefixIndexSearcher lowerIndexSearcher) {
		this.lowerLevelSearcher = lowerIndexSearcher;
	}

	@Override
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number) {
		if (this.upperLevelIndex == null)
			return this.getAnswerNormal(query, TimeComponent, Number);
		else
			return this.getAnswerRecursive(query, TimeComponent, Number);
	}

	@Override
	public int[][] getAnswerIDs(Graph query) {
		List<IGraphResult> answer = this.getAnswer(query, new long[4],
				new int[2]);
		int[] result = new int[answer.size()];
		List<Integer> result2 = new ArrayList<Integer>();
		int counter1 = 0;
		for (IGraphResult oneAnswer : answer) {
			if (oneAnswer.getG().getEdgeCount() == query.getEdgeCount())
				result2.add(oneAnswer.getID());
			else
				result[counter1++] = oneAnswer.getID();
		}
		int[][] finalResult = new int[2][];
		finalResult[0] = Arrays.copyOf(result, counter1);
		finalResult[1] = new int[result2.size()];
		for (int w = 0; w < result2.size(); w++)
			finalResult[1][w] = result2.get(w);
		return finalResult;
	}

	/**
	 * Recursively call the UpperLevelIndex
	 * 
	 * @param query
	 * @param TimeComponent
	 * @param Number
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<IGraphResult> getAnswerRecursive(Graph query,
			long[] TimeComponent, int[] Number) {
		// 1. First, get Answer of the upperLevelIndex
		long startTime = System.currentTimeMillis();
		long[] temp = new long[4];
		int[] tempNum = new int[2];
		// The prefix subgraph isomorphism test is calculated as filtering cost
		List<IGraphResult> containedFeatures = this.upperLevelIndex.getAnswer(
				query, temp, tempNum);
		int[] containedFeatureIDs = new int[containedFeatures.size()];
		for (int i = 0; i < containedFeatureIDs.length; i++) {
			containedFeatureIDs[i] = containedFeatures.get(i).getDocID();
		}
		TimeComponent[2] += System.currentTimeMillis() - startTime;
		List<Integer> nonSubgraphs = this.searcher.nonSubgraphs(
				containedFeatureIDs, TimeComponent);
		// 2. Filtering
		IGraphFetcher candidateFetcher = postingFetcher.getComplement(
				nonSubgraphs, TimeComponent);
		IGraphFetcherPrefix candidateFetcherPrefix = null;
		if (this.lowerLevelSearcher == null)
			candidateFetcherPrefix = new GraphFetcherLucenePrefix(
					(GraphFetcherLucene) candidateFetcher, this.searcher);
		else
			candidateFetcherPrefix = new GraphFetcherIndexPrefix(
					(GraphFetcherDB) candidateFetcher, this.lowerLevelSearcher);

		// 3. Verify
		Number[0] = candidateFetcherPrefix.size();
		List<IGraphResult> answer = this.verifier.verify(
				candidateFetcherPrefix, query, TimeComponent);
		Number[1] = answer.size();
		return answer;
	}

	private List<IGraphResult> getAnswerNormal(Graph query,
			long[] TimeComponent, int[] Number) {
		// 1. Get All the Subgraphs not contained in the query
		List<Integer> nonSubgraphs = searcher
				.nonSubgraphs(query, TimeComponent);
		// 2. Filtering: the DISK Lucene Posting Fetcher
		IGraphFetcher candidateFetcher = postingFetcher.getComplement(
				nonSubgraphs, TimeComponent);
		IGraphFetcherPrefix candidateFetcherPrefix = null;
		if (this.lowerLevelSearcher == null)
			candidateFetcherPrefix = new GraphFetcherLucenePrefix(
					(GraphFetcherLucene) candidateFetcher, this.searcher);
		else
			candidateFetcherPrefix = new GraphFetcherIndexPrefix(
					(GraphFetcherDB) candidateFetcher, this.lowerLevelSearcher);
		// 3. Verify
		Number[0] = candidateFetcherPrefix.size();
		List<IGraphResult> answer = this.verifier.verify(
				candidateFetcherPrefix, query, TimeComponent);
		Number[1] = answer.size();
		return answer;
	}

	public static String getLucene() {
		return "lucene/";
	}

	public static String getIndexName() {
		return "index";
	}

	public PrefixIndexSearcher getSearcher() {
		return this.searcher;
	}
	// /**
	// * Build One SupSearch_PrefxIndex with LowerLevelIndex
	// * @param indexSearcher
	// * @param postingFetcher
	// * @param verifier
	// * @param lowerLevelSearcher
	// */
	// public SupSearch_PrefixIndex(PrefixIndexSearcher indexSearcher,
	// PostingFetcher postingFetcher,
	// VerifierISOPrefix verifier, PrefixIndexSearcher lowerLevelSearcher){
	// this.searcher = indexSearcher;
	// this.postingFetcher = postingFetcher;
	// this.verifier = verifier;
	// this.upperLevelIndex = null;
	// this.lowerLevelSearcher = lowerLevelSearcher;
	//
	// }
	// /**
	// * Build One SupSearch_PrefixIndex with UpperLevelIndex
	// * @param indexSearcher
	// * @param postingFetcher
	// * @param verifier
	// * @param uppderLevelIndex
	// */
	// public SupSearch_PrefixIndex(PrefixIndexSearcher indexSearcher,
	// PostingFetcher postingFetcher,
	// VerifierISOPrefix verifier, SupSearch_PrefixIndex upperLevelIndex){
	// this.searcher = indexSearcher;
	// this.postingFetcher = postingFetcher;
	// this.verifier = verifier;
	// this.upperLevelIndex = upperLevelIndex;
	// }
}
