package edu.psu.chemxseer.structure.query;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

public class TestQuery implements Comparable<TestQuery> {
	private Graph g;
	private int scores;
	private int frequency;

	public TestQuery(Graph g, int scores, int freq) {
		this.g = g;
		this.scores = scores;
		this.frequency = freq;
	}

	public TestQuery(String aline) {
		String[] tokens = aline.split(",");
		this.g = MyFactory.getDFSCoder().parse(tokens[0],
				MyFactory.getGraphFactory());
		this.scores = Integer.parseInt(tokens[2]);
		this.frequency = Integer.parseInt(tokens[1]);
	}

	public Graph getG() {
		return g;
	}

	public void setG(Graph g) {
		this.g = g;
	}

	public int getScores() {
		return scores;
	}

	public void setScores(int scores) {
		this.scores = scores;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		String gString = MyFactory.getDFSCoder().serialize(g);
		String results = gString + "," + frequency + "," + scores;
		return results;
	}

	@Override
	public int compareTo(TestQuery other) {
		int score1 = this.getScores();
		int score2 = other.getScores();
		if (score1 < score2)
			return -1;
		else if (score1 == score2)
			return 0;
		else
			return 1;
	}
}
