package edu.psu.chemxseer.structure.postings.Impl;

import java.text.ParseException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.iso.FastSUStateLabelling;
import edu.psu.chemxseer.structure.postings.Interface.IPostingBuilderLuceneVectorizer;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;

/**
 * Currently Only Support DFS parser: More parsers will be added soon
 * 
 * @author dayuyuan
 * 
 */
public class PostingBuilderLuceneVectorizerSingle implements
		IPostingBuilderLuceneVectorizer {
	private IIndexPrefix searcher;

	public PostingBuilderLuceneVectorizerSingle(IIndexPrefix searcher) {
		this.searcher = searcher;
	}

	/*
	 * Vectorize the Database Graph: (1) Prefix can be either -1 or an number
	 * >=0 (2) The label of "g" is extension labeled if prefix >=0
	 * 
	 * @see edu.psu.chemxseer.structure.subsearch.Interfaces.
	 * PostingBuilderLuceneVectorizer#vectorize(int, de.parmol.graph.Graph,
	 * java.util.Map)
	 */
	@Override
	public Document vectorize(int gID, Graph g, Map<Integer, String> gHash)
			throws ParseException {
		Document gDoc = new Document();

		if (g.getEdgeCount() == 0)
			return gDoc;
		// 1. First Field: Prefix Field, given the graph "g" find the prefix
		// feature of the graph "g"
		int prefixID = this.searcher.getPrefixID(g);
		if (gHash == null) {
			String byteString = new Integer(prefixID).toString();
			gDoc.add(new Field("subGraphs", byteString, Field.Store.YES,
					Field.Index.NOT_ANALYZED));
		} else if (prefixID != -1) {
			gDoc.add(new Field("subGraphs", gHash.get(prefixID),
					Field.Store.YES, Field.Index.NOT_ANALYZED));
		} else
			gDoc.add(new Field("subGraphs", new Integer(-1).toString(),
					Field.Store.YES, Field.Index.NOT_ANALYZED));
		// 2. Second Field: the Graph String with the feature as prefix
		String graphString = null;
		if (prefixID == -1) {
			graphString = MyFactory.getDFSCoder().serialize(g);
		} else {
			FastSUCompleteEmbedding emb = searcher.getEmbedding(prefixID, g);
			if (emb.isIsomorphic())
				graphString = "null";
			else {
				FastSU iso = new FastSU();
				FastSUStateLabelling labelISO = iso.graphExtensionLabeling(emb
						.getState());
				graphString = MyFactory.getDFSCoder().writeArrayToText(
						labelISO.getExtension());
			}
		}
		Field stringField = new Field("gString", graphString, Field.Store.YES,
				Field.Index.NO);
		gDoc.add(stringField);
		// 3. Third Field: the Graph ID field
		Field IDField = new Field("gID", new Integer(gID).toString(),
				Field.Store.YES, Field.Index.NO);
		gDoc.add(IDField);
		return gDoc;
	}
}
