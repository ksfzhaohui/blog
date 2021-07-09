package com.data.algorithm.leetcode;

public class 超过一半的数 {

	public static void main(String[] args) {
		fun1();
	}

	/**
	 * 数组中有一个数字出现的次数超过数组长度的一半，也就相当于大于其他所有数字出现的次数和。 我们可以用数组来遍历，我们需要保存两个值，一个是数字，一个是次数。
	 * 我们来分析,当下一个数字和上一个数字相同，那么次数+1；如果下一个数字和上一个数字不同，次数-1；如果次数为0，我们保存下一个数字，并把次数置为1。
	 * 那么超过一半的次数的数字，一定是最后一次把次数设置为1的数字。
	 */
	public static void fun1() {
		int nums[] = { 3, 3, 2, 4, 3, 3, 2, 3, 4, 3, 5, 3, 6, 3, 3 };
		int num = nums[0], times = 1;
		int len = nums.length;
		for (int i = 1; i < len; i++) {
			if (times == 0) {
				num = nums[i];
				times = 1;
			}

			if (num == nums[i]) {
				times++;
			} else {
				times--;
			}
		}

		System.out.println(num);
	}

	/**
	 * 如果某数出现的次数大于数组长度的一半，那么若我们将数组中所有数字排序(快排)的话，中间的数肯定就是要求的数;
	 */
	public static void fun2() {

	}
}
