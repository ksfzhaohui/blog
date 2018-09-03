## 系列文章

[Spring整合Quartz分布式调度](https://my.oschina.net/OutOfMemory/blog/1790200)

[Quartz数据库表分析](https://my.oschina.net/OutOfMemory/blog/1799185)

[Quartz调度源码分析](https://my.oschina.net/OutOfMemory/blog/1800560)

[基于Netty+Zookeeper+Quartz调度分析](https://my.oschina.net/OutOfMemory/blog/1941396)

# 前言

前几篇文章分别从使用和源码层面对Quartz做了简单的分析，在分析的过程中也发现了Quartz不足的地方；比如底层调度依赖数据库的悲观锁，谁先抢到谁调度，这样会导致节点负载不均衡；还有调度和执行耦合在一起，导致调度器会受到业务的影响；下面看看如何来解决这几个问题；

## 思路

调度器和执行器拆成不同的进程，调度器还是依赖Quartz本身的调度方式，但是调度的并不是具体业务的QuartzJobBean，而是统一的一个RemoteQuartzJobBean，在此Bean中通过Netty远程调用执行器去执行具体业务Bean；具体的执行器在启动时注册到Zookeeper中，调度器可以在Zookeeper获取执行器信息，并通过相关的负载算法指定具体的执行器去执行，以下看简单的实现；

## 执行器

### 1.执行器配置文件

```
executor_name=firstExecutor
service_address=127.0.0.1:8000
registry_address=127.0.0.1:2181
```

配置了执行器的名称，执行器启动的ip和端口以及Zookeeper的地址信息；

### 2.执行器服务

```
    <bean id="executorServer" class="com.zh.job.executor.ExecutorServer">
        <constructor-arg name="executorName" value="${executor_name}"/>
        <constructor-arg name="serviceAddress" value="${service_address}" />
        <constructor-arg name="serviceRegistry" ref="serviceRegistry" />
    </bean>
```

ExecutorServer通过Netty启动服务，并向Zookeeper注册服务，部分代码如下：

```
EventLoopGroup bossGroup = new NioEventLoopGroup();
EventLoopGroup workerGroup = new NioEventLoopGroup();
try {
    // 创建并初始化 Netty 服务端 Bootstrap 对象
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup);
    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(new RpcDecoder(Request.class));
            pipeline.addLast(new RpcEncoder(Response.class));
            pipeline.addLast(new ExecutorServerHandler(handlerMap));
        }
    });
    bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    // 获取 RPC 服务器的 IP 地址与端口号
    String[] addressArray = StringUtils.splitByWholeSeparator(serviceAddress, ":");
    String ip = addressArray[0];
    int port = Integer.parseInt(addressArray[1]);
    // 启动 RPC 服务器
    ChannelFuture future = bootstrap.bind(ip, port).sync();
    // 注册 RPC 服务地址
    if (serviceRegistry != null) {
        serviceRegistry.register(executorName, serviceAddress);
        LOGGER.info("register service: {} => {}", executorName, serviceAddress);
    }
    LOGGER.info("server started on port {}", port);
    // 关闭 RPC 服务器
    future.channel().closeFuture().sync();
} finally {
    workerGroup.shutdownGracefully();
    bossGroup.shutdownGracefully();
}
```

在Netty中指定了编码器解码器，同时指定了ExecutorServerHandler用来处理调度器发送来的消息（更多代码查看项目源码）；最后向Zookeeper注册服务，路径格式如下：

```
/job_registry/firstExecutor/address-0000000008
```

job_registry是固定值，firstExecutor是配置的具体执行器名称；

### 3.配置加载任务

添加注解类，用来指定具体的业务Job：

```
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ExecutorTask {
 
    String name();
 
}
```

例如具体的业务Task如下所示：

```
@ExecutorTask(name = "firstTask")
public class FirstTask implements IJobHandler {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(FirstTask.class);
 
    @Override
    public Result execute(String param) throws Exception {
        LOGGER.info("execute firstTask");
        return SUCCESS;
    }
 
}
```

在启动执行器服务时，加载有ExecutorTask注解的任务类，此处定义的name要和调度端的名称相互匹配；

### 4.执行具体业务

Netty中指定了ExecutorServerHandler用来处理接受的调度器信息，通过反射的方式来调用具体的业务Job，部分代码如下：

```
private Object handle(Request request) throws Exception {
     // 获取服务对象
     String serviceName = request.getInterfaceName();
     Object serviceBean = handlerMap.get(serviceName);
     if (serviceBean == null) {
         throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
     }
     // 获取反射调用所需的参数
     Class<?> serviceClass = serviceBean.getClass();
     String methodName = request.getMethodName();
     Class<?>[] parameterTypes = request.getParameterTypes();
     Object[] parameters = request.getParameters();
     // 使用 CGLib 执行反射调用
     FastClass serviceFastClass = FastClass.create(serviceClass);
     FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
     return serviceFastMethod.invoke(serviceBean, parameters);
 }
```

serviceName对应的就是定义的”firstTask”，然后通过serviceName找到对应的Bean，然后反射调用，最终返回结果；

## 调度器

调度器还是依赖Quartz的原生调度方式，只不过调度器不在执行相关业务Task，所以相关配置也是类似，同样依赖数据库；

### 1.定义调度任务

```
<bean id="firstTask"
     class="org.springframework.scheduling.quartz.JobDetailFactoryBean">
     <property name="jobClass" value="com.zh.job.scheduler.RemoteQuartzJobBean" />
     <property name="jobDataMap">
         <map>
             <entry key="executorBean" value-ref="firstExecutor" />
         </map>
     </property>
 </bean>
 
 <bean id="firstExecutor" class="com.zh.job.scheduler.ExecutorBean">
     <constructor-arg name="executorName" value="firstExecutor"></constructor-arg>
     <constructor-arg name="discoveryAddress" value="${discovery_address}"></constructor-arg>
 </bean>
```

同样在调度端定义了名称问firstTask的任务，可以发现此类是RemoteQuartzJobBean，并不是具体的业务Task；同时也指定了jobDataMap，用来指定执行器名称和发现的Zookeeper地址；

### 2.RemoteQuartzJobBean

```
public class RemoteQuartzJobBean extends QuartzJobBean {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteQuartzJobBean.class);
 
    private ExecutorBean executorBean;
 
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        LOGGER.info("jobName:" + jobKey.getName() + ",group:" + jobKey.getGroup());
        IJobHandler executor = JobProxy.create(IJobHandler.class, jobKey, this.executorBean);
        Result result;
        try {
            result = executor.execute("");
            LOGGER.info("result:" + result);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
 
    public ExecutorBean getExecutorBean() {
        return executorBean;
    }
 
    public void setExecutorBean(ExecutorBean executorBean) {
        this.executorBean = executorBean;
    }
 
}
```

此类同样继承于QuartzJobBean，这样Quartz才能调度Bean，在此Bean中通过jobKey和executorBean创建了IJobHandler的代理类，具体代码如下：

```
public static <T> T create(final Class<?> interfaceClass, final JobKey jobKey, final ExecutorBean executor) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象并设置请求属性
                        Request request = new Request();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(jobKey.getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
 
                        String serviceAddress = null;
                        ServiceDiscovery serviceDiscovery = ServiceDiscoveryFactory
                                .getServiceDiscovery(executor.getDiscoveryAddress());
                        // 获取 RPC 服务地址
                        if (serviceDiscovery != null) {
                            serviceAddress = serviceDiscovery.discover(executor.getExecutorName());
                            LOGGER.debug("discover service: {} => {}", executor.getExecutorName(), serviceAddress);
                        }
                        if (StringUtil.isEmpty(serviceAddress)) {
                            throw new RuntimeException("server address is empty");
                        }
                        // 从 RPC 服务地址中解析主机名与端口号
                        String[] array = StringUtil.split(serviceAddress, ":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        // 创建 RPC 客户端对象并发送 RPC 请求
                        ExecutorClient client = new ExecutorClient(host, port);
                        long time = System.currentTimeMillis();
                        Response response = client.send(request);
                        LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
                        if (response == null) {
                            throw new RuntimeException("response is null");
                        }
                        // 返回 RPC 响应结果
                        if (response.hasException()) {
                            throw response.getException();
                        } else {
                            return response.getResult();
                        }
                    }
                });
    }
```

在Request中指定了InterfaceName为jobKey.getName()，也就是这里的firstTask；通过Zookeeper发现服务时指定了executor.getExecutorName()，这样可以在Zookeeper中找到具体的执行器地址，当然这里的地址可能是一个列表，可以通过负载均衡算法(随机，轮询，一致性hash等等)进行分配，获取到地址后通过Netty远程连接执行器，发送执行job等待返回结果；

## 简单测试

分别执行调度器和执行器，相关日志如下：

### 1.执行器日志

```
2018-09-03 11:17:02 [main] 13::: DEBUG com.zh.job.sample.executor.ExecutorBootstrap - start server
2018-09-03 11:17:03 [main] 31::: DEBUG com.zh.job.registry.impl.ZookeeperServiceRegistry - connect zookeeper
2018-09-03 11:17:03 [main] 49::: DEBUG com.zh.job.registry.impl.ZookeeperServiceRegistry - create address node: /job_registry/firstExecutor/address-0000000009
2018-09-03 11:17:03 [main] 107::: INFO  com.zh.job.executor.ExecutorServer - register service: firstExecutor => 127.0.0.1:8000
2018-09-03 11:17:03 [main] 109::: INFO  com.zh.job.executor.ExecutorServer - server started on port 8000
2018-09-03 11:17:15 [nioEventLoopGroup-3-1] 17::: INFO  com.zh.job.sample.executor.task.FirstTask - execute firstTask
```

### 2.调度器日志

```
2018-09-03 11:17:14 [myScheduler_Worker-1] 28::: INFO  com.zh.job.scheduler.RemoteQuartzJobBean - jobName:firstTask,group:DEFAULT
2018-09-03 11:17:15 [myScheduler_Worker-2] 28::: INFO  com.zh.job.scheduler.RemoteQuartzJobBean - jobName:firstTask,group:DEFAULT
2018-09-03 11:17:15 [myScheduler_Worker-1] 33::: DEBUG com.zh.job.registry.impl.ZookeeperServiceDiscovery - connect zookeeper
2018-09-03 11:17:15 [myScheduler_Worker-2] 54::: DEBUG com.zh.job.registry.impl.ZookeeperServiceDiscovery - get only address node: address-0000000009
2018-09-03 11:17:15 [myScheduler_Worker-1] 54::: DEBUG com.zh.job.registry.impl.ZookeeperServiceDiscovery - get only address node: address-0000000009
2018-09-03 11:17:15 [myScheduler_Worker-2] 42::: DEBUG com.zh.job.scheduler.JobProxy$1 - discover service: firstExecutor => 127.0.0.1:8000
2018-09-03 11:17:15 [myScheduler_Worker-1] 42::: DEBUG com.zh.job.scheduler.JobProxy$1 - discover service: firstExecutor => 127.0.0.1:8000
2018-09-03 11:17:15 [myScheduler_Worker-1] 55::: DEBUG com.zh.job.scheduler.JobProxy$1 - time: 369ms
2018-09-03 11:17:15 [myScheduler_Worker-1] 33::: INFO  com.zh.job.scheduler.RemoteQuartzJobBean - result:com.zh.job.common.bean.Result@33b61489
```

## 总结

本文通过一个实例来分析如何解决原生Quartz调度存在不足的问题，主要体现在调度器与执行器的隔离上，各司其责发挥各自的优势；

## 示例代码地址

[https://github.com/ksfzhaohui...](https://github.com/ksfzhaohui/job)