package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex2;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

public class SubSearch_LindexSimplePlus implements ISearcher {
	private IIndex2 indexSearcher;
	private IPostingFetcher in_memFetcher;
	private VerifierISO verifier;

	private IPostingFetcher on_diskFetcher;
	private String baseName;

	public SubSearch_LindexSimplePlus(IIndex2 indexSearcher,
			IPostingFetcher in_memFetcher, IPostingFetcher on_diskFetcher,
			VerifierISO verifier, String baseName) {
		this.indexSearcher = indexSearcher;
		this.in_memFetcher = in_memFetcher;
		this.on_diskFetcher = on_diskFetcher;
		this.verifier = verifier;
		this.baseName = baseName;
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
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = 0;

		List<IGraphResult> answer = null;
		List<Integer> maxSubgraphs = this.indexSearcher.maxSubgraphs(query,
				TimeComponent);
		// In Memory Hit
		if (maxSubgraphs != null && maxSubgraphs.get(0) == -1) {
			IGraphFetcher answerFetcher = this.in_memFetcher.getPosting(
					maxSubgraphs.get(1), TimeComponent);
			answer = answerFetcher.getAllGraphs(TimeComponent);
			Number[0] = 0;
		} else {
			// Decide whether to load on-disk Lindex
			boolean loadOnDisk = true;
			boolean onDisk = false;
			List<Integer> onDiskMaxSubs = null;
			int[] maximumSub = new int[1];
			maximumSub[0] = -1;

			if (loadOnDisk) {
				// Find the maximum subgraph
				FastSUCompleteEmbedding fastSu = this.indexSearcher
						.designedSubgraph(query, maxSubgraphs, maximumSub,
								TimeComponent);
				// Load the on-disk index
				LindexSearcher on_diskIndex = this.loadOndiskIndex(
						maximumSub[0], TimeComponent);
				if (on_diskIndex != null) {
					onDiskMaxSubs = on_diskIndex.maxSubgraphs(fastSu,
							TimeComponent);
					if (onDiskMaxSubs != null & onDiskMaxSubs.size() > 0
							&& onDiskMaxSubs.get(0) == -1) {
						onDisk = true;
						IGraphFetcher answerFetcher = this.on_diskFetcher
								.getPosting(
										maximumSub[0] + "_"
												+ onDiskMaxSubs.get(1),
										TimeComponent);
						answer = answerFetcher.getAllGraphs(TimeComponent);
						Number[0] = 0;
					}
				}
			}
			if (!loadOnDisk || !onDisk) {
				IGraphFetcher candidateFetcher = this.in_memFetcher.getJoin(
						maxSubgraphs, TimeComponent);
				Number[0] = candidateFetcher.size();
				answer = this.verifier.verify(query, candidateFetcher, true,
						TimeComponent);

			}
		}
		Number[1] = answer.size();
		return answer;
	}

	/**
	 * Load the on disk index, TimeComponent[2], index loopup time
	 * 
	 * @param in_memoryFeatureID
	 * @param TimeComponent
	 * @return
	 */
	private LindexSearcher loadOndiskIndex(int in_memoryFeatureID,
			long[] TimeComponent) {
		long start = System.currentTimeMillis();
		LindexSearcher on_diskIndex = null;
		try {
			on_diskIndex = LindexConstructor.loadSearcher(baseName,
					getOnDiskIndexName(in_memoryFeatureID));
		} catch (IOException e) {
			e.printStackTrace();
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		return on_diskIndex;
	}

	/********** This part will be replace to configuration file latter ***************/
	private static String onDiskBase = "onDiskIndex/";

	public static String getOnDiskIndexName(int id) {
		return onDiskBase + id;
	}

	public static String getLuceneName() {
		return "lucene";
	}

	public static String getIn_MemoryIndexName() {
		return "in_memory_index";
	}

	public static String getOnDiskLuceneName() {
		return "onDiskLucene";
	}

	public static String getOnDiskFolderName() {
		return onDiskBase;
	}
}
