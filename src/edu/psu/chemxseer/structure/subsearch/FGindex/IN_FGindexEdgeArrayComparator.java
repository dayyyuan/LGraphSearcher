package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.util.Comparator;

public class IN_FGindexEdgeArrayComparator implements
		Comparator<IN_FGindexEdgeArray> {
	/**
	 * Given two FGindexEdgeArray, compare their order
	 */
	@Override
	public int compare(IN_FGindexEdgeArray o1, IN_FGindexEdgeArray o2) {
		if (o1.getArrayFrequency() < o2.getArrayFrequency())
			return -1;
		else if (o1.getArrayFrequency() == o2.getArrayFrequency())
			return 0;
		else
			return 1;

	}

}
