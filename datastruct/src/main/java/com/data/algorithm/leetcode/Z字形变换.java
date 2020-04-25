package com.data.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 * L6
 * 将一个给定字符串根据给定的行数，以从上往下、从左到右进行 Z 字形排列。

比如输入字符串为 "LEETCODEISHIRING" 行数为 3 时，排列如下：

L   C   I   R
E T O E S I I G
E   D   H   N
之后，你的输出需要从左往右逐行读取，产生出一个新的字符串，比如："LCIRETOESIIGEDHN"。

请你实现这个将字符串进行指定行数变换的函数：
 * @author hui.zhao.cfs
 *
 */
public class Z字形变换 {

	public static String convert(String s, int numRows) {
		if (numRows < 2)
			return s;
		List<StringBuilder> rows = new ArrayList<StringBuilder>();
		for (int i = 0; i < numRows; i++)
			rows.add(new StringBuilder());
		int i = 0, flag = -1;
		for (char c : s.toCharArray()) {
			rows.get(i).append(c);
			if (i == 0 || i == numRows - 1)
				flag = -flag;
			i += flag;
		}
		StringBuilder res = new StringBuilder();
		for (StringBuilder row : rows)
			res.append(row);
		return res.toString();
	}
	
	public static void main(String[] args) {
		 System.out.println(convert("LEETCODEISHIRING",4));
	}
}
