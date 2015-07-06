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
package edu.psu.chemxseer.structure.parmol.gSpanInduced;

import java.util.*;

import de.parmol.GSpan.EdgeNodeRelabler;
import de.parmol.GSpan.Relabler;
import de.parmol.graph.*;

/**
 * This class representates the shrunkable Graph Dataset
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class DataBaseModified implements EdgeNodeRelabler {

	private SortedSet sortedFrequentGSpanEdges;
	private Map edgeMap;
	private int[] nodeLabels;
	private int[] edgeLabels;
	Relabler nodeRelabler;
	Relabler edgeRelabler;
	private int size;
	/** the factory used for building frequent fragments */
	public GraphFactory factory;
	/** the size of the biggest DataBase Graph */
	public int maxNodeCount = 0;

	/**
	 * creates a new sorted, cleandup and renamed Dataset as expected in
	 * graphSet_Projection
	 * 
	 * @param graphs
	 * @param minFreq
	 * @param fragments
	 * @param factory
	 */
	public DataBaseModified(Collection graphs, float[] minFreq,
			FragmentSet fragments, GraphFactory factory) {
		Map graphEdges = new HashMap();
		Map graphNodes = new HashMap();
		this.factory = factory;
		size = graphs.size();

		readGraphs(graphs, graphEdges, graphNodes);
		nodeRelabler = doNodes(graphNodes, minFreq, fragments);
		edgeRelabler = doEdges(graphEdges, minFreq);

		edgeMap = getAllEdges(graphs, nodeRelabler, edgeRelabler);
		// int count = edgeMap.size();
		sortedFrequentGSpanEdges = filterInfrequent(minFreq);
		// int count2 =edgeMap.size();
		// System.out.println();

	}

	/**
	 * counts the node and edge labels in the collection
	 * 
	 * @param graphs
	 * @param edges
	 *            the map for the edgelabel counts
	 * @param nodes
	 *            the map for the nodelabel counts
	 */
	private void readGraphs(Collection graphs, Map edges, Map nodes) {
		for (Iterator git = graphs.iterator(); git.hasNext();) {
			ClassifiedGraph graph = (ClassifiedGraph) git.next();
			for (int i = 0; i < graph.getNodeCount(); i++) {
				Integer lab = new Integer(graph.getNodeLabel(graph.getNode(i)));
				GraphSet s = (GraphSet) nodes.get(lab);
				if (s == null) {
					s = new GraphSet();
					nodes.put(lab, s);
				}
				s.add(graph);
			}
			for (int i = 0; i < graph.getEdgeCount(); i++) {
				Integer lab = new Integer(graph.getEdgeLabel(graph.getEdge(i)));
				GraphSet s = (GraphSet) edges.get(lab);
				if (s == null) {
					s = new GraphSet();
					edges.put(lab, s);
				}
				s.add(graph);
			}
		}
	}

	/**
	 * Used by doNodes(..) and doEdges(..)
	 * 
	 * @author Marc Woerlein (simawoer@stud.informatik.uni-erlangen.de)
	 */
	private class Tuple implements Comparable {
		int count;
		int label;

		/**
		 * creates a new Tuple
		 * 
		 * @param c
		 *            count of the given label l
		 * @param l
		 */
		public Tuple(int c, int l) {
			this.count = c;
			this.label = l;
		}

		@Override
		public int compareTo(Object o) {
			Tuple other = (Tuple) o;
			if (this.count != other.count)
				return other.count - this.count;
			return other.label - this.label;
		}

		@Override
		public String toString() {
			return label + "(" + count + ")";
		}
	}

	/**
	 * creates a node Relabler for frequent node labels and add single noded
	 * frequent Graphs to fragments
	 * 
	 * @param nodes
	 *            map of nodelabel counts
	 * @param minFreq
	 * @param fragments
	 *            the set for frequent graphs
	 * @return the builded Relabler
	 */
	private Relabler doNodes(Map nodes, float[] minFreq, FragmentSet fragments) {
		SortedSet list = new TreeSet();
		int max = -1;
		for (Iterator nit = nodes.keySet().iterator(); nit.hasNext();) {
			Integer n = (Integer) nit.next();
			int ni = n.intValue();
			GraphSet s = (GraphSet) nodes.get(n);
			if (s.isFrequent(minFreq)) {
				list.add(new Tuple(s.size(), ni));
				if (ni > max)
					max = ni;
				// create single noded frequent Graph
				MutableGraph ng = factory.createGraph();
				ng.addNode(n.intValue());
				float frequencies[] = new float[1];
				frequencies[0] = s.size();
				fragments.add(new FrequentFragment(ng, s, s, frequencies));
				// ---
			}
		}
		Relabler nodeRelabler = new Relabler(max, Graph.NO_NODE);
		nodeLabels = new int[list.size()];
		int i = 0;
		for (Iterator it = list.iterator(); it.hasNext(); i++) {
			Tuple t = (Tuple) it.next();
			nodeLabels[i] = t.label;
			nodeRelabler.addLabel(t.label, i);
		}
		return nodeRelabler;
	}

	/**
	 * @param edges
	 *            map of edgelable counts
	 * @param minFreq
	 * @return a edge Relabler for frequent edge labels
	 */
	private Relabler doEdges(Map edges, float[] minFreq) {
		SortedSet list = new TreeSet();
		int max = -1;
		for (Iterator eit = edges.keySet().iterator(); eit.hasNext();) {
			Integer e = (Integer) eit.next();
			int ei = e.intValue();
			GraphSet s = (GraphSet) edges.get(e);
			if (s.isFrequent(minFreq)) {
				list.add(new Tuple(s.size(), ei));
				if (ei > max)
					max = ei;
			}
		}
		Relabler edgeRelabler = new Relabler(max, Graph.NO_EDGE);
		edgeLabels = new int[list.size()];
		int i = 0;
		for (Iterator it = list.iterator(); it.hasNext(); i++) {
			Tuple t = (Tuple) it.next();
			edgeLabels[i] = t.label;
			edgeRelabler.addLabel(t.label, i);
		}
		return edgeRelabler;
	}

	/**
	 * build for each graph in the Collection a corresponding GSpanGraph and
	 * builds an map between edges an containing GSpanGraph
	 * 
	 * @param graphs
	 *            the set of graphs
	 * @param nodeR
	 *            for renaming nodes
	 * @param edgeR
	 *            for renaming edges
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	private Map getAllEdges(Collection graphs, Relabler nodeR, Relabler edgeR) {
		Map map = new TreeMap();
		for (Iterator git = graphs.iterator(); git.hasNext();) {
			ClassifiedGraph g = (ClassifiedGraph) git.next();
			GSpanGraph gg = new GSpanGraph(g, nodeR, edgeR, factory);
			int nodeCount = gg.getRealGraph().getNodeCount();
			if (maxNodeCount < nodeCount)
				maxNodeCount = nodeCount;
			if (gg.getEdgeCount() == 0)
				size--;
			for (Iterator eit = gg.edgeIterator(); eit.hasNext();) {
				GSpanEdge e = (GSpanEdge) eit.next();
				GraphSet s = (GraphSet) map.get(e);
				if (s == null) {
					s = new GraphSet();
					map.put(e, s);
				}
				s.add(gg);
			}
		}
		return map;
	}

	/**
	 * removes all Infrequent edges
	 * 
	 * @param minFreq
	 * @return a set of frequent Edges
	 */
	private SortedSet filterInfrequent(float[] minFreq) {
		SortedSet edges = new TreeSet();
		for (Iterator eit = edgeMap.keySet().iterator(); eit.hasNext();) {
			GSpanEdge e = (GSpanEdge) eit.next();
			GraphSet s = (GraphSet) edgeMap.get(e);
			if (!s.isFrequent(minFreq)) {
				for (Iterator git = s.iterator(); git.hasNext();) {
					GSpanGraph gg = (GSpanGraph) git.next();
					gg.remove(e);
					if (gg.getEdgeCount() == 0)
						size--;
				}
				eit.remove();
			} else
				edges.add(e);
		}
		return edges;
	}

	/**
	 * @return an iterator over all frequent edges in this DataBase
	 */
	public Iterator frequentEdges() {
		return new Iterator() {
			GSpanEdge[] edgeArray = (GSpanEdge[]) sortedFrequentGSpanEdges
					.toArray(new GSpanEdge[sortedFrequentGSpanEdges.size()]);
			int pos = 0;

			@Override
			public boolean hasNext() {
				return pos < edgeArray.length;
			}

			@Override
			public Object next() {
				if (hasNext()) {
					return edgeArray[pos++];
				} else
					throw new NoSuchElementException("No more elements");
			}

			@Override
			public void remove() {
				GSpanEdge edge = edgeArray[pos - 1];
				edgeArray[pos - 1] = null;
				if (edge == null)
					throw new NoSuchElementException("element always deleted");
				for (Iterator ggit = getContainingGraphs(edge).iterator(); ggit
						.hasNext();) {
					GSpanGraph gg = (GSpanGraph) ggit.next();
					gg.remove(edge);
					if (gg.getEdgeCount() == 0)
						size--;
				}
				sortedFrequentGSpanEdges.remove(edge);
				edgeMap.remove(edge);
			}
		};
	}

	/**
	 * @param edge
	 * @return a set of GSpanGraphs containing the given edge
	 */
	// public GraphSet getContainingGraphs(GSpanEdge edge){
	// return (GraphSet) edgeMap.get(edge);
	// }

	public GraphSet getContainingGraphs(GSpanEdge edge) {
		if (edge == null) {
			// get all the graphs that are in the database
			GraphSet allSet = new GraphSet();
			for (Iterator eit = this.frequentEdges(); eit.hasNext();) {
				GSpanEdge oneEdge = (GSpanEdge) eit.next();
				GraphSet set = (GraphSet) this.edgeMap.get(oneEdge);
				allSet.addAll(set);
			}
			return allSet;
		} else
			return (GraphSet) edgeMap.get(edge);
	}

	/**
	 * @return the number ob remaining Graphs in this Set
	 */
	public int size() {
		return size;
	}

	// for relabeling real node/edge labels to/from DataBase node/edge labels
	@Override
	public int getRealEdgeLabel(int edge) {
		return edgeLabels[edge];
	}

	@Override
	public int getRealNodeLabel(int node) {
		return nodeLabels[node];
	}

	@Override
	public int getEdgeLabel(int edge) {
		return edgeRelabler.getLabel(edge);
	}

	@Override
	public int getNodeLabel(int node) {
		return nodeRelabler.getLabel(node);
	}

}