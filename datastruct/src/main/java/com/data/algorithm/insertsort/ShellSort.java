package com.data.algorithm.insertsort;

import java.util.Arrays;

/**
 * 希尔排序
 * 
 * 首先将待排序的元素分为多个子序列，使得每个子序列的元素个数相对较少，
 * 对各个子序列分别进行直接插入排序，待整个待排序序列“基本有序”后，再对所有元素进行一次直接插入排序
 * 
 * 过程： ⑴ 选择一个步长序列t1，t2，…，tk，其中ti>tj（i<j），tk=1； ⑵ 按步长序列个数 k，对待排序元素序列进行 k趟排序；
 * ⑶每趟排序，根据对应的步长ti，将待排序列分割成ti个子序列，分别对各子序列进行直接插入排序
 * 
 * @author hui.zhao.cfs
 *
 */
public class ShellSort {

	public static void main(String[] args) {

		int[] data = new int[] { 5, 3, 6, 2, 1, 9, 4, 8, 7 };
		shellSort(data);
		System.out.println(Arrays.toString(data));

	}

	private static void shellSort(int num[]) {
		int temp;
		// 默认步长为数组长度除以2
		int step = num.length;
		while (true) {
			step = step / 2;
			// 确定分组数
			for (int i = 0; i < step; i++) {
				// 对分组数据进行直接插入排序
				for (int j = i + step; j < num.length; j = j + step) {
					temp = num[j];
					int k;
					for (k = j - step; k >= 0; k = k - step) {
						if (num[k] > temp) {
							num[k + step] = num[k];
						} else {
							break;
						}
					}
					num[k + step] = temp;
				}
			}
			if (step == 1) {
				break;
			}
		}
	}

}
