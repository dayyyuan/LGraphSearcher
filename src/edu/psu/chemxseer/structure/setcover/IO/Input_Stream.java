package edu.psu.chemxseer.structure.setcover.IO;

import java.util.Iterator;

import de.parmol.util.FrequentFragment;
import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputStream;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;

/**
 * A implementation of the stream input Sets are generate one after another with
 * DFS stream. Attention: hasNext is hard to implement given the (1) feature
 * generator (2) prunning available Therefore, I use a trick, when next()
 * returns a null pattern, then the next call to hasNext() will return false.
 * Therefore, there will be one hasNext()=true, but next=null. Need to take that
 * into consideration while running the code
 * 
 * @author dayuyuan
 * 
 */
public class Input_Stream implements IInputStream {

	protected GSpanMiner_MultiClass_Iterative patternGenerator;
	protected IFeatureConverter converter;
	protected ISet[] edgeSet;
	protected long enumeratedSetCount;
	protected long patternEnumerationTime;

	/**
	 * Construct & REturn a Input_Stream
	 * 
	 * @param gen
	 * @param converter
	 * @param type
	 * @return
	 */
	public static Input_Stream newInstance(GSpanMiner_MultiClass_Iterative gen,
			IFeatureConverter converter) {
		return new Input_Stream(gen, converter);
	}

	protected Input_Stream() {
		// dummy constructor
	}

	private Input_Stream(GSpanMiner_MultiClass_Iterative gen,
			IFeatureConverter converter) {
		this.patternGenerator = gen;
		this.converter = converter;
		this.edgeSet = buildEdgeSet(gen.genAllDistinctEdges());
	}

	private ISet[] buildEdgeSet(FrequentFragment[] genEdges) {
		ISet[] result = new ISet[genEdges.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = this.converter.toSetPair(patternGenerator
					.getFeature(genEdges[i]));
		}
		return result;
	}

	@Override
	public void closeInputStream() throws Exception {
		// Free the patternGenerator, so that GC can collect it
		patternGenerator = null;
	}

	/**
	 * Return the next CoverSet
	 * 
	 * @return
	 */
	public ISet nextSet() {
		long startT = System.currentTimeMillis();
		if (patternGenerator == null)
			return null;
		FrequentFragment frag = patternGenerator.nextPattern();
		if (frag == null)
			return null;
		Set_Pair result = this.converter.toSetPair(patternGenerator
				.getFeature(frag));
		this.patternEnumerationTime += System.currentTimeMillis() - startT;
		return result;
	}

	@Override
	public boolean pruneBranch() {
		if (this.patternGenerator != null)
			return this.patternGenerator.prune();
		else
			return false;
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
		this.enumeratedSetCount = 0;
		this.patternEnumerationTime = 0;
		if (patternGenerator != null)
			this.patternGenerator.restart();
		return new StreamIterator();
	}

	private class StreamIterator implements Iterator<ISet> {
		private boolean isNextAvailable;

		public StreamIterator() {
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
				return null;
			} else {
				enumeratedSetCount++;
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
		return this.converter.getQRange();
	}

	@Override
	public int getGCount() {
		return this.converter.getGRange();
	}

	@Override
	public ISet[] getEdgeSets() {
		return edgeSet;
	}

	public void setMinSupport(float[] maxinumSupport) {
		if (patternGenerator != null)
			this.patternGenerator.setMinSupport(maxinumSupport);
	}

	@Override
	public long getEnumeratedSetCount() {
		return enumeratedSetCount;
	}

	@Override
	public boolean fullRangeConverter() {
		return this.converter.isFullRangeConverter();
	}

	@Override
	public long getPatternEnumerationTime() {
		return this.patternEnumerationTime;
	}
}
