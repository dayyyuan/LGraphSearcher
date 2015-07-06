package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * This is a Class saving candidate Features GFeatures implementation
 * 
 * @author dayuyuan
 * 
 */
public class PostingFeatures {
	protected NoPostingFeatures<IOneFeature> features;
	protected FeaturePosting postingFetcher;
	protected FeaturePostingMem memRepresent;

	public PostingFeatures(String postingFile,
			NoPostingFeatures<IOneFeature> features) {
		if (postingFile != null)
			this.postingFetcher = new FeaturePosting(postingFile);
		else
			this.postingFetcher = null;

		this.features = features;
		this.memRepresent = null;
	}

	public PostingFeatures(FeaturePosting postingFetcher2,
			NoPostingFeatures<IOneFeature> newFeatures) {
		this.postingFetcher = postingFetcher2;
		this.features = newFeatures;
		this.memRepresent = null;
	}

	/**
	 * Load the posting file into memory
	 */
	public void loadPostingIntoMemory() {
		if (memRepresent != null)
			System.out
					.println("Error in loadPostingIntoMemory:: PostingFeatures, the posting already"
							+ "exists");
		else {
			this.memRepresent = new FeaturePostingMem();
			for (int i = 0; i < features.getfeatureNum(); i++) {
				long shift = features.getFeature(i).getPostingShift();
				this.memRepresent.insertPosting(shift,
						postingFetcher.getPosting(shift));
			}
		}
	}

	/**
	 * To save the memory, remove and grabage college the in-memory posting
	 */
	public void discardInMemPosting() {
		this.memRepresent = null;
	}

	public int[] getPosting(IOneFeature feature) {
		if (this.memRepresent == null) {
			return this.postingFetcher.getPosting(feature.getPostingShift());
		} else
			return this.memRepresent.getPosting(feature.getPostingShift());
	}

	public int[] getPosting(Integer featureID) {
		return this.getPosting(this.features.getFeature(featureID));
	}

	public PostingFeatures getSelectedFeatures(String newFeatureFile,
			String newPostingFile, boolean reserveID) {
		// 1. Get Selected Features
		List<IOneFeature> selectedFeatures = this.features
				.getSelectedFeatures();
		// 2. Store
		try {
			return this.saveFeatures(newFeatureFile, newPostingFile, reserveID,
					selectedFeatures);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public PostingFeatures getUnSelectedFeatures(String newFeatureFile,
			String newPostingFile, boolean reserveID) throws IOException {
		// 1. Get Selected Features
		List<IOneFeature> selectedFeatures = this.features
				.getUnSelectedFeatures();
		// 2. Store
		try {
			return this.saveFeatures(newFeatureFile, newPostingFile, reserveID,
					selectedFeatures);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private PostingFeatures saveFeatures(String newFeatureFile,
			String newPostingFile, boolean reserveID,
			List<IOneFeature> selectedFeatures) throws IOException {
		// 2. Record the Postings
		if (newPostingFile != null) {
			FileOutputStream tempStream = new FileOutputStream(newPostingFile);
			FileChannel postingChannel = tempStream.getChannel();
			int index = 0;
			for (IOneFeature oneFeature : selectedFeatures) {
				int fID = index;
				if (reserveID)
					fID = oneFeature.getFeatureId();
				long shift = oneFeature.getPostingShift();
				long newShift = this.postingFetcher.savePostings(
						postingChannel, shift, fID);
				oneFeature.setPostingShift(newShift);
				index++;
			}
			tempStream.close();
			postingChannel.close();
		}
		// 3. Save the Features
		NoPostingFeatures<IOneFeature> newFeatures = new NoPostingFeatures<IOneFeature>(
				newFeatureFile, selectedFeatures, reserveID);
		// 4. Return
		if (newPostingFile != null) {
			return new PostingFeatures(newPostingFile, newFeatures);
		} else
			return new PostingFeatures(this.postingFetcher, newFeatures);
	}

	public NoPostingFeatures<IOneFeature> getFeatures() {
		return this.features;
	}

}
