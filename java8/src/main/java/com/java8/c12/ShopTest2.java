package com.java8.c12;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * 并行--使用流还是CompletableFuture
 * 
 * 1.如果使用的是计算密集型，并没有I/O，那么推荐使用Stream接口
 * 2.反之如果并行的任务涉及等待I/O操作，那么使用CompletableFuture更加灵活
 * 
 * @author hui.zhao.cfs
 *
 */
public class ShopTest2 {

	static List<Shop> shops = Arrays.asList(new Shop("BestPrice"), new Shop("LetsSaveBig"), new Shop("MyFavoriteShop"),
			new Shop("BuyItAll"), new Shop("taobao"), new Shop("jd"));

	// 因为是等待型任务，所有创建的线程越多越好，当然肯定也不是无限创建
	static Executor executor = Executors.newFixedThreadPool(Math.min(shops.size(), 100), new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});

	public static void main(String[] args) {
		long start = System.nanoTime();
		System.out.println(findPrices("myPhone27S"));
		long duration = (System.nanoTime() - start) / 1_000_000;
		System.out.println("Done in " + duration + " msecs");

		start = System.nanoTime();
		System.out.println(findPricesParallel("myPhone27S"));
		duration = (System.nanoTime() - start) / 1_000_000;
		System.out.println("Done in " + duration + " msecs");

		start = System.nanoTime();
		System.out.println(findPricesFuture("myPhone27S"));
		duration = (System.nanoTime() - start) / 1_000_000;
		System.out.println("Done in " + duration + " msecs");

		start = System.nanoTime();
		System.out.println(findPricesFutureExcutor("myPhone27S"));
		duration = (System.nanoTime() - start) / 1_000_000;
		System.out.println("Done in " + duration + " msecs");

	}

	public static List<String> findPrices(String product) {
		return shops.stream().map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)))
				.collect(toList());
	}

	public static List<String> findPricesParallel(String product) {
		return shops.parallelStream()
				.map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)))
				.collect(toList());
	}

	public static List<String> findPricesFuture(String product) {
		List<CompletableFuture<String>> priceFutures = shops.stream().map(
				shop -> CompletableFuture.supplyAsync(() -> shop.getName() + " price is " + shop.getPrice(product)))
				.collect(Collectors.toList());
		// join和get类似，唯一不同就是join不会抛出异常
		return priceFutures.stream().map(CompletableFuture::join).collect(toList());
	}

	public static List<String> findPricesFutureExcutor(String product) {
		List<CompletableFuture<String>> priceFutures = shops
				.stream().map(shop -> CompletableFuture
						.supplyAsync(() -> shop.getName() + " price is " + shop.getPrice(product), executor))
				.collect(Collectors.toList());
		return priceFutures.stream().map(CompletableFuture::join).collect(toList());
	}

}
