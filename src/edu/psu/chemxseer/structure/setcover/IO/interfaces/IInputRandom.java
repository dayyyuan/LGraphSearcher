package edu.psu.chemxseer.structure.setcover.IO.interfaces;

import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;

/**
 * An input support random access of arbitrary set given setID
 * 
 * @author dayuyuan
 * 
 */
public interface IInputRandom extends IInputSequential {
	/**
	 * Return the set with ID = setID May return null if the set is already
	 * deleted or the setID is out of boundary
	 * 
	 * @param setID
	 * @return
	 */
	public ISet getSet(int setID);

	/**
	 * Return the total number of sets from the input
	 * 
	 * @return
	 */
	public int getSetCount();

	/**
	 * Delete the "set" from the input [A lazy delete may be applied]
	 * 
	 * @param setID
	 */
	public void delete(int setID);

	/**
	 * Swap the "set" to score set, with score "0" The score need to setup
	 * latter
	 * 
	 * @param set
	 * @return
	 */
	public SetwithScore swapToScoreSet(ISet set);
}
