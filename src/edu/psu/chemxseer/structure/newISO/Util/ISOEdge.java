package edu.psu.chemxseer.structure.newISO.Util;

public class ISOEdge {
	private int nodeALabel;
	private int nodeBLabel;
	private int edgeLabel;

	public ISOEdge(int labelA, int labelB, int edgeLabel) {
		this.nodeALabel = labelA;
		this.nodeBLabel = labelB;
		this.edgeLabel = edgeLabel;
	}

	public boolean equals(ISOEdge oneEdge) {
		if (this.edgeLabel != oneEdge.edgeLabel)
			return false;
		if (this.nodeALabel == oneEdge.nodeALabel
				&& this.nodeBLabel == oneEdge.nodeBLabel)
			return true;
		if (this.nodeALabel == oneEdge.nodeBLabel
				&& this.nodeBLabel == oneEdge.nodeALabel)
			return true;
		return false;
	}

}
