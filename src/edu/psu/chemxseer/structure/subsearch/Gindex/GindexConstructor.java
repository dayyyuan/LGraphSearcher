package edu.psu.chemxseer.structure.subsearch.Gindex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.OrderedIntSet;

public class GindexConstructor {

	private Map<Integer, int[]> postings;

	/**
	 * For Index Construction, all features are stored into the index Posting
	 * Stored in memory Only selected features are marked "discrminative"
	 * 
	 * @param allFeatures
	 * @param exhaustSearch
	 */
	public GindexSearcher constructWithPostings(PostingFeatures rawFeatures,
			boolean exhaustSearch) {
		int maxEdgeNum = 0;
		NoPostingFeatures<IOneFeature> allFeatures = rawFeatures.getFeatures();
		boolean[] discriminative = new boolean[allFeatures.getfeatureNum()];
		for (int i = 0; i < discriminative.length; i++)
			discriminative[i] = false;

		this.postings = new HashMap<Integer, int[]>();
		HashMap<String, Integer> gHash = new HashMap<String, Integer>();
		for (int i = 0; i < allFeatures.getfeatureNum(); i++) {
			IOneFeature feature = allFeatures.getFeature(i);
			// if(feature.getFeatureId() != i)
			// System.out.println("Excpetion: Inconsistent in GindexM: the featureID and it position does not match");
			// Put all patterns in to the hashtable, some of the white node will
			// be pruned latter
			String gString = MyFactory.getDFSCoder().serialize(
					feature.getFeatureGraph());
			gHash.put(gString, i);
			if (feature.isSelected()) {
				discriminative[i] = true;
				if (maxEdgeNum < feature.getFeatureGraph().getEdgeCount())
					maxEdgeNum = feature.getFeatureGraph().getEdgeCount();
				// Populate the posting File for selected features only
				postings.put(i, rawFeatures.getPosting(feature));
			}
		}
		return new GindexSearcher(gHash, discriminative, maxEdgeNum,
				exhaustSearch);
	}

	/**
	 * No posting stored construction of GindexSearcher
	 * 
	 * @param allFeatures
	 * @param exhaustSearch
	 */
	public static GindexSearcher construct(
			NoPostingFeatures<IOneFeature> allFeatures, boolean exhaustSearch) {
		int maxEdgeNum = 0;
		boolean[] discriminative = new boolean[allFeatures.getfeatureNum()];
		for (int i = 0; i < discriminative.length; i++)
			discriminative[i] = false;

		HashMap<String, Integer> gHash = new HashMap<String, Integer>();
		for (int i = 0; i < allFeatures.getfeatureNum(); i++) {
			IOneFeature feature = allFeatures.getFeature(i);
			// if(feature.getFeatureId() != i)
			// System.out.println("Excpetion: Inconsistent in GindexM: the featureID and it position does not match");
			// Put all patterns in to the hashtable, some of the white node will
			// be pruned latter
			String gString = MyFactory.getDFSCoder().serialize(
					feature.getFeatureGraph());
			gHash.put(gString, i);
			if (feature.isSelected()) {
				discriminative[i] = true;
				if (maxEdgeNum < feature.getFeatureGraph().getEdgeCount())
					maxEdgeNum = feature.getFeatureGraph().getEdgeCount();
			}
		}
		return new GindexSearcher(gHash, discriminative, maxEdgeNum,
				exhaustSearch);
	}

	public int[] getCandidatesBuild(GindexSearcher searcher,
			PostingFeatures candidateFeatures, IOneFeature feature) {
		Graph g = feature.getFeatureGraph();
		if (g == null)
			g = MyFactory.getDFSCoder().parse(feature.getDFSCode(),
					MyFactory.getGraphFactory());
		OrderedIntSet candidates = new OrderedIntSet();

		List<Integer> subIndices = searcher.maxSubgraphs(g, new long[4]);

		if (subIndices.size() > 1) {
			candidates
					.add(this.getPosting(candidateFeatures, subIndices.get(0)));
			for (int i = 1; i < subIndices.size(); i++) {
				int index = subIndices.get(i);
				int[] postings = this.getPosting(candidateFeatures, index);
				candidates.join(postings);
			}
		} else if (subIndices.size() == 1)
			candidates
					.add(this.getPosting(candidateFeatures, subIndices.get(0)));

		if (candidates.size() < feature.getFrequency())
			System.out.println("Error: in GindexM: getCandidateBuild: "
					+ candidates.size());
		return candidates.getItems();
	}

	private int[] getPosting(PostingFeatures candidateFeatures, int fID) {
		if (this.postings == null) {
			return candidateFeatures.getPosting(fID);
		} else
			return this.postings.get(fID);
	}

	public void addDisriminativeTerm(GindexSearcher searcher,
			PostingFeatures candidateFeatures, int fID, int featureEdgeNum)
			throws IOException {
		if (searcher.discriminative[fID] == false) {
			searcher.discriminative[fID] = true;
			this.postings.put(fID, candidateFeatures.getPosting(fID));
		} else
			System.out
					.println("An Exception in Add Dicriminative Term: the term is already discriminative");
		if (searcher.maxEdgeNum < featureEdgeNum)
			searcher.maxEdgeNum = featureEdgeNum;
	}

