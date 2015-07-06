package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.GraphFetcherDB;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.util.OrderedIntSet;

/**
 * Edge Index should be a index, composed of indexSearcher (lookup),
 * postingFetcher and verifier.
 * 
 * However, due to the previous implementation, those three parts are hardcooded
 * and wired together Thus, a warper is designed instead.
 * 
 * @author duy113
 * 
 */
public class EdgeIndex {
	// Edge index: Contains either Frequent & InFrequent Edges
	// Frequent Edges are used to index TCFG in graphArray
	// InFrequent Edges are used to index Graph in the Graph Database
	private HashMap<IN_FGindexEdge, IN_FGindexEdgeEntries> edgeIndex;
	private IGraphDatabase gDB; // only for load of infrequent graphs

	/**
	 * For index loading: In_memory
	 * 
	 * @param gDB
	 */
	public EdgeIndex(IGraphDatabase gDB) {
		this.gDB = gDB;
		this.edgeIndex = new HashMap<IN_FGindexEdge, IN_FGindexEdgeEntries>();

	}

	/**
	 * For index construction or index loaidng on_disk
	 */
	public EdgeIndex() {
		this.edgeIndex = new HashMap<IN_FGindexEdge, IN_FGindexEdgeEntries>();
	}

	public void populateInfrequentGraphs() throws IOException {
		int stepWise = 1000; // load 1000 graphs from database each time
		Graph[] graphs = null;
		int bound = 0;

		for (int i = 0; i < gDB.getTotalNum();) {
			if (i + stepWise <= gDB.getTotalNum()) {
				graphs = gDB.loadGraphs(i, i + stepWise);
				bound = stepWise;
			} else {
				graphs = gDB.loadGraphs(i, gDB.getTotalNum());
				bound = graphs.length;
			}
			for (int j = 0; j < bound; j++) {
				Graph addedGraph = null;
				addedGraph = graphs[j];
				this.insertGraph(addedGraph, i + j);
			}
			i += stepWise;
		}
		this.sortIndex();
	}

	public IN_FGindexEdgeEntries get(IN_FGindexEdge key) {
		return edgeIndex.get(key);
	}

	/**
	 * iven a Graph querys, search for all distinct edges of this graph save
	 * them in pair: <FGindexEdge, inner frequency of this edge on graph>
	 * In_FGindexEdge is an edge Integer it the frequency of the edge
	 * 
	 * @param query
	 * @return
	 */
	public Map<IN_FGindexEdge, Integer> getAllEdges(Graph query) {
		Map<IN_FGindexEdge, Integer> edges = new HashMap<IN_FGindexEdge, Integer>();
		int edgeCount = query.getEdgeCount();
		for (int edgeIndex = 0; edgeIndex < edgeCount; ++edgeIndex) {
			int edge = query.getEdge(edgeIndex);
			IN_FGindexEdge fgEdge = new IN_FGindexEdge(query.getNodeLabel(query
					.getNodeA(edge)), query.getNodeLabel(query.getNodeB(edge)),
					query.getEdgeLabel(edge));
			Integer currentNum = edges.get(fgEdge);
			if (currentNum == null)
				edges.put(fgEdge, 1); // frequency = 1
			else
				edges.put(fgEdge, currentNum + 1); // frequency ++
		}
		return edges;
	}

