package edu.psu.chemxseer.structure.setcover.update;

import java.util.List;
import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class SubSearch_LindexSimpleUpdatable_TCFG extends
		SubSearch_LindexSimpleUpdatable {
	protected double minSupport; // The minimum support of frequent-subgraph
									// queries

	public SubSearch_LindexSimpleUpdatable_TCFG(
			SubSearch_LindexSimpleUpdatable subSearch_LindexUpdatable,
			double minSupport) {
		super(subSearch_LindexUpdatable);
		this.minSupport = minSupport;
	}

	@Override
	/**
	 * The difference is that: 
	 * 1. The index omit its on-disk part, and only have the in-memory part
	 * 2. Algorithm is triggered to update the in-memory index
	 * 3. For frequent-subgraph queries, because we assume that they can be answered by directly looking update the 
	 * on-disk index, so here, we omit the verification cost. 
	 * Also, we does not include them into the training queries, since they are 100% covered. 
	 */
	public List<IGraphResult> getAnswer(Graph query, long[] TimeComponent,
			int[] Number, boolean recordQuery) {
		// First look for g's subgraphs
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = 0;
		List<IGraphResult> answer = null;
		List<Integer> maxSubgraphs = indexSearcher.maxSubgraphs(query,
				TimeComponent);
		// TODO: all Subgraphs can be obtained by tracing back the subgraphs
		// instead of running the subgraph isomorphism test again.
		List<Integer> allSubgraphs = indexSearcher.subgraphs(query,
				TimeComponent);
		if (maxSubgraphs != null && maxSubgraphs.get(0) == -1) {// graph g hits
																// on one of the
																// index term
			IGraphFetcher answerFetcher = this.postingFetcher.getPosting(
					maxSubgraphs.get(1), TimeComponent);
			answer = answerFetcher.getAllGraphs(TimeComponent);
			Number[0] = 0;
		} else {
			IGraphFetcher candidateFetcher = this.postingFetcher.getJoin(
					maxSubgraphs, TimeComponent);
			Number[0] = candidateFetcher.size();
			answer = this.verifier.verify(query, candidateFetcher, true,
					TimeComponent);
		}
		Number[1] = answer.size();
		// 1. For frequent subgraph queries, its verification cost is set to be
		// zero
		double support = minSupport * this.postingFetcher.getDBSize();
		if (Number[1] > support) {
			TimeComponent[3] = 0;
		}
		// 2. Only store infrequent-subgraphs queries. Frequent-subgraph queries
		// will be answered directly
		if (recordQuery == true && Number[1] < support) {
			if (queryCapacity == queries.size()) {
				queryFeatureContain.poll();
				queryFeatureEqual.poll();
				this.queries.poll();
				this.qGCount.poll();
			}
			if (maxSubgraphs.get(0) == -1) {
				int tID = maxSubgraphs.get(1);
				queryFeatureContain.offer(this.indexUpdator.getParents(tID));
				queryFeatureEqual.offer(tID);
			} else {
				queryFeatureContain.offer(OrderedIntSets.toArray(allSubgraphs));
				queryFeatureEqual.offer(-1);
			}
			this.queries.offer(querySerializer.serialize(query));
			qGCount.offer(answer.size());
			// float ratio = ((float)(Number[0]+1))/((float)(Number[1] + 1));
		}
		return answer;
	}

}