	/**
	 * Some White Code should be erased, because it is not a prefix of an black
	 * node
	 * 
	 * @author dyuan
	 */
	// public void reorganize(){
	// //1. Collect all White Node and Black Node
	// List<String> blackNodes = new ArrayList<String>();
	// HashSet<String> whiteNodes = new HashSet<String>();
	// for(Iterator<Entry<String, Integer>> it =
	// this.gHash.entrySet().iterator();it.hasNext();){
	// Entry<String, Integer> aEntry = it.next();
	// if(this.discriminative[aEntry.getValue()] == true)
	// blackNodes.add(aEntry.getKey());
	// else whiteNodes.add(aEntry.getKey());
	// }
	// //2. For each black node: find its parent white nodes, remove this white
	// node from whiteNodes HashSet
	// for(Iterator<String> it = blackNodes.iterator(); it.hasNext();){
	// String oneBlack = it.next();
	// String[] tokens = oneBlack.split(">");
	// String org = tokens[0] + ">";
	// whiteNodes.remove(org);
	// for(int i = 1; i< tokens.length-1; i++){
	// org += tokens[i] + ">";
	// whiteNodes.remove(org);
	// }
	// }
	// //3. For all the left whited nodes in the HashSet, they should be removed
	// from gHash
	// for(Iterator<String> it = whiteNodes.iterator(); it.hasNext();){
	// this.gHash.remove(it.next());
	// }
	// //4. Reorganized the ID
	// boolean[] newDiscriminative = new boolean[this.gHash.size()];
	// for(int i = 0; i< newDiscriminative.length; i++)
	// newDiscriminative[i] = false;
	// int counter = 0;
	// for(Iterator<Entry<String, Integer>> it =
	// this.gHash.entrySet().iterator(); it.hasNext();){
	// Entry<String, Integer> aEntry = it.next();
	// if(this.discriminative[aEntry.getValue()])
	// newDiscriminative[counter] = true;
	// aEntry.setValue(counter);
	// counter++;
	// }
	// this.discriminative = newDiscriminative;
	// System.out.println(this.discriminative.length +
	// "number of features are stored");
	// }

	public static void saveSearcher(GindexSearcher searcher, String baseName,
			String indexName) throws IOException {
		BufferedWriter gindexFileWriter = new BufferedWriter(new FileWriter(
				new File(baseName, indexName)));
		int num = 0;
		if (!searcher.exhaustSearch) {
			for (int i = 0; i < searcher.discriminative.length; i++)
				if (searcher.discriminative[i])
					num++;
			gindexFileWriter.write(num + " " + searcher.discriminative.length
					+ " " + searcher.maxEdgeNum + "\n");
		} else {
			gindexFileWriter.write(searcher.gHash.size() + " "
					+ searcher.gHash.size() + " " + searcher.maxEdgeNum + "\n");
		}

		Iterator<Entry<String, Integer>> iter = searcher.gHash.entrySet()
				.iterator();
		while (iter.hasNext()) {
			StringBuffer buf = new StringBuffer(1024);
			Entry<String, Integer> currentEntry = iter
					.next();
			buf.append(currentEntry.getKey());
			buf.append(",");
			int value = currentEntry.getValue();
			buf.append(value);
			if (searcher.exhaustSearch || searcher.discriminative[value]) {
				buf.append(",");
				buf.append(1);
			}
			buf.append('\n');
			gindexFileWriter.write(buf.toString());
		}
		gindexFileWriter.close();
	}

	public static GindexSearcher loadSearcher(String baseName,
			String indexName, boolean exhaustSearch) throws IOException {
		BufferedReader indexFileReader = new BufferedReader(new FileReader(
				new File(baseName, indexName)));
		String aLine = indexFileReader.readLine();
		String[] aLineToken = aLine.split(" ");
		int featureNum = Integer.parseInt(aLineToken[1]); // total number of
															// patterns stored
		// int maxEdgeNum = Integer.parseInt(aLineToken[2]);

		aLine = indexFileReader.readLine();
		String[] tokens = null;
		int indexTermIndex = -1;
		boolean[] discriminative = null;
		if (!exhaustSearch)
			discriminative = new boolean[featureNum];
		else
			discriminative = null; // for exhaustSearch, the discriminative
									// array is assigned null

		HashMap<String, Integer> gHash = new HashMap<String, Integer>();
		while (aLine != null) {
			tokens = aLine.split(",");
			indexTermIndex = Integer.parseInt(tokens[1]);// Index
			gHash.put(tokens[0], indexTermIndex);// DFS code

			if (!exhaustSearch) {
				if (tokens.length == 2) {// redundant, not discriminative index
											// term
					discriminative[indexTermIndex] = false;
				}
				// else, discriminative index term
				else
					discriminative[indexTermIndex] = true;
			}
			// else discriminative == null, continue
			aLine = indexFileReader.readLine();
		}
		indexFileReader.close();
		return new GindexSearcher(gHash, discriminative, indexTermIndex,
				exhaustSearch);
	}

}
