package edu.psu.chemxseer.structure.postings.Interface;

import de.parmol.graph.Graph;

/**
 * The graph results interface: Can return the graph and also its index
 * 
 * @author dayuyuan
 * 
 */
public interface IGraphResult extends Comparable<IGraphResult> {
	/**
	 * The Graph
	 * 
	 * @return
	 */
	public Graph getG();

	/**
	 * The real graph ID
	 * 
	 * @return
	 */
	public int getID();

	/**
	 * Internal Used Only
	 * 
	 * @return
	 */
	public int getDocID();

}
