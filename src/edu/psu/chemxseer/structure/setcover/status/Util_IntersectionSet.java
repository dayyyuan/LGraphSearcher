package edu.psu.chemxseer.structure.setcover.status;

import java.util.Arrays;

public class Util_IntersectionSet {

	private Util_IntersectionSet() {
		// dummy constructor to make sure that MaxCoverStatus_IntersectionSet
		// can not be instantiated
	}

	/**
	 * Given two sorted array as the input Return (n+1) if
	 * |firstArray-secondArray| > n (strictly greater) Else, return K =
	 * |firstArray-secondArray| <= n
	 * 
	 * @param firstArray
	 * @param secondArray
	 * @param n
	 * @return
	 */
	public static int contain(int[] firstArray, int firstArraySize,
			int[] secondArray, int secondArraySize, int n) {
		if (firstArraySize - secondArraySize > n)
			return n + 1;
		int i = 0, j = 0;
		int counter = 0;
		while (i < firstArraySize && j < secondArraySize) {
			if (firstArray[i] < secondArray[j]) {
				counter++;
				i++;
				if (counter > n)
					return n + 1;
			} else if (firstArray[i] == secondArray[j]) {
				i++;
				j++;
				continue;
			} else if (firstArray[i] > secondArray[j])
				j++; // continue;
		}
		int k = firstArraySize - i + counter;
		if (k > n)
			return n + 1;
		else
			return k;
	}

	/*
	 * (private static int remove(int[] firstArray, int firstArraySize, int[]
	 * secondArray, int secondArraySize, int n){
	 * if(firstArraySize-secondArraySize > n) return n+1;
	 * 
	 * int i = 0, j = 0; int counter = 0; while( i < firstArraySize && j <
	 * secondArraySize){ if(firstArray[i] < secondArray[j]){ counter++; i++;
	 * if(counter > n) return n+1; } else if(firstArray[i] == secondArray[j]){
	 * i++;j++; continue; } else if(firstArray[i] > secondArray[j]) j++;
	 * //continue; } int k = firstArraySize- i + counter; if(k > n) return n+1;
	 * else return k; }
	 * 
	 * private static void testContain(int[] firstArray, int firstArraySize,
	 * int[] secondArray, int secondArraySize, int n, boolean santa){ int[]
	 * tempFirst = Arrays.copyOfRange(firstArray, 0, firstArraySize); int[]
	 * tempSecond = Arrays.copyOfRange(secondArray, 0, secondArraySize); int[]
	 * res = OrderedIntSets.remove(tempFirst, tempSecond); boolean flag = false;
	 * if(res.length > n) flag = true; if(santa != flag)
	 * System.out.println("stop"); }
	 */
	/**
	 * Return -2 if multiple value are found (except for exceptItemI) Return -1
	 * if non value are found (except for exceptItemI) Return the single value
	 * if one and only one is found
	 * 
	 * @param firstArray
	 * @param firstArrayBoundary
	 * @param secondArray
	 * @param secondArrayBoundary
	 * @param exceptItemI
	 * @return
	 */
	/*
	 * public static int retainSingleValue(int[] firstArray, int
	 * firstArrayBoundary, int[] secondArray, int secondArrayBoundary, int
	 * exceptItemI){ int temp = retainSingleValueReal(firstArray,
	 * firstArrayBoundary, secondArray, secondArrayBoundary, exceptItemI); int[]
	 * temp2 = testRetain(firstArray, firstArrayBoundary, secondArray,
	 * secondArrayBoundary, exceptItemI); if(temp >= 0 && temp2.length == 1){
	 * temp = temp; // do nothing } else if(temp < 0 && temp2.length!=1){ temp =
	 * temp; // do nothing } else System.out.println("Chu shier la"); return
	 * temp; }
	 */
	public static int retainSingleValue(int[] firstArray,
			int firstArrayBoundary, int[] secondArray, int secondArrayBoundary) {
		int result = -1;
		int counter = 0;
		int iterOne = 0;
		for (int iterTwo = 0; iterOne < firstArrayBoundary
				&& iterTwo < secondArrayBoundary;) {
			if (firstArray[iterOne] == secondArray[iterTwo]) {
				iterOne++;
				iterTwo++;
			} else if (firstArray[iterOne] < secondArray[iterTwo]) {
				result = firstArray[iterOne];
				counter++;
				iterOne++;
			} else
				iterTwo++;
			if (counter > 1)
				return -2; // multiple sets are found
		}
		while (iterOne < firstArrayBoundary) {
			result = firstArray[iterOne];
			counter++;
			if (counter > 1)
				return -2;
			iterOne++;
		}
		return result;
	}

