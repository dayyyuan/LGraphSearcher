package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.util.LinkedList;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * A implementation of OneFeatureAdvance
 * 
 * @author dayuyuan
 * 
 */
public class OneFeatureExt implements IOneFeature {

	private IOneFeature oneFeature;
	private LinkedList<OneFeatureExt> parents;
	private LinkedList<OneFeatureExt> children;
	private boolean visited;

	public OneFeatureExt(IOneFeature feature) {
		this.oneFeature = feature;
		this.parents = this.children = null;
		this.visited = false;
	}

	public boolean addParent(OneFeatureExt parent) {
		if (parents == null)
			parents = new LinkedList<OneFeatureExt>();
		if (!parents.contains(parent)) {
			parents.add(parent);
			return true;
		} else
			return false;
	}

	public boolean removeParent(OneFeatureExt parent) {
		return this.parents.remove(parent);
	}

	public boolean addChild(OneFeatureExt child) {
		if (children == null)
			children = new LinkedList<OneFeatureExt>();
		if (!children.contains(child)) {
			children.add(child);
			return true;
		} else
			return false;
	}

	public boolean removeChild(OneFeatureExt child) {
		return this.children.remove(child);
	}

	public void setVisited() {
		visited = true;
	}

	public void setUnvisited() {
		visited = false;
	}

	public boolean isVisited() {
		return visited;
	}

	public List<OneFeatureExt> getChildren() {
		return this.children;
	}

	public List<OneFeatureExt> getParents() {
		return this.parents;
	}

	public void removeChildren() {
		if (this.children != null)
			this.children.clear();
	}

	public void removeParents() {
		if (this.parents != null)
			this.parents.clear();
	}

	public IOneFeature getOriFeature() {
		return this.oneFeature;
	}

	@Override
	public boolean isSelected() {
		return this.oneFeature.isSelected();
	}

	@Override
	public void setSelected() {
		this.oneFeature.setSelected();
	}

	@Override
	public void setUnselected() {
		this.oneFeature.setUnselected();
	}

	@Override
	public Graph getFeatureGraph() {
		return this.oneFeature.getFeatureGraph();
	}

	@Override
	public void creatFeatureGraph(int gID) {
		this.oneFeature.creatFeatureGraph(gID);
	}

	@Override
	public String getDFSCode() {
		return this.oneFeature.getDFSCode();
	}

	@Override
	public int getFrequency() {
		return this.oneFeature.getFrequency();
	}

	@Override
	public void setFrequency(int frequency) {
		this.oneFeature.setFrequency(frequency);
	}

	@Override
	public long getPostingShift() {
		return this.oneFeature.getPostingShift();
	}

	@Override
	public void setPostingShift(long shift) {
		this.oneFeature.setPostingShift(shift);
	}

	@Override
	public int getFeatureId() {
		return this.oneFeature.getFeatureId();
	}

	@Override
	public void setFeatureId(int id) {
		this.oneFeature.setFeatureId(id);
	}

	@Override
	public String toFeatureString() {
		return this.oneFeature.toFeatureString();
	}
}
