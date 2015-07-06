package edu.psu.chemxseer.structure.newISO.newFastSU;

import edu.psu.chemxseer.structure.newISO.QuickSI.SimpleQuickSI;
import edu.psu.chemxseer.structure.newISO.Util.GraphDBFrequency;
import edu.psu.chemxseer.structure.newISO.Util.ISOEdgeGraph;
import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

public class SimpleFastSU extends SimpleQuickSI {

	public SimpleFastSU() {
		// dummy
	}

	public SimpleFastSU(ISOEdgeGraph small, ISOGraph big, Embedding[][] blocks,
			GraphDBFrequency gDB) {
		graphS = small;
		graphB = big;
		int sNodeCount = small.getNodeCount();
		int bNodeCount = big.getNodeCount();

		this.F = new boolean[bNodeCount];
		this.H = new int[sNodeCount];
		for (int i = 0; i < F.length; i++)
			F[i] = false;
		for (int i = 0; i < H.length; i++)
			H[i] = -1;
		ISOFastSUOrder order = new ISOFastSUOrder(small, blocks, gDB);
		sequence = order.getSequence();

	}
}
