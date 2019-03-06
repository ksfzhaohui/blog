package com.rocketmq.longpolling;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DataCenter {

	private static Random random = new Random();
	private static BlockingQueue<String> queue = new LinkedBlockingQueue<>();
	private static AtomicInteger num = new AtomicInteger();

	public static void start() {
		while (true) {
			try {
				Thread.sleep(random.nextInt(5) * 1000);
				String data = "hello world" + num.incrementAndGet();
				queue.put(data);
				System.out.println("store data:" + data);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getData() throws InterruptedException {
		return queue.take();
	}

}
