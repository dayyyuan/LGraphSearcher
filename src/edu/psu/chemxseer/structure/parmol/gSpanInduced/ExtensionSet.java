/*
 * Created on Jan 1, 2005
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
 * Dayu: This file is brought here only because it is only visible in the package
 * 
 */
package edu.psu.chemxseer.structure.parmol.gSpanInduced;

import java.util.*;

/**
 * This class is a kind of sorted set for extensions (used by min DFSCode
 * search)
 * 
 * @author Marc Woerlein <marc.woerlein@gmx.de>
 */
public class ExtensionSet {
	TreeMap map = new TreeMap();

	/** creats a new empty ExtensionSet */
	public ExtensionSet() {
	}

	/**
	 * add the given Extension to the set
	 * 
	 * @param e
	 * @return <code> true </code>, if e is added correctly
	 */
	public boolean add(Object e) {
		ArrayList s = (ArrayList) map.get(e);
		if (s == null)
			map.put(e, s = new ArrayList());
		return s.add(e);
	}

	/**
	 * add all Embeddings inside the given Collection
	 * 
	 * @param c
	 * @return <code> true </code>, if all Embeddings are added correctly
	 */
	public boolean addAll(Collection c) {
		boolean ret = true;
		for (Iterator it = c.iterator(); it.hasNext();) {
			ret = ret && add(it.next());
		}
		return ret;
	}

	/**
	 * remove the given Extension to the set
	 * 
	 * @param e
	 * @return <code> true </code>, if e is removed correctly
	 */
	public boolean remove(Object e) {
		ArrayList s = (ArrayList) map.get(e);
		if (s == null)
			return false;
		boolean ret = s.remove(e);
		if (s.isEmpty())
			map.remove(e);
		return ret;
	}

	/**
	 * remove all Embeddings inside the given Collection
	 * 
	 * @param c
	 * @return <code> true </code>, if all Embeddings are removed correctly
	 */
	public boolean removeAll(Collection c) {
		boolean ret = true;
		for (Iterator it = c.iterator(); it.hasNext();) {
			ret = ret && remove(it.next());
		}
		return ret;
	}

	/**
	 * @return a list of the minimal Extensions (only similiar Extensions)
	 */
	public ArrayList headList() {
		if (map.isEmpty())
			return new ArrayList();
		ArrayList ret = (ArrayList) map.get(map.firstKey());
		return ret;
	}

}
