package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

/**
 * The Supergraph Searcher Corresponding to the CIndexFlat
 * 
 * @author dayuyuan
 * 
 */
public class SupSearch_CIndexFlat implements ISearcher {
	private CIndexFlat searcher;
	private IPostingFetcher postingFetcher;
	private VerifierISO verifier;

	public SupSearch_CIndexFlat(CIndexFlat searcher,
			IPostingFetcher postingFetcher, VerifierISO verifier) {
		this.searcher = searcher;
		this.postingFetcher = postingFetcher;
		this.verifier = verifier;
	}

	/**
	 * Filter + Verification Approach finding the answer for supergraph search
	 * 
	 * @param query
	 * @param TimeComponent
	 * @param Number
	 * @return
	 */
	@Override
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number) {
		ArrayList<Integer> noContainedSubs = searcher.getNoSubgraphs(query,
				TimeComponent);
		IGraphFetcher candidateFetcher = postingFetcher.getComplement(
				noContainedSubs, TimeComponent);
		Number[0] = candidateFetcher.size();
		List<IGraphResult> answer = verifier.verify(query, candidateFetcher,
				false, TimeComponent);
		Number[1] = answer.size();
		return answer;
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
	 * 
	 * @param query
	 * @param subsID
	 *            : subgraph features that are contained in the query
	 * @param TimeComponent
	 * @param Number
	 * @return
	 */
	public List<IGraphResult> getAnswer(Graph query, int[] subsID,
			long[] TimeComponent, int[] Number) {
		ArrayList<Integer> noContainedSubs = searcher.getNoSubgraphs(subsID,
				TimeComponent);
		IGraphFetcher candidateFetcher = postingFetcher.getComplement(
				noContainedSubs, TimeComponent);
		Number[0] = candidateFetcher.size();
		List<IGraphResult> answer = verifier.verify(query, candidateFetcher,
				false, TimeComponent);
		Number[1] = answer.size();
		return answer;
	}

	public static String getIndexName() {
		return "index";
	}

	public static String getLuceneName() {
		return "lucene";
	}

	public String[] getIndexFeatures() {
		return this.searcher.indexingGraphs;
	}
}
