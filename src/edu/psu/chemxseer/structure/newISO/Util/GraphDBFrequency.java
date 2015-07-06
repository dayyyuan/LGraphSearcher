package edu.psu.chemxseer.structure.newISO.Util;

import java.util.Hashtable;

public class GraphDBFrequency {

	Hashtable<Integer, Float> nodeSupport;
	Hashtable<ISOEdge, Float> edgeSupport;

	public float getNodeSupport(int nodelabel) {
		return nodeSupport.get(nodelabel);
	}

	public float getEdgeSupport(int nodeALabel, int nodeBLabel, int edgeLabel) {
		return edgeSupport.get(new ISOEdge(nodeALabel, nodeBLabel, edgeLabel));
	}
}
