package com.data.algorithm.tree.rightView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 给定一棵二叉树，想象自己站在它的右侧，按照从顶部到底部的顺序，返回从右侧所能看到的节点值。
	输入: [1,2,3,null,5,null,4]
	输出: [1, 3, 4]
	解释:
	   1            <---
	 /   \
	2     3         <---
	 \     \
	  5     4       <---

 * @author hui.zhao.cfs
 *
 */
public class Solution {

	public static List<Object> rightSideView(TreeNode root) {
		List<Object> res = new ArrayList<Object>();
		if (root == null) {
			return null;
		}

		Queue<TreeNode> queue = new LinkedList<>();
		queue.offer(root);
		while (!queue.isEmpty()) {
			int size = queue.size();
			for (int i = 0; i < size; i++) {
				TreeNode treeNode = queue.poll();
				if (treeNode.left != null) {
					queue.offer(treeNode.left);
				}
				if (treeNode.right != null) {
					queue.offer(treeNode.right);
				}
				if (i == size - 1) {
					res.add(treeNode.data);
				}
			}
		}

		return res;
	}
}

class TreeNode {
	Object data;
	TreeNode left;
	TreeNode right;

	public TreeNode() {

	}

	public TreeNode(Object data) {
		this.data = data;
	}

	public TreeNode(Object data, TreeNode left, TreeNode right) {
		this.data = data;
		this.left = left;
		this.right = right;
	}
}
