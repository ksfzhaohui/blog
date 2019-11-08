## RocketMQ整体结构

![](https://oscimg.oschina.net/oscnet/b95324fc1ac6246060bdf2bb1e0edba92e3.jpg)

如上图所示，整体可以分成4个角色，分别是：Producer，Consumer，Broker以及NameServer；

### 1.NameServer

可以理解为是消息队列的协调者，Broker向它注册路由信息，同时Client向其获取路由信息，如果使用过Zookeeper，就比较容易理解了，但是功能比Zookeeper弱；  
NameServer本身是没有状态的，并且多个NameServer直接并没有通信，可以横向扩展多台，Broker会和每一台NameServer建立长连接；

### 2.Broker

Broker是RocketMQ的核心，提供了消息的接收，存储，拉取等功能，一般都需要保证Broker的高可用，所以会配置Broker Slave，当Master挂掉之后，Consumer然后可以消费Slave；  
Broker分为Master和Slave，一个Master可以对应多个Slave，Master与Slave的对应关系通过指定相同的BrokerName，不同的BrokerId来定义，BrokerId为0表示Master，非0表示Slave；

### 3.Producer

消息队列的生产者，需要与NameServer建立连接，从NameServer获取Topic路由信息，并向提供Topic服务的Broker Master建立连接；Producer无状态，看集群部署；

### 4.Consumer

消息队列的消费者，同样与NameServer建立连接，从NameServer获取Topic路由信息，并向提供Topic服务的Broker Master，Slave建立连接；

### 5.Topic和Message Queue

在介绍完以上4个角色以后，还需要重点介绍一下上面提到的Topic和Message Queue；字面意思就是主题，用来区分不同类型的消息，发送和接收消息前都需要先创建Topic，针对Topic来发送和接收消息，为了提高性能和吞吐量，引入了Message Queue，一个Topic可以设置一个或多个Message Queue，有点类似kafka的分区(Partition)，这样消息就可以并行往各个Message Queue发送消息，消费者也可以并行的从多个Message Queue读取消息；

## 单机配置和部署

以下部署在**centos7，jdk1.8，rocketmq4.3.2**；启动RocketMQ的顺序是先启动NameServer，然后再启动Broker；

### 1.NameServer启动

```
[root@localhost bin]# ./mqnamesrv
Java HotSpot(TM) 64-Bit Server VM warning: Using the DefNew young collector with the CMS collector is deprecated and will likely be removed in a future release
Java HotSpot(TM) 64-Bit Server VM warning: UseCMSCompactAtFullCollection is deprecated and will likely be removed in a future release.
The Name Server boot success. serializeType=JSON
```

如上日志表示启动成功，默认端口为9876；

### 2.Broker启动

```
[root@localhost bin]# ./mqbroker
Java HotSpot(TM) 64-Bit Server VM warning: INFO: os::commit_memory(0x00000005c0000000, 8589934592, 0) failed; error='Cannot allocate memory' (errno=12)
#
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 8589934592 bytes for committing reserved memory.
# An error report file with more information is saved as:
# /root/rocketmq-all-4.3.2-bin-release/bin/hs_err_pid3977.log
```

启动失败，报内存不足，主要是rocketmq默认配置的启动参数值比较大，修改runbroker.sh即可

```
[root@localhost bin]# vi runbroker.sh

JAVA_OPT="${JAVA_OPT} -server -Xms8g -Xmx8g -Xmn4g"
```

默认配置的可用内存为8g，虚拟机内存不够，修改为如下即可

```
JAVA_OPT="${JAVA_OPT} -server -Xms128m -Xmx128m -Xmn128m"
```

再次启动，日志如下，表示启动成功，默认端口为10911；

```
[root@localhost bin]# ./mqbroker
The broker[localhost.localdomain, 192.168.237.128:10911] boot success. serializeType=JSON
```

### 3.简单测试

#### 3.1生产者

```
public class SyncProducer {

     public static void main(String[] args) throws Exception {
           // 构造Producer
           DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName");
           producer.setNamesrvAddr("192.168.237.128:9876");
           // 初始化Producer，整个应用生命周期内，只需要初始化1次
           producer.start();
           for (int i = 0; i < 100; i++) {
                Message msg = new Message("TopicTest", "TagA",
                           ("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                SendResult sendResult = producer.send(msg);
                System.out.println(sendResult);
           }
           producer.shutdown();
     }
}
```

创建了一个DefaultMQProducer对象，同时设置了GroupName和NameServer地址，然后创建Message消息通过DefaultMQProducer将消息发送出去，返回一个SendResult对象；

#### 3.2消费者

```
public class PushConsumer {

     public static void main(String[] args) throws MQClientException {
           DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please rename to unique group name");
           consumer.setNamesrvAddr("192.168.237.128:9876");
           consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
           consumer.subscribe("TopicTest", "*");
           consumer.registerMessageListener(new MessageListenerConcurrently() {
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                ConsumeConcurrentlyContext context) {
                     System.out.printf(Thread.currentThread().getName() + "Receive New Messages :" + msgs + "%n");
                     return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
           });
           consumer.start();
     }
}
```

同样指定了GroupName和NameServer地址，订阅了Topic；

#### 3.3运行测试

直接运行生产者报如下错误：

```
Exception in thread "main" org.apache.rocketmq.client.exception.MQClientException: No route info of this topic, TopicTest
See http://rocketmq.apache.org/docs/faq/ for further details.
    at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.sendDefaultImpl(DefaultMQProducerImpl.java:634)
    at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1253)
    at org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl.send(DefaultMQProducerImpl.java:1203)
    at org.apache.rocketmq.client.producer.DefaultMQProducer.send(DefaultMQProducer.java:214)
    at com.rocketmq.SyncProducer.main(SyncProducer.java:26)
```

错误显示"没有此Topic的路由信息"，也就是生产者在发送消息的时候没有获取到路由信息，找不到指定的Broker,可能的原因：  
1.Broker没有正确连接NameServer  
2.Producer没有连接NameServer  
3.Topic没有被正确创建  
SyncProducer中指定了NameServer的地址，同时RocketMQ默认情况下会自动创建Topic，所以原因是Broker没有注册到NameServer，重新指定NameServer再启动：

```
[root@localhost bin]# ./mqbroker -n localhost:9876
The broker[localhost.localdomain, 192.168.237.128:10911] boot success. serializeType=JSON and name server is localhost:9876
```

再次运行SyncProducer，日志如下：

```
SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C4C60000, offsetMsgId=C0A8ED8000002A9F000000000000229C, messageQueue=MessageQueue[topic=TopicTest, brokerName=localhost.localdomain, queueId=1], queueOffset=11]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C4CD0001, offsetMsgId=C0A8ED8000002A9F000000000000234D, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=2], queueOffset=9]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C4D90002, offsetMsgId=C0A8ED8000002A9F00000000000023FE, messageQueue=MessageQueue[topic=TopicTest, brokerName=localhost.localdomain, queueId=3], queueOffset=9]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C4E80003, offsetMsgId=C0A8ED8000002A9F00000000000024AF, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=0], queueOffset=11]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C4F40004, offsetMsgId=C0A8ED8000002A9F0000000000002560, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=1], queueOffset=12]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C4F70005, offsetMsgId=C0A8ED8000002A9F0000000000002611, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=2], queueOffset=10]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C5030006, offsetMsgId=C0A8ED8000002A9F00000000000026C2, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=3], queueOffset=10]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C5070007, offsetMsgId=C0A8ED8000002A9F0000000000002773, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=0], queueOffset=12]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C50A0008, offsetMsgId=C0A8ED8000002A9F0000000000002824, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=1], queueOffset=13]

SendResult [sendStatus=SEND_OK, msgId=0A0D53073B6073D16E933086C50D0009, offsetMsgId=C0A8ED8000002A9F00000000000028D5, messageQueue=MessageQueue [topic=TopicTest, brokerName=localhost.localdomain, queueId=2], queueOffset=11]
```

消费者使用的是push模式，可以实时接受消息：

```
ConsumeMessageThread_13Receive New Messages :[MessageExt [queueId=1, storeSize=177,queueOffset=11, sysFlag=0, bornTimestamp=1547086138566, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430770, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F000000000000229C, commitLogOffset=8860, bodyCRC=705268097, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=12, CONSUME_START_TIME=1547086138573, UNIQ_KEY=0A0D53073B6073D16E933086C4C60000, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 48], transactionId='null'}]]

ConsumeMessageThread_3Receive New Messages :[MessageExt [queueId=2, storeSize=177, queueOffset=9, sysFlag=0, bornTimestamp=1547086138573, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430783, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F000000000000234D, commitLogOffset=9037, bodyCRC=1561245975, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=10, CONSUME_START_TIME=1547086138598, UNIQ_KEY=0A0D53073B6073D16E933086C4CD0001, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 49], transactionId='null'}]]

ConsumeMessageThread_17Receive New Messages :[MessageExt [queueId=3, storeSize=177, queueOffset=9, sysFlag=0, bornTimestamp=1547086138585, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430794, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F00000000000023FE, commitLogOffset=9214, bodyCRC=1141369005, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=10, CONSUME_START_TIME=1547086138601, UNIQ_KEY=0A0D53073B6073D16E933086C4D90002, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 50], transactionId='null'}]]

ConsumeMessageThread_9Receive New Messages :[MessageExt [queueId=0, storeSize=177, queueOffset=11, sysFlag=0, bornTimestamp=1547086138600, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430807, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F00000000000024AF, commitLogOffset=9391, bodyCRC=855693371, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=12, CONSUME_START_TIME=1547086138612, UNIQ_KEY=0A0D53073B6073D16E933086C4E80003, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 51], transactionId='null'}]]

ConsumeMessageThread_15Receive New Messages :[MessageExt [queueId=1, storeSize=177, queueOffset=12, sysFlag=0, bornTimestamp=1547086138612, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430809, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F0000000000002560, commitLogOffset=9568, bodyCRC=761548184, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=13, CONSUME_START_TIME=1547086138626, UNIQ_KEY=0A0D53073B6073D16E933086C4F40004, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 52], transactionId='null'}]]

ConsumeMessageThread_11Receive New Messages :[MessageExt [queueId=2, storeSize=177, queueOffset=10, sysFlag=0, bornTimestamp=1547086138615, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430820, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F0000000000002611, commitLogOffset=9745, bodyCRC=1516469518, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=11, CONSUME_START_TIME=1547086138628, UNIQ_KEY=0A0D53073B6073D16E933086C4F70005, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 53], transactionId='null'}]]

ConsumeMessageThread_4Receive New Messages :[MessageExt [queueId=3, storeSize=177,queueOffset=10, sysFlag=0, bornTimestamp=1547086138627, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430824, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F00000000000026C2,commitLogOffset=9922, bodyCRC=1131031732,reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=11, CONSUME_START_TIME=1547086138633, UNIQ_KEY=0A0D53073B6073D16E933086C5030006, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 54], transactionId='null'}]]

ConsumeMessageThread_14Receive New Messages :[MessageExt [queueId=0, storeSize=177, queueOffset=12, sysFlag=0, bornTimestamp=1547086138631, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430827, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F0000000000002773, commitLogOffset=10099, bodyCRC=879565858, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=13, CONSUME_START_TIME=1547086138635, UNIQ_KEY=0A0D53073B6073D16E933086C5070007, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 55], transactionId='null'}]]

ConsumeMessageThread_10Receive New Messages :[MessageExt [queueId=1, storeSize=177, queueOffset=13, sysFlag=0, bornTimestamp=1547086138634, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430830, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F0000000000002824, commitLogOffset=10276, bodyCRC=617742771, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=14, CONSUME_START_TIME=1547086138638, UNIQ_KEY=0A0D53073B6073D16E933086C50A0008, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 56], transactionId='null'}]]

ConsumeMessageThread_7Receive New Messages :[MessageExt [queueId=2, storeSize=177, queueOffset=11, sysFlag=0, bornTimestamp=1547086138637, bornHost=/192.168.237.1:53524, storeTimestamp=1547139430833, storeHost=/192.168.237.128:10911, msgId=C0A8ED8000002A9F00000000000028D5, commitLogOffset=10453, bodyCRC=1406480677, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=12, CONSUME_START_TIME=1547086138641, UNIQ_KEY=0A0D53073B6073D16E933086C50D0009, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 57], transactionId='null'}]]
```

## 多机集群配置和部署

分别部署两台NameServer，两台Broker并且分别提供Slave，准备两台电脑分别是本机的windows以及虚拟机centos；

### 1.启动NameServer

分别启动2台NameServer，端口号都使用默认的9876，地址端口如下：

```
192.168.237.128:9876
10.13.83.7:9876
```

### 2.启动Broker

每台机器上分别启动一个Master和一个Slave，互为主备，在主目录下的conf文件夹下提供了多种broker配置模式，分别有：2m-2s-async，2m-2s-sync，2m-noslave，可以以此为模版做如下配置：

#### 2.1配置10.13.83.7Master和Slave

Master配置如下：

```
namesrvAddr=192.168.237.128:9876;10.13.83.7:9876
brokerClusterName=DefaultCluster
brokerName=broker-a
brokerId=0
deleteWhen=04
fileReservedTime=48
brokerRole=SYNC_MASTER
flushDiskType=ASYNC_FLUSH
listenPort=10911
storePathRootDir=E:/rocketmq-all-4.3.2-bin-release/store-a-m
```

Slave配置如下：

```
namesrvAddr=192.168.237.128:9876;10.13.83.7:9876
brokerClusterName=DefaultCluster
brokerName=broker-a
brokerId=1
deleteWhen=04
fileReservedTime=48
brokerRole=SLAVE
flushDiskType=ASYNC_FLUSH
listenPort=10811
storePathRootDir=E:/rocketmq-all-4.3.2-bin-release/store-a-s
```

分别启动结果如下：

```
E:\rocketmq-all-4.3.2-bin-release\bin>mqbroker -c E:\rocketmq-all-4.3.2-bin-rele
ase\conf\broker-m.conf
The broker[broker-a, 10.13.83.7:10911] boot success. serializeType=JSON and name
 server is 192.168.237.128:9876;10.13.83.7:9876
```

以上是Master启动日志，Slave日志如下：

```
E:\rocketmq-all-4.3.2-bin-release\bin>mqbroker -c E:\rocketmq-all-4.3.2-bin-rele
ase\conf\broker-s.conf
The broker[broker-a, 10.13.83.7:10811] boot success. serializeType=JSON and name
 server is 192.168.237.128:9876;10.13.83.7:9876
```

#### 2.2配置10.13.83.7Slave

Master配置如下：

```
namesrvAddr=192.168.237.128:9876;10.13.83.7:9876
brokerClusterName=DefaultCluster
brokerName=broker-b
brokerId=0
deleteWhen=04
fileReservedTime=48
brokerRole=SYNC_MASTER
flushDiskType=ASYNC_FLUSH
listenPort=10911
storePathRootDir=/root/rocketmq-all-4.3.2-bin-release/store-b-m
```

Slave配置如下：

```
namesrvAddr=192.168.237.128:9876;10.13.83.7:9876
brokerClusterName=DefaultCluster
brokerName=broker-b
brokerId=1
deleteWhen=04
fileReservedTime=48
brokerRole=SLAVE
flushDiskType=ASYNC_FLUSH
listenPort=10811
storePathRootDir=/root/rocketmq-all-4.3.2-bin-release/store-b-s
```

启动日志分别如下：

```
[root@localhost bin]# ./mqbroker -c ../conf/broker-m.conf 
The broker[broker-b, 192.168.237.128:10911] boot success. serializeType=JSON and name server is 192.168.237.128:9876;10.13.83.7:9876
```

```
[root@localhost bin]# ./mqbroker -c ../conf/broker-s.conf 
The broker[broker-b, 192.168.237.128:10811] boot success. serializeType=JSON and name server is 192.168.237.128:9876;10.13.83.7:9876
```

### 3.配置说明

#### 1.namesrvAddr

NameServer地址，可以配置多个，用逗号分隔；

#### 2.brokerClusterName

所属集群名称，如果节点较多可以配置多个

#### 3.brokerName

broker名称，master和slave使用相同的名称，表明他们的主从关系

#### 4.brokerId

0表示Master，大于0表示不同的slave

#### 5.deleteWhen

表示几点做消息删除动作，默认是凌晨4点

#### 6.fileReservedTime

在磁盘上保留消息的时长，单位是小时

#### 7.brokerRole

有三个值：SYNC\_MASTER，ASYNC\_MASTER，SLAVE；同步和异步表示Master和Slave之间同步数据的机制；

#### 8.flushDiskType

刷盘策略，取值为：ASYNC\_FLUSH，SYNC\_FLUSH表示同步刷盘和异步刷盘；SYNC\_FLUSH消息写入磁盘后才返回成功状态，ASYNC\_FLUSH不需要；

#### 9.listenPort

启动监听的端口号

#### 10.storePathRootDir

存储消息的根目录

## 管理工具

### 1.命令行管理工具

mqadmin是RocketMQ自带的命令行管理工具，可以创建、修改Topic，查询消息，更新配置信息等操作，具体可以通过如下命令查看：

```
E:\rocketmq-all-4.3.2-bin-release\bin>mqadmin
The most commonly used mqadmin commands are:
   updateTopic          Update or create topic
   deleteTopic          Delete topic from broker and NameServer.
   updateSubGroup       Update or create subscription group
   deleteSubGroup       Delete subscription group from broker.
   updateBrokerConfig   Update broker's config
   updateTopicPerm      Update topic perm
   topicRoute           Examine topic route info
   topicStatus          Examine topic Status info
   topicClusterList     get cluster info for topic
   brokerStatus         Fetch broker runtime status data
   queryMsgById         Query Message by Id
   queryMsgByKey        Query Message by Key
   queryMsgByUniqueKey  Query Message by Unique key
   queryMsgByOffset     Query Message by offset
   printMsg             Print Message Detail
   printMsgByQueue      Print Message Detail
   sendMsgStatus        send msg to broker.
   brokerConsumeStats   Fetch broker consume stats data
   producerConnection   Query producer's socket connection and client vers
   consumerConnection   Query consumer's socket connection, client version
ubscription
   consumerProgress     Query consumers's progress, speed
   consumerStatus       Query consumer's internal data structure
   cloneGroupOffset     clone offset from other group.
   clusterList          List all of clusters
   topicList            Fetch all topic list from name server
   updateKvConfig       Create or update KV config.
   deleteKvConfig       Delete KV config.
   wipeWritePerm        Wipe write perm of broker in all name server
   resetOffsetByTime    Reset consumer offset by timestamp(without client
t).
   updateOrderConf      Create or update or delete order conf
   cleanExpiredCQ       Clean expired ConsumeQueue on broker.
   cleanUnusedTopic     Clean unused topic on broker.
   startMonitoring      Start Monitoring
   statsAll             Topic and Consumer tps stats
   allocateMQ           Allocate MQ
   checkMsgSendRT       check message send response time
   clusterRT            List All clusters Message Send RT
   getNamesrvConfig     Get configs of name server.
   updateNamesrvConfig  Update configs of name server.
   getBrokerConfig      Get broker config by cluster or special broker!
   queryCq              Query cq command.
   sendMessage          Send a message
   consumeMessage       Consume message

See 'mqadmin help <command>' for more information on a specific command.
```

列出了所有支持的命令以及简单的介绍，如果想看详细的可以如下命令：

```
E:\rocketmq-all-4.3.2-bin-release\bin>mqadmin help statsAll
usage: mqadmin statsAll [-a] [-h] [-n <arg>] [-t <arg>]
 -a,--activeTopic         print active topic only
 -h,--help                Print help
 -n,--namesrvAddr <arg>   Name server address list, eg: 192.168.0.1:9876;192.168
.0.2:9876
 -t,--topic <arg>         print select topic only
```

### 2.图形界面管理工具

除了命令，还提供了图形界面管理工具，在RocketMQ的扩展包里面，具体地址如下：

```
https://github.com/apache/rocketmq-externals/tree/release-rocketmq-console-1.0.0/rocketmq-console
```

目前的稳定版本是1.0.0，可以下载下来在本地运行，对application.properties做简单配置：

```
rocketmq.config.namesrvAddr=10.13.83.7:9876
```

需要指定NameServer的地址，然后就可以打包运行了，运作之后会启动8080端口，直接访问地址：

```
http://localhost:8080
```

![](https://oscimg.oschina.net/oscnet/4e6af5773de8faa6f4216a80e3d5c9f0811.jpg)

## 总结

本文从最简单的安装部署入手，并对常用的配置参数做了简单介绍；然后了解了RocketMQ的部署的整体结构，分别对其中的角色做了简单介绍；最后介绍了两种RocketMQ的管理工具，方便对RocketMQ的监控和管理。