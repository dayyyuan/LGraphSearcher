package edu.psu.chemxseer.structure.newISO.newFastSU;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import edu.psu.chemxseer.structure.newISO.QuickSI.ConstrainEntry;
import edu.psu.chemxseer.structure.newISO.QuickSI.ForwardEntry;
import edu.psu.chemxseer.structure.newISO.QuickSI.QIEdge;
import edu.psu.chemxseer.structure.newISO.Util.GraphDBFrequency;
import edu.psu.chemxseer.structure.newISO.Util.ISOEdgeGraph;
import edu.psu.chemxseer.structure.util.PriorityQueueSelfImp;

/**
 * The Class Generate the order of the FastSU Search
 * 
 * @author dayuyuan
 * 
 */
public class ISOFastSUOrder {
	private ISOEdgeGraph query; // the query graph
	private Embedding[][] blocks; // block[i] represents the embedding for
									// pattern i
	private GraphDBFrequency graphDB;

	// Data Member
	private int[] nodeVisited; // -1, unvisited, 0, visited but sequence not
								// generated, 1, visited & sequence generated
	private int coveredCount;
	private Embedding[][] queryNodeInvertedIndex;
	// queryNodeInvertedIndex[i] is an array of embeddings covering node i
	private PriorityQueueSelfImp<Embedding> adjEmbeddings;

	// Result
	private ForwardEntry rootSequence;
	private ForwardEntry lastSequence;

	public FastSUSequence getSequence() {
		return new FastSUSequence(rootSequence, query.getNodeCount());
	}

	/**
	 * Given the Query Graph & the Blocks Information Construct a ISOFastSUOrder
	 * object & Generate the Order of this search order on the query Graph
	 * 
	 * @param query
	 * @param blocks
	 */
	public ISOFastSUOrder(ISOEdgeGraph query, Embedding[][] blocks,
			GraphDBFrequency graphDB) {
		this.query = query;
		this.blocks = blocks;
		this.graphDB = graphDB;

		this.nodeVisited = new int[query.getNodeCount()];
		for (int i = 0; i < nodeVisited.length; i++)
			this.nodeVisited[i] = -1; // all nodes unvisited
		this.coveredCount = 0;
		this.adjEmbeddings = new PriorityQueueSelfImp<Embedding>(true,
				Embedding.getComparator());
		this.rootSequence = null;
	}

	/**
	 * Initialize the inverted index: this.queryNodeInvertedIndex
	 */
	private void initializeInvertedIndex() {
		List<Embedding>[] invertedIndex = new List[this.query.getNodeCount()];
		for (int i = 0; i < invertedIndex.length; i++)
			invertedIndex[i] = new ArrayList<Embedding>();

		for (int i = 0; i < this.blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				Embedding emb = blocks[i][j];
				int[] mapping = emb.getMap();
				for (int queryNode : mapping)
					invertedIndex[queryNode].add(emb);
			}
		}

