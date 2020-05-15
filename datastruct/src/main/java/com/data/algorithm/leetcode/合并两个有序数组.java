package com.data.algorithm.leetcode;

import java.util.Arrays;

public class 合并两个有序数组 {

	public static int[] merge(int a1[], int a2[]) {
		if (a1 == null && a2 == null) {
			return null;
		}
		if (a1 == null) {
			return a2;
		}
		if (a2 == null) {
			return a1;
		}

		int len = a1.length + a2.length;
		int all[] = new int[len];

		int index = 0, index1 = 0, index2 = 0;
		while (index1 <= a1.length - 1 && index2 <= a2.length - 1) {
			if (a1[index1] < a2[index2]) {
				all[index++] = a1[index1];
				index1++;
			} else {
				all[index++] = a2[index2];
				index2++;
			}
		}
		if (index2 > a2.length - 1) {
			for (int k = index1; k <= a1.length - 1; k++) {
				all[index++] = a1[k];
			}
		}
		if (index1 > a1.length - 1) {
			for (int k = index2; k <= a2.length - 1; k++) {
				all[index++] = a2[k];
			}
		}

		return all;
	}

	public static void main(String[] args) {

		int a1[] = { 1, 2, 4, 5 };
		int a2[] = { 3, 4, 7, 8 };

		System.out.println(Arrays.toString(merge(a1, a2)));
	}

}
