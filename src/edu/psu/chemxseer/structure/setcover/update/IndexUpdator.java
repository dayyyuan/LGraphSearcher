package edu.psu.chemxseer.structure.setcover.update;

import java.util.Arrays;
import java.util.Map;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair_Index;
import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureConverterSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureWrapperSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.impl.StreamingAlgorithm_InvertedIndex_Update;
import edu.psu.chemxseer.structure.setcover.status.BranchBounds;
import edu.psu.chemxseer.structure.setcover.status.IBranchBound;
import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap_InvertedIndex;
import edu.psu.chemxseer.structure.setcover.status.Status_Decomp_Adv;
import edu.psu.chemxseer.structure.setcover.status.Status_Opt;
import edu.psu.chemxseer.structure.setcover.status.Status_SubSearchOpt2;
import edu.psu.chemxseer.structure.setcover.status.Status_SupSearchOpt2;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * IndexUpdator coordinate the update of the graph index for new features (1)
 * decides the right time to trigger the index-update procedure (2) construct
 * the swap-solver for index-feature update (3) update the index
 * 
 * @author dayuyuan
 * 
 */
public class IndexUpdator {
	// The index for update
	private ISearch_LindexUpdatable index;
	// The solver for index-feature update
	public StreamingAlgorithm_InvertedIndex_Update swapSolver;
	// The input for pattern enumeration
	private IndexUpdator_Input input;
	// the threshold for queries/database graphs to be sampled for feature
	// update
	private int topGCount;

	public IndexUpdator(ISearch_LindexUpdatable index, int topGCount) {
		this.index = index;
		if (topGCount < 0 || topGCount > index.getGCount()) {
			topGCount = index.getGCount();
		} else
			this.topGCount = topGCount;
	}

	/**
	 * Trigger the update of the index by first constructing the swapSolver
	 * minSupport is applied on both database graphs & query graphs
	 */
	public float[] initializeUpdate(AppType aType, StatusType sType,
			double minSupport, double lambda) {
		float[] stat = new float[4];
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		long beforeTime = System.currentTimeMillis();
		System.out.println("Start IndexUpdator Initialization");
		// Construct the input
		int qCount = index.getQCount();
		int gCount = index.getGCount();
		input = IndexUpdator_Input.newInstance(sType, index,
				new FeatureConverterSimple(qCount, gCount, aType));
		swapSolver = buildSwapSolver(qCount, gCount, aType, sType, lambda);
		// Construct the input again (with sample database & queries)
		if (topGCount < index.getGCount()) {
			int[] gIDs = getUnCoveredGIDsInOrder(swapSolver.getStatus(),
					gCount, topGCount);
			IGraphDatabase qDB = index.getQueryDB();
			input.setConverter(new FeatureConverterSimple(qCount, gCount, gIDs,
					aType));
			input.constructPatternGen(index, index.getGraphDB(gIDs), qDB,
					minSupport, minSupport);
			stat[2] = gIDs.length;
			stat[3] = qDB.getTotalNum();
		} else {
			IGraphDatabase gDB = index.getGraphDB();
			IGraphDatabase qDB = index.getQueryDB();
			input.constructPatternGen(index, gDB, qDB, minSupport, minSupport);
			stat[2] = gDB.getTotalNum();
			stat[3] = qDB.getTotalNum();
		}

		long afterTime = System.currentTimeMillis();
		MemoryConsumptionCal.runGC();
		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("Time for IndexUpdator Initialization: "
				+ (afterTime - beforeTime));
		System.out.println("Space for IndexUpdator Initialization: "
				+ (afterMem - beforeMem));
		stat[0] = (float) minSupport;
		stat[1] = swapSolver.getSelectedFetureCount();
		return stat;
	}

