package edu.psu.chemxseer.structure.postings.Impl;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IPostingBuilderLuceneVectorizer;

/**
 * In charge of building the lucene index for postingFetcher. It need the
 * support of one PostingBuilderLuceneVectorizer. The vectorizer can be either
 * VectorizerNormal or VectorizerPartition.
 * 
 * @author dayuyuan
 * 
 */
public class PostingBuilderLucene {
	IPostingBuilderLuceneVectorizer vectorizer;

	public PostingBuilderLucene(IPostingBuilderLuceneVectorizer vectorizer) {
		this.vectorizer = vectorizer;
	}

	public void buildLuceneIndex(String lucenePath, int maxFieldLength,
			IGraphDatabase gDB, Map<Integer, String> gHash)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		// 1. First Step: construct a index writer
		MaxFieldLength maxL = new MaxFieldLength(maxFieldLength);
		// Here we use NIOFDirectory, preferred class except for running on
		// windows
		Directory dic;
		try {
			dic = new NIOFSDirectory(new File(lucenePath));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No Index Directory Available");
			return;
		}
		Analyzer analyzer = new WhitespaceAnalyzer();
		IndexWriter indexWriter = new IndexWriter(dic, analyzer, true, maxL);

		// 2. Second Step: populate the graph database
		for (int i = 0; i < gDB.getTotalNum(); i++)
			this.populateDocsToLucene(i, gDB.findGraph(i), indexWriter, gHash);
		// close the index writer
		indexWriter.close(true);
	}

	private void populateDocsToLucene(int gID, Graph g,
			IndexWriter indexWriter, Map<Integer, String> gHash)
			throws CorruptIndexException, IOException {
		Document doc = null;
		try {
			doc = vectorizer.vectorize(gID, g, gHash);
		} catch (ParseException e) {

			e.printStackTrace();
		}
		if (doc != null) {
			indexWriter.addDocument(doc);
		}
	}

}
