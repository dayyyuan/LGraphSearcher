package edu.psu.chemxseer.structure.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * The int atom map takes an integer as input and return the label of that
 * corresponding integer
 * 
 * @author dayuyuan
 * 
 */
public class IntAtomMap {
	private Map<Integer, String> map;

	private IntAtomMap(final Map<Integer, String> theMap) {
		this.map = theMap;
	}

	public String getLabel(int id) {
		Integer temp = new Integer(id);
		if (map.containsKey(temp))
			return map.get(temp);
		else
			throw new NoSuchElementException();
	}

	/**
	 * Given a file, read the file (containing a int to Label mapping) and
	 * construct a IntAtomMap
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static IntAtomMap newInstance(String fileName) throws IOException {
		Map<Integer, String> inputMap = new HashMap<Integer, String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String aLine = reader.readLine();
		while (aLine != null) {
			String[] temp = aLine.split(" ");
			inputMap.put(Integer.parseInt(temp[1]), temp[0]);
			aLine = reader.readLine();
		}
		reader.close();
		return new IntAtomMap(inputMap);
	}
}
