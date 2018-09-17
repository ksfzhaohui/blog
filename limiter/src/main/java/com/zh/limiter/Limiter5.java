package com.zh.limiter;

import java.io.IOException;

import com.zh.limiter.impl.RedisLimiter;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 分布式限流：限制某个接口的总并发/请求数
 *
 */
public class Limiter5 {

	public static void main(String[] args) throws IOException, InterruptedException {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(10);
		JedisPool pool = new JedisPool(jedisPoolConfig, "localhost");

		final RedisLimiter limiter = new RedisLimiter(pool, "10");

		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					if (limiter.acquire()) {
						System.out.println(Thread.currentThread().getName() + "成功获取");
					} else {
						System.out.println(Thread.currentThread().getName() + "超过最大限制数");
					}
				}
			}).start();
		}

		Thread.sleep(20000);
	}
}
