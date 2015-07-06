package edu.psu.chemxseer.structure.setcover.interfaces;

import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;

public interface IBranchBoundCalculator {
	public int getUpperBound(IFeatureWrapper oneFeature, short minFeatureID);
}
