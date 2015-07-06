package edu.psu.chemxseer.structure.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.parsers.SmilesParser;
import edu.psu.chemxseer.structure.parmolExtension.SDFParserModified;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;

public class PreProcessTools {
	/**
	 * Given a database file downloaded from eMolecuels: <smiles id> Change the
	 * format of the file to: <id => smiles>
	 * 
	 * @param eMoleculesFile
	 * @param SmilesFileName
	 * @throws IOException
	 */
	public static void changeStyle(String eMoleculesFile, String SmilesFileName)
			throws IOException {
		System.out.println("IN ChangeSyle");
		String spliter = " => ";
		// Open InputFile
		BufferedReader fileBufReader = new BufferedReader(new FileReader(
				eMoleculesFile));
		// Open OutputFile
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(
				SmilesFileName));
		String lineString;
		int graphCount = 0;
		String lineToken[];
		// Skip the first line
		fileBufReader.readLine();
		while ((lineString = fileBufReader.readLine()) != null) {
			// Test whether this graph is connected
			lineToken = lineString.split(" ");
			if (lineToken[0].contains("\\")) {
				lineToken[0].replace("\\", "\\\\");
			}
			if (lineToken[0].contains("*") || lineToken[0].contains("+"))
				continue;
			// 11340785: must have to remove: 2nd
			// 29547010: 1st
			// 712990: warning
			if (lineToken[1].equals("11340785")
					|| lineToken[1].equals("29547004")
					|| lineToken[1].equals("712990")
					|| lineToken[1].equals("713014")
					|| lineToken[1].equals("716534"))
				continue;

			Graph g = null;
			try {
				g = MyFactory.getSmilesParser().parse(lineToken[0],
						MyFactory.getGraphFactory());
			} catch (Exception e) {
				System.out.println("Things that I do not want to see");
				System.out.println(lineToken[0]);
				System.out.println(lineToken[1]);
				e.printStackTrace();
			}

			if (g == null)
				System.out.println("Things that santa do not want to see"
						+ lineString);
			else if (!GraphConnectivityTester.isConnected(g))
				continue;
			outputWriter.write(graphCount++ + spliter + lineToken[0] + "\n");
		}
		// Close input File
		try {
			fileBufReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Close out File
		try {
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("In processor: changeStyle, " + graphCount
				+ " number of graphs" + "has been formated into Smiles Format");
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		// Write the meta information of the smile data file:
		System.out.println("Wrtier Metat Data");
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				SmilesFileName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + graphCount);
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a file of graphs in SDF format Populate graphs in this file into A
	 * new graph file [with ID = > in Smiles format] Pay attention: all graphs
	 * have to be connected
	 * 
	 * @param SDFFileName
	 * @param graphFactory
	 * @throws IOException
	 * @throws DataAccessException
	 * @throws IOException
	 */
	public static void changeFormat(String SDFFileName, String SmilesFileName,
			GraphFactory graphFactory) throws IOException {
		// Open InputFile
		BufferedReader fileBufReader = new BufferedReader(new FileReader(
				SDFFileName));
		// Open OutputFile
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(
				SmilesFileName));

		SDFParserModified SDFParser = MyFactory.getSDFParserM();
		SmilesParser smilesParser = MyFactory.getSmilesParser();
		String lineString;
		StringBuffer graphBuffer = new StringBuffer(1024);
		Graph oneGraph = null;

		int index = 0;
		String spliter = " => ";
		boolean attStatus = false;
		while ((lineString = fileBufReader.readLine()) != null) {
			if (lineString.startsWith(">")) {
				// enter the attribute status
				attStatus = true;
			}
			if (attStatus) {
				// One graph Has been read
				if (attStatus && lineString.equals("$$$$")) {
					// Transform this graphString to a realGraph and save into
					// database
					try {
						oneGraph = SDFParser.parse(graphBuffer.toString(),
								graphFactory);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					if (GraphConnectivityTester.isConnected(oneGraph)) {
						if (index > 0)
							outputWriter.newLine();
						outputWriter.write(index + spliter
								+ smilesParser.serialize(oneGraph));
						index++;
						// System.out.println(index + ":" + oneGraph.getName());
					}

					graphBuffer.delete(0, graphBuffer.length());
					attStatus = false;
				} else
					continue;
			} else
				graphBuffer = graphBuffer.append(lineString + '\n');
		}
		// Close input File
		try {
			fileBufReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Close out File
		try {
			outputWriter.newLine();
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("In processor: changeFormat, " + index
				+ " number of graphs" + "has been formated into Smiles Format");
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		// Write the meta information of the smile data file:
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				SmilesFileName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + index);
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void changeFormatSimple(String SDFFileName,
			String SmilesFileName, GraphFactory graphFactory)
			throws IOException {
		// Open InputFile
		BufferedReader fileBufReader = new BufferedReader(new FileReader(
				SDFFileName));
		// Open OutputFile
		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(
				SmilesFileName));

		SDFParserModified SDFParser = MyFactory.getSDFParserM();
		SmilesParser smilesParser = MyFactory.getSmilesParser();
		Graph oneGraph = null;

		double aveEdgeCount = 0;
		double aveNodeCount = 0;

		int index = 0;
		String spliter = " => ";
		while (fileBufReader.ready()) {
			try {
				oneGraph = SDFParser.parse(fileBufReader, graphFactory);
			} catch (ParseException e) {
				System.out.println("skip one graph");
				while (fileBufReader.ready()) {
					String aLine = fileBufReader.readLine();
					if (aLine.equals("$$$$"))
						break;
				}
				continue;
			}

			if (GraphConnectivityTester.isConnected(oneGraph)) {
				aveEdgeCount = (aveEdgeCount)
						* (index / ((double) index + 1))
						+ oneGraph.getEdgeCount() / ((double) index + 1);
				aveNodeCount = (aveNodeCount)
						* (index / ((double) index + 1))
						+ oneGraph.getNodeCount() / ((double) index + 1);
				if (index > 0)
					outputWriter.newLine();
				outputWriter.write(index + spliter
						+ smilesParser.serialize(oneGraph));
				index++;
			}

		}
		// Close input File
		try {
			fileBufReader.close();
			outputWriter.flush();
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("In processor: changeFormat, " + index
				+ " number of graphs" + "has been formated into Smiles Format");
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		// Write the meta information of the smile data file:
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				SmilesFileName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + index);
		metaWriter.write("Ave EdgeCount: " + aveEdgeCount + " Ave NodeCount: "
				+ aveNodeCount);
		// Close meta data file
		try {
			metaWriter.flush();
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param graphDBNameOld
	 * @param graphDBName
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void pruningUnconnected(String graphDBNameOld,
			String graphDBName) throws IOException, ParseException {
		System.out.println("In Pruning Unconnected");
		SmilesParser sParser = new SmilesParser();
		// InputStream file = new FileInputStream(graphDBNameOld);
		// Graph[] graphs = sParser.parse(file, MyFactory.getGraphFactory());
		BufferedReader bin = new BufferedReader(new FileReader(graphDBNameOld));

		LinkedList<Graph> graphlists = new LinkedList<Graph>();
		String line;
		while ((line = bin.readLine()) != null) {
			int pos = line.indexOf(" => ");
			try {
				graphlists.add(sParser.parse(
						line.substring(pos + " => ".length()),
						line.substring(0, pos), MyFactory.getGraphFactory()));
				System.out.println("GraphID: " + line.substring(0, pos));
			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}
		}

		Graph[] graphs = new Graph[graphlists.size()];
		graphlists.toArray(graphs);
		System.out.println("After Parsing all Graphs");
		int connect = 0;
		BufferedWriter indexWriter = new BufferedWriter(new FileWriter(
				graphDBName));
		for (int i = 0; i < graphs.length; i++) {
			if (GraphConnectivityTester.isConnected(graphs[i])) {
				StringBuffer buf = new StringBuffer();
				buf.append(connect);
				buf.append(" => ");
				buf.append(sParser.serialize(graphs[i]));
				buf.append("\n");
				indexWriter.write(buf.toString());
				connect++;
			}
		}
		indexWriter.close();
		// Write the meta information of the smile data file:
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				graphDBName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + connect);
		// Close meta data file
		try {
			metaWriter.close();
			bin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("TOTAL Graph: " + graphs.length + "Connected : "
				+ connect);

	}

	/*
	 * public static void main(String[] args){ String SDFFile =
	 * "/Users/dayuyuan/Downloads/NCI-Open_2012-05-01.sdf"; String SmilesFile =
	 * "/Users/dayuyuan/Downloads/NCI-Open_2012-05-01.smiles"; //String SDFFile
	 * = "/Users/dayuyuan/Desktop/NCI.txt"; //String SmilesFile =
	 * "/Users/dayuyuan/Desktop/NCISmiles.txt"; try {
	 * PreProcessTools.changeFormatSimple(SDFFile, SmilesFile,
	 * MyFactory.getGraphFactory()); } catch (IOException e) {
	 * e.printStackTrace(); } GraphDatabase_OnDisk gDB = new
	 * GraphDatabase_OnDisk(SmilesFile, MyFactory.getSmilesParser());
	 * statistics(gDB); }
	 */

	public static void statistics(IGraphDatabase gDB) {
		int[] stat = new int[100];
		Arrays.fill(stat, 0);
		for (int i = 0; i < gDB.getTotalNum(); i++) {
			int edgeCount = gDB.findGraph(i).getEdgeCount();
			if (edgeCount > 99)
				++stat[99];
			else
				++stat[edgeCount];
		}
		for (int i = 0; i < 100; i++) {
			System.out.println("edge" + i + " : " + stat[i]);
		}
	}
}
