package edu.psu.chemxseer.structure.setcover.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.newExps.Util;
import edu.psu.chemxseer.structure.setcover.update.IndexUpdator;
import edu.psu.chemxseer.structure.setcover.update.SubSearch_LindexSimpleUpdatable;
import edu.psu.chemxseer.structure.setcover.update.SubSearch_LindexSimpleUpdatableBuilder;
import edu.psu.chemxseer.structure.setcover.update.SubSearch_LindexSimpleUpdatable_TCFG;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimple;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;

public class ExpGraphSearch {

	public static float[] runExp(IMaxCoverSolver solver, String fileToStore,
			int K) throws IOException {
		float[] stat = new float[11];
		IFeatureWrapper[] result = solver.runGreedy(K, stat);
		System.out.println("Time: " + stat[0] + " Space: " + stat[4]
				+ " Ave_Space: " + stat[5] + " Coverage: "
				+ solver.coveredItemsCount());

		// save the experiment results
		IFeatureWrapper[] alReadySelected = solver.getFixedSelected();
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileToStore));
		if (result != null) {
			for (IFeatureWrapper i : result)
				writer.write(i.getFeature().getDFSCode() + "\t");
		}
		if (alReadySelected != null) {
			for (IFeatureWrapper i : alReadySelected)
				writer.write(i.getFeature().getDFSCode() + "\t");
		}
		writer.flush();
		writer.close();
		return stat;
	}

	public static float[] runExpStorePostings(IMaxCoverSolver solver,
			String fileToStore, int K) throws IOException {
		float[] stat = new float[11];
		IFeatureWrapper[] result = solver.runGreedy(K, stat);
		System.out.println("Time: " + stat[0] + " Space: " + stat[4]
				+ " Ave_Space: " + stat[5] + " Coverage: "
				+ solver.coveredItemsCount());

		// save the experiment results
		IFeatureWrapper[] alReadySelected = solver.getFixedSelected();
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileToStore));

		if (alReadySelected != null) {
			for (IFeatureWrapper i : alReadySelected)
				writer.write(i.getFeature().getDFSCode() + "\n");
		}

		if (result != null) {
			for (IFeatureWrapper oneFeature : result) {
				writer.write(oneFeature.getFeature().getDFSCode() + ","
						+ oneFeature.containedDatabaseGraphs().length + ","
						+ oneFeature.containedQueryGraphs().length + "\n");
			}
		}
		writer.flush();
		writer.close();
		return stat;
	}

	/**
	 * Given the stored featureFile Load & Returnt he selected features
	 * 
	 * @param featureFile
	 * @return
	 * @throws IOException
	 */
	public static NoPostingFeatures<IOneFeature> loadFeatures(String featureFile)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(featureFile));
		String line = reader.readLine();
		String[] tokens = line.split("\t");
		OneFeatureImpl[] features = new OneFeatureImpl[tokens.length];
		Set<String> status = new HashSet<String>();
		int index = 0;
		for (String token : tokens) {
			if (status.contains(token))
				continue;
			else {
				features[index] = new OneFeatureImpl(token, -1, -1, index,
						false);
				status.add(token);
				index++;
			}
		}
		reader.close();
		return new NoPostingFeatures<IOneFeature>(
				Arrays.copyOf(features, index));
	}

	public static String doUpdate_TCFG(StatusType sType, IGraphDatabase gDB,
			int topGCount, IGraphDatabase newQuery, IGraphDatabase testQuery,
			String lindexFolder, String lindexUpdateFolder, double minSupport,
			double lambda) throws IOException {
		System.out.println("Start Update Index: " + lindexUpdateFolder);
		long start = System.currentTimeMillis();
		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple lindex = lindexBuilder.loadIndex(gDB,
				lindexFolder, MyFactory.getDFSCoder(), true);
		ExpSubSearch.runIndexWithMinNum(lindex, testQuery,
				(int) (gDB.getTotalNum() * minSupport));

		SubSearch_LindexSimpleUpdatableBuilder builder = new SubSearch_LindexSimpleUpdatableBuilder();
		SubSearch_LindexSimpleUpdatable lindexUpdatable = new SubSearch_LindexSimpleUpdatable_TCFG(
				builder.buildIndexSub(lindex, gDB, newQuery, lindexFolder),
				minSupport);
		long end = System.currentTimeMillis();
		System.out
				.println("After Building Index Updatable with Queries, takes: "
						+ (end - start));
		// 1.2 Do index updating & save the index
		start = System.currentTimeMillis();
		IndexUpdator updator = new IndexUpdator(lindexUpdatable, topGCount); // 100/100000
																				// =
																				// 0.01
		float[] beforeUpdateStat = updator.initializeUpdate(AppType.subSearch,
				sType, minSupport, lambda);
		float[] updateStat = updator.doUpdate();
		builder.saveUpdatedIndex(lindexUpdatable, lindexUpdateFolder);
		// 1.3 After the index updating, do query processing test, query
		// processing with InMem Queries
		lindex = builder.loadIndexSub(gDB, lindexUpdateFolder);
		float[] runExpStat = ExpSubSearch.runIndexWithMinNum(lindex, testQuery,
				(int) (gDB.getTotalNum() * minSupport));
		return Util.stateToString(Util.joinArray(beforeUpdateStat, updateStat,
				runExpStat));
	}

	public static String doUpdate(StatusType sType, IGraphDatabase gDB,
			int topGraphs, IGraphDatabase newQuery, IGraphDatabase testQuery,
			String lindexFolder, String lindexUpdateFolder, double minSupport,
			double lambda) throws IOException {
		System.out.println("Start Update Index: " + lindexUpdateFolder);
		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple lindex = lindexBuilder.loadIndex(gDB,
				lindexFolder, MyFactory.getDFSCoder(), true);
		// ExpSubSearch.runIndex(lindex,testQuery);
		SubSearch_LindexSimpleUpdatableBuilder builder = new SubSearch_LindexSimpleUpdatableBuilder();
		SubSearch_LindexSimpleUpdatable lindexUpdatable = builder
				.buildIndexSub(lindex, gDB, newQuery, lindexFolder);
		// 1.2 Do index updating & save the index
		IndexUpdator updator = new IndexUpdator(lindexUpdatable, topGraphs); // 100/100000
																				// =
																				// 0.01
		float[] beforeUpdateStat = updator.initializeUpdate(AppType.subSearch,
				sType, minSupport, lambda);
		float[] updateStat = updator.doUpdate();
		System.out.println(updator.getFeatureCount());
		builder.saveUpdatedIndex(lindexUpdatable, lindexUpdateFolder);
		// 1.3 After the index updating, do query processing test, query
		// processing with InMem Queries
		lindex = builder.loadIndexSub(gDB, lindexUpdateFolder);
		float[] runExpStat = ExpSubSearch.runIndex(lindex, testQuery);
		return Util.stateToString(Util.joinArray(beforeUpdateStat, updateStat,
				runExpStat));
	}

}
