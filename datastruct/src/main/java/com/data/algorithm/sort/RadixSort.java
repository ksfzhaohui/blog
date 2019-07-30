package com.data.algorithm.sort;

import java.util.Arrays;

/**
 * 
 * 基数排序:
 * 将所有待比较数值（正整数）统一为同样的数位长度，数位较短的数前面补零。然后，从最低位开始，依次进行一次排序。这样从最低位排序一直到最高位排序完成以后,数列就变成一个有序序列。
 * 对于基数排序有两种方法：
 * 最高位优先法（MSD）(Most Significant Digit first)
 * 最低位优先法（LSD）(Least Significant Digit first)
 * @author hui.zhao.cfs
 *
 */
public class RadixSort {

	public static void main(String[] args) {
		Integer[] array = new Integer[] { 1200, 292, 121, 72, 233, 44, 12 };
		radixSort(array, 10, 4);
		System.out.println(Arrays.toString(array));
	}

	/*
	 * 8.基数排序 稳定的排序算法 array 代表数组 radix 代表基数 d 代表排序元素的位数
	 */
	public static void radixSort(Integer[] array, int radix, int d) {
		// 临时数组
		Integer[] tempArray = new Integer[array.length];
		// count用于记录待排序元素的信息,用来表示该位是i的数的个数
		Integer[] count = new Integer[radix];

		int rate = 1;
		for (int i = 0; i < d; i++) {
			// 重置count数组，开始统计下一个关键字
			Arrays.fill(count, 0);
			// 将array中的元素完全复制到tempArray数组中
			System.arraycopy(array, 0, tempArray, 0, array.length);

			// 计算每个待排序数据的子关键字
			for (int j = 0; j < array.length; j++) {
				int subKey = (tempArray[j] / rate) % radix;
				count[subKey]++;
			}
			// 统计count数组的前j位（包含j）共有多少个数
			for (int j = 1; j < radix; j++) {
				count[j] = count[j] + count[j - 1];
			}
			// 按子关键字对指定的数据进行排序 ，因为开始是从前往后放，现在从后忘前读取，保证基数排序的稳定性
			for (int m = array.length - 1; m >= 0; m--) {
				int subKey = (tempArray[m] / rate) % radix;
				array[--count[subKey]] = tempArray[m]; // 插入到第--count[subKey]位，因为数组下标从0开始
			}
			rate *= radix;// 前进一位
			System.out.print("第" + (i + 1) + "次：");
			System.out.println(Arrays.toString(array));
		}
	}
}
