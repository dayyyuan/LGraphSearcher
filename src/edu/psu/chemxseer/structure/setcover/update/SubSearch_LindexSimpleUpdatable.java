package edu.psu.chemxseer.structure.setcover.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimple;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class SubSearch_LindexSimpleUpdatable extends SubSearch_LindexSimple
		implements ISearch_LindexUpdatable {
	// Query Related: constructed after tracking & before updating
	protected PostingFetcherMem queryFetcher; // given fID, fetcher the queries
												// containing fID
	protected HashMap<Integer, List<Integer>> featureEqualQ; // featureEqualQ[fID]
																// = qID, such
																// that f
																// isomorphic to
																// q

	// Query Related: tracking
	// given qID, fetcher the features contained in F (not include feature
	// isomorphic to F)
	protected Queue<int[]> queryFeatureContain;
	protected Queue<Integer> queryFeatureEqual;
	protected Queue<String> queries; // the list of queries
	protected Queue<Integer> qGCount;
	protected GraphParser querySerializer;
	protected int queryCapacity; // the maximum number queries that can be
									// stored
	protected LindexSearcherUpdatable indexUpdator;

	public SubSearch_LindexSimpleUpdatable(
			SubSearch_LindexSimpleUpdatable copyIndex) {
		super(copyIndex.indexSearcher, copyIndex.postingFetcher,
				copyIndex.verifier);
		this.queryFetcher = copyIndex.queryFetcher;
		this.featureEqualQ = copyIndex.featureEqualQ;
		this.queryFeatureContain = copyIndex.queryFeatureContain;
		this.queryFeatureEqual = copyIndex.queryFeatureEqual;
		this.queries = copyIndex.queries;
		this.qGCount = copyIndex.qGCount;
		this.querySerializer = copyIndex.querySerializer;
		this.queryCapacity = copyIndex.queryCapacity;
		this.indexUpdator = copyIndex.indexUpdator;
	}

	/**
	 * Constructor No queries are pre-fed, queries are collected during query
	 * processing
	 * 
	 * @param indexSearcher
	 * @param postingFetcher
	 * @param verifier
	 * @param querySerializer
	 * @param qCapacity
	 *            : the maximum number of queries the system can hold
	 */
	public SubSearch_LindexSimpleUpdatable(LindexSearcher indexSearcher,
			PostingFetcherMem postingFetcher, VerifierISO verifier,
			GraphParser querySerializer, int qCapacity) {
		super(indexSearcher, postingFetcher, verifier);
		this.indexUpdator = new LindexSearcherUpdatable(indexSearcher);
		this.queryFeatureContain = new LinkedList<int[]>();
		this.queryFeatureEqual = new LinkedList<Integer>();
		this.queries = new LinkedList<String>();
		this.qGCount = new LinkedList<Integer>();
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
	public SubSearch_LindexSimpleUpdatable(LindexSearcher indexSearcher,
			PostingFetcherMem postingFetcher, VerifierISO verifier,
			GraphParser querySerializer, IGraphDatabase query) {
		super(indexSearcher, postingFetcher, verifier);
		this.indexUpdator = new LindexSearcherUpdatable(indexSearcher);
		this.queryFeatureContain = new LinkedList<int[]>();
		this.queryFeatureEqual = new LinkedList<Integer>();
		this.queries = new LinkedList<String>();
		this.qGCount = new LinkedList<Integer>();
		this.querySerializer = querySerializer;
		this.queryCapacity = query.getTotalNum();
		int qCount = 0;
		for (int qID = 0; qID < queryCapacity; qID++) {
			Graph q = query.findGraph(qID);
			List<IGraphResult> r = this.getAnswer(q, new long[4], new int[2],
					true);
			if (r.size() > 0)
				qCount++;
		}
		System.out.println("Num of Non-zero queries: " + qCount);
	}

	@Override
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number, boolean recordQuery) {
		// First look for g's subgraphs
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = 0;

		List<IGraphResult> answer = null;
		List<Integer> maxSubgraphs = indexSearcher.maxSubgraphs(query,
				TimeComponent);
		// maxSubgraphs = indexSearcher.getRealMaxSubgraphs(maxSubgraphs);
		// TODO: all Subgraphs can be obtained by tracing back the subgraphs
		// instead of running the subgraph isomorphism test again.
		List<Integer> allSubgraphs = indexSearcher.subgraphs(query,
				TimeComponent);
		if (maxSubgraphs == null || maxSubgraphs.size() == 0) {
			// System.out.println("Empty Max-subgraphs for query: " +
			// MyFactory.getDFSCoder().serialize(query));
			return new ArrayList<IGraphResult>();
		} else if (maxSubgraphs.get(0) == -1) {// graph g hits on one of the
												// index term
			IGraphFetcher answerFetcher = this.postingFetcher.getPosting(
					maxSubgraphs.get(1), TimeComponent);
			answer = answerFetcher.getAllGraphs(TimeComponent);
			Number[0] = 0;
		} else {
			IGraphFetcher candidateFetcher = this.postingFetcher.getJoin(
					maxSubgraphs, TimeComponent);
			Number[0] = candidateFetcher.size();
			answer = this.verifier.verify(query, candidateFetcher, true,
					TimeComponent);
		}
		Number[1] = answer.size();
		if (recordQuery == true) {
			if (queryCapacity == queries.size()) {
				queryFeatureContain.poll();
				queryFeatureEqual.poll();
				this.queries.poll();
				this.qGCount.poll();
			}
			if (maxSubgraphs.get(0) == -1) {
				int tID = maxSubgraphs.get(1);
				queryFeatureContain.offer(indexUpdator.getParents(tID));
				queryFeatureEqual.offer(tID);
			} else {
				queryFeatureContain.offer(OrderedIntSets.toArray(allSubgraphs));
				queryFeatureEqual.offer(-1);
			}
			this.queries.offer(querySerializer.serialize(query));
			qGCount.offer(answer.size());
			int candidatesCount = Number[0];
			if (candidatesCount == 0)
				candidatesCount = Number[1];
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
		this.featureEqualQ = new HashMap<Integer, List<Integer>>();
		qID = 0;
		for (int fID : this.queryFeatureEqual) {
			if (fID >= 0) {
				if (!this.featureEqualQ.containsKey(fID))
					this.featureEqualQ.put(fID, new ArrayList<Integer>());
				this.featureEqualQ.get(fID).add(qID);
			}
			qID++;
		}
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
		int[] result = new int[this.qGCount.size()];
		int index = 0;
		for (Integer count : this.qGCount)
			result[index++] = count;
		return result;
	}

	// TODO: Need to be updated better
	@Override
	public int[] getQEqual() {
		int[] result = new int[this.qGCount.size()];
		Arrays.fill(result, 0);
		return result;
	}

	@Override
	public int[] getGQCount() {
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

		List<Integer> maxSubgraphs = this.indexSearcher.maxSubgraphs(newGraph,
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
			List<Integer> superGraphs = this.indexSearcher.minimalSupergraphs(
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
		return this.featureEqualQ.get(tID);
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
		// return the unExamedQ & unExamedG
		int[][] result = new int[2][];
		Graph f = setPair.getFeature().getFeature().getFeatureGraph();
		// For subgraph search, the UnExamedQ contains queries covered by the
		// features as the maximum subgraph.
		// the UnExamedG contains graphs uncovered by the feature, but covered
		// by its subgraphs
		List<Integer> maxSubs = this.indexSearcher.maxSubgraphs(f, new long[4]);
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
			int[] filteredQueries = this.queryFetcher.getUnion(minSups,
					new long[4]).getOrderedIDs();
			result[0] = OrderedIntSets.remove(setPair.getValueQ(),
					filteredQueries);
			List<Integer> edgeSubs = this.indexSearcher.containingEdges(f);
			int[] coveredSubgraphs = this.postingFetcher.getUnion(edgeSubs,
					new long[4]).getOrderedIDs();
			result[1] = OrderedIntSets.remove(coveredSubgraphs,
					setPair.getValueG());
		}
		return result;
	}

	@Override
	public boolean clearQueryLogs() {
		queryFeatureContain.clear();
		queryFeatureEqual.clear();
		this.queries.clear();
		this.qGCount.clear();
		return true;
	}

	@Override
	public boolean shrinkQueryWindow(int toSize) {
		if (this.queries.size() < toSize)
			return false;
		else {
			while (queries.size() > toSize) {
				this.queryFeatureContain.poll();
				this.queryFeatureEqual.poll();
				this.queries.poll();
				this.qGCount.poll();
			}
			return true;
		}
	}

	/**
	 * Return true if there is sufficient data to update the index
	 * 
	 * @return
	 */
	public boolean sufficientDataToUpdate() {
		return this.queries.size() > (0.1 * this.getGCount());
	}
}
