package edu.psu.chemxseer.structure.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * An extension to the "set" classes
 * 
 * @author dayuyuan
 * 
 * @param <T>
 */
public class SelfImplementSet2<T> {
	private ArrayList<T> items;
	private Comparator<T> comp;
	private int capacity;
	private int size;

	public SelfImplementSet2(Comparator<T> comp) {
		capacity = 0;
		size = 0;
	}

	/**
	 * Remove all index terms that are larger than size
	 * 
	 * @param numOfSets
	 * @return
	 */
	public void removeLarge(int index) {
		this.size = index + 1;
	}

	public boolean addAll(T[] c) {
		if (c == null || c.length == 0)
			return false;
		capacity = c.length + size;
		ArrayList<T> newItems = new ArrayList<T>(items.size());
		int iter = 0;
		int i = 0;
		int j = 0;
		while (i < size) {
			while (j < c.length) {
				if (comp.compare(items.get(i), c[j]) < 0) {
					newItems.add(items.get(i));
					iter++;
					i++;
					break;
				} else if (comp.compare(items.get(i), c[j]) == 0)
					j++;
				else {
					newItems.add(c[j]);
					iter++;
					j++;
					continue;
				}
			}
			if (j == c.length)
				break;
		}
		while (i < size) {
			newItems.add(items.get(i));
			iter++;
			i++;
		}
		while (j < c.length) {
			newItems.add(c[j]);
			iter++;
			j++;
		}
		items = newItems;
		size = iter;
		return true;
	}

	// /**
	// *
	// * @param c
	// * @param fromIndex : inclusive
	// * @param toIndex : exclusive
	// * @return
	// */
	// public boolean addAll(T[] c, int fromIndex, int toIndex){
	// if(c == null|| c.length == 0 || toIndex-fromIndex <=0)
	// return false;
	// this.onElement = c[fromIndex];
	// capacity = toIndex-fromIndex+size;
	// T[] newItems = (T[]) Array.newInstance(this.onElement.getClass(),
	// c.length);
	// int iter = 0;
	// int i = 0;
	// int j = fromIndex;
	// while(i < size){
	// while(j < toIndex){
	// if(items[i].compareTo(c[j]) < 0){
	// newItems[iter]=items[i];
	// iter++;
	// i++;
	// break;
	// }
	// else if(items[i].compareTo(c[j])==0)
	// j++;
	// else{
	// newItems[iter]=c[j];
	// iter++;
	// j++;
	// continue;}
	// }
	// if(j == toIndex)
	// break;
	// }
	// while(i < size){
	// newItems[iter] = items[i];
	// iter++;
	// i++;
	// }
	// while(j < toIndex){
	// newItems[iter]=c[j];
	// iter++;
	// j++;
	// }
	// items = newItems;
	// size = iter;
	// return true;
	// }
	//
	public boolean addAll(List<T> c) {
		if (c == null || c.size() == 0)
			return false;
		capacity = c.size() + size;
		ArrayList<T> newItems = new ArrayList<T>();
		int iter = 0;
		int i = 0;
		int j = 0;
		while (i < size) {
			while (j < c.size()) {
				if (comp.compare(items.get(i), c.get(j)) < 0) {
					newItems.add(items.get(i));
					iter++;
					i++;
					break;
				} else if (comp.compare(items.get(i), c.get(j)) == 0)
					j++;
				else {
					newItems.add(c.get(j));
					iter++;
					j++;
					continue;
				}
			}
			if (j == c.size())
				break;
		}
		while (i < size) {
			newItems.add(items.get(i));
			iter++;
			i++;
		}
		while (j < c.size()) {
			newItems.add(c.get(j));
			iter++;
			j++;
		}
		items = newItems;
		size = iter;
		return true;
	}

	public boolean retainAll(T[] c) {
		if (c == null || c.length == 0)
			return false;
		int iter = 0, i = 0, j = 0;
		// i is index on item, j is index on c
		while (i < size && j < c.length) {
			if (comp.compare(items.get(i), c[j]) > 0)
				j++;
			else if (comp.compare(items.get(i), c[j]) == 0) {
				items.add(iter++, c[j]);
				j++;
				i++;
				continue;
			} else {// items[i] < c[j]
				i++;
				continue;
			}
		}
		size = iter;
		return true;
	}

