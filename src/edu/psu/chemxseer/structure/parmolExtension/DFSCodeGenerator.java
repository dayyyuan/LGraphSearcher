package edu.psu.chemxseer.structure.parmolExtension;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import de.parmol.GSpan.DFSCode;
import de.parmol.GSpan.DataBase;
import de.parmol.GSpan.GSpanEdge;
import de.parmol.graph.Graph;
import de.parmol.util.FragmentSet;
import edu.psu.chemxseer.structure.preprocess.MyFactory;

/**
 * This implementation is obligated, and this is wrong, not correct.
 * 
 * @author dayuyuan
 * 
 */
public class DFSCodeGenerator {
	private int maxEdgeNum;
	private FragmentSet m_frequentSubgraphs;
	private Queue<DFSCode> allInitialCodes;
	private DFSCode currentCode;
	private Stack<Iterator<DFSCode>> allCodesit;
	int itLevel;

	public DFSCodeGenerator(Graph g, int maxEdgeNum) {
		this.maxEdgeNum = maxEdgeNum;
		this.m_frequentSubgraphs = new FragmentSet();
		List<Graph> m_graphs = new LinkedList<Graph>();
		m_graphs.add(g);
		float[] dayuFrequency = new float[1];
		dayuFrequency[0] = (float) 1.0;
		DataBase gs = new DataBase(m_graphs, dayuFrequency,
				m_frequentSubgraphs, MyFactory.getGraphFactory());
		this.allInitialCodes = new LinkedList<DFSCode>();
		for (Iterator eit = gs.frequentEdges(); eit.hasNext();) {
			GSpanEdge edge = (GSpanEdge) eit.next();
			DFSCode code = new DFSCode(edge, gs); // create DFSCode for the
			allInitialCodes.offer(code);
		}

		this.allCodesit = new Stack<Iterator<DFSCode>>();
		this.itLevel = -1;
		this.currentCode = null;
	}

	public String nextCode() {
		if (currentCode == null && itLevel == -1) {
			if (allInitialCodes.isEmpty())
				return null;
			else {
				this.currentCode = allInitialCodes.poll();
				return nextCode();
			}
		} else {
			DFSCode theCode = this.currentCode;
			boolean needtoGrow = true;
			String label = theCode.toString(MyFactory.getDFSCoder());
			Graph graph = theCode.getSubgraph();
			if (graph.getEdgeCount() == this.maxEdgeNum)
				needtoGrow = false;

			boolean minCode = theCode.isMin();
			boolean canGrow = true;

			if (needtoGrow && minCode) {
				Iterator<DFSCode> childIt = theCode.childIterator(false, false);
				// Need to Grow
				if (childIt.hasNext()) {
					this.currentCode = childIt.next();
					allCodesit.push(childIt);
					itLevel++;
				} else
					canGrow = false;
			}

			if (!needtoGrow || !minCode || !canGrow) {
				while (!this.allCodesit.isEmpty()) {
					Iterator<DFSCode> sameLevelIt = this.allCodesit.pop();
					if (sameLevelIt.hasNext()) {
						this.currentCode = sameLevelIt.next();
						String testString = this.currentCode.toString(MyFactory
								.getDFSCoder());
						allCodesit.push(sameLevelIt);
						break;
					}
					itLevel--;
				}
				if (itLevel == -1) {
					if (theCode != this.currentCode)
						System.out.println("Impossible");
					else
						this.currentCode = null;
				}

			}
			if (minCode)
				return label;
			else
				return nextCode();
		}
	}

	public Graph nextGraph() {
		if (currentCode == null && itLevel == -1) {
			if (allInitialCodes.isEmpty())
				return null;
			else {
				this.currentCode = allInitialCodes.poll();
				return nextGraph();
			}
		} else {
			DFSCode theCode = this.currentCode;
			boolean needtoGrow = true;
			Graph graph = theCode.getSubgraph();
			if (graph.getEdgeCount() == this.maxEdgeNum)
				needtoGrow = false;

			boolean minCode = theCode.isMin();
			boolean canGrow = true;

			if (needtoGrow && minCode) {
				Iterator<DFSCode> childIt = theCode.childIterator(false, false);
				// Need to Grow
				if (childIt.hasNext()) {
					this.currentCode = childIt.next();
					allCodesit.push(childIt);
					itLevel++;
				} else
					canGrow = false;
			}

			if (!needtoGrow || !minCode || !canGrow) {
				while (!this.allCodesit.isEmpty()) {
					Iterator<DFSCode> sameLevelIt = this.allCodesit.pop();
					if (sameLevelIt.hasNext()) {
						this.currentCode = sameLevelIt.next();
						allCodesit.push(sameLevelIt);
						break;
					}
					itLevel--;
				}
				if (itLevel == -1) {
					if (theCode != this.currentCode)
						System.out.println("Impossible");
					else
						this.currentCode = null;
				}

			}
			if (minCode)
				return graph;
			else
				return nextGraph();
		}
	}

	public void earlyPruning() {
		DFSCode theCode = this.currentCode;
		while (!this.allCodesit.isEmpty()) {
			Iterator<DFSCode> sameLevelIt = this.allCodesit.pop();
			if (sameLevelIt.hasNext()) {
				this.currentCode = sameLevelIt.next();
				allCodesit.push(sameLevelIt);
				break;
			}
			itLevel--;
		}
		if (itLevel == -1) {
			if (theCode != this.currentCode)
				System.out.println("Impossible");
			else
				this.currentCode = null;
		}
	}

	public static void main(String args[]) throws ParseException {
		// String gString =
		// "C-C-S-C2-N(-C(-C1=N-C(-[F])(=N-C(-[F])(=N-1)))(=C3-C=2(-C=C-C=C-3)))(-C-C-C)";
		// Graph g = MyFactory.getSmilesParser().parse(gString,
		// MyFactory.getGraphFactory());
		// DFSCodeGenerator gen2 = new DFSCodeGenerator(g, 3);
		// int count = 0;
		// while(true){
		// String label = gen2.nextCode();
		// if(label == null)
		// break;
		// if(label.equals("<0 1 6 1 7>")){
		// System.out.println(label);
		// Iterator<DFSCode> childIt = this..childIterator(false, false);
		// }
		// count ++;
		// }
	}
}
