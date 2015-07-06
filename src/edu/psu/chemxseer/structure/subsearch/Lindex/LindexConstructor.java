package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.iso.CanonicalDFS;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUStateLabelling;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureExt;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class LindexConstructor {

	/**
	 * Construct a on-disk Lindex
	 * 
	 * @param features
	 * @param in_memoryIndex
	 * @param rootTermID
	 */
	public static LindexSearcher constructOnDisk(
			NoPostingFeatures_Ext<IOneFeature> features,
			LindexSearcher in_memoryIndex, int rootTermID) {

		LindexSearcher result = constructIndex(features);
		// 4. Relabel each index term as an extension of their parent
		try {
			// System.out.println("YDY: relabel");
			relabelIndexTerms(
					features,
					in_memoryIndex
							.getTermFullLabel(in_memoryIndex.indexTerms[rootTermID]),
					result);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Construct a Lindex, building from selected candidate features Pay
	 * attention: The Features and the LindexTerms has the same index on the
	 * arrays The ID of the Features should be the same as the position of that
	 * features on the array
	 * 
	 * @param candidateFeatures
	 * @param postingFileName
	 */
	public static LindexSearcher construct(
			NoPostingFeatures_Ext<IOneFeature> candidateFeatures) {
		LindexSearcher result = constructIndex(candidateFeatures);

		// 4. Relabel each index term as an extension of their parent
		try {
			// System.out.println("YDY: relabel");
			relabelIndexTerms(candidateFeatures, result);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// System.out.println("2. Time for Construction LindexBase: " +
		// (System.currentTimeMillis()-time1));
		// System.out.println("YDY: finish building LindexNewBase");
		return result;
	}

	private static LindexSearcher constructIndex(
			NoPostingFeatures_Ext<IOneFeature> candidateFeatures) {
		LindexTerm dummyHead = new LindexTerm(-1, -1);// A dummy head
		LindexTerm[] indexTerms = new LindexTerm[candidateFeatures
				.getfeatureNum()];
		// Initialize each L index term with their id and feature's posting..
		// shift
		// 1. Convert Features to IndexTerms
		for (int i = 0; i < candidateFeatures.getfeatureNum(); i++) {
			indexTerms[i] = new LindexTerm(i, candidateFeatures
					.getFeatureExt(i).getFrequency());
		}

		// 2. Add children relationship among those features
		// We make sure that the index of IndexTerm is consistent with the index
		// of its underlying feature
		for (int i = 0; i < candidateFeatures.getfeatureNum(); i++) {
			OneFeatureExt oneFeature = candidateFeatures.getFeatureExt(i);
			List<OneFeatureExt> childFeatures = oneFeature.getChildren();
			if (childFeatures == null || childFeatures.size() == 0)
				continue;
			LindexTerm[] childTerms = new LindexTerm[childFeatures.size()];
			for (int temp = 0; temp < childFeatures.size(); temp++) {
				int w = childFeatures.get(temp).getFeatureId();
				/*
				 * if(w >= indexTerms.length || w < 0)
				 * System.out.println("lala");
				 */
				childTerms[temp] = indexTerms[w];
			}
			indexTerms[i].setChildren(childTerms);
		}
		// 3. Add T_parent relationship among those features
		// System.out.println("YDY: added parent");
		addParentRelation(candidateFeatures, indexTerms, dummyHead);
		return new LindexSearcher(indexTerms, dummyHead);
	}

	/**
	 * Find the parent of each Lindex term, the T_parent selection procedure
	 * involve complicated optimization and selection algorithm Parent
	 * Assignment rule: 1. choose the one with minimum frequency 2. If a tie
	 * happens, assign the parent whose has minimum number of valid children
	 * currently 3. If a tie happens, choose the one with minimum edge //
	 * approximate minimum depth
	 * 
	 * @param features
	 */
	private static void addParentRelation(
			NoPostingFeatures_Ext<IOneFeature> features,
			LindexTerm[] indexTerms, LindexTerm dummyHead) {
		CanonicalDFS dfsParser = MyFactory.getDFSCoder();
		for (int i = 0; i < features.getfeatureNum(); i++) {
			OneFeatureExt ithFeature = features.getFeatureExt(i);
			List<OneFeatureExt> parentFeatures = ithFeature.getParents();
			if (parentFeatures == null || parentFeatures.size() == 0) {
				// Assign parent features: the dummy head
				dummyHead.addChild(indexTerms[i]);
				indexTerms[i].setParent(dummyHead);
				// Add a canonical labels of graph g in first layer LindexTerm
				int[][] dfsCode = dfsParser.serializeToArray(ithFeature
						.getFeatureGraph());
				indexTerms[i].setExtension(dfsCode);
			} else {
				int minimumParentIndex = chooseTParent(features,
						parentFeatures, indexTerms);
				indexTerms[i].setParent(indexTerms[minimumParentIndex]);
			}
		}
	}

	public static int chooseTParent(
			NoPostingFeatures_Ext<IOneFeature> features,
			List<OneFeatureExt> parentFeatures, LindexTerm[] indexTerms) {
		// Parent Assignment rule:
		// 1. choose the one with minimum frequency
		// 2. If a tie happens, assign the parent whose has minimum number of
		// valid children currently
		// 3. If a tie happens, choose the one with minimum edge // approximate
		// minimum depth
		int minimumParentIndex = 0;
		int minimumFrequency = Integer.MAX_VALUE;
		int minimumChildrenNum = Integer.MAX_VALUE;
		for (OneFeatureExt parentFeature : parentFeatures) {
			// A. First choose the parent with the minimum frequency
			int tID = parentFeature.getFeatureId();
			int tFrequency = features.getFeatureExt(
					parentFeature.getFeatureId()).getFrequency();
			int validChildNum = 0;
			if (tFrequency <= minimumFrequency) {
				// calculate number of valid children
				LindexTerm[] children = indexTerms[parentFeature.getFeatureId()]
						.getChildren();
				for (int w = 0; w < children.length; w++) {
					if (children[w].getParent() == null)
						continue;
					else if (children[w].getParent().getId() == tID)
						validChildNum++;
				}
			}
			if (tFrequency < minimumFrequency) {
				minimumParentIndex = parentFeature.getFeatureId();
				minimumFrequency = tFrequency;
				minimumChildrenNum = validChildNum;
			} else if (tFrequency == minimumFrequency) {
				// B. Second choose the parent who has minimum number of valid
				// children
				if (validChildNum < minimumChildrenNum) {
					minimumParentIndex = parentFeature.getFeatureId();
					minimumChildrenNum = validChildNum;
				} else if (validChildNum == minimumChildrenNum) {
					// c. Third choose the one with minimum edge // approximate
					// minimum depth
					int originalParentEdge = features
							.getFeatureExt(minimumParentIndex)
							.getFeatureGraph().getEdgeCount();
					int testParentEdge = parentFeature.getFeatureGraph()
							.getEdgeCount();
					if (testParentEdge < originalParentEdge) {
						minimumParentIndex = parentFeature.getFeatureId();
					} else
						continue; // if it is a tie, randomly choose the
									// previous one, otherwise continue
				} else
					continue;
			} else
				continue;
		}
		return minimumParentIndex;
	}

	/**
	 * Relabel LindexTerms as an extension of their parent For first layer
	 * LindexTerm, label them as DFS canonical code
	 * 
	 * @param candidateFeatures
	 * @throws ParseException
	 */
	private static void relabelIndexTerms(
			NoPostingFeatures_Ext<IOneFeature> candidateFeatures,
			LindexSearcher searcher) throws ParseException {
		candidateFeatures.createGraphs();
		CanonicalDFS dfsParser = MyFactory.getDFSCoder();
		// breadth first search of the graph lattice
		// First: find the firstLayerseeds of this Lindex
		LindexTerm[] firstLayerSeeds = searcher.dummyHead.getChildren();
		// Then: starting from each seed term
		FastSU fastSu = new FastSU();
		for (int i = 0; i < firstLayerSeeds.length; i++) {
			LindexTerm seed = firstLayerSeeds[i];
			Graph seedGraph = dfsParser.parse(seed.getExtension().clone(),
					MyFactory.getGraphFactory());

			LindexTerm[] children = seed.getChildren();
			if (children == null || children.length == 0)
				continue;
			for (int j = 0; j < children.length; j++) {
				LindexTerm childTerm = children[j];
				OneFeatureExt childFeature = candidateFeatures
						.getFeatureExt(childTerm.getId());
				if (childTerm.getParent() != seed)
					continue; // not a real child
				// SeedState is a FastSUState mapping between graph parent and
				// graph child
				FastSUStateLabelling seedState = fastSu.graphExtensionLabeling(
						seedGraph, childFeature.getFeatureGraph());
				childTerm.setExtension(seedState.getExtension());
				// keep on searching
				relabelIndexTermsSearch(childTerm, seedState,
						candidateFeatures, fastSu);
			}
		}
	}

	private static void relabelIndexTermsSearch(LindexTerm term,
			FastSUStateLabelling oriState,
			NoPostingFeatures_Ext<IOneFeature> candidateFeatures, FastSU fastSu) {
		LindexTerm[] children = term.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			LindexTerm childTerm = children[i];

			if (childTerm.getParent() != term)
				continue;
			else {
				// corFeature is the feature corresponding to childTerm
				OneFeatureExt corFeature = candidateFeatures
						.getFeatureExt(childTerm.getId());
				FastSUStateLabelling newState = fastSu.graphExtensionLabeling(
						oriState, corFeature.getFeatureGraph());
				childTerm.setExtension(newState.getExtension());
				relabelIndexTermsSearch(children[i], newState,
						candidateFeatures, fastSu);
			}
		}
	}

	private static void relabelIndexTerms(
			NoPostingFeatures_Ext<IOneFeature> candidateFeatures,
			int[][] rootTermFullLabel, LindexSearcher searcher)
			throws ParseException {
		candidateFeatures.createGraphs();
		CanonicalDFS dfsParser = MyFactory.getDFSCoder();
		// breadth first search of the graph lattice
		// First: find the firstLayerseeds of this Lindex
		LindexTerm[] firstLayerSeeds = searcher.dummyHead.getChildren();
		// Then: starting from each seed term
		FastSU fastSu = new FastSU();
		for (int i = 0; i < firstLayerSeeds.length; i++) {
			LindexTerm seedTerm = firstLayerSeeds[i];
			OneFeatureExt seedFeature = candidateFeatures
					.getFeatureExt(seedTerm.getId());
			Graph g = dfsParser.parse(rootTermFullLabel,
					MyFactory.getGraphFactory());

			FastSUStateLabelling seedState = fastSu.graphExtensionLabeling(g,
					seedFeature.getFeatureGraph());
			seedTerm.setExtension(seedState.getExtension());
			// keep on searching
			relabelIndexTermsSearch(seedTerm, seedState, candidateFeatures,
					fastSu);
		}
	}

	/**
	 * Save the index searcher
	 * 
	 * @param searcher
	 * @param baseName
	 * @param indexName
	 * @throws IOException
	 */
	public static void saveSearcher(LindexSearcher searcher, String baseName,
			String indexName) throws IOException {
		// First write # of feature
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				baseName, indexName)));
		saveSearcher(searcher, writer);
		writer.close();
	}

	protected static void saveSearcher(LindexSearcher searcher,
			BufferedWriter writer) throws IOException {
		writer.write(searcher.indexTerms.length + "\n");
		boolean[] visitedArray = new boolean[searcher.getFeatureCount()];
		for (int i = 0; i < visitedArray.length; i++)
			visitedArray[i] = false;
		// Search the graph Lattice starting from the dummy head
		LindexTerm[] firstLayer = searcher.dummyHead.getChildren();

		for (int i = 0; i < firstLayer.length; i++) {
			backupTerm(searcher, firstLayer[i], visitedArray, writer);
		}
	}

	private static void backupTerm(LindexSearcher searcher, LindexTerm ithTerm,
			boolean[] visitedArray, Writer writer) throws IOException {
		LindexTerm[] c = ithTerm.getChildren();

		if (c == null || c.length == 0) {
			writer.write(ithTerm.toString(searcher.dummyHead));
			visitedArray[ithTerm.getId()] = true;
		} else {
			// Back up its children first
			for (int j = 0; j < c.length; j++) {
				if (visitedArray[c[j].getId()] == true)
					continue;
				else
					backupTerm(searcher, c[j], visitedArray, writer);
			}
			writer.write(ithTerm.toString(searcher.dummyHead));
			visitedArray[ithTerm.getId()] = true;
		}
	}

	public static LindexSearcher loadSearcher(String baseName, String indexName)
			throws IOException {
		File inputFile = new File(baseName, indexName);
		if (!inputFile.exists())
			return null;
		BufferedReader indexFileReader = new BufferedReader(new FileReader(
				inputFile));
		return loadSearcher(indexFileReader);
	}

	protected static LindexSearcher loadSearcher(BufferedReader indexFileReader)
			throws IOException {
		String aLine = indexFileReader.readLine();
		int featureNum = Integer.parseInt(aLine);
		LindexTerm[] indexTerms = new LindexTerm[featureNum];
		int[] tParentsIndex = new int[featureNum];
		for (int i = 0; i < tParentsIndex.length; i++)
			tParentsIndex[i] = -1;

		aLine = indexFileReader.readLine();
		String[] tokens = null;
		String[] children = null;
		int indexTermIndex = 0;
		// int indexString = -1;
		int frequency = -1;
		while (aLine != null && aLine.length() != 0) {
			tokens = aLine.split(" => ");
			int[][] label = readText(tokens[0]);
			indexTermIndex = Integer.parseInt(tokens[1]);// Index
			// indexString = Integer.parseInt(tokens[2]);
			frequency = Integer.parseInt(tokens[3]);
			indexTerms[indexTermIndex] = new LindexTerm(label, indexTermIndex,
					frequency);

			// We assume that before this index terms, its children are all in
			// indexTerms
			if (tokens.length > 4 && tokens[4].length() != 0) {
				children = tokens[4].split(",");
				LindexTerm[] childTerms = new LindexTerm[children.length];
				for (int i = 0; i < childTerms.length; i++) {
					int childIndex = Integer.parseInt(children[i]);
					if (indexTerms[childIndex] == null)
						System.out
								.println("TEST: a children node has not been initialized");
					childTerms[i] = indexTerms[childIndex];
				}
				indexTerms[indexTermIndex].setChildren(childTerms);
			}

			// Add tParent to tParents
			if (tokens.length == 6 && tokens[5].length() != 0) {
				tParentsIndex[indexTermIndex] = Integer.parseInt(tokens[5]);
			}
			aLine = indexFileReader.readLine();
		}
		// Assign tParent
		for (int i = 0; i < indexTerms.length; i++) {
			int parentId = tParentsIndex[i];
			if (parentId == -1) {
				indexTerms[i].setParent(null);
			} else
				indexTerms[i].setParent(indexTerms[parentId]);
		}
		// Assign dummy head
		LindexTerm dummyHead = new LindexTerm(-1, -1);// A dummy head
		for (int i = 0; i < indexTerms.length; i++)
			if (indexTerms[i].getParent() == null) {
				dummyHead.addChild(indexTerms[i]);
				indexTerms[i].setParent(dummyHead);
			}
		return new LindexSearcher(indexTerms, dummyHead);
	}

	/**
	 * Parse the text to lindex Terms
	 * 
	 * @param text
	 * @return
	 */
	protected static int[][] readText(String text) {
		if (text.length() == 0)
			return new int[0][];
		String[] entries = text.split("><");
		int[][] resutls = new int[entries.length][5];
		// The first and last entry need to be dealt specially
		entries[0] = entries[0].substring(1);
		entries[entries.length - 1] = entries[entries.length - 1].substring(0,
				entries[entries.length - 1].length() - 1);
		String[] temp;
		for (int i = 0; i < entries.length; i++) {
			temp = entries[i].split(",");
			for (int j = 0; j < temp.length; j++)
				resutls[i][j] = Integer.parseInt(temp[j]);
		}
		return resutls;
	}

}
