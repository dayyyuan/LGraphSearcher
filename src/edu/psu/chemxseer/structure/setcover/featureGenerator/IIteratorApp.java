package edu.psu.chemxseer.structure.setcover.featureGenerator;

import java.util.Iterator;

/**
 * Common interface of iterator of items of a set The iterator returns results:
 * results[0] = qID, results[1] = gID;
 * 
 * @author dayuyuan
 * 
 */
public interface IIteratorApp extends Iterator<int[]> {

	public int size();
}
