package edu.psu.chemxseer.structure.util.subgen;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.graph.MutableGraph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.util.Leg;

/**
 * Another implementation of SubgraphGenetor1, This version is more advanced
 * 
 * @author dyuan
 * 
 */
public class SubgraphGenerator2 {
	/* Inner representation of query graph */
	private int[][] vertices; // Linked List representation of g,
								// vertices[nodeI][j]= nodeJ
	private int[][] connectivity;
	// Matrix representation of g, connectivity[nodeI][nodeJ] = edge(nodeI,
	// nodeJ);
	private int[] edgeLabel; // edgeLabel(edge) = label;

	/* Inner representation of subgraph */
	private int[][] subVertices; // Linked List representation of subgraph
	private int[] subgraphNodesDegree;
	// subgraphNodesDegree[nodeI] = -1, not in subgraph
	// subgraphNodesDegree[nodeI] = degree(nodeI) in subgraph

	/* private int[][] subConnectivity; */
	private int subgraphNodeNum, subgraphEdgeNum;

	// candidates legs that can be added to extend subgraph
	private Queue<Leg>[] candidates;
	private Leg[] historyRecord;
	private int candidateValidIndex;
	private int maximumSubgraphDepth;
	private boolean seedsInitialized;

	public SubgraphGenerator2(Graph g, int maxDepth) {
		internalGraphInitialization(g);
		recordInitialization(maxDepth + 1);
	}

	/**
	 * Initialization Initial Representation of graph g
	 * 
	 * @param g
	 */
	private void internalGraphInitialization(Graph g) {
		int nodeCount = g.getNodeCount();
		this.vertices = new int[nodeCount][];
		this.connectivity = new int[nodeCount][nodeCount];
		// this.edges = new int[g.getEdgeCount()][2];
		this.edgeLabel = new int[g.getEdgeCount()];
		for (int i = 0; i < nodeCount; i++) {
			vertices[i] = new int[g.getDegree(i)];
			for (int temp = 0; temp < nodeCount; temp++)
				this.connectivity[i][temp] = -1;
			this.connectivity[i][i] = g.getNodeLabel(i);

			for (int j = 0; j < vertices[i].length; j++) {
				int edge = g.getNodeEdge(i, j);
				int anotherNode = g.getOtherNode(edge, i);
				if (i < anotherNode) {
					edgeLabel[edge] = g.getEdgeLabel(edge);
				} // Because of symmetry, we skip the assignment of edges array
					// when i > anotherNode
				connectivity[i][anotherNode] = edge;
				vertices[i][j] = anotherNode;
			}
		}
	}

	/**
	 * Initialization of Internal data structure
	 * 
	 * @param maxDepth
	 */
	@SuppressWarnings("unchecked")
	private void recordInitialization(int maxDepth) {
		if (maxDepth < this.edgeLabel.length)
			this.maximumSubgraphDepth = maxDepth;
		else
			this.maximumSubgraphDepth = edgeLabel.length;
		subVertices = new int[vertices.length][];
		for (int i = 0; i < subVertices.length; i++) {
			subVertices[i] = new int[vertices[i].length];
			for (int j = 0; j < subVertices[i].length; j++)
				subVertices[i][j] = -1;
		}
		subgraphNodesDegree = new int[vertices.length];
		for (int i = 0; i < subgraphNodesDegree.length; i++)
			subgraphNodesDegree[i] = -1;

		candidates = new Queue[this.maximumSubgraphDepth + 1];
		for (int i = 0; i < candidates.length; i++)
			candidates[i] = new LinkedList<Leg>();

		candidateValidIndex = -1;
		historyRecord = new Leg[maximumSubgraphDepth];
		subgraphNodeNum = 0;
		subgraphEdgeNum = 0;
		seedsInitialized = false;
	}

