package edu.psu.chemxseer.structure.setcover.update;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexConstructor;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimple;
import edu.psu.chemxseer.structure.supersearch.LWFull.LindexSearcher_Ext;

public class SubSearch_LindexSimpleUpdatableBuilder {
	/**
	 * Build a SubgraphSearch_LindexUpdatable with queries
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
	public SubSearch_LindexSimpleUpdatable buildIndexSub(
			NoPostingFeatures_Ext<IOneFeature> features, IGraphDatabase gDB,
			IGraphDatabase qDB, String baseName, PostingFeatures posFeatures)
			throws IOException, ParseException {
		// 0 step: features are all selected
		// 1st step: build the searcher
		long start = System.currentTimeMillis();
		features.mineSubSuperRelation();
		long time1 = System.currentTimeMillis();
		System.out.println("1. Mine super-sub graph relationships: "
				+ (time1 - start));
		LindexSearcher in_memoryIndex = LindexConstructor.construct(features);
		long time2 = System.currentTimeMillis();
		System.out.println("2. Building Lindex: " + (time2 - time1));
		LindexConstructor.saveSearcher(in_memoryIndex, baseName,
				SubSearch_LindexSimple.getIndexName());
		// 2rd step: build postings
		Map<String, Integer> selectedFeatures = new HashMap<String, Integer>();
		for (IOneFeature aFeature : features)
			selectedFeatures
					.put(aFeature.getDFSCode(), aFeature.getFeatureId());
		PostingBuilderMem posBuilder = PostingBuilderMem.buildPosting(baseName
				+ "posBuilder", posFeatures, selectedFeatures);
		PostingFetcherMem posting = new PostingFetcherMem(gDB, posBuilder);
		return new SubSearch_LindexSimpleUpdatable(in_memoryIndex, posting,
				new VerifierISO(), qDB.getParser(), qDB);
	}

	/**
	 * Mainly build the in-memory posting (populate & store)
	 * 
	 * @param lindex
	 * @param gDB
	 * @param qDB
	 * @param baseName
	 * @return
	 * @throws IOException
	 */
	public SubSearch_LindexSimpleUpdatable buildIndexSub(
			SubSearch_LindexSimple lindex, IGraphDatabase gDB,
			IGraphDatabase qDB, String baseName) throws IOException {
		LindexSearcher in_memoryIndex = lindex.indexSearcher;
		PostingBuilderMem posBuilder = PostingBuilderMem.buildPosting(baseName
				+ "posBuilder", lindex.getPostingFetcher(),
				in_memoryIndex.getFeatureCount());
		PostingFetcherMem posting = new PostingFetcherMem(gDB, posBuilder);
		return new SubSearch_LindexSimpleUpdatable(in_memoryIndex, posting,
				new VerifierISO(), qDB.getParser(), qDB);
	}

	public SubSearch_LindexSimpleUpdatable buildIndexSub(
			SubSearch_LindexSimple lindex, IGraphDatabase gDB, String baseName,
			int qCapacity) {
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		LindexSearcher in_memoryIndex = lindex.indexSearcher;
		PostingBuilderMem posBuilder = PostingBuilderMem.buildPosting(baseName
				+ "posBuilder", lindex.getPostingFetcher(),
				in_memoryIndex.getFeatureCount());
		PostingFetcherMem posting = new PostingFetcherMem(gDB, posBuilder);
		return new SubSearch_LindexSimpleUpdatable(in_memoryIndex, posting,
				new VerifierISO(), MyFactory.getUnCanDFS(), qCapacity);
	}

	/**
	 * Save the updated index
	 * 
	 * @param lindexUpdatable
	 * @param baseName
	 * @throws IOException
	 */
	public void saveUpdatedIndex(
			SubSearch_LindexSimpleUpdatable lindexUpdatable, String baseName)
			throws IOException {
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		LindexConstructor.saveSearcher(lindexUpdatable.indexSearcher, baseName,
				SubSearch_LindexSimple.getIndexName());
		((PostingFetcherMem) lindexUpdatable.getPostingFetcher())
				.savePosting(baseName + "posBuilder");
	}

	/**
	 * Load a SubgraphSearch_LindexUpdatabale with queries
	 * 
	 * @param gDB
	 * @param baseName
	 * @param qDB
	 * @return
	 * @throws IOException
	 */
	public SubSearch_LindexSimpleUpdatable loadIndexSub(IGraphDatabase gDB,
			String baseName, IGraphDatabase qDB) throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_LindexSimple.getIndexName());
		LindexSearcher_Ext in_memoryIndex2 = new LindexSearcher_Ext(
				in_memoryIndex);
		PostingFetcherMem posting = new PostingFetcherMem(gDB, baseName
				+ "posBuilder");
		return new SubSearch_LindexSimpleUpdatable(in_memoryIndex2, posting,
				new VerifierISO(), MyFactory.getUnCanDFS(), qDB);
	}

	public SubSearch_LindexSimpleUpdatable loadIndexSub(IGraphDatabase gDB,
			String baseName, int queryCount) throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_LindexSimple.getIndexName());
		LindexSearcher_Ext in_memoryIndex2 = new LindexSearcher_Ext(
				in_memoryIndex);
		PostingFetcherMem posting = new PostingFetcherMem(gDB, baseName
				+ "posBuilder");
		return new SubSearch_LindexSimpleUpdatable(in_memoryIndex2, posting,
				new VerifierISO(), MyFactory.getUnCanDFS(), queryCount);
	}

	/**
	 * Load a SubgraphSearch_Lindex (no queries, but in-memory postings)
	 * 
	 * @param gDB
	 * @param lindexUpdateFolder
	 * @return
	 * @throws IOException
	 */
	public SubSearch_LindexSimple loadIndexSub(IGraphDatabase gDB,
			String baseName) throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_LindexSimple.getIndexName());
		PostingFetcherMem posting = new PostingFetcherMem(gDB, baseName
				+ "posBuilder");
		return new SubSearch_LindexSimple(in_memoryIndex, posting,
				new VerifierISO());
	}

}
