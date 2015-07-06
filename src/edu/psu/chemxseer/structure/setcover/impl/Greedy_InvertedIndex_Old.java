//package edu.psu.chemxseer.structure.setcover.impl;
//
//import java.util.Arrays;
//
//import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
//import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputRandom;
//import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
//import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
//import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
//import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
//import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
//import edu.psu.chemxseer.structure.setcover.status.ICoverStatus;
//import edu.psu.chemxseer.structure.setcover.status.Status_BooleanArrayImpl;
//import edu.psu.chemxseer.structure.setcover.status.Status_Decomp;
//import edu.psu.chemxseer.structure.setcover.status.invertedindex.IInvertedIndex;
//import edu.psu.chemxseer.structure.setcover.status.invertedindex.InvertedIndexArray;
//import edu.psu.chemxseer.structure.setcover.status.invertedindex.InvertedIndexDecomp_Abstract;
//import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;
//import edu.psu.chemxseer.structure.util.PriorityQueueSelfImp;
//
///**
// * A Implementation of the Greedy algorithm for Max-K coverage problem.
// * (1) Inverted-index is used for fast process of the update
// * (2) a max heap is used for fast process of the update
// * In each iteration of feature selection, we (1) fetch and select the feature with maximum marginal 
// * score from the max-heap (2) update the scores of other sets accordingly
// * @author dayuyuan
// */
//@Deprecated
//public class Greedy_InvertedIndex_Old implements IMaxCoverSolver{
//	private ICoverStatus status; // denote which item has been covered & which is not
//	private IInputRandom input; // since we need to fetch a set given its ID, the input has to support random access
//	private IInvertedIndex invertedIndex;
//	private PriorityQueueSelfImp<SetwithScore> maxQueue;
//	
//	private int currentK;
//	private IFeatureWrapper[] topKSetIDs;
//	private int coveredItemCount;
//	
//	public Greedy_InvertedIndex_Old(IInputRandom input, StatusType sType, AppType aType){
//		this.input = input;
//		// Construct the inverted index
//		if(sType == StatusType.normal){
//			this.invertedIndex = InvertedIndexArray.newInstance(input);
//			this.status = Status_BooleanArrayImpl.newInstance(input.getQCount(), input.getGCount());
//		}
//		else{
//			this.invertedIndex = InvertedIndexDecomp_Abstract.newInstance(input, aType);
//			this.status = Status_Decomp.newInstance(input.getQCount(), input.getGCount(), aType);
//		}
//		// Construct the maxQueue
//		maxQueue = new PriorityQueueSelfImp<SetwithScore>(false, SetwithScore.getComparator());
//		for(ISet set: input){
//			SetwithScore scoreSet = input.swapToScoreSet(set);;
//			scoreSet.setScore(scoreSet.size());
//			maxQueue.add(scoreSet);
//		}
//		currentK = 0;
//		topKSetIDs = new IFeatureWrapper[input.getSetCount()];
//		this.coveredItemCount=0;
//	}
//
//	@Override
//	public IFeatureWrapper[] runGreedy(int K, long[] TimeCost, double[] MemCost) {
//		MemoryConsumptionCal.runGC();
//		double startMem = MemoryConsumptionCal.usedMemoryinMB();
//		long startTime = System.currentTimeMillis();
//		MemCost[1] = 0;
//		
//		if(currentK < K)
//			currentK = this.runRealGreedy(K, MemCost);
//		
//		long endTime = System.currentTimeMillis();
//		TimeCost[0] = endTime-startTime;
//		MemoryConsumptionCal.runGC();
//		double endMem = MemoryConsumptionCal.usedMemoryinMB();
//		MemCost[1] -= MemCost[0];
//		MemCost[0] = endMem-startMem;
//		return Arrays.copyOf(topKSetIDs, currentK);
//	}
//	
//	/**
//	 * Run the Greedy algorithms: Assuming currentK sets are already selected, 
//	 * Need to find K sets in total
//	 * @return total number of sets selected: may be smaller than K
//	 * @param K
//	 */
//	private int runRealGreedy(int K, double[] MemCost){
//		for(int i = currentK; i< K; i++){
//			SetwithScore set = this.maxQueue.pollMax();
//			if(set == null || set.getScore() == 0)
//				return i;
//			// else set!=0
//			set.lazyDelete();
//			topKSetIDs[i] = set.getFeature();
//			// update scores of other indexes
//			// 1. Find the newly covered items
//			ISet it = this.status.updateCoverageWithSetReturn(set.getSet());
//			this.coveredItemCount += set.getScore();
//			// 2. For each Item in the set, 
//			// through the inverted index, find the candidate "set"
//			// containing the item & update their score on the maxHeap (decrease by 1)
//			for(int[] item:it){
//				int[] sIDs = this.invertedIndex.getCoveredSetPosIDs(item[0], item[1], -1);
//				for(int setID : sIDs){
//					SetwithScore theSet = (SetwithScore)this.input.getSet(setID);
//					if(theSet!=null){
//						theSet.setScore(theSet.getScore()-1);
//						this.maxQueue.changeKey(theSet, -1);
//					}
//				}
//			}
//			double ratio = (double)(i-currentK) / (double) (i-currentK+1);
//			MemCost[1] = MemCost[1] *ratio
//					+ MemoryConsumptionCal.usedMemoryinMB()/(i-currentK + 1);
//		}
//		return K;
//	}
//
//	@Override
//	public int coveredItemsCount() {
//		return this.coveredItemCount;
//	}
//	@Override
//	public int realCoveredItemCount() {
//		return this.status.getCoveredItemCount();
//	}
//	@Override
//	public IFeatureWrapper[] getFixedSelected() {
//		ISet[] edgeSets = input.getEdgeSets();
//		IFeatureWrapper[] edgeFeatures = new IFeatureWrapper[edgeSets.length];
//		for(int i = 0; i< edgeSets.length; i++)
//			edgeFeatures[i] = edgeSets[i].getFeature();
//		return edgeFeatures;
//	}
//	@Override
//	public int getEnumeratedPatternNum() {
//		return input.getEnumeratedSetCount();
//	}
// }
