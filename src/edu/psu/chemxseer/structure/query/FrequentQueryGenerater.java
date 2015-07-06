package edu.psu.chemxseer.structure.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureComparatorBase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;

/**
 * Generate & Save Frequent Queries
 * 
 * @author dayuyuan
 * 
 */
public class FrequentQueryGenerater {

	/**
	 * Given the whole set of frequent features filter features with edge <
	 * minSize, features with edge >= maxSize For each feature size, choose
	 * "num" as queries Output are written in the outputFolder, with id = edge
	 * count;
	 * 
	 * @param queries
	 * @param minSize
	 * @param maxSize
	 * @param num
	 * @throws IOException
	 */
	public static void generateFrequentQueries(
			NoPostingFeatures<OneFeatureImpl> frequentFeatures, int minSize,
			int maxSize, int num, String outputFolder) throws IOException {
		frequentFeatures.sortFeatures(new FeatureComparatorBase());

		// 2. Select features as frequent queries, and store them
		ArrayList<OneFeatureImpl> featureList = new ArrayList<OneFeatureImpl>();
		int currentSize = minSize;
		for (int i = 0; i < frequentFeatures.getfeatureNum()
				&& currentSize < maxSize; i++) {
			OneFeatureImpl onefeature = frequentFeatures.getFeature(i);
			if (onefeature.getFeatureGraph().getEdgeCount() < minSize)
				continue; // skip
			else if (onefeature.getFeatureGraph().getEdgeCount() == currentSize)
				featureList.add(onefeature);
			else {
				// queries of size "currentSize"
				doSaving(outputFolder, featureList, currentSize, num);
				featureList.clear();
				currentSize++;
				featureList.add(onefeature);
			}
		}
		doSaving(outputFolder, featureList, currentSize, num);
	}

	private static void doSaving(String FrequentQueryFolder,
			ArrayList<OneFeatureImpl> featureList, int id, int num)
			throws IOException {

		int totalNum = featureList.size();
		while (totalNum < num) {
			featureList.addAll(featureList);
			totalNum = featureList.size();
		}

		List<OneFeatureImpl> selectedFeatures = new ArrayList<OneFeatureImpl>(
				num);

		// choose num out of totalNum
		int[] indexes = new int[totalNum];
		for (int w = 0; w < indexes.length; w++)
			indexes[w] = w;
		Random rd = new Random();
		int j = 0;
		int swapTemp = 0;
		;
		for (int w = 0; w < num; w++) {
			j = (int) (rd.nextFloat() * (totalNum - w)) + w;
			swapTemp = indexes[w];
			indexes[w] = indexes[j];
			indexes[j] = swapTemp;
		}
		Arrays.sort(indexes, 0, num);

		for (int i = 0; i < num; i++) {
			featureList.get(indexes[i]).setSelected();
			selectedFeatures.add(featureList.get(indexes[i]));
		}

		new NoPostingFeatures<OneFeatureImpl>(FrequentQueryFolder
				+ File.separator + id, selectedFeatures, true);
	}
}
