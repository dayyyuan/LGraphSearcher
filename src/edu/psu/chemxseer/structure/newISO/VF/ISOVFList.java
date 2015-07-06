package edu.psu.chemxseer.structure.newISO.VF;

import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

/**
 * This Class is especially design and used for subgraph isomorphism test
 * between on query graph and a list of candidate graph
 * 
 * @author dayuyuan
 * 
 */
public class ISOVFList extends ISOVF {
	private VFStateList seedState;
	private ISOGraph graphS;
	private ISOGraph[] graphBs;

	public ISOVFList(ISOGraph graphS, ISOGraph[] graphBs) {
		this.graphS = graphS;
		this.graphBs = graphBs;
		int maxGraphBSize = 0;
		for (int i = 0; i < graphBs.length; i++) {
			if (maxGraphBSize < graphBs[i].getNodeCount())
				maxGraphBSize = graphBs[i].getNodeCount();
		}
		seedState = new VFStateList(graphS, graphBs[0], maxGraphBSize);
	}

	public boolean[] isListIsomorphic() {
		boolean[] isoResults = new boolean[graphBs.length];
		for (int i = 0; i < isoResults.length; i++) {
			if (i != 0)
				seedState = new VFStateList(graphS, graphBs[i], seedState);
			isoResults[i] = match(seedState);
		}
		return isoResults;
	}
}
