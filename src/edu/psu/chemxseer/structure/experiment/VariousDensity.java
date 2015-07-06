package edu.psu.chemxseer.structure.experiment;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.math.MathException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.PreProcessTools2;
import edu.psu.chemxseer.structure.query.InFrequentQueryGenerater2;

public class VariousDensity {

	private String baseName;

	public VariousDensity(String baseName) {
		this.baseName = baseName;
	}

	public void preprocess() throws IOException {

		for (int i = 1; i < 6; i++) {
			String folderName = baseName + i + "/";
			File folder = new File(folderName);
			if (!folder.exists())
				folder.mkdirs();
			PreProcessTools2.covertToSdtGraph(baseName + "50_0." + i + ".data",
					folderName + "DBFile", MyFactory.getUnCanDFS());
		}
	}

	public static void main(String[] args) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException,
			MathException {
		String baseName = "/data/home/duy113/VLDBJExp/GenerateExp_update/";
		// String baseName =
		// "/home/duy113/Experiment/LindexJournal/GeneratedExp/";
		// VariousDensity exp = new VariousDensity(baseName);
		// try {
		// exp.preprocess();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		for (int i = 1; i < 6; i++) {
			String dbName = baseName + i + "/DBFile";
			String baseNameI = baseName + i + "/";
			BasicExpBuilder builder = new BasicExpBuilder(dbName,
					MyFactory.getDFSCoder(), baseNameI);
			builder.buildGIndexDF(0.1, 0);
			builder.buildLindexDF(0);
			builder.buildFGindex(0.03, 0);
			builder.buildLindexAdvTCFG(0);
		}

		// Generate Queries
		for (int i = 1; i < 6; i++) {
			System.out.println("Generate Queries: " + i);
			String dbName = baseName + i + "/DBFile";
			String baseNameI = baseName + i + "/";
			GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbName,
					MyFactory.getDFSCoder());
			InFrequentQueryGenerater2 queryGen = new InFrequentQueryGenerater2();
			queryGen.generateInFrequentQueries2(4, 30, 1000, 0.01, gDB, 0,
					baseNameI + "uniformQueries");
		}

	}

}
