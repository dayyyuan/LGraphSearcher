package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.experiments.ExpGraphSearch;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;

/**
 * Test the scalability of the algorithm studied
 * 
 * @author dayuyuan
 * 
 */
public class IndexLargeScale {
	public static void main(String[] args) throws IOException, ParseException {
		// String baseName = "/data/santa/VLDB13/LargeScale/subSearch/";
		String baseName = "/data/home/duy113/VLDBSetCover4/LargeScale/";
		// String baseName = "/Users/dayuyuan/Documents/Experiment/LargeScale/";
		if (args == null || args.length == 0)
			return;
		else {
			BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
			buildOriginalIndex(writer, baseName);
			writer.flush();
			writer.close();
		}
	}

	public static void buildOriginalIndex(BufferedWriter writer,
			String queryBaseName) throws IOException, ParseException {
		writer.write("Scalability Test \n");
		for (int i = 6; i >= 0; i--) {
			String baseName = queryBaseName + i + "/";
			GraphDatabase_InMem trainQuery = new GraphDatabase_InMem(
					new GraphDatabase_OnDisk(queryBaseName + "TrainQuery",
							MyFactory.getDFSCoder()));
			GraphDatabase_InMem testQuery = new GraphDatabase_InMem(
					new GraphDatabase_OnDisk(queryBaseName + "TestQuery",
							MyFactory.getDFSCoder()));
			GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseName
					+ "DataAIDS", MyFactory.getSmilesParser());

			// float[] beforeStatus = new float[]{(float)0, 0 ,
			// gDB.getTotalNum(), 0};
			// float[] updateStatus = new float[11];
			// float[] runIndexStatus = new float[0];
			writer.write("Build Indexes within BaseName: " + baseName + "\n");
			String lindexFolder = null;
			String lindexUpdateFolder = null;
			String str = null;

			// 1. Build GIndexDF 0.02
			/*
			 * System.out.println("Build Lindex with Minimum Support 0.02");
			 * beforeStatus[0] = (float) 0.02; ISearcher gIndex =
			 * IndexConstruction.buildGIndexDF(gDB, baseName, 0.02,
			 * updateStatus, "0.02"); runIndexStatus = Util.runIndex(gIndex,
			 * testQuery); str = Util.stateToString(Util.joinArray(beforeStatus,
			 * updateStatus, runIndexStatus)); writer.write(str);
			 * writer.flush();
			 * 
			 * //2. Build GIndexDF 0.01
			 * System.out.println("Build Lindex with Minimum Support 0.01");
			 * beforeStatus[0] = (float) 0.01; gIndex =
			 * IndexConstruction.buildGIndexDF(gDB, baseName, 0.01,
			 * updateStatus, "0.01"); runIndexStatus = Util.runIndex(gIndex,
			 * testQuery); str = Util.stateToString(Util.joinArray(beforeStatus,
			 * updateStatus, runIndexStatus)); writer.write(str);
			 * writer.flush();
			 */

			// 3. Update GIndexDF 0.02
			System.out.println("Update LindexDF0.02 with stream99 -1");
			lindexFolder = baseName + "LindexDF0.02/";
			lindexUpdateFolder = baseName + "LindexDF0.02Update99/";
			str = ExpGraphSearch.doUpdate(StatusType.decomposePreSelectAdv,
					gDB, gDB.getTotalNum(), trainQuery, testQuery,
					lindexFolder, lindexUpdateFolder, 0.02, 0.99);
			writer.write(str);
			writer.flush();

			if (i != 6) {
				// 4. Update GIndexDF 0.02
				System.out
						.println("Update LindexDF0.02 with stream99 top 10,000");
				lindexFolder = baseName + "LindexDF0.02/";
				lindexUpdateFolder = baseName + "LindexDF0.02Update99_2/";
				str = ExpGraphSearch.doUpdate(StatusType.decomposePreSelectAdv,
						gDB, 10000, trainQuery, testQuery, lindexFolder,
						lindexUpdateFolder, 0.02, 0.99);
				writer.write(str);
				writer.flush();
			}

			// 5. Build FGIndexTCFG
			/*
			 * System.out.println("Build & Run FGIndex 0.03"); beforeStatus[0] =
			 * (float) 0.03; ISearcher fgIndex =
			 * IndexConstruction.buildFGindex(gDB, baseName, 0.03, updateStatus,
			 * "0.03"); runIndexStatus = Util.runIndex(fgIndex, testQuery); str
			 * = Util.stateToString(Util.joinArray(beforeStatus, updateStatus,
			 * runIndexStatus)); writer.write(str); writer.flush();
			 * 
			 * System.out.println("Build & Run FGIndex 0.02"); beforeStatus[0] =
			 * (float) 0.02; fgIndex = IndexConstruction.buildFGindex(gDB,
			 * baseName, 0.02, updateStatus, "0.02"); runIndexStatus =
			 * Util.runIndex(fgIndex, testQuery); str =
			 * Util.stateToString(Util.joinArray(beforeStatus, updateStatus,
			 * runIndexStatus)); writer.write(str); writer.flush();
			 * 
			 * System.out.println("Build & Run FGIndex 0.01"); beforeStatus[0] =
			 * (float) 0.01; fgIndex = IndexConstruction.buildFGindex(gDB,
			 * baseName, 0.01, updateStatus, "0.01"); runIndexStatus =
			 * Util.runIndex(fgIndex, testQuery); str =
			 * Util.stateToString(Util.joinArray(beforeStatus, updateStatus,
			 * runIndexStatus)); writer.write(str); writer.flush();
			 */

			// 6. Update FGindexTCFG 0.03
			System.out.println("Update TCFG0.03 with stream99 -1");
			lindexFolder = baseName + "LindexTCFG0.03/";
			lindexUpdateFolder = baseName + "LindexTCFG0.03Update99/";
			str = ExpGraphSearch.doUpdate(StatusType.decomposePreSelectAdv,
					gDB, gDB.getTotalNum(), trainQuery, testQuery,
					lindexFolder, lindexUpdateFolder, 0.02, 0.99);
			writer.write(str);
			writer.flush();

			if (i != 6) {
				// 7. Update LindexTCFG3 0.03
				System.out.println("Update TCFG0.03 with stream99 top ");
				lindexFolder = baseName + "LindexTCFG0.03/";
				lindexUpdateFolder = baseName + "LindexTCFG0.03Update99_2/";
				str = ExpGraphSearch.doUpdate(StatusType.decomposePreSelectAdv,
						gDB, 10000, trainQuery, testQuery, lindexFolder,
						lindexUpdateFolder, 0.02, 0.99);
				writer.write(str);
				writer.flush();
			}
		}
		/*
		 * writer.write("Build Indexes Stream99 Large Scale Data Test \n");
		 * for(int i = 6; i >=0; i--){ int K = 2000; double minSupport = 0.02;
		 * String baseName = queryBaseName + i + "/"; GraphDatabase_InMem
		 * testQuery = new GraphDatabase_InMem( new
		 * GraphDatabase_OnDisk(queryBaseName + "TestQuery",
		 * MyFactory.getDFSCoder()) ); GraphDatabase_OnDisk gDB = new
		 * GraphDatabase_OnDisk(baseName + "DataAIDS",
		 * MyFactory.getSmilesParser());
		 * 
		 * String str = IndexConstruction.buildSwapIndex(baseName, gDB,
		 * testQuery, baseName+"stream99/", K, minSupport, 0.99);
		 * writer.write(str); writer.flush(); }
		 */
	}
}
