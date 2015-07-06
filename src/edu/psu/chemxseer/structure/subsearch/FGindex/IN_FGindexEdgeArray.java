package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.util.Arrays;

/**
 * Size m arrays is a collection of graphs whose edgenumber = n, with m
 * appearance of certain distinct edge pay attention: those IDs in each
 * EdgeArray is sorted
 * 
 * @author dayuyuan
 * 
 */
public class IN_FGindexEdgeArray {
	// Frequency of edges in this EdgeArray
	private int edgeFrequency;
	private int[] graphIDs;
	// The number of graphs in graphIDs
	private int sizeOfGraphIDs;

	private boolean sorted;
	private int indexInEdgeEntry;

	public long getMemoryConsumption() {
		// edgeFrequency, sizeOfGraphIDs, indexInEdgeEntry;
		long memoryConsumption = 12;
		// boolean sorted
		memoryConsumption += 1;
		// graphIDs
		memoryConsumption += graphIDs.length << 2;
		return memoryConsumption;
	}

	public IN_FGindexEdgeArray(int frequency, int[] graphs, boolean sorted,
			int indexInEdgeEntry) {
		// first step, sort those graphs ids
		if (!sorted) {
			Arrays.sort(graphs);
			sorted = true;
		}
		// second step
		this.edgeFrequency = frequency;
		this.sizeOfGraphIDs = graphs.length;
		graphIDs = graphs.clone();
		this.indexInEdgeEntry = indexInEdgeEntry;
	}

	public IN_FGindexEdgeArray(int frequency, int indexInEdgeEntry) {
		this.edgeFrequency = frequency;
		this.indexInEdgeEntry = indexInEdgeEntry;
		graphIDs = null;
		this.sizeOfGraphIDs = 0;
		sorted = false;
	}

	public int getArrayFrequency() {
		return this.edgeFrequency;
	}

	public void setArrayFrequency(int frequency) {
		this.edgeFrequency = frequency;
	}

	public int[] getGraphIDs() {
		int[] results = new int[this.sizeOfGraphIDs];
		for (int i = 0; i < results.length; i++)
			results[i] = this.graphIDs[i];
		return results;
	}

	public void setGraphIDs(int[] graphIDs, boolean sorted) {
		if (!sorted)
			Arrays.sort(graphIDs);
		this.sorted = true;
		this.graphIDs = graphIDs.clone();
		this.sizeOfGraphIDs = graphIDs.length;
	}

	/**
	 * Add a new graph id into the graphArray
	 * 
	 * @param graphId
	 */
	public void addGraphID(int graphId) {
		if (this.graphIDs == null)
			this.graphIDs = new int[2];
		if (this.sizeOfGraphIDs < this.graphIDs.length) {
			this.graphIDs[sizeOfGraphIDs] = graphId;
			++sizeOfGraphIDs;
		} else if (sizeOfGraphIDs == this.graphIDs.length) {
			// enlarge the graphIDs array
			int[] newArray = new int[2 * graphIDs.length];
			for (int i = 0; i < sizeOfGraphIDs; i++)
				newArray[i] = graphIDs[i];
			this.graphIDs = newArray;
			graphIDs[sizeOfGraphIDs] = graphId;
			++sizeOfGraphIDs;
		} else {
			// For test only
			System.out.println("It is wired in addGraphsID: " + sizeOfGraphIDs
					+ " : " + graphIDs.length);
		}
		this.sorted = false;
	}

	public boolean isSorted() {
		return this.sorted;
	}

	/**
	 * Sort the IDs of graphs in a Edge Arrays
	 */
	public void sortIDs() {
		if (!sorted) {
			// System.out.println("Before sorting: ");
			Arrays.sort(graphIDs, 0, this.sizeOfGraphIDs - 1);
			// System.out.println("after sorting: ");
			sorted = true;
		}
	}

	public int getIndexInEntry() {
		return this.indexInEdgeEntry;
	}

	public void setIndexInEntry(int indexInEntry) {
		this.indexInEdgeEntry = indexInEntry;
	}

}
