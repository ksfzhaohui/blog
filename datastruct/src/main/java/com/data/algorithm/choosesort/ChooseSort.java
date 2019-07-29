package com.data.algorithm.choosesort;

import java.util.Arrays;

/**
 * 选择排序法
 * 
 * 首先在未排序序列中找到最小（大）元素，存放到排序序列的起始位置，然后，
 * 再从剩余未排序元素中继续寻找最小（大）元素，然后放到已排序序列的末尾
 * @author hui.zhao.cfs
 *
 */
public class ChooseSort {
	/**
	 * 选择排序法
	 * 
	 * @param src
	 *            待排序数组
	 * @return
	 */
	public static int[] doChooseSort(int src[]) {
		int len = src.length;
		int temp;
		int smallestLocation;
		for (int i = 0; i < len; i++) {
			temp = src[i];
			smallestLocation = i;
			for (int j = i + 1; j < len; j++) {
				if (src[j] < temp) {
					temp = src[j];
					smallestLocation = j;
				}
			}
			src[smallestLocation] = src[i];
			src[i] = temp;
		}
		return src;
	}

	public static void main(String[] args) {
		int src[] = { 4, 5, 2, 1, 9, 0, 3 };
		System.out.println(Arrays.toString(src));
	}

}
