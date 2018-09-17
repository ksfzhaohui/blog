package com.zh.limiter;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;

/**
 * 平滑限流：漏桶算法
 *
 */
public class Limiter4 {

	public static void main(String[] args) throws InterruptedException {
		RateLimiter limiter = RateLimiter.create(5, 1000, TimeUnit.MILLISECONDS);
		for (int i = 0; i < 5; i++) {
			System.out.println(limiter.acquire());
		}

		Thread.sleep(1000);
		for (int i = 0; i < 20; i++) {
			System.out.println(limiter.acquire());
		}
	}
}
