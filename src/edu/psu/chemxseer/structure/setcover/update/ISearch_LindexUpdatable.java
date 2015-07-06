package edu.psu.chemxseer.structure.setcover.update;

import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherMem;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher0;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;

public interface ISearch_LindexUpdatable extends ISearcher {
	/**
	 * Before the feature updating, all latest queries are stored in queriesF.
	 * queryFetcher is empty at this time. Build queryFetcher for later usage
	 * during feature update.
	 */
	public void constructQueryPosting();

	/**
	 * Return the total number of queries (stored)
	 * 
	 * @return
	 */
	public int getQCount();

	/**
	 * Return the total number of database graphs (stored)
	 * 
	 * @return
	 */
	public int getGCount();

	/**
	 * For each query "q", return the number of graphs containing "q". For
	 * subgraph search
	 * 
	 * @return
	 */
	public int[] getQGCount();

	/**
	 * For each database "g", return the number of queries containing "q". For
	 * supergraph search
	 * 
	 * @return
	 */
	public int[] getGQCount();

	/**
	 * For eqch query, return the number of other queries isomorphic to itself.
	 * 
	 * @return
	 */
	public int[] getQEqual();

	/**
	 * Return the set of sampled database graphs that will be used for pattern
	 * enumeration The ID of the database graphs are incrementally ordered
	 * starting from 0. Thus at the same time that the graph is returned, a map
	 * of the current ID of the graph and its old ID is also build and
	 * maintained.
	 * 
	 * @return
	 */
	public IGraphDatabase getGraphDB(int[] gIDs);

	/**
	 * Return the graph database
	 * 
	 * @return
	 */
	public IGraphDatabase getGraphDB();

	/**
	 * Return the set of query graphs that will be used for pattern enumeration
	 * The ID of the queries are incrementally ordered starting from 0. Thus at
	 * the same time that the graph is returned, a map of the current ID of the
	 * graph and its old ID is also build and maintained.
	 * 
	 * @return
	 */
	public IGraphDatabase getQueryDB(int[] qIDs);

	/**
	 * Return the query database
	 * 
	 * @return
	 */
	public IGraphDatabase getQueryDB();

	/**
	 * Return the support of newGraph
	 * 
	 * @param newGraph
	 * @param noVerifiedTrueG
	 *            : ordered
	 * @param noVerifiedFalseG
	 *            : ordered
	 * @param noVerifiedTrueQ
	 *            : ordered
	 * @param noVerifiedFalseQ
	 *            : ordered
	 * @return
	 */
	int[][] getContainingGraphandQuery(Graph newGraph, int[] noVerifiedTrueG,
			int[] noVerifiedFalseG, int[] noVerifiedTrueQ,
			int[] noVerifiedFalseQ);

	/**
	 * Return the IDs of the IGraphResult; result[0] contains results subgraph
	 * or supergraph isomorphic to the query result[1] contains results
	 * equal(isomorphic) to the query
	 * 
	 * @param query
	 * @param input
	 * @return
	 */
	public int[][] getIDs(Graph query, List<IGraphResult> input,
			int[] orderedTrueAnswer);

	/**
	 * Return the results & at the same time record the queries if recordQuery
	 * is set true
	 * 
	 * @param query
	 * @param TimeComponent
	 * @param Number
	 * @param recordQuery
	 * @return
	 */
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number, boolean recordQuery);

	/**
	 * Return the index searcher
	 * 
	 * @return
	 */
	public LindexSearcher getIndexSearcher();

	/**
	 * Return the index update
	 * 
	 * @return
	 */
	public LindexSearcherUpdatable getIndexUpdator();

	/**
	 * Return the graph fetcher
	 * 
	 * @return
	 */
	public IPostingFetcher0 getGraphFetcher();

	/**
	 * Return the query fetcher
	 * 
	 * @return
	 */
	public PostingFetcherMem getQueryFetcher();

	/**
	 * Return queries equals to the term (tID)
	 * 
	 * @param tID
	 * @return
	 */
	public List<Integer> getEqualQ(int tID);

	/**
	 * Do the swap of indexing features
	 * 
	 * @param swapPair
	 * @param termPos
	 */

	public void doSwap(SetwithScore[] swapPair, int termPos);

	/**
	 * Return the set of uncoveredExamedQG for setPair this is used for
	 * construction of the set-pair-index objects
	 * 
	 * @param setPair
	 * @return
	 */
	public int[][] getUnExamedQG(Set_Pair setPair);

	/**
	 * This method is invoked after the update of the index features because of
	 * the update of the features, previous recorded relationships between
	 * queries and index features
	 * 
	 * @return
	 */
	public boolean clearQueryLogs();

	public boolean shrinkQueryWindow(int toSize);

}
