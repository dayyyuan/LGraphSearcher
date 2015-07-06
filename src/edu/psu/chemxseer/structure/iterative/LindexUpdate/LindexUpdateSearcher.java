package edu.psu.chemxseer.structure.iterative.LindexUpdate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.CanonicalDFS;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.iso.FastSUStateLabelling;
import edu.psu.chemxseer.structure.iterative.CandidateInfo;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTermComparator;

public class LindexUpdateSearcher extends LindexSearcher {
	protected List<LindexTerm> insertedTerms; // the newly inserted terms
	protected Set<LindexTerm> deletedTerms; // some set has been deleted

	public LindexUpdateSearcher(LindexSearcher searcher,
			List<LindexTerm> insertedTerms, Set<LindexTerm> deletedTerms) {
		super(searcher);
		this.insertedTerms = insertedTerms;
		this.deletedTerms = deletedTerms;
	}

	public LindexUpdateSearcher(LindexSearcher searcher) {
		super(searcher);
		this.insertedTerms = new ArrayList<LindexTerm>();
		this.deletedTerms = new HashSet<LindexTerm>();
	}

	public void deleteOneFeature(LindexTerm term) {
		this.deletedTerms.add(term);
	}

	/**
	 * (1) Construct a Lindex Term (2) Insert the new Term in sertedTerms list
	 * (3) Find the new Terms maximal subgraphs (real), and then add the new
	 * Term as the child of the maximal subgraphs (4) Find the new Terms minimal
	 * supergraphs (real), and then add the new Term and the maximal supergraphs
	 * parent Pay attention, the new Term only have other new terms as their
	 * child (t_parent child), no other already indexed terms. [Because I don't
	 * want to relabel them]
	 * 
	 * @param newFeature
	 * @return
	 */
	public LindexTerm insertNewFeature(CandidateInfo newFeature) {
		// (1,2). Inserted the feature into the lattice
		// Construct a new LindexTerm
		int[] support = newFeature.getSupport();
		int featureID = this.indexTerms.length + insertedTerms.size();
		LindexTerm newTerm = new LindexTerm(featureID, support.length);
		this.insertedTerms.add(newTerm);

		// (3.1) add newFeature as the child of the old indexing features
		List<Integer> maxSubs = newFeature.getMaxSubs();
		LindexTerm[] maxTerms = this.getTerms(maxSubs);

		// (3.2) set t_parents of the new Feature
		this.chooseTParent(newTerm, maxTerms);
		// Extension labeling
		FastSU fastSu = new FastSU();
		FastSUStateLabelling seedState = fastSu.graphExtensionLabeling(
				this.getTermFullLabel(newTerm.getParent()),
				newFeature.getCandidateGraph());
		newTerm.setExtension(seedState.getExtension());
		// (4) set children of the new Features
		LindexTerm[] minTerms = this.getTerms(newFeature.getMinSups());
		newTerm.setChildren(minTerms);
		return newTerm;
	}

