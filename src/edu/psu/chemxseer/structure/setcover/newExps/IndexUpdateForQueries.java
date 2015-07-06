package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.update.IndexUpdator;
import edu.psu.chemxseer.structure.setcover.update.SubSearch_LindexSimpleUpdatable;
import edu.psu.chemxseer.structure.setcover.update.SubSearch_LindexSimpleUpdatableBuilder;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimple;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;

public class IndexUpdateForQueries {
	/**
	 * Assume: [1] Existence of the two folders: AIDS_10K_0.005Q and
	 * AIDS_10K_0.003Q [2] The build of the GreedyDecompose Index
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
			String baseName325 = baseName + "QueryChange325/";
			File temp = new File(baseName325);
			if (!temp.exists())
				temp.mkdirs();
			String baseName523 = baseName + "QueryChange523";
			temp = new File(baseName523);
			if (!temp.exists())
				temp.mkdirs();
			writer.write("UpdateIndex5 to Query 3\n");
			updateIndexForQueries(writer, baseName5, baseName3, baseName523);
			writer.write("UpdateIndex3 to Query 5\n");
			updateIndexForQueries(writer, baseName3, baseName5, baseName325);

			writer.flush();
			writer.close();
		}
	}

	public static void updateIndexForQueries(BufferedWriter writer,
			String oriIndexBaseName, String newQueryBaseName,
			String storedBaseName) throws IOException {
		GraphDatabase_InMem gDB = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(oriIndexBaseName + "DataAIDS",
						MyFactory.getSmilesParser()));
		GraphDatabase_InMem trainQuery = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(newQueryBaseName + "TrainQuery",
						MyFactory.getDFSCoder()));
		GraphDatabase_InMem testQuery = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(newQueryBaseName + "TestQuery",
						MyFactory.getDFSCoder()));
		double minSupport = 0.02;
		int topGCount = -1;
		// update 2
		String lindexFolder = oriIndexBaseName + "GreedyDecompose/";
		String lindexUpdateFolder = storedBaseName + "stream2/";
		String str = doUpdate(StatusType.decomposePreSelectAdv, gDB, topGCount,
				trainQuery, testQuery, lindexFolder, lindexUpdateFolder,
				minSupport, 0);
		writer.write(str);
		writer.flush();

		// update 3
		lindexFolder = oriIndexBaseName + "GreedyDecompose/";
		lindexUpdateFolder = storedBaseName + "stream3/";
		str = doUpdate(StatusType.decomposePreSelectAdv, gDB, topGCount,
				trainQuery, testQuery, lindexFolder, lindexUpdateFolder,
				minSupport, 1);
		writer.write(str);
		writer.flush();

		// update 99
		lindexFolder = oriIndexBaseName + "GreedyDecompose/";
		lindexUpdateFolder = storedBaseName + "stream99/";
		str = doUpdate(StatusType.decomposePreSelectAdv, gDB, topGCount,
				trainQuery, testQuery, lindexFolder, lindexUpdateFolder,
				minSupport, 0.99);
		writer.write(str);
		writer.flush();
	}

	public static String doUpdate(StatusType sType, IGraphDatabase gDB,
			int topGCount, IGraphDatabase newQuery, IGraphDatabase testQuery,
			String lindexFolder, String lindexUpdateFolder, double minSupport,
			double lambda) throws IOException {
		System.out.println("Start Update Index: " + lindexUpdateFolder);
		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple lindex = lindexBuilder.loadIndex(gDB,
				lindexFolder, MyFactory.getDFSCoder(), true);
		SubSearch_LindexSimpleUpdatableBuilder builder = new SubSearch_LindexSimpleUpdatableBuilder();
		SubSearch_LindexSimpleUpdatable lindexUpdatable = builder
				.buildIndexSub(lindex, gDB, newQuery, lindexFolder);
		// 1.2 Do index updating & save the index
		IndexUpdator updator = new IndexUpdator(lindexUpdatable, topGCount); // 100/100000
																				// =
																				// 0.01
		float[] beforeUpdateStat = updator.initializeUpdate(AppType.subSearch,
				sType, minSupport, lambda);
		float[] updateStat = updator.doUpdate();
		System.out.println(updator.getFeatureCount());
		builder.saveUpdatedIndex(lindexUpdatable, lindexUpdateFolder);
		// 1.3 After the index updating, do query processing test, query
		// processing with InMem Queries
		lindex = builder.loadIndexSub(gDB, lindexUpdateFolder);
		float[] runExpStat = Util.runIndex(lindex, testQuery);
		return Util.stateToString(Util.joinArray(beforeUpdateStat, updateStat,
				runExpStat));
	}
}
