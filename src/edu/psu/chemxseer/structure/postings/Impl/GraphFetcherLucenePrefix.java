package edu.psu.chemxseer.structure.postings.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcherPrefix;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResultPref;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;

public class GraphFetcherLucenePrefix implements IGraphFetcherPrefix {
	private IIndexPrefix prefixIndex;
	private GraphFetcherLucene lucene;

	/**
	 * Current support the DFS graph parser only
	 * 
	 * @param lucene
	 * @param prefixIndex
	 *            : the Index Storing the Prefix Features of the Database Graphs
	 */
	public GraphFetcherLucenePrefix(GraphFetcherLucene lucene,
			IIndexPrefix prefixIndex) {
		this.lucene = lucene;
		this.prefixIndex = prefixIndex;
	}

	public GraphFetcherLucenePrefix(IndexSearcher luceneSearcher, TopDocs hits,
			GraphParser gParser, IIndexPrefix prefixIndex) {
		this.lucene = new GraphFetcherLucene(luceneSearcher, hits, gParser);
		this.prefixIndex = prefixIndex;
	}

	@Override
	/**
	 * The difference is that GraphFetcherLucenePrefix return the GraphResultPrefix
	 */
	public List<IGraphResultPref> getGraphs(long[] TimeComponent) {
		if (lucene.start == lucene.scoreDocs.length)
			return null; // no graphs need to return
		else {
			long startTime = System.currentTimeMillis();
			int end = Math.min(lucene.start + batchCount,
					lucene.scoreDocs.length);
			List<IGraphResultPref> results = new ArrayList<IGraphResultPref>();
			for (int i = lucene.start; i < end; i++) {
				int docID = lucene.scoreDocs[i].doc;
				Document graphDoc = null;
				try {
					graphDoc = lucene.searcher.doc(docID);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (graphDoc != null)
					results.add(new GraphResultLucenePrefix(graphDoc, docID,
							this.prefixIndex));
			}
			lucene.start = end;
			TimeComponent[1] += System.currentTimeMillis() - startTime;
			return results;
		}
	}

	@Override
	public List<IGraphResultPref> getAllGraphs(long[] TimeComponent) {
		List<IGraphResultPref> answer = new ArrayList<IGraphResultPref>();
		List<IGraphResultPref> temp = this.getGraphs(TimeComponent);
		while (temp != null) {
			answer.addAll(temp);
			temp = this.getGraphs(TimeComponent);
		}
		Collections.sort(answer);
		return answer;
	}

	@Override
	public int[] getOrderedIDs() {
		return lucene.getOrderedIDs();
	}

	@Override
	public IGraphFetcherPrefix join(IGraphFetcher fetcher) {
		lucene.join(fetcher);
		return this;
	}

	@Override
	public IGraphFetcherPrefix remove(IGraphFetcher fetcher) {
		lucene.remove(fetcher);
		return this;
	}

	@Override
	public IGraphFetcherPrefix remove(int[] orderedSet) {
		lucene.remove(orderedSet);
		return this;
	}

	@Override
	public int size() {
		return lucene.size();
	}
}
