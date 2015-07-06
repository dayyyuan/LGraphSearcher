package edu.psu.chemxseer.structure.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * Similar interface to OrderedIntSet Only difference is that OrderedIntSet is
 * faster on multiple chain operations The static function is fast on single
 * operations because no new object is constructed
 * 
 * @author dayuyuan
 * 
 */
public class OrderedIntSets {
	/**
	 * Given two array, firstArray & second array and their boundary Return the
	 * position of the item on firstArray = item on second array
	 * 
	 * @param firstArray
	 * @param start1
	 * @param end1
	 * @param secondArray
	 * @param start2
	 * @param end2
	 * @return
	 */
	public static int[] getJoinPosition(int[] firstArray, int start1, int end1,
			int[] secondArray, int start2, int end2) {
		if (firstArray == null || secondArray == null)
			return new int[0];
		int[] pos = new int[end1 - start1];
		int iter = 0, i = start1, j = start2;
		// i is index on item, j is index on c
		while (i < end1 && j < end2) {
			if (firstArray[i] > secondArray[j])
				j++;
			else if (firstArray[i] == secondArray[j]) {
				pos[iter++] = i;
				j++;
				i++;
			} else {// items[i] < c[j]
				i++;
				continue;
			}
		}
		int[] result = new int[iter];
		for (int w = 0; w < iter; w++)
			result[w] = pos[w];
		return result;

	}

	/**
	 * Given two arrays: arrayOne and arrayTwo, and assume that these two arrays
	 * are sorted Find out what is the intersection set size for the two arrays
	 * 
	 * @param arrayOne
	 * @param arrayTwo
	 */
	public static int getJoinSize(int[] arrayOne, int[] arrayTwo) {
		if (arrayOne == null || arrayTwo == null)
			return 0;
		else {
			int iter = 0, i = 0, j = 0;
			// i is index on item, j is index on c
			while (i < arrayOne.length && j < arrayTwo.length) {
				if (arrayOne[i] > arrayTwo[j])
					j++;
				else if (arrayOne[i] == arrayTwo[j]) {
					j++;
					i++;
					iter++;
				} else {// items[i] < c[j]
					i++;
					continue;
				}
			}
			return iter;
		}
	}

	public static int[] getCompleteSet(int[] items, int wholeBound) {
		int[] results = new int[wholeBound - items.length];
		int i = 0, resultsIndex = 0, itemsIndex = 0;
		for (; i < wholeBound & itemsIndex < items.length; i++) {
			if (i < items[itemsIndex])
				results[resultsIndex++] = i;
			else if (i == items[itemsIndex])
				itemsIndex++;
			else if (i > items[itemsIndex])
				System.out.println("Illigle Items: not sorted");
		}
		for (; i < wholeBound; i++, resultsIndex++)
			results[resultsIndex] = i;
		return results;

	}

	/**
	 * Get the union of the two
	 * 
	 * @param arrayOne
	 * @param arrayTwo
	 * @return
	 */
	public static int[] getUnion(int[] arrayOne, int[] arrayTwo) {
		if (arrayOne == null || arrayOne.length == 0)
			if (arrayTwo == null)
				return new int[0];
			else
				return arrayTwo;
		else if (arrayTwo == null || arrayTwo.length == 0)
			return arrayOne;
		int[] newItems = new int[arrayOne.length + arrayTwo.length];
		int iter = 0;
		int i = 0;
		int j = 0;
		while (i < arrayOne.length) {
			while (j < arrayTwo.length) {
				if (arrayOne[i] < arrayTwo[j]) {
					newItems[iter] = arrayOne[i];
					iter++;
					i++;
					break;
				} else if (arrayOne[i] == arrayTwo[j])
					j++;
				else {
					newItems[iter] = arrayTwo[j];
					iter++;
					j++;
					continue;
				}
			}
			if (j == arrayTwo.length)
				break;
		}
		while (i < arrayOne.length) {
			newItems[iter] = arrayOne[i];
			iter++;
			i++;
		}
		while (j < arrayTwo.length) {
			newItems[iter] = arrayTwo[j];
			iter++;
			j++;
		}
		return Arrays.copyOfRange(newItems, 0, iter);
	}

