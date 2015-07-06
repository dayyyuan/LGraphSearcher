package edu.psu.chemxseer.structure.setcover.IO;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IBucket;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.InputType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;

public class Bucket_Factory {

	private Bucket_Factory() {
		// prevent the factory being initialized
	}

	/**
	 * Given the bucketFile, load a Bucket return null if such bucket can not be
	 * loaded
	 * 
	 * @param bucketFile
	 * @param inputType
	 * @return
	 */
	public static IBucket loadBucket(String bucketFile,
			IFeatureConverter converter, InputType inputType) {
		if (inputType == InputType.inMem)
			return Bucket_Mem.newInstance(bucketFile, converter);
		else if (inputType == InputType.onDisk)
			return Bucket_File.newInstance(bucketFile, true, converter);
		else
			return null;
	}

	/**
	 * Create an empty bucket & store on bucketFile if type = in_mem, then the
	 * bucketFile will not be used
	 * 
	 * @param bucketFile
	 * @param type
	 * @return
	 */
	public static IBucket createBucket(String bucketFile,
			IFeatureConverter converter, InputType iType) {
		if (iType == InputType.inMem)
			return Bucket_Mem.newEmptyInstance();
		else if (iType == InputType.onDisk)
			return Bucket_File.newEmptyInstance(bucketFile, false, converter);
		else
			return null;
	}
}
