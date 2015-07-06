package edu.psu.chemxseer.structure.setcover.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUStateLabelling;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherDBPrefix;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMemPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResultPref;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * The LindexSimple that support the insertion & deletion of features. This is
 * the new implementation for the set-cover feature update. Different from the
 * old implementation.
 * 
 * @author dayuyuan
 * 
 */
public class LindexSearcherUpdatable {
	// For Each Index Term, their parents terms are maintained
	// for the fast update (especially deletion) of the LindexSearcher
	private Set<Integer>[] parents;
	private LindexSearcher searcher;
	private PostingFetcherMemPrefix postingFetcher; // this is against rule of
													// isolation

	// TEST Purpose
	// private String[] testString;

	public LindexSearcherUpdatable(LindexSearcher searcher) {
		this.searcher = searcher;
		/*
		 * this.testString = new String[indexTerms.length]; for(int i=0; i<
		 * indexTerms.length; i++) testString[i] =
		 * MyFactory.getDFSCoder().serialize
		 * (MyFactory.getDFSCoder().parse(this.getTermFullLabel(indexTerms[i]),
		 * MyFactory.getGraphFactory()));
		 */
		initialize();
	}

	public LindexSearcherUpdatable(LindexSearcher searcher,
			PostingFetcherMemPrefix dbFetcher) {
		this.searcher = searcher;
		this.postingFetcher = dbFetcher;
		initialize();
	}

	/**
	 * Depth first search of the lattice to construct the parents
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		this.parents = new Set[searcher.getFeatureCount()];
		for (int i = 0; i < parents.length; i++)
			parents[i] = new HashSet<Integer>();
		// 1. Dummy Head's children
		for (LindexTerm child : searcher.dummyHead.getChildren())
			parents[child.getId()].add(-1);

		// 2. Normal Term's children
		for (int termID = 0; termID < parents.length; termID++) {
			LindexTerm[] children = searcher.indexTerms[termID].getChildren();
			if (children == null)
				continue;
			for (LindexTerm child : children)
				parents[child.getId()].add(termID);
		}
	}

	/**
	 * Given the newGraph, construct a new index term with tID & freq Swap the
	 * newly constructed index term with old term at position (tID) Assumption:
	 * tID denotes the position of the lindex term on the indexTerms array
	 * 
	 * @param newGraph
	 * @param tID
	 * @param freq
	 */
	public void swapIndexTerm(Graph newGraph, int tID, int freq) {
		this.deletIndexTerm(searcher.indexTerms[tID]);
		this.insertIndexTerm(newGraph, tID, freq);
	}

	/**
	 * Delete an index term T Connect T's parent to T's children if they are
	 * only connected by T Also update T's children's labels if they are
	 * previously labeled with T as the parent
	 * 
	 * @param oldIndexTerm
	 */
	private void deletIndexTerm(LindexTerm oldIndexTerm) {
		Set<Integer> parents = this.parents[oldIndexTerm.getId()];
		LindexTerm[] children = oldIndexTerm.getChildren();
		if (parents.size() == 1 && parents.contains(-1)) {
			System.out
					.println("Can not remove a first-level feature: to maintain the complitness of the index");
			throw new InputMismatchException();
		} else {
			// The parent is not dummy head
			for (LindexTerm child : children)
				this.parents[child.getId()].remove(oldIndexTerm.getId());
			for (Integer pID : parents) {
				// 1. Remove oldIndexTerm from the children of parent
				searcher.indexTerms[pID].removeChild(oldIndexTerm);
				Collection<LindexTerm> noConnectTerms = getNoConnect(
						searcher.indexTerms[pID], children);
				for (LindexTerm noConnectTerm : noConnectTerms) {
					// 2. Add noConnectTerm as new child of the parent term
					searcher.indexTerms[pID].addChild(noConnectTerm);
					this.parents[noConnectTerm.getId()].add(pID);
				}
			}
			// update the t_parent & label
			for (LindexTerm child : children) {
				// 3. Find the new parent and relabel the index.
				if (child.getParent() == oldIndexTerm) {
					LindexTerm newParent = this.chooseTParent(child);
					updateParent(child, newParent);
				}
			}
		}
		searcher.indexTerms[oldIndexTerm.getId()] = null;
	}

