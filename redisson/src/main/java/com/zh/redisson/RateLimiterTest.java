package com.zh.redisson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RateLimiterTest {

	public static void main(String[] args) {
		Config config = new Config();
		config.useSingleServer().setAddress("redis://localhost:6379");
		RedissonClient client = Redisson.create(config);

		RRateLimiter rateLimiter = client.getRateLimiter("rate_limiter");
		rateLimiter.trySetRate(RateType.OVERALL, 1, 5, RateIntervalUnit.SECONDS);

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10; i++) {
			executorService.submit(() -> {
				try {
					rateLimiter.acquire();
					System.out.println("时间:" + System.currentTimeMillis() + ",线程" + Thread.currentThread().getId()
							+ "进入数据区：" + System.currentTimeMillis());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}
}
