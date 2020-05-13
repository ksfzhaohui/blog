package com.data.algorithm.heap;

import java.util.Arrays;

/**
 * 最大堆
 * 
 * @author hui.zhao.cfs
 *
 */
public class MaxHeap {

	public static void main(String[] args) {
		int[] arr = { 50, 10, 90, 30, 70, 40, 80, 60, 20 };
		heapSort(arr);
		System.out.println(Arrays.toString(arr));

		int[] arr2 = { 50, 10, 90, 30, 70, 40, 80, 60, 20 };
		heapSort2(arr2);
		System.out.println(Arrays.toString(arr2));
	}

	private static void heapSort2(int[] arr) {
		for (int i = arr.length / 2; i >= 0; i--) {
			sort2(arr, i);
		}
	}

	private static void sort2(int[] arr, int i) {
		while (leftChild(i) < arr.length) {
			int childPos = leftChild(i);
			// 判断是否有右子树
			if (leftChild(i) + 1 < arr.length) {
				if (arr[leftChild(i)] < arr[leftChild(i) + 1]) {
					childPos = leftChild(i) + 1;
				}
			}

			if (arr[childPos] > arr[i]) {
				int temp = arr[childPos];
				arr[childPos] = arr[i];
				arr[i] = temp;
				i = childPos;
			} else {
				break;
			}
		}
	}

	private static void heapSort(int[] arr) {
		// 从中间位置开始
		for (int i = arr.length / 2; i >= 0; i--) {
			heapAdjust(arr, i, arr.length);
		}
	}

	private static void heapAdjust(int[] arr, int i, int len) {
		// 当前要比较的父元素
		int father = arr[i];
		int pos;
		while (leftChild(i) < len) {
			pos = leftChild(i);

			// 如果左节点小于右节点，则将要交换的位置指向右节点
			if (arr[pos] < arr[pos + 1]) {
				pos++;
			}

			if (father < arr[pos]) {
				arr[i] = arr[pos];
				i = pos;
			} else {
				break;
			}
		}
		arr[i] = father;

	}

	private static int leftChild(int i) {
		return 2 * i + 1;
	}

}
