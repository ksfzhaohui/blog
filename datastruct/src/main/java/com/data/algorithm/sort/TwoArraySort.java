package com.data.algorithm.sort;

import java.util.Arrays;

/**
 * 两个有序数组合并
 * 
 * @author ksfzhaohui
 *
 */
public class TwoArraySort {

	public static void main(String[] args) {
		int[] a1 = { 1, 3, 5, 7, 9 };
		int[] a2 = { 2, 4, 6, 8, 10 };

		int size = a1.length + a2.length;
		int[] all = new int[size];

		int a1Index = 0;
		int a2Index = 0;
		for (int i = 0; i < size; i++) {
			if (a1Index > a1.length - 1) {
				all[i] = a2[a2Index];
				a2Index++;
				continue;
			}
			if (a2Index > a2.length - 1) {
				all[i] = a1[a1Index];
				a1Index++;
				continue;
			}
			if (a1[a1Index] <= a2[a2Index]) {
				all[i] = a1[a1Index];
				a1Index++;
			} else {
				all[i] = a2[a2Index];
				a2Index++;
			}
		}

		System.out.println(Arrays.toString(all));
	}
}
