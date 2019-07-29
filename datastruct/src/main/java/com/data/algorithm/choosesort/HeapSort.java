package com.data.algorithm.choosesort;

import java.util.Arrays;

/**
 * 堆排序： 堆排序是对简单选择排序的改进简单选择排序是从n个记录中找出一个最小的记录，需要比较n-1次。
 * 但是这样的操作并没有把每一趟的比较结果保存下来，在后一趟的比较中，有许多比较在前一趟已经做过了，
 * 但由于前一趟排序时未保存这些比较结果，所以后一趟排序时又重复执行了这些比较操作，因而记录的比较次数较多。
 * 
 * 
 * 
 *
 */
public class HeapSort {

	public static void main(String[] args) {
		int arrays[] = { 5, 4, 8, 9, 3, 2, 7 };
		for (int i = 0; i < arrays.length; i++) {

			// 每次建堆就可以排除一个元素了
			maxHeapify(arrays, arrays.length - i);

			// 交换
			int temp = arrays[0];
			arrays[0] = arrays[(arrays.length - 1) - i];
			arrays[(arrays.length - 1) - i] = temp;

		}
		System.out.println(Arrays.toString(arrays));
	}

	/**
	 * 建堆
	 *
	 * @param arrays
	 *            看作是完全二叉树
	 * @param currentRootNode
	 *            当前父节点位置
	 * @param size
	 *            节点总数
	 */
	public static void heapify(int[] arrays, int currentRootNode, int size) {

		if (currentRootNode < size) {
			// 左子树和右字数的位置
			int left = 2 * currentRootNode + 1;
			int right = 2 * currentRootNode + 2;

			// 把当前父节点位置看成是最大的
			int max = currentRootNode;

			if (left < size) {
				// 如果比当前根元素要大，记录它的位置
				if (arrays[max] < arrays[left]) {
					max = left;
				}
			}
			if (right < size) {
				// 如果比当前根元素要大，记录它的位置
				if (arrays[max] < arrays[right]) {
					max = right;
				}
			}
			// 如果最大的不是根元素位置，那么就交换
			if (max != currentRootNode) {
				int temp = arrays[max];
				arrays[max] = arrays[currentRootNode];
				arrays[currentRootNode] = temp;

				// 继续比较，直到完成一次建堆
				heapify(arrays, max, size);
			}
		}
	}

	/**
	 * 完成一次建堆，最大值在堆的顶部(根节点)
	 */
	public static void maxHeapify(int[] arrays, int size) {

		// 从数组的尾部开始，直到第一个元素(角标为0)
		for (int i = size - 1; i >= 0; i--) {
			heapify(arrays, i, size);
		}

	}

}
