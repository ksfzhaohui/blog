**Zipkin是什么**  
[Zipkin](https://zipkin.io/)分布式跟踪系统；它可以帮助收集时间数据，解决在microservice架构下的延迟问题；它管理这些数据的收集和查找；Zipkin的设计是基于谷歌的Google Dapper论文。  
每个应用程序向Zipkin报告定时数据，Zipkin UI呈现了一个依赖图表来展示多少跟踪请求经过了每个应用程序；如果想解决延迟问题，可以过滤或者排序所有的跟踪请求，并且可以查看每个跟踪请求占总跟踪时间的百分比。

**为什么使用Zipkin**  
随着业务越来越复杂，系统也随之进行各种拆分，特别是随着微服务架构和容器技术的兴起，看似简单的一个应用，后台可能有几十个甚至几百个服务在支撑；一个前端的请求可能需要多次的服务调用最后才能完成；当请求变慢或者不可用时，我们无法得知是哪个后台服务引起的，这时就需要解决如何快速定位服务故障点，Zipkin分布式跟踪系统就能很好的解决这样的问题。

**Zipkin下载和启动**  
官方提供了三种方式来启动，这里使用第二种方式来启动;

```bash
wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec'
java -jar zipkin.jar
```

首先下载zipkin.jar，然后直接使用-jar命令运行，要求jdk8以上版本；

```bash
[root@localhost ~]# java -jar zipkin.jar 
                                    ********
                                  **        **
                                 *            *
                                **            **
                                **            **
                                 **          **
                                  **        **
                                    ********
                                      ****
                                      ****
        ****                          ****
     ******                           ****                                 ***
  ****************************************************************************
    *******                           ****                                 ***
        ****                          ****
                                       **
                                       **
 
 
             *****      **     *****     ** **       **     **   **
               **       **     **  *     ***         **     **** **
              **        **     *****     ****        **     **  ***
             ******     **     **        **  **      **     **   **
 
:: Powered by Spring Boot ::         (v1.5.8.RELEASE)
......
on || application/json]}" onto public java.lang.Object org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint.invoke(javax.servlet.http.HttpServletRequest,java.security.Principal)
2017-12-06 22:09:17.498  INFO 7555 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2017-12-06 22:09:17.505  INFO 7555 --- [           main] o.s.c.support.DefaultLifecycleProcessor  : Starting beans in phase 0
2017-12-06 22:09:17.789  INFO 7555 --- [           main] b.c.e.u.UndertowEmbeddedServletContainer : Undertow started on port(s) 9411 (http)
2017-12-06 22:09:17.794  INFO 7555 --- [           main] zipkin.server.ZipkinServer               : Started ZipkinServer in 16.867 seconds (JVM running for 19.199)

```

基于Undertow WEB服务器，提供对外端口：9411，可以打开浏览器访问http://ip:9411  
![](https://static.oschina.net/uploads/space/2017/1208/151736_O2w0_159239.jpg)

详细参考：[https://zipkin.io/pages/quickstart.html](https://zipkin.io/pages/quickstart.html)

**Zipkin架构**  
跟踪器(Tracer)位于你的应用程序中，并记录发生的操作的时间和元数据,提供了相应的类库，对用户的使用来说是透明的，收集的跟踪数据称为Span;  
将数据发送到Zipkin的仪器化应用程序中的组件称为Reporter,Reporter通过几种传输方式之一将追踪数据发送到Zipkin收集器(collector)，  
然后将跟踪数据进行存储(storage),由API查询存储以向UI提供数据。  
架构图如下：

![](https://static.oschina.net/uploads/space/2017/1208/151808_9EAX_159239.png)

1.Trace  
Zipkin使用Trace结构表示对一次请求的跟踪，一次请求可能由后台的若干服务负责处理，每个服务的处理是一个Span，Span之间有依赖关系，Trace就是树结构的Span集合；

2.Span  
每个服务的处理跟踪是一个Span，可以理解为一个基本的工作单元，包含了一些描述信息：id，parentId，name，timestamp，duration，annotations等，例如：

```json
{
      "traceId": "bd7a977555f6b982",
      "name": "get-traces",
      "id": "ebf33e1a81dc6f71",
      "parentId": "bd7a977555f6b982",
      "timestamp": 1458702548478000,
      "duration": 354374,
      "annotations": [
        {
          "endpoint": {
            "serviceName": "zipkin-query",
            "ipv4": "192.168.1.2",
            "port": 9411
          },
          "timestamp": 1458702548786000,
          "value": "cs"
        }
      ],
      "binaryAnnotations": [
        {
          "key": "lc",
          "value": "JDBCSpanStore",
          "endpoint": {
            "serviceName": "zipkin-query",
            "ipv4": "192.168.1.2",
            "port": 9411
          }
        }
      ]
}
```

traceId：标记一次请求的跟踪，相关的Spans都有相同的traceId；  
id：span id；  
name：span的名称，一般是接口方法的名称；  
parentId：可选的id，当前Span的父Span id，通过parentId来保证Span之间的依赖关系，如果没有parentId，表示当前Span为根Span；  
timestamp：Span创建时的时间戳，使用的单位是微秒（而不是毫秒），所有时间戳都有错误，包括主机之间的时钟偏差以及时间服务重新设置时钟的可能性，  
出于这个原因，Span应尽可能记录其duration；  
duration：持续时间使用的单位是微秒（而不是毫秒）；  
annotations：注释用于及时记录事件；有一组核心注释用于定义RPC请求的开始和结束；

```
cs:Client Send，客户端发起请求；
sr:Server Receive，服务器接受请求，开始处理；
ss:Server Send，服务器完成处理，给客户端应答；
cr:Client Receive，客户端接受应答从服务器；
```

binaryAnnotations：二进制注释，旨在提供有关RPC的额外信息。

3.Transport  
收集的Spans必须从被追踪的服务运输到Zipkin collector，有三个主要的传输方式：HTTP, Kafka和Scribe；

4.Components  
有4个组件组成Zipkin：collector，storage，search，web UI  
collector：一旦跟踪数据到达Zipkin collector守护进程，它将被验证，存储和索引，以供Zipkin收集器查找；  
storage：Zipkin最初数据存储在Cassandra上，因为Cassandra是可扩展的，具有灵活的模式，并在Twitter中大量使用；但是这个组件可插入，除了Cassandra之外，还支持ElasticSearch和MySQL；  
search：一旦数据被存储和索引，我们需要一种方法来提取它。查询守护进程提供了一个简单的JSON API来查找和检索跟踪，主要给Web UI使用；  
web UI：创建了一个GUI，为查看痕迹提供了一个很好的界面；Web UI提供了一种基于服务，时间和注释查看跟踪的方法。

**实战**  
使用Zipkin和Brave实现http服务调用的跟踪，Brave 是用来装备Java程序的类库，提供了面向标准Servlet、Spring MVC、Http Client、JAX RS、Jersey、Resteasy 和 MySQL 等接口的装备能力，可以通过编写简单的配置和代码，让基于这些框架构建的应用可以向 Zipkin 报告数据。同时 Brave 也提供了非常简单且标准化的接口，在以上封装无法满足要求的时候可以方便扩展与定制。

提供四个工程，分别对应四个服务分别是：zipkin1，zipkin2，zipkin3，zipkin4；zipkin1通过httpclient调用zipkin2，然后zipkin2通过httpclient调用zipkin3和zipkin4，形成一个调用链；四个服务都是基于spring-boot来实现，对应的端口分别是8081，8082，8083，8084；

1.公共maven依赖库

```
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>io.zipkin.brave</groupId>
        <artifactId>brave-core</artifactId>
        <version>3.9.0</version>
    </dependency>
    <dependency>
        <groupId>io.zipkin.brave</groupId>
        <artifactId>brave-spancollector-http</artifactId>
        <version>3.9.0</version>
    </dependency>
    <dependency>
        <groupId>io.zipkin.brave</groupId>
        <artifactId>brave-web-servlet-filter</artifactId>
        <version>3.9.0</version>
    </dependency>
    <dependency>
        <groupId>io.zipkin.brave</groupId>
        <artifactId>brave-apache-http-interceptors</artifactId>
        <version>3.9.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpclient</artifactId>
    </dependency>
</dependencies>
```

2.核心类ZipkinBean提供需要使用的Bean

```java
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Brave.Builder;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.http.HttpSpanCollector.Config;
import com.github.kristofa.brave.httpclient.BraveHttpRequestInterceptor;
import com.github.kristofa.brave.httpclient.BraveHttpResponseInterceptor;
import com.github.kristofa.brave.servlet.BraveServletFilter;
 
@Configuration
public class ZipkinBean {
 
    /**
     * 配置收集器
     * 
     * @return
     */
    @Bean
    public SpanCollector spanCollector() {
        Config config = HttpSpanCollector.Config.builder().compressionEnabled(false).connectTimeout(5000)
                .flushInterval(1).readTimeout(6000).build();
        return HttpSpanCollector.create("http://192.168.237.128:9411", config, new EmptySpanCollectorMetricsHandler());
    }
 
    /**
     * Brave各工具类的封装
     * 
     * @param spanCollector
     * @return
     */
    @Bean
    public Brave brave(SpanCollector spanCollector) {
        Builder builder = new Builder("service1");// 指定serviceName
        builder.spanCollector(spanCollector);
        builder.traceSampler(Sampler.create(1));// 采集率
        return builder.build();
    }
 
    /**
     * 拦截器，需要serverRequestInterceptor,serverResponseInterceptor 分别完成sr和ss操作
     * 
     * @param brave
     * @return
     */
    @Bean
    public BraveServletFilter braveServletFilter(Brave brave) {
        return new BraveServletFilter(brave.serverRequestInterceptor(), brave.serverResponseInterceptor(),
                new DefaultSpanNameProvider());
    }
 
    /**
     * httpClient客户端，需要clientRequestInterceptor,clientResponseInterceptor分别完成cs和cr操作
     * 
     * @param brave
     * @return
     */
    @Bean
    public CloseableHttpClient httpClient(Brave brave) {
        CloseableHttpClient httpclient = HttpClients.custom()
                .addInterceptorFirst(new BraveHttpRequestInterceptor(brave.clientRequestInterceptor(),
                        new DefaultSpanNameProvider()))
                .addInterceptorFirst(new BraveHttpResponseInterceptor(brave.clientResponseInterceptor())).build();
        return httpclient;
    }
}
```

3.核心类ZipkinController对外接口

```java
@RestController
public class ZipkinController {
 
    @Autowired
    private CloseableHttpClient httpClient;
 
    @GetMapping("/service1")
    public String service() throws Exception {
        Thread.sleep(100);
        HttpGet get = new HttpGet("http://localhost:8082/service2");
        CloseableHttpResponse response = httpClient.execute(get);
        return EntityUtils.toString(response.getEntity(), "utf-8");
    }
}
```

分别启动四个服务，然后浏览器访问：[http://localhost:8081/service1](http://localhost:8081/service1)，正常调用结果返回：

```
service3 service4
```

可以观察zipkin web ui，查看服务的调用链：  
![](https://static.oschina.net/uploads/space/2017/1208/152025_yZd3_159239.jpg)

**总结**  
本文对zipkin有了一个初步了解，介绍了zipkin的启动以及整体架构，最后通过一个实例来大体了解zipkin是如何使用的，已经如何通过webui进行调用链的观察，后续就行更加深入的了解。