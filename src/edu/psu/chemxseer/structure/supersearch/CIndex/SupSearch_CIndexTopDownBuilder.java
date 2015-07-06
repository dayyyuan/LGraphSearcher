package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.io.File;
import java.io.IOException;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerNormal;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;

public class SupSearch_CIndexTopDownBuilder {

	/**
	 * Given the Raw Features: mine features & Build the Index The Feature
	 * Mining & Index construction is combined Need to figure out how to
	 * decouple it.
	 * 
	 * @param level
	 *            : how many levels of the hierarchy
	 * @param gDB
	 *            : the underlying graph database
	 * @param gSerializer
	 * @param rawFeatures
	 *            : all candidate features
	 * @param mintrainCount
	 *            : the minimum number of training queries contained
	 * @param baseName
	 * @return
	 * @throws IOException
	 */
	public SupSearch_CIndexTopDown buildCIndexTopDown(IGraphDatabase gDB,
			CIndexTreeFeatureNode featureRoot, GraphParser gSerializer,
			String baseName, boolean lucene_in_mem) throws IOException {
		System.out.println("Build CIndex_Top_Down");
		// 0. Make sure the base name is legal
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();

		// 2. Construct the Index Searcher
		long startTime = System.currentTimeMillis();
		CIndexTree searcher = CIndexTreeConstructor.construct(featureRoot);
		System.out.println("(0) # of indexing features: "
				+ searcher.getDistinctFeatureCount());
		System.out.println("(1) Time for Index Construction: "
				+ (System.currentTimeMillis() - startTime));
		CIndexTreeConstructor.saveSearcher(searcher, baseName
				+ SupSearch_CIndexTopDown.getIndexName());
		// 3. Build the Lucene Index:
		startTime = System.currentTimeMillis();
		String lucenePath = baseName + SupSearch_CIndexTopDown.getLuceneName();
		PostingBuilderLucene postingBuilder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer, searcher));
		postingBuilder.buildLuceneIndex(lucenePath, searcher.getNodeCount(),
				gDB, null);
		System.out.println("(2) Time for Lucece construction: "
				+ (System.currentTimeMillis() - startTime));
		PostingFetcherLucene posting = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		// 4. return
		return new SupSearch_CIndexTopDown(searcher, posting, new VerifierISO());
	}

	public SupSearch_CIndexTopDown buildCIndexTopDown(IGraphDatabase gDB,
			CIndexTree searcher, GraphParser gSerializer, String baseName,
			boolean lucene_in_mem) throws IOException {
		System.out.println("Build CIndex_Top_Down");
		// 0. Make sure the base name is legal
		File temp = new File(baseName);
		if (!temp.exists())
			temp.mkdirs();
		// 3. Build the Lucene Index:
		long startTime = System.currentTimeMillis();
		String lucenePath = baseName + SupSearch_CIndexTopDown.getLuceneName();
		PostingBuilderLucene postingBuilder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer, searcher));
		postingBuilder.buildLuceneIndex(lucenePath, searcher.getNodeCount(),
				gDB, null);
		System.out.println("(2) Time for Lucece construction: "
				+ (System.currentTimeMillis() - startTime));
		PostingFetcherLucene posting = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		// 4. return
		return new SupSearch_CIndexTopDown(searcher, posting, new VerifierISO());
	}

	/**
	 * Load index & supSearcher from the Disk
	 * 
	 * @param gDB
	 * @param baseName
	 * @param gSerializer
	 * @return
	 * @throws IOException
	 */
	public SupSearch_CIndexTopDown loadCIndexTopDown(IGraphDatabase gDB,
			String baseName, GraphParser gSerializer, boolean lucene_in_mem)
			throws IOException {
		CIndexTree searcher = CIndexTreeConstructor.loadSearcher(baseName,
				SupSearch_CIndexTopDown.getIndexName());
		PostingFetcherLucene posting = new PostingFetcherLucene(baseName
				+ SupSearch_CIndexTopDown.getLuceneName(), gDB.getTotalNum(),
				gSerializer, lucene_in_mem);
		return new SupSearch_CIndexTopDown(searcher, posting, new VerifierISO());
	}
}
