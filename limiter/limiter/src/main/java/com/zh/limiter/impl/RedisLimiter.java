package com.zh.limiter.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.collect.Lists;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisLimiter {

	/** 限流大小 **/
	private String limit;
	private JedisPool pool;
	private String script;

	public RedisLimiter(JedisPool pool, String limit) {
		this.pool = pool;
		this.limit = limit;

		script = getScript("limit.lua");
	}

	public boolean acquire() {
		String key = String.valueOf(System.currentTimeMillis() / 1000);
		Jedis jedis = pool.getResource();
		try {
			Object result = jedis.eval(script, Lists.newArrayList(key), Lists.newArrayList(limit));
			if ((Long) result != 0) {
				return true;
			}
		} catch (Exception e) {
			System.err.println(e.getStackTrace());
		} finally {
			jedis.close();
		}
		return false;
	}

	public String getScript(String path) {
		StringBuilder sb = new StringBuilder();

		InputStream stream = RedisLimiter.class.getClassLoader().getResourceAsStream(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));

		try {
			String str = "";
			while ((str = br.readLine()) != null) {
				sb.append(str).append(System.lineSeparator());
			}
		} catch (Exception e) {
			System.err.println(e.getStackTrace());
		}
		return sb.toString();
	}

}
