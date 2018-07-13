## **Spring-Cloud-Config简介**

Spring-Cloud-Config是Sping-Cloud下用于分布式配置管理的组件，分成了两个角色Config-Server和Config-Client；Config-Server端集中式存储/管理配置文件，并对外提供接口方便Config-Client访问，接口使用HTTP的方式对外提供访问；Config-Client通过接口获取配置文件，然后可以在应用中使用；Config-Server存储/管理的配置文件可以来自本地文件，远程Git仓库以及远程Svn仓库；

## **Config-Server端**

### 1.Config-Server依赖

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
    <version>2.0.0.RELEASE</version>
</dependency>
```

注：2.0以后的版本需要jdk1.8及以上版本

### 2.准备被管理的配置文件

Spring-Cloud-Config提供了对多种环境配置文件的支持，比如：开发环境，测试环境，生产环境等；为了更加全面的模拟，准备三个配置分别如下：

```
config-dev.properties
config-test.properties
config-pro.properties
```

分别是开发，测试以及生产的配置文件，内容也比较简单如下所示：

```
foo=hello dev/test/pro
```

### 3.准备启动配置文件

被管理的配置文件可以来自多个地方，包括：本地文件，远程Git仓库以及远程Svn仓库，下面分别在resources/application.properties中做配置;

#### 3.1本地文件

```
spring.application.name=config-server
server.port=8888
spring.profiles.active=native
spring.cloud.config.server.native.searchLocations=file:E:/github/spring-cloud-config-repo
```

指定了server端启动端口为8888，文件来自E:/github/spring-cloud-config-repo，以上三个文件放在此目录下

#### 3.2远程Git仓库

```
spring.application.name=config-server
server.port=8888
spring.profiles.active=git
spring.cloud.config.server.git.uri=https://github.com/ksfzhaohui/spring-cloud-config-repo
spring.cloud.config.server.git.default-label=master
```

spring.profiles.active默认值是git，git.uri指定地址，git仓库default-label默认值是master；

#### 3.3远程svn仓库

```
spring.profiles.active=subversion
spring.cloud.config.server.svn.uri=https://NJD9YZGJ2-PC.99bill.com:8443/svn/spring-cloud-config-repo
spring.cloud.config.server.svn.username=root
spring.cloud.config.server.svn.password=root
spring.cloud.config.server.svn.default-label=
```

配置了svn的用户名和密码，svn仓库default-label默认值是trunk，因为此处自建的svn服务器default-label为空，所以设置为空值即可；

### 4.准备启动类

```
@SpringBootApplication
@EnableConfigServer
public class ConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}
```

@EnableConfigServer启动配置服务器；

### 5.测试

不管使用以上的哪种方式配置，都可以通过使用http的方式访问，http可以有以下几种方式请求资源：

```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

application本实例中对应config；profile表示使用哪种环境的配置文件，这里可以是dev，test，pro；label可选的标签，git仓库默认值master，svn仓库默认值是trunk；

