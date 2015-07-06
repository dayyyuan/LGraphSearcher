package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.psu.chemxseer.structure.setcover.IO.Input_StreamDec;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap_InvertedIndex;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp_Adv;
import edu.psu.chemxseer.structure.setcover.status.Status_InvertedIndexArray;
import edu.psu.chemxseer.structure.setcover.status.Status_Opt;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class SwapAlgorithm_InvertedIndex implements IMaxCoverSolver {
	protected IInputSequential input;
	protected ICoverStatus_Swap_InvertedIndex status;
	protected long coveredItemCount;
	protected double lambda; // the parameter for threshold

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float scoreUpdateTime;
	protected long coveredCountBfSwap;
	protected long patternEnumerated;
	protected int swapCount;

	public SwapAlgorithm_InvertedIndex(IInputSequential input,
			StatusType sType, AppType aType, double lambda) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		long beforeTime = System.currentTimeMillis();

		this.input = input;
		if (sType == StatusType.normal)
			status = Status_InvertedIndexArray.newInstance(input.getQCount(),
					input.getGCount());
		else if (sType == StatusType.normalAdv)
			status = Status_Opt.newInstance(Status_InvertedIndexArray
					.newInstance(input.getQCount(), input.getGCount()), aType);
		else if (sType == StatusType.normalPreSelect)
			status = Status_InvertedIndexArray.newInstance(input.getQCount(),
					input.getGCount(), input.getEdgeSets());
		else if (sType == StatusType.normalPreSelectAdv)
			status = Status_Opt.newInstance(Status_InvertedIndexArray
					.newInstance(input.getQCount(), input.getGCount(),
							input.getEdgeSets()), aType);
		else if (sType == StatusType.decompose)
			status = Status_Decomp.newInstance(input.getQCount(),
					input.getGCount(), aType);
		else if (sType == StatusType.decomposeAdv)
			status = Status_Opt.newInstance(
					Status_Decomp.newInstance(input.getQCount(),
							input.getGCount(), aType), aType);
		else if (sType == StatusType.decomposePreSelect)
			status = Status_Decomp_Adv.newInstance(input.getQCount(),
					input.getGCount(), aType, input.getEdgeSets());
		else if (sType == StatusType.decomposePreSelectAdv)
			status = Status_Opt.newInstance(
					this.status = Status_Decomp_Adv.newInstance(
							input.getQCount(), input.getGCount(), aType,
							input.getEdgeSets()), aType);

		this.coveredItemCount = status.getCoveredItemCount();
		this.lambda = lambda;

		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out
				.println("Time for SwapAlgorithm1_InvertedIndex Construction: "
						+ (afterTime - beforeTime));
		System.out
				.println("Space for SwapAlgorithm1_InvertedIndex Construction: "
						+ (afterMem - beforeMem));
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {
		// before running the greedy algorithm, clean the status
		MemoryConsumptionCal.runGC();
		double[] MemCost = new double[2];
		MemCost[0] = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		statusUpdateTime = exclusiveExamTime = scoreUpdateTime = 0;
		coveredCountBfSwap = coveredItemCount = 0;
		swapCount = 0;
		patternEnumerated = 0;
		IFeatureWrapper[] result = this.runGreedy(K, MemCost);

		long endTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double endMem = MemoryConsumptionCal.usedMemoryinMB();
		MemCost[1] -= MemCost[0];
		MemCost[0] = endMem - MemCost[0];
		stat[0] = endTime - startTime;
		stat[1] = exclusiveExamTime;
		stat[2] = statusUpdateTime;
		stat[3] = scoreUpdateTime;
		stat[4] = input.getPatternEnumerationTime();
		stat[5] = (float) MemCost[0];
		stat[6] = (float) MemCost[1];
		stat[7] = patternEnumerated;
		stat[8] = swapCount;
		stat[9] = coveredCountBfSwap;
		stat[10] = coveredItemCount;

		return result;
	}

	/**
	 * Swap Criterion: (1) Benefit(newSet) = newly covered items + included the
	 * common part of newSet and oldSet (2) Loss(oldSet) = items covered by only
	 * oldSet = included common part of newSet and oldSet Swap if Benefit -Loss
	 * > Loss
	 * 
	 * @param K
	 * @param MemCost
	 * @return
	 */
	private IFeatureWrapper[] runGreedy(int K, double[] MemCost) {
		long startT = System.currentTimeMillis();
		Set<String> selectedFeatures = new HashSet<String>();
		// 1. Scan the first K sets & select all of them
		SetwithScore[] selectedSet = new SetwithScore[K];
		boolean scoreInitialized = false;
		int counter = 0;
		SetwithScore minSet = null;
		boolean swapExists = false;

		coveredCountBfSwap = coveredItemCount = status.getCoveredItemCount();
		for (Iterator<ISet> it = input.iterator(); it.hasNext(); patternEnumerated++) {
			ISet oneSet = it.next();
			if (oneSet == null) {
				if (input instanceof Input_StreamDec && swapExists) {
					boolean decreased = ((Input_StreamDec) input)
							.decreaseMinSup(it);
					if (decreased) {
						swapExists = false; // new round.
						continue;
					} else
						break;
				} else
					break;
			} else if (selectedFeatures.contains(oneSet.getFeature()
					.getFeature().getDFSCode()))
				continue;
			if (counter < K) {
				selectedSet[counter] = new SetwithScore(oneSet, counter, -1);
				counter++;
				selectedFeatures.add(oneSet.getFeature().getFeature()
						.getDFSCode());
				swapExists = true;
			} else {
				if (!scoreInitialized) {
					this.status.addToPos(selectedSet);
					this.coveredItemCount = this.status.getCoveredItemCount();
					System.out.println("Time for selecting first K sets: "
							+ (System.currentTimeMillis() - startT));
					startT = System.currentTimeMillis();
					initializeScore(selectedSet);
					scoreInitialized = true;
					System.out.println("Time for initializing: "
							+ (System.currentTimeMillis() - startT));
				}
				// find the selected set with minimum marginal benefit
				if (minSet == null) {
					minSet = this.findMinSet(selectedSet);
					// (1) First remove the minSet
					startT = System.currentTimeMillis();
					this.status.removeFromPos(minSet);
					statusUpdateTime += System.currentTimeMillis() - startT;
					startT = System.currentTimeMillis();
					this.updateScoreAfterRemove(selectedSet, minSet);
					scoreUpdateTime += System.currentTimeMillis() - startT;
				}
				// calculate the swap threshold
				// test whether the set should swap with minSet
				startT = System.currentTimeMillis();
				long gain = this.status.getUncoveredItemCount(oneSet);
				exclusiveExamTime += System.currentTimeMillis() - startT;
				// System.out.println("SetID: " + oneSet.getSetID() +
				// " newGain: " + gain);
				double threshold = lambda * minSet.getScore() + (1 - lambda)
						* ((double) this.coveredItemCount / (double) K);
				if (gain - minSet.getScore() > threshold) {
					SetwithScore newSet = new SetwithScore(oneSet,
							minSet.getPos(), gain);
					selectedSet[minSet.getPos()] = newSet;
					startT = System.currentTimeMillis();
					updateScoreBeforeInsert(selectedSet, newSet);
					scoreUpdateTime += System.currentTimeMillis() - startT;
					startT = System.currentTimeMillis();
					status.addToPos(oneSet, minSet.getPos());
					statusUpdateTime += System.currentTimeMillis() - startT;
					// Differ Two: update the scores for other set except for
					// the oneSet
					coveredItemCount += gain - minSet.getScore();

					selectedFeatures.remove(minSet.getFeature().getFeature()
							.getDFSCode());
					selectedFeatures.add(oneSet.getFeature().getFeature()
							.getDFSCode());
					minSet = null; // need to search for minSet again in the
									// next iteration
					swapExists = true;
					swapCount++;
				}
			}
			MemCost[1] = MemCost[1]
					* (patternEnumerated / (patternEnumerated + 1))
					+ MemoryConsumptionCal.usedMemoryinMB()
					/ (patternEnumerated + 1);
		}
		if (minSet != null) {
			startT = System.currentTimeMillis();
			this.status.addToPos(minSet.getSet(), minSet.getPos());
			statusUpdateTime += System.currentTimeMillis() - startT;
		}

		IFeatureWrapper[] result = new IFeatureWrapper[K];
		int index = 0;
		for (; index < K; index++) {
			if (selectedSet[index] != null)
				result[index] = selectedSet[index].getFeature();
			else
				break;
		}
		result = Arrays.copyOfRange(result, 0, index);
		System.out.println("Time for Exclusive Exam: " + exclusiveExamTime
				+ " Time for Status Update: " + statusUpdateTime
				+ " Time for Score Update: " + scoreUpdateTime
				+ " with Total Swaps: " + swapCount);
		return result;
	}

	private void updateScoreAfterRemove(SetwithScore[] selectedSet,
			SetwithScore minSet) {
		// by removing minSet, some items of minSet will be covered only once
		Map<Integer, Integer> setIDValue = this.status
				.getOnlyCoveredSetPosIDs(minSet.getSet());

		for (Entry<Integer, Integer> entry : setIDValue.entrySet()) {
			// covered by one time except for minSet
			SetwithScore theSet = selectedSet[entry.getKey()]; // covered by
																// only one
																// others
			theSet.setScore(theSet.getScore() + entry.getValue());
		}
	}

	private void updateScoreBeforeInsert(SetwithScore[] selectedSet,
			SetwithScore newSet) {
		Map<Integer, Integer> setIDValue = this.status
				.getOnlyCoveredSetPosIDs(newSet.getSet());
		for (Entry<Integer, Integer> entry : setIDValue.entrySet()) {
			SetwithScore theSet = selectedSet[entry.getKey()];
			theSet.setScore(theSet.getScore() - entry.getValue());
		}
	}

	/**
	 * Calculate the scores of the selectedSet & set them to each set
	 * 
	 * @param selectedSet
	 */
	private void initializeScore(SetwithScore[] selectedSet) {
		for (SetwithScore oneSet : selectedSet)
			oneSet.setScore(status.getOnlyCoveredItemCount(oneSet.getSet()));
	}

	/**
	 * Return the set with least score: very light weighted due to the fact that
	 * the scores for each set is already known
	 * 
	 * @param selectedSet
	 * @return
	 */
	private SetwithScore findMinSet(SetwithScore[] selectedSet) {
		SetwithScore result = null;
		long minScore = Long.MAX_VALUE;
		for (SetwithScore oneSet : selectedSet) {
			if (oneSet.getScore() < minScore) {
				result = oneSet;
				minScore = result.getScore();
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
