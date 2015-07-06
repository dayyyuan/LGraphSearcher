package edu.psu.chemxseer.structure.iso;

import java.util.ArrayList;
import java.util.Comparator;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.parmolExtension.CompleteEmbedding;

public class GraphComparator implements Comparator<Graph> {
	@Override
	public int compare(Graph one, Graph two) {

		if (one.getNodeCount() < two.getNodeCount())
			return -1;
		else if (one.getNodeCount() == two.getNodeCount()) {
			if (one.getEdgeCount() < two.getEdgeCount())
				return -1;
			else if (one.getEdgeCount() == two.getEdgeCount())
				return 0;
			else
				return 1;
		} else
			return 1;
	}

	public boolean isSubgraph(Graph sub, Graph sup,
			ArrayList<CompleteEmbedding> tempEmbeddings) {
		// i < j
		if (!(this.compare(sub, sup) == -1))
			return false;
		// Test if i \ subset j
		else {
			int embeddingNum = CompleteEmbedding.getCompleteEmbeddings2(sup,
					sub, tempEmbeddings);
			if (embeddingNum > 0) {
				return true;
			} else
				return false;
		}
	}
}
