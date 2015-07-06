package edu.psu.chemxseer.structure.newISO.VF;

import edu.psu.chemxseer.structure.newISO.Util.ISOGraph;

public class ISOVF {
	protected VFState seedState;
	protected ISOGraph graphS, graphB;

	public ISOVF(ISOGraph small, ISOGraph big) {
		ISOGraph graphS = small;
		ISOGraph graphB = big;
		seedState = new VFState(graphS, graphB);
	}

	public ISOVF() {

	}

	public boolean isIsomorphic() {
		return match(seedState);
	}

	// Depth first search, branches are stored in candidateS and candidateB
	protected boolean match(VFState currentState) {
		// if the currentState covers all the nodes of graphS, then return true;
		if (currentState.isGoal())
			return true;
		else {
			// Compute the set P(s) of the pairs candidate for inclusion in M(s)
			// Also, here the candidate pair have to pass the feasibility test
			// F(s,candidateS, candidateB)
			int candidateS = currentState.getCandidateS();
			int[] candidateB = currentState.getCandidateB();
			if (candidateB == null)
				return false; // can not go further
			for (int i = 0; i < candidateB.length; i++) {
				// Compute the state s' obtained by adding (candidateS, B) to
				// M(s)
				VFState newState = new VFState(currentState, candidateS,
						candidateB[i]);
				if (match(newState))
					return true;
				else {
					// Restore the common shared data member to state:
					// currentState
					newState.restore();
					newState = null;
					continue;
				}
			}
		}
		// we failed to find a feasible deeper matching on the SSR tree
		return false;
	}
}
