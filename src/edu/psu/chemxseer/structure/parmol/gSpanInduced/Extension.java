/*
 * Created on Dec 30, 2004
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
 *  
 *  Dayu: This file is brough in here only because this class is only visible in
 *  the package
 */
package edu.psu.chemxseer.structure.parmol.gSpanInduced;

import de.parmol.graph.*;

/**
 * This class representates an extension used by the min DFSCode search
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class Extension implements Comparable {

	private int fromNode, fromLabel, edgeLabel, toNode, nodeLabel, edge;

	private Extension(int fromNode, int fromLabel, int edgeLabel, int toNode,
			int nodeLabel, int edge) {
		this.fromNode = fromNode;
		this.fromLabel = fromLabel;
		this.edgeLabel = edgeLabel;
		this.nodeLabel = nodeLabel;
		this.toNode = toNode;
		this.edge = edge;
	}

	/**
	 * @param gs
	 * @param g
	 * @param node
	 * @param edge
	 * @param fromNode
	 *            the corresponding node in the subgraph
	 * @return a new node extension created out of the given Graph g , using the
	 *         given node and edge as origin
	 */
	public static Extension newNodeExtension(DataBaseModified gs, Graph g,
			int node, int edge, int fromNode) {
		return new Extension(fromNode, gs.getNodeLabel(g.getNodeLabel(node)),
				gs.getEdgeLabel(g.getEdgeLabel(edge)), Graph.NO_NODE,
				gs.getNodeLabel(g.getNodeLabel(g.getOtherNode(edge, node))),
				edge);
	}

	/**
	 * @param gs
	 * @param g
	 * @param node
	 * @param edge
	 * @param fromNode
	 *            the corresponding node in the subgraph
	 * @param toNode
	 *            the corresponding node in the subgraph
	 * @return a new edge extension created out of the given Graph g, using the
	 *         given node and edge
	 */
	public static Extension newEdgeExtension(DataBaseModified gs, Graph g,
			int node, int edge, int fromNode, int toNode) {
		return new Extension(fromNode, gs.getNodeLabel(g.getNodeLabel(node)),
				gs.getEdgeLabel(g.getEdgeLabel(edge)), toNode,
				gs.getNodeLabel(g.getNodeLabel(g.getOtherNode(edge, node))),
				edge);
	}

	/** @return the corresponding start node in the subgraph */
	public int getFromNode() {
		return fromNode;
	}

	/**
	 * sets the corresponding end node in the subgraph
	 * 
	 * @param toNode
	 */
	public void setToNode(int toNode) {
		this.toNode = toNode;
	}

	/** @return the edge of the database Graphe */
	public int getEdge() {
		return edge;
	}

	/** @return <code> true </code> if this Extension is a node extension */
	public boolean isNodeExtension() {
		return toNode == Graph.NO_NODE;
	}

	/**
	 * @param newNode
	 *            the toNode name for node extensions (else use Graph.NO_NODE)
	 * @return the corresponding GSpanEdge
	 */
	public GSpanEdge getGSpanEdge(int newNode) {
		if (newNode != Graph.NO_NODE)
			return new GSpanEdge(fromNode, newNode, fromLabel, edgeLabel,
					nodeLabel);
		return new GSpanEdge(fromNode, toNode, fromLabel, edgeLabel, nodeLabel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		Extension other = (Extension) o;
		if (this.toNode == Graph.NO_NODE && other.toNode != Graph.NO_NODE)
			return 1;
		if (other.toNode == Graph.NO_NODE && this.toNode != Graph.NO_NODE)
			return -1;

		if (this.toNode == Graph.NO_NODE) { // both are node Extensions
			// greater Node has earlier node Extension
			if (this.fromNode != other.fromNode)
				return other.fromNode - this.fromNode;
			// same Node, sorted by labels
			if (this.edgeLabel != other.edgeLabel)
				return this.edgeLabel - other.edgeLabel;
			return this.nodeLabel - other.nodeLabel;
		} else { // both are edge Extensions
			// greater Node has later node Extension (should never be needed for
			// dfs)
			if (this.fromNode != other.fromNode)
				return this.fromNode - other.fromNode;
			// same Node, sorted by toNode and then by label
			if (this.toNode != other.toNode)
				return this.toNode - other.toNode;
			return this.edgeLabel - other.edgeLabel;
		}
	}

}
