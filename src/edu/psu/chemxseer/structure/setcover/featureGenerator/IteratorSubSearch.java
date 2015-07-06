package edu.psu.chemxseer.structure.setcover.featureGenerator;

import java.util.NoSuchElementException;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;

public class IteratorSubSearch implements IIteratorApp {
	private int[] dbNoSupport;
	private int[] querySupport;
	private int[] queryEqual;

	private int querySupportIndex = 0;
	private int queryEqualIndex = 0;
	private int dbIndex = 0;
	private int classOneRange;

	private boolean currentQEqual = false; // currentQ = True => use the equalQ,
											// currentQ = False => use the
											// containQ
	private int counter = 0;
	private int maxCounter = 0;

	/**
	 * Construct an IteratorSubSearch All values here are range related (mapped
	 * data)
	 * 
	 * @param set
	 * @param classOneRange
	 */
	public IteratorSubSearch(Set_Pair set, int classOneRange) {
		this.classOneRange = classOneRange;
		this.dbNoSupport = set.getValueNoG();
		this.querySupport = set.getValueQ();
		this.queryEqual = set.getValueQEqual();

		this.querySupportIndex = 0;
		this.queryEqualIndex = 0;
		this.dbIndex = 0;
		if (queryEqual.length == 0
				|| (querySupport.length > 0 && (queryEqual[0] > querySupport[0])))
			currentQEqual = false;
		else
			currentQEqual = true;
		this.counter = 0;
		this.maxCounter = querySupport.length * dbNoSupport.length
				+ classOneRange * queryEqual.length;
	}

	public IteratorSubSearch(Set_Pair_Index set, int classOneRange) {
		this.classOneRange = classOneRange;
		this.dbNoSupport = set.getUnExamedG();
		this.querySupport = set.getUnExamedQ();
		this.queryEqual = set.getValueQEqual();

		this.querySupportIndex = 0;
		this.queryEqualIndex = 0;
		this.dbIndex = 0;
		if (queryEqual.length == 0
				|| (querySupport.length > 0 && (queryEqual[0] > querySupport[0])))
			currentQEqual = false;
		else
			currentQEqual = true;
		this.counter = 0;
		this.maxCounter = querySupport.length * dbNoSupport.length
				+ classOneRange * queryEqual.length;
	}

	@Override
	public int size() {
		return this.maxCounter;
	}

	@Override
	public boolean hasNext() {
		if (counter == maxCounter)
			return false;
		else
			return true;
	}

	@Override
	// Return the <q, g> pair
	public int[] next() {
		if (this.hasNext() == false) {
			throw new NoSuchElementException();
		} else if ((!currentQEqual && this.dbIndex == this.dbNoSupport.length)
				|| (currentQEqual && this.dbIndex == classOneRange)) {
			if (currentQEqual)
				this.queryEqualIndex++;
			else
				this.querySupportIndex++;
			dbIndex = 0;
			// The case: equalIndex == querEqual.length && containIndex ==
			// querySupport.length won't happen
			if (queryEqualIndex == this.queryEqual.length)
				currentQEqual = false;
			else if (querySupportIndex == this.querySupport.length)
				currentQEqual = true;
			else if (querySupport[querySupportIndex] < queryEqual[queryEqualIndex])
				currentQEqual = false;
			else
				currentQEqual = true;
		}

		int[] result = new int[2];
		if (this.currentQEqual) { // equal query
			result[0] = this.queryEqual[queryEqualIndex];
			result[1] = dbIndex;
			dbIndex++;
		} else {
			// contain query
			result[0] = this.querySupport[querySupportIndex];
			result[1] = this.dbNoSupport[dbIndex];
			dbIndex++;
		}
		this.counter++;
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"remove() method is not supported");
	}

	// TEST ALGORITHMS:
	/*
	 * public int[] toSetSingleTest(FeatureWrapperSimple feature) { int[]
	 * dbNoSupport = feature.notContainedDatabaseGraphs(this.classOneNumber);
	 * int[] querySupport = feature.containedQueryGraphs(); int[] queryEqual =
	 * feature.getEquavalentQueryGraphs();
	 * 
	 * int resultCount = querySupport.length * dbNoSupport.length +
	 * this.classOneNumber * queryEqual.length; int[] result = new
	 * int[resultCount]; int counter = 0; // Add Items int containIndex = 0; int
	 * equalIndex = 0; while(containIndex < querySupport.length && equalIndex <
	 * queryEqual.length){ // choose the smaller item of containedQ and equalQ
	 * int containedQ = querySupport[containIndex]; int equalQ =
	 * queryEqual[equalIndex]; if(containedQ < equalQ){ // Add the filtered db
	 * graphs int base = classOneNumber * containedQ ; for(int j =0; j<
	 * dbNoSupport.length; j++){ int gID = dbNoSupport[j]; result[counter++] =
	 * base + gID; } containIndex++; } else{ // all can be filtered, no need for
	 * verification int base = classOneNumber * equalQ ; for(int gID = 0; gID <
	 * classOneNumber; gID++){ result[counter++] = base + gID; } equalIndex++; }
	 * } // Filtered Graphs: while(containIndex<querySupport.length){ int base =
	 * classOneNumber * querySupport[containIndex] ; for(int j =0; j<
	 * dbNoSupport.length; j++){ int gID = dbNoSupport[j]; result[counter++] =
	 * base + gID; } containIndex++; } // Direct Answer Graphs: while(equalIndex
	 * < queryEqual.length){ int base = classOneNumber * queryEqual[equalIndex]
	 * ; for(int gID = 0; gID < classOneNumber; gID++){ result[counter++] = base
	 * + gID; } equalIndex++; } return result; }
	 */
}