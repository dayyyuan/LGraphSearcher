package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * The Factory generating OneFeature
 * 
 * @author dayuyuan
 * 
 */
public abstract class FeatureFactory {

	public enum FeatureFactoryType {
		OneFeature, MultiFeature
	}

	public abstract IOneFeature genOneFeature(int id, String featureString);
}
