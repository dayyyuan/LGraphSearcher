package edu.psu.chemxseer.structure.setcover.update;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.supersearch.LWFull.LindexSearcher_Ext;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_Lindex;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class SupSearch_LindexUpdatable extends SupSearch_Lindex implements
		ISearch_LindexUpdatable {
	// Query Related: constructed after tracking & before updating
	public PostingFetcherMem queryFetcher; // given fID, fetcher the queries
											// containing fID
	// Query Related: tracking
	// given qID, fetcher the features contained in F (not include feature
	// isomorphic to F)
	private Queue<int[]> queryFeatureContain;
	private Queue<String> queries; // the list of queries
	private int[] gQCount; // gQCount[gID] = # of queries containing the graph g
	private GraphParser querySerializer;
	private int queryCapacity; // the maximum number queries that can be stored

	private LindexSearcherUpdatable indexUpdator;

	/**
	 * Constructor No queries are fed, queries are collected during query
	 * processing
	 * 
	 * @param indexSearcher
	 * @param postingFetcher
	 * @param verifier
	 * @param querySerializer
	 * @param qCapacity
	 *            : the maximum number of queries the system can hold
	 */
	public SupSearch_LindexUpdatable(LindexSearcher_Ext indexSearcher,
			PostingFetcherMem postingFetcher, VerifierISO verifier,
			GraphParser querySerializer, int qCapacity) {
		super(indexSearcher, postingFetcher, verifier);
		this.indexUpdator = new LindexSearcherUpdatable(indexSearcher);
		this.queryFeatureContain = new LinkedList<int[]>();
		this.queries = new LinkedList<String>();
		this.querySerializer = querySerializer;
		this.queryCapacity = qCapacity;
	}

	/**
	 * Constructor with input queries
	 * 
	 * @param indexSearcher
	 * @param postingFetcher
	 * @param verifier
	 * @param querySerializer
	 * @param queries
	 */
	public SupSearch_LindexUpdatable(LindexSearcher_Ext indexSearcher,
			PostingFetcherMem postingFetcher, VerifierISO verifier,
			GraphParser querySerializer, IGraphDatabase qDB) {
		super(indexSearcher, postingFetcher, verifier);
		this.indexUpdator = new LindexSearcherUpdatable(indexSearcher);
		this.queryFeatureContain = new LinkedList<int[]>();
		this.queries = new LinkedList<String>();
		this.gQCount = new int[this.getGCount()];
		Arrays.fill(gQCount, 0);
		this.querySerializer = querySerializer;
		this.queryCapacity = qDB.getTotalNum();
		for (Graph q : qDB) {
			this.getAnswer(q, new long[4], new int[2], true);
		}
	}

	@Override
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number, boolean recordQuery) {
		// First look for g's subgraphs
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = 0;
		List<IGraphResult> answer = null;
		List<Integer> minNonSubgraphs = indexSearcher.minNoSubgraphs(query,
				TimeComponent);
		// TODO: all Subgraphs can be obtained by tracing back the subgraphs
		// instead of running the subgraph isomorphism test again.
		List<Integer> allSubgraphs = indexSearcher.subgraphs(query,
				TimeComponent);
		IGraphFetcher candidateFetcher = postingFetcher.getComplement(
				minNonSubgraphs, TimeComponent);
		Number[0] = candidateFetcher.size();
		answer = this.verifier.verify(query, candidateFetcher, false,
				TimeComponent);
		Number[1] = answer.size();
		if (recordQuery == true) {
			if (queryCapacity == queries.size()) {
				queryFeatureContain.poll();
				Graph oldQuery = null;
				try {
					oldQuery = querySerializer.parse(queries.poll(),
							MyFactory.getGraphFactory());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (oldQuery != null) {
					int[][] oldAnswer = this.getAnswerIDs(oldQuery);
					for (int i = 0; i < oldAnswer[0].length; i++)
						gQCount[oldAnswer[0][i]]--;
					for (int i = 0; i < oldAnswer[1].length; i++)
						gQCount[oldAnswer[1][i]]--;
				}
			}
			queryFeatureContain.offer(OrderedIntSets.toArray(allSubgraphs));
			this.queries.offer(querySerializer.serialize(query));
			for (IGraphResult graph : answer)
				gQCount[graph.getID()]++;

			// float ratio = ((float)(Number[0]+1))/((float)(Number[1] + 1));
		}
		return answer;
	}

	@Override
	public void constructQueryPosting() {
		// Assumption: qIDs start from 0 to |Q|
		PostingBuilderMem temp = new PostingBuilderMem();
		int qID = 0;
		for (int[] fIDs : this.queryFeatureContain) {
			for (int fID : fIDs) {
				temp.insertOnePostingOneGraph(fID, qID);
			}
			qID++;
		}
		String[] queryDB = new String[queries.size()];
		this.queries.toArray(queryDB);
		this.queryFetcher = new PostingFetcherMem(new GraphDatabase_InMem(
				queryDB, querySerializer), temp);
	}

	@Override
	public int getQCount() {
		return this.queries.size();
	}

	@Override
	public int getGCount() {
		return this.postingFetcher.getDBSize();
	}

	@Override
	public int[] getQGCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int[] getGQCount() {
		return this.gQCount;
	}

	@Override
	public int[] getQEqual() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IGraphDatabase getGraphDB(int[] gIDs) {
		IGraphDatabase gDB = ((PostingFetcherMem) this.postingFetcher).getDB();
		String[] result = new String[gIDs.length];
		for (int i = 0; i < gIDs.length; i++)
			result[i] = gDB.findGraphString(gIDs[i]);
		return new GraphDatabase_InMem(result, gDB.getParser());
	}

	@Override
	public IGraphDatabase getGraphDB() {
		return ((PostingFetcherMem) this.postingFetcher).getDB();
	}

	@Override
	public IGraphDatabase getQueryDB(int[] qIDs) {
		String[] result = new String[qIDs.length];
		List<String> temp = new ArrayList<String>();
		temp.addAll(this.queries);
		for (int i = 0; i < qIDs.length; i++)
			result[i] = temp.get(qIDs[i]);
		return new GraphDatabase_InMem(result, this.querySerializer);
	}

	@Override
	public IGraphDatabase getQueryDB() {
		return new GraphDatabase_InMem(this.queries, this.querySerializer);
	}

	@Override
	// Assume that noVerifiedTrueG and noVerifieFalseG are all sorted
	public int[][] getContainingGraphandQuery(Graph newGraph,
			int[] noVerifiedTrueG, int[] noVerifiedFalseG,
			int[] noVerifiedTrueQ, int[] noVerifiedFalseQ) {
		List<IGraphResult> gAnswer = null, qAnswer = null;
		int[][] gSupport = null, qSupport = null;

		List<Integer> maxSubgraphs = indexSearcher.maxSubgraphs(newGraph,
				new long[4]);
		if (maxSubgraphs != null && maxSubgraphs.get(0) == -1) {// graph g hits
																// on one of the
																// index term
			IGraphFetcher gAnswerFetcher = this.postingFetcher.getPosting(
					maxSubgraphs.get(1), new long[4]);
			IGraphFetcher qAnswerFetcher = this.queryFetcher.getPosting(
					maxSubgraphs.get(1), new long[4]);
			gAnswer = gAnswerFetcher.getAllGraphs(new long[4]);
			qAnswer = qAnswerFetcher.getAllGraphs(new long[4]);
			gSupport = getIDs(newGraph, gAnswer, new int[0]);
			qSupport = getIDs(newGraph, qAnswer, new int[0]);
		} else {
			List<Integer> superGraphs = indexSearcher.minimalSupergraphs(
					newGraph, new long[4], maxSubgraphs);
			IGraphFetcher gCandidateFetcher = this.postingFetcher.getJoin(
					maxSubgraphs, new long[4]);
			IGraphFetcher qCandidateFetcher = this.queryFetcher.getJoin(
					maxSubgraphs, new long[4]);

			int[] gNoVerifiedTrue = noVerifiedTrueG.clone();
			int[] qNoVerifiedTrue = noVerifiedTrueQ.clone();

			if (superGraphs != null && superGraphs.size() > 0) {
				IGraphFetcher gTrueFetcher = this.postingFetcher.getUnion(
						superGraphs, new long[4]);
				IGraphFetcher qTrueFetcher = this.queryFetcher.getUnion(
						superGraphs, new long[4]);
				gNoVerifiedTrue = OrderedIntSets.getUnion(gNoVerifiedTrue,
						gTrueFetcher.getOrderedIDs());
				qNoVerifiedTrue = OrderedIntSets.getUnion(qNoVerifiedTrue,
						qTrueFetcher.getOrderedIDs());
			}

			gCandidateFetcher.remove(gNoVerifiedTrue).remove(noVerifiedFalseG);
			qCandidateFetcher.remove(qNoVerifiedTrue).remove(noVerifiedFalseQ);

			gAnswer = this.verifier.verify(newGraph, gCandidateFetcher, true,
					new long[4]);
			qAnswer = this.verifier.verify(newGraph, qCandidateFetcher, true,
					new long[4]);
			gSupport = getIDs(newGraph, gAnswer, gNoVerifiedTrue);
			qSupport = getIDs(newGraph, qAnswer, qNoVerifiedTrue);
		}
		int[][] result = new int[4][];
		result[0] = gSupport[0];
		result[1] = gSupport[1];
		result[2] = qSupport[0];
		result[3] = qSupport[1];
		return result;
	}

	@Override
	// Assume that trueAnswer does not have a graph equals to the query
	public int[][] getIDs(Graph query, List<IGraphResult> verified,
			int[] trueAnswer) {
		int[] result = new int[verified.size()];
		List<Integer> result2 = new ArrayList<Integer>();
		int counter1 = 0;
		for (IGraphResult oneAnswer : verified) {
			if (oneAnswer.getG().getEdgeCount() == query.getEdgeCount())
				result2.add(oneAnswer.getID());
			else
				result[counter1++] = oneAnswer.getID();
		}
		int[][] finalResult = new int[2][];
		finalResult[0] = Arrays.copyOf(result, counter1);
		if (trueAnswer.length > 0)
			finalResult[0] = OrderedIntSets
					.getUnion(finalResult[0], trueAnswer);
		finalResult[1] = new int[result2.size()];
		for (int w = 0; w < result2.size(); w++)
			finalResult[1][w] = result2.get(w);
		return finalResult;
	}

	@Override
	public LindexSearcherUpdatable getIndexUpdator() {
		return this.indexUpdator;
	}

	@Override
	public LindexSearcher getIndexSearcher() {
		return this.indexSearcher;
	}

	@Override
	public PostingFetcherMem getGraphFetcher() {
		return (PostingFetcherMem) this.postingFetcher;
	}

	@Override
	public PostingFetcherMem getQueryFetcher() {
		return this.queryFetcher;
	}

	@Override
	public List<Integer> getEqualQ(int tID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doSwap(SetwithScore[] swapPair, int termPos) {
		Graph newGraph = swapPair[1].getFeature().getFeature()
				.getFeatureGraph();
		int[] dbPosting = swapPair[1].getFeature().containedDatabaseGraphs();
		int[] queryPosting = swapPair[1].getFeature().containedQueryGraphs();
		this.indexUpdator.swapIndexTerm(newGraph, termPos, dbPosting.length);
		((PostingFetcherMem) this.postingFetcher).updatePosting(termPos,
				dbPosting);
		this.queryFetcher.updatePosting(termPos, queryPosting);
	}

	@Override
	public int[][] getUnExamedQG(Set_Pair setPair) {
		// for supergraph search:
		// the UnExamed Queries contain those queries uncovered by the feature,
		// but covered by its subgraphs
		// the UnExamed Graphs contain those graphs covered by the feature, but
		// not covered by its sueprgraphs.
		// return the unExamedQ & unExamedG
		int[][] result = new int[2][];
		Graph f = setPair.getFeature().getFeature().getFeatureGraph();
		List<Integer> maxSubs = indexSearcher.maxSubgraphs(f, new long[4]);

		if (maxSubs == null || maxSubs.size() == 0)
			System.out
					.println("Exception because the setPair does not contain or equals to any edge features");
		else if (maxSubs.get(0) == -1) {
			result[0] = result[1] = new int[0]; // there is no need for
												// evaluation,
			// since the feature equals to an already selected one
		} else {
			List<Integer> minSups = this.indexSearcher.minimalSupergraphs(f,
					new long[4], maxSubs);
			int[] filteredGraphs = this.postingFetcher.getUnion(minSups,
					new long[4]).getOrderedIDs();
			result[1] = OrderedIntSets.remove(setPair.getValueG(),
					filteredGraphs);

			List<Integer> edgeSubs = this.indexSearcher.containingEdges(f);
			int[] coveredQueries = this.postingFetcher.getUnion(edgeSubs,
					new long[4]).getOrderedIDs();
			result[0] = OrderedIntSets.remove(coveredQueries,
					setPair.getValueQ());
		}
		return result;
	}

	@Override
	public boolean clearQueryLogs() {
		queryFeatureContain.clear();
		this.queries.clear();
		return true;
	}

	@Override
	public boolean shrinkQueryWindow(int toSize) {
		return this.queries.size() > 0.1 * this.getGCount();
	}
}
