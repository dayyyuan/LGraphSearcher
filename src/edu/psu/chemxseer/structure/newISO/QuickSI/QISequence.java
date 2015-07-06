package edu.psu.chemxseer.structure.newISO.QuickSI;

import java.util.PriorityQueue;
import java.util.Stack;

import edu.psu.chemxseer.structure.newISO.Util.GraphDBFrequency;
import edu.psu.chemxseer.structure.newISO.Util.ISOEdgeGraph;
import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

/**
 * An implementation of the QISequence Given a query graph q, a QI-Sequence is a
 * sequence that represents a rooted spanning tree for q.
 * 
 * @author dayuyuan
 * 
 */
public class QISequence implements QISequenceInterface {
	// A QI Sequence is an Array of QISequenceEntry, which is derived from a
	// query graph
	private ForwardEntry[] sequence;
	private ISOGraph graph;

	public QISequence(ISOGraph query) {
		graph = query;
		buildSequence();
	}

	public QISequence(ISOEdgeGraph query, GraphDBFrequency graphDB) {
		graph = query;
		buildSequence2(query, graphDB);
	}

	@Override
	public ForwardEntry getEntry(int index) {
		return sequence[index];
	}

	/**
	 * This is simplified version of building a QISequence Construct the
	 * QISequence by A depth first search of the underlying graph
	 */
	private void buildSequence() {
		this.sequence = new ForwardEntry[graph.getNodeCount()];
		int nodeNum = graph.getNodeCount();
		int[] parents = new int[nodeNum];
		int[] map = new int[nodeNum]; // map[vi] = sequenceIndex
		for (int i = 0; i < map.length; i++)
			map[i] = -1;
		int index = 0;

		// depth first search
		Stack<Integer> dfs = new Stack<Integer>();
		// choose the node 0 to be the first node
		map[0] = 0;
		parents[0] = -1;
		ForwardEntry firstEntry = new ForwardEntry(0, -1, graph.getNodeLabel(0));
		this.sequence[index++] = firstEntry;
		if (graph.getDegree(0) > 2)
			firstEntry.addConstrain(ConstrainEntry.buildDegreeConstrain(graph
					.getDegree(0)));
		for (int i = 0; i < graph.getDegree(0); i++) {
			int adjNode = graph.getAdjacentNode(0, i);
			dfs.push(adjNode);
			parents[adjNode] = 0;
		}
		// keep on searching
		while (!dfs.isEmpty()) {
			int currentNode = dfs.pop();
			ForwardEntry currentEntry = null;
			if (map[currentNode] == -1) {
				// find a forward edge
				// System.out.println("index " + index + " currentNode " +
				// currentNode);
				currentEntry = new ForwardEntry(currentNode,
						parents[currentNode], graph.getNodeLabel(currentNode));
				this.sequence[index] = currentEntry;
				map[currentNode] = index;
			} else
				continue;
			// add a edge constrain
			int degree = graph.getDegree(currentNode);
			if (degree > 2)
				currentEntry.addConstrain(ConstrainEntry
						.buildDegreeConstrain(degree));

			for (int i = 0; i < degree; i++) {
				int childNode = graph.getAdjacentNode(currentNode, i);
				if (childNode == parents[currentNode])
					continue; // this is actually not a childNode
				if (map[childNode] != -1 && map[childNode] < index - 1) {
					// find a backward edge
					currentEntry.addConstrain(ConstrainEntry
							.buildEdgeConstrain(map[childNode]));
				} else {
					dfs.push(childNode);
					parents[childNode] = currentNode;
				}
			}
			index++;
		}
	}