	/**
	 * generate the candidates by intersecting postings of infrequent edges
	 * 
	 * @param g
	 * @param timeComponent
	 *            [2]
	 * @return
	 */
	public IGraphFetcher getInfrequentEdgeCandidates(Graph g,
			long[] timeComponent) {
		long start = System.currentTimeMillis();
		Set<Entry<IN_FGindexEdge, Integer>> edgesofG = this.getAllEdges(g)
				.entrySet();
		int graphSize = g.getEdgeCount();
		OrderedIntSet set = new OrderedIntSet();
		boolean firstTime = true;
		for (Iterator<Entry<IN_FGindexEdge, Integer>> iter = edgesofG
				.iterator(); iter.hasNext();) {
			// For each distinct edge in this graph
			Entry<IN_FGindexEdge, Integer> currentEntry = iter.next();
			// Add graph with id = graphIndex into the triple(graphSizeN,
			// frequencyM, id)
			// of a edgeEntries currentEndgeEntries associated with this edgeSet
			IN_FGindexEdgeEntries currentEdgeEntries = this.edgeIndex
					.get(currentEntry.getKey());
			if (currentEdgeEntries != null && !currentEdgeEntries.isFrequent()) {
				if (firstTime) {
					int[] result1 = currentEdgeEntries
							.getGraphsWithMinSizeMinFreq(graphSize,
									currentEntry.getValue());
					set.add(result1);
					firstTime = false;
				} else {
					int[] result2 = currentEdgeEntries
							.getGraphsWithMinSizeMinFreq(graphSize,
									currentEntry.getValue());
					set.join(result2);
				}
			}
		}
		int[] graphIDs = set.getItems();

		timeComponent[2] += System.currentTimeMillis() - start;
		return new GraphFetcherDB(this.gDB, graphIDs, false);
	}

	/************ Construction *******************************/
	/**
	 * Add a not Frequent Graph [database graph] into infrequent edge index
	 * 
	 * @param g
	 * @return
	 */
	private boolean insertGraph(Graph g, int gid) {
		// triple <edgeNum, edgeFrequency, graphIndex>
		Set<Entry<IN_FGindexEdge, Integer>> edgesofG = this.getAllEdges(g)
				.entrySet();
		for (Iterator<Entry<IN_FGindexEdge, Integer>> iter = edgesofG
				.iterator(); iter.hasNext();) {
			// For each distinct edge in this graph
			Entry<IN_FGindexEdge, Integer> currentEntry = iter.next();
			// Add graph with id = graphIndex into the triple(graphSizeN,
			// frequencyM, id)
			// of a edgeEntries currentEndgeEntries associated with this edgeSet
			IN_FGindexEdgeEntries currentEdgeEntries = this.edgeIndex
					.get(currentEntry.getKey());
			if (currentEdgeEntries == null) {
				// Create a new infrequent currentEdgeEntries
				currentEdgeEntries = new IN_FGindexEdgeEntries(false);
				this.edgeIndex.put(currentEntry.getKey(), currentEdgeEntries);
			}
			if (currentEdgeEntries.isFrequent())
				continue;
			else
				currentEdgeEntries.addGraph(g.getEdgeCount(),
						currentEntry.getValue(), gid);
		}
		return true;
	}

	private void sortIndex() {
		Set<Entry<IN_FGindexEdge, IN_FGindexEdgeEntries>> edges = this.edgeIndex
				.entrySet();
		for (Iterator<Entry<IN_FGindexEdge, IN_FGindexEdgeEntries>> eIt = edges
				.iterator(); eIt.hasNext();) {
			eIt.next().getValue().sortEntry();
		}

	}

	/**
	 * Insert indexing features
	 * 
	 * @param key
	 * @param currentEdgeEntries
	 */
	public void put(IN_FGindexEdge key, IN_FGindexEdgeEntries currentEdgeEntries) {
		this.edgeIndex.put(key, currentEdgeEntries);
	}

	/************************ Index Saving ******************************************/
	/**
	 * EdgeIndex: A, B, e(A, B), frequent/infrequent, long shift 2.
	 * EdgeIndexFile: Each line represent each EdgeEntries
	 * <Entry1><Entry2><Entry3> Entry1 = [graph
	 * size][edgeArray1][edgeArray2][edgeArray3] edgeArray1 = arraySize,
	 * graphIDshift 3. GraphIdshift id, id, id, id \n
	 * 
	 * @param index
	 * @param indexWriter
	 * @param edgeWriter
	 * @param graphsWriter
	 * @throws IOException
	 */
	public void writeEdges(BufferedWriter indexWriter, FileChannel edgeWriter,
			FileChannel graphsWriter) throws IOException {
		Set<Entry<IN_FGindexEdge, IN_FGindexEdgeEntries>> edgeSet = edgeIndex
				.entrySet();
		for (Iterator<Entry<IN_FGindexEdge, IN_FGindexEdgeEntries>> it = edgeSet
				.iterator(); it.hasNext();) {
			Entry<IN_FGindexEdge, IN_FGindexEdgeEntries> currentEntry = it
					.next();
			StringBuffer buf = new StringBuffer();
			// First write the edge
			buf.append(currentEntry.getKey().toString());
			buf.append(',');
			if (currentEntry.getValue().isFrequent())
				buf.append(1);
			else
				buf.append(0);
			buf.append(',');
			// The write the shift
			long shift = wirteEdgeEntry(currentEntry.getValue(), edgeWriter,
					graphsWriter);
			buf.append(shift);
			buf.append('\n');
			indexWriter.write(buf.toString());
		}
	}

