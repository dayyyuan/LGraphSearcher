package edu.psu.chemxseer.structure.iterative.FeatureGenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import de.parmol.Settings;
import de.parmol.graph.ClassifiedGraph;
import de.parmol.graph.DefaultGraphClassifier;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.parsers.GraphParser;
import de.parmol.util.Debug;
import edu.psu.chemxseer.structure.iterative.CandidateInfo;
import edu.psu.chemxseer.structure.iterative.QueryInfo;
import edu.psu.chemxseer.structure.parmol.gSpanInduced.AbstractMiner;
import edu.psu.chemxseer.structure.parmol.gSpanInduced.DataBaseModified;
import edu.psu.chemxseer.structure.parmol.gSpanInduced.FragmentSet;
import edu.psu.chemxseer.structure.parmol.gSpanInduced.GraphSet;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * Search for the optimum features (starting from the rootGraph) with
 * optimumGain, If an branch has upperBound < optimumGain, then prune the
 * branch, stop searching.
 * 
 * Attention: this is the Query Grouping approache, in which only a partial set
 * of queries are used
 * 
 * @author dayuyuan
 * 
 */
public class GroupQueryGenerator extends AbstractMiner {
	private int maxMustNonSelectSize;
	private int optimumGain;
	private int[][] rootGraph;
	private FeatureFinder selector;
	private CandidateInfo finalCandidateInfo;
	// PrintStream debug;
	GraphFactory factory = GraphFactory.getFactory(GraphFactory.LIST_GRAPH
			| GraphFactory.UNDIRECTED_GRAPH);

