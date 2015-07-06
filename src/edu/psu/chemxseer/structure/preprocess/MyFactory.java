package edu.psu.chemxseer.structure.preprocess;

import de.parmol.MoFa.DirectedListRingGraph;
import de.parmol.MoFa.DirectedMatrixRingGraph;
import de.parmol.MoFa.UndirectedListRingGraph;
import de.parmol.MoFa.UndirectedMatrixRingGraph;
import de.parmol.graph.ClassifiedDirectedListGraph;
import de.parmol.graph.ClassifiedDirectedMatrixGraph;
import de.parmol.graph.ClassifiedUndirectedListGraph;
import de.parmol.graph.ClassifiedUndirectedMatrixGraph;
import de.parmol.graph.DirectedListGraph;
import de.parmol.graph.DirectedMatrixGraph;
import de.parmol.graph.GraphFactory;
import de.parmol.graph.UndirectedListGraph;
import de.parmol.graph.UndirectedMatrixGraph;
import de.parmol.parsers.SmilesParser;
import edu.psu.chemxseer.structure.iso.CanonicalDFS;
import edu.psu.chemxseer.structure.iso.UnCanDFS;
import edu.psu.chemxseer.structure.parmolExtension.SDFParser2;
import edu.psu.chemxseer.structure.parmolExtension.SDFParserModified;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureImpl;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;

/**
 * This class is mainly constructed for making all utility object singleton This
 * class include several objects: 1. GraphFactory 2. CanonicalDFS 3.
 * SDFParserModified 4. SmilesParser 5. BasicDataSource
 * 
 * @author duy113
 * 
 */
public class MyFactory {
	private static GraphFactory myfactory;
	private static CanonicalDFS dfsCoder;
	private static UnCanDFS unCanDFS;
	private static SDFParserModified sdfParserModified;
	private static SDFParser2 sdfParser;
	private static SmilesParser smilesParser;

	static {
		Object o = UndirectedListGraph.Factory.instance;
		o = DirectedListGraph.Factory.instance;
		o = ClassifiedUndirectedListGraph.Factory.instance;
		o = ClassifiedDirectedListGraph.Factory.instance;
		o = UndirectedListRingGraph.Factory.instance;
		o = DirectedListRingGraph.Factory.instance;
		o = UndirectedMatrixGraph.Factory.instance;
		o = DirectedMatrixGraph.Factory.instance;
		o = ClassifiedUndirectedMatrixGraph.Factory.instance;
		o = ClassifiedDirectedMatrixGraph.Factory.instance;
		o = UndirectedMatrixRingGraph.Factory.instance;
		o = DirectedMatrixRingGraph.Factory.instance;
		o = o.getClass();
		o = OneFeatureImpl.Factory.instance;
		o = OneFeatureMultiClass.Factory.instance;

	}

	public static FeatureFactory getFeatureFactory(FeatureFactoryType type) {
		switch (type) {
		case OneFeature:
			return OneFeatureImpl.Factory.instance;
		case MultiFeature:
			return OneFeatureMultiClass.Factory.instance;
		default:
			return null;
		}
	}

	public static GraphFactory getGraphFactory() {
		if (myfactory == null) {
			int mask = GraphFactory.LIST_GRAPH | GraphFactory.UNDIRECTED_GRAPH
					| GraphFactory.CLASSIFIED_GRAPH;
			myfactory = GraphFactory.getFactory(mask);
		}
		return myfactory;
	}

	public static CanonicalDFS getDFSCoder() {
		if (dfsCoder == null)
			dfsCoder = new CanonicalDFS();
		return dfsCoder;
	}

	public static UnCanDFS getUnCanDFS() {
		if (unCanDFS == null)
			unCanDFS = new UnCanDFS(getDFSCoder());
		return unCanDFS;
	}

	public static SmilesParser getSmilesParser() {
		if (smilesParser == null)
			smilesParser = new SmilesParser();
		return smilesParser;
	}

	public static SDFParserModified getSDFParserM() {
		if (sdfParserModified == null)
			sdfParserModified = new SDFParserModified();
		return sdfParserModified;
	}

	public static SDFParser2 getSDFParserOriginal() {
		if (sdfParser == null)
			sdfParser = new SDFParser2();
		return sdfParser;
	}

}
