package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;

public class GeneratedExp {
	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		// 1. Build the Index with Varing "database size"
		// String dirName ="/data/home/duy113/SupSearchExp/Generated/";
		// String dirName = "/Users/dayuyuan/Documents/workspace/Experiment/";
		String dirName = "/data/santa/SupgraphSearchExp/Generated/";
		String oriQuery = "Query";
		String dbFileName = "DBFile";
		String trainQueryName = "TrainQuery";
		String testQueryName = "TestQuery";

		for (int i = 1; i <= 5; i++) {
			String baseName = dirName + i + "/";
			File temp = new File(baseName);
			if (!temp.exists())
				temp.mkdirs();
			// 1. Generate 10,000 subgraphs as database graph
			String featureFileName = baseName + "frequentSubgraphs";
			PostingFeatures freqFeatures = FeatureProcessorFG
					.frequentSubgraphMining(baseName + oriQuery,
							featureFileName, null, 0.005, 50,
							MyFactory.getDFSCoder());
			IGraphDatabase rawDB = new GraphDatabase_InMem(
					freqFeatures.getFeatures());
			RandomChoseDBGraph.randomlyChooseDBGraph(rawDB, 10000, baseName
					+ dbFileName);
			// 2. Get statistics:
			// System.out.println("Statistics of the overall query file");
			IGraphDatabase queryGraphs = new GraphDatabase_OnDisk(baseName
					+ oriQuery, MyFactory.getDFSCoder());
			// DataPreparation.getStatistics(queryGraphs);
			// System.out.println("statistics of the overal database file");
			// DataPreparation.getStatistics(featureFileName);
			// 3. Generate the train & testing queries
			RandomChoseDBGraph.randomlySplitDBGraph(queryGraphs, 8000, baseName
					+ trainQueryName, baseName + testQueryName);
			// 4. Build the Index & Run the Index
			// GraphDatabase trainingDB = new GraphDatabase_OnDisk(baseName +
			// dbFileName, MyFactory.getDFSCoder());
			// GraphDatabase trainQuery = new GraphDatabase_OnDisk(baseName +
			// trainQueryName, MyFactory.getDFSCoder());
			// System.out.println("Build LWIndex");
			// LWIndexExp.buildIndex(trainingDB, trainQuery, trainingDB,
			// MyFactory.getDFSCoder(), baseName);
			// System.out.println("Build 2-level prefix Index");
			// PrefixIndexExp.buildHiIndex(trainingDB, trainingDB, 2, baseName);
			// System.out.println("Build GPTree Index");
			// GPTreeExp.buildIndex(trainingDB, trainingDB, baseName);
			// System.out.println("Build CIndexTopDown");
			// CIndexExp.buildIndexTopDown(trainingDB, trainQuery, trainingDB,
			// MyFactory.getDFSCoder(), baseName);
		}

	}
}
