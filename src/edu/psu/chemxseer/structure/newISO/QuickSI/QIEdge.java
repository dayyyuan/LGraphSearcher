package edu.psu.chemxseer.structure.newISO.QuickSI;

public class QIEdge implements Comparable<QIEdge> {
	private int edgeIndex;
	private float support;
	private int indgSize;
	private int degree;

	public QIEdge(int edgeIndex, float innerSupport, int indgSize,
			int nodeDegree) {
		this.edgeIndex = edgeIndex;
		this.support = innerSupport;
		this.indgSize = indgSize;
		this.degree = nodeDegree;
	}

	public int getEdgeIndex() {
		return this.edgeIndex;
	}

	@Override
	public int compareTo(QIEdge theOtherEdge) {
		if (this.support < theOtherEdge.support)
			return -1;
		else if (this.support == theOtherEdge.support) {
			if (this.indgSize > theOtherEdge.indgSize)
				return -1;
			else if (this.indgSize == theOtherEdge.support) {
				if (this.degree < theOtherEdge.degree)
					return -1;
				else if (this.degree == theOtherEdge.degree)
					return 0;
				else
					return 1;
			} else
				return 1;
		} else
			return 1;
	}

}
