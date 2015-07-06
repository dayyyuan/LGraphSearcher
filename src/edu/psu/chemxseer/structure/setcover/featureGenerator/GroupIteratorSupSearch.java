package edu.psu.chemxseer.structure.setcover.featureGenerator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class GroupIteratorSupSearch implements Iterator<Item_Group> {
	private int[] dbSupport;
	private int[] queryNoSupport;
	// private int[] querySupport;
	private int gIndex = 0;
	private int qRange;

	/**
	 * 
	 * @param set
	 * @param qRange
	 */
	public GroupIteratorSupSearch(Set_Pair set, int qRange) {
		this.dbSupport = set.getValueG();
		this.queryNoSupport = set.getValueNoQ();
		// this.querySupport = set.getValueQ();
		this.gIndex = 0;
		this.qRange = qRange;
	}

	public GroupIteratorSupSearch(Set_Pair_Index set, int qRange) {
		this.dbSupport = set.getUnExamedG();
		this.queryNoSupport = set.getUnExamedQ();
		this.gIndex = 0;
		this.qRange = qRange;
	}

	@Override
	public boolean hasNext() {
		return this.gIndex < dbSupport.length;
	}

	@Override
	public Item_Group next() {
		if (this.hasNext() == false)
			throw new NoSuchElementException();
		else
			return new Item_Group(dbSupport[gIndex++], queryNoSupport,
					OrderedIntSets.getCompleteSet(queryNoSupport, qRange),
					AppType.supSearch);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"remove() method is not supported");
	}

	public int[] getEquals() {
		return new int[0];
	}

}
