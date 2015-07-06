package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The class related CIndex experiments: both single CIndex & Hierachical CIndex
 * 
 * @author dayuyuan
 * 
 */
public class CIndexExp {

	public static void buildIndexTopDown(IGraphDatabase trainDB,
			IGraphDatabase trainQuery, IGraphDatabase realDB,
			GraphParser gParser, String base, double minSupt, int minQuerySplit)
			throws IOException {
		String baseName = base + "CIndexTopDown/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		// 1. Mine Frequent Features
		PostingFeaturesMultiClass frequentSubgraphs = FeatureProcessorDuralClass
				.frequentSubgraphMining(trainDB, trainQuery,
						trainDB.getParser(), baseName, minSupt, -1, 10);

		SupSearch_CIndexTopDownBuilder builder = new SupSearch_CIndexTopDownBuilder();
		System.out.println("Select Index Patterns for CIndexTopDown");
		CIndexTreeFeatureSelector fSelector = new CIndexTreeFeatureSelector(
				frequentSubgraphs, minQuerySplit);
		CIndexTreeFeatureNode root = fSelector.constructFeatureTree();
		// 2. Construct the TopDown cIndex
		builder.buildCIndexTopDown(realDB, root, gParser, baseName, false);
	}

	public static void runIndexTopDown(IGraphDatabase gDB,
			IGraphDatabase query, GraphParser gParser, String baseName,
			boolean lucene_in_mem) {
		// First Load the index
		SupSearch_CIndexTopDownBuilder builder = new SupSearch_CIndexTopDownBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadCIndexTopDown(gDB, baseName
					+ "CIndexTopDown/", gParser, lucene_in_mem);
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

	public static void memoryConsumptionTopDown(IGraphDatabase realDB,
			GraphParser gParser, String base, boolean lucene_in_mem)
			throws NumberFormatException, IOException {
		SupSearch_CIndexTopDownBuilder builder = new SupSearch_CIndexTopDownBuilder();
		for (int i = 0; i < 3; i++) {
			MemoryConsumptionCal.runGC();
			double start = MemoryConsumptionCal.usedMemoryinMB();
			@SuppressWarnings("unused")
			ISearcher searcher = builder.loadCIndexTopDown(realDB, base
					+ "CIndexTopDown/", gParser, lucene_in_mem);
			MemoryConsumptionCal.runGC();
			double end = MemoryConsumptionCal.usedMemoryinMB();
			System.out.print((end - start));
			System.out.print(",");
			searcher = null;
		}
		System.out.println();
	}

	public static int buildIndexBottomUp(IGraphDatabase trainDB,
			IGraphDatabase trainQuery, GraphParser gParser,
			IGraphDatabase realDB, String base, double minSupt,
			int baseFeatureCount, int uperFeatureCount) throws IOException {
		String baseName = base + "CIndexBottomUp/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();

		// 1. Mine First Level Index Features
		System.out
				.println("For Leve 0 of cIndex_BottomUp:frequent Subgraph Mining & Feature Selection");
		PostingFeaturesMultiClass firstFeatures = CIndexFeatureMiner
				.mineFeatures(baseFeatureCount, trainDB, trainQuery, gParser,
						baseName + "1", minSupt);

		// 2. Mine Second Level Index Features
		IGraphDatabase gDB2 = new GraphDatabase_InMem(
				firstFeatures.getFeatures());
		System.out
				.println("For Leve 1 of cIndex_BottomUp:frequent Subgraph Mining & Feature Selection");
		PostingFeaturesMultiClass secondFeatures = CIndexFeatureMiner
				.mineFeatures(uperFeatureCount, gDB2, trainQuery, gParser,
						baseName + "2", minSupt);
		PostingFeaturesMultiClass[] upperLevelFeatures = new PostingFeaturesMultiClass[1];
		upperLevelFeatures[0] = secondFeatures;

		// 3. Build the Cindex_bottomup
		SupSearch_CIndexBottomUpBuilder builder = new SupSearch_CIndexBottomUpBuilder();
		builder.buildCIndexBottomUp(realDB, firstFeatures.getFeatures(),
				upperLevelFeatures, baseName, gParser, false);

		return firstFeatures.getFeatures().getfeatureNum();
	}

	public static void runIndexBottomUp(IGraphDatabase gDB,
			IGraphDatabase query, GraphParser gParser, String baseName,
			boolean lucene_in_mem) {
		// First Load the index
		SupSearch_CIndexBottomUpBuilder builder = new SupSearch_CIndexBottomUpBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadCIndexBottomUp(gDB, baseName
					+ "CIndexBottomUp/", 2, gParser, lucene_in_mem);
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

	public static void buildIndex(IGraphDatabase trainDB,
			IGraphDatabase trainQuery, IGraphDatabase realDB,
			GraphParser gParser, String base, double minSupt, int patternCount)
			throws IOException {
		String baseName = base + "CIndex/";
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		// 1. Mine Indexing Features
		PostingFeaturesMultiClass indexingFeatures = CIndexFeatureMiner
				.mineFeatures(patternCount, trainDB, trainQuery, gParser,
						baseName + "1", minSupt);
		// 3. Build the Cindex_bottomup
		SupSearch_CIndexFlatBuilder builder = new SupSearch_CIndexFlatBuilder();
		builder.buildCIndexFlat(realDB, indexingFeatures.getFeatures(),
				baseName, gParser, false);
	}

	public static void runIndex(IGraphDatabase gDB, IGraphDatabase query,
			GraphParser gParser, String baseName, boolean lucene_in_mem) {
		// First Load the index
		SupSearch_CIndexFlatBuilder builder = new SupSearch_CIndexFlatBuilder();
		ISearcher searcher = null;
		try {
			searcher = builder.loadCIndexFlat(gDB, baseName + "CIndex/",
					gParser, lucene_in_mem);
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

	public static void memoryConsumptionFlat(IGraphDatabase realDB,
			GraphParser gParser, String base, boolean lucene_in_mem)
			throws NumberFormatException, IOException {
		SupSearch_CIndexFlatBuilder builder = new SupSearch_CIndexFlatBuilder();
		for (int i = 0; i < 3; i++) {
			MemoryConsumptionCal.runGC();
			double start = MemoryConsumptionCal.usedMemoryinMB();
			@SuppressWarnings("unused")
			ISearcher searcher = builder.loadCIndexFlat(realDB, base
					+ "CIndex/", gParser, lucene_in_mem);
			MemoryConsumptionCal.runGC();
			double end = MemoryConsumptionCal.usedMemoryinMB();
			System.out.print((end - start));
			System.out.print(",");
			searcher = null;
		}
		System.out.println();
	}
}
