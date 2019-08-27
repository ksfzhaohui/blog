package com.data.struct;

/**
 * 数组
 * 
 * @author hui.zhao.cfs
 *
 * @param <T>
 */
public class MyArrayList<T> implements Iterable<T> {
	private static final int DEFAULT_CAPACITY = 10; // 数组长度为10；

	private int theSize; // 线性表的长度；
	private T[] theItems; // 数组保存数据；

	public MyArrayList() {
		clear();
	}

	public void clear() {
		theSize = 0;
		ensureCapacity(DEFAULT_CAPACITY);
	}

	public int size() {
		return theSize;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public T get(int index) {
		if (index < 0 || index >= size())
			throw new ArrayIndexOutOfBoundsException();
		return theItems[index];
	}

	public T set(int idx, T newVal) {
		if (idx < 0 || idx >= size())
			throw new ArrayIndexOutOfBoundsException();
		T old = theItems[idx];
		theItems[idx] = newVal;
		return old;
	}

	@SuppressWarnings("unchecked")
	public void ensureCapacity(int newCapacity) // 用于数组实例化，还有用于数组的扩充；
	{
		if (newCapacity < theSize)
			return;
		T[] old = theItems;
		theItems = (T[]) new Object[newCapacity];
		for (int i = 0; i < size(); i++) {
			theItems[i] = old[i];
		}
	}

	public boolean add(T x) {
		add(size(), x);
		return true;
	}

	public void add(int index, T x) {
		if (theItems.length == size())
			ensureCapacity(size() * 2 + 1); // 添加前确认数组容量是否满了；
		for (int i = theSize; i > index; i--)
			theItems[i] = theItems[i - 1];
		theItems[index] = x;

		theSize++;
	}

	public T remove(int index) {
		T removedItem = theItems[index];
		for (int i = index; i < size() - 1; i++)
			theItems[i] = theItems[i + 1];

		theSize--;
		return removedItem;
	}

	// 通过内部类进行遍历线性表元素
	public java.util.Iterator<T> iterator() {
		return new ArrayListIterator();
	}

	private class ArrayListIterator implements java.util.Iterator<T> {
		private int current = 0;

		public boolean hasNext() {
			return current < size();
		}

		public T next() {
			if (!hasNext())
				throw new java.util.NoSuchElementException();
			return theItems[current++];
		}

		public void remove() {
			MyArrayList.this.remove(--current);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		MyArrayList<Integer> list = new MyArrayList<Integer>();

		// 添加数据；
		list.add(10);
		list.add(23);
		list.add(34);
		list.add(45);
		list.add(123);
		list.add(234);
		list.add(3, 56);

		System.out.println(list.get(1));
		list.set(2, 12);

		System.out.println(list.remove(1));

		MyArrayList.ArrayListIterator iterator = (MyArrayList.ArrayListIterator) list.iterator();

		while (iterator.hasNext()) {
			System.out.print(iterator.next() + " ");
		}
		System.out.println();

		// 也可以通过内部类的remove方法删除线性表的数据；
		iterator.remove();

		iterator = (MyArrayList.ArrayListIterator) list.iterator();

		while (iterator.hasNext()) {
			System.out.print(iterator.next() + " ");
		}

	}
}
