package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.io.Serializable;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * A Implementation of The IOneFeature
 * 
 * @author dayuyuan
 * 
 */
public class OneFeatureMultiClass implements IOneFeature, Serializable {
	private static final long serialVersionUID = 2103521311370783733L;

	protected String label;
	transient protected Graph featureGraph;
	protected int[] frequency;
	protected long[] shift;
	protected int id;
	protected boolean selected;

	@Override
	public OneFeatureMultiClass clone() {
		return new OneFeatureMultiClass(label, frequency, shift, id, selected);
	}

	/**
	 * 
	 * @param ID
	 *            = -1 then use the default index loaded from the disk
	 *            ID,frequency, shift, selected
	 * @param featureString
	 */
	public OneFeatureMultiClass(int ID, String featureString) {
		String[] tokens = featureString.split(",");
		this.label = tokens[1];
		if (tokens.length > 2) {
			String[] smallTokens = tokens[2].split(" ");
			this.frequency = new int[smallTokens.length];
			for (int i = 0; i < frequency.length; i++)
				frequency[i] = Integer.parseInt(smallTokens[i]);
			smallTokens = tokens[3].split(" ");
			this.shift = new long[smallTokens.length];
			for (int i = 0; i < shift.length; i++) {
				shift[i] = Long.parseLong(smallTokens[i]);
			}
			if (tokens.length > 4)
				this.selected = Boolean.parseBoolean(tokens[4]);
			else
				this.selected = false;
		}
		if (ID == -1)
			this.id = Integer.parseInt(tokens[0]);
		else
			this.id = ID;
	}

	/**
	 * Construct a MultiClass Feature
	 * 
	 * @param label
	 * @param frequency
	 * @param shift
	 * @param id
	 * @param selected
	 */
	public OneFeatureMultiClass(String label, int[] frequency, long[] shift,
			int id, boolean selected) {
		this.label = label;
		this.frequency = frequency;
		this.shift = shift;
		this.id = id;
		this.selected = selected;
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
	public int getFeatureId() {
		return id;
	}

	@Override
	public String toFeatureString() {
		StringBuffer bbuf = new StringBuffer();
		bbuf.append(this.id);
		bbuf.append(",");
		bbuf.append(this.label);
		bbuf.append(",");
		// The Frequency
		bbuf.append(this.frequency[0]);
		for (int i = 1; i < this.frequency.length; i++) {
			bbuf.append(" ");
			bbuf.append(this.frequency[i]);
		}
		bbuf.append(",");
		// The shift
		bbuf.append(this.shift[0]);
		for (int i = 1; i < this.shift.length; i++) {
			bbuf.append(" ");
			bbuf.append(this.shift[i]);
		}
		bbuf.append(",");
		bbuf.append(this.selected);
		return bbuf.toString();
	}

	@Override
	public void setFeatureId(int newID) {
		this.id = newID;
	}

	@Override
	public void setFrequency(int frequency) {
		this.frequency = new int[1];
		this.frequency[0] = frequency;
	}

	@Override
	public int getFrequency() {
		return this.frequency[0];
	}

	@Override
	public void setPostingShift(long shift) {
		this.shift = new long[1];
		this.shift[0] = shift;
	}

	@Override
	public long getPostingShift() {
		return this.shift[0];
	}

	/*************** The Underlying Are Class Specific Functions *******************/

	public void saveSpace() {
		int numofClasses = 0;
		for (long oneShift : this.shift) {
			if (oneShift == -1)
				continue;
			else
				numofClasses++;
		}
		if (numofClasses == shift.length)
			return;
		else {
			long[] newShift = new long[numofClasses];
			int[] newFreq = new int[numofClasses];
			int index = 0;
			for (int i = 0; i < shift.length; i++) {
				if (shift[i] != -1) {
					newShift[index] = this.shift[i];
					newFreq[index] = this.frequency[i];
					index++;
				}
			}
			this.shift = newShift;
			this.frequency = newFreq;
		}
	}

	public void setPostingShift(long shift2, int w) {
		this.shift[w] = shift2;
	}

	public int[] getAllFrequency() {
		return frequency;
	}

	public long[] getAllPostingShift() {
		return shift;
	}

	public void setPostingShifts(long[] newShift) {
		this.shift = newShift;
	}

	public static class Factory extends FeatureFactory {
		public final static Factory instance = new Factory();

		@Override
		public OneFeatureMultiClass genOneFeature(int id, String featureString) {
			return new OneFeatureMultiClass(id, featureString);
		}

	}

	public OneFeatureImpl toFeatureImpl() {
		return new OneFeatureImpl(label, this.frequency[0], this.shift[0], id,
				selected);
	}
}
