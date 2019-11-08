**系列文章**

1.  [Zookeeper实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1503392)
2.  [JMS实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1510101)
3.  [Redis实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1526063)

**前言**  
上一篇文件[JMS实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1510101)中使用JMS作为中间层，利用的JMS的发布订阅功能实现了对参数的集中式管理；同样分布式缓存Redis也提供了类似的发布订阅功能，并且Redis本身提供了缓存和持久化的功能，本文将介绍通过Redis实现简单的参数集中式管理。

**Maven引入**  
Spring相关的jar引入参考上一篇文章

```
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.4.0</version>
</dependency>
```

**目标**  
1.可以同时配置监听多个节点如/app1,/app2；  
2.希望只需要配置如/app1，就能够监听其子节点如/app1/modual1以及子节点的子节点如/app1/modual1/xxx/…；  
3.服务器启动能获取当前指定父节点下的所有子节点数据；  
4.在添加节点或者在更新节点数据的时候能够动态通知，这样代码中就能够实时获取最新的数据；  
5.spring配置中可以从Zookeeper中读取参数进行初始化。

虽然在实现的方式上有点区别，但是最终达成的目标是一致的，同样列出了这5条目标

**实现**  
RedisWatcher主要用来和Redis进行连接，然后对监听的节点进行初始化，模糊订阅需要监听的节点，最后接受数据的变更，更新本地数据，存储数据等。

1.同时配置监听多个节点  
提供一个字符串数组给用户用来添加需要监听的节点：

```
private String[] keyPatterns;
```

2.能够监听其子节点以及子节点的子节点  
使用Redis提供的psubscribe命令，订阅一个或多个符合给定模式的频道，提供了模糊订阅的功能

```
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
```

提供了使用匹配符*的模糊匹配功能，组装带*号的匹配字符串

```
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
```

3.服务器启动初始化节点数据  
通过使用keys命令来获取匹配节点的数据（keys命令可能引发性能问题，根据实际情况使用）

```
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
```

4.监听节点数据的变更  
目标2中通过psubscribe命令，使用模糊订阅来监听数据的变更，onMessage用来接受变更的数据

5.spring配置中可以从Redis中读取参数进行初始化

```
public class RedisPropPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
 
    private RedisWatcher rediswatcher;
 
    @Override
    protected Properties mergeProperties() throws IOException {
        return loadPropFromRedis(super.mergeProperties());
    }
 
    /**
     * 从Redis中加载配置的常量
     * 
     * @param result
     * @return
     */
    private Properties loadPropFromRedis(Properties result) {
        rediswatcher.watcherKeys();
        rediswatcher.fillProperties(result);
        return result;
    }
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
    <bean id="rediswatcher" class="zh.maven.DynamicConf.redis.RedisWatcher">
        <property name="keyPatterns" value="/a2,/a3" />
    </bean>
    <bean id="propertyConfigurer" class="zh.maven.DynamicConf.redis.RedisPropPlaceholderConfigurer">
        <property name="rediswatcher" ref="rediswatcher"></property>
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

**测试**  
1.启动Redis服务器

```
redis-server.exe
```

2.启动Redis客户端进行初始化数据

```
redis-cli.exe
```

```
redis 127.0.0.1:6379> set /a2/m1 zhaohui
OK
redis 127.0.0.1:6379> set /a3/m1/v2 nanjing
OK
redis 127.0.0.1:6379> set /a3/m1/v2/t2 codingo
OK
```

3.启动Main类

```
public class Main {
  
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring-config.xml" });
        Person person = (Person) context.getBean("person");
        System.out.println(person.toString());
        }
}
```

4.启动RedisPublish  
定时发布数据，同时查看集群节点的Main类日志输出

```
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
```

日志输出如下：

```
2017-08-30 10:44:00 - init key = /a2/m1,value = zhaohui
2017-08-30 10:44:00 - init key = /a3/m1/v2,value = nanjing
2017-08-30 10:44:00 - init key = /a3/m1/v2/t2,value = codingo
2017-08-30 10:44:00 - onPSubscribe=>/a2*=1
2017-08-30 10:44:00 - onPSubscribe=>/a3*=2
2017-08-30 10:44:00 - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@4bad4a49: defining beans [rediswatcher,propertyConfigurer,person]; root of factory hierarchy
name = zhaohui,address = nanjing,company = codingo
onPMessage=>/a2*=/a2/b4/c10=message_1504061045414
onPMessage=>/a2*=/a2/b4/c11=message_1504061047458
onPMessage=>/a2*=/a2/b4/c12=message_1504061049458
onPMessage=>/a2*=/a2/b4/c13=message_1504061051458
```

详细代码svn地址：[http://code.taobao.org/svn/temp-pj/DynamicConf](http://code.taobao.org/svn/temp-pj/DynamicConf)

**总结**  
关于参数的集中式管理一共写了三篇文章，分别利用Zookeeper，MQ以及Redis来实现了一个简单的参数的集中式管理，但更多的只是提供了一个思路  
离生产还有很大距离，本片文章也是这个系列的最后一篇，综合来看Zookeeper更加适合做参数的集中式管理平台，MQ方式本身没有提供存储的功能  
只能作为一个中间层存在；而Redis方式虽然提供了持久化功能，但是会因为选择不同的持久化方式会出现丢数据的可能，还有就是本身的集群方式  
并不是很完善；虽然Zookeeper本身并不是一个存储系统，但是紧紧用来存储少量的参数应该足够了。

**个人博客：[codingo.xyz](http://codingo.xyz/)**