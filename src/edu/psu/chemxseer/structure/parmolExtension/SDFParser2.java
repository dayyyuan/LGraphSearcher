package edu.psu.chemxseer.structure.parmolExtension;

/*
 * Created on 05.06.2005
 * 
 * Copyright 2005 Thorsten Meinl
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import de.parmol.Util;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.graph.MutableGraph;
import de.parmol.parsers.GraphParser;
import de.parmol.parsers.SLNParser;

/**
 * This parser reads the 2D structure of molecules in MDL's SDF format.
 * 
 * @author Thorsten Meinl <Thorsten.Meinl@informatik.uni-erlangen.de>
 */
public class SDFParser2 implements GraphParser {
	private boolean m_ignoreHydrogens = true;

	/** A public instance of the SDFParser */
	public final static SDFParser2 instance = new SDFParser2();
	private final static DateFormat DATE_FORMATTER = new SimpleDateFormat(
			"MMddyyHHmm");

	/**
	 * 
	 */
	public SDFParser2() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.parsers.GraphParser#parse(java.lang.String,
	 * de.parmol.graph.GraphFactory)
	 */

	public Graph parse(RandomAccessFile in, GraphFactory factory)
			throws ParseException, ParseException, IOException {
		final String moleculeName = in.readLine();
		if (moleculeName == null)
			return null;
		MutableGraph g = factory.createGraph(moleculeName);

		final String infoLine = in.readLine();
		in.readLine(); // skip the empty line

		String countsLine = in.readLine();
		final int atomCount = Integer.parseInt(countsLine.substring(0, 3)
				.trim());
		final int bondCount = Integer.parseInt(countsLine.substring(3, 6)
				.trim());
		final int atomLists = Integer.parseInt(countsLine.substring(7, 9)
				.trim());
		final boolean chiralFlag = countsLine.charAt(14) == '1';
		final int stextEntry = Integer.parseInt(countsLine.substring(15, 18)
				.trim());

		int[] atomIndices = parseAtomBlock(in, g, atomCount);
		parseBondBlock(in, g, atomIndices, bondCount);

		parseDataBlock(in);

		return g;
	}

	// Read "num" of graphs starting from the current in
	public Graph[] parse(RandomAccessFile in, GraphFactory factory, int num)
			throws ParseException, ParseException, IOException {
		Graph[] results = new Graph[num];
		for (int i = 0; i < num; i++) {
			results[i] = this.parse(in, factory);
			if (results[i] == null)
				System.out.println("Exception in SDFParser2");
		}
		return results;
	}

	private void parseDataBlock(RandomAccessFile in) throws IOException {
		// just skip everything until "$$$$"
		String line;
		while ((line = in.readLine()) != null && (!line.equals("$$$$"))) {
		}
	}

	private int[] parseAtomBlock(RandomAccessFile in, MutableGraph g,
			int atomCount) throws IOException, ParseException {
		final int[] atomIndices = new int[atomCount];

		for (int i = 0; i < atomCount; i++) {
			final String line = in.readLine();

			String temp = line.substring(0, 10).trim();
			if (temp.startsWith(".")) {
				temp = "0" + temp;
			} else if (temp.startsWith("-.")) {
				temp = "-0" + temp.substring(1);
			}

			final double xCoord = Double.parseDouble(temp);

			temp = line.substring(10, 20).trim();
			if (temp.startsWith(".")) {
				temp = "0" + temp;
			} else if (temp.startsWith("-.")) {
				temp = "-0" + temp.substring(1);
			}
			final double yCoord = Double.parseDouble(temp);

			temp = line.substring(20, 30).trim();
			if (temp.startsWith(".")) {
				temp = "0" + temp;
			} else if (temp.startsWith("-.")) {
				temp = "-0" + temp.substring(1);
			}
			final double zCoord = Double.parseDouble(temp);

			final String atomLabel = line.substring(31, 34).trim();
			final int massDifference = Integer.parseInt(line.substring(34, 36)
					.trim());

			// some SDF files, especially some from the NCI, contain an
			// additional space
			// between the mass difference and the charge. This violates the
			// specification.
			// Since currently we do not need the charge, we just skip it here
			// final int charge = Integer.parseInt(line.substring(36,
			// 39).trim());
			// final int valences = Integer.parseInt(line.substring(54, 57));

			if (!(m_ignoreHydrogens && atomLabel.equals("H"))) {
				atomIndices[i] = g.addNode(atomSymbol(atomLabel));
			} else {
				atomIndices[i] = Graph.NO_NODE;
			}
		}

		return atomIndices;
	}

	private void parseBondBlock(RandomAccessFile in, MutableGraph g,
			int[] atoms, int bondCount) throws IOException {
		for (int i = 0; i < bondCount; i++) {
			final String line = in.readLine();

			final int indexA = Integer.parseInt(line.substring(0, 3).trim());
			final int indexB = Integer.parseInt(line.substring(3, 6).trim());
			final int bondLabel = Integer.parseInt(line.substring(6, 9).trim());

			if ((atoms[indexA - 1] != Graph.NO_NODE)
					&& (atoms[indexB - 1] != Graph.NO_NODE)) {
				g.addEdge(atoms[indexA - 1], atoms[indexB - 1], bondLabel);
			}
		}
	}

	private int atomSymbol(String label) throws ParseException {
		for (int i = 1; i < SLNParser.ATOM_SYMBOLS.length; i++) {
			if (label.equals(SLNParser.ATOM_SYMBOLS[i]))
				return i;
		}

		throw new ParseException("Unknown atom label: " + label, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.parsers.GraphParser#serialize(de.parmol.graph.Graph)
	 */
	@Override
	public String serialize(Graph g) {
		StringBuffer buf = new StringBuffer(1024);

		buf.append(g.getName() + "\n");
		buf.append("  ParMol  " + DATE_FORMATTER.format(new Date()) + "\n");
		buf.append("\n");
		Util.format(g.getNodeCount(), buf, 3, false);
		Util.format(g.getEdgeCount(), buf, 3, false);
		buf.append("  0  0  0  0  0  0  0  0  0 V2000\n");

		for (int i = 0; i < g.getNodeCount(); i++) {
			buf.append("    0.0000    0.0000    0.0000 ");
			buf.append(SLNParser.ATOM_SYMBOLS[g.getNodeLabel(i)]);
			for (int k = SLNParser.ATOM_SYMBOLS[g.getNodeLabel(i)].length(); k < 3; k++)
				buf.append(' ');
			buf.append(" 0  0\n");
		}

		for (int i = 0; i < g.getEdgeCount(); i++) {
			final int edge = g.getEdge(i);

			Util.format(g.getNodeIndex(g.getNodeA(edge) + 1), buf, 3, false);
			Util.format(g.getNodeIndex(g.getNodeB(edge) + 1), buf, 3, false);
			Util.format(g.getEdgeLabel(edge), buf, 3, false);
			buf.append('\n');
		}

		buf.append("M  END\n$$$$\n");
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.parsers.GraphParser#serialize(de.parmol.graph.Graph[],
	 * java.io.OutputStream)
	 */
	@Override
	public void serialize(Graph[] graphs, OutputStream out) throws IOException {
		BufferedOutputStream bout = new BufferedOutputStream(out);

		for (int i = 0; i < graphs.length; i++) {
			bout.write(serialize(graphs[i]).getBytes());
		}
		bout.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.parsers.GraphParser#getDesiredGraphFactoryProperties()
	 */
	@Override
	public int getDesiredGraphFactoryProperties() {
		return GraphFactory.UNDIRECTED_GRAPH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.parsers.GraphParser#directed()
	 */
	@Override
	public boolean directed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parmol.parsers.GraphParser#getNodeLabel(int)
	 */
	@Override
	public String getNodeLabel(int nodeLabel) {
		return SLNParser.ATOM_SYMBOLS[nodeLabel];
	}

	/**
	 * Sets the value of the ignore hydrogen atoms flag
	 * 
	 * @param newValue
	 *            the new flag value
	 */
	public void setIgnoreHydrogens(boolean newValue) {
		m_ignoreHydrogens = newValue;
	}

	/**
	 * Return the current value of the ignore hydrogen atoms flag
	 * 
	 * @return the flag value
	 */
	public boolean getIgnoreHydrogens() {
		return m_ignoreHydrogens;
	}

	@Override
	public Graph parse(String text, GraphFactory factory) throws ParseException {
		System.out.println("DO Nothing in SDFParser2::parse");
		return null;
	}

	@Override
	public Graph[] parse(InputStream in, GraphFactory factory)
			throws IOException, ParseException {
		// TODO Auto-generated method stub
		System.out.println("DO Nothing in SDFParser2::parse");
		return null;
	}
}