	/**
	 * Assume that the firstArray & secondArray are sorted, return firstArray /
	 * secondArray, except the item exceptItemI
	 * 
	 * @param firstArray
	 * @param firstArrayBoundary
	 * @param secondArray
	 * @param secondArrayBoundary
	 * @param exceptItemI
	 * @return
	 */
	public static int[] retain(int[] firstArray, int firstArrayBoundary,
			int[] secondArray, int secondArrayBoundary, int exceptItemI) {
		if (firstArrayBoundary == 0)
			return new int[0];
		int[] result = new int[firstArrayBoundary];
		int resultIndex = 0;

		int iterOne = 0;
		for (int iterTwo = 0; iterOne < firstArrayBoundary
				&& iterTwo < secondArrayBoundary;) {
			if (firstArray[iterOne] == secondArray[iterTwo]) {
				iterOne++;
				iterTwo++;
				continue;
			} else if (firstArray[iterOne] < secondArray[iterTwo]) {
				if (firstArray[iterOne] != exceptItemI)
					result[resultIndex++] = firstArray[iterOne];
				iterOne++;
			} else
				iterTwo++;
		}
		while (iterOne < firstArrayBoundary) {
			if (firstArray[iterOne] != exceptItemI)
				result[resultIndex++] = firstArray[iterOne];
			iterOne++;
		}

		int[] res = Arrays.copyOf(result, resultIndex);
		return res;
	}

	/*
	 * private static int[] testRetain(int[] firstArray, int firstArrayBoundary,
	 * int[] secondArray, int secondArrayBoundary, int exceptItemI){
	 * if(firstArray == null || firstArray.length ==0)
	 * System.out.println("lala"); int[] tempFirst =
	 * Arrays.copyOfRange(firstArray, 0, firstArrayBoundary); int[] tempSecond =
	 * Arrays.copyOfRange(secondArray, 0, secondArrayBoundary); int[] temp =
	 * OrderedIntSets.remove(tempFirst, tempSecond); int[] except = new int[1];
	 * except[0] = exceptItemI; return OrderedIntSets.remove(temp, except); }
	 */
	// /**
	// * Prerequisite: firstArray & secondArray all contain no-negative number
	// * Return -1, if fistArray-secondArray = empty set
	// * Return -2, if firstArray-secondArray = set with size >1
	// * Return n, if firstArray-secondArray = n.
	// * @param firstArray
	// * @param seconArray
	// */
	// public static int retain(short[] firstArray, int firstArrayBoundary,
	// short[] secondArray, int secondArrayBoundary, int exceptItemI){
	// int result = -1;
	// if(firstArrayBoundary == 0)
	// return -1; //firstArray is empty
	//
	// // firstArray is not empty or null
	// int i = 0, j = 0;
	// while( i < firstArrayBoundary && j < secondArrayBoundary){
	// if(firstArray[i] == secondArray[j]){
	// i ++; j++;
	// }
	// // firstArray[i] not in secondArray
	// else if(firstArray[i] < secondArray[j]){
	// if(firstArray[i] == exceptItemI){
	// i++; // continue;
	// }
	// else{
	// if(result!=-1)
	// return -2; //no single result
	// else result = firstArray[i];
	// i++;
	// }
	// }
	// else //firstArray[i] > secondArray[j]
	// j++;
	// }
	// for(; i < firstArrayBoundary; i++){
	// if(firstArray[i] == exceptItemI)
	// continue;
	// if(result!=-1)
	// return -2; // no single result;
	// else result = firstArray[i];
	// }
	//
	// return result;
	// }
	//
	//
	//
	// /**
	// * Return A-B
	// * @param A
	// * @param B
	// */
	// public static int[] retain(int[] A, int[] B) {
	// if(B == null || B.length == 0)
	// return A;
	// else if(A == null)
	// return A;
	// int iter = 0, i = 0, j = 0;
	// int[] result = new int[A.length];
	// // i is index on item, j is index on c
	// while(i < A.length && j < B.length){
	// if(A[i] > B[j])
	// j++;
	// else if(A[i]== B[j]){
	// result[iter++]=B[j];
	// j++;
	// i++;
	// continue;
	// }
	// else {// items[i] < c[j]
	// i++;
	// continue;
	// }
	// }
	// int[] finalResult = new int[iter];
	// for(int w = 0; w < iter; w++)
	// finalResult[w] = result[w];
	// return finalResult;
	// }

}
