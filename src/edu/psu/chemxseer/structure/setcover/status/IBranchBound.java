package edu.psu.chemxseer.structure.setcover.status;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

/**
 * For know the IBranchBound are used as a separate object. It should be merged
 * to status latter on, and be only accessible with status
 * 
 * @author dayuyuan
 * 
 */
public interface IBranchBound {

	public void updateAfterInsert(ISet newSet, ICoverStatus_Swap status);

	public void initialize(ICoverStatus_Swap status);

	public void updateAfterDelete(ISet oldSet, ICoverStatus_Swap status);

	public boolean isUppBoundSmaller(ISet set, double d);

	public void clear();
}
