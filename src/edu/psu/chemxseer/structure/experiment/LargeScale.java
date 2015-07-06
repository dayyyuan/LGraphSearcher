package edu.psu.chemxseer.structure.experiment;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.math.MathException;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * Large Scale Experiment: 16-20
 * 
 * @author dayuyuan
 * 
 */
public class LargeScale {
	/**
	 * Problem of Gindex 0.1: to few frequent features are selected {especially
	 * the ">4 frequent features"} Problem of FGindex 0.03: too few frequent
	 * features are selected
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws MathException
	 */

	public static void main2(String[] args) throws IOException, ParseException {
		for (int i = 17; i < 18; i++) {
			String rootName = "/opt/santa/VLDBJExp/";
			String dbFileName = rootName + "LargeScaleExp/G" + i + "/GraphDB"
					+ i;
			String baseName = rootName + "LargeScaleExp/G" + i + "/";
			GraphParser gParser = MyFactory.getSmilesParser();

			BasicExpBuilder exp = new BasicExpBuilder(dbFileName, gParser,
					baseName);

			System.out
					.println("Build Gindex with max-min Support: 0.06: Flag 2");
			exp.buildGIndexDF(0.06, 2);
			// exp.buildLindexDF(2);
			System.out
					.println("Build Gindex with max-min Support: 0.03: Flag 3");
			exp.buildGIndexDF(0.03, 3);
			// exp.buildLindexDF(3);

			// System.out.println("Build FGindex with max-min Support: 0.02: Flag 2");
			// exp.buildFGindex(0.02, 2);
			// exp.buildLindexAdvTCFG(2);
			// System.out.println("Build FGindex with max-min Support: 0.01: Flag 3");
			// exp.buildFGindex(0.01, 3);
			// exp.buildLindexAdvTCFG(3);

			System.out
					.println("Build Gindex with max-min Support: 0.02: Flag 4");
			exp.buildGIndexDF(0.02, 4);
			// exp.buildLindexDF(4);
			System.out
					.println("Build Gindex with max-min Support: 0.01: Flag 5");
			// exp.buildGIndexDF(0.01, 5);
			// exp.buildLindexDF(5);
			//
			// System.out.println("Build FGindex with max-min Support: 0.008: Flag 4");
			// exp.buildFGindex(0.008, 4);
			// exp.buildLindexAdvTCFG(4);
			// System.out.println("Build FGindex with max-min Support: 0.006: Flag 5");
			// exp.buildFGindex(0.006, 5);
			// exp.buildLindexAdvTCFG(5);
		}
	}

	public static void main(String[] args) throws IOException, ParseException,
			MathException {
		for (int i = 16; i < 21; i++) {
			String rootName = "/data/home/duy113/VLDBJExp/";
			String dbFileName = rootName + "LargeScaleExp/G" + i + "/GraphDB"
					+ i;
			String baseName = rootName + "LargeScaleExp/G" + i + "/";
			GraphParser gParser = MyFactory.getSmilesParser();

			// Mine Queries
			// if(i == 16){
			// System.out.println("Mine Frequent Features");
			// InFrequentQueryGenerater2 queryGen = new
			// InFrequentQueryGenerater2();
			// GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
			// MyFactory.getSmilesParser());
			// queryGen.generateInFrequentQueries2(4, 30, 1000, 0.005, gDB, 0,
			// baseName + "uniformQueries");
			// }

			BasicExpBuilder exp = new BasicExpBuilder(dbFileName, gParser,
					baseName);

			System.out.println("Build Gindex with max-min Support: 0.1");
			exp.buildGIndexDF(0.1, 0);
		}
		main2(args);
	}
}
