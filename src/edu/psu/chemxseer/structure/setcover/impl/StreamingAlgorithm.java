package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.psu.chemxseer.structure.setcover.IO.Input_StreamDec;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputStream;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.status.BranchBounds;
import edu.psu.chemxseer.structure.setcover.status.IBranchBound;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp_Adv;
import edu.psu.chemxseer.structure.setcover.status.Status_Opt;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * Major difference between the Streaming Algorithm & Swap Algorithm
 * implementation is that Streaming Algorithm takes a streaming input Branch &
 * Bound Techniques are used to reduce the search space
 * 
 * The algorithm contains both Algorithm one & two
 * 
 * @author dayuyuan
 * 
 */
public class StreamingAlgorithm implements IMaxCoverSolver {
	private IInputStream input; // the input stream returns only set_pair
	private ICoverStatus_Swap status; // use the decompose status only. other
										// status may not work on branch & bound
	private IBranchBound bbCal; // application dependent branch & bound
								// calculator
	private long coveredItemCount;
	// private IBranchBound bbCalNormal;
	private double lambda;

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float findMinSetTime;
	protected long coveredCountBfSwap;
	protected int swapCount;
	protected long patternEnumerated;

	protected double statusInitialSpace;

	/**
	 * Construct Stream Algorithm one
	 * 
	 * @param input
	 * @param aType
	 * @param bbCal
	 * @param criteria
	 */
	public StreamingAlgorithm(IInputStream input, StatusType sType,
			AppType aType, String bbCalFolder, double lambda) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		long beforeTime = System.currentTimeMillis();

		this.input = input;
		if (sType == StatusType.decompose) {
			this.status = Status_Decomp.newInstance(input.getQCount(),
					input.getGCount(), aType);
			this.bbCal = BranchBounds.getBBCal(aType, input.getQCount(),
					input.getGCount(), bbCalFolder);
		} else if (sType == StatusType.decomposeAdv) {
			this.status = Status_Opt.newInstance(
					Status_Decomp.newInstance(input.getQCount(),
							input.getGCount(), aType), aType);
			this.bbCal = BranchBounds.getBBCalAdv(aType, bbCalFolder, status);
		} else if (sType == StatusType.decomposePreSelect) {
			this.status = Status_Decomp_Adv.newInstance(input.getQCount(),
					input.getGCount(), aType, input.getEdgeSets());
			this.bbCal = BranchBounds.getBBCal(aType, input.getQCount(),
					input.getGCount(), bbCalFolder);
		} else if (sType == StatusType.decomposePreSelectAdv) {
			this.status = Status_Opt.newInstance(
					Status_Decomp_Adv.newInstance(input.getQCount(),
							input.getGCount(), aType, input.getEdgeSets()),
					aType);
			this.bbCal = BranchBounds.getBBCalAdv(aType, bbCalFolder, status);
		} else
			throw new UnsupportedOperationException();
		this.lambda = lambda;
		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		statusInitialSpace = MemoryConsumptionCal.usedMemoryinMB() - beforeMem;
		System.out.println("Time for StreamingAlgorithm Construction: "
				+ (afterTime - beforeTime));
		System.out.println("Space for StreamingAlgorithm Construction: "
				+ statusInitialSpace);
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {
		// before running the greedy algorithm, clean the status
		bbCal.clear();
		MemoryConsumptionCal.runGC();
		double[] MemCost = new double[2];
		MemCost[0] = MemCost[1] = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		statusUpdateTime = exclusiveExamTime = findMinSetTime = 0;
		swapCount = 0;
		patternEnumerated = 0;

		IFeatureWrapper[] result = this.runGreedy(K, MemCost);
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
	 * common part of newSet and oldSet CriteriaOne: Benefit(newSet) -
	 * Loss(oldSet) > Loss(oldSet) CriteriaTwo: Benefit(newSet) - Loss(oldSet) >
	 * 1/k coverage
	 * 
	 * @param K
	 * @param MemCost
	 * @return
	 */
	private IFeatureWrapper[] runGreedy(int K, double[] MemCost) {
		long startT = System.currentTimeMillis();
		// 1. Scan the first K sets & select all of them
		Set<String> selectedFeatures = new HashSet<String>();
		ISet[] selectedSet = new ISet[K];
		SetwithScore minSet = null;
		int counter = 0;
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
				if (counter == K) {
					// update the status
					status.addToPos(selectedSet);
					coveredItemCount = status.getCoveredItemCount();
					bbCal.initialize(status);
				}
			} else {
				// find the selected set with minimum marginal benefit
				if (minSet == null) {
					startT = System.currentTimeMillis();
					minSet = this.findMinSet(selectedSet);
					findMinSetTime += System.currentTimeMillis() - startT;

					startT = System.currentTimeMillis();
					this.status.removeFromPos(minSet);
					this.bbCal.updateAfterDelete(minSet.getSet(),
							status);
					statusUpdateTime += System.currentTimeMillis() - startT;
				}
				double threshold = lambda * minSet.getScore() + (1 - lambda)
						* ((double) this.coveredItemCount / (double) K);
				startT = System.currentTimeMillis();
				long gain = this.status.getUncoveredItemCount(oneSet);
				exclusiveExamTime += System.currentTimeMillis() - startT;

				if (gain - minSet.getScore() > threshold) {
					selectedSet[minSet.getPos()] = oneSet;
					startT = System.currentTimeMillis();
					this.status.addToPos(oneSet, minSet.getPos());
					// update the branchBound
					this.bbCal.updateAfterInsert(oneSet, status);
					statusUpdateTime += System.currentTimeMillis() - startT;

					coveredItemCount += gain - minSet.getScore();
					selectedFeatures.remove(minSet.getFeature().getFeature()
							.getDFSCode());
					selectedFeatures.add(oneSet.getFeature().getFeature()
							.getDFSCode());
					minSet = null; // need to search for minSet again in the
									// next iteration
					swapExists = true;
					swapCount++;
				} else {
					// branch & bound pruning
					boolean smaller = this.bbCal.isUppBoundSmaller(
							oneSet,
							(int) (minSet.getScore() + threshold));
					if (smaller) // prune the whole branch
						input.pruneBranch();
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
			statusUpdateTime = +System.currentTimeMillis() - startT;
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
