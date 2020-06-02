package com.zh.javasdk.aqs;

import java.util.concurrent.CountDownLatch;

/**
 * 它可以实现类似计数器的功能。比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行
 * 
 * @author hui.zhao.cfs
 *
 */
public class CountDownLatchTest {

	public static void main(String[] args) {
		final CountDownLatch latch = new CountDownLatch(2);

		new Thread() {
			public void run() {
				try {
					System.out.println("子线程" + Thread.currentThread().getName() + "正在执行");
					Thread.sleep(3000);
					System.out.println("子线程" + Thread.currentThread().getName() + "执行完毕");
					latch.countDown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();

		new Thread() {
			public void run() {
				try {
					System.out.println("子线程" + Thread.currentThread().getName() + "正在执行");
					Thread.sleep(3000);
					System.out.println("子线程" + Thread.currentThread().getName() + "执行完毕");
					latch.countDown();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();

		try {
			System.out.println("等待2个子线程执行完毕...");
			latch.await();
			System.out.println("2个子线程已经执行完毕--->继续执行主线程");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
