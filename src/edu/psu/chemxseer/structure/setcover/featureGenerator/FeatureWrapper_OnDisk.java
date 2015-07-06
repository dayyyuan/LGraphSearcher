package edu.psu.chemxseer.structure.setcover.featureGenerator;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * A wrapper of PostingFeaturesMultiClass Different from CoverSet_FeatureWrapper
 * since the postings are stored on-disk, instead of in-memory
 * 
 * @author dayuyuan
 * 
 */
public class FeatureWrapper_OnDisk implements IFeatureWrapper,
		Comparable<FeatureWrapper_OnDisk> {
	private int fID;
	private PostingFeaturesMultiClass postings;
	protected int gain; // Number of Items the Cover-Set contains

	public FeatureWrapper_OnDisk(PostingFeaturesMultiClass features, int ID,
			int gain) {
		this.postings = features;
		this.fID = ID;
		this.gain = gain;
	}

	@Override
	public int[] containedDatabaseGraphs() {
		return this.postings.getPosting(fID)[0];
	}

	@Override
	public int[] containedQueryGraphs() {
		return this.postings.getPosting(fID)[2];
	}

	@Override
	public int[] notContainedDatabaseGraphs(int totalDBGraphs) {
		int[] result = OrderedIntSets.getCompleteSet(
				this.postings.getPosting(fID)[0], totalDBGraphs);
		return result;
	}

	@Override
	public int[] notContainedQueryGraphs(int totalQueryGraphs) {
		int[] result = OrderedIntSets.getCompleteSet(
				this.postings.getPosting(fID)[2], totalQueryGraphs);
		return result;
	}

	@Override
	public int[] getEquavalentDatabaseGraphs() {
		return this.postings.getPosting(fID)[1];
	}

	@Override
	public int[] getEquavalentQueryGraphs() {
		return this.postings.getPosting(fID)[3];
	}

	@Override
	public OneFeatureMultiClass getFeature() {
		return this.postings.getMultiFeatures().getFeature(fID);
	}

	@Override
	public int getFetureID() {
		return this.fID;
	}

	public int[][] getPosting() {
		return this.postings.getPosting(fID);
	}

	@Override
	public int compareTo(FeatureWrapper_OnDisk other) {
		if (this.gain < other.gain)
			return -1;
		else if (this.gain == other.gain)
			return 0;
		else
			return 1;
	}

	public double getGain() {
		return gain;
	}
}

// class SetComparator implements Comparator<CoverSet_FeatureWrapper2>{
// private IFeatureSetConverter converter;
// private int maxGain = 0;
//
// public SetComparator (IFeatureSetConverter converter){
// this.converter = converter;
// }
// public int getMaxGain(){
// return this.maxGain;
// }
//
// @Override
// public int compare(CoverSet_FeatureWrapper2 arg0,
// CoverSet_FeatureWrapper2 arg1) {
// if(arg0.gain == -1)
// arg0.gain = converter.featureToSet_Array(arg0).getItemCount();
// if(arg1.gain == -1)
// arg1.gain = converter.featureToSet_Array(arg1).getItemCount();
// if(arg0.gain < arg1.gain){
// if(arg1.gain > this.maxGain)
// this.maxGain = arg1.gain;
// return -1;
// }
// else if(arg0.gain == arg1.gain)
// return 0;
// else{
// if(arg0.gain > this.maxGain)
// this.maxGain = arg0.gain;
// return 1;
// }
// }
//
// }
