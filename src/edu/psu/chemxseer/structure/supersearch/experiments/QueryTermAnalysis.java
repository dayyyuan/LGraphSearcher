package edu.psu.chemxseer.structure.supersearch.experiments;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexConstructor;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.supersearch.GPTree.GPFeatureMiner;
import edu.psu.chemxseer.structure.supersearch.PrefIndex.PrefixFeatureMiner;

/**
 * This class analyze terms use for each query: Given the selected patterns, I
 * build an Lindex For each query, I searched the Lattice index, counter the
 * number of maximum subgraph & minimal supergraphs output them and study
 * 
 * @author dayuyuan
 * 
 */
public class QueryTermAnalysis {

	/**
	 * Given the selected features [1] Construct a Lindex [2] Find the (maxinum)
	 * subgraph and minimum supergraph, and return their count in an array
	 * 
	 * @param query
	 * @param features
	 * @return
	 * @throws ParseException
	 */
	public static void testCount(IGraphDatabase query,
			NoPostingFeatures<IOneFeature> rawFeatures) throws ParseException {
		NoPostingFeatures_Ext<IOneFeature> features = new NoPostingFeatures_Ext<IOneFeature>(
				rawFeatures);
		long start = System.currentTimeMillis();
		features.mineSubSuperRelation();
		long time1 = System.currentTimeMillis();
		System.out.println("1. Mine super-sub graph relationships: "
				+ (time1 - start));
		LindexSearcher searcher = LindexConstructor.construct(features);

		int[][] result = new int[3][query.getTotalNum()];
		for (int i = 0; i < query.getTotalNum(); i++) {
			Graph q = query.findGraph(i);
			if (q == null)
				break;
			List<Integer> allSubs = searcher.subgraphs(q, new long[4]);
			if (allSubs != null)
				result[0][i] = allSubs.size();
			List<Integer> maxSubs = searcher.maxSubgraphs(q, new long[4]);
			if (maxSubs != null) {
				result[1][i] = maxSubs.size();
				List<Integer> minSups = searcher.testNonSubgraphs(maxSubs);
				if (minSups != null)
					result[2][i] = minSups.size();
			}
			// System.out.println(result[0][i] + "," + result[1][i] + "," +
			// result[2][i]);
		}
		System.out.println(getAverage(result[0]));
		System.out.println(getAverage(result[1]));
		System.out.println(getAverage(result[2]));
	}

	public static int getAverage(int[] array) {
		double result = array[0];
		for (int i = 1; i < array.length; i++) {
			result = result * (i / (double) (i + 1)) + array[i]
					/ (double) (i + 1);
			// System.out.println("test" + result);
		}
		return new Double(result).intValue();
		// return (int)result;
	}

	public static void main(String[] args) throws ParseException, IOException {
		System.out.println("smallExp");
		smallAIDS();
		System.out.println("largeExp");
		largeAIDS();
		// System.out.println(getAverage(new int[]{4,5,6,67,8}));
	}

	public static void largeAIDS() throws IOException, ParseException {
		String home = "/data/santa/SupSearchExp/AIDSLargeNew/";
		IGraphDatabase trainingDB = new GraphDatabase_OnDisk(home
				+ "DBFile_Raw", MyFactory.getSmilesParser());
		IGraphDatabase query = new GraphDatabase_OnDisk(home + "testQuery",
				MyFactory.getUnCanDFS());
		double[] minSupts = new double[] { 0.05, 0.03, 0.02, 0.01, 0.008, 0.006 };

		for (int i = 1; i < minSupts.length; i++) {
			double minSupt = minSupts[i];
			String baseName = home + "MinSup_" + minSupt;

			String name = baseName + "PrefIndex/";
			PrefixFeatureMiner miner = new PrefixFeatureMiner();
			NoPostingFeatures<IOneFeature> onlyFeatures = new NoPostingFeatures<IOneFeature>(
					name + "feature",
					MyFactory.getFeatureFactory(FeatureFactoryType.OneFeature));
			PostingFeatures freqFeatures = new PostingFeatures(
					name + "posting", onlyFeatures);
			NoPostingFeatures<IOneFeature> selectedFeatures = miner
					.minPrefixFeatures(freqFeatures, trainingDB.getTotalNum())
					.getFeatures();
			selectedFeatures.saveFeatures(name + "selectedFeatures");
			testCount(query, selectedFeatures);

			System.out.println(baseName + "GPTree");
			name = baseName + "GPTree/";
			GPFeatureMiner miner2 = new GPFeatureMiner();
			NoPostingFeatures<IOneFeature> sigFeatures = miner2
					.minSignificantFeatures(freqFeatures, 1.25).getFeatures();
			sigFeatures.saveFeatures(name + "sigFeatures");
			testCount(query, sigFeatures);
		}

	}

	public static void smallAIDS() throws IOException, ParseException {
		String dirName = "/data/santa/SupSearchExp/AIDSNew/";
		String testQuery25 = dirName + "TestQuery25";
		String dbFileName = dirName + "DBFile";
		IGraphDatabase query = new GraphDatabase_OnDisk(testQuery25,
				MyFactory.getSmilesParser());

		double[] minSupts = new double[4];
		minSupts[0] = 0.05;
		minSupts[1] = 0.03;
		minSupts[2] = 0.02;
		minSupts[3] = 0.01;

		for (int j = 0; j < 4; j++) {
			double minSupt = minSupts[j];
			for (int i = 2; i <= 10; i = i + 2) {
				String baseName = dirName + "G_" + i + "MinSup_" + minSupt
						+ "/";
				IGraphDatabase trainingDB = new GraphDatabase_OnDisk(dbFileName
						+ i, MyFactory.getDFSCoder());

				System.out.println(baseName + "PrefixIndex");
				String name = baseName + "PrefIndex/";
				PrefixFeatureMiner miner = new PrefixFeatureMiner();
				NoPostingFeatures<IOneFeature> onlyFeatures = new NoPostingFeatures<IOneFeature>(
						name + "feature",
						MyFactory
								.getFeatureFactory(FeatureFactoryType.OneFeature));
				PostingFeatures freqFeatures = new PostingFeatures(name
						+ "posting", onlyFeatures);
				NoPostingFeatures<IOneFeature> selectedFeatures = miner
						.minPrefixFeatures(freqFeatures,
								trainingDB.getTotalNum()).getFeatures();
				selectedFeatures.saveFeatures(name + "selectedFeatures");
				testCount(query, selectedFeatures);

				System.out.println(baseName + "GPTree");
				name = baseName + "GPTree/";
				GPFeatureMiner miner2 = new GPFeatureMiner();
				freqFeatures.getFeatures().setAllUnSelected();
				NoPostingFeatures<IOneFeature> sigFeatures = miner2
						.minSignificantFeatures(freqFeatures, 1.25)
						.getFeatures();
				sigFeatures.saveFeatures(name + "sigFeatures");
				testCount(query, sigFeatures);
			}
		}
	}
}
