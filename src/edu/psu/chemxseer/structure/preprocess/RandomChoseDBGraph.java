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
import java.util.Random;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.FGindex.EdgeIndex;

public class RandomChoseDBGraph {

	public static void saveGDB(String[] gString, String fileName)
			throws IOException {
		// First Step: filter out graphs that are not in between the boundary
		BufferedWriter dbWriter = new BufferedWriter(new FileWriter(fileName));
		for (int i = 0; i < gString.length; i++) {
			dbWriter.write(i + " => " + gString[i] + "\n");
		}
		dbWriter.close();
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		// Write the meta information of the new file
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(fileName
				+ "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + gString.length);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + 0 + ", Average NodeNum: " + 0);
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveGDB(Graph[] graphs, GraphParser gParser,
			String fileName) throws IOException {
		// First Step: filter out graphs that are not in between the boundary
		BufferedWriter dbWriter = new BufferedWriter(new FileWriter(fileName));
		int count = 0;
		float edgeNum = 0, nodeNum = 0;
		for (int i = 0; i < graphs.length; i++) {
			Graph theGraph = graphs[i];
			dbWriter.write(count++ + " => " + gParser.serialize(theGraph)
					+ "\n");
			edgeNum += theGraph.getEdgeCount();
			nodeNum += theGraph.getNodeCount();
		}
		dbWriter.close();
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		// Write the meta information of the new file
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(fileName
				+ "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + count);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + (edgeNum) / count
				+ ", Average NodeNum: " + (nodeNum) / count);
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a graph database gDB, first prune distinct edges with the edgeIndex
	 * Then randomly selected chooseN graphs
	 * 
	 * @param gDB
	 * @param chooseN
	 * @param pruneDistinctEdges
	 * @param edgeIndex
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public static Graph[] randomlyChooseDBGraph(GraphDatabase_OnDisk gDB,
			int chooseN, boolean pruneDistinctEdges, EdgeIndex edgeIndex)
			throws IOException, ParseException {
		if (pruneDistinctEdges == false)
			return randomlyChooseDBGraph(gDB, chooseN);
		// First Step: filter out graphs that are not in between the boundary
		String tempFileName = gDB.getDBFileName() + "_temp";
		BufferedWriter tempDBWriter = new BufferedWriter(new FileWriter(
				tempFileName));
		int count = 0;
		float edgeNum = 0, nodeNum = 0;
		for (int i = 0; i < gDB.getTotalNum(); i++) {
			Graph theGraph = gDB.findGraph(i);
			if (edgeIndex.containInfrequentEdges(theGraph))
				continue;
			else {
				tempDBWriter.write(count++ + " => " + gDB.findGraphString(i)
						+ "\n");
				edgeNum += theGraph.getEdgeCount();
				nodeNum += theGraph.getNodeCount();
			}
		}
		tempDBWriter.close();
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		// Write the meta information of the new file
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				tempFileName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + count);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + (edgeNum) / count
				+ ", Average NodeNum: " + (nodeNum) / count);
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return randomlyChooseDBGraph(
				new GraphDatabase_OnDisk(tempFileName, gDB.getParser()),
				chooseN);

	}

	/**
	 * Given a graph database gDB, randomly chooseN graphs, then return those
	 * graphs
	 * 
	 * @param gDB
	 * @param chooseN
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static Graph[] randomlyChooseDBGraph(IGraphDatabase gDB, int chooseN)
			throws ParseException, IOException {
		// First get the number of graphs in this database, which can be found
		// in the metadata file
		int m = gDB.getTotalNum();
		// System.out.println("Choose " + chooseN + " from " + m);
		if (m < chooseN) {
			System.out
					.println("There is not need to choseN, the database is smaller than chooseN");
			return null;
		}
		// Then randomly select n from m: the first chooseN indexes are sorted
		// as stored in indexes
		int[] indexes = new int[m];
		for (int i = 0; i < m; i++)
			indexes[i] = i;
		Random rd = new Random();
		int j = 0;
		int swapTemp = 0;
		;
		for (int i = 0; i < chooseN; i++) {
			j = (int) (rd.nextFloat() * (m - i)) + i;
			swapTemp = indexes[i];
			indexes[i] = indexes[j];
			indexes[j] = swapTemp;
		}
		Arrays.sort(indexes, 0, chooseN);
		// Read those db graphs and save them into the new file
		Graph[] results = new Graph[chooseN];
		for (int i = 0; i < chooseN; i++) {
			int gID = indexes[i];
			results[i] = gDB.findGraph(gID);
		}

		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		return results;
	}

	/**
	 * Given a graph database gDB, randomly choose N graphs, and then store them
	 * in the chosen database
	 * 
	 * @param gDB
	 * @param chooseN
	 * @param chosenDBName
	 * @return
	 * @throws IOException
	 */
	public static void randomlyChooseDBGraph(IGraphDatabase gDB, int chooseN,
			String chosenDBName) throws IOException {
		// First get the number of graphs in this database, which can be found
		// in the metadata file
		int m = gDB.getTotalNum();
		System.out.println("Choose " + chooseN + " from " + m);
		int step = chooseN;
		if (m < chooseN) {
			System.out
					.println("There is not need to choseN, the database is smaller than chooseN, Merge DB");
			step = m / 2;
		}
		// Then randomly select n from m: the first chooseN indexes are sorted
		// as stored in indexes
		BufferedWriter chosenDBWriter = new BufferedWriter(new FileWriter(
				chosenDBName));
		int realChooseN = 0;
		while (realChooseN < chooseN) {
			int[] indexes = new int[m];
			for (int i = 0; i < m; i++)
				indexes[i] = i;
			Random rd = new Random();
			int j = 0;
			int swapTemp = 0;
			;
			for (int i = 0; i < step; i++) {
				j = (int) (rd.nextFloat() * (m - i)) + i;
				swapTemp = indexes[i];
				indexes[i] = indexes[j];
				indexes[j] = swapTemp;
			}
			Arrays.sort(indexes, 0, step);
			String spliter = " => ";
			// Read those db graphs and save them into the new file
			for (int i = 0; i < step; i++) {
				int gID = indexes[i];
				String gString = gDB.findGraphString(gID);
				chosenDBWriter.write(i + spliter + gString);
				chosenDBWriter.newLine();
			}
			realChooseN += step;
			if (realChooseN + step > chooseN)
				step = chooseN - realChooseN;
		}
		chosenDBWriter.close();
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();

		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				chosenDBName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + chooseN);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + 0 + ", Average NodeNum: " + 0);
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void randomlySplitDBGraph(IGraphDatabase gDB, int chooseN,
			String chosenDBName, String leftDBName) throws IOException {
		// First get the number of graphs in this database, which can be found
		// in the metadata file
		int m = gDB.getTotalNum();
		System.out.println("Choose " + chooseN + " from " + m);
		if (m < chooseN) {
			System.out
					.println("There is not need to choseN, the database is smaller than chooseN");
			return;
		}
		// Then randomly select n from m: the first chooseN indexes are sorted
		// as stored in indexes
		int[] indexes = new int[m];
		for (int i = 0; i < m; i++)
			indexes[i] = i;
		Random rd = new Random();
		int j = 0;
		int swapTemp = 0;
		;
		for (int i = 0; i < chooseN; i++) {
			j = (int) (rd.nextFloat() * (m - i)) + i;
			swapTemp = indexes[i];
			indexes[i] = indexes[j];
			indexes[j] = swapTemp;
		}
		Arrays.sort(indexes, 0, chooseN);
		Arrays.sort(indexes, chooseN, m);
		BufferedWriter chosenDBWriter = new BufferedWriter(new FileWriter(
				chosenDBName));
		String spliter = " => ";
		// Read those db graphs and save them into the new file
		for (int i = 0; i < chooseN; i++) {
			int gID = indexes[i];
			String gString = gDB.findGraphString(gID);
			chosenDBWriter.write(i + spliter + gString);
			chosenDBWriter.newLine();
		}
		chosenDBWriter.close();
		// Read those db graphs and save them into the new file
		BufferedWriter leftDBWriter = new BufferedWriter(new FileWriter(
				leftDBName));
		for (int i = chooseN; i < m; i++) {
			int gID = indexes[i];
			String gString = gDB.findGraphString(gID);
			leftDBWriter.write((i - chooseN) + spliter + gString);
			leftDBWriter.newLine();
		}
		leftDBWriter.close();
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();

		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				chosenDBName + "_Meta"));
		BufferedWriter metaWriter2 = new BufferedWriter(new FileWriter(
				leftDBName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		metaWriter2.write(bartDateFormat.format(date));
		metaWriter2.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + chooseN);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + 0 + ", Average NodeNum: " + 0);
		metaWriter2.write("Number of Graphs:" + (m - chooseN));
		metaWriter2.newLine();
		metaWriter2.write("Average EdgeNum: " + 0 + ", Average NodeNum: " + 0);
		// Close meta data file
		try {
			metaWriter.close();
			metaWriter2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param smilesDBFile
	 * @param chooseN
	 * @param chosenDBFile
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void randomlyChooseTwoDBGraph(String smilesDBFile,
			int chooseN, String chosenDBFile) throws ParseException,
			IOException {
		// First get the number of graphs in this database, which can be found
		// in the metadata file
		BufferedReader metaFile = new BufferedReader(new FileReader(
				smilesDBFile + "_Meta"));
		metaFile.readLine();
		String[] temp = metaFile.readLine().split(":");
		int m = Integer.parseInt(temp[1]);
		metaFile.close();
		int edgeNum = 0, nodeNum = 0;
		// Then randomly select n from m: the first chooseN indexes are sorted
		// as stored in indexes
		int[] indexes = new int[m];
		for (int i = 0; i < m; i++)
			indexes[i] = i;
		Random rd = new Random();
		int j = 0;
		int swapTemp = 0;
		;
		for (int i = 0; i < 2 * chooseN; i++) {
			j = (int) (rd.nextFloat() * (m - i)) + i;
			swapTemp = indexes[i];
			indexes[i] = indexes[j];
			indexes[j] = swapTemp;
		}
		Arrays.sort(indexes, 0, 2 * chooseN);
		// Read those db graphs and save them into the new file
		BufferedReader fullDBReader = new BufferedReader(new FileReader(
				smilesDBFile));
		BufferedWriter chosenDBWriter1 = new BufferedWriter(new FileWriter(
				chosenDBFile + "_1"));
		BufferedWriter chosenDBWriter2 = new BufferedWriter(new FileWriter(
				chosenDBFile + "_2"));
		int fileLineIndex = 0;
		int i = 0;
		String aLine = null;
		String spliter = " => ";
		while ((aLine = fullDBReader.readLine()) != null && i < 2 * chooseN) {
			if (fileLineIndex < indexes[i]) {
				fileLineIndex++;
				continue; // keep on reading
			} else if (fileLineIndex == indexes[i] && i < chooseN) {
				// index=> orignalIndex =>smiles
				String gString = aLine.split(spliter)[1];
				Graph g = MyFactory.getSmilesParser().parse(gString,
						MyFactory.getGraphFactory());
				edgeNum += g.getEdgeCount();
				nodeNum += g.getNodeCount();
				chosenDBWriter1.write(i + spliter + gString);
				chosenDBWriter1.newLine();
				i++;
				fileLineIndex++;
			} else if (fileLineIndex == indexes[i] && i >= chooseN) {
				// index=> orignalIndex =>smiles
				String gString = aLine.split(spliter)[1];
				Graph g = MyFactory.getSmilesParser().parse(gString,
						MyFactory.getGraphFactory());
				edgeNum += g.getEdgeCount();
				nodeNum += g.getNodeCount();
				chosenDBWriter2.write(i + spliter + gString);
				chosenDBWriter2.newLine();
				i++;
				fileLineIndex++;
			} else if (fileLineIndex > indexes[i])
				System.out
						.println("Exception: Processor: randomlyChooseDBGraph");
		}

		// Close out File
		try {
			chosenDBWriter1.close();
			chosenDBWriter2.close();
			fullDBReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();
		// Write the meta information of the new file
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				chosenDBFile + "_1" + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + i);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + (float) (edgeNum) / i
				+ ", Average NodeNum: " + (float) (nodeNum) / i);
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void merge(IGraphDatabase gDB1, IGraphDatabase gDB2,
			String newDBName) throws IOException {
		BufferedWriter newDBWriter = new BufferedWriter(new FileWriter(
				newDBName));
		String spliter = " => ";
		int gID = 0;
		for (int i = 0; i < gDB1.getTotalNum(); i++) {
			newDBWriter.write(gID + spliter + gDB1.findGraphString(i) + "\n");
			gID++;
		}
		for (int j = 0; j < gDB2.getTotalNum(); j++) {
			newDBWriter.write(gID + spliter + gDB2.findGraphString(j) + "\n");
			gID++;
		}
		newDBWriter.flush();
		newDBWriter.close();

		// Intrigue java garbage collector
		Runtime r = Runtime.getRuntime();
		r.gc();

		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(newDBName
				+ "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:"
				+ (gDB1.getTotalNum() + gDB2.getTotalNum()));
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + 0 + ", Average NodeNum: " + 0);
		// Close meta data file
		metaWriter.close();
	}

}
