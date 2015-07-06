package edu.psu.chemxseer.structure.setcover.status;

import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;

public class Status_Opt {

	public static ICoverStatus_Swap newInstance(ICoverStatus_Swap status,
			AppType aType) {
		if (aType == AppType.subSearch)
			return new Status_SubSearchOpt2(status);
		else if (aType == AppType.supSearch)
			return new Status_SupSearchOpt2(status);
		/*
		 * else if(aType == AppType.classification) return new
		 * Status_ClassificationOpt2(status);
		 */
		else
			return null; // exceptions
	}

	public static ICoverStatus_Swap_InvertedIndex newInstance(
			ICoverStatus_Swap_InvertedIndex status, AppType aType) {
		if (aType == AppType.subSearch)
			return new Status_SubSearchOpt2(status);
		else if (aType == AppType.supSearch)
			return new Status_SupSearchOpt2(status);
		/*
		 * else if(aType == AppType.classification) return new
		 * Status_ClassificationOpt2(status);
		 */
		else
			return null; // exceptions
	}

	public static ICoverStatus_Swap newInstance3(ICoverStatus_Swap status,
			AppType aType) {
		if (aType == AppType.subSearch)
			return new Status_SubSearchOpt3(status);
		/*
		 * else if(aType == AppType.supSearch) return new
		 * Status_SupSearchOpt3(status); else if(aType ==
		 * AppType.classification) return new Status_ClassificationOpt2(status);
		 */
		else
			return null; // exceptions
	}

	public static ICoverStatus_Swap_InvertedIndex newInstance3(
			ICoverStatus_Swap_InvertedIndex status, AppType aType) {
		if (aType == AppType.subSearch)
			return new Status_SubSearchOpt3(status);
		/*
		 * else if(aType == AppType.supSearch) return new
		 * Status_SupSearchOpt3(status); else if(aType ==
		 * AppType.classification) return new Status_ClassificationOpt2(status);
		 */
		else
			return null; // exceptions
	}

}
