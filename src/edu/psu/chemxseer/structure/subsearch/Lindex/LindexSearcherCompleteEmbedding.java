package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.util.HashMap;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.CanonicalDFS;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.iso.FastSUStateExpandable;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

// The implementation of the LindexSearcher, which can return the maxsubgraph & their corresponding
// complete embedding
public class LindexSearcherCompleteEmbedding extends LindexSearcher {

	protected LindexSearcherCompleteEmbedding(LindexTerm[] indexTerms,
			LindexTerm dummyHead) {
		super(indexTerms, dummyHead);
	}

	/**
	 * Given the query, return the maximum subgraph features & their
	 * corresponding mappings as well If an feature is isomorphic to the query,
	 * the the map is return as <feature, null>
	 * 
	 * @param query
	 * @param TimeComponent
	 * @return
	 */
	public HashMap<Integer, int[][]> maxSubgraphs2(Graph query,
			long[] TimeComponent) {
		long start = System.currentTimeMillis();
		HashMap<Integer, int[][]> maxSubTermIds = new HashMap<Integer, int[][]>();
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
				maxSubTermIds.put(seeds[i].getId(), null);
				TimeComponent[2] += System.currentTimeMillis() - start;
				return maxSubTermIds; // find the precise maxSubTerms
			} else if (fastSuExt.issubIsomorphic()) {// is extendable
				// grows the seed state
				boolean findPrecise = maximumSubgraphSearch2(fastSuExt,
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
			return null;
		return maxSubTermIds;
	}

	private boolean maximumSubgraphSearch2(FastSUCompleteEmbedding fastSu,
			HashMap<Integer, int[][]> maxSubTerms, LindexTerm oriTerm) {
		// 0. Get the current mapping of the fastSu
		int[][] mapps = fastSu.getMaps();

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
					maxSubTerms.put(childTerm.getId(), null);
					return true; // find the precise maxSubTerms
				} else if (next.issubIsomorphic()) {
					extendable = true;
					// Further growing to test node children[i], success means
					// precise locate
					boolean success = maximumSubgraphSearch2(next, maxSubTerms,
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
				maxSubTerms.put(oriTerm.getId(), null);
				return true; // find the precise maxSubTerms
			} else
				maxSubTerms.put(oriTerm.getId(), mapps);
		}
		return false;
	}
}
