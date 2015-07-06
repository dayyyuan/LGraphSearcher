package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class CIndexFlatConstructor {

	/**
	 * Construct the CIndexFlat with a set of selected Features
	 * 
	 * @param selectedFeatures
	 */
	public static CIndexFlat construct(
			NoPostingFeatures<IOneFeature> selectedFeatures) {
		String[] indexingGraphs = new String[selectedFeatures.getfeatureNum()];
		for (int i = 0; i < selectedFeatures.getfeatureNum(); i++) {
			indexingGraphs[i] = selectedFeatures.getFeature(i).getDFSCode();
		}
		return new CIndexFlat(indexingGraphs);
	}

	/**
	 * Load the Index
	 * 
	 * @param baseName
	 * @param indexFileName
	 * @throws IOException
	 */
	public static CIndexFlat loadSearcher(String baseName, String indexFileName)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(baseName
				+ indexFileName));
		String firstLine = reader.readLine();
		int featureSize = Integer.parseInt(firstLine);
		String[] indexingGraphs = new String[featureSize];
		for (int i = 0; i < featureSize; i++) {
			String temp = reader.readLine();
			if (temp == null)
				System.out.println("Exception in CIndexFlat(String)"); // For
																		// test
																		// only
			String[] tokens = temp.split(",");
			indexingGraphs[i] = tokens[1];
		}
		reader.close();
		return new CIndexFlat(indexingGraphs);
	}

	/**
	 * Save the index into the indexFile
	 * 
	 * @param baseName
	 * @param indexFileName
	 * @throws IOException
	 */
	public static void saveSearcher(CIndexFlat searcher, String baseName,
			String indexFileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				baseName, indexFileName)));
		writer.write(searcher.indexingGraphs.length + "\n");
		for (int i = 0; i < searcher.indexingGraphs.length; i++)
			writer.write(i + "," + searcher.indexingGraphs[i] + "\n");
		writer.close();
	}
}
