package com.data.algorithm.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 功能：把一个数组的值存入二叉树中，然后进行3种方式的遍历
 * 
 * 参考资料0:数据结构(C语言版)严蔚敏
 * 
 * 参考资料1：http://zhidao.baidu.com/question/81938912.html
 * 
 * 参考资料2：http://cslibrary.stanford.edu/110/BinaryTrees.html#java
 * 
 * @author ocaicai@yeah.net @date: 2011-5-17
 * 
 */
public class BinTreeTraverse {

	private int[] array = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private static List<Node> nodeList = null;

	/**
	 * 内部类：节点
	 * 
	 * @author ocaicai@yeah.net @date: 2011-5-17
	 * 
	 */
	private static class Node {
		Node leftChild;
		Node rightChild;
		int data;

		Node(int newData) {
			leftChild = null;
			rightChild = null;
			data = newData;
		}
	}

	public void createBinTree() {
		nodeList = new LinkedList<Node>();
		// 将一个数组的值依次转换为Node节点
		for (int nodeIndex = 0; nodeIndex < array.length; nodeIndex++) {
			nodeList.add(new Node(array[nodeIndex]));
		}
		// 对前lastParentIndex-1个父节点按照父节点与孩子节点的数字关系建立二叉树
		for (int parentIndex = 0; parentIndex < array.length / 2 - 1; parentIndex++) {
			// 左孩子
			nodeList.get(parentIndex).leftChild = nodeList.get(parentIndex * 2 + 1);
			// 右孩子
			nodeList.get(parentIndex).rightChild = nodeList.get(parentIndex * 2 + 2);
		}
		// 最后一个父节点:因为最后一个父节点可能没有右孩子，所以单独拿出来处理
		int lastParentIndex = array.length / 2 - 1;
		// 左孩子
		nodeList.get(lastParentIndex).leftChild = nodeList.get(lastParentIndex * 2 + 1);
		// 右孩子,如果数组的长度为奇数才建立右孩子
		if (array.length % 2 == 1) {
			nodeList.get(lastParentIndex).rightChild = nodeList.get(lastParentIndex * 2 + 2);
		}
	}

	/**
	 * 先序遍历
	 * 
	 * 这三种不同的遍历结构都是一样的，只是先后顺序不一样而已
	 * 
	 * @param node
	 *            遍历的节点
	 */
	public static void preOrderTraverse(Node node) {
		if (node == null)
			return;
		System.out.print(node.data + " ");
		preOrderTraverse(node.leftChild);
		preOrderTraverse(node.rightChild);
	}

	public static List<Node> preOrderTraverse2(Node node) {
		List<Node> list = new ArrayList<>();
		Stack<Node> stack = new Stack<>();
		stack.push(node);

		while (!stack.isEmpty()) {
			Node temp = stack.pop();
			if (temp != null) {
				list.add(temp);
				stack.push(temp.rightChild);
				stack.push(temp.leftChild);
			}
		}

		return list;
	}

	/**
	 * 中序遍历
	 * 
	 * 这三种不同的遍历结构都是一样的，只是先后顺序不一样而已
	 * 
	 * @param node
	 *            遍历的节点
	 */
	public static void inOrderTraverse(Node node) {
		if (node == null)
			return;
		inOrderTraverse(node.leftChild);
		System.out.print(node.data + " ");
		inOrderTraverse(node.rightChild);
	}

	public static List<Node> inOrderTraverse2(Node node) {
		List<Node> list = new ArrayList<>();
		Stack<Node> stack = new Stack<>();

		while (node != null || !stack.isEmpty()) {
			while (node != null) {
				stack.push(node);
				node = node.leftChild;
			}
			if (!stack.isEmpty()) {
				Node temp = stack.pop();
				list.add(temp);
				node = temp.rightChild;
			}
		}
		return list;
	}

	/**
	 * 后序遍历
	 * 
	 * 这三种不同的遍历结构都是一样的，只是先后顺序不一样而已
	 * 
	 * @param node
	 *            遍历的节点
	 */
	public static void postOrderTraverse(Node node) {
		if (node == null)
			return;
		postOrderTraverse(node.leftChild);
		postOrderTraverse(node.rightChild);
		System.out.print(node.data + " ");
	}

	public static List<Node> postOrderTraverse2(Node root) {
		Deque<Node> stack = new LinkedList<>();
		stack.push(root);
		List<Node> ret = new ArrayList<>();
		while (!stack.isEmpty()) {
			Node node = stack.pop();
			if (node != null) {
				ret.add(node);
				stack.push(node.leftChild);
				stack.push(node.rightChild);
			}
		}
		Collections.reverse(ret);
		return ret;
	}

	public static void main(String[] args) {
		BinTreeTraverse binTree = new BinTreeTraverse();
		binTree.createBinTree();
		// nodeList中第0个索引处的值即为根节点
		Node root = nodeList.get(0);

		System.out.println("先序遍历：");
		preOrderTraverse(root);
		System.out.println();
		List<Node> preList = preOrderTraverse2(root);
		for (Node node : preList) {
			System.out.print(node.data + " ");
		}
		System.out.println();

		System.out.println("中序遍历：");
		inOrderTraverse(root);
		System.out.println();
		List<Node> inList = inOrderTraverse2(root);
		for (Node node : inList) {
			System.out.print(node.data + " ");
		}
		System.out.println();

		System.out.println("后序遍历：");
		postOrderTraverse(root);
		System.out.println();
		List<Node> postList = postOrderTraverse2(root);
		for (Node node : postList) {
			System.out.print(node.data + " ");
		}
	}

}
