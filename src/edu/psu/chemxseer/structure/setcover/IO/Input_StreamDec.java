package edu.psu.chemxseer.structure.setcover.IO;

import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputStream;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;

public class Input_StreamDec implements IInputStream {

	private Input_Stream input;
	private float[] minimumSupport;
	private float[] maxinumSupport;
	private float[] currentSupport;

	private long enumeratedSetCount;
	private long patternEnumerationTime;

	/**
	 * Construct & REturn a Input_Stream
	 * 
	 * @param gen
	 * @param converter
	 * @param type
	 * @return
	 */
	public static Input_StreamDec newInstance(Input_Stream input) {
		float[] minimumSupport = input.patternGenerator.getSettings().minimumClassFrequencies
				.clone();
		float[] maximumSupport = new float[minimumSupport.length];
		for (int i = 0; i < minimumSupport.length; i++)
			if (minimumSupport[i] != 1)
				maximumSupport[i] = 9 * minimumSupport[i];
			else
				maximumSupport[i] = 1;
		return new Input_StreamDec(input, minimumSupport, maximumSupport);
	}

	private Input_StreamDec(Input_Stream input, float[] minimumSupport,
			float[] maximumSupport) {
		this.input = input;
		this.minimumSupport = minimumSupport;
		this.maxinumSupport = maximumSupport;
		this.currentSupport = new float[maximumSupport.length];
		for (int i = 0; i < currentSupport.length; i++)
			currentSupport[i] = maximumSupport[i];
	}

	@Override
	public void closeInputStream() throws Exception {
		this.input.closeInputStream();
	}

	/**
	 * Return the next CoverSet
	 * 
	 * @return
	 */
	public ISet nextSet() {
		ISet nextSet = input.nextSet();
		return nextSet;
	}

	public boolean decreaseMinSup(Iterator<ISet> it) {
		boolean change = false;
		for (int i = 0; i < this.currentSupport.length; i++)
			if (currentSupport[i] / 2 > minimumSupport[i]) {
				currentSupport[i] /= 3;
				change = true;
			} else if (currentSupport[i] > minimumSupport[i]) {
				currentSupport[i] = minimumSupport[i];
				change = true;
			} else
				continue;
		if (change) {
			System.out.println(this.enumeratedSetCount);
			input.setMinSupport(currentSupport);
			((StreamIterator) it).setNextAvailable();
		}
		return change;
	}

	@Override
	public boolean pruneBranch() {
		return input.pruneBranch();
	}

	@Override
	public void storeSelected(int[] selectedFeatureIDs,
			String selectedFeatureFile) {
		throw new UnsupportedOperationException(
				"storeSelected() method is not supported");
	}

	/**
	 * hasNext is hard to implement given the (1) feature generator (2) prunning
	 * available Therefore, I use a trick, when next() returns a null pattern,
	 * then the next call to hasNext() will return false. Therefore, there will
	 * be one hasNext()=true, but next=null. Need to take that into
	 * consideration while running the code
	 * 
	 * @author dayuyuan
	 * 
	 */
	@Override
	public Iterator<ISet> iterator() {
		enumeratedSetCount = 0;
		patternEnumerationTime = 0;
		input.setMinSupport(maxinumSupport);
		return new StreamIterator();
	}

	private class StreamIterator implements Iterator<ISet> {
		private boolean isNextAvailable;

		public StreamIterator() {
			this.isNextAvailable = true;
		}

		public void setNextAvailable() {
			this.isNextAvailable = true;
		}

		@Override
		public boolean hasNext() {
			return this.isNextAvailable;
		}

		@Override
		public ISet next() {
			ISet nextSet = nextSet();
			if (nextSet == null) {
				isNextAvailable = false;
				enumeratedSetCount += input.enumeratedSetCount;
				patternEnumerationTime += input.patternEnumerationTime;
				return null;
			} else {
				return nextSet;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		}
	}

	@Override
	public int getQCount() {
		return input.getQCount();
	}

	@Override
	public int getGCount() {
		return input.getGCount();
	}

	@Override
	public ISet[] getEdgeSets() {
		return input.getEdgeSets();
	}

	@Override
	public long getEnumeratedSetCount() {
		return enumeratedSetCount;
	}

	@Override
	public boolean fullRangeConverter() {
		return this.input.fullRangeConverter();
	}

	@Override
	public long getPatternEnumerationTime() {
		return this.patternEnumerationTime;
	}
}
