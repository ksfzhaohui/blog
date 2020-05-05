package com.data.algorithm.leetcode;

import java.util.HashMap;

public class 罗马数字转整数 {

	static HashMap<Character, Integer> map = new HashMap<>();
	static {
		map.put('I', 1);
		map.put('V', 5);
		map.put('X', 10);
		map.put('L', 50);
		map.put('C', 100);
		map.put('D', 500);
		map.put('M', 1000);

	}

	public static int romanToInt(String s) {
		if (s == null || s.length() <= 0) {
			return 0;
		}
		int value = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (map.containsKey(c)) {
				value += map.get(c);
			} else {
				return value;
			}
		}
		return value;
	}

	public static void main(String[] args) {
		System.out.println(romanToInt("III"));
	}
}
