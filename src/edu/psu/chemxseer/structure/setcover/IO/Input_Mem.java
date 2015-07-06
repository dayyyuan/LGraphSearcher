package edu.psu.chemxseer.structure.setcover.IO;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputRandom;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureConverterSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;

/**
 * (1) A in-memory input, assume all the items can be stored in-memory (2) Also
 * Implements the IBucket
 * 
 * @author dayuyuan
 * 
 */
public class Input_Mem implements IInputRandom {

	ArrayList<ISet> allSets;
	ListIterator<ISet> currentIterator;
	int gCount;
	int qCount;

	Input_Mem(List<ISet> allSets, int qCount, int gCount) {
		this.allSets = new ArrayList<ISet>();
		this.allSets.addAll(allSets);
		this.qCount = qCount;
		this.gCount = gCount;
	}

	Input_Mem() {
		this.allSets = new ArrayList<ISet>();
		gCount = qCount = 0;
	}

	/**
	 * Create an empty new Instance of Input_Mem
	 * 
	 * @return
	 */
	public static Input_Mem newEmptyInstance() {
		return new Input_Mem();

	}

	/**
	 * Assume that the sets are stored on disk (fileName), load them into
	 * memory. return null if the setFile does not exists or can not be loaded
	 * Depending on the type of the set stored on disk, it can be either
	 * Set_Array or Set_Pair
	 * 
	 * @param setFile
	 * @param converter
	 * @param sType
	 * @return
	 */
	public static Input_Mem newInstance(String setFile,
			IFeatureConverter converter) {
		Input_File input = Input_File.newInstance(setFile, converter);
		List<ISet> allSets = new ArrayList<ISet>();
		for (ISet set : input)
			allSets.add(set);
		input.closeInputStream();
		return new Input_Mem(allSets, converter.getQRange(),
				converter.getGRange());
	}

	/**
	 * Load the features (+ their postings) into memory & construct the
	 * in-memory sets The type of the set depends on sType
	 * 
	 * @param features
	 * @param converter
	 * @return
	 */
	public static Input_Mem newInstance(PostingFeaturesMultiClass features,
			IFeatureConverter converter) {
		List<ISet> allSets = new ArrayList<ISet>();
		allSets.addAll(Arrays.asList(converter.toSetPairs(features)));
		return new Input_Mem(allSets, converter.getQRange(),
				converter.getGRange());
	}

	public static Input_Mem newInstance(
			List<IFeatureWrapper> candidateFeatures,
			FeatureConverterSimple converter) {
		List<ISet> allSets = new ArrayList<ISet>();
		allSets.addAll(Arrays.asList(converter.toSetPairs(candidateFeatures)));
		return new Input_Mem(allSets, converter.getQRange(),
				converter.getGRange());
	}

	@Override
	public void storeSelected(int[] selectedSetID, String selectedFeatureFile) {
		Arrays.sort(selectedSetID);
		ObjectOutputStream outputStream;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(
					selectedFeatureFile));
			int setCount = selectedSetID.length;
			outputStream.writeInt(setCount);
			for (int rID : selectedSetID) {
				ISet theSet = this.getSet(rID);
				if (theSet instanceof Set_Pair)
					outputStream.writeObject(theSet);
				else
					System.out
							.println("Exception in Input_Mem: storeSelected: can not serialize ISet object except for"
									+ "implemnetations Set_Array or Set_Pair");
			}
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Iterator<ISet> iterator() {
		currentIterator = this.allSets.listIterator();
		return currentIterator;
	}

	@Override
	public ISet getSet(int setID) {
		int pos = setID - 1;
		if (pos < 0 || pos > allSets.size()) {
			throw new NoSuchElementException();
		}
		if (allSets.get(pos).isDeleted())
			return null;
		return allSets.get(pos);
	}

	@Override
	public int getSetCount() {
		return this.allSets.size();
	}

	@Override
	public void delete(int setID) {
		this.allSets.get(setID).lazyDelete();
	}

	@Override
	public void closeInputStream() {
		// dummy closeInputStream for Input_Mem
	}

	@Override
	public SetwithScore swapToScoreSet(ISet set) {
		SetwithScore newSet = new SetwithScore(set);
		this.setSet(newSet);
		return newSet;
	}

	private void setSet(SetwithScore newSet) {
		int pos = newSet.getSetID() - 1;
		this.allSets.set(pos, newSet);
	}

	@Override
	public int getQCount() {
		return this.qCount;
	}

	@Override
	public int getGCount() {
		return this.gCount;
	}

	public void reAssignID() {
		for (int i = 0; i < this.allSets.size(); i++) {
			allSets.get(i).setSetID(i + 1);
		}
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
		return currentIterator.nextIndex();
	}

	@Override
	public long getPatternEnumerationTime() {
		// TODO: for now, just assume that the memory access time is
		// neglectable.
		return 0;
	}
}
