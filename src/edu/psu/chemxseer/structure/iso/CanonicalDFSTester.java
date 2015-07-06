//package edu.psu.chemxseer.structure.iso;
//
//import java.text.ParseException;
//
//import de.parmol.graph.Graph;
//import de.parmol.parsers.GraphParser;
//import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
//import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
//import edu.psu.chemxseer.structure.preprocess.MyFactory;
//
///**
// * Test the correctness of the new implementation
// * @author dayuyuan
// *
// */
//public class CanonicalDFSTester {
//	public static void main(String[] args) throws ParseException{
//		String dbFile = "/Users/dayuyuan/Documents/Experiment/SupSearch/DBFile";
//		IGraphDatabase gDB = new GraphDatabase_OnDisk(dbFile, MyFactory.getDFSCoder());
//		
//		GraphParser oriParser = new CanonicalDFS();
//		CanonicalDFSImpl newParser = new CanonicalDFSImpl();
//		
//		//1. First Test the Correctness of the input & outpu
//		FastSU su = new FastSU();
////		for(Graph g : gDB){
////			String serilized = newParser.serializeNonCanonical(g);
////			Graph newG = newParser.parse(serilized, MyFactory.getGraphFactory());
////			if(g.getEdgeCount() == newG.getEdgeCount() 
////					&& g.getNodeCount() == newG.getNodeCount() && su.isIsomorphic(newG, g)){
////				continue;
////			}
////			else {
////				int gNode = g.getNodeCount();
////				int gEdge = g.getEdgeCount();
////				int newGNode = newG.getNodeCount();
////				int ngewGEdge = newG.getEdgeCount();
////				System.out.println("This is so wrong");
////			}
////		}
//		//2. Then Test the Correctness of the canonical labeling
//		for(int i = 0; i< gDB.getTotalNum(); i++){
//			String ori = gDB.findGraphString(i);
//			Graph g = gDB.findGraph(i);
//			String serilized = newParser.serialize(g);
//			Graph newG = newParser.parse(serilized, MyFactory.getGraphFactory());
//			if(g.getEdgeCount() == newG.getEdgeCount() 
//					&& g.getNodeCount() == newG.getNodeCount() && su.isIsomorphic(newG, g)){
//				continue;
//			}
//			else {
//				int gNode = g.getNodeCount();
//				int gEdge = g.getEdgeCount();
//				int newGNode = newG.getNodeCount();
//				int ngewGEdge = newG.getEdgeCount();
//				System.out.println("This is so wrong");
//			}
//		}
//		//3. Test Wether the new label equals to old label
//		for(Graph g : gDB){
//			String newLabel = newParser.serialize(g);
//			String oldLabel = oriParser.serialize(g);
//			if(newLabel.equals(oldLabel))
//				continue;
//			else {
//				System.out.println("This is So Wrong");
//			}
//		}
//	}
// }
