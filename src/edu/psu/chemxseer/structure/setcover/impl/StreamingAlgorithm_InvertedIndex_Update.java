package edu.psu.chemxseer.structure.setcover.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.psu.chemxseer.structure.setcover.IO.Input_StreamDec;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSwapSolver;
import edu.psu.chemxseer.structure.setcover.status.IBranchBound;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap_InvertedIndex;
import edu.psu.chemxseer.structure.setcover.update.IndexUpdator;
import edu.psu.chemxseer.structure.setcover.update.IndexUpdator_Input;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The Inverted Index Methods for Stream Swap Algorithm
 * 
 * @author dayuyuan
 * 
 */
public class StreamingAlgorithm_InvertedIndex_Update extends
		StreamingAlgorithm_InvertedIndex implements IMaxCoverSwapSolver {
	private Iterator<ISet> inputIterator;
	private IndexUpdator indexUpdator;
	private boolean swapExists;
	private Set<String> selectedFeatures;
	private Map<Integer, Integer> setPosLindexTermPosMap;

	public long statusUpdateTime;
	public long scoreUpdateTime;
	public long exclusiveExamTime;
	public long swapCount;
	public double avgMemCost;
	public long findFullSupportTime;

	public StreamingAlgorithm_InvertedIndex_Update(IndexUpdator_Input input,
			ICoverStatus_Swap_InvertedIndex status, IBranchBound bbCal,
			double initialSpace, double lambda, IndexUpdator indexUpdator) {
		super(input, status, bbCal, lambda, initialSpace);
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		this.indexUpdator = indexUpdator;
		this.feedAlreadySelected(indexUpdator);
		this.initSelectedFeatures();
		this.inputIterator = input.iterator();
		MemoryConsumptionCal.runGC();
		super.statusInitialSpace += MemoryConsumptionCal.usedMemoryinMB()
				- beforeMem;
	}

	/**
	 * Update Selected Feature Status Update BBCal
	 * 
	 * @param index
	 */
	private void feedAlreadySelected(IndexUpdator indexUpdator) {
		this.setPosLindexTermPosMap = new HashMap<Integer, Integer>();
		ISet[] selectedSets = indexUpdator
				.getSelectedSets(setPosLindexTermPosMap);
		this.preSelectedFeatures = new SetwithScore[selectedSets.length];
		// update preSelectedFeatures & status
		for (int i = 0; i < selectedSets.length; i++)
			preSelectedFeatures[i] = new SetwithScore(selectedSets[i], i, -1);
		// the position record the position of the features on the solver, but
		// not the index
		// the set ID is accordance with the index.
		this.status.addToPos(preSelectedFeatures);
		this.initializeScore(preSelectedFeatures);
		this.coveredItemCount = this.status.getCoveredItemCount();
		bbCal.initialize(status);
	}

	/**
	 * Initialize the selected features
	 */
	private void initSelectedFeatures() {
		this.selectedFeatures = new HashSet<String>();
		for (ISet set : preSelectedFeatures)
			selectedFeatures.add(set.getFeature().getFeature().getDFSCode());
		for (ISet set : fixSelectedFeatures)
			selectedFeatures.add(set.getFeature().getFeature().getDFSCode());
	}

	@Override
	public Iterator<SetwithScore[]> iterator() {
		return new SwapIterator();
	}

	/**
	 * Do the next swap & return the swapped pair
	 * 
	 * @return
	 */
	public SetwithScore[] doNextSwap() {
		SetwithScore[] result = new SetwithScore[2];
		SetwithScore minSet = null;
		while (true) {
			ISet oneSet = inputIterator.next();
			if (oneSet == null) {
				if (input instanceof Input_StreamDec && swapExists) {
					boolean decreased = ((Input_StreamDec) input)
							.decreaseMinSup(inputIterator);
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
			// find the selected set with minimum marginal benefit
			if (minSet == null) {
				minSet = this.findMinSet(this.preSelectedFeatures);
				// System.out.println(minSet.getScore());
				// (1) First remove the minSet
				long startT = System.currentTimeMillis();
				this.status.removeFromPos(minSet);
				this.bbCal.updateAfterDelete(minSet.getSet(), status);
				statusUpdateTime += System.currentTimeMillis() - startT;
				startT = System.currentTimeMillis();
				this.updateScoreAfterRemove(preSelectedFeatures, minSet);
				scoreUpdateTime += System.currentTimeMillis() - startT;
				result[0] = minSet;
			}
			// calculate the swap threshold
			// test whether the set should swap with minSet
			long startT = System.currentTimeMillis();
			double threshold = lambda
					* minSet.getScore()
					+ (1 - lambda)
					* ((double) this.coveredItemCount / (double) preSelectedFeatures.length);
			long gain = this.status.getUncoveredItemCount(oneSet);
			exclusiveExamTime += System.currentTimeMillis() - startT;

			if (gain > minSet.getScore() + threshold) {
				// If I am using only partial data set for calculation
				// then here, I call the function to computer the full data for
				// the queries & data graph
				if (!input.fullRangeConverter()) {
					startT = System.currentTimeMillis();
					oneSet = indexUpdator.getFullSet(oneSet,
							((IndexUpdator_Input) input)
									.getFullFeatureConverter()); // the real set
																	// (with
																	// full
																	// database
																	// graph &
																	// queries)
					gain = this.status.getUncoveredItemCount(oneSet); // The
																		// real
																		// gain
					findFullSupportTime += System.currentTimeMillis() - startT;
				}
				// System.out.println(newGain);
				SetwithScore newSet = new SetwithScore(oneSet, minSet.getPos(),
						gain);
				preSelectedFeatures[minSet.getPos()] = newSet;
				startT = System.currentTimeMillis();
				this.updateScoreBeforeInsert(preSelectedFeatures, newSet);
				scoreUpdateTime += System.currentTimeMillis() - startT;

				startT = System.currentTimeMillis();
				this.status.addToPos(oneSet, minSet.getPos());
				this.bbCal.updateAfterInsert(oneSet, status);
				statusUpdateTime += System.currentTimeMillis() - startT;

				coveredItemCount += gain - minSet.getScore();
				// update selected features as well.
				selectedFeatures.remove(minSet.getFeature().getFeature()
						.getDFSCode());
				selectedFeatures.add(oneSet.getFeature().getFeature()
						.getDFSCode());
				minSet = null; // need to search for minSet again in the next
								// iteration
				swapExists = true;
				result[1] = newSet;
				swapCount++;
				break;
			} else {
				// branch & bound
				boolean smaller = this.bbCal.isUppBoundSmaller(oneSet,
						(int) (minSet.getScore() + threshold));
				if (smaller) // prune the whole branch
					input.pruneBranch();
			}

			avgMemCost = avgMemCost
					* (patternEnumerated / (patternEnumerated + 1))
					+ MemoryConsumptionCal.usedMemoryinMB()
					/ (patternEnumerated + 1);
			patternEnumerated++;
		}
		if (result[1] == null)
			return null;
		else
			return result;
	}

	/**
	 * The iterator class returns the pair of swapping features
	 * 
	 * @author dayuyuan
	 * 
	 */
	class SwapIterator implements Iterator<SetwithScore[]> {
		private boolean isNextAvailable;

		public SwapIterator() {
			this.isNextAvailable = true;
			swapExists = true;
			statusUpdateTime = exclusiveExamTime = scoreUpdateTime = findFullSupportTime = 0;
			swapCount = patternEnumerated = 0;
			coveredCountBfSwap = coveredItemCount;
			MemoryConsumptionCal.runGC();
			avgMemCost = MemoryConsumptionCal.usedMemoryinMB();
		}

		@Override
		public boolean hasNext() {
			return this.isNextAvailable;
		}

		@Override
		public SetwithScore[] next() {
			SetwithScore[] nextSet = doNextSwap();
			if (nextSet == null) {
				isNextAvailable = false;
				return null;
			} else
				return nextSet;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		}

	}

	@Override
	public int getLindexTermPos(int setPos) {
		return this.setPosLindexTermPosMap.get(setPos);
	}

	public float getSelectedFetureCount() {
		return this.preSelectedFeatures.length;
	}

	public ICoverStatus_Swap_InvertedIndex getStatus() {
		return this.status;
	}

	public int testOnlyGetSetPos(int lindexTermPos) {
		for (Entry<Integer, Integer> entry : this.setPosLindexTermPosMap
				.entrySet())
			if (entry.getValue() == lindexTermPos)
				return entry.getKey();
		return -1;
	}
}
