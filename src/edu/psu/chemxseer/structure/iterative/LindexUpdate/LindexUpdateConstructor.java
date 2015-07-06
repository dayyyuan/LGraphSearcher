package edu.psu.chemxseer.structure.iterative.LindexUpdate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.psu.chemxseer.structure.subsearch.Lindex.LindexConstructor;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;

/**
 * Only for Loading and Saving the Updated Index on Disk
 * 
 * @author dayuyuan
 * 
 */
public class LindexUpdateConstructor extends LindexConstructor {

	/**
	 * Save a Lindex Searcher
	 * 
	 * @param baseName
	 * @param indexName
	 * @throws IOException
	 */
	public static void saveSearcher(LindexUpdateSearcher searcher,
			String baseName, String indexName) throws IOException {
		// 1. First write # of inserted feature
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				baseName, indexName)));
		writer.write(searcher.insertedTerms.size() + "\n");
		// 2. Write all indexing features: both orignal & newly inserted
		saveSearcher(searcher, writer);
		// 3. Write All the deleted Features (IDs)
		StringBuffer sbf = new StringBuffer();
		for (LindexTerm oneTerm : searcher.deletedTerms) {
			sbf.append(oneTerm.getId());
			sbf.append(',');
		}
		if (sbf.length() > 0)
			sbf.deleteCharAt(sbf.length() - 1);
		sbf.append('\n');
		writer.write(sbf.toString());
		// 4. Close the Writer
		writer.close();
	}

	/**
	 * Load the Index, slight change of the LindexConstructors
	 * 
	 * @param baseName
	 * @param indexName
	 * @return
	 * @throws IOException
	 */
	public static LindexUpdateSearcher loadSearcher(String baseName,
			String indexName) throws IOException {
		File inputFile = new File(baseName, indexName);
		if (!inputFile.exists())
			return null;
		BufferedReader indexFileReader = new BufferedReader(new FileReader(
				inputFile));
		String aLine = indexFileReader.readLine();
		int insertFeatureNum = Integer.parseInt(aLine);
		aLine = indexFileReader.readLine();
		int featureNum = Integer.parseInt(aLine);

		LindexTerm[] indexTerms = new LindexTerm[featureNum];
		LindexTerm[] newTerms = new LindexTerm[insertFeatureNum];

		int[] tParentsIndex = new int[featureNum + insertFeatureNum];
		for (int i = 0; i < tParentsIndex.length; i++)
			tParentsIndex[i] = -1;

		aLine = indexFileReader.readLine();
		String[] tokens = null;
		String[] children = null;
		int indexTermIndex = 0;

		int frequency = -1;
		while (aLine != null && aLine.length() != 0) {
			tokens = aLine.split(" => ");
			int[][] label = readText(tokens[0]);
			indexTermIndex = Integer.parseInt(tokens[1]);// Index
			frequency = Integer.parseInt(tokens[3]);
			LindexTerm thisIndex = new LindexTerm(label, indexTermIndex,
					frequency);

			if (indexTermIndex < featureNum)
				indexTerms[indexTermIndex] = thisIndex;
			else
				newTerms[indexTermIndex - featureNum] = thisIndex;

			// We assume that before this index terms, its children are all in
			// indexTerms
			if (tokens.length > 4 && tokens[4].length() != 0) {
				children = tokens[4].split(",");
				LindexTerm[] childTerms = new LindexTerm[children.length];
				for (int i = 0; i < childTerms.length; i++) {
					int childIndex = Integer.parseInt(children[i]);
					if (childIndex < featureNum) {
						if (indexTerms[childIndex] == null)
							System.out
									.println("TEST: a children node has not been initialized");
						childTerms[i] = indexTerms[childIndex];
					} else {
						if (newTerms[childIndex - featureNum] == null)
							System.out
									.println("TEST: a children node has not been initialized");
						childTerms[i] = newTerms[childIndex - featureNum];
					}
				}
				thisIndex.setChildren(childTerms);
			}

			// Add tParent to tParents
			if (tokens.length == 6 && tokens[5].length() != 0) {
				tParentsIndex[indexTermIndex] = Integer.parseInt(tokens[5]);
			}
			aLine = indexFileReader.readLine();
		}
		// Assign tParent
		LindexTerm dummyHead = new LindexTerm(-1, -1);// A dummy head
		for (int i = 0; i < tParentsIndex.length; i++) {
			int parentId = tParentsIndex[i];
			LindexTerm thisTerm = null;
			if (i < featureNum)
				thisTerm = indexTerms[i];
			else
				thisTerm = newTerms[i - featureNum];

			if (parentId == -1) {
				thisTerm.setParent(dummyHead);
				dummyHead.addChild(thisTerm);
			} else if (parentId < featureNum)
				thisTerm.setParent(indexTerms[parentId]);
			else
				thisTerm.setParent(newTerms[parentId - featureNum]);
		}
		// Delete Index Features
		Set<LindexTerm> deletedTerms = new HashSet<LindexTerm>();
		aLine = indexFileReader.readLine();
		tokens = aLine.split(",");
		for (String oneString : tokens) {
			int termID = Integer.parseInt(oneString);
			if (termID > featureNum)
				deletedTerms.add(indexTerms[termID]);
			else
				deletedTerms.add(newTerms[termID - featureNum]);
		}
		List<LindexTerm> insertedTerms = new ArrayList<LindexTerm>();
		for (LindexTerm newTerm : newTerms)
			insertedTerms.add(newTerm);
		indexFileReader.close();
		return new LindexUpdateSearcher(new LindexSearcher(indexTerms,
				dummyHead), insertedTerms, deletedTerms);
	}

}
