package edu.psu.chemxseer.structure.supersearch.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.supersearch.LWSetCover.MaxCover_FeatureSelector;
import edu.psu.chemxseer.structure.supersearch.LWTree.SupSearch_LWIndex;
import edu.psu.chemxseer.structure.supersearch.LWTree.SupSearch_LWIndexBuilder;

public class LWIndexTest {
	private static String base = "/Users/dayuyuan/Documents/workspace/Experiment/";
	private static String queryFile = "/Users/dayuyuan/Documents/workspace/Experiment/TestQuery";
	private static String dbFile = "/Users/dayuyuan/Documents/workspace/Experiment/SupSearchDB";

	public PostingFeaturesMultiClass preProcess() throws IOException {
		// 1.First step: mine the queryFile to generate the graph database
		PostingFeatures freqFeatures = FeatureProcessorFG
				.frequentSubgraphMining(queryFile, dbFile, null, 0.05, 50,
						MyFactory.getSmilesParser());
		// 2. Second step: mine the raw features (dual class) from both query
		// database file & query file
		IGraphDatabase queryDB = new GraphDatabase_OnDisk(queryFile,
				MyFactory.getSmilesParser());
		IGraphDatabase gDB = new GraphDatabase_InMem(freqFeatures.getFeatures());
		return FeatureProcessorDuralClass.frequentSubgraphMining(gDB, queryDB,
				gDB.getParser(), base, 0.01, -1, 10);
	}

	public void buildIndex(PostingFeaturesMultiClass frequentSubgraphs)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, ParseException {
		// 1. Mine the Indexing Features: how many, until all covered
		MaxCover_FeatureSelector selector = new MaxCover_FeatureSelector();
		IGraphDatabase gDB = new GraphDatabase_InMem(new NoPostingFeatures(
				dbFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature)));
		NoPostingFeatures<OneFeatureMultiClass> selectedFeatures = selector
				.minePrefixFeatures(frequentSubgraphs, gDB, Integer.MAX_VALUE,
						1);
		// 2. Build the Index using the selectedFeatures
		File dir = new File(base + "LWIndex/");
		if (!dir.exists())
			dir.mkdirs();

		SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
		// build & save the index
		builder.buildIndex(new NoPostingFeatures_Ext<OneFeatureMultiClass>(
				selectedFeatures), frequentSubgraphs.getClassGraphsCount()[1],
				gDB, base + "LWIndex/", false);

	}

	public void runQueries() throws IOException, ParseException {
		// 1. Load the Index
		IGraphDatabase gDB = new GraphDatabase_InMem(new NoPostingFeatures(
				dbFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature)));
		SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
		SupSearch_LWIndex index = builder.loadIndex(gDB, base + "LWIndex/",
				false);
		// 2. Process Queries:
		IGraphDatabase query = new GraphDatabase_OnDisk(queryFile,
				MyFactory.getSmilesParser());
		FastSU iso = new FastSU();
		long[] totalTime = new long[4];
		for (int i = 0; i < query.getTotalNum(); i++) {
			Graph q = query.findGraph(i);
			int realAnswerCount = 0;
			long[] TimeComponent = new long[4];
			int[] Number = new int[2];
			int answerCount = index.getAnswer(q, TimeComponent, Number).size();
			totalTime[0] += TimeComponent[0];
			totalTime[1] += TimeComponent[1];
			totalTime[2] += TimeComponent[2];
			totalTime[3] += TimeComponent[3];
			// for(int w = 0; w < gDB.getTotalNum(); w++){
			// if(iso.isIsomorphic(gDB.findGraph(w), q))
			// realAnswerCount++;
			// }
			// if(realAnswerCount!= answerCount){
			// System.out.println("This is what I don't want to see");
			// }
		}

		System.out.println(totalTime[0] + "," + totalTime[1] + ","
				+ totalTime[2] + "," + totalTime[3]);
	}

	public static void main(String[] args) throws IOException, ParseException {
		LWIndexTest test = new LWIndexTest();
		IGraphDatabase trainingDB = new GraphDatabase_InMem(
				new NoPostingFeatures(dbFile, MyFactory
						.getFeatureFactory(FeatureFactoryType.OneFeature)));
		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(queryFile,
				MyFactory.getSmilesParser());
		PostingFeaturesMultiClass frequentSubgraphs = FeatureProcessorDuralClass
				.frequentSubgraphMining(trainingDB, trainQuery,
						trainingDB.getParser(), base + "LWIndex/", 0.1, -1, 10);

		test.buildIndex(frequentSubgraphs);
		test.runQueries();
	}
}
