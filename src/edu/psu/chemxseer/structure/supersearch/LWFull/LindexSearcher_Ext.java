package edu.psu.chemxseer.structure.supersearch.LWFull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.CanonicalDFS;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;

/**
 * LindexSearch to satisfy the need for Supergraph Search
 * 
 * @author dayuyuan
 * 
 */
public class LindexSearcher_Ext extends LindexSearcher {

	public LindexSearcher_Ext(LindexTerm[] indexTerms, LindexTerm dummyHead) {
		super(indexTerms, dummyHead);
	}

	public LindexSearcher_Ext(LindexSearcher construct) {
		super(construct);
	}

	public List<Integer> minNoSubgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();

		CanonicalDFS dfsParser = MyFactory.getDFSCoder();
		LindexTerm[] seeds = this.dummyHead.getChildren();
		short[] status = new short[super.indexTerms.length];
		Arrays.fill(status, (short) 0);

		for (LindexTerm seed : seeds) {
			Graph seedGraph = dfsParser.parse(seed.getExtension(),
					MyFactory.getGraphFactory());
			FastSUCompleteEmbedding fastSuExt = null;
			if (gComparator.compare(seedGraph, query) <= 0)
				fastSuExt = new FastSUCompleteEmbedding(seedGraph, query);

			if (fastSuExt != null && fastSuExt.isIsomorphic()) {
				// Mark seed's children as minNoSubgraphs
				LindexTerm[] children = seed.getChildren();
				for (LindexTerm child : children)
					if (status[child.getId()] == 0)
						this.markAsNonSubgraphs(status, child);
			} else if (fastSuExt != null && fastSuExt.issubIsomorphic()) {// is
																			// expendable
				this.minNonSubgraphSearch(fastSuExt, seed, status);
			} else
				this.markAsNonSubgraphs(status, seed);
		}
		TimeComponent[2] += System.currentTimeMillis() - start;

		// return procedure
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < status.length; i++)
			if (status[i] == 1)
				result.add(i);
		return result;
	}

	private void minNonSubgraphSearch(FastSUCompleteEmbedding fastSu,
			LindexTerm term, short[] status) {
		LindexTerm[] children = term.getChildren();
		// No further node to grow
		if (children == null || children.length == 0) {
			return; // nothing to be added as minNonSubgraph
		} else {
			for (LindexTerm child : children) {
				if (status[child.getId()] != 0 || child.getParent() != term)
					continue;
				// else: the child term is not visited yet
				FastSUCompleteEmbedding next = new FastSUCompleteEmbedding(
						fastSu, child.getExtension());
				if (next.isIsomorphic()) {
					LindexTerm[] grandChildren = child.getChildren();
					for (LindexTerm grandChild : grandChildren)
						if (status[grandChild.getId()] == 0)
							this.markAsNonSubgraphs(status, grandChild);
				} else if (next.issubIsomorphic()) {
					minNonSubgraphSearch(next, child, status);
				} else
					this.markAsNonSubgraphs(status, child); // status[child.getID()]
															// == 0 for sure
			}
		}
	}

	private void markAsNonSubgraphs(short[] status, LindexTerm term) {
		// mark this seed as a NoSubgraphs
		status[term.getId()] = 1;
		// mark all the descendant of seed to be visited
		Collection<LindexTerm> cTerms = super.getSuperTerms(term);
		for (LindexTerm cTerm : cTerms)
			status[cTerm.getId()] = 2;
	}
}
