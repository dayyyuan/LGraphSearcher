package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.experiments.ExpGraphSearch;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorL;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimple;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_LWFullBuilder;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_Lindex;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;

public class Util {
	/**
	 * Merge the three status arrays and return
	 * 
	 * @param ArrayOne
	 * @param ArrayTwo
	 * @param ArrayThree
	 * @return
	 */
	public static float[] joinArray(float[] ArrayOne, float[] ArrayTwo,
			float[] ArrayThree) {
		float[] result = new float[ArrayOne.length + ArrayTwo.length
				+ ArrayThree.length];
		int index = 0;
		for (float f : ArrayOne)
			result[index++] = f;
		for (float f : ArrayTwo)
			result[index++] = f;
		for (float f : ArrayThree)
			result[index++] = f;
		return result;
	}

	/**
	 * Format for output
	 * 
	 * @param fs
	 * @return
	 */
	public static String stateToString(float[] fs) {
		StringBuffer sbuf = new StringBuffer();
		for (float f : fs) {
			sbuf.append(String.format("%.2f", f));
			sbuf.append(" ");
		}
		sbuf.append("\n");
		return sbuf.toString();
	}

	/**
	 * Mine features with maxSolver
	 * 
	 * @param solver
	 * @param fileToStore
	 * @param K
	 * @return
	 * @throws IOException
	 */
	public static float[] mineFeatureWithMaxSolver(IMaxCoverSolver solver,
			String fileToStore, int K) throws IOException {
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

	/**
	 * Given the feature file, load the features
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

	/**
	 * Given Selected Features by Max-cover solver, build and Lindex and store
	 * in corresponding folder
	 * 
	 * @param gDB
	 * @param folder
	 * @param featureFile
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void buildLindexGivenMaxCoverSelectedFeatures(
			IGraphDatabase gDB, String folder, String featureFile)
			throws IOException, ParseException {
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		NoPostingFeatures<IOneFeature> features = ExpGraphSearch
				.loadFeatures(featureFile);
		SubSearch_LindexSimpleBuilder builder = new SubSearch_LindexSimpleBuilder();
		builder.buildIndex(new NoPostingFeatures_Ext<IOneFeature>(features),
				gDB, folder, MyFactory.getUnCanDFS());
	}

	public static void buildLWindexGivenMaxCoverSelectedFeatures(
			IGraphDatabase gDB, String folder, String featureFile)
			throws IOException, ParseException {
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		NoPostingFeatures<IOneFeature> features = ExpGraphSearch
				.loadFeatures(featureFile);
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		builder.buildLindex(new NoPostingFeatures_Ext<IOneFeature>(features),
				gDB, folder, true, MyFactory.getUnCanDFS());
	}

	public static SupSearch_Lindex buildLWindexWithNewDB(IGraphDatabase newDB,
			String newDBName, String folder, String featureFile)
			throws IOException, ParseException {
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				newDBName, folder + "edge", null, newDB.getParser());
		NoPostingFeatures<IOneFeature> features = ExpGraphSearch
				.loadFeatures(featureFile);
		features = FeatureProcessorL.mergeFeatures(features,
				edgeFeatures.getFeatures());

		// Remove Redundancy: for now I don't know where I got the redundancy in
		// my graphs
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		SupSearch_Lindex temp = builder.buildLindex(
				new NoPostingFeatures_Ext<IOneFeature>(features), newDB,
				folder, true, MyFactory.getUnCanDFS());
		return temp;
	}

	public static SupSearch_Lindex buildLWindexWithNewDB(IGraphDatabase newDB,
			String newDBName, String folder,
			NoPostingFeatures<IOneFeature> features) throws IOException,
			ParseException {
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				newDBName, folder + "edge", null, newDB.getParser());
		features = FeatureProcessorL.mergeFeatures(features,
				edgeFeatures.getFeatures());

		// Remove Redundancy: for now I don't know where I got the redundancy in
		// my graphs
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		SupSearch_Lindex temp = builder.buildLindex(
				new NoPostingFeatures_Ext<IOneFeature>(features), newDB,
				folder, true, MyFactory.getUnCanDFS());
		return temp;
	}

	public static SubSearch_LindexSimple buildLindexWithNewDB(
			IGraphDatabase newDB, String newDBName, String folder,
			String featureFile) throws IOException, ParseException {
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				newDBName, folder + "edge", null, newDB.getParser());
		NoPostingFeatures<IOneFeature> features = ExpGraphSearch
				.loadFeatures(featureFile);
		features = FeatureProcessorL.mergeFeatures(features,
				edgeFeatures.getFeatures());

		// Remove Redundancy: for now I don't know where I got the redundancy in
		// my graphs
		SubSearch_LindexSimpleBuilder builder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple temp = builder.buildIndex(
				new NoPostingFeatures_Ext<IOneFeature>(features), newDB,
				folder, MyFactory.getUnCanDFS());
		return temp;
	}

	public static void buildLindexWithNewDB(IGraphDatabase newDB,
			String newDBName, String folder,
			NoPostingFeatures<IOneFeature> features) throws IOException,
			ParseException {
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		PostingFeatures edgeFeatures = FeatureProcessorL.findEdgeOneFeatures(
				newDBName, folder + "edge", null, newDB.getParser());
		features = FeatureProcessorL.mergeFeatures(features,
				edgeFeatures.getFeatures());

		// Remove Redundancy: for now I don't know where I got the redundancy in
		// my graphs
		SubSearch_LindexSimpleBuilder builder = new SubSearch_LindexSimpleBuilder();
		builder.buildIndex(new NoPostingFeatures_Ext<IOneFeature>(features),
				newDB, folder, MyFactory.getUnCanDFS());
	}

	public static float[] runIndex(String lindexName, IGraphDatabase gDB,
			IGraphDatabase testQuery) throws IOException {
		SubSearch_LindexSimpleBuilder lindexBuilder = new SubSearch_LindexSimpleBuilder();
		SubSearch_LindexSimple lindex = lindexBuilder.loadIndex(gDB,
				lindexName, MyFactory.getUnCanDFS(), true);
		float[] status = new float[0];
		try {
			status = AIDSExp.runQueries(testQuery, lindex);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}

	public static float[] runIndexSupSearch(String lindexName,
			IGraphDatabase gDB, IGraphDatabase testQuery) throws IOException {
		SupSearch_LWFullBuilder lindexBuilder = new SupSearch_LWFullBuilder();
		SupSearch_Lindex lindex = lindexBuilder.loadLindex(gDB, lindexName,
				true, MyFactory.getUnCanDFS());
		float[] status = new float[0];
		try {
			status = AIDSExp.runQueries(testQuery, lindex);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}

	public static float[] runIndex(ISearcher index, IGraphDatabase testQuery)
			throws IOException {
		float[] status = new float[0];
		try {
			status = AIDSExp.runQueries(testQuery, index);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status;
	}
}
