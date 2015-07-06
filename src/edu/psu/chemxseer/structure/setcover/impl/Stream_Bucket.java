package edu.psu.chemxseer.structure.setcover.impl;

/*import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;

 import edu.psu.chemxseer.structure.setcover.IO.Input_Bucket;
 import edu.psu.chemxseer.structure.setcover.IO.interfaces.IBucket;
 import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputBucket;
 import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
 import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
 import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.InputType;
 import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
 import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
 import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
 import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_HW;
 import edu.psu.chemxseer.structure.setcover.status.Status_IntArrayImpl;
 import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
 import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;*/

/**
 * The strem_bucket methods proposed by HuiWen
 * @author dayuyuan
 *
 */
//public class Stream_Bucket implements IMaxCoverSolver{
//	private ICoverStatus_HW status;
//	private IInputSequential input;
//	private double p;
//	private int coveredItemCount;
//	
//	// to support continuity
//	private boolean greedyran;
//	private IInputBucket buckets; // all the buckets storing the selected sets
//	
//	/**
//	 * We need to specify the the IInpuSequential can be enumerated only once
//	 * @param input
//	 * @param status
//	 */
//	public Stream_Bucket(IInputSequential input, double p, IFeatureConverter converter, StatusType sType){
//		MemoryConsumptionCal.runGC();
//		double beforeMem = MemoryConsumptionCal.usedMemoryinMB();
//		long beforeTime = System.currentTimeMillis();
//		
//		if(sType == StatusType.normal)
//			this.status = Status_IntArrayImpl.newInstance(input.getQCount(), input.getGCount());
//		else{
//			System.out.println("Exception: for now We do not suppoort SetType.pair for Stream_Bucket");
//		}
//		this.input = input;
//		this.greedyran = false;
//		this.coveredItemCount = 0;
//		// The in-memory buckets, storing the sets
//		buckets = Input_Bucket.newEmptyInstance("temp", Math.log(p), converter, InputType.inMem);
//		
//		long afterTime = System.currentTimeMillis();
//		MemoryConsumptionCal.runGC();
//		double afterMem = MemoryConsumptionCal.usedMemoryinMB();
//		System.out.println("Time for Stream_Bucket Construction: " + (afterTime-beforeTime));
//		System.out.println("Space for Stream_Bucket Construction: " + (afterMem-beforeMem));
//	}
//
//
//	@Override
//	public IOneFeature[] runGreedy(int K, long[] TimeCost, double[] MemCost) {
//		MemoryConsumptionCal.runGC();
//		MemCost[0] = MemoryConsumptionCal.usedMemoryinMB();
//		long startTime = System.currentTimeMillis();
//	
//		this.coveredItemCount = 0;
//		if(this.greedyran == false){
//			this.runRealGreedy(MemCost);
//			this.greedyran = true;
//		}
//		// collecting the top K features
//		IOneFeature[] result = new IOneFeature[K];
//		int counter = 0;
//		for(IBucket bucket: buckets){
//			if(counter == K)
//				break;
//			for(ISet set: bucket)
//				if(counter == K)
//					break;
//				else {
//					result[counter++] = set.getFeature();
//					coveredItemCount += set.size();
//				}
//		}
//		
//		long endTime = System.currentTimeMillis();
//		TimeCost[0] = endTime-startTime;
//		MemoryConsumptionCal.runGC();
//		double endMem = MemoryConsumptionCal.usedMemoryinMB();
//		MemCost[1] -= MemCost[0];
//		MemCost[0] = endMem-MemCost[0];
//		
//		return result;
//	}
//	/**
//	 * Run greedy algorithm
//	 * (1) For each incoming sets, find Us & Ls, Us is defined as the sets with weight > w(s)/p
//	 * (2) Compute S-Us, if W(s-Us) then insert S to the corresponding bucket by keeping sets 
//	 * 	 in this bucket in decreasing order.
//	 * (3) Otherwise, update Us, goes to step (1)
//	 * (4) update all the sets in Ls
//	 * @return
//	 */
//	private void  runRealGreedy(double[] MemCost){
//		double iterCount = 0;
//		for(ISet oneSet: this.input){
//			ISet selectSet= oneSet;
//			Map<Integer, Integer> Us = null; 
//			
//			while(selectSet.size() > 0){
//				//(1) Find Us & Ls: Us is sets with weight > score/p
//				int score = selectSet.size();
//				double threshold = score/p;
//				
//				if(Us == null){
//					Us = new HashMap<Integer, Integer>();
//					for(IBucket bucket: buckets){
//						boolean timeToBreak = false;
//						for(ISet storedSet: bucket){
//							if(storedSet.size() >= threshold)
//								Us.put(storedSet.getSetID(), storedSet.size());
//							else {
//								timeToBreak = true;
//								break;
//							}
//						}
//						if(timeToBreak)
//							break;
//					}
//				}
//				else {
//					for(Iterator<Entry<Integer, Integer>> it = Us.entrySet().iterator(); it.hasNext();){
//						Entry<Integer, Integer> oneEntry =  it.next();
//						if(oneEntry.getValue() < threshold)
//							it.remove();
//					}
//				}
//				//(2) Given the Us (set IDs), checkout the status file: 
//				selectSet = this.status.getCoverage(selectSet, Us.keySet());
//				if(selectSet.size() < threshold){
//					continue; // keep on finding the right place for theSet
//				}
//				else{
//					// append theSet to the right place
//					this.buckets.appendWithOrder(selectSet, selectSet.size());
//					// update Ls, for each the set S in Ls, we need to update them to S-theSet & update the 
//					// status correspondingly
//					this.status.updateCoverage(selectSet, Us.keySet())
//;					
//					for(IBucket bucket: this.buckets){
//						for(Iterator<ISet> it = bucket.iterator();it.hasNext();){
//							ISet lsSet =  it.next();
//							if(lsSet.size() < threshold)
//								it.remove();
//							lsSet = this.status.getCoverage(lsSet);
//							this.buckets.append(lsSet, lsSet.size());
//						}	
//					}
//					break;
//				}
//			}
//
//			MemCost[1] = MemCost[1] * (iterCount /(iterCount + 1))
//					+ MemoryConsumptionCal.usedMemoryinMB()/(iterCount + 1);
//			iterCount ++;
//		}
//	}
//	
//	public int coveredItemsCount(){
//		return this.coveredItemCount;
//	}
//	@Override
//	public int realCoveredItemCount() {
//		return this.status.getCoveredItemCount();
//	}
//	
//	@Override
//	public IOneFeature[] getFixedSelected() {
//		ISet[] edgeSets = input.getEdgeSets();
//		IOneFeature[] edgeFeatures = new IOneFeature[edgeSets.length];
//		for(int i = 0; i< edgeSets.length; i++)
//			edgeFeatures[i] = edgeSets[i].getFeature();
//		return edgeFeatures;
//	}
//	@Override
//	public int getEnumeratedPatternNum() {
//		return input.getEnumeratedSetCount();
//	}
// }