	public String nextSubgraph() {
		if (!seedsInitialized)
			seeds();
		long time = System.currentTimeMillis();
		boolean succeed = depthFirstSearch(time);
		if (!succeed)
			return null;

		// Write the subgraph in a matrix representation: subConnectivity, &
		// subEdges
		int[] subSuperNodeMap = new int[vertices.length];// subSuperEdgeMap[superNode]
															// = subNode
		for (int i = 0; i < subSuperNodeMap.length; i++)
			subSuperNodeMap[i] = -1;
		int[] subEdgeLabel = new int[this.subgraphEdgeNum];
		for (int i = 0; i < subEdgeLabel.length; i++)
			subEdgeLabel[i] = -1;

		int subNodesNum = 0;// Number of nodes in subgraph
		for (int superIndex = 0; superIndex < subgraphNodesDegree.length; superIndex++) {
			if (subgraphNodesDegree[superIndex] != -1) {
				subSuperNodeMap[superIndex] = subNodesNum;
				subNodesNum++;
			}
		}
		int[][] subConnectivity = new int[subNodesNum][subNodesNum];
		for (int i = 0; i < subConnectivity.length; i++)
			for (int j = 0; j < subConnectivity.length; j++)
				subConnectivity[i][j] = -1;
		int subEdge = 0;
		for (int superNodeI = 0; superNodeI < subgraphNodesDegree.length; superNodeI++) {
			if (subgraphNodesDegree[superNodeI] != -1) {
				int subNodeI = subSuperNodeMap[superNodeI];
				// Assign node label in subConnectivity
				subConnectivity[subNodeI][subNodeI] = connectivity[superNodeI][superNodeI];
				for (int j = 0; j < subgraphNodesDegree[superNodeI]; j++) {
					int superNodeJ = subVertices[superNodeI][j];
					if (superNodeJ < superNodeI)
						continue; // make sure of the symmetric: each edge is
									// only visited once
					subEdgeLabel[subEdge] = edgeLabel[connectivity[superNodeI][superNodeJ]];
					int subNodeJ = subSuperNodeMap[superNodeJ];
					subConnectivity[subNodeI][subNodeJ] = subEdge;
					subConnectivity[subNodeJ][subNodeI] = subEdge;
					// make sure of the symmetric: both connectivity[i][j] and
					// connectivity[j][i] has to be assigned
					++subEdge;
				}
			}
		}

		return MyFactory.getDFSCoder().serialize(subConnectivity, subEdgeLabel);
	}

	public Graph nextSubgraphG() {
		if (!seedsInitialized)
			seeds();
		boolean succeed = depthFirstSearch(System.currentTimeMillis());
		if (!succeed)
			return null;
		if (this.candidateValidIndex == 0)
			return null;
		GraphFactory factory = MyFactory.getGraphFactory();
		MutableGraph g;
		g = factory.createGraph();
		Leg oneLeg = null;
		int nodeMapping[] = new int[vertices.length];
		for (int i = 0; i < nodeMapping.length; i++)
			nodeMapping[i] = -1;
		for (int i = 0; i < candidateValidIndex; i++) {
			oneLeg = historyRecord[i];
			if (oneLeg.isNodeOnlyExtension()) {
				int firstNode = oneLeg.getNode();
				int node = g.addNode(connectivity[firstNode][firstNode]);
				nodeMapping[firstNode] = node;
			} else if (oneLeg.isNodeExtension()) {
				int oldNode = oneLeg.getAlreadyInNode();
				int newNode = oneLeg.getNewNode();
				int node = g.addNodeAndEdge(nodeMapping[oldNode],
						connectivity[newNode][newNode],
						edgeLabel[connectivity[oldNode][newNode]]);
				nodeMapping[newNode] = node;
			} else {
				int nodeA = oneLeg.getNodeA();
				int nodeB = oneLeg.getNodeB();
				g.addEdge(nodeMapping[nodeA], nodeMapping[nodeB],
						edgeLabel[connectivity[nodeA][nodeB]]);
			}
		}
		g.saveMemory();
		return g;

	}

	public boolean earlyPruning() {
		restoreSubgraph();
		return true;
	}

	/**
	 * Before depth first search, subgraph is a empty set possible extension is
	 * a node extension, changing empty subgraph to a one node subgraph.
	 */
	private void seeds() {
		// Sort each nodes according to their index number
		// and then add these nodes as seed in this.candidates
		// These seeds are node only extension
		// System.out.println("YDY: In Seeds");
		candidates[0].clear();
		for (int i = 0; i < vertices.length; i++)
			candidates[0].offer(new Leg(i));
		candidateValidIndex = 0;
		seedsInitialized = true;
	}

