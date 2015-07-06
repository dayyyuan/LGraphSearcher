package edu.psu.chemxseer.structure.util;

public class MinHeap<T extends Comparable<T>> {
	private Object[] heap;
	private int size;
	private int capacity;

	// Constructor
	public MinHeap(int capacity) {
		this.capacity = capacity;
		heap = new Object[capacity + 1];
		size = 0;
	}

	public int getSize() {
		return size;
	}

	@SuppressWarnings("unchecked")
	public T getMin() {
		return (T) heap[1];
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
		Object temp = heap[pos1];
		heap[pos1] = heap[pos2];
		heap[pos2] = temp;
	}

	@SuppressWarnings("unchecked")
	public void insert(T entry) {
		if (size == this.capacity)
			enlargeHeap();
		// do insertion
		size++;
		heap[size] = entry;
		int currentPosition = size;
		while (currentPosition != 1
				&& ((T) heap[currentPosition])
						.compareTo((T) heap[parent(currentPosition)]) == -1) {
			swap(currentPosition, parent(currentPosition));
			currentPosition = parent(currentPosition);
		}
	}

	private void enlargeHeap() {
		int toSize = 2 * capacity;
		Object[] newHeap = new Object[toSize];
		for (int i = 1; i < capacity; i++) {
			newHeap[i] = heap[i];
		}
		this.capacity = toSize;
		this.heap = newHeap;
	}

	public void print() {
		for (int i = 1; i < size; i++)
			System.out.println(heap[i] + " ");
		System.out.println();
	}

	@SuppressWarnings("unchecked")
	public T deleteMin() {
		swap(1, size);
		size--;
		if (size != 0)
			pushdown(1);
		return (T) heap[size + 1];
	}

	@SuppressWarnings("unchecked")
	private void pushdown(int position) {
		int smallestchild;
		while (!isleaf(position)) {
			smallestchild = leftChild(position);
			if (smallestchild < size
					&& ((T) heap[smallestchild])
							.compareTo((T) heap[smallestchild + 1]) == 1)
				smallestchild = smallestchild + 1;
			if (((T) heap[position]).compareTo((T) heap[smallestchild]) != 1)
				return; // terminate push down
			swap(position, smallestchild);
			position = smallestchild;
		}
	}
}
