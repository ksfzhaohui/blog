**系列文章**

1.  [Zookeeper实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1503392)
2.  [JMS实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1510101)
3.  [Redis实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1526063)

**前言**  
应用项目中都会有一些参数，一般的做法通常可以选择将其存储在本地配置文件或者内存变量中；对于集群机器规模不大、配置变更不是特别频繁的情况下，这两种方式都能很好的解决；但是一旦集群机器规模变大，且配置信息越来越频繁，依靠这两种方式就越来越困难；我们希望能够快速的做到全局参数的变更，因此需要一种参数的集中式管理，下面利用Zookeeper的一些特性来实现简单的参数管理。

**准备**

```
jdk:1.7.0_80
zookeeper:3.4.3
curator:2.6.0
spring:3.1.2
```

**Maven引入**

```
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>3.1.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>3.1.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-beans</artifactId>
    <version>3.1.2.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.4.3</version>
    <exclusions>
        <exclusion>
            <groupId>com.sun.jmx</groupId>
            <artifactId>jmxri</artifactId>
        </exclusion>
        <exclusion>
            <groupId>com.sun.jdmk</groupId>
            <artifactId>jmxtools</artifactId>
        </exclusion>
        <exclusion>
            <groupId>javax.jms</groupId>
            <artifactId>jms</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>2.6.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>2.6.0</version>
</dependency>
```

**目标**  
1.可以同时配置监听多个节点如/app1,/app2；  
2.希望只需要配置如/app1，就能够监听其子节点如/app1/modual1以及子节点的子节点如/app1/modual1/xxx/…；  
3.服务器启动能获取当前指定父节点下的所有子节点数据；  
4.在添加节点或者在更新节点数据的时候能够动态通知，这样代码中就能够实时获取最新的数据；  
5.spring配置中可以从Zookeeper中读取参数进行初始化。

**实现**  
提供ZKWatcher类主要用来和Zookeeper建立连接，监听节点，初始化节点数据，更新节点数据，存储节点数据等

1.同时配置监听多个节点  
提供一个字符串数组给用户用来添加需要监听的节点：

```
private String[] keyPatterns;
```

2.能够监听其子节点以及子节点的子节点  
使用递归的方式用来获取指定监听节点的子节点：

```
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
```

3.服务器启动初始化节点数据  
上面已经递归获取了所有的节点，所有可以遍历获取所有节点数据，并且存储在Map中：

```
private Map<String, String> keyValueMap = new ConcurrentHashMap<String, String>();
 
if (pathList != null && pathList.size() > 0) {
    for (String path : pathList) {
        keyValueMap.put(path, readPath(path));
        watcherPath(path);
    }
}
 
private String readPath(String path) throws Exception {
    byte[] buffer = client.getData().forPath(path);
    String value = new String(buffer);
    logger.info("readPath:path = " + path + ",value = " + value);
    return value;
}
```

4.监听节点数据的变更  
使用PathChildrenCache用来监听子节点的CHILD\_ADDED，CHILD\_UPDATED，CHILD_REMOVED事件：

```
private void watcherPath(String path) {
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
                    watcherPath(event.getData().getPath());
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
```

5.spring配置中可以从Zookeeper中读取参数进行初始化  
实现自定义的PropertyPlaceholderConfigurer类ZKPropPlaceholderConfigurer：

```
public class ZKPropPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
 
    private ZKWatcher zkwatcher;
 
    @Override
    protected Properties mergeProperties() throws IOException {
        return loadPropFromZK(super.mergeProperties());
    }
 
    /**
     * 从zk中加载配置的常量
     * 
     * @param result
     * @return
     */
    private Properties loadPropFromZK(Properties result) {
        zkwatcher.watcherKeys();
        zkwatcher.fillProperties(result);
        return result;
    }
    ......
}
```

通过以上的处理，可以使用如下简单的配置来达到目标：

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"
    xmlns:tx="http://www.springframework.org/schema/tx"
    xsi:schemaLocation="http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.0.xsd
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
        http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-3.0.xsd">
 
    <bean id="zkwatcher" class="zh.maven.DynamicConf.ZKWatcher">
        <property name="keyPatterns" value="/a2,/a3/m1" />
    </bean>
 
    <bean id="propertyConfigurer" class="zh.maven.DynamicConf.ZKPropPlaceholderConfigurer">
        <property name="zkwatcher" ref="zkwatcher"></property>
    </bean>
 
    <bean id="person" class="zh.maven.DynamicConf.Person">
        <property name="name">
            <value>${/a2/m1}</value>
        </property>
        <property name="address">
            <value>${/a3/m1/v2}</value>
        </property>
        <property name="company">
            <value>${/a3/m1/v2/t2}</value>
        </property>
    </bean>
