#JMS消息确认和事务

## **保证消息传送**

保证消息传送有3个主要部分：消息自主性，存储并转发以及底层消息确认，下面具体看一下这些概念；

### 1.消息自主性

消息是自包含的自主性实体，在设计分布式消息应用程序时，要将此作为头条法则；当JMS客户端发送一条消息时，它就完成了它的所有工作，一旦该数据被传送出去，它就被认为是”安全的”，  
而且不在受该客户端的控制，类似JDBC客户端和数据库直接的约定；

### 2.保存并转发消息传送

可以将消息标记为持久化的，这样JMS服务器负责保存消息，以确保在提供者发生故障或消费客户端发生故障的情况下，消息可以恢复正常；消息可以集中存储或本地存储；

### 3.消息确认

JMS规定了多种确认模式，这些确认是保证消息传送的关键部分；服务器确认从JMS生产者接受消息，而JMS消费者确认从服务器接受消息；确认协议允许JMS提供者监测一条消息的整个过程，  
以便了解是否成功的生产和消费了该消息；

## **消息确认**

消息确认协议是保证消息传送的关键所在，JMS主要定义了三种确认模式：auto\_acknowledge,dups\_ok\_acknowledge以及client\_acknowledge；

### 1.auto_acknowledge

auto_acknowledge是JMS提供的自动确认模式，下面分别从生产者和消费者角度来分析，以下使用ActiveMQ来作为消息服务器；

#### 1.1生产者和消息服务器

