package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.math.MathException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.query.InFrequentQueryGenerater2;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class Experiment {
	public static void main(String[] args) throws IOException, ParseException,
			MathException {
		if (args == null || args.length == 0)
			return;
		else {
			BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
			String baseName = "/data/home/duy113/VLDBSetCover4/";

			String baseName8 = baseName + "AIDS_0.008Q/";
			String baseName3 = baseName + "AIDS_0.003Q/";
			// 1. Create above folders, fill the DataAIDS files
			// 2. Generate the queries
			// generateQueries();
			// 3. Construct indexes
			// writer.write("Build Original Index8\n");
			// IndexConstruction.buildOriginalIndex(writer, baseName8, true);
			// writer.write("Run Index8 with Query 3\n");
			// IndexConstruction.runOriginalIndex(writer, baseName8, baseName3);
			// writer.write("Build Original Index3\n");
			// IndexConstruction.buildOriginalIndex(writer, baseName3, false);
			// writer.write("Run Index3 with Query 8\n");
			// IndexConstruction.runOriginalIndex(writer, baseName3, baseName8);
			// 4. Update HerusiticsIndexes
			// writer.write("Update Herusitics with Query 8\n");
			// IndexUpdateForHerusitics.updateHerusitics(writer, baseName8,
			// baseName8);
			// writer.write("Update Herusitics with Query 3\n");
			// IndexUpdateForHerusitics.updateHerusitics(writer, baseName8,
			// baseName3);

			// 5. Update Query Changes: may not work, plan changed
			/*
			 * String baseName328 = baseName + "AIDS_328/"; String baseName823 =
			 * baseName + "AIDS_823/";
			 * writer.write("Update GreedyDecompose build on Query 3 with Query 8\n"
			 * ); IndexUpdateForQueries.updateIndexForQueries(writer, baseName3,
			 * baseName8, baseName328);
			 * writer.write("Update GreedyDecompose build on Query 8 with Query 3\n"
			 * ); IndexUpdateForQueries.updateIndexForQueries(writer, baseName8,
			 * baseName3, baseName823);
			 */

			// 6. Update DB Changes: try to see if it works, tested on EMOL data
			// set again.
			String baseNameNCI = baseName + "AIDSEMOL/";
			String baseNameNCIData = baseNameNCI + "Ori/";
			IndexUpdateForDB.preProcessing0(baseNameNCIData);
			// IndexUpdateForDB.preProcessDBChanage(baseNameNCI);
			// String folder = baseNameNCI + "2/";
			// writer.write("Re-construct new indexed on the newDB and newQuery on 20% mix\n");
			// IndexConstruction.buildOriginalIndex(writer, folder, true);;
			// writer.write("Re-based old index in AIDS_0.008 with the newDatabase & run newQuery");
			// IndexUpdateForDB.reconstructIndexForNewDB(writer, folder,
			// baseName8);
			// writer.write("Update the older index with new indexes to acomodate the change");
			// IndexUpdateForDB.updateIndexForNewDB(writer, folder);

			// 7. Update with Difference FeatureCount;
			// writer.write("Run index update with different feature count: ");
			// IndexWithDifFeatureCount.buildOriginalIndex(writer, baseName3);
			IndexWithDifFeatureCount.runOriginalIndex(writer, baseName3,
					baseName8);
			// IndexWithDifFeatureCount.buildOriginalIndex(writer, baseName8);
			// IndexWithDifFeatureCount.updateOriginalIndex(writer, baseName3,
			// baseName8);

			writer.flush();
			writer.close();
		}
	}

	public static void generateQueries() {
		String rawQueryFile = "/data/home/duy113/VLDB13/AIDS/subSearch/Query_Together_raw";
		String baseName3 = "/data/home/duy113/VLDBSetCover4/AIDS_0.003Q/";
		String baseName8 = "/data/home/duy113/VLDBSetCover4/AIDS_0.008Q/";
		NoPostingFeatures<IOneFeature> rawQuery = new NoPostingFeatures<IOneFeature>(
				rawQueryFile,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));

		InFrequentQueryGenerater2 queryGen = new InFrequentQueryGenerater2();
		try {
			queryGen.generateInFrequentQueries2(4, 100, 10000 + 2000, rawQuery,
					0, baseName3 + "Query_Together");
			IGraphDatabase Data2_Together = new GraphDatabase_InMem(
					new NoPostingFeatures<IOneFeature>(baseName3
							+ "Query_Together", MyFactory
							.getFeatureFactory(FeatureFactoryType.OneFeature)));
			RandomChoseDBGraph.randomlySplitDBGraph(Data2_Together, 10000,
					baseName3 + "TrainQuery", baseName3 + "TestQuery");

			GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(baseName8
					+ "DataAIDS", MyFactory.getSmilesParser());
			queryGen.generateInFrequentQueries2(4, 100, 10000 + 2000, 0.008,
					gDB, 0, baseName8 + "Query_Together");
			Data2_Together = new GraphDatabase_InMem(
					new NoPostingFeatures<IOneFeature>(baseName8
							+ "Query_Together", MyFactory
							.getFeatureFactory(FeatureFactoryType.OneFeature)));
			RandomChoseDBGraph.randomlySplitDBGraph(Data2_Together, 10000,
					baseName8 + "TrainQuery", baseName8 + "TestQuery");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
