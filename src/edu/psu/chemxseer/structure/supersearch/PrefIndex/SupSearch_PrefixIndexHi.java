package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

/**
 * Hierarchical Prefix Index
 * 
 * @author dayuyuan
 * 
 */
public class SupSearch_PrefixIndexHi implements ISearcher {
	private List<SupSearch_PrefixIndex> indexes;

	/**
	 * The upper-most index is built & added in to the hierarchy first
	 */
	public SupSearch_PrefixIndexHi() {
		// dummy index constructor
		this.indexes = new ArrayList<SupSearch_PrefixIndex>();
	}

	/**
	 * Add on Index into the Indexes Array Pay attention: the new
	 * SupSearch_PrefixIndex should be consistent with the upper-level indexes
	 * 
	 * @param index
	 * @return
	 */
	public boolean addIndexLow(SupSearch_PrefixIndex index) {
		if (indexes.size() == 0) {
			this.indexes.add(index);
			return true;
		} else {
			if (indexes.get(indexes.size() - 1).lowerLevelSearcher
					.equals(index.searcher)) {
				this.indexes.add(index);
				return true;
			} else {
				System.out.println("Exception: Wrong Insertion of the Index");
			}
		}
		return false;
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

	@Override
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number) {
		return this.indexes.get(indexes.size() - 1).getAnswer(query,
				TimeComponent, Number);
	}
}
