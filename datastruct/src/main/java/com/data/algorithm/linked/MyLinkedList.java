package com.data.algorithm.linked;

/**
 * 单向链表
 * 
 * @author hui.zhao.cfs
 *
 * @param <T>
 */
public class MyLinkedList<T> {

	private Node<T> header = null; // 头结点
	int size = 0; // 表示链表长度

	public MyLinkedList() {
		this.header = new Node<T>();
	}

	public boolean add(T data) {
		if (size == 0) {
			header.data = data;
		} else {
			// 根据需要添加的内容，封装为结点
			Node<T> newNode = new Node<T>(data);
			// 得到当前最后一个结点
			Node<T> last = getNode(size - 1);
			// 在最后一个结点后加上新结点
			last.addNext(newNode);
		}
		size++;// 当前大小自增加1
		return true;
	}

	public boolean insert(int index, T data) {
		Node<T> newNode = new Node<T>(data);
		// 得到第N个结点
		Node<T> cNode = getNode(index);
		newNode.next = cNode.next;
		cNode.next = newNode;
		size++;
		return true;

	}

	// 遍历当前链表，取得当前索引对应的元素
	private Node<T> getNode(int index) {
		// 先判断索引正确性
		if (index > size || index < 0) {
			throw new RuntimeException("索引值有错：" + index);
		}
		Node<T> tem = new Node<T>();
		tem = header;
		int count = 0;
		while (count != index) {
			tem = tem.next;
			count++;
		}
		return tem;
	}

	// 根据索引，取得该索引下的数据
	public T get(int index) {
		// 先判断索引正确性
		if (index >= size || index < 0) {
			throw new RuntimeException("索引值有错：" + index);
		}
		Node<T> tem = new Node<T>();
		tem = header;
		int count = 0;
		while (count != index) {
			tem = tem.next;
			count++;
		}
		T data = tem.data;
		return data;
	}

	public int size() {
		return size;
	}

	// 设置第N个结点的值
	public boolean set(int index, T e) {
		// 先判断索引正确性
		if (index > size || index < 0) {
			throw new RuntimeException("索引值有错：" + index);
		}
		// 得到第x个结点
		Node<T> cNode = getNode(index);
		cNode.data = e;
		return true;
	}

	public void display() {
		System.out.println("显示链表内容");
		Node<T> node = header;
		while (node != null) {
			System.out.print(node.data + " ");
			node = node.next;
		}
	}

	// 用来存放数据的结点型内部类
	class Node<e> {
		private T data; // 结点中存放的数据

		Node() {
		}

		Node(T data) {
			this.data = data;
		}

		Node<T> next; // 用来指向该结点的下一个结点

		// 在此结点后加一个结点
		void addNext(Node<T> node) {
			next = node;
		}
	}

	public static void main(String[] args) {
		MyLinkedList<String> list = new MyLinkedList<String>();

		for (int i = 0; i < 100; i++)
			list.add("add " + i);
		System.out.println(list.get(3));
		System.out.println(list.size());

		list.display();
	}

}
