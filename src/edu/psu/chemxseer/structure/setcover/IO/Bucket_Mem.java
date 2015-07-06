package edu.psu.chemxseer.structure.setcover.IO;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;

public class Bucket_Mem implements IBucket {

	private Input_Mem input;

	private Bucket_Mem(Input_Mem input) {
		this.input = input;
	}

	/**
	 * Create an empty bucket
	 * 
	 * @return
	 */
	public static IBucket newEmptyInstance() {
		return new Bucket_Mem(Input_Mem.newEmptyInstance()); // for buckets we
																// do not care
																// about the
																// universeSize
	}

	public static IBucket newInstance(String setFile,
			IFeatureConverter converter) {
		return new Bucket_Mem(Input_Mem.newInstance(setFile, converter));
	}

	@Override
	public void insertWithOrder(ISet theSet) {
		// this.iterator returns an ListIterator in the Input_Mem class
		ListIterator<ISet> it = (ListIterator<ISet>) this.iterator();
		while (it.hasNext()) {
			ISet oneSet = it.next();
			if (oneSet.size() >= theSet.size())
				continue;
			else
				break;
		}
		it.add(theSet);
	}

	@Override
	public boolean append(ISet set) {
		return input.allSets.add(set);
	}

	@Override
	public void closeInputStream() throws Exception {
		input.closeInputStream();
	}

	@Override
	public Iterator<ISet> iterator() {
		return input.iterator();
	}

	@Override
	public void flush() throws IOException {
		// The method is not impelemnted for memory buckets
	}

	@Override
	public int getQCount() {
		return this.input.getQCount();
	}

	@Override
	public int getGCount() {
		return this.input.getGCount();
	}
}
