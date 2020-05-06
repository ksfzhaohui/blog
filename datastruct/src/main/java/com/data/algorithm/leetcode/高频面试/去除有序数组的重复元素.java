package com.data.algorithm.leetcode.高频面试;

import java.util.Arrays;

public class 去除有序数组的重复元素 {

	static void removeDuplicates(int[] nums) {
		int slow = 0;
		int fast = 1;

		while (fast < nums.length) {
			if (nums[slow] != nums[fast]) {
				slow++;
				nums[slow] = nums[fast];
			}

			fast++;
		}
	}

	public static void main(String[] args) {
		int nums[] = { 1, 2, 2, 3, 4, 4 };
		removeDuplicates(nums);
		System.out.println(Arrays.toString(nums));
	}

}
