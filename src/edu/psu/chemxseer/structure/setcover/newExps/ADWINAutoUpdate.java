package edu.psu.chemxseer.structure.setcover.newExps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.parmol.graph.Graph;

import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_InMem;
import edu.psu.chemxseer.structure.postings.Impl.GraphDatabase_OnDisk;
import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.postings.Interface.IGraphResult;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.RandomChoseDBGraph;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.AppType;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.StatusType;
import edu.psu.chemxseer.structure.setcover.newExps.ADWINTracer.MonitorType;
import edu.psu.chemxseer.structure.setcover.update.IndexUpdator;
import edu.psu.chemxseer.structure.setcover.update.SubSearch_LindexSimpleUpdatable;
import edu.psu.chemxseer.structure.setcover.update.SubSearch_LindexSimpleUpdatableBuilder;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.FeatureFactory.FeatureFactoryType;
import edu.psu.chemxseer.structure.subsearch.Interfaces.IOneFeature;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * Experiment on Stream Mining: [1] Model that the query comes in a stream:
 * query graphs comes one after another, then do several topics change of the
 * query.The algorithm should detect the change automatically. [2] Model the
 * database graphs comes in a stream: this is already tested in Exp210
 * 
 * @author dayuyuan
 * 
 */
public class ADWINAutoUpdate {

	public static void main(String[] args) throws IOException, ParseException {
		// Run the Test Experiment
		if (args == null || args.length == 0)
			return;
		else {
			BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
			int[] segments = new int[] { 8 };
			for (int seg : segments) {
				// 1. Run the QueryStream (0.003, 0.05)
				// String queryBaseName =
				// "/data/home/duy113/VLDBSetCover2/QueryStream_0.003and0.05/"+seg
				// + "/";
				String queryBaseName = "/data/home/duy113/VLDBSetCover2/QueryStream_0.003and0.05More/"
						+ seg + "/";
				File file = new File(queryBaseName);
				if (!file.exists())
					file.mkdirs();
				String qName1 = queryBaseName + "queryOne";
				String qName2 = queryBaseName + "queryTwo";
				String queryName = queryBaseName + "TrainQuery";
				String qNameRaw2 = "/data/home/duy113/VLDBSetCover/AIDS_10K/0.05/FGindex/patterns";
				String qNameRaw1 = "/data/home/duy113/VLDB13/AIDS/subSearch_0.05/Query_Together_0.03_raw";
				sampleQueryLogs(qNameRaw1, qName1, qNameRaw2, qName2, 80000);
				blendingQueries(qName1, qName2, seg, queryName);
				difSupportQuery(writer, queryBaseName);
				streamNoUpdate(writer, queryBaseName);
			}

		}
	}

	public static void difSupportQuery(BufferedWriter writer, String queryBase)
			throws IOException {
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(
				"/data/home/duy113/VLDBSetCover2/AIDS_10K_0.003Q/DataAIDS",
				MyFactory.getSmilesParser());
		GraphDatabase_OnDisk query = new GraphDatabase_OnDisk(queryBase
				+ "TrainQuery", MyFactory.getDFSCoder());
		int queryCapacity = 50000;
		int K = 2000;
		double minSupport = 0.02;
		int batchCount = 100;
		System.out.println("stream99");
		writer.write("stream99 \n");
		String indexBase = "/data/home/duy113/VLDBSetCover2/AIDS_10K_0.003Q/"
				+ minSupport + "/" + K + "/stream99/";
		SubSearch_LindexSimpleUpdatableBuilder builder = new SubSearch_LindexSimpleUpdatableBuilder();
		// 1. Update 1
		SubSearch_LindexSimpleUpdatable lindexUpdatable = builder.loadIndexSub(
				gDB, indexBase, queryCapacity);
		writer.write("run index with update 2000K 0.02MInSupport, Stream99, -1 Threshold, Monitor Candidates \n");
		batchUpdateWithADWIN(lindexUpdatable, -1, query, minSupport, 0.99,
				0.02, MonitorType.candidates, batchCount, writer);
		writer.flush();
		// 2. Update 2
		/*
		 * lindexUpdatable = builder.loadIndexSub(gDB, indexBase,
		 * queryCapacity); writer.write(
		 * "run index with update 2000K 0.02MInSupport, Stream99, 100 Threshold, Monitor Candidates \n"
		 * ); batchUpdateWithADWIN(lindexUpdatable,query, 0.01, minSupport,
		 * 0.99, 0.01, MonitorType.candidates, batchCount, writer);
		 * writer.flush(); //3. Update 3 lindexUpdatable =
		 * builder.loadIndexSub(gDB, indexBase, queryCapacity); writer.write(
		 * "run index with update 2000K 0.02MInSupport, Stream99, -1 Threshold, Monitor FP \n"
		 * ); batchUpdateWithADWIN(lindexUpdatable,query, -1, minSupport, 0.99,
		 * 0.01, MonitorType.fp, batchCount, writer); writer.flush(); //4.
		 * Update 4 lindexUpdatable = builder.loadIndexSub(gDB, indexBase,
		 * queryCapacity); writer.write(
		 * "run index with update 2000K 0.02MInSupport, Stream99, 100 Threshold, Monitor FP \n"
		 * ); batchUpdateWithADWIN(lindexUpdatable,query, 0.01, minSupport,
		 * 0.99, 0.01, MonitorType.fp, batchCount, writer); writer.flush();
		 */
	}

