package edu.psu.chemxseer.structure.util;

/**
 * This is a self implementation of the min heap We provide two operations:
 * insert and deletemin This code is extended from
 * http://www.cs.usfca.edu/~galles/cs245/lecture/MinHeap.java.html
 * 
 * @author dayuyuan
 * 
 * @param <Value>
 */

public class MinHeapInteger {
	private int[] heap;
	private int[] values;
	private int maxSize;
	private int size;

	// Constructor
	public MinHeapInteger(int max) {
		maxSize = max + 1;
		heap = new int[maxSize];
		values = new int[maxSize];
		size = 0;
	}

	public int[] getKeys() {
		return heap;
	}

	public int[] getValues() {
		return values;
	}

	public int getSize() {
		return size;
	}

	public int getMin() {
		return heap[1];
	}

	public void clear() {
		this.size = 0;
	}

	// The 0 item is left empty
	// keys and values are stored starting from the 1st item
	public boolean isEmpty() {
		if (size == 0)
			return true;
		else
			return false;
	}

	private int leftChild(int pos) {
		return 2 * pos;
	}

	private int parent(int pos) {
		return pos / 2;
	}

	private boolean isleaf(int pos) {
		if (pos > size / 2 && pos <= size)
			return true;
		else
			return false;
	}

	private void swap(int pos1, int pos2) {
		int temp;
		int tempValue;
		temp = heap[pos1];
		tempValue = values[pos1];
		heap[pos1] = heap[pos2];
		values[pos1] = values[pos2];
		heap[pos2] = temp;
		values[pos2] = tempValue;

	}

	public void insert(int key, int value) {
		if (size == this.maxSize)
			enlargeHeap();
		// do insertion
		size++;
		heap[size] = key;
		values[size] = value;
		int currentPosition = size;

		while (heap[currentPosition] < heap[parent(currentPosition)]) {
			swap(currentPosition, parent(currentPosition));
			currentPosition = parent(currentPosition);
		}
	}

	private void enlargeHeap() {
		int toSize = 2 * maxSize;
		int[] newHeap = new int[toSize];
		int[] newValues = new int[toSize];
		for (int i = 1; i < maxSize; i++) {
			newHeap[i] = heap[i];
			newValues[i] = values[i];
		}
		this.maxSize = toSize;
		this.heap = newHeap;
		this.values = newValues;
	}

	public void print() {
		for (int i = 1; i < size; i++)
			System.out.println(heap[i] + " ");
		System.out.println();
	}

	public int deleteMin() {
		swap(1, size);
		size--;
		if (size != 0)
			pushdown(1);
		return values[size + 1];
	}

	private void pushdown(int position) {
		int smallestchild;
		while (!isleaf(position)) {
			smallestchild = leftChild(position);
			if (smallestchild < size
					&& heap[smallestchild] > heap[smallestchild + 1])
				smallestchild = smallestchild + 1;
			if (heap[position] <= heap[smallestchild])
				return; // terminate push down
			swap(position, smallestchild);
			position = smallestchild;
		}
	}

	private void pushup(int position) {
		while (position != 1) {
			int parent = parent(position);
			if (heap[position] < heap[parent])
				swap(position, parent);
			position = parent;
		}
	}

	// extended function
	public boolean updateKey(int v, int newKey) {
		// First step: find the position of the entry with value = v, worst case
		// O(n)
		int index = -1;
		for (int i = 1; i <= size; i++)
			if (values[i] == v) {
				index = i;
				break;
			}
		if (index == -1) {
			System.out.println("Exception: Can not find value v");
			return false;
		}
		// Update key value, have to increase the key, push down
		if (this.heap[index] <= newKey) {
			this.heap[index] = newKey;
			pushdown(index);
		} else {
			this.heap[index] = newKey;
			pushup(index);
		}
		return true;
	}

	// Only effective when the key is a integer
	// enlarge the key to be key + 1;
	public boolean upgradeKey(int v, int extension) {
		// First step: find the position of the entry with value = v, worst case
		// O(n)
		int index = -1;
		for (int i = 1; i <= size; i++)
			if (values[i] == v) {
				index = i;
				break;
			}
		if (index == -1) {
			System.out.println("Exception: Can not find value v");
			return false;
		}
		// Update key value, have to increase the key, push down
		this.heap[index] += extension;
		;
		pushdown(index);
		return true;
	}

	public boolean degradeKey(int v, int extension) {
		// First step: find the position of the entry with value = v, worst case
		// O(n)
		int index = -1;
		for (int i = 1; i <= size; i++)
			if (values[i] == v) {
				index = i;
				break;
			}
		if (index == -1) {
			System.out.println("Exception: Can not find value v");
			return false;
		}
		// Update key value, have to increase the key, push down
		this.heap[index] -= extension;
		;
		pushup(index);
		return true;
	}

	public boolean deleteEntry(int v) {
		int index = -1;
		for (int i = 1; i <= size; i++)
			if (values[i] == v) {
				index = i;
				break;
			}
		if (index == -1) {
			System.out.println("Exception: Can not find value v");
			return false;
		} else {
			swap(this.size, index);
			this.pushdown(index);
			this.size--;
			return true;
		}
	}

	public boolean insertOrUpdate(int v, int newKey) {
		// First step: find the position of the entry with value = v, worst case
		// O(n)
		int index = -1;
		for (int i = 1; i <= size; i++)
			if (values[i] == v) {
				index = i;
				break;
			}
		if (index == -1) {
			this.insert(newKey, v);
		}
		// Update key value, have to increase the key, push down
		if (this.heap[index] <= newKey) {
			pushdown(index);
		} else
			pushup(index);
		return true;
	}

}
