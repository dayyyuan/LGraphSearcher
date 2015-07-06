package edu.psu.chemxseer.structure.setcover.featureGenerator;

import java.util.Iterator;
import java.util.List;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.update.ISearch_LindexUpdatable;

public class FeatureConverterSimple implements IFeatureConverter {
	private int gRange;
	private int qRange;
	private AppType aType;

	// The input of gMap and qMap is very strict, such that map[small] <
	// map[big]
	// that is gMap and qMap are strickly ordered
	private int[] gMap; // gMap[current id] = original id

	// private int[] qMap; //qMap[current id] = original id

	/**
	 * Construction of a Simple Feature Converter
	 * 
	 * @param qRange
	 * @param gRange
	 * @param aType
	 */
	public FeatureConverterSimple(int qRange, int gRange, AppType aType) {
		this.gRange = gRange;
		this.qRange = qRange;
		this.aType = aType;
	}

	/**
	 * The input of gMap and qMap is very strict, such that map[small] <
	 * map[big] That is the qMap and gMap are strickly ordered
	 * 
	 * @param qRange
	 * @param gRange
	 * @param qMap
	 * @param gMap
	 * @param aType
	 */
	/*
	 * public FeatureConverterSimple(int qRange, int gRange, int[] qMap, int[]
	 * gMap,AppType aType){ this.gRange = gRange; this.qRange = qRange;
	 * this.qMap = qMap; this.gMap = gMap; this.aType = aType; }
	 */

	public FeatureConverterSimple(int qRange, int gRange, int[] gMap,
			AppType aType) {
		this.qRange = qRange;
		this.gRange = gRange;
		this.gMap = gMap;
		this.aType = aType;
	}

	@Override
	public Set_Pair toSetPair(OneFeatureMultiClass oneFeature, int[][] postings) {
		FeatureWrapperSimple feature = new FeatureWrapperSimple(oneFeature,
				postings);
		return toSetPair(feature);
	}

	@Override
	public Set_Pair[] toSetPairs(PostingFeaturesMultiClass features) {
		Set_Pair[] result = new Set_Pair[features.getFeatures().getfeatureNum()];
		for (int i = 0; i < result.length; i++) {
			result[i] = toSetPair(new FeatureWrapperSimple(features, i));
		}
		return result;
	}

	public Set_Pair[] toSetPairs(List<IFeatureWrapper> features) {
		Set_Pair[] result = new Set_Pair[features.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = toSetPair(features.get(i));
		}
		return result;
	}

	@Override
	public IIteratorApp getIterator(Set_Pair set) {
		if (this.aType == AppType.subSearch)
			return new IteratorSubSearch(set, this.getGRange());
		else if (this.aType == AppType.supSearch)
			return new IteratorSupSearch(set);
		else if (this.aType == AppType.classification)
			return new IteratorClassification(set);
		else
			return null;
	}

	@Override
	public IIteratorApp getIterator(Set_Pair_Index set_Pair_Index) {
		if (this.aType == AppType.subSearch)
			return new IteratorSubSearch(set_Pair_Index, this.getGRange());
		else if (this.aType == AppType.supSearch)
			return new IteratorSupSearch(set_Pair_Index);
		else
			return null;
	}

	@Override
	public Iterator<Item_Group> getItemGroupIterator(Set_Pair set) {
		if (this.aType == AppType.subSearch)
			return new GroupIteratorSubSearch(set, this.getGRange());
		else if (this.aType == AppType.supSearch)
			return new GroupIteratorSupSearch(set, this.getQRange());
		else if (this.aType == AppType.classification)
			throw new UnsupportedOperationException(
					"FeatureConverterSimple: getItemGruopIterator, Classification is not supported");
		else
			return null;
	}

	@Override
	public Iterator<Item_Group> getItemGroupIterator(Set_Pair_Index set_Pair) {
		if (this.aType == AppType.subSearch)
			return new GroupIteratorSubSearch(set_Pair, this.getGRange());
		else if (this.aType == AppType.supSearch)
			return new GroupIteratorSupSearch(set_Pair, this.getQRange());
		else if (this.aType == AppType.classification)
			throw new UnsupportedOperationException(
					"FeatureConverterSimple: getItemGruopIterator, Classification is not supported");
		else
			return null;
	}

	@Override
	public Set_Pair toSetPair(IFeatureWrapper feature) {
		return new Set_Pair(feature, feature.getFetureID() + 1, this);
	}

	@Override
	public Set_Pair_Index toSetPairIndex(FeatureWrapperSimple feature,
			ISearch_LindexUpdatable index) {
		return new Set_Pair_Index(toSetPair(feature), index);
	}

	@Override
	public int getGRange() {
		return this.gRange;
	}

	@Override
	public int getGCount() {
		if (this.gMap == null)
			return gRange;
		else
			return gMap.length;
	}

	@Override
	public int getQRange() {
		return this.qRange;
	}

	@Override
	public int getQCount() {
		/*
		 * if(this.qMap==null) return qRange; else return qMap.length;
		 */
		return qRange;
	}

	@Override
	public int[] getGMapInOrder(int[] input) {
		if (this.gMap == null)
			return input; // return in-order directly
		else {
			int[] result = new int[input.length];
			for (int i = 0; i < result.length; i++)
				result[i] = gMap[input[i]]; // must be sure, that gMap[small] <
											// gMap[big]. will this hold?
			return result;
		}
	}

	/*
	 * public int[] getQMapInOrder(int[] input){ if(this.qMap == null) return
	 * input; // return in-order directly else{ int[] result= new
	 * int[input.length]; for(int i = 0;i < result.length;i++) result[i] =
	 * qMap[input[i]]; return result; } }
	 */

	@Override
	public IFeatureConverter getFullRangeConverter() {
		return new FeatureConverterSimple(this.qRange, this.gRange, this.aType);
	}

	@Override
	public boolean isAppType(AppType inputType) {
		return this.aType == inputType;
	}

	@Override
	public boolean isFullRangeConverter() {
		return gMap == null; // && qMap == null;
	}
}
