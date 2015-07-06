package edu.psu.chemxseer.structure.experiment;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.FGindex.SubSearch_FGindex;
import edu.psu.chemxseer.structure.subsearch.FGindex.SubSearch_FGindexBuilder;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_Gindex;
import edu.psu.chemxseer.structure.subsearch.Gindex.SubSearch_GindexBuilder;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorL;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimplePlusBuilder;

/**
 * The class run the basic Experiment with AIDS 40K graphs
 * 
 * @author dayuyuan
 * 
 */
public class BasicExpBuilder {
	protected String dbFileName;
	protected GraphParser dbParser;
	protected String baseName;

	//
	// /**
	// * Build the Index for Basic Experiment
	// * @param args
	// * @throws IOException
	// * @throws ParseException
	// */
	// public static void main(String[] args) throws IOException,
	// ParseException{
	// String dbFileName = "/data/santa/VLDBJExp/BasicExp/DBFile";
	// String baseName = "/data/santa/VLDBJExp/BasicExp/";
	// // String dbFileName =
	// "/home/duy113/Experiment/LindexJournal/BasicExpNew/DBFile";
	// // String baseName =
	// "/home/duy113/Experiment/LindexJournal/BasicExpNew/";
	// GraphParser dbParser = MyFactory.getSmilesParser();
	// BasicExpBuilder basic = new BasicExpBuilder(dbFileName, dbParser,
	// baseName);
	//
	// // System.out.println("DF");
	// // basic.buildGIndexDF();
	// // basic.buildLindexDF();
	// //
	// // System.out.println("DT");
	// // basic.buildGindexDT();
	// // basic.buildLindexDT();
	// // basic.buildSwiftIndex();
	// //
	// // System.out.println("TCFG");
	// // basic.buildFGindex();
	// // basic.buildLindexAdvTCFG();
	//
	// System.out.println("MimR");
	// basic.buildGindexMimR();
	// basic.buildLindexMimR();
	// basic.buildLindexAdvMimR();
	//
	// System.out.println();
	// }
	//
	//
	//
	public BasicExpBuilder(String dbFileName, GraphParser dbParser,
			String baseName) {
		this.dbFileName = dbFileName;
		this.dbParser = dbParser;
		this.baseName = baseName;
	}

	public ISearcher buildGIndexDF(double minFreq, int flag)
			throws IOException, ParseException {
		// 0. Create Folder
		String temp = baseName + "GindexDF" + flag + "/";
		if (flag == 0)
			temp = baseName + "GindexDF/";
		File folder = new File(temp);
		if (!folder.exists())
			folder.mkdirs();
		// 1. Mine Features
		PostingFeatures candidateFeatures = FeatureProcessorG
				.frequentSubgraphMining(dbFileName, temp + "patterns", temp
						+ "postings", minFreq, 4, 10, dbParser);
		// PostingFeatures candidateFeatures = new PostingFeatures(temp +
		// "postings",
		// new NoPostingFeatures(temp + "patterns",
		// MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature)));
		// 2. Build Index
		SubSearch_GindexBuilder builder = new SubSearch_GindexBuilder();
		SubSearch_Gindex gIndex = builder.buildIndex(candidateFeatures,
				new GraphDatabase_OnDisk(dbFileName, dbParser), false, temp,
				temp + "GPatterns", temp + "GPostings", dbParser);
		return gIndex;
	}

	public void buildLindexDF(int flag) throws CorruptIndexException,
			LockObtainFailedException, IOException, ParseException {
		// 0. Create Folder
		String temp = baseName + "LindexDF" + flag + "/";
		if (flag == 0)
			temp = baseName + "LindexDF/";
		File folder = new File(temp);
		if (!folder.exists())
			folder.mkdirs();
		// 1. Use DF features
		String gIndexPatterns = baseName + "GindexDF" + flag + "/GPatterns";
		if (flag == 0)
			gIndexPatterns = baseName + "GindexDF/GPatterns";
		NoPostingFeatures<IOneFeature> features = new NoPostingFeatures<IOneFeature>(
				gIndexPatterns,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
		NoPostingFeatures_Ext<IOneFeature> lindexFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				features);
		SubSearch_LindexSimpleBuilder builder = new SubSearch_LindexSimpleBuilder();
		builder.buildIndex(lindexFeatures, new GraphDatabase_OnDisk(dbFileName,
				dbParser), temp, dbParser);
	}

