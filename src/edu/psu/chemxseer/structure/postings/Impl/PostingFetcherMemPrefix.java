package edu.psu.chemxseer.structure.postings.Impl;

import java.util.Arrays;
import java.util.List;

import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcherPrefix;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndexPrefix;
import edu.psu.chemxseer.structure.util.OrderedIntSet;

public class PostingFetcherMemPrefix extends PostingBuilderMem implements
		IPostingFetcherPrefix {
	private GraphDatabase_Prefix gDB;
	private IIndexPrefix index;

	public PostingFetcherMemPrefix(GraphDatabase_Prefix gDB, IIndexPrefix index) {
		super();
		this.gDB = gDB;
		this.index = index;
	}

	public PostingFetcherMemPrefix(GraphDatabase_Prefix gDB, String fileName,
			IIndexPrefix index) {
		super(fileName);
		this.gDB = gDB;
		this.index = index;
	}

	public PostingFetcherMemPrefix(GraphDatabase_Prefix gDB,
			PostingBuilderMem postingBuilder, IIndexPrefix index) {
		super(postingBuilder);
		this.gDB = gDB;
		this.index = index;
	}

	@Override
	public GraphFetcherDBPrefix getPosting(int featureID, long[] TimeComponent) {
		Integer id = this.nameConverter.get(featureID);
		long start = System.currentTimeMillis();
		if (id == null) {
			System.out.println("Error in getPosting: illiegal input featureID");
			return null;
		} else {
			int bound = bounds.get(id);
			int[] temp = Arrays.copyOf(postings.get(id), bound);
			TimeComponent[0] += System.currentTimeMillis() - start;
			GraphFetcherDBPrefix result = new GraphFetcherDBPrefix(gDB, temp,
					false, index);
			if (result.size() == 0)
				System.out
						.println("Empty Return Result in PostingFetcherMem: getPosting");
			return result;
		}
	}

	public GraphFetcherDBPrefix getPosting(int[] dbPosting) {
		return new GraphFetcherDBPrefix(gDB, dbPosting, false, index);
	}

	@Override
	public GraphFetcherDBPrefix getPosting(String featureString,
			long[] TimeComponent) {
		System.out
				.println("The PostingFetcherMem: getPosting(FeatureString) is not implemented");
		throw new UnsupportedOperationException();
	}

	@Override
	public GraphFetcherDBPrefix getUnion(List<Integer> featureIDs,
			long[] TimeComponent) {
		long start = System.currentTimeMillis();
		OrderedIntSet set = new OrderedIntSet();
		for (int i = 0; i < featureIDs.size(); i++) {
			Integer it = this.nameConverter.get(featureIDs.get(i));
			if (it == null) {
				System.out
						.println("Error in getUnion: illigale input featureID");
				return null;
			} else {
				int bound = bounds.get(it);
				set.add(this.postings.get(it), 0, bound);
			}

		}
		int[] temp = set.getItems();
		TimeComponent[0] += System.currentTimeMillis() - start;
		GraphFetcherDBPrefix result = new GraphFetcherDBPrefix(gDB, temp,
				false, index);
		if (result.size() == 0)
			System.out
					.println("Empty Return Result in PostingFetcherMem: getUnion");
		return result;
	}

	@Override
	public GraphFetcherDBPrefix getJoin(List<Integer> featureIDs,
			long[] TimeComponent) {
		long start = System.currentTimeMillis();
		OrderedIntSet set = new OrderedIntSet();
		for (int i = 0; i < featureIDs.size(); i++) {
			Integer it = this.nameConverter.get(featureIDs.get(i));
			if (it == null) {
				System.out
						.println("Error in getJoin: illigale input featureID");
				return null;
			} else if (i == 0)
				set.add(this.postings.get(it), 0, bounds.get(it));
			else
				set.join(this.postings.get(it), 0, bounds.get(it));
		}
		int[] temp = set.getItems();
		TimeComponent[0] += System.currentTimeMillis() - start;
		GraphFetcherDBPrefix result = new GraphFetcherDBPrefix(gDB, temp,
				false, index);
		if (result.size() == 0)
			System.out
					.println("Empty Return Result in PostingFetcherMem: getJoin");
		return result;
	}

	@Override
	public GraphFetcherDBPrefix getJoin(String[] featureStrings,
			long[] TimeComponent) {
		System.out
				.println("The PostingFetcherMem: getJoin(FeatureString) is not implemented");
		throw new UnsupportedOperationException();
	}

	@Override
	public GraphFetcherDBPrefix getComplement(List<Integer> featureIDs,
			long[] TimeComponent) {
		long start = System.currentTimeMillis();
		OrderedIntSet set = new OrderedIntSet();
		for (int i = 0; i < featureIDs.size(); i++) {
			Integer it = this.nameConverter.get(featureIDs.get(i));
			if (it == null) {
				System.out
						.println("Error in getUnion: illigale input featureID");
				return new GraphFetcherDBPrefix(gDB, new int[0], true, index);
			} else
				set.add(this.postings.get(it), 0, bounds.get(it));
		}
		int[] temp = set.getItems();
		TimeComponent[0] += System.currentTimeMillis() - start;
		GraphFetcherDBPrefix result = new GraphFetcherDBPrefix(gDB, temp, true,
				index); // reverse
		if (result.size() == 0)
			System.out
					.println("Empty Return Result in PostingFetcherMem: getComplete");
		return result;
	}

	@Override
	public PostingBuilderMem loadPostingIntoMemory(IIndex0 indexSearcher) {
		return this;
	}

	@Override
	public int getDBSize() {
		return this.gDB.getTotalNum();
	}

	@Override
	public int[] getPostingID(int featureID) {
		Integer id = this.nameConverter.get(featureID);
		if (id == null) {
			// System.out.println("Error in getPosting: illiegal input featureID");
			return new int[0];
		} else {
			int bound = bounds.get(id);
			return Arrays.copyOf(postings.get(id), bound);
		}
	}

	public GraphDatabase_Prefix getDB() {
		return this.gDB;
	}

}
