package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.MathException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.query.InFrequentQueryGenerater2;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorL;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimple;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;

public class IndexUpdateForDB {

	public static void main(String[] args) throws IOException, ParseException,
			MathException {
		if (args == null || args.length == 0)
			return;
		else {
			BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
			String baseName = "/data/home/duy113/VLDBSetCover3/";
			String baseName5 = baseName + "AIDS_10K_0.005Q/";
			String baseNameNCI = baseName + "DataChangeNCI/";

			// 1. Assume that the DataAIDS & DataNCI are in the folder Ori,
			// Generated the Mixed Dataset
			// preProcessing0(baseNameNCI + "Ori/");
			// preProcessDBChanage(baseNameNCI);
			// 2. Build new Indexes on the New Graph Data Set
			IndexConstruction.buildOriginalIndex(writer, baseNameNCI, false);
			// 3. Re-base the old indexes and see their performance
			reconstructIndexForNewDB(writer, baseNameNCI, baseName5);
			// 4. Update the index stored in _newDB
			updateIndexForNewDB(writer, baseNameNCI);
			writer.flush();
			writer.close();
		}
	}

	public static void reconstructIndexForNewDB(BufferedWriter writer,
			String baseNameNew, String baseNameOld)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, ParseException {
		writer.write("StartRebased of Indexes for  All Data \n");
		// 1. Rebuild LindexDFNewDB
		String str = rebaseLindexDF(baseNameNew, baseNameOld);
		writer.write(str);
		writer.flush();
		// 2. Rebuild LindexTCFGNewDB
		str = rebaseLindexTCFG(baseNameNew, baseNameOld);
		writer.write(str);
		writer.flush();
		// 3. Rebuild Stream
		rebaseLindexStream(writer, baseNameNew, baseNameOld);
	}