	private StreamingAlgorithm_InvertedIndex_Update buildSwapSolver(int qCount,
			int gCount, AppType aType, StatusType sType, double lambda) {
		// Given the input, construct a IMaxCoverSwapSolver
		MemoryConsumptionCal.runGC();
		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
		ICoverStatus_Swap_InvertedIndex status = null;
		IBranchBound bbCal = null;
		if (sType == StatusType.decomposePreSelectAdv) {
			status = Status_Opt.newInstance(
					Status_Decomp_Adv.newInstance(qCount, gCount, aType,
							input.getEdgeSets()), aType);
			if (aType == AppType.subSearch)
				bbCal = BranchBounds.getBBCalAdv(aType, index.getQGCount(),
						index.getQEqual(), status);
			else
				bbCal = BranchBounds.getBBCalAdv(aType, index.getGQCount(),
						null, status);
		} else if (sType == StatusType.decomposedPreSelectAdv2) {
			status = Status_Opt.newInstance3(
					Status_Decomp_Adv.newInstance(qCount, gCount, aType,
							input.getEdgeSets()), aType);
			if (aType == AppType.subSearch)
				bbCal = BranchBounds.getBBCalAdv3(aType, index.getQGCount(),
						index.getQEqual(), gCount);
			else
				bbCal = BranchBounds.getBBCalAdv3(aType, index.getGQCount(),
						null, qCount);
		}
		MemoryConsumptionCal.runGC();
		double mem = MemoryConsumptionCal.usedMemoryinMB() - beforeMem;
		return new StreamingAlgorithm_InvertedIndex_Update(input, status,
				bbCal, mem, lambda, this);
	}

	/**
	 * Get the top number of un-covered graphs.
	 * 
	 * @param status
	 * @param gCount
	 * @param topGCount
	 * @return
	 */
	private int[] getUnCoveredGIDsInOrder(
			ICoverStatus_Swap_InvertedIndex status, int gCount, int topGCount) {
		int[] coveredCount = new int[gCount];
		for (int gID = 0; gID < gCount; gID++)
			coveredCount[gID] = (int) status.getCoveredItemCountForG(gID);
		// Here I do not consider the time-efficiency, lol
		int[] temp = coveredCount.clone();
		Arrays.sort(temp); // ascending coverage.
		int threshold = temp[topGCount];// find the topGCount number of graphs
										// with small coverage.

		temp = new int[topGCount];
		for (int i = 0, index = 0; i < gCount && index < topGCount; i++)
			if (coveredCount[i] <= threshold)
				temp[index++] = i;
		return temp;
	}

	/**
	 * Return all Graphs with coverage less than coveredThreshold
	 * 
	 * @param status
	 * @param gCount
	 * @param coveredThreshold
	 * @return
	 */
	/*
	 * private int[] getUnCoveredGIDsInOrder(ICoverStatus_Swap_InvertedIndex
	 * status, int gCount, int coveredThreshold) { boolean[] result = new
	 * boolean[gCount]; Arrays.fill(result, false); int count = 0; for(int gID =
	 * 0; gID < gCount; gID++) if(status.getCoveredItemCountForG(gID) <
	 * coveredThreshold){ result[gID] = true; count++; } int[] temp = new
	 * int[count]; for(int i = 0, index = 0; i < gCount; i++) if(result[i])
	 * temp[index++] = i; return temp; }
	 */
	/**
	 * Return all queries with coverage less than coveredThreshold
	 * 
	 * @param status
	 * @param qCount
	 * @param coveredThreshold
	 * @return
	 */
	/*
	 * private int[] getUnCoveredQIDsInOrder(ICoverStatus_Swap_InvertedIndex
	 * status, int qCount, int coveredThreshold) { boolean[] result = new
	 * boolean[qCount]; Arrays.fill(result, false); int count = 0; for(int qID =
	 * 0; qID < qCount; qID++) if(status.getCoveredItemCountForQ(qID) <
	 * coveredThreshold){ result[qID] = true; count++; } int[] temp = new
	 * int[count]; for(int i = 0, index = 0; i < qCount; i++) if(result[i])
	 * temp[index++] = i; return temp; }
	 */

