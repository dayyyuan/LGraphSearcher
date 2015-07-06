package edu.psu.chemxseer.structure.subsearch.Interfaces;

import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;

/**
 * The main interface for subgraph search problem: There are Gindex, FGindex,
 * Lindex, Lindex+, QuickSI, TreeDelta algorithms implementing the subgraph
 * search problem. Usually, one subgraph search algorithm contains (1)
 * indexSearcher (2) postingFetcher and (3) verifier. But there are exceptions
 * [Especially for some on-Disk index].
 * 
 * @author dayuyuan
 * 
 */
public interface ISearcher {
	/**
	 * Given the query graph q, search for database graphs containing the query
	 * q The GraphResults is returned in order TimeComponent[0] = posting
	 * fetching TimeComponent[1] = DB graph loading time TimeComponent[2] =
	 * filtering cost (index lookup: maximum subgraph search & minimal
	 * supergraph search) TimeComponent[3] = verification cost (subgraph
	 * isomorphism test in verification step) Number[0] = verified graphs number
	 * Number[1] = True answer size
	 * 
	 * @param q
	 * @param TimeComponent
	 * @param Number
	 */
	public List<IGraphResult> getAnswer(Graph q, long[] TimeComponent,
			int[] Number);

	/**
	 * Return the answer IDs: Result[0] contains the graphs not equals to the
	 * query Result[1] contained the graphs equal to the query
	 * 
	 * @param query
	 * @return
	 */
	int[][] getAnswerIDs(Graph query);
}
