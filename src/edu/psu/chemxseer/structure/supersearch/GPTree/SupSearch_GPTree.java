package edu.psu.chemxseer.structure.supersearch.GPTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherLucenePrefix;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

public class SupSearch_GPTree implements ISearcher {
	protected GPTreeSearcher dbPrefix; // Data base graphs prefix index
	protected VerifierISOPrefix verifier;

	protected CRGraphSearcher crGraph; // CRGraph for Filtering
	protected IPostingFetcher positngFetcher;

	/**
	 * Construct a SupSearch_GPTree
	 * 
	 * @param crGraph
	 * @param postingFetcher
	 * @param verifier
	 * @param prefixSearcher
	 */
	public SupSearch_GPTree(CRGraphSearcher crGraph,
			IPostingFetcher postingFetcher, VerifierISOPrefix verifier,
			GPTreeSearcher prefixSearcher) {
		this.crGraph = crGraph;
		this.positngFetcher = postingFetcher;
		this.verifier = verifier;
		this.dbPrefix = prefixSearcher;
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
		// 1. First Step, Filtering
		List<Integer> noSubgraphs = crGraph.getNonSubgraphs(query,
				TimeComponent);
		// 2. Filtering
		IGraphFetcher candidateFetcher = this.positngFetcher.getComplement(
				noSubgraphs, TimeComponent);
		GraphFetcherLucenePrefix candidateFetcherPrefix = new GraphFetcherLucenePrefix(
				(GraphFetcherLucene) candidateFetcher, dbPrefix);
		// 3. Verify
		Number[0] = candidateFetcherPrefix.size();
		List<IGraphResult> answer = verifier.verify(candidateFetcherPrefix,
				query, TimeComponent);
		Number[1] = answer.size();
		return answer;
	}

	public static String getCRPrefixName() {
		String result = new String("CRGraphPrefixIndex");
		return result;
	}

	public static String getCRGraphName() {
		String result = new String("CRGraph");
		return result;
	}

	public static String getLucene() {
		return "lucene";
	}

	public static String getPrefixIndex() {
		return "prefixIndex";
	}
}
