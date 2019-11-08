package zh.maven.DynamicConf.redis;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import zh.maven.DynamicConf.AbstractWatcher;

public class RedisWatcher extends AbstractWatcher {

	private static Logger LOGGER = LoggerFactory.getLogger(RedisWatcher.class);
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 6379;

	private Jedis jedis;

	@Override
	public void watcherKeys() {
		try {
			connect();
			initKeyValues();
			watcherPaths();
		} catch (Exception e) {
			LOGGER.error("watcherKeys error", e);
		}
	}

	@Override
	public void fillProperties(Properties result) {
		Iterator<String> keyItor = keyValueMap.keySet().iterator();
		while (keyItor.hasNext()) {
			String key = (String) keyItor.next();
			String v = (String) keyValueMap.get(key);
			result.put(key, v != null ? v : "");
		}
	}

	@Override
	public String getKeyValue(String key) {
		return keyValueMap.get(key);
	}

	private void initKeyValues() {
		for (String keyPattern : keyPatterns) {
			Set<String> keys = jedis.keys(keyPattern + "*");
			for (String key : keys) {
				String value = jedis.get(key);
				keyValueMap.put(key, value);
				LOGGER.info("init key = " + key + ",value = " + value);
			}
		}
	}

	private void watcherPaths() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				jedis.psubscribe(new JedisPubSub() {

					@Override
					public void onMessage(String channel, String message) {
						try {
							keyValueMap.put(channel, message);
							LOGGER.info("key = " + channel + ",value = " + message);
						} catch (Exception e) {
							LOGGER.error("onMessage error", e);
						}
					}

					@Override
					public void onPMessage(String arg0, String arg1, String arg2) {
						System.out.println("onPMessage=>" + arg0 + "=" + arg1 + "="
								+ arg2);
					}

					@Override
					public void onPSubscribe(String pattern, int subscribedChannels) {
						LOGGER.info("onPSubscribe=>" + pattern + "=" + subscribedChannels);
					}

					@Override
					public void onPUnsubscribe(String arg0, int arg1) {
					}

					@Override
					public void onSubscribe(String arg0, int arg1) {
					}

					@Override
					public void onUnsubscribe(String arg0, int arg1) {
					}
				}, getSubKeyPatterns());
			}
		}).start();
	}

	/**
	 * 获取订阅的模糊channel
	 * 
	 * @return
	 */
	private String[] getSubKeyPatterns() {
		String[] subKeyPatterns = new String[keyPatterns.length];
		for (int i = 0; i < keyPatterns.length; i++) {
			subKeyPatterns[i] = keyPatterns[i] + "*";
		}
		return subKeyPatterns;
	}

	private void connect() {
		jedis = new Jedis(HOST, PORT);
	}

}
