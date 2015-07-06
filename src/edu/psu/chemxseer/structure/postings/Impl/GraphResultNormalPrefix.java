package edu.psu.chemxseer.structure.postings.Impl;

import edu.psu.chemxseer.structure.postings.Interface.IGraphResultPref;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;

public class GraphResultNormalPrefix extends GraphResultNormal implements
		IGraphResultPref {
	private int prefixID;
	private int[][] suffix;

	/**
	 * Currently support DFS parser only Prefix + Suffix If the prefixID ==-1,
	 * then the suffix is just the graph
	 * 
	 * @param ID
	 * @param prefix
	 * @param suffix
	 */
	public GraphResultNormalPrefix(int ID, int prefixID, int[][] suffix,
			IIndexPrefix index) {
		super(ID, null);
		this.prefixID = prefixID;
		this.suffix = suffix;
		if (prefixID != -1) {
			int[][] prefix = index.getTotalLabel(prefixID);
			if (suffix != null) {
				int[][] gString = new int[prefix.length + suffix.length][];
				for (int i = 0; i < prefix.length; i++)
					gString[i] = prefix[i];
				for (int j = 0; j < suffix.length; j++)
					gString[prefix.length + j] = suffix[j];
				this.g = MyFactory.getDFSCoder().parse(gString,
						MyFactory.getGraphFactory());
			} else
				this.g = MyFactory.getDFSCoder().parse(prefix,
						MyFactory.getGraphFactory());
		} else {
			if (suffix != null)
				this.g = MyFactory.getDFSCoder().parse(suffix,
						MyFactory.getGraphFactory());
			else {
				System.out.println("Exception: both prefix & suffix are null");
				this.g = null;
			}
		}
	}

	@Override
	public int getPrefixFeatureID() {
		return this.prefixID;
	}

	@Override
	public int[][] getSuffix() {
		return suffix;
	}
}
