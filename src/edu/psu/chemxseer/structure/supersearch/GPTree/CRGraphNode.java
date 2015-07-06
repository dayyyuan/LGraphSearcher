package edu.psu.chemxseer.structure.supersearch.GPTree;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUStateLabelling;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * Represent a node of the DAG of the CRGraph: (1) Different from the
 * LindexTerm: Labels has no extension relationships (2) Different from the
 * PrefixFeature: Labels has no extension relationships, but the nodes have
 * child/parent realtionships
 * 
 * @author dayuyuan
 * 
 */
public class CRGraphNode implements Comparable<CRGraphNode> {
	private int nodeID;
	private int[][] graphLabel;
	private int prefixID;

	private CRGraphNode[] children;

	/**
	 * Build a CRGraphNode with no children nor prefix. The graphLabel is the
	 * full graph label. Extension Labelling will be constructed soonner
	 * 
	 * @param nodeID
	 * @param graphLabel
	 */
	public CRGraphNode(int nodeID, int[][] graphLabel) {
		this.nodeID = nodeID;
		this.graphLabel = graphLabel;
		this.prefixID = -1;
		this.children = null;
	}

	/**
	 * Construct a CRGraphNode. Assume that All the Child nodes are constructed
	 * and stored in allNodes
	 * 
	 * @param nodeString
	 * @param allNodes
	 */
	public CRGraphNode(String nodeString, CRGraphNode[] allNodes) {
		String[] tokens = nodeString.split(",");
		this.nodeID = Integer.parseInt(tokens[0]);
		this.graphLabel = MyFactory.getDFSCoder().parseTextToArray(tokens[1]);
		this.prefixID = Integer.parseInt(tokens[2]);
		if (tokens.length == 3)
			this.children = null;
		else {
			this.children = new CRGraphNode[tokens.length - 3];
			for (int i = 3; i < tokens.length; i++) {
				children[i - 3] = allNodes[Integer.parseInt(tokens[i])];
				// TODO: TEST ONLY, WILL BE REMOVED LATTER
				if (children[i - 3] == null)
					System.out
							.println("Exception in the Construction of CRGraphNODE from the nodeString");
			}
		}
	}

	/**
	 * @return the children
	 */
	public CRGraphNode[] getChildren() {
		return children;
	}

	/**
	 * @return the graphLabel
	 */
	public int[][] getGraphLabel() {
		return graphLabel;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(CRGraphNode[] children) {
		this.children = children;
	}

	/**
	 * relabel the graph as the suffix of the prefix feature
	 * 
	 * @param graphLabel
	 *            the graphLabel to set
	 */
	public void reLabelGraph(int[][] prefix, int prefixID) {
		FastSU iso = new FastSU();
		FastSUStateLabelling labeling = iso.graphExtensionLabeling(prefix,
				graphLabel);
		this.graphLabel = labeling.getExtension();
		// the graphLabel can be null since the feature is the same as its
		// prefix
		this.prefixID = prefixID;
	}

	/**
	 * The prefix is not the CRGraphNode parent, but the prefix feature in
	 * prefixTree
	 * 
	 * @return the prefixID
	 */
	public int getPrefixID() {
		return prefixID;
	}

	/**
	 * @return the nodeID
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * @param nodeID
	 *            the nodeID to set
	 */
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	/**
	 * Return null if "prefix" exists, but inputed as Null
	 * 
	 * @param prefix
	 * @return
	 */
	public Graph getGraph(int[][] prefix) {
		int[][] labels = null;
		if (this.prefixID != -1)
			if (prefix == null)
				return null;
			else if (graphLabel.length == 0) {
				labels = prefix;
			} else {
				labels = new int[prefix.length + this.graphLabel.length][];
				for (int i = 0; i < prefix.length; i++)
					labels[i] = prefix[i];
				for (int j = 0; j < graphLabel.length; j++)
					labels[prefix.length + j] = graphLabel[j];
			}
		else
			labels = graphLabel;
		return MyFactory.getDFSCoder().parse(labels,
				MyFactory.getGraphFactory());
	}

	/**
	 * nodeID,graphLabel,prefixID,childrens List
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.nodeID);
		buf.append(',');
		buf.append(MyFactory.getDFSCoder().writeArrayToText(this.graphLabel));
		buf.append(',');
		buf.append(this.prefixID);
		if (this.children != null && this.children.length > 0) {
			for (int i = 0; i < this.children.length; i++) {
				buf.append(',');
				buf.append(children[i].nodeID);
			}

		}
		return buf.toString();
	}

	@Override
	public int compareTo(CRGraphNode arg1) {
		if (this.graphLabel.length < arg1.graphLabel.length)
			return -1;
		else if (this.graphLabel.length == arg1.graphLabel.length)
			return 0;
		else
			return 1;
	}
}
