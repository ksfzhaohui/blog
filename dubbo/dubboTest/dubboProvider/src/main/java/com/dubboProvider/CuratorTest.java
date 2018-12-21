package com.dubboProvider;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

public class CuratorTest {

	static String path = "/dubbo";
	static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
			.sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

	public static void main(String[] args) throws Exception {
		client.start();
		List<String> paths = listChildren(path);
		for (String path : paths) {
			Stat stat = new Stat();
			System.err.println(
					"path:" + path + ",value:" + new String(client.getData().storingStatIn(stat).forPath(path)));
		}
	}

	private static List<String> listChildren(String path) throws Exception {
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

}
