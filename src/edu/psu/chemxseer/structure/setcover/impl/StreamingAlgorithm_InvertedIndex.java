package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap_InvertedIndex;
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
public class StreamingAlgorithm_InvertedIndex implements IMaxCoverSolver {
	protected IInputStream input; // the input stream returns only set_pair
	protected ICoverStatus_Swap_InvertedIndex status; // use the decompose
														// status only. other
														// status may not work
														// on branch & bound
	protected IBranchBound bbCal; // application dependent branch & bound
									// calculator
	protected long coveredItemCount;
	protected double lambda; // for interpolating the swap criteria
	protected SetwithScore[] preSelectedFeatures; // the set of preselected
													// features
	protected ISet[] fixSelectedFeatures;

	protected float exclusiveExamTime;
	protected float statusUpdateTime;
	protected float scoreUpdateTime;
	protected long coveredCountBfSwap;
	protected int swapCount;
	protected long patternEnumerated;

	public double statusInitialSpace;

	/**
	 * Streaming Input & Pair-wise set type
	 * 
	 * @param input
	 * @param aType
	 */
	public StreamingAlgorithm_InvertedIndex(IInputStream input,
			StatusType sType, AppType aType, String bbCalFolder, double lambda) {
		MemoryConsumptionCal.runGC();
		long beforeTime = System.currentTimeMillis();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		this.input = input;
		this.fixSelectedFeatures = input.getEdgeSets();
		if (sType == StatusType.decompose) {
			status = Status_Decomp.newInstance(input.getQCount(),
					input.getGCount(), aType);
			this.bbCal = BranchBounds.getBBCal(aType, status.getQCount(),
					status.getGCount(), bbCalFolder);
		} else if (sType == StatusType.decomposeAdv) {
			status = Status_Opt.newInstance(
					Status_Decomp.newInstance(input.getQCount(),
							input.getGCount(), aType), aType);
			this.bbCal = BranchBounds.getBBCalAdv(aType, bbCalFolder, status);
			// this.bbCal = ExperimentTwo.getBBCal(aType, status.getQCount(),
			// status.getGCount(), bbCalFolder);
		} else if (sType == StatusType.decomposePreSelect) {
			status = Status_Decomp_Adv.newInstance(input.getQCount(),
					input.getGCount(), aType, fixSelectedFeatures);
			this.bbCal = BranchBounds.getBBCal(aType, status.getQCount(),
					status.getGCount(), bbCalFolder);
		} else if (sType == StatusType.decomposePreSelectAdv) {
			status = Status_Opt.newInstance(
					Status_Decomp_Adv.newInstance(input.getQCount(),
							input.getGCount(), aType, fixSelectedFeatures),
					aType);
			this.bbCal = BranchBounds.getBBCalAdv(aType, bbCalFolder, status);
			// this.bbCal = ExperimentTwo.getBBCal(aType, status.getQCount(),
			// status.getGCount(), bbCalFolder);
		} else
			System.out
					.println("Exception: StreamingAlgorithm does not support single input");

		this.coveredItemCount = status.getCoveredItemCount();
		this.lambda = lambda;
		this.preSelectedFeatures = null;
		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		statusInitialSpace = MemoryConsumptionCal.usedMemoryinMB() - beforeMem;
		System.out.println("Time for StreamingAlgorithm Construction: "
				+ (afterTime - beforeTime));
		System.out.println("Space for StreamingAlgorithm Construction: "
				+ statusInitialSpace);
	}

	protected StreamingAlgorithm_InvertedIndex(IInputStream input,
			ICoverStatus_Swap_InvertedIndex status, IBranchBound bbCal,
			double lambda, double statusInitialSpace) {
		this.input = input;
		this.status = status;
		this.bbCal = bbCal;
		this.coveredItemCount = status.getCoveredItemCount();
		this.lambda = lambda;
		this.fixSelectedFeatures = input.getEdgeSets();
		this.preSelectedFeatures = null;

		this.statusInitialSpace = statusInitialSpace;
	}

