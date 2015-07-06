package edu.psu.chemxseer.structure.subsearch.TreeDelta;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.iso.FastSU;

public class GraphWrapper {
	private Graph g;
	private List<GraphWrapper> parents;
	private List<GraphWrapper> children;
	private boolean visited;

	public GraphWrapper(Graph oneSub) {
		this.g = oneSub;
		this.parents = new ArrayList<GraphWrapper>();
		this.children = new ArrayList<GraphWrapper>();
		this.visited = false;
	}

	public boolean isVisited() {
		return this.visited;
	}

	public void setVisited() {
		this.visited = true;
	}

	public void setUnvisited() {
		this.visited = false;
	}

	public Graph getG() {
		return g;
	}

	public void setG(Graph g) {
		this.g = g;
	}

	public List<GraphWrapper> getParents() {
		return parents;
	}

	public void setParents(List<GraphWrapper> parents) {
		this.parents = parents;
	}

	public List<GraphWrapper> getChildren() {
		return children;
	}

	public void setChildren(List<GraphWrapper> children) {
		this.children = children;
	}

	public void addChild(GraphWrapper child) {
		this.children.add(child);
	}

	public void addParent(GraphWrapper parent) {
		this.parents.add(parent);
	}

	/**
	 * Given a set of candidate features, find subgraph- super graph
	 * relationships among these candidate features
	 * 
	 * @param candidateFeatures
	 * @return
	 * @throws ParseException
	 */
	public static void mineSubSuperRelation(List<GraphWrapper> allGraphs) {
		// First sort the candidateFeatures
		GraphWrapperComparator comp = new GraphWrapperComparator();
		Collections.sort(allGraphs, comp);
		// Second: find the containment relationship
		FastSU fastSu = new FastSU();
		HashSet<GraphWrapper> offsprings = new HashSet<GraphWrapper>();
		for (int i = allGraphs.size() - 1; i >= 0; i--) {
			// Initialize offsprings, children
			offsprings.clear();
			for (int j = i + 1; j < allGraphs.size(); j++) {
				if (offsprings.contains(allGraphs.get(j)))
					continue;
				// Graphi < Graphj
				// Test whether Graphj is a subgraph os Graphi
				boolean isSub = fastSu.isIsomorphic(allGraphs.get(i).getG(),
						allGraphs.get(j).getG());
				if (isSub) {
					allGraphs.get(i).addChild(allGraphs.get(j));
					allGraphs.get(j).addParent(allGraphs.get(i));
					// add j and all j's children into offsprings of i
					offsprings.add(allGraphs.get(j));
					addOffspring(offsprings, allGraphs.get(j));
				} else
					continue;
			}
		}
	}

	/**
	 * Add all terms that descent to term into offsprings
	 * 
	 * @param offsprings
	 * @param term
	 */
	private static void addOffspring(Collection<GraphWrapper> offsprings,
			GraphWrapper term) {
		Queue<GraphWrapper> queue = new LinkedList<GraphWrapper>();
		queue.offer(term);
		while (!queue.isEmpty()) {
			GraphWrapper oneFeature = queue.poll();
			List<GraphWrapper> children = oneFeature.getChildren();
			if (children == null || children.size() == 0)
				continue;
			else {
				for (int i = 0; i < children.size(); i++) {
					if (offsprings.contains(children.get(i)))
						continue;
					else {
						offsprings.add(children.get(i));
						queue.offer(children.get(i));
					}
				}
			}
		}
	}
}
