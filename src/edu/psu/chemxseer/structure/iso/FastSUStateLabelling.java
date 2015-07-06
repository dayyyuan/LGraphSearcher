package edu.psu.chemxseer.structure.iso;

import java.util.LinkedList;
import java.util.Queue;

import de.parmol.graph.Graph;

/**
 * FastSUStateLabelling is one extension of FastSUState It support relabel the
 * big graph as an extension of the small graph if small subgraph is subgraph
 * isomorphic to the big graph After extension relabeling, it can further
 * permuted inner representation of the big graph. This permuted big graph can
 * be further utilized when convert a larger graph as an extension of the big
 * graph
 * 
 * @author dayuyuan
 * 
 */
public class FastSUStateLabelling extends FastSUState {

	protected int[][] extension;
	protected int[] order;
	protected int edgeNumS;
	protected int edgeNumB;

	public FastSUStateLabelling(FastSUState state) {
		super(state);
		edgeNumS = state.getEdgeCountS();
		edgeNumB = state.getEdgeCountB();
		extension = new int[edgeNumB - edgeNumS][5];
		order = new int[nodeCountB];
	}

	public FastSUStateLabelling(Graph small, Graph big) {
		super(small, big);
		edgeNumS = small.getEdgeCount();
		edgeNumB = big.getEdgeCount();
		extension = new int[edgeNumB - edgeNumS][5];
		order = new int[nodeCountB];
	}

	public FastSUStateLabelling(int[][] labels, Graph large) {
		nodeCountS = 0;
		for (int i = 0; i < labels.length; i++) {
			if (labels[i][0] > nodeCountS)
				nodeCountS = labels[i][0];
			if (labels[i][1] > nodeCountS)
				nodeCountS = labels[i][1];
		}
		nodeCountS++;

		nodeCountB = large.getNodeCount();
		// Initial internal representation of graph small and graph big
		graphInitial(labels, large);
		datamemberInitial();
		this.findInitialPair();
		edgeNumS = labels.length;
		edgeNumB = large.getEdgeCount();
		extension = new int[edgeNumB - edgeNumS][5];
		order = new int[nodeCountB];
	}

	public FastSUStateLabelling(int[][] small, int[][] big) {
		nodeCountS = 0;
		for (int i = 0; i < small.length; i++) {
			if (small[i][0] > nodeCountS)
				nodeCountS = small[i][0];
			if (small[i][1] > nodeCountS)
				nodeCountS = small[i][1];
		}
		nodeCountS++;

		nodeCountB = 0;
		for (int i = 0; i < big.length; i++) {
			if (big[i][0] > nodeCountB)
				nodeCountB = big[i][0];
			if (big[i][1] > nodeCountB)
				nodeCountB = big[i][1];
		}
		nodeCountB++;

		// Initial internal representation of graph small and graph big
		graphInitial(small, big);
		datamemberInitial();
		this.findInitialPair();
		edgeNumS = small.length;
		edgeNumB = big.length;
		extension = new int[edgeNumB - edgeNumS][5];
		order = new int[nodeCountB];
	}

	/**
	 * Given a relabelled isomorphism between graph small and graph big now try
	 * to relabel graph large as an isomorphic extension to graph big
	 */
	public FastSUStateLabelling(FastSUStateLabelling oneIsomorphism, Graph large) {
		nodeCountS = oneIsomorphism.nodeCountB;
		nodeCountB = large.getNodeCount();
		// Initial internal representation of graph small and graph big
		graphInitial(oneIsomorphism.verticesB.clone(),
				oneIsomorphism.connectivityB.clone(), large);
		datamemberInitial();
		this.findInitialPair();
		edgeNumS = oneIsomorphism.edgeNumB;
		edgeNumB = large.getEdgeCount();
		extension = new int[edgeNumB - edgeNumS][5];
		order = new int[nodeCountB];
	}

	/**
	 * 
	 * @param oneIsomorphism
	 *            : the isomorphism between supper small graph & small graph
	 * @param labels
	 *            : label for the large graph
	 */
	public FastSUStateLabelling(FastSUStateLabelling oneIsomorphism, int[][] big) {
		nodeCountS = oneIsomorphism.nodeCountB;
		nodeCountB = 0;
		for (int i = 0; i < big.length; i++) {
			if (big[i][0] > nodeCountB)
				nodeCountB = big[i][0];
			if (big[i][1] > nodeCountB)
				nodeCountB = big[i][1];
		}
		nodeCountB++;
		// Initial internal representation of graph small and graph big
		graphInitial(oneIsomorphism.verticesB.clone(),
				oneIsomorphism.connectivityB.clone(), big);
		datamemberInitial();
		this.findInitialPair();
		edgeNumS = oneIsomorphism.edgeNumB;
		edgeNumB = big.length;
		extension = new int[edgeNumB - edgeNumS][5];
		order = new int[nodeCountB];
	}

