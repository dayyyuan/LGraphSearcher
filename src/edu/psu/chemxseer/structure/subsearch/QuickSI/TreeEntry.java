package edu.psu.chemxseer.structure.subsearch.QuickSI;

import java.util.ArrayList;
import java.util.List;

/**
 * Each Tree Entry may or may not corresponds to a Features
 * @author dyuan
 *
 */
/**
 * @author dyuan
 * 
 */
public class TreeEntry {
	private int[] entry; // 5 nodes
	private List<TreeEntry> childNodes;
	private int featureID; // may be -1
	private int entryID; // each entry has on id

	/**
	 * If the input "nodeString" is null, then entry will be assigned to be 5
	 * "-1";
	 * 
	 * @param labelEntry
	 */
	public TreeEntry(int[] labelEntry, int entryID) {
		if (labelEntry == null) {
			entry = new int[5];
			for (int i = 0; i < 5; i++)
				entry[i] = -1;
		} else {
			entry = new int[labelEntry.length];
			for (int i = 0; i < entry.length; i++)
				entry[i] = labelEntry[i];
		}
		this.childNodes = new ArrayList<TreeEntry>();
		this.featureID = -1;
		this.entryID = entryID;
	}

	/**
	 * @return the featureID
	 */
	public int getFeatureID() {
		return featureID;
	}

	/**
	 * @param featureID
	 *            the featureID to set
	 */
	public void setFeatureID(int featureID) {
		this.featureID = featureID;
	}

	/**
	 * @return the entry
	 */
	public int[] getEntry() {
		return entry;
	}

	/**
	 * @return the childNodes
	 */
	public List<TreeEntry> getChildNodes() {
		return childNodes;
	}

	/**
	 * @param entry
	 *            the entry to set
	 */
	public void setEntry(int[] entry) {
		this.entry = entry;
	}

	/**
	 * @param childNodes
	 *            the childNodes to set
	 */
	public void setChildNodes(List<TreeEntry> childNodes) {
		this.childNodes = childNodes;
	}

	public void addOneChilde(TreeEntry node) {
		this.childNodes.add(node);
	}

	public TreeEntry getNode(String nodeString) {
		String[] parsed = nodeString.substring(1, nodeString.length() - 1)
				.split(" ");
		int[] nodeEntry = new int[5];
		for (int i = 0; i < parsed.length; i++)
			nodeEntry[i] = Integer.parseInt(parsed[i]);

		for (int i = 0; i < this.childNodes.size(); i++) {
			if (childNodes.get(i).equals(nodeEntry))
				return childNodes.get(i);
		}
		return null; // return null if no such childNode has been found

	}

	public boolean equals(int[] nodeEntry) {
		for (int i = 0; i < entry.length; i++)
			if (nodeEntry[i] != entry[i])
				return false;
		return true;
	}

	/**
	 * Find the childEntry that equals to "nodeEntry" return null, if can not
	 * find
	 * 
	 * @param nodeEntry
	 * @return
	 */
	public TreeEntry getChild(int[] nodeEntry) {
		for (int i = 0; i < this.childNodes.size(); i++) {
			if (childNodes.get(i).equals(nodeEntry))
				return childNodes.get(i);
		}
		return null;
	}

	/**
	 * Return the string representation of the object
	 */
	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(this.entryID);
		sbuf.append(' ');
		sbuf.append(this.featureID);
		for (int i = 0; i < this.entry.length; i++) {
			sbuf.append(" ");
			sbuf.append(entry[i]);
		}
		if (entry == null || entry.length == 0)
			System.out.println("Exception in ToString");
		for (int i = 0; i < this.childNodes.size(); i++) {
			sbuf.append(" ");
			sbuf.append(childNodes.get(i).entryID);
		}
		return sbuf.toString();
	}

	/**
	 * Given the string representation of the Tree, construct its memory
	 * representation and return. The childrenID are returned throught the input
	 * parameter
	 * 
	 * @param stringRep
	 * @param childrenID
	 * @return
	 */
	public static TreeEntry loadEntry(String stringRep, List<Integer> childrenID) {
		String[] temp = stringRep.split(" ");
		TreeEntry treeEntry = new TreeEntry(null, Integer.parseInt(temp[0]));
		treeEntry.featureID = Integer.parseInt(temp[1]);
		treeEntry.entry = new int[5];
		for (int i = 0; i < 5; i++)
			treeEntry.entry[i] = Integer.parseInt(temp[2 + i]);
		for (int i = 7; i < temp.length; i++)
			childrenID.add(Integer.parseInt(temp[i]));
		return treeEntry;
	}

	public int getEntryID() {
		return entryID;
	}

	public void setEntryID(int entryID) {
		this.entryID = entryID;
	}

}
