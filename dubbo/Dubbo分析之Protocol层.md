## 系列文章

[Dubbo分析Serialize层](https://my.oschina.net/OutOfMemory/blog/2236611)  
[Dubbo分析之Transport层](https://my.oschina.net/OutOfMemory/blog/2251388)  
[Dubbo分析之Exchange 层](https://my.oschina.net/OutOfMemory/blog/2252445)  
[Dubbo分析之Protocol层](https://my.oschina.net/OutOfMemory/blog/2413695)  
[Dubbo分析之Cluster层](https://my.oschina.net/OutOfMemory/blog/2885469)  
[Dubbo分析之Registry层](https://my.oschina.net/OutOfMemory/blog/2991498)

## 前言

紧接着上文[Dubbo分析之Exchange层](https://my.oschina.net/OutOfMemory/blog/2252445)，继续分析protocol远程调用层，官方介绍：封装RPC调用，以Invocation, Result为中心，扩展接口为Protocol, Invoker, Exporter；

## Protocol接口类分析

Protocol可以说是Dubbo的核心层了，在此基础上可以扩展很多主流的服务，比如：redis，Memcached，rmi，WebService，http(tomcat，jetty)等等；下面看一下接口类源码：

```
public interface Protocol {
    /**
     * 暴露远程服务：<br>
     * 1. 协议在接收请求时，应记录请求来源方地址信息：RpcContext.getContext().setRemoteAddress();<br>
     * 2. export()必须是幂等的，也就是暴露同一个URL的Invoker两次，和暴露一次没有区别。<br>
     * 3. export()传入的Invoker由框架实现并传入，协议不需要关心。<br>
     * 
     * @param <T> 服务的类型
     * @param invoker 服务的执行体
     * @return exporter 暴露服务的引用，用于取消暴露
     * @throws RpcException 当暴露服务出错时抛出，比如端口已占用
     */
    <T> Exporter<T> export(Invoker<T> invoker) throws RpcException;
  
    /**
     * 引用远程服务：<br>
     * 1. 当用户调用refer()所返回的Invoker对象的invoke()方法时，协议需相应执行同URL远端export()传入的Invoker对象的invoke()方法。<br>
     * 2. refer()返回的Invoker由协议实现，协议通常需要在此Invoker中发送远程请求。<br>
     * 3. 当url中有设置check=false时，连接失败不能抛出异常，需内部自动恢复。<br>
     * 
     * @param <T> 服务的类型
     * @param type 服务的类型
     * @param url 远程服务的URL地址
     * @return invoker 服务的本地代理
     * @throws RpcException 当连接服务提供方失败时抛出
     */
    <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException;
  
}
```

主要定义了2个接口，一个是暴露远程服务，另一个是引用远程服务，其实就是服务端和客户端；dubbo提供了对多种服务的扩展，可以查看META-INF/dubbo/internal/com.alibaba.dubbo.rpc.Protocol：

```
filter=com.alibaba.dubbo.rpc.protocol.ProtocolFilterWrapper
listener=com.alibaba.dubbo.rpc.protocol.ProtocolListenerWrapper
mock=com.alibaba.dubbo.rpc.support.MockProtocol
dubbo=com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol
injvm=com.alibaba.dubbo.rpc.protocol.injvm.InjvmProtocol
rmi=com.alibaba.dubbo.rpc.protocol.rmi.RmiProtocol
hessian=com.alibaba.dubbo.rpc.protocol.hessian.HessianProtocol
com.alibaba.dubbo.rpc.protocol.http.HttpProtocol
com.alibaba.dubbo.rpc.protocol.webservice.WebServiceProtocol
thrift=com.alibaba.dubbo.rpc.protocol.thrift.ThriftProtocol
memcached=com.alibaba.dubbo.rpc.protocol.memcached.MemcachedProtocol
redis=com.alibaba.dubbo.rpc.protocol.redis.RedisProtocol
rest=com.alibaba.dubbo.rpc.protocol.rest.RestProtocol
registry=com.alibaba.dubbo.registry.integration.RegistryProtocol
qos=com.alibaba.dubbo.qos.protocol.QosProtocolWrapper
```

dubbo协议是默认提供的协议，其他扩展的协议包括：hessian，http(tomcat，jetty)，injvm，memcached，redis，rest，rmi，thrift，webservice；以上扩展的协议有些仅仅是作为引用远程服务存在(客户端)，比如redis，memcached，通过特定的命令对缓存进行操作；当然也可以扩展自己的协议，分别实现接口类Protocol, Invoker, Exporter；之前分别介绍的serialize层，transport层以及exchange层主要是在使用默认的DubboProtocol才依赖这几个底层，其他扩展协议直接依赖第三方扩展包；  
下面重点分析一下DubboProtocol类，首先看一下refer实现方法：

```
public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException {
    optimizeSerialization(url);
    // create rpc invoker.
    DubboInvoker<T> invoker = new DubboInvoker<T>(serviceType, url, getClients(url), invokers);
    invokers.add(invoker);
    return invoker;
}
```

在客户端定一个的每个dubbo:reference，都会在此处实例化一个对应的DubboInvoker；在方法内部首先对序列化优化进行处理，主要是对Kryo,FST等序列化方式进行优化，此方法不仅在客户端，同时服务器端也存在；接下来就是创建了一个DubboInvoker，同时创建与服务器端的连接：

```
private ExchangeClient[] getClients(URL url) {
        // whether to share connection
        boolean service_share_connect = false;
        int connections = url.getParameter(Constants.CONNECTIONS_KEY, 0);
        // if not configured, connection is shared, otherwise, one connection for one service
        if (connections == 0) {
            service_share_connect = true;
            connections = 1;
        }
 
        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; i++) {
            if (service_share_connect) {
                clients[i] = getSharedClient(url);
            } else {
                clients[i] = initClient(url);
            }
        }
        return clients;
    }
```

默认向指定的服务器创建一个连接，可以通过指定connections设置建立多个连接，在并发比较大的情况下可以设置多个；

```
private ExchangeClient initClient(URL url) {
 
        // client type setting.
        String str = url.getParameter(Constants.CLIENT_KEY, url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_CLIENT));
 
        url = url.addParameter(Constants.CODEC_KEY, DubboCodec.NAME);
        // enable heartbeat by default
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
 
        // BIO is not allowed since it has severe performance issue.
        if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
            throw new RpcException("Unsupported client type: " + str + "," +
                    " supported client type is " + StringUtils.join(ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions(), " "));
        }
 
        ExchangeClient client;
        try {
            // connection should be lazy
            if (url.getParameter(Constants.LAZY_CONNECT_KEY, false)) {
                client = new LazyConnectExchangeClient(url, requestHandler);
            } else {
                client = Exchangers.connect(url, requestHandler);
            }
        } catch (RemotingException e) {
            throw new RpcException("Fail to create remoting client for service(" + url + "): " + e.getMessage(), e);
        }
        return client;
    }
```

此方法主要通过Exchange层接口来和服务端建立连接，同时提供了懒连接的方式，要等到真正发送请求的时候才建立连接，返回ExchangeClient；DubboInvoker内部通过ExchangeClient来发送请求给服务端；再来看一下export方法：

```
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
       URL url = invoker.getUrl();
 
       // export service.
       String key = serviceKey(url);
       DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, exporterMap);
       exporterMap.put(key, exporter);
 
       //export an stub service for dispatching event
       Boolean isStubSupportEvent = url.getParameter(Constants.STUB_EVENT_KEY, Constants.DEFAULT_STUB_EVENT);
       Boolean isCallbackservice = url.getParameter(Constants.IS_CALLBACK_SERVICE, false);
       if (isStubSupportEvent && !isCallbackservice) {
           String stubServiceMethods = url.getParameter(Constants.STUB_EVENT_METHODS_KEY);
           if (stubServiceMethods == null || stubServiceMethods.length() == 0) {
               if (logger.isWarnEnabled()) {
                   logger.warn(new IllegalStateException("consumer [" + url.getParameter(Constants.INTERFACE_KEY) +
                           "], has set stubproxy support event ,but no stub methods founded."));
               }
           } else {
               stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods);
           }
       }
 
       openServer(url);
       optimizeSerialization(url);
       return exporter;
   }
```

每个dubbo:service都会绑定一个Exporter，首先通过url获取一个key(包括：port，serviceName，serviceVersion，serviceGroup)，然后将实例化的DubboExporter通过key值保存在一个Map中，后续在接收到消息的时候从新定位到具体的Exporter；接下来就是创建服务器：

```
private void openServer(URL url) {
        // find server.
        String key = url.getAddress();
        //client can export a service which's only for server to invoke
        boolean isServer = url.getParameter(Constants.IS_SERVER_KEY, true);
        if (isServer) {
            ExchangeServer server = serverMap.get(key);
            if (server == null) {
                serverMap.put(key, createServer(url));
            } else {
                // server supports reset, use together with override
                server.reset(url);
            }
        }
    }
 
    private ExchangeServer createServer(URL url) {
        // send readonly event when server closes, it's enabled by default
        url = url.addParameterIfAbsent(Constants.CHANNEL_READONLYEVENT_SENT_KEY, Boolean.TRUE.toString());
        // enable heartbeat by default
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
        String str = url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_SERVER);
 
        if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str))
            throw new RpcException("Unsupported server type: " + str + ", url: " + url);
 
        url = url.addParameter(Constants.CODEC_KEY, DubboCodec.NAME);
        ExchangeServer server;
        try {
            server = Exchangers.bind(url, requestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
        }
        str = url.getParameter(Constants.CLIENT_KEY);
        if (str != null && str.length() > 0) {
            Set<String> supportedTypes = ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions();
            if (!supportedTypes.contains(str)) {
                throw new RpcException("Unsupported client type: " + str);
            }
        }
        return server;
    }
```

以上主要就是通过Exchangers的bind方法来启动服务器，并返回对应的ExchangeServer，同样也保存在本地的Map中；最后同样做了序列化优化处理；

## Invoker类分析

refer()返回的Invoker由协议实现，协议通常需要在此Invoker中发送远程请求，export()传入的Invoker由框架实现并传入，协议不需要关心；接口类如下：

```
public interface Invoker<T> extends Node {
 
    Class<T> getInterface();
 
    Result invoke(Invocation invocation) throws RpcException;
}
```

本节介绍的是refer方法返回的Invoker，默认的dubbo协议下，实现了DubboInvoker，实现了其中的invoke方法，此方法在客户端调用远程方法的时候会被调用；

```
public Result invoke(Invocation inv) throws RpcException {
    if (destroyed.get()) {
        throw new RpcException("Rpc invoker for service " + this + " on consumer " + NetUtils.getLocalHost()
                + " use dubbo version " + Version.getVersion()
                + " is DESTROYED, can not be invoked any more!");
    }
    RpcInvocation invocation = (RpcInvocation) inv;
    invocation.setInvoker(this);
    if (attachment != null && attachment.size() > 0) {
        invocation.addAttachmentsIfAbsent(attachment);
    }
    Map<String, String> contextAttachments = RpcContext.getContext().getAttachments();
    if (contextAttachments != null) {
        /**
         * invocation.addAttachmentsIfAbsent(context){@link RpcInvocation#addAttachmentsIfAbsent(Map)}should not be used here,
         * because the {@link RpcContext#setAttachment(String, String)} is passed in the Filter when the call is triggered
         * by the built-in retry mechanism of the Dubbo. The attachment to update RpcContext will no longer work, which is
         * a mistake in most cases (for example, through Filter to RpcContext output traceId and spanId and other information).
         */
        invocation.addAttachments(contextAttachments);
    }
    if (getUrl().getMethodParameter(invocation.getMethodName(), Constants.ASYNC_KEY, false)) {
        invocation.setAttachment(Constants.ASYNC_KEY, Boolean.TRUE.toString());
    }
    RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
 
 
    try {
        return doInvoke(invocation);
    } catch (InvocationTargetException e) { // biz exception
        Throwable te = e.getTargetException();
        if (te == null) {
            return new RpcResult(e);
        } else {
            if (te instanceof RpcException) {
                ((RpcException) te).setCode(RpcException.BIZ_EXCEPTION);
            }
            return new RpcResult(te);
        }
    } catch (RpcException e) {
        if (e.isBiz()) {
            return new RpcResult(e);
        } else {
            throw e;
        }
    } catch (Throwable e) {
        return new RpcResult(e);
    }
}
 
protected abstract Result doInvoke(Invocation invocation) throws Throwable;
```

在DubboInvoker的抽象类中提供了invoke方法，做统一的附件(Attachment)处理，方法传入的参数是一个RpcInvocation对象，包含了方法调用的相关参数：

```
public class RpcInvocation implements Invocation, Serializable {
 
    private static final long serialVersionUID = -4355285085441097045L;
 
    private String methodName;
 
    private Class<?>[] parameterTypes;
 
    private Object[] arguments;
 
    private Map<String, String> attachments;
 
    private transient Invoker<?> invoker;
     
    ....省略...
}
```

包含了方法名称，方法参数，参数值，附件信息；可能你会发现没有接口，版本等信息，这些信息其实包含在附件中；在invoke方法中首先处理的就是把attachment信息保存到RpcInvocation中；接下来就是调用DubboInvoker中的doInvoke方法：

```
protected Result doInvoke(final Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation) invocation;
        final String methodName = RpcUtils.getMethodName(invocation);
        inv.setAttachment(Constants.PATH_KEY, getUrl().getPath());
        inv.setAttachment(Constants.VERSION_KEY, version);
 
        ExchangeClient currentClient;
        if (clients.length == 1) {
            currentClient = clients[0];
        } else {
            currentClient = clients[index.getAndIncrement() % clients.length];
        }
        try {
            boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
            boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
            int timeout = getUrl().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
            if (isOneway) {
                boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
                currentClient.send(inv, isSent);
                RpcContext.getContext().setFuture(null);
                return new RpcResult();
            } else if (isAsync) {
                ResponseFuture future = currentClient.request(inv, timeout);
                RpcContext.getContext().setFuture(new FutureAdapter<Object>(future));
                return new RpcResult();
            } else {
                RpcContext.getContext().setFuture(null);
                return (Result) currentClient.request(inv, timeout).get();
            }
        } catch (TimeoutException e) {
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        } catch (RemotingException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
```

此方法首先获取ExchangeClient，如果实例化了多个ExchangeClient，会通过顺序的方式遍历使用ExchangeClient；通过ExchangeClient将RpcInvocation发送给服务器端，提供了三种发送方式：单边通信方式，双边通信(同步)，双边通信(异步)；在上文[Dubbo分析之Exchange层](https://my.oschina.net/OutOfMemory/blog/2252445)中，发送完请求之后直接返回DefaultFuture参数，如果调用get方法将阻塞直到返回结果或者超时，同步方式就是直接调用get方法，阻塞等待结果，下面重点看一下异步方式；异步方式将返回的DefaultFuture放入了RpcContext中，然后返回了一个空对象，这里其实使用了ThreadLocal功能，所以每次在客户端业务代码中，调用完异步请求，都需要通过RpcContext获取ResponseFuture，比如：

```
// 此调用会立即返回null
fooService.findFoo(fooId);
// 拿到调用的Future引用，当结果返回后，会被通知和设置到此Future
Future<Foo> fooFuture = RpcContext.getContext().getFuture(); 
  
// 此调用会立即返回null
barService.findBar(barId);
// 拿到调用的Future引用，当结果返回后，会被通知和设置到此Future
Future<Bar> barFuture = RpcContext.getContext().getFuture(); 
  
// 此时findFoo和findBar的请求同时在执行，客户端不需要启动多线程来支持并行，而是借助NIO的非阻塞完成
  
// 如果foo已返回，直接拿到返回值，否则线程wait住，等待foo返回后，线程会被notify唤醒
Foo foo = fooFuture.get(); 
// 同理等待bar返回
Bar bar = barFuture.get(); 
  
// 如果foo需要5秒返回，bar需要6秒返回，实际只需等6秒，即可获取到foo和bar，进行接下来的处理。
```

官网的一个列子，很好的说明了异步的使用方式以及其优势；

## Exporter类分析

在上文[Dubbo分析之Exchange层](https://my.oschina.net/OutOfMemory/blog/2252445)中，服务端接收到消息之后，调用handler的reply方法处理消息，而此handler定义在DubboProtocol中，如下：

```
private ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
 
        @Override
        public Object reply(ExchangeChannel channel, Object message) throws RemotingException {
            if (message instanceof Invocation) {
                Invocation inv = (Invocation) message;
                Invoker<?> invoker = getInvoker(channel, inv);
                // need to consider backward-compatibility if it's a callback
                if (Boolean.TRUE.toString().equals(inv.getAttachments().get(IS_CALLBACK_SERVICE_INVOKE))) {
                    String methodsStr = invoker.getUrl().getParameters().get("methods");
                    boolean hasMethod = false;
                    if (methodsStr == null || methodsStr.indexOf(",") == -1) {
                        hasMethod = inv.getMethodName().equals(methodsStr);
                    } else {
                        String[] methods = methodsStr.split(",");
                        for (String method : methods) {
                            if (inv.getMethodName().equals(method)) {
                                hasMethod = true;
                                break;
                            }
                        }
                    }
                    if (!hasMethod) {
                        logger.warn(new IllegalStateException("The methodName " + inv.getMethodName()
                                + " not found in callback service interface ,invoke will be ignored."
                                + " please update the api interface. url is:"
                                + invoker.getUrl()) + " ,invocation is :" + inv);
                        return null;
                    }
                }
                RpcContext.getContext().setRemoteAddress(channel.getRemoteAddress());
                return invoker.invoke(inv);
            }
            throw new RemotingException(channel, "Unsupported request: "
                    + (message == null ? null : (message.getClass().getName() + ": " + message))
                    + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress());
        }
         
        ...省略...
}
```

服务端接收到message就是上面的RpcInvocation，里面包含了接口，方法，参数等信息，服务器端通过反射的方式来处理；首先获取了对应的DubboExporter，如果获取，通过key(包括：port，serviceName，serviceVersion，serviceGroup)获取对应的DubboExporter，然后调用DubboExporter中的invoker，此时的invoker是系统传过来的，不像客户端Invoker是协议端自己创建的，系统创建的invoker，以链表的方式存在，内部调用对应的filter，具体有哪些filter，在启动服务时已经初始化好了在ProtocolFilterWrapper的buildInvokerChain中，具体有哪些filter可以查看META-INF/dubbo/internal/com.alibaba.dubbo.rpc.Filter:

```
cache=com.alibaba.dubbo.cache.filter.CacheFilter
validation=com.alibaba.dubbo.validation.filter.ValidationFilter
echo=com.alibaba.dubbo.rpc.filter.EchoFilter
generic=com.alibaba.dubbo.rpc.filter.GenericFilter
genericimpl=com.alibaba.dubbo.rpc.filter.GenericImplFilter
token=com.alibaba.dubbo.rpc.filter.TokenFilter
accesslog=com.alibaba.dubbo.rpc.filter.AccessLogFilter
activelimit=com.alibaba.dubbo.rpc.filter.ActiveLimitFilter
classloader=com.alibaba.dubbo.rpc.filter.ClassLoaderFilter
context=com.alibaba.dubbo.rpc.filter.ContextFilter
consumercontext=com.alibaba.dubbo.rpc.filter.ConsumerContextFilter
exception=com.alibaba.dubbo.rpc.filter.ExceptionFilter
executelimit=com.alibaba.dubbo.rpc.filter.ExecuteLimitFilter
deprecated=com.alibaba.dubbo.rpc.filter.DeprecatedFilter
compatible=com.alibaba.dubbo.rpc.filter.CompatibleFilter
timeout=com.alibaba.dubbo.rpc.filter.TimeoutFilter
trace=com.alibaba.dubbo.rpc.protocol.dubbo.filter.TraceFilter
future=com.alibaba.dubbo.rpc.protocol.dubbo.filter.FutureFilter
monitor=com.alibaba.dubbo.monitor.support.MonitorFilter
```

这里列出了所有的filter，包含消费端和服务端，具体使用哪些，通过filter的注解@Activate来进行过滤，每个filter就行了分组；具体执行的顺序是怎么样的，同样在注解里面指定了，格式如下：

```
@Activate(group = Constants.PROVIDER, order = -110000)
@Activate(group = Constants.PROVIDER, order = -10000)
@Activate(group = Constants.CONSUMER, value = Constants.GENERIC_KEY, order = 20000)
```

每个固定的filter有各自的功能，同样也可以进行扩展，处理完了交给下一个，最后通过反射调用返回RpcResult；

## 总结

本文大体介绍了一下Protocol层使用的默认dubbo协议介绍，Protocol层还对其他第三方协议进行了扩展，后面会继续介绍；另外关于filter还可以在详细介绍一下；

## 示例代码地址

[https://github.com/ksfzhaohui...](https://github.com/ksfzhaohui/blog)  
[https://gitee.com/OutOfMemory...](https://gitee.com/OutOfMemory/blog)