	/**
	 * update the index to accommodate the changes.
	 */
	public float[] doUpdate() {
		int swapCount = 0;
		MemoryConsumptionCal.runGC();
		double mem = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();
		long indexUpdateTime = 0, startT = 0;
		long oldCoverage = swapSolver.coveredItemsCount();
		System.out.println("Coverage before update: " + oldCoverage);

		for (SetwithScore[] swapPair : swapSolver) {
			swapCount++;
			if (swapPair == null)
				break;
			if (swapPair[0] != null && swapPair[1] != null) {
				startT = System.currentTimeMillis();
				int termPos = swapSolver.getLindexTermPos(swapPair[1].getPos());
				index.doSwap(swapPair, termPos);
				indexUpdateTime += System.currentTimeMillis() - startT;
			}
		}
		index.clearQueryLogs();
		MemoryConsumptionCal.runGC();
		double endMem = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("Time for Exclusive Exam: "
				+ swapSolver.exclusiveExamTime + " Time for Status Update: "
				+ swapSolver.statusUpdateTime + " Time for Score Update: "
				+ swapSolver.scoreUpdateTime + " Time for Find Full Set: "
				+ swapSolver.findFullSupportTime
				+ " Time for update the index: " + indexUpdateTime
				+ " with Total Swaps: " + swapCount);
		long totalTime = System.currentTimeMillis() - startTime;

		float[] stat = new float[11];
		stat[0] = totalTime;
		stat[1] = swapSolver.exclusiveExamTime;
		stat[2] = swapSolver.statusUpdateTime;
		stat[3] = swapSolver.scoreUpdateTime;
		stat[4] = input.getPatternEnumerationTime();
		stat[5] = (float) (endMem - mem + swapSolver.statusInitialSpace);
		stat[6] = (float) (swapSolver.avgMemCost - mem + swapSolver.statusInitialSpace);
		stat[7] = swapSolver.getEnumeratedPatternNum();
		stat[8] = swapSolver.swapCount;
		stat[9] = oldCoverage;
		stat[10] = swapSolver.coveredItemsCount();

		System.out.println("Time: " + totalTime + " Space: " + (endMem - mem)
				+ " Ave_Space: " + swapSolver.avgMemCost + " Coverage: "
				+ swapSolver.coveredItemsCount());
		return stat;
	}

	/**
	 * Given the Index, return the sets (features) selected for indexing
	 * 
	 * @return
	 */
	public ISet[] getSelectedSets(Map<Integer, Integer> setPosLindexPosMap) {
		return this.input.toSets(this.index, setPosLindexPosMap);
	}

	/**
	 * Construct a set "full set" containing support on the whole set of
	 * database graphs & query graphs
	 * 
	 * @param oneSet
	 * @return
	 */
	private Set_Pair getFullSet2(Set_Pair oneSet,
			IFeatureConverter fullFeatureConverter) {
		Graph oneGraph = oneSet.getFeature().getFeature().getFeatureGraph();
		int[][] postings = index.getContainingGraphandQuery(oneGraph,
				oneSet.getValueG(), oneSet.getValueNoG(), oneSet.getValueQ(),
				oneSet.getValueNoQ());
		IFeatureWrapper oldFeature = oneSet.getFeature();
		FeatureWrapperSimple newFeature = new FeatureWrapperSimple(
				oldFeature.getFetureID(), oneGraph, postings[0], postings[1],
				postings[2], postings[3]);
		Set_Pair result = new Set_Pair(newFeature, oneSet.getSetID(),
				fullFeatureConverter);
		return result;
	}

	public ISet getFullSet(ISet aSet, IFeatureConverter fullFeatureConverter) {
		if (aSet instanceof Set_Pair)
			return getFullSet2((Set_Pair) aSet, fullFeatureConverter);
		else if (aSet instanceof Set_Pair_Index) {
			Set_Pair oneSet = ((Set_Pair_Index) aSet).getSet();
			Set_Pair result = this.getFullSet2(oneSet, fullFeatureConverter);
			((Set_Pair_Index) aSet).setSet(result);
			return aSet;
		} else
			throw new ClassCastException();
	}

	public long getFeatureCount() {
		return this.input.getEnumeratedSetCount();
	}

}
