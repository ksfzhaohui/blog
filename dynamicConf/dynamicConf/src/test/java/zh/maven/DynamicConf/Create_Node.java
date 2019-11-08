package zh.maven.DynamicConf;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class Create_Node {

	static String path = "/a3/m3";
	static CuratorFramework client = CuratorFrameworkFactory.builder()
			.connectString("127.0.0.1:2181").sessionTimeoutMs(5000)
			.retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

	public static void main(String[] args) throws Exception {
		client.start();
		client.create().creatingParentsIfNeeded()
				.withMode(CreateMode.PERSISTENT)
				.forPath(path, "init".getBytes());
	}

}
