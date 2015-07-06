package edu.psu.chemxseer.structure.parmolExtension;

/*
 * Created on Dec 11, 2004
 * 
 * Copyright 2004, 2005 Marc Wörlein
 * 
 * This file is part of ParMol.
 * ParMol is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * ParMol is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ParMol; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.parmol.AbstractMiner;
import de.parmol.Settings;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.parsers.*;
import de.parmol.util.*;
import de.parmol.GSpan.DFSCode;
import de.parmol.GSpan.DataBase;
import de.parmol.GSpan.GSpanEdge;
import de.parmol.GSpan.GraphSet;
import de.parmol.Gaston.Miner;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeaturePosting;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

//
/**
 * Edited by Dayu Yuan DayuYuan: All Distinct Edges are Returned (both frequent
 * & infrequent), All Edge-0 nodes are pruned, all frequent subgraphs are
 * returned
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class GSpanMiner_Edge extends AbstractMiner {

	// PrintStream debug;
	GraphFactory factory = GraphFactory.getFactory(GraphFactory.LIST_GRAPH
			| GraphFactory.UNDIRECTED_GRAPH);
	private int numberOfPatterns;

	/**
	 * create a new Miner
	 * 
	 * @param settings
	 */
	public GSpanMiner_Edge(Settings settings) {
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
			this.numberOfPatterns++;
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
		} else {
			m_settings.stats.earlyFilteredNonClosedFragments++;
		}
		return my;
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
		// 1. Parse the input
		Settings s = parseInput(args);
		// 2. Mining
		long startTime = System.currentTimeMillis();
		GSpanMiner_Edge m = doFrequentFeatureMining(s);
		System.out.println("The Total Time Complexity: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("The Total Number of Patterns Enumerated: "
				+ m.numberOfPatterns);
		System.out.println("The Total Number of Patterns Found: "
				+ m.m_frequentSubgraphs.size());

		// 3. return
		try {
			return getFeatures(m, s, featureFileName, postingFileName);
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
	 * @param maxNonSelectDepth
	 * @return
	 */
	public static PostingFeatures gSpanMining(String[] args,
			String postingFileName) {
		return gSpanMining(args, null, postingFileName);
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
	private static GSpanMiner_Edge doFrequentFeatureMining(Settings s) {
		if (s.directedSearch) {
			System.out.println(Miner.class.getName()
					+ " does not implement the search for directed graphs");
			System.exit(1);
		}
		System.out.println("YDY: Start Mining");
		GSpanMiner_Edge m = new GSpanMiner_Edge(s);
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

	private static PostingFeatures getFeatures(GSpanMiner_Edge m, Settings s,
			String featureFileName, String postingFileName) throws IOException {

		FileChannel postingChannel = null;
		if (postingFileName != null)
			postingChannel = new FileOutputStream(postingFileName).getChannel();

		List<IOneFeature> allFeatures = new ArrayList<IOneFeature>();
		int featureIndex = 0;

		for (Iterator it = m.m_frequentSubgraphs.iterator(); it.hasNext();) {
			FrequentFragment currentFragment = (FrequentFragment) it.next();
			// Only Prune Single Node
			if (currentFragment.getFragment().getEdgeCount() == 0)
				continue;
			// First write posting into postingFile
			Graph[] supportingSet = currentFragment.getSupportedGraphs();
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
		return new PostingFeatures(postingFileName, features);
	}
}
