package edu.psu.chemxseer.structure.setcover.featureGenerator;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;

public class IteratorSupSearch implements IIteratorApp {
	private int[] dbSupport;
	private int[] noQuerySupport;
	private int dbIndex;
	private int queryIndex;
	private int counter = 0;
	private int maxCounter = 0;

	/**
	 * Construct an IteratorSupSearch: all items are range related, that is
	 * mapped value
	 * 
	 * @param set
	 */
	public IteratorSupSearch(Set_Pair set) {
		this.dbSupport = set.getValueG();
		this.noQuerySupport = set.getValueNoQ();
		this.dbIndex = this.queryIndex = 0;
		this.counter = 0;
		this.maxCounter = noQuerySupport.length * dbSupport.length;
	}

	public IteratorSupSearch(Set_Pair_Index set) {
		this.dbSupport = set.getUnExamedG();
		this.noQuerySupport = set.getUnExamedQ();
		this.dbIndex = this.queryIndex = 0;
		this.counter = 0;
		this.maxCounter = noQuerySupport.length * dbSupport.length;
	}

	@Override
	public boolean hasNext() {
		if (this.counter < this.maxCounter)
			return true;
		else
			return false;
	}

	@Override
	public int[] next() {
		if (hasNext() == false)
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		int[] result = new int[2];
		if (this.dbIndex == this.dbSupport.length) {
			dbIndex = 0;
			this.queryIndex++;
		}
		result[0] = this.noQuerySupport[queryIndex];
		result[1] = this.dbSupport[dbIndex++];
		this.counter++;
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"remove() method is not supported");
	}

	@Override
	public int size() {
		return this.maxCounter;
	}

	// FOR TEST COMPARISION
	/*
	 * int[] featureToSet_Array(IFeatureWrapper oneFeature, int classOneNumber)
	 * { int[] dbSupport = oneFeature.containedDatabaseGraphs(); int[]
	 * noQuerySupport = oneFeature
	 * .notContainedQueryGraphs(this.classTwoNumber);
	 * 
	 * int resultCount = noQuerySupport.length * dbSupport.length; int[] results
	 * = new int[resultCount]; int counter = 0; for (int i = 0; i <
	 * noQuerySupport.length; i++) { int qID = noQuerySupport[i]; int base = qID
	 * * classOneNumber; for (int j = 0; j < dbSupport.length; j++){
	 * results[counter++] = base + dbSupport[j]; } } return results; }
	 */
}
