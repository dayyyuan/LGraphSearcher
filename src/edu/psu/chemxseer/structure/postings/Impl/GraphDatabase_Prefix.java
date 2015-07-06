package edu.psu.chemxseer.structure.postings.Impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUStateLabelling;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.update.LWIndexSearcher2;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;

public class GraphDatabase_Prefix extends GraphDatabase_Basic implements
		IGraphDatabase {
	private IGraphDatabase gDB; // no prefix gDB
	private int[] prefix; // -1 means no prefix, otherwise means existence of
							// prefix
	private IIndexPrefix prefixIndex;

	public static GraphDatabase_Prefix buildPrefixDB(GraphDatabase_InMem gDB2,
			PostingFetcherMem posting, LWIndexSearcher2 index) {
		GraphDatabase_InMem gDB = new GraphDatabase_InMem(gDB2);
		GraphDatabase_Prefix result = new GraphDatabase_Prefix(gDB, index);
		// relabel the graph database
		LindexTerm[] terms = index.getAllTerms();
		// sort all terms with respect to their index value
		Arrays.sort(terms, new TermComparator(index));
		for (LindexTerm aTerm : terms) { // from the high score Term to low
											// score Term
			int[] gIDs = posting.getPostingID(aTerm.getId());
			for (int gID : gIDs) {
				if (result.getPrefixID(gID) == -1) {
					result.relabel(gID, aTerm.getId());
				}
				// else if prefix exists, do not relabel
			}
		}
		return result;
	}

	private void relabel(int gID, int newPrefixID) {
		Graph g = this.findGraph(gID);
		FastSU iso = new FastSU();
		FastSUStateLabelling labelISO = iso.graphExtensionLabeling(
				prefixIndex.getTotalLabel(newPrefixID), g);
		String graphString = MyFactory.getDFSCoder().writeArrayToText(
				labelISO.getExtension());
		gDB.setGString(gID, graphString);
		this.prefix[gID] = newPrefixID;
	}

	public void realbel(Graph g, int gID, int newPrefixID) {
		if (newPrefixID < 0)
			throw new IndexOutOfBoundsException();
		FastSU iso = new FastSU();
		FastSUStateLabelling labelISO = iso.graphExtensionLabeling(
				prefixIndex.getTotalLabel(newPrefixID), g);
		String graphString = MyFactory.getDFSCoder().writeArrayToText(
				labelISO.getExtension());
		gDB.setGString(gID, graphString);
		this.prefix[gID] = newPrefixID;
	}

	static class TermComparator implements Comparator<LindexTerm> {
		IIndexPrefix prefixIndex;

		public TermComparator(IIndexPrefix prefixIndex) {
			this.prefixIndex = prefixIndex;
		}

		@Override
		public int compare(LindexTerm arg0, LindexTerm arg1) {
			return prefixIndex.getPrefixGain(arg1.getId())
					- prefixIndex.getPrefixGain(arg0.getId());
		}

	}

	public GraphDatabase_Prefix(IGraphDatabase gDB, IIndexPrefix prefixIndex) {
		super(MyFactory.getDFSCoder());
		this.gDB = gDB;
		this.prefix = new int[gDB.getTotalNum()];
		Arrays.fill(prefix, -1);
		this.prefixIndex = prefixIndex;
	}

	public GraphDatabase_Prefix(IGraphDatabase gDB, int[] prefix,
			IIndexPrefix prefixIndex) {
		super(MyFactory.getDFSCoder());
		this.gDB = gDB;
		this.prefix = prefix;
	}

	public GraphDatabase_Prefix(IGraphDatabase gDB, String prefixFile,
			IIndexPrefix prefixIndex) {
		super(MyFactory.getDFSCoder());
		this.gDB = gDB;
		try {
			this.prefix = load(prefixFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[] load(String prefixFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(prefixFile));
		String aLine = reader.readLine();
		int size = Integer.parseInt(aLine);
		int[] result = new int[size];
		int i = 0;
		while (aLine != null) {
			aLine = reader.readLine();
			result[i++] = Integer.parseInt(aLine);
		}
		reader.close();
		return result;
	}

	public void storePrefix(String prefixFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(prefixFile));
		writer.write(prefix.length + "\n");
		for (int i : prefix)
			writer.write(i + "\n");
		writer.flush();
		writer.close();
	}

	@Override
	/**
	 * Cancatinate the prefix with the suffix
	 */
	public String findGraphString(int gID) {
		if (prefix[gID] == -1) {
			return this.gDB.findGraphString(gID);
		} else {
			int[][] prefixArray = this.prefixIndex.getTotalLabel(prefix[gID]);
			return MyFactory.getDFSCoder().writeArrayToText(prefixArray)
					+ gDB.findGraphString(gID);
		}
	}

	@Override
	public int getTotalNum() {
		return this.gDB.getTotalNum();
	}

	public int getPrefixID(int gID) {
		return this.prefix[gID];
	}

	public int[][] getSuffix(int gID) {
		return MyFactory.getDFSCoder().parseTextToArray(
				gDB.findGraphString(gID));
	}

	public void setSuffix(int[][] suffix, int gID) {
		this.gDB.setGString(gID,
				MyFactory.getDFSCoder().writeArrayToText(suffix));
	}

	@Override
	public void setGString(int gID, String serialize) {
		throw new UnsupportedOperationException();
	}

}
