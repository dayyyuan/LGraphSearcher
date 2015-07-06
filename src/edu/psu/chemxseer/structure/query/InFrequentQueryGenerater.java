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

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.parmolExtension.DFSCodeGenerator;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.GraphsPlain;
import edu.psu.chemxseer.structure.postings.Interface.IGraphs;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.subsearch.FGindex.EdgeIndex;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

/**
 * InFrequentQueryGenerator: (1) Sample Database Graph First (2) Enumerate
 * Subgraphs of the Sampled Graphs: Each embeeding will be enumerated (3)
 * Uniform Sampling is used during pattern enumeration (4) While Normal Sampling
 * is used, the selected features are further samples with normal distribution
 * (5) Two output format is available A: For each edge, a set of queries are
 * returned. each edge size will have equal amount of queries B: The file is
 * returned as a whole set of queries.
 * 
 * Two problems: (1) The pattern enumeration is not complete (2) The normal
 * sampling is not well defined. since it is based on all appearance of patterns
 * eg. pattern p have frequency 10, then there will be 10 appearance feeded to
 * the normal sampler, not only one
 * 
 * @author dayuyuan
 * 
 */
public class InFrequentQueryGenerater {

	/**
	 * Generate sets of queries sampled uniformly from all possible subgraphs
	 * enumerations Queries in each set contain same number of edges Each set
	 * contains equal number of queries
	 * 
	 * @param minSize
	 * @param maxSubSize
	 * @param num
	 * @param candidateGraphFile
	 * @param edgeIndex
	 *            : to prune graphs containing infrequent edges
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */
	public static void generateInFrequentQueriesUniform(int minSize,
			int maxSubSize, int num, GraphDatabase_OnDisk gDB,
			EdgeIndex edgeIndex, String outputFile) throws IOException,
			ParseException, MathException {
		// First randomly selected #2num of graphs: those graphs can not has
		// distinct edges
		Graph[] selectedGraphs = RandomChoseDBGraph.randomlyChooseDBGraph(gDB,
				20 * num, false, edgeIndex);
		// Then generate subgraphs of each selected graph
		// randomly choose those subgraphs and add then into the query file
		@SuppressWarnings("unchecked")
		ArrayList<String> subgraphs[] = new ArrayList[maxSubSize - minSize];
		for (int i = 0; i < subgraphs.length; i++)
			subgraphs[i] = new ArrayList<String>();

		generateUniformCore(num, selectedGraphs, maxSubSize, minSize, subgraphs);
		WriteSeperately(num, maxSubSize - minSize, minSize, subgraphs,
				outputFile);
	}

	// Uniform Sampling
	private static void generateUniformCore(int num, Graph[] selectedGraphs,
			int maxSubSize, int minSize, ArrayList<String> subgraphs[]) {
		Random rd = new Random();
		Random rd3 = new Random();
		int limit = 10000;

		for (int i = 0; i < selectedGraphs.length; i++) {
			Graph g = selectedGraphs[i];
			float edgeNum = g.getEdgeCount();
			DFSCodeGenerator subgraphGenerator = new DFSCodeGenerator(g,
					maxSubSize);
			Graph subg = null;

			int count = 0;
			while ((subg = subgraphGenerator.nextGraph()) != null) {
				count++;
				if (count > limit)
					break;
				float subEdgeNum = subg.getEdgeCount();
				if (Math.random() > (subEdgeNum / edgeNum))
					continue;
				if (Math.random() < (subEdgeNum / edgeNum))
					continue;
				if (subEdgeNum >= minSize && subEdgeNum < maxSubSize) {
					double randomv = rd.nextDouble();

					if (randomv > (float) 1 / (float) 10) {
						int index = (int) (subEdgeNum - minSize);
						if (rd3.nextFloat() > (float) 1 / (float) 10)
							subgraphs[index].add(MyFactory.getDFSCoder()
									.serialize(subg));
					} else
						continue;
				}
			}
		}
	}

