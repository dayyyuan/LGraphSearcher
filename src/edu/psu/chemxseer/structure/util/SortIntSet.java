package edu.psu.chemxseer.structure.util;

import java.util.Arrays;
import java.util.Comparator;

public class SortIntSet {
	/**
	 * Sort values according to the keys
	 * 
	 * @param values
	 * @param keys
	 * @return sorted values
	 */
	public static int[] sort(int[] values, int[] keys) {
		if (values == null || keys == null || values.length != keys.length) {
			System.out.println("Illegial Input");
			return null;
		} else {
			if (values.length < 10) {
				int[] result = new int[values.length];
				boolean[] selected = new boolean[values.length];
				Arrays.fill(selected, false);

				for (int i = 0; i < result.length; i++) {
					int minKey = Integer.MAX_VALUE;
					int minID = 0;
					// find the min-unselected key & its ID
					for (int ID = 0; ID < values.length; ID++) {
						if (selected[ID])
							continue;
						else if (keys[ID] < minKey) {
							minID = ID;
							minKey = keys[ID];
						}
					}
					result[i] = values[minID];
					selected[minID] = true;
				}
				return result;
			} else {
				Integer[] tempValues = new Integer[values.length];
				for (int i = 0; i < values.length; i++)
					tempValues[i] = values[i];
				Arrays.sort(tempValues, new IntegerComparator(keys));
				int[] result = new int[tempValues.length];
				for (int i = 0; i < tempValues.length; i++)
					result[i] = tempValues[i];
				return result;
			}
		}
	}

	static class IntegerComparator implements Comparator<Integer> {
		private int[] keys1;
		private int[] keys2;

		public IntegerComparator(int[] keys) {
			this.keys1 = keys;
		}

		public IntegerComparator(int[] keys1, int[] keys2) {
			this.keys1 = keys1;
			this.keys2 = keys2;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			if (keys2 == null)
				return keys1[o1] - keys1[o2];
			else {
				if (keys1[o1] == keys1[o2])
					return keys2[o1] - keys2[o2];
				else
					return keys1[o1] - keys1[o2];
			}
		}

	}

	public static int[] sort(int[] values, int[] keys1, int[] keys2) {
		if (values == null || keys1 == null || keys2 == null
				|| values.length != keys1.length
				|| values.length != keys2.length) {
			System.out.println("Illegial Input");
			return null;
		} else {
			if (values.length < 10) {
				int[] result = new int[values.length];
				boolean[] selected = new boolean[values.length];
				Arrays.fill(selected, false);

				for (int i = 0; i < result.length; i++) {
					int minKey1 = Integer.MAX_VALUE;
					int minKey2 = Integer.MAX_VALUE;
					int minID = 0;

					// find the min-unselected key & its ID
					for (int ID = 0; ID < values.length; ID++) {
						if (selected[ID])
							continue;
						else if (keys1[ID] < minKey1
								|| (keys1[ID] == minKey1 && keys2[ID] < minKey2)) {
							minID = ID;
							minKey1 = keys1[ID];
							minKey2 = keys2[ID];
						}
					}
					result[i] = values[minID];
					selected[minID] = true;
				}
				return result;
			} else {
				Integer[] tempValues = new Integer[values.length];
				for (int i = 0; i < values.length; i++)
					tempValues[i] = values[i];
				Arrays.sort(tempValues, new IntegerComparator(keys1, keys2));
				int[] result = new int[tempValues.length];
				for (int i = 0; i < tempValues.length; i++)
					result[i] = tempValues[i];
				return result;
			}
		}
	}
}
