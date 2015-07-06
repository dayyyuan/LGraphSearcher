package edu.psu.chemxseer.structure.postings.Interface;

public interface IPostingFetcher0 {
	public int[] getPostingID(int featureID);

	/**
	 * Return the size of the database
	 * 
	 * @return
	 */
	public int getDBSize();
}
