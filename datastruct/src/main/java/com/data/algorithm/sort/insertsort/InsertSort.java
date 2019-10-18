package com.data.algorithm.sort.insertsort;

import java.util.Arrays;

/**
 * 
 * 直接插入排序:
 * 
 * 从第一个元素开始，该元素可以认为已经被排序；取出下一个元素，在已经排序的元素序列中从后向前扫描；
 * 如果该元素（已排序）大于新元素，将该元素移到下一位置；
 * 重复步骤3，直到找到已排序的元素小于或者等于新元素的位置；
 * 将新元素插入到该位置后；重复步骤2~5。
 * @author hui.zhao.cfs
 *
 */
public class InsertSort {

	/**
	 * 
	 * @param src
	 *            待排序数组
	 * @return
	 */
	public static int[] doInsertSort(int[] src) {
		int len = src.length;
		int temp;
		int j;
		for (int i = 1; i < len; i++) {
			temp = src[i];
			for (j = i; j > 0; j--) {
				if (src[j - 1] > temp) {
					src[j] = src[j - 1];
				} else {// 如果当前的数，不小前面的数，那就说明不小于前面所有的数，因为前面已经是排好了序的，所以直接通出当前一轮的比较
					break;
				}
			}
			src[j] = temp;
		}
		return src;
	}

	public static void main(String[] args) {
		int src[] = { 4, 5, 2, 1, 9, 0, 3 };
		doInsertSort(src);
		System.out.println(Arrays.toString(src));
	}
}
