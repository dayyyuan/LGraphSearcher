package edu.psu.chemxseer.structure.subsearch.Interfaces;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;

/**
 * IIndex0, with incorporation of Prefix embedding recording
 * 
 * @author dayuyuan
 * 
 */
public interface IIndexPrefix extends IIndex0 {
	/**
	 * Given the featureID fID,given the embedding between current "query"
	 * [stored in the searcher] and the feature fID. (1) If the query equals the
	 * internal stored query & all feature are examined, (1.1) Return the
	 * embedding if it exists (1.2) Return null if it does not exists (2) If the
	 * query not equals to the internal stored query, then update the internal
	 * stored query & set all examined to be false. Test the isomorphism, and
	 * return (3) If the query equals, but the all examined is set false, then
	 * test the isomorphism and return.
	 * 
	 * @param fID
	 * @return
	 */
	public FastSUCompleteEmbedding getEmbedding(int fID, Graph query);

	/**
	 * Given the featureID fID, return the whole label representation of this
	 * feature The prefix is represented as the "linked-list" representation.
	 * DFS
	 * 
	 * @param fID
	 * @return
	 */
	public int[][] getTotalLabel(int fID);

	/**
	 * Return the suffix for the graph with gID: only this graph with gID
	 * 
	 * @param gID
	 * @return
	 */
	public int[][] getExtension(int gID);

	/**
	 * Given a graph "g", find the prefix feature ID of this graph g Mostly for
	 * index construction [posting building] return -1 if there is no prefix
	 * feature
	 * 
	 * @param g
	 * @return
	 */
	public int getPrefixID(Graph g);

	/**
	 * Return the prefix ID for the graph with gID
	 * 
	 * @param gID
	 * @return
	 */
	public int getPrefixID(int gID); // Get the Prefix of the graph with id =
										// gID

	/**
	 * Return the prefix score
	 * 
	 * @param id
	 * @return
	 */
	public int getPrefixGain(int id);

}
