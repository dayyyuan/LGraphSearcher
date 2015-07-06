package edu.psu.chemxseer.structure.setcover.experiments;

import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputRandom;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputStream;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.InputType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.impl.External_Bucket;
import edu.psu.chemxseer.structure.setcover.impl.Greedy_InvertedIndex;
import edu.psu.chemxseer.structure.setcover.impl.Greedy_Scan;
import edu.psu.chemxseer.structure.setcover.impl.StreamingAlgorithm;
import edu.psu.chemxseer.structure.setcover.impl.StreamingAlgorithm_InvertedIndex;
import edu.psu.chemxseer.structure.setcover.impl.SwapAlgorithm;
import edu.psu.chemxseer.structure.setcover.impl.SwapAlgorithm_InvertedIndex;
import edu.psu.chemxseer.structure.setcover.impl.SwapAlgorithm_Old;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;

/**
 * construct & return the set-cover solver
 * 
 * @author dayuyuan
 * 
 */
public class SolverFactory {

	public enum SolverType {
		BruteScan, GreedyIndex, SwapOne_Old, Swap, SwapIndex, BucketSort, StreamSort, SwapBB, SwapIndexBB, DirectIterative
	};

	// private constructor to prevent from being constructed
	private SolverFactory() {
	}

	/**
	 * @param featureGen
	 * @param solverType
	 * @param aType
	 * @param bbCalFolder
	 * @param sType
	 * @param decInput
	 * @param lambda
	 * @return
	 */
	public static IMaxCoverSolver getStreamSolver(
			GSpanMiner_MultiClass_Iterative featureGen, SolverType solverType,
			AppType aType, String bbCalFolder, StatusType sType,
			Boolean decInput, double lambda) {
		switch (solverType) {
		case SwapBB:
			return getStreamSolver(featureGen, solverType, aType, sType,
					bbCalFolder, decInput, lambda);
		case SwapIndexBB:
			return getStreamSolverIndex(featureGen, solverType, aType, sType,
					bbCalFolder, decInput, lambda);
		case SwapOne_Old:
			return getSwapAlgorithm_Old(featureGen, aType, sType, decInput);
		case Swap:
			return getSwapAlgorithm(featureGen, aType, sType, decInput, lambda);
		case SwapIndex:
			return getSwapAlgorithmInvertedIndex(featureGen, aType, sType,
					decInput, lambda);
		default:
			return null;
		}
	}

	public static IMaxCoverSolver getSolver(PostingFeaturesMultiClass features,
			SolverType solverType, AppType aType, StatusType sType,
			InputType iType, String diskFile, double lambda) {
		double p = 1.01;
		switch (solverType) {
		case BruteScan:
			return getBruteForceSolver(features, aType, sType, iType, diskFile);
		case GreedyIndex:
			return getInvertedIndexSolver(features, aType, sType);
		case SwapOne_Old:
			return getSwapAlgorithm_Old(features, aType, sType, iType, diskFile);
		case Swap:
			return getSwapAlgorithm(features, aType, sType, iType, diskFile,
					lambda);
		case SwapIndex:
			return getSwapAlgorithmInvertedIndex(features, aType, sType, iType,
					diskFile, lambda);
		case BucketSort:
			return getBucketSorting(features, aType, sType, iType, p, diskFile);
		default:
			return null;
		}
	}

	private static IMaxCoverSolver getBruteForceSolver(
			PostingFeaturesMultiClass features, AppType aType,
			StatusType sType, InputType iType, String diskFile) {
		IInputSequential input = InputGenerator.getSequentialInput(features,
				aType, iType, diskFile);
		Greedy_Scan solver = new Greedy_Scan(input, sType, aType);
		return solver;
	}

	private static IMaxCoverSolver getInvertedIndexSolver(
			PostingFeaturesMultiClass features, AppType aType, StatusType sType) {
		IInputRandom input = InputGenerator.getRandomInput(features, aType);
		Greedy_InvertedIndex solver = new Greedy_InvertedIndex(input, sType,
				aType);
		return solver;
	}

