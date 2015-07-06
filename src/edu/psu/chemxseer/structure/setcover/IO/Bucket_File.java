package edu.psu.chemxseer.structure.setcover.IO;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;

/**
 * Write the sets one after another: needed in the Sort_SetCover
 * 
 * @author dayuyuan
 * 
 */
public class Bucket_File implements IBucket {
	private ObjectOutputStream outputStream = null; // writer to the file
	private boolean append;
	private Input_File input;
	private int qCount = 0;
	private int gCount = 0;

	/**
	 * Load the bucket_file from the disk
	 * 
	 * @param inputFileName
	 * @param append
	 * @param universeSize
	 * @return
	 */
	public static Bucket_File newInstance(String inputFileName, boolean append,
			IFeatureConverter converter) {
		return new Bucket_File(inputFileName, append, converter);
	}

	/**
	 * Create a new empty BucketFile.
	 * 
	 * @param inputFileName
	 * @param append
	 * @param universeSize
	 * @return
	 */
	public static Bucket_File newEmptyInstance(String inputFileName,
			boolean append, IFeatureConverter converter) {
		return new Bucket_File(inputFileName, append, converter);
	}

	private Bucket_File(String inputFileName, boolean append,
			IFeatureConverter converter) {
		this.input = new Input_File(inputFileName, converter);
		this.append = append;
	}

	@Override
	public void finalize() {
		if (this.outputStream != null) {
			System.out
					.println("Exceptin in Bucket_File: finalize, the outputstream is not closed properly");
			this.closeOutputStream();
		}
		this.input.finalize();
	}

	/**
	 * Don't forget to close the output stream
	 */
	public void closeOutputStream() {
		if (outputStream != null)
			try {
				this.outputStream.flush();
				this.outputStream.close();
				this.outputStream = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void closeInputStream() throws Exception {
		this.input.closeInputStream();
	}

	@Override
	public boolean append(ISet set) {
		try {
			if (outputStream == null)
				createOutput();
			if (set instanceof Set_Pair)
				outputStream.writeObject(set);
			else
				System.out
						.println("Exception in Input_Mem: storeSelected: can not serialize ISet object except for"
								+ "implemnetations Set_Array or Set_Pair");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void createOutput() throws IOException {
		// 1. Before create outputStream, if the inputStream is open, close it
		this.input.closeInputStream(); // first close the input stream
		// 2. create the outputStream
		try {
			this.outputStream = new ObjectOutputStream(new FileOutputStream(
					input.getFileName(), append));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Iterator<ISet> iterator() {
		// before returning the iterator, need to close the output stream
		this.closeOutputStream(); // first close the outputstream
		return input.iterator();
	}

	@Override
	public void insertWithOrder(ISet theSet) {
		throw new UnsupportedOperationException(
				"remove() method is not supported");
	}

	@Override
	public void flush() throws IOException {
		if (this.outputStream != null)
			outputStream.flush();
	}

	@Override
	public int getQCount() {
		return this.qCount;
	}

	@Override
	public int getGCount() {
		return this.gCount;
	}
}
