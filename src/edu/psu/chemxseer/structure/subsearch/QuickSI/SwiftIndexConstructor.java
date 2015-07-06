package edu.psu.chemxseer.structure.subsearch.QuickSI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class SwiftIndexConstructor {

	public static SwiftIndexSearcher construct(
			NoPostingFeatures<IOneFeature> features) {
		// 1. Build the root Node
		int[] ID = new int[1];
		ID[0] = 0;
		TreeEntry root = new TreeEntry(null, ID[0]++);
		// 2. Insert other tree nodes
		for (int i = 0; i < features.getfeatureNum(); i++) {
			String featureLabel = features.getFeature(i).getDFSCode();
			String[] labelString = featureLabel.substring(1,
					featureLabel.length() - 1).split("><");
			insert2Tree(root, labelString, 0, features.getFeature(i), ID);
		}
		return new SwiftIndexSearcher(root, ID[0]);
	}

	public static void insert2Tree(TreeEntry parent, String[] labelString,
			int startIndex, IOneFeature feature, int[] ID) {
		String[] temp = labelString[startIndex].split(" ");
		int[] labelEntry = new int[temp.length];
		for (int i = 0; i < temp.length; i++)
			labelEntry[i] = Integer.parseInt(temp[i]);

		TreeEntry child = parent.getChild(labelEntry);
		if (child == null) {
			child = new TreeEntry(labelEntry, ID[0]++);
			parent.addOneChilde(child);
		}
		// keep on growing or....
		if (startIndex == labelString.length - 1) {
			child.setFeatureID(feature.getFeatureId());
		} else {
			// keep on growing
			insert2Tree(child, labelString, startIndex + 1, feature, ID);
		}
	}

	/**
	 * Save the searcher
	 * 
	 * @param searcher
	 * @param baseName
	 * @param indexName
	 * @throws IOException
	 */
	public static void saveSearcher(SwiftIndexSearcher searcher,
			String baseName, String indexName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				baseName, indexName)));
		// post-order visited the tree node and record them
		record(writer, searcher.root);
		writer.close();
	}

	private static void record(BufferedWriter writer, TreeEntry parent)
			throws IOException {
		// post-order visiting
		List<TreeEntry> children = parent.getChildNodes();
		for (int i = 0; i < children.size(); i++)
			record(writer, children.get(i));
		writer.write(parent.toString() + "\n");
	}

	public static SwiftIndexSearcher loadSearcher(String baseName,
			String indexName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				baseName, indexName)));
		// load the index structure
		Map<Integer, TreeEntry> map = new HashMap<Integer, TreeEntry>();
		String aLine = reader.readLine();
		List<Integer> childrenID = new ArrayList<Integer>();
		TreeEntry root = null;
		int featureCount = 0;
		while (aLine != null) {
			childrenID.clear();
			TreeEntry newEntry = TreeEntry.loadEntry(aLine, childrenID);
			// first insert this newEntry into the map
			map.put(newEntry.getEntryID(), newEntry);
			// add children to the newEntry
			for (int i = 0; i < childrenID.size(); i++)
				newEntry.addOneChilde(map.get(childrenID.get(i)));
			root = newEntry; // tricky, the root will be the last entry
			aLine = reader.readLine();
			featureCount++;
		}
		reader.close();
		return new SwiftIndexSearcher(root, featureCount);
	}
}
