package com.data.algorithm.search;

/**
 * 顺序查找适合于存储结构为顺序存储或链接存储的线性表
 * 
 * @author hui.zhao.cfs
 *
 */
public class SequenceSearch {

	public static void main(String[] args) {
		int src[] = { 4, 5, 2, 1, 9, 0, 3 };
		System.out.println(search(src, 3));
	}

	private static int search(int array[], int v) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == v) {
				return i;
			}
		}
		return -1;
	}

}
