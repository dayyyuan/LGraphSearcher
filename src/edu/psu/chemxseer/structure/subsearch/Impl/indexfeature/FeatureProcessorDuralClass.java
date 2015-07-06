package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.ClassifiedGraph;
import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.iso.UnCanDFS;
import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;

/**
 * This is the Feature Miner for "Set-Cover" feature selection pre-processing
 * 
 * @author dayuyuan
 * 
 */
public class FeatureProcessorDuralClass {
	private FeatureProcessorDuralClass() {
	}

	/**
	 * Merge the two dataset to one
	 * 
	 * @param gDB1
	 * @param gDB2
	 * @param gSerializer
	 * @param mergedFileName
	 * @param classFileName
	 * @throws IOException
	 */
	private static void mergeGraphFile(IGraphDatabase gDB1,
			IGraphDatabase gDB2, GraphParser gSerializer,
			String mergedFileName, String classFileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				mergedFileName));
		BufferedWriter classWriter = new BufferedWriter(new FileWriter(
				classFileName));

		String spliter = " => ";
		int id = 0;
		for (int i = 0; i < gDB1.getTotalNum(); i++, id++) {
			if (gSerializer == gDB1.getParser())
				writer.write(id + spliter + gDB1.findGraphString(i));
			else
				writer.write(id + spliter
						+ gSerializer.serialize(gDB1.findGraph(i)));
			writer.newLine();
			classWriter.write(id + spliter + 1 + "," + 0);
			classWriter.newLine();
		}
		for (int i = 0; i < gDB2.getTotalNum(); i++, id++) {
			if (gSerializer == gDB2.getParser())
				writer.write(id + spliter + gDB2.findGraphString(i));
			else
				writer.write(id + spliter
						+ gSerializer.serialize(gDB2.findGraph(i)));
			writer.newLine();
			classWriter.write(id + spliter + 0 + "," + 1);
			classWriter.newLine();
		}
		writer.close();
		classWriter.close();
	}

	/**
	 * Merge the two graph databases together and return the list of classified
	 * graphs with gDB1 as class one and gDB2 as class two. Also, the graph is
	 * re-numbered
	 * 
	 * @param gDB1
	 * @param gDB2
	 * @return
	 */
	private static List<ClassifiedGraph> mergeGraph(IGraphDatabase gDB1,
			IGraphDatabase gDB2) {
		List<ClassifiedGraph> results = new ArrayList<ClassifiedGraph>(
				gDB1.getTotalNum() + gDB2.getTotalNum());
		float[] freq1 = new float[] { 1.0f, 0f };
		float[] freq2 = new float[] { 0f, 1.0f };
		UnCanDFS parser = MyFactory.getUnCanDFS();
		for (int i = 0; i < gDB1.getTotalNum(); i++) {
			Graph theGraph = gDB1.findGraph(i);
			ClassifiedGraph temp = (ClassifiedGraph) (parser.parse(
					parser.serialize(theGraph), Integer.toString(i),
					MyFactory.getGraphFactory()));
			temp.setClassFrequencies(freq1);
			results.add(temp);
		}
		int size = results.size();
		for (int j = 0; j < gDB2.getTotalNum(); j++) {
			Graph theGraph = gDB2.findGraph(j);
			ClassifiedGraph temp = (ClassifiedGraph) (parser.parse(
					parser.serialize(theGraph), Integer.toString(size + j),
					MyFactory.getGraphFactory()));
			temp.setClassFrequencies(freq2);
			results.add(temp);
		}
		return results;
	}

	/*
	 * private static PostingFeaturesMultiClass frequentSubgraphMining(String
	 * gDBFileName, String classFrequencyFile, String featureFileName, String[]
	 * postingFileNames, GraphParser gParser, double minimumFrequency, double
	 * minimuFrequency2, int maxNonSelectDepth){
	 * 
	 * String[] args = {"-minimumFrequencies="+(-minimumFrequency) + "," +
	 * (-minimuFrequency2), "-maximumFrequencies=100%,100%",
	 * "-maximumFragmentSize="+maxNonSelectDepth, "-graphFile="+gDBFileName,
	 * "-closedFragmentsOnly=false", "-outputFile=temp", "-parserClass=" +
	 * gParser.getClass().getName(), "-classFrequencyFile=" +
	 * classFrequencyFile,
	 * "-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
	 * "-memoryStatistics=false", "-debug=-1"}; return
	 * GSpanMiner_MultiClass.gSpanMining(args, featureFileName,
	 * postingFileNames); }
	 */

	private static PostingFeaturesMultiClass frequentSubgraphMining2(
			String gDBFileName, String classFrequencyFile,
			String featureFileName, String[] postingFileNames,
			GraphParser gParser, double minimumFrequency,
			double minimumFrequency2, int maxNonSelectDepth) {

		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency) + ","
						+ (-minimumFrequency2),
				"-maximumFrequencies=100%,100%",
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-graphFile=" + gDBFileName,
				"-closedFragmentsOnly=false",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-classFrequencyFile=" + classFrequencyFile,
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=-1" };
		return GSpanMiner_MultiClass_Iterative.gSpanMining(args,
				featureFileName, postingFileNames);
	}

	/**
	 * Return a Pattern Enumerator
	 * 
	 * @param gDBFileName
	 * @param classFrequencyFile
	 * @param gParser
	 * @param minimumFrequency
	 * @param minimumFrequency2
	 * @param maxNonSelectDepth
	 * @return
	 */
	private static GSpanMiner_MultiClass_Iterative getPatternEnumerator(
			String gDBFileName, String classFrequencyFile, GraphParser gParser,
			double minimumFrequency, double minimumFrequency2,
			int maxNonSelectDepth) {
		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency) + ","
						+ (-minimumFrequency2),
				"-maximumFrequencies=100%,100%",
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-graphFile=" + gDBFileName,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-classFrequencyFile=" + classFrequencyFile,
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=-1" };

		return GSpanMiner_MultiClass_Iterative.getMiner(args);
	}

	private static GSpanMiner_MultiClass_Iterative getPatternEnumerator(
			List<ClassifiedGraph> inputGraphs, double minimumFrequency,
			double minimumFrequency2, int maxNonSelectDepth) {
		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency) + ","
						+ (-minimumFrequency2),
				"-maximumFrequencies=100%,100%",
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass="
						+ MyFactory.getSmilesParser().getClass().getName(),
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=-1" };

		return GSpanMiner_MultiClass_Iterative.getMiner(args, inputGraphs);
	}

	/**
	 * Merge the gDB&gQuery together and store at baseName + "merge" and
	 * baseName + "class" Then mine features according to the minSupport1/2 and
	 * maxLength Finally, store the posting & feature file at baseName +
	 * feature, baseName + postings The minimum support is set to be -1, if we
	 * want to return all distinct patterns ( 1)
	 * 
	 * @param gDB
	 * @param gQuery
	 * @param graphSerializer
	 * @param baseName
	 * @param minSupt1
	 * @param minSupt2
	 * @param maxLength
	 * @return
	 */
	public static PostingFeaturesMultiClass frequentSubgraphMining(
			IGraphDatabase gDB, IGraphDatabase gQuery,
			GraphParser graphSerializer, String baseName, double minSupt1,
			double minSupt2, int maxLength) {
		// 1. First Step: merge and prepare for the file
		try {
			mergeGraphFile(gDB, gQuery, graphSerializer, baseName + "merge",
					baseName + "class");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 2. Second Step: mine raw frequent features
		String[] postingFiles = new String[4];
		for (int i = 0; i < 4; i++)
			postingFiles[i] = baseName + "posting" + i;
		PostingFeaturesMultiClass rawFeatures = frequentSubgraphMining2(
				baseName + "merge", baseName + "class", baseName + "feature",
				postingFiles, graphSerializer, minSupt1, minSupt2, maxLength);
		return rawFeatures;
	}

	public static GSpanMiner_MultiClass_Iterative getPatternEnumerator(
			IGraphDatabase gDB, IGraphDatabase gQuery,
			GraphParser graphSerializer, String baseName, double minSupt1,
			double minSupt2, int maxLength) {
		// 1. First Step: merge and prepare for the file
		try {
			mergeGraphFile(gDB, gQuery, graphSerializer, baseName + "merge",
					baseName + "class");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 2. Second Step: mine raw frequent features
		return getPatternEnumerator(baseName + "merge", baseName + "class",
				graphSerializer, minSupt1, minSupt2, maxLength);
	}

	public static GSpanMiner_MultiClass_Iterative getPatternEnumerator(
			IGraphDatabase gDB1, IGraphDatabase gDB2, double minSupport1,
			double minSupport2, int maxLength) {
		// 1. First Step: merge and prepare for the file
		List<ClassifiedGraph> graphs = mergeGraph(gDB1, gDB2);
		// 2. Second Step: mine raw frequent features
		return getPatternEnumerator(graphs, minSupport1, minSupport2, maxLength);
	}

	public static PostingFeaturesMultiClass loadFeatures(String baseFolder,
			int datasetSize) {
		String fName = baseFolder + "features";
		String pName = baseFolder + "postings";
		String[] pNames = new String[] { pName + 0, pName + 1, pName + 2,
				pName + 3 };
		// PostingFeaturesMultiClass features =
		// FeatureProcessorDuralClass.frequentSubgraphMining2(datasetFolder +
		// "data_merged",
		// datasetFolder+ "data_class", fName, pNames, MyFactory.getUnCanDFS(),
		// minSupport, -1, 10);
		NoPostingFeatures<OneFeatureMultiClass> tempFeature = new NoPostingFeatures<OneFeatureMultiClass>(
				fName,
				MyFactory.getFeatureFactory(FeatureFactoryType.MultiFeature));
		int[] tempCount = new int[2];
		tempCount[0] = tempCount[1] = datasetSize;
		PostingFeaturesMultiClass features = new PostingFeaturesMultiClass(
				pNames, tempFeature, tempCount);
		return features;
	}
}
