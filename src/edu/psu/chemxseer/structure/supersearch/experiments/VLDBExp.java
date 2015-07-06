package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.PreProcessTools2;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.supersearch.CIndex.CIndexExp;
import edu.psu.chemxseer.structure.supersearch.GPTree.GPTreeExp;
import edu.psu.chemxseer.structure.supersearch.LWFull.LWIndexExcExp;
import edu.psu.chemxseer.structure.supersearch.LWTree.LWIndexExp;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.PrefixIndexExp;

//The VLDB first submission experiment:
// Add the following two things:
// [1] First, the cold start experiment
// [2] Second, the synthetic data experiment: this does not yield prominsing results
public class VLDBExp {
	public static void main(String[] args) {
		try {
			// coldStartSmall();
			// coldStartBig();
			synthetic3();
			// synthetic();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void coldStartSmall() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		// Cold Start Small
		String smallGraphDir = "/data/santa/SupSearchExp/AIDSEDBT/";
		String dbFileName = smallGraphDir + "DBFile";

		String testQuery25 = smallGraphDir + "TestQuery25";
		String testQuery35 = smallGraphDir + "TestQuery35";
		IGraphDatabase query25 = new GraphDatabase_OnDisk(testQuery25,
				MyFactory.getSmilesParser());
		IGraphDatabase query35 = new GraphDatabase_OnDisk(testQuery35,
				MyFactory.getSmilesParser());

		int dataSetID = 4;
		String baseName = smallGraphDir + "G_" + dataSetID + "MinSup_0.01/";
		IGraphDatabase gDB = new GraphDatabase_OnDisk(dbFileName + 4,
				MyFactory.getDFSCoder());

		System.out.println("LWIndexInclusiveColdStart");
		LWIndexExp.buildIndex(gDB, gDB, gDB, gDB.getParser(),
				baseName + "Cold", 0.01, Integer.MAX_VALUE, 2);
		LWIndexExp.runIndex(gDB, query25, baseName + "Cold", true);
		LWIndexExp.runIndex(gDB, query35, baseName + "Cold", true);

		System.out.println(baseName + "LWIndexExclusiveColdStart");
		LWIndexExcExp.buildIndexWithNoFeatureMining(gDB, gDB, gDB,
				gDB.getParser(), baseName + "ColdLWIndexExc/", baseName
						+ "ColdLWIndex/", 0.01);
		LWIndexExcExp
				.runIndex(gDB, query25, baseName + "ColdLWIndexExc/", true);
		LWIndexExcExp
				.runIndex(gDB, query35, baseName + "ColdLWIndexExc/", true);
	}

	public static void coldStartBig() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		String home = "/data/santa/SupSearchExp/AIDSLargeEDBT/";
		IGraphDatabase gDB = new GraphDatabase_OnDisk(home + "DBFile_Raw",
				MyFactory.getSmilesParser());
		IGraphDatabase query = new GraphDatabase_OnDisk(home + "testQuery",
				MyFactory.getUnCanDFS());
		double minSupt = 0.03;
		String baseName = home + "MinSup_" + minSupt;

		System.out.println(baseName + "LWIndexInclusive");
		LWIndexExp.buildIndex(gDB, gDB, gDB, gDB.getParser(),
				baseName + "Cold", minSupt, Integer.MAX_VALUE, 2);
		System.gc();
		LWIndexExp.runIndex(gDB, query, baseName + "Cold", true);
		System.gc();

		System.out.println(baseName + "LWIndexExclusive");
		LWIndexExcExp.buildIndexWithNoFeatureMining(gDB, gDB, gDB,
				gDB.getParser(), baseName + "ColdLWIndexExc/", baseName
						+ "ColdLWIndex/", minSupt);
		System.gc();
		LWIndexExcExp.runIndex(gDB, query, baseName + "ColdLWIndexExc/", true);
		System.gc();
	}

