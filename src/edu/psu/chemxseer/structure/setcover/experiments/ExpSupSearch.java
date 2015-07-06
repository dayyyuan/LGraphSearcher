package edu.psu.chemxseer.structure.setcover.experiments;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures_Ext;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_LWFullBuilder;
import edu.psu.chemxseer.structure.supersearch.LWFull.SupSearch_Lindex;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;

/**
 * Given already mined features (1) Build index (2) Run the index
 * 
 * @author dayuyuan
 * 
 */
public class ExpSupSearch {

	public static void buildLWindex(GraphDatabase_OnDisk gDB, String folder,
			String featureFile) throws IOException, ParseException {
		File dir = new File(folder);
		if (!dir.exists())
			dir.mkdirs();
		NoPostingFeatures<IOneFeature> features = ExpGraphSearch
				.loadFeatures(featureFile);
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		builder.buildLindex(new NoPostingFeatures_Ext<IOneFeature>(features),
				gDB, folder, true, MyFactory.getDFSCoder());
	}

	public static void runLWindex(IGraphDatabase gDB, String folder,
			IGraphDatabase query) {
		SupSearch_LWFullBuilder builder = new SupSearch_LWFullBuilder();
		// First Load the index
		SupSearch_Lindex searcher = null;
		try {
			searcher = builder.loadLindex(gDB, folder, true,
					MyFactory.getDFSCoder());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searcher != null) {
			try {
				AIDSExp.runQueries(query, searcher);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
