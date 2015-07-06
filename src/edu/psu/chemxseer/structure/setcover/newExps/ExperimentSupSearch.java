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
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class ExperimentSupSearch {
	public static void main(String[] args) throws IOException, ParseException {
		// 1.
		String baseName = "/data/home/duy113/VLDBSetCover4/SupSearch_1.5/";
		// String baseName = "/Users/dayuyuan/Documents/Experiment/SetCover/";
		// preProcess0(baseName); //prepare for data
		// 2. buildOriginalindex purely on the AIDS data
		BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
		IndexConstructionSupSearch.buildOriginalIndex(writer, baseName
				+ "AIDS/");
		// 3. Re-construct Indexes
		IndexConstructionSupSearch.buildOriginalIndex(writer, baseName
				+ "AIDSEMOL/");
		// 4. Re-based the AIDS index
		IndexUpdateForDBSupSearch.rebaseGPTree(writer, baseName + "AIDSEMOL/",
				baseName + "AIDS/");
		IndexUpdateForDBSupSearch.rebaseLindexStream(writer, baseName
				+ "AIDSEMOL/", baseName + "AIDS/");
		// 5. update the re-based index
		IndexUpdateForDBSupSearch.updateIndexForNewDB(writer, baseName
				+ "AIDSEMOL/");
	}

	public static void preProcess0(String rootName) throws IOException {
		String baseName = rootName + "AIDS/";
		String baseName2 = rootName + "EMOL/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		temp = new File(baseName2);
		if (!temp.exists())
			temp.mkdirs();

		// 1. Generate DB AIDS: sample 40,000 frequent subgraphs
		// String rawQueryFile =
		// "/Users/dayuyuan/Documents/Experiment/SetCover/Query_Together_raw";
		// String rawQueryFile =
		// "/data/home/duy113/VLDBSetCover4/AIDS_0.008Q/Query_Together_raw";
		String rawQueryFile = "/data/home/duy113/VLDB13/AIDS/subSearch/Query_Together_raw";
		NoPostingFeatures<IOneFeature> rawDB = new NoPostingFeatures<IOneFeature>(
				rawQueryFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		IGraphDatabase gDB = new GraphDatabase_InMem(rawDB);
		String gDBName = baseName + "DataAIDS";
		RandomChoseDBGraph.randomlyChooseDBGraph(gDB, 40000, gDBName);
		// 2. Generate Query AIDS: sample 10,000 graphs
		String AIDSDBName = "/data/home/duy113/VLDBSetCover4/AIDS_0.003Q/DataAIDS";
		// String AIDSDBName = rootName + "rawDB";
		IGraphDatabase qDB = new GraphDatabase_OnDisk(AIDSDBName,
				MyFactory.getSmilesParser());
		String qNameTogether = baseName + "Query_Together";
		RandomChoseDBGraph.randomlyChooseDBGraph(qDB, 12000, qNameTogether);
		qDB = new GraphDatabase_OnDisk(qNameTogether,
				MyFactory.getSmilesParser());
		RandomChoseDBGraph.randomlySplitDBGraph(qDB, 10000, baseName
				+ "TrainQuery", baseName + "TestQuery");

		// 3. Generate DB for EMOL
		// rawQueryFile =
		// "/Users/dayuyuan/Documents/Experiment/SetCover/QueryNCI_Together_raw";
		rawQueryFile = "/data/home/duy113/VLDBSetCover4/AIDSEMOL/Ori/QueryNCI_Together_raw";
		rawDB = new NoPostingFeatures<IOneFeature>(rawQueryFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		gDB = new GraphDatabase_InMem(rawDB);
		gDBName = baseName2 + "DataNCI";
		RandomChoseDBGraph.randomlyChooseDBGraph(gDB, 16000, gDBName);
		// 4. Generate Query EMOL
		// String dbNCI =
		// "/Users/dayuyuan/Documents/Experiment/SetCover/DataNCI";
		String dbNCI = "/data/home/duy113/VLDBSetCover4/AIDSEMOL/Ori/DataNCI";
		qDB = new GraphDatabase_OnDisk(dbNCI, MyFactory.getSmilesParser());
		qNameTogether = baseName2 + "QueryNCI_Together";
		RandomChoseDBGraph.randomlyChooseDBGraph(qDB, 4800, qNameTogether);
		qDB = new GraphDatabase_OnDisk(qNameTogether,
				MyFactory.getSmilesParser());
		RandomChoseDBGraph.randomlySplitDBGraph(qDB, 4000, baseName2
				+ "TrainQueryNCI", baseName2 + "TestQueryNCI");
		// 5. Generated Mixed Data & Query
		preProcessDBChanage(rootName);
	}

	public static void preProcessDBChanage(String baseName) throws IOException {

		IGraphDatabase db = new GraphDatabase_OnDisk(
				baseName + "AIDS/DataAIDS", MyFactory.getDFSCoder());
		IGraphDatabase TrainQuery = new GraphDatabase_OnDisk(baseName
				+ "AIDS/TrainQuery", MyFactory.getSmilesParser());
		IGraphDatabase TestQuery = new GraphDatabase_OnDisk(baseName
				+ "AIDS/TestQuery", MyFactory.getSmilesParser());

		String foldName = baseName + "AIDSEMOL/";
		File temp = new File(foldName);
		if (!temp.exists())
			temp.mkdirs();
		RandomChoseDBGraph.randomlyChooseDBGraph(db,
				(int) (db.getTotalNum() * 0.6), foldName + "DataAIDS_6");

		RandomChoseDBGraph.merge(
				new GraphDatabase_OnDisk(foldName + "DataAIDS_6", MyFactory
						.getDFSCoder()),
				new GraphDatabase_OnDisk(baseName + "EMOL/DataNCI", MyFactory
						.getDFSCoder()), foldName + "DataAIDS");

		RandomChoseDBGraph.randomlyChooseDBGraph(TrainQuery,
				(int) (TrainQuery.getTotalNum() * 0.6), foldName
						+ "TrainQuery_6");
		RandomChoseDBGraph.merge(new GraphDatabase_OnDisk(foldName
				+ "TrainQuery_6", MyFactory.getDFSCoder()),
				new GraphDatabase_OnDisk(baseName + "EMOL/TrainQueryNCI",
						MyFactory.getSmilesParser()), foldName + "TrainQuery");

		RandomChoseDBGraph
				.randomlyChooseDBGraph(TestQuery,
						(int) (TestQuery.getTotalNum() * 0.6), foldName
								+ "TestQuery_6");
		RandomChoseDBGraph.merge(new GraphDatabase_OnDisk(foldName
				+ "TestQuery_6", MyFactory.getDFSCoder()),
				new GraphDatabase_OnDisk(baseName + "EMOL/TestQueryNCI",
						MyFactory.getSmilesParser()), foldName + "TestQuery");
	}

}
