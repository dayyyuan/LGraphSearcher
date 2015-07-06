package edu.psu.chemxseer.structure.setcover.newExps;

import moa.classifiers.core.driftdetection.ADWIN;

public class ADWINTracer {

	public enum MonitorType {
		candidates, fp, test
	};

	private ADWIN adwin;
	private MonitorType type;
	private int preWindowSize;

	public ADWINTracer(double confidence, MonitorType type) {
		this.adwin = new ADWIN(confidence);
		this.type = type;
		this.preWindowSize = 0;
	}

	/**
	 * Insert the value of running the experiments: First four entry is the
	 * running-time Fifth number if the candidates count Sixth number if the
	 * answer count
	 * 
	 * @param values
	 */
	public void insertValue(float[] values) {
		if (this.type == MonitorType.candidates) {
			this.adwin.setInput(values[4]);
			this.preWindowSize++;
		} else if (this.type == MonitorType.fp) {
			this.adwin.setInput(values[4] / values[5]);
			this.preWindowSize++;
		} else if (this.type == MonitorType.test) {
			this.adwin.setInput(values[0]);
			this.preWindowSize++;
		} else
			throw new UnsupportedOperationException();
	}

	public boolean needToUpdate() {
		if (preWindowSize == this.adwin.getWidth())
			return false;
		else if (preWindowSize - adwin.getWidth() < 0.2 * preWindowSize) { // the
																			// deletion
																			// if
																			// less
																			// than
			shrinkWindowSize();
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Shrink the window size by discarding data before the shift And return the
	 * new size of the window
	 * 
	 * @return
	 */
	public int shrinkWindowSize() {
		this.preWindowSize = adwin.getWidth();
		return preWindowSize;
	}

	public int getLength() {
		return preWindowSize;
	}
}
