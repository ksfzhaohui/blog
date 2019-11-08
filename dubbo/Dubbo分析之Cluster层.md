## 系列文章

[Dubbo分析Serialize层](https://my.oschina.net/OutOfMemory/blog/2236611)  
[Dubbo分析之Transport层](https://my.oschina.net/OutOfMemory/blog/2251388)  
[Dubbo分析之Exchange 层](https://my.oschina.net/OutOfMemory/blog/2252445)  
[Dubbo分析之Protocol层](https://my.oschina.net/OutOfMemory/blog/2413695)  
[Dubbo分析之Cluster层](https://my.oschina.net/OutOfMemory/blog/2885469)  
[Dubbo分析之Registry层](https://my.oschina.net/OutOfMemory/blog/2991498)

## 前言

紧接上文[Dubbo分析之Protocol层](https://segmentfault.com/a/1190000016885261)，本文继续分析dubbo的cluster层，此层封装多个提供者的路由及负载均衡，并桥接注册中心，以Invoker为中心，扩展接口为Cluster, Directory, Router, LoadBalance；

## Cluster接口

整个cluster层可以使用如下图片概括：  
![](https://oscimg.oschina.net/oscnet/0e1d2a885fcf5ecf173fbcb18740d12046d.jpg)

各节点关系：  
这里的Invoker是Provider的一个可调用Service的抽象，Invoker封装了Provider地址及Service接口信息；  
Directory代表多个Invoker，可以把它看成List ，但与List不同的是，它的值可能是动态变化的，比如注册中心推送变更；  
Cluster将Directory中的多个Invoker伪装成一个 Invoker，对上层透明，伪装过程包含了容错逻辑，调用失败后，重试另一个；  
Router负责从多个Invoker中按路由规则选出子集，比如读写分离，应用隔离等；  
LoadBalance负责从多个Invoker中选出具体的一个用于本次调用，选的过程包含了负载均衡算法，调用失败后，需要重选；

Cluster经过目录，路由，负载均衡获取到一个可用的Invoker，交给上层调用，接口如下：

```
@SPI(FailoverCluster.NAME)
public interface Cluster {
 
    /**
     * Merge the directory invokers to a virtual invoker.
     *
     * @param <T>
     * @param directory
     * @return cluster invoker
     * @throws RpcException
     */
    @Adaptive
    <T> Invoker<T> join(Directory<T> directory) throws RpcException;
 
}
```

Cluster是一个集群容错接口，经过路由，负载均衡之后获取的Invoker，由容错机制来处理，dubbo提供了多种容错机制包括：  
**Failover Cluster：**失败自动切换，当出现失败，重试其它服务器 \[1\]。通常用于读操作，但重试会带来更长延迟。可通过 retries=”2″ 来设置重试次数(不含第一次)。  
**Failfast Cluster：**快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。  
**Failsafe Cluster：**失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。  
**Failback Cluster：**失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。  
**Forking Cluster：**并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks=”2″ 来设置最大并行数。  
**Broadcast Cluster：**广播调用所有提供者，逐个调用，任意一台报错则报错 \[2\]。通常用于通知所有提供者更新缓存或日志等本地资源信息。

默认使用了FailoverCluster，失败的时候会默认重试其他服务器，默认为两次；当然也可以扩展其他的容错机制；看一下默认的FailoverCluster容错机制，具体源码在FailoverClusterInvoker中：

```
public Result doInvoke(Invocation invocation, final List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
       List<Invoker<T>> copyinvokers = invokers;
       checkInvokers(copyinvokers, invocation);
       int len = getUrl().getMethodParameter(invocation.getMethodName(), Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES) + 1;
       if (len <= 0) {
           len = 1;
       }
       // retry loop.
       RpcException le = null; // last exception.
       List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(copyinvokers.size()); // invoked invokers.
       Set<String> providers = new HashSet<String>(len);
       for (int i = 0; i < len; i++) {
           //Reselect before retry to avoid a change of candidate `invokers`.
           //NOTE: if `invokers` changed, then `invoked` also lose accuracy.
           if (i > 0) {
               checkWhetherDestroyed();
               copyinvokers = list(invocation);
               // check again
               checkInvokers(copyinvokers, invocation);
           }
           Invoker<T> invoker = select(loadbalance, invocation, copyinvokers, invoked);
           invoked.add(invoker);
           RpcContext.getContext().setInvokers((List) invoked);
           try {
               Result result = invoker.invoke(invocation);
               if (le != null && logger.isWarnEnabled()) {
                   logger.warn("Although retry the method " + invocation.getMethodName()
                           + " in the service " + getInterface().getName()
                           + " was successful by the provider " + invoker.getUrl().getAddress()
                           + ", but there have been failed providers " + providers
                           + " (" + providers.size() + "/" + copyinvokers.size()
                           + ") from the registry " + directory.getUrl().getAddress()
                           + " on the consumer " + NetUtils.getLocalHost()
                           + " using the dubbo version " + Version.getVersion() + ". Last error is: "
                           + le.getMessage(), le);
               }
               return result;
           } catch (RpcException e) {
               if (e.isBiz()) { // biz exception.
                   throw e;
               }
               le = e;
           } catch (Throwable e) {
               le = new RpcException(e.getMessage(), e);
           } finally {
               providers.add(invoker.getUrl().getAddress());
           }
       }
       throw new RpcException(le != null ? le.getCode() : 0, "Failed to invoke the method "
               + invocation.getMethodName() + " in the service " + getInterface().getName()
               + ". Tried " + len + " times of the providers " + providers
               + " (" + providers.size() + "/" + copyinvokers.size()
               + ") from the registry " + directory.getUrl().getAddress()
               + " on the consumer " + NetUtils.getLocalHost() + " using the dubbo version "
               + Version.getVersion() + ". Last error is: "
               + (le != null ? le.getMessage() : ""), le != null && le.getCause() != null ? le.getCause() : le);
   }
```

invocation是客户端传给服务器的相关参数包括(方法名称，方法参数，参数值，附件信息)，invokers是经过路由之后的服务器列表，loadbalance是指定的负载均衡策略；首先检查invokers是否为空，为空直接抛异常，然后获取重试的次数默认为2次，接下来就是循环调用指定次数，如果不是第一次调用(表示第一次调用失败)，会重新加载服务器列表，然后通过负载均衡策略获取唯一的Invoker，最后就是通过Invoker把invocation发送给服务器，返回结果Result；

具体的doInvoke方法是在抽象类AbstractClusterInvoker中被调用的：

```
public Result invoke(final Invocation invocation) throws RpcException {
       checkWhetherDestroyed();
       LoadBalance loadbalance = null;
       List<Invoker<T>> invokers = list(invocation);
       if (invokers != null && !invokers.isEmpty()) {
           loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl()
                   .getMethodParameter(RpcUtils.getMethodName(invocation), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
       }
       RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
       return doInvoke(invocation, invokers, loadbalance);
   }
    
    protected List<Invoker<T>> list(Invocation invocation) throws RpcException {
       List<Invoker<T>> invokers = directory.list(invocation);
       return invokers;
   }
```

首先通过Directory获取Invoker列表，同时在Directory中也会做路由处理，然后获取负载均衡策略，最后调用具体的容错策略；下面具体看一下Directory；

## Directory接口

接口定义如下：

```
public interface Directory<T> extends Node {
 
    /**
     * get service type.
     *
     * @return service type.
     */
    Class<T> getInterface();
 
    /**
     * list invokers.
     *
     * @return invokers
     */
    List<Invoker<T>> list(Invocation invocation) throws RpcException;
 
}
```

目录服务作用就是获取指定接口的服务列表，具体实现有两个：StaticDirectory和RegistryDirectory，同时都继承于AbstractDirectory；从名字可以大致知道StaticDirectory是一个固定的目录服务，表示里面的Invoker列表不会动态改变；RegistryDirectory是一个动态的目录服务，通过注册中心动态更新服务列表；list实现在抽象类中：

```
public List<Invoker<T>> list(Invocation invocation) throws RpcException {
       if (destroyed) {
           throw new RpcException("Directory already destroyed .url: " + getUrl());
       }
       List<Invoker<T>> invokers = doList(invocation);
       List<Router> localRouters = this.routers; // local reference
       if (localRouters != null && !localRouters.isEmpty()) {
           for (Router router : localRouters) {
               try {
                   if (router.getUrl() == null || router.getUrl().getParameter(Constants.RUNTIME_KEY, false)) {
                       invokers = router.route(invokers, getConsumerUrl(), invocation);
                   }
               } catch (Throwable t) {
                   logger.error("Failed to execute router: " + getUrl() + ", cause: " + t.getMessage(), t);
               }
           }
       }
       return invokers;
   }
```

首先检查目录是否被销毁，然后调用doList，具体在实现类中定义，最后调用路由功能，下面重点看一下StaticDirectory和RegistryDirectory中的doList方法

### 1.RegistryDirectory

是一个动态的目录服务，所有可以看到RegistryDirectory同时也继承了NotifyListener接口，是一个通知接口，注册中心有服务列表更新的时候，同时通知RegistryDirectory，通知逻辑如下：

```
public synchronized void notify(List<URL> urls) {
        List<URL> invokerUrls = new ArrayList<URL>();
        List<URL> routerUrls = new ArrayList<URL>();
        List<URL> configuratorUrls = new ArrayList<URL>();
        for (URL url : urls) {
            String protocol = url.getProtocol();
            String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
            if (Constants.ROUTERS_CATEGORY.equals(category)
                    || Constants.ROUTE_PROTOCOL.equals(protocol)) {
                routerUrls.add(url);
            } else if (Constants.CONFIGURATORS_CATEGORY.equals(category)
                    || Constants.OVERRIDE_PROTOCOL.equals(protocol)) {
                configuratorUrls.add(url);
            } else if (Constants.PROVIDERS_CATEGORY.equals(category)) {
                invokerUrls.add(url);
            } else {
                logger.warn("Unsupported category " + category + " in notified url: " + url + " from registry " + getUrl().getAddress() + " to consumer " + NetUtils.getLocalHost());
            }
        }
        // configurators
        if (configuratorUrls != null && !configuratorUrls.isEmpty()) {
            this.configurators = toConfigurators(configuratorUrls);
        }
        // routers
        if (routerUrls != null && !routerUrls.isEmpty()) {
            List<Router> routers = toRouters(routerUrls);
            if (routers != null) { // null - do nothing
                setRouters(routers);
            }
        }
        List<Configurator> localConfigurators = this.configurators; // local reference
        // merge override parameters
        this.overrideDirectoryUrl = directoryUrl;
        if (localConfigurators != null && !localConfigurators.isEmpty()) {
            for (Configurator configurator : localConfigurators) {
                this.overrideDirectoryUrl = configurator.configure(overrideDirectoryUrl);
            }
        }
        // providers
        refreshInvoker(invokerUrls);
    }
```

此通知接口会接受三种类别的url包括：router(路由)，configurator(配置)，provider(服务提供方)；  
路由规则：决定一次dubbo服务调用的目标服务器，分为条件路由规则和脚本路由规则，并且支持可扩展，向注册中心写入路由规则的操作通常由监控中心或治理中心的页面完成；  
配置规则：向注册中心写入动态配置覆盖规则 \[1\]。该功能通常由监控中心或治理中心的页面完成；  
provider：动态提供的服务列表  
路由规则和配置规则其实就是对provider服务列表更新和过滤处理，refreshInvoker方法就是根据三种url类别刷新本地的invoker列表，下面看一下RegistryDirectory实现的doList接口：

```
public List<Invoker<T>> doList(Invocation invocation) {
        if (forbidden) {
            // 1. No service provider 2. Service providers are disabled
            throw new RpcException(RpcException.FORBIDDEN_EXCEPTION,
                "No provider available from registry " + getUrl().getAddress() + " for service " + getConsumerUrl().getServiceKey() + " on consumer " +  NetUtils.getLocalHost()
                        + " use dubbo version " + Version.getVersion() + ", please check status of providers(disabled, not registered or in blacklist).");
        }
        List<Invoker<T>> invokers = null;
        Map<String, List<Invoker<T>>> localMethodInvokerMap = this.methodInvokerMap; // local reference
        if (localMethodInvokerMap != null && localMethodInvokerMap.size() > 0) {
            String methodName = RpcUtils.getMethodName(invocation);
            Object[] args = RpcUtils.getArguments(invocation);
            if (args != null && args.length > 0 && args[0] != null
                    && (args[0] instanceof String || args[0].getClass().isEnum())) {
                invokers = localMethodInvokerMap.get(methodName + "." + args[0]); // The routing can be enumerated according to the first parameter
            }
            if (invokers == null) {
                invokers = localMethodInvokerMap.get(methodName);
            }
            if (invokers == null) {
                invokers = localMethodInvokerMap.get(Constants.ANY_VALUE);
            }
            if (invokers == null) {
                Iterator<List<Invoker<T>>> iterator = localMethodInvokerMap.values().iterator();
                if (iterator.hasNext()) {
                    invokers = iterator.next();
                }
            }
        }
        return invokers == null ? new ArrayList<Invoker<T>>(0) : invokers;
    }
```

refreshInvoker处理之后，服务列表已methodInvokerMap存在，一个方法对应服务列表Map>>；  
通过Invocation中指定的方法获取对应的服务列表，如果具体的方法没有对应的服务列表，则获取”*”对应的服务列表；处理完之后就在父类中进行路由处理，路由规则同样是通过通知接口获取的，路由规则在下章介绍；

### 2.StaticDirectory

这是一个静态的目录服务，里面的服务列表在初始化的时候就已经存在，并且不会改变；StaticDirectory用得比较少,主要用在服务对多注册中心的引用；

```
protected List<Invoker<T>> doList(Invocation invocation) throws RpcException {
 
    return invokers;
}
```

因为是静态的，所有doList方法也很简单，直接返回内存中的服务列表即可；

## Router接口

路由规则决定一次dubbo服务调用的目标服务器，分为条件路由规则和脚本路由规则，并且支持可扩展，接口如下：

```
public interface Router extends Comparable<Router> {
 
    /**
     * get the router url.
     *
     * @return url
     */
    URL getUrl();
 
    /**
     * route.
     *
     * @param invokers
     * @param url        refer url
     * @param invocation
     * @return routed invokers
     * @throws RpcException
     */
    <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;
 
}
```

接口中提供的route方法通过一定的规则过滤出invokers的一个子集；提供了三个实现类：ScriptRouter，ConditionRouter和MockInvokersSelector  
ScriptRouter：脚本路由规则支持 JDK 脚本引擎的所有脚本，比如：javascript, jruby, groovy 等，通过type=javascript参数设置脚本类型，缺省为javascript；  
ConditionRouter：基于条件表达式的路由规则，如：host = 10.20.153.10 => host = 10.20.153.11；=> 之前的为消费者匹配条件，所有参数和消费者的 URL 进行对比，=> 之后为提供者地址列表的过滤条件，所有参数和提供者的 URL 进行对比；  
MockInvokersSelector：是否被配置为使用mock，此路由器保证只有具有协议MOCK的调用者出现在最终的调用者列表中，所有其他调用者将被排除；

下面重点看一下ScriptRouter源码

```
public ScriptRouter(URL url) {
       this.url = url;
       String type = url.getParameter(Constants.TYPE_KEY);
       this.priority = url.getParameter(Constants.PRIORITY_KEY, 0);
       String rule = url.getParameterAndDecoded(Constants.RULE_KEY);
       if (type == null || type.length() == 0) {
           type = Constants.DEFAULT_SCRIPT_TYPE_KEY;
       }
       if (rule == null || rule.length() == 0) {
           throw new IllegalStateException(new IllegalStateException("route rule can not be empty. rule:" + rule));
       }
       ScriptEngine engine = engines.get(type);
       if (engine == null) {
           engine = new ScriptEngineManager().getEngineByName(type);
           if (engine == null) {
               throw new IllegalStateException(new IllegalStateException("Unsupported route rule type: " + type + ", rule: " + rule));
           }
           engines.put(type, engine);
       }
       this.engine = engine;
       this.rule = rule;
   }
```

构造器分别初始化脚本引擎(engine)和脚本代码(rule)，默认的脚本引擎是javascript；看一个具体的url：

```
"script://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" + URL.encode("（function route(invokers) { ... } (invokers)）")
```

script协议表示一个脚本协议，rule后面是一段javascript脚本，传入的参数是invokers；

```
（function route(invokers) {
    var result = new java.util.ArrayList(invokers.size());
    for (i = 0; i < invokers.size(); i ++) {
        if ("10.20.153.10".equals(invokers.get(i).getUrl().getHost())) {
            result.add(invokers.get(i));
        }
    }
    return result;
} (invokers)）; // 表示立即执行方法
```

如上这段脚本过滤出host为10.20.153.10，具体是如何执行这段脚本的，在route方法中：

```
public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
     try {
         List<Invoker<T>> invokersCopy = new ArrayList<Invoker<T>>(invokers);
         Compilable compilable = (Compilable) engine;
         Bindings bindings = engine.createBindings();
         bindings.put("invokers", invokersCopy);
         bindings.put("invocation", invocation);
         bindings.put("context", RpcContext.getContext());
         CompiledScript function = compilable.compile(rule);
         Object obj = function.eval(bindings);
         if (obj instanceof Invoker[]) {
             invokersCopy = Arrays.asList((Invoker<T>[]) obj);
         } else if (obj instanceof Object[]) {
             invokersCopy = new ArrayList<Invoker<T>>();
             for (Object inv : (Object[]) obj) {
                 invokersCopy.add((Invoker<T>) inv);
             }
         } else {
             invokersCopy = (List<Invoker<T>>) obj;
         }
         return invokersCopy;
     } catch (ScriptException e) {
         //fail then ignore rule .invokers.
         logger.error("route error , rule has been ignored. rule: " + rule + ", method:" + invocation.getMethodName() + ", url: " + RpcContext.getContext().getUrl(), e);
         return invokers;
     }
 }
```

首先通过脚本引擎编译脚本，然后执行脚本，同时传入Bindings参数，这样在脚本中就可以获取invokers，然后进行过滤；最后来看一下负载均衡策略

## LoadBalance接口

在集群负载均衡时，Dubbo提供了多种均衡策略，缺省为random随机调用，可以自行扩展负载均衡策略；接口类如下：

```
@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {
 
    /**
     * select one invoker in list.
     *
     * @param invokers   invokers.
     * @param url        refer url
     * @param invocation invocation.
     * @return selected invoker.
     */
    @Adaptive("loadbalance")
    <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;
 
}
```

SPI定义了默认的策略为RandomLoadBalance，提供了一个select方法，通过策略从服务列表中选择一个invoker；dubbo默认提供了多种策略：  
**Random LoadBalance：**随机，按权重设置随机概率，在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重；  
**RoundRobin LoadBalance：**轮询，按公约后的权重设置轮询比率；存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，  
久而久之，所有请求都卡在调到第二台上；  
**LeastActive LoadBalance：**最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差；使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大；  
**ConsistentHash LoadBalance**：一致性 Hash，相同参数的请求总是发到同一提供者；当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动；

下面重点看一下默认的RandomLoadBalance源码

```
protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size(); // Number of invokers
        int totalWeight = 0; // The sum of weights
        boolean sameWeight = true; // Every invoker has the same weight?
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            totalWeight += weight; // Sum
            if (sameWeight && i > 0
                    && weight != getWeight(invokers.get(i - 1), invocation)) {
                sameWeight = false;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            // If (not every invoker has the same weight & at least one invoker's weight>0), select randomly based on totalWeight.
            int offset = random.nextInt(totalWeight);
            // Return a invoker based on the random value.
            for (int i = 0; i < length; i++) {
                offset -= getWeight(invokers.get(i), invocation);
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        // If all invokers have the same weight value or totalWeight=0, return evenly.
        return invokers.get(random.nextInt(length));
    }
```

首先计算总权重，同时检查是否每一个服务都有相同的权重；如果总权重大于0并且服务的权重都不相同，则通过权重来随机选择，否则直接通过Random函数来随机；

## 总结

本文围绕Cluster层中的几个重要的接口从上到下来分别介绍，并重点介绍了其中的某些实现类；结合官方提供的调用图，还是很容易理解此层的。