## 前言
谈到RPC肯定绕不开TCP通信，而主流的RPC框架都依赖于Netty等通信框架，这时候我们还要考虑是使用长连接还是短连接：

* 短连接：每次通信结束后关闭连接，下次通信需要重新创建连接；优点就是无需管理连接，无需保活连接；
* 长连接：每次通信结束不关闭连接，连接可以复用，保证了性能；缺点就是连接需要统一管理，并且需要保活；

主流的RPC框架都会追求性能选择使用长连接，所以如何保活连接就是一个重要的话题，也是本文的主题，下面会重点介绍一些保活策略；

## 为什么需要保活
上面介绍的长连接、短连接并不是TCP提供的功能，所以长连接是需要应用端自己来实现的，包括：连接的统一管理，如何保活等；如何保活之前我们了解一下为什么需要保活？主要原因是网络不是100%可靠的，我们创建好的连接可能由于网络原因导致连接已经不可用了，如果连接一直有消息往来，那么系统马上可以感知到连接断开；但是我们系统可能长时间没有消息来往，导致系统不能及时感知到连接不可用，也就是不能及时处理重连或者释放连接；常见的保活策略使用心跳机制由应用层来实现，还有网络层提供的TCP Keepalive保活探测机制；

## TCP Keepalive机制
TCP Keepalive是操作系统实现的功能，并不是TCP协议的一部分，需要在操作系统下进行相关配置，开启此功能后，如果连接在一段时间内没有数据往来，TCP将发送Keepalive探针来确认连接的可用性，Keepalive几个内核参数配置：

* tcp_keepalive_time：连接多长时间没有数据往来发送探针请求，默认为7200s（2h）；
* tcp_keepalive_probes：探测失败重试的次数默认为10次；
* tcp_keepalive_intvl：重试的间隔时间默认75s；
以上参数可以修改到/etc/sysctl.conf文件中；是否使用Keepalive用来保活就够了，其实还不够，Keepalive只是在网络层就行保活，如果网络本身没有问题，但是系统由于其他原因已经不可用了，这时候Keepalive并不能发现；所以往往还需要结合心跳机制来一起使用；

## 心跳机制
何为心跳机制，简单来讲就是客户端启动一个定时器用来定时发送请求，服务端接到请求进行响应，如果多次没有接受到响应，那么客户端认为连接已经断开，可以断开半打开的连接或者进行重连处理；下面以Dubbo为例来看看是如何具体实施的；

### Dubbo2.6.X
在HeaderExchangeClient中启动了定时器ScheduledThreadPoolExecutor来定期执行心跳请求：
```
ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("dubbo-remoting-client-heartbeat", true));
```
在实例化HeaderExchangeClient时启动心跳定时器：
```
private void startHeartbeatTimer() {
        stopHeartbeatTimer();
        if (heartbeat > 0) {
            heartbeatTimer = scheduled.scheduleWithFixedDelay(
                    new HeartBeatTask(new HeartBeatTask.ChannelProvider() {
                        @Override
                        public Collection<Channel> getChannels() {
                            return Collections.<Channel>singletonList(HeaderExchangeClient.this);
                        }
                    }, heartbeat, heartbeatTimeout),
                    heartbeat, heartbeat, TimeUnit.MILLISECONDS);
        }
    }
```
heartbeat默认为60秒，heartbeatTimeout默认为heartbeat*3，可以理解至少出现三次心跳请求还未收到回复才会任务连接已经断开；HeartBeatTask为执行心跳的任务：
```
public void run() {
		long now = System.currentTimeMillis();
		for (Channel channel : channelProvider.getChannels()) {
			if (channel.isClosed()) {
				continue;
			}
			Long lastRead = (Long) channel.getAttribute(HeaderExchangeHandler.KEY_READ_TIMESTAMP);
			Long lastWrite = (Long) channel.getAttribute(HeaderExchangeHandler.KEY_WRITE_TIMESTAMP);
			if ((lastRead != null && now - lastRead > heartbeat)
					|| (lastWrite != null && now - lastWrite > heartbeat)) {
				// 发送心跳
			}
			if (lastRead != null && now - lastRead > heartbeatTimeout) {
				if (channel instanceof Client) {
					((Client) channel).reconnect();
				} else {
					channel.close();
				}
			}
		}
	}
```
因为Dubbo双端都会发送心跳请求，所以可以发现有两个时间点分别是：lastRead和lastWrite；当然时间和最后读取，最后写的时间间隔大于heartbeat就会发送心跳请求；如果多次心跳未返回结果，也就是最后读取消息时间大于heartbeatTimeout会判定当前是Client还是Server，如果是Client会发起reconnect，Server会关闭连接，这样的考虑是合理的，客户端调用是强依赖可用连接的，而服务端可以等待客户端重新建立连接；以上只是介绍的Client，同样Server端也有相同的心跳处理，在可以查看HeaderExchangeServer；