	//
	// public void buildGindexDT() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "GindexDT/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Mine Features
	// PostingFeatures candidateFeatures =
	// FeatureProcessorG.frequentSubtreeMining(dbFileName, temp + "patterns",
	// temp + "postings",
	// 0.05, 4, 10, dbParser);
	// //2. Build Index
	// SubgraphSearch_GindexBuilder builder = new
	// SubgraphSearch_GindexBuilder();
	// builder.buildIndex(candidateFeatures, new
	// GraphDatabase_OnDisk(dbFileName, dbParser),
	// false, temp,temp + "GPatterns", temp+ "GPostings", dbParser);
	// }
	// public void buildLindexDT() throws CorruptIndexException,
	// LockObtainFailedException, IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "LindexDT/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Use DF features
	// NoPostingFeatures<IOneFeature> normalFeatures = new
	// NoPostingFeatures<IOneFeature>(baseName + "GindexDT/GPatterns",
	// MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
	// NoPostingFeatures_Ext<IOneFeature> features = new
	// NoPostingFeatures_Ext<IOneFeature>(normalFeatures);
	// SubgraphSearch_LindexBuilder builder = new
	// SubgraphSearch_LindexBuilder();
	// builder.buildIndex(features, new GraphDatabase_OnDisk(dbFileName,
	// dbParser), temp, dbParser);
	// }
	// public void buildSwiftIndex() throws CorruptIndexException,
	// LockObtainFailedException, IOException{
	// //0. Create Folder
	// String temp = baseName + "SwiftIndex/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Use DF features
	// NoPostingFeatures features = new NoPostingFeatures(baseName +
	// "GindexDT/GPatterns",
	// MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature) );
	// SubgraphSearch_QuickSIBuilder builder = new
	// SubgraphSearch_QuickSIBuilder();
	// builder.buildIndex(features, new GraphDatabase_OnDisk(dbFileName,
	// dbParser), temp, dbParser);
	// }

	public SubSearch_FGindex buildFGindex(double minFreq, int flag)
			throws IOException {
		// 0. Create Folder
		String temp = baseName + "FGindex" + flag + "/";
		if (flag == 0)
			temp = baseName + "FGindex/";
		File folder = new File(temp);
		if (!folder.exists())
			folder.mkdirs();
		// 1. Mine Frequent Features
		PostingFeatures features = FeatureProcessorFG.frequentSubgraphMining(
				dbFileName, temp + "patterns", temp + "postings", minFreq, 10,
				dbParser);
		// 2. Mine Frequent TCFG & Construct Index
		SubSearch_FGindexBuilder builder = new SubSearch_FGindexBuilder();
		return builder.buildIndex(features.getFeatures(),
				new GraphDatabase_OnDisk(dbFileName, dbParser), temp, dbParser);
	}

