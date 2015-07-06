package edu.psu.chemxseer.structure.newISO.newFastSU;

public class Pattern implements Comparable<Pattern> {
	private int fID;
	private int fSize; // # of nodes this feature contains
	private int visitedEmbCount;
	private float dbInnerSupport;
	// the inner support of the pattern on the database graphs (average)
	private int queryInnerSupport;
	// the inner support of the pattern on the query graph
	private float score;

	public Pattern(int fID, int queryInnerSupport, float dbInnerSupport,
			int fSize) {
		this.fID = fID;
		this.queryInnerSupport = queryInnerSupport;
		this.dbInnerSupport = dbInnerSupport;
		this.visitedEmbCount = 0;
		this.fSize = fSize;
		this.score = queryInnerSupport * dbInnerSupport;
	}

	/**
	 * @return the fID
	 */
	public int getfID() {
		return fID;
	}

	/**
	 * @param visitedEmbCount
	 *            the visitedEmbCount to set
	 */
	public void increaseVisitedEmbCount() {
		this.visitedEmbCount++;
		// recauculate the score
		this.score = (this.queryInnerSupport - this.visitedEmbCount)
				* (this.dbInnerSupport - this.visitedEmbCount);
	}

	@Override
	/**
	 * Compare the Value based on the score (product of dbInnerSupport & queryInnerSupport)
	 * Also, take the fSize into consideration
	 */
	public int compareTo(Pattern other) {
		if (this.equals(other))
			return 0;
		else {
			if (this.score < other.score)
				return -1;
			else if (this.score == other.score) {
				if (this.fSize < other.fSize)
					return -1;
				else if (fSize == other.fSize)
					return 0;
				else
					return 1;
			} else
				return 1;
		}
	}

	public float getScore() {
		return this.score;
	}

}
