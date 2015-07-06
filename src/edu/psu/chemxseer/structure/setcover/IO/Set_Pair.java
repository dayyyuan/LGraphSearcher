package edu.psu.chemxseer.structure.setcover.IO;

import java.io.Serializable;
import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.Item_Group;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;

/**
 * A set implemented as a set of pairs
 * 
 * The set implementation can be just as a collection of "IItems", however it
 * may take too much space, therefore, I implement set with primitive data
 * members And convert them to the object when sequentially read.
 * 
 * The interface of the reading a set will be changed to primitive types latter
 * 
 * @author dayuyuan
 * 
 */
public class Set_Pair extends AbstractISet implements ISet, Serializable {
	private static final long serialVersionUID = -3230906475124729725L;
	private int size; // lazy initialization
	// To simplify the serialization, we do not serialize the converter, the
	// converter need to be set manually.
	// This will increase the change of the error that Set_Pair can cause, but
	// it seems I have no other options.
	transient public IFeatureConverter converter;

	public Set_Pair(IFeatureWrapper feature, int setID,
			IFeatureConverter converter) {
		super(setID, false, feature);
		this.feature = feature;
		this.converter = converter;
		this.size = -1;
	}

	/**
	 * This is not a good strategy, I need to figure this out later. However for
	 * now,
	 * 
	 * @param converter
	 */
	public void setFeatureConverter(IFeatureConverter converter) {
		this.converter = converter;
	}

	@Override
	public int size() {
		if (size == -1) {
			size = converter.getIterator(this).size();
		}
		return size;
	}

	@Override
	public Iterator<int[]> iterator() {
		if (converter == null)
			System.out
					.println("No converter to use, the converter is not set, this may be caused by Deserialization");
		return converter.getIterator(this);
	}

	public Iterator<Item_Group> getItemGroupIterator() {
		return this.converter.getItemGroupIterator(this);
	}

	/**
	 * Return the ordered ValueQ
	 * 
	 * @return
	 */
	public int[] getValueQ() {
		// return
		// this.converter.getQMapInOrder(this.feature.containedQueryGraphs());
		return this.feature.containedQueryGraphs();
	}

	/**
	 * Return the ordered ValueG
	 * 
	 * @return
	 */
	public int[] getValueG() {
		return this.converter.getGMapInOrder(this.feature
				.containedDatabaseGraphs());
	}

	/**
	 * Return the ordered ValueQEqual
	 * 
	 * @return
	 */
	public int[] getValueQEqual() {
		// return
		// this.converter.getQMapInOrder(this.feature.getEquavalentQueryGraphs());
		return this.feature.getEquavalentQueryGraphs();
	}

	/**
	 * REturn the ordered ValueGEqual
	 * 
	 * @return
	 */
	public int[] getValueGEqual() {
		return this.converter.getGMapInOrder(this.feature
				.getEquavalentDatabaseGraphs());
	}

	/**
	 * Return the ordered ValueNoG
	 * 
	 * @return
	 */
	public int[] getValueNoG() {
		return this.converter.getGMapInOrder(this.feature
				.notContainedDatabaseGraphs(converter.getGCount()));
	}

	/**
	 * Return the ordered ValueNoQ
	 * 
	 * @return
	 */
	public int[] getValueNoQ() {
		// return
		// this.converter.getQMapInOrder(this.feature.notContainedQueryGraphs(converter.getQCount()));
		return this.feature.notContainedQueryGraphs(converter.getQCount());
	}

	public IFeatureConverter getFeatureConverter() {
		return this.converter;
	}
}
