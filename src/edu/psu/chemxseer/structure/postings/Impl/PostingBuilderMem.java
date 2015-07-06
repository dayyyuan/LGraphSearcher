package edu.psu.chemxseer.structure.postings.Impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

public class PostingBuilderMem {
	protected static int step = 20;
	protected ArrayList<int[]> postings;
	protected ArrayList<Integer> bounds;
	protected Map<Integer, Integer> nameConverter;

	// Dummy Constructor
	public PostingBuilderMem() {
		this.postings = new ArrayList<int[]>();
		this.bounds = new ArrayList<Integer>();
		this.nameConverter = new HashMap<Integer, Integer>();
	}

	/**
	 * Load the Postings From the Disk
	 * 
	 * @param fileName
	 */
	public PostingBuilderMem(String fileName) {
		try {
			this.loadPostings(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Copy Constructor
	 * 
	 * @param postingBuilder
	 */
	public PostingBuilderMem(PostingBuilderMem postingBuilder) {
		this.postings = postingBuilder.postings;
		this.bounds = postingBuilder.bounds;
		this.nameConverter = postingBuilder.nameConverter;
	}

	private void loadPostings(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				fileName)));
		String aLine = reader.readLine();
		int size = Integer.parseInt(aLine);
		this.postings = new ArrayList<int[]>(size);
		this.bounds = new ArrayList<Integer>(size);
		this.nameConverter = new HashMap<Integer, Integer>(size);
		for (int i = 0; i < size; i++) {
			aLine = reader.readLine();
			if (aLine.length() > 0) {
				String[] tokens = aLine.split(",");
				int[] posting = new int[tokens.length];
				for (int w = 0; w < posting.length; w++)
					posting[w] = Integer.parseInt(tokens[w]);
				this.postings.add(posting);
				this.bounds.add(posting.length);
			} else {
				this.postings.add(new int[0]);
				this.bounds.add(0);
			}
		}
		for (int i = 0; i < size; i++) {
			aLine = reader.readLine();
			String[] tokens = aLine.split(",");
			Integer key = Integer.parseInt(tokens[0]);
			Integer value = Integer.parseInt(tokens[1]);
			this.nameConverter.put(key, value);
		}
		reader.close();
	}

	/**
	 * Save the Postings
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void savePosting(String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				fileName)));
		writer.write(this.postings.size() + "\n");
		for (int i = 0; i < postings.size(); i++) {
			int[] posting = postings.get(i);
			int bound = this.bounds.get(i);
			StringBuffer sbuf = new StringBuffer();
			for (int w = 0; w < bound; w++) {
				sbuf.append(posting[w]);
				sbuf.append(',');
			}
			if (bound > 0)
				sbuf.deleteCharAt(sbuf.length() - 1);
			// else
			// System.out.println("Exception in PostingBuilderMem:savePosting, empty posting for feture "
			// + i);
			sbuf.append('\n');
			writer.write(sbuf.toString());
		}
		for (Entry<Integer, Integer> entry : this.nameConverter.entrySet()) {
			writer.write(entry.getKey().toString() + "," + entry.getKey()
					+ "\n");
		}
		writer.close();
	}

	/**
	 * Insert the postings for the feature: featureID
	 * 
	 * @param featureID
	 * @param posting
	 */
	public void insertOnePosting(Integer featureID, int[] posting) {
		if (this.nameConverter.containsKey(featureID)) {
			System.out.println("Error in Insert Postings: This feature exists");
			return;
		} else {
			this.nameConverter.put(featureID, this.postings.size());
			this.postings.add(posting);
			this.bounds.add(posting.length);
		}
	}

	public void insertOnePostingOneGraph(Integer fID, int gID) {
		if (this.nameConverter.containsKey(fID))
			this.insertOneGraphIndex(gID, nameConverter.get(fID));
		else {
			int index = nameConverter.size();
			nameConverter.put(fID, index);
			this.postings.add(new int[step]);
			this.bounds.add(0);
			this.insertOneGraphIndex(gID, index);
		}
	}

