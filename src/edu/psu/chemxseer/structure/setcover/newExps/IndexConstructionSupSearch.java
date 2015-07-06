package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

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
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorL;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.GPTree.GPFeatureMiner;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_LWFullBuilder;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_Lindex;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class IndexConstructionSupSearch {

	public static void buildOriginalIndex(BufferedWriter writer, String baseName)
			throws IOException, ParseException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseName
				+ "DataAIDS", MyFactory.getDFSCoder());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(baseName
				+ "TestQuery", MyFactory.getSmilesParser());
		// IGraphDatabase trainQuery = new GraphDatabase_OnDisk(baseName +
		// "TrainQuery", MyFactory.getSmilesParser());

		double minSupport = 0.005; // Adjustable
		int K = 1000; // Adjustable
		BBPrepare.buildSupSearchBB(baseName);
		float[] beforeStatus = new float[] { (float) minSupport, K,
				gDB.getTotalNum(), 0 };
		float[] updateStatus = new float[11];
		float[] runIndexStatus = new float[0];
		String str = null;
		// 0. Build & Run Greedy Algorithm
		ISearcher gpTree = buildGPTreeIndex(baseName + "GPTree/", gDB,
				minSupport, updateStatus);
		runIndexStatus = Util.runIndex(gpTree, testQuery);
		str = Util.stateToString(Util.joinArray(beforeStatus, updateStatus,
				runIndexStatus));
		writer.write(str);
		writer.flush();
		// 1. Run cIndex algorithm, which is the same as the batch mining
		/*
		 * beforeStatus[3] = gDB.getTotalNum(); Arrays.fill(updateStatus, 0);
		 * updateStatus = buildBatchCoverIndex(baseName, gDB, minSupport, K);
		 * runIndexStatus = Util.runIndexSupSearch(baseName+"GreedyDecompose/",
		 * gDB, testQuery); str =
		 * Util.stateToString(Util.joinArray(beforeStatus, updateStatus,
		 * runIndexStatus)); writer.write(str); writer.flush(); //2. Run
		 * SwapIndex2 str = buildSwapIndex(baseName, gDB, testQuery,
		 * baseName+"stream2/", K, minSupport, 0); writer.write(str);
		 * writer.flush(); //3. Run SwapIndex3 str = buildSwapIndex(baseName,
		 * gDB, testQuery, baseName+"stream3/", K, minSupport, 1);
		 * writer.write(str); writer.flush();
		 */
		// 4. Run SwapIndex4
		// str = buildSwapIndex(baseName, gDB, testQuery, baseName+"stream99/",
		// K, minSupport, 0.99);
		// writer.write(str);
		// writer.flush();
	}

	static public ISearcher buildGPTreeIndex(String GPName,
			GraphDatabase_OnDisk gDB, double minSupport, float[] status)
			throws ParseException, IOException {
		// 0. Create Folder
		File folder = new File(GPName);
		if (!folder.exists())
			folder.mkdirs();
		// 1. Mine Frequent Features
		long start = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double mem = MemoryConsumptionCal.usedMemoryinMB();
		PostingFeatures allFeatures = FeatureProcessorFG
				.frequentSubgraphMining(gDB.getDBFileName(), GPName
						+ "patterns", GPName + "postings", minSupport, 10,
						gDB.getParser());
		long end = System.currentTimeMillis();
		status[4] = end - start;
		// 2. Mine Significant features
		start = System.currentTimeMillis();
		GPFeatureMiner miner = new GPFeatureMiner();
		NoPostingFeatures<IOneFeature> sigFeatures = miner
				.minSignificantFeatures(allFeatures, 1.5).getFeatures();
		sigFeatures.saveFeatures(GPName + "sigFeatures");
		end = System.currentTimeMillis();
		status[1] = end - start;
		// 3. Mine-edge features
		start = System.currentTimeMillis();
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				gDB.getDBFileName(), GPName + "edge", GPName + "edgePosting",
				gDB.getParser());
		// 4. Combine this two features
		NoPostingFeatures<IOneFeature> features = FeatureProcessorL
				.mergeFeatures(sigFeatures, edgeFeatures.getFeatures());
		NoPostingFeatures_Ext<IOneFeature> selectedFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				new NoPostingFeatures<IOneFeature>(null,
						features.getSelectedFeatures(), false));
		// 5. Build the LWFullIndex index with all those features
		SupSearch_LWFullBuilder lindexBuilder = new SupSearch_LWFullBuilder();
		SupSearch_Lindex result = lindexBuilder.buildLindex(selectedFeatures,
				gDB, GPName, true, MyFactory.getUnCanDFS());
		end = System.currentTimeMillis();
		status[2] = end - start;
		status[0] = status[1] + status[2] + status[4];
		MemoryConsumptionCal.runGC();
		status[5] = status[6] = (float) (MemoryConsumptionCal.usedMemoryinMB() - mem);
		status[7] = allFeatures.getFeatures().getfeatureNum();
		status[8] = selectedFeatures.getfeatureNum();
		return result;
	}

	static public float[] buildBatchCoverIndex(String baseName,
			IGraphDatabase gDB, IGraphDatabase qDB, double minSupport, int K)
			throws IOException, ParseException {
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
				SolverType.GreedyIndex, AppType.supSearch,
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
		Util.buildLWindexGivenMaxCoverSelectedFeatures(gDB, indexFolder,
				indexFolder + "GreedyDecomposePreSelect");
		return status;
	}

	static public String buildSwapIndex(String baseName, IGraphDatabase gDB,
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
						AppType.supSearch, baseName,
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
		Util.buildLWindexGivenMaxCoverSelectedFeatures(gDB, indexFolder,
				featureFileName);
		float[] runExpStat = Util
				.runIndexSupSearch(indexFolder, gDB, testQuery);
		return Util.stateToString(Util.joinArray(beforeStat, mineStat,
				runExpStat));
	}
}