	/**
	 * create a new Miner
	 * 
	 * @param settings
	 */
	public GroupQueryGenerator(Settings settings) {
		super(settings);
		this.m_frequentSubgraphs = new FragmentSet();
		GraphSet.length = settings.minimumClassFrequencies.length;
		empty = new float[settings.minimumClassFrequencies.length];
		Debug.out = System.out;
		Debug.dlevel = m_settings.debug;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.AbstractMiner#getGraphFactory(de.parmol.graph.GraphParser)
	 */
	@Override
	protected GraphFactory getGraphFactory(GraphParser parser) {
		int mask = parser.getDesiredGraphFactoryProperties()
				| GraphFactory.CLASSIFIED_GRAPH;
		if (m_settings.ringSizes[0] > 2)
			mask |= GraphFactory.RING_GRAPH;
		return GraphFactory.getFactory(mask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.AbstractMiner#startMining()
	 */
	@Override
	protected void startRealMining() {
		long start = System.currentTimeMillis();
		Debug.print(1, "renaming DataBase ... ");
		// ADDED By DAYU
		float[] dayuFrequency = new float[m_settings.minimumClassFrequencies.length];
		for (int i = 0; i < dayuFrequency.length; i++) {
			// System.out.println(m_settings.minimumClassFrequencies[i]);
			dayuFrequency[i] = (float) 1.0;
		}
		DataBaseModified gs = new DataBaseModified(m_graphs,
				m_settings.minimumClassFrequencies, m_frequentSubgraphs,
				factory);
		// DataBase gs = new DataBase(m_graphs,
		// m_settings.minimumClassFrequencies, m_frequentSubgraphs, factory);
		Debug.println(1, "done (" + (System.currentTimeMillis() - start)
				+ " ms)");

		Debug.println(1, "minSupport: " + m_settings.minimumClassFrequencies[0]);
		Debug.println(1, "graphs    : " + m_graphs.size());
		graphSet_Projection(gs);
	}

	/**
	 * searches Subgraphs for each frequent edge in the DataBase
	 * 
	 * @param gs
	 */
	private void graphSet_Projection(DataBaseModified gs) {
		GraphSet allGraphs = new GraphSet();
		for (Iterator it = gs.frequentEdges(); it.hasNext();) {
			edu.psu.chemxseer.structure.parmol.gSpanInduced.GSpanEdge edge = (edu.psu.chemxseer.structure.parmol.gSpanInduced.GSpanEdge) it
					.next();
			GraphSet temp = gs.getContainingGraphs(edge);
			allGraphs.addAll(temp);
		}
		DFSCode2 code = new DFSCode2(this.rootGraph, gs, allGraphs); // create
																		// DFSCode
																		// for
																		// the
		// current edge
		long time = System.currentTimeMillis();
		// Debug.println(2,"");
		float[] freq = new float[1];
		subgraph_Mining(code, freq); // recursive search

		// eit.remove(); // shrink database
		Debug.println(1, "\tdone (" + (System.currentTimeMillis() - time)
				+ " ms)");
		if (gs.size() < m_settings.minimumClassFrequencies[0] && gs.size() != 0) { // not
																					// needed
			Debug.println("remaining Graphs: " + gs.size());
			Debug.println("May not happen!!!");
			return;
		}
		Debug.println(2, "remaining Graphs: " + gs.size());
	}

	private static float[] empty;

	private float[] getMax(float[] a, float[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] > b[i])
				return a;
			if (b[i] > a[i])
				return b;
		}
		return a;
	}

	private boolean unequal(float[] a, float[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return true;
		}
		return false;
	}

	/**
	 * recursive search for frequent Subgraphs
	 * 
	 * @param code
	 *            the DFSCode with is found and checked for children
	 * @return the highest occurring frequency, of this branch
	 */
	private void subgraph_Mining(DFSCode2 code, float[] freq) {
		if (!code.isMin()) {
			Debug.println(2, code.toString(m_settings.serializer) + " not min");
			m_settings.stats.duplicateFragments++;
			for (int i = 0; i < empty.length; i++)
				freq[i] = empty[i];
			return;
		}
		float[] max = empty;

		float[] my = code.getFrequencies();
		Debug.println(2,
				"  found graph " + code.toString(m_settings.serializer));
		Iterator it = code.childIterator(m_settings.findTreesOnly,
				m_settings.findPathsOnly);
		for (; it.hasNext();) {
			DFSCode2 next = (DFSCode2) it.next();
			// This is edited by Dayu, affect efficiency
			if (next.isFrequent(m_settings.minimumClassFrequencies)) {
				if (next.toFragment().getFragment().getEdgeCount() <= this.maxMustNonSelectSize) {
					Graph codeG = (Graph) next.getSubgraph().clone();
					CandidateInfo cInfo = selector.makeCandidateInfo(codeG,
							next.toFragment().getSupportedGraphs());
					// Use fake upper bound for filtering
					if (cInfo != null
							&& cInfo.getUpperBound() < this.optimumGain)
						continue; // stop searching
					if (cInfo != null && cInfo.getGain() > this.optimumGain) {
						optimumGain = cInfo.getGain();
						finalCandidateInfo = cInfo;
					}
					// else: keep on searching
					float[] a = new float[freq.length];
					subgraph_Mining(next, a);
					max = getMax(max, a);
				} else
					continue; // early pruning
			}
		}
		for (int i = 0; i < my.length; i++)
			freq[i] = my[i];

	}

	/**
	 * The main program, for starting a mine-proces
	 * 
	 * @param args
	 *            parameters parsable by de.parmol.Settings
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static CandidateInfo genCandidate(double minFreq,
			int maxNonSelectDepth, int[][] rootGraph, FeatureFinder selector,
			List<QueryInfo> queries, int optimumGain)
			throws FileNotFoundException, IOException, ParseException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		String gDBFileName = null;
		String[] args = {
				"-minimumFrequencies=" + (-minFreq),
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-graphFile=" + gDBFileName,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=de.parmol.parsers.SmilesParser",
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=-1" };
		if ((args.length == 0) || args[0].equals("--help")) {
			System.out.println("Usage: " + AbstractMiner.class.getName()
					+ " options, where options are:\n");
			Settings.printUsage();
			System.exit(1);
		}
		Settings s = new Settings(args);
		if (s.directedSearch) {
			System.out.println(GroupQueryGenerator.class.getName()
					+ " does not implement the search for directed graphs");
			System.exit(1);
		}
		GroupQueryGenerator m = new GroupQueryGenerator(s);
		m.rootGraph = rootGraph;
		m.selector = selector;
		m.maxMustNonSelectSize = maxNonSelectDepth;
		m.optimumGain = optimumGain;

		m.readGraphs(queries);
		m.startMining();
		return m.finalCandidateInfo;
	}

	/**
	 * Read the graphs from the "query" logs
	 * 
	 * @param queries
	 * @throws IOException
	 * @throws ParseException
	 */
	protected void readGraphs(List<QueryInfo> queries) throws IOException,
			ParseException {
		long t = 0;
		if (m_settings.debug > 0) {
			System.out.print("Parsing graphs...");
			t = System.currentTimeMillis();
		}

		m_graphs = new ArrayList<Graph>(queries.size());
		for (int i = 0; i < queries.size(); i++) {
			QueryInfo q = queries.get(i);
			Graph g = q.getQueryGraph();
			for (int j = 0; j < q.getFrequency(); j++)
				m_graphs.add(g);
		}
		m_settings.directedSearch = MyFactory.getSmilesParser().directed();
		int classCount = 1;
		if (m_settings.classFrequencyFile != null) {
			DefaultGraphClassifier classifier = new DefaultGraphClassifier(
					m_settings.classFrequencyFile);

			for (Iterator it = m_graphs.iterator(); it.hasNext();) {
				Graph g = (Graph) it.next();

				if (g instanceof ClassifiedGraph) {
					float[] cf = classifier.getClassFrequencies(g.getName());
					if (cf == null) {
						System.err.println("No class frequencies found for "
								+ g.getName());
						((ClassifiedGraph) g)
								.setClassFrequencies(new float[classCount]);
					} else {
						((ClassifiedGraph) g).setClassFrequencies(cf);
						classCount = cf.length;
					}
				}
			}
		}

		float[] frequencySum = new float[classCount];

		for (Iterator it = m_graphs.iterator(); it.hasNext();) {
			final Graph g = (Graph) it.next();

			if (g instanceof ClassifiedGraph) {
				float[] cf = ((ClassifiedGraph) g).getClassFrequencies();

				for (int k = 0; k < frequencySum.length; k++) {
					frequencySum[k] += cf[k];
				}
			} else {
				for (int k = 0; k < frequencySum.length; k++) {
					frequencySum[k] += 1;
				}
			}
		}

		for (int i = 0; i < m_settings.minimumClassFrequencies.length; i++) {
			if (m_settings.minimumClassFrequencies[i] < 0) {
				m_settings.minimumClassFrequencies[i] *= -frequencySum[i];
			}
		}

		for (int i = 0; i < m_settings.maximumClassFrequencies.length; i++) {
			if (m_settings.maximumClassFrequencies[i] < 0) {
				m_settings.maximumClassFrequencies[i] *= -frequencySum[i];
			}
		}

		if (m_settings.debug > 0) {
			System.out.println("done (" + (System.currentTimeMillis() - t)
					+ "ms)");

			if (m_settings.debug > 3) {
				int maxEdgeCount = 0, edgeSum = 0;
				BitSet nodeLabelsUsed = new BitSet(), edgeLabelsUsed = new BitSet();
				for (java.util.Iterator it = m_graphs.iterator(); it.hasNext();) {
					de.parmol.graph.Graph g = (de.parmol.graph.Graph) it.next();

					edgeSum += g.getEdgeCount();
					// maxEdgeCount = Math.max(maxEdgeCount, g.getEdgeCount());
					if (g.getEdgeCount() > maxEdgeCount)
						maxEdgeCount = g.getEdgeCount();
					for (int k = 0; k < g.getNodeCount(); k++) {
						nodeLabelsUsed.set(g.getNodeLabel(g.getNode(k)));
					}
					for (int k = 0; k < g.getEdgeCount(); k++) {
						edgeLabelsUsed.set(g.getEdgeLabel(g.getEdge(k)));
					}

				}

				int nlsum = 0;
				for (int i = 0; i < nodeLabelsUsed.size(); i++) {
					if (nodeLabelsUsed.get(i))
						nlsum++;
				}

				int elsum = 0;
				for (int i = 0; i < edgeLabelsUsed.size(); i++) {
					if (edgeLabelsUsed.get(i))
						elsum++;
				}

				System.out.println("Maximal edge count: " + maxEdgeCount);
				System.out.println("Average edge count: "
						+ (edgeSum / m_graphs.size()));
				System.out.println("Node label count: " + nlsum);
				System.out.println("Edge label count: " + elsum);
			}
			System.out
					.println("================================================================================");
		}
	}
}
