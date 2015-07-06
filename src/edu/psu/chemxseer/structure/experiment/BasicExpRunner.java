package edu.psu.chemxseer.structure.experiment;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.postings.Interface.IGraphs;
import edu.psu.chemxseer.structure.subsearch.FGindex.SubSearch_FGindexBuilder;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_GindexBuilder;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimplePlusBuilder;
import edu.psu.chemxseer.structure.subsearch.QuickSI.SubSearch_QuickSIBuilder;

public class BasicExpRunner extends BasicExpBuilder {

	// /**
	// * Run the Experiment For Basic Exp
	// * @param args
	// * @throws IOException
	// * @throws ParseException
	// */
	// public static void main(String[] args) throws IOException,
	// ParseException{
	//
	// // String dbFileName = "/data/santa/VLDBJExp/BasicExp/DBFile";
	// // String baseName = "/data/santa/VLDBJExp/BasicExp/";
	// String dbFileName =
	// "/home/duy113/Experiment/LindexJournal/BasicExp/DBFile";
	// String baseName = "/home/duy113/Experiment/LindexJournal/BasixExp/";
	// GraphParser dbParser = MyFactory.getSmilesParser();
	// BasicExpRunner exp = new BasicExpRunner(dbFileName, dbParser, baseName);
	// //Generate Queries:
	// try {
	// exp.genQueries(true);
	// } catch (MathException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// // exp.buildGindexTreeDelta();
	// // exp.buildLindexTreeDelta();
	//
	// //
	// // SubgraphSearch searcher = null;
	// //
	// // String queryFile =
	// "/home/duy113/Experiment/VLDBJExp/BasicExp/Queries/NormalQueryTest";
	// // InFrequentQueryGenerater qGen = new
	// InFrequentQueryGenerater(queryFile);
	// // IGraphs[] queries = qGen.loadInfrequentQueries();
	// //
	// // for(int i = 1; i< 11; i++){
	// // searcher = exp.loadIndex(i);
	// // exp.runExp(queries, searcher);
	// // }
	// }
	//
	// public void genQueries(boolean genTrainQueries) throws IOException,
	// ParseException, MathException{
	// GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(this.dbFileName,
	// this.dbParser);
	// File QueryFolder = new File(baseName + "Queries");
	// if(!QueryFolder.exists())
	// QueryFolder.mkdir();
	// InFrequentQueryGenerater2 queryGen =null;
	// String queryFileName1 = baseName + "Queries/UniformQueryTest2";
	// queryGen = new InFrequentQueryGenerater2(queryFileName1);
	// queryGen.generateInFrequentQueries(4, 28, 50, gDB, 0);
	// // queryGen = new InFrequentQueryGenerater(queryFileName1);
	// //
	// //
	// // FGindexConstructor fgIndexSearcher = new FGindexConstructor(gDB);
	// // fgIndexSearcher.loadIndex(baseName + "FGindex/",
	// SubgraphSearch_FGindex.getIn_MemoryIndexName());
	// // queryGen.generateInFrequentQueriesUniform(4, 24, 50, gDB,
	// fgIndexSearcher.getEdgeIndex());
	//
	// // String queryFileName2 = baseName + "Queries/NormalQueryTest";
	// // queryGen = new InFrequentQueryGenerater(queryFileName2);
	// // SubgraphSearch subgraphSearcher = this.loadIndex(7);
	// // queryGen.generateInFrequentQueriesNormal(4, 24, 1, gDB, edgeIndex,
	// subgraphSearcher);
	//
	// if(genTrainQueries){
	// // String queryFileName3 = baseName + "Queries/NormalQueryTrain";
	// // queryGen = new InFrequentQueryGenerater(queryFileName3);
	// // queryGen.generateInFrequentQueriesNormal2(4, 24, 1000, gDB, edgeIndex,
	// subgraphSearcher);
	//
	// // String queryFileName4 = baseName + "Queries/UniformQueryTrain";
	// // queryGen = new InFrequentQueryGenerater2(queryFileName4);
	// // queryGen.generateInFrequentQueries2(4, 24, 50, gDB, 0);
	// }
	//
	// }
	//
	public BasicExpRunner(String dbFileName, GraphParser dbParser,
			String baseName) {
		super(dbFileName, dbParser, baseName);
	}

