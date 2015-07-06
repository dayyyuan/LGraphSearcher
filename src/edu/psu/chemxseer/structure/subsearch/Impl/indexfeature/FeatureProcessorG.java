package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.parmolExtension.GindexMiner;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;

/**
 * The Raw Feature Mining for Gindex
 * 
 * @author dayuyuan
 * 
 */
public class FeatureProcessorG {

	public static void frequentSubgraphMining(GraphDatabase_OnDisk gDB,
			String featureFileName, String postingFileName,
			double minimumFrequency, int minMustSelectDepth,
			int maxNonSelectDepth, GraphParser gParser) {
		frequentSubgraphMining(gDB.getDBFileName(), featureFileName,
				postingFileName, minimumFrequency, minMustSelectDepth,
				maxNonSelectDepth, gParser);
	}

	public static PostingFeatures frequentSubgraphMining(String gDBFileName,
			String featureFileName, String postingFileName,
			double minimumFrequency, int minMustSelectDepth,
			int maxNonSelectDepth, GraphParser gParser) {

		// String[] args = {"-minimumFrequencies="+(-minimumFrequency),
		// "-maximumFragmentSize="+maxNonSelectDepth,
		// "-graphFile="+gDBFileName,
		// "-closedFragmentsOnly=flase", "-outputFile=temp",
		// "-parserClass=edu.psu.chemxseer.structure.iso.DFSCoder",
		// "-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
		// "-memoryStatistics=false", "-debug=1"};
		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency),
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-graphFile=" + gDBFileName,
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=0" };
		return GindexMiner.gIndexMining(args, featureFileName, postingFileName,
				minMustSelectDepth);

	}

	public static PostingFeatures frequentSubtreeMining(String gDBFileName,
			String featureFileName, String postingFileName,
			double minimumFrequency, int minMustSelectDepth,
			int maxNonSelectDepth, GraphParser gParser) {

		String[] args = {
				"-minimumFrequencies=" + (-minimumFrequency),
				"-maximumFragmentSize=" + maxNonSelectDepth,
				"-graphFile=" + gDBFileName,
				"-findTreesOnly=true",
				"-closedFragmentsOnly=flase",
				"-outputFile=temp",
				"-parserClass=" + gParser.getClass().getName(),
				"-serializerClass=edu.psu.chemxseer.structure.iso.CanonicalDFS",
				"-memoryStatistics=false", "-debug=0" };
		return GindexMiner.gIndexMining(args, featureFileName, postingFileName,
				minMustSelectDepth);
	}
}
