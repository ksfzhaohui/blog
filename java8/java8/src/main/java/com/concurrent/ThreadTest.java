package com.concurrent;

import java.util.stream.LongStream;

public class ThreadTest {

	public static final int THRESHOLD = 10_000;
	public static long[] numbers;
	private static long allSum;

	public static void main(String[] args) throws Exception {
		numbers = LongStream.rangeClosed(1, 10_000_000).toArray();
		int taskSize = (int) (numbers.length / THRESHOLD);
		for (int i = 1; i <= taskSize; i++) {
			final int key = i;
			new Thread(new Runnable() {
				public void run() {
					sumAll(sum((key - 1) * THRESHOLD, key * THRESHOLD));
				}
			}).start();
		}
		Thread.sleep(100);
		System.out.println("allSum = " + getAllSum());
	}

	private static synchronized long sumAll(long threadSum) {
		return allSum += threadSum;
	}

	public static synchronized long getAllSum() {
		return allSum;
	}

	private static long sum(int start, int end) {
		long sum = 0;
		for (int i = start; i < end; i++) {
			sum += numbers[i];
		}
		return sum;
	}
}
