package edu.psu.chemxseer.structure.setcover.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class ClassificationBB implements IBranchBound {
	// The G & Q segmentations is build & updated when a new pattern is selected
	// There are cases that one segments is empty: size = 0, empty segments are
	// not put in Gsegments or Qsegments
	private List<int[]> Gsegments;
	private List<int[]> Qsegments;
	private int qCount;
	private int gCount;

	/**
	 * Construct a Classification BB
	 * 
	 * @param qCount
	 * @param GCount
	 */
	public ClassificationBB(int qCount, int gCount) {
		Gsegments = new ArrayList<int[]>();
		Qsegments = new ArrayList<int[]>();
		this.qCount = qCount;
		this.gCount = gCount;
	}

	@Override
	public void updateAfterInsert(ISet notCare, ICoverStatus_Swap status) {

		Gsegments.clear();
		Qsegments.clear();

		boolean[] testedG = new boolean[gCount];
		Arrays.fill(testedG, false);
		boolean[] testedQ = new boolean[qCount];
		Arrays.fill(testedQ, false);
		// Getting slower & slower, because I need to find the bound
		for (int gID = 0; gID < gCount; gID++) {
			if (testedG[gID])
				continue;
			else {
				int[] qIDs = findQ(gID, testedQ, status);
				if (qIDs.length != 0) {
					int[] gIDs = findG(qIDs[0], testedG, status);
					Gsegments.add(gIDs);
					Qsegments.add(qIDs);
					for (int id : gIDs)
						testedG[id] = true;
					for (int id : qIDs)
						testedQ[id] = true;
				} else
					testedG[gID] = true;
			}
		}
	}

	/**
	 * Given the gID, find all the <q,g>pair such that <q, g>is not covered yet
	 * Skip the query q if testedQ[q] = true, since testedQ[q] = true means that
	 * this query if added to one segments already
	 * 
	 * @param gID
	 * @param testedQ
	 * @return
	 */
	private int[] findQ(int gID, boolean[] testedQ, ICoverStatus_Swap index) {
		List<Integer> result = new ArrayList<Integer>();
		for (int qID = 0; qID < testedQ.length; qID++)
			if ((!testedQ[qID])
					&& ((ICoverStatus_SwapInternal) index).isCovered(qID, gID))
				result.add(qID); // auto boxing
		int[] answer = new int[result.size()];
		for (int i = 0; i < answer.length; i++)
			answer[i] = result.get(i);
		return answer;
	}

	private int[] findG(int qID, boolean[] testedG, ICoverStatus_Swap index) {
		List<Integer> result = new ArrayList<Integer>();
		for (int gID = 0; gID < testedG.length; gID++)
			if ((!testedG[gID])
					&& ((ICoverStatus_SwapInternal) index).isCovered(qID, gID))
				result.add(gID);
		int[] answer = new int[result.size()];
		for (int i = 0; i < answer.length; i++)
			answer[i] = result.get(i);
		return answer;
	}

	/**
	 * Given to the G(p) and Q(p) Return the upper bound for classification
	 * 
	 * @param Qp
	 * @param Gp
	 * @return
	 */
	@Override
	public boolean isUppBoundSmaller(ISet set, double threshold) {
		if (!(set instanceof Set_Pair))
			throw new ClassCastException();

		int[] Gp = ((Set_Pair) set).getValueG();
		int[] Qp = ((Set_Pair) set).getValueQ();
		long score = 0;
		int size = Gsegments.size();
		for (int i = 0; i < size; i++) {
			int[] Gi = Gsegments.get(i);
			int[] Qi = Qsegments.get(i);
			int Gip = OrderedIntSets.getJoinSize(Gi, Gp);
			int Qip = OrderedIntSets.getJoinSize(Qi, Qp);
			score += Math.max(Gip * Qi.length, Qip & Gi.length);
			if (score > threshold)
				return false;
		}
		return true;
	}

	@Override
	public void updateAfterDelete(ISet oldSet, ICoverStatus_Swap status) {
		// TODO Auto-generated method stub

	}

	// @Override
	public long getCoveredCount() {
		long score = 0;
		for (int i = 0; i < this.Gsegments.size(); i++) {
			score += Gsegments.get(i).length * Qsegments.get(i).length;
		}
		return score;
	}

	@Override
	public void clear() {
		Gsegments.clear();
		Qsegments.clear();
	}

	@Override
	public void initialize(ICoverStatus_Swap status) {
		this.updateAfterInsert(null, status);
	}
}
