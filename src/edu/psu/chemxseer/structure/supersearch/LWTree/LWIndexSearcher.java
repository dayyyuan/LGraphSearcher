package edu.psu.chemxseer.structure.supersearch.LWTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.CanonicalDFS;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.iso.FastSUStateExpandable;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;

/**
 * The index structure for the LWIndex: (1) The LWindex adopt the LindexSearch
 * structure (2) I can also return all the embeddings of between the maxSubgrpah
 * features and the query
 * 
 * @author dayuyuan
 * 
 */
public class LWIndexSearcher extends LindexSearcher implements IIndexPrefix {
	// Given a query, the featureEmbeddings stores all the subgraphs of the
	// query and their corresponding
	// fastSUEmbeddings
	private HashMap<Integer, FastSUCompleteEmbedding> featureEmbeddings;
	private boolean allexamed;
	private Graph internalQuery;

	// Each feature is associated with two set of training queries
	// (1). queries containing the feature,
	// (2). queries not containing the feature
	// For type 1 query, assigning the feature as a prefix of a graph will save
	// ISO cost by prefix sharing
	// For type 2 query, assigning the feature as a prefix of a graph will
	// filter out this "g"
	// Only Useful during the DTTreeSearcher Construction
	// [posting construction, will be called by
	// PostingBuilderLuceneVectorizerSingle]
	protected int[] fSize;
	private int[] prefixQueryCount; // |Queries containing the features|
	private int[] filteredQueryCount; // |Queries no containing features|

	/**
	 * For Index Loading Only
	 * 
	 * @param lindexSearcher
	 */
	public LWIndexSearcher(LindexSearcher lindexSearcher) {
		super(lindexSearcher);
		this.featureEmbeddings = new HashMap<Integer, FastSUCompleteEmbedding>();
		this.internalQuery = null;
		this.allexamed = false;
	}

	/**
	 * For LWIndexSearcher Construction Only: Instead of building the
	 * LindexSearcher, we also need to initialize type1 and type2 scored
	 * 
	 * @param lindexSearcher
	 * @param features
	 * @param querySize
	 */
	public LWIndexSearcher(LindexSearcher lindexSearcher,
			NoPostingFeatures_Ext<OneFeatureMultiClass> features, int querySize) {
		super(lindexSearcher);
		this.featureEmbeddings = new HashMap<Integer, FastSUCompleteEmbedding>();
		this.prefixQueryCount = new int[features.getfeatureNum()];
		this.filteredQueryCount = new int[features.getfeatureNum()];
		this.fSize = new int[features.getfeatureNum()];
		for (int i = 0; i < features.getfeatureNum(); i++) {
			OneFeatureMultiClass oneFeature = features.getFeature(i);
			int queryFrequency = oneFeature.getAllFrequency()[2];
			prefixQueryCount[i] = queryFrequency;
			filteredQueryCount[i] = querySize - queryFrequency;
			fSize[i] = oneFeature.getFeatureGraph().getEdgeCount();
		}
		this.internalQuery = null;
		this.allexamed = false;
	}

	@Override
	// Filtering Cost for TimeComponent[2]
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		this.internalQuery = query;
		this.allexamed = true;

