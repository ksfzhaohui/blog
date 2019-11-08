package zh.maven.DynamicConf.zk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zh.maven.DynamicConf.AbstractWatcher;

/**
 * 基于zk的参数通知
 * 
 * @author hui.zhao.cfs
 *
 */
public class ZKWatcher extends AbstractWatcher {

	private static Logger logger = LoggerFactory.getLogger(ZKWatcher.class);
	private final String zkAddress = "127.0.0.1:2181";
	private final int timeout = 10000;
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	private CuratorFramework client = null;

	/**
	 * 监听所有keyPatterns
	 * 
	 * @throws Exception
	 */
	@Override
	public void watcherKeys() {
		try {
			connect();
			watcherPaths();
		} catch (Exception e) {
		}
	}

	@Override
	public String getKeyValue(String key) {
		return keyValueMap.get(key);
	}

	/**
	 * 将所有keyValue填充Properties
	 * 
	 * @param result
	 */
	@Override
	public void fillProperties(Properties result) {
		Iterator<String> keyItor = keyValueMap.keySet().iterator();
		while (keyItor.hasNext()) {
			String key = (String) keyItor.next();
			String v = (String) keyValueMap.get(key);
			result.put(key, v != null ? v : "");
		}
	}

	/**
	 * 与zk进行连接
	 * 
	 * @throws InterruptedException
	 */
	private void connect() throws InterruptedException {
		client = CuratorFrameworkFactory.builder().connectString(zkAddress).sessionTimeoutMs(timeout)
				.retryPolicy(new RetryNTimes(5, 5000)).build();
		client.getConnectionStateListenable().addListener(connectionListener);
		client.start();
		countDownLatch.await();
	}

	/**
	 * 监听所有path
	 * 
	 * @throws Exception
	 */
	private void watcherPaths() throws Exception {
		List<String> pathList = new ArrayList<String>();
		for (String key : keyPatterns) {
			pathList.addAll(listChildren(key));
		}

		logger.info("watcher path : " + pathList);

		if (pathList != null && pathList.size() > 0) {
			for (String path : pathList) {
				keyValueMap.put(path, readPath(path));
				watcherPath(path);
			}
		}
	}

	/** zk连接监听器 **/
	private ConnectionStateListener connectionListener = new ConnectionStateListener() {

		@Override
		public void stateChanged(CuratorFramework client, ConnectionState connectionState) {
			if (connectionState == ConnectionState.CONNECTED) {
				logger.info("connected established");
				countDownLatch.countDown();
			} else if (connectionState == ConnectionState.LOST) {
				logger.info("connection lost, waiting for reconection");
				try {
					reconnect();
				} catch (Exception e) {
					logger.error("reconnect error", e);
				}
			}

		}
	};

	/**
	 * 与zk重新连接
	 * 
	 * @throws InterruptedException
	 */
	private void reconnect() throws InterruptedException {
		unregister();
		connect();
	}

	private void unregister() {
		if (client != null) {
			client.close();
			client = null;
		}
	}

	/**
	 * 递归获取指定path下所以子path
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private List<String> listChildren(String path) throws Exception {
		List<String> pathList = new ArrayList<String>();
		pathList.add(path);
		List<String> list = client.getChildren().forPath(path);
		if (list != null && list.size() > 0) {
			for (String cPath : list) {
				String temp = "";
				if ("/".equals(path)) {
					temp = path + cPath;
				} else {
					temp = path + "/" + cPath;
				}
				pathList.addAll(listChildren(temp));
			}
		}
		return pathList;
	}

	/**
	 * 获取指定path对应的value
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private String readPath(String path) throws Exception {
		byte[] buffer = client.getData().forPath(path);
		String value = new String(buffer);
		logger.info("readPath:path = " + path + ",value = " + value);
		return value;
	}

	private void watcherPath(String path) {
		watcherPath(path, true);
	}

	/**
	 * 监听指定的path
	 * 
	 * @param path
	 */
	private void watcherPath(String path, final boolean isInit) {
		PathChildrenCache cache = null;
		try {
			cache = new PathChildrenCache(client, path, true);
			cache.start(StartMode.POST_INITIALIZED_EVENT);
			cache.getListenable().addListener(new PathChildrenCacheListener() {

				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					switch (event.getType()) {
					case CHILD_ADDED:
						logger.info("CHILD_ADDED," + event.getData().getPath());
						if (!isInit) {
							watcherPath(event.getData().getPath());
						}
						keyValueMap.put(event.getData().getPath(), new String(event.getData().getData()));
						break;
					case CHILD_UPDATED:
						logger.info("CHILD_UPDATED," + event.getData().getPath());
						keyValueMap.put(event.getData().getPath(), new String(event.getData().getData()));
						break;
					case CHILD_REMOVED:
						logger.info("CHILD_REMOVED," + event.getData().getPath());
						break;
					default:
						break;
					}
				}
			});
		} catch (Exception e) {
			if (cache != null) {
				try {
					cache.close();
				} catch (IOException e1) {
				}
			}
			logger.error("watch path error", e);
		}
	}

}
