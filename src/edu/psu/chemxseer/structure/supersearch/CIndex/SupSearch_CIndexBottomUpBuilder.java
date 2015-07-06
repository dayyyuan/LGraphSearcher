package edu.psu.chemxseer.structure.supersearch.CIndex;

import java.io.File;
import java.io.IOException;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.PostingFeaturesMultiClass;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class SupSearch_CIndexBottomUpBuilder extends
		SupSearch_CIndexFlatBuilder {
	/**
	 * Build Bottom Up CIndex
	 * 
	 * @param gDB
	 * @param firstLevelFeature
	 * @param upperLevelFeatures
	 *            : need to be posting Features, since we are using "in-memory"
	 *            postings for upper level index, which is built directly from
	 *            the mined features
	 * @param baseName
	 * @param gSerializer
	 * @return
	 * @throws IOException
	 */
	public SupSearch_CIndexBottomUp buildCIndexBottomUp(IGraphDatabase gDB,
			NoPostingFeatures<IOneFeature> firstLevelFeature,
			PostingFeaturesMultiClass[] upperLevelFeatures, String baseName,
			GraphParser gSerializer, boolean lucene_in_mem) throws IOException {
		System.out.println("Build CIndex BottomUP");
		// 1. First build the First level flat index
		String firstLevelBaseName = baseName + "main/";
		File temp = new File(firstLevelBaseName);
		if (!temp.exists())
			temp.mkdirs();
		SupSearch_CIndexFlatBuilder builder = new SupSearch_CIndexFlatBuilder();
		System.out.println("Level 0");
		SupSearch_CIndexFlat firstIndex = builder.buildCIndexFlat(gDB,
				firstLevelFeature, firstLevelBaseName, gSerializer,
				lucene_in_mem);
		SupSearch_CIndexBottomUp Index = new SupSearch_CIndexBottomUp(
				firstIndex);
		// 2. Other level of index
		for (int i = 0; i < upperLevelFeatures.length; i++) {
			// 2.2 Given the feature, construct higher level indexed
			int level = i + 1;
			System.out.println("Level " + level);
			String levelBaseName = baseName + i + "/";
			temp = new File(levelBaseName);
			if (!temp.exists())
				temp.mkdirs();
			SupSearch_CIndexFlat upperIndex = builder.buildCIndexFlat(
					firstIndex, upperLevelFeatures[i], levelBaseName);
			Index.addOneLevel(upperIndex);
			firstIndex = upperIndex;
		}
		return Index;
	}

	/**
	 * Load the Index into Memory:
	 * 
	 * @param gDB
	 * @param baseName
	 * @param level
	 * @param gSerializer
	 * @return
	 * @throws IOException
	 */
	public SupSearch_CIndexBottomUp loadCIndexBottomUp(IGraphDatabase gDB,
			String baseName, int level, GraphParser gSerializer,
			boolean lucene_in_mem) throws IOException {
		SupSearch_CIndexFlat firstLevel = loadCIndexFlat(gDB, baseName
				+ "main/", gSerializer, lucene_in_mem);
		SupSearch_CIndexBottomUp Index = new SupSearch_CIndexBottomUp(
				firstLevel);
		level = level - 1;
		for (int i = 0; i < level; i++) {
			SupSearch_CIndexFlat otherLevel = loadCIndexFlat(firstLevel,
					baseName + i + "/");
			Index.addOneLevel(otherLevel);
			firstLevel = otherLevel;
		}
		return Index;
	}
}
