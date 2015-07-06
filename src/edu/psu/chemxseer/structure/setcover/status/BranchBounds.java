package edu.psu.chemxseer.structure.setcover.status;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;

public class BranchBounds {

	/**
	 * Construct & Return a BranchBound Calculator
	 * 
	 * @param aType
	 * @param qCount
	 * @param gCount
	 * @param baseName
	 * @return
	 */
	public static IBranchBound getBBCal(AppType aType, int qCount, int gCount,
			String baseName) {
		switch (aType) {
		case subSearch:
			return new SubSearchBB(loadArrays(baseName + "qGCovered"),
					loadArrays(baseName + "qEqual"), gCount);
		case supSearch:
			return new SupSearchBB(loadArrays(baseName + "gQCovered"), qCount);
		case classification:
			return new ClassificationBB(qCount, gCount);
		default:
			return null;
		}
	}

	/**
	 * Construct & Return an advanced branch & Bound Calculator
	 * 
	 * @param aType
	 * @param baseName
	 * @param status
	 * @return
	 */
	public static IBranchBound getBBCalAdv(AppType aType, String baseName,
			ICoverStatus_Swap status) {
		switch (aType) {
		case subSearch:
			return new SubSearchBB2(loadArrays(baseName + "qGCovered"),
					loadArrays(baseName + "qEqual"),
					(Status_SubSearchOpt2) status);
		case supSearch:
			return new SupSearchBB2(loadArrays(baseName + "gQCovered"),
					(Status_SupSearchOpt2) status);
		default:
			return null;
		}
	}

	public static IBranchBound getBBCalAdv(AppType aType, int[] unEqualCount,
			int[] equalCount, ICoverStatus_Swap status) {
		if (aType == AppType.subSearch)
			return new SubSearchBB2(unEqualCount, equalCount,
					(Status_SubSearchOpt2) status);
		else if (aType == AppType.supSearch)
			return new SupSearchBB2(unEqualCount, (Status_SupSearchOpt2) status);
		else
			return null;
	}

	public static IBranchBound getBBCalAdv3(AppType aType, int[] unEqualCount,
			int[] equalCount, int gCount) {
		switch (aType) {
		case subSearch:
			return new SubSearchBB3(unEqualCount, equalCount, gCount);
			/*
			 * case supSearch: return new SupSearchBB(loadArrays(baseName +
			 * "gQCovered"), qCount); case classification: return new
			 * ClassificationBB(qCount, gCount);
			 */
		default:
			return null;
		}
	}

	/**
	 * Given a file name, load the int array contained in that file
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static int[] loadArrays(String fileName) {
		int[] result = null;
		try {
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(fileName));
			String aLine = reader.readLine();
			if (aLine == null) {
				reader.close();
				return null;// something must be wrong here
			}
			int count = Integer.parseInt(aLine);
			result = new int[count];
			for (int i = 0; i < count; i++) {
				aLine = reader.readLine();
				if (aLine == null) {
					reader.close();
					return null; // something must be wrong again
				}
				result[i] = Integer.parseInt(aLine);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
