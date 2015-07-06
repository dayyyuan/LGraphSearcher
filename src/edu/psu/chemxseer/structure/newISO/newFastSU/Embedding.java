package edu.psu.chemxseer.structure.newISO.newFastSU;

import java.util.Comparator;

import edu.psu.chemxseer.structure.util.HasPos;

public class Embedding implements HasPos {
	protected int[] map; // the mapping from the small graph to the big graph
	protected int embID;
	protected Pattern subgraph;
	protected boolean visited; // denote whether this embedding has been visited
								// or not
	protected float score;
	protected int priorityQueueID;
	protected static EmbeddingComparator comp;

	public static EmbeddingComparator getComparator() {
		if (comp == null)
			comp = new EmbeddingComparator();
		return comp;
	}

	private Embedding(int[] map, int embID, Pattern subgraph) {
		this.map = map;
		this.embID = embID;
		this.subgraph = subgraph;
		this.visited = false;
		this.score = subgraph.getScore();
		this.priorityQueueID = -1;
	}

	public Embedding[] getInstances(int[][] mappings, Pattern pattern) {
		this.subgraph = pattern;
		Embedding[] results = new Embedding[mappings.length];
		for (int i = 0; i < results.length; i++)
			results[i] = new Embedding(mappings[i], i, pattern);
		return results;
	}

	/**
	 * @return the map
	 */
	public int[] getMap() {
		return map;
	}

	/**
	 * @return the embID
	 */
	public int getEmbID() {
		return embID;
	}

	/**
	 * @return the subgraph
	 */
	public Pattern getSubgraph() {
		return subgraph;
	}

	/**
	 * @param map
	 *            the map to set
	 */
	public void setMap(int[] map) {
		this.map = map;
	}

	/**
	 * @param embID
	 *            the embID to set
	 */
	public void setEmbID(int embID) {
		this.embID = embID;
	}

	/**
	 * @param subgraph
	 *            the subgraph to set
	 */
	public void setSubgraph(Pattern subgraph) {
		this.subgraph = subgraph;
	}

	/**
	 * @return the visited
	 */
	public boolean isVisited() {
		return visited;
	}

	/**
	 * @param visited
	 *            the visited to set
	 */
	public void setVisited() {
		this.visited = true;
	}

	public void udpateScore() {
		this.score = this.subgraph.getScore();
	}

	@Override
	public int getPos() {
		return this.priorityQueueID;
	}

	@Override
	public void setPos(int id) {
		this.priorityQueueID = id;
	}

	public int getPatternID() {
		return this.subgraph.getfID();
	}
}

class EmbeddingComparator implements Comparator<Embedding> {
	@Override
	public int compare(Embedding arg0, Embedding arg1) {
		if (arg0.score < arg1.score)
			return -1;
		else if (arg0.score == arg1.score) {
			if (arg0.map.length < arg1.map.length)
				return -1;
			else if (arg0.map.length == arg1.map.length)
				return 0;
			else
				return 1;
		} else
			return 1;
	}
}