	/**
	 * Update Posting of the Feature
	 * 
	 * @param featureID
	 * @param newPosting
	 */
	public void updatePosting(Integer featureID, int[] newPosting) {
		if (!this.nameConverter.containsKey(featureID)) {
			this.insertOnePosting(featureID, newPosting);
		} else {
			int index = nameConverter.get(featureID);
			this.postings.set(index, newPosting);
			this.bounds.set(index, newPosting.length);
		}
	}

	/**
	 * Insert one graph (gID) into the postings of featureIDs Given the
	 * assumption
	 * 
	 * @param gID
	 * @param featureIDs
	 */
	public void insertOneGraph(int gID, int[] featureIDs) {
		for (int featureID : featureIDs) {
			if (!nameConverter.containsKey(featureID)) {
				System.out
						.println("Wrong input in PostingBuilderMem:insertOneGraph");
				continue;
			}
			insertOneGraphIndex(gID, this.nameConverter.get(featureID));
		}
	}

	private void insertOneGraphIndex(int gID, int index) {
		int[] posting = this.postings.get(index);
		if (posting.length == bounds.get(index))
			posting = Arrays.copyOfRange(posting, 0, posting.length + step);
		posting[bounds.get(index)] = gID;
		bounds.set(index, bounds.get(index) + 1);
		postings.set(index, posting);
	}

	/**
	 * We adopts a lazy deletion of the deleted graphs The update will be
	 * triggered after several deletion This function is called to process a
	 * batch of operations.
	 * 
	 * @param sortedGIDs
	 *            : have to be sorted and in increasing order
	 */
	public void deleteGraphs(int[] sortedGIDs) {
		for (int index = 0; index < this.postings.size(); index++) {
			this.postings.set(index, OrderedIntSets.remove(postings.get(index),
					0, bounds.get(index), sortedGIDs, 0, sortedGIDs.length));
			this.bounds.set(index, postings.get(index).length);
		}
	}

	/**
	 * Build & Save PostingBuilderMem
	 * 
	 * @param fileName
	 * @param posFeatures
	 *            : only build postings for selected features
	 * @return
	 * @throws IOException
	 */
	public static PostingBuilderMem buildPosting(String fileName,
			PostingFeatures posFeatures, Map<String, Integer> selectedFeatures)
			throws IOException {
		PostingBuilderMem result = new PostingBuilderMem();
		NoPostingFeatures<IOneFeature> noPosFeatures = posFeatures
				.getFeatures();
		for (IOneFeature aFeature : noPosFeatures) {
			if (selectedFeatures.get(aFeature.getDFSCode()) != null)
				result.insertOnePosting(
						selectedFeatures.get(aFeature.getDFSCode()),
						posFeatures.getPosting(aFeature));
		}
		result.savePosting(fileName);
		return result;
	}

	public static PostingBuilderMem buildPosting(String fileName,
			PostingFeaturesMultiClass posFeatures,
			Map<String, Integer> selectedFeatures) throws IOException {
		PostingBuilderMem result = new PostingBuilderMem();
		NoPostingFeatures<OneFeatureMultiClass> noPosFeatures = posFeatures
				.getMultiFeatures();
		for (OneFeatureMultiClass aFeature : noPosFeatures) {
			if (selectedFeatures.get(aFeature.getDFSCode()) != null) {
				result.insertOnePosting(
						selectedFeatures.get(aFeature.getDFSCode()),
						posFeatures.getFullPosting(aFeature.getFeatureId(), 0));
			}
			// I only care about the postings on database graphs
		}
		result.savePosting(fileName);
		return result;
	}

	public static PostingBuilderMem buildPosting(String fileName,
			IPostingFetcher pFetcher, int tLength) {
		PostingBuilderMem result = new PostingBuilderMem();
		for (int i = 0; i < tLength; i++) {
			int[] temp = pFetcher.getPostingID(i);
			// if(temp.length==0)
			// System.out.println("Exception in PostingBuilderMem: buildPosting, empty posting for feature"
			// + i);
			result.insertOnePosting(i, temp);
		}
		try {
			result.savePosting(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
