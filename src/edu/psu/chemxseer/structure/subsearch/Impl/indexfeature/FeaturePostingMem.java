package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.util.HashMap;

import edu.psu.chemxseer.structure.subsearch.Interfaces.IFeaturePosting;

/**
 * The Postings are stored in memory
 * 
 * @author dayuyuan
 * 
 */
public class FeaturePostingMem implements IFeaturePosting {
	private HashMap<Long, int[]> postings;

	public FeaturePostingMem() {
		postings = new HashMap<Long, int[]>();
	}

	@Override
	public int[] getPosting(long postingShift) {
		int[] answer = postings.get(postingShift);
		if (answer == null)
			return new int[0];
		else
			return answer;
	}

	public void insertPosting(long postingShift, int[] posting) {
		this.postings.put(postingShift, posting);
	}
}
