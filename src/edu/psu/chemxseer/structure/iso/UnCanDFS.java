package edu.psu.chemxseer.structure.iso;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

public class UnCanDFS implements GraphParser {
	private CanonicalDFS realDFS;

	public UnCanDFS() {
		this.realDFS = MyFactory.getDFSCoder();
	}

	public UnCanDFS(CanonicalDFS realDFS) {
		this.realDFS = realDFS;
	}

	@Override
	public Graph parse(String text, GraphFactory factory) throws ParseException {
		return realDFS.parse(text, factory);
	}

	@Override
	public String serialize(Graph g) {
		return realDFS.serializeNonCanonical(g);
	}

	@Override
	public void serialize(Graph[] graphs, OutputStream out) throws IOException {
		BufferedOutputStream bout = new BufferedOutputStream(out);
		for (int i = 0; i < graphs.length; i++) {
			bout.write(graphs[i].getName().getBytes());
			bout.write(" => ".getBytes());
			bout.write(serialize(graphs[i]).getBytes());
			bout.write("\n".getBytes());
		}
		bout.flush();
	}

	@Override
	public Graph[] parse(InputStream in, GraphFactory factory)
			throws IOException, ParseException {
		return realDFS.parse(in, factory);
	}

	@Override
	public int getDesiredGraphFactoryProperties() {
		return realDFS.getDesiredGraphFactoryProperties();
	}

	@Override
	public String getNodeLabel(int nodeLabel) {
		return realDFS.getNodeLabel(nodeLabel);
	}

	@Override
	public boolean directed() {
		return realDFS.directed();
	}

	public Object parse(String dfsCode, String string, GraphFactory graphFactory) {
		return realDFS.parse(dfsCode, string, graphFactory);
	}

}
