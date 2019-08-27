package com.data.algorithm.bloom;

import java.util.HashSet;
import java.util.Set;

public class BloomFilterTest {

	public static void main(String[] args) {
//		hashSetTest();
		bloomFilterTest();
	}

	public static void hashSetTest() {
		long star = System.currentTimeMillis();

		Set<Integer> hashset = new HashSet<>(100000000);
		for (int i = 0; i < 100000000; i++) {
			hashset.add(i);
		}
		System.out.println(hashset.contains(1));
		System.out.println(hashset.contains(2));

		long end = System.currentTimeMillis();
		System.out.println("执行时间：" + (end - star));
	}

	public static void bloomFilterTest() {
		long star = System.currentTimeMillis();
		BloomFilters bloomFilters = new BloomFilters(100000000);
		for (int i = 0; i < 100000000; i++) {
			bloomFilters.add(i + "");
		}
		System.out.println(bloomFilters.check(1 + ""));
		System.out.println(bloomFilters.check(2 + ""));
		System.out.println(bloomFilters.check(3 + ""));
		System.out.println(bloomFilters.check(999999 + ""));
		System.out.println(bloomFilters.check(400230340 + ""));
		long end = System.currentTimeMillis();
		System.out.println("执行时间：" + (end - star));
	}
}
