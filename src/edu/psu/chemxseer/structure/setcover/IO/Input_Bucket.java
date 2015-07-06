package edu.psu.chemxseer.structure.setcover.IO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.psu.chemxseer.structure.setcover.IO.interfaces.IBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputBucket;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.IInputSequential;
import edu.psu.chemxseer.structure.setcover.IO.interfaces.ISet;
import edu.psu.chemxseer.structure.setcover.experiments.InputGenerator.InputType;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureConverter;
import edu.psu.chemxseer.structure.util.FileUtil;

/**
 * Input_Bucket: can be either in-memory or on-disk depending on the Bucket-Type
 * 
 * @author dayuyuan
 * 
 */
public class Input_Bucket implements IInputBucket {

	private List<IBucket> buckets;
	// the largest bucket is at the head, bucketID is the position of the Bucket
	// on the list
	// buckets are ordered from bigger to smaller
	private double logP; // logP is for segmentation
	private InputType type;
	private String bucketBase;
	private IFeatureConverter converter;

	/**
	 * Create a new & empty Input_Bucket All the buckets are stored based on the
	 * "baseFileName". The baseFileName can be null for in-memory buckets
	 * 
	 * @param baseFile
	 * @param logP
	 * @param type
	 * @return
	 */
	public static Input_Bucket newEmptyInstance(String baseFile, double logP,
			IFeatureConverter converter, InputType iType) {
		if (baseFile == null)
			baseFile = "temp";
		return new Input_Bucket(baseFile, logP, converter, iType);
	}

	/**
	 * Load all the well-built buckets Can be either loadSetSingle or
	 * loadSetDoulbe, depends on the set stored on the disk
	 * 
	 * @param fileName
	 * @param logP
	 *            : the ratio between successive level of buckets
	 * @param type
	 *            : in_memory bucket or on_disk bucket
	 * @return
	 */
	public static Input_Bucket newInstance(String baseFile, double logP,
			IFeatureConverter converter, InputType iType) {
		if (baseFile == null)
			baseFile = "temp";
		Input_Bucket input = new Input_Bucket(baseFile, logP, converter, iType);
		input.loadInput();
		return input;
	}

	/**
	 * Given an input_sequential, based on the logP, buckentize all the sets
	 * (w.r.t their sizes). The SetType depends on the input construct an
	 * input_bucket store on disk if type = on_disk store in memory if type =
	 * in_memory
	 * 
	 * @param input
	 * @param baseFile
	 *            : the baseFile should be a folder
	 * @param logP
	 * @param iType
	 * @return
	 */
	public static Input_Bucket newInstance(IInputSequential input,
			String baseFile, double logP, IFeatureConverter converter,
			InputType iType) {
		if (baseFile == null)
			baseFile = "temp";
		File baseDir = new File(baseFile);
		if (baseDir.exists())
			FileUtil.delete(baseDir);
		baseDir.mkdirs();

		Input_Bucket result = new Input_Bucket(baseFile, logP, converter, iType);
		for (ISet oneSet : input) {
			int gain = oneSet.size();
			result.append(oneSet, gain);
		}
		result.flush();
		return result;
	}

	/**
	 * Given already stored buckets file, newInstance a Input_Bucket
	 * 
	 * @param baseFile
	 * @param logP
	 * @param type
	 */
	private Input_Bucket(String baseFile, double logP,
			IFeatureConverter converter, InputType type) {
		bucketBase = baseFile;
		if (bucketBase.charAt(bucketBase.length() - 1) != '/')
			bucketBase = bucketBase + "/";
		this.logP = logP;
		this.type = type;
		this.buckets = new ArrayList<IBucket>();
		this.converter = converter;
	}

