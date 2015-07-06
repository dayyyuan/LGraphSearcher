package edu.psu.chemxseer.structure.setcover.experiments;

import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.setcover.IO.Input_Bucket;
import edu.psu.chemxseer.structure.setcover.IO.Input_File;
import edu.psu.chemxseer.structure.setcover.IO.Input_Mem;
import edu.psu.chemxseer.structure.setcover.IO.Input_Stream;
import edu.psu.chemxseer.structure.setcover.IO.Input_StreamDec;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputRandom;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputStream;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureConverterSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The InputGenerator help to generate input for the "set-cover" feature
 * selector It takes the following factors into consideration: (1) The input
 * graph database & training queries (2) The applications: subSearch, supSearch,
 * classification (3) The output type: random, sequential, stream, bucket &
 * their sub types (4) The type for each item in the set: either single item &
 * pair item
 * 
 * @author dayuyuan
 * 
 */
public class InputGenerator {

	private InputGenerator() {
		// Make sure that the input generator is singleton
	}

	public enum AppType {
		subSearch, supSearch, classification
	};

	public enum StatusType {
		normal, normalPreSelect, decompose, decomposePreSelect, normalAdv, normalPreSelectAdv, decomposeAdv, decomposePreSelectAdv, decomposedAdv2, decomposedPreSelectAdv2
	};

	public enum InputType {
		inMem, onDisk
	};

	/**
	 * Return the RandomInput: randomInput has to be in-memory, since disk does
	 * not support random operation (super slow)
	 * 
	 * @param features
	 * @param aType
	 * @return
	 */
	public static IInputRandom getRandomInput(
			PostingFeaturesMultiClass features, AppType aType) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();

		int[] dimension = features.getClassGraphsCount();
		IFeatureConverter converter = new FeatureConverterSimple(dimension[1],
				dimension[0], aType);
		IInputRandom result = Input_Mem.newInstance(features, converter);

		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("MemoryConsumption of Random Input: "
				+ (afterMem - beforeMem));

		return result;
	}

	/**
	 * Return the Sequential Input
	 * 
	 * @param features
	 * @param aType
	 * @return
	 */
	public static IInputSequential getSequentialInput(
			PostingFeaturesMultiClass features, AppType aType, InputType iType,
			String tempFile) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();

		int[] dimension = features.getClassGraphsCount();
		IFeatureConverter converter = new FeatureConverterSimple(dimension[1],
				dimension[0], aType);
		IInputSequential result = null;
		if (iType == InputType.inMem)
			result = getRandomInput(features, aType);
		else
			result = Input_File.newInstance(features, converter, tempFile);

		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("MemoryConsumption of Sequential Input: "
				+ (afterMem - beforeMem));

		return result;
	}

	public static IFeatureConverter getConverter(
			PostingFeaturesMultiClass features, AppType aType) {
		int[] dimension = features.getClassGraphsCount();
		IFeatureConverter converter = new FeatureConverterSimple(dimension[1],
				dimension[0], aType);
		return converter;
	}

	public static IInputStream getStreamInput(
			GSpanMiner_MultiClass_Iterative gen, AppType aType,
			boolean decSupport) {
		if (decSupport)
			return getDecStreamInput(gen, aType);
		else
			return getStreamInput(gen, aType);
	}

	/**
	 * Return the Stream Input: for stream input, there is no concept of on_disk
	 * or in_memory
	 * 
	 * @param gen
	 * @param aType
	 * @return
	 */
	private static Input_Stream getStreamInput(
			GSpanMiner_MultiClass_Iterative gen, AppType aType) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		int[] dimension = gen.getClassGraphCount();
		IFeatureConverter converter = new FeatureConverterSimple(dimension[1],
				dimension[0], aType);
		Input_Stream result = Input_Stream.newInstance(gen, converter);
		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("MemoryConsumption of Stream Input: "
				+ (afterMem - beforeMem));
		return result;
	}

	/**
	 * Return the Stream Input with decreasing minimum support.
	 * 
	 * @param gen
	 * @param aType
	 * @return
	 */
	private static IInputStream getDecStreamInput(
			GSpanMiner_MultiClass_Iterative gen, AppType aType) {
		Input_Stream input = getStreamInput(gen, aType);
		return Input_StreamDec.newInstance(input);
	}

	/**
	 * Construct & Return the bucket input
	 * 
	 * @param input
	 * @param logP
	 * @param converter
	 * @param iType
	 * @return
	 */
	public static IInputBucket getBucketInput(IInputSequential input,
			double logP, InputType iType, AppType aType, String tempFile) {
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();

		long startTime = System.currentTimeMillis();

		IFeatureConverter converter = new FeatureConverterSimple(
				input.getQCount(), input.getGCount(), aType);
		IInputBucket result = Input_Bucket.newInstance(input, tempFile, logP,
				converter, iType);
		long endTime = System.currentTimeMillis();
		System.out.println("Time of BucketInput construction: "
				+ (endTime - startTime));

		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("MemoryConsumption of Bucket Input: "
				+ (afterMem - beforeMem));

		return result;
	}
}
