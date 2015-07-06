package edu.psu.chemxseer.structure.setcover.IO.interfaces;

/**
 * A stream input, beside supporting sequential read of all sets
 * (IInputSequential) It also support pruning of the branch
 * 
 * @author dayuyuan
 * 
 */
public interface IInputStream extends IInputSequential {
	/**
	 * Prune the branch for stream input only.
	 * 
	 * @return
	 */
	public boolean pruneBranch();

	/**
	 * return true if the data set for input is a full data set, with full range
	 * converter, that is, the number of graphs equals to the range of the
	 * graphs
	 * 
	 * @return
	 */
	public boolean fullRangeConverter();

	/**
	 * Return the time-cost on pattern enuemration.
	 * 
	 * @return
	 */
	@Override
	public long getPatternEnumerationTime();
}
