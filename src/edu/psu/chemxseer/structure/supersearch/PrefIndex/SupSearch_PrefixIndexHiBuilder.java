package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.io.File;
import java.io.IOException;

import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderMem;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISOPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * The strategy of the building of SupSearch_PrefxiIndex [1] Bottom Level: using
 * lucene, for each database graph, find all of its subgraphs, find the prefix
 * index [2] Other Level: using in-memory postings, the postings is build by
 * looking from the features directly the prefix is still need to be looked up.
 * 
 * @author dayuyuan
 * 
 */
public class SupSearch_PrefixIndexHiBuilder {

	/**
	 * Build An Index Searcher: need its upperLevelSearcher To Build a Index
	 * Searcher, we need to know its "upper-level" index, Since we need to find
	 * the prefix for each indexing features of this level. and extend labeling
	 * all the indexing features
	 * 
	 * @param features
	 * @param upperLevelSearcher
	 * @return
	 * @throws IOException
	 */
	private PrefixIndexSearcher buildSearcher(
			NoPostingFeatures<IOneFeature> features, String baseName,
			PrefixIndexSearcher upperLevelSearcher, int level)
			throws IOException {
		// 1. Given the UpperLevelIndex, build the prefixIndexSearcher
		// System.out.println("Build A PrefixIndex Searcher Only");
		long startTime = System.currentTimeMillis();
		PrefixIndexSearcher prefIndexSearcher = null;
		if (upperLevelSearcher == null)
			prefIndexSearcher = PrefixIndexSearcherConstructor
					.construct(features);
		else
			prefIndexSearcher = PrefixIndexSearcherConstructor.construct(
					features, upperLevelSearcher);
		PrefixIndexSearcherConstructor.saveSearcher(prefIndexSearcher,
				baseName, SupSearch_PrefixIndex.getIndexName());
		System.out.println("Level " + level
				+ ": Time for PrefixIndex Searcher Construciton: "
				+ (System.currentTimeMillis() - startTime));
		return prefIndexSearcher;
	}

	/**
	 * Given the searcherName, and its upperLevelSearcher (can be null). Load
	 * the index searcher from the disk
	 * 
	 * @param indexBaseName
	 * @param upperLevelSearcher
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private PrefixIndexSearcher loadSearcher(String indexBaseName,
			PrefixIndexSearcher upperLevelSearcher)
			throws NumberFormatException, IOException {
		PrefixIndexSearcher prefIndexSearcher = PrefixIndexSearcherConstructor
				.loadSearcher(indexBaseName,
						SupSearch_PrefixIndex.getIndexName(),
						upperLevelSearcher);
		return prefIndexSearcher;
	}

	/**
	 * Build the Postings & Verifiers: To build the Postings Fetcher, we need to
	 * get the Database (which is the index searcher of the next level).
	 * 
	 * @param indexFeatures
	 * @param baseName
	 * @param thisSearcher
	 * @param lowerSearcher
	 * @return
	 * @throws IOException
	 */
	private SupSearch_PrefixIndex buildSingleIndex(
			PostingFeatures indexFeatures, String baseName,
			PrefixIndexSearcher thisSearcher,
			PrefixIndexSearcher lowerSearcher,
			SupSearch_PrefixIndex upperIndex, int level) throws IOException {
		// System.out.println("Build not-bottom level PrefixIndex with searcher");
		if (lowerSearcher != null) {
			// 2. Build the In-Memory Prefix-index Postings
			long startTime = System.currentTimeMillis();
			NoPostingFeatures<IOneFeature> noPostingFeature = indexFeatures
					.getFeatures();
			PostingBuilderMem postingBuilder = new PostingBuilderMem();
			for (int i = 0; i < noPostingFeature.getfeatureNum(); i++) {
				int[] support = indexFeatures.getPosting(i);
				postingBuilder.insertOnePosting(i, support);
			}
			postingBuilder.savePosting(baseName
					+ SupSearch_PrefixIndex.getLucene());
			System.out.println("Level " + level
					+ ": Time for Inmemory Posting Construction "
					+ (System.currentTimeMillis() - startTime));
			PostingFetcherMem fetcher = new PostingFetcherMem(lowerSearcher,
					postingBuilder);
			// 3. Build the Verifier
			VerifierISOPrefix verifier = new VerifierISOPrefix(thisSearcher,
					true);
			return new SupSearch_PrefixIndex(thisSearcher, fetcher, verifier,
					upperIndex, lowerSearcher);
		} else {
			System.out
					.println("The lower Searcher can not be not working: return null");
			return null;
		}
	}