	/**
	 * 
	 * @param maxSubs
	 * @return
	 */
	/*
	 * private LindexTerm[] getTerms(int[] maxSubs){ LindexTerm[] maxTerms = new
	 * LindexTerm[maxSubs.length]; for(int i = 0; i< maxSubs.length; i++){
	 * if(maxSubs[i] >= this.indexTerms.length) maxTerms[i] =
	 * this.insertedTerms.get(maxSubs[i]-indexTerms.length); else maxTerms[i] =
	 * this.indexTerms[maxSubs[i]]; } return maxTerms; }
	 */
	/**
	 * Previous In Lindex Constructor
	 * 
	 * @param theTerm
	 * @param maxSubs
	 */
	private void chooseTParent(LindexTerm theTerm, LindexTerm[] maxTerms) {
		// Parent Assignment rule:
		// 1. choose the one with minimum frequency
		// 2. If a tie happens, assign the parent whose has minimum number of
		// valid children currently
		// 3. If a tie happens, choose the one with minimum edge // approximate
		// minimum depth
		LindexTerm tParent = null;
		int minimumFrequency = Integer.MAX_VALUE;
		int minimumChildrenNum = Integer.MAX_VALUE;

		for (int t = 0; t < maxTerms.length; t++) {
			// A. First choose the parent with the minimum frequency
			LindexTerm parentFeature = maxTerms[t];
			int tFrequency = parentFeature.getFrequency();
			int validChildNum = 0;
			if (tFrequency <= minimumFrequency) {
				// calculate number of valid children
				LindexTerm[] children = parentFeature.getChildren();
				for (int w = 0; w < children.length; w++) {
					if (children[w].getParent() == null)
						continue;
					else if (children[w].getParent() == parentFeature)
						validChildNum++;
				}
			}
			if (tFrequency < minimumFrequency) {
				tParent = parentFeature;
				minimumFrequency = tFrequency;
				minimumChildrenNum = validChildNum;
			} else if (tFrequency == minimumFrequency) {
				// B. Second choose the parent who has minimum number of valid
				// children
				if (validChildNum < minimumChildrenNum) {
					tParent = parentFeature;
					minimumChildrenNum = validChildNum;
				} else if (validChildNum == minimumChildrenNum) {
					// c. Third choose the one with minimum edge // approximate
					// minimum depth
					if (parentFeature.getId() < tParent.getId()) {
						tParent = parentFeature;
					} else
						continue; // if it is a tie, randomly choose the
									// previous one, otherwise continue
				} else
					continue;
			} else
				continue;
		}
		theTerm.setParent(tParent);
	}

	/**
	 * Given the query, return the maximum subgraphs (1) For the "deleted"
	 * features, if they are selected as maximum super-graphs, their parents are
	 * the real-maximal supergraphs (2) For the newly inserted features, since
	 * no already indexed feature will use it as T-parent, so the search may
	 * stop
	 */
	@Override
	public List<Integer> maxSubgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		List<Integer> maxSubTermIds = new LinkedList<Integer>();
		LindexTerm[] seeds = this.dummyHead.getChildren();
		CanonicalDFS dfsParser = MyFactory.getDFSCoder();
		boolean[] preciseLocate = new boolean[1];
		preciseLocate[0] = false;
		for (int i = 0; i < seeds.length; i++) {
			Graph seedGraph = dfsParser.parse(seeds[i].getExtension(),
					MyFactory.getGraphFactory());
			if (gComparator.compare(seedGraph, query) > 0)
				continue;
			FastSUCompleteEmbedding fastSuExt = new FastSUCompleteEmbedding(
					seedGraph, query);
			this.maximumSubgraphSearch(fastSuExt, maxSubTermIds, seeds[i],
					preciseLocate);
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		// Return Process
		if (maxSubTermIds.size() == 0)
			return null;
		if (preciseLocate[0]) {
			List<Integer> results = new ArrayList<Integer>(2);
			results.add(-1);
			results.add(maxSubTermIds.get(0));
			return results;
		} else {
			return maxSubTermIds;
		}
	}

	/**
	 * The new version is written by @author Dayu Yuan on Nov. 2 Return true, if
	 * oriTerm itself or a child(non-deleted) selected as the maximum subgraphs
	 * Return false, if neither oriTerm nor none of oriTerm (non-deleted) is
	 * selected as the maximum subgraphs of the query
	 * 
	 * @param fastSu
	 * @param maxSubTerms
	 * @param oriTerm
	 * @param preciseLocate
	 * @return
	 */
	private boolean maximumSubgraphSearch(FastSUCompleteEmbedding fastSu,
			Collection<Integer> maxSubTerms, LindexTerm oriTerm,
			boolean[] preciseLocate) {
		if (fastSu.isIsomorphic()) {
			if (this.deletedTerms.contains(oriTerm)) {
				return false;
			} else {
				maxSubTerms.clear();
				maxSubTerms.add(oriTerm.getId());
				preciseLocate[0] = true;
				return true; // find the precise maxSubTerms and return
			}
		} else if (fastSu.issubIsomorphic()) {
			LindexTerm[] children = oriTerm.getChildren();

			if (children == null || children.length == 0) {
				if (this.deletedTerms.contains(oriTerm))
					return false; // neither oriTerm nore its children is
									// selected
				else {
					maxSubTerms.add(oriTerm.getId()); // select oriTerm as one
														// of the maximum
														// subgraphs
					return true;
				}
			}

			boolean extendable = false; // true of any of oriTerm's children or
										// descendent get selected
			for (int i = 0; i < children.length; i++) {
				if (children[i].getParent() != oriTerm)
					continue; // skip unrelated children
				FastSUCompleteEmbedding next = new FastSUCompleteEmbedding(
						fastSu, children[i].getExtension());
				boolean success = this.maximumSubgraphSearch(next, maxSubTerms,
						children[i], preciseLocate);
				if (success)
					extendable = true;
			}

			if (extendable == false) {
				if (this.deletedTerms.contains(oriTerm))
					return false; // neither oriTerm nore its children is
									// selected
				else {
					maxSubTerms.add(oriTerm.getId());
					return true;
				}
			} else
				return true; // extendable = true;
		} else
			return false;

	}

