**系列文章**

1.  [Zookeeper实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1503392)
2.  [JMS实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1510101)
3.  [Redis实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1526063)

**前言**  
上一篇文件[Zookeeper实现参数的集中式管理](https://my.oschina.net/OutOfMemory/blog/1503392)介绍了使用Zookeeper对节点的监听通知机制简单实现了对参数的集中式管理，其实JMS的发布订阅机制也能实现类似的功能，集群节点通过订阅指定的节点，同时使用jms对消息的过滤器功能，实现对指定参数的更新，本文将介绍通过JMS实现简单的参数集中式管理。

**Maven引入**  
Spring相关的jar引入参考上一篇文章

```
<dependency>
    <groupId>javax.jms</groupId>
    <artifactId>jms</artifactId>
    <version>1.1</version>
</dependency>
<dependency>
    <groupId>org.apache.activemq</groupId>
    <artifactId>activemq-all</artifactId>
    <version>5.10.0</version>
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
MQWatcher主要用来和JMS建立连接，同时订阅指定节点，建立点对点连接，过滤出需要监听的数据，更新数据，初始化数据，存储数据等  
InitConfServer主要作为点对点连接的服务器端用来初始化数据

1.同时配置监听多个节点  
提供一个字符串数组给用户用来添加需要监听的节点：

```
private String[] keyPatterns;
```

2.能够监听其子节点以及子节点的子节点  
使用了一种和Zookeeper不一样的方式，JMS的方式是将所有的数据变更都发送到订阅者，然后订阅者通过过滤出需要的数据进行更新

```
/** MQ的过滤器 **/
private StringBuffer keyFilter = new StringBuffer();
 
private final String TOPIC = "dynamicConfTopic";
 
private void watcherPaths() throws JMSException {
    Topic topic = session.createTopic(TOPIC);
    MessageConsumer consumer = session.createConsumer(topic, keyFilter.toString());
    consumer.setMessageListener(new MessageListener() {
 
        @Override
        public void onMessage(Message message) {
            try {
                String key = message.getStringProperty(IDENTIFIER);
                TextMessage tm = (TextMessage) message;
                keyValueMap.put(key, tm.getText());
                LOGGER.info("key = " + key + ",value = " + tm.getText());
            } catch (JMSException e) {
                LOGGER.error("onMessage error", e);
            }
        }
    });
}
```

对TOPIC进行了订阅，并且指定了过滤器keyFilter，keyFilter正是基于keyPatterns组装而成的

```
private final String IDENTIFIER = "confKey";
 
/**
* 生成接受过滤器
*/
private void generateKeyFilter() {
    for (int i = 0; i < keyPatterns.length; i++) {
        keyFilter.append(IDENTIFIER + " LIKE '" + keyPatterns[i] + "%'");
        if (i < keyPatterns.length - 1) {
            keyFilter.append(" OR ");
        }
    }
    LOGGER.info("keyFilter : " + keyFilter.toString());
}
```

对指定的属性IDENTIFIER，通过LIKE和OR关键字进行过滤

3.服务器启动初始化节点数据  
通过点对点的方式，在服务器启动时通过请求响应模式来获取初始化数据

```
private final String QUEUE = "dynamicConfQueue";
 
/**
 * 初始化key-value值
 * 
 * @throws JMSException
 */
private void initKeyValues() throws JMSException {
    TemporaryQueue responseQueue = null;
    MessageProducer producer = null;
    MessageConsumer consumer = null;
    Queue queue = queueSession.createQueue(QUEUE);
 
    TextMessage requestMessage = queueSession.createTextMessage();
    requestMessage.setText(generateKeyString());
    responseQueue = queueSession.createTemporaryQueue();
    producer = queueSession.createProducer(queue);
    consumer = queueSession.createConsumer(responseQueue);
    requestMessage.setJMSReplyTo(responseQueue);
    producer.send(requestMessage);
 
    MapMessage receiveMap = (MapMessage) consumer.receive();
    @SuppressWarnings("unchecked")
    Enumeration<String> mapNames = receiveMap.getPropertyNames();
    while (mapNames.hasMoreElements()) {
        String key = mapNames.nextElement();
        String value = receiveMap.getStringProperty(key);
        keyValueMap.put(key, value);
        LOGGER.info("init key = " + key + ",value = " + value);
    }
}
```

通过对指定QUEUE请求，同时建立一个临时的响应QUEUE，然后接受一个MapMessage，用来初始化keyValueMap

4.监听节点数据的变更  
通过发布订阅模式，接受所有数据，然后进行过滤，目标2中已经有相关实现

5.spring配置中可以从Zookeeper中读取参数进行初始化

```
public class MQPropPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
 
    private MQWatcher mqwatcher;
 
    @Override
    protected Properties mergeProperties() throws IOException {
        return loadPropFromMQ(super.mergeProperties());
    }
 
    /**
     * 从MQ中加载配置的常量
     * 
     * @param result
     * @return
     */
    private Properties loadPropFromMQ(Properties result) {
        mqwatcher.watcherKeys();
        mqwatcher.fillProperties(result);
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
    <bean id="mqwatcher" class="zh.maven.DynamicConf.mq.MQWatcher">
        <property name="keyPatterns" value="/a2,/a3" />
    </bean>
    <bean id="propertyConfigurer" class="zh.maven.DynamicConf.mq.MQPropPlaceholderConfigurer">
        <property name="mqwatcher" ref="mqwatcher"></property>
    </bean>
</beans>
```

**测试**  
1.启动ActiveMQ

```
activemq.bat
```

2.InitConfServer启动  
用来监听集群节点的初始化请求，获取到集群节点发送来的keyPatterns，然后将符合其模式的数据封装成MapMessage发送给集群节点

```
@Override
public void onMessage(Message message) {
    try {
        TextMessage receiveMessage = (TextMessage) message;
        String keys = receiveMessage.getText();
        LOGGER.info("keys = " + keys);
        MapMessage returnMess = session.createMapMessage();
        returnMess.setStringProperty("/a2/m1", "zhaohui");
        returnMess.setStringProperty("/a3/m1/v2", "nanjing");
        returnMess.setStringProperty("/a3/m1/v2/t2", "zhaohui");
 
        QueueSender sender = session.createSender((Queue) message.getJMSReplyTo());
        sender.send(returnMess);
    } catch (Exception e) {
        LOGGER.error("onMessage error", e);
    }
}
```

以上代码只是进行了简单的模拟，提供了一个思路

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

4.启动TopicPublisher  
定时发布数据，同时查看集群节点的Main类日志输出

```
public class TopicPublisher {
    private static final String TOPIC = "dynamicConfTopic";
    private static final String IDENTIFIER = "confKey";
 
    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection();
        connection.start();
 
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(TOPIC);
 
        MessageProducer producer = session.createProducer(topic);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
        int i=1;
        while (true) {
            TextMessage message = session.createTextMessage();
            message.setStringProperty(IDENTIFIER, "/a2/"+i);
            message.setText("message_" + System.currentTimeMillis());
            producer.send(message);
            System.out.println("Sent message: " + message.getText());
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
2017-08-14 21:52:23 - keyFilter : confKey LIKE '/a2%' OR confKey LIKE '/a3%'
2017-08-14 21:52:24 - init key = /a3/m1/v2/t2,value = zhaohui
2017-08-14 21:52:24 - init key = /a3/m1/v2,value = nanjing
2017-08-14 21:52:24 - init key = /a2/m1,value = zhaohui
2017-08-14 21:52:24 - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@223dd567: defining beans [person,mqwatcher,propertyConfigurer]; root of factory hierarchy
name = zhaohui,address = nanjing,company = zhaohui
2017-08-14 21:52:33 - key = /a2/1,value = message_1502718753819
2017-08-14 21:52:35 - key = /a2/2,value = message_1502718755832
2017-08-14 21:52:37 - key = /a2/3,value = message_1502718757846
2017-08-14 21:52:39 - key = /a2/4,value = message_1502718759860
2017-08-14 21:52:41 - key = /a2/5,value = message_1502718761876
```

详细代码svn地址：[http://code.taobao.org/svn/temp-pj/DynamicConf](http://code.taobao.org/svn/temp-pj/DynamicConf)

**总结**  
通过JMS实现了一个简单的参数化平台系统，当然想在生产中使用还有很多需要优化的地方，本文在于提供一个思路；后续有时间准备对DynamicConf提供更加完善的方案。

**个人博客：[codingo.xyz](http://codingo.xyz/)**