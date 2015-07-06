package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.experiments.BBPrepare;
import edu.psu.chemxseer.structure.setcover.experiments.SolverFactory;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.InputType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.experiments.SolverFactory.SolverType;
import edu.psu.chemxseer.structure.setcover.impl.StreamingAlgorithm_InvertedIndex;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.subsearch.FGindex.SubSearch_FGindexBuilder;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_GindexBuilder;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorL;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimple;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The Index Construction Experiments: (1) Given the baseFolder, containing
 * prepared data DataAIDS and TrainQuery (2) Construct the several indexed with
 * distinct names (3) The number of features selected for indexing is adjustable
 * (4) Support the run-indexes with various testing queries
 * 
 * @author dayuyuan
 * 
 */
public class IndexConstruction {
	/**
	 * Assumption: [1] Two Folders "AIDS_10K_0.005Q", "AIDS_10K_0.003Q" [2] Each
	 * contains DataAIDS, TrainQuery and TestQuery [3] Each contains
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {
		if (args == null || args.length == 0)
			return;
		else {
			BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
			String baseName = "/data/home/duy113/VLDBSetCover3/";
			String baseName5 = baseName + "AIDS_10K_0.005Q/";
			String baseName3 = baseName + "AIDS_10K_0.003Q/";
			writer.write("Build Original Index5\n");
			buildOriginalIndex(writer, baseName5, true);
			writer.write("Run Index5 with Query 3\n");
			runOriginalIndex(writer, baseName5, baseName3);

			writer.write("Build Original Index3\n");
			buildOriginalIndex(writer, baseName3, false);
			writer.write("Run Index3 with Query 5\n");
			runOriginalIndex(writer, baseName3, baseName5);
			writer.flush();
			writer.close();
		}
	}

	/**
	 * Construct Indexes
	 * 
	 * @param writer
	 * @param baseName
	 * @param buildQueryInDependent
	 *            : build the Gindex and FGindex if true
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void buildOriginalIndex(BufferedWriter writer,
			String baseName, boolean buildQueryInDependent) throws IOException,
			ParseException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseName
				+ "DataAIDS", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(baseName
				+ "TestQuery", MyFactory.getDFSCoder());
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(baseName
				+ "TrainQuery", MyFactory.getDFSCoder());
		double minSupport = 0.02; // Adjustable
		int K = 2000; // Adjustable
		BBPrepare.buildSubSearchBB(baseName);

		float[] beforeStatus = new float[] { (float) minSupport, K,
				gDB.getTotalNum(), 0 };
		float[] updateStatus = new float[11];
		float[] runIndexStatus = new float[0];
		writer.write("Build Indexes within BaseName: " + baseName + "\n");
		if (buildQueryInDependent) {
			// 1. Build GIndexDF
			System.out.println("Build & Run GIndex");
			ISearcher gIndex = buildGIndexDF(gDB, baseName, minSupport,
					updateStatus, "");
			runIndexStatus = Util.runIndex(gIndex, testQuery);
			String str = Util.stateToString(Util.joinArray(beforeStatus,
					updateStatus, runIndexStatus));
			writer.write(str);
			writer.flush();

			// 2. Build FGIndexTCFG
			System.out.println("Build & Run FGIndex");
			ISearcher fgIndex = buildFGindex(gDB, baseName, 0.03, updateStatus,
					"");
			runIndexStatus = Util.runIndex(fgIndex, testQuery);
			str = Util.stateToString(Util.joinArray(beforeStatus, updateStatus,
					runIndexStatus));
			writer.write(str);
			writer.flush();
		}
		// 3. Batch-max-coverage
		beforeStatus[3] = gDB.getTotalNum();
		Arrays.fill(updateStatus, 0);
		updateStatus = buildBatchCoverIndex(baseName, gDB, trainQuery,
				minSupport, K);
		runIndexStatus = Util.runIndex(baseName + "GreedyDecompose/", gDB,
				testQuery);
		String str = Util.stateToString(Util.joinArray(beforeStatus,
				updateStatus, runIndexStatus));
		writer.write(str);
		writer.flush();
		// 4. stream2
		str = buildSwapIndex(baseName, gDB, trainQuery, testQuery, baseName
				+ "stream2/", K, minSupport, 0);
		writer.write(str);
		writer.flush();
		// 5. stream3
		str = buildSwapIndex(baseName, gDB, trainQuery, testQuery, baseName
				+ "stream3/", K, minSupport, 1);
		writer.write(str);
		writer.flush();
		// 6. streama
		str = buildSwapIndex(baseName, gDB, trainQuery, testQuery, baseName
				+ "stream99/", K, minSupport, 0.99);
		writer.write(str);
		writer.flush();
	}

	public static void runOriginalIndex(BufferedWriter writer,
			String indexBaseName, String queryBaseName) throws IOException,
			ParseException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(indexBaseName
				+ "DataAIDS", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(queryBaseName
				+ "TestQuery", MyFactory.getDFSCoder());
		// 1. Build GIndexDF
		System.out.println("Build & Run GIndex");
		String str = Util.stateToString(Util.runIndex(indexBaseName
				+ "LindexDF/", gDB, testQuery));
		writer.write(str);
		writer.flush();

		// 2. Build FGIndexTCFG
		System.out.println("Build & Run FGIndex");
		str = Util.stateToString(Util.runIndex(indexBaseName + "LindexTCFG/",
				gDB, testQuery));
		writer.write(str);
		writer.flush();
		// 3. Batch-max-coverage
		/*
		 * String str = Util.stateToString(Util.runIndex(indexBaseName +
		 * "GreedyDecompose/", gDB, testQuery)); writer.write(str);
		 * writer.flush(); //4. stream2 str =
		 * Util.stateToString(Util.runIndex(indexBaseName + "stream2/", gDB,
		 * testQuery)); writer.write(str); writer.flush(); //5. stream3 str =
		 * Util.stateToString(Util.runIndex(indexBaseName + "stream3/", gDB,
		 * testQuery)); writer.write(str); writer.flush(); //6. streama str =
		 * Util.stateToString(Util.runIndex(indexBaseName + "stream99/", gDB,
		 * testQuery)); writer.write(str); writer.flush();
		 */
	}

	static ISearcher buildGIndexDF(GraphDatabase_OnDisk gDB, String baseFolder,
			double minFreq, float[] status, String nameSuffix)
			throws IOException, ParseException {
		String temp = baseFolder + "GindexDF" + nameSuffix + "/";
		File folder = new File(temp);
		if (!folder.exists())
			folder.mkdirs();
		// 1. Mine Features
		long start = System.currentTimeMillis();

		MemoryConsumptionCal.runGC();
		double mem = MemoryConsumptionCal.usedMemoryinMB();
		PostingFeatures candidateFeatures = FeatureProcessorG
				.frequentSubgraphMining(gDB.getDBFileName(), temp + "patterns",
						temp + "postings", minFreq, 4, 10, gDB.getParser());
		long end = System.currentTimeMillis();
		status[4] = end - start;
		// 2. Build Index
		start = System.currentTimeMillis();
		SubSearch_GindexBuilder builder = new SubSearch_GindexBuilder();
		builder.buildIndexReal(candidateFeatures, gDB, false, temp, temp
				+ "GPatterns", temp + "GPostings", MyFactory.getUnCanDFS(),
				false);
		end = System.currentTimeMillis();
		status[1] = end - start;
		// 3. Building LindexDF
		// 0. Create Folder
		String lindexBase = baseFolder + "LindexDF" + nameSuffix + "/";
		File lindexFolder = new File(lindexBase);
		if (!lindexFolder.exists())
			lindexFolder.mkdirs();
		// 1. Use DF features
		start = System.currentTimeMillis();
		String gIndexPatterns = temp + "/GPatterns";
		NoPostingFeatures<IOneFeature> features = new NoPostingFeatures<IOneFeature>(
				gIndexPatterns,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		NoPostingFeatures_Ext<IOneFeature> lindexFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				features);
		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple index = lindexBuilder.buildIndex(lindexFeatures,
				gDB, lindexBase, MyFactory.getUnCanDFS());
		end = System.currentTimeMillis();
		status[2] = end - start;
		status[0] = status[1] + status[2] + status[4];

		MemoryConsumptionCal.runGC();
		status[5] = status[6] = (float) (MemoryConsumptionCal.usedMemoryinMB() - mem);
		status[7] = candidateFeatures.getFeatures().getfeatureNum();
		status[8] = features.getfeatureNum();
		return index;
	}

	static ISearcher buildFGindex(GraphDatabase_OnDisk gDB, String baseFolder,
			double minFreq, float[] status, String nameSuffix)
			throws IOException, ParseException {
		// 0. Create Folder
		String temp = baseFolder + "FGindex" + nameSuffix + "/";
		File folder = new File(temp);
		if (!folder.exists())
			folder.mkdirs();
		// 1. Mine Frequent Features
		long start = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double mem = MemoryConsumptionCal.usedMemoryinMB();
		PostingFeatures allFeatures = FeatureProcessorFG
				.frequentSubgraphMining(gDB.getDBFileName(), temp + "patterns",
						temp + "postings", minFreq, 10, gDB.getParser());
		long end = System.currentTimeMillis();
		status[4] = end - start;
		// 2. Mine Frequent TCFG & Construct Index
		start = System.currentTimeMillis();
		SubSearch_FGindexBuilder builder = new SubSearch_FGindexBuilder();
		builder.buildIndex(allFeatures.getFeatures(), gDB, temp,
				MyFactory.getUnCanDFS());
		end = System.currentTimeMillis();
		status[1] = end - start;
		// 0. Create Folder
		String lindexBase = baseFolder + "LindexTCFG" + nameSuffix + "/";
		File lindexFolder = new File(lindexBase);
		if (!lindexFolder.exists())
			lindexFolder.mkdirs();
		// 1. Mine-edge features & load previous mined FG features
		start = System.currentTimeMillis();
		String fgFeatures = temp + "StatusRecordedFeatures";
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				gDB.getDBFileName(), temp + "edge", temp + "edgePosting",
				gDB.getParser());
		NoPostingFeatures<IOneFeature> freqFeatures = new NoPostingFeatures<IOneFeature>(
				fgFeatures,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));

		// 2. Combine this two features
		NoPostingFeatures<IOneFeature> features = FeatureProcessorL
				.mergeFeatures(freqFeatures, edgeFeatures.getFeatures());
		NoPostingFeatures_Ext<IOneFeature> selectedFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				new NoPostingFeatures<IOneFeature>(null,
						features.getSelectedFeatures(), false));
		/*
		 * NoPostingFeatures_Ext<OneFeatureImpl> onDiskFeatures = new
		 * NoPostingFeatures_Ext<OneFeatureImpl>(new
		 * NoPostingFeatures<OneFeatureImpl>(null,
		 * features.getUnSelectedFeatures(),false));
		 */
		// 3. Build the L-plus index with all those features
		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple result = lindexBuilder.buildIndex(
				selectedFeatures, gDB, lindexBase, MyFactory.getUnCanDFS());
		end = System.currentTimeMillis();
		status[2] = end - start;
		status[0] = status[1] + status[2] + status[4];
		MemoryConsumptionCal.runGC();
		status[5] = status[6] = (float) (MemoryConsumptionCal.usedMemoryinMB() - mem);
		status[7] = allFeatures.getFeatures().getfeatureNum();
		status[8] = selectedFeatures.getfeatureNum();
		return result;
	}

	static float[] buildBatchCoverIndex(String baseName, IGraphDatabase gDB,
			IGraphDatabase qDB, double minSupport, int K) throws IOException,
			ParseException {
		System.out
				.println("\n+ Greedy Inverted Index: decomposed preselected status, decomposed index");
		long startT = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double mem = MemoryConsumptionCal.usedMemoryinMB();

		String indexFolder = baseName + "GreedyDecompose/";
		File temp = new File(indexFolder);
		if (!temp.exists())
			temp.mkdirs();

		PostingFeaturesMultiClass freqFeatures = FeatureProcessorDuralClass
				.frequentSubgraphMining(gDB, qDB, gDB.getParser(), baseName,
						minSupport, -1, 10);
		long endT = System.currentTimeMillis();

		IMaxCoverSolver solver = SolverFactory.getSolver(freqFeatures,
				SolverType.GreedyIndex, AppType.subSearch,
				StatusType.decomposePreSelect, InputType.inMem, "temp1", 0);
		float[] status = Util.mineFeatureWithMaxSolver(solver, indexFolder
				+ "GreedyDecomposePreSelect", K);
		System.out.println("How many patterns enuemrated: "
				+ solver.getEnumeratedPatternNum());
		solver = null;

		status[0] = System.currentTimeMillis() - startT;
		status[4] += endT - startT;
		MemoryConsumptionCal.runGC();
		status[5] = (float) (MemoryConsumptionCal.usedMemoryinMB() - mem);
		status[7] = freqFeatures.getFeatures().getfeatureNum();
		Util.buildLindexGivenMaxCoverSelectedFeatures(gDB, indexFolder,
				indexFolder + "GreedyDecomposePreSelect");
		return status;
	}

	static String buildSwapIndex(String baseName, IGraphDatabase gDB,
			IGraphDatabase qDB, IGraphDatabase testQuery, String indexFolder,
			int K, double minSupport, double lambda) throws IOException,
			ParseException {
		GSpanMiner_MultiClass_Iterative gen = FeatureProcessorDuralClass
				.getPatternEnumerator(gDB, qDB, minSupport, -1, 10);
		float[] beforeStat = new float[4];
		beforeStat[0] = (float) minSupport;
		beforeStat[1] = K;
		beforeStat[2] = gen.getClassGraphCount()[0];
		beforeStat[3] = gen.getClassGraphCount()[1];

		StreamingAlgorithm_InvertedIndex streamSolver = (StreamingAlgorithm_InvertedIndex) SolverFactory
				.getStreamSolver(gen, SolverType.SwapIndexBB,
						AppType.subSearch, baseName,
						StatusType.decomposePreSelectAdv, false, lambda);

		File temp = new File(indexFolder);
		if (!temp.exists())
			temp.mkdirs();

		String featureFileName = indexFolder + "SwapIndexBB_DecompPreAdv";
		float[] mineStat = Util.mineFeatureWithMaxSolver(streamSolver,
				featureFileName, K);
		System.out.println("How many patterns enuemrated: "
				+ streamSolver.getEnumeratedPatternNum());
		MemoryConsumptionCal.runGC();
		Util.buildLindexGivenMaxCoverSelectedFeatures(gDB, indexFolder,
				featureFileName);

		float[] runExpStat = Util.runIndex(indexFolder, gDB, testQuery);
		return Util.stateToString(Util.joinArray(beforeStat, mineStat,
				runExpStat));
	}

}
