package edu.psu.chemxseer.structure.iso;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.graph.MutableGraph;
import de.parmol.parsers.GraphParser;
import de.parmol.parsers.SLNParser;

public class CanonicalDFS implements GraphParser {
	@Override
	public Graph parse(String text, GraphFactory factory) {
		return parse(text, null, factory);
	}

	public Graph parse(String text, String gID, GraphFactory factory) {
		int[][] sequence = parseTextToArray(text);
		HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
		MutableGraph g = factory.createGraph(gID);
		return this.parse(sequence, g, nodeMap);
	}

	public Graph parse(int[][] sequence, GraphFactory factory) {
		HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
		MutableGraph g = factory.createGraph(null);
		return parse(sequence, g, nodeMap);
	}

	public Graph parse(MutableGraph prefGraph, String suffix,
			GraphFactory factory) {
		// assume that the prefix graph prefGraph's node order is coherent witht
		// he suffix string
		int[][] sequence = this.parseTextToArray(suffix);
		HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < prefGraph.getNodeCount(); i++)
			nodeMap.put(i, i);
		return this.parse(sequence, prefGraph, nodeMap);
	}

	private Graph parse(int[][] sequence, MutableGraph g,
			Map<Integer, Integer> nodeMap) {
		// Add the first edge or first Node Edge
		if (sequence[0][0] != -1) {
			int nodeID = g.addNode(sequence[0][2]);
			nodeMap.put(sequence[0][0], nodeID);
			if (sequence[0][1] != -1) {
				nodeID = g.addNodeAndEdge(sequence[0][0], sequence[0][4],
						sequence[0][3]);
				nodeMap.put(sequence[0][1], nodeID);
			}
		} else {
			if (sequence[0][1] != -1) {
				int nodeID = g.addNode(sequence[0][4]);
				nodeMap.put(sequence[0][1], nodeID);
			}
		}
		// Dealing with the first entry
		if (sequence.length == 1)
			return g;

		for (int i = 1; i < sequence.length; i++) {
			Integer node1ID = nodeMap.get(sequence[i][0]);
			Integer node2ID = nodeMap.get(sequence[i][1]);
			if (node1ID == null && node2ID == null)
				System.out.println("This Graph is Not Connected");
			if (node1ID != null && node2ID == null) {
				int nodeID = g.addNodeAndEdge(node1ID, sequence[i][4],
						sequence[i][3]);
				nodeMap.put(sequence[i][1], nodeID);
			} else if (node1ID == null && node2ID != null) {
				int nodeID = g.addNodeAndEdge(node2ID, sequence[i][2],
						sequence[i][3]);
				nodeMap.put(sequence[i][0], nodeID);
			} else if (node1ID != null && node2ID != null)
				g.addEdge(node1ID, node2ID, sequence[i][3]);
			else
				System.out
						.println("Error in Canoinical DFS parser: redundant edge");
		}
		g.saveMemory();
		return g;
	}

	public int[][] parseTextToArray(String text) {
		String[] entries = text.split("><");
		int[][] sequence = new int[entries.length][5];
		// The first and last entry need to be dealt specially
		entries[0] = entries[0].substring(1);
		entries[entries.length - 1] = entries[entries.length - 1].substring(0,
				entries[entries.length - 1].length() - 1);
		String[] temp;
		for (int i = 0; i < entries.length; i++) {
			temp = entries[i].split(" ");
			for (int j = 0; j < temp.length; j++)
				sequence[i][j] = Integer.parseInt(temp[j]);
		}
		return sequence;
	}

	public String writeArrayToText(int[][] array) {
		StringBuffer buf = new StringBuffer(1024);
		if (array == null)
			System.out.println("aya");
		for (int i = 0; i < array.length; i++) {
			buf.append('<');
			buf.append(array[i][0]);
			buf.append(' ');
			buf.append(array[i][1]);
			buf.append(' ');
			buf.append(array[i][2]);
			buf.append(' ');
			buf.append(array[i][3]);
			buf.append(' ');
			buf.append(array[i][4]);
			buf.append('>');
		}
		return buf.toString();
	}

	@Override
	public String serialize(Graph g) {
		int[][] minSequence = this.serializeToArray(g);
		return writeArrayToText(minSequence);
	}

	public int[][] serializeToArray(Graph g) {
		CanonicalDFSImplInternal temp = new CanonicalDFSImplInternal();
		boolean exception = temp.dealCornerCase(g);
		if (exception == false)
			return temp.minSequence;
		else {
			temp.initialize(g);
			temp.findSerialization();
			return temp.minSequence;
		}
	}

	public String serialize(int[][] graphConnectivity, int[] graphEdgelabel) {
		CanonicalDFSImplInternal temp = new CanonicalDFSImplInternal();
		boolean exception = temp.dealCornerCase(graphConnectivity,
				graphEdgelabel);
		if (exception == false)
			this.writeArrayToText(temp.minSequence);
		else {
			temp.initialize(graphConnectivity, graphEdgelabel);
			temp.findSerialization();
			this.writeArrayToText(temp.minSequence);
		}
		return this.writeArrayToText(temp.minSequence);
	}

	@Override
	public void serialize(Graph[] graphs, OutputStream out) throws IOException {
		BufferedOutputStream bout = new BufferedOutputStream(out);
		for (int i = 0; i < graphs.length; i++) {
			bout.write(graphs[i].getName().getBytes());
			bout.write(" => ".getBytes());
			bout.write(serialize(graphs[i]).getBytes());
			bout.write("\n".getBytes());
		}
		bout.flush();
	}

	@Override
	public Graph[] parse(InputStream in, GraphFactory factory)
			throws IOException, ParseException {
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		LinkedList<Graph> graphs = new LinkedList<Graph>();
		String line;
		while ((line = bin.readLine()) != null) {
			int pos = line.indexOf(" => ");

			graphs.add(parse(line.substring(pos + " => ".length()),
					line.substring(0, pos), factory));
		}
		return graphs.toArray(new Graph[graphs.size()]);
	}

	@Override
	public int getDesiredGraphFactoryProperties() {
		return GraphFactory.UNDIRECTED_GRAPH;
	}

	@Override
	public String getNodeLabel(int nodeLabel) {
		return SLNParser.ATOM_SYMBOLS[nodeLabel];
	}

	@Override
	public boolean directed() {
		return false;
	}

	/**
	 * Serialize a Graph into one DFS code, not necessary to be the minimum
	 * 
	 * @param g
	 * @return
	 */
	public String serializeNonCanonical(Graph g) {
		if (g == null || g.getNodeCount() == 0)
			return new String(); // empty string returned for illegal input
		else {
			int nodeCount = g.getNodeCount();
			int edgeCount = g.getEdgeCount();
			if (edgeCount == 0) {
				if (nodeCount > 1) {
					System.out.println("Not Connected Graph, Illigal Input");
					return new String();
				} else {
					int[][] result = new int[1][];
					result[0] = new int[] { 0, -1, g.getNodeLabel(0), -1, -1 };
					return this.writeArrayToText(result);
				}
			} else {
				int[][] result = new int[edgeCount][];
				int edgeIndex = 0;
				for (int nodeA = 0; nodeA < nodeCount; nodeA++) {
					int degree = g.getDegree(nodeA);
					for (int i = 0; i < degree; i++) {
						int adjEdge = g.getNodeEdge(nodeA, i);
						int nodeB = g.getOtherNode(adjEdge, nodeA);
						if (nodeA < nodeB)
							result[edgeIndex++] = new int[] { nodeA, nodeB,
									g.getNodeLabel(nodeA),
									g.getEdgeLabel(adjEdge),
									g.getNodeLabel(nodeB) };
					}
				}
				return this.writeArrayToText(result);
			}
		}
	}

}
