package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.parmol.graph.Graph;

public class CIndexTreeLeafNode {
	private CIndexFlat index;
	private int[] nameConvention;

	/**
	 * Construct a CIndexTreeLeafNode
	 * 
	 * @param index
	 * @param nameConvention
	 */
	public CIndexTreeLeafNode(CIndexFlat index, int[] nameConvention) {
		this.index = index;
		this.nameConvention = nameConvention;
	}

	public List<Integer> getNoSubgraphs(Graph query) {
		if (nameConvention.length == 0) {
			return new ArrayList<Integer>();
		} else {
			List<Integer> nonSubgraphs = index.getNoSubgraphs(query,
					new long[4]);
			List<Integer> result = new ArrayList<Integer>();
			for (Integer it : nonSubgraphs)
				result.add(nameConvention[it]);
			return result;
		}
	}

	public void insertToHash(Map<Integer, Graph> distinctFeatures) {
		for (int i = 0; i < nameConvention.length; i++) {
			distinctFeatures.put(nameConvention[i], index.getFeatureGraph(i));
		}
	}

	public static CIndexTreeLeafNode parseNode(String[] tokens) {
		int size = tokens.length / 2;
		String[] labels = new String[size];
		int[] nameConvention = new int[size];
		for (int i = 0, j = 0; i < size; i++) {
			labels[i] = tokens[j++];
			nameConvention[i] = Integer.parseInt(tokens[j++]);
		}
		return new CIndexTreeLeafNode(new CIndexFlat(labels), nameConvention);
	}

	public String toNodeString() {
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < nameConvention.length; i++) {
			bf.append(",");
			bf.append(index.getFeatureString(i));
			bf.append(",");
			bf.append(this.nameConvention[i]);
		}
		return bf.toString();
	}

}
