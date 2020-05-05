package com.data.algorithm.leetcode.动态规划;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目：给你 k 种面值的硬币，面值分别为 c1, c2 ... ck，再给一个总金额 n，问你最少需要几枚硬币凑出这个金额，如果不可能凑出，则回答 -1
 * 。 比如说，k = 3，面值分别为 1，2，5，总金额 n = 11，那么最少需要 3 枚硬币，即 11 = 5 + 5 + 1 。下面走流程。
 * 
 * @author hui.zhao.cfs
 *
 */
public class 凑零钱 {

	private static Map<Integer, Integer> minMap = new HashMap<Integer, Integer>();

	private static int coinChange2(List<Integer> coinList, int amount) {
		if (amount == 0)
			return 0;
		if(minMap.containsKey(amount)){
			return minMap.get(amount);
		}
		int min = Integer.MAX_VALUE;
		for (int coin : coinList) {
			if (amount < coin)
				continue;
			int subProb = coinChange2(coinList, amount - coin);
			if (subProb + 1 < min) {
				min = subProb + 1;
			}
		}
		minMap.put(amount, min);
		return min;
	}
	
	private static int coinChange1(List<Integer> coinList, int amount) {
		if (amount == 0)
			return 0;
		int min = Integer.MAX_VALUE;
		for (int coin : coinList) {
			if (amount < coin)
				continue;
			int subProb = coinChange1(coinList, amount - coin);
			if (subProb + 1 < min) {
				min = subProb + 1;
			}
		}
		return min;
	}

	public static void main(String[] args) {
		List<Integer> coinList = new ArrayList<Integer>();
		coinList.add(1);
		coinList.add(2);
		coinList.add(5);

		System.out.println(coinChange1(coinList, 55));
		System.out.println(coinChange2(coinList, 55));
	}
}
