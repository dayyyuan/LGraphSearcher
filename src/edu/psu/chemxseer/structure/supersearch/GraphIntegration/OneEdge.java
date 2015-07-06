package edu.psu.chemxseer.structure.supersearch.GraphIntegration;

/**
 * @author dayuyuan
 * 
 */
public class OneEdge {
	private int edgeID;
	private int nodeAID;
	private int nodeBID;

	private int edgeLabel;
	private int nodeALabel;
	private int nodeBLabel;

	/**
	 * Construct A OneEdge Class
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param edge
	 * @param nodeALabel
	 * @param nodeBLabel
	 * @param edgeLabel
	 */
	public OneEdge(int nodeA, int nodeB, int edge, int nodeALabel,
			int nodeBLabel, int edgeLabel) {
		this.nodeAID = nodeA;
		this.nodeBID = nodeB;
		this.nodeALabel = nodeALabel;
		this.nodeBLabel = nodeBLabel;
		this.edgeID = edge;
		this.edgeLabel = edgeLabel;
	}

	/**
	 * Construct A OneEdge Class
	 * 
	 * @param nodeALabel
	 * @param nodeBLabel
	 * @param edgeLabel
	 */
	public OneEdge(int nodeALabel, int nodeBLabel, int edgeLabel) {
		this.nodeAID = nodeALabel;
		this.nodeBID = nodeBLabel;
		this.edgeLabel = edgeLabel;
	}

	@Override
	public int hashCode() {
		return nodeALabel * 101 + edgeLabel + nodeBLabel * 11;
	}

	public boolean equals(OneEdge aEdge) {
		if (edgeLabel != aEdge.edgeLabel)
			return false;
		// else
		if (nodeALabel == aEdge.nodeALabel && nodeBLabel == aEdge.nodeBLabel)
			return true;
		else if (nodeALabel == aEdge.nodeBLabel
				&& nodeBLabel == aEdge.nodeALabel)
			return true;
		else
			return false;

	}

	/**
	 * @return the edgeID
	 */
	public int getEdgeID() {
		return edgeID;
	}

	/**
	 * @return the nodeAID
	 */
	public int getNodeAID() {
		return nodeAID;
	}

	/**
	 * @return the nodeBID
	 */
	public int getNodeBID() {
		return nodeBID;
	}

	/**
	 * @return the edgeLabel
	 */
	public int getEdgeLabel() {
		return edgeLabel;
	}

	/**
	 * @return the nodeALabel
	 */
	public int getNodeALabel() {
		return nodeALabel;
	}

	/**
	 * @return the nodeBLabel
	 */
	public int getNodeBLabel() {
		return nodeBLabel;
	}

	/**
	 * @param edgeID
	 *            the edgeID to set
	 */
	public void setEdgeID(int edgeID) {
		this.edgeID = edgeID;
	}

	/**
	 * @param nodeAID
	 *            the nodeAID to set
	 */
	public void setNodeAID(int nodeAID) {
		this.nodeAID = nodeAID;
	}

	/**
	 * @param nodeBID
	 *            the nodeBID to set
	 */
	public void setNodeBID(int nodeBID) {
		this.nodeBID = nodeBID;
	}

	/**
	 * @param edgeLabel
	 *            the edgeLabel to set
	 */
	public void setEdgeLabel(int edgeLabel) {
		this.edgeLabel = edgeLabel;
	}

	/**
	 * @param nodeALabel
	 *            the nodeALabel to set
	 */
	public void setNodeALabel(int nodeALabel) {
		this.nodeALabel = nodeALabel;
	}

	/**
	 * @param nodeBLabel
	 *            the nodeBLabel to set
	 */
	public void setNodeBLabel(int nodeBLabel) {
		this.nodeBLabel = nodeBLabel;
	}

}
