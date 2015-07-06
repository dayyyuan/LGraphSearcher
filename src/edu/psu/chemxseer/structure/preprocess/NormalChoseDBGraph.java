package edu.psu.chemxseer.structure.preprocess;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.query.InFrequentQueryGenerater2;

/**
 * Chose the Database Graph with Normal Distribution
 * 
 * @author dayuyuan
 * 
 */
public class NormalChoseDBGraph {

	/**
	 * Sampling Graphs with normal distribution w.r.t edge count
	 * 
	 * @param mean
	 * @param variation
	 * @param gDB
	 * @param newDBName
	 * @param chooseN
	 * @throws MathException
	 * @throws IOException
	 */
	public static void sampleGraph(double mean, double variation,
			IGraphDatabase gDB, String newDBName, int chooseN)
			throws MathException, IOException {
		NormalDistribution nDist = new NormalDistributionImpl(mean, variation);
		List<Graph> selectedGraphs = new ArrayList<Graph>();
		Random rm = new Random();
		while (selectedGraphs.size() < chooseN) {
			// sample the graphs
			for (int i = 0; i < gDB.getTotalNum(); i++) {
				Graph g = gDB.findGraph(i);
				int edgeCount = g.getEdgeCount();
				double prob = nDist.cumulativeProbability(edgeCount);
				if (prob > 0.5)
					prob = 1 - prob;
				// decide whether to select or not
				if (prob > rm.nextDouble())
					selectedGraphs.add(g);
				else
					continue;
			}
		}
		// select precisely chooseN number of graphs
		int[] indexes = InFrequentQueryGenerater2.randomSelectK(
				selectedGraphs.size(), chooseN);
		// Write the selected graphs
		BufferedWriter chosenDBWriter = new BufferedWriter(new FileWriter(
				newDBName));
		String spliter = " => ";
		// Read those db graphs and save them into the new file
		float edgeCount = 0;
		float nodeCount = 0;
		for (int i = 0; i < chooseN; i++) {
			int gID = indexes[i];
			Graph g = selectedGraphs.get(gID);
			edgeCount += g.getEdgeCount();
			nodeCount += g.getNodeCount();
			chosenDBWriter.write(i + spliter
					+ MyFactory.getSmilesParser().serialize(g));
			chosenDBWriter.newLine();
		}
		chosenDBWriter.close();
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
		metaWriter.write("Number of Graphs:" + chooseN);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + (edgeCount / chooseN)
				+ ", Average NodeNum: " + (nodeCount / chooseN));
		// Close meta data file
		try {
			metaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
