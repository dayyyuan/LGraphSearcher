package edu.psu.chemxseer.structure.setcover.featureGenerator;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;

public class IteratorClassification implements IIteratorApp {
	private int[] dbSupport;
	private int[] dbNoSupport;
	private int[] querySupport;
	private int[] queryNoSupport;

	private int qIndexSupport;
	private int qIndexNoSupport;
	private int dbIndex;
	private boolean isQuerySupport;

	private int counter;
	private int maxCounter;

	public IteratorClassification(Set_Pair set) {
		this.dbSupport = set.getValueG();
		this.dbNoSupport = set.getValueNoG();
		this.querySupport = set.getValueQ();
		this.queryNoSupport = set.getValueNoQ();

		this.counter = 0;
		this.maxCounter = dbSupport.length * queryNoSupport.length
				+ querySupport.length * dbNoSupport.length;
		this.qIndexNoSupport = 0;
		this.qIndexSupport = 0;
	}

	@Override
	public boolean hasNext() {
		if (this.counter < this.maxCounter)
			return true;
		else
			return false;
	}

	@Override
	// This algorithm is changed because querySupport + queryNoSupport <
	// Complege Queryies
	// public int[] next() {
	// if(hasNext() == false)
	// throw new
	// UnsupportedOperationException("remove() method is not supported");
	//
	// if((this.isDBSupport && this.dbIndex == this.dbSupport.length) ||
	// (!this.isDBSupport && this.dbIndex == this.dbNoSupport.length)){
	// this.dbIndex = 0;
	// this.qID ++;
	// if(!isDBSupport)
	// this.qIndex ++;
	// if(qIndex == this.querySupport.length)
	// isDBSupport =true;
	// else if(qID < querySupport[qIndex])
	// isDBSupport = true;
	// else if(qID == querySupport[qIndex])
	// isDBSupport = false;
	// // qID > querySupport[qIndex] can not happen
	// }
	// int[] result= new int[2];
	// result[0] = qID;
	// if(isDBSupport)
	// result[1] = this.dbSupport[dbIndex++];
	// else result[1] = this.dbNoSupport[dbIndex++];
	// this.counter++;
	// return result;
	// }
	public int[] next() {
		if (hasNext() == false)
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		if (!this.isQuerySupport && this.dbIndex == this.dbSupport.length) {
			this.dbIndex = 0;
			qIndexNoSupport++;
			this.findQuerySupportStatus();
		} else if (this.isQuerySupport
				&& this.dbIndex == this.dbNoSupport.length) {
			this.dbIndex = 0;
			qIndexSupport++;
			this.findQuerySupportStatus();
		}

		int[] result = new int[2];
		if (isQuerySupport) {
			result[0] = querySupport[qIndexSupport];
			result[1] = this.dbNoSupport[dbIndex++];
		} else {
			result[0] = queryNoSupport[qIndexNoSupport];
			result[1] = this.dbSupport[dbIndex++];
		}
		this.counter++;
		return result;
	}

	private void findQuerySupportStatus() {
		if (this.qIndexSupport == this.querySupport.length
				&& this.qIndexNoSupport == this.queryNoSupport.length)
			System.out
					.println("Exception in IteratorClassification:isQuerySupport()");
		else if (qIndexSupport == this.querySupport.length)
			this.isQuerySupport = false;
		else if (qIndexNoSupport == this.queryNoSupport.length)
			this.isQuerySupport = true;
		else {
			if (this.querySupport[qIndexSupport] < queryNoSupport[qIndexNoSupport])
				this.isQuerySupport = true;
			else
				this.isQuerySupport = false;
		}

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
	// TEST Comparison Algorithm
	/*
	 * public int[] featureToSet_Array(IFeatureWrapper oneFeature,int
	 * classOneNumber, int classTwoNumber) { int[] dbSupport =
	 * oneFeature.containedDatabaseGraphs(); int[] dbNoSupport =
	 * oneFeature.notContainedDatabaseGraphs(classOneNumber); int[] querySupport
	 * = oneFeature.containedQueryGraphs();
	 * 
	 * int resultCount = dbSupport.length * (classTwoNumber -
	 * querySupport.length) + querySupport.length * dbNoSupport.length; int[]
	 * results =new int[resultCount]; int counter = 0;
	 * 
	 * for(int qID = 0, containedQueryIndex=0; qID < classTwoNumber; qID++){
	 * boolean isSupportQuery = false; int base = qID * classOneNumber;
	 * if(containedQueryIndex < querySupport.length){ if(qID ==
	 * querySupport[containedQueryIndex]){ isSupportQuery = true;
	 * containedQueryIndex++; } } if(isSupportQuery){ // Add all not-support
	 * database graphs for(int i =0; i< dbNoSupport.length; i++)
	 * results[counter++] = base + dbNoSupport[i]; } else{ // Add all support
	 * database graphs for(int i = 0; i< dbSupport.length; i++)
	 * results[counter++] = base + dbSupport[i]; } } return results; }
	 */

}
