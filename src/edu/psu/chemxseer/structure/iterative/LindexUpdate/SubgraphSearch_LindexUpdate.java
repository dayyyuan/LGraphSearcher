package edu.psu.chemxseer.structure.iterative.LindexUpdate;

import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_Lindex;

public class SubgraphSearch_LindexUpdate {
	public LindexUpdateSearcher indexSearcher;
	private IPostingFetcher postingFetcher;
	protected PostingFetcherMem insertedPosting;
	private VerifierISO verifier;

	public SubgraphSearch_LindexUpdate(SubSearch_Lindex lindex,
			GraphDatabase_OnDisk gDB) {
		this.indexSearcher = new LindexUpdateSearcher(lindex.indexSearcher);
		this.postingFetcher = lindex.getPostingFetcher();
		this.verifier = lindex.getVerifier();
		this.insertedPosting = new PostingFetcherMem(gDB);
	}

	public SubgraphSearch_LindexUpdate(LindexUpdateSearcher indexSearcher,
			IPostingFetcher postingFetcher, PostingFetcherMem insertedPostings,
			VerifierISO verifier) {
		this.indexSearcher = indexSearcher;
		this.postingFetcher = postingFetcher;
		this.insertedPosting = insertedPostings;
		this.verifier = verifier;
	}

	/**
	 * Fist find the maximum subgraph of the query g, if it hits, then return
	 * empty array else return the gap
	 * 
	 * @param g
	 * @return
	 */
	public int[] getGap(Graph g) {
		long[] TimeComponent = new long[4];
		List<Integer> maxSubs = indexSearcher.maxSubgraphs(g, TimeComponent);
		if (maxSubs.get(0) == -1)
			return new int[0];
		else {
			IGraphFetcher candidateFetcher = this.getCandidate(maxSubs);
			return this.getGap(g, candidateFetcher);
		}
	}

	/**
	 * Return the Answer considering both old features & newly inserted features
	 * Difference: returned answer is not the "DBGraphID" but the instead the
	 * "GraphDocumentID"
	 * 
	 * @param query
	 * @return
	 */
	public int[] getGap(Graph query, IGraphFetcher candidateFetcher) {
		long[] TimeComponent = new long[4];
		// 2. Verification
		List<IGraphResult> answer = this.verifier.verifyFalse(query,
				candidateFetcher, true, TimeComponent);
		int[] result = new int[answer.size()];
		int index = 0;
		for (IGraphResult oneR : answer)
			result[index++] = oneR.getDocID();
		return result;
	}

	/**
	 * Return the candidate consider both old features & newly inserted features
	 * If maxsubs[0] == -1, return null;
	 * 
	 * @param maxSubs
	 * @return
	 */
	public IGraphFetcher getCandidate(List<Integer> maxSubs) {
		long[] TimeComponent = new long[4];
		if (maxSubs.get(0) == -1)
			return null;
		// 1. Decide which part of the maxSubs are new features, and which part
		// are old features
		List<Integer> oldMaxSub = new ArrayList<Integer>();
		List<Integer> newMaxSub = new ArrayList<Integer>();
		this.indexSearcher
				.seperageOldNewFeatures(maxSubs, oldMaxSub, newMaxSub);
		IGraphFetcher fetcherOne = this.postingFetcher.getJoin(oldMaxSub,
				TimeComponent);
		IGraphFetcher fetcherTwo = this.insertedPosting.getJoin(newMaxSub,
				TimeComponent);
		IGraphFetcher candidateFetcher = null;
		if (oldMaxSub.size() > 0) {
			if (newMaxSub.size() > 0)
				candidateFetcher = fetcherOne.join(fetcherTwo);
			else
				candidateFetcher = fetcherOne;
		} else {
			System.out.println("This may not happen");
			candidateFetcher = fetcherTwo; // this may not happen
		}
		return candidateFetcher;
	}

	/**
	 * Given the query, find the answer Difference: returned answer is not the
	 * "DBGraphID" but the instead the "GraphDocumentID"
	 * 
	 * @param query
	 * @param maxSubs
	 * @return
	 */
	public int[] getAnswer(Graph query, List<Integer> maxSubs) {
		long[] TimeComponent = new long[4];
		// 0. Hit and Return
		if (maxSubs.get(0) == -1) {
			int TermID = maxSubs.get(1);
			int termStatus = indexSearcher.featureStatus(TermID);
			if (termStatus == 0) {
				IGraphFetcher answerFetcher = this.postingFetcher.getPosting(
						TermID, TimeComponent);
				return answerFetcher.getOrderedIDs();
			} else if (termStatus == 1) {
				IGraphFetcher answerFetcher = this.insertedPosting.getPosting(
						TermID, TimeComponent);
				return answerFetcher.getOrderedIDs();
			}
		}
		// 1. Get the candidate
		IGraphFetcher candidateFetcher = this.getCandidate(maxSubs);
		// 2. Verification
		List<IGraphResult> answer = this.verifier.verify(query,
				candidateFetcher, true, TimeComponent);
		int[] result = new int[answer.size()];
		int index = 0;
		for (IGraphResult oneR : answer)
			result[index++] = oneR.getDocID();
		return result;

	}

	public static String getLuceneName() {
		return "lucene/";
	}

	public static String getIndexName() {
		return "index";
	}

	public int getFeatureNum() {
		return this.indexSearcher.getFeatureNum();
	}

	public static String getInMemPostingName() {
		return "in_memPosting";
	}

}