5.1请求[http://localhost](http://localhost/):8888/config/dev/master，结果如下：

```
{"name":"config","profiles":["dev"],"label":"master","version":"e9884489051c3b962840ac0a710f0f949a82d0ea","state":null,"propertySources":[{"name":"https://github.com/ksfzhaohui/spring-cloud-config-repo/config-dev.properties","source":{"foo":"hello dev"}}]}
```

返回结果包含了详细的信息，最后的source里面是配置文件内容；

5.2请求[http://localhost](http://localhost/):8888/config-dev.yml，结果如下：

```
foo: hello dev
```

此种方式访问仅显示配置文件内容，同样properties后缀的也仅显示配置文件内容，只是显示的格式不一样；

5.3更新git上文件内容，请求[http://localhost](http://localhost/):8888/config-dev.yml，结果如下：

```
foo: hello dev update
```

获取到了最新的内容，其实每次在请求的时候都会去远程仓库中更新一下数据，日志如下：

```
2018-07-13 09:43:07.606  INFO 14040 --- [nio-8888-exec-7] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@34107ea3: startup date [Fri Jul 13 09:43:07 CST 2018]; root of context hierarchy
2018-07-13 09:43:07.610  INFO 14040 --- [nio-8888-exec-7] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: file:/C:/Users/HUIZHA~1.CFS/AppData/Local/Temp/config-repo-1042810186024067185/config-dev.properties
2018-07-13 09:43:07.611  INFO 14040 --- [nio-8888-exec-7] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@34107ea3: startup date [Fri Jul 13 09:43:07 CST 2018]; root of context hierarchy
```

把数据更新到本地的Temp路径下；

## **Config-Client端**

### 1.Config-Client依赖

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
    <version>2.0.0.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>2.0.0.RELEASE</version>
</dependency>
```

### 2.启动配置文件

在配置文件resources/bootstrap.properties中做如下配置：

```
spring.application.name=config
spring.cloud.config.label=master
spring.cloud.config.profile=test
spring.cloud.config.uri= http://localhost:8888/
server.port=8889
```

spring.application.name：对应{application}，本实例中是config；  
spring.cloud.config.label：对应{label}，指定server端配置的分支，此处填master即可；  
spring.cloud.config.profile：对应{profile}，指定client当前的环境，可选值：dev，test，pro；  
spring.cloud.config.uri：server端地址；  
server.port：client启动端口；

### 3.准备测试类

```
@SpringBootApplication
public class ConfigClient {
    public static void main(String[] args) {
        SpringApplication.run(ConfigClient.class, args);
    }
}

@RestController
public class HelloController {

    @Value("${foo}")
    String foo;

    @RequestMapping(value = "/hello")
    public String hello() {
        return foo;
    }

}
```

访问地址：[http://localhost](http://localhost/):8889/hello，返回结果如下：

```
hello test
```

## **关于Spring-Cloud-Config配置的更新**

### 1.Client端初始化配置文件

Client端在启动的时候，可以发现Server端有拉取配置文件的日志：

```
2018-07-13 12:47:36.330  INFO 13884 --- [nio-8888-exec-1] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@1917a8ad: startup date [Fri Jul 13 12:47:36 CST 2018]; root of context hierarchy
2018-07-13 12:47:36.399  INFO 13884 --- [nio-8888-exec-1] o.s.c.c.s.e.NativeEnvironmentRepository  : Adding property source: file:/C:/Users/HUIZHA~1.CFS/AppData/Local/Temp/config-repo-1261377317774171312/config-test.properties
2018-07-13 12:47:36.400  INFO 13884 --- [nio-8888-exec-1] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@1917a8ad: startup date [Fri Jul 13 12:47:36 CST 2018]; root of context hierarchy
```

### 2.Server端数据更新，Client如何更新

更新git中config-test.properties，请求[http://localhost](http://localhost/):8888/config-test.yml，结果如下：

```
foo: hello test update
```

Client请求[http://localhost](http://localhost/):8889/hello，结果如下：

```
hello test
```

可以发现Server端已经更新，但是Client端没有获取到最新的数据，还是使用的缓存的老数据；  
Spring-Cloud-Config提供了多种刷新机制，下面看一下最简单手动刷新：

#### 2.1引入依赖

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
    <version>2.0.0.RELEASE</version>
</dependency>
```

#### 2.2暴露全部endpoints

在bootstrap.properties中添加

```
management.endpoints.web.exposure.include=*
```

#### 2.3.修改HelloController

```
@RefreshScope
@RestController
public class HelloController {

    @Value("${foo}")
    String foo;

    @RequestMapping(value = "/hello")
    public String hello() {
        return foo;
    }
}
```

@RefreshScope在手动执行刷新的时候会更新此变量

#### 2.4.启动

观察启动日志，其中有一条映射如下：

```
2018-07-13 15:54:16.959 INFO 11372 --- [ main] s.b.a.e.w.s.WebMvcEndpointHandlerMapping : Mapped "{[/actuator/refresh],methods=[POST],produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}" onto public java.lang.Objectorg.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)
```

/actuator/refresh提供了手动刷新的功能，并且必须使用POST方式；

#### 2.5.测试

访问地址：[http://localhost](http://localhost/):8889/hello，返回结果如下：

```
hello test
```

更新git上的配置文件，配置值为foo=hello test update；

访问地址：[http://localhost](http://localhost/):8889/hello，返回结果如下：

```
hello test
```

执行手动刷新操作：

```
C:\curl-7.61.0\I386>curl -X POST http://localhost:8889/actuator/refresh
["config.client.version","foo"]
```

访问地址：[http://localhost](http://localhost/):8889/hello，返回结果如下：

```
hello test update
```

### 3.如何自动更新

在生产环境下不可能每次都去手动触发refresh，github提供了webhook功能，当某个事件发生时，通过发送http的方式告诉接收方，这样就可以在接收到事件的时候触发refresh请求；

## **几个待分析问题**

### 1.多个Client节点如何更新

正常情况下Client会有很多个节点，而且节点会出现上线和下线，如何同时通知每个节点，Spring-Cloud-Config提供了Spring Cloud Bus来批量处理；

### 2.更新机制

在执行refresh的时候，只会把变动的参数发送给Client端，没有变动的不会发送，节约了流量；但是如果配置文件被多个不同的Client使用，是否会出现不相干的参数会发送给每个Client；

### 3.多配置文件的支持

Server可以同时加载多个配置文件，Client也可以支持多个配置文件；

### 4.Server端如何保证数据的可靠性

Server端集中管理配置，所以服务的可靠性很重要；

## **总结**

本文分别从Server端和Client端结合实例来介绍Spring-Cloud-Config如何使用，然后大体介绍了如何使用更新功能，最后留了几个待分析的问题，后续进行分析。
