package edu.psu.chemxseer.structure.subsearch.TreeDelta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphs;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_Gindex;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.subgen.SubgraphGenerator2;

/**
 * This is a simulation of the Tree+Delta The is only for the sake of feature
 * mining, but not for the
 * 
 * @author dayuyuan
 * 
 */
public class FeatureSelector {
	// Given a already build Gindex with the subTree features
	// and all the training queries, find all the subGraph features that are
	// discriminative with
	// respect to the subTree features, and discriminative with repsect to other
	// subgraph features
	private SubSearch_Gindex treeIndex;
	private Map<String, IOneFeature> subGraphFeatures; // featureDFSCode, TG
	private int maxEdgeCount;
	private float dbSize;

	private float epsilong;
	private float delta;
	private float theta; // the discriminative ration between deltaTG and
							// deltaTGPrime

	private int id = 0;

	/**
	 * Build a Feature Selector
	 * 
	 * @param treeIndex
	 * @param maxEdgeCount
	 * @param gDB
	 * @throws IOException
	 */
	public FeatureSelector(SubSearch_Gindex treeIndex, int maxEdgeCount,
			GraphDatabase_OnDisk gDB) throws IOException {
		this.treeIndex = treeIndex;
		this.subGraphFeatures = new HashMap<String, IOneFeature>();
		this.maxEdgeCount = maxEdgeCount;
		this.dbSize = gDB.getTotalNum();

		this.epsilong = (float) 0.1; // discrimiantive ratio between graph and
										// trees
		this.delta = (float) 0.1;// minimum frequency
		this.theta = (float) 0.8; // discriminative ration between graphs

		id = 0;
	}

