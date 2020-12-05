package org.dubboRoute;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistry;
import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistryFactory;
import com.alibaba.dubbo.remoting.zookeeper.curator.CuratorZookeeperTransporter;

/**
 * 注册器
 *
 */
public class Registry {
	public static void main(String[] args) {
		URL registryUrl = URL.valueOf("zookeeper://127.0.0.1:2181");
		ZookeeperRegistryFactory zookeeperRegistryFactory = new ZookeeperRegistryFactory();
		zookeeperRegistryFactory.setZookeeperTransporter(new CuratorZookeeperTransporter());
		ZookeeperRegistry zookeeperRegistry = (ZookeeperRegistry) zookeeperRegistryFactory.createRegistry(registryUrl);
		URL routerURL = URL.valueOf("script://0.0.0.0/com.dubboApi.DemoService?category=routers&dynamic=false");
		routerURL = routerURL.addParameter("rule", URL.encode("(..JavaScript脚本..)"));
		zookeeperRegistry.register(routerURL); // 注册
	}
}
