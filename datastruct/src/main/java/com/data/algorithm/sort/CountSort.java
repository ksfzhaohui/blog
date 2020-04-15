package com.data.algorithm.sort;

import java.util.Arrays;

/**
 * 计数排序:
 * 不是基于比较的排序算法，其核心在于将输入的数据值转化为键存储在额外开辟的数组空间中。 
 * 作为一种线性时间复杂度的排序，计数排序要求输入的数据必须是有确定范围的整数
 * @author hui.zhao.cfs
 *
 */
public class CountSort {

	public static void main(String[] args) throws Exception {
		int[] array = { 9, 8, 7, 6, 5, 4, 3, 12, 6, 1, 0 };
		countSort(array, 12);
		System.out.println(Arrays.toString(array));
	}

	public static void countSort(int[] array, int range) throws Exception {
		if (range <= 0) {
			throw new Exception("range can't be negative or zero.");
		}

		if (array.length <= 1) {
			return;
		}

		//辅助计数数组
		int[] countArray = new int[range + 1];
		 //找出每个数字出现的次数
		for (int i = 0; i < array.length; i++) {
			int value = array[i];
			if (value < 0 || value > range) {
				throw new Exception("array element overflow range.");
			}
			countArray[value] += 1;
		}
		
		for(int i=0;i<countArray.length;i++){
			if(countArray[i]>0){
				for(int k=0;k<countArray[i];k++){
					System.out.print(i+"==");
				}
			}
		}

		//对所有的计数累加
		for (int i = 1; i < countArray.length; i++) {
			countArray[i] += countArray[i - 1];
		}

		int[] temp = new int[array.length];
		for (int i = array.length - 1; i >= 0; i--) {
			int value = array[i];
			int position = countArray[value] - 1;

			temp[position] = value;
			countArray[value] -= 1;
		}

		for (int i = 0; i < array.length; i++) {
			array[i] = temp[i];
		}
	}
}
