package edu.psu.chemxseer.structure.supersearch.LWTree;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerSingle;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexConstructor;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_Lindex;

public class SupSearch_LWIndexBuilder {

	/**
	 * (1) Assume that the input features are already mined (2) Compared of the
	 * LindexBuilder, only the 2nd step: building the postings is different
	 * 
	 * @param features
	 * @param gDB
	 * @param baseName
	 * @return
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws ParseException
	 */
	public SupSearch_LWIndex buildIndex(
			NoPostingFeatures_Ext<OneFeatureMultiClass> features,
			int querySize, IGraphDatabase gDB, String baseName,
			boolean lucene_in_mem) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		// 0 step: features are all selected
		// 1st step: build the searcher
		System.out.println("Build the LWIndex");
		long start = System.currentTimeMillis();
		features.mineSubSuperRelation();
		long time1 = System.currentTimeMillis();
		System.out.println("(1) Mine super-sub graph relationships: "
				+ (time1 - start));
		LindexSearcher in_memoryIndexTemp = LindexConstructor
				.construct(NoPostingFeatures_Ext.getGeneralType(features));
		long time2 = System.currentTimeMillis();
		System.out.println("(2) Building Lindex: " + (time2 - time1));
		LindexConstructor.saveSearcher(in_memoryIndexTemp, baseName,
				SupSearch_LWIndex.getIndexName());

		// 2nd step: build the postings for the in_memoryIndex
		time2 = System.currentTimeMillis();
		LWIndexSearcher in_memoryIndex = new LWIndexSearcher(
				in_memoryIndexTemp, features, querySize);
		PostingBuilderLucene builder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerSingle(in_memoryIndex));
		builder.buildLuceneIndex(baseName + SubSearch_Lindex.getLuceneName(),
				in_memoryIndexTemp.getFeatureCount(), gDB, null);
		long time3 = System.currentTimeMillis();
		System.out.println("(3) Buildling Lucene for Lindex: "
				+ (time3 - time2));
		// 3rd step: return
		GraphParser gSerializer = MyFactory.getDFSCoder();
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SubSearch_Lindex.getLuceneName(), gDB.getTotalNum(),
				gSerializer, lucene_in_mem);
		return new SupSearch_LWIndex(in_memoryIndex, posting,
				new VerifierISOPrefix(in_memoryIndex, false));
	}

	/**
	 * Only Support the gSerializer = DFS coder
	 * 
	 * @param gDB
	 * @param baseName
	 * @return
	 * @throws IOException
	 */
	public SupSearch_LWIndex loadIndex(IGraphDatabase gDB, String baseName,
			boolean lucene_in_mem) throws IOException {
		LindexSearcher in_memoryIndexTemp = LindexConstructor.loadSearcher(
				baseName, SubSearch_Lindex.getIndexName());
		LWIndexSearcher in_memoryIndex = new LWIndexSearcher(in_memoryIndexTemp);
		GraphParser gSerializer = MyFactory.getDFSCoder();
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SubSearch_Lindex.getLuceneName(), gDB.getTotalNum(),
				gSerializer, lucene_in_mem);
		return new SupSearch_LWIndex(in_memoryIndex, posting,
				new VerifierISOPrefix(in_memoryIndex, false));
	}
}
