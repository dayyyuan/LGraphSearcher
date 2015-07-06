package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Adv;
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

/**
 * The Implementation of the Streaming-Set-Cover Algorithms Which use the
 * streaming model, assuming # of items are already known, sets comes one after
 * another When a new set comes from the stream, the algorithm measures the
 * contribution of the new set Decide whether to select it or not. If selected,
 * the selected set with least contribution is swapped out.
 * 
 * The float[] stat is not updated
 * 
 * @author dayuyuan
 * 
 */
@Deprecated
public class SwapAlgorithm_Old implements IMaxCoverSolver {
	// Here I use IInputSequential, which does not support early prunning of
	// branches
	private IInputSequential input;
	private ICoverStatus_Swap status;
	private double delta; // swap parameter
	private long coveredItemCount;

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float scoreUpdateTime;
	protected long coveredCountBfSwap;
	protected long patternEnumerated;
	protected int swapCount;

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
	/*
	 * public SwapAlgorithm(IInputSequential input, ICoverStatus_Swap status,
	 * double delta){ this.input = input; this.status = status; this.delta =
	 * delta; }
	 */

	public SwapAlgorithm_Old(IInputSequential input, StatusType sType,
			AppType aType) {
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
		this.delta = 1.0;
		this.coveredItemCount = 0;

		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("Time for SwapAlgorithm1 Construction: "
				+ (afterTime - beforeTime));
		System.out.println("Space for SwapAlgorithm1 Construction: "
				+ (afterMem - beforeMem));
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {
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
	 * Swap Criterion: (1) Benefit(newSet) = newly covered items = not included
	 * the common part of newSet and oldSet (2) Loss(oldSet) = items covered by
	 * oldSet only but not newSet or other selected set = not included the
	 * common part of newSet and oldSet Benefit(newSet) - Loss(oldSet) >
	 * Loss(oldSet) Swap if Benefit -Loss > Loss
	 * 
	 * @param K
	 * @param MemCost
	 * @return
	 */
	private IFeatureWrapper[] runGreedy(int K, double[] MemCost) {
		// 1. Scan the first K sets & select all of them
		long startT = System.currentTimeMillis();
		ISet[] selectedSet = new ISet[K];
		int counter = 0;
		coveredCountBfSwap = coveredItemCount = status.getCoveredItemCount();
		for (ISet oneSet : this.input) {
			if (oneSet == null)
				break;
			if (counter < K) {
				// select the set
				selectedSet[counter++] = oneSet;
				// update the status
				if (counter == K) {
					this.status.addToPos(selectedSet);
					this.coveredItemCount = status.getCoveredItemCount();
					System.out.println("Time for selecting first K sets: "
							+ (System.currentTimeMillis() - startT));
					startT = System.currentTimeMillis();

				}
			} else {
				// find the selected set with minimum marginal benefit
				SetwithScore minSet = this.findMinSet(selectedSet, oneSet);
				// calculate the swap threshold
				double threshold = 0;
				threshold = delta * minSet.getScore();

				// test whether the set should swap with minSet
				startT = System.currentTimeMillis();
				long gain = this.status.getUncoveredItemCount(oneSet);
				exclusiveExamTime += System.currentTimeMillis() - startT;

				if (gain - minSet.getScore() > threshold) {
					/*
					 * System.out.println("SwapOld: update new set " +
					 * oneSet.getSetID() + " with " + minSet.getPos() + "(" +
					 * minSet.getSetID() + "score " + minSet.getScore() + ")" +
					 * " for score " + gain);
					 */
					// do swap
					selectedSet[minSet.getPos()] = oneSet;

					startT = System.currentTimeMillis();
					this.status.removeFromPos(minSet);
					this.status.addToPos(oneSet, minSet.getPos());
					statusUpdateTime += System.currentTimeMillis() - startT;

					coveredItemCount += gain - minSet.getScore();
					swapCount++;
				}
			}
			MemCost[1] = MemCost[1]
					* (patternEnumerated / (patternEnumerated + 1))
					+ MemoryConsumptionCal.usedMemoryinMB()
					/ (patternEnumerated + 1);
			patternEnumerated++;
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

	public SetwithScore findMinSet(ISet[] selectedSet, ISet newSet) {
		long minScore = Long.MAX_VALUE;
		ISet minSet = null;
		int minPos = -1;

		for (int pos = 0; pos < selectedSet.length; pos++) {
			ISet oneSelectSet = selectedSet[pos];
			ISet reducedSet = new Set_Pair_Adv((Set_Pair) oneSelectSet,
					(Set_Pair) newSet);
			long score = this.status.getOnlyCoveredItemCount(reducedSet);
			if (score < minScore) {
				minSet = oneSelectSet;
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
