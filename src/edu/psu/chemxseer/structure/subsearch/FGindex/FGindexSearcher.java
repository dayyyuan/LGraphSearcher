package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex1;
import edu.psu.chemxseer.structure.util.OrderedIntSet;

/**
 * Searcher of the Index-IGI or Ondisk-IGI
 * 
 * @author duy113
 * 
 */
public class FGindexSearcher implements IIndex1 {
	// graphArrays stored the DFS code of each TCFG
	protected String[] graphArray;
	protected int maxGraphSize; // The maximum graph size of graphs in
								// graphArray
	// Edge index: Contains both Frequent & InFrequent Edges
	// Frequent Edges are used to index TCFG in graphArray
	// InFrequent Edges are used to index Graph in the Graph Database
	protected EdgeIndex edgeIndex;

	protected FGindexSearcher(String[] graphArray, int maxGraphSize,
			EdgeIndex edgeIndex) {
		this.graphArray = graphArray;
		this.maxGraphSize = maxGraphSize;
		this.edgeIndex = edgeIndex;
	}

	/**
	 * Get the TCFG feature of the query: count as query processing time
	 */
	@Override
	public int designedSubgraph(Graph query, boolean[] exactMatch,
			long[] TimeComponent) {
		// First step: find the set of distinct edges in g
		long start = System.currentTimeMillis();
		Map<IN_FGindexEdge, Integer> gEdges = edgeIndex.getAllEdges(query);
		int sizeG = query.getEdgeCount();
		FastSU fastSu = new FastSU();
		for (int graphSize = sizeG; graphSize <= this.maxGraphSize; ++graphSize) {
			// For each edge e, find graphs that has size i, whose edge
			// frequency
			// is larger than count(e, g), then do intersection
			OrderedIntSet candidateGraphI = new OrderedIntSet();
			boolean firstTime = true;
			Set<Entry<IN_FGindexEdge, Integer>> gEdgesSet = gEdges.entrySet();
			for (Iterator<Entry<IN_FGindexEdge, Integer>> it = gEdgesSet
					.iterator(); it.hasNext();) {
				Entry<IN_FGindexEdge, Integer> currentGEntry = it.next();
				IN_FGindexEdgeEntries repository = this.edgeIndex
						.get(currentGEntry.getKey());
				if (!repository.isFrequent())
					continue; // Non frequent edges are not count
				int minFrequency = currentGEntry.getValue();
				int[] graphswithMinimumFreq = repository
						.getGraphsWithMinimumFreq(graphSize, minFrequency);
				if (graphswithMinimumFreq == null)
					break;
				if (firstTime) {
					candidateGraphI.add(graphswithMinimumFreq);
					if (candidateGraphI.size() == 0)
						break;
					firstTime = false;
				} else
					candidateGraphI.join(graphswithMinimumFreq);
			}
			if (candidateGraphI.size() == 0)
				continue;
			// Test each graphs in this candidateGraphI set
			int[] candidateGraphIArray = candidateGraphI.getItems();
			for (int t = 0; t < candidateGraphIArray.length; t++) {
				Graph featureGraph = MyFactory.getDFSCoder().parse(
						this.graphArray[candidateGraphIArray[t]],
						MyFactory.getGraphFactory());
				if (fastSu.isIsomorphic(query, featureGraph)) {
					if (query.getEdgeCount() == featureGraph.getEdgeCount())
						exactMatch[0] = true;
					else
						exactMatch[0] = false;
					TimeComponent[2] += System.currentTimeMillis() - start;
					return candidateGraphIArray[t];
				}
			}
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		return -1;
	}

	// query processing time
	@Override
	public List<Integer> maxSubgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		List<Integer> graphIds = new ArrayList<Integer>();
		// First step find the set of distinct edges in g
		Map<IN_FGindexEdge, Integer> gEdges = edgeIndex.getAllEdges(query);
		int sizeG = query.getEdgeCount();
		FastSU fastSu = new FastSU();
		boolean success = false;
		for (int i = sizeG - 1; i > 0; i--) {
			// For each edge e, find graphs that has size i, whose edge
			// frequency
			// is larger than count(e, g), then do union operation
			OrderedIntSet candidateGraphI = new OrderedIntSet();
			Set<Entry<IN_FGindexEdge, Integer>> gEdgesSet = gEdges.entrySet();

			for (Iterator<Entry<IN_FGindexEdge, Integer>> it = gEdgesSet
					.iterator(); it.hasNext();) {
				Entry<IN_FGindexEdge, Integer> currentGEntry = it.next();
				IN_FGindexEdgeEntries repository = this.edgeIndex
						.get(currentGEntry.getKey());
				// Only takes frequent edges into consideration
				if (repository.isFrequent()) {
					int[] graphswithMaxFreq = repository
							.getGraphsWithMaximumFreq(i,
									currentGEntry.getValue());
					if (graphswithMaxFreq == null)
						continue;
					else
						candidateGraphI.add(graphswithMaxFreq);
				}
			}
			// Test each graphs in this candidateGraphI set in descending order
			int[] candidateGraphIArray = candidateGraphI.getItems();
			for (int t = candidateGraphIArray.length - 1; t >= 0; t--) {
				int graphId = candidateGraphIArray[t];
				// Test whether graphArray[graphId]'s any edge is contained in
				// gEdges
				Graph featureGraph = MyFactory.getDFSCoder().parse(
						graphArray[graphId], MyFactory.getGraphFactory());
				Map<IN_FGindexEdge, Integer> willRemove = edgeIndex
						.getAllEdges(featureGraph);
				;
				Set<Entry<IN_FGindexEdge, Integer>> willRmovedEntries = willRemove
						.entrySet();
				boolean contains = true;
				for (Iterator<Entry<IN_FGindexEdge, Integer>> rit = willRmovedEntries
						.iterator(); rit.hasNext();) {
					Entry<IN_FGindexEdge, Integer> currentEntry = rit.next();
					IN_FGindexEdge currentEdge = currentEntry.getKey();
					if (gEdges.containsKey(currentEdge)) {
						if (gEdges.get(currentEdge) < currentEntry.getValue()) {
							contains = false;
							break;
						}
					} else {
						contains = false;
						break;
					}
				}
				// If graphArray[graphId] is subgraph isomorphic to g
				if (contains && fastSu.isIsomorphic(featureGraph, query)) {
					graphIds.add(graphId);
					// Remove all edges that are in this graph
					Set<IN_FGindexEdge> willRemoveEdges = willRemove.keySet();
					for (Iterator<IN_FGindexEdge> rit = willRemoveEdges
							.iterator(); rit.hasNext();)
						gEdges.remove(rit.next());
					if (gEdges.isEmpty()) {
						success = true;
						break;
					}
				}
			}
			if (success)
				break;
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		return graphIds;
	}

	@Override
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		List<Integer> graphIds = new ArrayList<Integer>();
		// First step find the set of distinct edges in g
		Map<IN_FGindexEdge, Integer> gEdges = edgeIndex.getAllEdges(query);
		int sizeG = query.getEdgeCount();
		FastSU fastSu = new FastSU();
		for (int i = sizeG - 1; i > 0; i--) {
			// For each edge e, find graphs that has size i, whose edge
			// frequency
			// is larger than count(e, g), then do union operation
			OrderedIntSet candidateGraphI = new OrderedIntSet();
			Set<Entry<IN_FGindexEdge, Integer>> gEdgesSet = gEdges.entrySet();

			for (Iterator<Entry<IN_FGindexEdge, Integer>> it = gEdgesSet
					.iterator(); it.hasNext();) {
				Entry<IN_FGindexEdge, Integer> currentGEntry = it.next();
				IN_FGindexEdgeEntries repository = this.edgeIndex
						.get(currentGEntry.getKey());
				// Only takes frequent edges into consideration
				if (repository != null && repository.isFrequent()) {
					int[] graphswithMaxFreq = repository
							.getGraphsWithMaximumFreq(i,
									currentGEntry.getValue());
					if (graphswithMaxFreq == null)
						continue;
					else
						candidateGraphI.add(graphswithMaxFreq);
				}
			}
			// Test each graphs in this candidateGraphI set in descending order
			int[] candidateGraphIArray = candidateGraphI.getItems();
			for (int t = candidateGraphIArray.length - 1; t >= 0; t--) {
				int graphId = candidateGraphIArray[t];
				// Test whether graphArray[graphId]'s any edge is contained in
				// gEdges
				Graph featureGraph = MyFactory.getDFSCoder().parse(
						graphArray[graphId], MyFactory.getGraphFactory());
				// Edge Test First
				Map<IN_FGindexEdge, Integer> willRemove = edgeIndex
						.getAllEdges(featureGraph);
				;
				Set<Entry<IN_FGindexEdge, Integer>> willRmovedEntries = willRemove
						.entrySet();
				boolean contains = true;
				for (Iterator<Entry<IN_FGindexEdge, Integer>> rit = willRmovedEntries
						.iterator(); rit.hasNext();) {
					Entry<IN_FGindexEdge, Integer> currentEntry = rit.next();
					IN_FGindexEdge currentEdge = currentEntry.getKey();
					if (gEdges.containsKey(currentEdge)) {
						if (gEdges.get(currentEdge) < currentEntry.getValue()) {
							contains = false;
							break;
						}
					} else {
						contains = false;
						break;
					}
				}
				// If graphArray[graphId] is subgraph isomorphic to g
				if (contains && fastSu.isIsomorphic(featureGraph, query)) {
					graphIds.add(graphId);
				}
			}
		}
		TimeComponent[2] = System.currentTimeMillis() - start;
		return graphIds;

	}

	public EdgeIndex getEdgeIndex() {
		return this.edgeIndex;
	}

	public int getFeatureCount() {
		return this.graphArray.length;
	}

	@Override
	public int[] getAllFeatureIDs() {
		// TODO Not Implemented
		return null;
	}
}
