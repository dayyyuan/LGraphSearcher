package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.util.Comparator;

/**
 * Compare index term based on underlying graphs The smaller the graph (number
 * of nodes), the smaller the Lindex term
 * 
 * @author dayuyuan
 * 
 */
public class LindexTermComparator implements Comparator<LindexTerm> {

	@Override
	public int compare(LindexTerm o1, LindexTerm o2) {
		if (o1.getMaxNodeIndex() < o2.getMaxNodeIndex())
			return -1;
		else if (o1.getMaxNodeIndex() == o2.getMaxNodeIndex())
			return 0;
		else
			return 1;
	}

}
