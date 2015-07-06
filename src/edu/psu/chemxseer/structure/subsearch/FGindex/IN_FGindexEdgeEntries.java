package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.util.ArrayList;
import java.util.Arrays;

import edu.psu.chemxseer.structure.util.OrderedIntSet;

public class IN_FGindexEdgeEntries {
	// edge Entries are sorted according to their size
	// size 1 edge entry saves all graphs that has only one edge
	// size 2 edge entry saves all graphs that has only two edges
	// size n edge entry saves all graphs that has only n edges
	private ArrayList<IN_FGindexEdgeEntry> edgeEntries;
	private boolean frequent;

	/**
	 * Return the calculated memory consumption of this class Treat ArrayList as
	 * an array No 20 bytes memory for an empty object
	 * 
	 * @return
	 */
	public long getMemoryConsumption() {
		// boolean frequent
		long memoryConsumption = 1;
		// The pointer at each entry of the array takes 8 byte, omit the size of
		// the array (20 byte)
		memoryConsumption += edgeEntries.size() << 3;
		for (int i = 0; i < edgeEntries.size(); i++)
			memoryConsumption += edgeEntries.get(i).getMemoryConsumption();
		;
		return memoryConsumption;
	}

	public IN_FGindexEdgeEntries(boolean frequent) {
		this.edgeEntries = new ArrayList<IN_FGindexEdgeEntry>();
		this.frequent = frequent;
	}

	public IN_FGindexEdgeEntries(IN_FGindexEdgeEntry[] entries, boolean frequent) {
		this.edgeEntries = new ArrayList<IN_FGindexEdgeEntry>(entries.length);
		this.frequent = frequent;
		for (int i = 0; i < entries.length; i++)
			this.edgeEntries.add(entries[i]);
	}

	/**
	 * Return all graphs in this graph entries, that are of size = graphSize and
	 * its edge frequency >= minimumFrequency Union
	 * 
	 * @param graphSize
	 * @param minimumFrequency
	 * @return
	 */
	public int[] getGraphsWithMinimumFreq(int graphSize, int minimumFrequency) {
		IN_FGindexEdgeEntry oneEntry = this.getSizeNEdgeEntry(graphSize);
		if (oneEntry == null)
			return null;
		else {
			IN_FGindexEdgeArray oneArray = oneEntry
					.getEdgeArrayMinFreq(minimumFrequency);

			if (oneArray == null)
				return null;
			OrderedIntSet set = new OrderedIntSet();
			set.add(oneArray.getGraphIDs());
			IN_FGindexEdgeArray biggerArray = oneEntry
					.getBiggerEdgeArray(oneArray);
			while (biggerArray != null) {
				if (biggerArray.getGraphIDs() != null)
					set.add(biggerArray.getGraphIDs());
				biggerArray = oneEntry.getBiggerEdgeArray(biggerArray);
			}
			return set.getItems();
		}
	}

	/**
	 * Return all graphs in this graph entries, that are of size = graphSize and
	 * its edge frequency <=- maximumFrequency
	 * 
	 * @param graphSize
	 * @param maximumFrequency
	 * @return
	 */
	public int[] getGraphsWithMaximumFreq(int graphSize, int maximumFrequency) {
		IN_FGindexEdgeEntry oneEntry = this.getSizeNEdgeEntry(graphSize);
		if (oneEntry == null)
			return null;
		else {
			IN_FGindexEdgeArray oneArray = oneEntry
					.getEdgeArrayMaxFreq(maximumFrequency);
			if (oneArray == null)
				return null;

			OrderedIntSet set = new OrderedIntSet();
			set.add(oneArray.getGraphIDs());
			IN_FGindexEdgeArray smallerArray = oneEntry
					.getSmallerEdgeArray(oneArray);
			while (smallerArray != null) {
				if (smallerArray.getGraphIDs() != null)
					set.add(smallerArray.getGraphIDs());
				smallerArray = oneEntry.getSmallerEdgeArray(smallerArray);
			}
			return set.getItems();
		}
	}

	/**
	 * Return all graphs in this graph entries, that are of size = graphSize and
	 * its edge frequency = frequency
	 * 
	 * @param graphSize
	 * @param frequency
	 * @return
	 */
	public int[] getGraphsWithFreq(int graphSize, int frequency) {
		IN_FGindexEdgeEntry oneEntry = this.getSizeNEdgeEntry(graphSize);
		if (oneEntry == null)
			return null;
		IN_FGindexEdgeArray oneArray = oneEntry.getFreqMEdgeArray(frequency);
		if (oneArray == null)
			return null;
		return oneArray.getGraphIDs();
	}

	/**
	 * Return all graphs in this graph entries, that are of size >= minGraphSize
	 * and its edge frequency >= minFrequency
	 * 
	 * @param minGraphSize
	 * @param minFrequency
	 * @return
	 */
	public int[] getGraphsWithMinSizeMinFreq(int minGraphSize, int minFrequency) {
		int lastIndex = this.edgeEntries.size() - 1;
		int maxGraphSize = this.edgeEntries.get(lastIndex).getEntrySize();
		if (minGraphSize > maxGraphSize)
			return null;
		OrderedIntSet set = new OrderedIntSet();

		for (int i = lastIndex; i >= 0; i--) {
			int graphSize = this.edgeEntries.get(i).getEntrySize();
			if (graphSize < minGraphSize)
				break;
			int[] graphsOfFixSizeMinFrequency = this.getGraphsWithMinimumFreq(
					graphSize, minFrequency);
			if (graphsOfFixSizeMinFrequency == null)
				continue;
			set.add(graphsOfFixSizeMinFrequency);
		}
		return set.getItems();
	}

