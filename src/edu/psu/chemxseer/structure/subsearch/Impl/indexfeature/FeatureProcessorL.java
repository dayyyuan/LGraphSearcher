package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_Edge;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * The Feature Mining & Preprocesing Algorithms for Lindex
 * 
 * @author dayuyuan
 * 
 */
public class FeatureProcessorL {
	// /**
	// * All Distinct Edges are Included in TCFGs
	// * @param candidateFeatures
	// * @param delta
	// * @throws ParseException
	// */
	// public void mineTCFG(IFeatures candidateFeatures, float delta) throws
	// ParseException{
	// // First sort all candidateFeatures
	// candidateFeatures.sortFeatures();
	// IOneFeature[] allFeatures = candidateFeatures.getFeatures();
	// FastSU fastSu = new FastSU();
	// candidateFeatures.createGraphs();
	// // Then put those features into a layer array
	// int maxLayer = allFeatures[allFeatures.length-1].getEdgeNum();
	// ArrayList<IOneFeature>[] layerFeatures= new ArrayList[maxLayer];
	// for(int i = 0; i< maxLayer; i++)
	// layerFeatures[i] = new ArrayList<IOneFeature>();
	// for(int i = 0; i< allFeatures.length; i++){
	// int edgeNum = allFeatures[i].getEdgeNum();
	// if(edgeNum == 0) continue;
	// layerFeatures[edgeNum-1].add(allFeatures[i]);
	// }
	// int TCFGNum = 0;
	//
	// //All layer 0 graphs [ graphs with edge count = 1] are selected
	// ArrayList<IOneFeature> firstLayerFeature = layerFeatures[0];
	// for(int i = 0; i < firstLayerFeature.size(); i++){
	// firstLayerFeature.get(i).setSelected();
	// ++TCFGNum;
	// }
	// ArrayList<IOneFeature> lastLayerFeature =
	// layerFeatures[layerFeatures.length-1];
	// // All layer n graphs [graphs with edge count = maximum layer] are
	// selected
	// for(int i = 0; i< lastLayerFeature.size(); i++){
	// lastLayerFeature.get(i).setSelected();
	// ++TCFGNum;
	// }
	// //Mine frequency tolerant closed subgraphs
	// for(int i = 1; i< (layerFeatures.length-1); i++){
	// ArrayList<IOneFeature> childFeatures = layerFeatures[i+1];
	// for(int j = 0; j< layerFeatures[i].size(); j++){
	// Graph small = layerFeatures[i].get(j).getFeatureGraph();
	// float threshold = (1-delta) * layerFeatures[i].get(j).getFrequency();
	// // childrenFeatures must not be empty nor null
	// boolean TCFGstatus = true;
	// for(int t = 0; t< childFeatures.size(); t++){
	// boolean iso = fastSu.isIsomorphic(small,
	// childFeatures.get(t).getFeatureGraph());
	// if(iso && childFeatures.get(t).getFrequency() >= threshold){
	// //layerFeatures[i].get(j).setUnselected();
	// TCFGstatus = false;
	// break;
	// }
	// }
	// if(TCFGstatus){
	// layerFeatures[i].get(j).setSelected();
	// ++TCFGNum;
	// }
	// //else layerFeatures[i].get(j).setUnselected();
	// }
	// }
	// System.out.println("After mining delta-TCFG: ");
	// System.out.println("Total number of FGidnex features: " +
	// candidateFeatures.getfeatureNum());
	// System.out.println("Number of TCGF among those features: " + TCFGNum);
	// }

	/**
	 * Given the graphdatabase, find all fragments of edge = 1;
	 * 
	 * @param gDB
	 * @return
	 * @throws IOException
	 */
	public static PostingFeatures findEdgeOneFeatures(String dbFileName,
			String edgeFile, String edgePosting, GraphParser gParser) {
		return frequentSubgraphMining(dbFileName, edgeFile, edgePosting,
				(float) 0.1, 1, gParser);
	}

	/**
	 * Given the GraphDataset, find all the frequent fragment (all distinct edge
	 * 1 features are included)
	 * 
	 * @param gDBFileName
	 * @param featureFileName
	 * @param postingFileName
	 * @param minimumFrequency
	 * @param maxNonSelectDepth
	 * @param gParser
	 * @return
	 */
	public static PostingFeatures frequentSubgraphMining(String gDBFileName,
			String featureFileName, String postingFileName,
			float minimumFrequency, int maxNonSelectDepth, GraphParser gParser) {

		// String[] args = {"-minimumFrequencies="+(-minimumFrequency),
		// "-maximumFragmentSize="+maxNonSelectDepth,
		// "-graphFile="+gDBFileName,
		// "-closedFragmentsOnly=flase", "-outputFile=temp",
		// "-parserClass=edu.psu.chemxseer.structure.iso.DFSCoder",
		// "-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
		// "-memoryStatistics=false", "-debug=1"};
		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency),
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-graphFile=" + gDBFileName,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=0" };
		return GSpanMiner_Edge.gSpanMining(args, featureFileName,
				postingFileName);
	}

	/**
	 * Merge all the edge features and subgraphFeatures. Does not change
	 * selected information.
	 * 
	 * All edges are selected
	 * 
	 * @param subgraphFeatures
	 * @param edgeFeatures
	 * @return
	 */
	public static NoPostingFeatures<IOneFeature> mergeFeatures(
			NoPostingFeatures<IOneFeature> subgraphFeatures,
			NoPostingFeatures<IOneFeature> edgeFeatures) {

		List<IOneFeature> allFeatures = new ArrayList<IOneFeature>();
		// 1. Add edge features
		int edgeCount = edgeFeatures.getfeatureNum();
		for (int i = 0; i < edgeCount; i++) {
			IOneFeature aFeature = edgeFeatures.getFeature(i);
			aFeature.setSelected();
			allFeatures.add(aFeature);
		}
		// 2. Add non-edge subgraph features
		for (int i = 0; i < subgraphFeatures.getfeatureNum(); i++) {
			IOneFeature aFeature = subgraphFeatures.getFeature(i);
			if (aFeature.getFeatureGraph().getEdgeCount() <= 1)
				continue;
			else
				allFeatures.add(aFeature);
		}
		return new NoPostingFeatures<IOneFeature>(allFeatures, false);
	}

	/**
	 * subgraphFeatures-removedFeatures
	 * 
	 * @param subgraphFeatures
	 * @param removedFeatures
	 * @return
	 * @throws IOException
	 */
	public static NoPostingFeatures<OneFeatureImpl> removeFeatures(
			NoPostingFeatures<OneFeatureImpl> subgraphFeatures,
			NoPostingFeatures<OneFeatureImpl> removedFeatures)
			throws IOException {
		Set<String> removedStrings = new HashSet<String>();
		for (int i = 0; i < removedFeatures.getfeatureNum(); i++)
			removedStrings.add(removedFeatures.getFeature(i).getDFSCode());

		for (int i = 0; i < subgraphFeatures.getfeatureNum(); i++) {
			IOneFeature oneFeature = subgraphFeatures.getFeature(i);
			if (removedStrings.contains(oneFeature.getDFSCode()))
				oneFeature.setUnselected();
			else
				oneFeature.setSelected();
		}
		List<OneFeatureImpl> selectedFeatures = subgraphFeatures
				.getSelectedFeatures();
		return new NoPostingFeatures<OneFeatureImpl>(null, selectedFeatures,
				false);
	}
}
