package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.parmolExtension.GSpanMiner_Frequent;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;

/**
 * The Feature Processor Steps [Feature Mining] for FGFeatures (raw)
 * 
 * @author dayuyuan
 * 
 */
public class FeatureProcessorFG {

	public static PostingFeatures frequentSubgraphMining(String gDBFileName,
			String featureFileName, String postingFileName,
			double minimumFrequency, int maxNonSelectDepth, GraphParser gParser) {

		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency),
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-graphFile=" + gDBFileName,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=1" };
		return GSpanMiner_Frequent.gSpanMining(args, featureFileName,
				postingFileName);
	}

	public static PostingFeatures frequentSubgraphMining(IGraphDatabase gDB,
			String featureFileName, String postingFileName,
			double minimumFrequency, int maxNonSelectDepth, GraphParser gParser) {
		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency),
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=0" };
		return GSpanMiner_Frequent.gSpanMining(gDB, args, featureFileName,
				postingFileName);
	}
}