	protected void graphInitial(int[][] vertices, int[][] connectivity,
			Graph big) {
		this.verticesS = vertices;
		this.connectivityS = connectivity;

		this.verticesB = new int[nodeCountB][];
		this.connectivityB = new int[nodeCountB][nodeCountB];

		for (int i = 0; i < nodeCountB; i++) {
			verticesB[i] = new int[big.getDegree(i)];
			for (int temp = 0; temp < nodeCountB; temp++)
				this.connectivityB[i][temp] = NULL_EDGE;

			connectivityB[i][i] = big.getNodeLabel(i);
			for (int j = 0; j < verticesB[i].length; j++) {
				int edge = big.getNodeEdge(i, j);
				int anotherNode = big.getNodeB(edge);
				if (anotherNode == i)
					anotherNode = big.getNodeA(edge);
				connectivityB[i][anotherNode] = big.getEdgeLabel(edge);
				verticesB[i][j] = anotherNode;
			}
		}
	}

	protected void graphInitial(int[][] vertices, int[][] connectivity,
			int[][] largeLabels) {
		this.verticesS = vertices;
		this.connectivityS = connectivity;

		this.verticesB = new int[nodeCountB][];
		this.connectivityB = new int[nodeCountB][nodeCountB];

		for (int i = 0; i < nodeCountB; i++) {
			for (int j = 0; j < nodeCountB; j++)
				this.connectivityB[i][j] = NULL_EDGE;
		}
		for (int i = 0; i < largeLabels.length; i++) {
			int nodeA = largeLabels[i][0];
			int nodeB = largeLabels[i][1];
			this.connectivityB[nodeA][nodeA] = largeLabels[i][2];
			this.connectivityB[nodeA][nodeB] = largeLabels[i][3];
			this.connectivityB[nodeB][nodeA] = largeLabels[i][3];
			this.connectivityB[nodeB][nodeB] = largeLabels[i][4];
		}
		for (int i = 0; i < nodeCountB; i++) {
			int count = 0;
			for (int j = 0; j < nodeCountB; j++)
				if (j == i)
					continue;
				else if (this.connectivityB[i][j] != NULL_EDGE)
					count++;
			this.verticesB[i] = new int[count];
			for (int j = 0, iter = 0; j < nodeCountB; j++)
				if (j == i)
					continue;
				else if (this.connectivityB[i][j] != NULL_EDGE)
					this.verticesB[i][iter++] = j;
		}

	}

