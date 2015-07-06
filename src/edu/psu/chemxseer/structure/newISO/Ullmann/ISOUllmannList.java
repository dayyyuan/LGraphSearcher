package edu.psu.chemxseer.structure.newISO.Ullmann;

import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

/**
 * Ullmann algorithm: isomorphism testing one query graph with a list of target
 * graphs Compared to ISOUllmann, ListUllmann saved memory by storing only one
 * copy of data member Those data member are reused for each graphB
 * 
 * @author dayuyuan
 * 
 */
public class ISOUllmannList {
	// graphS is the smaller graph and graphB is the bigger graph
	ISOGraph graphS;
	ISOGraph[] graphBs;
	// Ullmann algorithm arrange the search order correspond respectively to the
	// points
	// of graphS in order of decreasing degree.
	int[] indexS; // indexS[index] = node
	int[] nodeS; // nodesS[node] = index
	// Array F is used to record which vertices in graphB has already been
	// mapped
	// if F[i] = true, then graphB(i) is used, i is the index of the node
	protected boolean[] F;
	// Array H is used to record the mapping between vertices in graphS and
	// graphB
	// if H[i]= k, then S(i) matched with B(k), i is index number, not node
	protected int[] H;
	// M records the partial mapping between S and B
	protected boolean[][] M;
	protected boolean[][][] MD;
	// For initialM, the first alreadyMappedIndex number of nodes has been
	// mapped successfully due to
	// previous knowledge, isomorphism test start from the
	// initialM[alreadymappedIndex]
	protected int alreadyMappedIndex;

	/**
	 * Initialization of setting
	 * 
	 * @param small
	 * @param big
	 */
	public ISOUllmannList(Graph small, Graph[] big) {
		graphS = new ISOGraph(small);
		// Difference1:
		graphBs = new ISOGraph[big.length];
		int bSize = 0;
		for (int i = 0; i < big.length; i++) {
			graphBs[i] = new ISOGraph(big[i]);
			if (bSize < graphBs[i].getNodeCount())
				bSize = graphBs[i].getNodeCount();
		}
		// end of Difference1

		int sSize = graphS.getNodeCount();

		// find the search order: sort by degree of nodes in graphS
		int[] degrees = new int[sSize];
		for (int i = 0; i < degrees.length; i++)
			degrees[i] = graphS.getDegree(i);
		this.indexS = new int[sSize];
		this.nodeS = new int[sSize];
		// indexS[i] = node, nodesS[node] = index
		bucketSort(degrees, this.indexS, nodeS);

		/**
		 * Initializing inner data members
		 */
		this.M = new boolean[sSize][bSize];
		this.MD = new boolean[sSize][sSize][bSize];
		alreadyMappedIndex = 0;
		this.H = new int[sSize];
		for (int i = 0; i < H.length; i++)
			H[i] = -1;
		this.F = new boolean[bSize];
		for (int i = 0; i < F.length; i++)
			F[i] = false;
	}

	/**
	 * Exactly the same A self implementation of bucket sort sort vertices
	 * according to their degree Input: map[i]
	 * 
	 * @param key
	 * @param object
	 * @return
	 */
	protected void bucketSort(int[] oriData, int[] afterData, int[] rafterData) {
		int min = oriData[0];
		int max = oriData[0];
		for (int i = 1; i < oriData.length; i++) {
			if (oriData[i] > max)
				max = oriData[i];
			else if (oriData[i] < min)
				min = oriData[i];
		}

		List<List<Integer>> buckets = new ArrayList<List<Integer>>(max - min
				+ 1);
		for (int i = min; i <= max; i++)
			buckets.add(new ArrayList<Integer>());
		// scan the key once
		for (int index = 0; index <= oriData.length; index++) {
			int keyValue = oriData[index];
			buckets.get(max - keyValue).add(index);
		}
		// sort:
		// After this sort, key[] is in decreasing order
		// objects[originalIndex] = newIndex
		for (int i = 0, k = 0; i < buckets.size(); i++) {
			for (int j = 0; j < buckets.get(i).size(); j++, k++) {
				afterData[k] = buckets.get(i).get(j);
				rafterData[buckets.get(i).get(j)] = k;
			}
		}
	}

	/**
	 * set initial matrix to start search for the ith graphB
	 * 
	 * @return
	 */
	protected boolean setInitialMatrix(int ith) {
		ISOGraph graphB = graphBs[ith];
		for (int indexI = 0; indexI < M.length; indexI++) {
			for (int nodeJ = 0; nodeJ < graphB.getNodeCount(); nodeJ++) {
				boolean flag = true;
				int nodeI = this.indexS[indexI];
				// test label
				if (graphS.getNodeLabel(nodeI) != graphB.getNodeLabel(nodeJ))
					flag = false;
				// test degree
				else if (graphS.getDegree(nodeI) > graphB.getDegree(nodeJ))
					flag = false;
				// can add further comparable test here
				M[indexI][nodeJ] = flag;
			}
		}
		return true;
	}

	public boolean[] isIsomorphic() {
		boolean[] isoResults = new boolean[graphBs.length];
		for (int i = 0; i < graphBs.length; i++) {
			setInitialMatrix(i);
			isoResults[i] = this.isIsomorphic(0, i);
		}
		return isoResults;
	}

