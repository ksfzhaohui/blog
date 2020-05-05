package com.data.algorithm.leetcode;

public class 最长公共前缀 {

	public static String longestCommonPrefix(String[] strs) {
		if (strs == null || strs.length < 1) {
			return "";
		}
		if (strs.length == 1) {
			return strs[0];
		}

		String common = "";
		for (int k = 1; k < Integer.MAX_VALUE; k++) {
			if (strs[0].length() >= k) {
				common = strs[0].substring(0, k);
			} else {
				return common;
			}
			for (int i = 1; i < strs.length; i++) {
				if (strs[i].length() >= k && common.equals(strs[i].substring(0, k))) {

				} else {
					return common.substring(0, k - 1);
				}
			}
		}
		return common;
	}

	public static void main(String[] args) {
		String strs[] = { "dog", "racecar", "car" };
		System.out.println(longestCommonPrefix(strs));
	}
}
