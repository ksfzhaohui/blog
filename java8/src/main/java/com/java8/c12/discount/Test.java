package com.java8.c12.discount;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import com.java8.c12.Shop;
import static java.util.stream.Collectors.*;

public class Test {

	static List<Shop> shops = Arrays.asList(new Shop("BestPrice"), new Shop("LetsSaveBig"), new Shop("MyFavoriteShop"),
			new Shop("BuyItAll"));

	static Executor executor = Executors.newFixedThreadPool(Math.min(shops.size(), 100), new ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
	});

	public static void main(String[] args) {
		long start = System.nanoTime();
		System.out.println(findPrices2("myPhone27S"));
		long duration = (System.nanoTime() - start) / 1_000_000;
		System.out.println("Done in " + duration + " msecs");
	}

	public static List<String> findPrices(String product) {
		return shops.stream().map(shop -> shop.getPrice2(product)).map(Quote::parse).map(Discount::applyDiscount)
				.collect(toList());
	}

	public static List<String> findPrices2(String product) {
		List<CompletableFuture<String>> priceFutures = shops.stream()
				.map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice2(product), executor))
				.map(future -> future.thenApply(Quote::parse))
				//thenCompose允许你对两个异步操作进行流水线，第一个操作完成时，将其结果作为参数传递给第二个操作
				.map(future -> future.thenCompose(
						quote -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote), executor)))
				.collect(toList());
		return priceFutures.stream().map(CompletableFuture::join).collect(toList());
	}

}
