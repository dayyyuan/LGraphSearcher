package edu.psu.chemxseer.structure.setcover.update;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_Prefix;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMemPrefix;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexConstructor;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_Lindex;
import edu.psu.chemxseer.structure.supersearch.LWFull.LindexSearcher_Ext;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_LWFull;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_Lindex;

public class SupSearch_LindexUpdatableBuilder {
	public SupSearch_LWindexUpdatable buildIndexSupPrefix(
			NoPostingFeatures_Ext<IOneFeature> features,
			GraphDatabase_InMem gDB, IGraphDatabase qDB, String baseName,
			PostingFeaturesMultiClass posFeatures) throws IOException,
			ParseException {
		// 0 step: features are all selected
		// 1st step: build the searcher
		System.out.println("Build the LWIndex");
		long start = System.currentTimeMillis();
		features.mineSubSuperRelation();
		long time1 = System.currentTimeMillis();
		System.out.println("(1) Mine super-sub graph relationships: "
				+ (time1 - start));
		LindexSearcher in_memoryIndexTemp = LindexConstructor
				.construct(features);
		long time2 = System.currentTimeMillis();
		System.out.println("(2) Building Lindex: " + (time2 - time1));
		LindexConstructor.saveSearcher(in_memoryIndexTemp, baseName,
				SupSearch_LWFull.getIndexName());
		LWIndexSearcher2 in_memoryIndex = new LWIndexSearcher2(
				in_memoryIndexTemp);
		// 2nd step: build the postings for the in_memoryIndex
		time2 = System.currentTimeMillis();
		Map<String, Integer> selectedFeatures = new HashMap<String, Integer>();
		for (IOneFeature aFeature : features)
			selectedFeatures
					.put(aFeature.getDFSCode(), aFeature.getFeatureId());
		PostingBuilderMem postingBuilder = PostingBuilderMem.buildPosting(
				baseName + "posBuilder", posFeatures, selectedFeatures);
		GraphDatabase_Prefix prefDB = GraphDatabase_Prefix.buildPrefixDB(gDB,
				new PostingFetcherMem(gDB, postingBuilder), in_memoryIndex);
		prefDB.storePrefix(baseName + "prefixDB");
		PostingFetcherMemPrefix postingFetcher = new PostingFetcherMemPrefix(
				prefDB, postingBuilder, in_memoryIndex);
		// 3rd step: return
		return new SupSearch_LWindexUpdatable(in_memoryIndex, postingFetcher,
				new VerifierISOPrefix(in_memoryIndex, false), qDB.getParser(),
				qDB);
	}

	/**
	 * Build a SupergraphSearch_LindexUpdatable with queries
	 * 
	 * @param features
	 * @param gDB
	 * @param qDB
	 * @param baseName
	 * @param posFeatures
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public SupSearch_LindexUpdatable buildIndexSup(
			NoPostingFeatures_Ext<IOneFeature> features, IGraphDatabase gDB,
			IGraphDatabase qDB, String baseName,
			PostingFeaturesMultiClass posFeatures) throws IOException,
			ParseException {
		// 0 step: features are all selected
		// 1st step: build the searcher
		long start = System.currentTimeMillis();
		features.mineSubSuperRelation();
		long time1 = System.currentTimeMillis();
		System.out.println("1. Mine super-sub graph relationships: "
				+ (time1 - start));
		LindexSearcher_Ext in_memoryIndex = new LindexSearcher_Ext(
				LindexConstructor.construct(features));
		long time2 = System.currentTimeMillis();
		System.out.println("2. Building Lindex: " + (time2 - time1));
		LindexConstructor.saveSearcher(in_memoryIndex, baseName,
				SupSearch_Lindex.getIndexName());
		// 2rd step: build postings
		Map<String, Integer> selectedFeatures = new HashMap<String, Integer>();
		for (IOneFeature aFeature : features)
			selectedFeatures
					.put(aFeature.getDFSCode(), aFeature.getFeatureId());

		PostingBuilderMem posBuilder = PostingBuilderMem.buildPosting(baseName
				+ "posBuilder", posFeatures, selectedFeatures);
		PostingFetcherMem posting = new PostingFetcherMem(gDB, posBuilder);
		return new SupSearch_LindexUpdatable(in_memoryIndex, posting,
				new VerifierISO(), qDB.getParser(), qDB);
	}

	public SupSearch_LindexUpdatable buildIndexSup(SupSearch_Lindex lindex,
			IGraphDatabase gDB, IGraphDatabase qDB, String baseName)
			throws IOException {

		LindexSearcher_Ext in_memoryIndex = lindex.indexSearcher;
		PostingBuilderMem posBuilder = PostingBuilderMem.buildPosting(baseName
				+ "posBuilder", lindex.getPostingFetcher(),
				in_memoryIndex.getFeatureCount());
		PostingFetcherMem posting = new PostingFetcherMem(gDB, posBuilder);
		return new SupSearch_LindexUpdatable(in_memoryIndex, posting,
				new VerifierISO(), qDB.getParser(), qDB);
	}

	/**
	 * Load a SupergraphSearch_LindexUpdatabale with queries
	 * 
	 * @param gDB
	 * @param baseName
	 * @param qDB
	 * @return
	 * @throws IOException
	 */
	public SupSearch_LindexUpdatable loadIndexSup(IGraphDatabase gDB,
			String baseName, IGraphDatabase qDB) throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_Lindex.getIndexName());
		LindexSearcher_Ext in_memoryIndex2 = new LindexSearcher_Ext(
				in_memoryIndex);
		PostingFetcherMem posting = new PostingFetcherMem(gDB, baseName
				+ "posBuilder");
		return new SupSearch_LindexUpdatable(in_memoryIndex2, posting,
				new VerifierISO(), MyFactory.getUnCanDFS(), qDB);
	}

	public SupSearch_Lindex loadIndexSup(IGraphDatabase gDB, String baseName,
			int qCount) throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_Lindex.getIndexName());
		LindexSearcher_Ext in_memoryIndex2 = new LindexSearcher_Ext(
				in_memoryIndex);
		PostingFetcherMem posting = new PostingFetcherMem(gDB, baseName
				+ "posBuilder");
		return new SupSearch_LindexUpdatable(in_memoryIndex2, posting,
				new VerifierISO(), MyFactory.getUnCanDFS(), qCount);
	}

	public void saveUpdatedIndex(SupSearch_LindexUpdatable lindexUpdatable,
			String baseName) throws IOException {
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		LindexConstructor.saveSearcher(lindexUpdatable.indexSearcher, baseName,
				SupSearch_Lindex.getIndexName());
		((PostingFetcherMem) lindexUpdatable.getPostingFetcher())
				.savePosting(baseName + "posBuilder");

	}

}