	/**
	 * Generate a set of queries of various size: sampled uniformly from all
	 * possible subgraphs enumerations. Different size set may contain different
	 * number of queries Queries are written together
	 * 
	 * @param minSize
	 * @param maxSubSize
	 * @param num
	 * @param candidateGraphFile
	 * @param edgeIndex
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */
	public static void generateInFrequentQueriesUniform2(int minSize,
			int maxSubSize, int num, GraphDatabase_OnDisk gDB,
			EdgeIndex edgeIndex, String outputFile) throws IOException,
			ParseException, MathException {
		int changedNum = num / (maxSubSize - minSize);
		// First randomly selected #2num of graphs: those graphs can not has
		// distinct edges
		Graph[] selectedGraphs = RandomChoseDBGraph.randomlyChooseDBGraph(gDB,
				5 * changedNum, true, edgeIndex);
		// Then generate subgraphs of each selected graph
		// randomly choose those subgraphs and add then into the query file
		@SuppressWarnings("unchecked")
		ArrayList<String> subgraphs[] = new ArrayList[maxSubSize - minSize];
		for (int i = 0; i < subgraphs.length; i++)
			subgraphs[i] = new ArrayList<String>();

		generateUniformCore(changedNum, selectedGraphs, maxSubSize, minSize,
				subgraphs);
		ArrayList<String> allSubs = new ArrayList<String>();
		for (int i = 0; i < subgraphs.length; i++)
			allSubs.addAll(subgraphs[i]);
		WriteTogether(num, allSubs, outputFile);
	}

	/**
	 * Generate queries of various set: In each size set, the queries are
	 * sampled with a normal distribution: With mean = mean, Variation = 1/2
	 * variation
	 * 
	 * @param minSize
	 * @param maxSubSize
	 * @param num
	 * @param candidateGraphFile
	 * @param edgeIndex
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */
	public static void generateInFrequentQueriesNormal(int minSize,
			int maxSubSize, int num, GraphDatabase_OnDisk gDB,
			EdgeIndex edgeIndex, ISearcher indexSearcher, String outputFile)
			throws IOException, ParseException, MathException {

		Graph[] selectedGraphs = RandomChoseDBGraph.randomlyChooseDBGraph(gDB,
				5 * num, true, edgeIndex);
		// Then generate subgraphs of each selected graph
		// randomly choose those subgraphs and add then into the query file
		@SuppressWarnings("unchecked")
		ArrayList<String> subgraphs[] = new ArrayList[maxSubSize - minSize + 1];
		for (int i = 0; i < subgraphs.length; i++)
			subgraphs[i] = new ArrayList<String>();

		generateUniformCore(num, selectedGraphs, maxSubSize, minSize, subgraphs);
		generateNormalCore(subgraphs, indexSearcher);

		WriteSeperately(num, maxSubSize - minSize, minSize, subgraphs,
				outputFile);
	}

	public static void generateInFrequentQueriesNormal2(int minSize,
			int maxSubSize, int num, GraphDatabase_OnDisk gDB,
			EdgeIndex edgeIndex, ISearcher indexSearcher, String outputFile)
			throws IOException, ParseException, MathException {

		int changedNum = num / (maxSubSize - minSize);
		Graph[] selectedGraphs = RandomChoseDBGraph.randomlyChooseDBGraph(gDB,
				5 * changedNum, true, edgeIndex);
		// Then generate subgraphs of each selected graph
		// randomly choose those subgraphs and add then into the query file
		@SuppressWarnings("unchecked")
		ArrayList<String> subgraphs[] = new ArrayList[maxSubSize - minSize + 1];
		for (int i = 0; i < subgraphs.length; i++)
			subgraphs[i] = new ArrayList<String>();
		generateUniformCore(changedNum, selectedGraphs, maxSubSize, minSize,
				subgraphs);
		ArrayList<String> allSubs = new ArrayList<String>();
		for (int i = 0; i < subgraphs.length; i++)
			allSubs.addAll(subgraphs[i]);

		@SuppressWarnings("unchecked")
		ArrayList<String>[] temp = new ArrayList[1];
		temp[0] = allSubs;
		generateNormalCore(temp, indexSearcher);

		WriteTogether(num, allSubs, outputFile);

	}

