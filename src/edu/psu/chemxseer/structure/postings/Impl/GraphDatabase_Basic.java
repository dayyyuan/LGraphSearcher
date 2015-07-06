package edu.psu.chemxseer.structure.postings.Impl;

import java.text.ParseException;
import java.util.Iterator;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

public abstract class GraphDatabase_Basic implements IGraphDatabase {

	protected GraphParser gParser;

	public GraphDatabase_Basic(GraphParser gParser) {
		this.gParser = gParser;
	}

	/**
	 * Excluding end, incuding start
	 * 
	 * @param graphIDs
	 * @param start
	 * @param end
	 * @return
	 */
	@Override
	public Graph[] loadGraphs(int[] graphIDs, int start, int end) {
		if (start < 0 || end > graphIDs.length)
			return null;
		Graph[] results = new Graph[end - start];
		for (int i = 0; i < end - start; i++) {
			String graphString = this.findGraphString(graphIDs[i + start]);
			try {
				results[i] = gParser.parse(graphString,
						MyFactory.getGraphFactory());

			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (results[i] == null)
				System.out.println("Excpetion in loadGraphs: graphDB");
		}
		return results;
	}

	@Override
	public Graph[] loadAllGraphs() {
		Graph[] graphs = new Graph[this.getTotalNum()];
		for (int i = 0; i < graphs.length; i++) {
			try {
				graphs[i] = gParser.parse(findGraphString(i),
						MyFactory.getGraphFactory());
			} catch (ParseException e) {
				System.out.println("Error in Load All Graphs");
				e.printStackTrace();
				return null;
			}
		}
		return graphs;
	}

	/**
	 * get graphs from the graph database with ID starting from startNum end
	 * with endNum, including the first one but not the last one
	 * 
	 * @param startNum
	 * @param endNum
	 * @return
	 */
	@Override
	public Graph[] loadGraphs(int start, int end) {
		if (start < 0 || end > this.getTotalNum())
			return null;
		Graph[] results = new Graph[end - start];
		for (int i = 0; i < end - start; i++) {
			String graphString = this.findGraphString(i + start);
			try {
				results[i] = gParser.parse(graphString,
						MyFactory.getGraphFactory());

			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (results[i] == null)
				System.out.println("Excpetion in loadGraphs: graphDB");
		}
		return results;

	}

	/**
	 * Given the graph ID, load the Smiles (String) from of this graph from the
	 * graph database file
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Graph findGraph(int id) {
		String gString = this.findGraphString(id);
		if (gString == null)
			return null;
		else {
			try {
				return gParser.parse(gString, MyFactory.getGraphFactory());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public GraphParser getParser() {
		return gParser;
	}

	@Override
	public abstract String findGraphString(int id);

	@Override
	public abstract int getTotalNum();

	@Override
	public Iterator<Graph> iterator() {
		return new dbIterator();
	}

	private class dbIterator implements Iterator<Graph> {
		private int index = 0;

		public dbIterator() {
			index = 0;
		}

		@Override
		public boolean hasNext() {
			if (index < getTotalNum())
				return true;
			else
				return false;
		}

		@Override
		public Graph next() {
			return findGraph(index++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
}
