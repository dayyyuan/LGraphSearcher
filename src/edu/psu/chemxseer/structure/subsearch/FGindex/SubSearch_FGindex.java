package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

public class SubSearch_FGindex implements ISearcher {
	private FGindex in_memoryIndex;
	private VerifierISO verifier;
	private IPostingFetcher onDiskPostingFetcher;
	private String baseName;

	public SubSearch_FGindex(FGindex in_memoryIndex, VerifierISO verifier,
			PostingFetcherLucene onDiskPostings, String baseName) {
		this.in_memoryIndex = in_memoryIndex;
		this.verifier = verifier;
		this.onDiskPostingFetcher = onDiskPostings;
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
		List<IGraphResult> answers = null;

		int[] hitIndex = new int[1];
		hitIndex[0] = -1;
		// 1. In-memory Index lookup
		answers = in_memoryIndex.hitAndReturn(query, hitIndex, TimeComponent);
		if (answers != null) {
			Number[1] = answers.size();
			return answers; // find a hit and return
		}
		// 2. Load the on-disk index
		if (hitIndex[0] >= 0) {
			int onDiskIndexID = hitIndex[0];
			FGindex on_diskIndex = null;
			try {
				on_diskIndex = loadOndiskIndex(hitIndex[0], TimeComponent);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (on_diskIndex != null) {
				hitIndex[0] = -1;
				answers = on_diskIndex.hitAndReturn(query, onDiskIndexID,
						hitIndex, TimeComponent);
				if (hitIndex[0] >= 0) {
					Number[1] = answers.size();
					return answers;
				}
			}
		}
		// 3. Filtering + verification
		IGraphFetcher candidateFetcher;
		IGraphFetcher r1 = in_memoryIndex.candidateByFeatureJoin(query,
				TimeComponent);
		IGraphFetcher r2 = in_memoryIndex.candidateByEdgeJoin(query,
				TimeComponent);
		if (r1 == null || r1.size() == 0)
			candidateFetcher = r2;
		else if (r2 == null || r2.size() == 0)
			candidateFetcher = r1;
		else {
			candidateFetcher = r1.join(r2);
		}
		Number[0] = candidateFetcher.size();
		answers = verifier.verify(query, candidateFetcher, true, TimeComponent);
		Number[1] = answers.size();
		return answers;
	}

	/**
	 * Load the onDisk index, counted as the index loopup time
	 * 
	 * @param TCFGId
	 * @param TimeComponent
	 * @return
	 * @throws IOException
	 */
	private FGindex loadOndiskIndex(int TCFGId, long[] TimeComponent)
			throws IOException {
		long start = System.currentTimeMillis();
		FGindexSearcher searcher = FGindexConstructor.loadSearcher(baseName,
				getOnDiskIndexName(TCFGId), null); // empty graphdatabase
		FGindex onDiskIGI = new FGindex(searcher, onDiskPostingFetcher);
		TimeComponent[2] += System.currentTimeMillis() - start;
		return onDiskIGI;
	}

	/********** This part will be replace to configuration file latter ***************/
	private static String onDiskBase = "onDiskIndex/";

	public static String getOnDiskIndexName(int TCFGID) {
		return onDiskBase + TCFGID;
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

	public EdgeIndex getEdgeIndex() {
		return this.in_memoryIndex.getEdgeIndex();
	}

}
