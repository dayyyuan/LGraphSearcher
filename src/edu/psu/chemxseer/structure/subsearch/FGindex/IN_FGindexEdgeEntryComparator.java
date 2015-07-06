package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.util.Comparator;

public class IN_FGindexEdgeEntryComparator implements
		Comparator<IN_FGindexEdgeEntry> {

	@Override
	public int compare(IN_FGindexEdgeEntry o1, IN_FGindexEdgeEntry o2) {
		if (o1.getEntrySize() < o2.getEntrySize())
			return -1;
		else if (o1.getEntrySize() > o2.getEntrySize())
			return 0;
		else
			return 1;
	}
}
