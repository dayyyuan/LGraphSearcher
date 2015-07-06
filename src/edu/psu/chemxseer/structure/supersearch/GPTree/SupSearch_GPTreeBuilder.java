package edu.psu.chemxseer.structure.supersearch.GPTree;

import java.io.IOException;
import java.text.ParseException;

import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerMultiField;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.PrefixIndexSearcher;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.PrefixIndexSearcherConstructor;

/**
 * Builder for the GPTree
 * 
 * @author dayuyuan
 * 
 */
public class SupSearch_GPTreeBuilder {

	/**
	 * Given all the mined features, construct a SupSearch_GPTree
	 * 
	 * @param filteringFeaturesRaw
	 *            : the significant features for indexing & filtering
	 * @param filteringPrefixFeatures
	 *            : the prefix features for the significant features
	 * @param prefixFeatures
	 *            : the prefix features for iso morphism test saving
	 * @param gDB
	 *            : graph database
	 * @param baseName
	 *            : the name for the index to be stored.
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public SupSearch_GPTree buildIndex(
			NoPostingFeatures<IOneFeature> significantFeatures,
			NoPostingFeatures<IOneFeature> significantFeaturesPrefix,
			NoPostingFeatures<IOneFeature> prefixFeatures, IGraphDatabase gDB,
			String baseName, boolean lucene_in_mem) throws ParseException,
			IOException {
		System.out.println("Build the GPTree Index");
		// 1. First Build the CRGraph Filtering Searcher
		long startTime = System.currentTimeMillis();
		PrefixIndexSearcher prefixSignificant = PrefixIndexSearcherConstructor
				.construct(significantFeaturesPrefix);
		System.out.println("(1) Time for Constructing the CRGraph Prefix: "
				+ (System.currentTimeMillis() - startTime));
		PrefixIndexSearcherConstructor.saveSearcher(prefixSignificant,
				baseName, SupSearch_GPTree.getCRPrefixName());

		startTime = System.currentTimeMillis();
		NoPostingFeatures_Ext<IOneFeature> significant = new NoPostingFeatures_Ext<IOneFeature>(
				significantFeatures);
		System.out
				.println("(2) Time for Mine containment relationships among CRGraph ndoes: "
						+ (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		significant.mineSubSuperRelation();
		CRGraphSearcher crGraph = CRGraphSearcherConstructor.construct(
				significant, prefixSignificant);
		System.out.println("(3) Time for Constructing the CRGraph Index: "
				+ (System.currentTimeMillis() - startTime));
		CRGraphSearcherConstructor.saveSearcher(crGraph, baseName,
				SupSearch_GPTree.getCRGraphName());
		// 2. Build the Prefix Searcher
		startTime = System.currentTimeMillis();
		PrefixIndexSearcher prefixSearcher = PrefixIndexSearcherConstructor
				.construct(prefixFeatures);
		System.out.println("(4) Time for Construcitng the Prefix Index: "
				+ (System.currentTimeMillis() - startTime));
		PrefixIndexSearcherConstructor.saveSearcher(prefixSearcher, baseName,
				SupSearch_GPTree.getPrefixIndex());

		// 3. Build the Posting Fetcher
		startTime = System.currentTimeMillis();
		PostingBuilderLuceneVectorizerMultiField vectorizer = new PostingBuilderLuceneVectorizerMultiField(
				crGraph, prefixSearcher);

		PostingBuilderLucene lucene = new PostingBuilderLucene(vectorizer);
		lucene.buildLuceneIndex(baseName + SupSearch_GPTree.getLucene(),
				crGraph.getFeatureCount(), gDB, null);
		System.out.println("(5) Time for Constructing the Lucene: "
				+ (System.currentTimeMillis() - startTime));

		PostingFetcherLucene postingFetcher = new PostingFetcherLucene(baseName
				+ SupSearch_GPTree.getLucene(), gDB.getTotalNum(),
				MyFactory.getDFSCoder(), lucene_in_mem);
		// 4. Build the Verifier
		VerifierISOPrefix verifier = new VerifierISOPrefix(prefixSearcher,
				false);

		return new SupSearch_GPTree(crGraph, postingFetcher, verifier,
				new GPTreeSearcher(prefixSearcher));
	}

	/**
	 * Given the GraphDatabase(only the size of the graph database matters) and
	 * the index base name load and construct a SupSearcher_GPTree
	 * 
	 * @param gDB
	 * @param baseName
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public SupSearch_GPTree loadIndex(IGraphDatabase gDB, String baseName,
			boolean lucene_in_mem) throws NumberFormatException, IOException {
		// 1. First Load the Prefix Filtering Searcher
		PrefixIndexSearcher prefixFilteringSearcher = PrefixIndexSearcherConstructor
				.loadSearcher(baseName, SupSearch_GPTree.getCRPrefixName(),
						null);
		// 2. Second Load the CRGraph
		CRGraphSearcher crGraph = CRGraphSearcherConstructor.loadSearcher(
				prefixFilteringSearcher, baseName,
				SupSearch_GPTree.getCRGraphName());
		// 3. Load the Prefix Sharing Searcher
		PrefixIndexSearcher prefixSearcher = PrefixIndexSearcherConstructor
				.loadSearcher(baseName, SupSearch_GPTree.getPrefixIndex(), null);
		// 4. Load the Posting Fetcher
		PostingFetcherLucene postingFetcher = new PostingFetcherLucene(baseName
				+ SupSearch_GPTree.getLucene(), gDB.getTotalNum(),
				MyFactory.getDFSCoder(), lucene_in_mem);
		// 5.
		VerifierISOPrefix verifier = new VerifierISOPrefix(prefixSearcher,
				false);

		return new SupSearch_GPTree(crGraph, postingFetcher, verifier,
				new GPTreeSearcher(prefixSearcher));
	}
}