		// reduce the memory consumption by changing the array list to array
		this.queryNodeInvertedIndex = new Embedding[query.getNodeCount()][];
		for (int i = 0; i < this.queryNodeInvertedIndex.length; i++) {
			this.queryNodeInvertedIndex[i] = new Embedding[invertedIndex[i]
					.size()];
			for (int j = 0; j < invertedIndex[i].size(); j++)
				queryNodeInvertedIndex[i][j] = invertedIndex[i].get(j);
		}
	}

	/**
	 * Find the Search Order of the query Graph
	 */
	private void genSearchOrder() {
		PriorityQueue<QIEdge> candidateEdge = new PriorityQueue<QIEdge>();
		// Stop when all the nodes are covered
		while (this.coveredCount < this.query.getNodeCount()) {
			if (this.adjEmbeddings.isEmpty()) {
				// 1. Find the minimum Embedding, then update the status & find
				// surrouding embeddings
				Embedding minEmb = this.findMin();
				if (minEmb == null)
					break; // all embeddings are visited
				this.updateStatus(minEmb);
				// 1.1. Generate the first QI Sequence
				ForwardEntry aEntry = this.findFirstEdgeUnConnected(minEmb,
						graphDB, candidateEdge);
				if (rootSequence == null) {
					rootSequence = aEntry;
					lastSequence = aEntry.nextEntry(); // two entries are
														// generated
				} else {
					lastSequence.attachNextEntry(aEntry);
					lastSequence = aEntry.nextEntry(); // two entries are
														// generated
				}
				// 1.2 Generate the other QI Sequence
				while (!candidateEdge.isEmpty()) {
					aEntry = this.findNextEdgeConnected(candidateEdge, graphDB);
					lastSequence.attachNextEntry(aEntry);
					lastSequence = aEntry;
				}
			} else {
				while (!this.adjEmbeddings.isEmpty()
						&& coveredCount < query.getNodeCount()) {
					// 2. Find theminimum Embedding, then update the status &
					// find surrouding embeddings
					Embedding minEmb = this.adjEmbeddings.popMin();
					this.updateStatus(minEmb);
					// 2.1 Generate all the candidate edges
					this.findCandidateEdgeConnected(minEmb, candidateEdge,
							graphDB);

					while (!candidateEdge.isEmpty()) {
						ForwardEntry aEntry = this.findNextEdgeConnected(
								candidateEdge, graphDB);
						lastSequence.attachNextEntry(aEntry);
						lastSequence = aEntry;
					}
				}
			}

		}
		if (this.coveredCount < this.query.getNodeCount()) {
			this.findCandidateEdgeConnected(candidateEdge, graphDB);
			while (!candidateEdge.isEmpty()) {
				ForwardEntry aEntry = this.findNextEdgeConnected(candidateEdge,
						graphDB);
				lastSequence.attachNextEntry(aEntry);
				lastSequence = aEntry;
			}
		}
	}

	/**
	 * Find the minimum subgraph pattern return null, if all embeddings are
	 * visited
	 * 
	 * @return
	 */
	private Embedding findMin() {
		Embedding minEmb = null;
		for (int i = 0; i < this.blocks.length; i++) {
			for (int j = 0; j < blocks[i].length; j++) {
				if (blocks[i][j].isVisited())
					continue;
				else if (minEmb == null)
					minEmb = blocks[i][j];
				else if (Embedding.getComparator()
						.compare(minEmb, blocks[i][j]) == 1)
					minEmb = blocks[i][j];
			}
		}
		return minEmb;
	}

	/**
	 * Given the newly selected embedding, update the visited status of the
	 * query nodes: from -1 to 0, QISequence not generated update the
	 * surrounding embeddings
	 * 
	 * @param newEmbedding
	 */
	private void updateStatus(Embedding newEmbedding) {
		newEmbedding.setVisited();
		// update the adjacent set
		// 1. newEmbedding need to be removed (since it is visited already)
		// 2. all the embeddings with the same patter of newEmbedding, need to
		// update their score
		// 3. insert embeddings that are newly adjacent
		if (!this.adjEmbeddings.isEmpty()) {
			// 1.
			boolean removeNew = this.adjEmbeddings.remove(newEmbedding);
			if (removeNew == false)
				System.out
						.println("Error in the maintainance of the sPriority Queue");
		}
		// 2.
		int patternID = newEmbedding.getPatternID();
		for (Embedding sibling : this.blocks[patternID]) {
			sibling.udpateScore(); // update the score
			if (this.adjEmbeddings.contains(sibling)) // update the position on
														// the priority queue
				this.adjEmbeddings.changeKey(sibling, -1); // key decreases
		}
		// 3.
		// for each of the nodes contained in the new embedding, update the
		// status to be visited
		int[] patternNodes = newEmbedding.getMap();
		Set<Embedding> surroundEmbed = new HashSet<Embedding>();

		for (int queryNode : patternNodes) {
			if (this.nodeVisited[queryNode] == 1) // this queryNode is already
													// visited & QI generated
				continue;
			else {
				this.nodeVisited[queryNode] = 0; // visit the node but the QI
													// sequence is not generated
				this.coveredCount++;
				findSurroundEmbedding(queryNode, surroundEmbed);
			}
		}
		for (Embedding adjEmb : surroundEmbed)
			this.adjEmbeddings.add(adjEmb);

	}

	/**
	 * Given a query node, find all the embeddings covering this node &
	 * connected to this node but this embedding is not in the
	 * adjacentpriorityqueue put them in the surroundEmbed set
	 * 
	 * @param queryNode
	 * @return
	 */
	private void findSurroundEmbedding(int queryNode,
			Set<Embedding> surroundEmbed) {
		for (Embedding emb : this.queryNodeInvertedIndex[queryNode]) {
			if (this.adjEmbeddings.contains(emb) || emb.isVisited())
				continue;
			else
				surroundEmbed.add(emb);
		}

		for (int i = 0; i < this.query.getDegree(queryNode); i++) {
			int adjNode = query.getAdjacentNode(queryNode, i);
			for (Embedding emb : this.queryNodeInvertedIndex[adjNode]) {
				if (this.adjEmbeddings.contains(emb) || emb.isVisited())
					continue;
				else
					surroundEmbed.add(emb);
			}
		}
	}

	/**
	 * The new Embedding is not connected with other selected nodes: Find the
	 * first edge as the QISequence And generate the candidateEdge for next
	 * Sequence
	 * 
	 * @param emb
	 * @param graphDB
	 * @param candidateEdge
	 * @return
	 */
	private ForwardEntry findFirstEdgeUnConnected(Embedding emb,
			GraphDBFrequency graphDB, PriorityQueue<QIEdge> candidateEdge) {
		// 1st Step: find the first edge
		// 1. Minimum edge weight 2. Minimum degree
		// Randomly select one from minEdges, here we select the first one
		int minEdge = -1;
		float minWeight = Integer.MAX_VALUE;
		int minDegree = Integer.MAX_VALUE;
		int[] queryNodes = emb.getMap();
		Arrays.sort(queryNodes);
		for (int i = 0; i < queryNodes.length; i++) {
			for (int j = i + 1; j < queryNodes.length; j++) {
				int edgeID = this.query.getEdge(queryNodes[i], queryNodes[j]);
				if (edgeID == -1)
					continue; // no such edge in the query graph
				int labelNode1 = query.getNodeLabel(queryNodes[i]);
				int labelNode2 = query.getNodeLabel(queryNodes[j]);
				float support = graphDB.getEdgeSupport(labelNode1, labelNode2,
						query.getEdgelabel(edgeID));

				if (support < minWeight) {
					minEdge = edgeID;
					minWeight = support;
					minDegree = query.getDegree(queryNodes[i])
							+ query.getDegree(queryNodes[j]);
				} else if (support == minWeight) {
					int newDegree = query.getDegree(queryNodes[i])
							+ query.getDegree(queryNodes[j]);
					if (newDegree < minDegree) {
						minEdge = edgeID;
						minDegree = newDegree;
					} else if (newDegree == minDegree)
						continue; // discard the succeeding one
				} else
					continue;
			}
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
		// 5. construct the first QIEntry
		ForwardEntry sequence = new ForwardEntry(startPoint, -1,
				query.getNodeLabel(startPoint));
		if (query.getDegree(startPoint) > 2)
			sequence.addConstrain(ConstrainEntry.buildDegreeConstrain(query
					.getDegree(startPoint)));
		// construct the second QIEntry
		ForwardEntry sequence2 = new ForwardEntry(endPoint, 0,
				query.getNodeLabel(endPoint));
		if (query.getDegree(endPoint) > 2)
			sequence2.addConstrain(ConstrainEntry.buildDegreeConstrain(query
					.getDegree(endPoint)));
		sequence.attachNextEntry(sequence2);
		// 6. Generate the candidateEdge for the next inclusion
		// 6.1 the start point and end point are both visited & QI generated
		this.nodeVisited[startPoint] = 1;
		this.nodeVisited[endPoint] = 1;
		// 6.2 Add edges surround the start point
		candidateEdge.clear();
		for (int i = 0; i < query.getDegree(startPoint); i++) {
			int adjNode = query.getAdjacentNode(startPoint, i);
			if (nodeVisited[adjNode] == 0) { // in the embedding, QI not
												// generated
				int indgSize = 0;
				for (int w = 0; w < query.getDegree(adjNode); w++)
					if (nodeVisited[query.getAdjacentNode(adjNode, w)] == 1) // #
																				// of
																				// constrains
																				// for
																				// the
																				// adjNode
						indgSize++;
				int adjEdge = query.getEdge(startPoint, adjNode);
				float edgeSupport = graphDB.getEdgeSupport(
						query.getNodeLabel(startPoint),
						query.getNodeLabel(adjNode),
						query.getEdgelabel(adjEdge));
				candidateEdge.offer(new QIEdge(adjEdge, edgeSupport, indgSize,
						query.getDegree(adjNode)));
			}
		}
		// 6.3 Add edges surround the end point
		for (int i = 0; i < query.getDegree(endPoint); i++) {
			int adjNode = query.getAdjacentNode(endPoint, i);
			if (nodeVisited[adjNode] == 0) { // in the embedding, QI not
												// generated
				int indgSize = 0;
				for (int w = 0; w < query.getDegree(adjNode); w++)
					if (nodeVisited[query.getAdjacentNode(adjNode, w)] == 1) // #
																				// of
																				// constrains
																				// for
																				// the
																				// adjNode
						indgSize++;
				int adjEdge = query.getEdge(endPoint, adjNode);
				float edgeSupport = graphDB.getEdgeSupport(
						query.getNodeLabel(endPoint),
						query.getNodeLabel(adjNode),
						query.getEdgelabel(adjEdge));
				candidateEdge.offer(new QIEdge(adjEdge, edgeSupport, indgSize,
						query.getDegree(adjNode)));
			}
		}
		return sequence;
	}

	/**
	 * For an new embedding connected to other selected embeddings, find the
	 * candidate edge
	 * 
	 * @param emb
	 * @param candidateEdge
	 * @param graphDB
	 */
	private void findCandidateEdgeConnected(Embedding emb,
			PriorityQueue<QIEdge> candidateEdge, GraphDBFrequency graphDB) {
		int[] queryNodes = emb.getMap();
		for (int queryNode : queryNodes) {
			int indgSize = 0;
			for (int w = 0; w < query.getDegree(queryNode); w++) {
				if (nodeVisited[query.getAdjacentNode(queryNode, w)] == 1) // #
																			// of
																			// constrains
																			// for
																			// the
																			// adjNode
					indgSize++;
			}
			for (int i = 0; i < this.query.getDegree(queryNode); i++) {
				int adjNode = this.query.getAdjacentNode(queryNode, i);
				if (this.nodeVisited[adjNode] == 1) {
					int adjEdge = query.getEdge(queryNode, adjNode);
					float edgeSupport = graphDB.getEdgeSupport(
							query.getNodeLabel(queryNode),
							query.getNodeLabel(adjNode),
							query.getEdgelabel(adjEdge));
					candidateEdge.offer(new QIEdge(adjEdge, edgeSupport,
							indgSize, query.getDegree(queryNode)));
				} else
					continue;
			}
		}
	}

	/**
	 * Also update all the "nodes" to be visited .
	 * 
	 * @param candidateEdge
	 * @param graphDB
	 */
	private void findCandidateEdgeConnected(
			PriorityQueue<QIEdge> candidateEdge, GraphDBFrequency graphDB) {

		for (int queryNode = 0; queryNode < query.getNodeCount(); queryNode++) {
			if (this.nodeVisited[queryNode] == 1)
				continue;
			else
				nodeVisited[queryNode] = 0;
			// Else the query Node is the a new node
			int indgSize = 0;
			for (int w = 0; w < query.getDegree(queryNode); w++) {
				if (nodeVisited[query.getAdjacentNode(queryNode, w)] == 1) // #
																			// of
																			// constrains
																			// for
																			// the
																			// adjNode
					indgSize++;
			}
			for (int i = 0; i < this.query.getDegree(queryNode); i++) {
				int adjNode = this.query.getAdjacentNode(queryNode, i);
				if (this.nodeVisited[adjNode] == 1) {
					int adjEdge = query.getEdge(queryNode, adjNode);
					float edgeSupport = graphDB.getEdgeSupport(
							query.getNodeLabel(queryNode),
							query.getNodeLabel(adjNode),
							query.getEdgelabel(adjEdge));
					candidateEdge.offer(new QIEdge(adjEdge, edgeSupport,
							indgSize, query.getDegree(queryNode)));
				} else
					continue;
			}
		}
	}

	/**
	 * Given a set of candidateEdge, select the min one and construct a sequence
	 * Also, update the candidatEdge by inserting surrouding edges
	 * 
	 * @param candidateEdge
	 * @param graphDB
	 * @return
	 */
	private ForwardEntry findNextEdgeConnected(
			PriorityQueue<QIEdge> candidateEdge, GraphDBFrequency graphDB) {
		// 1. pop out the minimum edge from mHeap, two nodes of the mHeap,
		// sequence already generated node &
		// sequence not generated node
		int minedge = candidateEdge.poll().getEdgeIndex();
		int minnodes[] = query.getEdgeNodes(minedge);
		int toBeaddedNode = minnodes[0];
		int alreadyInNode = minnodes[1];
		if (nodeVisited[toBeaddedNode] != 0) {
			toBeaddedNode = minnodes[1];
			alreadyInNode = minnodes[0];
		}
		// 2. Build the Sequence Edge
		ForwardEntry sequence = new ForwardEntry(toBeaddedNode,
				nodeVisited[alreadyInNode], query.getNodeLabel(toBeaddedNode));
		nodeVisited[toBeaddedNode] = 1;

		// 3. Add degree constrain for toBeaddedNode
		if (query.getDegree(toBeaddedNode) > 2)
			sequence.addConstrain(ConstrainEntry.buildDegreeConstrain(query
					.getDegree(toBeaddedNode)));

		// 4. Find the adjNode (edge) of the toBeaddedNode
		for (int i = 0; i < query.getDegree(toBeaddedNode); i++) {
			int adjNode = query.getAdjacentNode(toBeaddedNode, i);
			if (nodeVisited[adjNode] == 1) // add edge constrain
				sequence.addConstrain(ConstrainEntry
						.buildEdgeConstrain(nodeVisited[adjNode]));
			else if (nodeVisited[adjNode] == 0) { // adjNode has been visited by
													// the embeddings
				int adjEdge = query.getEdge(toBeaddedNode, adjNode);
				float edgeSupport = graphDB.getEdgeSupport(
						query.getNodeLabel(adjNode),
						query.getNodeLabel(toBeaddedNode),
						query.getEdgelabel(adjEdge));
				int indgSize = 0;
				for (int w = 0; w < query.getDegree(adjNode); w++)
					if (nodeVisited[query.getAdjacentNode(adjNode, w)] == 1) // #
																				// of
																				// constrains
																				// for
																				// the
																				// adjNode
						indgSize++;
				candidateEdge.offer(new QIEdge(adjEdge, edgeSupport, indgSize,
						query.getDegree(adjNode)));
			} else
				continue;
		}
		return sequence;
	}

}
