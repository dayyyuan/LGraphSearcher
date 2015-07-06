package edu.psu.chemxseer.structure.util;

public class IntArraySerializer {
	public static String serialize(int[] array) {
		if (array == null)
			return null;
		StringBuffer sbuf = new StringBuffer();
		for (int value : array) {
			sbuf.append(value);
			sbuf.append(",");
		}
		sbuf.deleteCharAt(sbuf.length() - 1);
		return sbuf.toString();

	}

	public static int[] parse(String text) {
		if (text == null)
			return new int[0];
		String[] tokens = text.split(",");
		if (tokens == null || tokens.length == 0)
			return new int[0];
		int[] result = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++)
			result[i] = Integer.parseInt(tokens[i]);
		return result;
	}
}
