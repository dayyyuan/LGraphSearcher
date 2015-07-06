package edu.psu.chemxseer.structure.util.subgen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.graph.MutableGraph;
import edu.psu.chemxseer.structure.iso.CanonicalDFS;

/**
 * @deprecated This class mainly focused on finding the whole set of subgraph
 *             given a query graph g We use reverse search tactics: When
 *             subgraph g is empty, it possible extension are all single nodes
 *             in query q. When subgraph g is a tree, it can be both edge & node
 *             + edge extended When subgraph g is a graph, it can only be edge
 *             extended NodeExtension: add in a node(and the edge connecting
 *             this new node and already in graph nodes), and after adding i,
 *             this node should have the largest index among all leaf nodes of
 *             this extended graph. EdgeExtension: add in a edge (connecting two
 *             already in graph nodes), and after adding e, this edge should
 *             have the largest index among all edges that in the same cycle of
 *             e.
 * @author dayu yuan
 * 
 */
@Deprecated
public class SubgraphGenerator {
	private Graph g;
	private MutableGraph subgraph;

	/* Inner representation of query graph */
	private int[][] vertices; // Linked List representation of g
	private int[][] connectivity; // Matrix representation of g
	private int[] edgeLabel;
	private int[][] edges; // edge -> [nodeA, nodeB], nodeA < nodeB

	// nodeVisitedOrder[i] = -1 / currentDepth of class when the ith node is
	// visited
	private boolean[] visitedNodes;
	private boolean[] visitedEdges;
	// nodeMapping[node in g] = node in subgraph
	private int[] nodeMapping;
	private int[] loopEdges;

	private ArrayList<Queue<Integer>> candidateNodes;
	// For each candidate node (next node), it has a corresponding parent node
	private ArrayList<Queue<Integer>> candidateNodeParent;
	private Queue<Integer> candidateEdge;

	private int[] alreadyVisitedNodes;
	private int maximumDepth;
	private int currentDepth;
	private int loopDepth;
	private int visitedNodeNum;
	private int visitedEdgeNum;
	private int maximumNode;
	private int[] temp;// mainly used here for maxEdge(nodeA, nodeB)
	private int[] tempParent; // mainly used here for maxEdge(notdA, nodeB)
	private CanonicalDFS parser;

