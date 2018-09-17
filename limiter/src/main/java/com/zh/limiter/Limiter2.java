package com.zh.limiter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 限制时间窗口请求数
 *
 */
public class Limiter2 {

	private static int concurrent_number = 10;

	private static LoadingCache<Long, AtomicLong> counter;

	static {
		counter = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS)
				.build(new CacheLoader<Long, AtomicLong>() {

					@Override
					public AtomicLong load(Long key) throws Exception {
						return new AtomicLong(0);
					}
				});
	}

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		while (true) {
			long currentSeconds = System.currentTimeMillis() / 1000;
			if (counter.get(currentSeconds).incrementAndGet() > concurrent_number) {
				System.err.println("时间窗口请求数超过上限,currentSeconds=" + currentSeconds);
				continue;
			}
			// 处理业务
			System.out.println("处理业务中");
			Thread.sleep(50);
		}
	}
}