	public void buildLindexAdvTCFG(int flag) throws IOException, ParseException {
		// 0. Create Folder
		String temp = baseName + "LindexTCFG" + flag + "/";
		if (flag == 0)
			temp = baseName + "LindexTCFG/";
		File folder = new File(temp);
		if (!folder.exists())
			folder.mkdirs();
		// 1. Mine-edge features & load previous mined FG features
		String fgFeatures = baseName + "FGindex" + flag
				+ "/StatusRecordedFeatures";
		if (flag == 0)
			fgFeatures = baseName + "FGindex/StatusRecordedFeatures";
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				dbFileName, temp + "edge", temp + "edgePosting", dbParser);
		NoPostingFeatures<IOneFeature> freqFeatures = new NoPostingFeatures<IOneFeature>(
				fgFeatures,
				MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));

		// 2. Combine this two features
		NoPostingFeatures<IOneFeature> features = FeatureProcessorL
				.mergeFeatures(freqFeatures, edgeFeatures.getFeatures());
		NoPostingFeatures_Ext<IOneFeature> selectedFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				new NoPostingFeatures<IOneFeature>(null,
						features.getSelectedFeatures(), false));
		NoPostingFeatures_Ext<IOneFeature> onDiskFeatures = new NoPostingFeatures_Ext<IOneFeature>(
				new NoPostingFeatures<IOneFeature>(null,
						features.getUnSelectedFeatures(), false));
		// 3. Build the Lindex-plus index with all those features
		SubSearch_LindexSimplePlusBuilder builder = new SubSearch_LindexSimplePlusBuilder();
		builder.buildIndex(selectedFeatures, onDiskFeatures,
				new GraphDatabase_OnDisk(dbFileName, dbParser), temp, dbParser);
	}
	//
	// public void buildLindexTCFG() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "LindexTCFG/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Mine-edge features & load previous mined FG features
	// PostingFeatures edgeFeatures =
	// FeatureProcessorL.findEdgeOneFeatures(dbFileName, temp+"edge",
	// temp+"edgePosting", dbParser);
	// PostingFeatures subgraphFeatures = new PostingFeatures(baseName +
	// "FGindex/patterns", baseName + "FGindex/postings");
	// subgraphFeatures.loadFeatures();
	// //2. Combine this two features
	// PostingFeatures features =
	// FeatureProcessorL.mergeFeatures(subgraphFeatures, edgeFeatures);
	// NoPostingFeatures_Ext selectedFeatures = new
	// NoPostingFeatures_Ext(features.getSelectedFeatures(null, null, false));
	// //LFeatures onDiskFeatures = new
	// LFeatures(features.getUnSelectedFeatures(null, null, false));
	// //3. Build the Lindex-plus index with all those features
	// SubgraphSearch_LindexBuilder builder = new
	// SubgraphSearch_LindexBuilder();
	// builder.buildIndex(selectedFeatures, new GraphDatabase_OnDisk(dbFileName,
	// dbParser), temp, dbParser);
	// }
	//
	// public void buildGindexMimR() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "GindexMimR/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Merge MimR Features with edge Patterns
	// PostingFeatures edgeFeatures = new PostingFeatures(baseName +
	// "LindexTCFG/edge", baseName + "LindexTCFG/edgePosting");
	// edgeFeatures.loadFeatures();
	// PostingFeatures mimrFeatures = new PostingFeatures(temp + "mimr", null);
	// mimrFeatures.loadFeatures();
	// //set all mimr Features selected
	// mimrFeatures.setAllSelected();
	// NoPostingFeatures_Ext subgraphFeatures = new
	// NoPostingFeatures_Ext(FeatureProcessorL.mergeFeatures(mimrFeatures,
	// edgeFeatures));
	// subgraphFeatures.saveFeatures(temp + "patterns");
	//
	// //2. Build the Gindex Exhaust
	// SubgraphSearch_GindexBuilder builder = new
	// SubgraphSearch_GindexBuilder();
	// builder.buildIndexWithoutFeatureSelection(subgraphFeatures, new
	// GraphDatabase_OnDisk(dbFileName, dbParser),
	// true, temp, dbParser);
	//
	// }
	//
	// public void buildLindexMimR() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "LindexMimR/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Load MimR Features
	// NoPostingFeatures_Ext subgraphFeatures = new
	// NoPostingFeatures_Ext(baseName + "GindexMimR/patterns", null);
	// subgraphFeatures.loadFeatures();
	// //2. Build the Lindex MimR
	// SubgraphSearch_LindexBuilder builder = new
	// SubgraphSearch_LindexBuilder();
	// builder.buildIndex(subgraphFeatures, new GraphDatabase_OnDisk(dbFileName,
	// dbParser), temp, dbParser);
	// }
	//
	// public void buildLindexAdvMimR() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "LindexMimRPlus/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Load MimR Features
	// PostingFeatures subgraphFeatures = new PostingFeatures(baseName +
	// "GindexMimR/patterns", null);
	// subgraphFeatures.loadFeatures();
	// PostingFeatures allFreqFeatures = new PostingFeatures(baseName +
	// "FGindex/patterns", baseName + "FGindex/postings");
	// allFreqFeatures.loadFeatures();
	// NoPostingFeatures_Ext onDiskFeatures = new
	// NoPostingFeatures_Ext(FeatureProcessorL.removeFeatures(allFreqFeatures,
	// subgraphFeatures));
	// //2. Build the Index
	// SubgraphSearch_LindexPlusBuilder builder = new
	// SubgraphSearch_LindexPlusBuilder();
	// builder.buildIndex(new NoPostingFeatures_Ext(subgraphFeatures),
	// onDiskFeatures, new GraphDatabase_OnDisk(dbFileName, dbParser),
	// temp, dbParser);
	// }
	//
	// public void buildGindexTreeDelta() throws IOException{
	// //0. Create Folder
	// String temp = baseName + "GindexTreeDelta/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Load the GindexTree index
	// GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(dbFileName,
	// dbParser);
	// SubgraphSearch_GindexBuilder gBuilder = new
	// SubgraphSearch_GindexBuilder();
	// SubgraphSearch_Gindex treeIndex = gBuilder.loadIndex(gDB, false, baseName
	// + "GindexDT/", dbParser);
	// IGraphs testQueries = new Graphs(baseName + "Queries/UniformQueryTrain");
	// testQueries.loadGraphs();
	// //2. Build the GindexTreeDelta index
	// SubgraphSearch_TreeDeltaBuilder tBuilder = new
	// SubgraphSearch_TreeDeltaBuilder();
	// PostingFeatures deltaFeatures = tBuilder.mineDelta(gDB, treeIndex,
	// testQueries, temp);
	// tBuilder.buildIndex(treeIndex, deltaFeatures, gDB, temp, dbParser);
	// }
	//
	// public void buildLindexTreeDelta() throws IOException, ParseException{
	// //0. Create Folder
	// String temp = baseName + "LindexTreeDelta/";
	// File folder = new File(temp);
	// if(!folder.exists())
	// folder.mkdirs();
	// //1. Load the GindexTree features & delta features and merge them
	// PostingFeatures tFeatures = new PostingFeatures(baseName +
	// "GindexDT/GPatterns", baseName + "GindexDT/GPostings");
	// tFeatures.loadFeatures();
	// PostingFeatures dFeatures = new PostingFeatures(
	// baseName + "GindexTreeDelta/" +
	// SubgraphSearch_TreeDelta.getDeltaFeature(), null);
	// dFeatures.loadFeatures();
	// PostingFeatures totalFeatures = tFeatures.mergeFeatures(dFeatures, temp +
	// "patterns");
	// //2. Build Lindex
	// SubgraphSearch_LindexBuilder builder = new
	// SubgraphSearch_LindexBuilder();
	// builder.buildIndex(new NoPostingFeatures_Ext(totalFeatures), new
	// GraphDatabase_OnDisk(dbFileName, dbParser), temp, dbParser);
	// }
	//
	// public void freMem(){
	// Runtime r = Runtime.getRuntime();
	// for(int i = 0; i< 100; i++)
	// r.gc();
	// }
}
