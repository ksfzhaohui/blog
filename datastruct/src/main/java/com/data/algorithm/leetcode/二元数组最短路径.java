package com.data.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个二维数组,二维数组中的每个数都是正数,要求从左上角走到右下角,每一步只能向右或者向下，沿途经过的数字要累加起来,返回最小路径和
 * 
 * @author hui.zhao.cfs
 *
 */
public class 二元数组最短路径 {


	private static int minPathSum(int[][] path, int i, int j) {
		if (i == path.length - 1 && j == path[0].length - 1)
			return path[i][j];
		if (i == path.length - 1) {
			return path[i][j] + minPathSum(path, i, j + 1);
		}
		if (j == path[0].length - 1) {
			return path[i][j] + minPathSum(path, i + 1, j);
		}
		return path[i][j] + Math.min(minPathSum(path, i, j + 1), minPathSum(path, i + 1, j));
	}

	private static Map<String, Integer> cache = new HashMap<String, Integer>();

	private static int minPathSum2(int[][] path, int i, int j) {
		int res = 0;
		if (i == path.length - 1 && j == path[0].length - 1)
			return path[i][j];
		if (i == path.length - 1) {
			if (cache.containsKey(i + "-" + (j + 1))) {
				return path[i][j] + cache.get(i + "-" + (j + 1));
			} else {
				res = path[i][j] + minPathSum(path, i, j + 1);
			}
		}
		if (j == path[0].length - 1) {
			if (cache.containsKey((i + 1) + "-" + j)) {
				return path[i][j] + cache.get((i + 1) + "-" + j);
			} else {
				res = path[i][j] + minPathSum(path, i + 1, j);
			}
		}
		int left;
		if (cache.containsKey(i + "-" + (j + 1))) {
			left = cache.get(i + "-" + (j + 1));
		} else {
			left = minPathSum(path, i, j + 1);
		}
		int down;
		if (cache.containsKey((i + 1) + "-" + j)) {
			down = cache.get((i + 1) + "-" + j);
		} else {
			down = minPathSum(path, i + 1, j);
		}
		res = path[i][j] + Math.min(left, down);
		cache.put(i + "-" + j, res);
		return res;
	}

	public static void main(String[] args) {
		int path[][] = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
		System.out.println(minPathSum(path, 0, 0));
		System.out.println(minPathSum2(path, 0, 0));
		int path2[][] = { { 1, 2, 3, 4 }, { 2, 5, 6, 7 }, { 2, 3, 4, 5 } };
		System.out.println(minPathSum(path2, 0, 0));
		System.out.println(minPathSum2(path2, 0, 0));

	}

}
