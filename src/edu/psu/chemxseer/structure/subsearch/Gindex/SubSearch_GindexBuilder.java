package edu.psu.chemxseer.structure.subsearch.Gindex;

import java.io.IOException;
import java.text.ParseException;

import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLucene;
import edu.psu.chemxseer.structure.postings.Impl.PostingBuilderLuceneVectorizerNormal;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureComparatorBase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexConstructor;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexSearcher;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class SubSearch_GindexBuilder {
	// For now, this parameters are hard coded, can be changed latter
	private int minEdgeNum = 3;
	private float disRatio = 2;

	public SubSearch_GindexBuilder() {

	}

	public SubSearch_GindexBuilder(int minEdgeNum) {
		this.minEdgeNum = minEdgeNum;
	}

	public SubSearch_Gindex loadIndex(GraphDatabase_OnDisk gDB,
			boolean exhaustSearch, String baseName, GraphParser gParser,
			boolean lucene_in_mem) throws IOException {
		GindexSearcher gIndex = GindexConstructor.loadSearcher(baseName,
				SubSearch_Gindex.getIndexName(), exhaustSearch);
		String lucenePath = baseName + SubSearch_Gindex.getLuceneName();
		PostingFetcherLucene posting = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), gParser, lucene_in_mem);
		return new SubSearch_Gindex(gIndex, posting, new VerifierISO());
	}

	/**
	 * Build the Index using candidate features without feature selections
	 * (assume all the features are already selected)
	 * 
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public SubSearch_Gindex buildIndexWithoutFeatureSelection(
			NoPostingFeatures_Ext<IOneFeature> candidateFeatures,
			GraphDatabase_OnDisk gDB, boolean exhaustSearch, String baseName,
			GraphParser gSerializer, boolean lucene_in_mem) throws IOException,
			ParseException {
		// set all features to be selected, sort those features (for Lindex
		// construction)
		for (int i = 0; i < candidateFeatures.getfeatureNum(); i++)
			candidateFeatures.getFeature(i).setSelected();
		candidateFeatures.mineSubSuperRelation();
		// 1. Construct Gindex
		long time1 = System.currentTimeMillis();
		GindexSearcher gIndex = GindexConstructor.construct(candidateFeatures,
				exhaustSearch);
		long time2 = System.currentTimeMillis();
		System.out
				.println("1. Time For Building Gindex without Feature Selection: "
						+ (time2 - time1));
		GindexConstructor.saveSearcher(gIndex, baseName,
				SubSearch_Gindex.getIndexName());
		// 2. Using Lindex to Build Lucene
		// 2.1 Build Lindex Searcher
		LindexSearcher linCons = LindexConstructor.construct(candidateFeatures);

		long time4 = System.currentTimeMillis();
		String lucenePath = baseName + SubSearch_Gindex.getLuceneName();
		PostingBuilderLucene postingBuilder = new PostingBuilderLucene(
				new PostingBuilderLuceneVectorizerNormal(gSerializer, linCons));
		postingBuilder.buildLuceneIndex(lucenePath, gIndex.getFeatureCount(),
				gDB, null);
		long time5 = System.currentTimeMillis();
		System.out.println("2. Time For Building Lucene: " + (time5 - time4));
		PostingFetcherLucene posting = new PostingFetcherLucene(lucenePath,
				gDB.getTotalNum(), gSerializer, lucene_in_mem);

		// 6. Return the Searcher
		return new SubSearch_Gindex(gIndex, posting, new VerifierISO());
	}

	/**
	 * Select Discriminative features from frequent subgraphs and build the
	 * Gindex
	 * 
	 * @param candidateFeatures
	 * @param gDB
	 * @param exhaustSearch
	 * @param baseName
	 * @param selectedFeatures
	 * @param selectedPostings
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public SubSearch_Gindex buildIndex(PostingFeatures candidateFeatures,
			GraphDatabase_OnDisk gDB, boolean exhaustSearch, String baseName,
			String selectedFeatures, String selectedPostings,
			GraphParser gSerializer) throws IOException, ParseException {
		return buildIndexReal(candidateFeatures, gDB, exhaustSearch, baseName,
				selectedFeatures, selectedPostings, gSerializer, true);
	}

	public SubSearch_Gindex buildIndexReal(PostingFeatures candidateFeatures,
			GraphDatabase_OnDisk gDB, boolean exhaustSearch, String baseName,
			String selectedFeatures, String selectedPostings,
			GraphParser gSerializer, boolean buildLucene) throws IOException,
			ParseException {
		// MemoryConsumptionCal.runGC();
		System.out.println("Start Build Gindex: Memory consumption in B "
				+ MemoryConsumptionCal.usedMemoryinMB());

		long start = System.currentTimeMillis();
		NoPostingFeatures<IOneFeature> noPostingFeatures = candidateFeatures
				.getFeatures();
		noPostingFeatures.createGraphs();
		noPostingFeatures.sortFeatures(new FeatureComparatorBase());

		int discriminativeFeaturesNum = 0, indiscriminativeFeaturesNum = 0;
		// 1. Features with edge less than minimumEdgeNum are all selected as
		// discriminative
		int selectedFeatureNum = 0;
		for (int i = 0; i < noPostingFeatures.getfeatureNum(); i++) {
			IOneFeature aFeature = noPostingFeatures.getFeature(i);
			if (aFeature.getFeatureGraph().getEdgeCount() <= minEdgeNum) {
				aFeature.setSelected();
				selectedFeatureNum++;
				discriminativeFeaturesNum++;
			} else
				aFeature.setUnselected();
		}
		long time1 = System.currentTimeMillis();
		System.out.println("1. Time for organizing candidate features: "
				+ (time1 - start));
		MemoryConsumptionCal.runGC();
		System.out.println("Memory consumption in B"
				+ MemoryConsumptionCal.usedMemoryinMB());
		// 2. Construct Gindex based on current selected features: use the
		// in-memory posting
		// Construct Postings: true [as to construct the postings for in-memory
		// features]
		GindexConstructor gIndexCon = new GindexConstructor();
		GindexSearcher gIndex = gIndexCon.constructWithPostings(
				candidateFeatures, exhaustSearch);
		long time2 = System.currentTimeMillis();
		System.out.println("# of Must Selected Features:"
				+ discriminativeFeaturesNum);
		System.out.println("2. Time For Constructing Gindex: "
				+ (time2 - time1));
		MemoryConsumptionCal.runGC();
		System.out.println("Memory consumption in B"
				+ MemoryConsumptionCal.usedMemoryinMB());
		// 3. Selects Discriminative features as Gindex terms
		// Because candidateFeatures is already sorted, each feature
		// is selected according to their edge number size
		for (int featureIndex = selectedFeatureNum; featureIndex < noPostingFeatures
				.getfeatureNum(); featureIndex++) {
			IOneFeature feature = noPostingFeatures.getFeature(featureIndex);
			if (feature.isSelected())
				System.out
						.println("Exception: In GindexBuilderLucene, buildIndex, step 4.1");
			int[] candidates = gIndexCon.getCandidatesBuild(gIndex,
					candidateFeatures, feature);
			if (candidates.length < feature.getFrequency()) {
				System.out
						.println("Exception: In GindexBuilderLucene, buildIndex, step 4.2");
			}
			if (candidates.length >= disRatio * feature.getFrequency()) {
				feature.setSelected();
				// update Gindex: change unselected feature into selected on
				try {
					gIndexCon.addDisriminativeTerm(gIndex, candidateFeatures,
							featureIndex, feature.getFeatureGraph()
									.getEdgeCount());
				} catch (IOException e) {
					e.printStackTrace();
				}
				discriminativeFeaturesNum++;
			} else
				indiscriminativeFeaturesNum++;
		}
		long time3 = System.currentTimeMillis();
		System.out.println("# of Discrimnative Features Selected: "
				+ discriminativeFeaturesNum
				+ "# of inDiscrimiantive Feature Selected: "
				+ indiscriminativeFeaturesNum);
		System.out.println("3. Time For Selecting F&D features: "
				+ (time3 - time2));
		MemoryConsumptionCal.runGC();
		System.out.println("Memory consumption in B"
				+ MemoryConsumptionCal.usedMemoryinMB());

		// store the selected features
		candidateFeatures.getFeatures().saveFeatures(
				baseName + "StatusRecordedFeatures");
		candidateFeatures.getSelectedFeatures(selectedFeatures,
				selectedPostings, true);

		// 4. Prune useless white nodes
		// gIndex.reorganize();
		// should store the gIndex after removal
		GindexConstructor.saveSearcher(gIndex, baseName,
				SubSearch_Gindex.getIndexName());

		if (buildLucene) {
			// 5. Build Lucene Index
			long time4 = System.currentTimeMillis();
			String lucenePath = baseName + SubSearch_Gindex.getLuceneName();
			PostingBuilderLucene postingBuilder = new PostingBuilderLucene(
					new PostingBuilderLuceneVectorizerNormal(gSerializer,
							gIndex));
			postingBuilder.buildLuceneIndex(lucenePath,
					gIndex.getFeatureCount(), gDB, null);
			long time5 = System.currentTimeMillis();
			System.out.println("4. Time For Building Lucene: "
					+ (time5 - time4));
			PostingFetcherLucene posting = new PostingFetcherLucene(lucenePath,
					gDB.getTotalNum(), gSerializer, false);

			// 6. Return the Searcher
			return new SubSearch_Gindex(gIndex, posting, new VerifierISO());
		} else
			return null;
	}

}
