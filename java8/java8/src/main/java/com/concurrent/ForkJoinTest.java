package com.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.LongStream;

public class ForkJoinTest extends java.util.concurrent.RecursiveTask<Long> {
	
	private static final long serialVersionUID = 1L;
	private final long[] numbers;
	private final int start;
	private final int end;
	public static final long THRESHOLD = 10_000;

	public ForkJoinTest(long[] numbers) {
		this(numbers, 0, numbers.length);
	}

	private ForkJoinTest(long[] numbers, int start, int end) {
		this.numbers = numbers;
		this.start = start;
		this.end = end;
	}

	@Override
	protected Long compute() {
		int length = end - start;
		if (length <= THRESHOLD) {
			return computeSequentially();
		}
		ForkJoinTest leftTask = new ForkJoinTest(numbers, start, start + length / 2);
		leftTask.fork();
		ForkJoinTest rightTask = new ForkJoinTest(numbers, start + length / 2, end);
		Long rightResult = rightTask.compute();
		// 注：join方法会阻塞，因此有必要在两个子任务的计算都开始之后才执行join方法
		Long leftResult = leftTask.join();
		return leftResult + rightResult;
	}

	private long computeSequentially() {
		long sum = 0;
		for (int i = start; i < end; i++) {
			sum += numbers[i];
		}
		return sum;
	}

	public static void main(String[] args) {
		System.out.println(forkJoinSum(10_000_000));
	}

	public static long forkJoinSum(long n) {
		long[] numbers = LongStream.rangeClosed(1, n).toArray();
		ForkJoinTask<Long> task = new ForkJoinTest(numbers);
		return new ForkJoinPool().invoke(task);
	}
}
