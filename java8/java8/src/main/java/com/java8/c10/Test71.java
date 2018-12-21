package com.java8.c10;

import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Test71 {

	public static void main(String[] args) {
		System.out.println(sequentialSum(100));

		// Stream.iterate方式
		System.out.println("Sequential sum done in:" + measureSumPerf(Test71::sequentialSum, 10_000_000) + " msecs");
		System.out.println("Parallel sum done in: " + measureSumPerf(Test71::parallelSum, 10_000_000) + " msecs");

		// for循环
		System.out.println("Iterative sum done in:" + measureSumPerf(Test71::iterativeSum, 10_000_000) + " msecs");

		// LongStream.rangeClosed方式
		System.out.println("Ranged sum done in:" + measureSumPerf(Test71::rangedSum, 10_000_000) + " msecs");
		System.out.println(
				"Parallel range sum done in:" + measureSumPerf(Test71::parallelRangedSum, 10_000_000) + " msecs");

		// 错误的使用并行
		System.out.println("SideEffect parallel sum done in: "
				+ measureSumPerf(Test71::sideEffectParallelSum, 10_000_000L) + "msecs");
	}

	/**
	 * 顺序执行sequential，more是顺序
	 * 
	 * @param n
	 * @return
	 */
	public static long sequentialSum(long n) {
		return Stream.iterate(1L, i -> i + 1).limit(n).sequential().reduce(0L, Long::sum);
	}

	/**
	 * 并行执行 parallel
	 * 
	 * @param n
	 * @return
	 */
	public static long parallelSum(long n) {
		return Stream.iterate(1L, i -> i + 1).limit(n).parallel().reduce(0L, Long::sum);
	}

	public static long iterativeSum(long n) {
		long result = 0;
		for (long i = 1L; i <= n; i++) {
			result += i;
		}
		return result;
	}

	/**
	 * iterate不易并行，无法有效的把流划分为小块来并行处理
	 * 
	 * @param n
	 * @return
	 */
	public static long rangedSum(long n) {
		return LongStream.rangeClosed(1, n).reduce(0L, Long::sum);
	}

	public static long parallelRangedSum(long n) {
		return LongStream.rangeClosed(1, n).parallel().reduce(0L, Long::sum);
	}

	public static long measureSumPerf(Function<Long, Long> adder, long n) {
		long fastest = Long.MAX_VALUE;
		for (int i = 0; i < 10; i++) {
			long start = System.nanoTime();
			long sum = adder.apply(n);
			long duration = (System.nanoTime() - start) / 1_000_000;
			System.out.println("Result: " + sum);
			if (duration < fastest)
				fastest = duration;
		}
		return fastest;
	}

	public static long sideEffectParallelSum(long n) {
		Accumulator accumulator = new Accumulator();
		LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add);
		return accumulator.total;
	}

}

class Accumulator {
	public long total = 0;

	public void add(long value) {
		total += value;
	}
}