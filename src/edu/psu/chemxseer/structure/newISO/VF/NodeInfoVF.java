package edu.psu.chemxseer.structure.newISO.VF;

/**
 * For the VF Algorithm, all the nodes are selected according to its label only:
 * from the smaller label to the larger label
 * 
 * @author dayuyuan
 * 
 */
public class NodeInfoVF implements Comparable<NodeInfoVF> {
	private int nodeIndex;
	private int nodeLabel;

	/**
	 * Constructing a NodeInfoVF object
	 * 
	 * @param nodeIndex
	 * @param nodeLabel
	 */
	public NodeInfoVF(int nodeIndex, int nodeLabel) {
		this.nodeIndex = nodeIndex;
		this.nodeLabel = nodeLabel;
	}

	@Override
	public int compareTo(NodeInfoVF arg0) {
		if (nodeLabel < arg0.nodeLabel)
			return -1;
		else if (nodeLabel == arg0.nodeLabel)
			return 0;
		else
			return 1;
	}

	public int getNodeIndex() {
		return this.nodeIndex;
	}

	public int getNodeLabel() {
		return this.nodeLabel;
	}

}
