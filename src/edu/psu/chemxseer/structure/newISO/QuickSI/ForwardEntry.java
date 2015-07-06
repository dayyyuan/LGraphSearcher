package edu.psu.chemxseer.structure.newISO.QuickSI;

public class ForwardEntry {
	private ForwardEntry nextEntry;
	private int node; // records a vertex vk in a query graph q
	private int pNode; // parent vertex of vk
	private int nodeLabel; // stores the label of vk
	private ConstrainEntry[] constrains;
	private int constrainSize;

	public ForwardEntry(int nodeIndex, int parentIndex, int nodeLabel) {
		node = nodeIndex;
		pNode = parentIndex;
		this.nodeLabel = nodeLabel;
		this.nextEntry = null;
	}

	public int getNodeID() {
		return node;
	}

	public int getParentID() {
		return pNode;
	}

	public int getNodeLabel() {
		return nodeLabel;
	}

	public void addConstrain(ConstrainEntry cEntry) {
		if (constrains == null)
			constrains = new ConstrainEntry[2];
		else if (constrains.length == constrainSize) {
			ConstrainEntry[] newConstrains = new ConstrainEntry[2 * constrainSize];
			for (int i = 0; i < constrainSize; i++)
				newConstrains[i] = constrains[i];
			constrains = newConstrains;
		}
		// add the newly added cEntry
		constrains[constrainSize] = cEntry;
		constrainSize++;
	}

	public ConstrainEntry[] getConstrains() {
		return this.constrains;
	}

	public boolean attachNextEntry(ForwardEntry next) {
		if (this.nextEntry != null) {
			return false;
		} else {
			this.nextEntry = next;
			return true;
		}
	}

	public ForwardEntry nextEntry() {
		return this.nextEntry;
	}
}