	/**
	 * Prune all the not-real max subgraphs
	 * 
	 * @param maxSubs
	 * @return
	 */
	@Override
	public List<Integer> getRealMaxSubgraphs(List<Integer> maxSubs) {
		LindexTerm[] terms = this.getTerms(maxSubs);
		this.getRealMaxSubgraph(terms);
		List<Integer> result = new ArrayList<Integer>();
		for (LindexTerm oneTerm : terms) {
			if (oneTerm == null)
				continue;
			else
				result.add(oneTerm.getId());
		}
		return result;
	}

	@Override
	protected void getRealMaxSubgraph(LindexTerm[] maxSubTerms) {
		// 1st: sort maximum subgraphs according to their node number
		Comparator<LindexTerm> compare = new LindexTermComparator();
		// QuickSort.quicksort(maxSubGraphs, compare);
		Arrays.sort(maxSubTerms, compare);

		Set<LindexTerm> subgraphsHash = new HashSet<LindexTerm>(
				maxSubTerms.length);
		for (int i = 0; i < maxSubTerms.length; i++)
			subgraphsHash.add(maxSubTerms[i]);

		// 2nd: Finding all candidates super graphs as the intersection of
		// offspring of each maximum subgraph
		for (int i = 0; i < maxSubTerms.length; i++) {
			if (maxSubTerms[i] == null)
				continue;
			else {
				// Start finding the set of maxSubGraphs[i]'s whole set of
				// children
				// In this process if we find a offspring of this term equals to
				// one other maxSubgraph, this subgraph is set to be null
				boolean[] notMaximum = new boolean[1];
				notMaximum[0] = false; // Assume this is the maximum
				getSuperTermsIndex(maxSubTerms[i], subgraphsHash, notMaximum);
				if (notMaximum[0]) {
					maxSubTerms[i] = null;
					continue; // there is no need of further intersection, since
								// maxSubgraph[i] is not maximum subgraph
				}
			}
		}
	}

	/**
	 * return -1 if the feature has been deleted or not in the index return 0 if
	 * the feature is normal feature in the index return 1 if the feature is
	 * newly inserted (with inserted postings)
	 * 
	 * @param featureID
	 * @return
	 */
	public int featureStatus(int featureID) {
		if (featureID >= this.indexTerms.length)
			return 1;
		else {
			LindexTerm theTerm = this.indexTerms[featureID];
			if (this.deletedTerms.contains(theTerm))
				return -1;
			else
				return 0;
		}
	}

	public int getFeatureNum() {
		return this.indexTerms.length + this.insertedTerms.size();
	}

	/**
	 * 
	 * @param allFeatureID
	 * @return oldFeatureIDs
	 * @return newFeatureIDs
	 */
	public void seperageOldNewFeatures(List<Integer> allFeatureID,
			List<Integer> oldFeatureIDs, List<Integer> newFeatureIDs) {
		for (Integer oneID : allFeatureID) {
			int status = this.featureStatus(oneID);
			if (status == 0)
				oldFeatureIDs.add(oneID);
			else if (status == 1)
				newFeatureIDs.add(oneID);
		}
	}

	public int[][] getTermFullLabel(int terID) {
		if (terID < this.indexTerms.length)
			return this.getTermFullLabel(this.indexTerms[terID]);
		else
			return this.getTermFullLabel(this.insertedTerms.get(terID
					- indexTerms.length));
	}

	@Override
	public int getFeatureCount() {
		return this.indexTerms.length + this.insertedTerms.size();
	}

}
