package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;
import java.util.HashSet;

import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus;
import edu.psu.chemxseer.structure.setcover.status.Status_BooleanArrayImpl;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp_Adv;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * A implementation of the greedy algorithm for max_k coverage problem IN each
 * iteration, the algorithm brute falsely scan all the sets and pick the one
 * with maximum marginal score If "K" features need to be selected, then "K"
 * sequential scan need to be applied
 * 
 * @author dayuyuan
 * 
 */
public class Greedy_Scan implements IMaxCoverSolver {

	private ICoverStatus status;
	private IInputSequential input;

	private int currentK;
	private IFeatureWrapper[] topKSetIDs;
	private HashSet<Integer> selectedSets;
	private long coveredItemCount;

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float findMinSetTime;
	protected long coveredCountBfSwap;

	protected double statusInitialSpace;

	public Greedy_Scan(IInputSequential input, StatusType sType, AppType aType) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		long beforeTime = System.currentTimeMillis();

		this.input = input;
		if (sType == StatusType.normal)
			this.status = Status_BooleanArrayImpl.newInstance(
					input.getQCount(), input.getGCount());
		else if (sType == StatusType.normalPreSelect)
			this.status = Status_BooleanArrayImpl.newInstance(
					input.getQCount(), input.getGCount(), input.getEdgeSets());

		else if (sType == StatusType.decompose)
			this.status = Status_Decomp.newInstance(input.getQCount(),
					input.getGCount(), aType);
		else if (sType == StatusType.decomposePreSelect)
			this.status = Status_Decomp_Adv.newInstance(input.getQCount(),
					input.getGCount(), aType, input.getEdgeSets());
		else
			throw new UnsupportedOperationException();
		currentK = 0;
		this.coveredItemCount = status.getCoveredItemCount();
		this.selectedSets = new HashSet<Integer>();
		// since the sequential input does not support the getSetCount()
		// function
		// topKSetIDs is initialize later when the greedy algorithm runs.

		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		statusInitialSpace = MemoryConsumptionCal.usedMemoryinMB() - beforeMem;
		System.out.println("Time for Greedy_Scan Construction: "
				+ (afterTime - beforeTime));
		System.out.println("Space for Greedy-Scan Construction: "
				+ statusInitialSpace);
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {
		MemoryConsumptionCal.runGC();
		double[] MemCost = new double[2];
		MemCost[0] = MemCost[1] = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		statusUpdateTime = exclusiveExamTime = findMinSetTime = 0;
		coveredCountBfSwap = 0;

		if (currentK < K) {
			currentK = this.runGreedy(K, MemCost);
		}
		long endTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double endMem = MemoryConsumptionCal.usedMemoryinMB();
		MemCost[1] = MemCost[1] - MemCost[0] + statusInitialSpace;
		MemCost[0] = endMem - MemCost[0] + statusInitialSpace;

		stat[0] = endTime - startTime;
		stat[1] = exclusiveExamTime;
		stat[2] = statusUpdateTime;
		stat[3] = findMinSetTime;
		stat[4] = input.getPatternEnumerationTime();
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
	 * @return total number of sets selected: may be smaller than K
	 * @param K
	 */
	private int runGreedy(int K, double[] MemCost) {
		coveredCountBfSwap = coveredItemCount = status.getCoveredItemCount();
		long startT = System.currentTimeMillis();
		for (int i = currentK; i < K; i++) {
			startT = System.currentTimeMillis();
			SetwithScore set = this.findMaxUnselectedSet();
			findMinSetTime += System.currentTimeMillis() - startT;

			if (set == null)
				return i;
			// else set!=0
			set.lazyDelete();
			topKSetIDs[i] = set.getFeature();
			// update the status
			this.coveredItemCount += set.getScore();
			this.selectedSets.add(set.getSetID());

			startT = System.currentTimeMillis();
			this.status.updateCoverage(set.getSet());
			statusUpdateTime += System.currentTimeMillis() - startT;

			double ratio = (double) (i - currentK)
					/ (double) (i - currentK + 1);
			MemCost[1] = MemCost[1] * ratio
					+ MemoryConsumptionCal.usedMemoryinMB()
					/ (i - currentK + 1);
		}
		return K;
	}

	/**
	 * Find the next available sets (unselected) covering the maximum number of
	 * uncovered items Return "null" if no set is available or all set have 0
	 * marginal gain.
	 * 
	 * @return
	 */
	private SetwithScore findMaxUnselectedSet() {
		long maxScore = 0;
		ISet result = null;
		if (this.topKSetIDs == null) { // with the initialization of the
										// topKSetIDs
			int count = 0;
			for (ISet set : this.input) {
				if (selectedSets.contains(set.getSetID()))
					continue;
				long score = status.getUncoveredItemCount(set);
				count++;
				if (score > maxScore) {
					maxScore = score;
					result = set;
				}
			}
			this.topKSetIDs = new IFeatureWrapper[count];
		} else {
			for (ISet set : this.input) {
				if (selectedSets.contains(set.getSetID()))
					continue;
				long score = status.getUncoveredItemCount(set);
				if (score > maxScore) {
					maxScore = score;
					result = set;
				}
			}
		}

		if (maxScore == 0)
			return null;
		else
			return new SetwithScore(result, -1, maxScore);
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
		ISet[] edgeSets = input.getEdgeSets();
		IFeatureWrapper[] edgeFeatures = new IFeatureWrapper[edgeSets.length];
		for (int i = 0; i < edgeSets.length; i++)
			edgeFeatures[i] = edgeSets[i].getFeature();
		return edgeFeatures;
	}

	@Override
	public long getEnumeratedPatternNum() {
		return input.getEnumeratedSetCount();
	}
}
