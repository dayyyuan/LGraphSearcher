package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.psu.chemxseer.structure.setcover.IO.Input_Mem;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureConverterSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.impl.Greedy_InvertedIndex;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * For Each BinaryTreeFeatureNode: (1) queries: all the queries in under this
 * node
 * 
 * @author dayuyuan
 * 
 */

public class CIndexTreeFeatureNode {
	private IOneFeature theFeature;
	private int[] queries; // Queries in this branch
	private CIndexTreeFeatureNode leftChild; // queries containing theFeature
	private CIndexTreeFeatureNode rightChild; // queries not containing
												// theFeature
	private CIndexTreeLeafNode leafNode;

	/**
	 * Construct a BinaryFeatureTree, return the root of this feature tree
	 * 
	 * @param wholeQueries
	 * @param selector
	 * @return
	 */
	public static CIndexTreeFeatureNode ConstructBinaryFeatureTree(
			int[] wholeQueries, CIndexTreeFeatureSelector selector) {
		List<IOneFeature> notContainedFeatures = new ArrayList<IOneFeature>();
		Set<IOneFeature> ancestorFeatures = new HashSet<IOneFeature>();
		CIndexTreeFeatureNode root = new CIndexTreeFeatureNode(wholeQueries,
				selector, notContainedFeatures, ancestorFeatures);
		return root;

	}

	/**
	 * 
	 * @param queries
	 * @param selector
	 * @param notContainedFeatures
	 *            : all the ancestorFeatures not contained in the queries
	 * @param ancestorFeatures
	 *            : all the ancestorFeatures
	 */
	private CIndexTreeFeatureNode(int[] queries,
			CIndexTreeFeatureSelector selector,
			List<IOneFeature> notContainedFeatures,
			Set<IOneFeature> ancestorFeatures) {
		this.queries = queries;
		// 1. Find and Set the Splitting Feature
		this.theFeature = this.findTheFeature(selector, notContainedFeatures,
				ancestorFeatures);

		if (theFeature == null) {
			IFeatureWrapper[] selectedFeatures = this.findKFeatures(selector,
					notContainedFeatures, ancestorFeatures);
			this.leafNode = this.constructLeafNode(selectedFeatures);
			return;
		}

		// 2. Split the Node
		int[][] splitQueries = selector.splitQueries(this.queries,
				this.theFeature);
		this.queries = null;
		// depth first search: constructing the binaryTree
		// A: left branch first:
		ancestorFeatures.add(theFeature);
		if (ancestorFeatures.size() < selector.getFeatureCount())
			this.leftChild = new CIndexTreeFeatureNode(splitQueries[0],
					selector, notContainedFeatures, ancestorFeatures);

		// B: right branch:
		notContainedFeatures.add(theFeature);
		if (ancestorFeatures.size() < selector.getFeatureCount())
			this.rightChild = new CIndexTreeFeatureNode(splitQueries[1],
					selector, notContainedFeatures, ancestorFeatures);

		// C: recover the ancesotFeatures, and the notContainedFeatures
		ancestorFeatures.remove(theFeature);
		notContainedFeatures.remove(notContainedFeatures.size() - 1);
	}

	private IFeatureWrapper[] findKFeatures(CIndexTreeFeatureSelector selector,
			List<IOneFeature> notContainedFeatures,
			Set<IOneFeature> ancestorFeatures) {
		// 1. First get all the candidate features:
		int[] unFilteredGraphs = selector
				.getUnfilteredGraphs(notContainedFeatures);
		List<IFeatureWrapper> candidateFeatures = selector
				.getUnSelectedFeaturesAndRelabel(ancestorFeatures,
						unFilteredGraphs, this.queries);

		Input_Mem input = Input_Mem.newInstance(candidateFeatures,
				new FeatureConverterSimple(queries.length,
						unFilteredGraphs.length, AppType.supSearch));
		input.reAssignID();

		Greedy_InvertedIndex solver = new Greedy_InvertedIndex(input,
				StatusType.normal, AppType.supSearch);
		IFeatureWrapper[] selectedFeature = solver.runGreedy(Integer.MAX_VALUE,
				queries.length, new float[10]);
		return selectedFeature;
	}

	private CIndexTreeLeafNode constructLeafNode(IFeatureWrapper[] kfeatures) {
		String[] gStrings = new String[kfeatures.length];
		int[] nameConvention = new int[kfeatures.length];
		for (int i = 0; i < gStrings.length; i++) {
			gStrings[i] = kfeatures[i].getFeature().getDFSCode();
			nameConvention[i] = kfeatures[i].getFeature().getFeatureId();
		}
		return new CIndexTreeLeafNode(new CIndexFlat(gStrings), nameConvention);
	}

	/**
	 * find the splitting feature
	 * 
	 * @return
	 */
	private IOneFeature findTheFeature(CIndexTreeFeatureSelector selector,
			List<IOneFeature> notContainedFeatures,
			Set<IOneFeature> ancestorFeatures) {
		if (this.queries.length < selector.getMinQuerySize())
			return null;
		// Greedy Algorithm for Feature Selection
		// 1. Step One: find all the database graphs that has not been filtered
		int[] unFilteredGraphs = selector
				.getUnfilteredGraphs(notContainedFeatures);
		// 2. Step Two: for each feature, calculate the score of that feature,
		// given all the unFilteredGraphs

		int maxFeatureScore = Integer.MIN_VALUE;
		IOneFeature greedyFeature = null;

		for (int i = 0; i < selector.getFeatureCount(); i++) {
			IOneFeature aFeature = selector.getFeature(i);
			if (ancestorFeatures.contains(aFeature))
				continue;
			else {
				int featureScore = 0;
				featureScore += selector.getFeatureScore(aFeature,
						this.queries, unFilteredGraphs);
				if (featureScore > maxFeatureScore) {
					maxFeatureScore = featureScore;
					greedyFeature = aFeature;
				}
			}
		}
		return greedyFeature;
	}

	/**
	 * @return the theFeature
	 */
	public IOneFeature getTheFeature() {
		return theFeature;
	}

	/**
	 * @return the leftChild
	 */
	public CIndexTreeFeatureNode getLeftChild() {
		return leftChild;
	}

	/**
	 * @return the rightChild
	 */
	public CIndexTreeFeatureNode getRightChild() {
		return rightChild;
	}

	/**
	 * @param theFeature
	 *            the theFeature to set
	 */
	public void setTheFeature(IOneFeature theFeature) {
		this.theFeature = theFeature;
	}

	/**
	 * @param leftChild
	 *            the leftChild to set
	 */
	public void setLeftChild(CIndexTreeFeatureNode leftChild) {
		this.leftChild = leftChild;
	}

	/**
	 * @param rightChild
	 *            the rightChild to set
	 */
	public void setRightChild(CIndexTreeFeatureNode rightChild) {
		this.rightChild = rightChild;
	}

	public int getNodeCount() {
		int count = 1;
		if (this.leftChild != null)
			count += this.leftChild.getNodeCount();
		if (this.rightChild != null)
			count += this.rightChild.getNodeCount();
		return count;
	}

	public boolean isLeafFeatures() {
		return this.theFeature == null;
	}

	public CIndexTreeLeafNode getLeafNode() {
		return leafNode;
	}

}
