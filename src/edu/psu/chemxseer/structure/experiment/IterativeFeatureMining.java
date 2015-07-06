package edu.psu.chemxseer.structure.experiment;

/**
 * The experiment for the Iterative Feature Mining Task (ICDE 2012)
 * 
 * @author dayuyuan
 * 
 */
public class IterativeFeatureMining {
	// protected String dbFileName;
	// protected GraphParser dbParser;
	// protected String baseName;
	//
	// protected double minFreq;
	// protected double gamma; // the parameter finding the lower bound of the
	// objective function
	// protected double alpha; // the parameter for the combination of the lower
	// bound and upper bound
	//
	//
	// public static void main(String[] args) throws Exception{
	// //1. build the old index
	// String dbFileName = null;
	// GraphParser dbParser = MyFactory.getSmilesParser();
	// String baseName = null;
	//
	// IterativeFeatureMining exp = new IterativeFeatureMining(dbFileName,
	// dbParser, baseName);
	// GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
	// dbParser);
	// exp.buildOldIndex(gDB);
	// //2. build the DF Index
	// exp.InsertDFFeatures();
	// exp.InsertTCFGFeatures();
	// }
	//
	// public IterativeFeatureMining(String dbFileName, GraphParser dbParser,
	// String baseName){
	// this.dbFileName = dbFileName;
	// this.dbParser = dbParser;
	// this.baseName = baseName;
	// }
	// /**
	// * Build one Gindex, one FGindex, one MimR index as the initial index
	// * Build
	// * @throws IOException
	// * @throws ParseException
	// * @throws MathException
	// */
	// public void buildOldIndex(GraphDatabase_OnDisk gDB) throws IOException,
	// ParseException, MathException{
	// BasicExpBuilder builder = new BasicExpBuilder(dbFileName, dbParser,
	// baseName + "1/");
	// builder.buildGIndexDF((float)0.05);
	// builder.buildLindexDF();
	// SubgraphSearch_FGindex fgIndex = builder.buildFGindex((float)0.02);
	// builder.buildLindexTCFG();
	//
	// //Generate Train Raw Queries
	// InFrequentQueryGenerater gen = new InFrequentQueryGenerater(baseName +
	// "1/TrainQueryRaw");
	// gen.generateInFrequentQueriesUniform2(4, 20, 10000, gDB,
	// fgIndex.getEdgeIndex());
	//
	// builder = new BasicExpBuilder(dbFileName, dbParser, baseName + "2/");
	// builder.buildGIndexDF((float)0.02);
	// builder.buildLindexDF();
	// builder.buildFGindex((float)0.025);
	// builder.buildLindexTCFG();
	//
	// //MIMR Features for further exploration
	// }
	//
	// private SubgraphSearch_LindexUpdate loadIndex(int i, String theBaseName)
	// throws IOException{
	// GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
	// dbParser);
	// SubgraphSearch_Lindex searcher = null;
	// SubgraphSearch_LindexBuilder Lbuilder = new
	// SubgraphSearch_LindexBuilder();
	//
	// switch(i){
	// case 1:
	// System.out.println("Load LindexDF");
	// searcher = Lbuilder.loadIndex(gDB, theBaseName+"LindexDF/", dbParser);
	// break;
	// case 2:
	// System.out.println("Load LindexTCFG");
	// searcher = Lbuilder.loadIndex(gDB, theBaseName+"LindexTCFG/", dbParser);
	// break;
	// case 3:
	// System.out.println("Load LindexMimR");
	// searcher = Lbuilder.loadIndex(gDB, theBaseName+"LindexMimR/", dbParser);
	// }
	// return new SubgraphSearch_LindexUpdate(searcher, gDB);
	// }
	//
	// public void InsertDFFeatures() throws Exception{
	// SubgraphSearch_LindexUpdate lindex = this.loadIndex(1, baseName + "1/");
	// //2.1 Generate the test queries
	// TestingQueries DFQueries = new TestingQueries();
	// HashMap<Graph, Integer> qMaps = DFQueries.loadQueriesRaw(baseName +
	// "1/TrainQueryRaw");
	// DFQueries.gapsFiltering(qMaps, lindex, baseName + "1/DFTrainQuery",
	// false);
	// ArrayList<TestQuery> testQueries = DFQueries.loadQueries(baseName +
	// "1/DFTrainQuery");
	//
	// FeatureFinder fFinder = new FeatureFinder(lindex, testQueries, minFreq,
	// true, gamma, alpha);
	// for(int w = 1; w< 4; w++){
	// long[] time = new long[1];
	// time[0] = 0;
	// BBAlgorithm(30, fFinder, time);
	// System.out.print(time[0]);
	// //1. Save the Index
	// SubgraphSearch_LindexupdateBuilder builder = new
	// SubgraphSearch_LindexupdateBuilder();
	// builder.saveIndex(lindex, baseName + "1/BBLindexDF_" + w + "/");
	// }
	// for(int w = 1; w< 4; w++){
	// long[] time = new long[1];
	// time[0] = 0;
	// QGAlgorithm(30, fFinder, time);
	// System.out.print(time[0]);
	// //1. Save the Index
	// SubgraphSearch_LindexupdateBuilder builder = new
	// SubgraphSearch_LindexupdateBuilder();
	// builder.saveIndex(lindex, baseName + "1/QGLindexDF_" + w + "/");
	// }
	// for(int w = 1; w< 4; w++){
	// long[] time = new long[1];
	// time[0] = 0;
	// TKAlgorithm(30, fFinder, time);
	// System.out.print(time[0]);
	// //1. Save the Index
	// SubgraphSearch_LindexupdateBuilder builder = new
	// SubgraphSearch_LindexupdateBuilder();
	// builder.saveIndex(lindex, baseName + "1/TKLindexDF_" + w + "/");
	// }
	// }
	//
	// public void InsertTCFGFeatures() throws Exception{
	// SubgraphSearch_LindexUpdate lindex = this.loadIndex(2, baseName + "1/");
	// //2.1 Generate the test queries
	// TestingQueries DFQueries = new TestingQueries();
	// HashMap<Graph, Integer> qMaps = DFQueries.loadQueriesRaw(baseName +
	// "1/TrainQueryRaw");
	// DFQueries.gapsFiltering(qMaps, lindex, baseName + "1/TCFGTrainQuery",
	// false);
	// ArrayList<TestQuery> testQueries = DFQueries.loadQueries(baseName +
	// "1/TCFGTrainQuery");
	//
	// FeatureFinder fFinder = new FeatureFinder(lindex, testQueries, minFreq,
	// true, gamma, alpha);
	// for(int w = 1; w< 4; w++){
	// long[] time = new long[1];
	// time[0] = 0;
	// BBAlgorithm(30, fFinder, time);
	// System.out.print(time[0]);
	// //1. Save the Index
	// SubgraphSearch_LindexupdateBuilder builder = new
	// SubgraphSearch_LindexupdateBuilder();
	// builder.saveIndex(lindex, baseName + "1/BBLindexTCFG_" + w + "/");
	// }
	// for(int w = 1; w< 4; w++){
	// long[] time = new long[1];
	// time[0] = 0;
	// QGAlgorithm(30, fFinder, time);
	// System.out.print(time[0]);
	// //1. Save the Index
	// SubgraphSearch_LindexupdateBuilder builder = new
	// SubgraphSearch_LindexupdateBuilder();
	// builder.saveIndex(lindex, baseName + "1/QGLindexTCFG_" + w + "/");
	// }
	// for(int w = 1; w< 4; w++){
	// long[] time = new long[1];
	// time[0] = 0;
	// TKAlgorithm(30, fFinder, time);
	// System.out.print(time[0]);
	// //1. Save the Index
	// SubgraphSearch_LindexupdateBuilder builder = new
	// SubgraphSearch_LindexupdateBuilder();
	// builder.saveIndex(lindex, baseName + "1/TKLindexTCFG_" + w + "/");
	// }
	// }
	//
	//
	// private void BBAlgorithm(int K, FeatureFinder fFinder, long[] time){
	// for(int i =0; i< K; i++){
	// long start = System.currentTimeMillis();
	// CandidateInfo newFeature = fFinder.findFeatureBB();
	// if(newFeature!=null)
	// time[0] += System.currentTimeMillis()-start;
	// fFinder.addNewFeature(newFeature);
	// }
	// }
	//
	// private void QGAlgorithm(int K, FeatureFinder fFinder, long[] time){
	// for(int i =0; i< K; i++){
	// long start = System.currentTimeMillis();
	// CandidateInfo newFeature = fFinder.findFeatureBB();
	// if(newFeature!=null)
	// time[0] += System.currentTimeMillis()-start;
	// fFinder.addNewFeature(newFeature);
	// }
	// }
	// private void TKAlgorithm(int K, FeatureFinder fFinder, long[] time){
	// int count = 0;
	// while(count < K){
	// long start = System.currentTimeMillis();
	// List<CandidateInfo> newFeatures = fFinder.findTopKFeatures(0.8);
	// if(newFeatures.size()!=0)
	// time[0] += System.currentTimeMillis()-start;
	// count += newFeatures.size();
	// for(CandidateInfo newFeature: newFeatures)
	// fFinder.addNewFeature(newFeature);
	// }
	// }

}
