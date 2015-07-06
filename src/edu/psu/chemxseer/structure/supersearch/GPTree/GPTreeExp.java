package edu.psu.chemxseer.structure.supersearch.GPTree;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The Class for GPTree Experiment
 * 
 * @author dayuyuan
 * 
 */
public class GPTreeExp {

	public static void buildIndex(IGraphDatabase trainDB,
			IGraphDatabase realDB, String base, double minSupt)
			throws ParseException, IOException {
		String baseName = base + "GPTree/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();

		// 1. Mine Features:
		PostingFeatures freqFeatures = FeatureProcessorFG
				.frequentSubgraphMining(trainDB, baseName + "feature", baseName
						+ "posting", minSupt, 10, trainDB.getParser());
		GPFeatureMiner miner = new GPFeatureMiner();
		// 1.1 Mine Significant Features
		PostingFeatures sigFeatures = miner.minSignificantFeatures(
				freqFeatures, 1.25);
		// 1.2 Prefix-Significant Features
		PostingFeatures preSigFeaturesRaw = FeatureProcessorFG
				.frequentSubgraphMining(
						new GraphDatabase_InMem(sigFeatures.getFeatures()),
						baseName + "preSigRw", baseName + "preSigRwPosting",
						minSupt, 10, MyFactory.getDFSCoder());
		NoPostingFeatures<IOneFeature> preSigFeatures = miner
				.minPrefixFeatures(preSigFeaturesRaw, sigFeatures.getFeatures()
						.getfeatureNum());
		// 1.3 Prefix Sharing Feature Mining
		NoPostingFeatures<IOneFeature> prefFeatures = miner.minPrefixFeatures(
				freqFeatures, trainDB.getTotalNum());
		// 2. Build the Index
		SupSearch_GPTreeBuilder builder = new SupSearch_GPTreeBuilder();
		builder.buildIndex(sigFeatures.getFeatures(), preSigFeatures,
				prefFeatures, realDB, baseName, false);
	}

	public static void runIndex(IGraphDatabase gDB, IGraphDatabase query,
			String baseName, boolean lucene_in_mem) {
		// First Load the index
		SupSearch_GPTreeBuilder builder = new SupSearch_GPTreeBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadIndex(gDB, baseName + "GPTree/",
					lucene_in_mem);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (searcher != null) {
			try {
				AIDSExp.runQueries(query, searcher);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void memoryConsumption(IGraphDatabase realDB, String base,
			boolean lucene_in_mem) throws NumberFormatException, IOException {
		SupSearch_GPTreeBuilder builder = new SupSearch_GPTreeBuilder();
		for (int i = 0; i < 3; i++) {
			MemoryConsumptionCal.runGC();
			double start = MemoryConsumptionCal.usedMemoryinMB();
			@SuppressWarnings("unused")
			ISearcher searcher = builder.loadIndex(realDB, base + "GPTree/",
					lucene_in_mem);
			MemoryConsumptionCal.runGC();
			double end = MemoryConsumptionCal.usedMemoryinMB();
			System.out.print((end - start));
			System.out.print(",");
			searcher = null;
		}
		System.out.println();
	}
}
