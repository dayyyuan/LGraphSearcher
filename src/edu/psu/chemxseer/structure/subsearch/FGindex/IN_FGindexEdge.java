package edu.psu.chemxseer.structure.subsearch.FGindex;

/**
 * Representing a edge in FGindex for indexing
 * 
 * @author dayuyuan
 * 
 */
public class IN_FGindexEdge {
	private int firstNode;
	private int secondNode;
	private int edgeLabel;

	public IN_FGindexEdge(int first, int second, int edge) {
		if (first < second) {
			firstNode = first;
			secondNode = second;
		} else {
			firstNode = second;
			secondNode = first;
		}
		edgeLabel = edge;
	}

	@Override
	public boolean equals(Object anotherEdge) {
		if (!(anotherEdge instanceof IN_FGindexEdge))
			return false;
		IN_FGindexEdge another = (IN_FGindexEdge) anotherEdge;
		if (firstNode == another.firstNode && secondNode == another.secondNode
				&& edgeLabel == another.edgeLabel)
			return true;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return firstNode * 100 + secondNode * 100 + edgeLabel;
	}

	public int getFirstNode() {
		return firstNode;
	}

	public void setFirstNode(int firstNode) {
		this.firstNode = firstNode;
	}

	public int getSecondNode() {
		return secondNode;
	}

	public void setSecondNode(int secondNode) {
		this.secondNode = secondNode;
	}

	public int getEdgeLable() {
		return edgeLabel;
	}

	public void setEdgeLable(int edgeLable) {
		this.edgeLabel = edgeLable;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(firstNode);
		buf.append(',');
		buf.append(secondNode);
		buf.append(',');
		buf.append(edgeLabel);
		return buf.toString();
	}

	/**
	 * Return the memory consumption of this class for each object No defualt 20
	 * bytes of memory for each object
	 * 
	 * @return
	 */
	public int getMemoryConsumption() {
		// 3 int: each of which occupty 4 byte
		return 12;
	}

}
