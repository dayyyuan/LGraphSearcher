package edu.psu.chemxseer.structure.postings.Interface;

import java.util.List;

import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderMem;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;

/**
 * Interface for fetching postings: Given several fIDs, do operation of the
 * postings of those pIDs return GraphFetcher
 * 
 * @author dayuyuan
 * 
 */
public interface IPostingFetcher extends IPostingFetcher0 {
	/**
	 * Given the featureID, retrieve the postings of this feature
	 * TimeComponent[0] = posting retrieval time
	 * 
	 * @param featureID
	 * @param TimeComponent
	 *            [0]
	 * @return
	 */
	public IGraphFetcher getPosting(int featureID, long[] TimeComponent);

	/**
	 * Given the featureString, retrieve the posting of this featureString
	 * TimeComponent[0] = posting retrieval time
	 * 
	 * @param featureString
	 * @param TimeComponent
	 * @return
	 */
	public IGraphFetcher getPosting(String featureString, long[] TimeComponent);

	/**
	 * Given the featureIDs, retrieve the Union of those features postings
	 * TimeComponent[0] = posting retrieval time
	 * 
	 * @param featureIDs
	 * @param TimeComponent
	 *            [0]
	 * @return
	 */
	public IGraphFetcher getUnion(List<Integer> featureIDs, long[] TimeComponent);

	/**
	 * Given the featureIDs, retrieve the Join of those features postings;
	 * TimeComponent[0] = posting retrieval time
	 * 
	 * @param featureIDs
	 * @param TimeComponent
	 *            [0]
	 * @return
	 */
	public IGraphFetcher getJoin(List<Integer> featureIDs, long[] TimeComponent);

	/**
	 * TimeComponent[0] = posting retrieval time
	 * 
	 * @param featureStrings
	 * @param TimeComponent
	 * @return
	 */
	public IGraphFetcher getJoin(String[] featureStrings, long[] TimeComponent);

	/**
	 * Given the featureIDs, retrieve the compliment of the Union of those
	 * features Postings
	 * 
	 * @param featureIDs
	 * @param TimeComponent
	 * @return
	 */
	public IGraphFetcher getComplement(List<Integer> featureIDs,
			long[] TimeComponent);

	/**
	 * Load all the postings into memory for fast access
	 * 
	 * @param indexSearcher
	 * @return
	 */
	public PostingBuilderMem loadPostingIntoMemory(IIndex0 indexSearcher);
}