	private SupSearch_PrefixIndex loadSingleIndex(String indexBaseName,
			PrefixIndexSearcher thisSearcher,
			PrefixIndexSearcher lowerSearcher, SupSearch_PrefixIndex upperIndex) {
		PostingFetcherMem fetcher = new PostingFetcherMem(lowerSearcher,
				indexBaseName + SupSearch_PrefixIndex.getLucene());
		VerifierISOPrefix verifier = new VerifierISOPrefix(thisSearcher, true);
		return new SupSearch_PrefixIndex(thisSearcher, fetcher, verifier,
				upperIndex, lowerSearcher);

	}

	/**
	 * Given the already mined features (features of different layers),
	 * construct the SupSearch_PrefixIndexHi index (store it) and return it (1)
	 * To build the searcher, we need its upper searcher (2) To build the other
	 * parts of the index (gDB), we need its lower searcher
	 * 
	 * @param features
	 *            : features[0] = bottom level features
	 * @param baseName
	 *            : ground index with iBaseName[0]
	 * @return
	 * @throws IOException
	 */
	public SupSearch_PrefixIndexHi buildIndex(PostingFeatures[] features,
			String baseName, IGraphDatabase gDB, boolean lucene_in_mem)
			throws IOException {
		SupSearch_PrefixIndexHi result = new SupSearch_PrefixIndexHi();

		String[] iBaseName = new String[features.length];
		for (int i = 0; i < iBaseName.length; i++) {
			iBaseName[i] = baseName + (i) + "/";
			File temp = new File(iBaseName[i]);
			if (!temp.exists())
				temp.mkdirs();
		}

		// build the searcher for the upper-most layer index
		int level = features.length - 1;
		PostingFeatures oneFeature = features[level];
		PrefixIndexSearcher thisSearcher = this.buildSearcher(
				oneFeature.getFeatures(), iBaseName[level], null, level);
		SupSearch_PrefixIndex upperIndex = null;

		// build the rest
		for (; level >= 0; --level) {
			// 1. First Step: construct the Index Searcher (this)
			PrefixIndexSearcher lowerSearcher = null;
			SupSearch_PrefixIndex thisIndex = null;
			if (level > 0) {
				lowerSearcher = this.buildSearcher(
						features[level - 1].getFeatures(),
						iBaseName[level - 1], thisSearcher, (level - 1));
				thisIndex = this.buildSingleIndex(features[level],
						iBaseName[level], thisSearcher, lowerSearcher,
						upperIndex, level);
			} // else: fir
			else {
				SupSearch_PrefixIndexBuilder builder = new SupSearch_PrefixIndexBuilder();
				thisIndex = builder.buildIndex(thisSearcher, gDB,
						iBaseName[level], lucene_in_mem);
			}
			// 3. Add into the Hierarchy
			result.addIndexLow(thisIndex);
			// 4. Update
			thisSearcher = lowerSearcher;
			upperIndex = thisIndex;
		}
		return result;
	}

	/**
	 * Load the hierarchy index into memory (1) To load the searcher, we need
	 * its upper searcher (2) To build the other parts of the index (gDB), we
	 * need its lower searcher
	 * 
	 * @param features
	 *            : features[0] = bottom level features
	 * @param baseName
	 *            : ground index with iBaseName[0]
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public SupSearch_PrefixIndexHi loadIndex(String baseName, int levelCount,
			IGraphDatabase gDB, boolean lucene_in_mem)
			throws NumberFormatException, IOException {
		SupSearch_PrefixIndexHi result = new SupSearch_PrefixIndexHi();
		// 1. Initialize the FileNames
		String[] iBaseName = new String[levelCount];
		for (int i = 0; i < levelCount; i++) {
			iBaseName[i] = baseName + i + "/";
		}
		// 2. build the upper-most level
		int level = levelCount - 1;
		PrefixIndexSearcher thisSearcher = this.loadSearcher(iBaseName[level],
				null);
		SupSearch_PrefixIndex upperIndex = null;

		// build the rest
		for (; level >= 0; --level) {
			// 1. First Step: construct the Index Searcher (this)
			PrefixIndexSearcher lowerSearcher = null;
			SupSearch_PrefixIndex thisIndex = null;
			if (level > 0) {
				lowerSearcher = this.loadSearcher(iBaseName[level - 1],
						thisSearcher);
				thisIndex = this.loadSingleIndex(iBaseName[level],
						thisSearcher, lowerSearcher, upperIndex);
			} // else: first level = 0, does not have lower Searcher
			else {
				SupSearch_PrefixIndexBuilder builder = new SupSearch_PrefixIndexBuilder();
				thisIndex = builder.loadIndex(thisSearcher, gDB,
						iBaseName[level], lucene_in_mem);
			}
			// 3. Add into the Hierarchy
			result.addIndexLow(thisIndex);
			// 4. Update
			thisSearcher = lowerSearcher;
			upperIndex = thisIndex;
		}
		return result;

	}
}
