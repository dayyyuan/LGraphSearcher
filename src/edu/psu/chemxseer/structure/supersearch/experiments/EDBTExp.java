package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.supersearch.CIndex.CIndexExp;
import edu.psu.chemxseer.structure.supersearch.LWFull.LWIndexExcExp;
import edu.psu.chemxseer.structure.supersearch.LWTree.LWIndexExp;

/**
 * The experiment designed for the EDBT submission
 * 
 * @author dayuyuan
 * 
 */
public class EDBTExp {
	public static void main(String[] args) {
		try {
			// smallGraphs();
			largeGraphs();
			// largeScale();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static void largeScale() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		String oriDir = "/data/santa/SupSearchExp/AIDSNew/";
		String smallGraphDir = "/data/santa/SupSearchExp/AIDSEDBT/";
		String trainQueryName = smallGraphDir + "TrainQuery";

		String testQuery25 = smallGraphDir + "TestQuery25";
		String testQuery35 = smallGraphDir + "TestQuery35";
		IGraphDatabase query25 = new GraphDatabase_OnDisk(testQuery25,
				MyFactory.getSmilesParser());
		IGraphDatabase query35 = new GraphDatabase_OnDisk(testQuery35,
				MyFactory.getSmilesParser());

		double minSup = 0.01;

		for (int j = 2; j < 11; j = j + 2) {
			if (j == 4)
				continue;
			int dataSetID = j;

			String baseName = smallGraphDir + "G_" + dataSetID + "MinSup_0.01"
					+ "/";
			String dbFileName = smallGraphDir + "DBFile" + j;
			IGraphDatabase trainingDB = new GraphDatabase_OnDisk(dbFileName,
					MyFactory.getDFSCoder());
			IGraphDatabase trainQuery = new GraphDatabase_OnDisk(
					trainQueryName, MyFactory.getSmilesParser());

			String oribaseName = oriDir + "G_" + dataSetID + "MinSup_0.01"
					+ "/LWIndex/";
			String fName = oribaseName + "feature";
			String[] pNames = new String[] { oribaseName + "posting0",
					oribaseName + "posting1", oribaseName + "posting2",
					oribaseName + "posting3" };
			int[] classCount = new int[] { trainingDB.getTotalNum(),
					trainQuery.getTotalNum() };

			NoPostingFeatures<OneFeatureMultiClass> features = new NoPostingFeatures<OneFeatureMultiClass>(
					fName,
					MyFactory
							.getFeatureFactory(FeatureFactoryType.MultiFeature));
			PostingFeaturesMultiClass frequentSubgraphs = new PostingFeaturesMultiClass(
					pNames, features, classCount);

			System.out.println(baseName + "LWIndexInclusive");
			LWIndexExp.buildIndex(trainingDB, trainQuery, trainingDB,
					trainQuery.getParser(), baseName, minSup,
					Integer.MAX_VALUE, 2, frequentSubgraphs);

			System.out.println(baseName + "LWIndexExclusive");

			LWIndexExcExp.buildIndexWithNoFeatureMining(trainingDB, trainQuery,
					trainingDB, trainQuery.getParser(), baseName
							+ "LWIndexExc/", baseName + "LWIndex/", minSup);
			LWIndexExcExp.runIndex(trainingDB, query25, baseName
					+ "LWIndexExc/", true);
			LWIndexExcExp.runIndex(trainingDB, query25, baseName
					+ "LWIndexExc/", true);
			LWIndexExcExp.runIndex(trainingDB, query35, baseName
					+ "LWIndexExc/", true);
			LWIndexExcExp.runIndex(trainingDB, query35, baseName
					+ "LWIndexExc/", true);
		}

	}

	public static void largeGraphs() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		String home = "/data/santa/SupSearchExp/AIDSLargeEDBT/";
		IGraphDatabase trainingDB = new GraphDatabase_OnDisk(home
				+ "DBFile_Raw", MyFactory.getSmilesParser());
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(home
				+ "trainQuery", MyFactory.getUnCanDFS());
		IGraphDatabase query = new GraphDatabase_OnDisk(home + "testQuery",
				MyFactory.getUnCanDFS()); // i > =6

		double[] minSupts = new double[] { 0.05, 0.03, 0.02, 0.01, 0.008, 0.006 };

		for (int i = 0; i < minSupts.length; i++) {
			double minSupt = minSupts[i];
			String baseName = home + "MinSup_" + minSupt;

			System.out.println(baseName + "CIndexTopDown: ");
			CIndexExp.buildIndexTopDown(trainingDB, trainQuery, trainingDB,
					MyFactory.getUnCanDFS(), baseName, minSupt, 100);
			CIndexExp.runIndexTopDown(trainingDB, query,
					MyFactory.getUnCanDFS(), baseName, true);
			CIndexExp.runIndexTopDown(trainingDB, query,
					MyFactory.getUnCanDFS(), baseName, true);
			System.gc();

			/*
			 * System.out.println(baseName + "LWIndexInclusive");
			 * LWIndexExp.buildIndex(trainingDB, trainQuery, trainingDB,
			 * trainQuery.getParser(), baseName, minSupt, Integer.MAX_VALUE,
			 * 10); System.gc(); LWIndexExp.runIndex(trainingDB, query,
			 * baseName, true); LWIndexExp.runIndex(trainingDB, query, baseName,
			 * true); System.gc();
			 * 
			 * System.out.println(baseName + "LWIndexExclusive");
			 * LWIndexExcExp.buildIndex(trainingDB, trainQuery, trainingDB,
			 * trainQuery.getParser(), baseName, minSupt); System.gc();
			 * LWIndexExcExp.runIndex(trainingDB, query, baseName, true);
			 * LWIndexExcExp.runIndex(trainingDB, query, baseName, true);
			 * System.gc();
			 */

			/*
			 * if(i == 0 || i ==2){ System.out.println(baseName +
			 * "PrefixIndexHi"); PrefixIndexExp.buildHiIndex(trainingDB,
			 * trainingDB, 2, baseName, minSupt); System.gc();
			 * PrefixIndexExp.runHiIndex(trainingDB, query, baseName, true);
			 * PrefixIndexExp.runHiIndex(trainingDB, query, baseName, true);
			 * 
			 * System.out.println(baseName + "GPTree");
			 * GPTreeExp.buildIndex(trainingDB, trainingDB, baseName, minSupt);
			 * System.gc(); GPTreeExp.runIndex(trainingDB, query, baseName,
			 * true); GPTreeExp.runIndex(trainingDB, query, baseName, true);
			 * System.gc();
			 * 
			 * System.out.println(baseName + "cIndexBU");
			 * CIndexExp.buildIndexBottomUp(trainingDB, trainQuery,
			 * MyFactory.getUnCanDFS(), trainingDB, baseName, minSupt,
			 * Integer.MAX_VALUE, 100); System.gc();
			 * CIndexExp.runIndexBottomUp(trainingDB, query, baseName, true);
			 * CIndexExp.runIndexBottomUp(trainingDB, query, baseName, true); }
			 */
		}
	}