	public ISearcher loadIndex(int i) throws IOException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
				dbParser);
		ISearcher searcher = null;
		SubSearch_GindexBuilder Gbuilder = new SubSearch_GindexBuilder();
		SubSearch_LindexSimpleBuilder Lbuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimplePlusBuilder LPbuilder = new SubSearch_LindexSimplePlusBuilder();
		SubSearch_QuickSIBuilder Qbuilder = new SubSearch_QuickSIBuilder();
		SubSearch_FGindexBuilder Fbuilder = new SubSearch_FGindexBuilder();
		// TODO: More experiments need to be run while lucene_in_mem is set true
		boolean lucene_im_mem = false;
		switch (i) {
		case 1:
			System.out.println("Load GindexDF");
			searcher = Gbuilder.loadIndex(gDB, false, baseName + "GindexDF/",
					dbParser, lucene_im_mem);
			break;
		case 2:
			System.out.println("Load LindexDF");
			searcher = Lbuilder.loadIndex(gDB, baseName + "LindexDF/",
					dbParser, lucene_im_mem);
			break;
		case 3:
			System.out.println("Load GindexDT");
			searcher = Gbuilder.loadIndex(gDB, false, baseName + "GindexDT/",
					dbParser, lucene_im_mem);
			break;
		case 4:
			System.out.println("Load LindexDT");
			searcher = Lbuilder.loadIndex(gDB, baseName + "LindexDT/",
					dbParser, lucene_im_mem);
			break;
		case 5:
			System.out.println("Load SwiftIndex");
			searcher = Qbuilder.loadIndex(gDB, baseName + "SwiftIndex/",
					dbParser, lucene_im_mem);
			break;
		case 6:
			System.out.println("Load FGindex");
			searcher = Fbuilder.loadIndex(gDB, baseName + "FGindex/", dbParser,
					lucene_im_mem);
			break;
		case 7:
			System.out.println("Load LindexTCFG");
			searcher = LPbuilder.loadIndex(gDB, baseName + "LindexTCFG/",
					dbParser, lucene_im_mem);
			break;
		case 8:
			System.out.println("Load GindexMimR");
			searcher = Gbuilder.loadIndex(gDB, true, baseName + "GindexMimR/",
					dbParser, lucene_im_mem);
			break;
		case 9:
			System.out.println("Load LindexMimR");
			searcher = Lbuilder.loadIndex(gDB, baseName + "LindexMimR/",
					dbParser, lucene_im_mem);
			break;
		case 10:
			System.out.println("Load LindexMimRPlus");
			searcher = LPbuilder.loadIndex(gDB, baseName + "LindexMimRPlus/",
					dbParser, lucene_im_mem);
			break;
		}
		return searcher;
	}

	public void runExp(IGraphs[] queries, ISearcher searcher)
			throws IOException, ParseException {
		long[] TimeComponent = new long[4];
		float[] Number = new float[3];
		for (int i = 0; i < queries.length; i++) {
			long start = System.currentTimeMillis();
			TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
			Number[0] = Number[1] = Number[2] = 0;
			int counter = 0;
			for (int j = 0; j < queries[i].getGraphNum(); j++) {
				long[] TimeComponent1 = new long[4];
				int[] Number1 = new int[2];
				Graph g = queries[i].getGraph(j);
				List<IGraphResult> answers = searcher.getAnswer(g,
						TimeComponent1, Number1);
				if (answers.size() == 0)
					continue;
				counter++;
				TimeComponent[0] += TimeComponent1[0];
				TimeComponent[1] += TimeComponent1[1];
				TimeComponent[2] += TimeComponent1[2];
				TimeComponent[3] += TimeComponent1[3];
				Number[0] += Number1[0];
				Number[1] += Number1[1];
				// if(Number1[1]!=queries[i].getSupport(j)){
				// int realCount = queries[i].getSupport(j);
				// System.out.println("bugs: " + Number1[0]+","+ Number1[1] +
				// ", "+ realCount);
				// //searcher.getAnswer(g, TimeComponent1, Number1);
				// }
				Number[2] += Number[0] / Number[1];
			}
			// long totalTime = System.currentTimeMillis() - start;
			// System.out.println((double)totalTime/ (double)(TimeComponent[0] +
			// TimeComponent[1] + TimeComponent[2] + TimeComponent[3] ));
			System.out.print("For queries: " + (i + 4) + "\t");
			System.out.print(TimeComponent[0] + "\t" + TimeComponent[1] + "\t"
					+ TimeComponent[2] + "\t" + TimeComponent[3] + "\t");
			System.out.println(Number[0] + "\t" + Number[1] + "\t" + Number[2]
					+ "\t" + counter);
		}
	}

	public void runExp2(NoPostingFeatures queries, ISearcher searcher)
			throws IOException, ParseException {
		long[] TimeComponent = new long[4];
		float[] Number = new float[3];
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = Number[2] = 0;
		int counter = 0;

		for (int i = 0; i < queries.getfeatureNum(); i++) {
			long[] TimeComponent1 = new long[4];
			int[] Number1 = new int[2];
			Graph g = queries.getFeature(i).getFeatureGraph();
			List<IGraphResult> answers = searcher.getAnswer(g, TimeComponent1,
					Number1);
			if (answers.size() == 0)
				continue;
			/*
			 * if(answers.size()!= queries.getFeature(i).getFrequency())
			 * System.out.println("lala");
			 */
			counter++;
			TimeComponent[0] += TimeComponent1[0];
			TimeComponent[1] += TimeComponent1[1];
			TimeComponent[2] += TimeComponent1[2];
			TimeComponent[3] += TimeComponent1[3];
			Number[0] += Number1[0];
			Number[1] += Number1[1];
			Number[2] += Number[0] / Number[1];
		}

		System.out.print(TimeComponent[0] + "\t" + TimeComponent[1] + "\t"
				+ TimeComponent[2] + "\t" + TimeComponent[3] + "\t");
		System.out.println(Number[0] + "\t" + Number[1] + "\t" + Number[2]
				+ "\t" + counter);
	}
}
