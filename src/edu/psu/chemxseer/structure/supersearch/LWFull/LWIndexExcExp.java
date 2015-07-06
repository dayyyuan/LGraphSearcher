package edu.psu.chemxseer.structure.supersearch.LWFull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.LWSetCover.MaxCover_FeatureSelector;
import edu.psu.chemxseer.structure.supersearch.LWSetCover.Stream_FeatureSelector;
import edu.psu.chemxseer.structure.supersearch.LWSetCover.Stream_FeatureSelector2;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class LWIndexExcExp {

	/**
	 * For Stream Feature, It is required to set the feature count
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public static int buildIndexWithFeatureMining(IGraphDatabase trainDB,
			IGraphDatabase trainQuery, IGraphDatabase realDB,
			GraphParser gParser, String baseName, double minSupt,
			int featureMinerID, int[] featureCount) throws IOException,
			ParseException {
		// 1. Mine Indexing Features
		File dir = new File(baseName);
		if (!dir.exists())
			dir.mkdirs();
		NoPostingFeatures<OneFeatureMultiClass> selectedFeatures = null;
		if (featureMinerID == 0) {
			// Batch-mode feature miner
			PostingFeaturesMultiClass frequentSubgraphs = FeatureProcessorDuralClass
					.frequentSubgraphMining(trainDB, trainQuery, gParser,
							baseName, minSupt, -1, 10);
			MaxCover_FeatureSelector selector = new MaxCover_FeatureSelector();
			selectedFeatures = selector.minePrefixFeatures(frequentSubgraphs,
					trainDB, featureCount[0], 2);
			featureCount[0] = selectedFeatures.getfeatureNum();
		} else {
			// Stream Miner
			GSpanMiner_MultiClass_Iterative gen = FeatureProcessorDuralClass
					.getPatternEnumerator(trainDB, trainQuery, minSupt, -1, 10);
			if (featureMinerID == 1) {
				// Stream1
				Stream_FeatureSelector selector = new Stream_FeatureSelector();
				selectedFeatures = selector.minePrefixFeatures(gen, realDB,
						featureCount[0]);
			} else {
				// Stream2
				Stream_FeatureSelector2 selector = new Stream_FeatureSelector2();
				selectedFeatures = selector.minePrefixFeatures(gen, realDB,
						featureCount[0]);
			}
		}
		selectedFeatures.saveFeatures(baseName + "selectedFeatures");

		// 2. Build the Index using the selectedFeatures
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		SupSearch_LWFull index = builder.buildIndex(
				new NoPostingFeatures_Ext<OneFeatureMultiClass>(
						selectedFeatures), trainQuery.getTotalNum(), realDB,
				baseName, false);
		return index.getFeatureCount();
	}

	/**
	 * Assume the features are selected by the LWIndexInclusive and stored under
	 * the LWIndex folder Build the LWIndexExclusive
	 */
	public static int buildIndexWithNoFeatureMining(IGraphDatabase trainingDB,
			IGraphDatabase trainQuery, IGraphDatabase realDB,
			GraphParser gParser, String base, String featureBase, double minSupt)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, ParseException {
		// 1. Mine Indexing Features:
		File dir = new File(base);
		if (!dir.exists())
			dir.mkdirs();

		NoPostingFeatures<OneFeatureMultiClass> selectedFeatures = new NoPostingFeatures<OneFeatureMultiClass>(
				featureBase + "/selectedFeatures",
				MyFactory.getFeatureFactory(FeatureFactoryType.MultiFeature));

		// 2. Build the Index using the selectedFeatures
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		// build & save the index
		SupSearch_LWFull index = builder.buildIndex(
				new NoPostingFeatures_Ext<OneFeatureMultiClass>(
						selectedFeatures), trainQuery.getTotalNum(), realDB,
				base, false);
		return index.getFeatureCount();
	}

	/*
	 * public static void runIndexExc(IGraphDatabase gDB, IGraphDatabase query,
	 * String base, boolean lucene_in_mem) { // First Load the index
	 * SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
	 * ISearcher searcher = null; try { searcher = builder.loadIndex(gDB, base +
	 * "LWIndexExc/", lucene_in_mem); } catch (IOException e) {
	 * e.printStackTrace(); } if(searcher!=null){ try {
	 * AIDSExp.runQueries(query, searcher); } catch (IOException e) {
	 * e.printStackTrace(); } catch (ParseException e) { e.printStackTrace(); }
	 * } }
	 */

	public static void runIndex(IGraphDatabase gDB, IGraphDatabase query,
			String baseName, boolean lucene_in_mem) {
		// First Load the index
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadIndex(gDB, baseName, lucene_in_mem);
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

	/**
	 * Building the Index by MIning Index Features with featureCount specified
	 * Assuming the raw frequent subgraphs are mined and stored under LWIndex
	 * folder
	 */
	/*
	 * public static int buildIndex2(IGraphDatabase trainingDB, IGraphDatabase
	 * trainQuery, IGraphDatabase realDB, GraphParser gParser, String base,
	 * double minSupt, int featureCount, int minScore) throws
	 * CorruptIndexException, LockObtainFailedException, IOException,
	 * ParseException{ //1. Mine Indexing Features: File dir = new File(base +
	 * "LWIndexExcK/"); if(!dir.exists()) dir.mkdirs();
	 * //PostingFeaturesMultiClass frequentSubgraphs = //
	 * CIndexFeatureMiner.minFreqFeatures(trainingDB, trainQuery, gParser,
	 * base+"LindexFull/", minSupt); String fName = base + "LWIndex/feature";
	 * String[] pNames = new String[]{base + "LWIndex/posting0", base +
	 * "LWIndex/posting1", base + "LWIndex/posting2", base +
	 * "LWIndex/posting3"}; int[] classCount = new
	 * int[]{trainingDB.getTotalNum(), trainQuery.getTotalNum()};
	 * NoPostingFeatures<OneFeatureMultiClass> features = new
	 * NoPostingFeatures<OneFeatureMultiClass>(fName,
	 * MyFactory.getFeatureFactory(FeatureFactoryType.MultiFeature));
	 * PostingFeaturesMultiClass frequentSubgraphs = new
	 * PostingFeaturesMultiClass(pNames, features,classCount);
	 * 
	 * MaxCover_FeatureSelector selector = new MaxCover_FeatureSelector();
	 * NoPostingFeatures<OneFeatureMultiClass> selectedFeatures =
	 * selector.minePrefixFeatures(frequentSubgraphs, trainingDB, featureCount,
	 * minScore); selectedFeatures.saveFeatures(base +
	 * "LWIndexExcK/selectedFeatures");
	 * 
	 * //2. Build the Index using the selectedFeatures SupSearch_LWFullBuilder
	 * builder = new SupSearch_LWFullBuilder(); // build & save the index
	 * SupSearch_LWFull index = builder.buildIndex(new
	 * NoPostingFeatures_Ext<OneFeatureMultiClass>(selectedFeatures),
	 * frequentSubgraphs.getClassGraphsCount()[1], realDB, base +
	 * "LWIndexExcK/", false); return index.getFeatureCount(); }
	 */

	public static void memoryConsumption(IGraphDatabase gDB, String base,
			boolean lucene_in_mem) throws IOException {
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		for (int i = 0; i < 3; i++) {
			MemoryConsumptionCal.runGC();
			double start = MemoryConsumptionCal.usedMemoryinMB();
			@SuppressWarnings("unused")
			ISearcher searcher = builder.loadIndex(gDB, base + "LWIndexExc/",
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
