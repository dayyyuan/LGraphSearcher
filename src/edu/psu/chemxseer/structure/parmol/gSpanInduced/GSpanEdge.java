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
 * Edited by Dayu Yuan
 * This file ware included into chemxseer gSpan only because the datamember
 * nodeA, nodeB, labels are visible only in package
 * 
 */

package edu.psu.chemxseer.structure.parmol.gSpanInduced;

/**
 * This class representates an edge in the DFSCode of GSpan
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class GSpanEdge implements Comparable {
	public int nodeA;
	public int nodeB;
	public int labelA;
	public int labelB, edgeLabel;
	public GSpanEdge prev = null; // the previous and next edge in the DFSCode
	public GSpanEdge next = null;

	/**
	 * creates a new edge tuple for an initial edge
	 * 
	 * @param labelA
	 * @param edgeLabel
	 * @param labelB
	 */
	public GSpanEdge(int labelA, int edgeLabel, int labelB) {
		this.nodeA = 0;
		this.nodeB = 1;
		this.edgeLabel = edgeLabel;
		if (labelA < labelB) {
			this.labelA = labelA;
			this.labelB = labelB;
		} else {
			this.labelA = labelB;
			this.labelB = labelA;
		}
	}

	/**
	 * creates a new edge tuple
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param labelA
	 * @param edgeLabel
	 * @param labelB
	 */
	public GSpanEdge(int nodeA, int nodeB, int labelA, int edgeLabel, int labelB) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.labelA = labelA;
		this.labelB = labelB;
		this.edgeLabel = edgeLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		GSpanEdge other = (GSpanEdge) o;
		if (this.nodeA == other.nodeA) {
			if (this.nodeB != other.nodeB)
				return this.nodeB - other.nodeB;
			if (this.labelA != other.labelA)
				return this.labelA - other.labelA;
			if (this.edgeLabel != other.edgeLabel)
				return this.edgeLabel - other.edgeLabel;
			return this.labelB - other.labelB;
		} else {
			if (this.nodeA < this.nodeB) { // this is forward edge
				if (this.nodeB == other.nodeA) {
					return -1; // see paper
				} else {
					if (other.nodeA > this.nodeA) {
						if (other.nodeA > this.nodeB)
							return -1;
						else
							return 1;
					} else {
						if (this.nodeA >= other.nodeB)
							return 1;
						else
							return -1;
					}
				}
			} else if (other.nodeA < other.nodeB) { // other is forward edge
				if (other.nodeB == this.nodeA) {
					return 1; // see paper
				} else {
					if (other.nodeA > this.nodeA) {
						if (other.nodeA >= this.nodeB)
							return -1;
						else
							return 1;
					} else {
						if (this.nodeA > other.nodeB)
							return 1;
						else
							return -1;
					}
				}
			} else { // compare two backwards edges with different nodeA
				return this.nodeA - other.nodeA;
			}
		}
	}

	@Override
	public String toString() {
		return nodeA + " " + nodeB + ": " + labelA + " " + edgeLabel + " "
				+ labelB;
	}

}
