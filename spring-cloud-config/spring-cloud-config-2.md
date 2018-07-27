##**前言**
上文中简单的介绍了Spring-Cloud-Config如何使用，如何手动更新配置文件，并且在文末提出了几个疑问，其中包括多个Client节点如何更新，Server端如何保证高可用性等；本文将重点介绍通过使用Spring Cloud Bus来批量更新客户端，以及Server如何保证高可用；

##**Spring Cloud Bus消息总线**
Spring Cloud Bus使用轻量级消息代理链接分布式系统的节点，可以用于广播状态改变（例如，配置改变）或其他管理指令；目前唯一实现的方式是用AMQP消息代理作为通道，其实本质是利用了MQ的广播机制在分布式的系统中传播消息，目前常用的有Kafka和RabbitMQ；下面重点使用kafka来实现多客户端刷新配置文件；

###1.总体更新流程
大致流程图如下所示：
![图片描述][1]

###2.kafka安装部署
kafka部署依赖Zookeeper，使用的版本分别是：kafka_2.11-1.0.1和zookeeper-3.4.3，具体如何安装部署可参考：[Kafka快速开始][2]

###3.server端改造
3.1添加新的依赖

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-kafka</artifactId>
</dependency>
```
3.2application.properties添加配置

```
#开启消息总线
spring.cloud.bus.trace.enabled=true
spring.cloud.stream.kafka.binder.brokers=192.168.237.128
spring.cloud.stream.kafka.binder.defaultBrokerPort=9092
```
###4.client改造
4.1添加新的依赖

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-kafka</artifactId>
</dependency>
```
4.2application.properties添加配置

```
#开启消息总线
spring.cloud.bus.trace.enabled=true
spring.cloud.stream.kafka.binder.brokers=192.168.237.128
spring.cloud.stream.kafka.binder.defaultBrokerPort=9092
```
###5.启动测试
5.1启动Server端
观察启动日志，可以发现/actuator/bus-refresh映射

```
2018-07-18 10:51:44.434  INFO 12532 --- [           main] s.b.a.e.w.s.WebMvcEndpointHandlerMapping : Mapped "{[/actuator/bus-refresh],methods=[POST]}" onto public java.lang.Object org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)
```
往下可以有如下这行日志：

```
2018-07-18 10:52:08.803  INFO 6308 --- [           main] o.s.c.s.b.k.p.KafkaTopicProvisioner      : Using kafka topic for outbound: springCloudBus
```
Server端连接kafka创建了一个名称为springCloudBus的Topic，用来作为配置文件更新的消息通知；可以去kafka上查看：

```
[root@localhost bin]# ./kafka-topics.sh --list --zookeeper 10.13.83.7:2181
__consumer_offsets
springCloudBus
```
5.2启动Client
分别指定启动端口为8881和8882，可以看到和Server端类似的日志，订阅了名为springCloudBus的Topic，这样Server端发送消息给kafka，kafka通知client更新数据；

5.3测试
分别访问http://localhost:8881/hello和http://localhost:8882/hello，结果如下：

```
hello test
```
更新git中的配置文件为：

```
foo=hello test update
```
POST方式请求Server端，用来更新配置文件

```
c:\curl-7.61.0\I386>curl -X POST http://localhost:8888/actuator/bus-refresh
```
分别访问http://localhost:8881/hello和http://localhost:8882/hello，结果如下：

```
hello test update
```
2个客户端都获取到了最新的数据，表示更新成功；

在上图中我们发现Server端承担了太多的任务，而上图中Server端是一个单点，这样就不能保证系统高可用，下面看一下如何分布式部署Server端；

##**Server端保证高可用**
Server端通过注册中心Eureka来保证高可用，下面看一下具体流程：
![图片描述][3]

###1.Eureka注册中心
1.1Eureka-Server依赖

```
<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-eureka-server</artifactId>
        <version>1.4.5.RELEASE</version>
</dependency>
```
1.2启动配置文件

```
spring.application.name=eureka-server
server.port=8880
 
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.serviceUrl.defaultZone=http://localhost:8880/eureka/
```
eureka.client.register-with-eureka：是否将自己注册到Eureka Server，默认为true
eureka.client.fetch-registry：是否从Eureka Server获取注册信息，默认为true
eureka.client.serviceUrl.defaultZone：Eureka Server交互地址

1.3准备启动类

```
@SpringBootApplication
@EnableEurekaServer
public class EurekaServer {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer.class, args);
    }
}
```
###2.改造Server端（服务提供方）
2.1Eureka-Client依赖

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
2.2启动配置文件

```
eureka.client.serviceUrl.defaultZone=http://localhost:8880/eureka/
```
指定注册中心地址，也就是Eureka-Server配置的地址

2.3启动类添加@EnableDiscoveryClient注释，实现服务注册和发现

```
@EnableDiscoveryClient
@EnableConfigServer
@SpringBootApplication
public class ConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}
```
###3.改造Client端（服务消耗方）
3.1Eureka-Client依赖

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
3.2启动配置文件

```
#spring.cloud.config.uri= http://localhost:8888/
spring.cloud.config.discovery.enabled=true
spring.cloud.config.discovery.serviceId=config-server
eureka.client.serviceUrl.defaultZone=http://localhost:8880/eureka/
```
注释掉具体的Server端地址
spring.cloud.config.discovery.enabled：开启服务发现支持
spring.cloud.config.discovery.serviceId：服务提供端的名称
eureka.client.serviceUrl.defaultZone：配置中心的地址

###4.测试
首先启动注册中心eurekaServer，端口为8880；然后启动多个config-server端，端口分别为：8887，8888；最后启动多个config-client端，端口分别是：8883，8884；
可以查看注册中心，注册的服务：
![图片描述][4]

分别访问http://localhost:8883/hello和http://localhost:8884/hello，结果如下：

```
hello test
```
更新git中的配置文件为：

```
foo=hello test update
```
POST方式请求Server端，用来更新配置文件

```
c:\curl-7.61.0\I386>curl -X POST http://localhost:8888/actuator/bus-refresh
```
这里只是选择了其中一个server端去更新，任意一个都可以；

分别访问http://localhost:8883/hello和http://localhost:8884/hello，结果如下：

```
hello test update
```
2个客户端都获取到了最新的数据，表示更新成功；

将8888端口的Server端停掉，再次更新配置文件为

```
foo=hello test update2
```
POST方式请求Server端，用来更新配置文件

```
c:\curl-7.61.0\I386>curl -X POST http://localhost:8887/actuator/bus-refresh
```
分别访问http://localhost:8883/hello和http://localhost:8884/hello，结果如下：

```
hello test update2
```
2个客户端都获取到了最新的数据，表示更新成功；

##**总结**
通过消息总线的方式解决了多个Client更新的问题，以及通过eureka来保证Server的高可用性；当然eureka注册中心和消息总线本身也需要高可用性，这里就不过多介绍了。

##**示例代码地址**
Github:[https://github.com/ksfzhaohui/blog][5]


  [1]: /img/bVbepBw
  [2]: https://codingo.xyz/index.php/2017/01/19/kafka_1/
  [3]: /img/bVbepBX
  [4]: /img/bVbepCj
  [5]: https://github.com/ksfzhaohui/blog