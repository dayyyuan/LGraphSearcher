package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.IOException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.experiments.ExpGraphSearch;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;

public class IndexUpdateForHerusitics {
	public static void updateHerusitics(BufferedWriter writer,
			String indexBaseName, String queryName) throws IOException {
		GraphDatabase_InMem trainQuery = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(queryName + "TrainQuery",
						MyFactory.getDFSCoder()));
		GraphDatabase_InMem testQuery = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(queryName + "TestQuery",
						MyFactory.getDFSCoder()));
		GraphDatabase_InMem newGDB = new GraphDatabase_InMem(
				new GraphDatabase_OnDisk(indexBaseName + "DataAIDS",
						MyFactory.getSmilesParser()));

		double minSupport = 0.02;
		int topGCount = -1;
		StatusType sType = StatusType.decomposePreSelectAdv;

		System.out.println("stream2");
		writer.write("UpdateLindexDF with stream2 \n");
		String lindexFolder = indexBaseName + "LindexDF/";
		String lindexUpdateFolder = queryName + "LindexDFUpdate2/";
		String str = ExpGraphSearch.doUpdate(sType, newGDB, topGCount,
				trainQuery, testQuery, lindexFolder, lindexUpdateFolder,
				minSupport, 0);
		writer.write(str);
		writer.flush();

		System.out.println("stream3");
		writer.write("UpdateLindexDF with stream3 \n");
		lindexUpdateFolder = queryName + "LindexDFUpdate3/";
		str = ExpGraphSearch.doUpdate(sType, newGDB, topGCount, trainQuery,
				testQuery, lindexFolder, lindexUpdateFolder, minSupport, 1);
		writer.write(str);
		writer.flush();

		System.out.println("stream99");
		writer.write("UpdateLindexDF with stream99 \n");
		lindexUpdateFolder = queryName + "LindexDFUpdate99/";
		str = ExpGraphSearch.doUpdate(sType, newGDB, topGCount, trainQuery,
				testQuery, lindexFolder, lindexUpdateFolder, minSupport, 0.99);
		writer.write(str);
		writer.flush();

		System.out.println("stream2");
		writer.write("UpdateLindexTCFG with stream2 \n");
		lindexFolder = indexBaseName + "LindexTCFG/";
		lindexUpdateFolder = queryName + "LindexTCFGUpdate2/";
		str = ExpGraphSearch.doUpdate(sType, newGDB, topGCount, trainQuery,
				testQuery, lindexFolder, lindexUpdateFolder, minSupport, 0);
		writer.write(str);
		writer.flush();

		System.out.println("stream3");
		writer.write("UpdateLindexTCFG with stream3 \n");
		lindexUpdateFolder = queryName + "LindexTCFGUpdate3/";
		str = ExpGraphSearch.doUpdate(sType, newGDB, topGCount, trainQuery,
				testQuery, lindexFolder, lindexUpdateFolder, minSupport, 1);
		writer.write(str);
		writer.flush();

		System.out.println("stream99");
		writer.write("UpdateLindexTCFG with stream99 \n");
		lindexUpdateFolder = queryName + "LindexTCFGUpdate99/";
		str = ExpGraphSearch.doUpdate(sType, newGDB, topGCount, trainQuery,
				testQuery, lindexFolder, lindexUpdateFolder, minSupport, 0.99);
		writer.write(str);
		writer.flush();

	}
}
