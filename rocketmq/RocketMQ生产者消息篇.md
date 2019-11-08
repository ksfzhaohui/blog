## 系列文章

[RocketMQ入门篇](https://my.oschina.net/OutOfMemory/blog/2999648)  
[RocketMQ生产者流程篇](https://my.oschina.net/OutOfMemory/blog/3010371)  
[RocketMQ生产者消息篇](https://my.oschina.net/OutOfMemory/blog/3013124)

## 前言

上文[RocketMQ生产者流程篇](https://my.oschina.net/OutOfMemory/blog/3010371)中详细介绍了生产者发送消息的流程，本文将重点介绍发送消息的通信模式以及各种不同的消息类型。

## 通信模式

RocketMQ提供了三种通讯模式，分别是：同步，异步和单向；可以查看内部类CommunicationMode：

```
public enum CommunicationMode {
    SYNC,
    ASYNC,
    ONEWAY,
}
```

下面分别看一下三种通讯模式如何使用

### 1.同步方式

看一个简单的发送同步消息的实例：

```
public class SyncProducer {

    public static void main(String[] args) throws Exception {

        System.setProperty("rocketmq.namesrv.domain", "localhost");
        // 构造Producer
        DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName2");
        // producer.setNamesrvAddr("192.168.237.128:9876");
        // 初始化Producer，整个应用生命周期内，只需要初始化1次
        producer.start();

        for (int i = 0; i < 1; i++) {
            Message msg = new Message("TopicTest6", "TagA",
                    ("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg);
            System.out.println(sendResult);
        }
        producer.shutdown();
    }
}
```

最简单的直接指定一个message参数默认使用的就是同步方式发送消息，可以看到在发送完消息之后，会立马返回了发送结果SendResult:

```
SendResult [sendStatus=SEND_OK, msgId=0A0D5307324873D16E9365360AC60000, offsetMsgId=0A0D530700002A9F0000000000001200, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=0], queueOffset=0]

```

### 2.异步方式

看一个简单的发送异步消息的实例：

```
public class AsyncProducer {

    public static void main(String[] args)
            throws MQClientException, RemotingException, InterruptedException, UnsupportedEncodingException {
        DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName2");
        producer.setRetryTimesWhenSendAsyncFailed(3);
        producer.setNamesrvAddr("192.168.237.128:9876");
        producer.start();
        for (int i = 0; i < 1; i++) {
            Message msg = new Message("TopicTest6", "TagA",
                    ("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println(sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
```

可以看到在发送消息时指定了SendCallback回调类，send发送方法返回值为void，发送成功之后会回调SendCallback的onSuccess方法，异常调用onException方法；发送成功日志如下：

```
SendResult [sendStatus=SEND_OK, msgId=0A0D5307261473D16E936536591E0000, offsetMsgId=0A0D530700002A9F00000000000012B2, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=0], queueOffset=1]

```

### 3.单向方式

看一个简单的发送单向消息的实例：

```
public class OneWayProducer {

    public static void main(String[] args) throws Exception {

        DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName2");
        producer.setNamesrvAddr("192.168.237.128:9876");
        producer.start();
        for (int i = 0; i < 1; i++) {
            Message msg = new Message("TopicTest6", "TagA",
                    ("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            producer.sendOneway(msg);
        }
        producer.shutdown();
    }
}
```

单向发送消息发送之后没有响应，但是在消费端可以收到消息，如下所示：

```
ConsumeMessageThread_6Receive New Messages :[MessageExt [queueId=2, storeSize=178, queueOffset=0, sysFlag=0, bornTimestamp=1550648529526, bornHost=/10.13.83.7:54213, storeTimestamp=1550648529530, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000001416, commitLogOffset=5142, bodyCRC=705268097, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=1, CONSUME_START_TIME=1550648529533, UNIQ_KEY=0A0D530747DC73D16E93653766750000, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 48], transactionId='null'}]]

```

### 4.发送状态

以上同步和异步实例显示的发送状态都是SEND\_OK，除了此状态还有其他三个状态：FLUSH\_DISK\_TIMEOUT，FLUSH\_SLAVE\_TIMEOUT和SLAVE\_NOT_AVAILABLE；具体可以查看内部类SendStatus：

```
public enum SendStatus {
    SEND_OK,
    FLUSH_DISK_TIMEOUT,
    FLUSH_SLAVE_TIMEOUT,
    SLAVE_NOT_AVAILABLE,
}
```

FLUSH\_DISK\_TIMEOUT：刷盘超时，Broker设置的刷盘策略为SYNC_FLUSH才可能出现此错误；  
FLUSH\_SLAVE\_TIMEOUT：主从同步超时，Broker设置了slave，并且指定同步策略为SYNC_Master；  
SLAVE\_NOT\_AVAILABLE：找不到salve，同样是Broke指定同步策略为SYNC_Master；  
SEND_OK：表示发送成功，以上情况都没有出现。  
注：必要时需要对各种异常场景进行处理，保证高质量的生产者。

## 消息类型

在RocketMQ的生产者端可以发送多种类型的消息包括：延迟消息，顺序消息以及事务消息，下面分别进行实例分析；

### 1.延迟消息

RocketMQ支持发送延迟消息，Broker收到消息后会延迟一段时间在处理，具体使用看如下代码：

```
Message msg = new Message("TopicTest6", "TagA",
                    ("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
msg.setDelayTimeLevel(3);
```

可以直接设置延迟时间等级，具体有哪些等级，以及每个等级对应的时间可以查看类MessageStoreConfig

```
private String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h";
```

类中的messageDelayLevel 变量包含了所有可以延迟的时间，使用空格分离，所以这里等级为3，其实对应的延迟时间就是10秒；分别观察生产者和消费者的日志如下：  
生产者发送消息日志如下：

```
Time [Wed Feb 20 18:25:32 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D5307268073D16E9365CCFA9E0000, offsetMsgId=0A0D530700002A9F0000000000001F42, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=1], queueOffset=4]

```

消费者接收日志如下：

```
Time [Wed Feb 20 18:25:42 CST 2019],ConsumeMessageThread_3Receive New Messages :[MessageExt [queueId=1, storeSize=219, queueOffset=2, sysFlag=0, bornTimestamp=1550658332318, bornHost=/10.13.83.7:55682, storeTimestamp=1550658342322, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002026, commitLogOffset=8230, bodyCRC=705268097, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=3, CONSUME_START_TIME=1550658342325, UNIQ_KEY=0A0D5307268073D16E9365CCFA9E0000, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=1}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 48], transactionId='null'}]]

```

可以发现发送消息的时间和接收消息的时间相差10秒；

### 2.顺序消息

顺序消息指生产者生产数据的顺序和消费者消费数据的顺序是一致的；顺序消息包括全局顺序消息和局部顺序消息，全局顺序消息指在某个Topic下所有消息都是顺序的，局部顺序消息指在Topic下的Message Queue中是顺序的；

#### 2.1全局顺序消息

RocketMQ在默认情况下并不能保证有序，一个Topic下会指定多个读写队列，生产者会将消息写入任意的Message Queue中，同样消费者可能会启动多个线程同时处理数据，所以并不能保证顺序；如何保证全局顺序需要只有一个读队列一个写队列，然后需要保证生产者和消费者不能并发处理，以下做一个简单的实例验证；  
生产者顺序的发送5条数据：

```
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_4Receive New Messages :[MessageExt [queueId=1, storeSize=219, queueOffset=3, sysFlag=0, bornTimestamp=1550713381399, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391403, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002575, commitLogOffset=9589, bodyCRC=705268097, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=4, CONSUME_START_TIME=1550713391405, UNIQ_KEY=0A0D5307418073D16E936914F6160000, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=1}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 48], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_5Receive New Messages :[MessageExt [queueId=2, storeSize=219, queueOffset=2, sysFlag=0, bornTimestamp=1550713381450, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391451, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002650, commitLogOffset=9808, bodyCRC=1561245975, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=3, CONSUME_START_TIME=1550713391453, UNIQ_KEY=0A0D5307418073D16E936914F64A0001, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=2}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 49], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_6Receive New Messages :[MessageExt [queueId=3, storeSize=219, queueOffset=4, sysFlag=0, bornTimestamp=1550713381455, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391460, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F000000000000272B, commitLogOffset=10027, bodyCRC=1141369005, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=5, CONSUME_START_TIME=1550713391463, UNIQ_KEY=0A0D5307418073D16E936914F64F0002, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=3}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 50], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_7Receive New Messages :[MessageExt [queueId=0, storeSize=219, queueOffset=4, sysFlag=0, bornTimestamp=1550713381462, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391464, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002806, commitLogOffset=10246, bodyCRC=855693371, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=5, CONSUME_START_TIME=1550713391490, UNIQ_KEY=0A0D5307418073D16E936914F6560003, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=0}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 51], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_8Receive New Messages :[MessageExt [queueId=1, storeSize=219, queueOffset=4, sysFlag=0, bornTimestamp=1550713381466, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391469, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F00000000000028E1, commitLogOffset=10465, bodyCRC=761548184, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=5, CONSUME_START_TIME=1550713391501, UNIQ_KEY=0A0D5307418073D16E936914F65A0004, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=1}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 52], transactionId='null'}]]

```

可以发现这5条数据分别写入了4个Message Queue中；再看一下消费者日志：

```
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_4Receive New Messages :[MessageExt [queueId=1, storeSize=219, queueOffset=3, sysFlag=0, bornTimestamp=1550713381399, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391403, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002575, commitLogOffset=9589, bodyCRC=705268097, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=4, CONSUME_START_TIME=1550713391405, UNIQ_KEY=0A0D5307418073D16E936914F6160000, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=1}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 48], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_5Receive New Messages :[MessageExt [queueId=2, storeSize=219, queueOffset=2, sysFlag=0, bornTimestamp=1550713381450, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391451, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002650, commitLogOffset=9808, bodyCRC=1561245975, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=3, CONSUME_START_TIME=1550713391453, UNIQ_KEY=0A0D5307418073D16E936914F64A0001, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=2}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 49], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_6Receive New Messages :[MessageExt [queueId=3, storeSize=219, queueOffset=4, sysFlag=0, bornTimestamp=1550713381455, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391460, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F000000000000272B, commitLogOffset=10027, bodyCRC=1141369005, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=5, CONSUME_START_TIME=1550713391463, UNIQ_KEY=0A0D5307418073D16E936914F64F0002, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=3}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 50], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_7Receive New Messages :[MessageExt [queueId=0, storeSize=219, queueOffset=4, sysFlag=0, bornTimestamp=1550713381462, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391464, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002806, commitLogOffset=10246, bodyCRC=855693371, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=5, CONSUME_START_TIME=1550713391490, UNIQ_KEY=0A0D5307418073D16E936914F6560003, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=0}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 51], transactionId='null'}]]
Time [Thu Feb 21 09:43:11 CST 2019],ConsumeMessageThread_8Receive New Messages :[MessageExt [queueId=1, storeSize=219, queueOffset=4, sysFlag=0, bornTimestamp=1550713381466, bornHost=/10.13.83.7:55720, storeTimestamp=1550713391469, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F00000000000028E1, commitLogOffset=10465, bodyCRC=761548184, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest6, MAX_OFFSET=5, CONSUME_START_TIME=1550713391501, UNIQ_KEY=0A0D5307418073D16E936914F65A0004, WAIT=true, DELAY=3, TAGS=TagA, REAL_QID=1}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 52], transactionId='null'}]]

```

消费者启动了5个线程同时从4个Message Queue中读取数据，所有不能保证数据的顺序性；  
分别做如下改造：设置Topic的读写队列分别为1，可以直接去[RocketMQ-console](http://localhost:8080/#/topic)去修改配置；然后设置消费者的处理线程数为1：

```
consumer.setConsumeThreadMin(1);
consumer.setConsumeThreadMax(1);
```

再次测试，生产者同样发送5条数据：

```
Time [Thu Feb 21 09:59:49 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D53071A0873D16E93692457700000, offsetMsgId=0A0D530700002A9F0000000000002EF5, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=0], queueOffset=11]
Time [Thu Feb 21 09:59:49 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D53071A0873D16E93692457790001, offsetMsgId=0A0D530700002A9F0000000000002FA7, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=0], queueOffset=12]
Time [Thu Feb 21 09:59:49 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D53071A0873D16E936924577F0002, offsetMsgId=0A0D530700002A9F0000000000003059, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=0], queueOffset=13]
Time [Thu Feb 21 09:59:49 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D53071A0873D16E93692457850003, offsetMsgId=0A0D530700002A9F000000000000310B, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=0], queueOffset=14]
Time [Thu Feb 21 09:59:49 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D53071A0873D16E93692457880004, offsetMsgId=0A0D530700002A9F00000000000031BD, messageQueue=MessageQueue [topic=TopicTest6, brokerName=broker-a, queueId=0], queueOffset=15]

```

可以发送所有的消息都写入了相同的队列，然后看消费者日志：

```
Time [Thu Feb 21 09:59:49 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=11, sysFlag=0, bornTimestamp=1550714389360, bornHost=/10.13.83.7:58033, storeTimestamp=1550714389364, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002EF5, commitLogOffset=12021, bodyCRC=705268097, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=12, CONSUME_START_TIME=1550714389372, UNIQ_KEY=0A0D53071A0873D16E93692457700000, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 48], transactionId='null'}]]
Time [Thu Feb 21 09:59:49 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=12, sysFlag=0, bornTimestamp=1550714389369, bornHost=/10.13.83.7:58033, storeTimestamp=1550714389371, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000002FA7, commitLogOffset=12199, bodyCRC=1561245975, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=13, CONSUME_START_TIME=1550714389379, UNIQ_KEY=0A0D53071A0873D16E93692457790001, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 49], transactionId='null'}]]
Time [Thu Feb 21 09:59:49 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=13, sysFlag=0, bornTimestamp=1550714389375, bornHost=/10.13.83.7:58033, storeTimestamp=1550714389379, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000003059, commitLogOffset=12377, bodyCRC=1141369005, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=15, CONSUME_START_TIME=1550714389385, UNIQ_KEY=0A0D53071A0873D16E936924577F0002, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 50], transactionId='null'}]]
Time [Thu Feb 21 09:59:49 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=14, sysFlag=0, bornTimestamp=1550714389381, bornHost=/10.13.83.7:58033, storeTimestamp=1550714389382, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F000000000000310B, commitLogOffset=12555, bodyCRC=855693371, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=15, CONSUME_START_TIME=1550714389385, UNIQ_KEY=0A0D53071A0873D16E93692457850003, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 51], transactionId='null'}]]
Time [Thu Feb 21 09:59:49 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=15, sysFlag=0, bornTimestamp=1550714389384, bornHost=/10.13.83.7:58033, storeTimestamp=1550714389386, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F00000000000031BD, commitLogOffset=12733, bodyCRC=761548184, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest6', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=16, CONSUME_START_TIME=1550714389409, UNIQ_KEY=0A0D53071A0873D16E93692457880004, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 52], transactionId='null'}]]

```

可以发送所有的消息都被同一个线程处理，并且从同一个Message Queue中读取数据，可以保证数据的顺序性；

#### 2.2局部顺序消息

生产者需要将相关业务的消息发送到同一个Message Queue，在消费端需要保证同一个Message Queue读取的消息不能被并发处理；  
生产者发送消息给同一个Message Queue可以通过MessageQueueSelector来实现：

```
SendResult sendResult = producer.send(msg, new MessageQueueSelector() {

        @Override
        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
            return mqs.get(0);
        }
}, i);
```

通过在select放在中为msg指定固定的Message Queue，这里为了方便给所有的消息都指定第0个队列；  
消费者保证同一个Message Queue读取的消息不能被并发处理，通过MessageListenerOrderly实现：

```
consumer.registerMessageListener(new MessageListenerOrderly() {
            
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                System.out.printf("Time [" + new Date().toString() + "]," +Thread.currentThread().getName() + "Receive New Messages :" + msgs + "%n");
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
        consumer.start();
```

简单测试一下，分开看一下生产者和消费者日志：

```
Time [Thu Feb 21 10:45:43 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D5307463C73D16E93694E5D8B0000, offsetMsgId=0A0D530700002A9F00000000000035E9, messageQueue=MessageQueue [topic=TopicTest7, brokerName=broker-a, queueId=0], queueOffset=5]
Time [Thu Feb 21 10:45:43 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D5307463C73D16E93694E5D940001, offsetMsgId=0A0D530700002A9F000000000000369B, messageQueue=MessageQueue [topic=TopicTest7, brokerName=broker-a, queueId=0], queueOffset=6]
Time [Thu Feb 21 10:45:43 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D5307463C73D16E93694E5D980002, offsetMsgId=0A0D530700002A9F000000000000374D, messageQueue=MessageQueue [topic=TopicTest7, brokerName=broker-a, queueId=0], queueOffset=7]
Time [Thu Feb 21 10:45:43 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D5307463C73D16E93694E5D9D0003, offsetMsgId=0A0D530700002A9F00000000000037FF, messageQueue=MessageQueue [topic=TopicTest7, brokerName=broker-a, queueId=0], queueOffset=8]
Time [Thu Feb 21 10:45:43 CST 2019],SendResult [sendStatus=SEND_OK, msgId=0A0D5307463C73D16E93694E5D9F0004, offsetMsgId=0A0D530700002A9F00000000000038B1, messageQueue=MessageQueue [topic=TopicTest7, brokerName=broker-a, queueId=0], queueOffset=9]

```

```
Time [Thu Feb 21 10:45:56 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=5, sysFlag=0, bornTimestamp=1550717143436, bornHost=/10.13.83.7:63916, storeTimestamp=1550717143440, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F00000000000035E9, commitLogOffset=13801, bodyCRC=705268097, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest7', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=10, UNIQ_KEY=0A0D5307463C73D16E93694E5D8B0000, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 48], transactionId='null'}]]
Time [Thu Feb 21 10:45:56 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=6, sysFlag=0, bornTimestamp=1550717143444, bornHost=/10.13.83.7:63916, storeTimestamp=1550717143445, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F000000000000369B, commitLogOffset=13979, bodyCRC=1561245975, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest7', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=10, UNIQ_KEY=0A0D5307463C73D16E93694E5D940001, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 49], transactionId='null'}]]
Time [Thu Feb 21 10:45:56 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=7, sysFlag=0, bornTimestamp=1550717143448, bornHost=/10.13.83.7:63916, storeTimestamp=1550717143451, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F000000000000374D, commitLogOffset=14157, bodyCRC=1141369005, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest7', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=10, UNIQ_KEY=0A0D5307463C73D16E93694E5D980002, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 50], transactionId='null'}]]
Time [Thu Feb 21 10:45:56 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=8, sysFlag=0, bornTimestamp=1550717143453, bornHost=/10.13.83.7:63916, storeTimestamp=1550717143454, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F00000000000037FF, commitLogOffset=14335, bodyCRC=855693371, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest7', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=10, UNIQ_KEY=0A0D5307463C73D16E93694E5D9D0003, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 51], transactionId='null'}]]
Time [Thu Feb 21 10:45:56 CST 2019],ConsumeMessageThread_1Receive New Messages :[MessageExt [queueId=0, storeSize=178, queueOffset=9, sysFlag=0, bornTimestamp=1550717143455, bornHost=/10.13.83.7:63916, storeTimestamp=1550717143456, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F00000000000038B1, commitLogOffset=14513, bodyCRC=761548184, reconsumeTimes=0, preparedTransactionOffset=0, toString()=Message{topic='TopicTest7', flag=0, properties={MIN_OFFSET=0, MAX_OFFSET=10, UNIQ_KEY=0A0D5307463C73D16E93694E5D9F0004, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 52], transactionId='null'}]]

```

### 3.事务消息

事务消息是指RocketMQ发送的消息和其他本地事件需要同时成功同时失败，可以理解为就是分布式事务；RocketMQ处理事务消息的大致流程如下：  
1.生产者发送"待确认"消息；  
2.RocketMQ接收到消息进行相关保存操作，成功以后返回状态给生产者；  
3.生产者接收到的返回如果为SEND_OK状态，将执行本地事务操作；  
4.根据本地事务执行的结果，生产者执行commit还是rollback；  
5.如果第四步生产者执行操作失败，服务器会在经过固定时间段对状态为"待确认"的消息发起回查请求；  
6.生产者接收到回查请求后根据本地事务的结果返回commit还是rollback；  
7.服务器收到结果后执行相关操作。

接下来看一下官方提供的实例  
TransactionProducerTest生产者类，类似DefaultMQProducer，主要设置了一个事务监听器类TransactionListener，用于开始本地事务会给服务器的回查接口；

```
public class TransactionProducerTest {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        TransactionListener transactionListener = new TransactionListenerImpl();
        TransactionMQProducer producer = new TransactionMQProducer("transactionProducerGroupName");
        producer.setNamesrvAddr("192.168.237.128:9876");
        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("client-transaction-msg-check-thread");
                        return thread;
                    }
                });

        producer.setExecutorService(executorService);
        producer.setTransactionListener(transactionListener);
        producer.start();

        String[] tags = new String[] { "TagA", "TagB", "TagC", "TagD", "TagE" };
        for (int i = 0; i < 1; i++) {
            try {
                Message msg = new Message("TopicTest1234", tags[i % tags.length], "KEY" + i,
                        ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                System.out.println("start send message " + msg);
                SendResult sendResult = producer.sendMessageInTransaction(msg, null);
                System.out.printf("%s%n", sendResult);

                Thread.sleep(10);
            } catch (MQClientException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 100000; i++) {
            Thread.sleep(1000);
        }
        producer.shutdown();
    }
}
```

还有一个监听器类TransactionListenerImpl

```
public class TransactionListenerImpl implements TransactionListener {
    private AtomicInteger transactionIndex = new AtomicInteger(0);

    private ConcurrentHashMap<String, Integer> localTrans = new ConcurrentHashMap<>();

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        System.out.println("executeLocalTransaction");
        int value = transactionIndex.getAndIncrement();
        int status = value % 3;
        localTrans.put(msg.getTransactionId(), status);
        return LocalTransactionState.UNKNOW;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        Integer status = localTrans.get(msg.getTransactionId());
        System.out.println("checkLocalTransaction:status = " + status);
        if (null != status) {
            switch (status) {
            case 0:
                return LocalTransactionState.UNKNOW;
            case 1:
                return LocalTransactionState.COMMIT_MESSAGE;
            case 2:
                return LocalTransactionState.ROLLBACK_MESSAGE;
            default:
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        }
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
```

以上监听器需要实现两个接口方法，分别是执行本地事务的方法和用于被服务器回调的方法；运行以上生产者相关日志如下：

```
start send message Message{topic='TopicTest1234', flag=0, properties={KEYS=KEY0, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 48], transactionId='null'}
executeLocalTransaction
SendResult [sendStatus=SEND_OK, msgId=0A0D5307383473D16E936A31CF040000, offsetMsgId=null, messageQueue=MessageQueue [topic=TopicTest1234, brokerName=broker-a, queueId=2], queueOffset=47]
checkLocalTransaction:status = 0
checkLocalTransaction:status = 0
checkLocalTransaction:status = 0
```

从日志可以看出，首先发送"待确认"消息，发送返回为SEND\_OK；然后执行本地事务，实例中返回的是一个LocalTransactionState.UNKNOW状态，导致服务器一直调用回查方法checkLocalTransaction，同时消费端一直没有消息被消费；做简单代码改动，将本地事务的执行结果改成LocalTransactionState.COMMIT\_MESSAGE，生产者消费者日志如下：

```
start send message Message{topic='TopicTest1234', flag=0, properties={KEYS=KEY0, WAIT=true, TAGS=TagA}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 48], transactionId='null'}
executeLocalTransaction
SendResult [sendStatus=SEND_OK, msgId=0A0D530740F073D16E936A3A2DC10000, offsetMsgId=null, messageQueue=MessageQueue [topic=TopicTest1234, brokerName=broker-a, queueId=2], queueOffset=58]

```

```
Time [Thu Feb 21 15:03:17 CST 2019],ConsumeMessageThread_3Receive New Messages :[MessageExt [queueId=2, storeSize=278, queueOffset=0, sysFlag=8, bornTimestamp=1550732597697, bornHost=/10.13.83.7:55029, storeTimestamp=1550732597758, storeHost=/10.13.83.7:10911, msgId=0A0D530700002A9F0000000000008853, commitLogOffset=34899, bodyCRC=613185359, reconsumeTimes=0, preparedTransactionOffset=34610, toString()=Message{topic='TopicTest1234', flag=0, properties={MIN_OFFSET=0, REAL_TOPIC=TopicTest1234, MAX_OFFSET=1, KEYS=KEY0, TRAN_MSG=true, UNIQ_KEY=0A0D530740F073D16E936A3A2DC10000, WAIT=true, PGROUP=transactionProducerGroupName, TAGS=TagA, REAL_QID=2}, body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 48], transactionId='0A0D530740F073D16E936A3A2DC10000'}]]

```

可以看到消息状态在服务器端被修改，这样消费端就可以消费此消息；

## 总结

本文首先介绍了RocketMQ发送消息的通讯模式，然后重点介绍了延迟消息，顺序消息以及事务消息，并且结合实例进行分析。

## 示例代码地址

[https://github.com/ksfzhaohui...](https://github.com/ksfzhaohui/blog)  
[https://gitee.com/OutOfMemory...](https://gitee.com/OutOfMemory/blog)