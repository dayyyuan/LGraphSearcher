package edu.psu.chemxseer.structure.supersearch.GPTree;

import java.text.ParseException;
import java.util.BitSet;
import java.util.List;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureComparatorBase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureExt;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.OrderedIntSet;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The Feature Miner for the GPTree
 * 
 * @author dayuyuan
 * 
 */
public class GPFeatureMiner {
	/**
	 * Given the frequent subgraphs, mine the significant features
	 * 
	 * @param frequentSubgraphs
	 * @param ratio
	 *            : 1/0.8 for default value
	 * @return
	 * @throws ParseException
	 */
	public PostingFeatures minSignificantFeatures(
			PostingFeatures frequentSubgraphs, double ratio)
			throws ParseException {
		System.out.println("Select Significant Patterns for GPTree");
		System.out.println("(1) Both Features & Postings are Stored in-memory");
		OrderedIntSet set = new OrderedIntSet();
		// 0. Load all the postings in-memory, for fast union operations
		MemoryConsumptionCal.runGC();
		double memoryStart = MemoryConsumptionCal.usedMemoryinMB();
		frequentSubgraphs.loadPostingIntoMemory();
		MemoryConsumptionCal.runGC();
		double memoryEnd = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(2) Space for Pattern Selection: "
				+ (memoryEnd - memoryStart));
		// 1. Mine the containment relationships between frequent subgraphs
		long startTime = System.currentTimeMillis();
		NoPostingFeatures_Ext<IOneFeature> freqFeaturesExt = new NoPostingFeatures_Ext<IOneFeature>(
				frequentSubgraphs.getFeatures());
		freqFeaturesExt.mineSubSuperRelation();
		freqFeaturesExt.sortFeatures(new FeatureComparatorBase());
		System.out.println("(3.1) Mine Containment Relationships: "
				+ (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		// 2. Mine Significant Subgraphs
		int selectedCount = 0;
		for (int i = freqFeaturesExt.getfeatureNum() - 1; i >= 0; i--) {
			OneFeatureExt oneFeature = freqFeaturesExt.getFeatureExt(i);
			List<OneFeatureExt> children = oneFeature.getChildren();
			if (children == null || children.size() == 0) {
				oneFeature.setSelected();
				selectedCount++;
			} else {
				// 2.2 get the union of the all children supports
				set.clear();
				for (int w = 0; w < children.size(); w++) {
					if (children.get(w).isSelected())
						set.add(frequentSubgraphs.getPosting(children.get(w)));
				}
				// 2.3 Do the selection
				if (oneFeature.getFrequency() > set.size() * ratio) {
					oneFeature.setSelected();
					selectedCount++;
				}
			}
		}
		// 3. Return the selected Features
		freqFeaturesExt.clearSubSuperRelation();
		System.out.println("(3.2) Time for Pattern Selection: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("(4) After Pattern Selection: " + selectedCount
				+ " out of " + freqFeaturesExt.getfeatureNum()
				+ " frequent patterns as selected for Indexing");
		return frequentSubgraphs.getSelectedFeatures(null, null, false);
	}

	/**
	 * GPTree Feature Selection Algorithm: In each iteration, the largest
	 * features "covering at least two items" are selected
	 * 
	 * @param frequentSubgraphs
	 *            : Pay attention, in the original work, only "edge-reduced"
	 *            subgraphs are mined
	 * @param dbCount
	 *            : to total number of database graphs
	 * @return
	 */
	public NoPostingFeatures<IOneFeature> minPrefixFeatures(
			PostingFeatures frequentSubgraphs, int dbCount) {
		System.out.println("Select index patterns for prefix");
		System.out.println("(1) Both Features & Postings are Stored in-memory");
		// 0. Load in-memory
		MemoryConsumptionCal.runGC();
		double memoryStart = MemoryConsumptionCal.usedMemoryinMB();
		frequentSubgraphs.loadPostingIntoMemory();
		MemoryConsumptionCal.runGC();
		double memoryEnd = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(2) Space for Pattern Selection: "
				+ (memoryEnd - memoryStart));

		// 1. First sort all features according to their size
		long startTime = System.currentTimeMillis();
		NoPostingFeatures<IOneFeature> features = frequentSubgraphs
				.getFeatures();
		features.sortFeatures(new FeatureComparatorBase());
		// 2. Choose the Larger Features first
		BitSet status = new BitSet(dbCount);
		int selectedCount = 0;
		for (int fID = features.getfeatureNum() - 1; fID >= 0; fID--) {
			IOneFeature oneF = features.getFeature(fID);
			int[] posting = frequentSubgraphs.getPosting(oneF);
			// 2.1 Decide Whether to Select this Feature of Not
			int count = 0;
			for (int gID : posting) {
				if (status.get(gID) == false)
					count++;
				if (count > 1) {
					break;
				}
			}
			// 2.2 Select the feature
			if (count > 1) {
				oneF.setSelected();
				for (int gID : posting)
					status.set(gID);
				selectedCount++;
			} else
				oneF.setUnselected();
		}
		System.out.println("(3) Time for Pattern Selection: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("(4) After Pattern Selection: " + selectedCount
				+ " out of " + features.getfeatureNum()
				+ " frequent patterns as selected for Indexing");
		// 3. Return the Features
		List<IOneFeature> selectedFeatures = features.getSelectedFeatures();
		return new NoPostingFeatures<IOneFeature>(selectedFeatures, false);
	}
}
