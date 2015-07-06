package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerNormal;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerPartition;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene2;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class SubSearch_LindexPlusBuilder {

	public SubSearch_LindexPlus loadIndex(GraphDatabase_OnDisk gDB,
			String baseName, GraphParser gSerializer, boolean lucene_in_mem)
			throws IOException {
		LindexSearcher in_memoryIndex = LindexConstructor.loadSearcher(
				baseName, SubSearch_LindexPlus.getIn_MemoryIndexName());
		PostingFetcherLucene in_memoryPostings = new PostingFetcherLucene2(
				baseName + SubSearch_LindexPlus.getLuceneName(),
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		PostingFetcherLucene on_diskPostings = new PostingFetcherLucene(
				baseName + SubSearch_LindexPlus.getOnDiskLuceneName(),
				gDB.getTotalNum(), gSerializer, lucene_in_mem);

		return new SubSearch_LindexPlus(new LindexSearcherAdv(in_memoryIndex),
				in_memoryPostings, on_diskPostings, new VerifierISO(), baseName);
	}

	public SubSearch_LindexPlus buildIndex(
			NoPostingFeatures_Ext<IOneFeature> features,
			NoPostingFeatures<IOneFeature> onDiskFeatures,
			GraphDatabase_OnDisk gDB, String baseName, GraphParser gSerializer,
			boolean lucene_in_mem) throws IOException, ParseException {
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

		LindexSearcherAdv in_memoryIndex = new LindexSearcherAdv(
				LindexConstructor.construct(features));
		long time3 = System.currentTimeMillis();
		System.out.println("2. Build Lindex in-memory: " + (time3 - time2));

		LindexConstructor.saveSearcher(in_memoryIndex, baseName,
				SubSearch_LindexPlus.getIn_MemoryIndexName());
		time3 = System.currentTimeMillis();
		// 2nd step: build the postings for the in_memoryIndex
		PostingBuilderLucene builder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerPartition(gSerializer,
						in_memoryIndex));
		builder.buildLuceneIndex(
				baseName + SubSearch_LindexPlus.getLuceneName(),
				in_memoryIndex.getFeatureCount(), gDB, null);
		long time4 = System.currentTimeMillis();
		System.out.println("3. Build lucene for Lindex in-memory: "
				+ (time4 - time3));
		// 3rd step: build the on-disk index and its postings
		PostingFetcherLucene on_diskFetcher = this.buildOnDiskIndex(
				in_memoryIndex, onDiskFeatures, baseName, gDB, gSerializer,
				lucene_in_mem);
		// 3rd step: return
		PostingFetcherLucene posting = new PostingFetcherLucene2(baseName
				+ SubSearch_LindexPlus.getLuceneName(), gDB.getTotalNum(),
				gSerializer, lucene_in_mem);
		return new SubSearch_LindexPlus(in_memoryIndex, posting,
				on_diskFetcher, new VerifierISO(), baseName);
	}

	protected PostingFetcherLucene buildOnDiskIndex(LindexSearcher lindex,
			NoPostingFeatures<IOneFeature> features, String baseName,
			GraphDatabase_OnDisk gDB, GraphParser gSerializer,
			boolean lucene_in_mem) throws IOException, ParseException {
		long time1 = System.currentTimeMillis();

		@SuppressWarnings("unchecked")
		List<IOneFeature>[] featureBelongs = new ArrayList[lindex
				.getFeatureCount()];
		// First step: find the maximum subgraph (only one) index terms for each
		// feature in allFeatures
		for (int i = 0; i < features.getfeatureNum(); i++) {
			IOneFeature theFeature = features.getFeature(i);
			if (theFeature.getFeatureGraph().getEdgeCount() == 0)
				continue;
			long[] TimeComponent = new long[4];
			List<Integer> maxSubs = lindex.maxSubgraphs(
					theFeature.getFeatureGraph(), TimeComponent);

			// skip if theFeature is already in the Lindex
			if (maxSubs != null && maxSubs.get(0) == -1)
				continue;
			else if (maxSubs == null || maxSubs.size() == 0) {
				System.out
						.println("It is so Wired in LindexCompleteAdvance: constructOnDisk: no subgraphs");
				return null;
			}
			int maximumSub = lindex.designedSubgraph(maxSubs, TimeComponent);

			if (featureBelongs[maximumSub] == null)
				featureBelongs[maximumSub] = new ArrayList<IOneFeature>();
			featureBelongs[maximumSub].add(theFeature);
		}

		// After above step: each in-memory index term is associated with a set
		// of on-disk features:
		// Next, construct on-disk lindex for each in-memory index term
		HashMap<String, String> featuresIndexString = new HashMap<String, String>();
		int in_memoryFeatureCount = lindex.getFeatureCount();
		for (int i = 0; i < in_memoryFeatureCount; i++) {
			// A: Get the inputFeatures ready
			if (featureBelongs[i] == null || featureBelongs[i].size() == 0)
				continue;
			IOneFeature[] on_diskFeaturesTemp = new IOneFeature[featureBelongs[i]
					.size()];
			for (int w = 0; w < on_diskFeaturesTemp.length; w++) {
				featureBelongs[i].get(w).setFeatureId(w);
				on_diskFeaturesTemp[w] = featureBelongs[i].get(w); // change of
																	// feature
																	// 'ID'
			}
			NoPostingFeatures<IOneFeature> on_diskFeatures = new NoPostingFeatures<IOneFeature>(
					on_diskFeaturesTemp);
			NoPostingFeatures_Ext<IOneFeature> inputFeatures = new NoPostingFeatures_Ext<IOneFeature>(
					on_diskFeatures);
			try {
				inputFeatures.mineSubSuperRelation(); // may change of feature
														// 'ID' again
			} catch (ParseException e) {
				e.printStackTrace();
			}
			for (int w = 0; w < inputFeatures.getfeatureNum(); w++) {
				featuresIndexString.put(inputFeatures.getFeature(w)
						.getDFSCode(), i + "_" + w);
			}
			// B: Start Building OnDisk Lindex & save them
			LindexSearcher cons = LindexConstructor.constructOnDisk(
					inputFeatures, lindex, i);
			LindexConstructor.saveSearcher(cons, baseName,
					SubSearch_LindexPlus.getOnDiskIndexName(i));
		}

		long time2 = System.currentTimeMillis();
		System.out.println("4. Build Lindexes on-disk: " + (time2 - time1));
		// C: build lucene index
		NoPostingFeatures_Ext<IOneFeature> luceneFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				features);
		HashMap<Integer, String> gHash = new HashMap<Integer, String>();
		// features.clearSubSuperRelation();
		luceneFeatures.mineSubSuperRelation(); // involve change of feature ID
		// Because the minSubSuperRelation involves sort of all the featues,
		// thus changes the feature id
		for (int i = 0; i < luceneFeatures.getfeatureNum(); i++) {
			String indexString = featuresIndexString.get(luceneFeatures
					.getFeature(i).getDFSCode());
			gHash.put(i, indexString);
			luceneFeatures.getFeature(i).setFeatureId(i); // change of feature
															// ID
		}
		LindexSearcher consForLuceneLindex = LindexConstructor
				.construct(luceneFeatures);

		PostingBuilderLucene postBuilder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer,
						consForLuceneLindex));
		postBuilder.buildLuceneIndex(
				baseName + SubSearch_LindexPlus.getOnDiskLuceneName(),
				consForLuceneLindex.getFeatureCount(), gDB, gHash);
		long time3 = System.currentTimeMillis();
		System.out.println("5. Build lucene for Lindexes on-disk: "
				+ (time3 - time2));

		return new PostingFetcherLucene(baseName
				+ SubSearch_LindexPlus.getOnDiskLuceneName(),
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
	}
}
