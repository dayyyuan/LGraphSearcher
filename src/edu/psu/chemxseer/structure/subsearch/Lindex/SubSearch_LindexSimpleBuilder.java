package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerNormal;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class SubSearch_LindexSimpleBuilder {
	/**
	 * 
	 * @param features
	 * @param gDB
	 * @param baseName
	 * @param gSerializer
	 * @return
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws ParseException
	 */
	public SubSearch_LindexSimple buildIndex(
			NoPostingFeatures_Ext<IOneFeature> features, IGraphDatabase gDB,
			String baseName, GraphParser gSerializer)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, ParseException {
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
				SubSearch_Lindex.getIndexName());

		// 2nd step: build the postings for the in_memoryIndex
		time2 = System.currentTimeMillis();
		PostingBuilderLucene builder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer,
						in_memoryIndex));
		builder.buildLuceneIndex(baseName + SubSearch_Lindex.getLuceneName(),
				in_memoryIndex.getFeatureCount(), gDB, null);
		long time3 = System.currentTimeMillis();
		System.out
				.println("3. Buildling Lucene for Lindex: " + (time3 - time2));
		// 3rd step: return
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SubSearch_Lindex.getLuceneName(), gDB.getTotalNum(),
				gSerializer, false);
		return new SubSearch_LindexSimple(in_memoryIndex, posting,
				new VerifierISO());
	}

	public SubSearch_LindexSimple loadIndex(IGraphDatabase gDB,
			String baseName, GraphParser gParser, boolean lucene_in_mem)
			throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_Lindex.getIndexName());
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SubSearch_Lindex.getLuceneName(), gDB.getTotalNum(), gParser,
				lucene_in_mem);
		return new SubSearch_LindexSimple(in_memoryIndex, posting,
				new VerifierISO());
	}
}
