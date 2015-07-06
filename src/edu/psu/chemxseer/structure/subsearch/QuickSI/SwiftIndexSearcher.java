package edu.psu.chemxseer.structure.subsearch.QuickSI;

import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex1;

public class SwiftIndexSearcher implements IIndex1 {
	// the empty root for the SwiftIndex Tree
	protected TreeEntry root;
	protected int totalFeatureCount;

	protected SwiftIndexSearcher(TreeEntry root, int featureNum) {
		this.root = root;
		this.totalFeatureCount = featureNum;
	}

	@Override
	public List<Integer> maxSubgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		List<TreeEntry> maxSubs = new ArrayList<TreeEntry>();
		boolean hit = maxSubSearch(query, null, maxSubs, root);
		if (hit) {
			List<Integer> result = new ArrayList<Integer>(2);
			result.add(-1);
			result.add(maxSubs.get(0).getFeatureID());
			TimeComponent[2] += System.currentTimeMillis() - start;
			return result;
		}

		TimeComponent[2] += System.currentTimeMillis() - start;
		List<Integer> result = new ArrayList<Integer>(maxSubs.size());
		for (TreeEntry oneEntry : maxSubs)
			result.add(oneEntry.getEntryID());
		return result;
	}

	@Override
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		List<TreeEntry> maxSubs = new ArrayList<TreeEntry>();
		getAllSubgraphs(query, null, maxSubs, root);
		List<Integer> result = new ArrayList<Integer>(maxSubs.size());
		for (TreeEntry oneEntry : maxSubs)
			result.add(oneEntry.getEntryID());
		TimeComponent[2] += System.currentTimeMillis() - start;
		return result;
	}

	/**
	 * does not implements
	 */
	@Override
	public int designedSubgraph(Graph query, boolean[] exactMatch,
			long[] TimeComponent) {
		return 0;
	}

	private void getAllSubgraphs(Graph query,
			FastSUCompleteEmbedding parentEmb, List<TreeEntry> allSubs,
			TreeEntry parent) {
		List<TreeEntry> children = parent.getChildNodes();
		for (int i = 0; i < children.size(); i++) {
			int[][] GVCode = new int[1][];
			GVCode[0] = children.get(i).getEntry();
			FastSUCompleteEmbedding fastSuExt = null;
			if (parentEmb != null)
				fastSuExt = new FastSUCompleteEmbedding(parentEmb, GVCode);
			else
				fastSuExt = new FastSUCompleteEmbedding(GVCode, query);

			if (fastSuExt.issubIsomorphic()) {// is expendable
				// add into the maxSubs
				if (children.get(i).getFeatureID() != -1)
					allSubs.add(children.get(i));
				// keep on searching
				getAllSubgraphs(query, fastSuExt, allSubs, children.get(i));
			}
		}
	}

	private boolean maxSubSearch(Graph query,
			FastSUCompleteEmbedding parentEmb, List<TreeEntry> maxSubs,
			TreeEntry parent) {
		List<TreeEntry> children = parent.getChildNodes();
		for (int i = 0; i < children.size(); i++) {
			int[][] GVCode = new int[1][];
			GVCode[0] = children.get(i).getEntry();
			FastSUCompleteEmbedding fastSuExt = null;
			if (parentEmb != null)
				fastSuExt = new FastSUCompleteEmbedding(parentEmb, GVCode);
			else
				fastSuExt = new FastSUCompleteEmbedding(GVCode, query);

			if (fastSuExt.isIsomorphic()
					&& children.get(i).getFeatureID() != -1) {
				maxSubs.clear();
				maxSubs.add(children.get(i));
				return true;
			} else if (fastSuExt.issubIsomorphic()) {// is expendable
				int preSize = maxSubs.size();
				boolean hit = maxSubSearch(query, fastSuExt, maxSubs,
						children.get(i));
				if (hit == true)
					return true;
				else {
					if (maxSubs.size() == preSize
							&& children.get(i).getFeatureID() != -1)
						maxSubs.add(children.get(i));
					else
						continue; // not a maximum subgraph
				}
			}
		}
		return false;
	}

	public int getFeatureCount() {
		return this.totalFeatureCount;
	}

	@Override
	public int[] getAllFeatureIDs() {
		// TODO Auto-generated method stub
		return null;
	}

}
