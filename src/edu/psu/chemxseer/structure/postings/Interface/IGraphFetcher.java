package edu.psu.chemxseer.structure.postings.Interface;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;

/**
 * Interface for graph fetcher. Graph fetcher contains a database & a set of
 * gIDs So that a set of GraphResult can be returned
 * 
 * @author dayuyuan
 * 
 */
public interface IGraphFetcher {
	public static int batchCount = 1000; // at most 1000 graphs are returned in
											// a batch

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
	public List<IGraphResult> getGraphs(long[] TimeComponent);

	/**
	 * Return the whole list of Graphs from the fetcher
	 * 
	 * @param TimeComponent
	 * @return
	 */
	public List<IGraphResult> getAllGraphs(long[] TimeComponent);

	/**
	 * Get the document IDs of the Graph Fetcher:
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
	public IGraphFetcher join(IGraphFetcher fetcher);

	/**
	 * Remove the Graph Fetcher
	 * 
	 * @param fetcher
	 * @return
	 */
	public IGraphFetcher remove(IGraphFetcher fetcher);

	/**
	 * Remove the graphs with ID equals to that in gNoVerifiedTrue
	 * 
	 * @param gNoVerifiedTrue
	 * @return
	 */
	public IGraphFetcher remove(int[] gNoVerifiedTrue);

	/**
	 * return the number of graphs that will be fetched;
	 * 
	 * @return
	 */
	public int size();
}
