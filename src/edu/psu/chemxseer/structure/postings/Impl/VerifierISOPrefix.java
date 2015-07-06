package edu.psu.chemxseer.structure.postings.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcherPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResultPref;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;

/**
 * The Implementation of the Verifier with Prefix Embedding available Important:
 * the input candidateFetcher must be prefixFetcher, which can return
 * graphResultPrefix
 * 
 * @author dayuyuan
 * 
 */
public class VerifierISOPrefix extends VerifierISO {
	private IIndexPrefix prefSearcher;
	// StoreEmbedding = true, then all the embedding from the "answer" & query
	// is recorded
	private boolean storeEmbedding;
	private Map<IGraphResult, FastSUCompleteEmbedding> storedEmbeddings;

	/**
	 * Constructor of the VeirifierISO Prefix
	 * 
	 * @param prefSearcher
	 * @param storeEmbedding
	 */
	public VerifierISOPrefix(IIndexPrefix prefSearcher, boolean storeEmbedding) {
		super();
		this.prefSearcher = prefSearcher;
		this.storeEmbedding = storeEmbedding;
		if (storeEmbedding)
			this.storedEmbeddings = new HashMap<IGraphResult, FastSUCompleteEmbedding>();
	}

	/**
	 * For Supergraph Search Use Only The Mapping from the "indexing features"
	 * to the query graph will be fetched This mapping will be extended to
	 * mappings from the "candidate graph" to the query
	 * 
	 * @param candidateFetcher
	 * @param TimeComponent
	 * @return
	 */
	public List<IGraphResult> verify(IGraphFetcherPrefix candidateFetcher,
			Graph query, long[] TimeComponent) {

		if (candidateFetcher == null || candidateFetcher.size() == 0)
			return new ArrayList<IGraphResult>();
		else {
			if (this.storeEmbedding)
				this.storedEmbeddings.clear();
			List<IGraphResult> answerSet = new ArrayList<IGraphResult>();

			List<IGraphResultPref> candidates = candidateFetcher
					.getGraphs(TimeComponent);
			long start = System.currentTimeMillis();
			while (candidates != null) {
				for (int i = 0; i < candidates.size(); i++) {
					IGraphResultPref oneCandidate = candidates
							.get(i);
					if (oneCandidate.getPrefixFeatureID() != -1) {
						int[][] suffix = oneCandidate.getSuffix();
						if (suffix != null) {
							FastSUCompleteEmbedding prefixEmbedding = this.prefSearcher
									.getEmbedding(
											oneCandidate.getPrefixFeatureID(),
											query);
							if (prefixEmbedding == null)
								continue; // this is not an answer
							if (this.storeEmbedding) {
								FastSUCompleteEmbedding newEmbedding = new FastSUCompleteEmbedding(
										prefixEmbedding, suffix);
								if (newEmbedding.issubIsomorphic()) {
									answerSet.add(oneCandidate);
									this.storedEmbeddings.put(oneCandidate,
											newEmbedding);
								}
							} else {
								FastSU su = new FastSU();
								boolean iso = su.isIsomorphic(prefixEmbedding,
										suffix);
								if (iso)
									answerSet.add(oneCandidate);
							}
						} else // the database graph is the same as the prefix
								// feature
						{
							answerSet.add(candidates.get(i));
							if (this.storeEmbedding)
								this.storedEmbeddings.put(oneCandidate,
										this.prefSearcher.getEmbedding(
												oneCandidate
														.getPrefixFeatureID(),
												query));
						}
					} else {
						// PrefixFeatureID = -1;
						if (this.storeEmbedding) {
							Graph g = oneCandidate.getG();
							if (g == null)
								System.out
										.println("Exception in VerifierISOPrefix: null g with not prefix");
							FastSUCompleteEmbedding newEmbedding = new FastSUCompleteEmbedding(
									g, query);
							if (newEmbedding.issubIsomorphic()) {
								answerSet.add(oneCandidate);
								this.storedEmbeddings.put(oneCandidate,
										newEmbedding);
							}
						} else {
							FastSU su = new FastSU();
							boolean iso = su.isIsomorphic(oneCandidate.getG(),
									query);
							if (iso)
								answerSet.add(candidates.get(i));
						}
					}

				}
				TimeComponent[3] += System.currentTimeMillis() - start;
				candidates = candidateFetcher.getGraphs(TimeComponent);
				start = System.currentTimeMillis();
			}
			return answerSet;
		}
	}

