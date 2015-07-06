package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.update.IndexUpdator;
import edu.psu.chemxseer.structure.setcover.update.SupSearch_LindexUpdatable;
import edu.psu.chemxseer.structure.setcover.update.SupSearch_LindexUpdatableBuilder;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorL;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_LWFullBuilder;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_Lindex;

public class IndexUpdateForDBSupSearch {

	public static void rebaseGPTree(BufferedWriter writer, String baseNameNCI,
			String oldBaseName) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseNameNCI
				+ "DataAIDS", MyFactory.getDFSCoder());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(baseNameNCI
				+ "TestQuery", MyFactory.getSmilesParser());

		String fgIndexPattern = oldBaseName + "GPTree/sigFeatures";
		String lindexBase = baseNameNCI + "GPTree_newDB/";
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

		SupSearch_LWFullBuilder lindexBuilder = new SupSearch_LWFullBuilder();
		SupSearch_Lindex index = lindexBuilder.buildLindex(selectedFeatures,
				gDB, lindexBase, true, MyFactory.getUnCanDFS());
		float[] status = Util.runIndex(index, testQuery);
		writer.write(Util.stateToString(status));
		writer.flush();
	}

	public static void rebaseLindexStream(BufferedWriter writer,
			String baseNameNew, String baseNameOld) throws IOException,
			ParseException {
		GraphDatabase_InMem gDBNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "DataAIDS",
						MyFactory.getDFSCoder()));
		GraphDatabase_InMem testQueryNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "TestQuery",
						MyFactory.getSmilesParser()));
		String lindexFolder = null;
		String newLindexFolder = null;
		float[] status = null;
		SupSearch_Lindex index = null;

		lindexFolder = baseNameOld + "GreedyDecompose/";
		newLindexFolder = baseNameNew + "GreedyDecompose_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		index = Util.buildLWindexWithNewDB(gDBNew, baseNameNew + "DataAIDS",
				newLindexFolder, lindexFolder + "GreedyDecomposePreSelect");
		status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();

		lindexFolder = baseNameOld + "stream2/";
		newLindexFolder = baseNameNew + "stream2_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		index = Util.buildLWindexWithNewDB(gDBNew, baseNameNew + "DataAIDS",
				newLindexFolder, lindexFolder + "SwapIndexBB_DecompPreAdv");
		status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();

		lindexFolder = baseNameOld + "stream3/";
		newLindexFolder = baseNameNew + "stream3_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		index = Util.buildLWindexWithNewDB(gDBNew, baseNameNew + "DataAIDS",
				newLindexFolder, lindexFolder + "SwapIndexBB_DecompPreAdv");
		status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();

		lindexFolder = baseNameOld + "stream99/";
		newLindexFolder = baseNameNew + "stream99_newDB/";
		System.out.println("Start Building Lindex with NewGDB: "
				+ newLindexFolder);
		index = Util.buildLWindexWithNewDB(gDBNew, baseNameNew + "DataAIDS",
				newLindexFolder, lindexFolder + "SwapIndexBB_DecompPreAdv");
		status = Util.runIndex(index, testQueryNew);
		writer.write(Util.stateToString(status));
		writer.flush();
	}

	public static void updateIndexForNewDB(BufferedWriter writer,
			String baseNameNew) throws IOException, ParseException {
		GraphDatabase_InMem gDBNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "DataAIDS",
						MyFactory.getDFSCoder()));
		GraphDatabase_InMem testQueryNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "TestQuery",
						MyFactory.getSmilesParser()));

		GraphDatabase_InMem trainQueryNew = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(baseNameNew + "TrainQuery",
						MyFactory.getSmilesParser()));

		double minSupport = 0.005;
		int topGCount = -1;
		StatusType sType = StatusType.decomposePreSelectAdv;
		String lindexFolder = null;
		String newLindexFolder = null;
		String str = null;

		lindexFolder = baseNameNew + "GPTree_newDB/";
		newLindexFolder = baseNameNew + "GPTreeStream99/";
		str = doUpdate(sType, gDBNew, topGCount, trainQueryNew, testQueryNew,
				lindexFolder, newLindexFolder, minSupport, 0.99);
		writer.write(str);
		writer.flush();

		/*
		 * lindexFolder = baseNameNew + "GreedyDecompose_newDB/";
		 * newLindexFolder = baseNameNew + "GreedyDecomposeStream99/"; str =
		 * doUpdate(sType, gDBNew, topGCount, trainQueryNew, testQueryNew,
		 * lindexFolder, newLindexFolder, minSupport, 0.99); writer.write(str);
		 * writer.flush();
		 * 
		 * writer.write("StartUpdate of Stream Indexes for NewDB \n");
		 * newLindexFolder = baseNameNew + "stream2_Update/"; lindexFolder =
		 * baseNameNew + "stream2_newDB/";
		 * System.out.println("Start Updating Lindex with NewGDB: " +
		 * newLindexFolder); str = doUpdate(sType, gDBNew, topGCount,
		 * trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
		 * minSupport, 0); writer.write(str); writer.flush();
		 * 
		 * newLindexFolder = baseNameNew + "stream3_Update/"; lindexFolder =
		 * baseNameNew + "stream3_newDB/";
		 * System.out.println("Start Updating Lindex with NewGDB: " +
		 * newLindexFolder); str = doUpdate(sType, gDBNew, topGCount,
		 * trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
		 * minSupport, 1); writer.write(str); writer.flush();
		 * 
		 * newLindexFolder = baseNameNew + "stream99_Update/"; lindexFolder =
		 * baseNameNew + "stream99_newDB/";
		 * System.out.println("Start Updating Lindex with NewGDB: " +
		 * newLindexFolder); str = doUpdate(sType, gDBNew, topGCount,
		 * trainQueryNew, testQueryNew, lindexFolder, newLindexFolder,
		 * minSupport, 0.99); writer.write(str); writer.flush();
		 */
	}

	private static String doUpdate(StatusType sType, IGraphDatabase gDB,
			int topGCount, IGraphDatabase newQuery, IGraphDatabase testQuery,
			String lindexFolder, String lindexUpdateFolder, double minSupport,
			double lambda) throws IOException {
		System.out.println("Start Update Index: " + lindexUpdateFolder);
		SupSearch_LWFullBuilder lindexBuilder = new SupSearch_LWFullBuilder();
		SupSearch_Lindex lindex = lindexBuilder.loadLindex(gDB, lindexFolder,
				true, MyFactory.getDFSCoder());

		SupSearch_LindexUpdatableBuilder builder = new SupSearch_LindexUpdatableBuilder();
		SupSearch_LindexUpdatable lindexUpdatable = builder.buildIndexSup(
				lindex, gDB, newQuery, lindexFolder);

		// 1.2 Do index updating & save the index
		IndexUpdator updator = new IndexUpdator(lindexUpdatable, topGCount); // 100/100000
																				// =
																				// 0.01
		float[] beforeUpdateStat = updator.initializeUpdate(AppType.supSearch,
				sType, minSupport, lambda);
		float[] updateStat = updator.doUpdate();
		System.out.println(updator.getFeatureCount());
		builder.saveUpdatedIndex(lindexUpdatable, lindexUpdateFolder);
		// 1.3 After the index updating, do query processing test, query
		// processing with InMem Queries
		lindex = builder.loadIndexSup(gDB, lindexUpdateFolder,
				testQuery.getTotalNum());
		float[] runExpStat = Util.runIndex(lindex, testQuery);
		return Util.stateToString(Util.joinArray(beforeUpdateStat, updateStat,
				runExpStat));
	}

}
