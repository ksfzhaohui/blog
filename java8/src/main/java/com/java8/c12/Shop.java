package com.java8.c12;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.java8.c12.discount.Discount;

public class Shop {

	private String name;

	public Shop(String name) {
		this.name = name;
	}

	public double getPrice(String product) {
		return calculatePrice(product);
	}

	public String getPrice2(String product) {
		double price = calculatePrice(product);
		Discount.Code code = Discount.Code.values()[new Random().nextInt(Discount.Code.values().length)];
		return String.format("%s:%.2f:%s", name, price, code);
	}

	public Future<Double> getPriceAsync(String product) {
		CompletableFuture<Double> futurePrice = new CompletableFuture<>();
		new Thread(() -> {
			try {
				double price = calculatePrice(product);
				futurePrice.complete(price);
			} catch (Exception ex) {
				// 错误处理 --不处理get的地方将一直堵塞
				futurePrice.completeExceptionally(ex);
			}
		}).start();
		return futurePrice;
	}

	public Future<Double> getPriceAsync2(String product) {
		return CompletableFuture.supplyAsync(() -> calculatePrice(product));
	}

	private double calculatePrice(String product) {
		delay();
		// String error = null;
		// error.length();
		return new Random().nextDouble() * product.charAt(0) + product.charAt(1);
	}

	public static void delay() {
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
