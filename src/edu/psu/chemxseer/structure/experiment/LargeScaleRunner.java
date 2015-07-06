package edu.psu.chemxseer.structure.experiment;

import java.io.IOException;
import java.text.ParseException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;

public class LargeScaleRunner {
	// Memory Consumption Measurement
	// public static void main(String[] args) throws IOException,
	// ParseException{
	// // String queryName =
	// "/home/duy113/Experiment/LindexJournal/LargeScale/G16/uniformQueries";
	// // NoPostingFeatures queries = new NoPostingFeatures(queryName,
	// // MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
	//
	// for(int i = 16; i <= 20; i++){
	// String baseName = "/data/home/duy113/VLDBJExp/LargeScaleExp/G" + i + "/";
	// String dbFileName = baseName + "GraphDB" + i;
	// GraphParser dbParser = MyFactory.getSmilesParser();
	// GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
	// MyFactory.getSmilesParser());
	// System.out.println(i);
	// System.out.println("Gindex Mem");
	// Runtime rc = Runtime.getRuntime();
	// MemoryConsumptionCal.runGC();
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();
	// GindexConstructor gIndex = new GindexConstructor(false);
	// gIndex.loadIndex(baseName+"GindexDF/",
	// SubgraphSearch_Gindex.getIndexName());
	// MemoryConsumptionCal.runGC();
	// long end = rc.freeMemory();;
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// System.out.println("LindexDF Mem");
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();
	// LindexConstructor in_memoryIndex = new LindexConstructor();
	// in_memoryIndex.loadIndex(baseName+"LindexDF/",
	// SubgraphSearch_Lindex.getIndexName());
	// MemoryConsumptionCal.runGC();
	// long end =rc.freeMemory();;
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// System.out.println("FGindex Mem");
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();;
	// FGindexConstructor fgIndexSearcher = new FGindexConstructor(gDB);
	// fgIndexSearcher.loadIndex(baseName + "FGindex/",
	// SubgraphSearch_FGindex.getIn_MemoryIndexName());
	// MemoryConsumptionCal.runGC();
	// long end =rc.freeMemory();
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// System.out.println("Lindex+TCFG Mem");
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();;
	// LindexConstructor in_memoryIndex = new LindexConstructor();
	// in_memoryIndex.loadIndex(baseName+ "LindexTCFG/",
	// SubgraphSearch_LindexPlus.getIn_MemoryIndexName());
	// MemoryConsumptionCal.runGC();
	// long end = rc.freeMemory();;
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// }
	// }
	public static void main(String[] args) throws IOException, ParseException {
		String queryName = "/home/duy113/Experiment/LindexJournal/LargeScale/G16/uniformQueries";
		NoPostingFeatures queries = new NoPostingFeatures(queryName,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));

		for (int i = 16; i <= 20; i++) {
			String baseName = "/home/duy113/Experiment/LindexJournal/LargeScale/Gindex_Rebuild/"
					+ i + "/";
			String dbFileName = "/home/duy113/Experiment/LindexJournal/LargeScale/G"
					+ i + "/GraphDB" + i;
			GraphParser dbParser = MyFactory.getSmilesParser();
			GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
					MyFactory.getSmilesParser());
			System.out.println(i);
			BasicExpRunner runner = new BasicExpRunner(dbFileName, dbParser,
					baseName);
			runner.runExp2(queries, runner.loadIndex(0));
		}
	}

	// public static void main(String[] args) throws IOException,
	// ParseException{
	// for(int flag = 2; flag <= 4; flag++){
	// String baseName = "/opt/santa/VLDBJExp/LargeScaleExp/G17/";
	// String dbFileName = baseName + "GraphDB17";
	// GraphParser dbParser = MyFactory.getSmilesParser();
	// GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
	// MyFactory.getSmilesParser());
	// System.out.println("Gindex Mem");
	// Runtime rc = Runtime.getRuntime();
	// MemoryConsumptionCal.runGC();
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();
	// GindexConstructor gIndex = new GindexConstructor(false);
	// gIndex.loadIndex(baseName+"GindexDF" + flag+ "/",
	// SubgraphSearch_Gindex.getIndexName());
	// MemoryConsumptionCal.runGC();
	// long end = rc.freeMemory();;
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// System.out.println("LindexDF Mem");
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();
	// LindexConstructor in_memoryIndex = new LindexConstructor();
	// in_memoryIndex.loadIndex(baseName+"LindexDF" + flag + "/",
	// SubgraphSearch_Lindex.getIndexName());
	// MemoryConsumptionCal.runGC();
	// long end =rc.freeMemory();;
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// System.out.println("FGindex Mem");
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();;
	// FGindexConstructor fgIndexSearcher = new FGindexConstructor(gDB);
	// fgIndexSearcher.loadIndex(baseName + "FGindex" + flag + "/",
	// SubgraphSearch_FGindex.getIn_MemoryIndexName());
	// MemoryConsumptionCal.runGC();
	// long end =rc.freeMemory();
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// System.out.println("Lindex+TCFG Mem");
	// for(int j = 0; j< 10; j++){
	// MemoryConsumptionCal.runGC();
	// long start = rc.freeMemory();;
	// LindexConstructor in_memoryIndex = new LindexConstructor();
	// in_memoryIndex.loadIndex(baseName+ "LindexTCFG" + flag + "/",
	// SubgraphSearch_LindexPlus.getIn_MemoryIndexName());
	// MemoryConsumptionCal.runGC();
	// long end = rc.freeMemory();;
	// System.out.println(start-end);
	// MemoryConsumptionCal.runGC();
	// }
	//
	// }
	// }
}