	// /**
	// *
	// * @param c
	// * @param fromIndex: inclusive
	// * @param toIndex: exclusive
	// * @return
	// */
	// public boolean retainAll(T[] c, int fromIndex, int toIndex){
	// if(c == null || c.length == 0 || toIndex-fromIndex <=0)
	// return false;
	// int iter = 0, i = 0, j = fromIndex;
	// // i is index on item, j is index on c
	// while(i < size && j < toIndex){
	// if(items[i].compareTo(c[j])>0)
	// j++;
	// else if(items[i].compareTo(c[j])==0){
	// items[iter++]=c[j];
	// j++;
	// i++;
	// continue;
	// }
	// else {// items[i] < c[j]
	// i++;
	// continue;
	// }
	// }
	// size = iter;
	// return true;
	// }
	//
	public boolean retainAll(List<T> c) {
		if (c == null || c.size() == 0)
			return false;
		int iter = 0, i = 0, j = 0;
		// i is index on item, j is index on c
		while (i < size && j < c.size()) {
			if (comp.compare(items.get(i), c.get(j)) > 0)
				j++;
			else if (comp.compare(items.get(i), c.get(j)) == 0) {
				items.set(iter++, c.get(j));
				j++;
				i++;
				continue;
			} else {// items[i] < c[j]
				i++;
				continue;
			}
		}
		size = iter;
		return true;
	}

	public int size() {
		return size;
	}

	public boolean removeAll(T[] c) {
		if (c == null || c.length == 0)
			return false;
		int iter = 0, i = 0, j = 0;
		while (i < size && j < c.length) {
			if (comp.compare(items.get(i), c[j]) > 0)
				j++;
			else if (comp.compare(items.get(i), c[j]) == 0) {
				i++;
				j++;
				// iter did not update
			} else {
				// items[i] < c[j]
				items.set(iter, items.get(i));
				i++;
				iter++;
			}
		}
		while (i < size) {
			items.set(iter, items.get(i));
			i++;
			iter++;
		}
		size = iter;
		return true;
	}

	public boolean removeAll(List<T> c) {
		if (c == null || c.size() == 0)
			return false;
		int iter = 0, i = 0, j = 0;
		while (i < size && j < c.size()) {
			if (comp.compare(items.get(i), c.get(j)) > 0)
				j++;
			else if (comp.compare(items.get(i), c.get(j)) == 0) {
				i++;
				j++;
				// iter did not update
			} else {
				// items[i] < c[j]
				items.set(iter, items.get(i));
				i++;
				iter++;
			}
		}
		while (i < size) {
			items.set(iter, items.get(i));
			i++;
			iter++;
		}
		size = iter;
		return true;
	}

	/**
	 * 
	 * @param c
	 * @param fromIndex
	 *            : inclusive
	 * @param toIndex
	 *            : exclusive
	 * @returnInteger
	 */
	public boolean removeAll(T[] c, int fromIndex, int toIndex) {
		if (c == null || c.length == 0 || toIndex - fromIndex <= 0)
			return false;
		int iter = 0, i = 0, j = fromIndex;
		while (i < size && j < toIndex) {
			if (comp.compare(items.get(i), c[j]) > 0)
				j++;
			else if (comp.compare(items.get(i), c[j]) == 0) {
				i++;
				j++;
				// iter did not update
			} else {
				// items[i] < c[j]
				items.set(iter, items.get(i));
				i++;
				iter++;
			}
		}
		while (i < size) {
			items.set(iter, items.get(i));
			i++;
			iter++;
		}
		size = iter;
		return true;
	}

	// TODO: if capacity of the IntersectionSet always larger a lot than the
	// size of items
	// We do a further optimization of memory by freeing items;
	public boolean clear() {
		size = 0;
		return true;
	}

	public ArrayList<T> getItems() {
		ArrayList<T> results = new ArrayList<T>();
		for (int i = 0; i < size; i++)
			results.add(items.get(i));
		return results;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(4 * size);
		buf.append(items.get(0));
		for (int i = 1; i < size; i++) {
			buf.append(',');
			buf.append(items.get(i).toString());
		}
		return buf.toString();
	}

	public boolean print() {
		for (int i = 0; i < size; i++) {
			System.out.print(items.get(i).toString());
			System.out.print(' ');
		}
		System.out.println();
		System.out.println("Size: " + size + " Capacity: " + capacity);
		return true;
	}

	public HashSet<T> toHashSet() {
		HashSet<T> results = new HashSet<T>();
		for (int i = 0; i < size; i++)
			results.add(items.get(i));
		return results;
	}
}