	public SubgraphGenerator(Graph g, int maximumDepth, CanonicalDFS parser,
			GraphFactory factory) {
		this.g = g;
		this.subgraph = factory.createGraph();
		this.parser = parser;
		int nodeCount = g.getNodeCount();

		// 1. Initial internal representation of graph
		this.vertices = new int[nodeCount][];
		this.connectivity = new int[nodeCount][nodeCount];
		this.edges = new int[g.getEdgeCount()][2];
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
					edges[edge][0] = i;
					edges[edge][1] = anotherNode;
					edgeLabel[edge] = g.getEdgeLabel(edge);
				} // Because of symmetry, we skip the assignment of edges array
					// when i > anotherNode
				connectivity[i][anotherNode] = edge;
				vertices[i][j] = anotherNode;
			}
		}

		// 2. Initialize Other Internal Records
		initialize(maximumDepth);
	}

	/**
	 * Initialization of internal records
	 * 
	 * @param maxDepth
	 * @return
	 */
	private boolean initialize(int maxDepth) {
		this.visitedNodes = new boolean[this.vertices.length];
		for (int i = 0; i < this.visitedNodes.length; i++)
			this.visitedNodes[i] = false;

		this.visitedEdges = new boolean[this.edgeLabel.length];
		for (int i = 0; i < visitedEdges.length; i++)
			visitedEdges[i] = false;

		this.nodeMapping = new int[this.vertices.length];
		for (int j = 0; j < this.nodeMapping.length; j++)
			this.nodeMapping[j] = -1;

		this.candidateNodeParent = new ArrayList<Queue<Integer>>(maximumDepth);
		Queue<Integer> dumyParent = new LinkedList<Integer>();
		this.candidateNodeParent.add(0, dumyParent);

		this.candidateNodes = new ArrayList<Queue<Integer>>(maximumDepth);
		Queue<Integer> seeds = new LinkedList<Integer>();
		for (int i = 0; i < this.vertices.length; i++)
			seeds.add(i);
		this.candidateNodes.add(0, seeds);

		this.candidateEdge = new LinkedList<Integer>();

		this.alreadyVisitedNodes = new int[vertices.length];
		this.loopEdges = new int[edgeLabel.length];
		this.currentDepth = 0;
		this.maximumDepth = maxDepth;
		this.loopDepth = 0;
		this.visitedEdgeNum = 0;
		this.visitedNodeNum = 0;
		this.maximumNode = -1;
		this.temp = new int[vertices.length];
		this.temp = new int[vertices.length];

		return true;
	}

	public String nextSubGraphLabel() {
		// Can not grow further
		if (currentDepth == maximumDepth)
			traceBack();
		boolean canGrow = depthFirstNext();
		;
		boolean canTrace = false;
		while (!canGrow) {
			canTrace = traceBack();
			if (!canTrace)
				return null;

			canGrow = depthFirstNext();
		}
		// Grow to a subgraph, convert this subgraph into a String
		this.subgraph.saveMemory();
		return parser.serialize(this.subgraph);
	}

	/**
	 * Stop growing this depth first tree, trace back
	 * 
	 * @return
	 */
	public boolean earlyPrunning() {
		return traceBack();
	}

	/**
	 * Return true if a subgraph extension is applied Return false if can't not
	 * find a reasonable extension
	 * 
	 * @return
	 */
	private boolean depthFirstNext() {
		boolean canGrow = false;

		if (this.visitedNodeNum - 1 == this.visitedEdgeNum) {// tree
			if (!this.candidateNodes.get(currentDepth).isEmpty())// Can grow
																	// node
			{
				canGrow = true;
				if (currentDepth != 0)
					extendNodeEdge(
							this.candidateNodes.get(currentDepth).poll(),
							this.candidateNodeParent.get(currentDepth).poll());
				else
					extendNodeEdge(
							this.candidateNodes.get(currentDepth).poll(), -1);
			} else if (!this.candidateEdge.isEmpty())// Can grow edges
			{
				canGrow = true;
				extendEdge(this.candidateEdge.poll());
			}
		} else if (this.visitedNodeNum <= this.visitedEdgeNum) {// graph
			if (!this.candidateEdge.isEmpty())// Can grow edges
			{
				canGrow = true;
				extendEdge(this.candidateEdge.poll());
			}
		}
		return canGrow;
	}

	/**
	 * Extend a node and its corresponding edge to construct a new subgraph
	 * 
	 * @param nextNode
	 * @param edge
	 * @return true if extend is a success, false if can't extend
	 */
	private boolean extendNodeEdge(int nextNode, int nextNodeParent) {
		int edge = -1;
		if (nextNodeParent != -1)
			edge = connectivity[nextNodeParent][nextNode];
		this.currentDepth++;
		int nextNodeinSubgraph = this.subgraph.addNodeAndEdge(nextNodeParent,
				connectivity[nextNode][nextNode], edgeLabel[edge]);
		this.nodeMapping[nextNode] = nextNodeinSubgraph;
		// First Mark this node as visited
		this.visitedNodes[nextNode] = true;
		this.visitedNodeNum++;
		// Then mark this edge as visited
		this.visitedEdges[edge] = true;
		this.visitedEdgeNum++;

		if (maximumNode < nextNode)
			maximumNode = nextNode;
		this.alreadyVisitedNodes[currentDepth - 1] = nextNode;

		// Second, find nextNode & nextEdge for depth currentDepth
		if (candidateNodes.get(currentDepth) == null)
			candidateNodes.add(currentDepth, new LinkedList<Integer>());
		Queue<Integer> nextNodes = candidateNodes.get(currentDepth);
		if (candidateNodeParent.get(currentDepth) == null)
			candidateNodeParent.add(currentDepth, new LinkedList<Integer>());
		Queue<Integer> nextNodeParents = candidateNodeParent.get(currentDepth);

		for (int i = 0; i < vertices[nextNode].length; i++) {
			int anotherNode = vertices[nextNode][i];
			if (visitedNodes[anotherNode] == false)
			// The anotherNode has not been visited yet
			{
				if (anotherNode > this.maximumNode) {
					nextNodes.add(anotherNode);
					nextNodeParents.add(nextNode);
				}
			} else // The anotherNode has been visited
			{
				int tempEdge = connectivity[nextNode][anotherNode];
				if (tempEdge != edge
						&& tempEdge > maxEdge(nextNode, anotherNode))
					candidateEdge.add(tempEdge);
			}
		}

		// Third
		this.candidateNodes.add(currentDepth, nextNodes);
		this.candidateNodeParent.add(currentDepth, nextNodeParents);
		return true;
	}

	/**
	 * Find the largest edge along the path from node A to node B
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @return
	 */
	private int maxEdge(int nodeA, int nodeB) {
		// Initialization
		for (int i = 0; i < currentDepth; i++) {
			this.temp[alreadyVisitedNodes[i]] = -1;
			this.tempParent[alreadyVisitedNodes[i]] = -1;
		}
		// Breadth first search
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.offer(nodeA);
		int node;
		int otherNode;
		while (!queue.isEmpty()) {
			node = queue.poll();
			for (int i = 0; i < vertices[node].length; i++) {
				otherNode = vertices[node][i];
				if (temp[otherNode] == -1)// this otherNode has not been visited
											// before
				{
					tempParent[otherNode] = node;// Assign node as the parent of
													// otherNode
					temp[otherNode] = connectivity[node][otherNode];
					queue.offer(otherNode);
				} else { // this otherNode has been visited before
							// Test whether theNode is the parent of node
					if (tempParent[node] == otherNode)
						continue;
					else {
						boolean changed = false;
						if (connectivity[node][otherNode] > temp[otherNode]) {
							temp[otherNode] = connectivity[node][otherNode];
							changed = true;
						}
						if (temp[node] > temp[otherNode]) {
							temp[otherNode] = temp[node];
							changed = true;
						}
						if (changed)
							queue.offer(otherNode);
					}
				}
			}
		}
		return temp[nodeB];
	}

	/**
	 * 
	 * @param edge
	 * @return
	 */
	private boolean extendEdge(int edge) {
		// currentDepth unchanged
		this.visitedEdgeNum++;
		this.visitedEdges[edge] = true;
		this.loopEdges[loopDepth] = edge;
		this.loopDepth++;
		int nodeA = g.getNodeA(edge);
		int nodeB = g.getNodeB(edge);
		this.subgraph.addEdge(nodeMapping[nodeA], nodeMapping[nodeB],
				edgeLabel[edge]);
		// Second, find candidateEdge for depth currentDepth
		// already there
		return true;
	}

	/**
	 * Can't grow, trace back
	 * 
	 * @return
	 */
	private boolean traceBack() {
		if (this.visitedNodeNum - 1 == this.visitedEdgeNum)// tree
			return traceBackNode();
		else if (this.visitedNodeNum <= this.visitedEdgeNum)// graph
			return traceBackEdge();
		return true;
	}

	private boolean traceBackNode() {
		this.currentDepth--;
		if (this.currentDepth == -1)
			return false;// can't tractBackNode
		this.visitedNodeNum--;
		this.visitedEdgeNum--;
		int theNode = this.alreadyVisitedNodes[currentDepth];
		this.subgraph.removeNode(theNode);
		nodeMapping[theNode] = -1;

		// recover maximum node
		if (theNode == maximumNode) {
			maximumNode = -1;
			for (int i = 0; i < currentDepth; i++)
				if (alreadyVisitedNodes[i] > maximumNode)
					maximumNode = alreadyVisitedNodes[i];
		}
		// recover visitedNodes
		this.visitedNodes[theNode] = false;
		// recover candidateNodes, candidateNodeParent
		this.candidateNodes.get(currentDepth + 1).clear();
		this.candidateNodeParent.get(currentDepth + 1).clear();
		// recover candidateEdge (because of the involvement of theNode, several
		// loopEdge is added into candidateEdge)
		while (true) {
			int lastAddedEdge = this.candidateEdge.peek();
			if (g.getNodeA(lastAddedEdge) == theNode
					|| g.getNodeB(lastAddedEdge) == theNode)
				this.candidateEdge.poll();
			else
				break;
		}
		// recover edgeVisited
		for (int i = 0; i < vertices[theNode].length; i++)
			visitedEdges[connectivity[theNode][vertices[theNode][i]]] = false;
		return true;
	}

	private boolean traceBackEdge() {
		this.loopDepth--;
		if (loopDepth == -1)
			return false;
		this.visitedEdgeNum--;
		int theEdge = this.loopEdges[this.loopDepth];
		this.loopEdges[this.loopDepth] = -1;

		int nodeA = g.getNodeA(theEdge);
		int nodeB = g.getNodeB(theEdge);
		int theEdgeinSubgraph = this.subgraph.getEdge(nodeMapping[nodeA],
				nodeMapping[nodeB]);
		this.subgraph.removeEdge(theEdgeinSubgraph);
		visitedEdges[theEdge] = false;
		;
		return true;
	}
}
