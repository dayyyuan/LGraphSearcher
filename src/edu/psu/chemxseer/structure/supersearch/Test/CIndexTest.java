package edu.psu.chemxseer.structure.supersearch.Test;

public class CIndexTest {
	// public static void main(String[] args) throws IOException{
	// CIndexTest test = new CIndexTest();
	// test.topDownIndex();
	// }
	//
	// public void flatIndex() throws IOException{
	// String dbName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/graphDataSelected";
	// String featureFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/features";
	// String postingFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/postings";
	//
	// String baseName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/CIndexFlat/";
	// float minimumFrequency = 0.1f;
	//
	// PostingFeatures postingFeatures =
	// FeatureProcessorFG.frequentSubgraphMining(dbName, featureFileName,
	// postingFileName, minimumFrequency, 10, MyFactory.getSmilesParser());
	//
	// GraphDatabase gDB = new GraphDatabase_OnDisk(dbName,
	// MyFactory.getSmilesParser());
	//
	// SupSearch_CIndexFlatBuilder.buildCIndexFlat(gDB,
	// postingFeatures.getFeatures(), baseName, MyFactory.getDFSCoder());
	// SupSearch_CIndexFlat supSearcher =
	// SupSearch_CIndexFlatBuilder.loadCIndexFlat(gDB, baseName,
	// MyFactory.getDFSCoder());
	//
	// for(int i = 0; i< gDB.getTotalNum(); i++){
	// Graph query = gDB.findGraph(i);
	// int[] Number= new int[2];
	// long[] TimeComponent = new long[4];
	// List<GraphResult> result = supSearcher.getAnswer(query, TimeComponent,
	// Number);
	// System.out.println(result.size());
	// }
	// }
	//
	// public void bottomUpIndex() throws IOException{
	// String dbName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/graphDataSelected";
	// String featureFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/features";
	// String postingFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/postings";
	// String baseName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/CIndexBottomUp/";
	// float minimumFrequency = 0.1f;
	//
	// // PostingFeatures postingFeatures =
	// FeatureProcessorFG.frequentSubgraphMining(dbName, featureFileName,
	// // postingFileName, minimumFrequency, 10, MyFactory.getSmilesParser());
	// // NoPostingFeatures<OneFeatureImpl> firstLevelFeatures =
	// postingFeatures.getFeatures();
	// //
	// // PostingFeatures upperLevelFeature =
	// FeatureProcessorFG.frequentSubgraphMining(firstLevelFeatures,
	// featureFileName,
	// // postingFileName, minimumFrequency, 10, MyFactory.getSmilesParser());
	// //
	// // PostingFeatures[] upperLevelFeatures = new PostingFeatures[1];
	// // upperLevelFeatures[0] = upperLevelFeature;
	//
	// GraphDatabase gDB = new GraphDatabase_OnDisk(dbName,
	// MyFactory.getSmilesParser());
	//
	// // SupSearch_CIndexBottomUpBuilder.buildCIndexBottomUp(gDB,
	// firstLevelFeatures, upperLevelFeatures,
	// // baseName, MyFactory.getDFSCoder());
	// SupSearch_CIndexBottomUp index =
	// SupSearch_CIndexBottomUpBuilder.loadCIndexBottomUp(gDB, baseName, 2,
	// MyFactory.getDFSCoder());
	//
	// for(int i = 0; i< gDB.getTotalNum(); i++){
	// Graph query = gDB.findGraph(i);
	// long[] TimeComponent = new long[4];
	// int[] Number = new int[2];
	// List<GraphResult> result = index.getAnswer(query, TimeComponent, Number);
	// System.out.println(result.size());
	// }
	// }
	//
	// public void topDownIndex() throws IOException{
	// String query =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/graphDataSelected";
	// String database =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/supSearchDB";
	// String baseName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/CIndexTopDown/";
	// PostingFeatures temp = FeatureProcessorFG.frequentSubgraphMining(query,
	// database,
	// null, 0.1f, 10, MyFactory.getSmilesParser());
	//
	// GraphDatabase gDB1 = new GraphDatabase_InMem(temp.getFeatures());
	// GraphDatabase gDB2 = new GraphDatabase_OnDisk(query,
	// MyFactory.getSmilesParser());
	//
	// String mergedFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/graphDataMerged";
	// String classFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/classFile";
	// String featureFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/featureFile";
	// String postingFileName=
	// "/Users/dayuyuan/Documents/workspace/Experiment1/postingBase";
	// String[] postingFileNames = new String[4];
	// for(int i = 0; i< 4; i++){
	// postingFileNames[i] = postingFileName + i;
	// }
	// FeatureProcessorDuralClass featureProcessor = new
	// FeatureProcessorDuralClass(0.1f, 10);
	// featureProcessor.mergeGraphFile(gDB1, gDB2, MyFactory.getDFSCoder(),
	// mergedFileName, classFileName);
	// PostingFeaturesMultiClass features= featureProcessor.
	// frequentSubgraphMining(mergedFileName, classFileName, featureFileName,
	// postingFileNames, MyFactory.getDFSCoder());
	//
	// //1. Build the Index
	// SupSearch_CIndexTopDownBuilder.buildCIndexTopDown(gDB1,
	// features, 10, MyFactory.getDFSCoder(), baseName);
	// //2. Load the Index
	// SupSearch_CIndexTopDown searcher =
	// SupSearch_CIndexTopDownBuilder.loadCIndexFlat(gDB1, baseName,
	// MyFactory.getDFSCoder());
	//
	// }
}
