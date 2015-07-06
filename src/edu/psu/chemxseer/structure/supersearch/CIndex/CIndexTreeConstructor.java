package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CIndexTreeConstructor {

	public static CIndexTree construct(CIndexTreeFeatureNode dummyRoot) {
		int[] nID = new int[1];
		nID[0] = 0;
		CIndexTreeNode rootFeature = copyNode(dummyRoot, nID);
		return new CIndexTree(rootFeature, nID[0]);
	}

	private static CIndexTreeNode copyNode(CIndexTreeFeatureNode feature,
			int[] nID) {
		CIndexTreeNode theNode;
		if (feature.isLeafFeatures()) {
			theNode = new CIndexTreeNode(-1, -1, null, feature.getLeafNode());
			nID[0]++;
		} else {
			theNode = new CIndexTreeNode(nID[0]++, feature.getTheFeature()
					.getFeatureId(), feature.getTheFeature().getDFSCode(), null);
			if (feature.getLeftChild() != null)
				theNode.setLeft(copyNode(feature.getLeftChild(), nID));
			if (feature.getRightChild() != null)
				theNode.setRight(copyNode(feature.getRightChild(), nID));
		}
		return theNode;
	}

	public static CIndexTree loadSearcher(String baseName, String indexFileName)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				baseName, indexFileName)));
		int featureCount = Integer.parseInt(reader.readLine());
		CIndexTreeNode rootFeature = parseNode(reader);
		return new CIndexTree(rootFeature, featureCount);
	}

	// Pre-order search
	private static CIndexTreeNode parseNode(BufferedReader reader)
			throws IOException {
		String aLine = reader.readLine();
		if (aLine == null || aLine.length() == 0)
			return null;
		else {
			CIndexTreeNode currentNode = CIndexTreeNode.parseNode(aLine);
			currentNode.setLeft(parseNode(reader));
			currentNode.setRight(parseNode(reader));
			return currentNode;
		}
	}

	/**
	 * save the index on disk
	 * 
	 * @param idnexFileName
	 * @throws IOException
	 */
	public static void saveSearcher(CIndexTree searcher, String indexFileName)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(indexFileName));
		writer.write(searcher.nodeCount + "\n");
		writeNode(searcher.rootFeature, writer);
		writer.close();
	}

	private static void writeNode(CIndexTreeNode node, BufferedWriter writer)
			throws IOException {
		writer.write(node.toNodeString() + "\n");
		if (node.getLeft() == null)
			writer.newLine();
		else
			writeNode(node.getLeft(), writer);
		if (node.getRight() == null)
			writer.newLine();
		else
			writeNode(node.getRight(), writer);
	}

}
