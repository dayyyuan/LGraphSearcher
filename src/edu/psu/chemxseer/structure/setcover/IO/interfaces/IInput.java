package edu.psu.chemxseer.structure.setcover.IO.interfaces;

/**
 * The dummy interface for all the Input: A collection of sets.
 * 
 * @author dayuyuan
 * 
 */
public interface IInput {

	public int getQCount();

	public int getGCount();

	/**
	 * Close the input stream
	 * 
	 * @throws Exception
	 */
	public void closeInputStream() throws Exception;
}
