package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

/**
 * This is a implementation of Features_Ext This class is mainly design for the
 * Lindex The major add-on features of this class over the normal
 * NoPostingFeatures classes is the saving of parents/children relationships
 * 
 * @author dayuyuan
 * 
 */
public class NoPostingFeatures_Ext<T extends IOneFeature> extends
		NoPostingFeatures<T> {
	// Features Extension
	private OneFeatureExt[] featuresExt;
	protected boolean subsuperRelationExist;

	/**
	 * Given normal Features: construct a "super-sub" graph relationship
	 * preserved FeaturesExt
	 * 
	 * @param newNormalFeatures
	 */
	public NoPostingFeatures_Ext(NoPostingFeatures<T> normalFeatures) {
		super(normalFeatures);
		subsuperRelationExist = false;
		// New Features
		this.featuresExt = new OneFeatureExt[normalFeatures.getfeatureNum()];
		for (int i = 0; i < normalFeatures.getfeatureNum(); i++)
			this.featuresExt[i] = new OneFeatureExt(
					normalFeatures.getFeature(i));
	}

	public static NoPostingFeatures_Ext<IOneFeature> getGeneralType(
			NoPostingFeatures_Ext<? extends IOneFeature> template) {
		return new NoPostingFeatures_Ext<IOneFeature>(template);
	}

	private NoPostingFeatures_Ext(
			NoPostingFeatures_Ext<? extends IOneFeature> template) {
		super(template.features, template.featureFileName,
				template.graphAvailabel);
		this.featuresExt = template.featuresExt;
		this.subsuperRelationExist = template.subsuperRelationExist;
	}

	/**
	 * Reorder the features, features ID does not change
	 * 
	 * @return
	 * @throws ParseException
	 */
	public boolean mineSubSuperRelation() throws ParseException {
		// First sort the candidateFeatures
		this.createGraphs();
		this.sortFeatures(new FeatureComparatorAdv());

		HashSet<OneFeatureExt> offsprings = new HashSet<OneFeatureExt>();
		FastSU fastSu = new FastSU();
		for (int i = featuresExt.length - 1; i >= 0; i--) {
			// Initialize offsprings, children
			offsprings.clear();
			for (int j = i + 1; j < featuresExt.length; j++) {
				if (offsprings.contains(featuresExt[j]))
					continue;
				boolean isSub = fastSu.isIsomorphic(
						featuresExt[i].getFeatureGraph(),
						featuresExt[j].getFeatureGraph());
				if (isSub) {
					featuresExt[i].addChild(featuresExt[j]);
					featuresExt[j].addParent(featuresExt[i]);
					// add j and all j's children into offsprings of i
					offsprings.add(featuresExt[j]);
					addOffspring(offsprings, featuresExt[j]);
				} else
					continue;
			}
		}
		return true;
	}

	/**
	 * Reorder the features according to the orders of the featuresExt Reorder
	 * the ID
	 */
	@SuppressWarnings("unchecked")
	private void reorderUnderlyingFeatures() {
		for (int i = 0; i < this.featuresExt.length; i++) {
			features[i] = featuresExt[i].getOriFeature();
			features[i].setFeatureId(i);
		}
	}

	/**
	 * Add all terms that descent to term into offsprings
	 * 
	 * @param offsprings
	 * @param term
	 */
	private void addOffspring(Collection<OneFeatureExt> offsprings,
			OneFeatureExt term) {
		Queue<OneFeatureExt> queue = new LinkedList<OneFeatureExt>();
		queue.offer(term);
		while (!queue.isEmpty()) {
			OneFeatureExt oneFeature = queue.poll();
			List<OneFeatureExt> children = oneFeature.getChildren();
			if (children == null || children.size() == 0)
				continue;
			else {
				for (int i = 0; i < children.size(); i++) {
					if (offsprings.contains(children.get(i)))
						continue;
					else {
						offsprings.add(children.get(i));
						queue.offer(children.get(i));
					}
				}
			}
		}
	}

	public boolean existSubSuperRelation() {
		if (this.subsuperRelationExist)
			return true;
		else
			return false;
	}

	public boolean clearSubSuperRelation() {
		for (int i = 0; i < this.featuresExt.length; i++) {
			featuresExt[i].removeChildren();
			featuresExt[i].removeParents();
		}
		this.subsuperRelationExist = false;
		return true;
	}

	public void setAllUnvisited() {
		for (OneFeatureExt oneFeature : this.featuresExt)
			oneFeature.setUnvisited();
	}

	public void setAllVisited() {
		for (OneFeatureExt oneFeature : this.featuresExt)
			oneFeature.setVisited();
	}

	@Override
	public boolean sortFeatures(Comparator<IOneFeature> comparator) {
		Arrays.sort(this.featuresExt, comparator);
		reorderUnderlyingFeatures();
		return true;
	}

	public OneFeatureExt getFeatureExt(int featureIndex) {
		return this.featuresExt[featureIndex];
	}
}
