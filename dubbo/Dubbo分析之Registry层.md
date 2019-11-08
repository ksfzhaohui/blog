## 系列文章

[Dubbo分析Serialize层](https://my.oschina.net/OutOfMemory/blog/2236611)  
[Dubbo分析之Transport层](https://my.oschina.net/OutOfMemory/blog/2251388)  
[Dubbo分析之Exchange 层](https://my.oschina.net/OutOfMemory/blog/2252445)  
[Dubbo分析之Protocol层](https://my.oschina.net/OutOfMemory/blog/2413695)  
[Dubbo分析之Cluster层](https://my.oschina.net/OutOfMemory/blog/2885469)  
[Dubbo分析之Registry层](https://my.oschina.net/OutOfMemory/blog/2991498)

## 前言

紧接上文[Dubbo分析之Cluster层](https://my.oschina.net/OutOfMemory/blog/2885469)，本文继续分析dubbo的register层；此层封装服务地址的注册与发现，以服务URL为中心，扩展接口为RegistryFactory, Registry, RegistryService；

## Registry接口

接口定义如下：

```
public interface Registry extends Node, RegistryService {
}
 
public interface RegistryService {
 
    void register(URL url);
 
    void unregister(URL url);
     
    void subscribe(URL url, NotifyListener listener);
 
    void unsubscribe(URL url, NotifyListener listener);
 
    List<URL> lookup(URL url);
 
}
```

主要提供了注册(register)，注销(unregister)，订阅(subscribe)，退订(unsubscribe)等功能；dubbo提供了多种注册方式分别是：Multicast ，Zookeeper，Redis以及Simple方式；  
Multicast：Multicast注册中心不需要启动任何中心节点，只要广播地址一样，就可以互相发现；  
Zookeeper：Zookeeper是Apacahe Hadoop的子项目，是一个树型的目录服务，支持变更推送，适合作为Dubbo服务的注册中心，工业强度较高，可用于生产环境，并推荐使用；  
Redis：基于Redis实现的注册中心，使用 Redis的Publish/Subscribe事件通知数据变更；  
Simple：Simple注册中心本身就是一个普通的Dubbo服务，可以减少第三方依赖，使整体通讯方式一致；  
后面重点介绍官方推荐的Zookeeper注册方式；具体的Register是在RegistryFactory中生成的，具体看一下接口定义；

## RegistryFactory接口

接口定义如下：

```
@SPI("dubbo")
public interface RegistryFactory {
 
    @Adaptive({"protocol"})
    Registry getRegistry(URL url);
 
}
```

RegistryFactory提供了SPI扩展，默认使用dubbo，具体有哪些扩展可以查看META-INF/dubbo/internal/com.alibaba.dubbo.registry.RegistryFactory：

```
dubbo=com.alibaba.dubbo.registry.dubbo.DubboRegistryFactory
multicast=com.alibaba.dubbo.registry.multicast.MulticastRegistryFactory
zookeeper=com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistryFactory
redis=com.alibaba.dubbo.registry.redis.RedisRegistryFactory
```

已推荐使用的Zookeeper为实例，查看ZookeeperRegistryFactory，提供了createRegistry方法：

```
private ZookeeperTransporter zookeeperTransporter;
 
public Registry createRegistry(URL url) {
       return new ZookeeperRegistry(url, zookeeperTransporter);
}
```

实例化ZookeeperRegistry，两个参数分别是url和zookeeperTransporter，zookeeperTransporter是操作Zookeeper的客户端组件包括：zkclient和curator两种方式

```
@SPI("curator")
public interface ZookeeperTransporter {
 
    @Adaptive({Constants.CLIENT_KEY, Constants.TRANSPORTER_KEY})
    ZookeeperClient connect(URL url);
 
}
```

ZookeeperTransporter同样提供了SPI扩展，默认使用curator方式；接下来重点看一下Zookeeper注册中心。

## Zookeeper注册中心

### 1.整体设计流程

在dubbo的整体设计中，可以大致查看Registry层的大致流程，首先通过RegistryFactory实例化Registry，Registry可以接收RegistryProtocol传过来的注册(register)和订阅(subscribe)消息，然后Registry通过ZKClient来向Zookeeper指定的目录下写入url信息，如果是订阅消息Registry会通过NotifyListener来通知RegitryDirctory进行更新url，最后就是Cluster层通过路由，负载均衡选择具体的提供方；

### 2.Zookeeper注册中心流程

官方提供了dubbo在Zookeeper中心的流程图：  
![](https://oscimg.oschina.net/oscnet/2e9f60a874c0e4d872a8aead78f607617fa.jpg)

流程说明：  
服务提供者启动时: 向/dubbo/com.foo.BarService/providers目录下写入自己的URL地址；  
服务消费者启动时: 订阅/dubbo/com.foo.BarService/providers目录下的提供者URL地址；并向/dubbo/com.foo.BarService/consumers目录下写入自己的URL地址；  
监控中心启动时: 订阅/dubbo/com.foo.BarService 目录下的所有提供者和消费者URL地址。  
下面分别从注册(register)，注销(unregister)，订阅(subscribe)，退订(unsubscribe)四个方面来分析

### 3.注册(register)

ZookeeperRegistry的父类FailbackRegistry中实现了register方法，FailbackRegistry从名字可以看出来具有：失败自动恢复，后台记录失败请求，定时重发功能；下面具体看一下register方法：

```
public void register(URL url) {
        super.register(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);
        try {
            // Sending a registration request to the server side
            doRegister(url);
        } catch (Exception e) {
            Throwable t = e;
 
            // If the startup detection is opened, the Exception is thrown directly.
            boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)
                    && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to register " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            } else {
                logger.error("Failed to register " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            }
 
            // Record a failed registration request to a failed list, retry regularly
            failedRegistered.add(url);
        }
    }
```

后台记录了失败的请求，包括failedRegistered和failedUnregistered，注册的时候将里面存放的url删除，然后执行doRegister方法，此方式在ZookeeperRegistry中实现，主要是在Zookeeper指定的目录下写入url信息，如果失败会记录注册失败的url，等待自动恢复；doRegister相关代码如下：

```
protected void doRegister(URL url) {
        try {
            zkClient.create(toUrlPath(url), url.getParameter(Constants.DYNAMIC_KEY, true));
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
}
```

调用zkClient的create方法在Zookeeper上创建节点，默认创建临时节点，create方法在AbstractZookeeperClient中实现，具体源码如下：

```
public void create(String path, boolean ephemeral) {
       if (!ephemeral) {
           if (checkExists(path)) {
               return;
           }
       }
       int i = path.lastIndexOf('/');
       if (i > 0) {
           create(path.substring(0, i), false);
       }
       if (ephemeral) {
           createEphemeral(path);
       } else {
           createPersistent(path);
       }
   }
```

path指定需要创建的目录，ephemeral指定是否是创建临时节点，并且提供了递归创建目录，除了叶子目录其他目录都是持久化的；可以发现不管是创建临时目录还是持久化目录，都没有指定目录的Data，所有使用的是默认值，也就是本地ip地址；实例中创建的目录如下：

```
/dubbo/com.dubboApi.DemoService/providers/dubbo%3A%2F%2F10.13.83.7%3A20880%2Fcom.dubboApi.DemoService%3Fanyhost%3Dtrue%26application%3Dhello-world-app%26dubbo%3D2.0.2%26generic%3Dfalse%26interface%3Dcom.dubboApi.DemoService%26methods%3DsyncSayHello%2CsayHello%2CasyncSayHello%26pid%3D13252%26serialization%3Dprotobuf%26side%3Dprovider%26timestamp%3D1545297239027
```

dubbo是一个根节点，然后是service名称，providers是固定的一个类型，如果是消费端这里就是consumers，最后就是一个临时节点；使用临时节点的目的就是提供者出现断电等异常停机时，注册中心能自动删除提供者信息；可以通过如下方法查询当前的目录节点信息：

```
public class CuratorTest {
 
    static String path = "/dubbo";
    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
 
    public static void main(String[] args) throws Exception {
        client.start();
        List<String> paths = listChildren(path);
        for (String path : paths) {
            Stat stat = new Stat();
            System.err.println(
                    "path:" + path + ",value:" + new String(client.getData().storingStatIn(stat).forPath(path)));
        }
    }
 
    private static List<String> listChildren(String path) throws Exception {
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
}
```

递归遍历/dubbo目录下的所有子目录，同时将节点存储的数据都查询出来，结果如下：

```
path:/dubbo,value:10.13.83.7
path:/dubbo/com.dubboApi.DemoService,value:10.13.83.7
path:/dubbo/com.dubboApi.DemoService/configurators,value:10.13.83.7
path:/dubbo/com.dubboApi.DemoService/providers,value:10.13.83.7
path:/dubbo/com.dubboApi.DemoService/providers/dubbo%3A%2F%2F10.13.83.7%3A20880%2Fcom.dubboApi.DemoService%3Fanyhost%3Dtrue%26application%3Dhello-world-app%26dubbo%3D2.0.2%26generic%3Dfalse%26interface%3Dcom.dubboApi.DemoService%26methods%3DsyncSayHello%2CsayHello%2CasyncSayHello%26pid%3D4712%26serialization%3Dprotobuf%26side%3Dprovider%26timestamp%3D1545358401966,value:10.13.83.7
```

除了最后一个节点是临时节点，其他都是持久化的；

### 4.注销(unregister)

同样在父类FailbackRegistry中实现了unregister方法，代码如下：

```
public void unregister(URL url) {
       super.unregister(url);
       failedRegistered.remove(url);
       failedUnregistered.remove(url);
       try {
           // Sending a cancellation request to the server side
           doUnregister(url);
       } catch (Exception e) {
           Throwable t = e;
 
           // If the startup detection is opened, the Exception is thrown directly.
           boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                   && url.getParameter(Constants.CHECK_KEY, true)
                   && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol());
           boolean skipFailback = t instanceof SkipFailbackWrapperException;
           if (check || skipFailback) {
               if (skipFailback) {
                   t = t.getCause();
               }
               throw new IllegalStateException("Failed to unregister " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
           } else {
               logger.error("Failed to uregister " + url + ", waiting for retry, cause: " + t.getMessage(), t);
           }
 
           // Record a failed registration request to a failed list, retry regularly
           failedUnregistered.add(url);
       }
   }
```

注销时同样删除了failedRegistered和failedUnregistered存放的url，然后调用doUnregister，删除Zookeeper中的目录节点，失败的情况下会存储在failedUnregistered中，等待重试；

```
protected void doUnregister(URL url) {
    try {
        zkClient.delete(toUrlPath(url));
    } catch (Throwable e) {
        throw new RpcException("Failed to unregister " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
    }
}
 
//CuratorZookeeperClient删除操作
public void delete(String path) {
    try {
        client.delete().forPath(path);
    } catch (NoNodeException e) {
    } catch (Exception e) {
        throw new IllegalStateException(e.getMessage(), e);
    }
}
```

直接使用CuratorZookeeperClient中的delete方法删除临时节点；

### 5.订阅(subscribe)

服务消费者启动时，会先向Zookeeper注册消费者节点信息，然后订阅…/providers目录下提供者的URL地址；消费端也同样需要注册节点信息，是因为监控中心需要对服务端和消费端都进行监控；下面重点看一下订阅的相关代码，在父类FailbackRegistry中实现了subscribe方法：

```
public void subscribe(URL url, NotifyListener listener) {
       super.subscribe(url, listener);
       removeFailedSubscribed(url, listener);
       try {
           // Sending a subscription request to the server side
           doSubscribe(url, listener);
       } catch (Exception e) {
           Throwable t = e;
 
           List<URL> urls = getCacheUrls(url);
           if (urls != null && !urls.isEmpty()) {
               notify(url, listener, urls);
               logger.error("Failed to subscribe " + url + ", Using cached list: " + urls + " from cache file: " + getUrl().getParameter(Constants.FILE_KEY, System.getProperty("user.home") + "/dubbo-registry-" + url.getHost() + ".cache") + ", cause: " + t.getMessage(), t);
           } else {
               // If the startup detection is opened, the Exception is thrown directly.
               boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                       && url.getParameter(Constants.CHECK_KEY, true);
               boolean skipFailback = t instanceof SkipFailbackWrapperException;
               if (check || skipFailback) {
                   if (skipFailback) {
                       t = t.getCause();
                   }
                   throw new IllegalStateException("Failed to subscribe " + url + ", cause: " + t.getMessage(), t);
               } else {
                   logger.error("Failed to subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
               }
           }
 
           // Record a failed registration request to a failed list, retry regularly
           addFailedSubscribed(url, listener);
       }
   }
```

类似的格式，同样存储了失败了订阅url信息，重点看ZookeeperRegistry中的doSubscribe方法：

```
private final ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildListener>> zkListeners = new ConcurrentHashMap<URL, ConcurrentMap<NotifyListener, ChildListener>>();
 
protected void doSubscribe(final URL url, final NotifyListener listener) {
       try {
           if (Constants.ANY_VALUE.equals(url.getServiceInterface())) {
               String root = toRootPath();
               ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
               if (listeners == null) {
                   zkListeners.putIfAbsent(url, new ConcurrentHashMap<NotifyListener, ChildListener>());
                   listeners = zkListeners.get(url);
               }
               ChildListener zkListener = listeners.get(listener);
               if (zkListener == null) {
                   listeners.putIfAbsent(listener, new ChildListener() {
                       @Override
                       public void childChanged(String parentPath, List<String> currentChilds) {
                           for (String child : currentChilds) {
                               child = URL.decode(child);
                               if (!anyServices.contains(child)) {
                                   anyServices.add(child);
                                   subscribe(url.setPath(child).addParameters(Constants.INTERFACE_KEY, child,
                                           Constants.CHECK_KEY, String.valueOf(false)), listener);
                               }
                           }
                       }
                   });
                   zkListener = listeners.get(listener);
               }
               zkClient.create(root, false);
               List<String> services = zkClient.addChildListener(root, zkListener);
               if (services != null && !services.isEmpty()) {
                   for (String service : services) {
                       service = URL.decode(service);
                       anyServices.add(service);
                       subscribe(url.setPath(service).addParameters(Constants.INTERFACE_KEY, service,
                               Constants.CHECK_KEY, String.valueOf(false)), listener);
                   }
               }
           } else {
               List<URL> urls = new ArrayList<URL>();
               for (String path : toCategoriesPath(url)) {
                   ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
                   if (listeners == null) {
                       zkListeners.putIfAbsent(url, new ConcurrentHashMap<NotifyListener, ChildListener>());
                       listeners = zkListeners.get(url);
                   }
                   ChildListener zkListener = listeners.get(listener);
                   if (zkListener == null) {
                       listeners.putIfAbsent(listener, new ChildListener() {
                           @Override
                           public void childChanged(String parentPath, List<String> currentChilds) {
                               ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds));
                           }
                       });
                       zkListener = listeners.get(listener);
                   }
                   zkClient.create(path, false);
                   List<String> children = zkClient.addChildListener(path, zkListener);
                   if (children != null) {
                       urls.addAll(toUrlsWithEmpty(url, path, children));
                   }
               }
               notify(url, listener, urls);
           }
       } catch (Throwable e) {
           throw new RpcException("Failed to subscribe " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
       }
   }
```

在ZookeeperRegistry中定义了一个zkListeners变量，每个URL对应了一个map；map里面分别是NotifyListener和ChildListener的对应关系，消费端订阅时这里的NotifyListener其实就是RegistryDirectory，ChildListener是一个内部类，用来在监听的节点发生变更时，通知对应的消费端，具体的监听处理是在zkClient.addChildListener中实现的：

```
public List<String> addChildListener(String path, final ChildListener listener) {
    ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
    if (listeners == null) {
        childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener, TargetChildListener>());
        listeners = childListeners.get(path);
    }
    TargetChildListener targetListener = listeners.get(listener);
    if (targetListener == null) {
        listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
        targetListener = listeners.get(listener);
    }
    return addTargetChildListener(path, targetListener);
}
 
public CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
    return new CuratorWatcherImpl(listener);
}
 
public List<String> addTargetChildListener(String path, CuratorWatcher listener) {
    try {
        return client.getChildren().usingWatcher(listener).forPath(path);
    } catch (NoNodeException e) {
        return null;
    } catch (Exception e) {
        throw new IllegalStateException(e.getMessage(), e);
    }
}
 
private class CuratorWatcherImpl implements CuratorWatcher {
 
    private volatile ChildListener listener;
 
    public CuratorWatcherImpl(ChildListener listener) {
        this.listener = listener;
    }
 
    public void unwatch() {
        this.listener = null;
    }
 
    @Override
    public void process(WatchedEvent event) throws Exception {
        if (listener != null) {
            String path = event.getPath() == null ? "" : event.getPath();
            listener.childChanged(path,
                    StringUtils.isNotEmpty(path)
                            ? client.getChildren().usingWatcher(this).forPath(path)
                            : Collections.<String>emptyList());
        }
    }
}
```

CuratorWatcherImpl实现了Zookeeper的监听接口CuratorWatcher，用来在节点有变更时通知对应的ChildListener，这样ChildListener就可以通知RegistryDirectory进行更新数据；

### 6.退订(unsubscribe)

在父类FailbackRegistry中实现了unsubscribe方法

```
public void unsubscribe(URL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            // Sending a canceling subscription request to the server side
            doUnsubscribe(url, listener);
        } catch (Exception e) {
            Throwable t = e;
 
            // If the startup detection is opened, the Exception is thrown directly.
            boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true);
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to unsubscribe " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            } else {
                logger.error("Failed to unsubscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            }
 
            // Record a failed registration request to a failed list, retry regularly
            Set<NotifyListener> listeners = failedUnsubscribed.get(url);
            if (listeners == null) {
                failedUnsubscribed.putIfAbsent(url, new ConcurrentHashSet<NotifyListener>());
                listeners = failedUnsubscribed.get(url);
            }
            listeners.add(listener);
        }
    }
```

同样使用failedUnsubscribed用来存储失败退订的url，具体看ZookeeperRegistry中的doUnsubscribe方法

```
protected void doUnsubscribe(URL url, NotifyListener listener) {
        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
        if (listeners != null) {
            ChildListener zkListener = listeners.get(listener);
            if (zkListener != null) {
                if (Constants.ANY_VALUE.equals(url.getServiceInterface())) {
                    String root = toRootPath();
                    zkClient.removeChildListener(root, zkListener);
                } else {
                    for (String path : toCategoriesPath(url)) {
                        zkClient.removeChildListener(path, zkListener);
                    }
                }
            }
        }
    }
```

退订就比较简单了，只需要移除监听器就可以了；

### 7.失败重试

FailbackRegistry从名字可以看出来具有：失败自动恢复，后台记录失败请求，定时重发功能；在FailbackRegistry的构造器中启动了一个定时器：

```
this.retryFuture = retryExecutor.scheduleWithFixedDelay(new Runnable() {
           @Override
           public void run() {
               // Check and connect to the registry
               try {
                   retry();
               } catch (Throwable t) { // Defensive fault tolerance
                   logger.error("Unexpected error occur at failed retry, cause: " + t.getMessage(), t);
               }
           }
       }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
```

实例化了一个间隔5秒执行一次重试的定时器，retry部分代码如下：

```
protected void retry() {
        if (!failedRegistered.isEmpty()) {
            Set<URL> failed = new HashSet<URL>(failedRegistered);
            if (failed.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Retry register " + failed);
                }
                try {
                    for (URL url : failed) {
                        try {
                            doRegister(url);
                            failedRegistered.remove(url);
                        } catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
                            logger.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
                    logger.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        ...省略...
}
```

定期检查是否存在失败的注册(register)，注销(unregister)，订阅(subscribe)，退订(unsubscribe)URL，如果存在则重试；

## 总结

本文首先介绍了RegistryFactory, Registry, RegistryService几个核心接口，然后以Zookeeper为注册中心重点介绍了注册(register)，注销(unregister)，订阅(subscribe)，退订(unsubscribe)方式。

## 示例代码地址

[https://github.com/ksfzhaohui...](https://github.com/ksfzhaohui/blog)  
[https://gitee.com/OutOfMemory...](https://gitee.com/OutOfMemory/blog)