	/**
	 * Given a parent term and a set of candidate terms (no partial order exists
	 * in between) Return all the terms that are not reachable from the parent
	 * 
	 * @param parent
	 * @param candidates
	 *            : assumption, there is not parent-child relationships
	 * @return
	 */
	protected Collection<LindexTerm> getNoConnect(LindexTerm parent,
			LindexTerm[] candidates) {
		// Breadth first search
		Queue<LindexTerm> queue = new LinkedList<LindexTerm>();
		HashSet<LindexTerm> queueSet = new HashSet<LindexTerm>(); // to avoid
																	// duplicate
																	// visit
		HashSet<LindexTerm> candidateSet = new HashSet<LindexTerm>();
		for (LindexTerm oneCandidate : candidates)
			candidateSet.add(oneCandidate);

		queue.offer(parent);
		while (!queue.isEmpty()) {
			LindexTerm aterm = queue.poll();
			LindexTerm[] children = aterm.getChildren();
			if (children == null || children.length == 0)
				continue;
			for (LindexTerm child : children) {
				if (queueSet.contains(child))
					continue; // stop the search
				else if (candidateSet.contains(child)) {
					queueSet.add(child);
					candidateSet.remove(child);
					if (candidateSet.isEmpty())
						break;
				} else {
					queue.offer(child);
					queueSet.add(child);
				}
			}
		}
		return candidateSet;
	}

	/**
	 * Augment based on LindexConstructor: chooseTParent
	 * 
	 * @param theTerm
	 * @return
	 */
	private LindexTerm chooseTParent(LindexTerm theTerm) {
		// Parent Assignment rule:
		// 1. choose the one with minimum frequency
		// 2. If a tie happens, assign the parent whose has minimum number of
		// valid children currently
		// 3. If a tie happens, choose the one with minimum edge // approximate
		// minimum depth
		LindexTerm minParent = null;
		int minimumFrequency = Integer.MAX_VALUE;
		int minimumChildrenNum = Integer.MAX_VALUE;
		for (Integer pID : this.parents[theTerm.getId()]) {
			// A. First choose the parent with the minimum frequency
			LindexTerm parent = searcher.indexTerms[pID];
			int tFrequency = parent.getFrequency();
			int validChildNum = 0;
			if (tFrequency <= minimumFrequency) {
				// calculate number of valid children
				LindexTerm[] siblings = parent.getChildren();
				for (LindexTerm cousin : siblings) {
					if (cousin.getParent() == parent)
						validChildNum++;
				}
			}
			if (tFrequency < minimumFrequency) {
				minParent = parent;
				minimumFrequency = tFrequency;
				minimumChildrenNum = validChildNum;
			} else if (tFrequency == minimumFrequency) {
				// B. Second choose the parent who has minimum number of valid
				// children
				if (validChildNum < minimumChildrenNum) {
					minParent = parent;
					minimumChildrenNum = validChildNum;
				} else if (validChildNum == minimumChildrenNum) {
					// c. Third choose the one with minimum edge // approximate
					// minimum depth
					int originalParentEdge = minParent.getMaxNodeIndex();
					int testParentEdge = searcher.indexTerms[pID]
							.getMaxNodeIndex();
					if (testParentEdge < originalParentEdge)
						minParent = parent;
					else
						continue; // if it is a tie, randomly choose the
									// previous one, otherwise continue
				} else
					continue;
			} else
				continue;
		}
		return minParent;
	}

	/**
	 * Given theTerm, and its old prefix update theTerm with newParent &
	 * newExtension based on newParent as well. The offsprings of theTerm need
	 * to update their extension too.
	 * 
	 * @param theTerm
	 * @param newParent
	 */
	private void updateParent(LindexTerm theTerm, LindexTerm newParent) {
		// 1. Update the Parent & Extension of theTerm
		int[][] oldLabel = searcher.getTermFullLabel(theTerm);
		Graph g = MyFactory.getDFSCoder().parse(oldLabel,
				MyFactory.getGraphFactory());
		Graph newPrefix = MyFactory.getDFSCoder().parse(
				searcher.getTermFullLabel(newParent),
				MyFactory.getGraphFactory());
		FastSU fastSu = new FastSU();
		FastSUStateLabelling state = fastSu
				.graphExtensionLabeling(newPrefix, g);
		int[] order = state.getOrder();
		if (this.postingFetcher != null) {
			GraphFetcherDBPrefix dbFetcher = postingFetcher.getPosting(
					theTerm.getId(), new long[4]);
			List<IGraphResultPref> relabelGraphs = dbFetcher
					.getGraphsWithPrefixID(theTerm.getId());
			for (IGraphResultPref oneGraph : relabelGraphs) {
				dbFetcher.gDB.setSuffix(FastSUStateLabelling.rebase(
						oneGraph.getSuffix(), order), oneGraph.getID());
			}
		}
		theTerm.setExtension(state.getExtension());
		theTerm.setParent(newParent);
		/*
		 * int[][] temps = this.getTermFullLabel(theTerm); String str2 =
		 * MyFactory
		 * .getDFSCoder().serialize(MyFactory.getDFSCoder().parse(temps,
		 * MyFactory.getGraphFactory()));
		 * if(!testString[theTerm.getId()].equals( str2)){ String str1 =
		 * testString[theTerm.getId()]; System.out.println("shit"); }
		 */
		// 2. Update the Extension of the offsprings of theTerm
		// order[oldNodeID] = newNodeID;

		// Hence it is good to update the nodeID of the extensions.
		List<LindexTerm> offsprings = new ArrayList<LindexTerm>();
		searcher.getPrefixLabelSuperTerms(theTerm, offsprings);
		for (LindexTerm child : offsprings) {
			// if prefix-sharing database graph, then rebase the database graph
			// as well
			if (this.postingFetcher != null) {
				GraphFetcherDBPrefix dbFetcher = postingFetcher.getPosting(
						child.getId(), new long[4]);
				List<IGraphResultPref> relabelGraphs = dbFetcher
						.getGraphsWithPrefixID(child.getId());
				for (IGraphResultPref oneGraph : relabelGraphs) {
					dbFetcher.gDB.setSuffix(FastSUStateLabelling.rebase(
							oneGraph.getSuffix(), order), oneGraph.getID());
				}
			}
			child.setExtension(FastSUStateLabelling.rebase(
					child.getExtension(), order));
		}

		/*
		 * for(LindexTerm child:offsprings){
		 * if(!testString[theTerm.getId()].equals(
		 * MyFactory.getDFSCoder().serialize
		 * (MyFactory.getDFSCoder().parse(this.getTermFullLabel(theTerm),
		 * MyFactory.getGraphFactory())))) System.out.println("shit"); }
		 */
	}

