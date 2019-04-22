package com.concurrent;

import java.util.stream.LongStream;

public class StreamTest {

	public static void main(String[] args) {
		System.out.println("sum = " + parallelRangedSum(10_000_000));
	}

	public static long parallelRangedSum(long n) {
		return LongStream.rangeClosed(1, n).parallel().reduce(0L, Long::sum);
	}
}
