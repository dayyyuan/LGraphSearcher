package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.util.SelfImplementSet;

public class SubSearch_Lindex implements ISearcher {
	public LindexSearcher indexSearcher;
	public IPostingFetcher postingFetcher;
	protected VerifierISO verifier;

	public SubSearch_Lindex(LindexSearcher indexSearcher,
			IPostingFetcher postingFetcher, VerifierISO verifier) {
		this.indexSearcher = indexSearcher;
		this.postingFetcher = postingFetcher;
		this.verifier = verifier;
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
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number) {
		// First look for g's subgraphs
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = 0;

		List<IGraphResult> answer = null;
		IGraphFetcher trueFetcher = null;
		List<Integer> maxSubgraphs = indexSearcher.maxSubgraphs(query,
				TimeComponent);

		if (maxSubgraphs != null && maxSubgraphs.get(0) == -1) {// graph g hits
																// on one of the
																// index term
			IGraphFetcher answerFetcher = this.postingFetcher.getPosting(
					maxSubgraphs.get(1), TimeComponent);
			answer = answerFetcher.getAllGraphs(TimeComponent);
			Number[0] = 0;
		} else {
			List<Integer> superGraphs = indexSearcher.minimalSupergraphs(query,
					TimeComponent, maxSubgraphs);
			IGraphFetcher candidateFetcher = this.postingFetcher.getJoin(
					maxSubgraphs, TimeComponent);

			SelfImplementSet<IGraphResult> set = new SelfImplementSet<IGraphResult>();
			if (superGraphs != null && superGraphs.size() > 0) {
				trueFetcher = this.postingFetcher.getUnion(superGraphs,
						TimeComponent);
				candidateFetcher = candidateFetcher.remove(trueFetcher);
			}
			Number[0] = candidateFetcher.size();
			answer = this.verifier.verify(query, candidateFetcher, true,
					TimeComponent);
			if (trueFetcher != null && trueFetcher.size() > 0) {
				set.clear();
				set.addAll(answer);
				set.addAll(trueFetcher.getAllGraphs(TimeComponent));
				answer = set.getItems();
			}
		}
		Number[1] = answer.size();
		return answer;
	}

	public static String getLuceneName() {
		return "lucene/";
	}

	public static String getIndexName() {
		return "index";
	}

	public IPostingFetcher getPostingFetcher() {
		return this.postingFetcher;
	}

	public VerifierISO getVerifier() {
		return this.verifier;
	}
}
