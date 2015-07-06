package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.supersearch.CIndex.CIndexExp;
import edu.psu.chemxseer.structure.supersearch.GPTree.GPTreeExp;
import edu.psu.chemxseer.structure.supersearch.LWTree.LWIndexExp;
import edu.psu.chemxseer.structure.supersearch.LWTree.SupSearch_LWIndex;
import edu.psu.chemxseer.structure.supersearch.LWTree.SupSearch_LWIndexBuilder;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.PrefixIndexExp;

public class AIDSPatch {
	/**
	 * In the current version, the cIndexTopDown lucene index is not complete.
	 * Therefore, we need to re-build and re-run CIndexTopDown Experiments
	 * 
	 * @throws IOException
	 */
	public void reBuildCIndexTopDown() {
		String dirName = "/data/home/duy113/SupSearchExp/AIDSNew/";
		String dbFileName = dirName + "DBFile";
		String testQuery25 = dirName + "TestQuery25";
		String trainQueryName = dirName + "TrainQuery";
		IGraphDatabase query = new GraphDatabase_OnDisk(testQuery25,
				MyFactory.getSmilesParser());
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(trainQueryName,
				MyFactory.getSmilesParser());
		double[] minSupts = new double[4];
		minSupts[0] = 0.05;
		minSupts[1] = 0.03;
		minSupts[2] = 0.02;
		minSupts[3] = 0.01;
		System.out.println("ReBuildIndex");
		for (int j = 0; j < 4; j++) {
			double minSupt = minSupts[j];
			for (int i = 2; i <= 10; i = i + 2) {
				String baseName = dirName + "G_" + i + "MinSup_" + minSupt
						+ "/";
				IGraphDatabase realDB = new GraphDatabase_OnDisk(
						dbFileName + i, MyFactory.getDFSCoder());
				try {
					System.out.println(baseName);
					// CIndexExp.buildIndexTopDownLuceneOnly(realDB, baseName,
					// minSupt);
					SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
					SupSearch_LWIndex temp = builder.loadIndex(realDB, baseName
							+ "LWIndex/", false);
					CIndexExp.buildIndexTopDown(
							realDB,
							trainQuery,
							realDB,
							MyFactory.getUnCanDFS(),
							baseName,
							minSupt,
							2 * trainQuery.getTotalNum()
									/ temp.getFeatureCount());
				} catch (IOException e) {
					e.printStackTrace();
				}
				CIndexExp.runIndexTopDown(realDB, query,
						MyFactory.getSmilesParser(), baseName, false);
			}
		}
	}

	/**
	 * Current Memory Measurement is not precise. The following function measure
	 * the static memory consumption of each index
	 * 
	 * @throws IOException
	 */
	public void memoryMesurement() {
		String dirName = "/data/home/duy113/SupSearchExp/AIDSNew/";
		String dbFileName = dirName + "DBFile";
		double[] minSupts = new double[4];
		minSupts[0] = 0.05;
		minSupts[1] = 0.03;
		minSupts[2] = 0.02;
		minSupts[3] = 0.01;

		for (int j = 0; j < 4; j++) {
			double minSupt = minSupts[j];
			for (int i = 2; i <= 10; i = i + 2) {

				String baseName = dirName + "G_" + i + "MinSup_" + minSupt
						+ "/";
				System.out.println(baseName);
				IGraphDatabase realDB = new GraphDatabase_OnDisk(
						dbFileName + i, MyFactory.getDFSCoder());
				try {
					System.out.println("LWIndex");
					LWIndexExp.memoryConsumption(realDB, baseName, false);
					System.out.println("PrefixHi");
					PrefixIndexExp.memoryConsumptionHi(realDB, baseName, false);
					System.out.println("GPTree");
					GPTreeExp.memoryConsumption(realDB, baseName, false);
					System.out.println("CIndexFlat");
					CIndexExp.memoryConsumptionFlat(realDB,
							MyFactory.getSmilesParser(), baseName, false);
					System.out.println("CIndexTopDown");
					CIndexExp.memoryConsumptionTopDown(realDB,
							MyFactory.getSmilesParser(), baseName + "2", false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * For large experiments only: Due to the fact that LW-index takes longer
	 * time to be mined on large queries. We use the Database distribution as
	 * the surrogate to test the performance of LW-index
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 */
	public void buildLWIndexFake() {
		String home = "/data/home/duy113/SupSearchExp/AIDSLargeNew/";
		IGraphDatabase trainingDB = new GraphDatabase_OnDisk(home
				+ "DBFile_Raw", MyFactory.getSmilesParser());
		IGraphDatabase query = new GraphDatabase_OnDisk(home + "testQuery",
				MyFactory.getUnCanDFS()); // i > =6
		boolean lucene_im_mem = false;
		double[] minSupts = new double[4];
		minSupts[0] = 0.05;
		minSupts[1] = 0.03;
		minSupts[2] = 0.02;
		minSupts[3] = 0.01;
		int lwIndexCount[] = new int[1];
		lwIndexCount[0] = 590;

		for (int i = 0; i < minSupts.length; i++) {
			double minSupt = minSupts[i];
			String baseName = home + "MinSup_Fake_" + minSupt;
			try {
				LWIndexExp.buildIndex(trainingDB, trainingDB, trainingDB,
						MyFactory.getUnCanDFS(), baseName, minSupt,
						Integer.MAX_VALUE, 2);
				LWIndexExp.runIndex(trainingDB, query, baseName, lucene_im_mem);
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
	}

	/*
	 * public static void main(String[] args){ AIDSPatch exp = new AIDSPatch();
	 * exp.buildLWIndexFake(); // build the Fake LW-Index for Large Experiments
	 * exp.reBuildCIndexTopDown(); //rebuild the CIndexTopDown and Run //measure
	 * the memory consumption exp.memoryMesurement(); }
	 */
}
