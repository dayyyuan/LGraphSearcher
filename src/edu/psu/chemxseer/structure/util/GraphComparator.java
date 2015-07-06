package edu.psu.chemxseer.structure.util;

import java.util.Comparator;

import de.parmol.graph.Graph;

/**
 * Compare graphs based on their size
 * 
 * @author dyuan
 * 
 */
public class GraphComparator implements Comparator<Graph> {

	@Override
	public int compare(Graph o1, Graph o2) {
		if (o1.getEdgeCount() < o2.getEdgeCount())
			return 1;
		else if (o1.getEdgeCount() == o2.getEdgeCount())
			return 0;
		else
			return -1;
	}

}