	public static void batchUpdateWithADWIN(
			SubSearch_LindexSimpleUpdatable lindexUpdatable, int topGCount,
			IGraphDatabase query, double minSupport, double lambda,
			double adwinConfidence, MonitorType type, int batchCount,
			BufferedWriter writer) throws IOException {
		int start = 0, end = batchCount;
		ADWINTracer tracer = new ADWINTracer(adwinConfidence, type);
		boolean needUpdate = false;
		while (end < query.getTotalNum()) {
			float[] status = runBatchQuery(query, lindexUpdatable, start, end);
			tracer.insertValue(status);
			String str = Util.stateToString(status);
			writer.write(str.substring(0, str.length() - 1));
			if (tracer.needToUpdate()) {
				System.out.println(start);
				lindexUpdatable.shrinkQueryWindow(tracer.shrinkWindowSize()
						* batchCount);
				writer.write(tracer.getLength() * batchCount + "\n");
				needUpdate = true;
			}
			if (needUpdate && lindexUpdatable.sufficientDataToUpdate()) {
				// construct the update for index update
				IndexUpdator updator = new IndexUpdator(lindexUpdatable,
						topGCount);
				float[] beforeUpdateStat = updator.initializeUpdate(
						AppType.subSearch, StatusType.decomposePreSelectAdv,
						minSupport, lambda);
				float[] updateStat = updator.doUpdate();
				str = Util.stateToString(Util.joinArray(beforeUpdateStat,
						updateStat, new float[0]));
				writer.write(str.substring(0, str.length() - 1));
				System.out.println(updator.getFeatureCount());
				lindexUpdatable.clearQueryLogs();
				needUpdate = false;
			}
			writer.write("\n");
			writer.flush();
			start = end;
			end += batchCount;
			if (end > query.getTotalNum())
				end = query.getTotalNum();
		}
	}

	public static void streamNoUpdate(BufferedWriter writer, String queryBase)
			throws IOException {
		// 2. Load the index
		GraphDatabase_OnDisk gDB = new GraphDatabase_OnDisk(
				"/data/home/duy113/VLDBSetCover2/AIDS_10K_0.003Q/DataAIDS",
				MyFactory.getSmilesParser());
		GraphDatabase_OnDisk query = new GraphDatabase_OnDisk(queryBase
				+ "TrainQuery", MyFactory.getDFSCoder());
		int queryCapacity = 50000;
		int K = 2000;
		double minSupport = 0.02;
		int batchCount = 100;
		System.out.println("stream99");
		writer.write("stream99 \n");
		String indexBase = "/data/home/duy113/VLDBSetCover2/AIDS_10K_0.003Q/"
				+ minSupport + "/" + K + "/stream99/";
		SubSearch_LindexSimpleUpdatableBuilder builder = new SubSearch_LindexSimpleUpdatableBuilder();
		// 1. Update 1
		SubSearch_LindexSimpleUpdatable lindexUpdatable = builder.loadIndexSub(
				gDB, indexBase, queryCapacity);
		// 3. Run the queries one after another
		int start = 0, end = batchCount;
		while (end < query.getTotalNum()) {
			float[] status = runBatchQuery(query, lindexUpdatable, start, end);
			writer.write(Util.stateToString(status));
			writer.flush();
			start = end;
			end += batchCount;
			if (end > query.getTotalNum())
				end = query.getTotalNum();
		}
	}