		this.featureEmbeddings.clear();
		long start = System.currentTimeMillis();
		List<Integer> subGraphIds = new ArrayList<Integer>();
		// Always return "-1" as one of the subgraphs of the feature
		subGraphIds.add(-1);
		// Add other subgraphs
		LindexTerm[] seeds = this.dummyHead.getChildren();
		CanonicalDFS dfsParser = MyFactory.getDFSCoder();
		for (int i = 0; i < seeds.length; i++) {
			Graph seedGraph = dfsParser.parse(seeds[i].getExtension(),
					MyFactory.getGraphFactory());
			if (seedGraph.getNodeCount() > query.getNodeCount()
					|| seedGraph.getEdgeCount() > query.getEdgeCount())
				continue;
			FastSUCompleteEmbedding fastSuExt = new FastSUCompleteEmbedding(
					seedGraph, query);
			if (fastSuExt.isIsomorphic()) {
				subGraphIds.add(seeds[i].getId());
				this.featureEmbeddings.put(seeds[i].getId(), fastSuExt);
				break;
			} else if (fastSuExt.issubIsomorphic()) {// is expendable
				// grows the seed state
				subGraphIds.add(seeds[i].getId());
				subgraphSearch(fastSuExt, subGraphIds, seeds[i]);
			} else
				continue;
		}
		Collections.sort(subGraphIds);
		TimeComponent[2] += System.currentTimeMillis() - start;
		return subGraphIds;
	}

	private boolean subgraphSearch(FastSUCompleteEmbedding fastSu,
			Collection<Integer> maxSubTerms, LindexTerm oriTerm) {
		this.featureEmbeddings.put(oriTerm.getId(), fastSu);
		LindexTerm[] children = oriTerm.getChildren();
		// No further node to grow
		if (children == null || children.length == 0) {
			return false;
		} else {
			for (int i = 0; i < children.length; i++) {
				if (children[i].getParent() != oriTerm)
					continue;
				LindexTerm childTerm = children[i];

				FastSUCompleteEmbedding next = new FastSUCompleteEmbedding(
						fastSu, childTerm.getExtension());
				if (next.isIsomorphic()) {
					maxSubTerms.add(childTerm.getId());
					this.featureEmbeddings.put(childTerm.getId(), next);
					// return true; // find the precise maxSubTerms
				} else if (next.issubIsomorphic()) {
					maxSubTerms.add(childTerm.getId());
					// Further growing to test node children[i]
					subgraphSearch(next, maxSubTerms, children[i]);
				}
			}
		}
		return false;
	}

	@Override
	public List<Integer> maxSubgraphs(Graph query, long[] TimeComponent) {
		this.internalQuery = query;
		this.allexamed = true;
		this.featureEmbeddings.clear();
		long start = System.currentTimeMillis();
		List<Integer> maxSubTermIds = new ArrayList<Integer>();
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
			if (fastSuExt.isIsomorphic()) {
				this.featureEmbeddings.put(seeds[i].getId(), fastSuExt);
				List<Integer> result = new ArrayList<Integer>(2);
				result.add(-1);
				result.add(seeds[i].getId());
				TimeComponent[2] += System.currentTimeMillis() - start;
				return result; // find the precise maxSubTerms
			} else if (fastSuExt.issubIsomorphic()) {// is expendable
				// grows the seed state
				boolean findPrecise = maximumSubgraphSearch(fastSuExt,
						maxSubTermIds, seeds[i]);
				if (findPrecise) {
					preciseLocate[0] = true;
					break;
				}
			} else
				continue;
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		// Return Process
		if (maxSubTermIds.size() == 0)
			return maxSubTermIds; // return empty least
		if (preciseLocate[0]) {
			List<Integer> results = new ArrayList<Integer>(2);
			results.add(-1);
			results.add(maxSubTermIds.get(0));
			return results;
		} else {
			return maxSubTermIds;
		}
	}

	private boolean maximumSubgraphSearch(FastSUCompleteEmbedding fastSu,
			Collection<Integer> maxSubTerms, LindexTerm oriTerm) {
		this.featureEmbeddings.put(oriTerm.getId(), fastSu);

		LindexTerm[] children = oriTerm.getChildren();
		boolean extendable = false;
		// No further node to grow
		if (children == null || children.length == 0) {
			extendable = false;
		} else {
			for (int i = 0; i < children.length; i++) {
				if (children[i].getParent() != oriTerm)
					continue;
				LindexTerm childTerm = children[i];
				FastSUCompleteEmbedding next = new FastSUCompleteEmbedding(
						fastSu, childTerm.getExtension());
				if (next.isIsomorphic()) {
					maxSubTerms.clear();
					maxSubTerms.add(childTerm.getId());
					this.featureEmbeddings.put(childTerm.getId(), next);
					return true; // find the precise maxSubTerms
				} else if (next.issubIsomorphic()) {
					extendable = true;
					// Further growing to test node children[i], success means
					// precise locate
					boolean success = maximumSubgraphSearch(next, maxSubTerms,
							children[i]);
					if (success)
						return true;
				}
			}
		}
		FastSUStateExpandable oriState = fastSu.getState();
		if (extendable == false) {
			if (oriState.getNodeCountB() == oriState.getNodeCountS()
					&& oriState.getEdgeCountB() == oriState.getEdgeCountS()) {
				maxSubTerms.clear();
				maxSubTerms.add(oriTerm.getId());
				return true; // find the precise maxSubTerms
			} else
				maxSubTerms.add(oriTerm.getId());
		}
		return false;
	}

	/**
	 * Run the maxSubgraphs algorithm bascially, but the difference is that
	 * instead of returning the maxSubgraphs, here in this methods we return the
	 * minNoSubgraphs
	 * 
	 * @param query
	 * @param TimeComponent
	 * @return
	 */
	public List<Integer> minNoSubgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();

		this.internalQuery = query;
		this.allexamed = true;
		this.featureEmbeddings.clear();

		CanonicalDFS dfsParser = MyFactory.getDFSCoder();
		LindexTerm[] seeds = this.dummyHead.getChildren();
		short[] status = new short[super.indexTerms.length];
		Arrays.fill(status, (short) 0);

		for (LindexTerm seed : seeds) {
			Graph seedGraph = dfsParser.parse(seed.getExtension(),
					MyFactory.getGraphFactory());
			FastSUCompleteEmbedding fastSuExt = null;
			if (gComparator.compare(seedGraph, query) <= 0)
				fastSuExt = new FastSUCompleteEmbedding(seedGraph, query);

			if (fastSuExt != null && fastSuExt.isIsomorphic()) {
				this.featureEmbeddings.put(seed.getId(), fastSuExt);
				// Mark seed's children as minNoSubgraphs
				LindexTerm[] children = seed.getChildren();
				for (LindexTerm child : children)
					if (status[child.getId()] == 0)
						this.markAsNonSubgraphs(status, child);
			} else if (fastSuExt != null && fastSuExt.issubIsomorphic()) {// is
																			// expendable
				this.featureEmbeddings.put(seed.getId(), fastSuExt);
				this.minNonSubgraphSearch(fastSuExt, seed, status);
			} else
				this.markAsNonSubgraphs(status, seed);
		}
		TimeComponent[2] += System.currentTimeMillis() - start;

		// return procedure
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < status.length; i++)
			if (status[i] == 1)
				result.add(i);
		return result;
	}

	private void minNonSubgraphSearch(FastSUCompleteEmbedding fastSu,
			LindexTerm term, short[] status) {
		LindexTerm[] children = term.getChildren();
		// No further node to grow
		if (children == null || children.length == 0) {
			return; // nothing to be added as minNonSubgraph
		} else {
			for (LindexTerm child : children) {
				if (status[child.getId()] != 0 || child.getParent() != term)
					continue;
				// else: the child term is not visited yet
				FastSUCompleteEmbedding next = new FastSUCompleteEmbedding(
						fastSu, child.getExtension());
				if (next.isIsomorphic()) {
					this.featureEmbeddings.put(child.getId(), next);
					LindexTerm[] grandChildren = child.getChildren();
					for (LindexTerm grandChild : grandChildren)
						if (status[grandChild.getId()] == 0)
							this.markAsNonSubgraphs(status, grandChild);
				} else if (next.issubIsomorphic()) {
					this.featureEmbeddings.put(child.getId(), next);
					minNonSubgraphSearch(next, child, status);
				} else
					this.markAsNonSubgraphs(status, child); // status[child.getID()]
															// == 0 for sure
			}
		}
	}

	private void markAsNonSubgraphs(short[] status, LindexTerm term) {
		// mark this seed as a NoSubgraphs
		status[term.getId()] = 1;
		// mark all the descendant of seed to be visited
		Collection<LindexTerm> cTerms = super.getSuperTerms(term);
		for (LindexTerm cTerm : cTerms)
			status[cTerm.getId()] = 2;
	}

	@Override
	public FastSUCompleteEmbedding getEmbedding(int fID, Graph query) {
		boolean runISO = false;
		if (query == this.internalQuery) {
			if (this.allexamed) {
				return this.featureEmbeddings.get(fID);
			} else if (featureEmbeddings.containsKey(fID))
				return this.featureEmbeddings.get(fID);
			else {
				runISO = true;
			}
		} else {
			this.internalQuery = query;
			this.allexamed = false;
			this.featureEmbeddings.clear();
			runISO = true;
		}
		if (runISO) {
			FastSUCompleteEmbedding emb = new FastSUCompleteEmbedding(
					this.getTermFullLabel(this.indexTerms[fID]), query);
			if (emb.issubIsomorphic()) {
				this.featureEmbeddings.put(fID, emb);
				return emb;
			} else
				return null;
		} else
			return null; // may not happen
	}

	@Override
	public int[][] getTotalLabel(int fID) {
		int[][] labels = super.getTermFullLabel(this.indexTerms[fID]);
		return labels;
	}

	@Override
	/**
	 * Given an arbitrary database graph, find its prefix feature
	 */
	public int getPrefixID(Graph g) {
		// Since the value is not initialized, there is no way of finding the
		// prefix for this graph "g"
		if (this.prefixQueryCount == null || this.filteredQueryCount == null)
			return -1;
		// 1. Find all the maximal subgraphs of "g"
		long[] TimeComponent = new long[4];
		List<Integer> maxSubs = this.maxSubgraphs(g, TimeComponent);
		if (maxSubs.size() != 0 && maxSubs.get(0) == -1) {
			return maxSubs.get(1);
		} else if (maxSubs.size() == 0)
			return -1; // return -1 if no prefix is found
		else {
			// 2. Rank all those maximal subgraphs, and return the one with
			// maximum gain function
			int result = -1;
			int maxGainValue = -1;
			for (Integer maxSub : maxSubs) {
				int gainValue = this.getGainValue(maxSub, g.getEdgeCount());
				if (gainValue > maxGainValue) {
					maxGainValue = gainValue;
					result = maxSub;
				}
			}
			return result;
		}
	}

	/**
	 * Given a featureID, find the gain function of assigning a database graph g
	 * to this featureID's "benefit"
	 * 
	 * @param featureID
	 *            : prefix feature ID
	 * @param sizeG
	 *            : size of (# of edges) the database graph [how much ISO test
	 *            is saved while this database graph is not verified with ISO
	 *            test]
	 * @return
	 */
	private int getGainValue(int featureID, int sizeG) {
		if (featureID < 0 || featureID > prefixQueryCount.length)
			System.out.println("Exception");
		int typeOne = this.prefixQueryCount[featureID] * this.fSize[featureID];
		int typeTwo = this.filteredQueryCount[featureID] * sizeG;
		return typeOne + typeTwo;
	}

	@Override
	public int getPrefixID(int fID) {
		return this.indexTerms[fID].getParent().getId();
	}

	@Override
	public int[][] getExtension(int fID) {
		return this.indexTerms[fID].getExtension();
	}

	@Override
	public int getPrefixGain(int id) {
		return this.getGainValue(id, 10); // assume that the db has average size
											// of 10
	}

}
