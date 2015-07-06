package edu.psu.chemxseer.structure.newISO.newFastSU;

import edu.psu.chemxseer.structure.newISO.QuickSI.ForwardEntry;
import edu.psu.chemxseer.structure.newISO.QuickSI.QISequenceInterface;

public class FastSUSequence implements QISequenceInterface {
	private ForwardEntry[] sequence;

	public FastSUSequence(ForwardEntry firstEntry, int entryCount) {
		this.sequence = new ForwardEntry[entryCount];
		int counter = 0;
		while (firstEntry != null) {
			sequence[counter++] = firstEntry;
			firstEntry = firstEntry.nextEntry();
		}
	}

	@Override
	public ForwardEntry getEntry(int index) {
		return sequence[index];
	}

}
