package edu.psu.chemxseer.structure.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_Frequent;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.GraphsPlain;
import edu.psu.chemxseer.structure.postings.Interface.IGraphs;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * In order to conquer the difficulty of the pattern enumeration. Here I propose
 * to use gSpan to mine the database graph directly (1) Problem, not each
 * embedding is enumerated, eg. pattern p may have several embedding on graph g,
 * but p is counted for only once.
 * 
 * @author dayuyuan
 * 
 */
public class InFrequentQueryGenerater2 {

	/**
	 * 1. Then enumerate all subgraphs contained in those sampled graphs 2.
	 * Sample subgraphs of each edge count, according to uniform distribution or
	 * normal distribution based on the type The output file is organized such
	 * that: A: Each edge count corresponds to a set of queries B: Those sets
	 * have same number of queries.
	 * 
	 * @param minSize
	 * @param maxSubSize
	 * @param num
	 * @param gDB
	 * @param type
	 *            : type 0: uniform sampling, type 1: normal sampling
	 * @param output
	 *            : the output file
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */
	public void generateInFrequentQueries(int minSize, int maxSubSize, int num,
			double minSuport, GraphDatabase_OnDisk sampledGraphs, int type,
			String output) throws IOException, ParseException, MathException {
		// Then mine all the subgraphs of selectedGraphs
		NoPostingFeatures<IOneFeature> features = generateAllSubgraphs(
				sampledGraphs.getDBFileName(), sampledGraphs.getParser(),
				output + "_raw", maxSubSize, minSuport).getFeatures();
		@SuppressWarnings("unchecked")
		List<IOneFeature>[] subgraphs = new ArrayList[maxSubSize - minSize];
		for (int i = 0; i < maxSubSize - minSize; i++)
			subgraphs[i] = new ArrayList<IOneFeature>();
		for (int i = 0; i < features.getfeatureNum(); i++) {
			IOneFeature feature = features.getFeature(i);
			int edgeCount = feature.getFeatureGraph().getEdgeCount();
			if (edgeCount < minSize && edgeCount >= maxSubSize)
				continue;
			else
				subgraphs[edgeCount - minSize].add(feature);
		}
		if (type == 0)
			this.generateUniformCore(subgraphs, num);
		else if (type == 1)
			this.generateNormalCore(subgraphs, num);
		this.writeQueries(subgraphs, output);
	}

	/**
	 * 1 First enumerate all subgraphs contained in those sampled graphs 2
	 * Sample subgraphs according to uniform distribution or normal distribution
	 * based on the type the output is organized such that: One set of queries
	 * of varieties of size are returned.
	 * 
	 * @param minSize
	 * @param maxSubSize
	 * @param num
	 * @param gDB
	 * @param type
	 *            : type 0, uniform, type 1, normal distribution
	 * @param output
	 *            : output file
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */
	public void generateInFrequentQueries2(int minSize, int maxSubSize,
			int num, double minSupport, GraphDatabase_OnDisk sampleGraphs,
			int type, String output) throws IOException, ParseException,
			MathException {

		// Then mine all the subgraphs of selectedGraphs
		NoPostingFeatures<IOneFeature> features = generateAllSubgraphs(
				sampleGraphs.getDBFileName(), sampleGraphs.getParser(),
				output + "_raw", maxSubSize, minSupport).getFeatures();
		@SuppressWarnings("unchecked")
		List<IOneFeature>[] subgraphs = new ArrayList[1];
		subgraphs[0] = new ArrayList<IOneFeature>();

		for (int i = 0; i < features.getfeatureNum(); i++) {
			IOneFeature feature = features.getFeature(i);
			int edgeCount = feature.getFeatureGraph().getEdgeCount();
			if (edgeCount < minSize && edgeCount >= maxSubSize)
				continue;
			else
				subgraphs[0].add(feature);
		}

		if (type == 0)
			this.generateUniformCore(subgraphs, num);
		else if (type == 1)
			this.generateNormalCore(subgraphs, num);
		this.writeQueries(subgraphs, output);
	}

	/**
	 * Given all the rawQueries, run a sampling algorithm on those raw queries:
	 * 
	 * @param minSize
	 * @param maxSubSize
	 * @param num
	 * @param rawQueries
	 * @param type
	 *            : type 0, uniform, type 1, normal distribution
	 * @param output
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */
	public void generateInFrequentQueries2(int minSize, int maxSubSize,
			int num, NoPostingFeatures<IOneFeature> rawQueries, int type,
			String output) throws IOException, ParseException, MathException {

		// Then mine all the subgraphs of selectedGraphs
		@SuppressWarnings("unchecked")
		List<IOneFeature>[] subgraphs = new ArrayList[1];
		subgraphs[0] = new ArrayList<IOneFeature>();

		for (int i = 0; i < rawQueries.getfeatureNum(); i++) {
			IOneFeature feature = rawQueries.getFeature(i);
			int edgeCount = feature.getFeatureGraph().getEdgeCount();
			if (edgeCount < minSize && edgeCount >= maxSubSize)
				continue;
			else
				subgraphs[0].add(feature);
		}

		if (type == 0)
			this.generateUniformCore(subgraphs, num);
		else if (type == 1)
			this.generateNormalCore(subgraphs, num);
		this.writeQueries(subgraphs, output);
	}