	/**
	 * 2. EdgeIndexFile: Each line represent each EdgeEntries
	 * <Entry1><Entry2><Entry3> Entry1 = [graph
	 * size][edgeArray1][edgeArray2][edgeArray3] edgeArray1 = arraySize,
	 * graphIDshift 3. GraphIdshift id, id, id, id \n
	 * 
	 * @param entries
	 * @param edgeWriter
	 * @param graphsWriter
	 * @return
	 * @throws IOException
	 */
	private long wirteEdgeEntry(IN_FGindexEdgeEntries entries,
			FileChannel edgeWriter, FileChannel graphsWriter)
			throws IOException {
		IN_FGindexEdgeEntry[] entryArray = entries.getEdgeEntries();
		long position = edgeWriter.position();
		StringBuffer buf = new StringBuffer();
		// write each entry
		for (int i = 0; i < entryArray.length; i++) {
			IN_FGindexEdgeArray[] edgeArrays = entryArray[i].getEdgeArrays();
			buf.append('<');
			buf.append('[');
			buf.append(entryArray[i].getEntrySize()); // graph Size
			buf.append(']');
			// write each array
			for (int j = 0; j < edgeArrays.length; j++) {
				buf.append('[');
				buf.append(edgeArrays[j].getArrayFrequency());
				buf.append(',');
				long graphIDshift = writeGraphIDs(edgeArrays[j].getGraphIDs(),
						graphsWriter);
				buf.append(graphIDshift);
				buf.append(']');
			}
			buf.append('>');
		}
		buf.append('\n');
		byte[] bytes = buf.toString().getBytes();
		int start = 0;
		ByteBuffer bbuf = ByteBuffer.allocate(1024);
		int length = bbuf.capacity();
		while (start < bytes.length) {
			if (start + length <= bytes.length)
				bbuf.put(bytes, start, length);
			else
				bbuf.put(bytes, start, bytes.length - start);
			bbuf.flip();
			edgeWriter.write(bbuf);
			bbuf.clear();
			start = start + length;
		}
		return position;
	}

	private long writeGraphIDs(int[] ids, FileChannel graphsWriter)
			throws IOException {
		long position = graphsWriter.position();
		StringBuffer graphsBuf = new StringBuffer();
		graphsBuf.append(ids[0]);
		for (int i = 1; i < ids.length; i++) {
			graphsBuf.append(',');
			graphsBuf.append(ids[i]);
		}
		graphsBuf.append('\n');
		byte[] bytes = graphsBuf.toString().getBytes();
		int start = 0;
		ByteBuffer bbuf = ByteBuffer.allocate(1024);
		int length = bbuf.capacity();
		while (start < bytes.length) {
			if (start + length <= bytes.length)
				bbuf.put(bytes, start, length);
			else
				bbuf.put(bytes, start, bytes.length - start);
			bbuf.flip();
			graphsWriter.write(bbuf);
			bbuf.clear();
			start = start + length;
		}
		return position;
	}

