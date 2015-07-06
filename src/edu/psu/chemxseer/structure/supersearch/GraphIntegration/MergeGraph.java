package edu.psu.chemxseer.structure.supersearch.GraphIntegration;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.OrderedIntSet;

public class MergeGraph {
	/**
	 * Randomly Selected "count" number of graphs, and then merge them together
	 * to generated a synthesis graph and return
	 * 
	 * @param gDB
	 * @param chooseN
	 * @throws IOException
	 * @throws ParseException
	 */
	public static Graph mergeGraphs(IGraphDatabase gDB, int chooseN)
			throws ParseException, IOException {
		Graph[] gs = RandomChoseDBGraph.randomlyChooseDBGraph(gDB, chooseN);
		IntegrateGraph result = new IntegrateGraph(gs[0]);
		MergeGraph mg = new MergeGraph();
		// int oriEdgeCount =gs[0].getEdgeCount(), oriNodeCount =
		// gs[0].getNodeCount();
		for (int i = 1; i < gs.length; i++) {
			mg.insertGraph(result, gs[i]);
			// oriEdgeCount += gs[i].getEdgeCount();
			// oriNodeCount += gs[i].getNodeCount();
		}
		Graph g = result.getGraph();
		// System.out.println(" EdgeCount: " + oriEdgeCount + ", NodeCount: " +
		// oriNodeCount + ", NewEdgeCount: " + g.getEdgeCount() +
		// ", NewNodeCount: " + g.getNodeCount());
		return g;
	}

	public static void main(String args[]) throws ParseException, IOException {
		// String dbFileName =
		// "/Users/dayuyuan/Documents/workspace/Experiment/feature";
		// GraphDatabase gDB = new GraphDatabase_InMem(new
		// NoPostingFeatures(dbFileName,
		// MyFactory.getFeatureFactory(FeatureFactoryType.MultiFeature)));
		// String dbFileName =
		// "/Users/dayuyuan/Documents/Experiment/SetCover/DBFile";
		// GraphDatabase gDB = new GraphDatabase_OnDisk(dbFileName,
		// MyFactory.getSmilesParser());
		String dbFileName = "/Users/dayuyuan/Documents/Experiment/SetCover/MultiClassFeatures";
		IGraphDatabase gDB = new GraphDatabase_InMem(
				new NoPostingFeatures<IOneFeature>(dbFileName, MyFactory
						.getFeatureFactory(FeatureFactoryType.MultiFeature)));
		// TEST ONE:
		for (int i = 0; i < 100; i++) {
			Graph mergedGraph = mergeGraphs(gDB, 3);
			// String gString =
			// MyFactory.getSmilesParser().serialize(mergedGraph);
			// Graph newMergedGraph = MyFactory.getSmilesParser().parse(gString,
			// MyFactory.getGraphFactory());
			String gString = MyFactory.getUnCanDFS().serialize(mergedGraph);
			Graph newMergedGraph = MyFactory.getDFSCoder().parse(gString,
					MyFactory.getGraphFactory());
			if (newMergedGraph.getEdgeCount() != mergedGraph.getEdgeCount())
				System.out.println("lala");
			System.out.println(" EdgeCount: " + mergedGraph.getEdgeCount()
					+ ", NodeCount: " + mergedGraph.getNodeCount());
		}
		// TEST TWO:
	}

	/**
	 * Integrate another graph into the Integrated Graph
	 * 
	 * @param g
	 */
	public void insertGraph(IntegrateGraph intG, Graph g) {
		// 1. First find the search order according to the edge frequency
		int[] nodeSequence = FindSearchOrder.getNodeSequence(intG, g);
		// 2.Given the node sequence, start the insertion and matching
		boolean[] mappedIntegratedNode = new boolean[intG.getNodeCount()
				+ g.getNodeCount()];
		for (int i = 0; i < mappedIntegratedNode.length; i++)
			mappedIntegratedNode[i] = false;
		int[] mapGraphNode = new int[g.getNodeCount()];
		for (int i = 0; i < mapGraphNode.length; i++)
			mapGraphNode[i] = -1;

		int nodeIndex = 0;
		for (; nodeIndex < nodeSequence.length; nodeIndex++) {
			if (nodeIndex == 0) {
				OneEdge firstEdge = identifyFirstEdge(nodeSequence[0],
						nodeSequence[1], g, intG);
				if (firstEdge == null)
					return; // do nothing
				mappedIntegratedNode[firstEdge.getNodeAID()] = mappedIntegratedNode[firstEdge
						.getNodeBID()] = true;
				mapGraphNode[nodeSequence[0]] = firstEdge.getNodeAID();
				mapGraphNode[nodeSequence[1]] = firstEdge.getNodeBID();
				// Integrate the edge
				// int edge = firstEdge.getEdgeID();
				intG.addGraphContainingEdge(firstEdge, g.getID());
				nodeIndex = 0;
			} else {
				int nodeB = nodeSequence[nodeIndex];
				int bigNodeB = identifyNode(nodeB, g, mapGraphNode,
						mappedIntegratedNode, intG);
				if (bigNodeB == -1)
					bigNodeB = intG.getNodeCount(); // no possible matching for
													// the nodeB

				mappedIntegratedNode[bigNodeB] = true;
				mapGraphNode[nodeB] = bigNodeB;
				// Add Node into Integrated Graph
				if (bigNodeB == intG.getNodeCount())
					intG.addNode(g.getNodeLabel(nodeB));
				else if (bigNodeB > intG.getNodeCount())
					System.out.println("Excpetion: 1 in MergeGraph");
				// Add Edge into Integrated Graph
				for (int w = 0; w < g.getDegree(nodeB); w++) {
					int edge = g.getNodeEdge(nodeB, w);
					int nodeA = g.getOtherNode(edge, nodeB);
					int bigNodeA = mapGraphNode[nodeA];
					if (bigNodeA == -1)
						continue; // not mapped yet
					else {
						int bigEdge = intG.getEdge(bigNodeA, bigNodeB);
						if (bigEdge == -1)
							bigEdge = intG.addEdge(bigNodeA, bigNodeB,
									g.getEdgeLabel(edge));
						OneEdge aEdge = new OneEdge(bigNodeA, bigNodeB,
								bigEdge, g.getNodeLabel(nodeA),
								g.getNodeLabel(nodeB), g.getEdgeLabel(edge));
						intG.addGraphContainingEdge(aEdge, g.getID());
					}
				}

			}
		}
	}

