package edu.psu.chemxseer.structure.supersearch.LWSetCover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.parmol.util.FrequentFragment;

import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.supersearch.LWTree.LWIndexSetConverter;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The implementation of the stream mining algorithm Patterns are enumerated one
 * after another The algorithm decides whether to select a pattern or not To
 * find the minSet, the score for each set is computed
 * 
 * @author dayuyuan
 * 
 */
public class Stream_FeatureSelector {
	// No Rule Saying that All Edges Need to be Selected
	public NoPostingFeatures<OneFeatureMultiClass> minePrefixFeatures(
			GSpanMiner_MultiClass_Iterative patternGenerator,
			IGraphDatabase gDB, int K) {
		System.out.println("Select Index-Patterns for LW-Index");
		System.out.println("(1) Only K index patterns are stored in memory");
		System.out.println("(2) Use streamming algorithm");

		int[] status = new int[gDB.getTotalNum()]; // denote the maximum
													// coverage of a status
		@SuppressWarnings("unchecked")
		LinkedList<Integer>[] statusList = new LinkedList[gDB.getTotalNum()];
		Arrays.fill(status, 0);
		for (int i = 0; i < gDB.getTotalNum(); i++)
			statusList[i] = new LinkedList<Integer>();

		// 1. Select the First $K$ Features
		int querySize = patternGenerator.getClassGraphCount()[1];
		double memoryStart = MemoryConsumptionCal.usedMemoryinMB();
		CoverSet_Weighted[] selectedSets = new CoverSet_Weighted[K];
		int index = 0;
		FrequentFragment nextPattern = null;
		while (index < K
				&& (nextPattern = patternGenerator.nextPattern()) != null) {
			CoverSet_Weighted newSet = LWIndexSetConverter.featureToSet_Weight(
					patternGenerator.getFeature(nextPattern), gDB, querySize);
			selectedSets[index++] = newSet;
			insertNewSet(newSet, status, statusList);
		}
		double memoryEnd = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(3) Space for Pattern Selection: "
				+ (memoryEnd - memoryStart));

		// 2. Run Streaming Algorithm
		long start = System.currentTimeMillis();
		int minSetID = -1;
		int minSetScore1KTimes = 0;

		while ((nextPattern = patternGenerator.nextPattern()) != null) {
			CoverSet_Weighted newSet = LWIndexSetConverter.featureToSet_Weight(
					patternGenerator.getFeature(nextPattern), gDB, querySize);
			if (minSetID == -1) {
				// find the set with the minimum score after removal
				int[] temp = new int[1];
				minSetID = findMinSet(selectedSets, status, statusList, temp);
				minSetScore1KTimes = temp[0];
			}
			// decide whether to swap or not
			int newGain1KTimes = this.gainForNewSet1KTimes(newSet, status);
			if (newGain1KTimes > 2 * minSetScore1KTimes) {
				// do swap
				// System.out.println("Swap " + newGain + " with " + minSetID +
				// " and score " + minSetScore);
				this.removeSelectedSet(selectedSets[minSetID], status,
						statusList);
				selectedSets[minSetID] = newSet;
				this.insertNewSet(newSet, status, statusList);
				minSetID = -1;
			}
		}
		System.out.println("(4) Time for Pattern Selection: "
				+ (System.currentTimeMillis() - start));
		System.out.println("(5) After Pattern Selection: " + K + " out of "
				+ patternGenerator.getEnumeratedPatternNum()
				+ " frequent patterns as selected for Indexing");

