package com.data.algorithm.heap;

import java.util.Arrays;

/**
 * 最小堆
 * @author hui.zhao.cfs
 *
 */
public class MinHeap {

	public static void main(String[] args) {
		int[] arr = { 50, 10, 90, 30, 70, 40, 80, 60, 20 };
		heapSort(arr);
		System.out.println(Arrays.toString(arr));
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

			// 如果左节点大于右节点，则将要交换的位置指向右节点
			if (arr[pos] > arr[pos + 1]) {
				pos++;
			}

			if (father > arr[pos]) {
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