	public static void synthetic() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		// String name ="/data/santa/SupSearchExp/GeneratedVLDB/";
		String name = "/data/home/duy113/SupSearchExp/GeneratedVLDB/";
		for (int i = 1; i <= 5; i++) {
			String dirName = name + i + "/";
			String rawQuerName = dirName + "rawQuery.data";
			String dbFileName = dirName + "DBFile";
			String trainQueryName = dirName + "TestQuery";
			String testQueryName = dirName + "TrainQuery";
			// 1.1 Convert the rawDB to the DB Format, Same Operation for
			// Queries
			/*
			 * if(i > 3){ PreProcessTools2.covertToSdtGraph(rawQuerName,
			 * rawQuerName+"temp", MyFactory.getUnCanDFS());
			 * RandomChoseDBGraph.randomlySplitDBGraph(new
			 * GraphDatabase_OnDisk(rawQuerName+"temp",
			 * MyFactory.getUnCanDFS()), 2000, trainQueryName, testQueryName); }
			 * double querySupport = 0.005; if(i == 4) querySupport = 0.006;
			 * else if(i == 5) querySupport = 0.007; PostingFeatures dbFeatures
			 * = FeatureProcessorFG.frequentSubgraphMining (rawQuerName+"temp",
			 * dbFileName+"raw", null, querySupport, 50,
			 * MyFactory.getUnCanDFS()); GraphDatabase gDB = new
			 * GraphDatabase_InMem(dbFeatures.getFeatures());
			 * RandomChoseDBGraph.randomlyChooseDBGraph(gDB, 10000, dbFileName);
			 * dbFeatures = null;
			 */
			IGraphDatabase gDB = null;
			if (i == 5) {
				NoPostingFeatures dbFeatures = new NoPostingFeatures(
						dbFileName + "raw",
						MyFactory
								.getFeatureFactory(FeatureFactoryType.OneFeature));
				gDB = new GraphDatabase_InMem(dbFeatures);
				RandomChoseDBGraph
						.randomlyChooseDBGraph(gDB, 40000, dbFileName);
			}
			gDB = new GraphDatabase_OnDisk(dbFileName, MyFactory.getDFSCoder());

			System.gc();
			double[] minSuppots = new double[] { 0.03, 0.01, 0.008, 0.006 }; // minimum
																				// support
			for (double minSupt : minSuppots) {
				String baseName = dirName + "MinSup_" + minSupt + "/";
				IGraphDatabase trainQuery = new GraphDatabase_OnDisk(
						trainQueryName, MyFactory.getUnCanDFS());
				IGraphDatabase testQuery = new GraphDatabase_OnDisk(
						testQueryName, MyFactory.getUnCanDFS());

				System.out.println(baseName + "LWIndexinclusive");
				LWIndexExp.buildIndex(gDB, trainQuery, gDB,
						trainQuery.getParser(), baseName, minSupt,
						Integer.MAX_VALUE, 2);
				LWIndexExp.runIndex(gDB, testQuery, baseName, true);

				System.out.println(baseName + "LWIndexExclusive");
				LWIndexExcExp.buildIndexWithNoFeatureMining(gDB, trainQuery,
						gDB, trainQuery.getParser(), baseName + "LWIndexExc/",
						baseName + "LWIndex/", minSupt);
				LWIndexExcExp.runIndex(gDB, testQuery,
						baseName + "LWIndexExc/", true);

				System.out.println(baseName + "PrefixIndexHi");
				PrefixIndexExp.buildHiIndex(gDB, gDB, 2, baseName, minSupt);
				System.gc();
				PrefixIndexExp.runHiIndex(gDB, testQuery, baseName, true);

				System.out.println(baseName + "GPTree");
				GPTreeExp.buildIndex(gDB, gDB, baseName, minSupt);
				System.gc();
				GPTreeExp.runIndex(gDB, testQuery, baseName, true);

				System.out.println(baseName + "CIndexTopDown: ");
				CIndexExp.buildIndexTopDown(gDB, trainQuery, gDB,
						MyFactory.getUnCanDFS(), baseName, minSupt, 100);
				CIndexExp.runIndexTopDown(gDB, testQuery,
						MyFactory.getUnCanDFS(), baseName, true);
			}

			/*
			 * System.out.println(baseName + "CIndexBottomUp"); int lwIndexCount
			 * = CIndexExp.buildIndexBottomUp(gDB, trainQuery,
			 * MyFactory.getUnCanDFS(), gDB, baseName, minSupt,
			 * Integer.MAX_VALUE, 100); System.gc();
			 * CIndexExp.runIndexBottomUp(gDB,
			 * testQuery,MyFactory.getUnCanDFS(), baseName, true);
			 * 
			 * System.out.println(baseName + "LWIndexExclusiveK");
			 * LWIndexExcExp.buildIndex2(gDB, trainQuery, gDB,
			 * trainQuery.getParser(), baseName, minSupt, lwIndexCount, 2);
			 * System.gc(); LWIndexExcExp.runIndex2(gDB, testQuery, baseName,
			 * true);
			 */
		}

	}

	public static void synthetic2() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		String name = "/data/home/duy113/SupSearchExp/GeneratedVLDB2/";
		for (int i = 1; i <= 5; i++) {
			String dirName = name + i + "/";
			String rawQuerName = dirName + "rawQuery.data";
			String rawDBName = dirName + "rawDB.data";
			String dbFileName = dirName + "DBFile";
			String trainQueryName = dirName + "TrainQuery";
			String testQueryName = dirName + "TestQuery";
			// 1.1 Convert the rawDB to the DB Format, Same Operation for
			// Queries
			PreProcessTools2.covertToSdtGraph(rawQuerName,
					rawQuerName + "temp", MyFactory.getUnCanDFS());
			PreProcessTools2.covertToSdtGraph(rawDBName, dbFileName,
					MyFactory.getUnCanDFS());
			System.gc();
			RandomChoseDBGraph.randomlySplitDBGraph(new GraphDatabase_OnDisk(
					rawQuerName + "temp", MyFactory.getUnCanDFS()), 8000,
					trainQueryName, testQueryName);
			double minSupt = 0.01; // minimum support
			String baseName = dirName + "MinSup_" + minSupt + "/";
			IGraphDatabase trainQuery = new GraphDatabase_OnDisk(
					trainQueryName, MyFactory.getUnCanDFS());
			IGraphDatabase testQuery = new GraphDatabase_OnDisk(testQueryName,
					MyFactory.getUnCanDFS());
			IGraphDatabase gDB = new GraphDatabase_OnDisk(dbFileName,
					MyFactory.getDFSCoder());
		}
	}

	public static void synthetic3() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		String name = "/data/home/duy113/SupSearchExp/GeneratedVLDB3/";
		for (int i = 1; i <= 5; i++) {
			String dirName = name + i + "/";
			String rawQuerName = dirName + "rawQuery.data";
			// String rawDBName = dirName + "rawDB.data";
			String dbFileName = dirName + "DBFile";
			String trainQueryName = dirName + "TrainQuery";
			String testQueryName = dirName + "TestQuery";
			double querySupport = 0.01;
			// 1.1 Convert the rawDB to the DB Format, Same Operation for
			// Queries
			PreProcessTools2.covertToSdtGraph(rawQuerName,
					rawQuerName + "temp", MyFactory.getUnCanDFS());
			PostingFeatures dbFeatures = FeatureProcessorFG
					.frequentSubgraphMining(rawQuerName + "temp", dbFileName
							+ "raw", null, querySupport, 50,
							MyFactory.getUnCanDFS());
			IGraphDatabase gDB = new GraphDatabase_InMem(
					dbFeatures.getFeatures());
			RandomChoseDBGraph.randomlyChooseDBGraph(gDB, 40000, dbFileName);
			dbFeatures = null;

			System.gc();
			RandomChoseDBGraph.randomlySplitDBGraph(new GraphDatabase_OnDisk(
					rawQuerName + "temp", MyFactory.getUnCanDFS()), 8000,
					trainQueryName, testQueryName);
			double minSupt = 0.01; // minimum support
			String baseName = dirName + "MinSup_" + minSupt + "/";
			IGraphDatabase trainQuery = new GraphDatabase_OnDisk(
					trainQueryName, MyFactory.getUnCanDFS());
			IGraphDatabase testQuery = new GraphDatabase_OnDisk(testQueryName,
					MyFactory.getUnCanDFS());
			gDB = new GraphDatabase_OnDisk(dbFileName, MyFactory.getDFSCoder());

			System.out.println(baseName + "LWIndexinclusive");
			LWIndexExp.buildIndex(gDB, trainQuery, gDB, trainQuery.getParser(),
					baseName, minSupt, Integer.MAX_VALUE, 2);
			LWIndexExp.runIndex(gDB, testQuery, baseName, true);

			System.out.println(baseName + "LWIndexExclusive");
			LWIndexExcExp.buildIndexWithNoFeatureMining(gDB, trainQuery, gDB,
					trainQuery.getParser(), baseName + "LWIndexExc/", baseName
							+ "LWIndex/", minSupt);
			LWIndexExcExp.runIndex(gDB, testQuery, baseName + "LWIndexExc",
					true);

			System.out.println(baseName + "PrefixIndexHi");
			PrefixIndexExp.buildHiIndex(gDB, gDB, 2, baseName, minSupt);
			System.gc();
			PrefixIndexExp.runHiIndex(gDB, testQuery, baseName, true);

			System.out.println(baseName + "GPTree");
			GPTreeExp.buildIndex(gDB, gDB, baseName, minSupt);
			System.gc();
			GPTreeExp.runIndex(gDB, testQuery, baseName, true);

			System.out.println(baseName + "CIndexTopDown: ");
			CIndexExp.buildIndexTopDown(gDB, trainQuery, gDB,
					MyFactory.getUnCanDFS(), baseName, minSupt, 100);
			CIndexExp.runIndexTopDown(gDB, testQuery, MyFactory.getUnCanDFS(),
					baseName, true);

			/*
			 * System.out.println(baseName + "CIndexBottomUp"); int lwIndexCount
			 * = CIndexExp.buildIndexBottomUp(gDB, trainQuery,
			 * MyFactory.getUnCanDFS(), gDB, baseName, minSupt,
			 * Integer.MAX_VALUE, 100); System.gc();
			 * CIndexExp.runIndexBottomUp(gDB,
			 * testQuery,MyFactory.getUnCanDFS(), baseName, true);
			 * 
			 * System.out.println(baseName + "LWIndexExclusiveK");
			 * LWIndexExcExp.buildIndex2(gDB, trainQuery, gDB,
			 * trainQuery.getParser(), baseName, minSupt, lwIndexCount, 2);
			 * System.gc(); LWIndexExcExp.runIndex2(gDB, testQuery, baseName,
			 * true);
			 */
		}
	}
}