	private static IMaxCoverSolver getSwapAlgorithm_Old(
			PostingFeaturesMultiClass features, AppType aType,
			StatusType sType, InputType iType, String diskFile) {
		IInputSequential input = InputGenerator.getSequentialInput(features,
				aType, iType, diskFile);
		SwapAlgorithm_Old solver = new SwapAlgorithm_Old(input, sType, aType);
		return solver;
	}

	private static IMaxCoverSolver getSwapAlgorithm_Old(
			GSpanMiner_MultiClass_Iterative featureGen, AppType aType,
			StatusType sType, Boolean decInput) {
		IInputSequential input = InputGenerator.getStreamInput(featureGen,
				aType, decInput);
		SwapAlgorithm_Old solver = new SwapAlgorithm_Old(input, sType, aType);
		return solver;
	}

	private static SwapAlgorithm getSwapAlgorithm(
			PostingFeaturesMultiClass features, AppType aType,
			StatusType sType, InputType iType, String diskFile, double lambda) {
		IInputSequential input = InputGenerator.getSequentialInput(features,
				aType, iType, diskFile);
		SwapAlgorithm solver = new SwapAlgorithm(input, sType, aType, lambda);
		return solver;
	}

	private static IMaxCoverSolver getSwapAlgorithm(
			GSpanMiner_MultiClass_Iterative featureGen, AppType aType,
			StatusType sType, Boolean decInput, double lambda) {
		IInputSequential input = InputGenerator.getStreamInput(featureGen,
				aType, decInput);
		SwapAlgorithm solver = new SwapAlgorithm(input, sType, aType, lambda);
		return solver;
	}

	private static IMaxCoverSolver getSwapAlgorithmInvertedIndex(
			PostingFeaturesMultiClass features, AppType aType,
			StatusType sType, InputType iType, String diskFile, double lambda) {
		IInputSequential input = InputGenerator.getSequentialInput(features,
				aType, iType, diskFile);
		SwapAlgorithm_InvertedIndex solver = new SwapAlgorithm_InvertedIndex(
				input, sType, aType, lambda);
		return solver;
	}

	private static IMaxCoverSolver getSwapAlgorithmInvertedIndex(
			GSpanMiner_MultiClass_Iterative featureGen, AppType aType,
			StatusType sType, Boolean decInput, double lambda) {
		IInputSequential input = InputGenerator.getStreamInput(featureGen,
				aType, decInput);
		SwapAlgorithm_InvertedIndex solver = new SwapAlgorithm_InvertedIndex(
				input, sType, aType, lambda);
		return solver;
	}

	private static External_Bucket getBucketSorting(
			PostingFeaturesMultiClass features, AppType aType,
			StatusType sType, InputType iType, double p, String diskFile) {
		IInputSequential input = InputGenerator.getSequentialInput(features,
				aType, iType, diskFile);
		// Construct the Bucket Input
		IInputBucket bucketInput = InputGenerator.getBucketInput(input,
				Math.log(p), iType, aType, diskFile + "bucket");
		External_Bucket solver = new External_Bucket(bucketInput, sType, aType);
		return solver;
	}

	private static IMaxCoverSolver getStreamSolverIndex(
			GSpanMiner_MultiClass_Iterative featureGen, SolverType solverType,
			AppType aType, StatusType sType, String bbCalFolder,
			Boolean decInput, double lambda) {
		IInputStream input = InputGenerator.getStreamInput(featureGen, aType,
				decInput);
		StreamingAlgorithm_InvertedIndex solver = new StreamingAlgorithm_InvertedIndex(
				input, sType, aType, bbCalFolder, lambda);
		return solver;
	}

	private static IMaxCoverSolver getStreamSolver(
			GSpanMiner_MultiClass_Iterative featureGen, SolverType solverType,
			AppType aType, StatusType sType, String bbCalFolder,
			Boolean decInput, double lambda) {
		IInputStream input = InputGenerator.getStreamInput(featureGen, aType,
				decInput);
		StreamingAlgorithm solver = new StreamingAlgorithm(input, sType, aType,
				bbCalFolder, lambda);
		return solver;
	}
}
