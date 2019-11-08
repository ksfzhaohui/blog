package zh.maven.DynamicConf;

import redis.clients.jedis.Jedis;

public class RedisPublish {

	public static void main(String[] args) {
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		int i = 0;
		while (true) {
			jedis.publish("/a2/b4/c1" + i, "message_" + System.currentTimeMillis());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
	}
}