	/**
	 * Join arrayOne with arrayTwo
	 * 
	 * @param arrayOne
	 * @param arrayTwo
	 * @return
	 */
	public static int[] join(int[] arrayOne, int[] arrayTwo) {
		if (arrayOne == null || arrayTwo == null || arrayOne.length == 0
				|| arrayTwo.length == 0)
			return new int[0];
		int tempSize = Math.min(arrayOne.length, arrayTwo.length);
		int[] result = new int[tempSize];

		int iter = 0, i = 0, j = 0;
		// i is index on item, j is index on c
		while (i < arrayOne.length && j < arrayTwo.length) {
			if (arrayOne[i] > arrayTwo[j])
				j++;
			else if (arrayOne[i] == arrayTwo[j]) {
				result[iter++] = arrayOne[j];
				j++;
				i++;
				continue;
			} else {// arrayOne[i] < arrayTwo[j]
				i++;
				continue;
			}
		}
		return Arrays.copyOfRange(result, 0, iter);
	}

	/**
	 * Remove ArrayTwo from ArrayOne
	 * 
	 * @param ArrayOne
	 * @param ArrayTwo
	 * @return
	 */
	public static int[] remove(int[] ArrayOne, int[] ArrayTwo) {
		if (ArrayTwo == null || ArrayTwo.length == 0)
			if (ArrayOne == null)
				return new int[0];
			else
				return ArrayOne;
		int[] result = new int[ArrayOne.length];
		int iter = 0, i = 0, j = 0;
		while (i < ArrayOne.length && j < ArrayTwo.length) {
			if (ArrayOne[i] > ArrayTwo[j])
				j++;
			else if (ArrayOne[i] == ArrayTwo[j]) {
				i++;
				j++;
				// iter did not update
			} else {
				// ArrayOne[i] < ArrayTwo[j]
				result[iter] = ArrayOne[i];
				i++;
				iter++;
			}
		}
		while (i < ArrayOne.length) {
			result[iter] = ArrayOne[i];
			i++;
			iter++;
		}
		return Arrays.copyOfRange(result, 0, iter);
	}

	/**
	 * Remove Entities on ArrayTwo from ArrayOne, bounded by the start & end
	 * bound
	 * 
	 * @param ArrayOne
	 * @param startOne
	 *            : inclusive
	 * @param endOne
	 *            : exclusive
	 * @param ArrayTwo
	 *            :
	 * @param startTwo
	 *            : inclusive
	 * @param endTwo
	 *            : exclusive
	 * @return
	 */
	public static int[] remove(int[] ArrayOne, int startOne, int endOne,
			int[] ArrayTwo, int startTwo, int endTwo) {
		if (ArrayOne == null || ArrayTwo == null)
			throw new NullPointerException();
		if (startOne < 0 || startTwo < 0 || endOne > ArrayOne.length
				|| endTwo > ArrayTwo.length)
			throw new IndexOutOfBoundsException();

		int[] result = new int[endOne - startOne];
		int iter = 0, i = startOne, j = startTwo;
		while (i < endOne && j < endTwo) {
			if (ArrayOne[i] > ArrayTwo[j])
				j++;
			else if (ArrayOne[i] == ArrayTwo[j]) {
				i++;
				j++;
				// iter did not update
			} else {
				// ArrayOne[i] < ArrayTwo[j]
				result[iter] = ArrayOne[i];
				i++;
				iter++;
			}
		}
		while (i < endOne) {
			result[iter] = ArrayOne[i];
			i++;
			iter++;
		}
		return Arrays.copyOfRange(result, 0, iter);
	}

	public static int[] toArray(Collection<Integer> input) {
		int[] result = new int[input.size()];
		int index = 0;
		for (Integer it : input)
			result[index++] = it;
		return result;
	}

	public static boolean isOrdered(int[] inputGIDs) {
		if (inputGIDs == null || inputGIDs.length <= 1)
			return true;
		int preValue = inputGIDs[0];
		for (int value : inputGIDs) {
			if (preValue > value)
				return false;
			preValue = value;
		}
		return true;
	}
}
