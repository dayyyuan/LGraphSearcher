package edu.psu.chemxseer.structure.supersearch.Test;

import java.io.IOException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_MultiClass_Iterative;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.supersearch.LWSetCover.Stream_FeatureSelector2;

public class LWSetCoverTest2 {
	public static void main(String[] args) throws IOException {

		GraphParser gParser = MyFactory.getUnCanDFS();
		double minSupt = 0.01;
		String baseName = "/Users/dayuyuan/Documents/Experiment/SupSearch/";
		IGraphDatabase trainDB = new GraphDatabase_OnDisk(baseName + "DBFile2",
				MyFactory.getUnCanDFS());

		IGraphDatabase trainQuery = new GraphDatabase_OnDisk(baseName
				+ "TrainQuery", MyFactory.getSmilesParser());

		GSpanMiner_MultiClass_Iterative gen = FeatureProcessorDuralClass
				.getPatternEnumerator(trainDB, trainQuery, trainDB.getParser(),
						baseName, 0.01, -1, 10);
		Stream_FeatureSelector2 selector2 = new Stream_FeatureSelector2();
		NoPostingFeatures<OneFeatureMultiClass> selectedFeatures2 = selector2
				.minePrefixFeatures(gen, trainDB, 100);
		selectedFeatures2.saveFeatures(baseName + "stream2");

	}

}