### Dubbo2.7.0
Dubbo2.7.0的心跳机制在2.6.X的基础上得到了加强，同样在HeaderExchangeClient中使用HashedWheelTimer开启心跳检测，这是Netty提供的一个时间轮定时器，在任务非常多，并且任务执行时间很短的情况下，HashedWheelTimer比Schedule性能更好，特别适合心跳检测；
```
HashedWheelTimer heartbeatTimer = new HashedWheelTimer(new NamedThreadFactory("dubbo-client-heartbeat", true), tickDuration,
                    TimeUnit.MILLISECONDS, Constants.TICKS_PER_WHEEL);
```
分别启动了两个定时任务：startHeartBeatTask和startReconnectTask：
```
private void startHeartbeatTimer() {
        AbstractTimerTask.ChannelProvider cp = () -> Collections.singletonList(HeaderExchangeClient.this);

        long heartbeatTick = calculateLeastDuration(heartbeat);
        long heartbeatTimeoutTick = calculateLeastDuration(heartbeatTimeout);
        HeartbeatTimerTask heartBeatTimerTask = new HeartbeatTimerTask(cp, heartbeatTick, heartbeat);
        ReconnectTimerTask reconnectTimerTask = new ReconnectTimerTask(cp, heartbeatTimeoutTick, heartbeatTimeout);

        // init task and start timer.
        heartbeatTimer.newTimeout(heartBeatTimerTask, heartbeatTick, TimeUnit.MILLISECONDS);
        heartbeatTimer.newTimeout(reconnectTimerTask, heartbeatTimeoutTick, TimeUnit.MILLISECONDS);
    }
```
HeartbeatTimerTask：用来定时发送心跳请求，心跳间隔时间默认为60秒；这里重新计算了时间，其实就是在原来的基础上除以3，其实就是缩短了检测间隔时间，增大了及时发现死链的概率；分别看一下两个任务：
```
protected void doTask(Channel channel) {
		Long lastRead = lastRead(channel);
		Long lastWrite = lastWrite(channel);
		if ((lastRead != null && now() - lastRead > heartbeat)
				|| (lastWrite != null && now() - lastWrite > heartbeat)) {
			Request req = new Request();
			req.setVersion(Version.getProtocolVersion());
			req.setTwoWay(true);
			req.setEvent(Request.HEARTBEAT_EVENT);
			channel.send(req);
		}
	}
```
同上检测最后读写时间和heartbeat的大小，注：普通请求和心跳请求都会更新读写时间；
```
protected void doTask(Channel channel) {
		Long lastRead = lastRead(channel);
		Long now = now();
		if (lastRead != null && now - lastRead > heartbeatTimeout) {
			if (channel instanceof Client) {
				((Client) channel).reconnect();
			} else {
				channel.close();
			}
		}
	}
```
同样的在超时的情况下，Client重连，Server关闭连接；同样Server端也有相同的心跳处理，在可以查看HeaderExchangeServer；

### Dubbo2.7.1-X
在Dubbo2.7.1之后，借助了Netty提供的IdleStateHandler来实现心跳机制服务：
```
public IdleStateHandler(
            long readerIdleTime, long writerIdleTime, long allIdleTime,
            TimeUnit unit) {
        this(false, readerIdleTime, writerIdleTime, allIdleTime, unit);
    }
```

* readerIdleTime：读超时时间；
* writerIdleTime：写超时时间；
* allIdleTime：所有类型的超时时间；
根据设置的超时时间，循环检查读写事件多久没有发生了，在pipeline中加入IdleSateHandler之后，可以在此pipeline的任意Handler的userEventTriggered方法之中检测IdleStateEvent事件；下面看看具体Client和Server端添加的IdleStateHandler：
#### Client端
```
	protected void initChannel(Channel ch) throws Exception {
		final NettyClientHandler nettyClientHandler = new NettyClientHandler(getUrl(), this);
		int heartbeatInterval = UrlUtils.getHeartbeat(getUrl());
		ch.pipeline().addLast("client-idle-handler", new IdleStateHandler(heartbeatInterval, 0, 0, MILLISECONDS))
				.addLast("handler", nettyClientHandler);
	}
```
Client端在NettyClient中添加了IdleStateHandler，指定了读写超时时间默认为60秒；60秒内没有读写事件发生，会触发IdleStateEvent事件在NettyClientHandler处理：
```
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            try {
                NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
                Request req = new Request();
                req.setVersion(Version.getProtocolVersion());
                req.setTwoWay(true);
                req.setEvent(Request.HEARTBEAT_EVENT);
                channel.send(req);
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
       } else {
            super.userEventTriggered(ctx, evt);
        }
    }
```
可以发现接收到IdleStateEvent事件发送了心跳请求；至于Client端如何处理重连，同样在HeaderExchangeClient中使用HashedWheelTimer定时器启动了两个任务：心跳任务和重连任务，感觉这里已经不需要心跳任务了，至于重连任务其实也可以放到userEventTriggered中处理；

#### Server端
```
protected void initChannel(NioSocketChannel ch) throws Exception {
		int idleTimeout = UrlUtils.getIdleTimeout(getUrl());
		final NettyServerHandler nettyServerHandler = new NettyServerHandler(getUrl(), this);
		ch.pipeline().addLast("server-idle-handler", new IdleStateHandler(0, 0, idleTimeout, MILLISECONDS))
				.addLast("handler", nettyServerHandler);
	}
```
Server端指定的超时时间默认为60*3秒，在NettyServerHandler中处理userEventTriggered
```
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
            try {
                channel.close();
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
        super.userEventTriggered(ctx, evt);
    }
```
Server端在指定的超时时间内没有发生读写，会直接关闭连接；相比之前现在只有Client发送心跳，单向发送心跳；同样的在HeaderExchangeServer中并没有启动多个认为，仅仅启动了一个CloseTimerTask，用来检测超时时间关闭连接；感觉这个任务是不是也可以不需要了，IdleStateHandler已经实现了此功能；

**综上：**在使用IdleStateHandler的情况下来同时在HeaderExchangeClient启动心跳+重连机制，HeaderExchangeServer启动了关闭连接机制；主要是因为IdleStateHandler是Netty框架特有了，而Dubbo是支持多种底层通讯框架的包括Mina，Grizzy等，应该是为了兼容此类框架存在的；

## 总结
本文首先介绍了RPC中引入的长连接方式，继而引出长连接的保活机制，为什么需要保活？然后分别介绍了网络层保活机制TCP Keepalive机制，应用层心跳机制；最后已Dubbo为例看各个版本中对心跳机制的进化。