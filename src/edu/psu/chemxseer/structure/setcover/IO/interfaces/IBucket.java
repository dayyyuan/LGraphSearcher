package edu.psu.chemxseer.structure.setcover.IO.interfaces;

import java.io.IOException;

/**
 * A bucket is a special case of the input, It is a partial set of the whole
 * input
 * 
 * @author dayuyuan
 * 
 */
public interface IBucket extends IInput, Iterable<ISet> {

	/**
	 * Append a set at the end of the Bucket
	 * 
	 * @param set
	 * @return
	 */
	public boolean append(ISet set);

	/**
	 * Append the set to the bucket
	 * 
	 * @param theSet
	 */
	public void insertWithOrder(ISet theSet);

	/**
	 * Write the reatable to disk
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException;

}
