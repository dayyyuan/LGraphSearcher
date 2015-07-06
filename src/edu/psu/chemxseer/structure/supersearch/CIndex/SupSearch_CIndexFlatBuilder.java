package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.io.File;
import java.io.IOException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerNormal;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * For CIndex Flat, the indexing features are selected with the (1) Weighted
 * version of the max-coverage algorithm (2) Normal verison of the max-coverage
 * algorithm introduced in the setcover toolkits.
 * 
 * @author dayuyuan
 * 
 */
public class SupSearch_CIndexFlatBuilder {
	/**
	 * Given the SelectedFetures & Graphdatabase, build the SupSearch_CIndexFlat
	 * (bottom-level)
	 * 
	 * @param gDB
	 * @param selectedFeatures
	 *            : the selected features
	 * @return
	 * @throws IOException
	 */
	public SupSearch_CIndexFlat buildCIndexFlat(IGraphDatabase gDB,
			NoPostingFeatures<IOneFeature> selectedFeatures, String baseName,
			GraphParser gSerializer, boolean lucene_in_mem) throws IOException {
		System.out.println("Build CIndexFlat");
		// Make Sure that the BaseName Folder is created
		File tempFile = new File(baseName);
		if (!tempFile.exists())
			tempFile.mkdirs();
		long startTime = System.currentTimeMillis();
		CIndexFlat searcher = CIndexFlatConstructor.construct(selectedFeatures);
		System.out.println("(1) Time for cIndex flat Construction:"
				+ (System.currentTimeMillis() - startTime));
		CIndexFlatConstructor.saveSearcher(searcher, baseName,
				SupSearch_CIndexFlat.getIndexName());

		startTime = System.currentTimeMillis();
		String lucenePath = baseName + SupSearch_CIndexFlat.getLuceneName();
		PostingBuilderLucene postingBuilder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer, searcher));
		postingBuilder.buildLuceneIndex(lucenePath, searcher.getFeatureCount(),
				gDB, null);
		System.out.println("(2) Time for cIndex flat Lucene Construction:"
				+ (System.currentTimeMillis() - startTime));
		PostingFetcherLucene posting = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		return new SupSearch_CIndexFlat(searcher, posting, new VerifierISO());
	}

	/**
	 * Load the cIndex from the Disk
	 * 
	 * @param gDB
	 * @param baseName
	 * @param gSerializer
	 * @return
	 * @throws IOException
	 */
	public SupSearch_CIndexFlat loadCIndexFlat(IGraphDatabase gDB,
			String baseName, GraphParser gSerializer, boolean lucene_in_mem)
			throws IOException {
		CIndexFlat searcher = CIndexFlatConstructor.loadSearcher(baseName,
				SupSearch_CIndexFlat.getIndexName());
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SupSearch_CIndexFlat.getLuceneName(), gDB.getTotalNum(),
				gSerializer, lucene_in_mem);
		return new SupSearch_CIndexFlat(searcher, posting, new VerifierISO());
	}

	/**
	 * Build an Upper level Index on the top of the bottomIndex
	 * 
	 * @param bottomIndex
	 * @param selectedFeatures
	 * @param baseName
	 * @return
	 * @throws IOException
	 */
	protected SupSearch_CIndexFlat buildCIndexFlat(
			SupSearch_CIndexFlat bottomIndex,
			PostingFeaturesMultiClass selectedFeatures, String baseName)
			throws IOException {
		System.out.println("Build an upper level cIndexFlat");
		// 0. Make Sure that the BaseName exists:
		File tempFile = new File(baseName);
		if (!tempFile.exists())
			tempFile.mkdirs();

		// 1. Build the Searcher
		long startTime = System.currentTimeMillis();
		NoPostingFeatures<IOneFeature> noPostingFeatures = selectedFeatures
				.getFeatures();
		CIndexFlat searcher = CIndexFlatConstructor
				.construct(noPostingFeatures);
		System.out.println("(1) Time for CIndexFlat Construction: "
				+ (System.currentTimeMillis() - startTime));
		CIndexFlatConstructor.saveSearcher(searcher, baseName,
				SupSearch_CIndexFlat.getIndexName());

		// 2. Build the PostingSearcher
		startTime = System.currentTimeMillis();
		GraphDatabase_InMem newGDB = new GraphDatabase_InMem(
				bottomIndex.getIndexFeatures(), MyFactory.getDFSCoder());
		PostingBuilderMem postBuild = new PostingBuilderMem();

		for (int i = 0; i < noPostingFeatures.getfeatureNum(); i++) {
			postBuild
					.insertOnePosting(i, selectedFeatures.getFullPosting(i, 0));
		}
		System.out
				.println("(2) Time for CIndexFlat in-memory posting construction:"
						+ (System.currentTimeMillis() - startTime));
		postBuild.savePosting(baseName + SupSearch_CIndexFlat.getLuceneName());
		IPostingFetcher fetcher = new PostingFetcherMem(newGDB, postBuild);
		// 3. Return
		return new SupSearch_CIndexFlat(searcher, fetcher, new VerifierISO());
	}

	/**
	 * Load an Upper level Index
	 * 
	 * @param gDB
	 * @param baseName
	 * @return
	 * @throws IOException
	 */
	protected SupSearch_CIndexFlat loadCIndexFlat(
			SupSearch_CIndexFlat lowerLevelIndex, String baseName)
			throws IOException {
		CIndexFlat searcher = CIndexFlatConstructor.loadSearcher(baseName,
				SupSearch_CIndexFlat.getIndexName());
		IGraphDatabase gDB = new GraphDatabase_InMem(
				lowerLevelIndex.getIndexFeatures(), MyFactory.getDFSCoder());
		PostingFetcherMem posting = new PostingFetcherMem(gDB, baseName
				+ SupSearch_CIndexFlat.getLuceneName());
		return new SupSearch_CIndexFlat(searcher, posting, new VerifierISO());
	}

}
