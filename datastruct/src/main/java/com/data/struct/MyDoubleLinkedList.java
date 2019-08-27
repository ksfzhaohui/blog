package com.data.struct;

/**
 * 双向链表
 * 
 * @author hui.zhao.cfs
 *
 * @param <T>
 */
public class MyDoubleLinkedList<T> implements Iterable<T> {
	private int theSize; // 链表长度
	private int modCount = 0; // 代表从构造方法以来，对链表所做改变的次数；
	private Node<T> beginMarker; // 头节点
	private Node<T> endMarker; // 尾节点

	public MyDoubleLinkedList() {
		clear();
	}

	// 初始化，创建头节点和尾节点，然后设置大小为0
	public void clear() {
		beginMarker = new Node<T>(null, null, null);
		endMarker = new Node<T>(null, beginMarker, null);
		beginMarker.next = endMarker;

		theSize = 0;
		modCount++;
	}

	public int modeCount() {
		return modCount;
	}

	public int size() {
		return theSize;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean add(T x) {
		add(size(), x);
		return true;
	}

	public void add(int index, T x) {
		addBefore(getNode(index), x);
	}

	public T get(int index) {
		return getNode(index).data;
	}

	public T set(int index, T newVal) {
		Node<T> p = getNode(index);
		T oldVal = p.data;
		p.data = newVal;
		return oldVal;
	}

	public T remove(int index) {
		return remove(getNode(index));
	}

	private T remove(Node<T> p) {
		p.next.prev = p.prev;
		p.prev.next = p.next;
		theSize--;
		modCount++;

		return p.data;
	}

	private void addBefore(Node<T> p, T x) {
		Node<T> newNode = new Node<T>(x, p.prev, p);
		p.prev.next = newNode;
		p.prev = newNode;
		theSize++;
		modCount++;
	}

	private Node<T> getNode(int index) {
		Node<T> p;
		if (index < 0 || index > size())
			throw new IndexOutOfBoundsException();

		/*
		 * 如果索引是该线链表前半部分的一个节点，那么我们将以向后的方式遍历该链表； 否则，我们将从终端向前找；
		 */
		if (index < size() / 2) {
			p = beginMarker.next;
			for (int i = 0; i < index; i++)
				p = p.next;
		} else {
			p = endMarker;
			for (int i = size(); i > index; i--)
				p = p.prev;
		}
		return p;
	}

	public java.util.Iterator<T> iterator() {
		return new LinkedListIterator();
	}

	private class LinkedListIterator implements java.util.Iterator<T> {

		private Node<T> current = beginMarker.next;
		private int expectedModCount = modCount;
		private boolean okToRemove = false;

		public boolean hasNext() {
			return current != endMarker;
		}

		public T next() {
			if (modCount != expectedModCount)
				throw new java.util.ConcurrentModificationException();
			if (!hasNext())
				throw new java.util.NoSuchElementException();
			T nextItem = current.data;
			current = current.next;
			okToRemove = true;
			return nextItem;
		}

		public void remove() {
			if (modCount != expectedModCount)
				throw new java.util.ConcurrentModificationException();
			if (!okToRemove)
				throw new IllegalStateException();
			MyDoubleLinkedList.this.remove(current.prev);
			okToRemove = false;
			expectedModCount++;

		}

	}

	private static class Node<T> {
		public T data; // 数据
		public Node<T> prev; // 上一个节点
		public Node<T> next; // 下一个节点

		public Node(T d, Node<T> p, Node<T> n) {
			data = d;
			prev = p;
			next = n;
		}
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		MyDoubleLinkedList<String> list = new MyDoubleLinkedList<String>();
		list.add("I");
		list.add("am");
		list.add("a");
		list.add("man");

		System.out.println(list.size());
		System.out.println(list.get(1));
		list.set(1, "is");
		System.out.println(list.remove(2));

		System.out.println(list.modeCount());

		MyDoubleLinkedList.LinkedListIterator iterator = (MyDoubleLinkedList.LinkedListIterator) list.iterator();
		while (iterator.hasNext()) {
			System.out.print(iterator.next() + " ");
		}
	}

}
