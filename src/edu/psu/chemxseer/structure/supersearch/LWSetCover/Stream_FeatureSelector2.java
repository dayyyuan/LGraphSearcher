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
 * Similar to Stream_FeatureSelector, the only difference is on findMinSet &
 * StatusList Here, the StatusList actually is an inverted index, instead of
 * just the score
 * 
 * 
 * This implementation is actually wrong, This is because inserting or deleting
 * on set, even this set contains a item which is ranked 2nd on the covereage
 * list The score of the first ranked cover item contained set need to be
 * updated as well. Then the algorithm is very tediou, which may not be very
 * efficient in comparison to measuring afresh I may banned this implementation
 * 
 * @author dayuyuan
 * 
 */
public class Stream_FeatureSelector2 {

	// No Rule Saying that All Edges Need to be Selected
	public NoPostingFeatures<OneFeatureMultiClass> minePrefixFeatures(
			GSpanMiner_MultiClass_Iterative patternGenerator,
			IGraphDatabase gDB, int K) {
		System.out.println("Select Index-Patterns for LW-Index");
		System.out.println("(1) Only K index patterns are stored in memory");
		System.out.println("(2) Use streamming algorithm");

		int[] status = new int[gDB.getTotalNum()]; // denote the maximum
													// coverage of a status
		Arrays.fill(status, 0);
		int[] scores = new int[K];
		Arrays.fill(scores, 0);

		@SuppressWarnings("unchecked")
		LinkedList<int[]>[] statusList = new LinkedList[gDB.getTotalNum()];
		for (int i = 0; i < gDB.getTotalNum(); i++) {
			statusList[i] = new LinkedList<int[]>();
		}

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
			selectedSets[index] = newSet;
			this.insertNewSet(newSet, index, status, statusList, scores);
			index++;
		}
		double memoryEnd = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(3) Space for Pattern Selection: "
				+ (memoryEnd - memoryStart));

		// 2. Run Streaming Algorithm
		long start = System.currentTimeMillis();
		int minSetID = -1;

		while ((nextPattern = patternGenerator.nextPattern()) != null) {
			CoverSet_Weighted newSet = LWIndexSetConverter.featureToSet_Weight(
					patternGenerator.getFeature(nextPattern), gDB, querySize);
			if (minSetID == -1) {
				// find the set with the minimum score after removal
				minSetID = findMinSet(scores);
			}
			// decide whether to swap or not
			int newGain = this.gainForNewSet(newSet, status);
			if (newGain > 2 * scores[minSetID]) {
				// do swap
				// System.out.println("Swap " + newGain + " with " + minSetID +
				// " and score " + scores[minSetID]);
				this.removeSelectedSet(selectedSets[minSetID], minSetID,
						status, statusList, scores);
				selectedSets[minSetID] = newSet;
				this.insertNewSet(newSet, minSetID, status, statusList, scores);
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
	private int gainForNewSet(CoverSet_Weighted newSet, int[] status) {
		int gain = 0;
		for (int i = 0; i < newSet.size(); i++) {
			int item = newSet.getItem(i);
			int score1KTimes = newSet.getScore1KTimes(i);
			if (score1KTimes > status[item])
				gain += score1KTimes - status[item];
		}
		return gain;
	}

	private int findMinSet(int[] scores) {
		int index = 0;
		int minScore = Integer.MAX_VALUE;
		int minIndex = 0;
		for (int oneScore : scores) {
			if (oneScore < minScore) {
				minScore = oneScore;
				minIndex = index;
			}
			index++;
		}
		return minIndex;
	}

	/**
	 * Remove on selected set, update the score
	 * 
	 * @param oneSet
	 * @param setPos
	 * @param status
	 * @param statusList
	 * @param statusInvertList
	 * @param setScores
	 */
	private void removeSelectedSet(CoverSet_Weighted oneSet, int setPos,
			int[] status, LinkedList<int[]>[] statusList, int[] setScores) {

		setScores[setPos] = 0;
		for (int i = 0; i < oneSet.size(); i++) {
			int item = oneSet.getItem(i);
			removeItem(statusList[item], oneSet.getScore1KTimes(i), setPos,
					setScores);
			if (statusList[item].size() == 0)
				status[item] = 0;
			// the setScores for other selected set is not affected
			else {
				int[] topElement = statusList[item].get(0);
				status[item] = topElement[0];
			}
		}

	}

	/**
	 * Remove the information related to setPos from list
	 * 
	 * @param list
	 * @return
	 */
	private void removeItem(LinkedList<int[]> list, int itemScore, int setPos,
			int[] scores) {
		if (list == null || list.size() == 0)
			System.out.println("Exception in removeItem: null list");
		else {
			int index = 0;
			for (int[] it : list) {
				if (it[1] == setPos)
					break;
				index++;
			}
			if (index >= list.size())
				System.out
						.println("Excpetion in removeItem: no item to remove");
			list.remove(index);

			if (list.size() > 0) {
				if (index == 0) { // first element is removed
					int[] currentTopElement = list.get(0);
					if (list.size() == 1)
						scores[currentTopElement[1]] += currentTopElement[0];
					else {
						int[] secTopElement = list.get(1);
						scores[currentTopElement[1]] += currentTopElement[0]
								- secTopElement[0];
					}
				} else if (index == 1) {
					int[] topElement = list.get(0);
					if (list.size() == 1)
						scores[topElement[1]] += itemScore;
					else {
						int[] secTopElement = list.get(1);
						scores[topElement[1]] += itemScore - secTopElement[0];
					}
				}
			}
		}
	}

	/**
	 * Insert a new Set & Update the corresponding score of related set & set
	 * the score for the new Set
	 * 
	 * @param oneSet
	 * @param setPos
	 * @param status
	 * @param statusList
	 * @param scores
	 */
	private void insertNewSet(CoverSet_Weighted oneSet, int setPos,
			int[] status, LinkedList<int[]>[] statusList, int[] scores) {
		scores[setPos] = 0;

		for (int i = 0; i < oneSet.size(); i++) {
			int item = oneSet.getItem(i);
			int score1KTimes = oneSet.getScore1KTimes(i);
			statusList[item] = insertItem(statusList[item], score1KTimes,
					setPos, scores);
			status[item] = statusList[item].get(0)[0];
		}
	}

	/**
	 * Insert one score to the list Maintain the order of the list from largest
	 * to smallest return the list
	 * 
	 * @param list
	 * @param itemScore
	 * @param setPos
	 * @return
	 */
	private LinkedList<int[]> insertItem(LinkedList<int[]> list, int itemScore,
			int setPos, int[] scores) {
		if (list == null) {
			LinkedList<int[]> result = new LinkedList<int[]>();
			result.add(new int[] { itemScore, setPos });
			return result;
		} else {
			// since the input is linkedList, the binary search algorithm may
			// not be a good strategy
			// insert the <itemScore, setPos> at the end of the item greater
			// than it.
			int index = 0;
			for (int[] f : list) {
				if (f[0] > itemScore)
					index++;
				else
					break;
			}
			list.add(index, new int[] { itemScore, setPos });

			if (list.size() == 1)
				scores[setPos] += itemScore;
			else if (index == 0) { // First Element
				int[] oldTop = list.get(1);
				scores[setPos] += itemScore - oldTop[0];

				if (list.size() == 2)
					scores[oldTop[1]] -= oldTop[0];
				else {
					int[] oldSecTop = list.get(2);
					scores[oldTop[1]] -= oldTop[0] - oldSecTop[0]; // loss all
																	// its score
				}
			} else if (index == 1) {
				int[] top = list.get(0);

				if (list.size() == 2)
					scores[top[1]] -= itemScore;
				else {
					int[] oldSecTop = list.get(2);
					scores[top[1]] -= itemScore - oldSecTop[0];
				}
			}
			return list;
		}
	}

	// //FOR TEST Purpose:
	// /**
	// * Return the gain for a selected set, given the status & statusList
	// * The statusList is ordered from large to small
	// * @param oneSet
	// * @param status
	// * @param statusList
	// * @return
	// */
	// private int gainForSelectedSet(CoverSet_Weighted oneSet, int[] status,
	// LinkedList<int[]>[] statusList){
	// int gain = 0;
	// for(int i = 0; i< oneSet.size(); i++){
	// int item = oneSet.getItem(i);
	// int score = oneSet.getScore(i);
	// if(score < status[item])
	// continue;
	// else if(score == status[item]){
	// if(statusList[item].size() == 1)
	// gain += score; // only covered by one item
	// else gain += score- statusList[item].get(1)[0];
	// // if 2nd weight on statusList[item] == score, gain += 0;
	// // else if 2nd weight on statusList[item] < score, gain += difference
	// }
	// else
	// System.out.println("Exception: how come the coverage weight > item weight");
	// }
	// return gain;
	// }
}
