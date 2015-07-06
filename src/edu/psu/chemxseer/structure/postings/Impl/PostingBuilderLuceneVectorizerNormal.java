package edu.psu.chemxseer.structure.postings.Impl;

/**
 * The whole posting list is indexed as the value 
 * @author dayuyuan
 *
 */
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Interface.IPostingBuilderLuceneVectorizer;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;

public class PostingBuilderLuceneVectorizerNormal implements
		IPostingBuilderLuceneVectorizer {
	private IIndex0 searcher;
	private GraphParser gParser;

	public PostingBuilderLuceneVectorizerNormal(GraphParser gParser,
			IIndex0 searcher) {
		this.searcher = searcher;
		this.gParser = gParser;
	}

	@Override
	public Document vectorize(int gID, Graph g, Map<Integer, String> gHash)
			throws ParseException {
		Document gDoc = new Document();

		if (g.getEdgeCount() == 0)
			return gDoc;
		String graphString = gParser.serialize(g);
		Field stringField = new Field("gString", graphString, Field.Store.YES,
				Field.Index.NO);
		gDoc.add(stringField);
		Field IDField = new Field("gID", new Integer(gID).toString(),
				Field.Store.YES, Field.Index.NO);
		gDoc.add(IDField);

		List<Integer> allIDs = searcher.subgraphs(g, new long[4]);

		// 0. Add One "-1" to the subGraphs fields [for pure mustNot search]
		gDoc.add(new Field("subGraphs", (new Integer(-1)).toString(),
				Field.Store.NO, Field.Index.NOT_ANALYZED));

		if (allIDs == null || allIDs.size() == 0)
			return gDoc;

		// 1.
		Collections.sort(allIDs);
		for (int i = 0; i < allIDs.size(); i++) {
			if (gHash == null) {
				String byteString = allIDs.get(i).toString();
				gDoc.add(new Field("subGraphs", byteString, Field.Store.NO,
						Field.Index.NOT_ANALYZED));
			} else {
				gDoc.add(new Field("subGraphs", gHash.get(allIDs.get(i)),
						Field.Store.NO, Field.Index.NOT_ANALYZED));
			}
		}
		// StringBuffer sBuf = new StringBuffer();
		// for(int i = 0; i< allIDs.length; i++ ){
		// if(gHash==null)
		// sBuf.append(new Integer(allIDs[i]).toString());
		// else {
		// sBuf.append(gHash.get(allIDs[i]));
		// // //TEST
		// // String indexString = gHash.get(allIDs[i]);
		// // String indexString2 = searcher.getLabel(allIDs[i]);
		// // if(!indexString.equals(indexString2)){
		// // System.out.println("lala");
		// // }
		// // //END OF TEST
		// }
		// sBuf.append(" ");
		// }
		// String termString = sBuf.substring(0, sBuf.length()-1);
		//
		// Field subgraphField = new Field("subGraphs", termString,
		// Field.Store.NO, Field.Index.ANALYZED_NO_NORMS);
		// gDoc.add(subgraphField);
		return gDoc;
	}
}
