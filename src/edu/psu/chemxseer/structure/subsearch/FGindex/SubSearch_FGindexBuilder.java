package edu.psu.chemxseer.structure.subsearch.FGindex;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Impl.PostingFetcherLucene;
import edu.psu.chemxseer.structure.postings.Impl.VerifierISO;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureComparatorAdv;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

public class SubSearch_FGindexBuilder {

	public SubSearch_FGindex loadIndex(IGraphDatabase gDB, String baseName,
			GraphParser gSerializer, boolean lucene_in_mem) throws IOException {
		FGindexSearcher searcher = FGindexConstructor.loadSearcher(baseName,
				SubSearch_FGindex.getIn_MemoryIndexName(), gDB);
		PostingFetcherLucene in_memory_postings = new PostingFetcherLucene(
				baseName + SubSearch_FGindex.getLuceneName(),
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		FGindex fgIndex = new FGindex(searcher, in_memory_postings);

		PostingFetcherLucene on_disk_postings = new PostingFetcherLucene(
				baseName + SubSearch_FGindex.getOnDiskLuceneName(),
				gDB.getTotalNum(), gSerializer, lucene_in_mem);
		SubSearch_FGindex wholeIndex = new SubSearch_FGindex(fgIndex,
				new VerifierISO(), on_disk_postings, baseName);
		return wholeIndex;
	}

	/**
	 * Given the Raw features and gDB, build the Index (excluding the posting)
	 * And also build and store the on-Disk indices
	 * 
	 * @param candidateFeatures
	 * @param db
	 * @return
	 * @throws IOException
	 */
	public SubSearch_FGindex buildIndex(
			NoPostingFeatures<IOneFeature> candidateFeatures,
			GraphDatabase_OnDisk db, String baseName, GraphParser gSerializer)
			throws IOException {
		File onDiskFolder = new File(baseName,
				SubSearch_FGindex.getOnDiskFolderName());
		if (!onDiskFolder.exists())
			onDiskFolder.mkdirs();

		// First step: mine TCFG
		long time1 = System.currentTimeMillis();
		float delta = (float) 0.1;
		// float delta = (float) 0.8;
		this.mineTCFG(candidateFeatures, delta);

		List<IOneFeature> TCFGFeatures = candidateFeatures
				.getSelectedFeatures();
		candidateFeatures.saveFeatures(baseName + "StatusRecordedFeatures");

		long time2 = System.currentTimeMillis();
		System.out.println("# of TCFG Features:" + TCFGFeatures.size());
		System.out.println("1. mine TCFG features: " + (time2 - time1));
		System.out.println("Memory consumption in B"
				+ MemoryConsumptionCal.usedMemoryinMB());
		// Second step: Construct In-Memory IGI for TCFGs
		/*
		 * FGindexSearcher in_memoryIndex =
		 * FGindexConstructor.constructInMem(TCFGFeatures, db); long time3 =
		 * System.currentTimeMillis();
		 * System.out.println("2. Construct IGI for TCFGs: " + (time3-time2));
		 * 
		 * FGindexConstructor.saveSearcher(in_memoryIndex, baseName,
		 * SubgraphSearch_FGindex.getIn_MemoryIndexName());
		 * 
		 * time3 = System.currentTimeMillis(); String lucenePath = baseName +
		 * SubgraphSearch_FGindex.getLuceneName(); PostingBuilderLucene
		 * postingBuilder = new PostingBuilderLucene(new
		 * PostingBuilderLuceneVectorizerNormal(gSerializer, in_memoryIndex));
		 * postingBuilder.buildLuceneIndex(lucenePath,
		 * in_memoryIndex.getFeatureCount(), db, null); long time4=
		 * System.currentTimeMillis();
		 * System.out.println("3. Build the Postings for in_memory IGI: " +
		 * (time4-time3));
		 * 
		 * // Third step: Construct On-Disk IGI for other features
		 * HashMap<Integer, String> gHash = new HashMap<Integer, String>();
		 * List<IOneFeature> ondiskFeatures = this.buildOnDiskIndices(baseName,
		 * in_memoryIndex, candidateFeatures, TCFGFeatures.size(), gHash); long
		 * time5 = System.currentTimeMillis();
		 * System.out.println("# of onDiskFeatures: " + ondiskFeatures.size());
		 * System.out.println("4. Construct the OnDisk indexes: " +
		 * (time5-time4));
		 * 
		 * String onDiskLucenePath = baseName +
		 * SubgraphSearch_FGindex.getOnDiskLuceneName(); FGindexSearcher
		 * on_diskIndex = FGindexConstructor.constructOnDisk(ondiskFeatures);
		 * postingBuilder = new PostingBuilderLucene(new
		 * PostingBuilderLuceneVectorizerNormal(gSerializer, on_diskIndex));
		 * postingBuilder.buildLuceneIndex(onDiskLucenePath, db.getTotalNum(),
		 * db, gHash); long time6 = System.currentTimeMillis();
		 * System.out.println("5. Construct the Postings for On-Disk Index: " +
		 * (time6-time5));
		 * 
		 * VerifierISO verif = new VerifierISO(); FGindex index = new
		 * FGindex(in_memoryIndex, new PostingFetcherLucene(lucenePath,
		 * db.getTotalNum(), gSerializer, false)); SubgraphSearch_FGindex result
		 * = new SubgraphSearch_FGindex(index,verif,new
		 * PostingFetcherLucene(onDiskLucenePath, db.getTotalNum(), gSerializer,
		 * false), baseName);
		 * 
		 * return result;
		 */
		return null;
	}

	/**
	 * ONLY TCFG Are Chosen, unlike Lindex-TCFG which contains all distinct
	 * edges
	 * 
	 * @param candidateFeatures
	 * @param delta
	 * @throws ParseException
	 */
	public void mineTCFG(NoPostingFeatures<IOneFeature> candidateFeatures,
			float delta) {
		// First sort all candidateFeatures
		candidateFeatures.sortFeatures(new FeatureComparatorAdv());
		FastSU fastSu = new FastSU();
		try {
			candidateFeatures.createGraphs();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Then put those features into a layer array
		int maxLayer = candidateFeatures
				.getFeature(candidateFeatures.getfeatureNum() - 1)
				.getFeatureGraph().getEdgeCount();
		@SuppressWarnings("unchecked")
		ArrayList<IOneFeature>[] layerFeatures = new ArrayList[maxLayer];
		for (int i = 0; i < maxLayer; i++)
			layerFeatures[i] = new ArrayList<IOneFeature>();
		for (int i = 0; i < candidateFeatures.getfeatureNum(); i++) {
			IOneFeature aFeature = candidateFeatures.getFeature(i);
			int edgeNum = aFeature.getFeatureGraph().getEdgeCount();
			layerFeatures[edgeNum - 1].add(aFeature);
		}
		int TCFGNum = 0;

		// All layer 0 graphs [ graphs with edge count = 1] are selected
		ArrayList<IOneFeature> firstLayerFeature = layerFeatures[0];
		for (int i = 0; i < firstLayerFeature.size(); i++) {
			firstLayerFeature.get(i).setSelected();
			++TCFGNum;
		}
		ArrayList<IOneFeature> lastLayerFeature = layerFeatures[layerFeatures.length - 1];
		// All layer n graphs [graphs with edge count = maximum layer] are
		// selected
		for (int i = 0; i < lastLayerFeature.size(); i++) {
			lastLayerFeature.get(i).setSelected();
			++TCFGNum;
		}

		// Mine frequency tolerant closed subgraphs\
		for (int i = 1; i < (layerFeatures.length - 1); i++) {
			ArrayList<IOneFeature> childFeatures = layerFeatures[i + 1];
			for (int j = 0; j < layerFeatures[i].size(); j++) {
				Graph small = layerFeatures[i].get(j).getFeatureGraph();
				float threshold = (1 - delta)
						* layerFeatures[i].get(j).getFrequency();
				// childrenFeatures must not be empty nor null
				boolean TCFGstatus = true;
				for (int t = 0; t < childFeatures.size(); t++) {
					boolean iso = fastSu.isIsomorphic(small,
							childFeatures.get(t).getFeatureGraph());
					if (iso && childFeatures.get(t).getFrequency() >= threshold) {
						layerFeatures[i].get(j).setUnselected();
						TCFGstatus = false;
						break;
					}
				}
				if (TCFGstatus) {
					layerFeatures[i].get(j).setSelected();
					++TCFGNum;
				} else
					layerFeatures[i].get(j).setUnselected();
			}
		}
		System.out.println("After mining delta-TCFG: ");
		System.out.println("Total number of FGidnex features: "
				+ candidateFeatures.getfeatureNum());
		System.out.println("Number of TCGF among those features: " + TCFGNum);
	}

	/*
	 * private List<IOneFeature> buildOnDiskIndices(String baseName,
	 * FGindexSearcher in_memoryIndex, NoPostingFeatures candidateFeatures, int
	 * featureCount, HashMap<Integer, String> gHash) throws IOException{ int
	 * numOfTCFG = featureCount; ArrayList<IOneFeature>[] closureSet = new
	 * ArrayList[numOfTCFG]; ArrayList<IOneFeature> oneDiskFeatures = new
	 * ArrayList<IOneFeature>(); for(int i = 0; i< closureSet.length; i++)
	 * closureSet[i] = new ArrayList<IOneFeature>();
	 * 
	 * for(int i = 0; i< candidateFeatures.getfeatureNum(); i++){ IOneFeature
	 * theFeature = candidateFeatures.getFeature(i); if(theFeature.isSelected())
	 * continue; else{ theFeature.setFeatureId(oneDiskFeatures.size());
	 * oneDiskFeatures.add(theFeature); } long[] times = new long[4]; boolean[]
	 * exactMatch = new boolean[1]; int cTCFG =
	 * in_memoryIndex.designedSubgraph(theFeature.getFeatureGraph(),exactMatch,
	 * times); if(cTCFG == -1){ System.out.println("what's up");
	 * in_memoryIndex.designedSubgraph(theFeature.getFeatureGraph(),
	 * exactMatch,times); } closureSet[cTCFG].add(theFeature); } for(int i = 0;
	 * i< closureSet.length; i++){ if(closureSet[i].isEmpty()) continue; else {
	 * FGindexSearcher onDiskIGI =
	 * FGindexConstructor.constructOnDisk(closureSet[i]); for(int j = 0; j<
	 * closureSet[i].size(); j++) gHash.put(closureSet[i].get(j).getFeatureId(),
	 * i + "_" + j); FGindexConstructor.saveSearcher(onDiskIGI, baseName,
	 * SubSearch_FGindex.getOnDiskIndexName(i)); } } return oneDiskFeatures; }
	 */

}
