package edu.psu.chemxseer.structure.postings.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;

/**
 * Lucene Lazy Fetcher all The Documents that need to be returned
 * 
 * @author dayuyuan
 * 
 */
public class GraphFetcherLucene implements IGraphFetcher {
	protected IndexSearcher searcher;
	protected ScoreDoc[] scoreDocs; // make sure that the docs are ordered by
									// their ids
	protected int start;
	protected GraphParser gParser;

	public GraphFetcherLucene(IndexSearcher searcher, TopDocs hits,
			GraphParser gParser) {
		this.searcher = searcher;
		this.scoreDocs = hits.scoreDocs;
		this.start = 0;
		this.gParser = gParser;
		Arrays.sort(scoreDocs, new DocComparator());
	}

	public GraphFetcherLucene(GraphFetcherLucene lucene) {
		this.searcher = lucene.searcher;
		this.scoreDocs = lucene.scoreDocs;
		this.start = 0;
		this.gParser = null;
		Arrays.sort(scoreDocs, new DocComparator());
	}

	@Override
	public List<IGraphResult> getGraphs(long[] TimeComponent) {
		if (start == scoreDocs.length)
			return null; // no graphs need to return
		else {
			long startTime = System.currentTimeMillis();
			int end = Math.min(start + batchCount, scoreDocs.length);
			List<IGraphResult> results = new ArrayList<IGraphResult>();
			for (int i = start; i < end; i++) {
				int docID = scoreDocs[i].doc;
				Document graphDoc = null;
				try {
					graphDoc = searcher.doc(docID);
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (graphDoc != null)
					results.add(new GraphResultLucene(gParser, graphDoc, docID));
			}
			start = end;
			TimeComponent[1] += System.currentTimeMillis() - startTime;
			return results;
		}
	}

	@Override
	public int size() {
		return this.scoreDocs.length;
	}

	@Override
	public int[] getOrderedIDs() {
		int[] results = new int[this.scoreDocs.length];
		for (int i = 0; i < results.length; i++)
			results[i] = scoreDocs[i].doc;
		return results;
	}

	@Override
	public IGraphFetcher join(IGraphFetcher fetcher) {
		// A copy of the retain operation of the
		// "SelfImplemntSet or IntersectionSet"
		int[] otherIDs = fetcher.getOrderedIDs();
		if (otherIDs == null || otherIDs.length == 0)
			return this; // no need for intersection at all
		int iter = 0, i = 0, j = 0;
		// i is index on item, j is index on c
		while (i < scoreDocs.length && j < otherIDs.length) {
			if (scoreDocs[i].doc > otherIDs[j])
				j++;
			else if (scoreDocs[i].doc == otherIDs[j]) {
				scoreDocs[iter++] = scoreDocs[i];
				j++;
				i++;
				continue;
			} else {// items[i] < c[j]
				i++;
				continue;
			}
		}
		ScoreDoc[] newS = new ScoreDoc[iter];
		for (int w = 0; w < iter; w++)
			newS[w] = this.scoreDocs[w];
		this.scoreDocs = newS;
		return this;
	}

	@Override
	public IGraphFetcher remove(IGraphFetcher fetcher) {
		// A copy of the retain operation of the
		// "SelfImplemntSet or IntersectionSet"
		int[] otherIDs = fetcher.getOrderedIDs();
		if (otherIDs == null || otherIDs.length == 0)
			return this; // no need for intersection at all
		int iter = 0, i = 0, j = 0;
		// i is index on item, j is index on c
		while (i < scoreDocs.length && j < otherIDs.length) {
			if (scoreDocs[i].doc > otherIDs[j])
				j++;
			else if (scoreDocs[i].doc == otherIDs[j]) {
				j++; // skip this item
				i++;
				continue;
			} else {// items[i] < c[j]
				scoreDocs[iter++] = scoreDocs[i];
				i++;
				continue;
			}
		}
		while (i < scoreDocs.length)
			scoreDocs[iter++] = scoreDocs[i++];

		ScoreDoc[] newS = new ScoreDoc[iter];
		for (int w = 0; w < iter; w++)
			newS[w] = this.scoreDocs[w];
		this.scoreDocs = newS;
		return this;
	}

	@Override
	public IGraphFetcher remove(int[] orderedSet) {
		// A copy of the retain operation of the
		// "SelfImplemntSet or IntersectionSet"
		int[] otherIDs = orderedSet;
		if (otherIDs == null || otherIDs.length == 0)
			return this; // no need for intersection at all
		int iter = 0, i = 0, j = 0;
		// i is index on item, j is index on c
		while (i < scoreDocs.length && j < otherIDs.length) {
			if (scoreDocs[i].doc > otherIDs[j])
				j++;
			else if (scoreDocs[i].doc == otherIDs[j]) {
				j++; // skip this item
				i++;
				continue;
			} else {// items[i] < c[j]
				scoreDocs[iter++] = scoreDocs[i];
				i++;
				continue;
			}
		}
		while (i < scoreDocs.length)
			scoreDocs[iter++] = scoreDocs[i++];

		ScoreDoc[] newS = new ScoreDoc[iter];
		for (int w = 0; w < iter; w++)
			newS[w] = this.scoreDocs[w];
		this.scoreDocs = newS;
		return this;
	}

	@Override
	public List<IGraphResult> getAllGraphs(long[] TimeComponent) {
		List<IGraphResult> answer = new ArrayList<IGraphResult>();
		List<IGraphResult> temp = this.getGraphs(TimeComponent);
		while (temp != null) {
			answer.addAll(temp);
			temp = this.getGraphs(TimeComponent);
		}
		Collections.sort(answer);
		return answer;
	}
}

class DocComparator implements Comparator<ScoreDoc> {
	@Override
	public int compare(ScoreDoc arg0, ScoreDoc arg1) {
		Integer one = arg0.doc;
		Integer two = arg1.doc;
		return one.compareTo(two);
	}
}
