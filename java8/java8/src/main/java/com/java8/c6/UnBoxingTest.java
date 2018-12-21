package com.java8.c6;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class UnBoxingTest {

	public static void main(String[] args) {
		IntPredicate evenNumbers = (int i) -> i % 2 == 0;
		// 无装箱
		System.out.println(evenNumbers.test(1000));

		// 有装箱
		Predicate<Integer> oddNumbers = (Integer i) -> i % 2 == 1;
		System.out.println(oddNumbers.test(1000));

	}
}