</beans>
```

详细代码svn地址：[http://code.taobao.org/svn/temp-pj/DynamicConf](http://code.taobao.org/svn/temp-pj/DynamicConf)

**测试**  
1.首先启动Zookeeper

```
zkServer.cmd
```

2.初始化需要使用的节点

```
public class Create_Node {
 
    static String path = "/a3/m1/v2/t2";
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
```

创建需要的节点方便ZKWatcher来监听，这里根据以上的配置，分别初始化/a3/m1/v2/t2和/a2/m1/v1/t1

3.启动Main，分别验证配置文件中的初始化以及代码动态获取参数

```
public class Main {
 
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring-config.xml" });
        Person person = (Person) context.getBean("person");
        System.out.println(person.toString());
 
        ZKWatcher zkwatcher = (ZKWatcher) context.getBean("zkwatcher");
        while (true) {
            Person p = new Person(zkwatcher.getKeyValue("/a2/m1"), zkwatcher.getKeyValue("/a3/m1/v2"),
                    zkwatcher.getKeyValue("/a3/m1/v2/t2"));
            System.out.println(p.toString());
 
            Thread.sleep(1000);
        }
    }
}
```

4.观察日志同时更新参数：

```
public class Set_Data {
 
    static String path = "/a3/m1/v2/t2";
    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
 
    public static void main(String[] args) throws Exception {
        client.start();
        Stat stat = new Stat();
        System.out.println(stat.getVersion());
        System.out.println("Success set node for :" + path + ",new version:"
                + client.setData().forPath(path, "codingo_v2".getBytes()).getVersion());
    }
}
```

部分日志如下：

```
2017-08-05 18:04:57 - watcher path : [/a2, /a2/m1, /a2/m1/v1, /a2/m1/v1/t2, /a3/m1, /a3/m1/v2, /a3/m1/v2/t2]
2017-08-05 18:04:57 - readPath:path = /a2,value = 
2017-08-05 18:04:57 - readPath:path = /a2/m1,value = zhaohui
2017-08-05 18:04:57 - readPath:path = /a2/m1/v1,value = 
2017-08-05 18:04:57 - readPath:path = /a2/m1/v1/t2,value = init
2017-08-05 18:04:57 - readPath:path = /a3/m1,value = 
2017-08-05 18:04:57 - readPath:path = /a3/m1/v2,value = nanjing
2017-08-05 18:04:57 - readPath:path = /a3/m1/v2/t2,value = codingo_v10
2017-08-05 18:04:57 - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@182f4aea: defining beans [zkwatcher,propertyConfigurer,person]; root of factory hierarchy
name = zhaohui,address = nanjing,company = codingo_v10
name = zhaohui,address = nanjing,company = codingo_v10
2017-08-05 18:04:57 - CHILD_ADDED,/a2/m1
2017-08-05 18:04:57 - CHILD_ADDED,/a3/m1/v2
2017-08-05 18:04:57 - CHILD_ADDED,/a2/m1/v1
2017-08-05 18:04:57 - CHILD_ADDED,/a2/m1/v1/t2
2017-08-05 18:04:57 - CHILD_ADDED,/a3/m1/v2/t2
name = zhaohui,address = nanjing,company = codingo_v10
name = zhaohui,address = nanjing,company = codingo_v10
name = zhaohui,address = nanjing,company = codingo_v10
2017-08-05 18:05:04 - CHILD_UPDATED,/a3/m1/v2/t2
name = zhaohui,address = nanjing,company = codingo_v11
name = zhaohui,address = nanjing,company = codingo_v11
```

**总结**  
通过Zookeeper实现了一个简单的参数化平台，当然想在生产中使用还有很多需要优化的地方，本文在于提供一个思路；当然除了Zookeeper还可以使用MQ，分布式缓存等来实现参数化平台。

**个人博客：[codingo.xyz](http://codingo.xyz/)**