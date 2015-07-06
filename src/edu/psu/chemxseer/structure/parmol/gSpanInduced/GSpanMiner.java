package edu.psu.chemxseer.structure.parmol.gSpanInduced;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.parmol.Settings;
import de.parmol.GSpan.Miner;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.parsers.GraphParser;
import de.parmol.util.Debug;

import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeaturePosting;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * Edited by DayuYuan: 
 * For GindexMining: all frequent subgraph + 
 * small subgraphs (edge count < minMustSelectSize) 
 * are returned.
 * 
 * Both supporting set and cover set are returned
 * By Covers et I mean database graphs that has one subgraph as an edge-induced subgraph
 */
//
/**
 * This class represents the Mining algorithm gSpan All Subgraphs of edge less
 * than minMustSelectSize are returned, except for all 0-edge nodes The feature
 * are directly mined with decreasing minimum support as defined in Gindex paper
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class GSpanMiner extends AbstractMiner {

	private int minMustSelectSize;
	private int maxMustNonSelectSize;
	// PrintStream debug;
	GraphFactory factory = GraphFactory.getFactory(GraphFactory.LIST_GRAPH
			| GraphFactory.UNDIRECTED_GRAPH);

	/**
	 * create a new Miner
	 * 
	 * @param settings
	 */
	public GSpanMiner(Settings settings) {
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
		DataBaseModified gs = new DataBaseModified(m_graphs, dayuFrequency,
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
	private void graphSet_Projection(DataBaseModified gs) {
		for (Iterator eit = gs.frequentEdges(); eit.hasNext();) {
			GSpanEdge edge = (GSpanEdge) eit.next();
			DFSCode code = new DFSCode(edge, gs); // create DFSCode for the
			// current edge
			long time = System.currentTimeMillis();
			Debug.print(
					1,
					"doing seed "
							+ m_settings.serializer.serialize(code.toFragment()
									.getFragment()) + " ...");
			Debug.println(2, "");
			subgraph_Mining(code); // recursive search
			// eit.remove(); //shrink database
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
	 *            the DFSCode with is found and checked for childs
	 * @return the highest occuring frequency, of this branch
	 */
	private float[] subgraph_Mining(DFSCode code) {
		if (!code.isMin()) {
			Debug.println(2, code.toString(m_settings.serializer) + " not min");
			m_settings.stats.duplicateFragments++;
			return empty;
		}
		float[] max = empty;

		float[] my = code.getFrequencies();
		Debug.println(2,
				"  found graph " + code.toString(m_settings.serializer));

		Iterator it = code.childIterator(m_settings.findTreesOnly,
				m_settings.findPathsOnly);
		for (; it.hasNext();) {
			DFSCode next = (DFSCode) it.next();
			if (next.toFragment().getFragment().getEdgeCount() < this.minMustSelectSize
					|| (next.isFrequent(m_settings.minimumClassFrequencies) && next
							.toFragment().getFragment().getEdgeCount() <= this.maxMustNonSelectSize)) {
				float[] a = subgraph_Mining(next);
				max = getMax(max, a);
			} else
				continue; // early pruning
		}
		if ((!m_settings.closedFragmentsOnly || max == empty || unequal(my, max))
				&& m_settings.checkReportingConstraints(code.getSubgraph(),
						code.getFrequencies())) {
			m_frequentSubgraphs.add(code.toFragment());
		} else {
			m_settings.stats.earlyFilteredNonClosedFragments++;
		}
		return my;
	}

	/**
	 * 
	 * @param args
	 * @param featureFileName
	 *            : save all the features
	 * @param coverFileName
	 *            : save the induced postings only
	 * @param minMustSelect
	 * @param maxMustNonSelectSize
	 * @return
	 */
	public static PostingFeatures gSpanMining(String[] args,
			String featureFileName, String coverFileName, int minMustSelect,
			int maxMustNonSelectSize) {
		// 1. Parse the input
		Settings s = parseInput(args);
		// 2. Mining
		GSpanMiner m = doFrequentFeatureMining(s, minMustSelect,
				maxMustNonSelectSize);
		// 3. return
		try {
			return getFeatures(m, s, featureFileName, coverFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	private static GSpanMiner doFrequentFeatureMining(Settings s,
			int minMustSelect, int maxMustNonSelectSize) {
		if (s.directedSearch) {
			System.out.println(Miner.class.getName()
					+ " does not implement the search for directed graphs");
			System.exit(1);
		}
		System.out.println("YDY: Start Mining");
		GSpanMiner m = new GSpanMiner(s);
		m.minMustSelectSize = minMustSelect;
		m.maxMustNonSelectSize = maxMustNonSelectSize;
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

	private static PostingFeatures getFeatures(GSpanMiner m, Settings s,
			String featureFileName, String coverFileName) throws IOException {
		FileChannel postingChannel = null;
		if (featureFileName != null)
			postingChannel = new FileOutputStream(coverFileName).getChannel();

		List<IOneFeature> allFeatures = new ArrayList<IOneFeature>();
		int featureIndex = 0;

		for (Iterator it = m.m_frequentSubgraphs.iterator(); it.hasNext();) {
			FrequentFragment currentFragment = (FrequentFragment) it.next();
			// Only Prune Single Node
			if (currentFragment.getFragment().getEdgeCount() == 0)
				continue;
			Graph[] supportingSet = currentFragment.getCoveredGraphs();
			int[] supportingList = new int[supportingSet.length];
			for (int i = 0; i < supportingSet.length; i++)
				supportingList[i] = Integer
						.parseInt(supportingSet[i].getName());

			long shift = -1;
			IOneFeature theFeature = new OneFeatureImpl(
					s.serializer.serialize(currentFragment.getFragment()),
					supportingList.length, shift, featureIndex, false);
			// 1. Write the Postings
			if (postingChannel != null) {
				shift = FeaturePosting.savePostings(postingChannel,
						supportingList, featureIndex);
				theFeature.setPostingShift(shift);
			}
			allFeatures.add(theFeature);
			featureIndex++;
		}
		// 2. Write the Features
		NoPostingFeatures<IOneFeature> features = new NoPostingFeatures<IOneFeature>(
				featureFileName, allFeatures, false);

		if (postingChannel != null)
			postingChannel.close();
		// 3. Return
		return new PostingFeatures(coverFileName, features);
	}

}
