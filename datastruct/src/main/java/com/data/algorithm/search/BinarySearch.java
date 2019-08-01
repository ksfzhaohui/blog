package com.data.algorithm.search;

/**
 * 二分查找:元素必须是有序的，如果是无序的则要先进行排序操作
 * 
 * @author hui.zhao.cfs
 *
 */
public class BinarySearch {

	public static void main(String[] args) {
		int src[] = { 1, 2, 3, 4, 9, 10, 13 };
		int search_value = 13;
		System.out.println(search1(src, search_value));
		System.out.println(search2(src, search_value, 0, 6));
	}

	/**
	 * 非递归方式
	 * 
	 * @param a
	 * @param value
	 * @return
	 */
	private static int search1(int a[], int value) {
		int low, high, mid;
		low = 0;
		high = a.length - 1;
		while (low <= high) {
			mid = (low + high) / 2;
			if (a[mid] == value)
				return mid;
			if (a[mid] > value)
				high = mid - 1;
			if (a[mid] < value)
				low = mid + 1;
		}
		return -1;
	}

	/**
	 * 递归方式
	 * 
	 * @param a
	 * @param value
	 * @param low
	 * @param high
	 * @return
	 */
	private static int search2(int a[], int value, int low, int high) {
		int mid = low + (high - low) / 2;
		if (a[mid] == value)
			return mid;
		if (a[mid] > value)
			return search2(a, value, low, mid - 1);
		if (a[mid] < value)
			return search2(a, value, mid + 1, high);
		return -1;
	}
}
