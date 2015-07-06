package edu.psu.chemxseer.structure.setcover.status;

//package edu.psu.chemxseer.structure.setcover.status.branchbound;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.List;
//
//import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
//import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;
//import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
//import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
//import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap;
//import edu.psu.chemxseer.structure.setcover.status.ICoverStatus_Swap_InvertedIndex;
//import edu.psu.chemxseer.structure.util.OrderedIntSets;
//import edu.psu.chemxseer.structure.util.PartialOrderedIntSets;
//
//public class Status_SupSearchOpt implements ICoverStatus_Swap, ICoverStatus_Swap_InvertedIndex{
//	// For each of the database graph, the uncovered queries
//	private int[][] uncoveredItems;
//	private int[] uncoveredItemSize;
//	private ICoverStatus_Swap status;
//
//	public Status_SupSearchOpt(ICoverStatus_Swap status){
//		// construct the uncoveredItems
//		int gSize = status.getGCount();
//		this.uncoveredItems = new int[gSize][];
//		this.uncoveredItemSize = new int[gSize];
//		Arrays.fill(uncoveredItemSize, 0);
//		this.status = status;
//	}
//	
//	private void construct(ICoverStatus_Swap status){
//		for(int gIndex = 0; gIndex< uncoveredItemSize.length; gIndex++){
//			uncoveredItems[gIndex] = status.getUnCoveredItemForGID(gIndex);	
//			uncoveredItemSize[gIndex] = uncoveredItems[gIndex].length;
//		}
//	}
//
//
//	/**
//	 * Assume the noPrunable are sorted as well
//	 * @param status
//	 * @param noPrunable
//	 */
//	public Status_SupSearchOpt(ICoverStatus_Swap status, int[][] noPrunable){
//		// construct the uncoveredItems
//		int gSize = status.getGCount();
//		this.uncoveredItems = new int[gSize][];
//		for(int gIndex = 0; gIndex< gSize; gIndex++){
//			uncoveredItems[gIndex] = OrderedIntSets.remove(status.getUnCoveredItemForGID(gIndex), noPrunable[gIndex]);	
//			uncoveredItemSize[gIndex ] = uncoveredItems[gIndex].length;
//		}
//		this.status = status;
//	}
//
//	public int[] getUnCoveredItems(int gID){
//		return this.uncoveredItems[gID];
//	}
//
//	/**
//	 * After this update, the unCoveredItems[qID] is not sorted anymore
//	 * @param qID
//	 * @param position: the position of newly covered items
//	 * @return
//	 */
//	public void removeUnCoveredItems(int gID, int[] position){
//		for(int i = position.length-1; i >=0; i--){
//			int pos = position[i];
//			int newPos = --uncoveredItemSize[gID];
//			uncoveredItems[gID][pos] = uncoveredItems[gID][newPos];
//		}
//	}
//
//	/**
//	 * After this update, the unCoveredItems[qID] is not sorted anymore
//	 * @param qID
//	 * @param value
//	 */
//	public void appendUnCoveredItems(int gID, int[] value){
//		if(this.uncoveredItemSize[gID] + value.length > uncoveredItems[gID].length){
//			uncoveredItems[gID] = Arrays.copyOf(uncoveredItems[gID], this.uncoveredItemSize[gID] + 2 * value.length);
//		}
//		for(int i = 0; i< value.length; i++)
//			uncoveredItems[gID][uncoveredItemSize[gID]++] = value[i]; 
//	}
//
//
//	@Override
//	public int[] getOnlyCoveredSetPosIDs(ISet minSet, int exceptID) {
//		if(status instanceof ICoverStatus_Swap_InvertedIndex)
//			return ((ICoverStatus_Swap_InvertedIndex)status).getOnlyCoveredSetPosIDs(minSet, exceptID);
//		else {
//			throw new UnsupportedOperationException();
//		}
//	}
//
//
//	@Override
//	public int getUncoveredItemCount(ISet newSet) {
//		int count = 0;
//		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
//		while(it.hasNext()){
//			Item_Group group = it.next();
//			count += getUncoveredItemPositions(group).length;
//		}
//		return count;
//	}
//	
//	@Override
//	public int getUncoveredItemCount(Item_Group group) {
//		return this.getUncoveredItemPositions(group).length;
//	}
//	
//	private int[] getUncoveredItemPositions(Item_Group newGroup) {
//		int gID = newGroup.getItemA();
//		int[] noQIDs = newGroup.getNoItemsB();
//		return PartialOrderedIntSets.removeGetPosition(uncoveredItems[gID], uncoveredItemSize[gID], noQIDs);
//	}
//
//
//	@Override
//	public int getOnlyCoveredItemCount(ISet selectSet) {
//		return this.status.getOnlyCoveredItemCount(selectSet);
//	}
//
//
//	@Override
//	public int getOnlyCoveredItemCount(Item_Group group) {
//		return this.status.getOnlyCoveredItemCount(group);
//	}
//
//
//	@Override
//	public void removeFromPos(SetwithScore minSet) {
//		this.status.removeFromPos(minSet);
//		Iterator<Item_Group> it = ((Set_Pair) minSet.getSet()).getItemGroupIterator();
//		while(it.hasNext()){
//			Item_Group group = it.next();
//			this.appendUnCoveredItems(group.getItemA(), this.getUncoveredItemValues(group));
//		}
//	}
//
//	
//	// US the brute force algorithm
//	private int[] getUncoveredItemValues(Item_Group oldGroup) {
//		List<Integer> result = new ArrayList<Integer>();
//		for(int[] item: oldGroup){
//			if(!this.status.isCovered(item[0], item[1]))
//				result.add(item[0]);
//		}
//		int[] finalResult = new int[result.size()];
//		for(int i = 0; i< finalResult.length; i++)
//			finalResult[i] = result.get(i);
//		return finalResult;
//		
//	}
//
//	@Override
//	public void addToPos(ISet newSet, int pos) {
//		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
//		while(it.hasNext()){
//			Item_Group group = it.next();
//			this.removeUnCoveredItems(group.getItemA(), getUncoveredItemPositions(group));
//		}
//		this.status.addToPos(newSet, pos);
//	}
//	
//	@Override
//	public void addToPos(ISet[] newSets){
//		status.addToPos(newSets);
//		this.construct(status);
//	}
//
//
//	@Override
//	public int getCoveredItemCount() {
//		int gCount = status.getGCount();
//		int result = 0;
//		for(int qID = 0; qID < this.uncoveredItemSize.length; qID++)
//			result += gCount-uncoveredItemSize[qID];
//		return result;
//	}
//
//
//
//	@Override
//	public void clear() {
//		this.status.clear();
//		Arrays.fill(this.uncoveredItemSize, 0);
//	}
//
//
//	@Override
//	public int getCoveredItemCountForQ(int qID) {
//		return this.status.getCoveredItemCountForG(qID);
//	}
//
//
//	@Override
//	public int getCoveredItemCountForG(int gID) {
//		// Different implementation
//		return this.status.getQCount()-this.uncoveredItemSize[gID];
//	}
//
//
//	@Override
//	public boolean isCovered(int qID, int gID) {
//		return this.status.isCovered(qID, gID);
//	}
//
//
//	@Override
//	public int getQCount() {
//		return this.status.getQCount();
//	}
//
//
//	@Override
//	public int getGCount() {
//		return this.status.getGCount();
//	}
//
//
//	@Override
//	public int[] getUnCoveredItemForQID(int qIndex) {
//		return status.getUnCoveredItemForGID(qIndex);
//	}
//
//
//	@Override
//	public int[] getUnCoveredItemForGID(int gIndex) {
//		// Simple implementation
//		return Arrays.copyOf(uncoveredItems[gIndex], uncoveredItemSize[gIndex]);
//	}
// }
