package edu.psu.chemxseer.structure.util;

/**
 * The major contribution of this class is to calculate the memory consuption
 * difference
 * 
 * @author dayuyuan
 * 
 */
public class MemoryConsumptionCal {

	public static final long mega = 1024L * 1024L;

	/**
	 * Memory measure method
	 * 
	 * @throws Exception
	 */
	public static void runGC() {
		// It helps to call Runtime.gc()
		// using several method calls:
		for (int r = 0; r < 10; ++r)
			try {
				_runGC();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	private static void _runGC() throws Exception {
		long usedMem1 = usedMemory(), usedMem2 = Long.MAX_VALUE;
		for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
			s_runtime.runFinalization();
			s_runtime.gc();
			Thread.yield();
			usedMem2 = usedMem1;
			usedMem1 = usedMemory();
		}
	}

	private static long usedMemory() {
		return s_runtime.totalMemory() - s_runtime.freeMemory();
	}

	public static double usedMemoryinMB() {
		return ((double) (s_runtime.totalMemory() - s_runtime.freeMemory()))
				/ mega;
	}

	private static final Runtime s_runtime = Runtime.getRuntime();

	/**
	 * Test results: Integer 4 byte, boolean 1 byte, char 2 byte An empty object
	 * 20; An array 20 byte, each entry 8 byte pointer The memory is located 8
	 * bytes per time For others still need test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MemoryConsumptionCal.runGC();
		long before = MemoryConsumptionCal.usedMemory();
		// long[] santa = new long[100];

		MemoryConsumptionCal.runGC();
		long after = MemoryConsumptionCal.usedMemory();
		System.out.print(before + " " + after + " " + (after - before));
	}
}
