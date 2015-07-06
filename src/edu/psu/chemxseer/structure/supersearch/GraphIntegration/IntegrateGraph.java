package edu.psu.chemxseer.structure.supersearch.GraphIntegration;

import java.util.HashMap;

import de.parmol.graph.Graph;
import de.parmol.graph.MutableGraph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * The inner representation of the integrated graphs Each edge is associated
 * with a list of gIDs, containing those edge The graph will change dynamically,
 * therefore, an matrix representaiton my not be appropriate, We use a linked
 * list representation only
 * 
 * @author dayuyuan
 * 
 */
public class IntegrateGraph {
	protected MutableGraph graph;
	protected int[][] edgeContainedGIds;
	protected int[] edgeFreq;

	protected HashMap<OneEdge, Integer> headTable;

	public IntegrateGraph(Graph g) {
		this.graph = MyFactory.getGraphFactory().createGraph();
		for (int node = 0; node < g.getNodeCount(); node++)
			graph.addNode(g.getNodeLabel(node));

		this.edgeFreq = new int[g.getEdgeCount() * 2];
		this.edgeContainedGIds = new int[edgeFreq.length][];
		this.headTable = new HashMap<OneEdge, Integer>();

		for (int edge = 0; edge < g.getEdgeCount(); edge++) {
			int nodeA = g.getNodeA(edge);
			int nodeB = g.getNodeB(edge);
			int bigEdge = graph.addEdge(nodeA, nodeB, g.getEdgeLabel(edge));
			OneEdge bigOneEdge = new OneEdge(nodeA, nodeB, bigEdge,
					g.getNodeLabel(nodeA), g.getNodeLabel(nodeB),
					g.getEdgeLabel(edge));
			this.addGraphContainingEdge(bigOneEdge, g.getID());
		}

	}

	/**
	 * Update Frequency & edgeContainedGIds & EdgeLabels
	 * 
	 * @param eID
	 * @param g
	 */
	public void addGraphContainingEdge(OneEdge aEdge, int gID) {
		// 1. Update the edgeFreq & edgeContainedGIds
		int eID = aEdge.getEdgeID();
		if (eID >= edgeFreq.length) {
			int[][] tempEdgeContainedGIds = new int[edgeFreq.length * 2][];
			int[] tempEdgeFreq = new int[tempEdgeContainedGIds.length];
			for (int w = 0; w < edgeFreq.length; w++) {
				tempEdgeContainedGIds[w] = edgeContainedGIds[w];
				tempEdgeFreq[w] = edgeFreq[w];
			}
			for (int w = edgeFreq.length; w < tempEdgeFreq.length; w++)
				tempEdgeFreq[w] = 0;

			edgeContainedGIds = tempEdgeContainedGIds;
			edgeFreq = tempEdgeFreq;
		}

		if (this.edgeFreq[eID] == 0) {
			edgeContainedGIds[eID] = new int[4];
		} else if (this.edgeFreq[eID] == edgeContainedGIds[eID].length) {
			int[] temp = new int[edgeContainedGIds[eID].length * 2];
			for (int i = 0; i < edgeContainedGIds[eID].length; i++)
				temp[i] = edgeContainedGIds[eID][i];
			this.edgeContainedGIds[eID] = temp;
		}

		edgeContainedGIds[eID][edgeFreq[eID]++] = gID;

		// 2. update the headTable
		Integer freq = this.headTable.get(aEdge);
		if (freq == null)
			headTable.put(aEdge, 1);
		else if (freq < edgeFreq[eID])
			headTable.put(aEdge, freq);

	}

	public Integer getHeaderTableFreq(OneEdge aEdge) {
		return this.headTable.get(aEdge);
	}

	public int getNodeCount() {
		return this.graph.getNodeCount();
	}

	public int addNode(int nodeLabel) {
		return this.graph.addNode(nodeLabel);
	}

	public int getEdge(int bigNodeA, int bigNodeB) {
		return this.graph.getEdge(bigNodeA, bigNodeB);
	}

	public int addEdge(int bigNodeA, int bigNodeB, int edgeLabel) {
		return this.graph.addEdge(bigNodeA, bigNodeB, edgeLabel);
	}

	public int getDegree(int node) {
		return this.graph.getDegree(node);
	}

	public int getNodeEdge(int bigAdjNewNode, int bw) {
		return this.graph.getNodeEdge(bigAdjNewNode, bw);
	}

	public int getOtherNode(int bigEdge, int bigAdjNewNode) {
		return this.graph.getOtherNode(bigEdge, bigAdjNewNode);
	}

	public int getNodeLabel(int bigNewNode) {
		return this.graph.getNodeLabel(bigNewNode);
	}

	public int getEdgeLabel(int bigEdge) {
		return this.graph.getEdgeLabel(bigEdge);
	}

	public int getEdgeFreq(int bigEdge) {
		return this.edgeFreq[bigEdge];
	}

	public Graph getGraph() {
		this.graph.saveMemory();
		return this.graph;
	}

}
