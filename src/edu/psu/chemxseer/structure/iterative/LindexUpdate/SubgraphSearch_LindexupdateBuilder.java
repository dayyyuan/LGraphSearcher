package edu.psu.chemxseer.structure.iterative.LindexUpdate;

import java.io.File;
import java.io.IOException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;

/**
 * The builder for the subgraphsearch_lindexupdate class
 * 
 * @author dayuyuan
 * 
 */
public class SubgraphSearch_LindexupdateBuilder {

	/**
	 * Save the Index:
	 * 
	 * @param index
	 * @param indexBaseName
	 * @throws IOException
	 */
	public void saveIndex(SubgraphSearch_LindexUpdate index,
			String indexBaseName) throws IOException {
		File dir = new File(indexBaseName);
		if (!dir.exists())
			dir.mkdirs();
		// 1. First Save the Major Index
		LindexUpdateConstructor.saveSearcher(index.indexSearcher,
				indexBaseName, SubgraphSearch_LindexUpdate.getIndexName());
		// 2. The Lucene is exactly the same as before [No need to duplicate]
		// 3. The InsertedPosting is different, need to save
		index.insertedPosting.savePosting(indexBaseName
				+ SubgraphSearch_LindexUpdate.getInMemPostingName());
	}

	/**
	 * Load the SubgraphSearch_LindexUpdate from the disk
	 * 
	 * @param gDB
	 * @param baseName
	 * @param gSerializer
	 * @param lucenePath
	 * @return
	 * @throws IOException
	 */
	public SubgraphSearch_LindexUpdate loadIndex(GraphDatabase_OnDisk gDB,
			String baseName, GraphParser gSerializer, String lucenePath,
			boolean lucene_in_mem) throws IOException {
		LindexUpdateSearcher indexSearcher = LindexUpdateConstructor
				.loadSearcher(baseName,
						SubgraphSearch_LindexUpdate.getIndexName());
		PostingFetcherLucene lucene = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		PostingFetcherMem memPosting = new PostingFetcherMem(gDB, baseName
				+ SubgraphSearch_LindexUpdate.getInMemPostingName());
		return new SubgraphSearch_LindexUpdate(indexSearcher, lucene,
				memPosting, new VerifierISO());
	}
}
