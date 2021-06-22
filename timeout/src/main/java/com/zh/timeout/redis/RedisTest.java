package com.zh.timeout.redis;

import redis.clients.jedis.Jedis;

public class RedisTest {
	
	public static void main(String[] args) {
		Jedis jedis = new Jedis("192.168.128.128", 6379,1000);
//		Jedis jedis = new Jedis("10.37.17.100", 6379,5);
		jedis.connect();
		jedis.get("LsqScanArchiveJobImpl_1_13");
	}

}
