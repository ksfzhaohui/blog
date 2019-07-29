package com.data.algorithm.swapsort;

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
		int temp;
		for (int i = 0; i < len; i++) {
			for (int j = i + 1; j < len; j++) {
				if (src[i] > src[j]) {
					temp = src[i];
					src[i] = src[j];
					src[j] = temp;
				}
			}
		}
		return src;
	}

	public static void main(String[] args) {
		int src[] = { 4, 5, 2, 1, 9, 0, 3 };
		System.out.println(Arrays.toString(src));
	}
}