	private static PostingFeatures generateAllSubgraphs(String dbFileName,
			GraphParser gParser, String featureFileName, int maxSize,
			double minFrequency) {
		String[] args = {
				"-minimumFrequencies=" + (-minFrequency),
				"-maximumFragmentSize=" + maxSize,
				"-graphFile=" + dbFileName,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=1" };
		return GSpanMiner_Frequent.gSpanMining(args, featureFileName, null);
	}

	private void generateUniformCore(List<IOneFeature> allSubs[], int num) {
		for (int i = 0; i < allSubs.length; i++) {
			List<IOneFeature> subs = allSubs[i];
			List<IOneFeature> selectedSubs = new ArrayList<IOneFeature>();
			int totalCount = 0;
			for (IOneFeature oneSub : subs)
				totalCount += oneSub.getFrequency();
			int[] indexes = randomSelectK(totalCount, num);
			int indexCount = 0;
			int featureCount = 0;
			for (IOneFeature oneSub : subs) {
				for (int w = 0; w < oneSub.getFrequency() && indexCount < num; w++) {
					if (indexes[indexCount] == featureCount) {
						selectedSubs.add(oneSub);
						indexCount++;
					} else if (indexes[indexCount] < featureCount)
						System.out.println("Something is wrong");
					featureCount++;
				}
				if (indexCount >= num)
					break;
			}
			allSubs[i].clear();
			allSubs[i].addAll(selectedSubs);
		}
	}

	private void generateNormalCore(List<IOneFeature> allSubs[], int num)
			throws MathException, IOException, ParseException {
		Random rd = new Random();
		for (int i = 0; i < allSubs.length; i++) {
			DescriptiveStatistics frequencies = new DescriptiveStatistics();
			for (int j = 0; j < allSubs[i].size(); j++) {
				frequencies.addValue(allSubs[i].get(j).getFrequency());
			}
			NormalDistribution nDist = new NormalDistributionImpl(
					frequencies.getMean(),
					frequencies.getStandardDeviation() / 2);
			List<IOneFeature> selectedSub = new ArrayList<IOneFeature>();
			for (int j = 0; j < allSubs[i].size(); j++) {
				double prob = nDist.density(frequencies.getElement(j));
				if (rd.nextDouble() < prob) {
					selectedSub.add(allSubs[i].get(j));
				}
			}
			allSubs[i].clear();
			allSubs[i].addAll(selectedSub);
		}
		// Make sure only "num" features are selected
		for (int i = 0; i < allSubs.length; i++) {
			if (allSubs[i].size() < num)
				allSubs[i].addAll(allSubs[i]);
			int[] indexes = randomSelectK(allSubs[i].size(), num);
			List<IOneFeature> selectedFeatures = new ArrayList<IOneFeature>();
			for (int w = 0; w < num; w++)
				selectedFeatures.add(allSubs[i].get(indexes[w]));
			allSubs[i].clear();
			allSubs[i].addAll(selectedFeatures);
		}

	}

	public static int[] randomSelectK(int totalCount, int num) {
		int[] indexes = new int[totalCount];
		for (int w = 0; w < indexes.length; w++)
			indexes[w] = w;
		Random rd5 = new Random();
		int j = 0;
		int swapTemp = 0;
		;
		for (int w = 0; w < num; w++) {
			j = (int) (rd5.nextFloat() * (totalCount - w)) + w;
			swapTemp = indexes[w];
			indexes[w] = indexes[j];
			indexes[j] = swapTemp;
		}
		Arrays.sort(indexes, 0, num);
		return indexes;
	}

	private void writeQueries(List<IOneFeature>[] subgraphs, String output)
			throws IOException {
		BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(
				output));
		StringBuffer conclusionBuf = new StringBuffer();
		// Write statistical information
		for (int i = 0; i < subgraphs.length; i++) {
			int size = subgraphs[i].size();
			conclusionBuf.append(size);
			conclusionBuf.append(',');
		}
		conclusionBuf.deleteCharAt(conclusionBuf.length() - 1);
		conclusionBuf.append('\n');
		outputFileWriter.write(conclusionBuf.toString());
		// Write the queries:
		for (int i = 0; i < subgraphs.length; i++) {
			for (IOneFeature oneFeature : subgraphs[i])
				outputFileWriter.write(oneFeature.toFeatureString() + "\n");
		}
		outputFileWriter.close();
	}

	public IGraphs[] loadInfrequentQueries(String queryFile) throws IOException {
		BufferedReader bin = new BufferedReader(new FileReader(queryFile));
		String firstLine = bin.readLine();
		if (firstLine == null) {
			bin.close();
			return null;
		}
		String[] conclusionInfo = firstLine.split(",");
		IGraphs[] queries = new IGraphs[conclusionInfo.length];
		int groupCount = Integer.parseInt(conclusionInfo[0]);

		for (int i = 0; i < queries.length; i++) {
			String[] queryStrings = new String[groupCount];
			int j = 0;
			while (j < queryStrings.length
					&& (firstLine = bin.readLine()) != null) {
				String tokens[] = firstLine.split(",");
				queryStrings[j++] = tokens[1];
			}
			queries[i] = new GraphsPlain(queryStrings);
		}
		bin.close();
		return queries;
	}
}
