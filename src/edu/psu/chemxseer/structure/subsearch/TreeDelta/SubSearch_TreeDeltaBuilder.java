package edu.psu.chemxseer.structure.subsearch.TreeDelta;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerNormal;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Interface.IGraphs;
import edu.psu.chemxseer.structure.subsearch.Gindex.GindexConstructor;
import edu.psu.chemxseer.structure.subsearch.Gindex.GindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_Gindex;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class SubSearch_TreeDeltaBuilder {
	public SubSearch_TreeDelta buildIndex(SubSearch_Gindex treeIndex,
			PostingFeatures deltaFeaturesPosting, GraphDatabase_OnDisk gDB,
			String baseName, GraphParser gSerializer, boolean lucene_in_mem)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		// 1. Construct TreeDelta index
		NoPostingFeatures<IOneFeature> deltaFeatures = deltaFeaturesPosting
				.getFeatures();
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < deltaFeatures.getfeatureNum(); i++)
			deltaFeatures.getFeature(i).setSelected();
		GindexSearcher deltaIndex = GindexConstructor.construct(deltaFeatures,
				true);
		long time2 = System.currentTimeMillis();
		System.out.println("1. Build TreeDelta index: " + (time2 - time1));
		GindexConstructor.saveSearcher(deltaIndex, baseName,
				SubSearch_TreeDelta.getDeltaIndexName());

		// 2. Build the lucene index for that
		time2 = System.currentTimeMillis();
		String lucenePath = baseName + SubSearch_TreeDelta.getLuceneName();
		PostingBuilderLucene postingBuilder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer,
						deltaIndex));
		postingBuilder.buildLuceneIndex(lucenePath,
				deltaIndex.getFeatureCount(), gDB, null);
		long time3 = System.currentTimeMillis();
		System.out.println("2. Build lucene for Delta: " + (time3 - time2));

		PostingFetcherLucene deltaPosting = new PostingFetcherLucene(
				lucenePath, gDB.getTotalNum(), gSerializer, lucene_in_mem);
		return new SubSearch_TreeDelta(treeIndex.indexSearcher, deltaIndex,
				treeIndex.postingFetcher, deltaPosting, treeIndex.verifier);
	}

	public SubSearch_TreeDelta loadIndex(GraphDatabase_OnDisk gDB,
			SubSearch_Gindex treeIndex, String baseName,
			GraphParser gSerializer, boolean lucene_in_mem) throws IOException {
		GindexSearcher deltaIndex = GindexConstructor.loadSearcher(baseName,
				SubSearch_TreeDelta.getDeltaIndexName(), true);
		PostingFetcherLucene deltaPosting = new PostingFetcherLucene(baseName
				+ SubSearch_TreeDelta.getLuceneName(), gDB.getTotalNum(),
				gSerializer, lucene_in_mem);

		return new SubSearch_TreeDelta(treeIndex.indexSearcher, deltaIndex,
				treeIndex.postingFetcher, deltaPosting, treeIndex.verifier);
	}

	public NoPostingFeatures<IOneFeature> mineDelta(GraphDatabase_OnDisk gDB,
			SubSearch_Gindex treeIndex, IGraphs testQueries, String baseName)
			throws IOException {
		FeatureSelector fSelector = new FeatureSelector(treeIndex, 10, gDB);
		NoPostingFeatures<IOneFeature> features = fSelector
				.mineFeatures(testQueries);

		String featureFileName = baseName
				+ SubSearch_TreeDelta.getDeltaFeature();
		List<IOneFeature> selectedFeatures = features.getSelectedFeatures();
		return new NoPostingFeatures<IOneFeature>(featureFileName,
				selectedFeatures, false);
	}
}
