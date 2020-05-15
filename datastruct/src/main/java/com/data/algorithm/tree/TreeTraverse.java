package com.data.algorithm.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public class TreeTraverse {

	private static List<Node> pre(Node root) {
		if (root == null) {
			return new ArrayList<>();
		}
		List<Node> list = new ArrayList<Node>();
		list.add(root);
		list.addAll(pre(root.leftChild));
		list.addAll(pre(root.rightChild));
		return list;
	}

	private static List<Node> pre2(Node root) {
		List<Node> list = new ArrayList<>();
		Stack<Node> stack = new Stack<>();
		stack.add(root);
		while (!stack.isEmpty()) {
			Node node = stack.pop();
			if (node != null) {
				list.add(node);
				stack.add(node.rightChild);
				stack.add(node.leftChild);
			}
		}
		return list;
	}

	private static List<Node> in(Node root) {
		if (root == null) {
			return new ArrayList<>();
		}
		List<Node> list = new ArrayList<Node>();
		list.addAll(in(root.leftChild));
		list.add(root);
		list.addAll(in(root.rightChild));
		return list;
	}

	private static List<Node> in2(Node root) {
		List<Node> list = new ArrayList<>();
		Stack<Node> stack = new Stack<>();
		while (root != null || !stack.isEmpty()) {
			while (root != null) {
				stack.add(root);
				root = root.leftChild;
			}
			if (!stack.isEmpty()) {
				Node temp = stack.pop();
				list.add(temp);
				root = temp.rightChild;
			}
		}
		return list;
	}

	private static List<Node> post(Node root) {
		if (root == null) {
			return new ArrayList<>();
		}
		List<Node> list = new ArrayList<Node>();
		list.addAll(post(root.leftChild));
		list.addAll(post(root.rightChild));
		list.add(root);
		return list;
	}

	/**
	 * 层遍历
	 * 
	 * @param node
	 * @return
	 */
	public static List<Node> levelOrder(Node node) {
		List<Node> list = new ArrayList<Node>();
		Queue<Node> queue = new LinkedBlockingQueue<>();
		queue.add(node);
		while (!queue.isEmpty()) {
			int size = queue.size();
			for (int i = 0; i < size; i++) {
				Node treeNode = queue.poll();
				list.add(treeNode);

				if (treeNode.leftChild != null) {
					queue.add(treeNode.leftChild);
				}
				if (treeNode.rightChild != null) {
					queue.add(treeNode.rightChild);
				}
			}

		}

		return list;
	}

	public static void main(String[] args) {
		Node t1 = new Node(1);
		Node t2 = new Node(2);
		Node t3 = new Node(3);
		Node t4 = new Node(4);
		Node t5 = new Node(5);

		t1.leftChild = t2;
		t1.rightChild = t3;

		t2.leftChild = t4;
		t2.rightChild = t5;

		System.out.println(levelOrder(t1));
	}
}

class Node {
	int value;
	Node leftChild;
	Node rightChild;

	public Node(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "v=" + value;
	}
}