	public IN_FGindexEdgeEntry[] getEdgeEntries() {
		if (edgeEntries == null)
			return null;
		IN_FGindexEdgeEntry[] results = new IN_FGindexEdgeEntry[this.edgeEntries
				.size()];
		this.edgeEntries.toArray(results);
		return results;
	}

	public void setEdgeEntries(IN_FGindexEdgeEntry[] edgeEntries, boolean sorted) {
		// First have to sort those edgeEntries
		if (!sorted) {
			IN_FGindexEdgeEntryComparator comparator = new IN_FGindexEdgeEntryComparator();
			Arrays.sort(edgeEntries, comparator);
		}
		// Assign the endgeEntries
		if (this.edgeEntries == null)
			this.edgeEntries = new ArrayList<IN_FGindexEdgeEntry>(
					edgeEntries.length);
		else
			this.edgeEntries.clear();
		for (int i = 0; i < edgeEntries.length; i++) {
			this.edgeEntries.add(edgeEntries[i]);
			edgeEntries[i].setIndex(i);
		}
	}

	/**
	 * given an sizeN Find a EdgeEntry of sizeN in this EdgeEntries If such
	 * EdgeEntry do not exist, first create such a sizeN EdgeEntry, and then
	 * return it
	 * 
	 * @param sizeN
	 * @return
	 */
	public IN_FGindexEdgeEntry getAndSetSizeNEdgeEntry(int sizeN) {
		for (int i = 0; i < this.edgeEntries.size(); i++) {
			if (this.edgeEntries.get(i).getEntrySize() == sizeN)
				return this.edgeEntries.get(i);
			else {
				// not such sizeN EdgeArray
				if (this.edgeEntries.get(i).getEntrySize() > sizeN) {
					IN_FGindexEdgeEntry edgeEntry = new IN_FGindexEdgeEntry(
							sizeN, i);
					this.edgeEntries.add(i, edgeEntry);
					for (int j = i + 1; j < edgeEntries.size(); j++)
						this.edgeEntries.get(j).setIndex(j);
					return edgeEntry;
				}
				// else continue;
			}
		}
		// if all edgeEntries have size smaller than sizeN
		int index = this.edgeEntries.size();
		IN_FGindexEdgeEntry edgeEntry = new IN_FGindexEdgeEntry(sizeN, index);
		this.edgeEntries.add(edgeEntry);
		return edgeEntry;
	}

	/**
	 * Return the edgeEntry if find it Return null if dose not exist
	 * 
	 * @param sizeN
	 * @return
	 */
	public IN_FGindexEdgeEntry getSizeNEdgeEntry(int sizeN) {
		for (int i = 0; i < this.edgeEntries.size(); i++) {
			if (this.edgeEntries.get(i).getEntrySize() == sizeN)
				return this.edgeEntries.get(i);
			else {
				// not such sizeM EdgeArray
				if (this.edgeEntries.get(i).getEntrySize() > sizeN) {
					break;
				}
				// else continue;
			}
		}
		return null;
	}

	/**
	 * Given the current edgeEntry, get the edgeEntry that precede this
	 * edgeEntry
	 * 
	 * @param edgeEntry
	 * @return
	 */
	public IN_FGindexEdgeEntry getSmallerEdgeEntry(IN_FGindexEdgeEntry edgeEntry) {
		int currentIndex = edgeEntry.getIndex();
		if (currentIndex - 1 >= 0)
			return this.edgeEntries.get(currentIndex - 1);
		else
			return null;
	}

	/**
	 * Given the current edgeEntry, get the edgeEntry that succeed this
	 * edgeEntry.
	 * 
	 * @param edgeEntry
	 * @return
	 */
	public IN_FGindexEdgeEntry getBiggerEdgeEntry(IN_FGindexEdgeEntry edgeEntry) {
		int currentIndex = edgeEntry.getIndex();
		if (currentIndex + 1 < this.edgeEntries.size())
			return this.edgeEntries.get(currentIndex + 1);
		else
			return null;
	}

	/**
	 * Add this graph with graphIndex into an EdgeEntry of size edgeNum and an
	 * EngeArray of size frequency
	 * 
	 * @param edgeNum
	 * @param frequency
	 * @param graphIndex
	 */
	public void addGraph(int edgeNum, int frequency, int graphIndex) {
		IN_FGindexEdgeEntry edgeEntry = this.getAndSetSizeNEdgeEntry(edgeNum);
		IN_FGindexEdgeArray edgeArray = edgeEntry
				.getAndSetFreqMEdgeArray(frequency);
		edgeArray.addGraphID(graphIndex);
	}

	public void sortEntry() {
		for (int i = 0; i < this.edgeEntries.size(); i++) {
			IN_FGindexEdgeEntry ithEntry = this.edgeEntries.get(i);
			ithEntry.sortArray();
		}
	}

	public boolean isFrequent() {
		return this.frequent;
	}
}
