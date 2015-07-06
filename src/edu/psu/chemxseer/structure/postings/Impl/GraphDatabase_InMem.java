package edu.psu.chemxseer.structure.postings.Impl;

import java.util.Arrays;
import java.util.Collection;

import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;

public class GraphDatabase_InMem extends GraphDatabase_Basic implements
		IGraphDatabase {
	private String[] graphStrings;

	public GraphDatabase_InMem(String[] gString, GraphParser gParser) {
		super(gParser);
		this.graphStrings = gString;
	}

	public GraphDatabase_InMem(Collection<String> gString, GraphParser gParser) {
		super(gParser);
		graphStrings = new String[gString.size()];
		graphStrings = gString.toArray(graphStrings);
	}

	public GraphDatabase_InMem(NoPostingFeatures<IOneFeature> features) {
		super(MyFactory.getDFSCoder());
		this.graphStrings = new String[features.getfeatureNum()];
		for (int i = 0; i < graphStrings.length; i++)
			graphStrings[i] = features.getFeature(i).getDFSCode();
	}

	public GraphDatabase_InMem(GraphDatabase_InMem gDB2) {
		super(gDB2.gParser);
		this.graphStrings = Arrays.copyOf(gDB2.graphStrings,
				gDB2.graphStrings.length);
	}

	public GraphDatabase_InMem(GraphDatabase_OnDisk gDB2) {
		super(gDB2.getParser());
		this.graphStrings = new String[gDB2.getTotalNum()];
		for (int i = 0; i < graphStrings.length; i++)
			graphStrings[i] = gDB2.findGraphString(i);
	}

	@Override
	public String findGraphString(int id) {
		if (id < 0 || id >= graphStrings.length) {
			System.out.println("Error: Illligal graphID");
			return null;
		} else
			return this.graphStrings[id];
	}

	@Override
	public int getTotalNum() {
		return this.graphStrings.length;
	}

	@Override
	public void setGString(int gID, String graphString2) {
		this.graphStrings[gID] = graphString2;
	}

}
