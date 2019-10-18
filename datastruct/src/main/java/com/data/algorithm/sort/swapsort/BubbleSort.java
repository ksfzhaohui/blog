package com.data.algorithm.sort.swapsort;

import java.util.Arrays;

/**
 * 冒泡排序法:
 * 
 * 一次比较两个元素，如果它们的顺序错误就把它们交换过来
 * 
 * @author Administrator
 * 
 */
public class BubbleSort {

	/**
	 * 冒泡排序法
	 * 
	 * @param src
	 *            待排序数组
	 * @return
	 */
	public static int[] doBubbleSort(int[] src) {
		int len = src.length;
		for (int i = 0; i < len - 1; i++) {
			for (int j = 0; j < len - 1 - i; j++) {
				if (src[j] > src[j + 1]) { // 相邻元素两两对比
					int temp = src[j + 1]; // 元素交换
					src[j + 1] = src[j];
					src[j] = temp;
				}
			}
		}
		return src;
	}

	public static void main(String[] args) {
		int src[] = { 4, 5, 2, 1, 9, 0, 3 };
		doBubbleSort(src);
		System.out.println(Arrays.toString(src));
	}
}
