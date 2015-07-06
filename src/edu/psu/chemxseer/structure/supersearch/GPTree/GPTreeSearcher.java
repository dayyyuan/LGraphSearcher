package edu.psu.chemxseer.structure.supersearch.GPTree;

import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.PrefixIndexSearcher;

/**
 * One Wrapper Class of the IndexSearcherPrefix: Since the concept of the
 * GPTreeSearcher is greatlly similar to that of PrefixIndex
 * 
 * @author dayuyuan
 * 
 */
public class GPTreeSearcher implements IIndexPrefix {

	private PrefixIndexSearcher internalSearcher;

	public GPTreeSearcher(PrefixIndexSearcher realSearcher) {
		this.internalSearcher = realSearcher;
	}

	@Override
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		return internalSearcher.subgraphs(query, TimeComponent);
	}

	@Override
	public FastSUCompleteEmbedding getEmbedding(int fID, Graph query) {
		return internalSearcher.getEmbedding(fID, query);
	}

	@Override
	public int[][] getTotalLabel(int fID) {
		return internalSearcher.getTotalLabel(fID);
	}

	@Override
	public int[][] getExtension(int gID) {
		return internalSearcher.getExtension(gID);
	}

	@Override
	public int getPrefixID(Graph g) {
		return internalSearcher.getPrefixID(g);
	}

	@Override
	public int getPrefixID(int fID) {
		return internalSearcher.getPrefixID(fID);
	}

	@Override
	public int[] getAllFeatureIDs() {
		return this.internalSearcher.getAllFeatureIDs();
	}

	@Override
	public int getPrefixGain(int id) {
		return this.internalSearcher.getPrefixGain(id);
	}

}
