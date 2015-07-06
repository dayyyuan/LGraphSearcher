package edu.psu.chemxseer.structure.setcover.update;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.parmol.util.FrequentFragment;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.setcover.IO.Input_Stream;
import edu.psu.chemxseer.structure.setcover.IO.Set_Pair;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureConverterSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureWrapperSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureProcessorDuralClass;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;
import edu.psu.chemxseer.structure.subsearch.Lindex.LindexTerm;

public class IndexUpdator_Input extends Input_Stream {

	private IFeatureConverter fullFeatureConverter;
	private ISearch_LindexUpdatable index;

	/**
	 * Construct the Indexupdate_Input
	 * 
	 * @param sType
	 *            : if adv2, then return the set-pair-index, if adv, then return
	 *            the set-pair
	 * @param index
	 * @param converter
	 * @return
	 */
	public static IndexUpdator_Input newInstance(StatusType sType,
			ISearch_LindexUpdatable index, IFeatureConverter converter) {
		return new IndexUpdator_Input(sType, index, converter);
	}

	/**
	 * The majore difference between Input_Stream & Input_Streamupdate is that,
	 * in Input_StreamUpdate, the edgeSet is constructed by looking up the
	 * searcher, instead of mining from the patternGenerator. Because of two
	 * reasons: (1) patternGenerator may contain incomplete database graphs (2)
	 * distinct edges can be found by looking up the index without extra cost
	 * 
	 * @param index
	 * @param converter
	 */
	private IndexUpdator_Input(StatusType sType, ISearch_LindexUpdatable index,
			IFeatureConverter converter) {
		super();
		this.converter = converter;
		this.fullFeatureConverter = converter.getFullRangeConverter();
		index.constructQueryPosting();
		LindexTerm[] edgeTerms = index.getIndexSearcher().getFirstLevelTerms();
		this.edgeSet = new ISet[edgeTerms.length];
		for (int i = 0; i < edgeSet.length; i++) {
			LindexTerm aTerm = edgeTerms[i];
			edgeSet[i] = this.converter.toSetPair(this.toFeatureWrapper(aTerm,
					index));
		}
		this.enumeratedSetCount = 0;
		this.patternGenerator = null;// delay the pattern generator construction
		if (sType == StatusType.decomposePreSelectAdv)
			this.index = null;
		else if (sType == StatusType.decomposedPreSelectAdv2)
			this.index = index;
	}

	/**
	 * Given the index (include both the queries & database graphs), try to
	 * identify the worst performed queries And enumerate patterns out of it.
	 * 
	 * @param index
	 */
	public void constructPatternGen(ISearch_LindexUpdatable index,
			IGraphDatabase gDB1, IGraphDatabase gDB2, double minSupport1,
			double minSupport2) {
		this.patternGenerator = FeatureProcessorDuralClass
				.getPatternEnumerator(gDB1, gDB2, minSupport1, minSupport2, 10);
	}

	@Override
	public Iterator<ISet> iterator() {
		this.enumeratedSetCount = 0;
		this.patternEnumerationTime = 0;
		if (patternGenerator != null)
			this.patternGenerator.restart();
		return new StreamIterator();
	}

	private class StreamIterator implements Iterator<ISet> {
		private boolean isNextAvailable;

		public StreamIterator() {
			this.isNextAvailable = true;
		}

		@Override
		public boolean hasNext() {
			return this.isNextAvailable;
		}

		@Override
		public ISet next() {
			ISet nextSet = nextSet();
			if (nextSet == null) {
				isNextAvailable = false;
				return null;
			} else {
				enumeratedSetCount++;
				return nextSet;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"remove() method is not supported");
		}
	}

	/**
	 * One major difference between IndexUpdator_Input & Input_Stream Here, a
	 * normal set_pair set is returned if the index is null Or a set_pair_index
	 * set is returned if the index is not null
	 * 
	 * @return
	 */
	@Override
	public ISet nextSet() {
		long startT = System.currentTimeMillis();
		if (patternGenerator == null)
			return null;
		FrequentFragment frag = patternGenerator.nextPattern();
		if (frag == null)
			return null;
		// The place that are different from Input_Stream.nextSet()
		ISet result = null;
		if (this.index == null)
			result = this.converter
					.toSetPair(patternGenerator.getFeature(frag));
		else
			result = this.converter.toSetPairIndex(
					patternGenerator.getFeature(frag), index);
		patternEnumerationTime += System.currentTimeMillis() - startT;
		return result;
	}

	/**
	 * Given a lindex term, convert to a iFeatureWrapper The feature ID is in
	 * accordance with term id (that is the position of the term on the index)
	 * 
	 * @param aTerm
	 * @param searcher
	 * @return
	 */
	private IFeatureWrapper toFeatureWrapper(LindexTerm aTerm,
			ISearch_LindexUpdatable searcher) {
		String gString = MyFactory.getDFSCoder().writeArrayToText(
				searcher.getIndexSearcher().getTermFullLabel(aTerm));
		int[] freq = new int[4];
		int[][] thePostings = new int[4][];
		thePostings[0] = searcher.getGraphFetcher().getPostingID(aTerm.getId());
		freq[0] = thePostings[0].length;
		freq[1] = 0;
		thePostings[1] = new int[0];
		thePostings[2] = searcher.getQueryFetcher().getPostingID(aTerm.getId());
		freq[2] = thePostings[2].length;
		if (this.converter.isAppType(AppType.subSearch)) {
			List<Integer> equals = searcher.getEqualQ(aTerm.getId());
			if (equals != null && equals.size() > 0) {
				freq[3] = equals.size();
				thePostings[3] = new int[freq[3]];
				for (int w = 0; w < equals.size(); w++)
					thePostings[3][w] = equals.get(w);
			} else {
				freq[3] = 0;
				thePostings[3] = new int[0];
			}
		} else {
			freq[3] = 0;
			thePostings[3] = new int[0];
		}

		OneFeatureMultiClass aFeature = new OneFeatureMultiClass(gString, freq,
				new long[4], aTerm.getId(), true);
		IFeatureWrapper result = new FeatureWrapperSimple(aFeature, thePostings);
		return result;
	}

	/**
	 * Given a Lindex Searcher, return all the index terms (except for the first
	 * level) as an Array of Set_Pairs. The featureID is in accordance with the
	 * index.position
	 * 
	 * @param index
	 * @return
	 */
	public Set_Pair[] toSets(ISearch_LindexUpdatable index,
			Map<Integer, Integer> setPosLindexPosMap) {
		LindexTerm[] firstLevel = index.getIndexSearcher().getFirstLevelTerms();
		Set<LindexTerm> firstTermSet = new HashSet<LindexTerm>();
		for (LindexTerm term : firstLevel)
			firstTermSet.add(term);
		LindexTerm[] allTerms = index.getIndexSearcher().getAllTerms();
		Set_Pair[] result = new Set_Pair[allTerms.length - firstLevel.length];
		for (int i = 0, j = 0; i < allTerms.length; i++) {
			if (firstTermSet.contains(allTerms[i]))
				continue;
			result[j] = this.converter.toSetPair(this.toFeatureWrapper(
					allTerms[i], index));
			setPosLindexPosMap.put(j, i); // The j th set maps to the ith index
											// feature
			j++;
		}
		return result;
	}

	public void setConverter(FeatureConverterSimple featureConverterSimple) {
		this.converter = featureConverterSimple;
		this.fullFeatureConverter = converter.getFullRangeConverter();
	}

	public IFeatureConverter getFullFeatureConverter() {
		return this.fullFeatureConverter;
	}
}
