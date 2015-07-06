package edu.psu.chemxseer.structure.parmolExtension;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import de.parmol.AbstractMiner;
import de.parmol.Settings;
import de.parmol.graph.ClassifiedGraph;
import de.parmol.graph.DefaultGraphClassifier;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.parsers.*;
import de.parmol.util.*;
import de.parmol.GSpan.DFSCode;
import de.parmol.GSpan.DataBase;
import de.parmol.GSpan.GSpanEdge;
import de.parmol.GSpan.GraphSet;
import de.parmol.Gaston.Miner;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeaturePosting;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

//
/**
 * Edited by Dayu Yuan This class represents the Mining algorithm gSpan that
 * will be applied on FGindex DayuYuan: Only frequent subgraph with edges >=1
 * are returned (single node subgraph or other infrequent subgraphs are not
 * returned)
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class GSpanMiner_Frequent extends AbstractMiner {

	// PrintStream debug;
	GraphFactory factory = GraphFactory.getFactory(GraphFactory.LIST_GRAPH
			| GraphFactory.UNDIRECTED_GRAPH);
	private int numberOfPatterns;

	/**
	 * create a new Miner
	 * 
	 * @param settings
	 */
	public GSpanMiner_Frequent(Settings settings) {
		super(settings);
		this.m_frequentSubgraphs = new FragmentSet();
		GraphSet.length = settings.minimumClassFrequencies.length;
		empty = new float[settings.minimumClassFrequencies.length];
		Debug.out = System.out;
		Debug.dlevel = m_settings.debug;
		this.numberOfPatterns = 0;
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
		DataBase gs = new DataBase(m_graphs,
				m_settings.minimumClassFrequencies, m_frequentSubgraphs,
				factory);
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
			this.numberOfPatterns++;
			// current edge
			long time = System.currentTimeMillis();
			// Debug.print(1, "doing seed " +
			// m_settings.serializer.serialize(code.toFragment().getFragment())
			// + " ...");
			// Debug.println(2,"");
			subgraph_Mining(code); // recursive search
			eit.remove(); // shrink database
			// Debug.println(1, "\tdone (" + (System.currentTimeMillis() - time)
			// + " ms)");
			if (gs.size() < m_settings.minimumClassFrequencies[0]
					&& gs.size() != 0) { // not needed
				Debug.println("remaining Graphs: " + gs.size());
				Debug.println("May not happen!!!");
				return;
			}
		}
		// Debug.println(2, "remaining Graphs: " + gs.size());
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
	 *            the DFSCode with is found and checked for childs
	 * @return the highest occuring frequency, of this branch
	 */
	private float[] subgraph_Mining(DFSCode code) {
		if (!code.isMin()) {
			// Debug.println(2,
			// code.toString(m_settings.serializer)+" not min");
			m_settings.stats.duplicateFragments++;
			return empty;
		}
		float[] max = empty;

		float[] my = code.getFrequencies();
		Debug.println(2,
				"  found graph " + code.toString(m_settings.serializer));
		if (code.getSubgraph().getEdgeCount() < m_settings.maximumFragmentSize) {
			Iterator it = code.childIterator(m_settings.findTreesOnly,
					m_settings.findPathsOnly);
			for (; it.hasNext();) {
				DFSCode next = (DFSCode) it.next();
				this.numberOfPatterns++;

				// This is edited by Dayu, affect efficiency
				if (next.isFrequent(m_settings.minimumClassFrequencies)) {
					float[] a = subgraph_Mining(next);
					max = getMax(max, a);
				} else
					continue; // early pruning
			}
		}

		if ((!m_settings.closedFragmentsOnly || max == empty || unequal(my, max))
				&& m_settings.checkReportingConstraints(code.getSubgraph(),
						code.getFrequencies())) {
			m_frequentSubgraphs.add(code.toFragment());
			// TEST
			System.out.println(MyFactory.getDFSCoder().serialize(
					code.toFragment().getFragment()));
		} else {
			m_settings.stats.earlyFilteredNonClosedFragments++;
		}
		return my;
	}

	/**
	 * First Step of Mining
	 * 
	 * @param args
	 * @return
	 */
	private static Settings parseInput(String[] args) {
		System.out.println("In GSpanMiner");
		if ((args.length == 0) || args[0].equals("--help")) {
			// System.out.println("Usage: " + AbstractMiner.class.getName() +
			// " options, where options are:\n");
			Settings.printUsage();
			System.exit(1);
		}
		Settings s = null;
		try {
			s = new Settings(args);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Second Step of Mining
	 * 
	 * @param s
	 * @param maxMustNonSelectSize
	 * @return
	 */
	private static GSpanMiner_Frequent doFrequentFeatureMining(Settings s) {
		if (s.directedSearch) {
			System.out.println(Miner.class.getName()
					+ " does not implement the search for directed graphs");
			System.exit(1);
		}
		System.out.println("YDY: Start Mining");
		GSpanMiner_Frequent m = new GSpanMiner_Frequent(s);
		try {
			m.setUp();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m.startMining();
		return m;
	}

	private static GSpanMiner_Frequent doFrequentFeatureMining(Settings s,
			IGraphDatabase gDB) throws IOException {
		if (s.directedSearch) {
			System.out.println(Miner.class.getName()
					+ " does not implement the search for directed graphs");
			System.exit(1);
		}
		System.out.println("YDY: Start Mining");
		GSpanMiner_Frequent m = new GSpanMiner_Frequent(s);
		m.setUp2(s, gDB);
		m.startMining();
		return m;
	}

	private void setUp2(Settings s, IGraphDatabase features) throws IOException {
		GraphParser parser = s.parser;
		long t = 0;
		// The only place that are changed
		m_graphs = new ArrayList<Graph>(features.getTotalNum());
		for (int i = 0; i < features.getTotalNum(); i++) {
			// Have to parse the DFS code, since we need to assign the graph to
			// a certain graph ID
			String realID = (new Integer(i)).toString();
			Graph theGraph = features.findGraph(i);
			if (theGraph.getName().equals(realID))
				m_graphs.add(theGraph);
			else {
				String dfsCode = MyFactory.getUnCanDFS().serialize(
						features.findGraph(i));
				m_graphs.add(MyFactory.getUnCanDFS().parse(dfsCode,
						(new Integer(i)).toString(),
						MyFactory.getGraphFactory()));
			}
		}
		// End of the change
		m_settings.directedSearch = parser.directed();
		int classCount = 1;
		if (m_settings.classFrequencyFile != null) {
			DefaultGraphClassifier classifier = new DefaultGraphClassifier(
					m_settings.classFrequencyFile);

			for (Iterator it = m_graphs.iterator(); it.hasNext();) {
				Graph g = (Graph) it.next();
				if (g instanceof ClassifiedGraph) {
					float[] cf = classifier.getClassFrequencies(g.getName());
					if (cf == null) {
						// System.err.println("No class frequencies found for "
						// + g.getName());
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
			// System.out.println("done (" + (System.currentTimeMillis() - t) +
			// "ms)");

			if (m_settings.debug > 3) {
				int maxEdgeCount = 0, edgeSum = 0;
				BitSet nodeLabelsUsed = new BitSet(), edgeLabelsUsed = new BitSet();
				for (java.util.Iterator it = m_graphs.iterator(); it.hasNext();) {
					de.parmol.graph.Graph g = (de.parmol.graph.Graph) it.next();

					edgeSum += g.getEdgeCount();
					maxEdgeCount = java.lang.Math.max(maxEdgeCount,
							g.getEdgeCount());

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

	private static PostingFeatures getFeatures(GSpanMiner_Frequent m,
			Settings s, String featureFileName, String postingFileName)
			throws IOException {
		FileChannel postingChannel = null;
		if (postingFileName != null)
			postingChannel = new FileOutputStream(postingFileName).getChannel();

		List<IOneFeature> allFeatures = new ArrayList<IOneFeature>();
		int featureIndex = 0;
		for (Iterator it = m.m_frequentSubgraphs.iterator(); it.hasNext();) {
			FrequentFragment currentFragment = (FrequentFragment) it.next();
			// prune single nodes and infrequent nodes
			if (currentFragment.getFragment().getEdgeCount() == 0) {
				continue;
			}
			// First write posting into postingFile
			Graph[] supportingSet = currentFragment.getSupportedGraphs();
			int[] supportingList = new int[supportingSet.length];
			for (int i = 0; i < supportingSet.length; i++)
				supportingList[i] = Integer
						.parseInt(supportingSet[i].getName());

			long shift = -1;
			// 1. Write the Postings
			if (postingChannel != null) {
				shift = FeaturePosting.savePostings(postingChannel,
						supportingList, featureIndex);
			}
			IOneFeature theFeature = new OneFeatureImpl(
					s.serializer.serialize(currentFragment.getFragment()),
					supportingList.length, shift, featureIndex, false);
			allFeatures.add(theFeature);
			featureIndex++;
		}
		// 2. Write the Features
		NoPostingFeatures<IOneFeature> features = new NoPostingFeatures<IOneFeature>(
				featureFileName, allFeatures, false);

		if (postingChannel != null)
			postingChannel.close();
		// 3. Return
		return new PostingFeatures(postingFileName, features);
	}

	/**
	 * Mining frequent subgraph features: all have to be frequent Save the
	 * feature to featureFileName, and postignFileName Features are returned as
	 * GFeature
	 * 
	 * @param args
	 * @param featureFileName
	 * @param postingFileName
	 * @param maxMustNonSelectSize
	 * @return
	 */
	public static PostingFeatures gSpanMining(String[] args,
			String featureFileName, String postingFileName) {
		long startTime = System.currentTimeMillis();
		// 1. Parse the input
		Settings s = parseInput(args);
		// 2. Mining
		GSpanMiner_Frequent m = doFrequentFeatureMining(s);
		// Total Number Of Features Encountered
		String titleString = "Mine Frequent Subgraphs with Min Support";
		for (int i = 0; i < s.minimumClassFrequencies.length; i++)
			titleString += " " + s.minimumClassFrequencies[i];
		System.out.println(titleString);
		System.out.println("The Total Time Complexity: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("The Total Number of Subgraphs Enumerated: "
				+ m.numberOfPatterns);
		System.out.println("The Total Number of Subgraphs Found: "
				+ m.m_frequentSubgraphs.size());
		// 3. return
		try {
			PostingFeatures result = getFeatures(m, s, featureFileName,
					postingFileName);
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Mining frequent features, posting File is saved but feature file is not
	 * 
	 * @param args
	 * @param postingFileName
	 * @return
	 */
	public static PostingFeatures gSpanMining(String[] args,
			String postingFileName) {
		return gSpanMining(args, null, postingFileName);
	}

	/****************************** Following two are for Supgraph search ************************/
	/**
	 * Mining frequent features out of "features", both features and postings
	 * are stored
	 * 
	 * @param features
	 * @param args
	 * @param featureFile
	 * @param postingFileName
	 * @return
	 */
	public static PostingFeatures gSpanMining(IGraphDatabase gDB,
			String[] args, String featureFile, String postingFileName) {
		// 1. Parse the input
		Settings s = parseInput(args);
		// 2. Mining
		long startTime = System.currentTimeMillis();
		GSpanMiner_Frequent m = null;
		try {
			m = doFrequentFeatureMining(s, gDB);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String titleString = "Mine Frequent Subgraphs with Min Support";
		for (int i = 0; i < s.minimumClassFrequencies.length; i++)
			titleString += " " + s.minimumClassFrequencies[i];
		System.out.println(titleString);
		System.out.println("The Total Time Complexity: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("The Total Number of Subgraphs Enumerated: "
				+ m.numberOfPatterns);
		System.out.println("The Total Number of Subgraphs Found: "
				+ m.m_frequentSubgraphs.size());
		// 3. return
		try {
			return getFeatures(m, s, featureFile, postingFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Mining frequent features out of "features", feature is not stored but
	 * posting is stored
	 * 
	 * @param features
	 * @param args
	 * @param postingFileName
	 * @return
	 */
	public static PostingFeatures gSpanMining(IGraphDatabase gDB,
			String[] args, String postingFileName) {
		return gSpanMining(gDB, args, null, postingFileName);
	}
}
