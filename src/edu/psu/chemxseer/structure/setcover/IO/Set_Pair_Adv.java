package edu.psu.chemxseer.structure.setcover.IO;

import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;

/**
 * The enumerator will return all the item covered by the positives but not
 * covered by the negatives.
 * 
 * @author dayuyuan
 * 
 */
public class Set_Pair_Adv implements ISet {

	private int setID;
	private Set_Pair positive;
	private Set_Pair negative;
	private boolean deleted;
	private int size;

	private IFeatureWrapper feature;

	/**
	 * Construct a Set_Pair_Adv with one positive & one negative only
	 * 
	 * @param positive
	 * @param negative
	 */
	public Set_Pair_Adv(Set_Pair positive, Set_Pair negative) {
		this.positive = positive;
		this.negative = negative;
		this.setID = positive.getSetID();
		this.deleted = false;
		this.size = -1;
		this.feature = positive.getFeature();
	}

	@Override
	public int getSetID() {
		return this.setID;
	}

	@Override
	public void setSetID(int i) {
		setID = i;
	}

	@Override
	public int size() {
		// System.out.println("this will take long time to find the size of the set_pair_adv");
		if (size == -1) {
			this.size = 0;
			Iterator<int[]> it = this.iterator();
			while (it.hasNext()) {
				size++;
				it.next();
			}
		}
		return size;
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
	public Iterator<int[]> iterator() {
		return new Set_Pair_AdvIterator();
	}

	class Set_Pair_AdvIterator implements Iterator<int[]> {
		private Iterator<int[]> itP;
		private Iterator<int[]> itN;
		private int[] nextItem;

		private int[] itemN;

		public Set_Pair_AdvIterator() {
			itP = positive.iterator();
			itN = negative.iterator();

			this.nextItem = null;
			if (itN.hasNext())
				itemN = itN.next();

			findElement();
		}

		@Override
		public boolean hasNext() {
			return this.nextItem != null;
		}

		@Override
		public int[] next() {
			if (!hasNext())
				throw new NoSuchElementException();
			else {
				int[] result = nextItem;
				findElement();
				return result;
			}
		}

		/**
		 * Find an element that is covered
		 * 
		 * @return
		 */
		private void findElement() {
			while (true) {
				if (!itP.hasNext()) {
					this.nextItem = null;
					break;
				} else if (itemN == null) {
					this.nextItem = itP.next();
					break;
				} else {
					this.nextItem = itP.next();
					while (itN.hasNext() && compareTo(nextItem, itemN) > 0)
						itemN = itN.next(); // will stop if itemP <= itemN ||
											// !it.hasNext()
					if (compareTo(nextItem, itemN) != 0)
						break; // itemP > itemN: itN.hasNext=true, itemP is
								// nextItem; itemP < itemN: itemP is nextItem
					else { // itemP == ItemN
						if (itN.hasNext())
							itemN = itN.next();
						continue;
					}
				}
			}
		}

		public int compareTo(int[] itemA, int[] itemB) {
			if (itemA[0] > itemB[0])
				return 1;
			else if (itemA[0] == itemB[0])
				if (itemA[1] > itemB[1])
					return 1;
				else if (itemA[1] == itemB[1])
					return 0;
				else
					return -1;
			else
				return -1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		}
	}

	@Override
	public IFeatureWrapper getFeature() {
		return this.feature;
	}
	/** TEST ONLY **********/
	/*
	 * public void testEqual(){ int[] positiveItems = new int[positive.size()];
	 * int counter = 0; for(IItem item: positive) positiveItems[counter++] =
	 * item.getItem(); boolean flag = isSorted(positiveItems);
	 * 
	 * int[] negativeItems = new int[negative.size()]; counter = 0; for(IItem
	 * item: negative) negativeItems[counter++] = item.getItem(); boolean flag2
	 * = isSorted(negativeItems);
	 * 
	 * int[] item = OrderedIntSets.remove(positiveItems, negativeItems);
	 * 
	 * if(item.length!= this.size()){ System.out.println("stop"); int[] items2 =
	 * new int[this.size]; counter = 0; for(IItem temp:this) items2[counter++] =
	 * temp.getItem(); int[] w1 = OrderedIntSets.remove(item, items2); int[] w2
	 * = OrderedIntSets.remove(items2, item); System.out.println("stop"); } }
	 * 
	 * private boolean isSorted(int[] array){ for(int i = 1; i< array.length;
	 * i++) if(array[i] < array[i-1]) return false; return true; }
	 */

}
