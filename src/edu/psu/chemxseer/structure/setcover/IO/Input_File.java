package edu.psu.chemxseer.structure.setcover.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.InputType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;

/**
 * All Sets are Pre-computed and Stored on Disk The streaming implementation of
 * the standard input interface (1) Load all the sets from the disk (2) Can
 * output set one after another
 * 
 * @author dayuyuan
 */
public class Input_File implements IInputSequential {

	private String inputFileName;
	private ObjectInputStream objectReader;
	private IFeatureConverter converter;
	private long enumeratedSetCount = 0;
	private long patternEnumerationTime = 0;

	/**
	 * Package default constructor, encapsulated from outsider access Create an
	 * Input_File Object
	 * 
	 * @param fileName
	 * @param universeSize
	 */
	Input_File(String fileName, IFeatureConverter converter) {
		this.inputFileName = fileName;
		this.converter = converter;
	}

	/**
	 * Assume that the sets are stored on disk (fileName), load them into
	 * memory. return null if the setFile does not exists or can not be loaded
	 * Depending on the type of the set stored on disk, it can be either
	 * Set_Array or Set_Pair
	 * 
	 * @param setFile
	 */
	public static Input_File newInstance(String setFile,
			IFeatureConverter converter) {
		Input_File result = new Input_File(setFile, converter);
		return result;
	}

	/**
	 * Given the features and converter, build a Input_File(Pair), storing at
	 * fileName Then load & return it.
	 * 
	 * @param features
	 * @param converter
	 * @param fileName
	 * @return
	 */
	public static Input_File newInstance(PostingFeaturesMultiClass features,
			IFeatureConverter converter, String fileName) {
		// 1. First step, convert & store, since only Bucket_File support the
		// write the file, we borrow the Bucket_File
		Bucket_File bucket_file = (Bucket_File) Bucket_Factory.createBucket(
				fileName, converter, InputType.onDisk);
		NoPostingFeatures<OneFeatureMultiClass> allFeatures = features
				.getMultiFeatures();
		for (OneFeatureMultiClass oneFeature : allFeatures) {
			bucket_file.append(converter.toSetPair(oneFeature,
					features.getPosting(oneFeature)));
		}
		bucket_file.closeOutputStream();
		// 2. Second step, load the input_file
		return newInstance(fileName, converter);
	}

	private void initialize() {
		if (this.objectReader == null) {
			File temp = new File(inputFileName);
			if (!temp.exists())
				return; // can not initialize the reader
			try {
				this.objectReader = new ObjectInputStream(new FileInputStream(
						this.inputFileName));
			} catch (FileNotFoundException e) {
				System.out
						.println("Error in Initializing the Input_File of Set Cover");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void finalize() {
		if (this.objectReader != null) {
			System.out
					.println("Exception in Input_File:finalize, the output stream is not closed properly"); // Log
																											// for
																											// debug
			this.closeInputStream();
		}
	}

	@Override
	public void closeInputStream() {
		if (objectReader != null)
			try {
				this.objectReader.close();
				this.objectReader = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public Iterator<ISet> iterator() {
		return new SetIteratorFile();
	}

	private class SetIteratorFile implements Iterator<ISet> {
		protected boolean readNext;
		private ISet nextSet = null;
		private boolean intheEnd;

		public SetIteratorFile() {
			closeInputStream();// First close the old stream
			initialize(); // Then start the new stream
			enumeratedSetCount = 0;
			patternEnumerationTime = 0;
			this.readNext = true;
			this.nextSet = null;
			this.intheEnd = false;
		}

		@Override
		public boolean hasNext() {
			if (intheEnd) {
				this.nextSet = null;
				return false;
			}
			if (readNext) {
				if (objectReader == null) {
					intheEnd = true;
					return false; // the object reader may not exists
				}
				try {
					nextSet = (ISet) objectReader.readObject();
				} catch (IOException e) {
					intheEnd = true;
					closeInputStream();
					return false;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				readNext = false;
			}
			return true;
		}

		@Override
		public ISet next() {
			long startT = System.currentTimeMillis();
			if (readNext) {
				hasNext();
			}
			if (nextSet == null)
				throw new NoSuchElementException();
			readNext = true;
			((Set_Pair) nextSet).setFeatureConverter(converter);
			patternEnumerationTime += System.currentTimeMillis() - startT;
			enumeratedSetCount++;
			return nextSet;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		}
	}

	@Override
	public void storeSelected(int[] selectedSetIDs, String selectedFeatureFile) {
		Arrays.sort(selectedSetIDs);
		ObjectOutputStream outputStream;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(
					selectedFeatureFile));
			int index = 0;
			for (ISet oneSet : this) {
				if (index == selectedSetIDs.length)
					break;
				if (oneSet.getSetID() == selectedSetIDs[index]) {
					if (oneSet instanceof Set_Pair)
						outputStream.writeObject(oneSet);
					else
						System.out
								.println("Exception in Input_Mem: storeSelected: can not serialize ISet object except for"
										+ "implemnetations Set_Array or Set_Pair");
					index++;
				} else if (oneSet.getSetID() < selectedSetIDs[index])
					continue;
				else {
					System.out
							.println("There must be something wrong since the selected set is not found");
				}
			}
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Return the fileName of the input stream
	 * 
	 * @return
	 */
	public String getFileName() {
		return this.inputFileName;
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
		List<ISet> result = new ArrayList<ISet>();
		for (ISet set : this) {
			if (set.getFeature().getFeature().getFeatureGraph().getEdgeCount() == 1)
				result.add(set);
		}
		ISet[] finalResult = new ISet[result.size()];
		result.toArray(finalResult);
		return finalResult;
	}

	@Override
	public long getEnumeratedSetCount() {
		return enumeratedSetCount;
	}

	@Override
	public long getPatternEnumerationTime() {
		return patternEnumerationTime;
	}

}
