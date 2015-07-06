package edu.psu.chemxseer.structure.subsearch.TreeDelta;

import java.util.Comparator;

import de.parmol.graph.Graph;

public class GraphWrapperComparator implements Comparator<GraphWrapper> {
	@Override
	public int compare(GraphWrapper o1, GraphWrapper o2) {
		Graph g1 = o1.getG();
		Graph g2 = o2.getG();
		if (g1.getNodeCount() < g2.getNodeCount())
			return -1;
		else if (g1.getNodeCount() == g2.getNodeCount()) {
			if (g1.getEdgeCount() < g2.getEdgeCount())
				return -1;
			else if (g1.getEdgeCount() == g2.getEdgeCount())
				return 0;
			else
				return 1;
		} else
			return 1;
	}

}
