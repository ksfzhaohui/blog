package com.data.algorithm.leetcode.高频面试;

import java.util.Stack;

public class 合法括号判定 {

	public static boolean isValid(String s) {
		Stack<Character> stack = new Stack<>();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '{' || s.charAt(i) == '[' || s.charAt(i) == '(') {
				stack.push(s.charAt(i));
			} else {
				if (!stack.isEmpty() && getLeftChar(s.charAt(i)) == stack.pop()) {

				} else {
					return false;
				}
			}
		}
		return stack.isEmpty();
	}

	private static char getLeftChar(char c) {
		if (c == '}')
			return '{';
		else if (c == ']')
			return '[';
		return '(';
	}
	
	public static void main(String[] args) {
		System.out.println(isValid("()[]{}"));
		System.out.println(isValid("([)]"));
		System.out.println(isValid("{[]}"));
	}

}
