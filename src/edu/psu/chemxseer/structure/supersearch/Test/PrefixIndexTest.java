package edu.psu.chemxseer.structure.supersearch.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.PrefixFeatureMiner;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.SupSearch_PrefixIndex;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.SupSearch_PrefixIndexBuilder;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.SupSearch_PrefixIndexHi;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.SupSearch_PrefixIndexHiBuilder;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;

/**
 * The Test For the correctness of the PrefixIndex
 * 
 * @author dayuyuan
 * 
 */
public class PrefixIndexTest {

	private static String base = "/Users/dayuyuan/Documents/workspace/Experiment1/";
	private static String queryFile = "/Users/dayuyuan/Documents/workspace/Experiment1/DBFile";
	private static String dbFile = "/Users/dayuyuan/Documents/workspace/Experiment1/SupSearchDB";

	public void buildHiIndex() throws IOException {
		String baseName = base + "PrefIndexHi/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		// I plan to mine 2 level index
		// 1. Mine Index Features
		PostingFeatures[] selectedFeatures = new PostingFeatures[2];
		PrefixFeatureMiner miner = new PrefixFeatureMiner();
		// 1.1 mine first level features
		NoPostingFeatures<IOneFeature> dbFeatures = new NoPostingFeatures<IOneFeature>(
				dbFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		IGraphDatabase gDB = new GraphDatabase_InMem(dbFeatures);
		PostingFeatures freqFeatures = FeatureProcessorFG
				.frequentSubgraphMining(gDB, baseName + "feature", baseName
						+ "posting", 0.01, 10, gDB.getParser());
		selectedFeatures[0] = miner.minPrefixFeatures(freqFeatures,
				gDB.getTotalNum());
		// 1.2 mine second level features
		freqFeatures = FeatureProcessorFG.frequentSubgraphMining(
				new GraphDatabase_InMem(selectedFeatures[0].getFeatures()),
				baseName + "feature_1", baseName + "posting1", 0.01, 10,
				gDB.getParser());
		selectedFeatures[1] = miner.minPrefixFeatures(freqFeatures,
				selectedFeatures[0].getFeatures().getfeatureNum());
		// 2. Build the Hierarchy index
		SupSearch_PrefixIndexHiBuilder builder = new SupSearch_PrefixIndexHiBuilder();
		builder.buildIndex(selectedFeatures, baseName, gDB, false);
	}

	public void buildIndex() throws CorruptIndexException,
			LockObtainFailedException, IOException {
		String baseName = base + "PrefIndex/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();

		NoPostingFeatures<IOneFeature> dbFeatures = new NoPostingFeatures<IOneFeature>(
				dbFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		IGraphDatabase gDB = new GraphDatabase_InMem(dbFeatures);
		// 1. Mine Frequent Subgraphs:
		PostingFeatures freqFeatures = FeatureProcessorFG
				.frequentSubgraphMining(gDB, baseName + "feature", baseName
						+ "posting", 0.01, 10, gDB.getParser());
		// 2. Mine Prefix Features
		PrefixFeatureMiner miner = new PrefixFeatureMiner();
		NoPostingFeatures<IOneFeature> selectedFeatures = miner
				.minPrefixFeatures(freqFeatures, gDB.getTotalNum())
				.getFeatures();
		// 3. Build the Index
		SupSearch_PrefixIndexBuilder builder = new SupSearch_PrefixIndexBuilder();
		builder.buildIndex(selectedFeatures, gDB, baseName, false);
	}

	public void runQueries() throws NumberFormatException, IOException,
			ParseException {
		String baseName = base + "PrefIndex/";
		// 1. Load the INdex
		NoPostingFeatures<IOneFeature> dbFeatures = new NoPostingFeatures<IOneFeature>(
				dbFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		IGraphDatabase gDB = new GraphDatabase_InMem(dbFeatures);
		SupSearch_PrefixIndexBuilder builder = new SupSearch_PrefixIndexBuilder();
		SupSearch_PrefixIndex index = builder.loadIndex(gDB, baseName, false);
		// 2. Queries

		IGraphDatabase query = new GraphDatabase_OnDisk(queryFile,
				MyFactory.getSmilesParser());
		AIDSExp.runQueries(query, index);
		// FastSU iso = new FastSU();
		// for(int i = 0; i < query.getTotalNum(); i++){
		// Graph q = query.findGraph(i);
		// long[] TimeComponent = new long[4];
		// int[] Number = new int[2];
		// List<GraphResult> answers = index.getAnswer(q, TimeComponent,
		// Number);
		// // for(GraphResult result : answers)
		// // System.out.println(result.getID());
		// int realAnswerCount = 0;
		// for(int w = 0; w < gDB.getTotalNum(); w++){
		// if(iso.isIsomorphic(gDB.findGraph(w), q)){
		// // System.out.println(w);
		// realAnswerCount++;
		// }
		//
		// }
		// if(realAnswerCount!= answers.size()){
		// System.out.println("This is what I don't want to see");
		// }
		// }
	}

	public void runHiQueries() throws NumberFormatException, IOException,
			ParseException {
		String baseName = base + "PrefIndexHi/";
		// 1. Load the 2-level hierachical index
		NoPostingFeatures<IOneFeature> dbFeatures = new NoPostingFeatures<IOneFeature>(
				dbFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		IGraphDatabase gDB = new GraphDatabase_InMem(dbFeatures);
		SupSearch_PrefixIndexHiBuilder builder = new SupSearch_PrefixIndexHiBuilder();
		SupSearch_PrefixIndexHi index = builder.loadIndex(baseName, 2, gDB,
				false);
		// 2. Query processing
		IGraphDatabase query = new GraphDatabase_OnDisk(queryFile,
				MyFactory.getSmilesParser());
		AIDSExp.runQueries(query, index);
		// FastSU iso = new FastSU();
		// for(int i = 0; i < query.getTotalNum(); i++){
		// Graph q = query.findGraph(i);
		// long[] TimeComponent = new long[4];
		// int[] Number = new int[2];
		// List<GraphResult> answers = index.getAnswer(q, TimeComponent,
		// Number);
		// // for(GraphResult result : answers)
		// // System.out.println(result.getID());
		// int realAnswerCount = 0;
		// for(int w = 0; w < gDB.getTotalNum(); w++){
		// if(iso.isIsomorphic(gDB.findGraph(w), q)){
		// // System.out.println(w);
		// realAnswerCount++;
		// }
		//
		// }
		// if(realAnswerCount!= answers.size()){
		// System.out.println("This is what I don't want to see");
		// }
		// }

	}

	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException, NumberFormatException,
			ParseException {
		PrefixIndexTest test = new PrefixIndexTest();
		test.buildIndex();
		test.runQueries();
		// test.buildHiIndex();
		// test.runHiQueries();
	}
}
