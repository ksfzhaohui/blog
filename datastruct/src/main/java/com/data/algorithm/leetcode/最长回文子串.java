package com.data.algorithm.leetcode;

/**
 * L5 给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为 1000。
 * 
 * 示例 1：
 * 
 * 输入: "babad" 输出: "bab" 注意: "aba" 也是一个有效答案。
 * 
 * @author hui.zhao.cfs
 *
 */
public class 最长回文子串 {

	public boolean isPalindromic(String s) {
		int len = s.length();
		for (int i = 0; i < len / 2; i++) {
			if (s.charAt(i) != s.charAt(len - i - 1)) {
				return false;
			}
		}
		return true;
	}

	// 暴力解法
	public String longestPalindrome(String s) {
		String ans = "";
		int max = 0;
		int len = s.length();
		for (int i = 0; i < len; i++)
			for (int j = i + 1; j <= len; j++) {
				String test = s.substring(i, j);
				if (isPalindromic(test) && test.length() > max) {
					ans = s.substring(i, j);
					max = Math.max(max, ans.length());
				}
			}
		return ans;
	}

	public String longestPalindrome2(String s) {
		int length = s.length();
		boolean[][] P = new boolean[length][length];
		int maxLen = 0;
		String maxPal = "";
		for (int len = 1; len <= length; len++) // 遍历所有的长度
			for (int start = 0; start < length; start++) {
				int end = start + len - 1;
				if (end >= length) // 下标已经越界，结束本次循环
					break;
				// 长度为 1 和 2 的单独判断下
				P[start][end] = (len == 1 || len == 2 || P[start + 1][end - 1]) && s.charAt(start) == s.charAt(end);
				if (P[start][end] && len > maxLen) {
					maxPal = s.substring(start, end + 1);
				}
			}
		return maxPal;
	}

	public static String palindrome(String s, int l, int r) {
		boolean flag=false;
		while (l >= 0 && r < s.length() && s.charAt(l) == s.charAt(r)) {
			// 向两边展开
			l--;
			r++;
			flag=true;
		}
		if(flag){
			return s.substring(l + 1, r );
		}
		return "";
	}

	public static String longestPalindrome3(String s) {
		String res = "";
		for (int i = 0; i < s.length(); i++) {
			String r1 = palindrome(s, i, i);
			String r2 = palindrome(s, i, i + 1);

			res = res.length() > r1.length() ? res : r1;
			res = res.length() > r2.length() ? res : r2;

		}
		return res;
	}
	
	public static void main(String[] args) {
		System.out.println(longestPalindrome3("aacxycaa"));
		System.out.println(longestPalindrome3("aba"));
		System.out.println(longestPalindrome3("abbaccsss"));
		System.out.println(longestPalindrome3("ccabbadd"));
	}
}
