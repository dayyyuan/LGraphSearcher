package edu.psu.chemxseer.structure.postings.Interface;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;

/**
 * Interface for a graph database; The database can be either on-disk (searched
 * via an in-memory index) or stored in-memory
 * 
 * @author dayuyuan
 * 
 */
public interface IGraphDatabase extends Iterable<Graph> {

	public Graph[] loadGraphs(int[] graphIDs, int start, int end);

	public Graph[] loadAllGraphs();

	/**
	 * get graphs from the graph database with ID starting from startNum end
	 * with endNum, including the first one but not the last one
	 * 
	 * @param startNum
	 * @param endNum
	 * @return
	 */
	public Graph[] loadGraphs(int start, int end);

	/**
	 * Given the graph ID, load the Smiles (String) from of this graph from the
	 * graph database file
	 * 
	 * @param id
	 * @return
	 */
	public Graph findGraph(int id);

	/**
	 * Return the graph parser
	 * 
	 * @return
	 */
	public GraphParser getParser();

	/**
	 * Return the graph string
	 * 
	 * @param id
	 * @return
	 */
	public String findGraphString(int id);

	/**
	 * Return the total number of database graphs
	 * 
	 * @return
	 */
	public int getTotalNum();

	public void setGString(int gID, String serialize);

}
