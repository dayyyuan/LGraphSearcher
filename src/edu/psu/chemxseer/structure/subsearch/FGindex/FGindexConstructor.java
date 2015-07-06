package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * IN-memory IGI constructor
 * 
 * @author dayuyuan
 * 
 */
public class FGindexConstructor {
	/**
	 * on-disk constructor Does not populate the infrequent database graph
	 * 
	 * @param features
	 */
	public static FGindexSearcher constructOnDisk(
			Collection<IOneFeature> features) {
		String[] graphArray = new String[features.size()];
		EdgeIndex edgeIndex = new EdgeIndex();
		int[] maxGraphSize = new int[1];
		maxGraphSize[0] = 0;
		int count = 0;
		for (Iterator<IOneFeature> it = features.iterator(); it.hasNext();) {
			addFeature(graphArray, it.next(), edgeIndex, maxGraphSize, count++);
		}
		return new FGindexSearcher(graphArray, maxGraphSize[0], edgeIndex);
	}

	/**
	 * In-memory constructor
	 * 
	 * @param features
	 * @param gDB
	 * @return
	 */
	public static FGindexSearcher constructInMem(
			Collection<IOneFeature> features, GraphDatabase_OnDisk gDB) {
		String[] graphArray = new String[features.size()];
		EdgeIndex edgeIndex = new EdgeIndex(gDB);
		int[] maxGraphSize = new int[1];
		maxGraphSize[0] = 0;
		int count = 0;
		for (IOneFeature oneFeature : features) {
			addFeature(graphArray, oneFeature, edgeIndex, maxGraphSize, count++);
		}
		try {
			edgeIndex.populateInfrequentGraphs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new FGindexSearcher(graphArray, maxGraphSize[0], edgeIndex);
	}

	/**
	 * Insert one Feature into the index, update the edgeIndex & also the
	 * graphArray
	 * 
	 * @param oneFeature
	 * @param edgeIndex
	 * @return
	 */
	private static void addFeature(String[] graphArray, IOneFeature oneFeature,
			EdgeIndex edgeIndex, int[] maxGraphSize, int ID) {
		// Add one TCFG feature into the IGI of FGindex
		// 1. Add the graph into graphArray
		graphArray[ID] = oneFeature.getDFSCode();
		Graph g = oneFeature.getFeatureGraph();
		if (g.getEdgeCount() > maxGraphSize[0])
			maxGraphSize[0] = g.getEdgeCount();

		// 2. Find all distinct edges of this graph, and add this graph into
		// triple <edgeNum, edgeFrequency, graphIndex>
		Set<Entry<IN_FGindexEdge, Integer>> edgesofG = edgeIndex.getAllEdges(g)
				.entrySet();

		for (Iterator<Entry<IN_FGindexEdge, Integer>> iter = edgesofG
				.iterator(); iter.hasNext();) {
			// For each distinct edge in this graph
			Entry<IN_FGindexEdge, Integer> currentEntry = iter.next();
			// Add graph with id = graphIndex into the triple(graphSizeN,
			// frequencyM, id)
			// of a edgeEntries currentEndgeEntries associated with this edgeSet
			IN_FGindexEdgeEntries currentEdgeEntries = edgeIndex
					.get(currentEntry.getKey());
			if (currentEdgeEntries == null) {
				// Add a frequent one
				currentEdgeEntries = new IN_FGindexEdgeEntries(true);
				edgeIndex.put(currentEntry.getKey(), currentEdgeEntries);
			}
			currentEdgeEntries.addGraph(oneFeature.getFeatureGraph()
					.getEdgeCount(), currentEntry.getValue(), ID);
		}
	}

	public static void saveSearcher(FGindexSearcher searcher, String baseName,
			String indexFileName) throws IOException {
		BufferedWriter indexWriter = new BufferedWriter(new FileWriter(
				new File(baseName, indexFileName)));
		@SuppressWarnings("resource")
		FileChannel edgeIndexWriter = new FileOutputStream(baseName
				+ indexFileName + "_edge").getChannel();
		@SuppressWarnings("resource")
		FileChannel graphIDsWriter = new FileOutputStream(baseName
				+ indexFileName + "_db").getChannel();
		// a. Write graphs
		writeFeatures(searcher, indexWriter);
		// b. Write edges index
		searcher.edgeIndex.writeEdges(indexWriter, edgeIndexWriter,
				graphIDsWriter);
		graphIDsWriter.close();
		edgeIndexWriter.close();
		indexWriter.close();
	}

	private static void writeFeatures(FGindexSearcher searcher,
			BufferedWriter indexWriter) {
		StringBuffer buf = new StringBuffer();
		buf.append(searcher.graphArray.length);
		buf.append(',');
		buf.append(searcher.maxGraphSize);
		buf.append('\n');
		try {
			indexWriter.write(buf.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < searcher.graphArray.length; i++) {
			buf.delete(0, buf.length());
			buf.append(i);
			buf.append(',');
			buf.append(searcher.graphArray[i]);
			buf.append('\n');
			try {
				indexWriter.write(buf.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * load the FGindex file from disk, into 3 file: 1. IndexFile First Line:
	 * size StartFome the second Line: id, graphs in graphArray [smiles]
	 * EdgeIndex: A, B, e(A, B),isSelected, long shift 2. EdgeIndexFile: Each
	 * line represent each EdgeEntries <Entry1><Entry2><Entry3> Entry1 = [graph
	 * size][edgeArray1][edgeArray2][edgeArray3] edgeArray1 = arraySize,
	 * graphIDshift 3. GraphIdshift id, id, id, id \n
	 * 
	 * @param indexFile, edgeIndexFile, graphIDsFile
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * 
	 * @throws IOException
	 */
	public static FGindexSearcher loadSearcher(String baseName,
			String indexFileName, IGraphDatabase gDB) throws IOException {
		BufferedReader indexReader = new BufferedReader(new FileReader(
				new File(baseName, indexFileName)));

		// Then create to random access file
		String edgeIndexName = baseName + indexFileName + "_edge";
		String graphIdsName = baseName + indexFileName + "_db";
		RandomAccessFile edgeReader = new RandomAccessFile(edgeIndexName, "r");
		RandomAccessFile graphReader = new RandomAccessFile(graphIdsName, "r");

		// Read graphs and construct a graph array
		String aLine = indexReader.readLine();
		String[] tokens = aLine.split(",");
		int featureCount = Integer.parseInt(tokens[0]);
		int maxGraphSize = Integer.parseInt(tokens[1]);

		String[] graphArray = new String[featureCount];
		loadGraphs(graphArray, indexReader);

		// Edge Index
		EdgeIndex edgeIndex = new EdgeIndex(gDB);
		edgeIndex.loadIndex(indexReader, edgeReader, graphReader);
		indexReader.close();
		edgeReader.close();
		graphReader.close();
		return new FGindexSearcher(graphArray, maxGraphSize, edgeIndex);
	}

	/**
	 * StartFome the second Line: id, graphs in graphArray [smiles]
	 * 
	 * @param graphArray
	 * @param indexReader
	 * @throws IOException
	 */
	private static void loadGraphs(String[] graphArray,
			BufferedReader indexReader) throws IOException {
		String line = indexReader.readLine();
		int index = 0;
		while (line != null) {
			String[] token = line.split(",");
			int id = Integer.parseInt(token[0]);
			graphArray[id] = token[1];
			++index;
			if (index == graphArray.length)
				break;
			line = indexReader.readLine();
		}
	}
}
