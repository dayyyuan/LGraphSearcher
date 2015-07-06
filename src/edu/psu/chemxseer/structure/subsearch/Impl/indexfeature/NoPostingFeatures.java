package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.ArrayIterator;

/**
 * Implementation of a collection of features <type T> (in-memory), no posting
 * files
 * 
 * @author dayuyuan
 * 
 */
public class NoPostingFeatures<T extends IOneFeature> implements Iterable<T> {
	protected String featureFileName;
	protected boolean graphAvailabel;
	protected IOneFeature[] features; // The whole set of features

	/**
	 * Copy constructor
	 * 
	 * @param template
	 */
	public NoPostingFeatures(NoPostingFeatures<T> template) {
		this.featureFileName = template.featureFileName;
		this.graphAvailabel = template.graphAvailabel;
		this.features = template.features;
	}

	public static NoPostingFeatures<IOneFeature> getGeneralType(
			NoPostingFeatures<? extends IOneFeature> template) {
		return new NoPostingFeatures<IOneFeature>(template.features,
				template.featureFileName, template.graphAvailabel);
	}

	protected NoPostingFeatures(IOneFeature[] input, String inputFilename,
			boolean inputGraphAvailabel) {
		this.features = input;
		this.featureFileName = inputFilename;
		this.graphAvailabel = inputGraphAvailabel;
	}

	/**
	 * Construct and Load a Features from the Disk
	 * 
	 * @param newFeatureFile
	 * @param factory
	 */
	public NoPostingFeatures(String newFeatureFile, FeatureFactory factory) {
		this.featureFileName = newFeatureFile;
		this.graphAvailabel = false;
		try {
			this.loadFeatures(factory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load the feature into memory, all the loaded features are of type
	 * "OneFeature"
	 * 
	 * @param featureFileName
	 * @return
	 * @throws IOException
	 */
	private void loadFeatures(FeatureFactory factory) throws IOException {
		// First read file head
		BufferedReader bin = null;
		bin = new BufferedReader(new InputStreamReader(new FileInputStream(
				featureFileName)));
		String firstLine = bin.readLine();
		if (firstLine == null) {
			bin.close();
			return;
		}
		features = new IOneFeature[Integer.parseInt(firstLine)];
		// System.out.println(this.features.length);
		// Read one Feature from feature file and Then load it into
		// this.features
		int iter = 0;
		while ((firstLine = bin.readLine()) != null) {
			features[iter] = factory.genOneFeature(iter, firstLine);
			iter++;
		}
		// Finish Loading features into memory
		bin.close();
	}

	/**
	 * Write the features to the featureFile and Construct a NoPostingFeatures
	 * class
	 * 
	 * @param featureFile
	 * @param features
	 * @param reserveID
	 * @throws IOException
	 */
	public NoPostingFeatures(String featureFile, List<T> features,
			boolean reserveID) throws IOException {
		// 1. Update the featureID
		if (reserveID) {
			int fID = 0;
			for (T oneFeature : features)
				oneFeature.setFeatureId(fID++);
		}
		// 2. Prepare to write the feature file
		if (featureFile != null) {
			BufferedWriter featureWritter = new BufferedWriter(new FileWriter(
					featureFile));
			featureWritter.write(features.size() + "\n");
			// Write the feature file
			for (T oneFeature : features) {
				featureWritter.write(oneFeature.toFeatureString() + '\n');
			}
			// finalize:
			if (featureWritter != null)
				featureWritter.close();
		}
		buidlFeatures(features, reserveID);
	}

	public NoPostingFeatures(T[] features) {
		this.features = features;
	}

	public NoPostingFeatures(List<T> features, boolean reserveID) {
		this.buidlFeatures(features, reserveID);
	}

	private void buidlFeatures(List<T> features, boolean reserveID) {
		this.features = new IOneFeature[features.size()];
		int i = 0;
		for (T oneFeature : features) {
			this.features[i] = oneFeature;
			if (reserveID == false)
				oneFeature.setFeatureId(i);
			i++;
		}
	}

	public boolean createGraphs() throws ParseException {
		if (this.graphAvailabel)
			return false;
		for (int i = 0; i < this.features.length; i++)
			features[i].creatFeatureGraph(i);
		this.graphAvailabel = true;
		return true;
	}

	public boolean sortFeatures(Comparator<IOneFeature> comparator) {
		Arrays.sort(this.features, comparator);
		return true;
	}

	@SuppressWarnings("unchecked")
	public T getFeature(int index) {
		return (T) features[index];
	}

	public int getfeatureNum() {
		return features.length;
	}

	public void saveFeatures(String newFileName) throws IOException {
		BufferedWriter featureWritter = null;
		if (newFileName != null)
			featureWritter = new BufferedWriter(new FileWriter(newFileName));
		else
			featureWritter = new BufferedWriter(new FileWriter(featureFileName));
		featureWritter.write(this.features.length + "\n");
		// Write the feature file
		for (int i = 0; i < this.features.length; i++) {
			featureWritter.write(features[i].toFeatureString() + '\n');
		}
		featureWritter.close();
	}

	public void setAllSelected() {
		for (int i = 0; i < this.features.length; i++)
			this.features[i].setSelected();
	}

	public void setAllUnSelected() {
		for (int i = 0; i < this.features.length; i++)
			this.features[i].setUnselected();
	}

	public NoPostingFeatures<T> mergeFeatures(NoPostingFeatures<T> featureTwo,
			String newFeatureFile) throws IOException {
		// 0. Build the new selected features (In memory)
		List<T> newFeatures = new ArrayList<T>();
		for (int i = 0; i < this.getfeatureNum(); i++)
			newFeatures.add(this.getFeature(i));
		for (int j = 0; j < featureTwo.getfeatureNum(); j++)
			newFeatures.add(featureTwo.getFeature(j));

		return new NoPostingFeatures<T>(newFeatureFile, newFeatures, false);
	}

	/**
	 * Return the List of Selected Features" If NoPostingFeatures is desired,
	 * pls use the construcor
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> getSelectedFeatures() {
		List<T> results = new ArrayList<T>();
		for (int i = 0; i < this.features.length; i++) {
			if (features[i].isSelected())
				results.add((T) features[i]);
		}
		return results;
	}

	/**
	 * Return the List of Unselected Features: If NoPostingFeatures is desired,
	 * pls use the constructor
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> getUnSelectedFeatures() {
		List<T> results = new ArrayList<T>();
		for (int i = 0; i < this.features.length; i++) {
			if (!features[i].isSelected())
				results.add((T) features[i]);
		}
		return results;
	}

	//
	// @Override
	// public Graph[] loadGraphs(int startNum, int endNum) {
	// Graph[] results = new Graph[endNum-startNum];
	// for(int i = 0; i< results.length; i++)
	// results[i] = this.getFeature(i+startNum).getFeatureGraph();
	// return results;
	// }
	//
	// @Override
	// public String findGraphString(int id) {
	// return this.getFeature(id).getDFSCode();
	// }
	//
	// @Override
	// public int getTotalNum() {
	// return this.getfeatureNum();
	// }

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		return new FeatureIterable(this.features);
	}

	class FeatureIterable extends ArrayIterator {

		public FeatureIterable(IOneFeature[] array) {
			super(array);
		}

		@Override
		@SuppressWarnings("unchecked")
		public T next() {
			return (T) super.next();
		}
	}

}