	/**
	 * Construct an new index term & insert into the index
	 * 
	 * @param newGraph
	 * @param freq
	 * @return
	 */
	public LindexTerm insertIndexTerm(Graph newGraph, int freq) {
		// the tID will be maintained incrementally.
		int id = searcher.indexTerms.length;
		searcher.indexTerms = Arrays.copyOf(searcher.indexTerms,
				searcher.indexTerms.length + 1);
		this.parents = Arrays.copyOf(parents, parents.length + 1);
		parents[parents.length - 1] = new HashSet<Integer>();
		return this.insertIndexTerm(newGraph, id, freq);
	}

	/**
	 * Insert a new index term in to the lattice To ease the operations, we do
	 * not change other terms t_paretns unless necessary, in which case the
	 * other term's t_paretns changes from newIndexTerm's parent to newIndexTerm
	 * 
	 * @param newGraph
	 * @param ID
	 * @param freq
	 */
	public LindexTerm insertIndexTerm(Graph newGraph, int tID, int freq) {
		LindexTerm newIndexTerm = new LindexTerm(tID, freq);
		List<Integer> maxSubsRaw = searcher.maxSubgraphs(newGraph, new long[4]);
		if (maxSubsRaw.size() > 0 && maxSubsRaw.get(0) == -1) {
			searcher.maxSubgraphs(newGraph, new long[4]);
			System.out
					.println("Exception in LindexSearcherUpdatable: insertIndexTerm, try to insert duplicate features");
		}
		List<Integer> minSups = searcher.minimalSupergraphs(newGraph,
				new long[4], maxSubsRaw);
		List<Integer> maxSubs = new ArrayList<Integer>();
		for (Integer it : maxSubsRaw) {
			if (it >= 0)
				maxSubs.add(it);
		}

		// 1. Set parents & children, tParetns & extension
		this.parents[newIndexTerm.getId()].clear();
		this.parents[newIndexTerm.getId()].addAll(maxSubs);

		LindexTerm[] children = new LindexTerm[minSups.size()];
		for (int i = 0; i < children.length; i++)
			children[i] = searcher.indexTerms[minSups.get(i)];
		newIndexTerm.setChildren(children);

		newIndexTerm.setParent(this.chooseTParent(newIndexTerm));
		FastSU fastSu = new FastSU();
		FastSUStateLabelling state = fastSu.graphExtensionLabeling(
				searcher.getTermFullLabel(newIndexTerm.getParent()), newGraph);
		newIndexTerm.setExtension(state.getExtension());
		// testString[newIndexTerm.getId()]=
		// MyFactory.getDFSCoder().serialize(MyFactory.getDFSCoder().parse(this.getTermFullLabel(newIndexTerm),
		// MyFactory.getGraphFactory()));
		// 2. In the old lattice, there won't be any direct connection between
		// non-max subs to non-min sups
		// But there are cases when a maxSub connect with min sups, we need to
		// break these connections
		// in the new lattice
		for (Integer sup : minSups)
			parents[sup].add(tID);
		for (Integer sub : maxSubs) {
			LindexTerm subTerm = searcher.indexTerms[sub];
			subTerm.addChild(newIndexTerm);
			for (Integer sup : minSups) {
				LindexTerm supTerm = searcher.indexTerms[sup];
				if (parents[sup].contains(sub)) {
					subTerm.removeChild(supTerm);
					parents[sup].remove(sub);
					if (supTerm.getParent() == subTerm) {
						this.updateParent(supTerm, newIndexTerm);
					}
				}
			}
		}
		searcher.indexTerms[newIndexTerm.getId()] = newIndexTerm;
		return newIndexTerm;
	}

	public int[] getParents(int tID) {
		if (tID < 0 || tID >= parents.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return OrderedIntSets.toArray(parents[tID]);
	}
}