		// 4. Return selected Features
		List<OneFeatureMultiClass> selectedFeatures = new ArrayList<OneFeatureMultiClass>();
		for (CoverSet_Weighted oneSet : selectedSets) {
			selectedFeatures.add(oneSet.getFeatures());
		}
		return new NoPostingFeatures<OneFeatureMultiClass>(selectedFeatures,
				true);
	}

	/**
	 * Return the gain for a new Set, given the current status
	 * 
	 * @param newSet
	 * @param status
	 * @return
	 */
	private int gainForNewSet1KTimes(CoverSet_Weighted newSet, int[] status) {
		int gain = 0;
		for (int i = 0; i < newSet.size(); i++) {
			int item = newSet.getItem(i);
			int score = newSet.getScore1KTimes(i);
			if (score > status[item])
				gain += score - status[item];
		}
		return gain;
	}

	private int findMinSet(CoverSet_Weighted[] sets, int[] status,
			LinkedList<Integer>[] statusList, int[] resultScore) {
		int index = 0;
		int minScore1KTimes = Integer.MAX_VALUE;
		int minIndex = 0;
		for (CoverSet_Weighted oneSet : sets) {
			int theScore1KTimes = this.gainForSelectedSet1KTimes(oneSet,
					status, statusList);
			if (theScore1KTimes < minScore1KTimes) {
				minScore1KTimes = theScore1KTimes;
				minIndex = index;
			}
			index++;
		}
		resultScore[0] = minScore1KTimes;
		return minIndex;
	}

	/**
	 * Return the gain for a selected set, given the status & statusList The
	 * statusList is ordered from large to small
	 * 
	 * @param oneSet
	 * @param status
	 * @param statusList
	 * @return
	 */
	private int gainForSelectedSet1KTimes(CoverSet_Weighted oneSet,
			int[] status, LinkedList<Integer>[] statusList) {
		int gain = 0;
		for (int i = 0; i < oneSet.size(); i++) {
			int item = oneSet.getItem(i);
			int score = oneSet.getScore1KTimes(i);
			if (score < status[item])
				continue;
			else if (score == status[item]) {
				if (statusList[item].size() == 1)
					gain += score; // only covered by one item
				else
					gain += score - statusList[item].get(1);
				// if 2nd weight on statusList[item] == score, gain += 0;
				// else if 2nd weight on statusList[item] < score, gain +=
				// difference
			} else
				System.out
						.println("Exception: how come the coverage weight > item weight");
		}
		return gain;
	}

	private void removeSelectedSet(CoverSet_Weighted oneSet, int[] status,
			LinkedList<Integer>[] statusList) {
		for (int i = 0; i < oneSet.size(); i++) {
			int item = oneSet.getItem(i);
			int score1KTimes = oneSet.getScore1KTimes(i);
			status[item] = removeItem(statusList[item], score1KTimes);
		}
	}

	private void insertNewSet(CoverSet_Weighted oneSet, int[] status,
			LinkedList<Integer>[] statusList) {
		for (int i = 0; i < oneSet.size(); i++) {
			int item = oneSet.getItem(i);
			int score1KTimes = oneSet.getScore1KTimes(i);
			statusList[item] = insertItem(statusList[item], score1KTimes);
			status[item] = statusList[item].getFirst();
		}
	}

	/**
	 * Return the itemScore from the list And return the largest item from the
	 * list or 0
	 * 
	 * @param list
	 * @param itemScore
	 * @return
	 */
	private int removeItem(LinkedList<Integer> list, int itemScore) {
		if (list == null)
			System.out.println("Exception in removeItem: null list");
		else {
			boolean success = list.remove(new Integer(itemScore));
			if (success == false)
				System.out
						.println("Excpetion in removeItem: no itemScore to remove");
		}

		if (list.size() > 0)
			return list.getFirst();
		else
			return 0;
	}

	/**
	 * Insert one score to the list Maintain the order of the list from largest
	 * to smallest return the list
	 * 
	 * @param list
	 * @param itemScore
	 * @return
	 */
	private LinkedList<Integer> insertItem(LinkedList<Integer> list,
			int itemScore) {
		if (list == null) {
			LinkedList<Integer> result = new LinkedList<Integer>();
			result.add(itemScore);
			return result;
		} else {
			// since the input is linkedList, the binary search algorithm may
			// not be a good strategy
			int index = 0;
			for (Integer f : list) {
				if (f > itemScore)
					index++;
				else
					break;
			}
			list.add(index, itemScore);
			return list;
		}
	}
}
