package edu.psu.chemxseer.structure.newISO.newFastSU;

/**
 * A Object Recording the mappings from a subgraph pattern to the query graph
 * Map[bigGraphNode] = smallgraphNode or -1 if not mapped.
 * 
 * @author dayuyuan
 * 
 */
public class SubgraphMapping {
	private int subgraphID;
	private int[][] maps;
	// The number of nodes that are in the partial mapping
	private int nodeCount;

	public SubgraphMapping(int id, int[][] maps) {
		this.maps = maps;
		this.subgraphID = id;
		this.nodeCount = 0;
		if (maps != null && maps.length > 0 && maps[0] != null)
			for (int i = 0; i < maps[0].length; i++) {
				if (maps[0][i] != -1)
					nodeCount++;
			}
	}

	public int getSubgraphID() {
		return subgraphID;
	}

	public void setSubgraphID(int subgraphID) {
		this.subgraphID = subgraphID;
	}

	public int[][] getMaps() {
		return maps;
	}

	public void setMaps(int[][] maps) {
		this.maps = maps;
	}

	public int getMappedNodeCount() {
		return nodeCount;
	}

}
