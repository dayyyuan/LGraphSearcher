package edu.psu.chemxseer.structure.subsearch.Interfaces;

import java.util.List;

import de.parmol.graph.Graph;

/**
 * Simpliest Index Searcher
 * 
 * @author dayuyuan
 * 
 */
public interface IIndex0 {
	/**
	 * Given the query graph "query", return all the subgraphs feature IDs
	 * contained in "query"
	 * 
	 * @param query
	 * @return TimeComponent[2], index lookup time
	 */
	public List<Integer> subgraphs(Graph query, long TimeComponent[]);

	/**
	 * Return the index feature IDs of all the index features
	 * 
	 * @return
	 */
	public int[] getAllFeatureIDs();
}
