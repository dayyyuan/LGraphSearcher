package edu.psu.chemxseer.structure.supersearch.LWTree;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.LWSetCover.MaxCover_FeatureSelector;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class LWIndexExp {
	/**
	 * Train the indexing features with the trainingDB & trainingQuery Build the
	 * graph index with the realDB
	 */
	public static int buildIndex(IGraphDatabase trainDB,
			IGraphDatabase trainQuery, IGraphDatabase realDB,
			GraphParser gParser, String base, double minSupt, int featureCount,
			int minScore) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		// 1. Mine Indexing Features
		File dir = new File(base + "LWIndex/");
		if (!dir.exists())
			dir.mkdirs();
		PostingFeaturesMultiClass frequentSubgraphs = FeatureProcessorDuralClass
				.frequentSubgraphMining(trainDB, trainQuery, gParser, base
						+ "LWIndex/", minSupt, -1, 10);

		MaxCover_FeatureSelector selector = new MaxCover_FeatureSelector();
		NoPostingFeatures<OneFeatureMultiClass> selectedFeatures = selector
				.minePrefixFeatures(frequentSubgraphs, trainDB, featureCount,
						minScore);
		selectedFeatures.saveFeatures(base + "LWIndex/selectedFeatures");
		// 2. Build the Index using the selectedFeatures
		SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
		// build & save the index
		SupSearch_LWIndex index = builder.buildIndex(
				new NoPostingFeatures_Ext<OneFeatureMultiClass>(
						selectedFeatures), frequentSubgraphs
						.getClassGraphsCount()[1], realDB, base + "LWIndex/",
				false);
		return index.getFeatureCount();
	}

	/**
	 * Train the indexing features with streaming algorithms Build the graph
	 * index with the realDB
	 */
	/*
	 * public static int buildIndexStream(IGraphDatabase trainDB, IGraphDatabase
	 * trainQuery, IGraphDatabase realDB, GraphParser gParser, String base,
	 * double minSupt, int featureCount) throws CorruptIndexException,
	 * LockObtainFailedException, IOException, ParseException{ //1. Mine
	 * Indexing Features File dir = new File(base + "LWIndexStream/");
	 * if(!dir.exists()) dir.mkdirs();
	 * 
	 * GSpanMiner_MultiClass_Iterative gen =
	 * CIndexFeatureMiner.genFreqFeatures(trainDB, trainQuery, gParser,
	 * base+"LWIndexStream/", minSupt); Stream_FeatureSelector selector = new
	 * Stream_FeatureSelector(); NoPostingFeatures<OneFeatureMultiClass>
	 * selectedFeatures = selector.minePrefixFeatures(gen, trainDB,
	 * featureCount); selectedFeatures.saveFeatures(base +
	 * "LWIndexStream/selectedFeatures"); //2. Build the Index using the
	 * selectedFeatures SupSearch_LWIndexBuilder builder = new
	 * SupSearch_LWIndexBuilder(); // build & save the index SupSearch_LWIndex
	 * index = builder.buildIndex( new
	 * NoPostingFeatures_Ext<OneFeatureMultiClass>(selectedFeatures),
	 * trainQuery.getTotalNum(), realDB, base + "LWIndexStream/", false); return
	 * index.getFeatureCount(); }
	 */

	/**
	 * Train the indexing features with the trainingDB & trainingQuery And the
	 * already mined frequent subgraphs Build the graph index with the realDB
	 */
	public static int buildIndex(IGraphDatabase trainingDB,
			IGraphDatabase trainQuery, IGraphDatabase realDB,
			GraphParser gParser, String base, double minSupt, int featureCount,
			int minScore, PostingFeaturesMultiClass frequentSubgraphs)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, ParseException {
		// 1. Mine Indexing Features
		File dir = new File(base + "LWIndex/");
		if (!dir.exists())
			dir.mkdirs();

		MaxCover_FeatureSelector selector = new MaxCover_FeatureSelector();
		NoPostingFeatures<OneFeatureMultiClass> selectedFeatures = selector
				.minePrefixFeatures(frequentSubgraphs, trainingDB,
						featureCount, minScore);
		selectedFeatures.saveFeatures(base + "LWIndex/selectedFeatures");
		// 2. Build the Index using the selectedFeatures
		SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
		// build & save the index
		SupSearch_LWIndex index = builder.buildIndex(
				new NoPostingFeatures_Ext<OneFeatureMultiClass>(
						selectedFeatures), frequentSubgraphs
						.getClassGraphsCount()[1], realDB, base + "LWIndex/",
				false);
		return index.getFeatureCount();
	}

	public static void runIndex(IGraphDatabase gDB, IGraphDatabase query,
			String base, boolean lucene_in_mem) {
		// First Load the index
		SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadIndex(gDB, base + "LWIndex/", lucene_in_mem);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searcher != null) {
			try {
				AIDSExp.runQueries(query, searcher);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * public static void runIndexStream(IGraphDatabase gDB, IGraphDatabase
	 * query, String base, boolean lucene_in_mem) { // First Load the index
	 * SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
	 * ISearcher searcher = null; try { searcher = builder.loadIndex(gDB, base +
	 * "LWIndexStream/", lucene_in_mem); } catch (IOException e) {
	 * e.printStackTrace(); } if(searcher!=null){ try {
	 * AIDSExp.runQueries(query, searcher); } catch (IOException e) {
	 * e.printStackTrace(); } catch (ParseException e) { e.printStackTrace(); }
	 * } }
	 */

	public static void memoryConsumption(IGraphDatabase gDB, String base,
			boolean lucene_in_mem) throws IOException {
		SupSearch_LWIndexBuilder builder = new SupSearch_LWIndexBuilder();
		for (int i = 0; i < 3; i++) {
			MemoryConsumptionCal.runGC();
			double start = MemoryConsumptionCal.usedMemoryinMB();
			builder.loadIndex(gDB, base + "LWIndex/", lucene_in_mem);
			MemoryConsumptionCal.runGC();
			double end = MemoryConsumptionCal.usedMemoryinMB();
			System.out.print((end - start));
			System.out.print(",");
		}
		System.out.println();
	}
}
