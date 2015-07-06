package edu.psu.chemxseer.structure.setcover.featureGenerator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class GroupIteratorSubSearch implements Iterator<Item_Group> {
	private int[] dbNoSupport;
	private int[] querySupport;
	private int[] queryEqual;
	private int qIndex = 0;
	private int gRange;

	/**
	 * 
	 * @param set
	 * @param gRange
	 */
	public GroupIteratorSubSearch(Set_Pair set, int gRange) {
		this.dbNoSupport = set.getValueNoG();
		this.querySupport = set.getValueQ();
		this.queryEqual = set.getValueQEqual();
		this.qIndex = 0;
		this.gRange = gRange;
	}

	public GroupIteratorSubSearch(Set_Pair_Index set, int gRange) {
		this.dbNoSupport = set.getUnExamedG();
		this.querySupport = set.getUnExamedQ();
		this.queryEqual = set.getValueQEqual();
		this.qIndex = 0;
		this.gRange = gRange;
	}

	@Override
	public boolean hasNext() {
		return this.qIndex < querySupport.length;
	}

	@Override
	public Item_Group next() {
		if (this.hasNext() == false) {
			throw new NoSuchElementException();
		} else {
			return new Item_Group(querySupport[qIndex++], dbNoSupport,
					OrderedIntSets.getCompleteSet(dbNoSupport, gRange),
					AppType.subSearch);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"remove() method is not supported");
	}

	public int[] getEquals() {
		return this.queryEqual;
	}

}
