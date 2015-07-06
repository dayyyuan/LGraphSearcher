package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.psu.chemxseer.structure.setcover.IO.Input_StreamDec;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp_Adv;
import edu.psu.chemxseer.structure.setcover.status.Status_IntArrayImpl;
import edu.psu.chemxseer.structure.setcover.status.Status_Opt;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class SwapAlgorithm implements IMaxCoverSolver {
	// Here I use IInputSequential, which does not support early prunning of
	// branches
	protected IInputSequential input;
	protected ICoverStatus_Swap status;
	protected long coveredItemCount;
	protected double lambda;

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float findMinSetTime;
	protected long coveredCountBfSwap;
	protected int swapCount;
	protected long patternEnumerated;

	/**
	 * Construct a Stream Max-Coverage Solver
	 * 
	 * @param input
	 *            : Input_DFSStream, the stream input
	 * @param delta
	 *            : control the swap criterion
	 * @param branchBound
	 *            : can be null if no branch& bound is needed
	 * @param type
	 *            : type = 0 [B_1 swap], type = 1 [T_current swap]
	 */
	public SwapAlgorithm(IInputSequential input, StatusType sType,
			AppType aType, double lambda) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		long beforeTime = System.currentTimeMillis();

		this.input = input;
		if (sType == StatusType.normal)
			this.status = Status_IntArrayImpl.newInstance(input.getQCount(),
					input.getGCount());
		else if (sType == StatusType.normalAdv)
			this.status = Status_Opt.newInstance(
					Status_IntArrayImpl.newInstance(input.getQCount(),
							input.getGCount()), aType);
		else if (sType == StatusType.normalPreSelect)
			this.status = Status_IntArrayImpl.newInstance(input.getQCount(),
					input.getGCount(), input.getEdgeSets());
		else if (sType == StatusType.normalPreSelectAdv)
			this.status = Status_Opt.newInstance(
					Status_IntArrayImpl.newInstance(input.getQCount(),
							input.getGCount(), input.getEdgeSets()), aType);

		else if (sType == StatusType.decompose)
			this.status = Status_Decomp.newInstance(input.getQCount(),
					input.getGCount(), aType);
		else if (sType == StatusType.decomposeAdv)
			this.status = Status_Opt.newInstance(
					Status_Decomp.newInstance(input.getQCount(),
							input.getGCount(), aType), aType);
		else if (sType == StatusType.decomposePreSelect)
			this.status = Status_Decomp_Adv.newInstance(input.getQCount(),
					input.getGCount(), aType, input.getEdgeSets());
		else if (sType == StatusType.decomposePreSelectAdv)
			this.status = Status_Opt.newInstance(
					Status_Decomp_Adv.newInstance(input.getQCount(),
							input.getGCount(), aType, input.getEdgeSets()),
					aType);
		this.coveredItemCount = status.getCoveredItemCount();
		this.lambda = lambda;

		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("Time for SwapAlgorithm1_modified Construction: "
				+ (afterTime - beforeTime));
		System.out.println("Space for SwapAlgorithm_modified Construction: "
				+ (afterMem - beforeMem));
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {

		MemoryConsumptionCal.runGC();
		double[] MemCost = new double[2];
		MemCost[0] = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		statusUpdateTime = exclusiveExamTime = findMinSetTime = 0;
		coveredCountBfSwap = swapCount = 0;
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
		stat[3] = findMinSetTime;
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
		// 1. Scan the first K sets & select all of them
		ISet[] selectedSet = new ISet[K];
		Set<String> selectedFeatures = new HashSet<String>();
		int counter = 0;
		SetwithScore minSet = null;
		boolean scoreInitialized = false;
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
				// select the set
				selectedSet[counter++] = oneSet;
				selectedFeatures.add(oneSet.getFeature().getFeature()
						.getDFSCode());
				swapExists = true;
			} else {
				if (!scoreInitialized) {
					// update the status
					this.status.addToPos(selectedSet);
					this.coveredItemCount = status.getCoveredItemCount();
					scoreInitialized = true;
				}
				// find the selected set with minimum marginal benefit
				if (minSet == null) {
					startT = System.currentTimeMillis();
					minSet = this.findMinSet(selectedSet);
					findMinSetTime += System.currentTimeMillis() - startT;

					startT = System.currentTimeMillis();
					this.status.removeFromPos(minSet);
					statusUpdateTime += System.currentTimeMillis() - startT;
				}
				// calculate the swap threshold
				// test whether the set should swap with minSet
				double threshold = lambda * minSet.getScore() + (1 - lambda)
						* ((double) this.coveredItemCount / (double) K);
				startT = System.currentTimeMillis();
				long gain = this.status.getUncoveredItemCount(oneSet);
				exclusiveExamTime += System.currentTimeMillis() - startT;

				if (gain - minSet.getScore() > threshold) {
					selectedSet[minSet.getPos()] = oneSet;
					startT = System.currentTimeMillis();
					this.status.addToPos(oneSet, minSet.getPos());
					statusUpdateTime += System.currentTimeMillis() - startT;

					coveredItemCount += gain - minSet.getScore();
					selectedFeatures.remove(minSet.getFeature().getFeature()
							.getDFSCode());
					selectedFeatures.add(oneSet.getFeature().getFeature()
							.getDFSCode());
					minSet = null; // need to search for minSet agin in the next
									// iteration
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
			this.status.addToPos(minSet.getSet(), minSet.getPos());
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
		return result;
	}

	private SetwithScore findMinSet(ISet[] selectedSet) {
		long minScore = Long.MAX_VALUE;
		ISet minSet = null;
		int minPos = -1;

		for (int pos = 0; pos < selectedSet.length; pos++) {
			ISet oneSet = selectedSet[pos];
			long score = this.status.getOnlyCoveredItemCount(oneSet);
			if (score < minScore) {
				minSet = oneSet;
				minScore = score;
				minPos = pos;
			}
		}
		return new SetwithScore(minSet, minPos, minScore);
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
