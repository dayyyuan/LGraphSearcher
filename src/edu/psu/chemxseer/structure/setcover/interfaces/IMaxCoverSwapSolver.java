package edu.psu.chemxseer.structure.setcover.interfaces;

import edu.psu.chemxseer.structure.setcover.IO.SetwithScore;

public interface IMaxCoverSwapSolver extends IMaxCoverSolver,
		Iterable<SetwithScore[]> {

	int getLindexTermPos(int setPos);

}
