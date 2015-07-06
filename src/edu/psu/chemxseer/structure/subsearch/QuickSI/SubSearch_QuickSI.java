package edu.psu.chemxseer.structure.subsearch.QuickSI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex1;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

public class SubSearch_QuickSI implements ISearcher {
	private IIndex1 indexSearcher;
	private IPostingFetcher postingFetcher;
	private VerifierISO verifier;

	public SubSearch_QuickSI(IIndex1 indexSearcher, IPostingFetcher postings,
			VerifierISO verifier) {
		this.indexSearcher = indexSearcher;
		this.postingFetcher = postings;
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
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = 0;
		List<IGraphResult> answer = null;
		// 1. first step: find the sudo maxSubgraphs of graph g
		List<Integer> maxSubs = indexSearcher
				.maxSubgraphs(query, TimeComponent);
		if (maxSubs.get(0) == -1) {
			Number[0] = 0;
			IGraphFetcher answerFetcher = this.postingFetcher.getPosting(
					maxSubs.get(1), TimeComponent);
			answer = answerFetcher.getAllGraphs(TimeComponent);
		}
		// 2. get the candidate set
		else {
			IGraphFetcher candidateFetcher = this.postingFetcher.getJoin(
					maxSubs, TimeComponent);
			Number[0] = candidateFetcher.size();
			// 3. verification
			answer = this.verifier.verify(query, candidateFetcher, true,
					TimeComponent);
		}
		Number[1] = answer.size();
		return answer;
	}

	public static String getLuceneName() {
		return "lucene/";
	}

	public static String getIndexName() {
		return "index/";
	}

	// public void test(Graph g, int gID){
	// long[] time = new long[4];
	// int[] subs = this.indexSearcher.subgraphs(g, time);
	// for(int i = 0; i< subs.length;i++){
	// List<GraphResult> postings = this.postingFetcher.getPosting(subs[i],
	// time);
	// boolean contain = false;
	// for(GraphResult onePos : postings){
	// if(onePos.getID() == gID){
	// contain = true;
	// break;
	// }
	// }
	// if(contain==false){
	// System.out.println("Ill build " + gID + "," + subs.length);
	// }
	// }
	// }

}
