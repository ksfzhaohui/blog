package com.data.struct;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class BinaryTree<E> {
	// 为什么要用静态内部类？静态内部类中不能访问外部类的非静态成员
	public static class TreeNode {
		// E data;
		Object data;
		TreeNode left;
		TreeNode right;

		public TreeNode() {

		}

		public TreeNode(Object data) {
			this.data = data;
		}

		// 构造一个新节点，该节点以left节点为其左孩子，right节点为其右孩子
		public TreeNode(Object data, TreeNode left, TreeNode right) {
			this.data = data;
			this.left = left;
			this.right = right;
		}
	}

	private TreeNode root;// 实现二叉树的类的数据域，即根结点来表示二叉树

	public BinaryTree() {
		this.root = new TreeNode();
	}

	// 以指定的根元素创建一颗二叉树
	public BinaryTree(E data) {
		this.root = new TreeNode(data);
	}

	// 为指定的结点添加子结点,为什么要有addNode方法？因为给定一系列的结点，通过调用该方法来构造成一颗树
	public TreeNode addNode(TreeNode parent, E data, boolean isLeft) {
		if (parent == null)
			throw new RuntimeException("父节点为空，无法添加子结点");
		if (isLeft && parent.left != null)
			throw new RuntimeException("节点已经左子节点，添加失败");
		if (!isLeft && parent.right != null)
			throw new RuntimeException("节点已经有右子节点，添加失败");
		TreeNode newNode = new TreeNode(data);
		if (isLeft)
			parent.left = newNode;
		else
			parent.right = newNode;
		return newNode;
	}

	public boolean empty() {
		return root.data == null;// 根据根元素判断二叉树是否为空
	}

	public TreeNode root() {
		if (empty())
			throw new RuntimeException("树空，无法访问根结点");
		return root;
	}

	public E parent(TreeNode node) {
		return null;// 采用二叉树链表存储时，访问父结点需要遍历整棵二叉树，因为这里不实现
	}

	// 访问指定节点的左结点，返回的是其左孩子的数据域
	public E leftChild(TreeNode parent) {
		if (parent == null)
			throw new RuntimeException("空结点不能访问其左孩子");
		return parent.left == null ? null : (E) parent.left.data;
	}

	public E rightChild(TreeNode parent) {
		if (parent == null)
			throw new RuntimeException("空结点不能访问其右孩子");
		return parent.right == null ? null : (E) parent.right.data;
	}

	public int deep() {
		return deep(root);
	}

	private int deep(TreeNode node) {
		if (node == null)
			return 0;
		else if (node.left == null && node.right == null)
			return 1;
		else {
			int leftDeep = deep(node.left);
			int rightDeep = deep(node.right);
			int max = leftDeep > rightDeep ? leftDeep : rightDeep;
			return max + 1;
		}
	}

	/*
	 * 二叉树的先序遍历，实现思想如下：树是一种非线性结构，树中各个结点的组织方式有多种方式
	 * 先序，即是一种组织方式。它将结点的非线性变成了按照某种方式组织成的线性结构
	 */
	// 返回一个list，树中结点以先序的方式存放在该list中
	public List<TreeNode> preTraverse() {
		return preOrderTraverse(root);
	}

	private List<TreeNode> preOrderTraverse(TreeNode node) {
		List<TreeNode> list = new ArrayList<TreeNode>();
		list.add(node);
		if (node.left != null)
			list.addAll(preOrderTraverse(node.left));// 递归的奇妙之处
		if (node.right != null)
			list.addAll(preOrderTraverse(node.right));
		return list;
	}

	// 中序遍历
	public List<TreeNode> inTraverse() {
		return inOrderTraverse(root);
	}

	private List<TreeNode> inOrderTraverse(TreeNode node) {
		List<TreeNode> list = new ArrayList<TreeNode>();
		if (node.left != null)
			list.addAll(inOrderTraverse(node.left));
		list.add(node);
		if (node.right != null)
			list.addAll(inOrderTraverse(node.right));
		return list;
	}

	// 后序遍历
	public List<TreeNode> postTraverse() {
		return post_Traverse(root);
	}

	private List<TreeNode> post_Traverse(TreeNode node) {
		List<TreeNode> list = new ArrayList<TreeNode>();
		if (node.left != null)
			list.addAll(post_Traverse(node.left));
		if (node.right != null)
			list.addAll(post_Traverse(node.right));
		list.add(node);
		return list;
	}

	// 层序遍历
	public List<TreeNode> levelTraverse() {
		return level_Traverse(root);
	}

	private List<TreeNode> level_Traverse(TreeNode node) {
		Queue<TreeNode> queue = new ArrayDeque<TreeNode>();
		List<TreeNode> list = new ArrayList<TreeNode>();// 按层序遍历定义的顺序将树中结点依次添加到数组列表中
		if (root != null)// 先将根结点入队列
			queue.offer(root);
		while (!queue.isEmpty())// 队列不空时，说明遍历还未结束
		{
			list.add(queue.peek());// 将队头元素添加到数组列表中
			TreeNode p = queue.poll();// 队头元素出队列
			if (p.left != null)
				queue.offer(p.left);// 队头元素的左孩子入队列
			if (p.right != null)
				queue.offer(p.right);// 队头元素的右孩子入队列
		}
		return list;
	}

	public static void main(String[] args) {
		BinaryTree<String> bt = new BinaryTree<String>("根节点");
		BinaryTree.TreeNode tn1 = bt.addNode(bt.root(), "第二层左子结点", true);
		BinaryTree.TreeNode tn2 = bt.addNode(bt.root(), "第二层右子结点", false);
		BinaryTree.TreeNode tn3 = bt.addNode(tn2, "第三层左子结点", true);

		List<BinaryTree.TreeNode> list1 = new ArrayList<BinaryTree.TreeNode>();
		list1 = bt.inTraverse();
		System.out.println("inorder traverse");
		for (BinaryTree.TreeNode node : list1)
			System.out.print(node.data + " ");

		List<BinaryTree.TreeNode> list2 = new ArrayList<BinaryTree.TreeNode>();
		list2 = bt.preTraverse();
		System.out.println("\n preorder traverse");
		for (BinaryTree.TreeNode node : list2)
			System.out.print(node.data + " ");
		List<BinaryTree.TreeNode> list3 = new ArrayList<BinaryTree.TreeNode>();
		list3 = bt.levelTraverse();
		System.out.println("\n level traverse");
		for (BinaryTree.TreeNode node : list3)
			System.out.println(node.data + " ");
	}
}