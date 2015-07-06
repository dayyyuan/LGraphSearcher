package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerNormal;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class SubSearch_LindexSimplePlusBuilder {

	public SubSearch_LindexSimplePlus loadIndex(GraphDatabase_OnDisk gDB,
			String baseName, GraphParser gSerializer, boolean lucene_in_mem)
			throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_LindexPlus.getIn_MemoryIndexName());
		PostingFetcherLucene in_memoryPostings = new PostingFetcherLucene(
				baseName + SubSearch_LindexPlus.getLuceneName(),
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		PostingFetcherLucene on_diskPostings = new PostingFetcherLucene(
				baseName + SubSearch_LindexPlus.getOnDiskLuceneName(),
				gDB.getTotalNum(), gSerializer, lucene_in_mem);

		return new SubSearch_LindexSimplePlus(in_memoryIndex,
				in_memoryPostings, on_diskPostings, new VerifierISO(), baseName);
	}

	public SubSearch_LindexSimplePlus buildIndex(
			NoPostingFeatures_Ext<IOneFeature> features,
			NoPostingFeatures<IOneFeature> onDiskFeatures,
			GraphDatabase_OnDisk gDB, String baseName, GraphParser gSerializer)
			throws IOException, ParseException {
		File onDiskFolder = new File(baseName,
				SubSearch_LindexPlus.getOnDiskFolderName());
		if (!onDiskFolder.exists())
			onDiskFolder.mkdirs();

		// 0 step: features are all selected
		// 1st step: build the searcher
		long time1 = System.currentTimeMillis();
		features.mineSubSuperRelation();
		long time2 = System.currentTimeMillis();
		System.out
				.println("1. Mine SubSuper Relationships: " + (time2 - time1));

		LindexSearcher in_memoryIndex = LindexConstructor.construct(features);
		long time3 = System.currentTimeMillis();
		System.out.println("2. Build Lindex in-memory: " + (time3 - time2));

		LindexConstructor.saveSearcher(in_memoryIndex, baseName,
				SubSearch_LindexPlus.getIn_MemoryIndexName());
		time3 = System.currentTimeMillis();
		// 2nd step: build the postings for the in_memoryIndex
		PostingBuilderLucene builder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer,
						in_memoryIndex));
		builder.buildLuceneIndex(
				baseName + SubSearch_LindexPlus.getLuceneName(),
				in_memoryIndex.getFeatureCount(), gDB, null);
		long time4 = System.currentTimeMillis();
		System.out.println("3. Build lucene for Lindex in-memory: "
				+ (time4 - time3));
		// 3rd step: build the on-disk index and its postings
		SubSearch_LindexPlusBuilder onDiskBuilder = new SubSearch_LindexPlusBuilder();
		PostingFetcherLucene on_diskFetcher = onDiskBuilder.buildOnDiskIndex(
				in_memoryIndex, onDiskFeatures, baseName, gDB, gSerializer,
				false);
		// 3rd step: return
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SubSearch_LindexPlus.getLuceneName(), gDB.getTotalNum(),
				gSerializer, false);
		return new SubSearch_LindexSimplePlus(in_memoryIndex, posting,
				on_diskFetcher, new VerifierISO(), baseName);
	}
}
