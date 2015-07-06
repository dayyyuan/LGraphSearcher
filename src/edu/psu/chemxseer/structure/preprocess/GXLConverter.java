package edu.psu.chemxseer.structure.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.sourceforge.gxl.GXLAtomicValue;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLEdge;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLGraphElement;
import net.sourceforge.gxl.GXLNode;

import de.parmol.graph.Graph;
import de.parmol.graph.GraphFactory;
import de.parmol.graph.MutableGraph;

/**
 * This is an implementation of the GXL converter: Given a Graph data set in GXL
 * format, we converted them into canonical DFS code format Then this DFS code
 * can be converted into Parmol Graph if necessary
 * 
 * @author dayuyuan
 * 
 */
public class GXLConverter {

	public GXLConverter() {

	}

	public Graph parse(File inputfile, String gName, GraphFactory factory)
			throws Exception {
		if (!inputfile.exists())
			throw new IOException();
		else {
			GXLDocument gxlDoc = new GXLDocument(inputfile);
			// get the graph
			GXLGraph oneGraph = gxlDoc.getDocumentElement().getGraphAt(0);
			if (oneGraph.getEdgeMode().equals("directed")) {
				System.out.println("Only Support undirect edges");
				throw new Exception();
			}
			// String gID = oneGraph.getID();
			MutableGraph g = factory.createGraph(gName);
			HashMap<GXLGraphElement, Integer> allNodes = new HashMap<GXLGraphElement, Integer>();
			// int count = 0;
			for (int i = 0; i < oneGraph.getGraphElementCount(); i++) {
				GXLGraphElement oneElement = oneGraph.getGraphElementAt(i);
				if (oneElement.getClass().getName().contains("GXLNode")) {
					GXLAtomicValue xValue = (GXLAtomicValue) oneElement
							.getAttr("x").getValue();
					GXLAtomicValue yValue = (GXLAtomicValue) oneElement
							.getAttr("y").getValue();
					int x = Integer.parseInt(xValue.getValue());
					int y = Integer.parseInt(yValue.getValue());
					int label = ((x / 32) << 2) + (y / 32) + 1;
					int index = g.addNode(label); // Uniform node label 6
					allNodes.put(oneElement, index);
					// System.out.println(oneElement.getID() + ": " + index);
				} else if (oneElement.getClass().getName().contains("GXLEdge")) {
					// Get the node source and target
					GXLEdge theEdge = (GXLEdge) oneElement;
					GXLGraphElement node1 = theEdge.getTarget();
					GXLGraphElement node2 = theEdge.getSource();
					int nodexIndex1 = allNodes.get(node1);
					int nodexIndex2 = allNodes.get(node2);
					GXLAtomicValue valenceValue = (GXLAtomicValue) theEdge
							.getAttr("valence").getValue();
					g.addEdge(nodexIndex2, nodexIndex1,
							Integer.parseInt(valenceValue.getValue()));
				}
			}
			g.saveMemory();
			return g;
		}
	}

	public int test(File inputfile, GraphFactory factory, int[] crossEdgeNum)
			throws Exception {
		if (!inputfile.exists())
			throw new IOException();
		else {
			GXLDocument gxlDoc = new GXLDocument(inputfile);
			// get the graph
			GXLGraph oneGraph = gxlDoc.getDocumentElement().getGraphAt(0);
			if (oneGraph.getEdgeMode().equals("directed")) {
				System.out.println("Only Support undirect edges");
				throw new Exception();
			}
			HashMap<GXLNode, Integer> regionBelonging = new HashMap<GXLNode, Integer>();
			int crossRegionEdgeNum = 0;
			// int inRegionEdgeNum = 0;
			for (int i = 0; i < oneGraph.getGraphElementCount(); i++) {
				GXLGraphElement oneElement = oneGraph.getGraphElementAt(i);
				if (oneElement.getClass().getName().contains("GXLNode")) {
					GXLNode theNode = (GXLNode) oneElement;
					GXLAtomicValue xValue = (GXLAtomicValue) oneElement
							.getAttr("x").getValue();
					GXLAtomicValue yValue = (GXLAtomicValue) oneElement
							.getAttr("y").getValue();
					int x = Integer.parseInt(xValue.getValue());
					int y = Integer.parseInt(yValue.getValue());
					int region = ((x / 32) << 2) + (y / 32);
					// regionNodeNum[region] ++;
					regionBelonging.put(theNode, region);
				} else if (oneElement.getClass().getName().contains("GXLEdge")) {
					// Get the node source and target
					GXLEdge theEdge = (GXLEdge) oneElement;
					GXLNode node1 = (GXLNode) theEdge.getTarget();
					GXLNode node2 = (GXLNode) theEdge.getSource();
					// int regionNode1 = regionBelonging.get(node1);
					// int regionNode2 = regionBelonging.get(node2);
					GXLAtomicValue xValue1 = (GXLAtomicValue) node1
							.getAttr("x").getValue();
					GXLAtomicValue yValue1 = (GXLAtomicValue) node1
							.getAttr("y").getValue();
					GXLAtomicValue xValue2 = (GXLAtomicValue) node2
							.getAttr("x").getValue();
					GXLAtomicValue yValue2 = (GXLAtomicValue) node2
							.getAttr("y").getValue();
					int x1 = Integer.parseInt(xValue1.getValue());
					int y1 = Integer.parseInt(yValue1.getValue());
					int x2 = Integer.parseInt(xValue2.getValue());
					int y2 = Integer.parseInt(yValue2.getValue());
					int dist = (int) Math.sqrt((x1 - x2) * (x1 - x2)
							+ (y1 - y2) * (y1 - y2));
					int region = dist / 4;
					if (region >= 15)
						region = 15;
					crossEdgeNum[region]++;

				}
			}
			return crossRegionEdgeNum;
		}
	}

	public static void main(String[] args) throws Exception {
		File folder = new File("/Users/dayuyuan/Downloads/COIL-DEL/data");
		File[] allfiles = folder.listFiles();
		int[] regionNodeNum = new int[16];
		int crossRegionEdgeNum = 0;
		GXLConverter test = new GXLConverter();
		for (int i = 0; i < allfiles.length; i++) {
			if (allfiles[i].isHidden())
				continue;
			else if (!allfiles[i].getName().contains("gxl"))
				continue;
			else {
				crossRegionEdgeNum += test.test(allfiles[i],
						MyFactory.getGraphFactory(), regionNodeNum);
			}
		}
		for (int i = 0; i < 16; i++) {
			System.out.println((float) regionNodeNum[i] / (float) 7200);
		}
		System.out.println(crossRegionEdgeNum);
	}
}
