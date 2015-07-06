package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.iso.FastSUStateExpandable;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * A Class Similar to the LindexTerm, but does not have children Only have
 * "prefixID"
 * 
 * @author dayuyuan
 * 
 */
public class PrefixFeature {
	private int[][] featureSuffix;
	private int featureID;
	private PrefixFeature prefixFeature;

	/**
	 * Construct a Prefix Feature The input featureString is in DFS code format
	 * 
	 * @param featureString
	 */
	public PrefixFeature(int featureID, String featureString,
			PrefixFeature prefixFeature) {
		this.featureID = featureID;
		this.featureSuffix = MyFactory.getDFSCoder().parseTextToArray(
				featureString);
		this.prefixFeature = prefixFeature;
	}

	/**
	 * Given the string representation of the PrefixFeature, and the
	 * indexSearcher(can be null) Construct a PrefixFeature
	 * 
	 * @param prefixFeatureString
	 * @param indexSearcher
	 */
	public PrefixFeature(String prefixFeatureString,
			PrefixIndexSearcher indexSearcher) {
		String[] tokens = prefixFeatureString.split(",");
		this.featureID = Integer.parseInt(tokens[0]);
		this.featureSuffix = MyFactory.getDFSCoder()
				.parseTextToArray(tokens[1]);
		int prefixID = Integer.parseInt(tokens[2]);
		if (prefixID == -1) {
			this.prefixFeature = null;
			// if(indexSearcher != null)
			// System.out.println("Exception: Cannot find the prefixFeature: Error in PrefixFeature Construction");
		} else
			this.prefixFeature = indexSearcher.getFeature(prefixID);
	}

	/**
	 * Construct a Prefix Feature (with prefix in the upperLevelIndex)
	 * 
	 * @param dfsCode
	 * @param upperLevelIndex
	 */
	public PrefixFeature(int featureID, Graph g,
			PrefixIndexSearcher upperLevelIndex) {
		this.featureID = featureID;
		int prefixID = upperLevelIndex.getPrefixID(g);
		if (prefixID != -1) {
			// 1. the emb is the embedding from the prefix to the graph g
			FastSUCompleteEmbedding emb = upperLevelIndex.getEmbedding(
					prefixID, g);
			// 2. relabel the graph g as the suffix of its prefix feature
			this.prefixFeature = upperLevelIndex.getFeature(prefixID);
			FastSU iso = new FastSU();
			FastSUStateExpandable suState = emb.getState();
			this.featureSuffix = iso.graphExtensionLabeling(suState)
					.getExtension();
		} else {
			this.prefixFeature = null;
			this.featureSuffix = MyFactory.getDFSCoder().serializeToArray(g);
		}
	}

	// /**
	// * Assign One Prefix Feature
	// * @param prefix
	// */
	// public FastSUStateLabelling assignPrefix(PrefixFeature prefix,
	// FastSUStateLabelling prefixISO){
	// if(prefixFeature != null)
	// return null;// the prefix is already bounded
	// FastSU iso = new FastSU();
	// FastSUStateLabelling labelling = null;
	// if(prefixISO == null){
	// if(prefix.prefixFeature != null){
	// System.out.println("No Prefix ISO Found, but the Prefix is prefixed labeled");
	// return null;
	// }
	// else labelling = iso.graphExtensionLabeling(prefix.featureSuffix,
	// this.featureSuffix);
	// }
	// else labelling = iso.graphExtensionLabeling(prefixISO,
	// this.featureSuffix);
	// this.featureSuffix = labelling.getExtension();
	// this.prefixFeature = prefix;
	// return labelling;
	// }
	public String getWholeGraphString() {
		return MyFactory.getDFSCoder().writeArrayToText(this.getWholeLabel());
	}

	public Graph getGraph() {
		if (this.prefixFeature != null) {
			int[][] wholeLabel = this.getWholeLabel();
			return MyFactory.getDFSCoder().parse(wholeLabel,
					MyFactory.getGraphFactory());
		} else
			return MyFactory.getDFSCoder().parse(this.featureSuffix,
					MyFactory.getGraphFactory());
	}

	public int[][] getWholeLabel() {
		if (this.prefixFeature != null) {
			int[][] prefix = this.prefixFeature.getWholeLabel();
			int[][] results = new int[prefix.length + featureSuffix.length][];
			for (int i = 0; i < prefix.length; i++)
				results[i] = prefix[i];
			for (int j = 0; j < featureSuffix.length; j++)
				results[prefix.length + j] = featureSuffix[j];
			return results;
		} else
			return this.featureSuffix;
	}

	public int getFeatureID() {
		return this.featureID;
	}

	/**
	 * FeatureID, FeatureString, prefixFeatureID
	 */
	@Override
	public String toString() {
		String featureString = MyFactory.getDFSCoder().writeArrayToText(
				this.featureSuffix);
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(this.featureID);
		sbuf.append(',');
		sbuf.append(featureString);
		sbuf.append(',');
		if (this.prefixFeature != null)
			sbuf.append(this.prefixFeature.featureID);
		else
			sbuf.append(-1);
		return sbuf.toString();
	}

	public PrefixFeature getPrefixFeature() {
		return this.prefixFeature;
	}

	public int[][] getSuffix() {
		return this.featureSuffix;
	}
}
