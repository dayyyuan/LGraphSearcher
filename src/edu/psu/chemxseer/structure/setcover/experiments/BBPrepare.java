package edu.psu.chemxseer.structure.setcover.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_Gindex;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_GindexBuilder;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;

public class BBPrepare {
	public static void buildSupSearchBB(String baseName) throws IOException,
			ParseException {
		String indexFolder = baseName + "GindexForBB/";
		File temp = new File(indexFolder);
		if (!temp.exists())
			temp.mkdirs();
		SubSearch_GindexBuilder builder = new SubSearch_GindexBuilder();
		SubSearch_Gindex gIndex = null;
		PostingFeatures candidateFeatures = FeatureProcessorG
				.frequentSubgraphMining(baseName + "TrainQuery", indexFolder
						+ "patterns", indexFolder + "postings", 0.05, 4, 10,
						MyFactory.getSmilesParser());
		// 0.2. Build Index
		gIndex = builder.buildIndex(
				candidateFeatures,
				new GraphDatabase_OnDisk(baseName + "TrainQuery", MyFactory
						.getSmilesParser()), false, indexFolder, indexFolder
						+ "GPatterns", indexFolder + "GPostings", MyFactory
						.getSmilesParser());
		// task one: for each "data2", how many data1 containing it

		IGraphDatabase gDB = new GraphDatabase_OnDisk(baseName + "DataAIDS",
				MyFactory.getDFSCoder());
		int[] gQCovered = new int[gDB.getTotalNum()];

		for (int gID = 0; gID < gDB.getTotalNum(); gID++) {
			if (gID % 1000 == 0)
				System.out.println(gID);
			gQCovered[gID] = gIndex.getAnswer(gDB.findGraph(gID), new long[4],
					new int[2]).size();
		}

		// serialize to files
		BufferedWriter writerOne = new BufferedWriter(new FileWriter(baseName
				+ "gQCovered"));
		writerOne.write(gQCovered.length + "\n");
		for (int count : gQCovered)
			writerOne.write(count + "\n");
		writerOne.flush();
		writerOne.close();

	}

	public static void buildSubSearchBB(String baseName) throws IOException,
			ParseException {
		String indexFolder = baseName + "GindexForBB/";
		File temp = new File(indexFolder);
		if (!temp.exists())
			temp.mkdirs();
		SubSearch_GindexBuilder builder = new SubSearch_GindexBuilder();
		SubSearch_Gindex gIndex = null;
		PostingFeatures candidateFeatures = FeatureProcessorG
				.frequentSubgraphMining(baseName + "DataAIDS", indexFolder
						+ "patterns", indexFolder + "postings", 0.05, 4, 10,
						MyFactory.getSmilesParser());
		// 0.2. Build Index
		gIndex = builder.buildIndex(
				candidateFeatures,
				new GraphDatabase_OnDisk(baseName + "DataAIDS", MyFactory
						.getSmilesParser()), false, indexFolder, indexFolder
						+ "GPatterns", indexFolder + "GPostings", MyFactory
						.getSmilesParser());
		// task one: for each "data2", how many data1 containing it
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(baseName
				+ "TrainQuery", MyFactory.getDFSCoder());
		int[] qGCovered = new int[trainQuery.getTotalNum()];
		String[] qStrings = new String[trainQuery.getTotalNum()];

		for (int qID = 0; qID < trainQuery.getTotalNum(); qID++) {
			if (qID % 1000 == 0)
				System.out.println(qID);
			String qString = MyFactory.getDFSCoder().serialize(
					trainQuery.findGraph(qID));
			qStrings[qID] = qString;
			qGCovered[qID] = gIndex.getAnswer(trainQuery.findGraph(qID),
					new long[4], new int[2]).size();
		}

		// task two: for each "trainQuery", how may trainQuery isomorphic to
		// itself
		Map<String, Integer> counter = new HashMap<String, Integer>();
		for (int qID = 0; qID < trainQuery.getTotalNum(); qID++) {
			String qString = qStrings[qID];
			if (counter.containsKey(qString))
				counter.put(qString, counter.get(qString) + 1);
			else
				counter.put(qString, 1);
		}
		int[] qEqual = new int[trainQuery.getTotalNum()];
		for (int w = 0; w < trainQuery.getTotalNum(); w++)
			qEqual[w] = counter.get(qStrings[w]);

		// serialize to files
		BufferedWriter writerOne = new BufferedWriter(new FileWriter(baseName
				+ "qGCovered"));
		writerOne.write(qGCovered.length + "\n");
		for (int count : qGCovered)
			writerOne.write(count + "\n");
		writerOne.flush();
		writerOne.close();
		BufferedWriter writerTwo = new BufferedWriter(new FileWriter(baseName
				+ "qEqual"));
		writerTwo.write(qEqual.length + "\n");
		for (int w = 0; w < qEqual.length; w++) {
			writerTwo.write((qEqual[w] - 1) + "\n");
		}
		writerTwo.flush();
		writerTwo.close();
	}

}
