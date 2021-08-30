package com.data.algorithm.leetcode;

public class 两个大字符串数字相加 {
	public static void main(String[] args) {
		String str1 = "123459";
		String str2 = "123";
		System.out.println(add(str1, str2));// 123582
	}

	private static String add(String str1, String str2) {
		// 任何一个字符串为null或空字符串，都不需要相加了
		if (str1 == null || "".equals(str1)) {
			return str2;
		}
		if (str2 == null || "".equals(str2)) {
			return str1;
		}
		int maxLength = Math.max(str1.length(), str2.length());
		// 定义一个存贮结果的字符串，长度要比最大长度字符串还长一位，用于存储可能出现的进位
		StringBuffer result = new StringBuffer(maxLength + 1);
		// 翻转两个字符串
		str1 = new StringBuffer(str1).reverse().toString();
		str2 = new StringBuffer(str2).reverse().toString();
		// 反转后的结果分别为：
		// 954321
		// 321
		int minLength = Math.min(str1.length(), str2.length());
		// 进位
		int carry = 0;
		// 当前位上的数值
		int currentNum = 0;
		// 循环变量
		int i = 0;
		for (; i < minLength; i++) {
			// 分别获取两个字符对应的字面数值，然后相加，再加上进位
			currentNum = str1.charAt(i) + str2.charAt(i) - 2 * '0' + carry;
			// 获取进位
			carry = currentNum / 10;
			// 处理当前位的最终值
			currentNum %= 10;
			// 保存当前位的值到最终的字符缓冲区中
			result.append(String.valueOf(currentNum));
		}
		if (str1.length() < str2.length()) {
			// 选择
			str1 = str2;
		}
		for (; i < str1.length(); i++) {
			// 分别获取两个字符对应的字面数值，然后相加，再加上进位
			currentNum = str1.charAt(i) - '0' + carry;
			// 获取进位
			carry = currentNum / 10;
			// 处理当前位的最终值
			currentNum %= 10;
			// 保存当前位的值到最终的字符缓冲区中
			result.append(String.valueOf(currentNum));
		}
		// 处理最后一个的进位(当循环结束后，是不是还可能会有一个进位)
		if (carry > 0) {
			result.append(String.valueOf(carry));
		}
		// 最后翻转恢复字符串，再返回
		return result.reverse().toString();
	}
}