	/**
	 * Given the nodeB, and the nodeMap, nodeMap[g.node] = integrated graph node
	 * 
	 * @param nodeB
	 * @param g
	 * @param smallToBig
	 * @return
	 */
	private int identifyNode(int newNode, Graph g, int[] smallToBig,
			boolean[] bigMapped, IntegrateGraph intG) {
		// 1. Find all the nodes that "nodeB" connected on graph g
		OrderedIntSet set = null;

		HashMap<Integer, Integer> nodeFreq = new HashMap<Integer, Integer>();

		for (int w = 0; w < g.getDegree(newNode); w++) {
			int edge = g.getNodeEdge(newNode, w);
			int adjNewNode = g.getOtherNode(edge, newNode);
			if (smallToBig[adjNewNode] == -1)
				continue; // this edge is not mapped, no constains at all
			else {
				List<Integer> candidateBig = new ArrayList<Integer>(); // based
																		// on
																		// one
																		// adjacent,
																		// find
																		// a
																		// list
																		// of
																		// candidates
				// find the adjacent node of nodeMap[nodeA] on the integrated
				// node [not mapped only]
				int bigAdjNewNode = smallToBig[adjNewNode];
				for (int bw = 0; bw < intG.getDegree(bigAdjNewNode); bw++) {
					int bigEdge = intG.getNodeEdge(bigAdjNewNode, bw);
					int bigNewNode = intG.getOtherNode(bigEdge, bigAdjNewNode);
					if (bigMapped[bigNewNode]) // this does not count
						continue;
					if (g.getNodeLabel(newNode) == intG
							.getNodeLabel(bigNewNode)
							&& g.getEdgeLabel(edge) == intG
									.getEdgeLabel(bigEdge)) {
						candidateBig.add(bigNewNode);

						int freq = intG.getEdgeFreq(bigEdge);
						if (!nodeFreq.containsKey(bigNewNode)
								|| nodeFreq.get(bigNewNode) < freq)
							nodeFreq.put(bigNewNode, freq);
					}
				}
				if (set == null) {
					set = new OrderedIntSet();
					set.add(candidateBig);
				} else
					set.join(candidateBig);
				if (set.size() == 0)
					break;
			}
		}
		// 2. From all the candidate, find the one with the minimum
		int maxBigNodeB = -1;
		int maxFreq = -1;
		int[] candidates = set.getItems();
		for (int i : candidates) {
			Integer freq = nodeFreq.get(i);
			// TEST
			if (freq == null)
				System.out.println("Exception");
			if (freq > maxFreq) {
				maxBigNodeB = i;
				maxFreq = freq;
			}
		}
		return maxBigNodeB;

	}

	/**
	 * Given the first & second node, return a edge [with the maximum frequency]
	 * on integrated graph
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param g
	 * @return
	 */
	private OneEdge identifyFirstEdge(int nodeA, int nodeB, Graph g,
			IntegrateGraph intG) {
		int nodeALabel = g.getNodeLabel(nodeA);
		int nodeBLabel = g.getNodeLabel(nodeB);
		int edgeLabel = g.getEdgeLabel(g.getEdge(nodeA, nodeB));

		int maxFreq = -1;
		OneEdge maxEdge = null;

		for (int bigNode = 0; bigNode < intG.getNodeCount(); bigNode++) {
			if (intG.getNodeLabel(bigNode) != nodeALabel)
				continue;
			else {
				// find surrounding nodes to see if there any matched the nodeB
				for (int w = 0; w < intG.getDegree(bigNode); w++) {
					int bigEdge = intG.getNodeEdge(bigNode, w);
					int adjBigNode = intG.getOtherNode(bigEdge, bigNode);

					if (intG.getNodeLabel(adjBigNode) == nodeBLabel
							&& intG.getEdgeLabel(bigEdge) == edgeLabel) {
						// found a matchable edge
						int freq = intG.getEdgeFreq(bigEdge);
						if (freq > maxFreq) {
							maxFreq = freq;
							maxEdge = new OneEdge(bigNode, adjBigNode, bigEdge,
									nodeALabel, nodeBLabel, edgeLabel);
						}
					} else
						continue;
				}
			}
		}

		return maxEdge;
	}
}
