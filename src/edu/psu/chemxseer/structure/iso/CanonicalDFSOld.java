package edu.psu.chemxseer.structure.iso;

//package edu.psu.chemxseer.structure.iso;
//import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.text.ParseException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Stack;
//
//import de.parmol.graph.Graph;
//import de.parmol.graph.GraphFactory;
//import de.parmol.graph.MutableGraph;
//import de.parmol.parsers.GraphParser;
//import de.parmol.parsers.SLNParser;
//
///**
// * This class is used to parse a DFS code into a Graph
// * And also serialize a graph into canonical DFS code
// * This canonical DFS code is use as a internal representation
// * of Graphs in disk-based graph representation
// * DFSCODE: <i j l_i l_ij l_j>, <i j l_i l_ij l_j>
// * @author Dayu Yuan
// *
// */
//public class CanonicalDFS implements GraphParser{
//	private int[][] vertices;
//	private int[][] connectivity;// connectivity[i][i] = label(i), connectivity[i][j] = edge(i,j)
//	private int[] edgeLabel;
//	private int[] nodeVisited;
//	private boolean[] edgeVisited;
//	private int[] parent;
//	private int[][] sequence;
//	private int[][] minSequence;
//	private Stack<Integer> branches;
//	private int minSequenceValidIndex;
//	private int pos;
//	private int depth;
//	public static int NOTYET = -1;
//	public static int HEAD = -100;
//
//
//	public boolean directed() {
//		return false;
//	}
//	public int getDesiredGraphFactoryProperties() {
//		return GraphFactory.UNDIRECTED_GRAPH;
//	}
//	public String getNodeLabel(int nodeLabel) {
//		return SLNParser.ATOM_SYMBOLS[nodeLabel];
//	}
//	public Graph[] parse(InputStream in, GraphFactory factory)
//			throws IOException, ParseException {
//		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
//
//		LinkedList<Graph> graphs = new LinkedList<Graph>();
//		String line;
//		while ((line = bin.readLine()) != null) {
//			int pos = line.indexOf(" => ");
//
//			graphs.add(parse(line.substring(pos + " => ".length()), line.substring(0, pos), factory));
//		}
//
//		return (Graph[]) graphs.toArray(new Graph[graphs.size()]);
//	}
//	public void serialize(Graph[] graphs, OutputStream out) throws IOException {
//		BufferedOutputStream bout = new BufferedOutputStream(out);
//		for (int i = 0; i < graphs.length; i++) {
//			bout.write(graphs[i].getName().getBytes());
//			bout.write(" => ".getBytes());
//			bout.write(serialize(graphs[i]).getBytes());
//			bout.write("\n".getBytes());
//		}
//		bout.flush();
//	}
//
//	public Graph parse(String text, GraphFactory factory){
//		return parse(text, null, factory);
//	}
//
//	public Graph parse(int[][] ext, GraphFactory factory){
//		return parse(ext, null, factory);
//	}
//
//	public Graph parse(String text, String id, GraphFactory factory){
//		readText(text);
//		HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
//		MutableGraph g = factory.createGraph(id);
//		return this.parser(g, nodeMap);
//	}
//
//	public Graph parse(int[][] text, String id, GraphFactory factory){
//		if(text == null || text.length ==0)
//			return null;
//		this.sequence = text;
//		HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
//		MutableGraph g = factory.createGraph(id);
//		return this.parser(g, nodeMap);
//	}
//
//	public Graph parse(MutableGraph prefGraph, String suffix, GraphFactory factory) {
//		//assume that the prefix graph prefGraph's node order is coherent witht he suffix string
//		this.readText(suffix);
//		HashMap<Integer, Integer> nodeMap = new HashMap<Integer, Integer>();
//		for(int i = 0; i< prefGraph.getNodeCount(); i++)
//			nodeMap.put(i, i);
//		return this.parser(prefGraph, nodeMap);
//	}
//
//	private Graph parser(MutableGraph g, Map<Integer, Integer> nodeMap){	
//		// Add the first edge or first Node Edge
//		if(sequence[0][0]!=-1){
//			int nodeID = g.addNode(sequence[0][2]);
//			nodeMap.put(sequence[0][0], nodeID);
//			if(sequence[0][1]!=-1){
//				nodeID = g.addNodeAndEdge(sequence[0][0], sequence[0][4], sequence[0][3]);
//				nodeMap.put(sequence[0][1], nodeID);
//			}
//		}
//		else{
//			if(sequence[0][1]!=-1){
//				int nodeID = g.addNode(sequence[0][4]);
//				nodeMap.put(sequence[0][1], nodeID);
//			}
//		}
//		// Dealing with the first entry
//		if(this.sequence.length == 1)
//			return g;
//
//		for(int i = 1; i< this.sequence.length; i++){
//			Integer node1ID = nodeMap.get(sequence[i][0]);
//			Integer node2ID = nodeMap.get(sequence[i][1]);
//			if(node1ID!=null && node2ID == null){
//				int nodeID = g.addNodeAndEdge(node1ID, sequence[i][4], sequence[i][3]);
//				nodeMap.put(sequence[i][1], nodeID);
//			}
//			else if(node1ID == null && node2ID !=null){
//				int nodeID = g.addNodeAndEdge(node2ID, sequence[i][2], sequence[i][3]);
//				nodeMap.put(sequence[i][0], nodeID);
//			}
//			else if(node1ID !=null && node2ID!=null)
//				g.addEdge(node1ID, node2ID, sequence[i][3]);
//			else System.out.println("Error in Canoinical DFS parser: redundant edge" );
//		}
//		g.saveMemory();
//		return g;
//	}
//
//	/**
//	 * A easy way but memory intensive of reading text 
//	 * @param text
//	 * @return
//	 */
//	private boolean readText(String text){
//		String[] entries = text.split("><");
//		this.sequence = new int[entries.length][5];
//		// The first and last entry need to be dealt specially
//		entries[0] = entries[0].substring(1);
//		entries[entries.length-1] = entries[entries.length-1].substring(0, entries[entries.length-1].length()-1);
//		String[] temp;
//		for(int i = 0; i< entries.length; i++){
//			temp = entries[i].split(" ");
//			for(int j = 0; j< temp.length; j++)
//				this.sequence[i][j] = Integer.parseInt(temp[j]);
//		}
//		return true;
//	}
//
//	/**
//	 * Return the array or null if no such array
//	 * @param text
//	 * @return
//	 */
//	public int[][] parseTextToArray(String text){
//		String[] entries = text.split("><");
//		if(entries == null || entries.length == 0 || entries[0].length() == 0)
//			return new int[0][];
//		int[][] sequence = new int[entries.length][5];
//		// The first and last entry need to be dealt specially
//		entries[0] = entries[0].substring(1);
//		entries[entries.length-1] = entries[entries.length-1].substring(0, entries[entries.length-1].length()-1);
//		String[] temp;
//		for(int i = 0; i< entries.length; i++){
//			temp = entries[i].split(" ");
//			for(int j = 0; j< temp.length; j++)
//				sequence[i][j] = Integer.parseInt(temp[j]);
//		}
//		return sequence;
//	}
//
//	public String writeArrayToText(int[][] array){
//		StringBuffer buf = new StringBuffer(1024);
//		if(array == null)
//			System.out.println("aya");
//		for(int i = 0; i< array.length; i++){
//			buf.append('<');
//			buf.append(array[i][0]);
//			buf.append(' ');
//			buf.append(array[i][1]);
//			buf.append(' ');
//			buf.append(array[i][2]);
//			buf.append(' ');
//			buf.append(array[i][3]);
//			buf.append(' ');
//			buf.append(array[i][4]);
//			buf.append('>');
//		}
//		return buf.toString();
//	}
//	//	
//	//	/**
//	//	 * A memory saving way of reading text
//	//	 * @param text
//	//	 * @return
//	//	 */
//	//	private boolean readTextFast(String text){
//	//		char[] charText = text.toCharArray();
//	//		int count = 0;
//	//		for(int i = 0; i< charText.length; i++)
//	//			if(charText[i]=='<')
//	//				count++;
//	//		this.sequence = new int[count][5];
//	//		// Initialize
//	//		for(int i = 0 ; i< this.sequence.length; i++)
//	//			for(int j = 0; j< 5; j++)
//	//				this.sequence[i][j]=0;
//	//		// Start reading
//	//		for(int i = 0, index = 0; i< count; i++)
//	//		{
//	//			// skip the first '<'
//	//			index++;
//	//			for(int j = 0; j< 5; j++, index++)
//	//				while(charText[index]!=','&& charText[index]!='>'){
//	//					this.sequence[i][j]=10*this.sequence[i][j]+charText[index];
//	//				}
//	//		}
//	//		return true;
//	//	}
//
//
//	/******************Serialization *******************************/
//	public String serializeNonCanonical(Graph g){
//		int[][] sequence = serializeNonCanonicalToArray(g);
//		return this.writeArrayToText(sequence);
//	}
//	public int[][] serializeNonCanonicalToArray(Graph g){
//		int nodeCount = g.getNodeCount();
//		int edgeCount = g.getEdgeCount();
//		int[][] graphConnectivity = new int[nodeCount][nodeCount];
//		for(int i = 0; i< nodeCount; i++)
//			for(int j = 0; j< nodeCount; j++)
//				graphConnectivity[i][j] = -1;
//		int[] edgelabels = new int[edgeCount];
//
//		for(int i = 0; i <nodeCount; i++)
//			graphConnectivity[i][i] = g.getNodeLabel(i);
//		for(int i = 0; i< edgeCount; i++){
//			edgelabels[i] = g.getEdgeLabel(i);
//			int nodeA = g.getNodeA(i);
//			int nodeB = g.getNodeB(i);
//			graphConnectivity[nodeA][nodeB] = graphConnectivity[nodeB][nodeA] = i;
//		}
//		return this.serializeNonCanonicalToArray(graphConnectivity, edgelabels);
//	}
//
//	public String serializeNonCanonical(int[][] graphConnectivity, int[] graphEdgeLabel){
//		int[][] sequence = serializeNonCanonicalToArray(graphConnectivity, graphEdgeLabel);
//		return writeArrayToText(sequence);
//	}
//
//	private int[][] serializeNonCanonicalToArray(int[][] graphConnectivity, int[] graphEdgeLabel){
//		boolean exception = this.dealCornerCase(graphConnectivity, graphEdgeLabel);
//		if(exception == false){
//			return this.minSequence;
//		}
//
//		int[][] sequence = new int[graphEdgeLabel.length][5];
//		int count = 0;
//		for(int i = 0; i< graphConnectivity.length; i++){
//			for(int j = i+1; j < graphConnectivity.length; j++)
//			{
//				if(graphConnectivity[i][j]!=-1){
//					sequence[count][0] = i;
//					sequence[count][1] = j;
//					sequence[count][2] = graphConnectivity[i][i];
//					sequence[count][3] = graphEdgeLabel[graphConnectivity[i][j]];
//					sequence[count][4] = graphConnectivity[j][j];
//					count++;
//				}
//			}
//		}
//		return sequence;
//	}
//	/**
//	 * Given a graph g (chemical molecule) Generate its canonical labels
//	 * @param g
//	 * @return
//	 */
//	public String serialize(Graph g){
//		serializeToArray(g);
//		return writeArrayToText(this.minSequence);
//	}
//
//	/**
//	 * Given a graph g (chemical molecule) Generate its canonical labels
//	 * @param g
//	 * @return
//	 */
//	public int[][] serializeToArray(Graph g){
//		boolean exception = this.dealCornerCase(g);
//		if(exception ==false)
//			return this.minSequence;
//		else{
//			initialize(g);
//			findSerialization();
//			return minSequence;
//		}
//	}	
//	/**
//	 * Given a graph g (chemical molecule), in the format of connectivity
//	 * and edgeLabel, Generate its canonical labels
//	 * @param graphConnectivity
//	 * @param graphEdgeLabel
//	 * @return
//	 */
//	public String serialize(int[][] graphConnectivity, int[] graphEdgelabel){
//		serializeToArray(graphConnectivity, graphEdgelabel);
//		return this.writeArrayToText(this.minSequence);
//	}
//
//	private int[][] serializeToArray(int[][] graphConnectivity, int[] graphEdgeLabel){
//		boolean exception = this.dealCornerCase(graphConnectivity, graphEdgeLabel);
//		if(exception == false)
//			return this.minSequence;
//		else{
//			initialize(graphConnectivity, graphEdgeLabel);
//			findSerialization();
//			return this.minSequence;
//		}
//	}
//
//	/**
//	 * Given the DFSCode (not necessary canonical), serialize it as a string
//	 * @param graphDFSCode
//	 * @return
//	 */
//	public String serialize(int[][] graphDFSCode){
//		return this.writeArrayToText(graphDFSCode);
//	}
//
//	private boolean dealCornerCase(Graph g){
//		// Dealing with the situation when g has no edge
//		if(g==null){
//			this.minSequence = null;
//			return false;
//		}
//		else if(g.getNodeCount() == 0){
//			this.minSequence = null;
//			return false;
//		}
//		else if(g.getEdgeCount() == 0)// No edge At all
//		{
//			if(g.getNodeCount() >1)
//			{
//				System.out.println("YDY: Exception in serialize of CanonicalDFS: not connected");
//				return false;
//			}
//			this.minSequence = new int[1][5];
//			minSequence[0][0] = 0;
//			minSequence[0][1] = -1;
//			minSequence[0][2] = g.getNodeLabel(0);
//			minSequence[0][3] = -1;
//			minSequence[0][4] = -1;
//			return false;
//		}
//		return true;
//	}
//
//	private boolean dealCornerCase(int[][] graphConnectivity, int[] graphEdgeLabel){
//		// Dealing with the situation when g has no edge
//		if(graphConnectivity==null||graphEdgeLabel == null)
//			return false;
//		else if(graphConnectivity.length == 0)
//			return false;
//		else if(graphEdgeLabel.length == 0)// No edge At all
//		{
//			if(graphConnectivity.length >1)
//			{
//				System.out.println("YDY: Exception in serialize of CanonicalDFS: not connected");
//				return false;
//			}
//			this.minSequence = new int[1][5];
//			minSequence[0][0] = 0;
//			minSequence[0][1] = -1;
//			minSequence[0][2] = graphConnectivity[0][0];
//			minSequence[0][3] = -1;
//			minSequence[0][4] = -1;
//			return false;
//		}
//		return true;
//	}
//
//	private boolean findSerialization(){
//		int[][] startEdges = findMinimumEdge();
//		// Assign the first entry of sequence and minSequence
//		fillDFSEntry(startEdges[0][0], 0, startEdges[0][1], 1);
//		pos++;
//		DFSEntryCopy(minSequence[0],sequence[0]);
//		minSequenceValidIndex = 0;
//
//		for(int i = 0; i< startEdges.length; i++){
//			initializeTree();
//			pos=1;
//			depth = 0;
//			// update status record
//			nodeVisited[startEdges[i][0]]=depth;// depth 0
//			nodeVisited[startEdges[i][1]]=++depth;// depth 1
//			parent[startEdges[i][0]]=HEAD;// No parent for first visited node
//			parent[startEdges[i][1]]=startEdges[i][0];
//			edgeVisited[connectivity[startEdges[i][0]][startEdges[i][1]]]=true;
//			branches.push(startEdges[i][0]);
//
//			// Keep on chaining edges starting from the second node
//			List<Integer> useLess = new LinkedList<Integer>();
//			boolean outcome = depthFirstSearch(startEdges[i][1],useLess);
//		}
//		return true;
//	}
//
//	/**
//	 * Fill DFS entry of this.sequence
//	 * @param iNode
//	 * @param iNodeDepth
//	 * @param jNode
//	 * @param jNodeDepth
//	 */
//	private void fillDFSEntry(int iNode, int iNodeDepth, int jNode, int jNodeDepth){
//		this.sequence[pos][0]=iNodeDepth;
//		this.sequence[pos][1]=jNodeDepth;
//		this.sequence[pos][2]=connectivity[iNode][iNode];
//		this.sequence[pos][3]=edgeLabel[connectivity[iNode][jNode]];
//		this.sequence[pos][4] =connectivity[jNode][jNode];
//	}
//
//	/**
//	 * Fill DFS entry of this.sequence
//	 * @param iNode
//	 * @param jNode
//	 * @param depthJNode
//	 */
//	private void fillDFSEntry(int iNode, int jNode, int depthJNode){
//		this.sequence[pos][0]=nodeVisited[iNode];
//		this.sequence[pos][1]=depthJNode;
//		this.sequence[pos][2]=connectivity[iNode][iNode];
//		this.sequence[pos][3]=edgeLabel[connectivity[iNode][jNode]];
//		this.sequence[pos][4] =connectivity[jNode][jNode];
//	}
//	/**
//	 * Initialize graph into internal structure, to minimize access time
//	 * 1. initialize vertices, connectivity
//	 * 2. create space for nodeDepth, edgeDiscovery, parent, branches without initialization
//	 * 3. create space for minsequence, sequence without initialization
//	 * @param g
//	 * @return
//	 */
//	private boolean initialize(Graph g){
//		int nodeCount = g.getNodeCount();
//		// Initial internal representation of graph small and graph big
//		this.vertices = new int[nodeCount][];
//		this.connectivity = new int[nodeCount][nodeCount];
//		this.edgeLabel = new int[g.getEdgeCount()];
//
//		for(int nodeI = 0; nodeI < nodeCount; nodeI++){
//			vertices[nodeI] = new int[g.getDegree(nodeI)];
//
//			for(int temp  = 0; temp < nodeCount; temp++){
//				this.connectivity[nodeI][temp] = -1;
//			}
//			this.connectivity[nodeI][nodeI]=g.getNodeLabel(nodeI);
//
//			for(int j = 0; j< vertices[nodeI].length;j++){
//				int edge = g.getNodeEdge(nodeI, j);
//				int nodeJ = g.getOtherNode(edge, nodeI);
//				connectivity[nodeI][nodeJ]=edge;
//				edgeLabel[edge] = g.getEdgeLabel(edge);
//				vertices[nodeI][j]=nodeJ;
//			}
//		}
//		// Initialization of nodeDiscovery, edgeDiscovery, parent and pos
//		// is postpone to initializeTree()
//		this.nodeVisited = new int[this.vertices.length];
//		this.edgeVisited = new boolean[g.getEdgeCount()];
//		this.parent = new int[this.vertices.length];
//
//		this.sequence = new int[g.getEdgeCount()][5];
//		this.minSequence = new int[g.getEdgeCount()][5];
//		if(this.branches == null)
//			this.branches = new Stack<Integer>();
//		else this.branches.clear();
//		this.pos = 0;
//		this.minSequenceValidIndex = -1;
//		this.depth = -1;
//		return true;
//	}
//
//	private boolean initialize(int[][] graphConnectivity, int[] edgeLabel){
//		// Initial internal representation of graph small and graph big
//		this.vertices = new int[graphConnectivity.length][];
//		this.connectivity = graphConnectivity;
//		this.edgeLabel = edgeLabel;
//		// Initialize this.vertices
//		for(int nodeI = 0; nodeI < graphConnectivity.length; nodeI++){
//			int degree = 0;
//			for(int nodeJ = 0; nodeJ < graphConnectivity.length; nodeJ++)
//				if(connectivity[nodeI][nodeJ]!=-1&& nodeI!=nodeJ)
//					degree++;
//			vertices[nodeI] = new int[degree];
//			for(int nodeJ = 0, j = 0; nodeJ < graphConnectivity.length; nodeJ++)
//				if(connectivity[nodeI][nodeJ]!=-1&& nodeI!=nodeJ){
//					vertices[nodeI][j] = nodeJ;
//					++j;
//				}
//		}
//		// Initialization of nodeDiscovery, edgeDiscovery, parent and pos
//		// is postpone to initializeTree()
//		this.nodeVisited = new int[this.vertices.length];
//		this.edgeVisited = new boolean[edgeLabel.length];
//		this.parent = new int[this.vertices.length];
//
//		this.sequence = new int[edgeLabel.length][5];
//		this.minSequence = new int[edgeLabel.length][5];
//		if(this.branches == null)
//			this.branches = new Stack<Integer>();
//		else this.branches.clear();
//		this.pos = 0;
//		this.minSequenceValidIndex = -1;
//		this.depth = -1;
//		return true;
//	}
//
//	/**
//	 * Initialize parent, nodeDepth, edgeDiscovery arrays
//	 * @return
//	 */
//	private boolean initializeTree(){
//
//		for(int i = 0; i< parent.length; i++){
//			this.nodeVisited[i]=NOTYET;
//			this.parent[i]=NOTYET;
//		}
//		for(int i = 0; i< edgeVisited.length; i++){
//			this.edgeVisited[i]=false;
//		}
//		return true;
//	}
//	/**
//	 * Generate DFS code entry according to DFS code specification
//	 * @param startNode
//	 * @param growedBackEdgeRes: the backEdges that has been added in this function: output
//	 * @return
//	 */
//	private boolean depthFirstSearch(int startNode, List<Integer> growedBackEdgeRes){
//		List<Integer> backWardEdgeDest = new LinkedList<Integer>();
//		List<Integer> nextForwardNode = new LinkedList<Integer>();
//
//		findNextNodes(startNode, backWardEdgeDest, nextForwardNode);
//		growedBackEdgeRes.clear();
//		// Must have to add backward edge starting from startNode first
//		for(Iterator<Integer> it = backWardEdgeDest.iterator(); it.hasNext(); ){
//			int theOtherNode = it.next();
//			growedBackEdgeRes.add(theOtherNode);
//			// depth of startNode and theOtherNode is already known
//			fillDFSEntry(startNode, theOtherNode,nodeVisited[theOtherNode]);
//			// Delay pos++, now this.sequence is valid up to pos
//			boolean growIsGoodIdea = false;
//			int compare2 = -100;
//			if(this.minSequenceValidIndex < pos-1)
//				System.out.println("YDY: Error in depthFristSearch of CanonicalDFS: index not consistent");
//			if(this.minSequenceValidIndex == pos-1)
//				growIsGoodIdea = true;// since minSequence is not valid till pos, minSequenceValidIndex should be pos-1
//			// both minSequence and sequence is valid on entry pos, and entry of sequence is smaller
//			else if((compare2=DFSEntryCompare(this.sequence[pos], this.minSequence[pos]))!=-1)
//				growIsGoodIdea = true;
//			if(growIsGoodIdea)
//			{
//				edgeVisited[connectivity[startNode][theOtherNode]]=true;
//				// Backward Edge, no need to update nodeDepth
//				DFSEntryCopy(this.minSequence[pos],this.sequence[pos]);
//				if(this.minSequenceValidIndex == pos-1||compare2 == 1)
//					this.minSequenceValidIndex = pos;
//				// if compare ==0, there is no need of updating minSequenceValidIndex
//				//minSequenceValidIndex = pos;
//				pos++;
//			}
//			else 
//				return false;// early termination of depth first search
//		}
//
//		// Then add forward edge starting from startNode
//		boolean canFurtherGrow = false;
//		if(nextForwardNode.isEmpty()){
//			if(depth== vertices.length-1){
//				return true;
//			}
//			else if(this.branches.isEmpty())
//				System.out.println("Error in depthFirstSearch of CanonicalDFS: no vertices to add in: " + 
//						this.depth);
//			else {
//				List<Integer> branchPopBackDest = new LinkedList<Integer>();
//				return depthFirstSearch(branches.pop(), branchPopBackDest);
//			}
//		}
//		int i = 0;
//		Stack<Integer> branchesRecord = (Stack<Integer>) this.branches.clone();
//		int minSequenceValidIndexRecord = this.minSequenceValidIndex;
//		int posRecord = pos;	
//		int depthRecord = depth;		
//		for(Iterator <Integer> it = nextForwardNode.iterator(); it.hasNext();i++){
//			int theOtherNode = it.next();
//			int edge = connectivity[startNode][theOtherNode];	
//			fillDFSEntry(startNode, theOtherNode, depth+1);
//			int compare = -100;
//			boolean growIsGoodIdea = false;
//			List<Integer> theOtherBackDest = new LinkedList<Integer>();
//			if(this.minSequenceValidIndex < pos-1)
//				System.out.println("YDY: Error in depthFristSearch of CanonicalDFS: index not consistent forward"
//						+ this.minSequenceValidIndex + " " + pos);
//			if(this.minSequenceValidIndex == pos-1)
//				growIsGoodIdea = true;
//			else if((compare=DFSEntryCompare(this.sequence[pos], this.minSequence[pos]))!=-1)
//				growIsGoodIdea = true;
//			if(growIsGoodIdea){
//				nodeVisited[theOtherNode]=++depth;
//				edgeVisited[edge]=true;
//				parent[theOtherNode]=startNode;
//				DFSEntryCopy(this.minSequence[pos],this.sequence[pos]);
//				if(this.minSequenceValidIndex == pos-1||compare == 1)
//					this.minSequenceValidIndex = pos;
//				// if compare ==0, there is no need of updating minSequenceValidIndex
//				pos++;
//				// keep on growing
//				this.branches.push(startNode);
//				boolean success = depthFirstSearch(theOtherNode,theOtherBackDest);
//				if(success)
//					canFurtherGrow = success;
//			}
//			else continue;
//			// back up any changes that we have done
//			this.branches.clear();
//			branches.addAll(branchesRecord);
//			nodeVisited[theOtherNode]=-1;
//			edgeVisited[edge] = false;
//			parent[theOtherNode]=-1;
//			pos = posRecord;
//			depth = depthRecord;
//			if(!canFurtherGrow)
//				this.minSequenceValidIndex = minSequenceValidIndexRecord;
//			// back up any changes that we have done on "the OtherNode"'s back edge
//			// Must have to add backward edge starting from startNode first
//			for(Iterator<Integer> iit = theOtherBackDest.iterator(); iit.hasNext(); ){
//				int theOtherNodeBackDest = iit.next();
//				edgeVisited[connectivity[theOtherNode][theOtherNodeBackDest]]=false;
//			}
//		}
//		return canFurtherGrow;
//	}
//
//	private void DFSEntryCopy(int[] is, int[] is2) {
//		for(int i = 0; i< is.length; i++)
//			is[i]=is2[i];
//
//	}
//
//	/**
//	 * Given a Graph g, find it's minimum edge
//	 * That is edge with minimum node label and edge label
//	 * e(a,b) < e(c, d) iff
//	 * la < lb ||
//	 * la = lb, lab < lcd
//	 * la = lb, lab = lcd, lc < ld
//	 * @param g
//	 * @return int[i][0]= minNodeA int[i][1]= minNodeB
//	 */
//	private int[][] findMinimumEdge(){
//		int minEdgeLabel = Integer.MAX_VALUE;
//		int minNodeALabel = Integer.MAX_VALUE;
//		int minNodeBLabel = Integer.MAX_VALUE;
//		int[][] edges = new int[2*edgeVisited.length][2];
//		int index = 0;
//		// Each edge is visited twice, with different firstnnode and secondnode order
//		for(int nodeI = 0; nodeI< vertices.length; nodeI++)
//			for(int jIndex = 0; jIndex< vertices[nodeI].length; jIndex++){
//				int nodeJ = vertices[nodeI][jIndex];
//				boolean change = false;
//				if(connectivity[nodeI][nodeI]< minNodeALabel){
//					change = true;// minimum Edge has minimum nodeI label
//				}
//				else if(connectivity[nodeI][nodeI]== minNodeALabel){
//					if(edgeLabel[connectivity[nodeI][nodeJ]]< minEdgeLabel)// minimum Edge has minimum edge label
//						change=true;
//					else if(edgeLabel[connectivity[nodeI][nodeJ]]== minEdgeLabel){
//						if(connectivity[nodeJ][nodeJ]< minNodeBLabel){
//							change = true; // minimum Edge has minimum nodeJ label
//						}
//						else if(connectivity[nodeJ][nodeJ]== minNodeBLabel){
//							edges[index][0]=nodeI;
//							edges[index][1]=nodeJ;
//							index++; // Add one minimum Edge
//						}
//					}
//				}
//				if(change){
//					index = 0;  // Replace previous found minimum edge with newly found one
//					edges[index][0]=nodeI;
//					edges[index][1]=nodeJ;
//					minEdgeLabel = edgeLabel[connectivity[nodeI][nodeJ]];
//					minNodeALabel =connectivity[nodeI][nodeI];
//					minNodeBLabel = connectivity[nodeJ][nodeJ];
//					index++;
//				}
//			}
//		int[][] results = new int[index][2];
//		for(int i = 0; i< index; i++)
//			results[i]=edges[i];
//		return results;
//	}
//	/**
//	 * Given a startingNode, find the set of backWardEdge according to increasing order
//	 * Also find the set of nextNodes, which are minimum forward edge dest 
//	 * @param g
//	 * @param startNode
//	 * @return
//	 */
//	private boolean findNextNodes(int startPoint, List<Integer> backWardEdgeDest, List<Integer> nextNodes){
//		// clear what ever in backWardEdgeDest and NextNodes
//		backWardEdgeDest.clear();
//		nextNodes.clear();
//		int minimumEdgeLabel = Integer.MAX_VALUE;
//		int minimumNodeLabel = Integer.MAX_VALUE;
//		for(int i = 0; i< vertices[startPoint].length; i++){
//			int anotherNode = vertices[startPoint][i];
//			int edge = connectivity[startPoint][anotherNode];
//			if(edgeVisited[edge]==true)
//				continue; // this edge has already been used
//
//			if(this.nodeVisited[anotherNode] != NOTYET)// backWardEdge
//				backWardEdgeDest.add(anotherNode);
//			else{
//				// forwardEdge
//				if(edgeLabel[edge]< minimumEdgeLabel){
//					minimumEdgeLabel = this.edgeLabel[connectivity[startPoint][anotherNode]];
//					minimumNodeLabel = connectivity[anotherNode][anotherNode];
//					nextNodes.clear();
//					nextNodes.add(anotherNode);
//				}
//				else if(edgeLabel[edge]== minimumEdgeLabel){
//					if(connectivity[anotherNode][anotherNode]< minimumNodeLabel){
//						minimumNodeLabel = connectivity[anotherNode][anotherNode];
//						nextNodes.clear(); 
//						nextNodes.add(anotherNode);
//					}
//					else if(connectivity[anotherNode][anotherNode]== minimumNodeLabel)
//						nextNodes.add(anotherNode);
//					else continue;
//				}
//				else continue;
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * Given two DFS entries, compare them according to their lexical order
//	 *1. Backward edges < Forward edges
//	 *2. For backward edge (m,n) < (m,p) iff depth(n) < depth(p)
//	 *2B.For backward edge (m,n) < (m,p) & depth(n) == depth(p) iff label(e1) < label(e2)
//	 *3. For forward edge(m,n) < (q,p) iff
//	 *   (A)depth(m) > depth(q), 
//	 *   (B)label(m) < label(q), 
//	 *   (C)label(e1) < label(e2),
//	 *   (D)label(n) < label(p) 
//	 * @param firstEntry
//	 * @param secondEntry
//	 * @return true 1 firstEntry  < secondEntry, 0 firstEntry == secondEntry
//	 * 		   false -1 firstEntry > secondEntry
//	 */
//	private int DFSEntryCompare(int[] firstEntry, int[] secondEntry){
//		if(firstEntry[0]>firstEntry[1]){
//			// firstEntry, back
//			if(secondEntry[0] <  secondEntry[1])
//				// secondEntry, forward
//				return 1; // Rule 1
//			else // Two backward edges
//				if(firstEntry[1]<secondEntry[1])
//					return 1; // Rule 2A
//				else if(firstEntry[1]==secondEntry[1]){
//					if(firstEntry[3]<secondEntry[3])
//						return 1;// Rule 2B
//					else if(firstEntry[3]>secondEntry[3])
//						return -1;// Rule 2B
//					else return 0;// Rule 2B
//				}
//				else return -1;// Rule 2A
//		}
//		else{
//			// firstEntry, forward
//			if(secondEntry[0]< secondEntry[1]){
//				// secondEntry, forward
//				if(firstEntry[0]>secondEntry[0])
//					return 1;// Rule 3A
//				else if(firstEntry[0]==secondEntry[0]){
//					if(firstEntry[2]< secondEntry[2])
//						return 1;// Rule 3B
//					else if(firstEntry[2]== secondEntry[2]){
//						if(firstEntry[3] < secondEntry[3])
//							return 1;// Rule 3C
//						else if(firstEntry[3] == secondEntry[3]){
//							if(firstEntry[4]< secondEntry[4])
//								return 1;// Rule 3D
//							else if(firstEntry[4]== secondEntry[4])
//								return 0;// Rule 3D
//							else return -1;// Rule 3D
//						}
//						else return -1;// Rule 3D
//					}
//					else return -1;// Rule 3B
//				}
//				else return -1;// Rule 3A
//			}
//			else // secondEntry, backward
//				return -1;// Rule 1;
//		}
//	}
//
//
// }