	/**
	 * Build a QISequence that will produce a effective search order while
	 * running the subgrpah isomorphism test 1st step: the average inner support
	 * of an edge e (or a vertex v) is the average number of its possible
	 * mappings in the graphs which contain this edge (vertex) 2nd step: finding
	 * the minimum spanning tree, extend the Prim's algorithm
	 */
	private void buildSequence2(ISOEdgeGraph query, GraphDBFrequency graphDB) {
		// Find the first edge, construct the 1st and 2nd sequence entry
		int firstEdge = this.selectFirstEdge(query, graphDB);
		int[] firstNodes = query.getEdgeNodes(firstEdge);

		// keep on finding the succeeding edges
		int[] nodeVisited = new int[query.getNodeCount()];
		boolean[] edgeVisited = new boolean[query.getEdgeCount()];
		for (int i = 0; i < nodeVisited.length; i++)
			nodeVisited[i] = -1;
		for (int i = 0; i < edgeVisited.length; i++)
			edgeVisited[i] = false;
		nodeVisited[firstNodes[0]] = 0;
		nodeVisited[firstNodes[1]] = 1;
		edgeVisited[firstEdge] = true;

		// ISOEdge:
		// 1. Find the edge with minimum weight
		// 2. Find the edge with maximum IndG, size of induced subgraph
		// 3. Find the vertex with the minimum vertex degree
		PriorityQueue<QIEdge> mHeap = new PriorityQueue<QIEdge>(
				query.getNodeCount());
		// add surrounding edges of the first nodes
		for (int i = 0; i < query.getDegree(firstNodes[0]); i++) {
			int adjNode = query.getAdjacentNode(firstNodes[0], i);
			int adjEdge = query.getEdge(firstNodes[0], adjNode);
			if (edgeVisited[adjEdge] == true)
				continue;
			else {
				float edgeSupport = graphDB.getEdgeSupport(
						query.getNodeLabel(firstNodes[0]),
						query.getNodeLabel(adjNode),
						query.getEdgelabel(adjEdge));
				int indgSize = 0;
				for (int j = 0; j < query.getDegree(adjNode); j++)
					if (nodeVisited[query.getAdjacentNode(adjNode, j)] != -1)
						indgSize++;
				QIEdge oneISOEdge = new QIEdge(adjEdge, edgeSupport, indgSize,
						query.getDegree(adjNode));
				mHeap.offer(oneISOEdge);
			}
		}

		// starting to find the 3rd entry of QISequence
		int index = 2;
		while (!mHeap.isEmpty()) {
			// pop out the minimum edge from mHeap
			int minedge = mHeap.poll().getEdgeIndex();
			int minnodes[] = query.getEdgeNodes(minedge);
			int toBeaddedNode = minnodes[0];
			int alreadyInNode = minnodes[1];
			if (nodeVisited[toBeaddedNode] != -1) {
				toBeaddedNode = minnodes[1];
				alreadyInNode = minnodes[0];
			}
			// Add this edge into the sequence
			this.sequence[index] = new ForwardEntry(toBeaddedNode,
					nodeVisited[alreadyInNode],
					query.getNodeLabel(toBeaddedNode));
			nodeVisited[toBeaddedNode] = index;
			edgeVisited[minedge] = true;
			// add degree constrain
			if (query.getDegree(toBeaddedNode) > 2)
				this.sequence[index].addConstrain(ConstrainEntry
						.buildDegreeConstrain(query.getDegree(toBeaddedNode)));

			// find edges surrounds last visited vertex and add them to minHeap
			for (int i = 0; i < query.getDegree(toBeaddedNode); i++) {
				int adjNode = query.getAdjacentNode(toBeaddedNode, i);
				int adjEdge = query.getEdge(toBeaddedNode, adjNode);
				if (edgeVisited[adjNode])
					continue;
				else if (nodeVisited[adjNode] != -1) // add edge constrain
					this.sequence[index].addConstrain(ConstrainEntry
							.buildEdgeConstrain(nodeVisited[adjNode]));
				else { // add a forward edge into minHeap
					float edgeSupport = graphDB.getEdgeSupport(
							query.getNodeLabel(adjNode),
							query.getNodeLabel(toBeaddedNode),
							query.getEdgelabel(adjEdge));
					int indgSize = 0;
					for (int w = 0; w < query.getDegree(adjNode); w++)
						if (nodeVisited[query.getAdjacentNode(adjNode, w)] != -1)
							indgSize++;
					mHeap.offer(new QIEdge(adjEdge, edgeSupport, indgSize,
							query.getDegree(adjNode)));
				}
			}
			index++;
		}
	}

	private int selectFirstEdge(ISOEdgeGraph query, GraphDBFrequency graphDB) {
		// 1st Step: find the first edge
		// 1. Minimum edge weight 2. Minimum degree
		// Randomly select one from minEdges, here we select the first one
		int minEdge = -1;
		float minWeight = Integer.MAX_VALUE;
		int minDegree = Integer.MAX_VALUE;
		for (int i = 0; i < query.getEdgeCount(); i++) {
			int[] nodes = query.getEdgeNodes(i);
			int labelNode1 = query.getNodeLabel(nodes[0]);
			int labelNode2 = query.getNodeLabel(nodes[1]);
			float support = graphDB.getEdgeSupport(labelNode1, labelNode2,
					query.getEdgelabel(i));
			if (support < minWeight) {
				minEdge = i;
				minWeight = support;
				minDegree = query.getDegree(nodes[0])
						+ query.getDegree(nodes[1]);
			} else if (support == minWeight) {
				int newDegree = query.getDegree(nodes[0])
						+ query.getDegree(nodes[1]);
				if (newDegree < minDegree) {
					minEdge = i;
					minDegree = newDegree;
				} else if (newDegree == minDegree)
					continue; // discard the succeeding one
			} else
				continue;
		}
		// 3. Starting point, lower vertex weight
		// 4. Starting point, high degree
		int[] nodes = query.getEdgeNodes(minEdge);
		int startPoint = nodes[0], endPoint = nodes[1];
		float nodeSupport0 = graphDB.getNodeSupport(query
				.getNodeLabel(nodes[0]));
		float nodeSupport1 = graphDB.getNodeSupport(query
				.getNodeLabel(nodes[1]));
		if (nodeSupport0 > nodeSupport1) {
			startPoint = nodes[1];
			endPoint = nodes[0];
		} else if (nodeSupport0 == nodeSupport1
				&& query.getDegree(nodes[0]) < query.getDegree(nodes[1])) {
			startPoint = nodes[1];
			endPoint = nodes[0];
		}
		// construct the first QIEntry
		this.sequence[0] = new ForwardEntry(startPoint, -1,
				query.getNodeLabel(startPoint));
		if (query.getDegree(startPoint) > 2)
			this.sequence[0].addConstrain(ConstrainEntry
					.buildDegreeConstrain(query.getDegree(startPoint)));
		// construct the second QIEntry
		this.sequence[1] = new ForwardEntry(endPoint, 0,
				query.getNodeLabel(endPoint));
		if (query.getDegree(endPoint) > 2)
			this.sequence[1].addConstrain(ConstrainEntry
					.buildDegreeConstrain(query.getDegree(endPoint)));
		return minEdge;
	}
}
