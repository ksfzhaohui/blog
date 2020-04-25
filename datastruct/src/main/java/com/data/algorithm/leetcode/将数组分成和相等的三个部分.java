package com.data.algorithm.leetcode;

/**
 * L1013
 * 给你一个整数数组 A，只有可以将其划分为三个和相等的非空部分时才返回 true，否则返回 false。 形式上，如果可以找出索引 i+1 <
 * j 且满足 (A[0] + A[1] + ... + A[i] == A[i+1] + A[i+2] + ... + A[j-1] == A[j] +
 * A[j-1] + ... + A[A.length - 1]) 就可以将数组三等分。
 * 
 * @author hui.zhao.cfs
 *
 */
public class 将数组分成和相等的三个部分 {

	public boolean canThreePartsEqualSum(int[] A) {

		if (A.length < 3) {
			return false;
		}
		int sum = 0;
		for (int i = 0; i < A.length; i++) {
			sum = sum + A[i];
		}
		if (sum % 3 != 0) {
			return false;
		}
		int average = sum / 3;

		int temp = 0;
		int size = 0;
		for (int i = 0; i < A.length; i++) {
			temp = temp + A[i];
			if (size == 2) {
				size++;
			}
			if (temp == average && size < 2) {
				temp = 0;
				size++;
			}
		}
		if (temp == average && size == 3) {
			return true;
		}
		return false;

	}
}
