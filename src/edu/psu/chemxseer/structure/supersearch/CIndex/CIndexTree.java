package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;

public class CIndexTree implements IIndex0 {

	protected CIndexTreeNode rootFeature;
	protected int nodeCount;
	protected Map<Integer, Graph> distinctFeatures;

	public CIndexTree(CIndexTreeNode rootNode, int nodeCount) {
		this.rootFeature = rootNode;
		this.nodeCount = nodeCount;
	}

	/**
	 * Return the subgraph not contained in the query [redundant removed]
	 * 
	 * @param query
	 * @param TimeComponent
	 * @return
	 */
	public List<Integer> getNoSubgraphs(Graph query, long[] TimeComponent) {
		long startTime = System.currentTimeMillis();
		List<Integer> results = new ArrayList<Integer>();
		CIndexTreeNode theNode = this.rootFeature;
		while (!theNode.isLeafNode()) {
			// test if the node is contained in the query
			boolean temp = theNode.isSubISO(query);
			if (temp) {
				theNode = theNode.getLeft(); // Contained Features
			} else {
				results.add(theNode.getFID());
				theNode = theNode.getRight(); // Not Contained Features
			}
		}
		results.addAll(theNode.getNoSubgraphs(query));

		TimeComponent[2] += System.currentTimeMillis() - startTime;
		return results;
	}

	@Override
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		long startTime = System.currentTimeMillis();
		if (this.distinctFeatures == null)
			createDistinctFeatures();
		List<Integer> results = new ArrayList<Integer>();
		FastSU iso = new FastSU();
		for (Entry<Integer, Graph> oneNode : this.distinctFeatures.entrySet()) {
			boolean temp = iso.isIsomorphic(oneNode.getValue(), query);
			if (temp)
				results.add(oneNode.getKey());
		}
		TimeComponent[2] += System.currentTimeMillis() - startTime;
		return results;
	}

	private void createDistinctFeatures() {
		this.distinctFeatures = new HashMap<Integer, Graph>();
		if (this.rootFeature != null)
			this.rootFeature.insertToHash(this.distinctFeatures);

	}

	public int getNodeCount() {
		return this.nodeCount;
	}

	public int getDistinctFeatureCount() {
		if (this.distinctFeatures == null)
			createDistinctFeatures();
		return this.distinctFeatures.size();
	}

	@Override
	public int[] getAllFeatureIDs() {
		if (this.distinctFeatures == null)
			createDistinctFeatures();
		int[] rest = new int[distinctFeatures.size()];
		int iter = 0;
		for (Integer key : this.distinctFeatures.keySet())
			rest[iter++] = key;
		return rest;
	}

}