	public static void smallGraphs() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		String smallGraphDir = "/data/santa/SupSearchExp/AIDSEDBT/";
		String dbFileName = smallGraphDir + "DBFile";
		String trainQueryName = smallGraphDir + "TrainQuery";

		String testQuery25 = smallGraphDir + "TestQuery25";
		String testQuery35 = smallGraphDir + "TestQuery35";
		IGraphDatabase query25 = new GraphDatabase_OnDisk(testQuery25,
				MyFactory.getSmilesParser());
		IGraphDatabase query35 = new GraphDatabase_OnDisk(testQuery35,
				MyFactory.getSmilesParser());

		double[] minSupts = new double[] { 0.05, 0.03, 0.02, 0.01, 0.008, 0.006 };
		int lwIndexCount[] = new int[] { -1, 184, -1, 264, 280, 283 };

		for (int j = 0; j < 6; j++) {
			double minSupt = minSupts[j]; // minimum support
			int dataSetID = 4;
			String baseName = smallGraphDir + "G_" + dataSetID + "MinSup_"
					+ minSupt + "/";
			IGraphDatabase trainingDB = new GraphDatabase_OnDisk(
					dbFileName + 4, MyFactory.getDFSCoder());
			IGraphDatabase trainQuery = new GraphDatabase_OnDisk(
					trainQueryName, MyFactory.getSmilesParser());

			// if(j == 0 || j == 2){
			// System.out.println(baseName + "LWIndexInclusive");
			// LWIndexExp.buildIndex(trainingDB, trainQuery, trainingDB,
			// trainQuery.getParser(), baseName, minSupt, Integer.MAX_VALUE,2);
			// System.gc();
			// LWIndexExp.runIndex(trainingDB, query25, baseName, true);
			// LWIndexExp.runIndex(trainingDB, query25, baseName, true);
			// System.gc();
			// }
			//
			// if(j == 0 || j ==2){
			// System.out.println(baseName + "LWIndexExclusive");
			// LWIndexExcExp.buildIndex(trainingDB, trainQuery, trainingDB,
			// trainQuery.getParser(), baseName, minSupt);
			// LWIndexExcExp.runIndex(trainingDB, query25, baseName, true);
			// LWIndexExcExp.runIndex(trainingDB, query25, baseName, true);
			// LWIndexExcExp.runIndex(trainingDB, query35, baseName, true);
			// LWIndexExcExp.runIndex(trainingDB, query35, baseName, true);
			// }
			//
			// if(j == 0 || j==2 ||j == 4 ||j==5){
			// System.out.println(baseName + "PrefixIndexHi");
			// if( j ==0 || j==2){
			// PrefixIndexExp.buildHiIndex(trainingDB, trainingDB, 2, baseName,
			// minSupt);
			// System.gc();
			// PrefixIndexExp.runHiIndex(trainingDB, query25, baseName, true);
			// PrefixIndexExp.runHiIndex(trainingDB, query25, baseName, true);
			// }
			// PrefixIndexExp.runHiIndex(trainingDB, query35, baseName, true);
			// PrefixIndexExp.runHiIndex(trainingDB, query35, baseName, true);
			// }
			//
			// if(j == 0 || j ==2 ||j ==4||j==5){
			// System.out.println(baseName + "GPTree");
			// if(j == 0 || j ==2) {
			// GPTreeExp.buildIndex(trainingDB, trainingDB, baseName, minSupt);
			// System.gc();
			// GPTreeExp.runIndex(trainingDB, query25, baseName, true);
			// GPTreeExp.runIndex(trainingDB, query25, baseName, true);
			// }
			// GPTreeExp.runIndex(trainingDB, query35, baseName, true);
			// GPTreeExp.runIndex(trainingDB, query35, baseName, true);
			// System.gc();
			// }

			System.out.println(baseName + "CIndexTopDown: ");
			CIndexExp.buildIndexTopDown(trainingDB, trainQuery, trainingDB,
					MyFactory.getUnCanDFS(), baseName, minSupt, 100);
			CIndexExp.runIndexTopDown(trainingDB, query25,
					MyFactory.getUnCanDFS(), baseName, true);
			CIndexExp.runIndexTopDown(trainingDB, query25,
					MyFactory.getUnCanDFS(), baseName, true);
			CIndexExp.runIndexTopDown(trainingDB, query35,
					MyFactory.getUnCanDFS(), baseName, true);
			CIndexExp.runIndexTopDown(trainingDB, query35,
					MyFactory.getUnCanDFS(), baseName, true);
			System.gc();

			// if(j == 0 || j ==2 || j==4||j==5){
			// System.out.println(baseName + "CIndexBottomUp");
			// if(j == 0 || j ==2){
			// lwIndexCount[j] = CIndexExp.buildIndexBottomUp(trainingDB,
			// trainQuery, MyFactory.getUnCanDFS(),
			// trainingDB, baseName, minSupt, Integer.MAX_VALUE, 100);
			// System.gc();
			// CIndexExp.runIndexBottomUp(trainingDB, query25, baseName, true);
			// CIndexExp.runIndexBottomUp(trainingDB, query25, baseName, true);
			//
			// }
			// CIndexExp.runIndexBottomUp(trainingDB, query35, baseName, true);
			// CIndexExp.runIndexBottomUp(trainingDB, query35, baseName, true);
			// System.gc();
			// }
			//
			// if(j == 0 || j==2||j==4||j==5){
			// System.out.println(baseName + "LWIndexExclusiveK");
			// LWIndexExcExp.buildIndex2(trainingDB, trainQuery, trainingDB,
			// trainQuery.getParser(), baseName, minSupt, lwIndexCount[j], 2);
			// System.gc();
			// LWIndexExcExp.runIndex2(trainingDB, query25, baseName, true);
			// LWIndexExcExp.runIndex2(trainingDB, query25, baseName, true);
			// LWIndexExcExp.runIndex2(trainingDB, query35, baseName, true);
			// LWIndexExcExp.runIndex2(trainingDB, query35, baseName, true);
			// System.gc();
			// }
			//
		}
	}
}
