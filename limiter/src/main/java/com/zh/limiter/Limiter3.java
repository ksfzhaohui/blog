package com.zh.limiter;

import com.google.common.util.concurrent.RateLimiter;

/**
 * 平滑限流：令牌桶算法
 *
 */
public class Limiter3 {

	public static void main(String[] args) throws InterruptedException {
		RateLimiter limiter = RateLimiter.create(5);
		System.out.println(limiter.acquire(50));
		System.out.println(limiter.acquire());
		System.out.println(limiter.acquire());
		System.out.println(limiter.acquire());
	}
}