生产者调用send()或者publish()方法发送消息，同时进行阻塞，直到从消息服务器接收到一个确认为止；底层确认对客户端编程模型来说是不可见的，如果在操作期间发生故障，就会抛出一个异常，同时认为该消息没有被传送；消息服务器接收到消息，如果是持久性消息就会持久化到磁盘，如果是非持久性消息就会存入内存，然后再通知生产者已经接收到消息；  
![](https://oscimg.oschina.net/oscnet/41bb3fe7dbbf46407d477226d9812741287.jpg)

上图中可能出现的异常  
1.1.1.发送消息失败  
可能由于网络原因导致发送消息失败，服务器没有感知，需要生产者做好异常检测或者重发机制；

1.1.2.持久化失败  
生产者成功发送消息给服务器，服务器在持久化时失败，服务器会在通知的时候，把错误信息返回给生产者，需要生产者做好异常检测；

1.1.3.服务器通知生产者失败  
成功接收消息和持久化，在通知生产者时，出现网络异常导致失败，服务器会将此消息删除，生产者会从阻塞中返回并抛出异常；

#### 1.2消息服务器和消费者

消费者获取到消息之后，需要向服务器发送确认信息，如果服务器没有接收到确认信息，会认为该消息未被传送，会试图重新传送；如果接收到确认消息，此消息将会从持久化存储器中删除；  
![](https://oscimg.oschina.net/oscnet/dc17436bf38530a66b78850c34d6bad317d.jpg)

上图中可能出现的异常  
1.2.1.接收消息失败  
对于Queue模型来说，是主动拉取消息，在没有成功拉取数据的情况下，服务器自然不会删除数据；对于Topic模型来说，消息服务器会推送给每个消费者一个消息的副本，如果是持久订阅者，一直到消息服务器接收到所有消息预定接收者的确认时，才会认为完成传送；如果是非持久订阅，就不会关心某一个接收者是否接收到消息；

1.2.2.消费者通知服务器失败  
消费者成功接收到消息，但是在处理完之后，通知服务器失败，导致服务器没有被删除，消息会被重发，消费者要做好幂等性处理；

1.2.3.删除持久化失败  
消费者成功接收到消息，服务器成功接收通知信息，在删除持久化数据时失败，导致数据没有被删除，消息会再次被消费，消费者要做好幂等性处理；

#### 1.3实例分析

1.3.1.准备ActiveMq作为服务器  
使用apache-activemq-5.15.4作为服务器，使用mysql作为持久化存储器，activemq.xml做如下配置：

```
<bean id="mysql-ds" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost/activemq?relaxAutoCommit=true"/>
    <property name="username" value="root"/>
    <property name="password" value="root"/>
    <property name="poolPreparedStatements" value="true"/>
</bean>
```

1.3.2.准备消息发送器  
使用如下实例做消息发送器，本实例使用Queue模型进行分析

```
public class QSender {
 
    private QueueConnectionFactory factory;
    private QueueConnection qConnection;
    private QueueSession qSession;
    private Queue queue;
    private QueueSender qSender;
 
    public QSender() {
        try {
            factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            qConnection = factory.createQueueConnection();
            qConnection.start();
 
            qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = qSession.createQueue("test");
            qSender = qSession.createSender(queue);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("初始化生产者失败");
        }
    }
 
    private void sendMessage(String text) {
        try {
            TextMessage message = qSession.createTextMessage(text);
            qSender.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
            System.err.println("发送消息失败，生产者做重发处理");
        }
    }
 
    private void exit() {
        try {
            if (qConnection != null) {
                qConnection.close();
            }
            if (qSession != null) {
                qSession.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) throws Exception {
        QSender sender = new QSender();
        String message = "test消息";
        System.out.println("准备发送消息：" + message);
        sender.sendMessage(message);
        System.out.println("消息已发送");
        sender.exit();
    }
 
}
```

1.3.3.准备消息接收器

```
public class QReceiverListener implements MessageListener {
 
    private QueueConnectionFactory factory;
    private QueueConnection qConnection;
    private QueueSession qSession;
    private Queue queue;
    private QueueReceiver qReceiver;
 
    public QReceiverListener() {
        try {
            factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            qConnection = factory.createQueueConnection();
            qConnection.start();
 
            qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = qSession.createQueue("test");
            qReceiver = qSession.createReceiver(queue);
            qReceiver.setMessageListener(this);
            System.out.println("等待接受消息......");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            System.out.println("消息内容：" + textMessage.getText() + ",是否重发：" + textMessage.getJMSRedelivered());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) throws IOException {
        new QReceiverListener();
    }
 
}
```

这里使用的是消息监听器的方式，有消息自动调用onMessage方法，当然也可以直接循环使用qReceiver.receive()方法；其实监听器方式本质上也是有一个consumer thread去不停的读取消息，具体可以查看类TcpTransport；

1.3.4.QSender测试分析  
运行QSender发送一条消息，QSender阻塞等待服务器返回通知信息，接收到成功通知，Qsender停止阻塞，执行其他的逻辑，结果如下：

```
准备发送消息：test消息
消息已发送
```

查看mysql数据库

```
mysql> select * from activemq_msgs;
+----+--------------+---------------------------------------------+-----------+------------+-----+----------+------+
| ID | CONTAINER    | MSGID_PROD                                  | MSGID_SEQ | EXPIRATION | MSG | PRIORITY | XID  |
+----+--------------+---------------------------------------------+-----------+------------+-----+----------+------+
| 15 | queue://test | ID:NJD9YZGJ2-PC-61961-1533606386511-1:1:1:1 |         1 |          0 | |        0 | NULL |
+----+--------------+---------------------------------------------+-----------+------------+-----+----------+------+
```

在发送器中send()方法会抛出一个JMSException异常，此异常是服务器返回异常的包装类，可以查看ActiveMQConnection部分源码：

```
public Response syncSendPacket(Command command, int timeout) throws JMSException {
    if (isClosed()) {
        throw new ConnectionClosedException();
    } else {
 
        try {
            Response response = (Response)(timeout > 0
                    ? this.transport.request(command, timeout)
                    : this.transport.request(command));
            if (response.isException()) {
                ExceptionResponse er = (ExceptionResponse)response;
                if (er.getException() instanceof JMSException) {
                    throw (JMSException)er.getException();
                } else {
                    if (isClosed()||closing.get()) {
                        LOG.debug("Received an exception but connection is closing");
                    }
                    JMSException jmsEx = null;
                    try {
                        jmsEx = JMSExceptionSupport.create(er.getException());
                    } catch(Throwable e) {
                        LOG.error("Caught an exception trying to create a JMSException for " +er.getException(),e);
                    }
                    if (er.getException() instanceof SecurityException && command instanceof ConnectionInfo){
                        forceCloseOnSecurityException(er.getException());
                    }
                    if (jmsEx !=null) {
                        throw jmsEx;
                    }
                }
            }
            return response;
        } catch (IOException e) {
            throw JMSExceptionSupport.create(e);
        }
    }
}
```

在发送消息的时候，可以指定一个超时时间，在指定时间内没有接收到服务器的通知消息，直接认为获取通知信息失败，抛出超时异常；正常情况下，生产者会接收到Response，此类中有方法isException()方法，判定是否有异常，如果有异常会将异常包装成JMSException，抛给生产者；

1.3.5.QReceiverListener测试与分析  
运行QReceiverListener，接收器会启动一个consumer thread专门去读取消息，读取到消息之后经过一系列处理之后，会调用onMessage()方法，此方法中需要读取消息，并进行业务逻辑处理，处理完之后会自动给服务器发送确认消息；确认消息非常重要，用来决定服务器是否会删除消息，不删除的话，消息会被重复消费，结果如下：

```
等待接受消息......
消息内容：test消息,是否重发：false
```

一次成功接收消息，重发标识为false；

查看mysql数据库

```
mysql> select * from activemq_msgs;
Empty set
```

具体可以看一下ActiveMQMessageConsumer中的部分代码：

```
if (listener != null && unconsumedMessages.isRunning()) {
    if (redeliveryExceeded(md)) {
        posionAck(md, "listener dispatch[" + md.getRedeliveryCounter() + "] to " + getConsumerId() + " exceeds redelivery policy limit:" + redeliveryPolicy);
        return;
    }
    ActiveMQMessage message = createActiveMQMessage(md);
    beforeMessageIsConsumed(md);
    try {
        boolean expired = isConsumerExpiryCheckEnabled() && message.isExpired();
        if (!expired) {
            listener.onMessage(message);
        }
        afterMessageIsConsumed(md, expired);
    } catch (RuntimeException e) {
        LOG.error("{} Exception while processing message: {}", getConsumerId(), md.getMessage().getMessageId(), e);
        md.setRollbackCause(e);
        if (isAutoAcknowledgeBatch() || isAutoAcknowledgeEach() || session.isIndividualAcknowledge()) {
            // schedual redelivery and possible dlq processing
            rollback();
        } else {
            // Transacted or Client ack: Deliver the next message.
            afterMessageIsConsumed(md, false);
        }
    }
}
```

可以看出大致处理流程首先生成了业务逻辑需要的ActiveMQMessage，然后执行beforeMessageIsConsumed()消息被消耗之前的处理，接着就是执行onMessage()，处理业务逻辑，如果处理成功，就执行afterMessageIsConsumed()给服务器发送确认信息；如果抛出RuntimeException异常，则rollback()回滚操作，rollback()里面会处理重发，并且设置了最大的重发次数(类RedeliveryPolicy中存放了变量DEFAULT\_MAXIMUM\_REDELIVERIES=6)，没有超过重发次数的情况下会发送一个MessageAck.REDELIVERED\_ACK\_TYPE消息类型，告诉服务器需要重发；否则发送一个MessageAck.POSION\_ACK\_TYPE消息类型，大致告诉服务器此消息不能被处理，可以删除了，下面模拟一下，只需要在onMessage方法最后抛出异常：

```
public void onMessage(Message message) {
    try {
        TextMessage textMessage = (TextMessage) message;
        System.out.println("消息内容：" + textMessage.getText() + ",是否重发：" + textMessage.getJMSRedelivered());
        throw new RuntimeException("test");
    } catch (JMSException e) {
        e.printStackTrace();
    }
}
```

执行之后日志如下：

```
消息内容：test消息,是否重发：true
ERROR | ID:NJD9YZGJ2-PC-49998-1533611501812-1:1:1:1 Exception while processing message: ID:NJD9YZGJ2-PC-49981-1533611498161-1:1:1:1:1
java.lang.RuntimeException: test
    at zh.maven.jmsClient.queue.listener.QReceiverListener.onMessage(QReceiverListener.java:47)
    at org.apache.activemq.ActiveMQMessageConsumer.dispatch(ActiveMQMessageConsumer.java:1404)
    at org.apache.activemq.ActiveMQMessageConsumer.iterate(ActiveMQMessageConsumer.java:1575)
    at org.apache.activemq.ActiveMQSessionExecutor.iterate(ActiveMQSessionExecutor.java:191)
    at org.apache.activemq.thread.PooledTaskRunner.runTask(PooledTaskRunner.java:133)
    at org.apache.activemq.thread.PooledTaskRunner$1.run(PooledTaskRunner.java:48)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
    at java.lang.Thread.run(Thread.java:745)
......
```

接收到如上6条消息之后，不再重复，消息被删除；

### 2.dups\_ok\_acknowledge

指示JMS生产者可以将一条消息向同一目的地发送两次以上；dups\_ok\_acknowledge模式基于以下假设：用于确保”一次而且仅仅一次”传送而必须的处理，会在提供者级别上导致额外开销，还会影响系统的性能和消息吞吐量，允许接受重复消息的应用程序，可以使用dups\_ok\_acknowledge模式来避免这种开销；  
在ActiveMQ中表示并不是没接收一条消息就确认，而是可以接收一个批次后才确认，具体可以查看afterMessageIsConsumed()方法中的部分代码：

```
if (session.getTransacted()) {
       // Do nothing.
} else if (isAutoAcknowledgeEach()) {
       ......
} else if (isAutoAcknowledgeBatch()) {
      ......
} else if (session.isClientAcknowledge()||session.isIndividualAcknowledge()) {
      ......
} else {
   throw new IllegalStateException("Invalid session state.");
}
         
private boolean isAutoAcknowledgeEach() {
    return session.isAutoAcknowledge() || ( session.isDupsOkAcknowledge() && getDestination().isQueue() );
}
 
private boolean isAutoAcknowledgeBatch() {
    return session.isDupsOkAcknowledge() && !getDestination().isQueue() ;
}
```

大致分成了三种确认方式：没接受一条消息确认一次，等接收一个批次再确认以及手动指定确认；

### 3.client_acknowledge

此模式可以控制何时发送确认消息，具体使用message.acknowledge()方法，当然只有在client_acknowledge模式下才有效，其他2个模式直接忽略；

```
@Override
public void onMessage(Message message) {
    try {
        TextMessage textMessage = (TextMessage) message;
        System.out.println("消息内容：" + textMessage.getText() + ",是否重发：" + textMessage.getJMSRedelivered());
        textMessage.acknowledge();
        throw new RuntimeException("test");
    } catch (JMSException e) {
        e.printStackTrace();
    }
}
```

简单模拟一下，在接收到消息之后直接确认，后续处理业务发生错误，这种情况下消息不会被重发；

## **事务性消息**

一个事务性发送，其中一组消息要么能够全部保证到达服务器，要么都不到达服务器，生产者、消费者与消息服务器直接都支持事务性；

### 1.事务性发送

![](https://oscimg.oschina.net/oscnet/078ae27c1e8fca773cc582ba8f9d0383140.jpg)

从生产者角度的来看，JMS提供者为这组消息提供了高速缓存，直到执行commit()命令，如果发生了故障或者执行rollback()，这些消息会丢失；

### 2.事务性接收

![](https://oscimg.oschina.net/oscnet/b1530f201fb5db1bf2aa44ec0f2a240c217.jpg)

从接收者的角度来看，这些消息会尽快的传送给接收者，但是他们一直由JMS提供者保存，知道接收者在会话对象上执行commit()为止；如果发生故障或者执行rollback()，提供者会重新发送这些消息，这些消息会被标志为重新传送；

### 3.事务性发送和接收

![](https://oscimg.oschina.net/oscnet/456349f462511a25fa75433911f5841d351.jpg)

如果事务性生产者和事务性消费者由同一会话创建，那么他们就能够组合在单个事务中；这样一来，JMS客户端就可以作为单独的工作单元生产和消费消息；

### 4.实例分析

QSender做如下改动：

```
qSession = qConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
 
private void sendMessage(String text) {
        try {
            for (int i = 0; i < 10; i++) {
                TextMessage message = qSession.createTextMessage(text + i);
                qSender.send(message);
            }
            qSender.send(qSession.createTextMessage("end"));
            qSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                qSession.rollback();
            } catch (JMSException e1) {
            }
            System.err.println("发送消息失败，生产者做重发处理");
        }
    }
```

指定QueueSession为事务性会话，发送完之后执行commit()，失败执行rollback();

QReceiver做如下改动：

```
qSession = qConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
@Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            System.out.println("消息内容：" + textMessage.getText() + ",是否重发：" + textMessage.getJMSRedelivered());
            if (textMessage.getText().equals("end")) {
                qSession.commit();
            }
        } catch (JMSException e) {
            try {
                qSession.rollback();
            } catch (JMSException e1) {
            }
            e.printStackTrace();
        }
    }
```

在接收完end结束标志之后，执行commit()方法，高速服务器接收完成；当然这里使用非事务性消费者也是可以接收消息的，事务的范围仅限于生产者或消费者与消息服务器的会话；可以发现JMS的事务和JDBC提供的事务很像，本质上提供的是本地事务；不过如果要跨越多个会话、队列、主题和数据库之间协调单个事务，那仅仅本地事务是不够的，这时候需要分布式事务；

### 5.分布式事务

允许多个资源参与到一个事务中，这些资源可以是数据库，JMS等等；JMS规范提供了下列JMS对象的XA版本：XAConnection、XAConnectionFactory、XAQueueConnection、XAQueueConnectionFactory、XAQueueSession、XASession、XATopicConnection、XATopicConnectionFactory、XATopicSession；具体的消息服务器去实现这些接口，让JMS也可以参与到全局事务中。

## **总结**

本文介绍了一下JMS的消息确认模式和本地事务，并以ActiveMQ作为服务器来做测试和分析，大体上了解了JMS的确认机制；重点介绍了一下本地事务，至于分布式事务一笔带过，其实在处理分布式事务的问题，MQ应用广泛实现最终一致性，这个可以深入分析一下。

## **参考**

java消息服务

## **示例代码地址**

[https://github.com/ksfzhaohui...](https://github.com/ksfzhaohui/blog)
