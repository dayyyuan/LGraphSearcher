package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex1;

/**
 * In-memory IGI and On-Disk IGI
 * 
 * @author duy113
 * 
 */
public class FGindex {
	IIndex1 indexSearcher;
	IPostingFetcher postingFetcher;
	EdgeIndex edgeIndex;

	public FGindex(FGindexSearcher searcher, IPostingFetcher in_memory_postings) {
		this.indexSearcher = searcher;
		this.edgeIndex = searcher.getEdgeIndex();
		this.postingFetcher = in_memory_postings;
	}

	/**
	 * Given the query graph, if it hits on any of the indexing feature return
	 * the postings directly without isomorphism test
	 * 
	 * @param query
	 * @param TimeComponent
	 *            (return)
	 * @return the postings or null
	 */
	public List<IGraphResult> hitAndReturn(Graph query, int[] hitIndex,
			long[] TimeComponent) {
		boolean[] exactMatch = new boolean[1];
		exactMatch[0] = false;
		hitIndex[0] = indexSearcher.designedSubgraph(query, exactMatch,
				TimeComponent);
		if (hitIndex[0] == -1)
			return null;
		else if (exactMatch[0])
			return postingFetcher.getPosting(hitIndex[0], TimeComponent)
					.getAllGraphs(TimeComponent);
		else
			return null;
	}

	public List<IGraphResult> hitAndReturn(Graph query, int onDiskIndexID,
			int[] hitIndex, long[] TimeComponent) {
		boolean[] exactMatch = new boolean[1];
		exactMatch[0] = false;
		hitIndex[0] = indexSearcher.designedSubgraph(query, exactMatch,
				TimeComponent);
		if (hitIndex[0] == -1)
			return null;
		else if (exactMatch[0])
			return postingFetcher.getPosting(onDiskIndexID + "_" + hitIndex[0],
					TimeComponent).getAllGraphs(TimeComponent);
		else
			return null;
	}

	/**
	 * Given the query graph, find all the candidate graphs by join operation of
	 * all the maximal subgraph features
	 * 
	 * @param query
	 * @param TimeComponent
	 * @return
	 */
	public IGraphFetcher candidateByFeatureJoin(Graph query,
			long[] TimeComponent) {
		List<Integer> features = indexSearcher.maxSubgraphs(query,
				TimeComponent);
		if (features == null || features.size() == 0)
			return null;
		else
			return postingFetcher.getJoin(features, TimeComponent);
	}

	/**
	 * Given the query graph, find all the candidate graph by join operation of
	 * the infrequent edges
	 * 
	 * @param query
	 * @param TimeComponent
	 * @return
	 */
	public IGraphFetcher candidateByEdgeJoin(Graph query, long[] TimeComponent) {
		return edgeIndex.getInfrequentEdgeCandidates(query, TimeComponent);
	}

	public EdgeIndex getEdgeIndex() {
		return this.edgeIndex;
	}
}
