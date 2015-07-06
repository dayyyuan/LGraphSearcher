package edu.psu.chemxseer.structure.iterative.FeatureGenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import de.parmol.AbstractMiner;
import de.parmol.Settings;
import de.parmol.GSpan.DFSCode;
import de.parmol.GSpan.DataBase;
import de.parmol.GSpan.GSpanEdge;
import de.parmol.GSpan.GraphSet;
import de.parmol.graph.ClassifiedGraph;
import de.parmol.graph.DefaultGraphClassifier;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.parsers.GraphParser;
import de.parmol.util.Debug;
import de.parmol.util.FragmentSet;
import edu.psu.chemxseer.structure.iterative.CandidateInfo;
import edu.psu.chemxseer.structure.iterative.QueryInfo;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * The branch and bound algorithm: exhaustive BB No Query Grouping at all
 * 
 * @author duy113
 * 
 */
public class ExhaustiveGenerator extends AbstractMiner {

	private int maxMustNonSelectSize;
	private FeatureFinder selector;

	private int optimumGain;
	private CandidateInfo finalCandidateInfo;

	GraphFactory factory = GraphFactory.getFactory(GraphFactory.LIST_GRAPH
			| GraphFactory.UNDIRECTED_GRAPH);

	/**
	 * create a new Miner
	 * 
	 * @param settings
	 */
	public ExhaustiveGenerator(Settings settings) {
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
			System.out.println(m_settings.minimumClassFrequencies[i]);
			dayuFrequency[i] = (float) 1.0;
		}
		DataBase gs = new DataBase(m_graphs, dayuFrequency,
				m_frequentSubgraphs, factory);
		// DataBase gs = new DataBase(m_graphs,
		// m_settings.minimumClassFrequencies, m_frequentSubgraphs, factory);
		Debug.println(1, "done (" + (System.currentTimeMillis() - start)
				+ " ms)");

		Debug.println(1, "minSupport: " + m_settings.minimumClassFrequencies[0]);
		Debug.println(1, "graphs    : " + m_graphs.size());
		graphSet_Projection(gs);
	}

	/**
	 * searches Subgraphs for each freqent edge in the DataBase
	 * 
	 * @param gs
	 */
	private void graphSet_Projection(DataBase gs) {
		for (Iterator eit = gs.frequentEdges(); eit.hasNext();) {
			GSpanEdge edge = (GSpanEdge) eit.next();
			DFSCode code = new DFSCode(edge, gs); // create DFSCode for the
			// current edge
			long time = System.currentTimeMillis();
			// Debug.print(1, "doing seed " +
			// m_settings.serializer.serialize(code.toFragment().getFragment())
			// + " ...");
			// Debug.println(2,"");
			float[] freq = new float[1];
			subgraph_Mining(code, freq); // recursive search
			eit.remove(); // shrink database
			Debug.println(1, "\tdone (" + (System.currentTimeMillis() - time)
					+ " ms)");
			if (gs.size() < m_settings.minimumClassFrequencies[0]
					&& gs.size() != 0) { // not needed
				Debug.println("remaining Graphs: " + gs.size());
				Debug.println("May not happen!!!");
				return;
			}
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
	private void subgraph_Mining(DFSCode code, float[] freq) {
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
		// FastSU fastSu = new FastSU();
		for (; it.hasNext();) {
			DFSCode next = (DFSCode) it.next();
			// This is edited by Dayu, affect efficiency
			if (next.isFrequent(m_settings.minimumClassFrequencies)
					&& next.toFragment().getFragment().getEdgeCount() <= this.maxMustNonSelectSize) {
				Graph codeG = (Graph) next.getSubgraph().clone();
				CandidateInfo cInfo = selector.makeCandidateInfo(codeG, next
						.toFragment().getSupportedGraphs());
				if (cInfo != null
						&& cInfo.getBranchUpperBound() < this.optimumGain)
					continue; // stop searching, skip the branch
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
		for (int i = 0; i < my.length; i++)
			freq[i] = my[i];
	}

	/**
	 * Exhaustive Search (Branch & Bound) with minimum support Finding a
	 * CandidateFeature
	 * 
	 * @param minFreq
	 * @param maxNonSelectDepth
	 * @param selector
	 * @param queries
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static CandidateInfo genCandidate(double minFreq,
			int maxNonSelectDepth, FeatureFinder selector, QueryInfo[] queries)
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
			Settings.printUsage();
			System.exit(1);
		}
		Settings s = new Settings(args);
		if (s.directedSearch) {
			System.exit(1);
		}
		ExhaustiveGenerator m = new ExhaustiveGenerator(s);
		m.selector = selector;
		m.maxMustNonSelectSize = maxNonSelectDepth;
		m.finalCandidateInfo = null;
		m.optimumGain = -1;
		m.readGraphs(queries);
		m.startMining();
		return m.finalCandidateInfo;
	}

	/**
	 * Reads graphs from the given InputStream, parses them with the given
	 * parser, and creates graphs by using the given factory
	 * 
	 * @param in
	 *            an InputStream with graphs
	 * @param parser
	 *            a GraphParser
	 * @throws IOException
	 *             if an error ocurred while reading from the stream
	 * @throws ParseException
	 *             if one of the graphs could not be parsed
	 */
	protected void readGraphs(QueryInfo[] queries) throws IOException,
			ParseException {
		long t = 0;
		if (m_settings.debug > 0) {
			// System.out.print("Parsing graphs...");
			t = System.currentTimeMillis();
		}

		m_graphs = new ArrayList<Graph>(queries.length);
		for (int i = 0; i < queries.length; i++) {
			QueryInfo q = queries[i];
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