	/**
	 * Copy Constructor
	 * 
	 * @param input
	 * @param preAlgorithm
	 */
	public StreamingAlgorithm_InvertedIndex(IInputStream input,
			StreamingAlgorithm_InvertedIndex preAlgorithm) {
		// No Extra Cost of the This InvertedIndex
		this.input = input;
		this.status = preAlgorithm.status;
		this.coveredItemCount = preAlgorithm.coveredItemCount;
		this.bbCal = preAlgorithm.bbCal;
		this.lambda = preAlgorithm.lambda;
		this.preSelectedFeatures = preAlgorithm.preSelectedFeatures;
		this.fixSelectedFeatures = preAlgorithm.fixSelectedFeatures;
		this.statusInitialSpace = 0;
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {
		// bbCal.clear();
		MemoryConsumptionCal.runGC();
		double[] MemCost = new double[2];
		MemCost[0] = MemCost[1] = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		statusUpdateTime = exclusiveExamTime = scoreUpdateTime = 0;
		coveredCountBfSwap = swapCount = 0;
		patternEnumerated = 0;

		IFeatureWrapper[] result = this.runGreedy(K, MemCost);

		long endTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double endMem = MemoryConsumptionCal.usedMemoryinMB();
		MemCost[1] = MemCost[1] - MemCost[0] + this.statusInitialSpace;
		MemCost[0] = endMem - MemCost[0] + this.statusInitialSpace;

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
		SetwithScore[] selectedSet = new SetwithScore[K];
		int counter = 0;
		boolean scoreInitialized = false;
		if (this.preSelectedFeatures != null && preSelectedFeatures.length == K) {
			for (int w = 0; w < K; w++)
				selectedSet[w] = preSelectedFeatures[w];
			counter = K;
			scoreInitialized = true;
			coveredCountBfSwap = coveredItemCount = status
					.getCoveredItemCount();
		}
		boolean swapExists = false;
		SetwithScore minSet = null;
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
				selectedSet[counter] = new SetwithScore(oneSet, counter, -1);
				counter++;
				selectedFeatures.add(oneSet.getFeature().getFeature()
						.getDFSCode());
				swapExists = true;
			} else {
				if (!scoreInitialized) {
					// update the status
					try {
						status.addToPos(selectedSet);
					} catch (OutOfMemoryError E) {
						System.out.println("Out of Memory: "
								+ java.lang.Runtime.getRuntime().maxMemory());
					}
					coveredItemCount = this.status.getCoveredItemCount();
					System.out.println("Time for selecting first K sets: "
							+ (System.currentTimeMillis() - startT));
					startT = System.currentTimeMillis();
					initializeScore(selectedSet);
					scoreInitialized = true;
					bbCal.initialize(status);
					System.out.println("Time for initializing: "
							+ (System.currentTimeMillis() - startT));
				}
				// find the selected set with minimum marginal benefit
				if (minSet == null) {
					minSet = this.findMinSet(selectedSet);
					// System.out.println(minSet.getScore());
					// (1) First remove the minSet
					startT = System.currentTimeMillis();
					this.status.removeFromPos(minSet);
					statusUpdateTime += System.currentTimeMillis() - startT;
					startT = System.currentTimeMillis();
					this.updateScoreAfterRemove(selectedSet, minSet);
					this.bbCal.updateAfterDelete(minSet.getSet(),
							status);
					scoreUpdateTime += System.currentTimeMillis() - startT;
				}
				// calculate the swap threshold
				// test whether the set should swap with minSet
				double threshold = lambda * minSet.getScore() + (1 - lambda)
						* ((double) this.coveredItemCount / (double) K);
				startT = System.currentTimeMillis();
				long gain = this.status.getUncoveredItemCount(oneSet);
				exclusiveExamTime += System.currentTimeMillis() - startT;

				if (gain > minSet.getScore() + threshold) {
					SetwithScore newSet = new SetwithScore(oneSet,
							minSet.getPos(), gain);
					selectedSet[minSet.getPos()] = newSet;
					startT = System.currentTimeMillis();
					this.updateScoreBeforeInsert(selectedSet, newSet);
					scoreUpdateTime += System.currentTimeMillis() - startT;
					startT = System.currentTimeMillis();
					this.status.addToPos(oneSet, minSet.getPos());
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
					// branch & bound
					boolean smaller = this.bbCal.isUppBoundSmaller(
							oneSet, minSet.getScore() + threshold);
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
			statusUpdateTime += System.currentTimeMillis() - startT;
		}

		this.preSelectedFeatures = selectedSet.clone();
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

	/**
	 * Given the newly inserted set newSet, it will cover some items that are
	 * covered by other sets we need to update the score of those sets
	 * correspondingly.
	 * 
	 * @param newSet
	 */

	protected void updateScoreAfterRemove(SetwithScore[] selectedSet,
			SetwithScore minSet) {
		// by removing minSet, some items of minSet will be covered only once
		Map<Integer, Integer> setIDValue = this.status
				.getOnlyCoveredSetPosIDs(minSet.getSet());
		for (Entry<Integer, Integer> entry : setIDValue.entrySet()) {
			// covered by =2 or =1 times, since can not be covered < 1 times
			SetwithScore theSet = selectedSet[entry.getKey()]; // covered by
																// only one
																// others
			theSet.setScore(theSet.getScore() + entry.getValue());
		}
	}

	protected void updateScoreBeforeInsert(SetwithScore[] selectedSet,
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
	protected void initializeScore(SetwithScore[] selectedSet) {
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
	protected SetwithScore findMinSet(SetwithScore[] selectedSet) {
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
		IFeatureWrapper[] edgeFeatures = new IFeatureWrapper[fixSelectedFeatures.length];
		for (int i = 0; i < fixSelectedFeatures.length; i++)
			edgeFeatures[i] = fixSelectedFeatures[i].getFeature();
		return edgeFeatures;
	}

	@Override
	public long getEnumeratedPatternNum() {
		return input.getEnumeratedSetCount();
	}
}
