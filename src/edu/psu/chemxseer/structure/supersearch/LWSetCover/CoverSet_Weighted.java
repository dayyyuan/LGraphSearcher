package edu.psu.chemxseer.structure.supersearch.LWSetCover;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;

public class CoverSet_Weighted {
	private int[] itemID;
	private int[] itemScore;
	private OneFeatureMultiClass feature;

	public CoverSet_Weighted(int[] itemID, float[] itemScore,
			OneFeatureMultiClass feature) {
		this.itemID = itemID;
		this.itemScore = new int[itemScore.length];
		for (int i = 0; i < itemScore.length; i++)
			this.itemScore[i] = (int) (itemScore[i] * 1000);
		this.feature = feature;
	}

	public CoverSet_Weighted(int[] itemID, OneFeatureMultiClass feature) {
		this.itemID = itemID;
		this.itemScore = new int[itemID.length];
		for (int i = 0; i < itemScore.length; i++)
			itemScore[i] = 0;
		this.feature = feature;
	}

	/**
	 * Assign the score to the item with itemID
	 * 
	 * @param itemID
	 * @param score
	 */
	public boolean assignScore(int ID, float score) {
		if (ID < 0 || ID >= itemID.length)
			return false;
		else {
			itemScore[ID] = (int) (score * 1000);
			return true;
		}
	}

	/**
	 * Get the ID with "ID"
	 * 
	 * @param ID
	 * @return
	 */
	public int getItem(int ID) {
		if (ID < 0 || ID >= itemID.length)
			return -1;
		else
			return itemID[ID];
	}

	/**
	 * Get the Score of the item with "ID"
	 * 
	 * @param ID
	 * @return
	 */
	public int getScore1KTimes(int ID) {
		if (ID < 0 || ID >= itemID.length)
			return -1;
		else
			return itemScore[ID];
	}

	public float getScore(int ID) {
		if (ID < 0 || ID >= itemID.length)
			return -1;
		else
			return ((float) itemScore[ID]) / 1000;
	}

	public int size() {
		return this.itemID.length;
	}

	public OneFeatureMultiClass getFeatures() {
		return this.feature;
	}

}
