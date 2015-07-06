package edu.psu.chemxseer.structure.util;

import java.util.HashSet;
import java.util.List;

/**
 * Support fast set operations, given the set & input are ordered int arrays
 * Benefit: (1) int array saves space compared with genetic (object) arrays (2)
 * Strong assumption that the input array are sorted
 * 
 * @author dayuyuan
 * 
 */
public class OrderedIntSet {
	private int[] items;
	private int capacity;
	private int size;

	/**
	 * Construct a new empty OrderedIntSet
	 */
	public OrderedIntSet() {
		capacity = 0;
		size = 0;
	}

	/**
	 * Add a sorted int array into the set
	 * 
	 * @param c
	 * @return
	 */
	public void add(int[] c) {
		if (c == null)
			return;
		else
			this.add(c, 0, c.length);
	}

	/**
	 * Add a sorted int array (fromIndex -> toIndex) into the set
	 * 
	 * @param c
	 * @param fromIndex
	 *            : inclusive
	 * @param toIndex
	 *            : exclusive
	 * @return
	 */
	public void add(int[] c, int fromIndex, int toIndex) {
		if (c == null || c.length == 0 || toIndex - fromIndex <= 0)
			return;
		int[] newItems;

		capacity = toIndex - fromIndex + size;
		newItems = new int[capacity];
		int iter = 0;
		int i = 0;
		int j = fromIndex;
		while (i < size) {
			while (j < toIndex) {
				if (items[i] < c[j]) {
					newItems[iter] = items[i];
					iter++;
					i++;
					break;
				} else if (items[i] == c[j])
					j++;
				else {
					newItems[iter] = c[j];
					iter++;
					j++;
					continue;
				}
			}
			if (j == toIndex)
				break;
		}
		while (i < size) {
			newItems[iter] = items[i];
			iter++;
			i++;
		}
		while (j < toIndex) {
			newItems[iter] = c[j];
			iter++;
			j++;
		}
		items = newItems;
		size = iter;
	}

	/**
	 * Add a sorted int list (fromIndex -> toIndex) into the set
	 * 
	 * @param c
	 */
	public void add(List<Integer> c) {
		if (c == null || c.size() == 0)
			return;
		Integer[] arrayC = new Integer[c.size()];
		c.toArray(arrayC);
		int[] inputC = new int[arrayC.length];
		for (int i = 0; i < arrayC.length; i++)
			inputC[i] = arrayC[i]; // auto de boxing
		this.add(inputC);
	}

	/**
	 * 
	 * @param c
	 */
	public void join(int[] c) {
		if (c == null || c.length == 0)
			return;
		else
			this.join(c, 0, c.length);

	}

	/**
	 * get the intersection of the set & c
	 * 
	 * @param c
	 * @param fromIndex
	 *            : inclusive
	 * @param toIndex
	 *            : exclusive
	 * @return
	 */
	public void join(int[] c, int fromIndex, int toIndex) {
		if (c == null || c.length == 0 || toIndex - fromIndex <= 0)
			return;
		int iter = 0, i = 0, j = fromIndex;
		// i is index on item, j is index on c
		while (i < size && j < toIndex) {
			if (items[i] > c[j])
				j++;
			else if (items[i] == c[j]) {
				items[iter++] = c[j];
				j++;
				i++;
				continue;
			} else {// items[i] < c[j]
				i++;
				continue;
			}
		}
		size = iter;
	}

	public void join(List<Integer> c) {
		if (c == null || c.size() == 0)
			return;
		int iter = 0, i = 0, j = 0;
		// i is index on item, j is index on c
		while (i < size && j < c.size()) {
			if (items[i] > c.get(j))
				j++;
			else if (items[i] == c.get(j)) {
				items[iter++] = c.get(j);
				j++;
				i++;
				continue;
			} else {// items[i] < c[j]
				i++;
				continue;
			}
		}
		size = iter;
	}

	public int size() {
		return size;
	}

	public void remove(int[] c) {
		if (c == null || c.length == 0)
			return;
		else
			remove(c, 0, c.length);
	}

	/**
	 * 
	 * @param c
	 * @param fromIndex
	 *            : inclusive
	 * @param toIndex
	 *            : exclusive
	 * @return
	 */
	public void remove(int[] c, int fromIndex, int toIndex) {
		if (c == null || c.length == 0 || toIndex - fromIndex <= 0)
			return;
		int iter = 0, i = 0, j = fromIndex;
		while (i < size && j < toIndex) {
			if (items[i] > c[j])
				j++;
			else if (items[i] == c[j]) {
				i++;
				j++;
				// iter did not update
			} else {
				// items[i] < c[j]
				items[iter] = items[i];
				i++;
				iter++;
			}
		}
		while (i < size) {
			items[iter] = items[i];
			i++;
			iter++;
		}
		size = iter;
	}

	// TODO: if capacity of the IntersectionSet always larger a lot than the
	// size of items
	// We do a further optimization of memory by freeing items;
	public boolean clear() {
		size = 0;
		return true;
	}

	public int[] getItems() {
		int[] results = new int[size];
		for (int i = 0; i < size; i++)
			results[i] = items[i];
		return results;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(4 * size);
		buf.append(items[0]);
		for (int i = 1; i < size; i++) {
			buf.append(',');
			buf.append(items[i]);
		}
		return buf.toString();
	}

	public void print() {
		for (int i = 0; i < size; i++) {
			System.out.print(items[i]);
			System.out.print(' ');
		}
		System.out.println();
		System.out.println("Size: " + size + " Capacity: " + capacity);
	}

	public HashSet<Integer> toHashSet() {
		HashSet<Integer> results = new HashSet<Integer>();
		for (int i = 0; i < size; i++)
			results.add(items[i]);
		return results;
	}

}
