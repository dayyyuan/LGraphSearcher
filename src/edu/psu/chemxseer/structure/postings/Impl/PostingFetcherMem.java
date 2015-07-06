package edu.psu.chemxseer.structure.postings.Impl;

import java.util.Arrays;
import java.util.List;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IGraphFetcher;
import edu.psu.chemxseer.structure.postings.Interface.IPostingFetcher;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IIndex0;
import edu.psu.chemxseer.structure.util.OrderedIntSet;

/**
 * The in-memory posting fetcher: all the postings are represented as arrays
 * 
 * @author dayuyuan
 * 
 */
public class PostingFetcherMem extends PostingBuilderMem implements
		IPostingFetcher {

	private IGraphDatabase gDB;

	public PostingFetcherMem(IGraphDatabase gDB) {
		super();
		this.gDB = gDB;
	}

	public PostingFetcherMem(IGraphDatabase gDB, String fileName) {
		super(fileName);
		this.gDB = gDB;
	}

	public PostingFetcherMem(IGraphDatabase newGDB,
			PostingBuilderMem postingBuilder) {
		super(postingBuilder);
		this.gDB = newGDB;
	}

	@Override
	public IGraphFetcher getPosting(int featureID, long[] TimeComponent) {
		Integer id = this.nameConverter.get(featureID);
		long start = System.currentTimeMillis();
		if (id == null) {
			System.out.println("Error in getPosting: illiegal input featureID");
			return null;
		} else {
			int bound = bounds.get(id);
			int[] temp = Arrays.copyOf(postings.get(id), bound);
			TimeComponent[0] += System.currentTimeMillis() - start;
			GraphFetcherDB result = new GraphFetcherDB(gDB, temp, false);
			if (result.size() == 0)
				System.out
						.println("Empty Return Result in PostingFetcherMem: getPosting");
			return result;
		}
	}

	@Override
	public IGraphFetcher getPosting(String featureString, long[] TimeComponent) {
		System.out
				.println("The PostingFetcherMem: getPosting(FeatureString) is not implemented");
		throw new UnsupportedOperationException();
	}

	@Override
	public IGraphFetcher getUnion(List<Integer> featureIDs, long[] TimeComponent) {
		if (featureIDs == null || featureIDs.size() == 0)
			return new GraphFetcherDB(gDB, new int[0], false);

		long start = System.currentTimeMillis();
		OrderedIntSet set = new OrderedIntSet();
		for (int i = 0; i < featureIDs.size(); i++) {
			Integer it = this.nameConverter.get(featureIDs.get(i));
			if (it == null) {
				// System.out.println("Error in getUnion: illigale input featureID: "
				// + featureIDs.get(i));
				continue;
			} else {
				int bound = bounds.get(it);
				set.add(this.postings.get(it), 0, bound);
			}

		}
		int[] temp = set.getItems();
		TimeComponent[0] += System.currentTimeMillis() - start;
		GraphFetcherDB result = new GraphFetcherDB(gDB, temp, false);
		if (result.size() == 0)
			System.out
					.println("Empty Return Result in PostingFetcherMem: getUnion");
		return result;
	}

	@Override
	public IGraphFetcher getJoin(List<Integer> featureIDs, long[] TimeComponent) {
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
		GraphFetcherDB result = new GraphFetcherDB(gDB, temp, false);
		// if(result.size() == 0)
		// System.out.println("Empty Return Result in PostingFetcherMem: getJoin");
		return result;
	}

	@Override
	public IGraphFetcher getJoin(String[] featureStrings, long[] TimeComponent) {
		System.out
				.println("The PostingFetcherMem: getJoin(FeatureString) is not implemented");
		throw new UnsupportedOperationException();
	}

	@Override
	public IGraphFetcher getComplement(List<Integer> featureIDs,
			long[] TimeComponent) {
		long start = System.currentTimeMillis();
		OrderedIntSet set = new OrderedIntSet();
		for (int i = 0; i < featureIDs.size(); i++) {
			Integer it = this.nameConverter.get(featureIDs.get(i));
			if (it == null) {
				System.out
						.println("Error in getUnion: illigale input featureID");
				return new GraphFetcherDB(gDB, new int[0], true);
			} else
				set.add(this.postings.get(it), 0, bounds.get(it));
		}
		int[] temp = set.getItems();
		TimeComponent[0] += System.currentTimeMillis() - start;
		GraphFetcherDB result = new GraphFetcherDB(gDB, temp, true); // reverse
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

	public IGraphDatabase getDB() {
		return this.gDB;
	}

}
