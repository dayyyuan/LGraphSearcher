package edu.psu.chemxseer.structure.postings.Impl;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResultPref;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;

/**
 * The Database Graph here has a prefix as a feature
 * 
 * @author dayuyuan
 * 
 */
public class GraphResultLucenePrefix implements IGraphResultPref {
	private Document graphDoc;
	private int docID;
	private IIndexPrefix index;

	/**
	 * For Now, it only support gParser to be DFS parser
	 * 
	 * @param graphDoc
	 * @param docID
	 * @param index
	 *            : Index Storing the Prefix (representation) of the graph.
	 */
	public GraphResultLucenePrefix(Document graphDoc, int docID,
			IIndexPrefix index) {
		this.graphDoc = graphDoc;
		this.docID = docID;
		this.index = index;
	}

	/**
	 * Return the Prefix Features of this GraphResult: (1) If there exist the
	 * prefix field, then the prefix filed is used (2) If there is not prefix
	 * field, then the subGraphs filed is used
	 */
	@Override
	public int getPrefixFeatureID() {
		Field prefixField = graphDoc.getField("prefix");
		if (prefixField == null)
			prefixField = graphDoc.getField("subGraphs");
		int i = Integer.parseInt(prefixField.stringValue());
		return i;
	}

	@Override
	public Graph getG() {
		int[][] suffix = this.getSuffix();
		int[][] prefix = this.getPrefix();
		if (prefix != null) {
			if (suffix != null) {
				int[][] gString = new int[prefix.length + suffix.length][];
				for (int i = 0; i < prefix.length; i++)
					gString[i] = prefix[i];
				for (int j = 0; j < suffix.length; j++)
					gString[prefix.length + j] = suffix[j];
				Graph g = MyFactory.getDFSCoder().parse(gString,
						MyFactory.getGraphFactory());
				g.saveMemory();
				return g;
			} else {
				// suffix == null
				return MyFactory.getDFSCoder().parse(prefix,
						MyFactory.getGraphFactory());
			}
		} else if (suffix != null)
			return MyFactory.getDFSCoder().parse(suffix,
					MyFactory.getGraphFactory());
		else
			return null; // both prefix & suffix == null
	}

	/**
	 * Return null if the prefixIndex == -1 Else return the prefix
	 * 
	 * @return
	 */
	private int[][] getPrefix() {
		int prefixIndex = this.getPrefixFeatureID();
		if (prefixIndex == -1)
			return null;
		int[][] prefixLabel = this.index.getTotalLabel(prefixIndex);
		return prefixLabel;
	}

	@Override
	public int getID() {
		int i = Integer.parseInt(graphDoc.get("gID"));
		return i;
	}

	@Override
	public int getDocID() {
		return this.docID;
	}

	@Override
	public int compareTo(IGraphResult o) {
		int id1 = this.getID();
		int id2 = o.getID();
		if (id1 < id2)
			return -1;
		else if (id1 == id2)
			return 0;
		else
			return 1;
	}

	@Override
	public int[][] getSuffix() {
		String gString = graphDoc.get("gString");
		if (gString.equals("null"))
			return null;
		return MyFactory.getDFSCoder().parseTextToArray(gString);
	}

}