	/**
	 * Do subgraph isomorphism test between GraphS and the ith GraphB
	 * Initialization step is complete
	 * 
	 * @return
	 */
	protected boolean isIsomorphic(int startingDepth, int ith) {
		// traceBack = false, the first time maps d, else not the first time
		boolean traceBack = false;
		if (refine(ith) == false)
			return false;
		int nodeCountS = graphS.getNodeCount();
		int nodeCountB = graphBs[ith].getNodeCount();
		for (int d = startingDepth; d < nodeCountS; d++) {
			// record a copy of mapping before depth d
			assign(MD[d], M, nodeCountS, nodeCountB);
			boolean findAK = false;
			// try to find a vertex K in graphB, which is matchable to vertex d
			// in graphS
			// If vertex d is never matched to any vertex(traceBack = false),
			// start search k from 0
			// else if k has matched with with d, then in this iteration,
			// start searching from H[d] +1;
			int k;
			// depth d is visited from depth d+1 or d, as a step of trace back
			if (traceBack) {
				// restore M to what what we have saved in MD[d]
				assign(M, MD[d], nodeCountS, nodeCountB);
				k = H[d] + 1;
				// In previous steps, d is matched with H[d], recover H and F
				F[H[d]] = false;
				H[d] = -1;
			}
			// depth d is visited from depth d-1
			else
				k = 0;
			// find vertex k, that is matchable with vertex d in graphS
			for (; k < nodeCountS; k++) {
				if (M[d][k] == false || F[k] == true)
					continue;
				// else: find a K that M[d][k] = true, F[k] = false
				else {
					findAK = true;
					break;
				}
			}
			// we find a match (d, k)
			if (findAK == true) {
				// 1.Set all "1" in this depth (d) to 0, except for k
				for (int j = 0; j < nodeCountB; j++)
					if (j != k)
						M[d][j] = false;
				M[d][k] = true;
				// 2. Update status: k is used, d is matched with k
				H[d] = k;
				F[k] = true;
				if (d + 1 == nodeCountS)
					return true;// find a successful match
				else if (refine(ith)) {
					traceBack = false;// keep on growing, go to step "for"
					continue; // continue searching for next vertex in graphS
								// (d+1)
				}
				// refine failed: kept on finding in this layer
				else {
					d = d - 1;
					traceBack = true;// go to step "for"
					continue; // continue searching for the dth layer
				}
			}
			// No K is available: trace back
			else {
				if (d == startingDepth)
					return false;
				// find match in death d stuck, re match depth d-1
				d = d - 2;
				traceBack = true;// continue searching for the d-1th layer
			}
		}
		return false;
	}

	/**
	 * Refinement process of early detection of failure partial match Refinement
	 * rule: for all vertices that has been matched, their adjacent vertices
	 * should also be matchable.
	 * 
	 * @return
	 */
	protected boolean refine(int ith) {
		boolean updated = true;
		int nodeCountS = graphS.getNodeCount();
		int nodeCountB = graphBs[ith].getNodeCount();
		while (updated) {
			updated = false;
			for (int i = 0; i < nodeCountS; i++) {
				boolean hasMatch = false;
				for (int j = 0; j < nodeCountB; j++) {
					if (M[i][j] != true)
						continue;
					if (canMatch(i, j, ith)) {
						hasMatch = true;
						break;
					} else {
						M[i][j] = false;
						updated = true;
					}
				}
				if (hasMatch == false)
					return false;
			}
		}
		return true;
	}

	/**
	 * The crux of ullman algorithm At any media state, if nodeS and nodeB are
	 * newly matched Their adjacent vertices have to matched and also their
	 * adjacent edges
	 * 
	 * @param nodeS
	 * @param nodeBArray
	 * @return
	 */
	protected boolean canMatch(int sindex, int bnode, int ith) {
		int snode = this.indexS[sindex];
		int degreeS = graphS.getDegree(snode);
		for (int i = 0; i < degreeS; i++) {
			boolean itherAdjHasAMap = false;
			int adjNodeS = graphS.getAdjacentNode(snode, i);
			int adjIndexS = this.nodeS[adjNodeS];
			if (this.H[adjIndexS] != -1)// this vertex has already been mapped
			{
				// find corresponding mapped vertex on super graph
				int adjNodeB = this.H[adjIndexS];
				if (graphS.getNodeLabel(adjNodeS) == graphBs[ith]
						.getNodeLabel(adjNodeB))
					itherAdjHasAMap = true;
				else
					return false;
			} else {
				for (int j = 0; j < graphBs[ith].getDegree(bnode); j++) {
					int adjNodeB = graphBs[ith].getAdjacentNode(bnode, j);
					if (this.F[adjNodeB])
						continue;
					if (M[adjIndexS][adjNodeB] == false)
						continue;
					// adjS maps with adjB, test edge(nodeS, adjS) and
					// edge(nodeB, adjB)
					else if (graphS.getEdgeLabel(snode, adjNodeS) != graphBs[ith]
							.getEdgeLabel(bnode, adjNodeB))
						continue;
					else {
						itherAdjHasAMap = true;
						break;
					}
				}
				// refine failed
				if (itherAdjHasAMap == false)
					return false;
			}
		}
		return true;
	}

	/**
	 * Copy data saved in data[][] into empty[][]
	 * 
	 * @param empty
	 * @param data
	 * @return
	 */
	protected boolean assign(boolean empty[][], boolean data[][], int Xsize,
			int Ysize) {
		if (empty.length != data.length)
			return false;
		for (int i = 0; i < empty.length && i < Xsize; i++) {
			if (empty[i].length != data[i].length)
				return false;
			for (int j = 0; j < empty[i].length && j < Ysize; j++)
				empty[i][j] = data[i][j];
		}
		return true;
	}
}