	private static void generateNormalCore(ArrayList<String> allSubs[],
			ISearcher indexSearcher) throws MathException, IOException,
			ParseException {
		long[] TimeComponent = new long[4];
		int[] number = new int[2];
		Random rd = new Random();
		for (int i = 0; i < allSubs.length; i++) {
			DescriptiveStatistics frequencies = new DescriptiveStatistics();
			for (int j = 0; j < allSubs[i].size(); j++) {
				Graph g = MyFactory.getDFSCoder().parse(allSubs[i].get(j),
						MyFactory.getGraphFactory());
				int frequency = indexSearcher.getAnswer(g, TimeComponent,
						number).size();
				frequencies.addValue(frequency);
			}
			NormalDistribution nDist = new NormalDistributionImpl(
					frequencies.getMean(),
					frequencies.getStandardDeviation() / 2);
			List<String> selectedSub = new ArrayList<String>();
			for (int j = 0; j < allSubs[i].size(); j++) {
				double prob = nDist.density(frequencies.getElement(j));
				if (rd.nextDouble() < prob) {
					selectedSub.add(allSubs[i].get(j));
				}
			}
			allSubs[i].clear();
			allSubs[i].addAll(selectedSub);
		}

	}

	private static int[] randomSelectK(int totalCount, int num) {
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

	private static void WriteTogether(int num, ArrayList<String> subgraphs,
			String infrequentQuery) throws IOException {
		BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(
				infrequentQuery));

		while (subgraphs.size() <= num) {
			subgraphs.addAll(subgraphs);
		}
		int[] indexes = randomSelectK(subgraphs.size(), num);
		outputFileWriter.write(num + "\n");
		for (int w = 0; w < num; w++) {
			StringBuffer buf = new StringBuffer();
			buf.append(w);
			buf.append(',');
			buf.append(subgraphs.get(indexes[w]));
			buf.append(',');
			buf.append(-1);// edgeNumber
			buf.append('\n');
			outputFileWriter.write(buf.toString());
		}
		outputFileWriter.close();
	}

	private static void WriteSeperately(int num, int difference, int minSize,
			ArrayList<String> subgraphs[], String infrequentQuery)
			throws IOException {
		BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(
				infrequentQuery));
		StringBuffer conclusionBuf = new StringBuffer();
		conclusionBuf.append(difference);
		// Write statistical information
		for (int i = 0; i < difference; i++) {
			int size = num;
			conclusionBuf.append(',');
			conclusionBuf.append(size);
		}
		conclusionBuf.append('\n');
		// System.out.println(conclusionBuf.toString());
		outputFileWriter.write(conclusionBuf.toString());
		// Write these graphs
		for (int i = 0; i < difference; i++) {
			String[] results = new String[num];
			while (subgraphs[i].size() <= num) {
				subgraphs[i].addAll(subgraphs[i]);
			}
			// Then randomly select num from size: the first num indexes are
			// sorted as stored in indexes
			int[] indexes = randomSelectK(subgraphs[i].size(), num);
			for (int w = 0; w < num; w++) {
				results[w] = subgraphs[i].get(indexes[w]);
				StringBuffer buf = new StringBuffer();
				buf.append(results[w]);
				buf.append(',');
				buf.append(i + minSize);// edgeNumber
				buf.append('\n');
				outputFileWriter.write(buf.toString());
			}
		}
		outputFileWriter.close();
	}

	/**
	 * Load query from query file
	 * 
	 * @return
	 * @throws IOException
	 */
	public IGraphs[] loadInfrequentQueries(String outputFile)
			throws IOException {
		BufferedReader bin = new BufferedReader((new FileReader(outputFile)));
		String firstLine = bin.readLine();
		if (firstLine == null) {
			bin.close();
			return null;
		}
		String[] conclusionInfo = firstLine.split(",");
		IGraphs[] queries = new IGraphs[Integer.parseInt(conclusionInfo[0])];
		int groupCount = Integer.parseInt(conclusionInfo[1]);

		for (int i = 0; i < queries.length; i++) {
			String[] queryStrings = new String[groupCount];
			int j = 0;
			while (j < queryStrings.length
					&& (firstLine = bin.readLine()) != null) {
				String tokens[] = firstLine.split(",");
				queryStrings[j++] = tokens[0];
			}
			queries[i] = new GraphsPlain(queryStrings);
		}
		bin.close();
		return queries;
	}

}