	private static String rebaseLindexDF(String baseNameNCI, String featureBase)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, ParseException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseNameNCI
				+ "DataAIDS", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(baseNameNCI
				+ "TestQuery", MyFactory.getDFSCoder());
		// 1. First step, find all the patterns should be indexed
		String gIndexPatterns = featureBase + "GindexDF/GPatterns";
		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		String lindexBase = baseNameNCI + "LindexDF_newDB/";
		File temp = new File(lindexBase);
		if (!temp.exists())
			temp.mkdirs();
		NoPostingFeatures<IOneFeature> features = new NoPostingFeatures<IOneFeature>(
				gIndexPatterns,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				gDB.getDBFileName(), lindexBase + "edge", lindexBase
						+ "edgePosting", gDB.getParser());
		features = FeatureProcessorL.mergeFeatures(features,
				edgeFeatures.getFeatures());
		// 2. Construct the Index
		NoPostingFeatures_Ext<IOneFeature> lindexFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				features);
		SubSearch_LindexSimple index = lindexBuilder.buildIndex(lindexFeatures,
				gDB, lindexBase, MyFactory.getUnCanDFS());
		float[] status = Util.runIndex(index, testQuery);
		String str = Util.stateToString(status);
		return str;
	}

	private static String rebaseLindexTCFG(String baseNameNCI,
			String oldBaseName) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseNameNCI
				+ "DataAIDS", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(baseNameNCI
				+ "TestQuery", MyFactory.getDFSCoder());

		String fgIndexPattern = oldBaseName + "FGindex/StatusRecordedFeatures";
		String lindexBase = baseNameNCI + "LindexTCFG_newDB/";
		File temp = new File(lindexBase);
		if (!temp.exists())
			temp.mkdirs();
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				gDB.getDBFileName(), lindexBase + "edge", lindexBase
						+ "edgePosting", gDB.getParser());
		NoPostingFeatures<IOneFeature> freqFeatures = new NoPostingFeatures<IOneFeature>(
				fgIndexPattern,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		NoPostingFeatures<IOneFeature> features = FeatureProcessorL
				.mergeFeatures(freqFeatures, edgeFeatures.getFeatures());
		NoPostingFeatures_Ext<IOneFeature> selectedFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				new NoPostingFeatures<IOneFeature>(null,
						features.getSelectedFeatures(), false));

		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple index = lindexBuilder.buildIndex(
				selectedFeatures, gDB, lindexBase, MyFactory.getUnCanDFS());
		float[] status = Util.runIndex(index, testQuery);
		String str = Util.stateToString(status);
		return str;
	}

	private static void rebaseLindexStream(BufferedWriter writer,
			String baseNameNew, String baseNameOld) throws IOException,
			ParseException {
		GraphDatabase_InMem gDBNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "DataAIDS",
						MyFactory.getSmilesParser()));
		GraphDatabase_InMem testQueryNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "TestQuery",
						MyFactory.getDFSCoder()));

		String lindexFolder = baseNameOld + "GreedyDecompose/";
		String newLindexFolder = baseNameNew + "GreedyDecompose_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		SubSearch_LindexSimple index = Util.buildLindexWithNewDB(gDBNew,
				baseNameNew + "DataAIDS", newLindexFolder, lindexFolder
						+ "GreedyDecomposePreSelect");
		float[] status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();

		lindexFolder = baseNameOld + "stream2/";
		newLindexFolder = baseNameNew + "stream2_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		index = Util.buildLindexWithNewDB(gDBNew, baseNameNew + "DataAIDS",
				newLindexFolder, lindexFolder + "SwapIndexBB_DecompPreAdv");
		status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();

		lindexFolder = baseNameOld + "stream3/";
		newLindexFolder = baseNameNew + "stream3_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		index = Util.buildLindexWithNewDB(gDBNew, baseNameNew + "DataAIDS",
				newLindexFolder, lindexFolder + "SwapIndexBB_DecompPreAdv");
		status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();

		lindexFolder = baseNameOld + "stream99/";
		newLindexFolder = baseNameNew + "stream99_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		index = Util.buildLindexWithNewDB(gDBNew, baseNameNew + "DataAIDS",
				newLindexFolder, lindexFolder + "SwapIndexBB_DecompPreAdv");
		status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();

	}

	public static void updateIndexForNewDB(BufferedWriter writer,
			String baseNameNew) throws IOException, ParseException {
		GraphDatabase_InMem gDBNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "DataAIDS",
						MyFactory.getSmilesParser()));
		GraphDatabase_InMem testQueryNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "TestQuery",
						MyFactory.getDFSCoder()));

		GraphDatabase_InMem trainQueryNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "TrainQuery",
						MyFactory.getDFSCoder()));

		double minSupport = 0.02;
		int topGCount = -1;
		StatusType sType = StatusType.decomposePreSelectAdv;

		String lindexFolder = baseNameNew + "LindexDF_newDB/";
		String newLindexFolder = baseNameNew + "LindexDFStream99/";
		String str = IndexUpdateForQueries.doUpdate(sType, gDBNew, topGCount,
				trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
				minSupport, 0.99);
		writer.write(str);
		writer.flush();

		lindexFolder = baseNameNew + "LindexTCFG_newDB/";
		newLindexFolder = baseNameNew + "LindexTCFGStream99/";
		str = IndexUpdateForQueries.doUpdate(sType, gDBNew, topGCount,
				trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
				minSupport, 0.99);
		writer.write(str);
		writer.flush();

		lindexFolder = baseNameNew + "GreedyDecompose_newDB/";
		newLindexFolder = baseNameNew + "GreedyDecomposeStream99/";
		str = IndexUpdateForQueries.doUpdate(sType, gDBNew, topGCount,
				trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
				minSupport, 0.99);
		writer.write(str);
		writer.flush();

		/*
		 * writer.write("StartUpdate of Stream Indexes for NewDB \n");
		 * newLindexFolder = baseNameNew + "stream2_Update/"; lindexFolder =
		 * baseNameNew + "stream2_newDB/";
		 * System.out.println("Start Updating Lindex with NewGDB: " +
		 * newLindexFolder); str = IndexUpdateForQueries.doUpdate(sType, gDBNew,
		 * trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
		 * minSupport, 0, threshold); writer.write(str); writer.flush();
		 * 
		 * newLindexFolder = baseNameNew + "stream3_Update/"; lindexFolder =
		 * baseNameNew + "stream3_newDB/";
		 * System.out.println("Start Updating Lindex with NewGDB: " +
		 * newLindexFolder); str = IndexUpdateForQueries.doUpdate(sType, gDBNew,
		 * trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
		 * minSupport, 1, threshold); writer.write(str); writer.flush();
		 * 
		 * newLindexFolder = baseNameNew + "stream99_Update/"; lindexFolder =
		 * baseNameNew + "stream99_newDB/";
		 * System.out.println("Start Updating Lindex with NewGDB: " +
		 * newLindexFolder); str = IndexUpdateForQueries.doUpdate(sType, gDBNew,
		 * trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
		 * minSupport, 0.99, threshold); writer.write(str); writer.flush();
		 */
	}

	public static void measureDifference(BufferedWriter writer,
			String baseNameNew, String baseNameOld) throws IOException {
		String rebasedIndexName = baseNameNew + "stream99_newDB/";
		GraphDatabase_InMem gDBNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "DataAIDS",
						MyFactory.getSmilesParser()));
		String originalIndexName = baseNameOld + "stream99";
		GraphDatabase_InMem gDBOld = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "DataAIDS",
						MyFactory.getSmilesParser()));

		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple lindexNew = lindexBuilder.loadIndex(gDBNew,
				rebasedIndexName, MyFactory.getDFSCoder(), true);
		SubSearch_LindexSimple lindexOri = lindexBuilder.loadIndex(gDBOld,
				originalIndexName, MyFactory.getDFSCoder(), true);

		Map<String, Integer> newPairs = lindexNew.getKeyValuePairs();
		Map<String, Integer> oriPairs = lindexOri.getKeyValuePairs();

		for (Entry<String, Integer> oneEntry : oriPairs.entrySet()) {
			String gString = oneEntry.getKey();
			if (newPairs.containsKey(gString)) {
				writer.write(gString + "," + oneEntry.getValue() + ","
						+ newPairs.get(gString) + "\n");
			}
		}
		writer.flush();
	}

	public static void preProcessing0(String NCIBaseName) throws IOException,
			ParseException, MathException {
		// String dataName =
		// "/data/home/duy113/NCIData/NCI-Open_2012-05-01.smiles";
		String dataName = "/data/home/duy113/VLDBJExp/LargeScaleExp/G20/GraphDB20";
		// sample the database graph, with graph edge > 40;
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dataName,
				MyFactory.getSmilesParser());
		List<String> gs = new ArrayList<String>();
		for (int i = 0; i < gDB.getTotalNum(); i++) {
			if (i == 198003)
				continue;
			// Graph g = gDB.findGraph(i);
			gs.add(gDB.findGraphString(i));
		}
		File temp = new File(NCIBaseName);
		if (!temp.exists())
			temp.mkdirs();
		System.out.println("How Many Graph meet the Criteria: " + gs.size());
		RandomChoseDBGraph.randomlyChooseDBGraph(new GraphDatabase_InMem(gs,
				MyFactory.getSmilesParser()), 40000, NCIBaseName + "DataNCI");
		// generate queries as well.
		gDB = new GraphDatabase_OnDisk(NCIBaseName + "DataNCI",
				MyFactory.getSmilesParser());
		InFrequentQueryGenerater2 queryGen = new InFrequentQueryGenerater2();
		queryGen.generateInFrequentQueries2(4, 100, 10000 + 2000, 0.008, gDB,
				0, NCIBaseName + "QueryNCI_Together");
		IGraphDatabase Data2_Together = new GraphDatabase_InMem(
				new NoPostingFeatures<IOneFeature>(NCIBaseName
						+ "QueryNCI_Together", MyFactory
						.getFeatureFactory(FeatureFactoryType.OneFeature)));
		RandomChoseDBGraph.randomlySplitDBGraph(Data2_Together, 10000,
				NCIBaseName + "TrainQueryNCI", NCIBaseName + "TestQueryNCI");
	}

	public static void preProcessDBChanage(String baseName) throws IOException {
		double[] ratio = new double[] { 0, 0.2, 0.4, 0.6, 0.8 };

		IGraphDatabase db = new GraphDatabase_OnDisk(baseName + "Ori/DataAIDS",
				MyFactory.getSmilesParser());
		IGraphDatabase db2 = new GraphDatabase_OnDisk(baseName + "Ori/DataNCI",
				MyFactory.getSmilesParser());
		IGraphDatabase TrainQuery = new GraphDatabase_OnDisk(baseName
				+ "Ori/TrainQuery", MyFactory.getDFSCoder());
		IGraphDatabase TrainQuery2 = new GraphDatabase_OnDisk(baseName
				+ "Ori/TrainQueryNCI", MyFactory.getDFSCoder());
		IGraphDatabase TestQuery = new GraphDatabase_OnDisk(baseName
				+ "Ori/TestQuery", MyFactory.getDFSCoder());
		IGraphDatabase TestQuery2 = new GraphDatabase_OnDisk(baseName
				+ "Ori/TestQueryNCI", MyFactory.getDFSCoder());

		for (int i = 1; i < 5; i++) {
			String foldName = baseName + i + "/";
			double rat = ratio[i];
			File temp = new File(foldName);
			if (!temp.exists())
				temp.mkdirs();
			RandomChoseDBGraph.randomlyChooseDBGraph(db,
					(int) (db.getTotalNum() * (1 - rat)), foldName
							+ "DataAIDS_" + rat);
			RandomChoseDBGraph.randomlyChooseDBGraph(db2,
					(int) (db2.getTotalNum() * rat), foldName + "DataNCI_"
							+ rat);
			RandomChoseDBGraph
					.merge(new GraphDatabase_OnDisk(foldName + "DataAIDS_"
							+ rat, MyFactory.getSmilesParser()),
							new GraphDatabase_OnDisk(foldName + "DataNCI_"
									+ rat, MyFactory.getSmilesParser()),
							foldName + "DataAIDS");

			RandomChoseDBGraph.randomlyChooseDBGraph(TrainQuery,
					(int) (TrainQuery.getTotalNum() * (1 - rat)), foldName
							+ "TrainQuery_" + rat);
			RandomChoseDBGraph.randomlyChooseDBGraph(TrainQuery2,
					(int) (TrainQuery2.getTotalNum() * rat), foldName
							+ "TrainQueryNCI_" + rat);
			RandomChoseDBGraph.merge(new GraphDatabase_OnDisk(foldName
					+ "TrainQuery_" + rat, MyFactory.getDFSCoder()),
					new GraphDatabase_OnDisk(foldName + "TrainQueryNCI_" + rat,
							MyFactory.getSmilesParser()), foldName
							+ "TrainQuery");

			RandomChoseDBGraph.randomlyChooseDBGraph(TestQuery,
					(int) (TestQuery.getTotalNum() * (1 - rat)), foldName
							+ "TestQuery_" + rat);
			RandomChoseDBGraph.randomlyChooseDBGraph(TestQuery2,
					(int) (TestQuery2.getTotalNum() * rat), foldName
							+ "TestQueryNCI_" + rat);
			RandomChoseDBGraph.merge(new GraphDatabase_OnDisk(foldName
					+ "TestQuery_" + rat, MyFactory.getDFSCoder()),
					new GraphDatabase_OnDisk(foldName + "TestQueryNCI_" + rat,
							MyFactory.getSmilesParser()), foldName
							+ "TestQuery");
		}
	}

	public static String doUpdate(StatusType sType, IGraphDatabase newGDB,
			int topGCount, String newDBName, IGraphDatabase newQuery,
			IGraphDatabase testQuery, String featureFileFolder,
			String newLindexFolder, double minSupport, double lambda)
			throws IOException, ParseException {
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		Util.buildLindexWithNewDB(newGDB, newDBName,
				newLindexFolder + "newDB/", featureFileFolder
						+ "SwapIndexBB_DecompPreAdv");
		return IndexUpdateForQueries.doUpdate(sType, newGDB, topGCount,
				newQuery, testQuery, newLindexFolder + "newDB/",
				newLindexFolder + "newDBQuery/", minSupport, lambda);
	}

}
