package edu.psu.chemxseer.structure.subsearch.Impl.indexfeature;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import edu.psu.chemxseer.structure.subsearch.Interfaces.IFeaturePosting;

/**
 * The Posting File of a Collection of Features:
 * 
 * @author dayuyuan
 * 
 */
public class FeaturePosting implements IFeaturePosting {
	protected String postingFileName;
	protected RandomAccessFile postingFile;

	public FeaturePosting(String postingFileName) {
		this.postingFileName = postingFileName;
		if (postingFileName != null)
			try {
				this.postingFile = new RandomAccessFile(this.postingFileName,
						"r");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	}

	public RandomAccessFile getPostingFile() {
		return this.postingFile;
	}

	public String getPostingFileName() {
		return this.postingFileName;
	}

	@Override
	public int[] getPosting(long postingShift) {
		String results = null;
		String[] tokens;
		if (this.postingFile == null)
			try {
				this.postingFile = new RandomAccessFile(this.postingFileName,
						"r");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		try {
			this.postingFile.seek(postingShift);
			results = this.postingFile.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		tokens = results.split(",");
		int[] posting = new int[tokens.length - 1];
		for (int i = 0; i < tokens.length - 1; i++)
			posting[i] = Integer.parseInt(tokens[i + 1]);
		return posting;
	}

	protected String getPostingString(long postingShift) {
		String results = null;
		if (this.postingFile == null)
			try {
				this.postingFile = new RandomAccessFile(this.postingFileName,
						"r");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		try {
			this.postingFile.seek(postingShift);
			results = this.postingFile.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return results;
	}

	/**
	 * Read the postings starting from the "featureShift", rewrite it two the
	 * postingChannel and return the new shift
	 * 
	 * @param postingChannel
	 * @param featureShift
	 * @param featureID
	 * @return
	 */
	public long savePostings(FileChannel postingChannel, long featureShift,
			int featureID) {
		ByteBuffer postbbuf = ByteBuffer.allocate(1024);
		long shift = -1;
		try {
			shift = postingChannel.position();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String postingString = this.getPostingString(featureShift);
		byte[] bytes = null;
		// Change ID if necessary
		int index = postingString.indexOf(",");
		bytes = (featureID + postingString.substring(index) + '\n').getBytes();

		// Starting writing the postings
		int start = 0;
		int length = postbbuf.capacity();
		while (start < bytes.length) {
			if (start + length <= bytes.length)
				postbbuf.put(bytes, start, length);
			else
				postbbuf.put(bytes, start, bytes.length - start);
			postbbuf.flip();
			try {
				postingChannel.write(postbbuf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			postbbuf.clear();
			start = start + length;
		}
		return shift;
	}

	public static long savePostings(FileChannel postingChannel, int[] postings,
			int featureID) throws IOException {
		Arrays.sort(postings);
		StringBuffer postingBuffer = new StringBuffer(200);
		ByteBuffer bbuf = ByteBuffer.allocate(1024);
		postingBuffer.append(featureID);

		for (int i = 0; i < postings.length; i++) {
			postingBuffer.append(',');
			postingBuffer.append(postings[i]);
		}
		postingBuffer.append('\n');
		byte[] bytes = postingBuffer.toString().getBytes();
		long shift = postingChannel.position();
		int start = 0;
		int length = bbuf.capacity();
		while (start < bytes.length) {
			if (start + length <= bytes.length)
				bbuf.put(bytes, start, length);
			else
				bbuf.put(bytes, start, bytes.length - start);
			bbuf.flip();
			postingChannel.write(bbuf);
			bbuf.clear();
			start = start + length;
		}
		return shift;
	}
}
