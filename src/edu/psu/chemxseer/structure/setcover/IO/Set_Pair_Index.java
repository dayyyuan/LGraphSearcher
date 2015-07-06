package edu.psu.chemxseer.structure.setcover.IO;

import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.update.ISearch_LindexUpdatable;

public class Set_Pair_Index implements ISet {
	private Set_Pair set;
	private int[] unExamedQ;
	private int[] unExamedG;

	public Set_Pair_Index(Set_Pair setPair, ISearch_LindexUpdatable index) {
		this.set = setPair;
		int[][] temp = index.getUnExamedQG(setPair);
		unExamedQ = temp[0];
		unExamedG = temp[1];
	}

	@Override
	public Iterator<int[]> iterator() {
		// Difference, getIterator<Set_Pair_Index> instead of
		// getIterator<Set_Pair>
		return set.converter.getIterator(this);
	}

	public Iterator<Item_Group> getItemGroupIterator() {
		// Difference, getItemGroupIterator<Set_Pair_Index> instead of
		// getItermGroupIterator<Set_Pair>
		return set.converter.getItemGroupIterator(this);
	}

	@Override
	public int getSetID() {
		return this.set.getSetID();
	}

	@Override
	public int size() {
		return this.set.size();
	}

	@Override
	public void lazyDelete() {
		this.set.lazyDelete();
	}

	@Override
	public boolean isDeleted() {
		return this.set.isDeleted();
	}

	@Override
	public IFeatureWrapper getFeature() {
		return this.set.getFeature();
	}

	@Override
	public void setSetID(int i) {
		this.set.setSetID(i);
	}

	public int[] getUnExamedG() {
		return unExamedG;
	}

	public int[] getUnExamedQ() {
		return unExamedQ;
	}

	public int[] getValueQEqual() {
		return set.getValueQEqual();
	}

	public int[] getValueQ() {
		return set.getValueQ();
	}

	public Set_Pair getSet() {
		return this.set;
	}

	public void setSet(Set_Pair result) {
		this.set = result;
	}
}
