package edu.psu.chemxseer.structure.subsearch.Gindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex1;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

public class SubSearch_Gindex implements ISearcher {
	public IIndex1 indexSearcher;
	public IPostingFetcher postingFetcher;
	public VerifierISO verifier;

	public SubSearch_Gindex(IIndex1 indexSearcher,
			PostingFetcherLucene postingFetcher, VerifierISO verifier) {
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
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = 0;

		List<IGraphResult> answer = null;
		int[] temp = new int[1];
		answer = this.hitAndReturn(query, temp, TimeComponent);

		if (answer != null) {
			Number[0] = 0; // No verification is needed
		} else {
			IGraphFetcher candidateFetcher;
			candidateFetcher = this
					.candidateByFeatureJoin(query, TimeComponent);
			Number[0] = candidateFetcher.size();
			answer = this.verifier.verify(query, candidateFetcher, true,
					TimeComponent);
		}
		Number[1] = answer.size();
		return answer;
	}

	private List<IGraphResult> hitAndReturn(Graph query, int[] hitIndex,
			long[] TimeComponent) {

		boolean[] exactMatch = new boolean[1];
		exactMatch[0] = false;
		hitIndex[0] = indexSearcher.designedSubgraph(query, exactMatch,
				TimeComponent);
		if (hitIndex[0] == -1)
			return null;
		else if (exactMatch[0]) {
			List<IGraphResult> result = null;
			IGraphFetcher gf = this.postingFetcher.getPosting(hitIndex[0],
					TimeComponent);
			result = gf.getAllGraphs(TimeComponent);
			return result;
		} else
			return null;
	}

	public IGraphFetcher candidateByFeatureJoin(Graph query,
			long[] TimeComponent) {
		List<Integer> features = indexSearcher.maxSubgraphs(query,
				TimeComponent);
		if (features == null || features.size() == 0)
			return null;
		else
			return postingFetcher.getJoin(features, TimeComponent);
	}

	/********* The Flowing Will be Replace Soon *****************************/
	public static String getLuceneName() {
		return "lucene/";
	}

	public static String getIndexName() {
		return "index";
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
