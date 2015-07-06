package edu.psu.chemxseer.structure.subsearch.Lindex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSUExt;
import edu.psu.chemxseer.structure.iso.FastSUState;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.util.subgen.SubgraphGenerator3;

/**
 * Capable of finding "direct" or "indirected" attachment
 * 
 * @author dayuyuan
 * 
 */
public class LindexSearcherAdv extends LindexSearcher {
	protected int maxEdgeNum;
	protected Set<String> termLabels;

	public LindexSearcherAdv(LindexSearcher searcher) {
		super(searcher);
		this.maxEdgeNum = -1;
		this.termLabels = new HashSet<String>();
		// TODO: optimize
		// This is far from optimized: should use dynamic programming
		// But here for simplicity, use exhuastive search
		for (int i = 0; i < this.indexTerms.length; i++) {
			Graph g = MyFactory.getDFSCoder().parse(
					this.getTermFullLabel(indexTerms[i]),
					MyFactory.getGraphFactory());
			termLabels.add(MyFactory.getDFSCoder().serialize(g));
		}
	}

	/**
	 * Given all the subGraph features contained in the query, find and return
	 * the set of features directly connected with the query (q is in the direct
	 * value set of f)
	 * 
	 * @param query
	 * @param allSubIds
	 * @param candidateFeatures
	 * @param canonicalLabels
	 * @return
	 */
	public Set<Integer> getDirectFeatures(Graph query, List<Integer> allSubIds) {
		Set<Integer> directFeatures = new HashSet<Integer>();

		long[] TimeComponent = new long[4];
		List<Integer> allMaximalIds = this.maxSubgraphs(query, TimeComponent);

		/*
		 * if(allMaximalIds == null){ System.out.println("lala"); }
		 */

		if (allMaximalIds != null && allMaximalIds.get(0) == -1) {
			directFeatures.add(allMaximalIds.get(1));
			return directFeatures;
		} else
			for (int i = 0; i < allMaximalIds.size(); i++)
				directFeatures.add(allMaximalIds.get(i));

		for (int i = 0; i < allSubIds.size(); i++) {
			if (directFeatures.contains(allSubIds.get(i)))
				continue; // already directed connected
			// else
			Graph indexTermGraph = this.getTermGraph(allSubIds.get(i));
			if (query.getEdgeCount() <= indexTermGraph.getEdgeCount() + 1
					&& query.getNodeCount() <= indexTermGraph.getNodeCount() + 1)
				directFeatures.add(allSubIds.get(i)); // Verfied without the
														// need of direct test
			else if (this.directTest(indexTermGraph, query,
					this.indexTerms[allSubIds.get(i)], this.getMaxEdgeNum(),
					termLabels))
				directFeatures.add(allSubIds.get(i));
		}
		return directFeatures;
	}

	private int getMaxEdgeNum() {
		if (maxEdgeNum != -1)
			return this.maxEdgeNum;
		else {
			// For all Leaf node, find its full label
			for (int i = 0; i < this.indexTerms.length; i++) {
				if (this.indexTerms[i].getChildren() == null
						|| this.indexTerms[i].getChildren().length == 0) {
					// For each leaf node
					int[][] labels = this.getTermFullLabel(this.indexTerms[i]);
					if (this.maxEdgeNum < labels.length)
						this.maxEdgeNum = labels.length;
				}
			}
			return this.maxEdgeNum;
		}
	}

	public boolean directTest(Graph termGraph, Graph targetGraph,
			LindexTerm theTerm, int maxEdge, Set<String> termLabels) {

		LindexTerm[] children = theTerm.getChildren();
		if (children == null || children.length == 0)
			return true; // the big graph in fastSu is direct to theTerm
		// Step to take:
		// 1. First find the complete mapping between termGraph and the target
		// graph
		FastSUExt fastSu = new FastSUExt();
		FastSUState fastSuState = fastSu.completeEmbedding(termGraph,
				targetGraph);
		if (fastSu.getMapNumber() == 0) {
			System.out.println("YDY: It is wired in direct Test");
		} else {
			int[][] mappings = fastSu.getMappings();
			// 2. Second starting from each mapping, grows the whole set of
			// subgraphs that
			// are between termGraph and the target graph
			int[][] connectivityBackUp = null;
			int[][] verticesBackUp = null;
			for (int i = 0; i < mappings.length; i++) {
				if (i == 0) {
					// back up
					connectivityBackUp = fastSuState.getBigMatrix().clone();
					verticesBackUp = fastSuState.getBigLinkList().clone();
				} else {
					// restore to previous value
					fastSuState.setBigMatrix(connectivityBackUp);
					fastSuState.setBigLinkList(verticesBackUp);
				}
				fastSuState.replaceMappingBase(mappings[i], mappings[i].length);
				fastSuState.reOrderBaseGraphB();
				// The maximum subgraph return has edge: maxEdge + 1;
				SubgraphGenerator3 subgraphsGenerator = new SubgraphGenerator3(
						fastSuState, maxEdge + 2);
				int subgraphEdgeNum = 0;
				int subgraphNodeNum = 0;
				int[] edgeNodeNum = new int[2];
				edgeNodeNum[0] = subgraphEdgeNum;
				edgeNodeNum[1] = subgraphNodeNum;
				String subString = subgraphsGenerator.nextSubgraph(edgeNodeNum);

				while (subString != null) {
					if (termLabels.contains(subString)) {
						subgraphsGenerator.earlyPruning();
					}
					// this subString does not hit on any index terms
					else if (edgeNodeNum[0] > maxEdge
							|| edgeNodeNum[0] == targetGraph.getEdgeCount() - 1)
						return true; // there is a path from termGraph to this
										// subString
					// else keep on growing
					subString = subgraphsGenerator.nextSubgraph(edgeNodeNum);
				}
			}
		}
		// Else: every subgraphs of TargetGraph is earlyPruned, thus return
		// false
		return false;
	}

	private Graph getTermGraph(int termId) {
		int[][] fullLabel = this.getTermFullLabel(this.indexTerms[termId]);
		return MyFactory.getDFSCoder().parse(fullLabel,
				MyFactory.getGraphFactory());
	}

	@Override
	public int getFeatureCount() {
		return this.indexTerms.length;
	}

}
