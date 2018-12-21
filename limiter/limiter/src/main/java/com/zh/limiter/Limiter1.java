package com.zh.limiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限制某个接口的总并发/请求数
 *
 */
public class Limiter1 {

	private static int concurrent_number = 10;

	private static AtomicInteger limiter = new AtomicInteger();

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					service();
				}
			}).start();
		}
	}

	private static void service() {
		try {
			if (limiter.incrementAndGet() > concurrent_number) {
				System.err.println(Thread.currentThread().getName() + "超过最大限制数");
				return;
			}
			// 处理业务
			System.out.println(Thread.currentThread().getName() + "处理业务中,并发数=" + limiter.get());
			Thread.sleep(1);
		} catch (InterruptedException e) {
		} finally {
			limiter.decrementAndGet();
		}
	}
}