	public static float[] runBatchQuery(IGraphDatabase query,
			SubSearch_LindexSimpleUpdatable searcher, int start, int end) {
		// Run the queries
		long[] TimeComponent = new long[4];
		float[] Number = new float[3];
		long[] TimeComponent1 = new long[4];
		int[] Number1 = new int[2];
		TimeComponent[0] = TimeComponent[1] = TimeComponent[2] = TimeComponent[3] = 0;
		Number[0] = Number[1] = Number[2] = 0;
		double memoryConsumption = 0;

		for (int i = start; i < end; i++) {
			TimeComponent1[0] = TimeComponent1[1] = TimeComponent1[2] = TimeComponent1[3] = 0;
			Number1[0] = Number1[1];
			Graph q = query.findGraph(i);
			List<IGraphResult> answers = searcher.getAnswer(q, TimeComponent1,
					Number1, true);

			if (i == 0)
				memoryConsumption = MemoryConsumptionCal.usedMemoryinMB();
			else {
				double ratio = 1 / (double) (i + 1);
				memoryConsumption = memoryConsumption * ratio * i
						+ MemoryConsumptionCal.usedMemoryinMB() * ratio;
			}
			if (answers.size() == 0)
				continue;
			TimeComponent[0] += TimeComponent1[0];
			TimeComponent[1] += TimeComponent1[1];
			TimeComponent[2] += TimeComponent1[2];
			TimeComponent[3] += TimeComponent1[3];
			Number[0] += Number1[0];
			Number[1] += Number1[1];
			Number[2] += (float) (Number1[0]) / (float) (Number1[1]);
		}
		System.out.println("Query Processing Result: ");
		System.out.print(TimeComponent[0] + "\t" + TimeComponent[1] + "\t"
				+ TimeComponent[2] + "\t" + TimeComponent[3] + "\t");
		System.out.println(Number[0] + "\t" + Number[1] + "\t" + Number[2]
				+ "\t" + query.getTotalNum());
		System.out.println("Average Memory Consumption: "
				+ (long) memoryConsumption);
		float[] stat = new float[7];
		stat[0] = TimeComponent[0];
		stat[1] = TimeComponent[1];
		stat[2] = TimeComponent[2];
		stat[3] = TimeComponent[3];
		stat[4] = Number[0];
		stat[5] = Number[1];
		stat[6] = end - start;
		return stat;
	}

	public static void sampleQueryLogs(String qNameRaw1, String qName1,
			String qNameRaw2, String qName2, int sampleCount) {

		IGraphDatabase qOne = new GraphDatabase_InMem(
				new NoPostingFeatures<IOneFeature>(qNameRaw1, MyFactory
						.getFeatureFactory(FeatureFactoryType.OneFeature)));
		IGraphDatabase qTwo = new GraphDatabase_InMem(
				new NoPostingFeatures<IOneFeature>(qNameRaw2, MyFactory
						.getFeatureFactory(FeatureFactoryType.OneFeature)));
		try {
			RandomChoseDBGraph.randomlyChooseDBGraph(qOne, sampleCount, qName1);
			RandomChoseDBGraph.randomlyChooseDBGraph(qTwo, sampleCount, qName2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void blendingQueries(String qName1, String qName2,
			int segments, String resultName) throws IOException {
		GraphDatabase_OnDisk qOne = new GraphDatabase_OnDisk(qName1,
				MyFactory.getDFSCoder());
		GraphDatabase_OnDisk qTwo = new GraphDatabase_OnDisk(qName2,
				MyFactory.getDFSCoder());
		String spliter = " => ";
		int graphID = 0;
		BufferedWriter writer = new BufferedWriter(new FileWriter(resultName));

		int start1 = 0, start2 = 0;
		int end1 = qOne.getTotalNum() / segments;
		int end2 = qTwo.getTotalNum() / segments;
		for (int i = 0; i < segments; i++) {
			for (; start1 < end1; start1++, graphID++)
				writer.write(graphID + spliter + qOne.findGraphString(start1)
						+ "\n");
			for (; start2 < end2; start2++, graphID++)
				writer.write(graphID + spliter + qTwo.findGraphString(start2)
						+ "\n");
			end1 += qOne.getTotalNum() / segments;
			end2 += qTwo.getTotalNum() / segments;
		}
		writer.close();
		// Write the meta information of the new file
		BufferedWriter metaWriter = new BufferedWriter(new FileWriter(
				resultName + "_Meta"));
		// 1. Processing Date
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(
				"EEEE-MMMM-dd-yyyy");
		Date date = new Date();
		metaWriter.write(bartDateFormat.format(date));
		metaWriter.newLine();
		// 2. Number of graphs in this file
		metaWriter.write("Number of Graphs:" + graphID++);
		metaWriter.newLine();
		metaWriter.write("Average EdgeNum: " + 0 + ", Average NodeNum: " + 0);
		// Close meta data file
		metaWriter.close();
	}
}
