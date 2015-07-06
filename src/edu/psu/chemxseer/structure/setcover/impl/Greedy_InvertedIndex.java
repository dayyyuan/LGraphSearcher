package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;

import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputRandom;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus;
import edu.psu.chemxseer.structure.setcover.status.Status_BooleanArrayImpl;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp_Adv;
import edu.psu.chemxseer.structure.setcover.status.invertedindex.IInvertedIndex;
import edu.psu.chemxseer.structure.setcover.status.invertedindex.InvertedIndexArray;
import edu.psu.chemxseer.structure.setcover.status.invertedindex.InvertedIndexDecomp_Abstract;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * A Implementation of the Greedy algorithm for Max-K coverage problem. (1)
 * Inverted-index is used for fast process of the update (2) Instead of using
 * maxQueue, I use a simplified version of none-queue In each iteration of
 * feature selection, we (1) fetch and select the feature with maximum marginal
 * score from the max-heap (2) update the scores of other sets accordingly
 * 
 * @author dayuyuan
 */
public class Greedy_InvertedIndex implements IMaxCoverSolver {

	private ICoverStatus status; // denote which item has been covered & which
									// is not
	private IInputRandom input; // since we need to fetch a set given its ID,
								// the input has to support random access
	private IInvertedIndex invertedIndex;

	private int currentK;
	private IFeatureWrapper[] topKSetIDs;
	private long coveredItemCount;

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float scoreUpdateTime;
	protected long coveredCountBfSwap;

	protected double statusInitialSpace;

	public Greedy_InvertedIndex(IInputRandom input, StatusType sType,
			AppType aType) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		this.input = input;
		// Construct the inverted index
		if (sType == StatusType.normal) {
			this.invertedIndex = InvertedIndexArray.newInstance(input);
			this.status = Status_BooleanArrayImpl.newInstance(
					input.getQCount(), input.getGCount());
		} else if (sType == StatusType.normalPreSelect) {
			this.invertedIndex = InvertedIndexArray.newInstance(input);
			this.status = Status_BooleanArrayImpl.newInstance(
					input.getQCount(), input.getGCount(), input.getEdgeSets());
		} else if (sType == StatusType.decompose) {
			this.invertedIndex = InvertedIndexDecomp_Abstract.newInstance(
					input, aType);
			this.status = Status_Decomp.newInstance(input.getQCount(),
					input.getGCount(), aType);
		} else if (sType == StatusType.decomposePreSelect) {
			this.invertedIndex = InvertedIndexDecomp_Abstract.newInstance(
					input, aType);
			this.status = Status_Decomp_Adv.newInstance(input.getQCount(),
					input.getGCount(), aType, input.getEdgeSets());
		} else
			throw new UnsupportedOperationException(); // does not supprot adv
														// status

		// Initialize the score
		if (sType == StatusType.normal || sType == StatusType.decompose) {
			for (ISet set : input) {
				SetwithScore scoreSet = input.swapToScoreSet(set);
				;
				scoreSet.setScore(scoreSet.size());
			}
		} else {
			for (ISet set : input) {
				SetwithScore scoreSet = input.swapToScoreSet(set);
				;
				scoreSet.setScore(this.status.getUncoveredItemCount(set));
			}
		}
		// Construct the maxQueue
		currentK = 0;
		topKSetIDs = new IFeatureWrapper[input.getSetCount()];
		this.coveredItemCount = status.getCoveredItemCount();

		MemoryConsumptionCal.runGC();
		statusInitialSpace = MemoryConsumptionCal.usedMemoryinMB() - beforeMem;
		/*
		 * System.out.println("Time for Greedy_InvertedIndex Construction: " +
		 * (afterTime-beforeTime));
		 * System.out.println("Space for Greedy-InvertedIndex Construction: " +
		 * (afterMem-beforeMem));
		 */
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] state) {
		return this.runGreedy(K, 0, state);
	}

	public IFeatureWrapper[] runGreedy(int K, int minScore, float[] stat) {
		MemoryConsumptionCal.runGC();
		double[] MemCost = new double[2];
		MemCost[1] = MemCost[0] = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();

		statusUpdateTime = exclusiveExamTime = scoreUpdateTime = 0;
		coveredCountBfSwap = 0;
		if (currentK < K) {
			currentK = this.runRealGreedy(K, minScore, MemCost);
		}
		long endTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double endMem = MemoryConsumptionCal.usedMemoryinMB();
		MemCost[1] = MemCost[1] - MemCost[0] + statusInitialSpace;
		MemCost[0] = endMem - MemCost[0] + statusInitialSpace;

		stat[0] = endTime - startTime;
		stat[1] = exclusiveExamTime;
		stat[2] = statusUpdateTime;
		stat[3] = scoreUpdateTime;
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
	 * @param minScore
	 *            : the set should have value > minScore
	 */
	private int runRealGreedy(int K, int minScore, double[] MemCost) {

		coveredCountBfSwap = coveredItemCount = status.getCoveredItemCount();
		long startT = 0;
		for (int i = currentK; i < K; i++) {
			startT = System.currentTimeMillis();
			SetwithScore set = this.findMaxUncovered();
			scoreUpdateTime += System.currentTimeMillis() - startT;

			if (set == null || set.getScore() < minScore)
				return i;
			// else set!=0
			set.lazyDelete(); // the lazy Delete only effective given the
								// in-memory input
			topKSetIDs[i] = set.getFeature();
			// update scores of other indexes
			// 1. Find the newly covered items
			startT = System.currentTimeMillis();
			ISet it = this.status.updateCoverageWithSetReturn(set.getSet());
			statusUpdateTime += System.currentTimeMillis() - startT;

			this.coveredItemCount += set.getScore();
			// 2. For each Item in the set,
			// through the inverted index, find the candidate "set"
			// containing the item & update their score on the maxHeap (decrease
			// by 1)
			startT = System.currentTimeMillis();
			for (int[] item : it) {
				int[] sIDs = this.invertedIndex.getCoveredSetPosIDs(item[0],
						item[1], -1);
				for (int setID : sIDs) {
					SetwithScore theSet = (SetwithScore) this.input
							.getSet(setID);
					if (theSet != null && !theSet.isDeleted()) {
						theSet.setScore(theSet.getScore() - 1);
					}
				}
			}
			scoreUpdateTime += System.currentTimeMillis() - startT;
			double ratio = (double) (i - currentK)
					/ (double) (i - currentK + 1);
			MemCost[1] = MemCost[1] * ratio
					+ MemoryConsumptionCal.usedMemoryinMB()
					/ (i - currentK + 1);
		}
		System.out.println("Time for status update: " + statusUpdateTime
				+ " Time for score update: " + scoreUpdateTime);
		return K;
	}

	private SetwithScore findMaxUncovered() {
		long maxScore = Long.MIN_VALUE;
		SetwithScore result = null;
		for (ISet set : input) {
			SetwithScore theSet = (SetwithScore) set;
			if (set.isDeleted())
				continue;
			if (theSet.getScore() > maxScore) {
				maxScore = theSet.getScore();
				result = theSet;
			}
		}
		return result;
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