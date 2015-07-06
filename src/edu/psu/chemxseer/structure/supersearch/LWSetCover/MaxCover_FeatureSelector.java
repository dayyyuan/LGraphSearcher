package edu.psu.chemxseer.structure.supersearch.LWSetCover;

import java.util.List;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureWrapper_OnDisk;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.supersearch.LWTree.LWIndexSetConverter;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * Feature Feature Selector for the Supgraph Search: based on the maximum
 * coverage model [the input set are weighted sets] In this implementation, we
 * use the simple greedy algorithm, in each iteration, the set with the maximum
 * "gain" coverage is selected. Here I did not use the class that provided by
 * the set-cover feature selection tool kits Instead I am impliment new One
 * 
 * @author dayuyuan
 * 
 */
public class MaxCover_FeatureSelector {

	/**
	 * Simple Greedy Algorithm for Feature Selection In each iteration, the gain
	 * is re-calculated, and the feature with the max-gain is selected The
	 * incremental score should be greater than minScore
	 * 
	 * @param frequentSubgraphs
	 * @param gDB
	 * @return
	 */
	public NoPostingFeatures<OneFeatureMultiClass> minePrefixFeatures(
			PostingFeaturesMultiClass frequentSubgraphs, IGraphDatabase gDB,
			int K, int minScore) {
		System.out.println("Select Index-Patterns for LW-Index");
		System.out.println("(1) Features & DB Postings Are Stored in Memory");
		System.out.println("(2) Use Brute Force Greedy Algorithm");
		// 1. Initialize the Status
		float[] status = new float[gDB.getTotalNum()];
		for (int i = 0; i < status.length; i++)
			status[i] = 0;
		// 2. Initialize the Features
		frequentSubgraphs.loadPostingIntoMemory();
		MemoryConsumptionCal.runGC();
		double memoryStart = MemoryConsumptionCal.usedMemoryinMB();
		NoPostingFeatures<OneFeatureMultiClass> features = frequentSubgraphs
				.getMultiFeatures();
		CoverSet_Weighted[] sets = new CoverSet_Weighted[features
				.getfeatureNum()];
		int querySize = frequentSubgraphs.getClassGraphsCount()[1];
		for (int i = 0; i < sets.length; i++) {
			features.getFeature(i).setUnselected();
			IFeatureWrapper oneFeature = new FeatureWrapper_OnDisk(
					frequentSubgraphs, i, -1);
			sets[i] = LWIndexSetConverter.featureToSet_Weight(oneFeature, gDB,
					querySize);
		}
		// 3. Run Greedy Feature Selection
		// In each iteration, I select the feature with the maximum "gain",
		// which is defined as the increase of the covered weight
		long start = System.currentTimeMillis();
		int selectedFeatureCount = 0;
		while (selectedFeatureCount < K) {
			float maxGain = 0;
			int selectfID = -1;
			for (int i = 0; i < features.getfeatureNum(); i++) {
				if (features.getFeature(i).isSelected())
					continue;
				float gain = 0;
				// 3.1 Calculate the gain function
				CoverSet_Weighted theSet = sets[i];
				for (int w = 0; w < theSet.size(); w++) {
					float coveredScore = status[theSet.getItem(w)];
					if (coveredScore < theSet.getScore(w))
						gain += theSet.getScore(w) - coveredScore;
				}
				// 3.2 Do the selection
				if (gain > maxGain) {
					maxGain = gain;
					selectfID = i;
				}
			}
			// 3.3 update the status, select the feature
			if (maxGain < minScore)
				break;
			else {
				features.getFeature(selectfID).setSelected();
				CoverSet_Weighted selectSet = sets[selectfID];
				for (int w = 0; w < selectSet.size(); w++) {
					float coveredScore = status[selectSet.getItem(w)];
					if (coveredScore < selectSet.getScore(w))
						status[selectSet.getItem(w)] = selectSet.getScore(w);
				}
			}
			selectedFeatureCount++;
		}
		MemoryConsumptionCal.runGC();
		double memoryEnd = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(3) Space for Pattern Selection: "
				+ (memoryEnd - memoryStart));
		System.out.println("(4) Time for Pattern Selection: "
				+ (System.currentTimeMillis() - start));
		System.out.println("(5) After Pattern Selection: "
				+ selectedFeatureCount + " out of " + sets.length
				+ " frequent patterns as selected for Indexing");
		// 4. Return selected Features
		List<OneFeatureMultiClass> selectedFeatures = features
				.getSelectedFeatures();
		return new NoPostingFeatures<OneFeatureMultiClass>(selectedFeatures,
				true);
	}

	/**
	 * Advanced implementation of Greedy Algorithm An inverted index is first
	 * build The gain function of each features is "updated" instead of
	 * "re-caulcaulte". In each iteration, the feature with the max-gain is
	 * selected, then the gain function of related feature is updated. The
	 * inverted index will help on finding the realted features whose score need
	 * to be updated.
	 * 
	 * @param frequentSubgraphs
	 * @param gDB
	 * @return
	 */
	public NoPostingFeatures<OneFeatureMultiClass> minePrefixFeaturesWithInvertedIndex(
			PostingFeaturesMultiClass frequentSubgraphs, IGraphDatabase gDB) {
		System.out.println("Select Index-Patterns for LW-Index");
		System.out.println("(1) Features & DB Postings Are Stored in Memory");
		System.out
				.println("(2) Inverted-index are built to facilitate score upate");
		// 1. Initialize the Status
		float[] status = new float[gDB.getTotalNum()];
		for (int i = 0; i < status.length; i++)
			status[i] = 0;
		float[] featureGain = new float[frequentSubgraphs.getFeatures()
				.getfeatureNum()];
		for (int i = 0; i < featureGain.length; i++)
			featureGain[i] = 0;

		// 2. Initialize the Features & Feature Gain
		MemoryConsumptionCal.runGC();
		double memoryStart = MemoryConsumptionCal.usedMemoryinMB();

		NoPostingFeatures<OneFeatureMultiClass> features = frequentSubgraphs
				.getMultiFeatures();
		CoverSet_Weighted[] sets = new CoverSet_Weighted[features
				.getfeatureNum()];
		int querySize = frequentSubgraphs.getClassGraphsCount()[1];
		for (int i = 0; i < sets.length; i++) {
			IFeatureWrapper oneFeature = new FeatureWrapper_OnDisk(
					frequentSubgraphs, i, -1);
			CoverSet_Weighted oneSet = LWIndexSetConverter.featureToSet_Weight(
					oneFeature, gDB, querySize);
			sets[i] = oneSet;
			for (int w = 0; w < oneSet.size(); w++)
				featureGain[i] += oneSet.getScore(w);
		}

		MemoryConsumptionCal.runGC();
		double memoryEnd = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(3) Space for Pattern Selection: "
				+ (memoryEnd - memoryStart));

		// 3. Build the inverted index
		long buildInvertedIndexStart = System.currentTimeMillis();
		int[][] index = new int[gDB.getTotalNum()][];
		float[][] scores = new float[gDB.getTotalNum()][];
		this.buildInvertedIndex(index, scores, sets);
		System.out.println("(4) Time for Inverted-Index Building "
				+ (System.currentTimeMillis() - buildInvertedIndexStart));
		MemoryConsumptionCal.runGC();
		memoryEnd = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("Total Memory After Inverted-Index Building: "
				+ (memoryEnd - memoryStart));

		// 4. Run Greedy Feature Selection
		// In each iteration, I select the feature with the maximum "gain",
		// which is defined as the increase of the covered weight
		int selectedFeatureCount = 0;
		long start = System.currentTimeMillis();
		while (selectedFeatureCount < sets.length) {
			// 4.1 Find the Feature with the maximum gain value
			float maxGain = 0;
			int maxfID = -1;
			for (int fID = 0; fID < featureGain.length; fID++) {
				if (features.getFeature(fID).isSelected())
					continue;
				if (featureGain[fID] > maxGain) {
					maxfID = fID;
					maxGain = featureGain[fID];
				}
			}
			// 4.2 select the feature
			if (maxGain == 0)
				break;
			features.getFeature(maxfID).setSelected();
			// 4.3 update the status & featureGain
			CoverSet_Weighted selectedSet = sets[maxfID];
			for (int w = 0; w < selectedSet.size(); w++) {
				int gID = selectedSet.getItem(w);
				float gScore = selectedSet.getScore(w);
				if (gScore <= status[gID])
					continue;
				else {
					// 4.3.1 update the score
					float oldStatusScore = status[gID];
					status[gID] = gScore;
					// 4.3.2 update the gain function
					for (int i = 0; i < scores[gID].length; i++) {
						if (scores[gID][i] <= oldStatusScore)
							continue;
						else if (scores[gID][i] <= gScore)
							featureGain[gID] -= scores[gID][i] - oldStatusScore;
						else
							featureGain[gID] -= gScore - oldStatusScore;
					}
				}
			}
		}
		System.out.println("(5) Time for Pattern Selection: "
				+ (System.currentTimeMillis() - start));
		System.out.println("(6) After Pattern Selection: "
				+ selectedFeatureCount + " out of " + sets.length
				+ " frequent patterns as selected for Indexing");
		// 4. Return selected Features
		List<OneFeatureMultiClass> selectedFeatures = features
				.getSelectedFeatures();
		return new NoPostingFeatures<OneFeatureMultiClass>(selectedFeatures,
				true);
	}

	/**
	 * Given all the sets, build an inverted index of database graphs
	 * 
	 * @param sets
	 * @return
	 */
	private void buildInvertedIndex(int[][] index, float[][] scores,
			CoverSet_Weighted[] inputSets) {
		// 1. Initialization
		int fSize = inputSets.length;
		int[] indexSize = new int[index.length];
		for (int i = 0; i < index.length; i++) {
			index[i] = new int[4];
			scores[i] = new float[4];
			indexSize[i] = 0;
		}
		// 2. Build the Inverted Index
		for (int fID = 0; fID < fSize; fID++) {
			for (int i = 0; i < inputSets[fID].size(); i++) {
				int gID = inputSets[fID].getItem(i);
				if (indexSize[gID] >= index[gID].length) {
					int[] temp = new int[2 * index[gID].length];
					float[] temp2 = new float[temp.length];
					for (int w = 0; w < index[gID].length; w++) {
						temp[w] = index[gID][w];
						temp2[w] = scores[gID][w];
					}
					index[gID] = temp;
					scores[gID] = temp2;
				}
				index[gID][indexSize[gID]++] = fID;
				scores[gID][indexSize[gID]] = inputSets[fID].getScore(gID);
			}
		}
		// 3. Save Space
		for (int gID = 0; gID < index.length; gID++) {
			if (indexSize[gID] < index[gID].length) {
				int[] temp = new int[indexSize[gID]];
				float[] temp2 = new float[temp.length];
				for (int i = 0; i < temp.length; i++) {
					temp[i] = index[gID][i];
					temp2[i] = scores[gID][i];
				}
				index[gID] = temp;
				scores[gID] = temp2;
			}
		}
	}
}
