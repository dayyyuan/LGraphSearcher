package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import org.apache.commons.math.MathException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.NormalChoseDBGraph;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * This class is in charge of preparing the experiment data (1) Build the graph
 * database: subgraphs of the queries (2) Randomly sampling the queries, divide
 * into "training" and "testing" queries (3) Generate Synthetic queries & graph
 * database
 * 
 * @author dayuyuan
 * 
 */
public class DataPreparation {

	// public static String baseName = "/data/home/duy113/SupSearchExp/AIDS/";
	// public static String baseName = "/data/santa/SupgraphSearchExp/AIDS/";
	public static String baseName = "/Users/dayuyuan/Documents/workspace/Experiment/";

	public static void main(String[] args) throws IOException, ParseException,
			MathException {
		// AIDSDataset();
		prePareQueries();
	}

	/**
	 * Mine the AIDSDataset
	 * 
	 * @throws IOException
	 */
	public static void AIDSDataset() throws IOException {
		// String queryName = baseName + "QueryFile";
		// IGraphDatabase qDB = new GraphDatabase_OnDisk(queryName,
		// MyFactory.getSmilesParser());
		String dbFileName = baseName + "DBFile";
		// 1. Construct the graph database, mine frequent subgraphs with minimum
		// support 0.5%, maximum support 10%
		// 0.001% failed.
		// PostingFeatures dbFeature =
		// FeatureProcessorFG.frequentSubgraphMining(queryName,
		// dbFileName+"raw", null, 0.005, 50, MyFactory.getSmilesParser());
		NoPostingFeatures<IOneFeature> dbFeatures = new NoPostingFeatures<IOneFeature>(
				dbFileName + "raw",
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		// getStatistics(dbFileName + "raw");
		IGraphDatabase gDB = new GraphDatabase_InMem(dbFeatures);
		for (int i = 10; i > 0; i--) {
			// sample the graph database
			int chooseNum = i * 10000;
			RandomChoseDBGraph.randomlyChooseDBGraph(gDB, chooseNum, dbFileName
					+ i);
			gDB = new GraphDatabase_OnDisk(dbFileName + i,
					MyFactory.getDFSCoder());
		}
	}

	public static void prePareQueries() throws ParseException, IOException,
			MathException {
		// 80, 20 partition of Queries
		String queryName = baseName + "QueryFile";
		String sampleQueryName = baseName + "QueryFile_10000";
		String trainQuery = baseName + "TrainQuery";
		String testQuery = baseName + "TestQuery";
		IGraphDatabase gDBRw = new GraphDatabase_OnDisk(queryName,
				MyFactory.getSmilesParser());
		RandomChoseDBGraph.randomlyChooseDBGraph(gDBRw, 10000, sampleQueryName);
		IGraphDatabase gDB = new GraphDatabase_OnDisk(sampleQueryName,
				MyFactory.getSmilesParser());
		RandomChoseDBGraph.randomlySplitDBGraph(gDB, 8000, trainQuery,
				testQuery);
		// Varying Edges: 15, 25, 35
		NormalChoseDBGraph.sampleGraph(15, 2, gDBRw, trainQuery + 15, 2000);
		NormalChoseDBGraph.sampleGraph(25, 2, gDBRw, trainQuery + 25, 2000);
		NormalChoseDBGraph.sampleGraph(35, 2, gDBRw, trainQuery + 35, 2000);
	}

	/**
	 * Filtering the Raw Subgraphs: (1) minFreq =< freq =< maxFreq (2) minEdge
	 * =< edge =< maxEdgeCount
	 * 
	 * @param rawDBFileName
	 * @param newDBFileName
	 * @param minFreq
	 * @param maxFreq
	 * @param minSize
	 * @param maxSize
	 * @throws IOException
	 */
	/*
	 * private static void filterFrequentSubgraphs(String rawDBFileName, String
	 * newDBFileName, int minFreq, int maxFreq, int minSize, int maxSize) throws
	 * IOException{ //1. Load the Features NoPostingFeatures<IOneFeature>
	 * rawFeatures = new NoPostingFeatures<IOneFeature>(rawDBFileName,
	 * MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature)); //2. Filter
	 * the too frequent subgraphs for(int i = 0; i< rawFeatures.getfeatureNum();
	 * i++){ IOneFeature aFeature = rawFeatures.getFeature(i); int edgeCount =
	 * aFeature.getFeatureGraph().getEdgeCount(); if(aFeature.getFrequency() >
	 * maxFreq || aFeature.getFrequency() < minFreq) aFeature.setUnselected();
	 * else if(edgeCount > maxSize || edgeCount < minSize)
	 * aFeature.setUnselected(); else aFeature.setSelected(); }
	 * NoPostingFeatures<IOneFeature> newFeatures = new
	 * NoPostingFeatures<IOneFeature>(rawFeatures.getSelectedFeatures(), false);
	 * newFeatures.saveFeatures(newDBFileName); }
	 */

	/**
	 * Results: The edge follows normal distribution (min edge = 10, max = 18
	 * for minsupport 0.01) The frequency follows exponential distribution (with
	 * the decrease of the min support, the total number of frequent subgraph
	 * grows exponentially, or according to power law. )
	 * 
	 * @param featureName
	 */
	public static void getStatistics(String featureName) {
		NoPostingFeatures<IOneFeature> rawFeatures = new NoPostingFeatures<IOneFeature>(
				featureName,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		HashMap<Integer, Integer> edgeCount = new HashMap<Integer, Integer>();
		int minEdge = Integer.MAX_VALUE, maxEdge = 0;

		HashMap<Integer, Integer> freqCount = new HashMap<Integer, Integer>();
		int minFreq = Integer.MAX_VALUE, maxFreq = 0;

		for (int i = 0; i < rawFeatures.getfeatureNum(); i++) {
			IOneFeature aFeature = rawFeatures.getFeature(i);
			int edge = aFeature.getFeatureGraph().getEdgeCount();
			if (edge < minEdge)
				minEdge = edge;
			if (edge > maxEdge)
				maxEdge = edge;
			if (edgeCount.containsKey(edge))
				edgeCount.put(edge, edgeCount.get(edge) + 1);
			else
				edgeCount.put(edge, 1);
			int freq = aFeature.getFrequency();
			if (freq < minFreq)
				minFreq = freq;
			if (freq > maxFreq)
				maxFreq = freq;
			if (freqCount.containsKey(freq))
				freqCount.put(freq, freqCount.get(freq) + 1);
			else
				freqCount.put(freq, 1);
		}

		// output
		System.out.println("EdgeCount: " + minEdge + "," + maxEdge);
		for (int i = minEdge; i <= maxEdge; i++) {
			if (edgeCount.get(i) == null)
				System.out.println(0);
			else
				System.out.println(edgeCount.get(i));
		}
		System.out.println("FreqCount: " + minFreq + "," + maxFreq);
		for (int i = minFreq; i <= maxFreq; i++) {
			if (freqCount.get(i) == null)
				System.out.println(0);
			else
				System.out.println(freqCount.get(i));
		}
	}

	/**
	 * Based on the experiment results: The peak is on minEdge + 25, the
	 * distribution is basically normal distribution with mean = minEdge +25,
	 * [min, min+25, min+50]. min = 1 So I can generate 3 data set: normal
	 * distribution with mean 15, 25, 35
	 * 
	 * @param gDB
	 */
	public static void getStatistics(IGraphDatabase gDB) {
		HashMap<Integer, Integer> edgeCount = new HashMap<Integer, Integer>();
		int minEdge = Integer.MAX_VALUE, maxEdge = 0;

		for (int i = 0; i < gDB.getTotalNum(); i++) {
			int edge = gDB.findGraph(i).getEdgeCount();
			if (edge < minEdge)
				minEdge = edge;
			if (edge > maxEdge)
				maxEdge = edge;
			if (edgeCount.containsKey(edge))
				edgeCount.put(edge, edgeCount.get(edge) + 1);
			else
				edgeCount.put(edge, 1);
		}

		// output
		System.out.println("EdgeCount");
		System.out.println(minEdge + ", " + maxEdge);
		for (int i = minEdge; i <= maxEdge; i++) {
			if (edgeCount.get(i) == null)
				System.out.println(0);
			else
				System.out.println(edgeCount.get(i));
		}
	}

}
