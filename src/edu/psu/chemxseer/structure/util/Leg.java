package edu.psu.chemxseer.structure.util;

/**
 * A leg used in Subgraph Generator for subgraph extension
 * 
 * @author dayuyuan
 * 
 */
public class Leg {
	private int nodeA, nodeB;
	// private int edgeLabel;
	private boolean extensionType;
	public static boolean NODE_EXTENSION = true;
	public static boolean EDGE_EXTENSION = false;

	/**
	 * When extension type is node extension nodeA is nodes already in subgraph,
	 * nodeB is newly added node When extension type is edge extension nodeA and
	 * nodeB are both nodes that already in subgraph
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param extensionType
	 */
	public Leg(int nodeA, int nodeB, boolean extensionType) {
		if (nodeA == -1 || nodeB == -1)
			System.out
					.println("YDY: Error in Leg construction: not a valid extension");
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		// this.edgeLabel = edgeLabel;
		this.extensionType = extensionType;
	}

	/**
	 * Classical node extension, only one node is
	 * 
	 * @param node
	 */
	public Leg(int node) {
		if (node == -1)
			System.out
					.println("YDY: Error in Leg construction: not a valid extension");
		this.nodeB = node;
		this.nodeA = -1;
		// this.edgeLabel = -1;
	}

	public boolean isNodeExtension() {
		return extensionType;
	}

	public boolean isEdgeExtension() {
		return !extensionType;
	}

	public boolean isNodeOnlyExtension() {
		if (this.nodeA == -1)
			return true;
		else
			return false;
	}

	/**
	 * Only eligible for node extension
	 * 
	 * @return
	 */
	public int getAlreadyInNode() {
		return nodeA;
	}

	/**
	 * Only eligible for node extension
	 * 
	 * @return
	 */
	public int getNewNode() {
		return nodeB;
	}

	/**
	 * Only eligible for edge extension
	 * 
	 * @return
	 */
	public int getNodeA() {
		return nodeA;
	}

	/**
	 * Only eligible for edge extension
	 * 
	 * @return
	 */
	public int getNodeB() {
		return nodeB;
	}

	/**
	 * Only eligible for node only extension
	 * 
	 * @return
	 */
	public int getNode() {
		return nodeB;
	}

	/**
	 * Only eligible for node+edge and edge extension
	 * 
	 * @return
	 */
	/*
	 * public int getEdgeLable(){ return edgeLabel; }
	 */

}
