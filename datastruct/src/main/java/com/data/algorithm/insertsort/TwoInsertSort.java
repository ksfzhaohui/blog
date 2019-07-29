package com.data.algorithm.insertsort;

import java.util.Arrays;

/**
 * 二分法插入排序，简称二分排序，是在插入第i个元素时，对前面的0～i-1元素进行折半，
 * 先跟他们中间的那个元素比，如果小，则对前半再进行折半，否则对后半进行折半，直到left<right，
 * 然后再把第i个元素前1位与目标位置之间的所有元素后移，再把第i个元素放在目标位置上
 * @author hui.zhao.cfs
 *
 */
public class TwoInsertSort {

	public static int[] twoInsertSort(int[] data) {
		int left, right, num;
		int middle, j;
		for (int i = 1; i < data.length; i++) {
			// 准备
			left = 0;
			right = i - 1;
			num = data[i];
			// 二分法查找插入位置
			while (right >= left) {
				// 指向已排序好的中间位置
				middle = (left + right) / 2;
				if (num < data[middle])
					right = middle - 1;// 插入的元素在右区间
				else
					left = middle + 1; // 插入的元素在左区间
			}
			// 后移排序码大于R[i]的记录
			for (j = i - 1; j >= left; j--) {
				data[j + 1] = data[j];
			}
			// 插入
			data[left] = num;
		}
		return data;
	}

	public static void main(String[] args) {
		int src[] = { 5, 4 ,8,9,3,2,7};
		System.out.println(Arrays.toString(src));
	}

}
