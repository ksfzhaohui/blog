## 前言

生产者向消息队列里面写入消息，不同的业务场景会采用不同的写入策略，比如：同步发送，异步发送，延迟发送，事务消息等；本文首先从分析生产者发送消息的流程开始，然后再来介绍各种发送消息的策略。

## 生产者流程

### 1.流程概述

生产者首先需要设置namesrv，或者指定其他方式更新namesrv；然后从namesrv获取topic的路由信息，路由信息包括broker以及Message Queue等信息，同时将路由信息保存在本地内存中，方便下次使用；最后从Message Queue列表中选择合适的Queue发送消息，实现负载均衡；

### 2.启动过程

DefaultMQProducer实例化提供了两个参数分别是：生产者组名称以及RPCHook，RPCHook是一个接口，具体实现交由业务端实现，两个方法分别是：doBeforeRequest和doAfterResponse，表示在执行请求之前和接收返回之后分别执行相关逻辑；  
接下来就是调用DefaultMQProducer的start方法，相关的初始化操作都在里面进行，内部其实调用的是DefaultMQProducerImpl的start方法，具体代码如下：

```
public void start(final boolean startFactory) throws MQClientException {
        switch (this.serviceState) {
            case CREATE_JUST:
                this.serviceState = ServiceState.START_FAILED;

                this.checkConfig();

                if (!this.defaultMQProducer.getProducerGroup().equals(MixAll.CLIENT_INNER_PRODUCER_GROUP)) {
                    this.defaultMQProducer.changeInstanceNameToPID();
                }

                this.mQClientFactory = MQClientManager.getInstance().getAndCreateMQClientInstance(this.defaultMQProducer, rpcHook);

                boolean registerOK = mQClientFactory.registerProducer(this.defaultMQProducer.getProducerGroup(), this);
                if (!registerOK) {
                    this.serviceState = ServiceState.CREATE_JUST;
                    throw new MQClientException("The producer group[" + this.defaultMQProducer.getProducerGroup()
                        + "] has been created before, specify another name please." + FAQUrl.suggestTodo(FAQUrl.GROUP_NAME_DUPLICATE_URL),
                        null);
                }

                this.topicPublishInfoTable.put(this.defaultMQProducer.getCreateTopicKey(), new TopicPublishInfo());

                if (startFactory) {
                    mQClientFactory.start();
                }

                log.info("the producer [{}] start OK. sendMessageWithVIPChannel={}", this.defaultMQProducer.getProducerGroup(),
                    this.defaultMQProducer.isSendMessageWithVIPChannel());
                this.serviceState = ServiceState.RUNNING;
                break;
            case RUNNING:
            case START_FAILED:
            case SHUTDOWN_ALREADY:
                throw new MQClientException("The producer service state not OK, maybe started once, "
                    + this.serviceState
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_SERVICE_NOT_OK),
                    null);
            default:
                break;
        }

        this.mQClientFactory.sendHeartbeatToAllBrokerWithLock();
    }
```

默认serviceState的状态为CREATE\_JUST，刚进入设置为START\_FAILED，初始化完成之后设置为RUNNING，再次初始化时会直接报错，下面看一下具体初始化了哪些信息；

#### 2.1检查配置

这里的检查其实就是对producerGroup进行合法性校验；

#### 2.2设置instanceName

如果producerGroup不等于默认的"CLIENT\_INNER\_PRODUCER",则设置DefaultMQProducer的instanceName为进程的pid；

#### 2.3创建MQClientInstance对象

