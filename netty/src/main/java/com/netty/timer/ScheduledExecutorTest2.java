package com.netty.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorTest2 {

	public static void main(String[] args) {

		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread() + " === " + System.currentTimeMillis() + " === task1");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, 1000, 1000, TimeUnit.MILLISECONDS);

//		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//			@Override
//			public void run() {
//				System.out.println(Thread.currentThread() + " === " + System.currentTimeMillis() + " === task2");
//
//			}
//		}, 1000, 1000, TimeUnit.MILLISECONDS);

	}

}
