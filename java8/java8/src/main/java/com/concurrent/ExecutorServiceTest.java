package com.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

public class ExecutorServiceTest {

	public static final int THRESHOLD = 10_000;
	public static long[] numbers;

	public static void main(String[] args) throws Exception {
		numbers = LongStream.rangeClosed(1, 10_000_000).toArray();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
		CompletionService<Long> completionService = new ExecutorCompletionService<Long>(executor);
		int taskSize = (int) (numbers.length / THRESHOLD);
		for (int i = 1; i <= taskSize; i++) {
			final int key = i;
			completionService.submit(new Callable<Long>() {

				@Override
				public Long call() throws Exception {
					return sum((key - 1) * THRESHOLD, key * THRESHOLD);
				}
			});
		}
		long sumValue = 0;
		for (int i = 0; i < taskSize; i++) {
			sumValue += completionService.take().get();
		}
		// 所有任务已经完成,关闭线程池
		System.out.println("sumValue = " + sumValue);
		executor.shutdown();
	}

	private static long sum(int start, int end) {
		long sum = 0;
		for (int i = start; i < end; i++) {
			sum += numbers[i];
		}
		return sum;
	}
}