首先检查 ConcurrentMap<String/_ clientId _/, MQClientInstance> factoryTable中是否已经存在已clientId为key的MQClientInstance，如果存在则取出，不存在则实例化；clientId是已ip地址，instanceName以及unitName组成的，例如：10.13.83.7[@12500](https://my.oschina.net/12500)

#### 2.4注册producer

将DefaultMQProducerImpl注册到MQClientInstance中，已producerGroup作为key，注册到ConcurrentMap<String/_group _/, MQProducerInner> producerTable中，如果已经存在此Group，则抛出异常；

#### 2.5初始化TopicPublishInfo

已topic名称为"TBW102"为key，实例化TopicPublishInfo作为value，存放在ConcurrentMap<String/_ topic _/, TopicPublishInfo> topicPublishInfoTable中,TopicPublishInfo用来存放topic的路由信息；

#### 2.6启动MQClientInstance

MQClientInstance启动会启动很多相关服务，具体可以看如下代码：

```
 public void start() throws MQClientException {

        synchronized (this) {
            switch (this.serviceState) {
                case CREATE_JUST:
                    this.serviceState = ServiceState.START_FAILED;
                    // If not specified,looking address from name server
                    if (null == this.clientConfig.getNamesrvAddr()) {
                        this.mQClientAPIImpl.fetchNameServerAddr();
                    }
                    // Start request-response channel
                    this.mQClientAPIImpl.start();
                    // Start various schedule tasks
                    this.startScheduledTask();
                    // Start pull service
                    this.pullMessageService.start();
                    // Start rebalance service
                    this.rebalanceService.start();
                    // Start push service
                    this.defaultMQProducer.getDefaultMQProducerImpl().start(false);
                    log.info("the client factory [{}] start OK", this.clientId);
                    this.serviceState = ServiceState.RUNNING;
                    break;
                case RUNNING:
                    break;
                case SHUTDOWN_ALREADY:
                    break;
                case START_FAILED:
                    throw new MQClientException("The Factory object[" + this.getClientId() + "] has been created before, and failed.", null);
                default:
                    break;
            }
        }
    }
```

默认serviceState的状态为CREATE\_JUST，刚进入设置为START\_FAILED，初始化完成之后设置为RUNNING，防止重复初始化；

2.6.1初始化NameServerAddr

首先判断DefaultMQProducer是否配置了NameServerAddr，如果没有配置会到一个地址下获取，地址默认为：[http://jmenv.tbsite.net](http://jmenv.tbsite.net/):8080/rocketmq/nsaddr，相关的逻辑在MixAll类中，代码如下：

```
    public static String getWSAddr() {
        String wsDomainName = System.getProperty("rocketmq.namesrv.domain", DEFAULT_NAMESRV_ADDR_LOOKUP);
        String wsDomainSubgroup = System.getProperty("rocketmq.namesrv.domain.subgroup", "nsaddr");
        String wsAddr = "http://" + wsDomainName + ":8080/rocketmq/" + wsDomainSubgroup;
        if (wsDomainName.indexOf(":") > 0) {
            wsAddr = "http://" + wsDomainName + "/rocketmq/" + wsDomainSubgroup;
        }
        return wsAddr;
    }
```

正常情况下我们需要设置自己的地址，可以通过如下方式设置：

```
System.setProperty("rocketmq.namesrv.domain", "localhost");
```

这种情况下就可以不用手动设置NameServerAddr；

2.6.2初始化RemotingClient

RemotingClient是一个接口类，底层使用的通讯框架是Netty，提供了实现类NettyRemotingClient，RemotingClient在初始化的时候实例化Bootstrap，方便后续用来创建Channel；

2.6.3启动定时器任务

总共启动了5个定时器任务，分别是：定时更新NameServerAddr信息，定时更新topic的路由信息，定时清理下线的broker，定时持久化Consumer的Offset信息，定时调整线程池；

2.6.3启动服务

pullMessageService和rebalanceService被用在消费端的两个服务类，分别是：从broker拉取消息的服务和均衡消息队列服务，负责分配消费者可消费的消息队列；

### 3.发送消息

相关发送消息的代码在DefaultMQProducerImpl的sendDefaultImpl方法中，部分代码如下所示：

```
private SendResult sendDefaultImpl(
        Message msg,
        final CommunicationMode communicationMode,
        final SendCallback sendCallback,
        final long timeout
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        this.makeSureStateOK();
        Validators.checkMessage(msg, this.defaultMQProducer);

        final long invokeID = random.nextLong();
        long beginTimestampFirst = System.currentTimeMillis();
        long beginTimestampPrev = beginTimestampFirst;
        long endTimestamp = beginTimestampFirst;
        TopicPublishInfo topicPublishInfo = this.tryToFindTopicPublishInfo(msg.getTopic());
        if (topicPublishInfo != null && topicPublishInfo.ok()) {
            boolean callTimeout = false;
            MessageQueue mq = null;
            Exception exception = null;
            SendResult sendResult = null;
            int timesTotal = communicationMode == CommunicationMode.SYNC ? 1 + this.defaultMQProducer.getRetryTimesWhenSendFailed() : 1;
            int times = 0;
            String[] brokersSent = new String[timesTotal];
            for (; times < timesTotal; times++) {
                String lastBrokerName = null == mq ? null : mq.getBrokerName();
                MessageQueue mqSelected = this.selectOneMessageQueue(topicPublishInfo, lastBrokerName);
                if (mqSelected != null) {
                    mq = mqSelected;
                    brokersSent[times] = mq.getBrokerName();
                    try {
                        beginTimestampPrev = System.currentTimeMillis();
                        long costTime = beginTimestampPrev - beginTimestampFirst;
                        if (timeout < costTime) {
                            callTimeout = true;
                            break;
                        }

                        sendResult = this.sendKernelImpl(msg, mq, communicationMode, sendCallback, topicPublishInfo, timeout - costTime);
                        endTimestamp = System.currentTimeMillis();
                        this.updateFaultItem(mq.getBrokerName(), endTimestamp - beginTimestampPrev, false);
                        switch (communicationMode) {
                            case ASYNC:
                                return null;
                            case ONEWAY:
                                return null;
                            case SYNC:
                                if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                                    if (this.defaultMQProducer.isRetryAnotherBrokerWhenNotStoreOK()) {
                                        continue;
                                    }
                                }

                                return sendResult;
                            default:
                                break;
                        }
                        ...以下代码省略...
```

此方法的四个参数分别是：msg为要发送的消息，communicationMode要使用的发送方式包括同步、异步、单向，sendCallback在异步情况下的回调函数，timeout发送消息的超时时间；

#### 3.1获取topic的路由信息

通过msg中设置的topic获取路由信息，具体代码如下：

```
private TopicPublishInfo tryToFindTopicPublishInfo(final String topic) {
        TopicPublishInfo topicPublishInfo = this.topicPublishInfoTable.get(topic);
        if (null == topicPublishInfo || !topicPublishInfo.ok()) {
            this.topicPublishInfoTable.putIfAbsent(topic, new TopicPublishInfo());
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic);
            topicPublishInfo = this.topicPublishInfoTable.get(topic);
        }

        if (topicPublishInfo.isHaveTopicRouterInfo() || topicPublishInfo.ok()) {
            return topicPublishInfo;
        } else {
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic, true, this.defaultMQProducer);
            topicPublishInfo = this.topicPublishInfoTable.get(topic);
            return topicPublishInfo;
        }
    }
```

首先从变量ConcurrentMap<String/_ topic _/, TopicPublishInfo> topicPublishInfoTable中获取是否存在指定topic的路由信息，如果获取不到则使用topic去nameServer获取路由信息，如果还是获取不到则使用默认的topic名称为"TBW102"去获取路由信息，需要使用默认名称去获取的情况是没有创建topic，需要broker自动创建topic的情况；获取路由信息使用的是MQClientInstance中的updateTopicRouteInfoFromNameServer方法，此方法根据topic获取路由信息，具体连接哪台nameServer，会从列表中顺序的选择nameServer，实现负载均衡；  
注：名称为"TBW102"的topic是系统自动创建的；

#### 3.2选择MessageQueue

成功获取到路由信息之后，需要从MessageQueue列表中选择一个，在这之前获取了默认发送消息失败的重试次数，默认为3次(只有发送模式是同步的情况下)，代码如下：

```
public MessageQueue selectOneMessageQueue(final TopicPublishInfo tpInfo, final String lastBrokerName) {
        if (this.sendLatencyFaultEnable) {
            try {
                int index = tpInfo.getSendWhichQueue().getAndIncrement();
                for (int i = 0; i < tpInfo.getMessageQueueList().size(); i++) {
                    int pos = Math.abs(index++) % tpInfo.getMessageQueueList().size();
                    if (pos < 0)
                        pos = 0;
                    MessageQueue mq = tpInfo.getMessageQueueList().get(pos);
                    if (latencyFaultTolerance.isAvailable(mq.getBrokerName())) {
                        if (null == lastBrokerName || mq.getBrokerName().equals(lastBrokerName))
                            return mq;
                    }
                }

                final String notBestBroker = latencyFaultTolerance.pickOneAtLeast();
                int writeQueueNums = tpInfo.getQueueIdByBroker(notBestBroker);
                if (writeQueueNums > 0) {
                    final MessageQueue mq = tpInfo.selectOneMessageQueue();
                    if (notBestBroker != null) {
                        mq.setBrokerName(notBestBroker);
                        mq.setQueueId(tpInfo.getSendWhichQueue().getAndIncrement() % writeQueueNums);
                    }
                    return mq;
                } else {
                    latencyFaultTolerance.remove(notBestBroker);
                }
            } catch (Exception e) {
                log.error("Error occurred when selecting message queue", e);
            }

            return tpInfo.selectOneMessageQueue();
        }

        return tpInfo.selectOneMessageQueue(lastBrokerName);
    }
```

以上代码在MQFaultStrategy，此类提供了选择MessageQueue的策略，相关策略可以看类变量：

```
private final LatencyFaultTolerance<String> latencyFaultTolerance = new LatencyFaultToleranceImpl();

    private boolean sendLatencyFaultEnable = false;

    private long[] latencyMax = {50L, 100L, 550L, 1000L, 2000L, 3000L, 15000L};
    private long[] notAvailableDuration = {0L, 0L, 30000L, 60000L, 120000L, 180000L, 600000L};
```

latencyFaultTolerance：延迟容错对象，维护brokers的延迟信息；  
sendLatencyFaultEnable：延迟容错开关，默认不开启；  
latencyMax：延迟级别数组；  
notAvailableDuration ：根据延迟级别，对应broker不可用的时长；

这样上面的这段代码就好理解了，首先判定是否开启开关，如果开启首先获取index，index初始为一个随机值，后面每次+1，这样在后面与MessageQueue长度取模的时候每个MessageQueue可以按顺序获取；获取MessageQueue之后需要判定其对应的Broker是否可用，同时也需要和当前指定的brokerName进行匹配；如果没有获取到就选择一个延迟相对小的，pickOneAtLeast会做排序处理；如果都不行就直接获取一个MessageQueue，不管其他条件了；

#### 3.3发送消息

在发送之前会做超时检测，如果前面两步已经超时了，则直接报超时，默认超时时间是3秒；部分代码如下：

```
    private SendResult sendKernelImpl(final Message msg,
                                      final MessageQueue mq,
                                      final CommunicationMode communicationMode,
                                      final SendCallback sendCallback,
                                      final TopicPublishInfo topicPublishInfo,
                                      final long timeout) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        long beginStartTime = System.currentTimeMillis();
        String brokerAddr = this.mQClientFactory.findBrokerAddressInPublish(mq.getBrokerName());
        if (null == brokerAddr) {
            tryToFindTopicPublishInfo(mq.getTopic());
            brokerAddr = this.mQClientFactory.findBrokerAddressInPublish(mq.getBrokerName());
        }

        SendMessageContext context = null;
        if (brokerAddr != null) {
            brokerAddr = MixAll.brokerVIPChannel(this.defaultMQProducer.isSendMessageWithVIPChannel(), brokerAddr);

            byte[] prevBody = msg.getBody();
            try {
                //for MessageBatch,ID has been set in the generating process
                if (!(msg instanceof MessageBatch)) {
                    MessageClientIDSetter.setUniqID(msg);
                }

                int sysFlag = 0;
                boolean msgBodyCompressed = false;
                if (this.tryToCompressMessage(msg)) {
                    sysFlag |= MessageSysFlag.COMPRESSED_FLAG;
                    msgBodyCompressed = true;
                }

                final String tranMsg = msg.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED);
                if (tranMsg != null && Boolean.parseBoolean(tranMsg)) {
                    sysFlag |= MessageSysFlag.TRANSACTION_PREPARED_TYPE;
                }

                if (hasCheckForbiddenHook()) {
                    CheckForbiddenContext checkForbiddenContext = new CheckForbiddenContext();
                    checkForbiddenContext.setNameSrvAddr(this.defaultMQProducer.getNamesrvAddr());
                    checkForbiddenContext.setGroup(this.defaultMQProducer.getProducerGroup());
                    checkForbiddenContext.setCommunicationMode(communicationMode);
                    checkForbiddenContext.setBrokerAddr(brokerAddr);
                    checkForbiddenContext.setMessage(msg);
                    checkForbiddenContext.setMq(mq);
                    checkForbiddenContext.setUnitMode(this.isUnitMode());
                    this.executeCheckForbiddenHook(checkForbiddenContext);
                }

                if (this.hasSendMessageHook()) {
                    context = new SendMessageContext();
                    context.setProducer(this);
                    context.setProducerGroup(this.defaultMQProducer.getProducerGroup());
                    context.setCommunicationMode(communicationMode);
                    context.setBornHost(this.defaultMQProducer.getClientIP());
                    context.setBrokerAddr(brokerAddr);
                    context.setMessage(msg);
                    context.setMq(mq);
                    String isTrans = msg.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED);
                    if (isTrans != null && isTrans.equals("true")) {
                        context.setMsgType(MessageType.Trans_Msg_Half);
                    }

                    if (msg.getProperty("__STARTDELIVERTIME") != null || msg.getProperty(MessageConst.PROPERTY_DELAY_TIME_LEVEL) != null) {
                        context.setMsgType(MessageType.Delay_Msg);
                    }
                    this.executeSendMessageHookBefore(context);
                }

                SendMessageRequestHeader requestHeader = new SendMessageRequestHeader();
                requestHeader.setProducerGroup(this.defaultMQProducer.getProducerGroup());
                requestHeader.setTopic(msg.getTopic());
                requestHeader.setDefaultTopic(this.defaultMQProducer.getCreateTopicKey());
                requestHeader.setDefaultTopicQueueNums(this.defaultMQProducer.getDefaultTopicQueueNums());
                requestHeader.setQueueId(mq.getQueueId());
                requestHeader.setSysFlag(sysFlag);
                requestHeader.setBornTimestamp(System.currentTimeMillis());
                requestHeader.setFlag(msg.getFlag());
                requestHeader.setProperties(MessageDecoder.messageProperties2String(msg.getProperties()));
                requestHeader.setReconsumeTimes(0);
                requestHeader.setUnitMode(this.isUnitMode());
                requestHeader.setBatch(msg instanceof MessageBatch);
                if (requestHeader.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                    String reconsumeTimes = MessageAccessor.getReconsumeTime(msg);
                    if (reconsumeTimes != null) {
                        requestHeader.setReconsumeTimes(Integer.valueOf(reconsumeTimes));
                        MessageAccessor.clearProperty(msg, MessageConst.PROPERTY_RECONSUME_TIME);
                    }

                    String maxReconsumeTimes = MessageAccessor.getMaxReconsumeTimes(msg);
                    if (maxReconsumeTimes != null) {
                        requestHeader.setMaxReconsumeTimes(Integer.valueOf(maxReconsumeTimes));
                        MessageAccessor.clearProperty(msg, MessageConst.PROPERTY_MAX_RECONSUME_TIMES);
                    }
                }

                SendResult sendResult = null;
                switch (communicationMode) {
                    case ASYNC:
                        Message tmpMessage = msg;
                        if (msgBodyCompressed) {
                            //If msg body was compressed, msgbody should be reset using prevBody.
                            //Clone new message using commpressed message body and recover origin massage.
                            //Fix bug:https://github.com/apache/rocketmq-externals/issues/66
                            tmpMessage = MessageAccessor.cloneMessage(msg);
                            msg.setBody(prevBody);
                        }
                        long costTimeAsync = System.currentTimeMillis() - beginStartTime;
                        if (timeout < costTimeAsync) {
                            throw new RemotingTooMuchRequestException("sendKernelImpl call timeout");
                        }
                        sendResult = this.mQClientFactory.getMQClientAPIImpl().sendMessage(
                            brokerAddr,
                            mq.getBrokerName(),
                            tmpMessage,
                            requestHeader,
                            timeout - costTimeAsync,
                            communicationMode,
                            sendCallback,
                            topicPublishInfo,
                            this.mQClientFactory,
                            this.defaultMQProducer.getRetryTimesWhenSendAsyncFailed(),
                            context,
                            this);
                        break;
                    case ONEWAY:
                    case SYNC:
                        long costTimeSync = System.currentTimeMillis() - beginStartTime;
                        if (timeout < costTimeSync) {
                            throw new RemotingTooMuchRequestException("sendKernelImpl call timeout");
                        }
                        sendResult = this.mQClientFactory.getMQClientAPIImpl().sendMessage(
                            brokerAddr,
                            mq.getBrokerName(),
                            msg,
                            requestHeader,
                            timeout - costTimeSync,
                            communicationMode,
                            context,
                            this);
                        break;
                    default:
                        assert false;
                        break;
                }

                if (this.hasSendMessageHook()) {
                    context.setSendResult(sendResult);
                    this.executeSendMessageHookAfter(context);
                }

                return sendResult;
```

此处的6个参数分别是：msg消息本身，mq要发送到的队列，communicationMode发送策略，sendCallback异步回调函数，topicPublishInfo路由信息，timeout发送超时时间(3秒-前2步消耗的时间)；首先需要获取指定broker的地址，这要才能创建channel与broker连接；然后就是一些hock处理；接下来就是准备发送的消息头SendMessageRequestHeader，最后根据不同的发送策略执行发送消息，此处就不在进入更加深入的分析；

## 总结

本文重点介绍了发送者的启动，以及发送消息的大概流程；关于消息的发送策略，以及消息的类型包括：顺序消息，事务消息等，将在后面的文章介绍。

## 示例代码地址

[https://github.com/ksfzhaohui...](https://github.com/ksfzhaohui/blog)  
[https://gitee.com/OutOfMemory...](https://gitee.com/OutOfMemory/blog)