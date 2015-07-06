package edu.psu.chemxseer.structure.newISO.QuickSI;

/**
 * This is an implementation of the Constrain QI Entry It can be either an extra
 * edge constrain, where edgeDegree indicates the node that vk connected to. or
 * a degree constraint, where edgeDegree indicate the degree.
 * 
 * @author dayuyuan
 */
public class ConstrainEntry {
	private boolean constrainType; // true: degree, false: edge
	private int edgeDegree;

	public ConstrainEntry(boolean constrainType, int constrainValue) {
		this.constrainType = constrainType;
		this.edgeDegree = constrainValue;
	}

	public static ConstrainEntry buildDegreeConstrain(int degree) {
		return new ConstrainEntry(true, degree);
	}

	public static ConstrainEntry buildEdgeConstrain(int destNode) {
		return new ConstrainEntry(false, destNode);
	}

	public boolean isDegreeContrain() {
		return constrainType;
	}

	public boolean isEdgeContrain() {
		return !constrainType;
	}

	public int getContrainValue() {
		return edgeDegree;
	}
}
