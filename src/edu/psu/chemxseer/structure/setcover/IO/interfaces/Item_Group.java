package edu.psu.chemxseer.structure.setcover.IO.interfaces;

import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;

public class Item_Group implements Iterable<int[]> {
	private int itemA;
	private int[] itemB;
	private int[] noItemB;
	private AppType aType;

	/**
	 * If aType = subSearch, then itemA is the query and itemB is the (not
	 * contained) database graph If aType = supSearch, then itemA is the
	 * database graph and itemB is the (not contained) query
	 * 
	 * @param itemA
	 * @param noItemB
	 * @param aType
	 */
	public Item_Group(int itemA, int[] itemB, int[] noItemB, AppType aType) {
		this.itemA = itemA;
		this.itemB = itemB;
		this.noItemB = noItemB;
		this.aType = aType;
	}

	public int getItemA() {
		return itemA;
	}

	public int[] getNoItemsB() {
		return noItemB;
	}

	public AppType getType() {
		return aType;
	}

	@Override
	public Iterator<int[]> iterator() {
		return new SubSearchIterator();
	}

	class SubSearchIterator implements Iterator<int[]> {
		private int index;

		public SubSearchIterator() {
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return this.index < itemB.length;
		}

		@Override
		public int[] next() {
			int[] result = new int[2];
			if (aType == AppType.subSearch) {
				result[0] = itemA;
				result[1] = itemB[index++];
			} else if (aType == AppType.supSearch) {
				result[1] = itemA;
				result[0] = itemB[index++];
			} else {
				throw new UnsupportedOperationException(
						"next() method is not supported for classification");
			}
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		}

	}
}
