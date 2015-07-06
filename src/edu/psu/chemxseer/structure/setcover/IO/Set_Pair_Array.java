package edu.psu.chemxseer.structure.setcover.IO;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;

/**
 * Internal representations are set of items.
 * 
 * @author dayuyuan
 * 
 */
public class Set_Pair_Array extends AbstractISet implements ISet {
	private static final long serialVersionUID = 479058554258362232L;
	private int[][] items;

	public Set_Pair_Array(List<int[]> items, int setID, IFeatureWrapper feature) {
		super(setID, false, feature);

		this.items = new int[items.size()][2];
		int counter = 0;
		for (int[] itemPair : items) {
			this.items[counter][0] = itemPair[0];
			this.items[counter][1] = itemPair[1];
			counter++;
		}
	}

	@Override
	public Iterator<int[]> iterator() {
		// TODO Auto-generated method stub
		return new Set_Pair_Array_Iterator();
	}

	class Set_Pair_Array_Iterator implements Iterator<int[]> {
		/** The end index to loop to */
		protected int endIndex = 0;
		/** The current iterator index */
		protected int index = 0;

		public Set_Pair_Array_Iterator() {
			this.endIndex = items.length;
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return (index < endIndex);
		}

		@Override
		public int[] next() {
			if (hasNext() == false) {
				throw new NoSuchElementException();
			}
			int[] result = items[index];
			index++;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		}
	}

	@Override
	public int size() {
		return this.items.length;
	}
}
