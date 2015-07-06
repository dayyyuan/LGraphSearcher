package edu.psu.chemxseer.structure.supersearch.GPTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureExt;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class CRGraphSearcherConstructor {

	public static CRGraphSearcher construct(
			NoPostingFeatures_Ext<IOneFeature> significantFeatures,
			IIndexPrefix FGPTree) {
		// 1. Construct all the CRGraphNode
		CRGraphNode[] allNodes = new CRGraphNode[significantFeatures
				.getfeatureNum()];
		for (int i = 0; i < allNodes.length; i++) {
			String dfsCode = significantFeatures.getFeature(i).getDFSCode();
			allNodes[i] = new CRGraphNode(i, MyFactory.getDFSCoder()
					.parseTextToArray(dfsCode));
		}
		// 1.2 add on parent-children relationships
		for (int i = 0; i < allNodes.length; i++) {
			OneFeatureExt oneFeature = significantFeatures.getFeatureExt(i);
			List<OneFeatureExt> children = oneFeature.getChildren();
			if (children == null || children.size() == 0)
				continue;
			CRGraphNode[] nodeChildren = new CRGraphNode[children.size()];
			for (int w = 0; w < children.size(); w++)
				nodeChildren[w] = allNodes[children.get(w).getFeatureId()];
			allNodes[i].setChildren(nodeChildren);
		}
		// 1.3 order all the nodes. It's easy.
		// Just order all the nodes according to their underlying graph size is
		// OK.
		// From small to big.
		Arrays.sort(allNodes);
		// 1.4 Set the ID
		for (int i = 0; i < allNodes.length; i++)
			allNodes[i].setNodeID(i);// reset the IDs
		// 2: Prefix Extension: Need to label each CRGraphnode as a suffix of a
		// feature in the FGPTree
		for (int i = 0; i < allNodes.length; i++) {
			int prefixID = FGPTree.getPrefixID(allNodes[i].getGraph(null));
			if (prefixID == -1) // no prefixID, not all graphs are covered? it
								// may happen
				continue;
			else {
				int[][] prefixLabel = FGPTree.getTotalLabel(prefixID);
				allNodes[i].reLabelGraph(prefixLabel, prefixID);
			}
		}
		return new CRGraphSearcher(allNodes, FGPTree);
	}

	/********************* Below is Saving and Loading *****************************/

	/**
	 * Load the CRGraphNodes from the Disk and Construct the Index
	 * 
	 * @param baseName
	 * @param indexName
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static CRGraphSearcher loadSearcher(IIndexPrefix FGPTree,
			String baseName, String indexName) throws NumberFormatException,
			IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				baseName, indexName)));
		int count = Integer.parseInt(reader.readLine());
		CRGraphNode[] allNodes = new CRGraphNode[count];
		String aLine = reader.readLine();
		while (aLine != null) {
			CRGraphNode oneNode = new CRGraphNode(aLine, allNodes);
			allNodes[oneNode.getNodeID()] = oneNode;
			aLine = reader.readLine();
		}
		reader.close();
		return new CRGraphSearcher(allNodes, FGPTree);
	}

	/**
	 * Save the Index on Disk, write to file
	 * 
	 * @param baseName
	 * @param indexName
	 * @throws IOException
	 */
	public static void saveSearcher(CRGraphSearcher searcher, String baseName,
			String indexName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				baseName, indexName)));
		writer.write(searcher.allNodes.length + "\n");
		boolean[] status = new boolean[searcher.allNodes.length];
		for (int i = 0; i < status.length; i++) {
			status[i] = false;
		}
		for (int i = 0; i < searcher.allNodes.length; i++) {
			if (status[i])
				continue;
			else
				saveIndex(writer, searcher.allNodes[i], status);
		}
		writer.close();
	}

	private static void saveIndex(BufferedWriter writer, CRGraphNode node,
			boolean[] status) throws IOException {
		CRGraphNode[] children = node.getChildren();
		if (children != null && children.length > 0) {
			for (CRGraphNode oneC : children) {
				if (status[oneC.getNodeID()])
					continue;
				else
					saveIndex(writer, oneC, status);
			}
		}
		writer.write(node.toString());
		writer.newLine();
		status[node.getNodeID()] = true;
	}
}