	/**
	 * The process of subgraphs generation is actually a depth first search of a
	 * subgraph generation tree In each step, one node + edge or only one edge
	 * is added, generating a new subgraph, and produces a set of possible
	 * candidates.
	 */
	private boolean depthFirstSearch(long timeInitial) {
		long time = System.currentTimeMillis();
		if (timeInitial - time > 10000)
			return false;
		// this candidates not actually valid to maximumSubgraphDepth
		// Only grows candidatValidIndex to maximumSubgraphDepth because
		// subgraph reaches the upper bound of its size
		if (candidateValidIndex == this.maximumSubgraphDepth) {
			// trace back
			restoreSubgraph();
			return depthFirstSearch(timeInitial);
		}
		// find the most recent candidate
		// System.out.println("YDY: depth " + candidateValidIndex);
		if (candidates[candidateValidIndex].isEmpty()) {
			if (candidateValidIndex == 0)
				return false;// no further track back
			else {
				restoreSubgraph();
				return depthFirstSearch(timeInitial);
			}
		}
		Leg oneLeg = candidates[candidateValidIndex].poll();
		// 1. do extension
		extendSubgraph(oneLeg);
		// 2. find candidates legs
		addExtensionCandidate();
		return true;// extension succeed
	}

	/**
	 * Extend one leg on subgraph, checking the validity of oneLeg
	 * 
	 * @param oneLeg
	 * @return
	 */
	private void extendSubgraph(Leg oneLeg) {
		// For node only extension:
		// only add this node into subgraph
		if (oneLeg.isNodeOnlyExtension()) {
			// System.out.println("YDY:NodeOnlyExtension");
			if (subgraphNodesDegree[oneLeg.getNode()] != -1)
				System.out.println("YDY:Error in NodeOnlyExtension");
			addNewNode(oneLeg.getNode());
			historyRecord[candidateValidIndex] = oneLeg;
		}
		// For node+edge extension
		// add node into subgraph, add edge into subgraph
		else if (oneLeg.isNodeExtension()) {
			// System.out.println("YDY:Node&EdgeExtension");
			if (subgraphNodesDegree[oneLeg.getAlreadyInNode()] == -1
					|| subgraphNodesDegree[oneLeg.getNewNode()] != -1)
				System.out.println("YDY:Error in Node&Edge Extension");
			// Add the new Node
			addNewNode(oneLeg.getNewNode());
			addSubgraphEdge(oneLeg.getAlreadyInNode(), oneLeg.getNewNode());
			historyRecord[candidateValidIndex] = oneLeg;
		}
		// For edge extension, add edge into subgraph
		else {
			// System.out.println("YDY:EdgeOnlyExtension");
			if (subgraphNodesDegree[oneLeg.getNodeA()] == -1
					|| subgraphNodesDegree[oneLeg.getNodeB()] == -1)
				System.out.println("YDY:Error in EdgeOnlyExtension");
			addSubgraphEdge(oneLeg.getNodeA(), oneLeg.getNodeB());
			historyRecord[candidateValidIndex] = oneLeg;
		}
	}

	private void addNewNode(int node) {
		// Set subgraphNodes added selected
		subgraphNodesDegree[node] = 0;
		subgraphNodeNum++;
	}

	private void addSubgraphEdge(int nodeA, int nodeB) {
		// The edge you tried to add do already exist
		if (subVertices[nodeA][subgraphNodesDegree[nodeA]] != -1)
			System.out.println("YDY:Error in AddSubgraphEdge 1");
		else if (subVertices[nodeB][subgraphNodesDegree[nodeB]] != -1)
			System.out.println("YDY:Error in AddSubgraphEdge 2");

		subVertices[nodeA][subgraphNodesDegree[nodeA]] = nodeB;
		subVertices[nodeB][subgraphNodesDegree[nodeB]] = nodeA;
		++subgraphNodesDegree[nodeA];
		++subgraphNodesDegree[nodeB];
		++subgraphEdgeNum;
	}

	private void removeSubgraphEdge(int nodeA, int nodeB) {
		// The edge you tried to remove do not exist
		if (subVertices[nodeA][subgraphNodesDegree[nodeA] - 1] == -1)
			System.out.println("YDY: Error in Removing Subgraph Edge 1");
		else if (subVertices[nodeB][subgraphNodesDegree[nodeB] - 1] == -1)
			System.out.println("YDY: Error in Removing subgraph Edge 2");

		subVertices[nodeA][subgraphNodesDegree[nodeA] - 1] = -1;
		subVertices[nodeB][subgraphNodesDegree[nodeB] - 1] = -1;
		--subgraphNodesDegree[nodeA];
		--subgraphNodesDegree[nodeB];
		--subgraphEdgeNum;
	}

