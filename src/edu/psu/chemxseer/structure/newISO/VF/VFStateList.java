package edu.psu.chemxseer.structure.newISO.VF;

import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

public class VFStateList extends VFState {

	public VFStateList(ISOGraph graphSmall, ISOGraph graphBig, int maxGraphBSize) {
		// Initialize common shared data member
		this.graphS = graphSmall;
		this.graphB = graphBig;
		coreS = new int[graphS.getNodeCount()];
		coreB = new int[maxGraphBSize];
		connectS = new int[coreS.length];
		connectB = new int[maxGraphBSize];
		for (int i = 0; i < coreS.length; i++) {
			coreS[i] = NULL_NODE;
			connectS[i] = NULL_NODE;
		}
		for (int i = 0; i < coreB.length; i++) {
			coreB[i] = NULL_NODE;
			connectB[i] = NULL_NODE;
		}
		// Pre-compute the search order of VF algorithm
		searchOrder = new int[graphS.getNodeCount()];
		findSearchOrder();

		// Initialize state owned data member
		currentDepth = 0;
		newlyAddedS = NULL_NODE;
		newlyAddedB = NULL_NODE;
		connectSNum = 0;
		connectBNum = 0;
	}

	public VFStateList(ISOGraph graphSmall, ISOGraph graphBig,
			VFStateList alreadyExistState) {
		if (graphSmall != alreadyExistState.graphS) {
			System.out.println("The smaller graph in alreadyExistState should "
					+ "be equal to graphSmall");
			return;
		}
		this.graphS = alreadyExistState.graphS;
		this.graphB = graphBig;
		coreS = alreadyExistState.coreS;
		coreB = alreadyExistState.coreB;
		connectS = alreadyExistState.connectS;
		connectB = alreadyExistState.connectB;
		// initialized
		for (int i = 0; i < coreS.length; i++) {
			coreS[i] = NULL_NODE;
			connectS[i] = NULL_NODE;
		}
		for (int i = 0; i < coreB.length; i++) {
			coreB[i] = NULL_NODE;
			connectB[i] = NULL_NODE;
		}
		// searchOrder is the same as that in alreadyExistState
		searchOrder = alreadyExistState.searchOrder;
		// Initialize state owned data member
		currentDepth = 0;
		newlyAddedS = NULL_NODE;
		newlyAddedB = NULL_NODE;
		connectSNum = 0;
		connectBNum = 0;
	}
}