	/**
	 * Load all the stored buckets: the smallest bucket has name counter = 0,
	 * but inserted at the last place
	 */
	private void loadInput() {
		// get all the files under the bucketBase folder & load them
		File baseDir = new File(bucketBase);
		String[] allFiles = baseDir.list();
		int maxNumber = Integer.MIN_VALUE;
		for (int i = 0; i < allFiles.length; i++) {
			int temp = -1;
			try {
				temp = Integer.parseInt(allFiles[i]);
			} catch (NumberFormatException exception) {
				continue;
			}
			if (maxNumber < temp)
				maxNumber = temp;
		}
		// Find the largest number
		buckets = new ArrayList<IBucket>(maxNumber + 1);
		for (int i = 0; i <= maxNumber; i++)
			buckets.add(Bucket_Factory.loadBucket(bucketBase + i, converter,
					type));
		Collections.reverse(buckets);
	}

	public void flush() {
		for (IBucket bucket : this.buckets)
			try {
				bucket.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void closeInputStream() throws Exception {
		for (IBucket bucket : this.buckets)
			bucket.closeInputStream();
	}

	@Override
	public int getBucketCount() {
		return this.buckets.size();
	}

	@Override
	public boolean append(ISet oneSet, long gain) {
		// First find what is the "level" the "gain" function should be in
		// buckets are sorted from the largest to the smallest.
		int bucketID = this.getBucketID(gain);
		if (bucketID < 0) {
			int newSize = this.buckets.size() - bucketID;
			List<IBucket> newBuckets = new ArrayList<IBucket>(newSize);
			bucketID = 0 - bucketID;
			for (int i = 0; i < bucketID; i++)
				newBuckets.add(Bucket_Factory.createBucket(bucketBase
						+ (newSize - 1 - i), converter, type));
			newBuckets.addAll(buckets); // add the rest of the buckets
			buckets = newBuckets;

			return append(oneSet, gain);
		} else {
			if (buckets.get(bucketID) != null) {
				buckets.get(bucketID).append(oneSet);
				return true;
			} else {
				System.out.println("Exception: One bucket does not exists");
				return false;
			}
		}
	}

	@Override
	public boolean appendWithOrder(ISet theSet, long gain) {
		// First find what is the "level" the "gain" function should be in
		// buckets are sorted from the largest to the smallest.
		int bucketID = this.getBucketID(gain);
		if (bucketID < 0) {
			int newSize = this.buckets.size() - bucketID;
			List<IBucket> newBuckets = new ArrayList<IBucket>(newSize);
			bucketID = 0 - bucketID;
			for (int i = 0; i < bucketID; i++)
				newBuckets.add(Bucket_Factory.createBucket(bucketBase
						+ (newSize - 1 - i), converter, type));
			newBuckets.addAll(buckets);
			buckets = newBuckets;

			return appendWithOrder(theSet, gain);
		} else {
			if (buckets.get(bucketID) != null) {
				buckets.get(bucketID).insertWithOrder(theSet);
				return true;
			} else {
				System.out.println("Exception: One bucket does not exists");
				return false;
			}
		}
	}

	/**
	 * Given the gain,calculate the bucketID of the set BucketID is the position
	 * of the Bucket on the List<IBucket> should be assigned to
	 * 
	 * @param gain
	 *            > =1
	 * @return
	 */
	private int getBucketID(long gain) {
		if (gain <= 1)
			return this.buckets.size() - 1; // the last bucket (smallest) on the
											// list
		int result = (int) (Math.log(gain) / logP);
		return this.buckets.size() - 1 - result;
	}

	@Override
	public Iterator<IBucket> iterator() {
		return this.buckets.iterator();
	}

	@Override
	public int getBucketThreshold(int bucketID) {
		// given the bucketID, reverse it
		int value = this.size() - 1 - bucketID;
		// p^value = e^(logP*value)
		return (int) Math.exp(this.logP * value);
	}

	public int size() {
		return this.buckets.size();
	}

	@Override
	public int getQCount() {
		return this.converter.getQRange();
	}

	@Override
	public int getGCount() {
		return this.converter.getGRange();
	}

	@Override
	public int getEnumeratedSetCount() {
		System.out
				.println("Not Implemented So Far: Input_Bucket:getEnumeratedSetCount");
		return -1;
	}

}
