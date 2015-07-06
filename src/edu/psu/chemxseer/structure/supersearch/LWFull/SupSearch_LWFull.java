package edu.psu.chemxseer.structure.supersearch.LWFull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcherPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcherPrefix;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.LWTree.LWIndexSearcher;

public class SupSearch_LWFull implements ISearcher {
	protected LWIndexSearcher indexSearcher;
	protected VerifierISOPrefix verifier;
	protected IPostingFetcherPrefix postingFetcher;

	public SupSearch_LWFull(LWIndexSearcher indexSearcher,
			IPostingFetcherPrefix postingFetcher, VerifierISOPrefix verifier) {
		this.indexSearcher = indexSearcher;
		this.verifier = verifier;
		this.postingFetcher = postingFetcher;
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

	@Override
	/**
	 * TimeComponent[0] = posting fetching 
	 * TimeComponent[1] = DB graph loading time 
	 * TimeComponent[2] = filtering cost
	 * TimeComponent[3] = verification cost
	 * Number[0] = verified graphs number 
	 * Number[1] = True answer size
	 */
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number) {
		// 1. First step is to find all features that are contained in the query
		// graph
		List<Integer> minNonSubgraphs = indexSearcher.minNoSubgraphs(query,
				TimeComponent);

		// 2. Get all the candidate graphs: just a summation operation
		IGraphFetcherPrefix candidateFetcher = postingFetcher.getComplement(
				minNonSubgraphs, TimeComponent);
		Number[0] = candidateFetcher.size();
		// 3. Verification phase
		List<IGraphResult> answer = this.verifier.verify(candidateFetcher,
				query, TimeComponent);
		Number[1] = answer.size();
		return answer;
	}

	public static String getIndexName() {
		return "index";
	}

	public int getFeatureCount() {
		return this.indexSearcher.getFeatureCount();
	}
}
