package edu.psu.chemxseer.structure.supersearch.PrefIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_Basic;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;
import edu.psu.chemxseer.structure.util.OrderedIntSets;

/**
 * The Implementation of the PrefixIndex:
 * 
 * @author dayuyuan
 * 
 */
public class PrefixIndexSearcher extends GraphDatabase_Basic implements
		IIndex0, IIndexPrefix, IGraphDatabase {

	private Map<Integer, FastSUCompleteEmbedding> subEmbedding;
	private boolean allExamed;
	private Graph internalQuery;

	protected PrefixFeature[] features;

	public PrefixIndexSearcher(PrefixFeature[] features) {
		super(MyFactory.getDFSCoder());
		this.features = features;
		this.subEmbedding = new HashMap<Integer, FastSUCompleteEmbedding>();
		this.allExamed = false;
		this.internalQuery = null;
	}

	@Override
	/**
	 * TimeComponent[2] is calculated: filtering cost
	 */
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		this.internalQuery = query;
		this.allExamed = true;
		long start = System.currentTimeMillis();
		List<Integer> results = new ArrayList<Integer>();
		subEmbedding.clear();
		// upp-most Index searcher
		for (int i = 0; i < this.features.length; i++) {
			Graph f = features[i].getGraph();
			if (f.getNodeCount() > query.getNodeCount()
					|| f.getEdgeCount() > query.getEdgeCount())
				continue;

			FastSUCompleteEmbedding embedding = new FastSUCompleteEmbedding(f,
					query);
			if (embedding.issubIsomorphic()) {
				results.add(i);
				subEmbedding.put(i, embedding);
			}
		}
		TimeComponent[2] = System.currentTimeMillis() - start;
		return results;
	}

	/**
	 * Not the Upper-Most Layer, subgraph search TimeComponent[2] is calculated
	 * as filtering cost
	 * 
	 * @param query
	 * @param candidate
	 * @param embes
	 * @param TimeComponent
	 * @return
	 */
	public List<Integer> subgraphs(Graph query, List<Integer> candidate,
			Map<Integer, FastSUCompleteEmbedding> embes, long[] TimeComponent) {
		this.internalQuery = query;
		this.allExamed = true;
		long start = System.currentTimeMillis();
		List<Integer> results = new ArrayList<Integer>();

		subEmbedding.clear();
		// lower-level Index Searcher
		for (Integer oneID : candidate) {
			PrefixFeature oneFeature = this.features[oneID];
			PrefixFeature prefFeature = oneFeature.getPrefixFeature();
			if (prefFeature == null) {
				Graph f = oneFeature.getGraph();
				if (f.getNodeCount() > query.getNodeCount()
						|| f.getEdgeCount() > query.getEdgeCount())
					continue;
				FastSUCompleteEmbedding embedding = new FastSUCompleteEmbedding(
						f, query);
				if (embedding.issubIsomorphic()) {
					results.add(oneID);
					subEmbedding.put(oneID, embedding);
				}
			} else {
				FastSUCompleteEmbedding prefEmb = embes.get(prefFeature
						.getFeatureID());
				FastSUCompleteEmbedding thisEmb = new FastSUCompleteEmbedding(
						prefEmb, oneFeature.getSuffix());
				if (thisEmb.issubIsomorphic()) {
					results.add(oneID);
					subEmbedding.put(oneID, thisEmb);
				}
			}
		}
		TimeComponent[2] += System.currentTimeMillis() - start;
		return results;
	}

	/**
	 * Given the query graph, return all the subgraphs features not subgraph
	 * isomorphic to the query The subEmbedding records all the subgraph
	 * features subgraph isomorphic to the query
	 * 
	 * @param query
	 * @param TimeComponent
	 * @return
	 */
	public List<Integer> nonSubgraphs(Graph query, long[] TimeComponent) {
		this.internalQuery = query;
		this.allExamed = true;
		long start = System.currentTimeMillis();
		List<Integer> results = new ArrayList<Integer>();
		subEmbedding.clear();
		for (int i = 0; i < this.features.length; i++) {
			Graph f = features[i].getGraph();
			if (f.getNodeCount() > query.getNodeCount()
					|| f.getEdgeCount() > query.getEdgeCount()) {
				results.add(i);
				continue;
			}
			FastSUCompleteEmbedding embedding = new FastSUCompleteEmbedding(f,
					query);
			if (embedding.issubIsomorphic())
				subEmbedding.put(i, embedding);
			else
				results.add(i);
		}
		TimeComponent[2] = System.currentTimeMillis() - start;
		return results;
	}

	/**
	 * Given Subgraphs, return the non-subgraphs
	 * 
	 * @param candidates
	 * @param TimeComponent
	 * @return
	 */
	public ArrayList<Integer> nonSubgraphs(int[] subgraphs, long[] TimeComponent) {
		long start = System.currentTimeMillis();
		int[] result = OrderedIntSets.getCompleteSet(subgraphs,
				this.getFeatureCount());
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < result.length; i++)
			results.add(result[i]);
		TimeComponent[2] += System.currentTimeMillis() - start;
		return results;
	}

	public int getFeatureCount() {
		return this.features.length;
	}

	@Override
	/**
	 * Given the Graph "g"
	 * (1) Find the Subgraphs of this graph "g" 
	 * (2) Select the One With the maximum gain as the prefix
	 */
	public int getPrefixID(Graph g) {
		long[] TimeComponent = new long[4];
		List<Integer> subgraphs = this.subgraphs(g, TimeComponent);
		int maxGain = Integer.MIN_VALUE;
		int prefix = -1;
		for (Integer oneSub : subgraphs) {
			int gain = this.getGainValue(oneSub);
			if (gain > maxGain) {
				maxGain = gain;
				prefix = oneSub;
			}
		}
		return prefix;
	}

	/**
	 * The gain function is the size of the feature with fID
	 * 
	 * @param fID
	 * @return
	 */
	private int getGainValue(int fID) {
		return this.features[fID].getGraph().getEdgeCount();
	}

	public PrefixFeature getFeature(int prefixID) {
		if (prefixID >= this.features.length || prefixID == -1) {
			System.out.println("Error in GetFeatures: out of boundary");
			return null;
		} else
			return this.features[prefixID];
	}

	@Override
	public int[][] getTotalLabel(int fID) {
		return this.features[fID].getWholeLabel();
	}

	@Override
	public int[][] getExtension(int gID) {
		return this.features[gID].getSuffix();
	}

	@Override
	public int getPrefixID(int gID) {
		return this.features[gID].getPrefixFeature().getFeatureID();
	}

	@Override
	public FastSUCompleteEmbedding getEmbedding(int fID, Graph query) {
		boolean runISO = false;
		if (query == this.internalQuery) {
			if (this.allExamed) {
				return this.subEmbedding.get(fID);
			} else if (subEmbedding.containsKey(fID))
				return this.subEmbedding.get(fID);
			else {
				runISO = true;
			}
		} else {
			this.internalQuery = query;
			this.allExamed = false;
			this.subEmbedding.clear();
			runISO = true;
		}
		if (runISO) {
			Graph featureGraph = this.features[fID].getGraph();
			if (featureGraph.getEdgeCount() > query.getEdgeCount()
					|| featureGraph.getNodeCount() > query.getNodeCount())
				return null;
			FastSUCompleteEmbedding emb = new FastSUCompleteEmbedding(
					featureGraph, query);
			if (emb.issubIsomorphic()) {
				this.subEmbedding.put(fID, emb);
				return emb;
			} else
				return null;
		} else
			return null; // may not happen
	}

	public Map<Integer, FastSUCompleteEmbedding> getAllEmbeddings() {
		return this.subEmbedding;
	}

	@Override
	public String findGraphString(int id) {
		return MyFactory.getDFSCoder().writeArrayToText(this.getTotalLabel(id));
	}

	@Override
	public void setGString(int gID, String serialize) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTotalNum() {
		return this.getFeatureCount();
	}

	@Override
	public int[] getAllFeatureIDs() {
		int[] rest = new int[this.features.length];
		for (int i = 0; i < rest.length; i++)
			rest[i] = i;
		return rest;
	}

	@Override
	public int getPrefixGain(int tID) {
		return this.getGainValue(tID);
	}

}
