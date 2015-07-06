package edu.psu.chemxseer.structure.setcover.experiments;

import java.io.IOException;
import java.text.ParseException;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.ISearcher;
import edu.psu.chemxseer.structure.subsearch.Lindex.SubSearch_LindexSimpleBuilder;
import edu.psu.chemxseer.structure.supersearch.experiments.AIDSExp;

/**
 * Given already mined features: (1) Build index with the features (2) Run the
 * index
 * 
 * @author dayuyuan
 * 
 */
public class ExpSubSearch {

	public static float[] runLindexWithMinNum(IGraphDatabase gDB,
			String folder, IGraphDatabase query, int minNum) {
		SubSearch_LindexSimpleBuilder builder = new SubSearch_LindexSimpleBuilder();
		// First Load the index
		ISearcher searcher = null;
		try {
			searcher = builder.loadIndex(gDB, folder, MyFactory.getDFSCoder(),
					true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searcher != null) {
			try {
				return AIDSExp.runQueries(query, searcher, minNum);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return new float[0];
	}

	public static float[] runLindex(IGraphDatabase gDB, String folder,
			IGraphDatabase query) {
		SubSearch_LindexSimpleBuilder builder = new SubSearch_LindexSimpleBuilder();
		// First Load the index
		ISearcher searcher = null;
		try {
			searcher = builder.loadIndex(gDB, folder, MyFactory.getDFSCoder(),
					true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searcher != null) {
			try {
				return AIDSExp.runQueries(query, searcher);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return new float[0];
	}

	public static float[] runIndex(ISearcher gIndex, IGraphDatabase query) {
		try {
			return AIDSExp.runQueries(query, gIndex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new float[0];
	}

	public static float[] runIndexWithMinNum(ISearcher gIndex,
			IGraphDatabase query, int minNum) {
		try {
			return AIDSExp.runQueries(query, gIndex, minNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new float[0];
	}
}
