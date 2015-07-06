package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerMultiField;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.supersearch.CIndex.SupSearch_CIndexFlat;

public class SupSearch_PrefixIndexBuilder {
	/**
	 * Construct a Single Layer of the SupSearch_PrefixIndex: no lowerLevel, no
	 * upperLevel May or may not have upperLevelindex
	 * 
	 * @param features
	 * @param gDB
	 * @param baseName
	 * @return
	 * @throws IOException
	 * @throws LockObtainFailedException
	 * @throws CorruptIndexException
	 */
	public SupSearch_PrefixIndex buildIndex(
			NoPostingFeatures<IOneFeature> features, IGraphDatabase gDB,
			String baseName, boolean lucene_in_mem)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		System.out.println("Build Prefix Index");
		// 1. Given the Features, construct the PrefixIndexSearcher
		long startTime = System.currentTimeMillis();
		PrefixIndexSearcher index = PrefixIndexSearcherConstructor
				.construct(features);
		System.out.println("(1) Time for PrefixIndex Construction: "
				+ (System.currentTimeMillis() - startTime));
		PrefixIndexSearcherConstructor.saveSearcher(index, baseName,
				SupSearch_PrefixIndex.getIndexName());
		// 2. Build the Prefix-index postings
		startTime = System.currentTimeMillis();
		PostingBuilderLuceneVectorizerMultiField postBuilder = new PostingBuilderLuceneVectorizerMultiField(
				index, index);
		PostingBuilderLucene builder = new PostingBuilderLucene(postBuilder);
		String lucenePath = baseName + SupSearch_PrefixIndex.getLucene();
		builder.buildLuceneIndex(lucenePath, index.getFeatureCount(), gDB, null);
		System.out.println("(2) Time for PrefixIndex lucene Construction: "
				+ (System.currentTimeMillis() - startTime));
		// 3. Build the Verifier
		VerifierISOPrefix prefixISO = new VerifierISOPrefix(index, false);
		PostingFetcherLucene postFetcher = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), MyFactory.getDFSCoder(), lucene_in_mem);
		// //TEST the correctness
		// long[] temp = new long[4];
		// for(int i = 0; i< features.getfeatureNum(); i++){
		// int freq = features.getFeature(i).getFrequency();
		// int postFreq = postFetcher.getPosting(i, temp).size();
		// if(freq!=postFreq){
		// System.out.println("lalal");
		// }
		// }
		// //END OF TEST
		return new SupSearch_PrefixIndex(index, postFetcher, prefixISO);
	}

	/**
	 * Load the Supsearch_PrefIndex from the Disk
	 * 
	 * @param gDB
	 * @param baseName
	 * @param upperLevelIndex
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public SupSearch_PrefixIndex loadIndex(IGraphDatabase gDB, String baseName,
			boolean lucene_in_mem) throws NumberFormatException, IOException {
		PrefixIndexSearcher searcher = PrefixIndexSearcherConstructor
				.loadSearcher(baseName, SupSearch_PrefixIndex.getIndexName(),
						null);
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SupSearch_CIndexFlat.getLuceneName(), gDB.getTotalNum(),
				MyFactory.getDFSCoder(), lucene_in_mem);
		VerifierISOPrefix prefixISO = new VerifierISOPrefix(searcher, false);
		return new SupSearch_PrefixIndex(searcher, posting, prefixISO);
	}

	protected SupSearch_PrefixIndex buildIndex(PrefixIndexSearcher index,
			IGraphDatabase gDB, String baseName, boolean lucene_in_mem)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		// 2. Build the Prefix-index postings
		long startTime = System.currentTimeMillis();
		PostingBuilderLuceneVectorizerMultiField postBuilder = new PostingBuilderLuceneVectorizerMultiField(
				index, index);
		PostingBuilderLucene builder = new PostingBuilderLucene(postBuilder);
		String lucenePath = baseName + SupSearch_PrefixIndex.getLucene();
		builder.buildLuceneIndex(lucenePath, index.getFeatureCount(), gDB, null);
		System.out.println("Level 0: Time for Lucene Index Construction: "
				+ (System.currentTimeMillis() - startTime));
		// 3. Build the Verifier
		VerifierISOPrefix prefixISO = new VerifierISOPrefix(index, false);
		PostingFetcherLucene postFetcher = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), MyFactory.getDFSCoder(), lucene_in_mem);
		return new SupSearch_PrefixIndex(index, postFetcher, prefixISO);
	}

	protected SupSearch_PrefixIndex loadIndex(PrefixIndexSearcher searcher,
			IGraphDatabase gDB, String baseName, boolean lucene_in_mem)
			throws NumberFormatException, IOException {
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SupSearch_CIndexFlat.getLuceneName(), gDB.getTotalNum(),
				MyFactory.getDFSCoder(), lucene_in_mem);
		VerifierISOPrefix prefixISO = new VerifierISOPrefix(searcher, false);
		return new SupSearch_PrefixIndex(searcher, posting, prefixISO);
	}

}
