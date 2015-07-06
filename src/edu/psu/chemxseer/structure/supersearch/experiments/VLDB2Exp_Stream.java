package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.supersearch.LWFull.LWIndexExcExp;

/**
 * Additional experiment for the VLDB Revision This experiment compares the
 * greedy algorithm & the streamining algorithm on (1) Running Time (2) Memory
 * Consumption (3)
 * 
 * @author dayuyuan
 * 
 */
public class VLDB2Exp_Stream {

	public static void main(String[] args) {
		try {
			streamMining();
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

	public static void streamMining() throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		String bigGraphDir = "/data/santa/SupSearchExp/AIDSLargeEDBT/";
		IGraphDatabase gDB = new GraphDatabase_OnDisk(bigGraphDir
				+ "DBFile_Raw", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(bigGraphDir
				+ "testQuery", MyFactory.getUnCanDFS());
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(bigGraphDir
				+ "trainQuery", MyFactory.getUnCanDFS());

		int dataSetID = 4;
		double[] minSupports = new double[] { 0.05, 0.03, 0.02, 0.01, 0.008,
				0.006 };

		for (double minSupport : minSupports) {
			String baseName = bigGraphDir + "G_" + dataSetID + "MinSup_"
					+ minSupport + "/";
			int[] selectFeatureCount = new int[] { Integer.MAX_VALUE };

			// The Greedy Algorithm
			System.out.println(baseName + "LWExclusive/");
			LWIndexExcExp.buildIndexWithFeatureMining(gDB, trainQuery, gDB,
					MyFactory.getUnCanDFS(), baseName + "LWIndexExc/",
					minSupport, 0, selectFeatureCount);
			LWIndexExcExp.runIndex(gDB, testQuery, baseName + "LWIndexExc/",
					true);

			// The Streaming Algorithm: Implementation One
			System.out.println(baseName + "LWExclusiveStream1/");
			LWIndexExcExp.buildIndexWithFeatureMining(gDB, trainQuery, gDB,
					MyFactory.getUnCanDFS(), baseName + "LWIndexExcStream1/",
					minSupport, 1, selectFeatureCount);
			LWIndexExcExp.runIndex(gDB, testQuery, baseName
					+ "LWIndexExcStream1/", true);

			// The Streaming Algorithm: IMplementation Two
			System.out.println(baseName + "LWExclusiveStream2/");
			LWIndexExcExp.buildIndexWithFeatureMining(gDB, trainQuery, gDB,
					MyFactory.getUnCanDFS(), baseName + "LWIndexExcStream2/",
					minSupport, 2, selectFeatureCount);
			LWIndexExcExp.runIndex(gDB, testQuery, baseName
					+ "LWIndexExcStream2/", true);
		}
	}
}