	/**
	 * Given the set of queries, mine the subgaph features and store them in the
	 * subGraphFeatures
	 * 
	 * @param queries
	 */
	public NoPostingFeatures<IOneFeature> mineFeatures(IGraphs queries) {
		this.subGraphFeatures.clear();
		for (int i = 0; i < queries.getGraphNum(); i++) {
			Graph query = queries.getGraph(i);
			if (query.getEdgeCount() == query.getNodeCount() - 1)
				continue; // tree query
			else {
				try {
					selectGraph(query);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		IOneFeature[] features = new IOneFeature[this.subGraphFeatures.size()];
		features = this.subGraphFeatures.values().toArray(features);
		return new NoPostingFeatures<IOneFeature>(features);
	}

	/**
	 * Current implementation is very slow, but that is OK since we do not
	 * consider the time complexity
	 * 
	 * @param query
	 * @throws IOException
	 */
	private void selectGraph(Graph query) throws IOException {
		// 1. Enumerate all the subgraphs
		SubgraphGenerator2 subGen = new SubgraphGenerator2(query,
				this.maxEdgeCount);
		Graph oneSub = subGen.nextSubgraphG();
		List<GraphWrapper> graphs = new ArrayList<GraphWrapper>();
		long[] dontCare = new long[4];
		while (oneSub != null) {
			if (oneSub.getEdgeCount() >= oneSub.getNodeCount()) {
				// only add graphs, do not consider trees
				// test whether the graph is frequent, estimated by using the
				// T(q)
				float freqUpper = this.treeIndex.candidateByFeatureJoin(oneSub,
						dontCare).size()
						/ dbSize;
				if (freqUpper < delta)
					subGen.earlyPruning();
				else
					graphs.add(new GraphWrapper(oneSub));
			}
			oneSub = subGen.nextSubgraphG();
		}
		// 2. Given all the subgraph features, we need to sort them and mine the
		// relationships
		GraphWrapper.mineSubSuperRelation(graphs);
		GraphWrapper root = new GraphWrapper(null);
		for (int i = 0; i < graphs.size(); i++) {
			if (graphs.get(i).getParents().size() == 0)
				root.addChild(graphs.get(i));
			// else continue
		}
		// 3. Feature selection
		List<GraphWrapper> firstLayer = root.getChildren();
		int deltaTG = 0;
		for (int i = 0; i < firstLayer.size(); i++) {
			GraphWrapper parent = firstLayer.get(i);
			parent.setVisited();
			String pDFSCode = MyFactory.getDFSCoder().serialize(parent.getG());
			if (this.subGraphFeatures.containsKey(pDFSCode))
				deltaTG = subGraphFeatures.get(pDFSCode).getFrequency();
			else {
				deltaTG = this.treeIndex.candidateByFeatureJoin(parent.getG(),
						dontCare).size();
				IOneFeature feature = new OneFeatureImpl(pDFSCode, deltaTG,
						(-1), id++, false);
				subGraphFeatures.put(pDFSCode, feature);
			}
			// intrigue to test its children
			for (int j = 0; j < parent.getChildren().size(); j++) {
				depthFirstSelection(deltaTG, parent.getChildren().get(j));
			}
		}
	}

	/**
	 * 
	 * @param deltaTG
	 * @param gPrime
	 * @throws IOException
	 */
	private void depthFirstSelection(int deltaTG, GraphWrapper gPrime)
			throws IOException {
		if (gPrime.isVisited())
			return; // nothing will happen
		else {
			String dfsCode = MyFactory.getDFSCoder().serialize(gPrime.getG());
			gPrime.setVisited();
			// gPrime is already selected
			if (this.subGraphFeatures.containsKey(dfsCode)) {
				// intrigue to test gPrime's children
				int newDeltaTG = subGraphFeatures.get(dfsCode).getFrequency();
				for (int i = 0; i < gPrime.getChildren().size(); i++)
					depthFirstSelection(newDeltaTG, gPrime.getChildren().get(i));
			}
			// gPrime is not selected yet
			else {
				long[] TimeComponent = new long[4];
				int deltaTGPrime = this.treeIndex.candidateByFeatureJoin(
						gPrime.getG(), TimeComponent).size();
				if (evaluate(deltaTG, deltaTGPrime)) {
					// select gPrime
					IOneFeature feature = new OneFeatureImpl(dfsCode,
							deltaTGPrime, -1, id++, false);
					subGraphFeatures.put(dfsCode, feature);
					// intrigue to test gPrime's children
					for (int i = 0; i < gPrime.getChildren().size(); i++)
						depthFirstSelection(deltaTGPrime, gPrime.getChildren()
								.get(i));
				} else {
					// not selected, intrigue to test gPrime's children
					for (int i = 0; i < gPrime.getChildren().size(); i++)
						depthFirstSelection(deltaTG, gPrime.getChildren()
								.get(i));
				}
			}
		}
	}

	/**
	 * evaluate whether g satisfies all the requirement Equation 17, 18, 19 in
	 * the original paper
	 * 
	 * @param g
	 * @return
	 */
	private boolean evaluate(float deltaTG, float deltaTGPrime) {
		deltaTG = deltaTG / dbSize;
		deltaTGPrime = deltaTGPrime / dbSize;
		if (deltaTGPrime > this.theta * deltaTG)
			return false; // not discriminative with each other
		float sendPar = delta + (1 - delta) * epsilong;
		if (deltaTG < max(epsilong, delta))
			return false; // equation 17
		else if (deltaTGPrime > sendPar)
			return false; // equation 18
		else if (deltaTGPrime < max(epsilong, delta))
			return false; // equation 18

		// equation 19
		float rightPar = delta * (1 - epsilong) * (1 - epsilong);
		float leftPar = (deltaTG - epsilong) * (deltaTGPrime - epsilong);

		if (leftPar < rightPar)
			return false;

		return true;
	}

	private float max(float input1, float input2) {
		if (input1 > input2)
			return input1;
		else
			return input2;
	}

	public void backUpDelta(BufferedWriter indexWriter) throws IOException {
		indexWriter.write(this.subGraphFeatures.size() + "\n");
		for (String oneKey : subGraphFeatures.keySet()) {
			indexWriter.write(oneKey + "\n");
		}
	}
}