	/**
	 * Relabel Graph B as an extension of Graph S Save those extension into
	 * extension return
	 * 
	 * @param extension
	 * @return
	 */
	protected void relabelGraphB() {
		// coreS, CoreB records the mapping between small and big
		int[] layer = new int[nodeCountB];
		for (int nodeB = 0; nodeB < nodeCountB; nodeB++)
			if (coreB[nodeB] != NULL_NODE) {
				layer[nodeB] = 0;// layer zero
				order[nodeB] = coreB[nodeB];
			} else {
				layer[nodeB] = NULL_NODE;
				order[nodeB] = NULL_NODE;
			}

		// reorder unmatched nodes in graphB
		// First : find all nodes that are of layer one, save into queue
		Queue<Integer> queue = new LinkedList<Integer>();
		int extensionIndex = 0;
		int verticesIndex = nodeCountS;
		for (int nodeB = 0; nodeB < nodeCountB; nodeB++) {
			if (coreB[nodeB] == NULL_NODE)
				continue;// Unmatched nodeB dose not count
			for (int j = 0; j < verticesB[nodeB].length; j++) {
				int adjNodeB = verticesB[nodeB][j];
				if (layer[adjNodeB] == NULL_NODE)// Find an adjacent node that
													// are not in small graph
				{
					layer[adjNodeB] = 1;
					order[adjNodeB] = verticesIndex++;
					queue.offer(adjNodeB);
				}
				if (layer[nodeB] < layer[adjNodeB]
						|| layer[nodeB] == layer[nodeB]
						&& order[nodeB] < order[adjNodeB]
						&& connectivityS[coreB[nodeB]][coreB[adjNodeB]] == NULL_NODE
						&& connectivityB[nodeB][adjNodeB] != NULL_NODE) {
					// 1, layer[nodeB] < layer [adjNodeB] 2, layer equals,
					// order[nodeB] < order[adjNodeB]
					extension[extensionIndex][0] = order[nodeB];
					extension[extensionIndex][1] = order[adjNodeB];
					extension[extensionIndex][2] = connectivityB[nodeB][nodeB];
					extension[extensionIndex][3] = connectivityB[nodeB][adjNodeB];
					extension[extensionIndex][4] = connectivityB[adjNodeB][adjNodeB];
					++extensionIndex;
				}
			}
		}
		// Then : iteratively find nodes of layer 2, layer3 and so on
		queue.offer(-1);// Now queue contains all first layer nodes + -1;
		int currentLayer = 1;
		while (!queue.isEmpty()) {
			int node = queue.poll();
			currentLayer++;
			while (node != -1) {
				for (int j = 0; j < verticesB[node].length; j++) {
					int adjNodeB = verticesB[node][j];
					if (layer[adjNodeB] == NULL_NODE) {
						layer[adjNodeB] = currentLayer;
						queue.offer(adjNodeB);
						order[adjNodeB] = verticesIndex++;
					}
					// layer[nodeB] < layer[adjNodeB]|| layer[nodeB] ==
					// layer[nodeB] && order[nodeB] < order[adjNodeB]
					// equals order[node] < order[adjNodeB]
					// since both node and adjNodeB are not in small graph, thus
					// connectivityS[ndoeB][adjNodeB] ==NULL_NODE
					// Without need of verification
					if (order[node] < order[adjNodeB]) {
						// 1, layer[nodeB] < layer [adjNodeB] 2, layer equals,
						// order[nodeB] < order[adjNodeB]
						extension[extensionIndex][0] = order[node];
						extension[extensionIndex][1] = order[adjNodeB];
						extension[extensionIndex][2] = connectivityB[node][node];
						extension[extensionIndex][3] = connectivityB[node][adjNodeB];
						extension[extensionIndex][4] = connectivityB[adjNodeB][adjNodeB];
						++extensionIndex;
					}
				}
				node = queue.poll();
			}
			if (!queue.isEmpty())
				queue.offer(-1);
		}
		// Finally relabel the extension into this.extension
		++extensionIndex;
	}

	protected void reOrderGraphB() {
		// Reorder graphB
		int[][] newVerticesB = new int[verticesB.length][];
		int[][] newConnectivityB = new int[verticesB.length][verticesB.length];
		for (int nodeB = 0; nodeB < nodeCountB; nodeB++) {
			int reorderB = order[nodeB];
			// System.out.println(reorderB + ", " + nodeB);
			newVerticesB[reorderB] = verticesB[nodeB];
			for (int t = 0; t < newConnectivityB.length; t++)
				newConnectivityB[reorderB][t] = -1;
			newConnectivityB[reorderB][reorderB] = connectivityB[nodeB][nodeB];
			for (int i = 0; i < verticesB[nodeB].length; i++) {
				int adjNodeB = verticesB[nodeB][i];
				newConnectivityB[reorderB][order[adjNodeB]] = connectivityB[nodeB][adjNodeB];
				newVerticesB[reorderB][i] = order[adjNodeB];
			}
		}
		this.verticesB = newVerticesB;
		this.connectivityB = newConnectivityB;
	}

	public String extensionToString() {
		StringBuffer sbuf = new StringBuffer(1024);
		for (int i = 0; i < extension.length; i++) {
			sbuf.append(extension[i][0]);
			sbuf.append(" ");
			sbuf.append(extension[i][1]);
			sbuf.append(" ");
			sbuf.append(extension[i][2]);
			sbuf.append(" ");
			sbuf.append(extension[i][3]);
			sbuf.append(" ");
			sbuf.append(extension[i][4]);
			sbuf.append(",");
		}
		sbuf.deleteCharAt(sbuf.length() - 1);
		return sbuf.toString();
	}

	public int[][] getExtension() {
		return extension.clone();
	}

	/**
	 * Return the mapping between the newGraphB and oldGraphB order[oldNodeID] =
	 * newNodeID;
	 * 
	 * @return
	 */
	public int[] getOrder() {
		return this.order.clone();
	}

	/**
	 * For all the nodes in extension2, rebase them with ID order[ordID]
	 * 
	 * @param extension2
	 * @param order2
	 * @return
	 */
	public static int[][] rebase(int[][] extension2, int[] order2) {
		int[][] results = extension2.clone();
		for (int i = 0; i < results.length; i++) {
			for (int j = 0; j < 2; j++)
				if (results[i][j] < order2.length)
					results[i][j] = order2[results[i][j]];
		}
		return results;
	}
}
