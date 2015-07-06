package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.util.Comparator;

import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * Feature Comparator Based on EdgeCount
 * 
 * @author dayuyuan
 * 
 */
public class FeatureComparatorBase implements Comparator<IOneFeature> {
	@Override
	public int compare(IOneFeature arg0, IOneFeature arg1) {
		int edge1 = arg0.getFeatureGraph().getEdgeCount();
		int edge2 = arg1.getFeatureGraph().getEdgeCount();
		if (edge1 < edge2)
			return -1;
		else if (edge1 == edge2)
			return 0;
		else
			return 1;
	}
}
