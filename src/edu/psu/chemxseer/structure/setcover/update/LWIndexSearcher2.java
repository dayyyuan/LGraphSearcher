package edu.psu.chemxseer.structure.setcover.update;

import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;
import edu.psu.chemxseer.structure.supersearch.LWTree.LWIndexSearcher;

public class LWIndexSearcher2 extends LWIndexSearcher {

	public LWIndexSearcher2(LindexSearcher lindexSearcher) {
		super(lindexSearcher);
		this.fSize = new int[indexTerms.length];
		int i = 0;
		for (LindexTerm term : this.indexTerms)
			fSize[i++] = this.getTermFullLabel(term).length;
	}

	@Override
	/**
	 * The Major Difference
	 */
	public int getPrefixID(Graph g) {
		// 1. Find all the maximal subgraphs of "g"
		long[] TimeComponent = new long[4];
		List<Integer> maxSubs = this.maxSubgraphs(g, TimeComponent);
		if (maxSubs.size() != 0 && maxSubs.get(0) == -1) {
			return maxSubs.get(1);
		} else if (maxSubs.size() == 0)
			return -1; // return -1 if no prefix is found
		else {
			// 2. Rank all those maximal subgraphs, and return the one with
			// maximum gain function
			int result = -1;
			int maxGainValue = -1;
			for (Integer maxSub : maxSubs) {
				int gainValue = fSize[maxSub];
				if (gainValue > maxGainValue) {
					maxGainValue = gainValue;
					result = maxSub;
				}
			}
			return result;
		}
	}

	@Override
	public int getPrefixGain(int fID) {
		return fSize[fID];
	}

}