	/**
	 * Find extension candidates legs of current subgraphs, then add these legs
	 * into this.candidates
	 * 
	 * @return
	 */
	private void addExtensionCandidate() {
		// There is no need of further collecting extension candidates
		if (candidateValidIndex == this.maximumSubgraphDepth - 1) {
			// but still has to update candidateValidIndex
			candidateValidIndex++;// update candidateValidIndex
			return;
		}
		candidateValidIndex++;// update candidateValidIndex
		if (isSubgraphATree()) {// tree
			addNodeExtensionCandidate();
			addEdgeExtensionCandidate();
			// if(edge!=0)
			// System.out.println("In EXTENSION : " + "nodes: " + nodes +
			// " EDGES: "+edge);
		} else
			addEdgeExtensionCandidate();
	}

	/**
	 * Test whether subgraph is a tree or a loop containing graph.
	 * 
	 * @return
	 */
	private boolean isSubgraphATree() {
		if (subgraphNodeNum - 1 == subgraphEdgeNum)
			return true;
		else if (subgraphEdgeNum >= subgraphNodeNum)
			return false;
		else
			System.out
					.println("YDY: Error in isSubgraphAtree, inconsistant nodeNum: "
							+ subgraphNodeNum + "edgeNum: " + subgraphEdgeNum);
		return false;
	}

	/**
	 * Given current subgraph, find a node that is not in current subgraph but
	 * has maximum index of all leaf nodes after adding this node into subgraph
	 * 
	 * @return
	 */
	private int addNodeExtensionCandidate() {
		HashSet<Integer> connectingEdges = new HashSet<Integer>(10);
		// Find maximumIndex and secondMaxIndex of all leaf nodes of current
		// subgraph
		int maximumIndex = -1;
		int secondMaxIndex = -1;
		for (int nodeI = vertices.length - 1; nodeI >= 0; nodeI--) {
			// only interested in leaf node
			if (subgraphNodesDegree[nodeI] == 1
					|| subgraphNodesDegree[nodeI] == 0)
				if (maximumIndex == -1)
					maximumIndex = nodeI;
				else if (secondMaxIndex == -1)
					secondMaxIndex = nodeI;
				else
					// maximumIndex & secondIndex are both corrected assigned
					break;

		}
		// collect adjacent nodes of leaf nodes
		// A. unselected in subgraph
		// B. if connected with maximumIndex then it has to be larger than the
		// secondMaxIndex,
		// B(2). else has to be larger than the maximumIndex
		// C. for single vertex V graph, it candidates can be vertex adjacent to
		// V, and must have a index larger
		// than V
		for (int nodeI = vertices.length - 1; nodeI >= 0; nodeI--) {
			// One edge connecting leaf node
			if (subgraphNodesDegree[nodeI] >= 1) {
				for (int j = 0; j < vertices[nodeI].length; j++) {
					int nodeJ = vertices[nodeI][j];
					// rule A: only interested in adjacent nodes that are not in
					// subgraph yeet
					if (subgraphNodesDegree[nodeJ] == -1)
						// rule B(2) || rule B
						if (nodeJ > maximumIndex
								|| (nodeI == maximumIndex && nodeJ > secondMaxIndex))
							if (connectingEdges.add(connectivity[nodeI][nodeJ]))
								candidates[candidateValidIndex].offer(new Leg(
										nodeI, nodeJ, Leg.NODE_EXTENSION));
				}
			}
			// rule C
			else if (subgraphNodesDegree[nodeI] == 0) {
				for (int j = 0; j < vertices[nodeI].length; j++) {
					int nodeJ = vertices[nodeI][j];
					if (subgraphNodesDegree[nodeJ] == -1
							&& nodeJ > maximumIndex)
						if (connectingEdges.add(connectivity[nodeI][nodeJ]))
							candidates[candidateValidIndex].offer(new Leg(
									nodeI, nodeJ, Leg.NODE_EXTENSION));
				}
			}
		}
		return connectingEdges.size();
	}

