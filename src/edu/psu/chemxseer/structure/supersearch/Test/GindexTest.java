package edu.psu.chemxseer.structure.supersearch.Test;

import java.io.IOException;
import java.text.ParseException;

import edu.psu.chemxseer.structure.experiment.BasicExpBuilder;
import edu.psu.chemxseer.structure.experiment.BasicExpRunner;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;

public class GindexTest {
	public static void main(String[] args) throws IOException, ParseException {
		String baseName = "/Users/dayuyuan/Documents/workspace/Experiment2/";
		String dbName = baseName + "DBFile";

		BasicExpBuilder builder = new BasicExpBuilder(dbName,
				MyFactory.getSmilesParser(), baseName);
		ISearcher searcher = builder.buildGIndexDF(0.1, 0);

		PostingFeatures queries = FeatureProcessorG.frequentSubgraphMining(
				dbName, dbName + "patterns", dbName + "postings", 0.05, 2, 30,
				MyFactory.getSmilesParser());

		BasicExpRunner runner = new BasicExpRunner(dbName, null, dbName);
		runner.runExp2(queries.getFeatures(), searcher);

	}
}
