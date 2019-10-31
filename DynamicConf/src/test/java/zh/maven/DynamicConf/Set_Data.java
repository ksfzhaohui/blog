package zh.maven.DynamicConf;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

public class Set_Data {

	static String path = "/a3/m1/v2/t2";
	static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
			.sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

	public static void main(String[] args) throws Exception {
		client.start();
		Stat stat = new Stat();
		System.out.println(stat.getVersion());
		System.out.println("Success set node for :" + path + ",new version:"
				+ client.setData().forPath(path, "codingo_v11".getBytes()).getVersion());
	}
}
