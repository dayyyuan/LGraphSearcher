package edu.psu.chemxseer.structure.experiment;

public class LargeScale2 {
	// private String featureBaseName;
	// public LargeScale2(String dbFileName,GraphParser dbParser, String
	// baseName, String featureBaseName) {
	// super(dbFileName, dbParser, baseName);
	// this.featureBaseName = featureBaseName;
	// }
	//
	// public static void main(String[] args) throws IOException,
	// ParseException{
	// int i = 21;
	//
	// String rootName = "/opt/santa/VLDBJExp/";
	// String dbFileName = rootName + "LargeScaleExp/G" + i + "/GraphDB" + i;
	// String baseName = rootName + "LargeScaleExp/G" + i + "/";
	// String featureFileBaseName = rootName + "LargeScaleExp/G20/";
	// GraphParser dbParser = MyFactory.getSmilesParser();
	// LargeScale2 exp = new LargeScale2(dbFileName, dbParser, baseName,
	// featureFileBaseName);
	//
	// exp.buildGIndexDF();
	// exp.buildLindexDF();
	// exp.buildFGindex();
	// exp.buildLindexAdvTCFG();
	//
	// //
	// // SubgraphSearch searcher = null;
	// // String queryFile = rootName + "LargeScaleExp/G" + i + "/Queries";
	// //
	// // IGraphs[] queries = new Graphs[1];
	// // queries[0] = new Graphs(queryFile);
	// // queries[0].loadGraphs();
	// // for(int w = 1; w< 2; w++){
	// // searcher = exp.loadIndex(w);
	// // exp.runExp(queries, searcher);
	// // }
	// // for(int w = 6; w< 7; w++){
	// // searcher = exp.loadIndex(w);
	// // exp.runExp(queries, searcher);
	// // }
	// }
	//
	// public void buildGIndexDF() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "GindexDF/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Use DF Features & Merge with new edge features
	// PostingFeatures edgeFeatures =
	// FeatureProcessorL.findEdgeOneFeatures(dbFileName, temp+"edge",
	// temp+"edgePosting", dbParser);
	// PostingFeatures oriFeatures = new PostingFeatures(featureBaseName +
	// "GindexDF/GPatterns", featureBaseName + "GindexDF/GPostings");
	// oriFeatures.loadFeatures();
	//
	// NoPostingFeatures_Ext features = new
	// NoPostingFeatures_Ext(FeatureProcessorL.mergeFeatures(oriFeatures,
	// edgeFeatures));
	//
	// //2. Build Index
	// SubgraphSearch_GindexBuilder builder = new
	// SubgraphSearch_GindexBuilder();
	// builder.buildIndexWithoutFeatureSelection(features, new
	// GraphDatabase_OnDisk(this.dbFileName, dbParser), false, temp, dbParser);
	// }
	//
	// public void buildLindexDF() throws CorruptIndexException,
	// LockObtainFailedException, IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "LindexDF/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Use DF features
	// PostingFeatures edgeFeatures =
	// FeatureProcessorL.findEdgeOneFeatures(dbFileName, temp+"edge",
	// temp+"edgePosting", dbParser);
	// PostingFeatures oriFeatures = new PostingFeatures(featureBaseName +
	// "GindexDF/GPatterns", featureBaseName + "GindexDF/GPostings");
	// oriFeatures.loadFeatures();
	// NoPostingFeatures_Ext features = new
	// NoPostingFeatures_Ext(FeatureProcessorL.mergeFeatures(oriFeatures,
	// edgeFeatures));
	// //2. Build Index
	// SubgraphSearch_LindexBuilder builder = new
	// SubgraphSearch_LindexBuilder();
	// builder.buildIndex(features, new GraphDatabase_OnDisk(dbFileName,
	// dbParser), temp, dbParser);
	// }
	//
	// public void buildFGindex() throws IOException{
	// //0. Create Folder
	// String temp = baseName + "FGindex/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Load old frequent features
	// FGFeatures candidateFeatures = new FGFeatures(featureBaseName +
	// "FGindex/patterns", featureBaseName + "FGindex/postings");
	// candidateFeatures.loadFeatures();
	// //2. Mine Frequent TCFG & Construct Index
	// SubgraphSearch_FGindexBuilder builder = new
	// SubgraphSearch_FGindexBuilder();
	// builder.buildIndex(candidateFeatures , new
	// GraphDatabase_OnDisk(dbFileName, dbParser), temp, dbParser);
	// }
	//
	// public void buildLindexAdvTCFG() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "LindexTCFG/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Mine-edge features & load previous mined FG features
	// PostingFeatures edgeFeatures =
	// FeatureProcessorL.findEdgeOneFeatures(dbFileName, temp+"edge",
	// temp+"edgePosting", dbParser);
	// PostingFeatures subgraphFeatures = new PostingFeatures(featureBaseName +
	// "FGindex/patterns", featureBaseName + "FGindex/postings");
	// subgraphFeatures.loadFeatures();
	// //2. Combine this two features
	// PostingFeatures features =
	// FeatureProcessorL.mergeFeatures(subgraphFeatures, edgeFeatures);
	// NoPostingFeatures_Ext selectedFeatures = new
	// NoPostingFeatures_Ext(features.getSelectedFeatures(null, null, false));
	// NoPostingFeatures_Ext onDiskFeatures = new
	// NoPostingFeatures_Ext(features.getUnSelectedFeatures(null, null, false));
	// //3. Build the Lindex-plus index with all those features
	// SubgraphSearch_LindexPlusBuilder builder = new
	// SubgraphSearch_LindexPlusBuilder();
	// builder.buildIndex(selectedFeatures, onDiskFeatures, new
	// GraphDatabase_OnDisk(dbFileName, dbParser), temp, dbParser);
	// }
	//

}
