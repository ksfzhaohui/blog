package com.java8.c8;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Test54 {

	public static void main(String[] args) {
		List<Integer> numbers = Arrays.asList(10, 10, 9, 8);
		// reduce第一个参数是初始值
		System.out.println(numbers.stream().reduce(0, (a, b) -> a + b));
		System.out.println(numbers.stream().reduce(1, (a, b) -> a * b));

		System.out.println(numbers.stream().reduce((a, b) -> a + b));
		System.out.println(numbers.stream().reduce((a, b) -> a * b));

		// 最大值最小值
		Optional<Integer> min = numbers.stream().reduce(Integer::min);
		Optional<Integer> max = numbers.stream().reduce(Integer::max);
		System.out.println(min);
		System.out.println(max);

		System.out.println(numbers.stream().reduce((x, y) -> x < y ? x : y));
		System.out.println(numbers.stream().reduce((x, y) -> x > y ? x : y));

	}

}
