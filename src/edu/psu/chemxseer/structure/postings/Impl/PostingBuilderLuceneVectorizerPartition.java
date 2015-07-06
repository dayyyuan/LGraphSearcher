package edu.psu.chemxseer.structure.postings.Impl;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Interface.IPostingBuilderLuceneVectorizer;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcherAdv;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * The whole posting list is indexed as the value But the value set is
 * partitioned into two parts: direct value set & indirect value set
 * 
 * @author dayuyuan
 * 
 */
public class PostingBuilderLuceneVectorizerPartition implements
		IPostingBuilderLuceneVectorizer {
	private LindexSearcherAdv searcher;
	private GraphParser gParser;

	public PostingBuilderLuceneVectorizerPartition(GraphParser gParser,
			LindexSearcherAdv searcher) {
		this.searcher = searcher;
		this.gParser = gParser;
	}

	@Override
	public Document vectorize(int gID, Graph query, Map<Integer, String> gHash)
			throws ParseException {
		Document gDoc = new Document();

		if (query.getEdgeCount() == 0)
			return gDoc;
		String graphString = gParser.serialize(query);
		Field stringField = new Field("gString", graphString, Field.Store.YES,
				Field.Index.NO);
		gDoc.add(stringField);
		Field IDField = new Field("gID", new Integer(gID).toString(),
				Field.Store.YES, Field.Index.NO);
		gDoc.add(IDField);

		List<Integer> allIDs = searcher.subgraphs(query, new long[4]);
		if (allIDs == null || allIDs.size() == 0)
			return gDoc; // this may not happen

		Set<Integer> directIDset = searcher.getDirectFeatures(query, allIDs);
		int[] directIDs = new int[directIDset.size()];
		int directIDindex = 0;
		for (Integer id : directIDset)
			directIDs[directIDindex++] = id;

		Collections.sort(allIDs);
		Arrays.sort(directIDs);

		// for all directIDs: subGraphs_d
		for (int id : directIDs) {
			if (gHash == null) {
				String byteString = new Integer(id).toString();
				gDoc.add(new Field("subGraphs_d", byteString, Field.Store.NO,
						Field.Index.NOT_ANALYZED));
			} else {
				gDoc.add(new Field("subGraphs_d", gHash.get(id),
						Field.Store.NO, Field.Index.NOT_ANALYZED));
			}
		}
		// for the indirectIDs: subgraphs_i
		int[] temp = new int[allIDs.size()];
		int tempIndex = 0;
		for (Integer it : allIDs)
			temp[tempIndex++] = it;
		int[] indirectIDs = OrderedIntSets.remove(temp, directIDs);

		for (int id : indirectIDs) {
			if (gHash == null) {
				String byteString = new Integer(id).toString();
				gDoc.add(new Field("subGraphs_i", byteString, Field.Store.NO,
						Field.Index.NOT_ANALYZED));
			} else {
				gDoc.add(new Field("subGraphs_i", gHash.get(id),
						Field.Store.NO, Field.Index.NOT_ANALYZED));
			}
		}

		// StringBuffer sBuf = new StringBuffer();
		//
		// // build the indirect value set
		// for(int i = 0; i< allIDs.length; i++ ){
		// if(directIds.contains(allIDs[i]))
		// continue;
		// if(gHash==null)
		// sBuf.append(new Integer(allIDs[i]).toString());
		// else
		// sBuf.append(gHash.get(allIDs[i]));
		// sBuf.append(' ');
		// }
		// // if(sBuf.length() ==0)
		// // System.out.print("lala");
		// if(sBuf.length()>0){
		// String termString = sBuf.substring(0, sBuf.length()-1);
		// Field subgraphField = new Field("subGraphs_i", termString,
		// Field.Store.NO, Field.Index.ANALYZED_NO_NORMS);
		// gDoc.add(subgraphField);
		// }
		// // build the direct value set
		// for(Integer id: directIds){
		// if(gHash==null)
		// sBuf.append(new Integer(id).toString());
		// else
		// sBuf.append(gHash.get(id));
		// sBuf.append(' ');
		// }
		// String termString = sBuf.substring(0, sBuf.length()-1);
		// Field subgraphField = new Field("subGraphs_d", termString,
		// Field.Store.NO, Field.Index.ANALYZED_NO_NORMS);
		// gDoc.add(subgraphField);

		return gDoc;
	}
}
