/*
 * Created on Feb 14, 2005
 * 
 * Copyright 2005 Marc Wörlein
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

import java.util.HashSet;
import de.parmol.graph.*;

/**
 * This is a specialized HashSet for storing Classified/GSpan-Graphs, which also
 * accumulates their frequencies for further use
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class GraphSet extends HashSet {
	static final long serialVersionUID = 12345; // was will Java denn da stehen
												// haben?
	/** the length of the frequencies Array */
	public static int length;
	private float[] freq;

	/** creates a new GraphSet */
	public GraphSet() {
		freq = new float[GraphSet.length];
	}

	/**
	 * add the given ClassifiedGraph
	 * 
	 * @param g
	 */
	public void add(ClassifiedGraph g) {
		if (super.add(g)) {
			float[] nf = g.getClassFrequencies();
			for (int i = 0; i < GraphSet.length; i++)
				freq[i] += nf[i];
		}
	}

	/**
	 * add the given GSpanGraph
	 * 
	 * @param g
	 */
	public void add(GSpanGraph g) {
		if (super.add(g)) {
			float[] nf = g.getRealGraph().getClassFrequencies();
			for (int i = 0; i < GraphSet.length; i++)
				freq[i] += nf[i];
		}
	}

	/** @return the frequencies of the current set */
	public final float[] getFreq() {
		return freq;
	};

	/**
	 * @param minFreq
	 * @return true, if current support not smaller then minFreq
	 */
	public boolean isFrequent(float[] minFreq) {
		for (int i = 0; i < length; i++)
			if (freq[i] < minFreq[i])
				return false;
		return true;
	}

}
