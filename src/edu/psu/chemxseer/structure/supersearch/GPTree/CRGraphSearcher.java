package edu.psu.chemxseer.structure.supersearch.GPTree;

import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSUCompleteEmbedding;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;

public class CRGraphSearcher implements IIndex0 {
	protected CRGraphNode[] allNodes;
	protected boolean[] visitedStatus;
	protected IIndexPrefix FGPTree;

	protected CRGraphSearcher(CRGraphNode[] allNodes, IIndexPrefix FGPTree) {
		this.allNodes = allNodes;
		this.FGPTree = FGPTree;
		this.visitedStatus = new boolean[allNodes.length];
		for (int i = 0; i < visitedStatus.length; i++)
			visitedStatus[i] = false;
	}

	private void clearStatus() {
		for (int i = 0; i < visitedStatus.length; i++)
			visitedStatus[i] = false;
	}

	@Override
	/**
	 * (1) All Nodes are organized based on certain partial order
	 * (2) Use the "FGPtree" to search on the CRGraph, to find the indexing features
	 * contained in "g". 
	 * The Time component[2] is calculated
	 */
	public List<Integer> subgraphs(Graph query, long[] TimeComponent) {
		// 1. Clear the status
		this.clearStatus();
		// 2. Search
		// 2.1 Search the FGPTree
		this.FGPTree.subgraphs(query, TimeComponent);

		long startTime = System.currentTimeMillis();
		List<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < allNodes.length; i++) {
			// on-line shedding
			if (visitedStatus[i] == true)
				continue;
			// search
			CRGraphNode aNode = allNodes[i];
			int prefix = aNode.getPrefixID();

			if (prefix == -1) {
				// Run subgrpah isomorphism test between theNode and the query
				FastSUCompleteEmbedding newEmbs = new FastSUCompleteEmbedding(
						aNode.getGraphLabel(), query);
				if (newEmbs.issubIsomorphic())
					results.add(i);
				else
					this.onLineSheding(aNode);
			} else {
				FastSUCompleteEmbedding embes = FGPTree.getEmbedding(prefix,
						query);
				if (embes == null)
					continue; // no subgraph isomorphic
				else {
					FastSUCompleteEmbedding newEmbs = null;
					// prefix feature is the same as this feature
					if (aNode.getGraphLabel().length == 0) {
						newEmbs = embes;
					} else
						newEmbs = new FastSUCompleteEmbedding(embes,
								aNode.getGraphLabel());

					if (newEmbs.issubIsomorphic())
						results.add(i);
					else
						this.onLineSheding(aNode); // aNode is not subgraph
													// isomorphic to the query,
													// none of its children will
				}
			}

		}
		TimeComponent[2] += System.currentTimeMillis() - startTime;
		return results;
	}

	/**
	 * Return the Minimum Non Subgraph of the Query q Since the CRGraphs are
	 * ordered
	 * 
	 * @param query
	 * @param TimeComponent
	 * @return
	 */
	/*
	 * public List<Integer> getNonSubgraphs(Graph query, long[] TimeComponent){
	 * List<Integer> result= new ArrayList<Integer>(); //1.Search for subgraphs
	 * List<Integer> subgraphs = this.subgraphs(query, TimeComponent); long
	 * startTime = System.currentTimeMillis(); Collections.sort(subgraphs); //2.
	 * Get the complimentary set for(int i = 0, j = 0; i< this.allNodes.length;
	 * i++){ if(j >= subgraphs.size()) result.add(i); else if( i ==
	 * subgraphs.get(j)){ j++; continue; } else if( i < subgraphs.get(j))
	 * result.add(i); else
	 * System.out.println("Error in getNoSubgraphs of CRGraphSearcher"); }
	 * TimeComponent[2] += System.currentTimeMillis()-startTime; return result;
	 * }
	 */
	public List<Integer> getNonSubgraphs(Graph query, long[] TimeComponent) {
		// 1. Clear the status
		this.clearStatus();
		// 2. Search
		// 2.1 Search the FGPTree
		this.FGPTree.subgraphs(query, TimeComponent);
		long startTime = System.currentTimeMillis();
		List<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < allNodes.length; i++) {
			// on-line shedding
			if (visitedStatus[i] == true)
				continue;
			// search
			CRGraphNode aNode = allNodes[i];
			int prefix = aNode.getPrefixID();

			FastSUCompleteEmbedding embes = null;
			if (prefix >= 0)
				embes = FGPTree.getEmbedding(prefix, query);

			if (embes == null) {
				// Run subgrpah isomorphism test between theNode and the query
				if (aNode.getGraphLabel() == null
						|| aNode.getGraphLabel().length == 0) {
					results.add(i);
					this.onLineSheding(aNode); // aNode is not subgraph
												// isomorphic to the query, none
												// of its children will
				} else {
					FastSUCompleteEmbedding newEmbs = new FastSUCompleteEmbedding(
							aNode.getGraphLabel(), query);
					if (newEmbs.issubIsomorphic())
						continue; // ignore subgraphs contained in the query
					else {
						results.add(i);
						this.onLineSheding(aNode);
					}
				}
			} else {
				FastSUCompleteEmbedding newEmbs = null;
				// prefix feature is the same as this feature
				if (aNode.getGraphLabel() == null
						|| aNode.getGraphLabel().length == 0)
					newEmbs = embes;
				else
					newEmbs = new FastSUCompleteEmbedding(embes,
							aNode.getGraphLabel());

				if (newEmbs.issubIsomorphic())
					continue;
				else {
					results.add(i);
					this.onLineSheding(aNode); // aNode is not subgraph
												// isomorphic to the query, none
												// of its children will
				}
			}
		}
		TimeComponent[2] += System.currentTimeMillis() - startTime;
		return results;
	}

	/**
	 * Mark all the visitedStatus[] of seeds descendants (including seed) to be
	 * true
	 * 
	 * @param visitedStatus
	 * @param seed
	 */
	private void onLineSheding(CRGraphNode seed) {
		visitedStatus[seed.getNodeID()] = true;
		CRGraphNode[] children = seed.getChildren();
		if (children == null || children.length == 0)
			return;

		for (int i = 0; i < children.length; i++) {
			CRGraphNode oneC = children[i];
			if (visitedStatus[oneC.getNodeID()])
				continue;
			else
				onLineSheding(oneC);
		}
	}

	public int getFeatureCount() {
		return this.allNodes.length;
	}

	@Override
	public int[] getAllFeatureIDs() {
		int[] rest = new int[this.allNodes.length];
		for (int i = 0; i < rest.length; i++)
			rest[i] = i;
		return rest;
	}

}
