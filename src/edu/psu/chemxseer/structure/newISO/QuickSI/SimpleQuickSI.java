package edu.psu.chemxseer.structure.newISO.QuickSI;

import edu.psu.chemxseer.structure.newISO.Util.GraphDBFrequency;
import edu.psu.chemxseer.structure.newISO.Util.ISOEdgeGraph;
import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

/**
 * An implementation of the quickSI algorithm, a simply version Implementation
 * details are not covered in the paper
 * 
 * @author dayuyuan
 * 
 */
public class SimpleQuickSI {
	protected ISOGraph graphS, graphB;
	// Array F is used to record which vertices in B has already been mapped
	// if F[i] = true, then graphB(i) is used
	protected boolean[] F;
	// Array H is used to record the mapping between vertices in graph S and
	// graph B
	// if H[i]= k, then S(i) matched with B(k)
	protected int[] H;
	protected QISequenceInterface sequence;
	protected static int NULL_NODE = -1;
	protected int currentDepth = -1;

	public SimpleQuickSI() {

	}

	public SimpleQuickSI(ISOEdgeGraph small, ISOGraph big, GraphDBFrequency gDB) {
		graphS = small;
		graphB = big;
		int sNodeCount = small.getNodeCount();
		int bNodeCount = big.getNodeCount();

		this.F = new boolean[bNodeCount];
		this.H = new int[sNodeCount];
		for (int i = 0; i < F.length; i++)
			F[i] = false;
		for (int i = 0; i < H.length; i++)
			H[i] = -1;
		this.sequence = new QISequence(small, gDB);
	}

	public boolean isIsomorphic() {
		return QuickSITest();
	}

	public boolean QuickSITest() {
		if (this.currentDepth == graphS.getNodeCount())
			return true;
		else if (this.currentDepth == 0) {
			// find all nodes in graphB that is matchable with the first node of
			// graphA
			for (int i = 0; i < graphB.getNodeCount(); i++) {
				if (match(currentDepth, i)) {
					this.H[sequence.getEntry(0).getNodeID()] = i;
					this.F[i] = true;
					currentDepth++;
					boolean canGrow = QuickSITest();
					if (canGrow == true)
						return true;
					else {
						// restore H and F
						this.H[sequence.getEntry(0).getNodeID()] = -1;
						this.F[i] = false;
						currentDepth--;
						continue;
					}
				}
			}
			return false;
		} else {
			int nodeS = this.sequence.getEntry(currentDepth).getNodeID();
			int parentS = this.sequence.getEntry(currentDepth).getParentID();
			int parentB = this.H[parentS];
			if (parentB == -1)
				System.out.println("It is wiredin line 60");
			for (int i = 0; i < graphB.getDegree(parentB); i++) {
				int nodeB = graphB.getAdjacentNode(parentB, i);
				if (match(currentDepth, nodeB)) {
					this.H[nodeS] = nodeB;
					this.F[nodeB] = true;
					currentDepth++;
					boolean canGrow = QuickSITest();
					if (canGrow == true)
						return true;
					else {
						// restore H and F
						this.H[sequence.getEntry(currentDepth).getNodeID()] = -1;
						this.F[i] = false;
						currentDepth--;
						continue;
					}
				}
			}
			return false; // can not find a graphB
		}

	}

	public boolean match(int sequenceIndex, int nodeIndexB) {
		int nodeIndexS = sequence.getEntry(sequenceIndex).getNodeID();
		// label
		if (sequence.getEntry(sequenceIndex).getNodeLabel() != graphB
				.getNodeLabel(nodeIndexB))
			return false;
		else {
			ConstrainEntry[] constrains = sequence.getEntry(sequenceIndex)
					.getConstrains();
			if (constrains == null)
				return true;
			else {
				for (int i = 0; i < constrains.length; i++) {
					// degree constrain
					if (constrains[i].isDegreeContrain()
							&& constrains[i].getContrainValue() > graphB
									.getDegree(nodeIndexB))
						return false;
					// backward edge constrain
					else if (constrains[i].isEdgeContrain()) {
						int aheadNodeS = this.sequence.getEntry(
								constrains[i].getContrainValue()).getNodeID();
						int aheadNodeB = this.H[aheadNodeS];
						if (aheadNodeB == -1
								|| graphS.getEdgeLabel(aheadNodeS, nodeIndexS) != graphB
										.getEdgeLabel(aheadNodeB, nodeIndexB))
							;
						return false;
					}
				}
				return true;
			}
		}
	}

}
