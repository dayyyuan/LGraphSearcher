package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.math.MathException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.query.InFrequentQueryGenerater2;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * Compare the performance of the indexes constructed by indexing different
 * number of selected features
 * 
 * @author dayuyuan
 * 
 */
public class IndexWithDifFeatureCount {

	public static void main(String[] args) throws IOException {
		if (args == null || args.length == 0)
			return;
		else {
			BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
			String baseName = "/data/home/duy113/VLDBSetCover4/DifK/";
			prePrepareData(baseName);
			writer.flush();
			writer.close();
		}
	}

	public static void prePrepareData(String baseName) {
		String rawQueryFile = "/data/home/duy113/VLDB13/AIDS/subSearch/Query_Together_raw";
		NoPostingFeatures<IOneFeature> rawQuery = new NoPostingFeatures<IOneFeature>(
				rawQueryFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseName
				+ "DataAIDS", MyFactory.getSmilesParser());

		InFrequentQueryGenerater2 queryGen = new InFrequentQueryGenerater2();
		try {
			queryGen.generateInFrequentQueries2(4, 100,
					gDB.getTotalNum() + 2000, rawQuery, 0, baseName
							+ "Query_Together");
			IGraphDatabase Data2_Together = new GraphDatabase_InMem(
					new NoPostingFeatures<IOneFeature>(baseName
							+ "Query_Together", MyFactory
							.getFeatureFactory(FeatureFactoryType.OneFeature)));
			RandomChoseDBGraph.randomlySplitDBGraph(Data2_Together, 10000,
					baseName + "TrainQuery", baseName + "TestQuery");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (MathException e) {
			e.printStackTrace();
		}
	}

	public static void buildOriginalIndex(BufferedWriter writer, String baseName)
			throws IOException, ParseException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseName
				+ "DataAIDS", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(baseName
				+ "TestQuery", MyFactory.getDFSCoder());
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(baseName
				+ "TrainQuery", MyFactory.getDFSCoder());
		double minSupport = 0.02; // Adjustable

		// int[] Ks = new int[]{1000, 3000, 4000, 5000};
		int[] Ks = new int[] { 2000 };
		writer.write("BuildIndexes With Dif Support \n");
		for (int K : Ks) {
			String str = IndexConstruction.buildSwapIndex(baseName, gDB,
					trainQuery, testQuery, baseName + "stream99/" + K + "/", K,
					minSupport, 0.99);
			writer.write(str);
			writer.flush();
		}
	}

	public static void updateOriginalIndex(BufferedWriter writer,
			String oldBaseName, String newBaseName) throws IOException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(oldBaseName
				+ "DataAIDS", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(newBaseName
				+ "TestQuery", MyFactory.getDFSCoder());
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(newBaseName
				+ "TrainQuery", MyFactory.getDFSCoder());
		double minSupport = 0.02; // Adjustable

		// int[] Ks = new int[]{1000, 3000, 4000, 5000};
		int[] Ks = new int[] { 2000 };
		writer.write("Update With Dif Feature Count \n");
		for (int K : Ks) {
			String lindexFolder = oldBaseName + "stream99/" + K + "/";
			String lindexUpdateFolder = newBaseName + "stream99/" + K + "/";
			String str = IndexUpdateForQueries.doUpdate(
					StatusType.decomposePreSelectAdv, gDB, -1, trainQuery,
					testQuery, lindexFolder, lindexUpdateFolder, minSupport,
					0.99);
			writer.write(str);
			writer.flush();
		}
	}

	public static void runOriginalIndex(BufferedWriter writer,
			String baseNameIndex, String baseNameQuery) throws IOException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseNameIndex
				+ "DataAIDS", MyFactory.getSmilesParser());
		IGraphDatabase testQuery = new GraphDatabase_OnDisk(baseNameQuery
				+ "TestQuery", MyFactory.getDFSCoder());

		int[] Ks = new int[] { 1000, 2000, 3000, 4000, 5000 };
		writer.write("Run Index With Dif Feature Count \n");
		for (int K : Ks) {
			String lindexFolder = baseNameIndex + "stream99/" + K + "/";
			String str = Util.stateToString(Util.runIndex(lindexFolder, gDB,
					testQuery));
			writer.write(str);
			writer.flush();
		}
	}
}
