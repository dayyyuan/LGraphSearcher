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
//import edu.psu.chemxseer.structure.util.PartialOrderedIntSets;
//
///**
// * The status optimized 
// * @author dayuyuan
// *
// */
//public class Status_SubSearchOpt implements ICoverStatus_Swap, ICoverStatus_Swap_InvertedIndex{
//	// For each query qID, the status-wrapper maintain a list of un-filtered graphs
//	// Those graphs does not included qID's supergraphs. 
//	// The length of qUnCovered is associated with qCoveredCount in the SubSearchBB
//	private int[][] uncoveredItems;
//	private int[] uncoveredItemSize;
//	private ICoverStatus_Swap status;
//	
//	public Status_SubSearchOpt(ICoverStatus_Swap status){
//		// construct the uncoveredItems
//		int qSize = status.getQCount();
//		this.status = status;
//		this.uncoveredItems = new int[qSize][];
//		this.uncoveredItemSize = new int[qSize];
//		Arrays.fill(uncoveredItemSize, 0);
//	}
//	
//	private void construct(ICoverStatus_Swap status){
//		for(int qIndex = 0; qIndex < uncoveredItemSize.length; qIndex++){
//			uncoveredItems[qIndex] = status.getUnOnlyCoveredItemForQID(qIndex)[0];
//			uncoveredItemSize[qIndex ] = uncoveredItems[qIndex].length;
//		}
//	}
//	
//	/**
//	 * Assume the noPrunable are sorted as well
//	 * @param status
//	 * @param noPrunable
//	 */
//	/*public Status_SubSearchOpt(ICoverStatus_Swap status, int[][] noPrunable){
//		int qSize = status.getQCount();
//		this.status = status;
//		this.uncoveredItems = new int[qSize][];
//		this.uncoveredItemSize = new int[qSize];
//		for(int qIndex = 0; qIndex < qSize; qIndex++){
//			uncoveredItems[qIndex] = OrderedIntSets.remove(status.getUnCoveredItemForQID(qIndex),
//					noPrunable[qIndex]);
//			uncoveredItemSize[qIndex] = uncoveredItems[qIndex].length;
//		}
//	}*/
//	
//	/**
//	 * After this update, the unCoveredItems[qID] is not sorted anymore
//	 * @param qID
//	 * @param position: the position of newly covered items
//	 * @return
//	 */
//	private void removeUnCoveredItems(int qID, int[] position){
//		for(int i = position.length-1; i >=0; i--){
//			int pos = position[i];
//			int newPos = --uncoveredItemSize[qID];
//			uncoveredItems[qID][pos] = uncoveredItems[qID][newPos];
//		}
//	}
//	
//	/**
//	 * After this update, the unCoveredItems[qID] is not sorted anymore
//	 * @param qID
//	 * @param value
//	 */
//	private void appendUnCoveredItems(int qID, int[] value){
//		if(this.uncoveredItemSize[qID] + value.length > uncoveredItems[qID].length){
//			uncoveredItems[qID] = Arrays.copyOf(uncoveredItems[qID], this.uncoveredItemSize[qID] + 2 * value.length);
//		}
//		for(int i = 0; i< value.length; i++)
//			uncoveredItems[qID][uncoveredItemSize[qID]++] = value[i]; 
//	}
//
//	@Override
//	public int getUncoveredItemCount(ISet newSet) {
//		int count = 0;
//		Iterator<Item_Group> it = ((Set_Pair) newSet).getItemGroupIterator();
//		while(it.hasNext()){
//			Item_Group group = it.next();
//			count += getUncoveredItemPositions(group).length;
//		}
//		// also consider the equal cases
//		int[] equal = ((Set_Pair)newSet).getValueQEqual();
//		for(int qID :equal)
//			count += uncoveredItemSize[qID];
//		return count;
//	}
//	
//	public int getUncoveredItemCount(Item_Group newGroup){
//		return this.getUncoveredItemPositions(newGroup).length;
//	}
//	
//	private int[] getUncoveredItemPositions(Item_Group newGroup) {
//		int qID = newGroup.getItemA();
//		int[] noGIDs = newGroup.getNoItemsB();
//		return PartialOrderedIntSets.removeGetPosition(uncoveredItems[qID], uncoveredItemSize[qID], noGIDs);
//	}
//	// USE the brute force algorithm
//	private int[] getUncoveredItemValues(Item_Group oldGroup) {
//		int[] temp = new int[status.getGCount()-oldGroup.getNoItemsB().length];
//		int index = 0;
//		for(int[] item: oldGroup){
//			if(!this.status.isCovered(item[0], item[1]))
//				temp[index++] = item[1];
//		}
//		return Arrays.copyOf(temp, index);
//		
//	}
//
//	@Override
//	public int getOnlyCoveredItemCount(ISet selectSet) {
//		return this.status.getOnlyCoveredItemCount(selectSet);
//	}
//
//	@Override
//	public int getOnlyCoveredItemCount(Item_Group group) {
//		return this.status.getOnlyCoveredItemCount(group);
//	}
//
//	@Override
//	public void removeFromPos(SetwithScore minSet) {
//		this.status.removeFromPos(minSet);
//		Set_Pair set = (Set_Pair) minSet.getSet();
//		Iterator<Item_Group> it = set.getItemGroupIterator();
//		while(it.hasNext()){
//			Item_Group group = it.next();
//			this.appendUnCoveredItems(group.getItemA(), this.getUncoveredItemValues(group));
//		}
//		int[] equal = set.getValueQEqual();
//		for(int qID: equal){
//			uncoveredItems[qID] = status.getUnOnlyCoveredItemForQID(qID)[0];
//			uncoveredItemSize[qID] = uncoveredItems[qID].length;
//		}
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
//		// also consider the equal cases
//		int[] equal = ((Set_Pair)newSet).getValueQEqual();
//		for(int qID :equal)
//			this.uncoveredItemSize[qID] = 0;
//		
//		this.status.addToPos(newSet, pos);
//	}
//	
//	@Override
//	public void addToPos(ISet[] newSets){
//		status.addToPos(newSets);
//		this.construct(status);
//	}
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
//	@Override
//	public void clear() {
//		this.status.clear();
//		Arrays.fill(this.uncoveredItemSize, 0);
//	}
//
//	@Override
//	public int getCoveredItemCountForQ(int qID) {
//		// Different implementation
//		return this.status.getGCount()-this.uncoveredItemSize[qID];
//	}
//
//	@Override
//	public int getCoveredItemCountForG(int gID) {
//		return this.status.getCoveredItemCountForG(gID);
//	}
//
//	@Override
//	public boolean isCovered(int qID, int gID) {
//		return this.status.isCovered(qID, gID);
//	}
//
//	@Override
//	public int getQCount() {
//		return this.status.getQCount();
//	}
//
//	@Override
//	public int getGCount() {
//		return this.status.getGCount();
//	}
//
//	@Override
//	public int[][] getUnOnlyCoveredItemForQID(int qIndex) {
//		// Simple implementation
//		return Arrays.copyOf(uncoveredItems[qIndex], uncoveredItemSize[qIndex]);
//	}
//
//	@Override
//	public int[][] getUnOnlyCoveredItemForGID(int gIndex) {
//		return status.getUnCoveredItemForGID(gIndex);
//	}
//
//	@Override
//	public int[] getOnlyCoveredSetPosIDs(ISet minSet, int exceptID) {
//		if(status instanceof ICoverStatus_Swap_InvertedIndex)
//			return ((ICoverStatus_Swap_InvertedIndex)status).getOnlyCoveredSetPosIDs(minSet, exceptID);
//		else {
//			throw new UnsupportedOperationException();
//		}
//	}
//}
//	
//
