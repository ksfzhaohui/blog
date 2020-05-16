package com.data.algorithm.tree;

import java.util.ArrayList;
import java.util.List;

import com.data.algorithm.tree.BinaryTree.TreeNode;

/**
 * 将有序数组转换为二叉搜索树
 * 
 * @author ksfzhaohui
 *
 */
public class ArrayToBST {

	public static TreeNode sortedArrayToBST(int[] nums, int start, int end) {
		if (start > end) {
			return null;
		}

		int pos = (end - start) / 2 + start;
		TreeNode root = new TreeNode();
		root.data = nums[pos];
		root.left = sortedArrayToBST(nums, start, pos - 1);
		root.right = sortedArrayToBST(nums, pos + 1, end);

		return root;
	}

	public static List<Integer> in(TreeNode root) {
		List<Integer> list = new ArrayList<Integer>();
		if (root == null) {
			return list;
		}
		list.addAll(in(root.left));
		list.add((int) root.data);
		list.addAll(in(root.right));
		return list;
	}

	public static void main(String[] args) {
		int nums[] = { 1, 2, 3, 4, 5, 6, 7 };
		TreeNode root = sortedArrayToBST(nums, 0, nums.length - 1);
		List<Integer> list = in(root);
		System.out.println(list);
	}

}
