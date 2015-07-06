package edu.psu.chemxseer.structure.setcover.featureGenerator;

import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.update.ISearch_LindexUpdatable;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;

/**
 * Convert Features into Item Sets (1) Subgraph Search Feature Selection (2)
 * SuperGraph Search Feature Selection (3) Graph Classification Feature
 * Selection This is a mapping between the range and count The count is the
 * actually size (0 - range) number of possible items in the postings The range
 * is the mapped range of the these (0-range) numbers
 * 
 * @author dayuyuan
 * 
 */
public interface IFeatureConverter {
	/**
	 * Given a feature & its posting, generate a Set_Pair The setID is set to be
	 * featureID + 1;
	 * 
	 * @param oneFeature
	 * @param postings
	 * @return
	 */
	public Set_Pair toSetPair(OneFeatureMultiClass oneFeature, int[][] postings);

	/**
	 * Similar to toSetPair(OneFeatureMultiClass oneFeature, int[][] postings)
	 * The setID is assigned as featureID + 1;
	 * 
	 * @param feature
	 * @return
	 */
	public Set_Pair toSetPair(IFeatureWrapper feature);

	/**
	 * Similar to toSetPair, but return toSetPairIndex
	 * 
	 * @param feature
	 * @param index
	 * @return
	 */
	public Set_Pair_Index toSetPairIndex(FeatureWrapperSimple feature,
			ISearch_LindexUpdatable index);

	/**
	 * Given a set of features & their postings, generate an Array of Set_Pairs
	 * Set IDs are set equals to featureID + 1;
	 * 
	 * @param features
	 * @return
	 */
	public Set_Pair[] toSetPairs(PostingFeaturesMultiClass features);

	/**
	 * Return the items iterator
	 * 
	 * @param feature
	 * @return
	 */
	public IIteratorApp getIterator(Set_Pair feature);

	/**
	 * Return the items iterator
	 * 
	 * @param feature
	 * @return
	 */
	public IIteratorApp getIterator(Set_Pair_Index set_Pair_Index);

	/**
	 * Return Item Group iterator
	 * 
	 * @param set_Pair
	 * @return
	 */
	public Iterator<Item_Group> getItemGroupIterator(Set_Pair set_Pair);

	/**
	 * Return Item Group Iterator
	 * 
	 * @param set_Pair
	 * @return
	 */
	public Iterator<Item_Group> getItemGroupIterator(Set_Pair_Index set_Pair);

	/**
	 * Return the total number of graphs
	 * 
	 * @return
	 */
	public int getGRange();

	/**
	 * Return the total number of queries
	 * 
	 * @return
	 */
	public int getQRange();

	/**
	 * Return the total number of possible graphs
	 * 
	 * @return
	 */
	public int getGCount();

	/**
	 * Return the total number of possible queries
	 * 
	 * @return
	 */
	public int getQCount();

	/**
	 * Given the ranged value, return the real mapped value of database graphs
	 * The results are ordered from small to large
	 * 
	 * @param input
	 * @return
	 */
	int[] getGMapInOrder(int[] input);

	/**
	 * Given the ranged value, return the real mapped value of queries The
	 * results are ordered from small to large
	 * 
	 * @param input
	 * @return
	 */
	// int[] getQMapInOrder(int[] input);

	/**
	 * Return the full range converter
	 * 
	 * @return
	 */
	public IFeatureConverter getFullRangeConverter();

	/**
	 * Test whether the appType == input appType
	 * 
	 * @param subsearch
	 * @return
	 */
	public boolean isAppType(AppType subsearch);

	/**
	 * Return true, if the converter is actually a full range converter Return
	 * false otherwise.
	 * 
	 * @return
	 */
	public boolean isFullRangeConverter();

}
