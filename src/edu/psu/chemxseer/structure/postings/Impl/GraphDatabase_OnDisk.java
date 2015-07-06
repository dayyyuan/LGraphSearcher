package edu.psu.chemxseer.structure.postings.Impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import de.parmol.graph.Graph;
import de.parmol.parsers.GraphParser;

import edu.psu.chemxseer.structure.postings.Interface.IGraphDatabase;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;
import edu.psu.chemxseer.structure.util.NumericConverter;

/**
 * The default saving formate of graphs are with Smiles This is self
 * implementation of the graph dataset if self It includes: 1. One database file
 * 2. One database description (meta data) file 3. Operation on those files:
 * including reading and writing
 * 
 * @author dayuyuan
 * 
 */
public class GraphDatabase_OnDisk extends GraphDatabase_Basic implements
		IGraphDatabase {
	protected RandomAccessFile databaseFile;
	protected String databaseFileName;
	protected long[] index;

	public GraphDatabase_OnDisk(String dbFileName, GraphParser gParser) {
		super(gParser);
		this.databaseFileName = dbFileName;
		try {
			this.databaseFile = new RandomAccessFile(dbFileName, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		File indexFile = new File(databaseFileName + "_index");
		if (!indexFile.exists())
			try {
				// create and store the index file
				createIndexFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			try {
				loadIndexFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public GraphDatabase_OnDisk() {
		super(null);
	}

	@Override
	public void finalize() {
		try {
			if (this.databaseFile != null)
				this.databaseFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean createIndexFile() throws IOException {
		OutputStream indexWriter = new FileOutputStream(databaseFileName
				+ "_index", false);
		// First write how many graphs exists in this graph database
		BufferedReader dbMetaReader = new BufferedReader(new FileReader(
				databaseFileName + "_Meta"));
		dbMetaReader.readLine();
		String FirstLine = dbMetaReader.readLine();
		int totalNum = Integer.parseInt(FirstLine.split(":")[1]);
		this.index = new long[totalNum];
		indexWriter.write(NumericConverter.int2byte(totalNum));

		long shift = this.databaseFile.getFilePointer();
		this.databaseFile.seek(0);
		String aLine = this.databaseFile.readLine();
		int i = 0;
		while (aLine != null) {
			this.index[i] = shift;
			indexWriter.write(NumericConverter.long2byte(shift)); // write a
																	// long
			i++;
			shift = this.databaseFile.getFilePointer();
			aLine = this.databaseFile.readLine();
		}
		indexWriter.flush();
		indexWriter.close();
		dbMetaReader.close();
		return true;
	}

	/**
	 * Given the graph database is already indexed, load the index into memory
	 * This part is not counted as memory occupation
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean loadIndexFile() throws IOException {
		BufferedInputStream indexStream = new BufferedInputStream(
				new FileInputStream(databaseFileName + "_index"));
		byte[] tempByte = new byte[4];
		indexStream.read(tempByte);
		// Have to find more smarter ways of changing a byte array into a long
		// numeric value
		int tempNum = NumericConverter.byte2int(tempByte);
		// create the in-memory index for the graph database
		this.index = new long[tempNum];
		// Initialize the in-memory index for the graph database
		byte[] tempByte2 = new byte[8];
		for (int i = 0; i < tempNum; i++) {
			indexStream.read(tempByte2);
			index[i] = NumericConverter.byte2long(tempByte2);
		}
		indexStream.close();
		return true;
	}

	@Override
	public String findGraphString(int id) {
		if (id < 0 || id > this.index.length) {
			System.out
					.println("Exception, GraphDatabase: findGraph, index out of boundary");
			return null;
		} else {
			long shift = this.index[id];
			String graphSmiles = null;
			try {
				this.databaseFile.seek(shift);
				graphSmiles = this.databaseFile.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (graphSmiles == null)
				System.out
						.println("Exception, GraphDatabase: findGraph, Did not find the shift");
			String[] tokens = graphSmiles.split(" => ");
			if (tokens.length <= 1)
				System.out.println("Exception in findGraphString: "
						+ graphSmiles);
			return tokens[1];
		}
	}

	@Override
	public int getTotalNum() {
		return this.index.length;
	}

	public String getDBFileName() {
		return this.databaseFileName;
	}

	/**
	 * Return the approximate memory consumption of this graph database
	 * 
	 * @return
	 */
	public double getMemoryConsumption() {
		// Calculate the size of the databaseFileName:
		// Not the string's size, but the character size
		int stringSize = databaseFileName.length() << 1;

		// Calculate the size of the RandomFile
		// MemoryConsumptionCal.runGC();
		double before = MemoryConsumptionCal.usedMemoryinMB();
		RandomAccessFile theFileCopy = null;
		try {
			theFileCopy = new RandomAccessFile(databaseFileName, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		double after = MemoryConsumptionCal.usedMemoryinMB();
		try {
			theFileCopy.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Calculate the index size
		long indexSize = index.length << 3;

		return indexSize + (after - before) + stringSize;
	}

	public void getAverageSize() {
		float nodeNum = 0;
		float edgeNum = 0;
		for (int i = 0; i < this.getTotalNum(); i++) {
			Graph g = this.findGraph(i);
			nodeNum += g.getNodeCount();
			edgeNum += g.getEdgeCount();
		}
		float totalNum = this.getTotalNum();
		System.out.println("EdgeNum " + edgeNum / totalNum);
		System.out.println("NodeNum " + nodeNum / totalNum);
	}

	@Override
	public void setGString(int gID, String serialize) {
		throw new UnsupportedOperationException();
	}

}
