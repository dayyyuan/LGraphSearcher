package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * The structure of the CIndexTopDown uses a BinaryTree
 * 
 * @author dayuyuan
 * 
 */
public class CIndexTreeNode {
	private String featureLabel;

	private CIndexTreeNode left;
	private CIndexTreeNode right;

	private int fID;// feature ID: not unique, for indexing
	private int nID;// nodeID: unique ID

	private CIndexTreeLeafNode leafNode; // the leafIndex associated with

	public static CIndexTreeNode parseNode(String aLine) {
		String[] tokens = aLine.split(",");
		int flag = Integer.parseInt(tokens[0]);
		if (flag != -1) {
			return new CIndexTreeNode(flag, Integer.parseInt(tokens[1]),
					tokens[2], null);
		} else {
			return new CIndexTreeNode(-1, -1, null,
					CIndexTreeLeafNode.parseNode(Arrays.copyOfRange(tokens, 1,
							tokens.length)));
		}
	}

	public CIndexTreeNode(int nID, int fID, String featureLabel,
			CIndexTreeLeafNode leafNode) {
		this.nID = nID;
		this.fID = fID;
		this.featureLabel = featureLabel;
		this.leafNode = leafNode;
	}

	/**
	 * @return the left
	 */
	public CIndexTreeNode getLeft() {
		return left;
	}

	/**
	 * @return the right
	 */
	public CIndexTreeNode getRight() {
		return right;
	}

	/**
	 * @param left
	 *            the left to set
	 */
	public void setLeft(CIndexTreeNode left) {
		this.left = left;
	}

	/**
	 * @param right
	 *            the right to set
	 */
	public void setRight(CIndexTreeNode right) {
		this.right = right;
	}

	public String toNodeString() {
		if (nID != -1) {
			return this.nID + "," + this.fID + "," + this.featureLabel;
		} else
			return Integer.toString(-1) + this.leafNode.toNodeString();
	}

	private Graph getGraph() {
		if (featureLabel != null)
			return MyFactory.getDFSCoder().parse(this.featureLabel,
					MyFactory.getGraphFactory());
		else
			return null;
	}

	public int getFID() {
		return fID;
	}

	/**
	 * Insert <FID, This> to the input HashMap
	 * 
	 * @param distinctFeatures
	 */
	public void insertToHash(Map<Integer, Graph> distinctFeatures) {
		if (this.isLeafNode()) {
			this.leafNode.insertToHash(distinctFeatures);
		} else {
			if (this.left != null)
				this.left.insertToHash(distinctFeatures);
			if (this.right != null)
				this.right.insertToHash(distinctFeatures);
			distinctFeatures.put(this.fID, this.getGraph());
		}
	}

	public boolean isLeafNode() {
		if (this.nID == -1) {
			if (this.leafNode == null) {
				System.out.println("exception in CIndexTreeNode: no leafIndex");
				return true;
			} else
				return true;
		} else
			return false;
	}

	public List<Integer> getNoSubgraphs(Graph query) {
		if (isLeafNode()) {
			return this.leafNode.getNoSubgraphs(query);
		} else
			return null;
	}

	public boolean isSubISO(Graph query) {
		FastSU su = new FastSU();
		Graph g = this.getGraph();
		if (g == null)
			return false;
		else
			return su.isIsomorphic(g, query);
	}

}