	/**
	 * Given current subgraph, find a edge that is A. not in current subgraph
	 * but in query graph q B. is the largest if added among all nodes in the
	 * same cycle
	 * 
	 * @return
	 */
	private int addEdgeExtensionCandidate() {
		int count = 0;
		for (int nodeI = 0; nodeI < vertices.length; nodeI++) {
			if (subgraphNodesDegree[nodeI] == -1)
				continue; // only interested in subgraph nodes
			int[] maxIndex = null;// results savers of maxSubgraphEdgeAlongPath
			// node I In query graph & subgraph
			for (int j = 0; j < vertices[nodeI].length; j++) {
				int nodeJ = vertices[nodeI][j];
				// node J In query graph & subgraph
				// undirected edge, only visited once
				if (subgraphNodesDegree[nodeJ] == -1 || nodeI > nodeJ)
					continue;
				else {
					// find edge(nodeI, nodeJ) not in subgraph
					boolean nodeJConnected2NodeIinSubgraph = false;
					for (int w = 0; w < subgraphNodesDegree[nodeI]; w++)
						if (nodeJ == subVertices[nodeI][w]) {
							nodeJConnected2NodeIinSubgraph = true;
							break;
						}
					// rule 2
					if (!nodeJConnected2NodeIinSubgraph) {
						if (maxIndex == null)
							maxIndex = maxSubgraphEdgeAlongPath(nodeI);
						if (connectivity[nodeI][nodeJ] > maxIndex[nodeJ]) {
							candidates[candidateValidIndex].offer(new Leg(
									nodeI, nodeJ, Leg.EDGE_EXTENSION));
							count++;
						}
					}
				}
			}
		}
		return count;
	}

	/**
	 * Find the maximum edge index along paths starting from nodeA Doing breath
	 * first search Avoiding information passing cycle
	 * 
	 * @param nodeA
	 * @return
	 */
	private int[] maxSubgraphEdgeAlongPath(int nodeA) {
		int[] parents = new int[vertices.length];
		int[] maxIndex2NodeA = new int[vertices.length];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = -1;
			maxIndex2NodeA[i] = -1;
		}
		// Breadth first search
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(nodeA);
		int nodeI;
		int nodeJ;
		while (!queue.isEmpty()) {
			nodeI = queue.poll();
			for (int j = 0; j < subgraphNodesDegree[nodeI]; j++) {
				nodeJ = subVertices[nodeI][j];
				if (parents[nodeI] == nodeJ)// nodeI is actually connected with
											// nodeA through nodeJ
					continue;
				boolean changeParent = false;
				if (connectivity[nodeI][nodeJ] > maxIndex2NodeA[nodeJ]) {
					maxIndex2NodeA[nodeJ] = connectivity[nodeI][nodeJ];
					changeParent = true;
				}
				if (maxIndex2NodeA[nodeI] > maxIndex2NodeA[nodeJ]) {
					maxIndex2NodeA[nodeJ] = maxIndex2NodeA[nodeI];
					changeParent = true;
				}
				if (changeParent) {
					parents[nodeJ] = nodeI;
					queue.offer(nodeJ);
				}
			}
		}
		return maxIndex2NodeA;
	}

	/**
	 * In the depth first search of subgraphs tree, we need to revisited above
	 * level therefore, have to restore any changed have been made on low level
	 * (depth)
	 */
	private void restoreSubgraph() {
		// if(this.subgraphNodeNum == this.subgraphEdgeNum-1)
		// System.out.println("Begining");
		// Restore the leg extension we have made: changing candidateVlidIndex
		// from n to n+1
		Leg addedLeg = historyRecord[candidateValidIndex - 1];
		if (addedLeg.isNodeOnlyExtension()) {
			--subgraphNodesDegree[addedLeg.getNode()];
			--subgraphNodeNum;
		} else if (addedLeg.isNodeExtension()) {
			removeSubgraphEdge(addedLeg.getAlreadyInNode(),
					addedLeg.getNewNode());
			// After moving: subgraphNodesDegree[addedLeg.getNodeB()] == 0;
			--subgraphNodesDegree[addedLeg.getNewNode()];
			--subgraphNodeNum;
		} else {
			removeSubgraphEdge(addedLeg.getNodeA(), addedLeg.getNodeB());
		}
		// All lower depth candidate set have to be cleared
		for (int i = candidateValidIndex; i < candidates.length; i++)
			candidates[i].clear();
		--candidateValidIndex;
	}
}
