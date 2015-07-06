package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.util.BitSet;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureComparatorBase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The Feature Mining for The Prefix Index
 * 
 * @author dayuyuan
 * 
 */
public class PrefixFeatureMiner {
	/**
	 * Prefix Feature Selection Algorithm: In each iteration, we select the
	 * feature with the maximum "gain" The gain function is defined as: size(f)
	 * * (covered-1)/covered
	 * 
	 * @param frequentSubgraphs
	 *            : Pay attention, in the original work, only "edge-reduced"
	 *            subgraphs are mined
	 * @param dbCount
	 *            : to total number of database graphs
	 * @return
	 */
	public PostingFeatures minPrefixFeatures(PostingFeatures frequentSubgraphs,
			int dbCount) {
		System.out.println("Select Index-Patterns for Prefix Tree");
		System.out.println("(1) Both Features & Postings are Stored in-memory");
		// 0. Load the in-memory Postings
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
		System.out.println("(3.1) Base Sort Features: "
				+ (System.currentTimeMillis() - startTime));
		// 2. Choose the Larger Features first
		BitSet status = new BitSet(dbCount);
		int selectedCount = 0;

		// Do feature selection
		startTime = System.currentTimeMillis();
		while (selectedCount < features.getfeatureNum()) {
			float maxGain = -1;
			IOneFeature nextF = null;
			for (int fID = features.getfeatureNum() - 1; fID >= 0; fID--) {
				IOneFeature oneF = features.getFeature(fID);
				if (oneF.isSelected())
					continue;
				else if (oneF.getFeatureGraph().getEdgeCount() < maxGain) // gain
																			// <
																			// edgeCount
																			// <
																			// maxGain
					break;
				int[] posting = frequentSubgraphs.getPosting(oneF);
				// 2.1 Calculate the gain function
				int newlyCoveredCount = 0;
				for (int gID : posting) {
					if (status.get(gID) == false)
						newlyCoveredCount++;
				}
				float gain = (float) (oneF.getFeatureGraph().getEdgeCount() * (newlyCoveredCount - 1))
						/ (float) (newlyCoveredCount);
				// 2.2 Update the score
				if (gain > maxGain) {
					maxGain = gain;
					nextF = oneF;
				}
			}
			if (maxGain <= 0)
				break;
			else {
				nextF.setSelected();
				selectedCount++;
				// udpate status
				int[] posting = frequentSubgraphs.getPosting(nextF);
				for (int gID : posting) {
					if (status.get(gID) == false)
						status.set(gID);
				}
			}
		}
		System.out.println("(3.2) Time for Pattern Selection: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("(4) After Pattern Selection: " + selectedCount
				+ " out of " + features.getfeatureNum()
				+ " frequent patterns as selected for Indexing");
		frequentSubgraphs.discardInMemPosting(); // discard the in-memory
													// postings to save space
		// 3. Return the Features
		return frequentSubgraphs.getSelectedFeatures(null, null, false);
	}
}
