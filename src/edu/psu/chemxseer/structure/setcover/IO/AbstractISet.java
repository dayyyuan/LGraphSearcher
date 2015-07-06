package edu.psu.chemxseer.structure.setcover.IO;

import java.io.Serializable;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;

public abstract class AbstractISet implements ISet, Serializable {
	private static final long serialVersionUID = 6174555120304016783L;

	protected int setID;
	protected boolean deleted;
	protected IFeatureWrapper feature;

	public AbstractISet(int setID, boolean deleted, IFeatureWrapper feature) {
		this.setID = setID;
		this.deleted = deleted;
		this.feature = feature;
	}

	@Override
	public void setSetID(int i) {
		setID = i;
	}

	@Override
	public void lazyDelete() {
		this.deleted = true;
	}

	@Override
	public boolean isDeleted() {
		return this.deleted;
	}

	@Override
	public int getSetID() {
		assert (this.setID > 0);
		return this.setID;
	}

	@Override
	public IFeatureWrapper getFeature() {
		return this.feature;
	}
}
