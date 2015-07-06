package edu.psu.chemxseer.structure.setcover.impl;

import java.util.Arrays;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputStream;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.status.IBranchBound;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * Similar to the Greedy_Scan, except Direct_Iterative use a streaming input,
 * with "prune" branch available
 * 
 * @author dayuyuan
 * 
 */
public class Direct_Iterative implements IMaxCoverSolver {
	private ICoverStatus_Swap status;
	private IInputStream input;
	private IBranchBound bbCal;
	private int currentK;
	private IFeatureWrapper[] topKFeature;
	private int coveredItemCount;

	private float exclusiveExamTime;
	private float statusUpdateTime;
	private int enumeratedPatternCount;

	public Direct_Iterative(IInputStream input, AppType aType,
			IBranchBound bbCal) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		long beforeTime = System.currentTimeMillis();
		this.input = input;
		this.status = Status_Decomp.newInstance(input.getQCount(),
				input.getGCount(), aType);
		this.currentK = 0;
		this.topKFeature = new IFeatureWrapper[0];
		this.coveredItemCount = 0;
		this.bbCal = bbCal;
		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("Time for Greedy_Scan Construction: "
				+ (afterTime - beforeTime));
		System.out.println("Space for Greedy-Scan Construction: "
				+ (afterMem - beforeMem));
	}

	@Override
	public IFeatureWrapper[] runGreedy(int K, float[] stat) {
		MemoryConsumptionCal.runGC();
		double memStart = MemoryConsumptionCal.usedMemoryinMB();
		double[] MemCost = new double[1];
		long startTime = System.currentTimeMillis();
		exclusiveExamTime = 0;
		statusUpdateTime = 0;
		enumeratedPatternCount = 0;

		if (currentK < K) {
			topKFeature = Arrays.copyOf(topKFeature, K);
			currentK = this.runGreedy(K, MemCost);
		}
		long endTime = System.currentTimeMillis();
		long timeCost = endTime - startTime;
		MemoryConsumptionCal.runGC();
		double endMem = MemoryConsumptionCal.usedMemoryinMB();
		double avgMemCost = MemCost[0] - memStart;
		double afterMemCost = endMem - memStart;
		stat[0] = timeCost;
		stat[1] = this.exclusiveExamTime;
		stat[2] = this.statusUpdateTime;
		stat[3] = 0; // because no such statistics
		stat[4] = input.getPatternEnumerationTime();
		stat[5] = (float) afterMemCost;
		stat[6] = (float) avgMemCost;
		stat[7] = this.enumeratedPatternCount;
		stat[8] = 0; // no swaps
		stat[9] = 0;
		stat[10] = this.coveredItemCount;
		return Arrays.copyOf(topKFeature, currentK);
	}

	/**
	 * Run the Greedy algorithms: Assuming currentK sets are already selected,
	 * Need to find K sets in total
	 * 
	 * @return total number of sets selected: may be smaller than K
	 * @param K
	 */
	private int runGreedy(int K, double[] MemCost) {
		for (int i = currentK; i < K; i++) {
			long startExam = System.currentTimeMillis();
			SetwithScore set = this.findMaxUnselectedSet();
			long endExam = System.currentTimeMillis();
			this.exclusiveExamTime += endExam - startExam;
			if (set == null)
				return i; // stop here
			topKFeature[i] = set.getFeature(); // need to be changed later since
												// the setID is not a valuable
												// results
			// update the status
			this.coveredItemCount += set.getScore();

			long startUpdate = System.currentTimeMillis();
			this.status.addToPos(set.getSet(), i);
			this.bbCal.updateAfterInsert(set.getSet(), status);
			long endUpdate = System.currentTimeMillis();
			this.statusUpdateTime = endUpdate - startUpdate;

			double ratio = (double) (i - currentK)
					/ (double) (i - currentK + 1);
			MemCost[0] = MemCost[0] * ratio
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

		for (ISet set : this.input) {
			if (set == null)
				break;
			long score = status.getUncoveredItemCount(set);
			if (score > maxScore) {
				maxScore = score;
				result = set;
			} else {
				boolean smaller = this.bbCal.isUppBoundSmaller(set,
						maxScore);
				if (smaller)
					input.pruneBranch();
			}
		}
		this.enumeratedPatternCount += input.getEnumeratedSetCount();
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
