package edu.psu.chemxseer.structure.newISO.newFastSU;

import edu.psu.chemxseer.structure.newISO.Util.GraphDBFrequency;
import edu.psu.chemxseer.structure.newISO.Util.ISOEdgeGraph;
import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

public class ListFastSU extends SimpleFastSU {
	protected ISOGraph graphS;
	protected ISOGraph[] graphBs;

	public ListFastSU(ISOEdgeGraph small, ISOGraph[] bigs,
			Embedding[][] blocks, GraphDBFrequency gDB) {
		this.graphS = small;
		this.graphBs = new ISOGraph[bigs.length];

		int sNodeCount = small.getNodeCount();
		int bNodeCount = 0;
		for (int i = 0; i < graphBs.length; i++) {
			if (graphBs[i].getNodeCount() > bNodeCount)
				bNodeCount = graphBs[i].getNodeCount();
		}

		this.F = new boolean[bNodeCount];
		this.H = new int[sNodeCount];
		for (int i = 0; i < F.length; i++)
			F[i] = false;
		for (int i = 0; i < H.length; i++)
			H[i] = -1;
		ISOFastSUOrder order = new ISOFastSUOrder(small, blocks, gDB);
		sequence = order.getSequence();
	}

	public boolean[] isListIsomorphic() {
		boolean[] results = new boolean[graphBs.length];
		for (int i = 0; i < graphBs.length; i++) {
			super.graphB = graphBs[i];
			results[i] = isIsomorphic();
			// clean the shared H and F
			for (int j = 0; j < graphB.getNodeCount(); j++)
				F[j] = false;
			for (int j = 0; j < H.length; j++)
				H[j] = -1;
		}
		return results;
	}

}
