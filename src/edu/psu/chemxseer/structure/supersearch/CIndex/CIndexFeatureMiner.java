package edu.psu.chemxseer.structure.supersearch.CIndex;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.setcover.IO.Input_Mem;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureConverterSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.impl.Greedy_InvertedIndex;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class CIndexFeatureMiner {

	/**
	 * Mine "K" indexing features, and store all the temporary results under the
	 * "baseName" folder
	 * 
	 * @param K
	 * @param gDB
	 * @param gQuery
	 * @param baseName
	 * @return
	 */
	public static PostingFeaturesMultiClass mineFeatures(int K,
			IGraphDatabase gDB, IGraphDatabase gQuery, GraphParser gParser,
			String baseName, double minSupt) {
		// 1. First Step: MineFrequent Features
		PostingFeaturesMultiClass rawFeatures = FeatureProcessorDuralClass
				.frequentSubgraphMining(gDB, gQuery, gDB.getParser(), baseName,
						minSupt, -1, 10);
		rawFeatures.loadPostingIntoMemory();
		// 2. Second Step: using the set-cover to mine the indexing features
		System.out.println("Select Index Patterns for CIndexFlat");
		System.out
				.println("(1) Boolean Matrix Status, MaxCoverSolver_Sequential solver, "
						+ "in_memory input");
		MemoryConsumptionCal.runGC();
		double startMemory = MemoryConsumptionCal.usedMemoryinMB();
		long startTime = System.currentTimeMillis();

		Input_Mem input = Input_Mem.newInstance(
				rawFeatures,
				new FeatureConverterSimple(gQuery.getTotalNum(), gDB
						.getTotalNum(), AppType.supSearch));
		Greedy_InvertedIndex solver = new Greedy_InvertedIndex(input,
				StatusType.normal, AppType.supSearch);
		IFeatureWrapper[] selectedFeature = solver.runGreedy(K,
				gQuery.getTotalNum(), new float[10]);
		for (IFeatureWrapper feature : selectedFeature)
			rawFeatures.getFeatures().getFeature(feature.getFetureID())
					.setSelected();

		MemoryConsumptionCal.runGC();
		double endMemory = MemoryConsumptionCal.usedMemoryinMB();
		System.out.println("(2) Space of cIndex pattern selection:"
				+ (endMemory - startMemory));
		System.out.println("(3) Time for cIndex pattern selection: "
				+ (System.currentTimeMillis() - startTime));
		System.out.println("(4) After Pattern Selection: "
				+ selectedFeature.length + " out of "
				+ rawFeatures.getFeatures().getfeatureNum()
				+ " frequent patterns as selected for Indexing");
		// 3. Return selected Features
		PostingFeaturesMultiClass selectedFeatures = rawFeatures
				.getSelectedFeatures(baseName + "selectedFeature", null, false);
		return selectedFeatures;
	}
}
