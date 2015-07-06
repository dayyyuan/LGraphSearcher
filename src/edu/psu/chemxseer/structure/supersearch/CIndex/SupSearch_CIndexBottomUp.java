package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

/**
 * The bottom up indexing structures of the cIndex features on the ith level
 * must be subgraphs of features on the (i-1) ith level. Given a query graph q,
 * if f not subgraph isomorphic to q, then the tree covered by f1 need not be
 * examed due to the exclusive logic
 * 
 * Pay attention: there is no computational sharing at all, just hierarchical
 * structures
 * 
 * @author dayuyuan
 * 
 */
public class SupSearch_CIndexBottomUp implements ISearcher {
	private List<SupSearch_CIndexFlat> hiIndex;

	public SupSearch_CIndexBottomUp(SupSearch_CIndexFlat basicIndex) {
		this.hiIndex = new ArrayList<SupSearch_CIndexFlat>();
		this.hiIndex.add(basicIndex);
	}

	public void addOneLevel(SupSearch_CIndexFlat oneLevelIndex) {
		this.hiIndex.add(oneLevelIndex);
	}

	@Override
	public int[][] getAnswerIDs(Graph query) {
		List<IGraphResult> answer = this.getAnswer(query, new long[4],
				new int[2]);
		int[] result = new int[answer.size()];
		List<Integer> result2 = new ArrayList<Integer>();
		int counter1 = 0;
		for (IGraphResult oneAnswer : answer) {
			if (oneAnswer.getG().getEdgeCount() == query.getEdgeCount())
				result2.add(oneAnswer.getID());
			else
				result[counter1++] = oneAnswer.getID();
		}
		int[][] finalResult = new int[2][];
		finalResult[0] = Arrays.copyOf(result, counter1);
		finalResult[1] = new int[result2.size()];
		for (int w = 0; w < result2.size(); w++)
			finalResult[1][w] = result2.get(w);
		return finalResult;
	}

	/**
	 * Given a query Graph, q find all the database graphs contained in q
	 * 
	 * @param query
	 * @param TimeComponent
	 * @param Number
	 * @return
	 */
	@Override
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number) {
		if (hiIndex.size() == 1)
			return hiIndex.get(0).getAnswer(query, TimeComponent, Number);
		else {
			long startTime = System.currentTimeMillis();
			long[] tempTime = new long[4];
			int[] tempNum = new int[2];
			int[] IDs = null;
			for (int i = hiIndex.size() - 1; i > 0; i--) {
				List<IGraphResult> upperLevelResults = null;
				if (i == hiIndex.size() - 1) // first level
					upperLevelResults = hiIndex.get(i).getAnswer(query,
							tempTime, tempNum);
				else
					// other leverl
					upperLevelResults = hiIndex.get(i).getAnswer(query, IDs,
							tempTime, tempNum);
				IDs = new int[upperLevelResults.size()];
				for (int w = 0; w < IDs.length; w++) {
					IDs[w] = upperLevelResults.get(w).getDocID();
				}
			}
			// bottom level index
			TimeComponent[2] += System.currentTimeMillis() - startTime; // all
																		// filtering
																		// time
			return hiIndex.get(0).getAnswer(query, IDs, TimeComponent, Number);
		}
	}
}
