package edu.psu.chemxseer.structure.setcover.IO;

import java.util.Comparator;
import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.util.HasPos;

/**
 * In implementation of the set: with field score & position Wrapper of an
 * implementation of ISet
 * 
 * @author dayuyuan
 * 
 */
public class SetwithScore implements ISet, HasPos {
	private ISet set;
	private int pos;
	private long score;

	public SetwithScore(ISet set, int pos, long score) {
		this.set = set;
		this.pos = pos;
		this.score = score;
	}

	public SetwithScore(ISet set) {
		this.set = set;
		pos = -1;
		score = -1;
	}

	@Override
	public void setSetID(int i) {
		set.setSetID(i);
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public void lazyDelete() {
		this.set.lazyDelete();
	}

	@Override
	public boolean isDeleted() {
		return set.isDeleted();
	}

	@Override
	public int getSetID() {
		return set.getSetID();
	}

	// SetWithScore Specific

	@Override
	public int getPos() {
		return pos;
	}

	@Override
	public void setPos(int pos) {
		this.pos = pos;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public long getScore() {
		return this.score;
	}

	public static Comparator<SetwithScore> getComparator() {
		if (comparator == null)
			comparator = new ScoreComparator();
		return comparator;
	}

	private static ScoreComparator comparator;

	@Override
	public Iterator<int[]> iterator() {
		return set.iterator();
	}

	public ISet getSet() {
		return this.set;
	}

	@Override
	public IFeatureWrapper getFeature() {
		return this.set.getFeature();
	}
}

class ScoreComparator implements Comparator<SetwithScore> {
	public ScoreComparator() {

	}

	@Override
	public int compare(SetwithScore arg0, SetwithScore arg1) {
		return (int) (arg0.getScore() - arg1.getScore());
	}

}
