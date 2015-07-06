package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * size n edge entry records all graphs that are of size n size n edgeEntry is
 * an arraylist of edgeArraies
 * 
 * @author dayuyuan
 * 
 */
public class IN_FGindexEdgeEntry {
	private int graphSize;
	private ArrayList<IN_FGindexEdgeArray> edgeArrays;
	private int indexInEdges;

	/**
	 * Return he memory consumption of this object
	 * 
	 * @return
	 */
	public long getMemoryConsumption() {
		// graphSize, indexInEdges
		long memoryConsumption = 8;
		// each pointers in the array edgeArrays takes 8 bytes
		memoryConsumption += edgeArrays.size() << 3;
		for (int i = 0; i < edgeArrays.size(); i++)
			memoryConsumption += edgeArrays.get(i).getMemoryConsumption();
		return memoryConsumption;
	}

	public IN_FGindexEdgeEntry(int graphSize, int index) {
		this.graphSize = graphSize;
		this.indexInEdges = index;
		this.edgeArrays = new ArrayList<IN_FGindexEdgeArray>();
	}

	public IN_FGindexEdgeEntry(int graphSize, int index,
			IN_FGindexEdgeArray[] edgeArrays) {
		this.graphSize = graphSize;
		this.indexInEdges = index;
		this.edgeArrays = new ArrayList<IN_FGindexEdgeArray>(edgeArrays.length);
		for (int i = 0; i < edgeArrays.length; i++) {
			this.edgeArrays.add(edgeArrays[i]);
			edgeArrays[i].setIndexInEntry(i);
		}
	}

	public int getIndex() {
		return indexInEdges;
	}

	public void setIndex(int index) {
		this.indexInEdges = index;
	}

	public int getEntrySize() {
		return graphSize;
	}

	public void setEntrySize(int graphSize) {
		this.graphSize = graphSize;
	}

	public IN_FGindexEdgeArray[] getEdgeArrays() {
		IN_FGindexEdgeArray[] results = new IN_FGindexEdgeArray[edgeArrays
				.size()];
		edgeArrays.toArray(results);
		return results;
	}

	public void setEdgeArrays(IN_FGindexEdgeArray[] edgeArray, boolean sorted) {
		if (!sorted) {
			// sort the array
			IN_FGindexEdgeArrayComparator edgeArrayComparator = new IN_FGindexEdgeArrayComparator();
			Arrays.sort(edgeArray, edgeArrayComparator);
		}
		// Then assign the this.edgeArrays
		if (edgeArrays == null)
			edgeArrays = new ArrayList<IN_FGindexEdgeArray>(edgeArray.length);
		else
			edgeArrays.clear();
		for (int i = 0; i < edgeArray.length; i++) {
			edgeArrays.add(edgeArray[i]);
			edgeArray[i].setIndexInEntry(i);
		}
	}

	/**
	 * given an sizeM Find a EdgeArray of sizeM in this EdgeEntry If such
	 * EdgeArray do not exist, first create such a sizeM EdgeArray, and then
	 * return it
	 * 
	 * @param freqM
	 * @return
	 */
	public IN_FGindexEdgeArray getAndSetFreqMEdgeArray(int freqM) {
		for (int i = 0; i < this.edgeArrays.size(); i++) {
			if (this.edgeArrays.get(i).getArrayFrequency() == freqM)
				return this.edgeArrays.get(i);
			else {
				// no such sizeM EdgeArray
				if (this.edgeArrays.get(i).getArrayFrequency() > freqM) {
					IN_FGindexEdgeArray edgeArray = new IN_FGindexEdgeArray(
							freqM, i);
					this.edgeArrays.add(i, edgeArray);
					// reset their inner-index
					for (int j = i + 1; j < edgeArrays.size(); j++)
						edgeArrays.get(j).setIndexInEntry(j);
					return edgeArray;
				}
			}
		}
		// Else, all edgearrays are smaller than freqM
		int numofArrays = this.edgeArrays.size();
		IN_FGindexEdgeArray edgeArray = new IN_FGindexEdgeArray(freqM,
				numofArrays);
		this.edgeArrays.add(edgeArray);
		return edgeArray;
	}

	public IN_FGindexEdgeArray getFreqMEdgeArray(int freqM) {
		for (int i = 0; i < this.edgeArrays.size(); i++) {
			if (this.edgeArrays.get(i).getArrayFrequency() == freqM)
				return this.edgeArrays.get(i);
			else {
				// not such sizeM EdgeArray
				if (this.edgeArrays.get(i).getArrayFrequency() > freqM) {
					break;
				}
			}
		}
		return null;
	}

	/**
	 * Given the minimum frequency Find the minimal edgearray that has frequency
	 * greater or equal to minfreq
	 * 
	 * @param minfreq
	 * @return
	 */
	public IN_FGindexEdgeArray getEdgeArrayMinFreq(int minfreq) {
		for (int i = 0; i < this.edgeArrays.size(); i++) {
			if (this.edgeArrays.get(i).getArrayFrequency() >= minfreq)
				return this.edgeArrays.get(i);
		}
		return null;
	}

	/**
	 * Given the maximum frequency Find the maximal edgearray that has frequency
	 * smaller than maxfreq
	 * 
	 * @param maxfreq
	 * @return
	 */
	public IN_FGindexEdgeArray getEdgeArrayMaxFreq(int maxfreq) {
		for (int i = this.edgeArrays.size() - 1; i >= 0; i--) {
			if (this.edgeArrays.get(i).getArrayFrequency() <= maxfreq)
				return this.edgeArrays.get(i);
			else
				continue;
		}
		return null;
	}

	public int getMaximumFrequency() {
		IN_FGindexEdgeArray last = this.edgeArrays
				.get(this.edgeArrays.size() - 1);
		return last.getArrayFrequency();
	}

	public int getMinimumFrequency() {
		return edgeArrays.get(0).getArrayFrequency();
	}

	public IN_FGindexEdgeArray getSmallerEdgeArray(IN_FGindexEdgeArray edgeArray) {
		int currentIndex = edgeArray.getIndexInEntry();
		if (currentIndex - 1 >= 0)
			return this.edgeArrays.get(currentIndex - 1);
		else
			return null;
	}

	public IN_FGindexEdgeArray getBiggerEdgeArray(IN_FGindexEdgeArray edgeArray) {
		int currentIndex = edgeArray.getIndexInEntry();
		if (currentIndex + 1 < this.edgeArrays.size())
			return this.edgeArrays.get(currentIndex + 1);
		else
			return null;
	}

	/**
	 * Sort each inner-array of this edge entry
	 */
	public void sortArray() {
		for (int i = 0; i < this.edgeArrays.size(); ++i)
			this.edgeArrays.get(i).sortIDs();
	}

}
