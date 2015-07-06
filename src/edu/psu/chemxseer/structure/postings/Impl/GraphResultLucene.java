package edu.psu.chemxseer.structure.postings.Impl;

import java.text.ParseException;

import org.apache.lucene.document.Document;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * The Lucene Graph Results works with the Lucene postings
 * 
 * @author dayuyuan
 * 
 */
public class GraphResultLucene implements IGraphResult {
	private Document graphDoc;
	private GraphParser gParser;
	private int docID;

	public GraphResultLucene(GraphParser gParser, Document graphDoc, int docID) {
		this.graphDoc = graphDoc;
		this.gParser = gParser;
	}

	@Override
	public int getID() {
		int i = Integer.parseInt(graphDoc.get("gID"));
		return i;
	}

	@Override
	public Graph getG() {
		try {
			String gString = graphDoc.get("gString");
			if (gString.equals("null"))
				return null;
			return gParser.parse(gString, MyFactory.getGraphFactory());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
}
