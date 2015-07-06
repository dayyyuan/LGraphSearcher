/*
 * Created on Dec 17, 2004
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

import de.parmol.GSpan.Relabler;
import de.parmol.graph.*;
import java.util.*;

/**
 * This class representates a relabled shrunkable Graph for the GSpan algorithm
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class GSpanGraph {
	private ClassifiedGraph realGraph;
	public MutableGraph me;

	/**
	 * creates a new GSpanGraph, which representates the given realGraph
	 * 
	 * @param realGraph
	 * @param node
	 * @param edge
	 * @param factory
	 */
	public GSpanGraph(ClassifiedGraph realGraph, Relabler node, Relabler edge,
			GraphFactory factory) {
		this.realGraph = realGraph;
		me = factory.createGraph();

		int[] nodes = new int[realGraph.getNodeCount()];
		for (int i = 0; i < nodes.length; i++) {
			int lab = realGraph.getNodeLabel(realGraph.getNode(i));
			if (node.getLabel(lab) != Graph.NO_NODE) {
				nodes[i] = me.addNode(node.getLabel(lab));
			} else {
				nodes[i] = Graph.NO_NODE;
			}
		}
		for (int i = 0; i < realGraph.getEdgeCount(); i++) {
			int e = realGraph.getEdge(i);
			int lab = realGraph.getEdgeLabel(e);
			if (edge.getLabel(lab) != Graph.NO_EDGE
					&& nodes[realGraph.getNodeA(e)] != Graph.NO_NODE
					&& nodes[realGraph.getNodeB(e)] != Graph.NO_NODE) {
				me.addEdge(nodes[realGraph.getNodeA(e)],
						nodes[realGraph.getNodeB(e)], edge.getLabel(lab));
			}
		}
		// TODO: delete empty nodes
		/*
		 * for (int i=0;i<me.getNodeCount();i++){ if
		 * (me.getDegree(me.getNode(i))==0) me.removeNode(me.getNode(i)); }
		 */
	}

	/**
	 * @return an Iterator over all GSpanEdges in this GSpanGraph
	 */
	public Iterator edgeIterator() {
		return new Iterator() {
			int pos = 0;

			@Override
			public boolean hasNext() {
				return pos < me.getEdgeCount();
			}

			@Override
			public Object next() {
				if (hasNext()) {
					int edge = me.getEdge(pos++);
					return new GSpanEdge(me.getNodeLabel(me.getNodeA(edge)),
							me.getEdgeLabel(edge), me.getNodeLabel(me
									.getNodeB(edge)));
				} else
					throw new NoSuchElementException("No more elements");
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * remove all occurrences of the given edge from this GSpanGraph
	 * 
	 * @param edge
	 */
	public void remove(GSpanEdge edge) {
		boolean deleted = true;
		while (deleted) {
			deleted = false;
			for (int i = 0; i < me.getEdgeCount() && !deleted; i++) {
				int realEdge = me.getEdge(i);
				int nodeA = me.getNodeA(realEdge);
				int nodeB = me.getNodeB(realEdge);
				if ((me.getNodeLabel(nodeA) == edge.labelA
						&& me.getNodeLabel(nodeB) == edge.labelB && me
						.getEdgeLabel(realEdge) == edge.edgeLabel)
						|| (me.getNodeLabel(nodeA) == edge.labelB
								&& me.getNodeLabel(nodeB) == edge.labelA && me
								.getEdgeLabel(realEdge) == edge.edgeLabel)) {
					me.removeEdge(realEdge);
					deleted = true;
				}
			}
		}
	}

	/** @return the corresponding real graph */
	public ClassifiedGraph getRealGraph() {
		return realGraph;
	}

	/** @return the number of Edges, remaining in this GSpanGraph */
	public int getEdgeCount() {
		return me.getEdgeCount();
	}

}
