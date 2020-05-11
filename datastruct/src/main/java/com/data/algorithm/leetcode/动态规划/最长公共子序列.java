package com.data.algorithm.leetcode.动态规划;

public class 最长公共子序列 {

	public static int longestCommonSubsequence(String text1, String text2) {
		if (text1.length() == text2.length()) {
			if (!text1.equals(text2)) {
				return 0;
			} else {
				return text1.length();
			}
		} else {
			String father, son;
			if (text1.length() > text2.length()) {
				father = text1;
				son = text2;
			} else {
				father = text2;
				son = text1;
			}

			return findLCS(father.toCharArray(), 0, son.toCharArray(), 0);
		}

	}

	public static int findLCS(char[] a, int i, char[] b, int j) {
		if (i == a.length || j == b.length) {
			return 0;
		}
		if (a[i] == b[j]) {
			return 1 + findLCS(a, i + 1, b, j + 1);
		} else {
			//return Math.max(findLCS(a, i + 1, b, j), findLCS(a, i, b, j + 1));
			return findLCS(a, i + 1, b, j);
		}
	}

	public static void main(String[] args) {
		System.out.println(longestCommonSubsequence("abcdedft", "acebt"));
	}

}