	/**
	 * For Supergraph Search Only The Mapping from the "indexing features" to
	 * the query graph will be fetched This mapping will be extended to mappings
	 * from the "candidate graph" to the query
	 * 
	 * @param candidateFetcher
	 * @param TimeComponent
	 * @return
	 */
	public List<IGraphResult> verifyFalse(IGraphFetcher candidateFetcher,
			Graph query, long[] TimeComponent) {
		if (candidateFetcher == null || candidateFetcher.size() == 0)
			return new ArrayList<IGraphResult>();

		else {
			if (this.storeEmbedding)
				this.storedEmbeddings.clear();

			List<IGraphResult> answerSet = new ArrayList<IGraphResult>();
			List<IGraphResult> candidates = candidateFetcher
					.getGraphs(TimeComponent);
			long start = System.currentTimeMillis();
			while (candidates != null) {
				for (int i = 0; i < candidates.size(); i++) {
					IGraphResultPref oneCandidate = (IGraphResultPref) candidates
							.get(i);
					FastSUCompleteEmbedding prefixEmbedding = this.prefSearcher
							.getEmbedding(oneCandidate.getPrefixFeatureID(),
									query);
					Graph g = null;

					if (g == null)
						if (storeEmbedding)
							this.storedEmbeddings.put(candidates.get(i),
									prefixEmbedding);
						else {
							FastSUCompleteEmbedding newEmbedding = new FastSUCompleteEmbedding(
									prefixEmbedding, g);
							if (!newEmbedding.issubIsomorphic())
								answerSet.add(candidates.get(i));
							else if (this.storeEmbedding)
								this.storedEmbeddings.put(candidates.get(i),
										newEmbedding);
						}
				}
				TimeComponent[3] += System.currentTimeMillis() - start;
				candidates = candidateFetcher.getGraphs(TimeComponent);
				start = System.currentTimeMillis();
			}
			return answerSet;
		}
	}

	/**
	 * If the Verifier stored the embeddings, return the embeddings else return
	 * null;
	 * 
	 * @return
	 */
	public Map<IGraphResult, FastSUCompleteEmbedding> getEmbeddings() {
		if (this.storeEmbedding)
			return this.storedEmbeddings;
		else
			return null;
	}

	/**
	 * Exactly the same as verifierISO
	 * 
	 * @param newGraph
	 * @param candidateFetcher
	 * @param order
	 * @param TimeComponent
	 * @return
	 */
	public List<IGraphResult> verify(Graph query,
			IGraphFetcherPrefix candidateFetcher, boolean order,
			long[] TimeComponent) {
		if (candidateFetcher == null || candidateFetcher.size() == 0)
			return new ArrayList<IGraphResult>();
		else {
			List<IGraphResult> answerSet = new ArrayList<IGraphResult>();
			List<IGraphResultPref> candidates = candidateFetcher
					.getGraphs(TimeComponent);
			long start = System.currentTimeMillis();
			while (candidates != null) {
				for (int i = 0; i < candidates.size(); i++) {
					Graph g = candidates.get(i).getG();
					if (g == null)
						continue;
					if (order && fastSu.isIsomorphic(query, g))
						answerSet.add(candidates.get(i));

					else if (!order && fastSu.isIsomorphic(g, query))
						answerSet.add(candidates.get(i));
				}
				TimeComponent[3] += System.currentTimeMillis() - start;
				candidates = candidateFetcher.getGraphs(TimeComponent);
				start = System.currentTimeMillis();
			}
			return answerSet;
		}
	}

}
