package edu.psu.chemxseer.structure.supersearch.Test;

/**
 * Test All the Features Mining Algorithms to see whether they work or not
 * 
 * @author dayuyuan
 * 
 */
public class FeaturesTest {
	// public static void main(String[] args) throws ParseException,
	// IOException{
	// FeaturesTest test = new FeaturesTest();
	// //test.extFeatures();
	// test.multiClass();
	// }
	//
	// public void normalFeatures(){
	// String dbName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/graphDataSelected";
	// String featureFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/features";
	// String postingFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/postings";
	// float minimumFrequency = 0.08f;
	//
	// //FeatureProcessorL.frequentSubgraphMining(dbName, featureFileName,
	// postingFileName, minimumFrequency, 10, MyFactory.getSmilesParser());
	// NoPostingFeatures<OneFeatureImpl> features = new
	// NoPostingFeatures<OneFeatureImpl>(featureFileName,
	// MyFactory.getFeatureFactory(FeatureFactory.OneFeature));
	// PostingFeatures postingFeatures = new PostingFeatures(postingFileName,
	// features);
	// System.out.println(postingFeatures.getFeatures().getfeatureNum());
	// int[] postings = postingFeatures.getPosting(10);
	// }
	//
	// public void extFeatures() throws ParseException{
	// String featureFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/features";
	// String postingFileName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/postings";
	// NoPostingFeatures<OneFeatureImpl> features = new
	// NoPostingFeatures<OneFeatureImpl>(featureFileName,
	// MyFactory.getFeatureFactory(FeatureFactory.OneFeature));
	// NoPostingFeatures_Ext<OneFeatureImpl> advFeatures = new
	// NoPostingFeatures_Ext<OneFeatureImpl>(features);
	// advFeatures.mineSubSuperRelation();
	// }
	//
	// public void multiClass() throws IOException{
	// FeatureProcessorDuralClass featureProcessor = new
	// FeatureProcessorDuralClass(0.1f, 10);
	// String dbName =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/graphDataSelected";
	// String dbName2 =
	// "/Users/dayuyuan/Documents/workspace/Experiment1/graphDataConnected";
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
	// // featureProcessor.mergeGraphFile(dbName, dbName2, mergedFileName,
	// classFileName);
	// // featureProcessor.frequentSubgraphMining(mergedFileName, classFileName,
	// featureFileName,
	// // postingFileNames, MyFactory.getSmilesParser());
	// NoPostingFeatures<OneFeatureMultiClass> rawFeatures =
	// new NoPostingFeatures<OneFeatureMultiClass>(featureFileName,
	// MyFactory.getFeatureFactory(FeatureFactory.MultiFeature));
	// int[] graphClass = new int[2];
	// graphClass[0] = 100;
	// graphClass[1] = 9001;
	// PostingFeaturesMultiClass features =
	// new PostingFeaturesMultiClass(postingFileNames, rawFeatures, graphClass);
	// System.out.println(features.getFeatures().getfeatureNum());
	// for(int i =0; i< 2; i++){
	// int[] postings = features.getFullPosting(100, i);
	// for(int j = 0; j< postings.length; j++)
	// System.out.print(postings[j]);
	// System.out.println();
	// }
	// }
}
