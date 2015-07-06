package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * The Constructor for the PrefixIndexSearcher
 * 
 * @author dayuyuan
 * 
 */
public class PrefixIndexSearcherConstructor {
	/**
	 * Given all the Features, Construct the PRefixIndex
	 * 
	 * @param features
	 */
	public static PrefixIndexSearcher construct(
			NoPostingFeatures<IOneFeature> inputFeatures) {
		PrefixFeature[] features = new PrefixFeature[inputFeatures
				.getfeatureNum()];
		for (int i = 0; i < features.length; i++) {
			features[i] = new PrefixFeature(i, inputFeatures.getFeature(i)
					.getDFSCode(), null);
		}
		return new PrefixIndexSearcher(features);
	}

	/**
	 * Given the features, and the UpperLevel Index (the prefix), construct the
	 * nextLevel PrefixIndexSearcher: Prefix labeling
	 * 
	 * @param inputFeatures
	 * @param upperLevelIndex
	 */
	public static PrefixIndexSearcher construct(
			NoPostingFeatures<IOneFeature> inputFeatures,
			PrefixIndexSearcher upperLevelIndex) {
		PrefixFeature[] features = new PrefixFeature[inputFeatures
				.getfeatureNum()];
		for (int i = 0; i < features.length; i++) {
			IOneFeature oneFeature = inputFeatures.getFeature(i);
			features[i] = new PrefixFeature(i, oneFeature.getFeatureGraph(),
					upperLevelIndex);
		}
		return new PrefixIndexSearcher(features);
	}

	/**
	 * Load the Index from Disk
	 * 
	 * @param baseName
	 * @param indexName
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static PrefixIndexSearcher loadSearcher(String baseName,
			String indexName, PrefixIndexSearcher upperLevelIndex)
			throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				baseName, indexName)));
		String firstLine = reader.readLine();
		int featureCount = Integer.parseInt(firstLine);
		PrefixFeature[] features = new PrefixFeature[featureCount];
		for (int i = 0; i < features.length; i++) {
			features[i] = new PrefixFeature(reader.readLine(), upperLevelIndex);
		}
		reader.close();
		return new PrefixIndexSearcher(features);
	}

	/**
	 * Save the Index on Disk
	 * 
	 * @param baseName
	 * @param indexName
	 * @throws IOException
	 */
	public static void saveSearcher(PrefixIndexSearcher searcher,
			String baseName, String indexName) throws IOException {
		// 1. Num of features
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				baseName, indexName)));
		writer.write(searcher.features.length + "\n");
		// 2. Each Feature
		for (int i = 0; i < searcher.features.length; i++) {
			writer.write(searcher.features[i].toString());
			writer.newLine();
		}
		// 3. Close the Writer
		writer.close();
	}

}
