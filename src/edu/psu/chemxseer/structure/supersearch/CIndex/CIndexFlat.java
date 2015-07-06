package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * The flat index of the CIndex: super succinct All index and postings are
 * stored in character format
 * 
 * @author dayuyuan
 * 
 */
public class CIndexFlat implements IIndex0 {
	protected String[] indexingGraphs;

	public CIndexFlat(String[] indexingTerms) {
		this.indexingGraphs = indexingTerms;
	}

	/************************ Query Processing ******************************************/
	/**
	 * Step 1: Find all the subgraph features not contained in the query "q"
	 * Given the query graph q, find all the features that are subgraph
	 * isomorphic to the query
	 * 
	 * @param q
	 * @return
	 */
	public ArrayList<Integer> getNoSubgraphs(Graph q, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		boolean[] featureStatus = new boolean[this.indexingGraphs.length];
		FastSU iso = new FastSU();
		for (int i = 0; i < featureStatus.length; i++) {
			Graph f = MyFactory.getDFSCoder().parse(indexingGraphs[i],
					MyFactory.getGraphFactory());
			featureStatus[i] = iso.isIsomorphic(f, q);
		}
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < featureStatus.length; i++) {
			if (!featureStatus[i])
				results.add(i);
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		return results;
	}

	/**
	 * Return the subgrpah features not in subgraphs
	 * 
	 * @param candidates
	 * @param TimeComponent
	 * @return
	 */
	public ArrayList<Integer> getNoSubgraphs(int[] subgraphs,
			long[] TimeComponent) {
		long start = System.currentTimeMillis();
		int[] result = OrderedIntSets.getCompleteSet(subgraphs,
				this.getFeatureCount());
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < result.length; i++)
			results.add(result[i]);
		TimeComponent[2] += System.currentTimeMillis() - start;
		return results;
	}

	@Override
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		FastSU iso = new FastSU();
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < this.indexingGraphs.length; i++) {
			Graph f = MyFactory.getDFSCoder().parse(indexingGraphs[i],
					MyFactory.getGraphFactory());
			boolean isoStatus = iso.isIsomorphic(f, query);
			if (isoStatus)
				results.add(i);
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		return results;
	}

	public int getFeatureCount() {
		return this.indexingGraphs.length;
	}

	@Override
	public int[] getAllFeatureIDs() {
		int[] rest = new int[this.indexingGraphs.length];
		for (int i = 0; i < rest.length; i++)
			rest[i] = i;
		return rest;

	}

	public Graph getFeatureGraph(int fID) {
		if (fID >= 0 || fID < this.indexingGraphs.length) {
			return MyFactory.getDFSCoder().parse(indexingGraphs[fID],
					MyFactory.getGraphFactory());
		} else
			return null;
	}

	public String getFeatureString(int fID) {
		if (fID >= 0 || fID < this.indexingGraphs.length) {
			return indexingGraphs[fID];
		} else
			return null;
	}
}
