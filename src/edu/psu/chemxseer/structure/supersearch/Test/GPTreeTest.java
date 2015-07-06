package edu.psu.chemxseer.structure.supersearch.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.supersearch.GPTree.GPFeatureMiner;
import edu.psu.chemxseer.structure.supersearch.GPTree.SupSearch_GPTree;
import edu.psu.chemxseer.structure.supersearch.GPTree.SupSearch_GPTreeBuilder;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;

/**
 * The Testing Class for the GPTree
 * 
 * @author dayuyuan
 * 
 */
public class GPTreeTest {

	private static String base = "/Users/dayuyuan/Documents/workspace/Experiment/GPTree/";
	private static String queryFile = "/Users/dayuyuan/Documents/workspace/Experiment/GPTree/TestQuery25";
	private static String dbFile = "/Users/dayuyuan/Documents/workspace/Experiment/GPTree/DBFile4";

	public void buildIndex() throws ParseException, IOException {
		String baseName = base + "GPTree/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();

		NoPostingFeatures dbFeatures = new NoPostingFeatures(dbFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		IGraphDatabase gDB = new GraphDatabase_InMem(dbFeatures);
		// 1. Mine Features:
		PostingFeatures freqFeatures = FeatureProcessorFG
				.frequentSubgraphMining(gDB, baseName + "feature", baseName
						+ "posting", 0.01, 10, gDB.getParser());
		GPFeatureMiner miner = new GPFeatureMiner();
		PostingFeatures sigFeatures = miner.minSignificantFeatures(
				freqFeatures, 1.25);
		PostingFeatures preSigFeaturesRaw = FeatureProcessorFG
				.frequentSubgraphMining(
						new GraphDatabase_InMem(sigFeatures.getFeatures()),
						baseName + "preSigRw", baseName + "preSigRwPosting",
						0.01, 10, MyFactory.getDFSCoder());
		NoPostingFeatures preSigFeatures = miner.minPrefixFeatures(
				preSigFeaturesRaw, sigFeatures.getFeatures().getfeatureNum());
		NoPostingFeatures prefFeatures = miner.minPrefixFeatures(freqFeatures,
				gDB.getTotalNum());
		// 2. Build the Index
		SupSearch_GPTreeBuilder builder = new SupSearch_GPTreeBuilder();
		builder.buildIndex(sigFeatures.getFeatures(), preSigFeatures,
				prefFeatures, gDB, baseName, false);

	}

	public void runQueries() throws NumberFormatException, IOException,
			ParseException {
		// 1. First Step: load the index
		String baseName = base;
		IGraphDatabase trainingDB = new GraphDatabase_OnDisk(dbFile,
				MyFactory.getDFSCoder());
		IGraphDatabase query = new GraphDatabase_OnDisk(queryFile,
				MyFactory.getSmilesParser());

		SupSearch_GPTreeBuilder builder = new SupSearch_GPTreeBuilder();
		SupSearch_GPTree index = builder.loadIndex(trainingDB, baseName, false);
		// 2. Run Queries
		AIDSExp.runQueries(query, index);
	}

	public static void main(String[] args) throws ParseException,
			NumberFormatException, IOException {
		GPTreeTest test = new GPTreeTest();
		test.runQueries();
	}
}
