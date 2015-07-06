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

public class SupSearch_CIndexTopDown implements ISearcher {
	protected CIndexTree indexTreeSearcher;
	private VerifierISO verifier;
	private IPostingFetcher postingFetcher;

	public SupSearch_CIndexTopDown(CIndexTree searcher,
			IPostingFetcher postingFetcher, VerifierISO verifier) {
		this.indexTreeSearcher = searcher;
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
		List<Integer> noContainedSubs = indexTreeSearcher.getNoSubgraphs(query,
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

	public static String getIndexName() {
		return "index";
	}

	public static String getLuceneName() {
		return "lucene";
	}
}
