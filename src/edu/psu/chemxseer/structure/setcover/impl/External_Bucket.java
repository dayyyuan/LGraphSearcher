package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;
import java.util.Iterator;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
import edu.psu.chemxseer.structure.setcover.status.Status_IntArrayImpl;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * An implementation of the extern_bucket sort algorithm (1) First given a
 * sequential readable input, sort those sets & put them into different bucket
 * based on their size (2) Run the externel buckentize algorithm
 * 
 * @author dayuyuan
 * 
 */
public class External_Bucket implements IMaxCoverSolver {

	private ICoverStatus status;
	private IInputBucket input;

	private int currentK;
	private IFeatureWrapper[] topKSetIDs;
	private long coveredItemCount;

	// to support continuity
	private Iterator<ISet> setIterator;
	private Iterator<IBucket> bucketIterator;
	private int bucketCounter;

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float findMinSetTime;
	protected long coveredCountBfSwap;

	public External_Bucket(IInputBucket input, StatusType sType, AppType aType) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		long beforeTime = System.currentTimeMillis();

		this.input = input;
		if (sType == StatusType.normal)
			status = Status_IntArrayImpl.newInstance(input.getQCount(),
					input.getGCount());
		else
			status = Status_Decomp.newInstance(input.getQCount(),
					input.getGCount(), aType);

		this.currentK = 0;
		this.coveredItemCount = 0;
		// since the sequential input does not support the getSetCount()
		// function
		// topKSetIDs is initialize later when the greedy algorithm runs.
		this.topKSetIDs = new IFeatureWrapper[0];
		this.bucketIterator = input.iterator();
		this.bucketCounter = -1;
		this.setIterator = null;

		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("Time for External_Bucket Construction: "
				+ (afterTime - beforeTime));
		System.out.println("Space for External_Bucket Construction: "
				+ (afterMem - beforeMem));
	}

	public void closeFileStreams() {
		try {
			input.closeInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {
		MemoryConsumptionCal.runGC();
		double[] MemCost = new double[2];
		MemCost[0] = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		statusUpdateTime = exclusiveExamTime = findMinSetTime = 0;
		coveredCountBfSwap = 0;

		if (currentK < K)
			currentK = this.runRealGreedy(K, MemCost);

		long endTime = System.currentTimeMillis();

		MemoryConsumptionCal.runGC();
		double endMem = MemoryConsumptionCal.usedMemoryinMB();
		MemCost[1] = MemCost[1] - MemCost[0];
		MemCost[0] = endMem - MemCost[0];

		stat[0] = endTime - startTime;
		stat[1] = exclusiveExamTime;
		stat[2] = statusUpdateTime;
		stat[3] = findMinSetTime;
		stat[4] = 0; // For now, just leave it to be zero
		stat[5] = (float) MemCost[0];
		stat[6] = (float) MemCost[1];
		stat[7] = input.getEnumeratedSetCount();
		stat[8] = 0;
		stat[9] = coveredCountBfSwap;
		stat[10] = coveredItemCount;
		return Arrays.copyOf(topKSetIDs, currentK);
	}

	/**
	 * Run the Greedy algorithms: Assuming currentK sets are already selected,
	 * Need to find K sets in total
	 * 
	 * @return return the total number of sets selected: may be smaller than K
	 * @cost[0] Time, don't have to recoreded here, cost[1] memory in MB
	 * @param K
	 */
	private int runRealGreedy(int K, double[] MemCost) {

		coveredCountBfSwap = coveredItemCount = status.getCoveredItemCount();

		IFeatureWrapper[] temp = new IFeatureWrapper[K];
		for (int i = 0; i < currentK; i++)
			temp[i] = this.topKSetIDs[i];
		this.topKSetIDs = temp;
		int resultIndex = currentK;
		long threshold = Long.MAX_VALUE;
		if (this.bucketCounter >= 0)
			threshold = input.getBucketThreshold(this.bucketCounter);

		long startT = System.currentTimeMillis();
		while (resultIndex < K) {
			if (this.setIterator == null || !this.setIterator.hasNext()) {
				if (this.bucketIterator.hasNext()) {
					IBucket oneBucket = this.bucketIterator.next();
					this.bucketCounter++;
					threshold = input.getBucketThreshold(this.bucketCounter);
					this.setIterator = oneBucket.iterator();
					continue; // next iteration
				} else
					break; // not anymore
			} else {
				ISet oneSet = this.setIterator.next();

				startT = System.currentTimeMillis();
				long gain = status.getUncoveredItemCount(oneSet);
				exclusiveExamTime += System.currentTimeMillis() - startT;
				if (gain == 0)
					continue;
				if (gain >= threshold) {
					this.topKSetIDs[resultIndex++] = oneSet.getFeature();
					// update the status
					startT = System.currentTimeMillis();
					this.status.updateCoverage(oneSet);
					statusUpdateTime += System.currentTimeMillis() - startT;
					this.coveredItemCount += gain;
				} else
					this.input.append(oneSet, gain);
			}
			double ratio = (double) (resultIndex - currentK)
					/ (double) (resultIndex - currentK + 1);
			MemCost[1] = MemCost[1] * ratio
					+ MemoryConsumptionCal.usedMemoryinMB()
					/ (resultIndex - currentK + 1);
		}
		return resultIndex;
	}

	@Override
	public long coveredItemsCount() {
		return this.coveredItemCount;
	}

	@Override
	public long realCoveredItemCount() {
		return this.status.getCoveredItemCount();
	}

	@Override
	public IFeatureWrapper[] getFixedSelected() {
		System.out
				.println("Not Supported in External_Bucket: getFixedSelected");
		return null;
	}

	@Override
	public long getEnumeratedPatternNum() {
		return input.getEnumeratedSetCount();
	}
}
