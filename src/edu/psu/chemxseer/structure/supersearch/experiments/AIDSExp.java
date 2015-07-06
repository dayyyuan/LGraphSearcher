package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.LWFull.LWIndexExcExp;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The experiment for the AIDS Dataset
 * 
 * @author dayuyuan
 * 
 */
public class AIDSExp {
	// private static double minSupt = 0.05;

	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		boolean lucene_im_mem = false;
		// 1. Build the Index with varying "database size"
		// String dirName ="/data/home/duy113/SupSearchExp/AIDSNew/";
		String dirName = "/data/santa/SupSearchExp/AIDSNew/";
		// String dirName = "/Users/dayuyuan/Documents/workspace/Experiment/";
		String dbFileName = dirName + "DBFile";
		String trainQueryName = dirName + "TrainQuery";
		// String testQuery15 = dirName + "TestQuery15";
		String testQuery25 = dirName + "TestQuery25";
		// String testQuery35 = dirName + "TestQuery35";
		IGraphDatabase query = new GraphDatabase_OnDisk(testQuery25,
				MyFactory.getSmilesParser());
		double[] minSupts = new double[4];
		minSupts[0] = 0.05;
		minSupts[1] = 0.03;
		minSupts[2] = 0.02;
		minSupts[3] = 0.01;
		// int lwIndexCount[] = new int[1];
		// lwIndexCount[0] = 479;
		System.out
				.println("Build LW-Index Full with efficient feature mining:");
		for (int j = 1; j < 4; j++) {
			double minSupt = minSupts[j];
			for (int i = 4; i <= 10; i = i + 2) {
				if (j == 1 && i == 4)
					continue; // goes into i = 6 directly
				String baseName = dirName + "G_" + i + "MinSup_" + minSupt
						+ "/";
				IGraphDatabase trainingDB = new GraphDatabase_OnDisk(dbFileName
						+ i, MyFactory.getDFSCoder());
				IGraphDatabase trainQuery = new GraphDatabase_OnDisk(
						trainQueryName, MyFactory.getSmilesParser());
				System.out.println(baseName + "LWIndexFull");
				LWIndexExcExp.buildIndexWithNoFeatureMining(trainingDB,
						trainQuery, trainingDB, trainQuery.getParser(),
						baseName + "LWIndexExc/", baseName + "LWIndex/",
						minSupt);
				LWIndexExcExp.runIndex(trainingDB, trainQuery, baseName
						+ "LWIndexExc/", lucene_im_mem);
				LWIndexExcExp.runIndex(trainingDB, query, baseName
						+ "LWIndexExc/", lucene_im_mem);
				// if(i == 2){
				// System.out.println(baseName + "CIndexFlat");
				// CIndexExp.buildIndex(trainingDB, trainQuery, trainingDB,
				// baseName, minSupt, lwIndexCount[0]);
				// }
				// else{
				// String featureBaseName = dirName + "G_2" + "MinSup_" +
				// minSupt + "/";
				// System.out.println(baseName + "CIndexFlat with Features " +
				// featureBaseName);
				// CIndexExp.buildIndex(featureBaseName, trainingDB, baseName,
				// minSupt);
				// }
				System.gc();
			}
		}
		// System.out.println("Run Query Processing: ");
		// for(int j = 0; j< 4; j++){
		// double minSupt = minSupts[j];
		// for(int i = 2; i<=10; i = i+2){
		// String baseName = dirName + "G_" + i + "MinSup_" + minSupt + "/";
		// GraphDatabase trainingDB = new GraphDatabase_OnDisk(dbFileName + i,
		// MyFactory.getDFSCoder());
		// GraphDatabase trainQuery = new GraphDatabase_OnDisk(trainQueryName,
		// MyFactory.getSmilesParser());
		// if(j!=0 || i!=2){
		// System.out.println(baseName + "LWindex");
		// //LWIndexExp.buildIndex(trainingDB, trainQuery, trainingDB,
		// trainQuery.getParser(),baseName, minSupt, lwIndexCount);
		// //System.gc();
		// LWIndexExp.runIndex(trainingDB, trainQuery, baseName, lucene_im_mem);
		// LWIndexExp.runIndex(trainingDB, query, baseName, lucene_im_mem);
		// System.gc();
		// System.out.println(baseName + "PrefixIndex");
		// //PrefixIndexExp.buildIndex(trainingDB, trainingDB, baseName,
		// minSupt);
		// //System.gc();
		// PrefixIndexExp.runIndex(trainingDB, trainQuery, baseName,
		// lucene_im_mem);
		// PrefixIndexExp.runIndex(trainingDB, query, baseName, lucene_im_mem);
		// System.gc();
		// System.out.println(baseName + "PrefixIndexHi");
		// //PrefixIndexExp.buildHiIndex(trainingDB, trainingDB, 2, baseName,
		// minSupt);
		// //System.gc();
		// PrefixIndexExp.runHiIndex(trainingDB, trainQuery, baseName,
		// lucene_im_mem);
		// PrefixIndexExp.runHiIndex(trainingDB, query, baseName,
		// lucene_im_mem);
		// System.gc();
		// System.out.println(baseName + "GPTree");
		// //GPTreeExp.buildIndex(trainingDB, trainingDB, baseName, minSupt);
		// //System.gc();
		// GPTreeExp.runIndex(trainingDB, trainQuery, baseName, lucene_im_mem);
		// GPTreeExp.runIndex(trainingDB, query, baseName, lucene_im_mem);
		// System.gc();
		// System.out.println(baseName + "CIndexFlat");
		// //CIndexExp.buildIndex(trainingDB, trainQuery, trainingDB, baseName,
		// minSupt, lwIndexCount[0]);
		// //System.gc();
		// CIndexExp.runIndex(trainingDB, trainQuery, baseName, lucene_im_mem);
		// CIndexExp.runIndex(trainingDB, query, baseName, lucene_im_mem);
		// System.gc();
		// }
		// if(j==0&&i==2){
		// System.out.println(baseName + "CIndexTopDown: " + lwIndexCount[0]);
		// CIndexExp.buildIndexTopDown(trainingDB, trainQuery,
		// trainingDB,MyFactory.getUnCanDFS(), baseName, minSupt,
		// 2*trainQuery.getTotalNum()/lwIndexCount[0] ); // 8000 test queries
		// //System.gc();
		// }
		// System.out.println(baseName + "CIndexTopDown: " + lwIndexCount[0]);
		// CIndexExp.runIndexTopDown(trainingDB, trainQuery, baseName,
		// lucene_im_mem);
		// CIndexExp.runIndexTopDown(trainingDB, query, baseName,
		// lucene_im_mem);
		// System.gc();
		// }
		// }
		// AIDSLargeExp.main(args);
	}

	public static float[] runQueries(IGraphDatabase query, ISearcher searcher)
			throws IOException, ParseException {
		// Run the queries
		long[] TimeComponent = new long[4];
		float[] Number = new float[3];
		long[] TimeComponent1 = new long[4];
		int[] Number1 = new int[2];
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = Number[2] = 0;
		double memoryConsumption = 0;

		for (int i = 0; i < query.getTotalNum(); i++) {
			TimeComponent1[0] = TimeComponent1[1] = TimeComponent1[2] = TimeComponent1[3] = 0;
			Number1[0] = Number1[1];
			Graph g = query.findGraph(i);
			List<IGraphResult> answers = searcher.getAnswer(g, TimeComponent1,
					Number1);

			if (i == 0)
				memoryConsumption = MemoryConsumptionCal.usedMemoryinMB();
			else {
				double ratio = 1 / (double) (i + 1);
				memoryConsumption = memoryConsumption * ratio * i
						+ MemoryConsumptionCal.usedMemoryinMB() * ratio;
			}
			if (answers.size() == 0)
				continue;
			TimeComponent[0] += TimeComponent1[0];
			TimeComponent[1] += TimeComponent1[1];
			TimeComponent[2] += TimeComponent1[2];
			TimeComponent[3] += TimeComponent1[3];
			Number[0] += Number1[0];
			Number[1] += Number1[1];
			Number[2] += (float) (Number1[0]) / (float) (Number1[1]);
		}
		System.out.println("Query Processing Result: ");
		System.out.print(TimeComponent[0] + "\t" + TimeComponent[1] + "\t"
				+ TimeComponent[2] + "\t" + TimeComponent[3] + "\t");
		System.out.println(Number[0] + "\t" + Number[1] + "\t" + Number[2]
				+ "\t" + query.getTotalNum());
		System.out.println("Average Memory Consumption: "
				+ (long) memoryConsumption);
		float[] stat = new float[7];
		stat[0] = TimeComponent[0];
		stat[1] = TimeComponent[1];
		stat[2] = TimeComponent[2];
		stat[3] = TimeComponent[3];
		stat[4] = Number[0];
		stat[5] = Number[1];
		stat[6] = query.getTotalNum();
		return stat;
	}

	/**
	 * Run Queries, for all the queries with support > minNum, then assume that
	 * the query can be answered direclty This is specially used for the
	 * lindex(FGIndex)
	 * 
	 * @param query
	 * @param searcher
	 * @param minNum
	 * @throws IOException
	 * @throws ParseException
	 */
	public static float[] runQueries(IGraphDatabase query, ISearcher searcher,
			int minNum) throws IOException, ParseException {
		// Run the queries
		long[] TimeComponent = new long[4];
		float[] Number = new float[3];
		long[] TimeComponent1 = new long[4];
		int[] Number1 = new int[2];
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = Number[2] = 0;
		double memoryConsumption = 0;

		for (int i = 0; i < query.getTotalNum(); i++) {
			TimeComponent1[0] = TimeComponent1[1] = TimeComponent1[2] = TimeComponent1[3] = 0;
			Number1[0] = Number1[1];
			Graph g = query.findGraph(i);
			List<IGraphResult> answers = searcher.getAnswer(g, TimeComponent1,
					Number1);
			if (i == 0)
				memoryConsumption = MemoryConsumptionCal.usedMemoryinMB();
			else {
				double ratio = 1 / (double) (i + 1);
				memoryConsumption = memoryConsumption * ratio * i
						+ MemoryConsumptionCal.usedMemoryinMB() * ratio;
			}
			if (answers.size() == 0)
				continue;
			if (answers.size() > minNum) {
				TimeComponent1[3] = 0; // No Subgraph Isomorphism Test Assumed
				Number1[0] = 0;
			}
			TimeComponent[0] += TimeComponent1[0];
			TimeComponent[1] += TimeComponent1[1];
			TimeComponent[2] += TimeComponent1[2];
			TimeComponent[3] += TimeComponent1[3];
			Number[0] += Number1[0];
			Number[1] += Number1[1];
			Number[2] += (float) (Number1[0]) / (float) (Number1[1]);
		}
		System.out.println("Query Processing Result: ");
		System.out.print(TimeComponent[0] + "\t" + TimeComponent[1] + "\t"
				+ TimeComponent[2] + "\t" + TimeComponent[3] + "\t");
		System.out.println(Number[0] + "\t" + Number[1] + "\t" + Number[2]
				+ "\t" + query.getTotalNum());
		System.out.println("Average Memory Consumption: "
				+ (long) memoryConsumption);
		float[] stat = new float[7];
		stat[0] = TimeComponent[0];
		stat[1] = TimeComponent[1];
		stat[2] = TimeComponent[2];
		stat[3] = TimeComponent[3];
		stat[4] = Number[0];
		stat[5] = Number[1];
		stat[6] = query.getTotalNum();
		return stat;
	}
}
