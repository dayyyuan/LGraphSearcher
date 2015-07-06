package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * A Implementation of One GFeature FeatureFile: <String(canonidalLabel), ID,
 * Frequency, PostingShift(location of postingList in PostingFile)>
 * 
 * @author dayuyuan
 * 
 */
public class OneFeatureImpl implements IOneFeature {
	protected String label;
	protected Graph featureGraph;
	protected int frequency;
	protected long shift;
	protected int id;
	protected boolean selected;

	/**
	 * 
	 * @param label
	 * @param frequency
	 * @param shift
	 * @param id
	 * @param selected
	 */
	public OneFeatureImpl(String label, int frequency, long shift, int id,
			boolean selected) {
		this.label = label;
		this.frequency = frequency;
		this.shift = shift;
		this.id = id;
		this.selected = selected;
	}

	/**
	 * Copy constructor
	 * 
	 * @param iOneFeature
	 */
	public OneFeatureImpl(OneFeatureImpl iOneFeature) {
		this.label = iOneFeature.label;
		this.frequency = iOneFeature.frequency;
		this.shift = iOneFeature.shift;
		this.id = iOneFeature.id;
		this.selected = iOneFeature.selected;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected() {
		selected = true;
	}

	@Override
	public void setUnselected() {
		selected = false;
	}

	@Override
	public Graph getFeatureGraph() {
		if (featureGraph != null)
			return featureGraph;
		else
			return MyFactory.getDFSCoder().parse(label,
					MyFactory.getGraphFactory());
	}

	@Override
	public void creatFeatureGraph(int gID) {
		if (featureGraph == null)
			featureGraph = MyFactory.getDFSCoder().parse(label,
					new Integer(gID).toString(), MyFactory.getGraphFactory());
	}

	@Override
	public String getDFSCode() {
		return label;
	}

	@Override
	public int getFrequency() {
		return frequency;
	}

	@Override
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public long getPostingShift() {
		return shift;
	}

	@Override
	public void setPostingShift(long shift) {
		this.shift = shift;
	}

	@Override
	public int getFeatureId() {
		return id;
	}

	@Override
	public void setFeatureId(int id) {
		this.id = id;
	}

	@Override
	public String toFeatureString() {
		StringBuffer bbuf = new StringBuffer();
		bbuf.append(this.id);
		bbuf.append(",");
		bbuf.append(this.label);
		bbuf.append(",");
		bbuf.append(this.frequency);
		bbuf.append(",");
		bbuf.append(shift);
		bbuf.append(",");
		bbuf.append(this.selected);
		return bbuf.toString();
	}

	public static class Factory extends FeatureFactory {
		public final static Factory instance = new Factory();

		@Override
		public OneFeatureImpl genOneFeature(int id, String featureString) {
			String[] tokens = featureString.split(",");
			String label = tokens[1];
			int frequency = -1;
			long shift = -1;
			boolean selected = false;
			int ID = -1;

			if (tokens.length > 2) {
				frequency = Integer.parseInt(tokens[2]);
				shift = Long.parseLong(tokens[3]);
				if (tokens.length > 4)
					selected = Boolean.parseBoolean(tokens[4]);
				else
					selected = false;
			}
			if (id == -1)
				ID = Integer.parseInt(tokens[0]);
			else
				ID = id;
			return new OneFeatureImpl(label, frequency, shift, ID, selected);
		}

	}
}
