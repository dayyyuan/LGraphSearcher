package edu.psu.chemxseer.structure.supersearch.experiments;

import java.util.Random;

public class GaussianGenerator {
	private int mean;
	private int deviation;
	private Random rd;

	public GaussianGenerator(int mean, int deviation) {
		this.mean = mean;
		this.deviation = deviation;
		rd = new Random();
	}

	public double getNext() {
		double result = rd.nextGaussian();
		return result * deviation + mean;
	}

}
