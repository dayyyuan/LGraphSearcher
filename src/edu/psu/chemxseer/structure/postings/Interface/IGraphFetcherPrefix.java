package edu.psu.chemxseer.structure.postings.Interface;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;

/**
 * Interface for graph fetcher. Graph fetcher contains a database & a set of
 * gIDs & a Prefix index So that a set of GraphResultPrefix can be returned
 * 
 * @author dayuyuan
 * 
 */
public interface IGraphFetcherPrefix {
	static int batchCount = 1000; // at most 1000 graphs are returned in a batch

	/**
	 * Return a list of Graphs with maximum number "maxNum" (depends on the
	 * implementation) Return "null" if no graphs left to return
	 * TimeComponent[1] = DB Loading Time
	 * 
	 * @param TimeComponent
	 *            [1] = DB Loading Time
	 * @return
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public List<IGraphResultPref> getGraphs(long[] TimeComponent);

	/**
	 * Return the whold list of Graphs from the fetcher
	 * 
	 * @param TimeComponent
	 * @return
	 */
	public List<IGraphResultPref> getAllGraphs(long[] TimeComponent);

	/**
	 * Get the document IDs of the Graph Fetcher
	 * 
	 * @return
	 */
	public int[] getOrderedIDs();

	/**
	 * Join with another graph fetcher
	 * 
	 * @param fetcher
	 * @return
	 */
	public IGraphFetcherPrefix join(IGraphFetcher fetcher);

	/**
	 * Remove the Graph Fetcher
	 * 
	 * @param fetcher
	 * @return
	 */
	public IGraphFetcherPrefix remove(IGraphFetcher fetcher);

	public IGraphFetcherPrefix remove(int[] sortedSet);

	/**
	 * return the number of graphs that will be fetched;
	 * 
	 * @return
	 */
	public int size();

}
