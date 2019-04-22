package com.concurrent;

import java.util.stream.LongStream;

public class SingleThread {

	public static long[] numbers;

	public static void main(String[] args) {
		numbers = LongStream.rangeClosed(1, 10_000_000).toArray();
		long sum = 0;
		for (int i = 0; i < numbers.length; i++) {
			sum += numbers[i];
		}
		System.out.println("sum  = " + sum);
	}

}
