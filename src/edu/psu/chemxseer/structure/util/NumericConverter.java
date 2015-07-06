package edu.psu.chemxseer.structure.util;

/**
 * A converter between byte[] to numeric number, such as Long type, Integer type
 * and short type
 * 
 * @author dayuyuan
 * 
 */
public class NumericConverter {

	/**
	 * Convert a type Long number into byte array of size 8
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] long2byte(long number) {
		byte[] results = new byte[8];
		for (int i = 0; i < 8; i++) {
			results[i] = (byte) (number >> 8 * (7 - i));
		}
		return results;
	}

	/**
	 * Convert a byte array into a type Long number
	 * 
	 * @param byteArray
	 * @return
	 */
	public static long byte2long(byte[] byteArray) {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			result |= byteArray[i] & 0XFF;
		}
		return result;
	}

	/**
	 * Convert an integer into a byte array
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] int2byte(int number) {
		byte[] results = new byte[4];
		results[0] = (byte) (number >> 24);
		results[1] = (byte) (number >> 16);
		results[2] = (byte) (number >> 8);
		results[3] = (byte) number;
		return results;
	}

	// /**
	// * Given an integer, 32 bytes, change it to a string
	// * @param number
	// * @return
	// */
	public static String int2String(int number) {
		// 1. First to byte
		char[] chars = int2Char(number);
		String result = new String(chars);
		// TESt
		/*
		 * char[] testChar = new char[2]; result.getChars(0, result.length(),
		 * testChar, 0); int testNumber = char2Int(testChar);
		 * if(number!=testNumber) System.out.println("lalala");
		 */
		// TESt
		return result;
	}

	public static char[] int2Char(int number) {
		char[] results = new char[2];
		results[0] = (char) (number >> 16);
		results[1] = (char) (number);
		return results;
	}

	public static int char2Int(char[] charArray) {
		int result = 0;
		result = (charArray[0] & 0XFFFF) << 16 | (charArray[1] & 0XFFFF);
		return result;
	}

	/**
	 * Convert a byte array into an integer
	 * 
	 * @param byteArray
	 * @return
	 */
	public static int byte2int(byte[] byteArray) {
		int result = 0;
		result = (byteArray[0] & 0XFF) << 24 | (byteArray[1] & 0XFF) << 16
				| (byteArray[2] & 0XFF) << 8 | (byteArray[3] & 0XFF);
		return result;
	}

	/**
	 * Convert a short number to a byte array
	 * 
	 * @param shortNum
	 * @return
	 */
	public static byte[] short2byte(short shortNum) {
		byte[] results = new byte[2];
		results[0] = (byte) (shortNum >> 8);
		results[1] = (byte) shortNum;
		return results;
	}

	public static short byte2short(byte[] byteArray) {
		short result = 0;
		result = (short) ((byteArray[0] & 0XFF) << 8 | (byteArray[1] & 0XFF));
		return result;
	}

	/**
	 * Test case:
	 */
	public static void main(String args[]) {
		int test = 182739;
		NumericConverter.int2String(test);
	}

}
