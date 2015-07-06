package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * This is the class related to the PrefixIndex
 * 
 * @author dayuyuan
 * 
 */
public class PrefixIndexExp {

	/**
	 * Build the PrefixIndex
	 * 
	 * @param trainDB
	 * @param realDB
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public static void buildIndex(IGraphDatabase trainDB,
			IGraphDatabase realDB, String base, double minSupt)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		String baseName = base + "PrefIndex/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		// 1. Mine Frequent Subgraphs:
		PostingFeatures freqFeatures = FeatureProcessorFG
				.frequentSubgraphMining(trainDB, baseName + "feature", baseName
						+ "posting", minSupt, 10, trainDB.getParser());
		// 2. Mine Prefix Features
		PrefixFeatureMiner miner = new PrefixFeatureMiner();
		NoPostingFeatures<IOneFeature> selectedFeatures = miner
				.minPrefixFeatures(freqFeatures, trainDB.getTotalNum())
				.getFeatures();
		// 3. Build the Index
		SupSearch_PrefixIndexBuilder builder = new SupSearch_PrefixIndexBuilder();
		builder.buildIndex(selectedFeatures, realDB, baseName, false);
	}

	/**
	 * Build a Hierachical PrefixIndex
	 * 
	 * @param trainDB
	 * @param realDB
	 * @throws IOException
	 */
	public static void buildHiIndex(IGraphDatabase trainDB,
			IGraphDatabase realDB, int level, String base, double minSupt)
			throws IOException {
		String baseName = base + "PrefIndexHi/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		// 1. Mine Index Features: multi level indexing features: this involve
		// multiple times frequent subgrph mining
		// and also feature selection.
		PostingFeatures[] selectedFeatures = new PostingFeatures[level];
		PrefixFeatureMiner miner = new PrefixFeatureMiner();
		for (int i = 0; i < level; i++) {
			PostingFeatures freqFeatures = FeatureProcessorFG
					.frequentSubgraphMining(trainDB, baseName + "feature" + i,
							baseName + "posting" + i, minSupt, 10,
							trainDB.getParser());
			System.out.println("Level: " + i);
			selectedFeatures[i] = miner.minPrefixFeatures(freqFeatures,
					trainDB.getTotalNum());
			trainDB = new GraphDatabase_InMem(selectedFeatures[i].getFeatures());
		}

		// 2. Build the Hierarchy index
		SupSearch_PrefixIndexHiBuilder builder = new SupSearch_PrefixIndexHiBuilder();
		builder.buildIndex(selectedFeatures, baseName, realDB, false);
	}

	public static void runIndex(IGraphDatabase gDB, IGraphDatabase fakeQuery,
			String base, boolean lucene_in_mem) {
		// First Load the index
		SupSearch_PrefixIndexBuilder builder = new SupSearch_PrefixIndexBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadIndex(gDB, base + "PrefIndex/",
					lucene_in_mem);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searcher != null) {
			try {
				AIDSExp.runQueries(fakeQuery, searcher);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}

	public static void runHiIndex(IGraphDatabase gDB, IGraphDatabase fakeQuery,
			String base, boolean lucene_in_mem) {
		// First Load the index
		SupSearch_PrefixIndexHiBuilder builder = new SupSearch_PrefixIndexHiBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadIndex(base + "PrefIndexHi/", 2, gDB,
					lucene_in_mem);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searcher != null) {
			try {
				AIDSExp.runQueries(fakeQuery, searcher);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}

	public static void memoryConsumptionHi(IGraphDatabase realDB, String base,
			boolean lucene_in_mem) throws NumberFormatException, IOException {
		SupSearch_PrefixIndexHiBuilder builder = new SupSearch_PrefixIndexHiBuilder();
		for (int i = 0; i < 3; i++) {
			MemoryConsumptionCal.runGC();
			double start = MemoryConsumptionCal.usedMemoryinMB();
			@SuppressWarnings("unused")
			ISearcher searcher = builder.loadIndex(base + "PrefIndexHi/", 2,
					realDB, lucene_in_mem);
			MemoryConsumptionCal.runGC();
			double end = MemoryConsumptionCal.usedMemoryinMB();
			System.out.print((end - start));
			System.out.print(",");
			searcher = null;
		}
		System.out.println();
	}
}
