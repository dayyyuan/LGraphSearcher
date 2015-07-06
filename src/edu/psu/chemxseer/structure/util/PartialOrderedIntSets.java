package edu.psu.chemxseer.structure.util;

import java.util.Arrays;

public class PartialOrderedIntSets {
	/**
	 * Return the (unsorted) set of integers = unSorted / sorted Return the
	 * position of unSorted array that are not in sorted
	 * 
	 * @param unSorted
	 * @param sorted
	 * @return
	 */
	public static int[] removeGetPosition(int[] unSorted, int unSortedEnd,
			int[] sorted) {
		int index = 0;
		int[] result = new int[unSorted.length];
		for (int i = 0; i < unSortedEnd; i++) {
			int item = unSorted[i];
			int position = Arrays.binarySearch(sorted, item);
			if (position < 0) // not contained in the sorted array
				result[index++] = i;
		}
		int[] finalResult = Arrays.copyOf(result, index);
		return finalResult;
	}

	public static int[] removeGetPosition(final int[] unSorted,
			int unSortedEnd, final int[] sorted2, final int[] sorted) {
		int index = 0;
		int[] result = new int[unSorted.length];
		for (int i = 0; i < unSortedEnd; i++) {
			int item = unSorted[i];
			int position = Arrays.binarySearch(sorted, item);
			if (position < 0) { // not contained in the sorted array
				int position2 = Arrays.binarySearch(sorted2, item);
				if (position2 < 0)
					result[index++] = i;
			}
		}
		int[] finalResult = Arrays.copyOf(result, index);
		return finalResult;
	}
}