	/*********************
	 * Index Loading
	 * 
	 * @throws IOException
	 ***********************/
	public void loadIndex(BufferedReader indexReader,
			RandomAccessFile edgeReader, RandomAccessFile graphReader)
			throws IOException {
		String edgeLine = indexReader.readLine();
		// this.edgeIndex = new HashMap<IN_FGindexEdge,
		// IN_FGindexEdgeEntries>();
		while (edgeLine != null) {
			String[] edgeTokens = edgeLine.split(",");
			int temp2 = Integer.parseInt(edgeTokens[3]);
			boolean isFrequent = true;
			if (temp2 == 0)
				isFrequent = false;
			IN_FGindexEdge oneEdge = new IN_FGindexEdge(
					Integer.parseInt(edgeTokens[0]),
					Integer.parseInt(edgeTokens[1]),
					Integer.parseInt(edgeTokens[2]));
			long entriesShift = Long.parseLong(edgeTokens[4]);
			IN_FGindexEdgeEntries oneEntries = loadEntries(entriesShift,
					edgeReader, graphReader, isFrequent);
			this.edgeIndex.put(oneEdge, oneEntries);
			edgeLine = indexReader.readLine();
		}
	}

	/**
	 * 2. EdgeIndexFile: Each line represent each EdgeEntries
	 * <Entry1><Entry2><Entry3> Entry1 = [graph
	 * size][edgeArray1][edgeArray2][edgeArray3] edgeArray1 = arraySize,
	 * graphIDshift 3. GraphIdshift id, id, id, id \n
	 * 
	 * @param shift
	 * @param edgeReader
	 * @param graphReader
	 * @return
	 * @throws IOException
	 */
	private IN_FGindexEdgeEntries loadEntries(long shift,
			RandomAccessFile edgeReader, RandomAccessFile graphReader,
			boolean isFrequent) throws IOException {
		edgeReader.seek(shift);
		String line = edgeReader.readLine();
		String[] entryTokens = line.substring(1, line.length() - 1).split("><");
		IN_FGindexEdgeEntry[] edgeEntries = new IN_FGindexEdgeEntry[entryTokens.length];
		for (int i = 0; i < edgeEntries.length; i++) {
			// For each EdgeEntry, find its corresponding edge array
			String entryLine = entryTokens[i].substring(1,
					entryTokens[i].length() - 1);

			String[] arrayTokens = entryLine.split("\\]\\[");
			int graphSize = Integer.parseInt(arrayTokens[0]);
			IN_FGindexEdgeArray[] edgeArrays = new IN_FGindexEdgeArray[arrayTokens.length - 1];
			for (int j = 0; j < edgeArrays.length; j++) {
				String[] inEachArray = arrayTokens[j + 1].split(",");
				int arraySize = Integer.parseInt(inEachArray[0]);
				long idShift = Long.parseLong(inEachArray[1]);
				int[] graphIds = loadGraphIDs(idShift, graphReader);
				edgeArrays[j] = new IN_FGindexEdgeArray(arraySize, graphIds,
						true, j);
			}
			edgeEntries[i] = new IN_FGindexEdgeEntry(graphSize, i, edgeArrays);
		}
		return new IN_FGindexEdgeEntries(edgeEntries, isFrequent);
	}

	private int[] loadGraphIDs(long shift, RandomAccessFile graphReader)
			throws IOException {
		graphReader.seek(shift);
		String line = graphReader.readLine();
		String[] tokens = line.split(",");
		int[] graphs = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++)
			graphs[i] = Integer.parseInt(tokens[i]);
		return graphs;
	}

	/**
	 * Return true if the input graph contains infrequent edges
	 * 
	 * @param theGraph
	 * @return
	 */
	public boolean containInfrequentEdges(Graph theGraph) {
		Set<Entry<IN_FGindexEdge, Integer>> edgesofG = this.getAllEdges(
				theGraph).entrySet();
		for (Iterator<Entry<IN_FGindexEdge, Integer>> iter = edgesofG
				.iterator(); iter.hasNext();) {
			// For each distinct edge in this graph
			Entry<IN_FGindexEdge, Integer> currentEntry = iter.next();
			// Add graph with id = graphIndex into the triple(graphSizeN,
			// frequencyM, id)
			// of a edgeEntries currentEndgeEntries associated with this edgeSet
			IN_FGindexEdgeEntries currentEdgeEntries = this.edgeIndex
					.get(currentEntry.getKey());
			if (currentEdgeEntries.isFrequent())
				continue;
			else
				return true; // contain infrequent edges
		}
		return false; // does not contain infrequent edges
	}
}
