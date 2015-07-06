package edu.psu.chemxseer.structure.parmolExtension;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import de.parmol.util.FrequentFragment;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureWrapperSimple;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeaturePosting;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;

/**
 * the GSpanMiner_MultiClass is implemented via "recursion", which is hard to
 * modify to support branch & bound
 * 
 * In this implementation, we change the "recursive" implementation to "iterate"
 * 
 * @author dayuyuan
 * 
 */
public class GSpanMiner_MultiClass_Iterative extends AbstractMiner {

	private Iterator edgeIterator;
	private LinkedList<Iterator> iteratorStack;
	private DataBase gs;
	private int[] classGraphsCount;
	private int[] deduction;
	private int incrementalID;
	private int numberOfPatterns;
	// When a pattern get returned, denotes whether there are childIterator
	// stored in the stack
	private boolean haveChildIterator;
	// PrintStream debug;
	GraphFactory factory = GraphFactory.getFactory(GraphFactory.LIST_GRAPH
			| GraphFactory.UNDIRECTED_GRAPH);

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

	/**
	 * Create a new graph miner, with input as a file in the settings
	 * 
	 * @param settings
	 * @param maxMustNonSelectSize
	 */
	public GSpanMiner_MultiClass_Iterative(Settings settings) {
		super(settings);
		this.m_frequentSubgraphs = new FragmentSet(); // may not be used
														// afterwards
		GraphSet.length = settings.minimumClassFrequencies.length;
		Debug.out = System.out;
		Debug.dlevel = m_settings.debug;
		this.iteratorStack = new LinkedList<Iterator>();
		this.incrementalID = 0;
		this.haveChildIterator = false;
		this.numberOfPatterns = 0;
		// Initialize the Graph Miner:
		try {
			this.setUp();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.startMining();
	}

	public void restart() {
		this.m_frequentSubgraphs = new FragmentSet(); // may not be used
														// afterwards
		GraphSet.length = super.m_settings.minimumClassFrequencies.length;
		Debug.out = System.out;
		Debug.dlevel = m_settings.debug;
		this.iteratorStack = new LinkedList<Iterator>();
		this.incrementalID = 0;
		this.haveChildIterator = false;
		this.numberOfPatterns = 0;
		// Initialize the Graph Miner:
		try {
			this.setUp();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.startMining();
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
			System.out.println("Usage: " + AbstractMiner.class.getName()
					+ " options, where options are:\n");
			Settings.printUsage();
			System.exit(1);
		}
		Settings s = null;
		try {
			s = new Settings(args);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return s;
	}

	private void initializeOutput() {
		// Get the Statistics of GraphCount
		this.classGraphsCount = null;
		for (Object g : this.m_graphs) {
			float[] freq = ((ClassifiedGraph) g).getClassFrequencies();
			if (classGraphsCount == null) {
				classGraphsCount = new int[freq.length];
				for (int i = 0; i < classGraphsCount.length; i++)
					classGraphsCount[i] = 0;

			}
			for (int i = 0; i < classGraphsCount.length; i++)
				classGraphsCount[i] += freq[i];
		}

		this.deduction = new int[classGraphsCount.length];
		for (int i = 0; i < deduction.length; i++) {
			if (i == 0)
				deduction[i] = 0;
			else
				deduction[i] = deduction[i - 1] + classGraphsCount[i - 1];
		}
	}

	/**
	 * Preparing the mining procedures
	 */
	@Override
	protected void startRealMining() {
		long start = System.currentTimeMillis();
		Debug.print(1, "renaming DataBase ... ");
		// Very important: only the frequent edges are returned in this case
		// ADDED By DAYU
		// This is added inorder to return all the distinct edges as features
		float[] dayuFrequency = new float[] { 1f, 0f };
		gs = new DataBase(m_graphs, dayuFrequency, m_frequentSubgraphs, factory);
		// gs = new DataBase(m_graphs, m_settings.minimumClassFrequencies,
		// m_frequentSubgraphs, factory);
		Debug.println(1, "done (" + (System.currentTimeMillis() - start)
				+ " ms)");

		Debug.println(1, "minSupport: " + m_settings.minimumClassFrequencies[0]);
		Debug.println(1, "graphs    : " + m_graphs.size());
		graphSet_Projection();
	}

	/**
	 * searches Subgraphs for each frequent edge in the DataBase
	 * 
	 * @param gs
	 */
	private void graphSet_Projection() {
		this.edgeIterator = gs.frequentEdges();
		this.initializeOutput();
	}

	/**
	 * Read One Edge, generate a DFS code for the edge Then call subgraph_mining
	 * 
	 * @param gs
	 */
	private FrequentFragment nextEdge() {
		GSpanEdge edge = (GSpanEdge) this.edgeIterator.next();
		DFSCode code = new DFSCode(edge, gs); // create DFSCode for the
		return subgraph_Mining(code); // recursive search // Dayu: modified by
										// Dayu, by using the stack
	}

	/**
	 * Start of the Iterative mining: put the "Iterator" in, return the current
	 * code.toFragment Return null if the code does not meet the constraints
	 * 
	 * @param edgeCode
	 *            the DFSCode with is found and checked for children
	 * @return the highest occurring frequency, of this branch
	 */
	private FrequentFragment subgraph_Mining(DFSCode edgeCode) {
		if (!edgeCode.isMin()) {
			Debug.println(2, edgeCode.toString(m_settings.serializer)
					+ " not min");
			m_settings.stats.duplicateFragments++;
			return null;
		}
		Iterator it = edgeCode.childIterator(m_settings.findTreesOnly,
				m_settings.findPathsOnly);
		if (this.iteratorStack.size() != 0) // at this time the iteratorStack
											// must be empty
			System.out.println("Sometime is wrong");
		// if(edgeCode.getSubgraph().getEdgeCount() <
		// m_settings.maximumFragmentSize)
		if (1 < m_settings.maximumFragmentSize)
			this.iteratorStack.push(it); // at the children iterator in stack
		if (m_settings.checkReportingConstraints(edgeCode.getSubgraph(),
				edgeCode.getFrequencies())) {
			this.haveChildIterator = true;
			return edgeCode.toFragment();
		} else
			return null;
	}

	/**
	 * (1) If the iteratorStack is empty, return null (2) Els if topIT, has
	 * next, construct a code and return, or return "null" if the code does not
	 * meet the constraints (3) topIT does not have next, pop the top iterator
	 * stack, and call subgraph_mining again
	 * 
	 * @return
	 */
	private FrequentFragment subgraph_Mining() {
		if (this.iteratorStack.isEmpty()) {
			this.edgeIterator.remove(); // shrink database
			return null;
		}

		Iterator topIT = this.iteratorStack.peek();
		if (topIT.hasNext()) {
			DFSCode code = (DFSCode) topIT.next();
			if (!code.isMin()) {
				Debug.println(2, code.toString(m_settings.serializer)
						+ " not min");
				m_settings.stats.duplicateFragments++;
				return null;
			}
			// System.out.println(MyFactory.getDFSCoder().serialize(code.getSubgraph()));
			if (code.isFrequent(m_settings.minimumClassFrequencies)) {
				if (code.getSubgraph().getEdgeCount() < m_settings.maximumFragmentSize) {
					// Keep on searching "next"'s children only if only next is
					// frequent & size < maxSize
					this.iteratorStack
							.push(code.childIterator(m_settings.findTreesOnly,
									m_settings.findPathsOnly));
					this.haveChildIterator = true;
				} else
					this.haveChildIterator = false;
				if (m_settings.checkReportingConstraints(code.getSubgraph(),
						code.getFrequencies())) {
					return code.toFragment();
				} else
					return null;
			} else
				return null; // not frequent at all
		} else {
			this.iteratorStack.pop(); // remove the dummy iterator
			return subgraph_Mining(); // call the subgraph_Mining() again
		}
	}

	/**
	 * Return the next pattern in the search tree: Return null if no next
	 * pattern exists
	 * 
	 * @return
	 */
	public FrequentFragment nextPattern() {
		FrequentFragment result = null;
		// 1. If the iteratorStack is empty, load a edge
		// return null, if no candidate edges left to be added
		if (this.iteratorStack.isEmpty()) {
			if (this.edgeIterator.hasNext())
				result = this.nextEdge();
			else
				return null; // no edge left
		} else {
			result = this.subgraph_Mining();
		}
		// result = null may be caused by
		// 1. The fragment returned does not meet criterion, thus return the
		// next
		// 2. The fragment can not be found, since this "edge" has no more
		// children available, thus go to (1)
		if (result == null)
			return nextPattern();
		else {
			numberOfPatterns++;
			return result;
		}
	}

	/**
	 * Prune the Current Branch For the previous code get returned, there are
	 * two cases (1) The code's children will be enumerate next, since an new
	 * iterator was stored in the stack (2) Else, the code's children will not
	 * be enumerated. in this case, the prune do nothing, we now we jump to
	 * another branch, then return false
	 */
	public boolean prune() {
		if (this.haveChildIterator) {
			this.iteratorStack.pop(); // remove the latest one
			this.haveChildIterator = false;
			return true;
		} else
			return false;
	}

	public int getEnumeratedPatternNum() {
		return this.numberOfPatterns;
	}

	/**
	 * Return the number graphs in different classes
	 * 
	 * @return
	 */
	public int[] getClassGraphCount() {
		return this.classGraphsCount;
	}

	/**
	 * Given a FrequentFragment pattern enumerated from the enumerator Construct
	 * and return a OneFeatureMultiClassWithPosting object
	 * 
	 * @param pattern
	 * @return
	 */
	public FeatureWrapperSimple getFeature(FrequentFragment pattern) {
		// 1. First Step:
		Graph[] supportingSet = pattern.getSupportedGraphs();
		int numofClasses = ((ClassifiedGraph) supportingSet[0])
				.getClassFrequencies().length;
		List<Integer>[] supportingList = new List[2 * numofClasses];
		for (int j = 0; j < supportingList.length; j++)
			supportingList[j] = new ArrayList<Integer>();

		for (int i = 0; i < supportingSet.length; i++) {
			float[] freq = ((ClassifiedGraph) supportingSet[i])
					.getClassFrequencies();
			for (int j = 0; j < freq.length; j++)
				if (freq[j] > 0)
					// equal support
					if (pattern.getFragment().getEdgeCount() == supportingSet[i]
							.getEdgeCount())
						supportingList[2 * j + 1].add(Integer
								.parseInt(supportingSet[i].getName())
								- deduction[j]);
					// containing support
					else
						supportingList[2 * j].add(Integer
								.parseInt(supportingSet[i].getName())
								- deduction[j]);
		}
		// 2. Second Step:
		int[][] thePostings = new int[supportingList.length][];
		for (int i = 0; i < thePostings.length; i++) {
			thePostings[i] = new int[supportingList[i].size()];
			for (int w = 0; w < supportingList[i].size(); w++)
				thePostings[i][w] = supportingList[i].get(w);
			Arrays.sort(thePostings[i]);
		}
		// 3. Third Step:
		Graph g = pattern.getFragment();
		int[] frequency = new int[thePostings.length];
		long[] shift = new long[thePostings.length];
		for (int i = 0; i < thePostings.length; i++)
			frequency[i] = thePostings[i].length;

		OneFeatureMultiClass feature = new OneFeatureMultiClass(MyFactory
				.getDFSCoder().serialize(g), frequency, shift, incrementalID++,
				false);

		return new FeatureWrapperSimple(feature, thePostings);
	}

	/************************** BELOW IS THE STATIC FUNCTION USED TO CALL **********************************/
	/*****************************************************************************************************/
	public static GSpanMiner_MultiClass_Iterative getMiner(String[] args) {
		Settings s = parseInput(args);
		s.debug = -1;
		GSpanMiner_MultiClass_Iterative m = new GSpanMiner_MultiClass_Iterative(
				s);
		return m;
	}

	/**
	 * Iterative Mining of the Search Space Sotre all the subgraphs meeting the
	 * constrain into m_frequentSubgraphs data member
	 */
	private void iterativeMining() {
		numberOfPatterns = 0;
		this.m_frequentSubgraphs.clear();
		FrequentFragment fragment = nextPattern();
		while (fragment != null) {
			// System.out.println(MyFactory.getUnCanDFS().serialize(fragment.getFragment()));
			this.m_frequentSubgraphs.add(fragment);
			fragment = nextPattern();
		}
	}

	/**
	 * Return the Features Mined
	 */
	private static PostingFeaturesMultiClass getFeatures(
			GSpanMiner_MultiClass_Iterative m, Settings s,
			String featureFileName, String[] postingFileNames)
			throws IOException {

		// Start OutPutting
		FileChannel[] postingChannels = null;
		if (postingFileNames != null) {
			postingChannels = new FileChannel[postingFileNames.length];
			for (int i = 0; i < postingChannels.length; i++) {
				if (postingFileNames[i] != null)
					postingChannels[i] = new FileOutputStream(
							postingFileNames[i]).getChannel();
			}
		}

		List<OneFeatureMultiClass> allFeatures = new ArrayList<OneFeatureMultiClass>();
		int featureIndex = 0;
		for (Iterator it = m.m_frequentSubgraphs.iterator(); it.hasNext();) {
			FrequentFragment currentFragment = (FrequentFragment) it.next();
			// prune single nodes and infrequent nodes
			if (currentFragment.getFragment().getEdgeCount() == 0)
				continue;
			// First write posting into postingFile
			Graph[] supportingSet = currentFragment.getSupportedGraphs();
			int numofClasses = ((ClassifiedGraph) supportingSet[0])
					.getClassFrequencies().length;
			long[] shift = new long[2 * numofClasses];
			List<Integer>[] supportingList = new List[2 * numofClasses];
			for (int j = 0; j < supportingList.length; j++)
				supportingList[j] = new ArrayList<Integer>();

			for (int i = 0; i < supportingSet.length; i++) {
				float[] freq = ((ClassifiedGraph) supportingSet[i])
						.getClassFrequencies();
				for (int j = 0; j < freq.length; j++)
					if (freq[j] > 0)
						// equal support
						if (currentFragment.getFragment().getEdgeCount() == supportingSet[i]
								.getEdgeCount())
							supportingList[2 * j + 1].add(Integer
									.parseInt(supportingSet[i].getName())
									- m.deduction[j]);
						// containing support
						else
							supportingList[2 * j].add(Integer
									.parseInt(supportingSet[i].getName())
									- m.deduction[j]);
			}
			// 1. Write the Postings
			if (postingChannels != null) {
				for (int i = 0; i < shift.length; i++) {
					int[] thePosting = new int[supportingList[i].size()];
					for (int w = 0; w < supportingList[i].size(); w++)
						thePosting[w] = supportingList[i].get(w);
					shift[i] = FeaturePosting.savePostings(postingChannels[i],
							thePosting, featureIndex);
				}
			}

			// Second, create the feature
			int[] frequency = new int[2 * numofClasses];
			for (int j = 0; j < frequency.length; j++)
				frequency[j] = supportingList[j].size();

			OneFeatureMultiClass theFeature = new OneFeatureMultiClass(
					s.serializer.serialize(currentFragment.getFragment()),
					frequency, shift, featureIndex, false);
			allFeatures.add(theFeature);
			featureIndex++;
		}
		if (postingChannels != null) {
			for (int j = 0; j < postingChannels.length; j++) {
				if (postingChannels[j] != null)
					postingChannels[j].close();
			}
		}
		NoPostingFeatures<OneFeatureMultiClass> features = new NoPostingFeatures<OneFeatureMultiClass>(
				featureFileName, allFeatures, false);

		return new PostingFeaturesMultiClass(postingFileNames, features,
				m.classGraphsCount);
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
	public static PostingFeaturesMultiClass gSpanMining(String[] args,
			String featureFileName, String[] postingFileNames) {
		// 1. Parse the input
		Settings s = parseInput(args);
		// 2. Mining
		long startTime = System.currentTimeMillis();
		GSpanMiner_MultiClass_Iterative m = new GSpanMiner_MultiClass_Iterative(
				s);
		float[] freq = s.minimumClassFrequencies;
		for (float oneFreq : freq) {
			System.out.print("minFrequency: " + oneFreq + ", ");
		}
		System.out.println();

		m.iterativeMining();
		System.out.println("The Total Time Complexity: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("The Total Number of Patterns Enumerated: "
				+ m.numberOfPatterns);
		System.out.println("The Total Number of Patterns Found: "
				+ m.m_frequentSubgraphs.size());
		// 3. return
		try {
			return getFeatures(m, s, featureFileName, postingFileNames);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public FrequentFragment[] genAllDistinctEdges() {
		List<FrequentFragment> result = new ArrayList<FrequentFragment>();
		while (this.edgeIterator.hasNext()) {
			GSpanEdge edge = (GSpanEdge) this.edgeIterator.next();
			DFSCode code = new DFSCode(edge, gs); // create DFSCode for the
			result.add(code.toFragment());
		}
		this.restart();
		FrequentFragment[] finalResult = new FrequentFragment[result.size()];
		result.toArray(finalResult);
		return finalResult;
	}

	public void setMinSupport(float[] minimumSupport) {
		int length = m_settings.minimumClassFrequencies.length;
		for (int i = 0; i < length; i++)
			m_settings.minimumClassFrequencies[i] = minimumSupport[i];
		this.restart();
	}

	public static GSpanMiner_MultiClass_Iterative getMiner(String[] args,
			List<ClassifiedGraph> inputGraphs) {
		Settings s = parseInput(args);
		s.debug = -1;
		GSpanMiner_MultiClass_Iterative m = new GSpanMiner_MultiClass_Iterative(
				s, inputGraphs);
		return m;
	}

	private void setUp2(Settings s, List<ClassifiedGraph> graphs)
			throws IOException {
		GraphParser parser = s.parser;
		long t = 0;
		// The only place that are changed
		m_graphs = graphs;

		// End of the change
		m_settings.directedSearch = parser.directed();
		int classCount = 2;
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

	/**
	 * Create a new graph miner, with input as a list of classified graphs
	 * 
	 * @param settings
	 * @param inputGraphs
	 */
	public GSpanMiner_MultiClass_Iterative(Settings settings,
			List<ClassifiedGraph> inputGraphs) {
		super(settings);
		this.m_frequentSubgraphs = new FragmentSet(); // may not be used
														// afterwards
		GraphSet.length = settings.minimumClassFrequencies.length;
		Debug.out = System.out;
		Debug.dlevel = m_settings.debug;
		this.iteratorStack = new LinkedList<Iterator>();
		this.incrementalID = 0;
		this.haveChildIterator = false;
		this.numberOfPatterns = 0;
		// Initialize the Graph Miner:
		try {
			this.setUp2(settings, inputGraphs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.startMining();
	}
}
