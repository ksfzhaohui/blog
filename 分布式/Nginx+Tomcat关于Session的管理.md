## **系列文章**

[Nginx+Tomcat关于Session的管理](https://my.oschina.net/OutOfMemory/blog/1821751)

[Tomcat Session管理分析](https://my.oschina.net/OutOfMemory/blog/1825123)

[Spring-Session基于Redis管理Session](https://my.oschina.net/OutOfMemory/blog/1837937)

## **前言**

Nginx+Tomcat对Session的管理一直有了解，但是一直没有实际操作一遍，本文从最简单的安装启动开始，通过实例的方式循序渐进的介绍了几种管理session的方式。

## **nginx安装配置**

### 1.安装nginx

```
[root@localhost ~]# yum install nginx
```

提示报如下错误：

```
No package nginx available.
```

解决办法安装epel：EPEL是企业版 Linux 附加软件包的简称，EPEL是一个由Fedora特别兴趣小组创建、维护并管理的，针对 红帽企业版 Linux(RHEL)及其衍生发行版(比如 CentOS、Scientific Linux、Oracle Enterprise Linux)的一个高质量附加软件包项目；

```
[root@localhost ~]# yum install epel-release
```

安装完之后，即可成功安装nginx；

### 2.启动、停止nginx

先进入nginx的目录

```
[root@localhost nginx]# cd /usr/sbin/
```

执行命令

```
./nginx 开启
./nginx -s stop  使用kill命令强制杀掉进程
./nginx -s quit  待nginx进程处理任务完毕进行停止
./nginx -s reload
```

## **nginx+tomcat负载均衡**

### 1.准备2个tomcat，分别指定端口为8081，8082

```
drwxr-xr-x. 9 root root      4096 May  7 14:16 apache-tomcat-7.0.88_8081
drwxr-xr-x. 9 root root      4096 May  7 14:16 apache-tomcat-7.0.88_8082
```

修改webapps/ROOT的index.jsp，方便测试

```
<%
if(request.getSession().getAttribute("key")==null){
   out.println("key is null,ready init.....");   
   request.getSession().setAttribute("key","value");
}else{
   out.println("key is not null,key="+request.getSession().getAttribute("key"));  
}
%>
<br> 
sessionID:<%=session.getId()%>   
<br>   
sessionCreateTime:<%= session.getCreationTime() %>
<br>
<% 
out.println("tomcat port 8081");   
%> 
```

最后的输出在两个tomcat下面指定各自的端口号8081和8082

### 2.nginx配置负载均衡(默认策略)

修改/etc/nginx/下面的nginx.conf

```
upstream tomcatTest {
     server 127.0.0.1:8081;   #tomcat-8081
     server 127.0.0.1:8082;   #tomcat-8082
}
 
server {
    listen       80 default_server;
    listen       [::]:80 default_server;
    server_name  _;
    root         /usr/share/nginx/html;
 
    # Load configuration files for the default server block.
    include /etc/nginx/default.d/*.conf;
 
    location / {
        proxy_pass http://tomcatTest;
    }
 
    error_page 404 /404.html;
        location = /40x.html {
    }
 
    error_page 500 502 503 504 /50x.html;
        location = /50x.html {
    }
}
```

此处配置的负载均衡策略是默认的轮询策略，nginx还支持其他策略包括：ip\_hash、weight、fair(第三方)、url\_hash(第三方)；  
默认策略每个web请求按时间顺序逐一分配到不同的后端服务器，这种情况下每次请求都会创建一个新的session，下面做简单测试:  
第一次请求[http://ip/](http://ip/)

```
key is null,ready init..... 
sessionID:E7A9782DED29FF04E21DF94078CB4F62 
sessionCreateTime:1527732911441
tomcat port 8082
```

第二次刷新[http://ip/](http://ip/)

```
key is null,ready init..... 
sessionID:7812E8E21DBB74CC7FBB75A0DFF2E9CB 
sessionCreateTime:1527732979810
tomcat port 8081
```

第三次刷新[http://ip/](http://ip/)

```
key is null,ready init..... 
sessionID:8895F41E299785A21995D5F8BB734B86 
sessionCreateTime:1527733011878
tomcat port 8082
```

可以发现每次都产生一个新的session，而且消息按时间顺序逐一分配到不同的后端服务器，一般需要保持session会话的网站都不允许出现每次请求都产生一个session；

### 3.nginx配置负载均衡(黏性Session)

每个请求按访问ip的hash结果分配，这样每个访客固定访问一个后端服务器，可以解决session的问题；nginx可以通过在upstream模块配置ip_hash来实现黏性Session；

```
upstream tomcatTest {
     ip_hash;
     server 127.0.0.1:8081;   #tomcat-8081
     server 127.0.0.1:8082;   #tomcat-8082
}
```

下面做简单测试:  
第一次请求[http://ip/](http://ip/)

```
key is null,ready init..... 
sessionID:859BADFB09A4ECEAEC5257F518C228A0 
sessionCreateTime:1527734181450
tomcat port 8081
```

第二次刷新[http://ip/](http://ip/)

```
key is not null,key=value 
sessionID:859BADFB09A4ECEAEC5257F518C228A0 
sessionCreateTime:1527734181450
tomcat port 8081
```

第三次刷新[http://ip/](http://ip/)

```
key is not null,key=value 
sessionID:859BADFB09A4ECEAEC5257F518C228A0 
sessionCreateTime:1527734181450
tomcat port 8081
```

可以发现第一次请求设置了key=value,后面每次都能获取到key值，sessionId没有改变，tomcat也没有改变，实现了黏性Session；  
此时可以把port=8081的tomcat停掉，然后再观察  
第四次刷新[http://ip/](http://ip/)

```
key is null,ready init..... 
sessionID:3C15FE2C8E8A9DCDC6EAD48180B78B80 
sessionCreateTime:1527735994476
tomcat port 8082
```

第五次刷新[http://ip/](http://ip/)

```
key is not null,key=value 
sessionID:3C15FE2C8E8A9DCDC6EAD48180B78B80 
sessionCreateTime:1527735994476
tomcat port 8082
```

可以发现消息转发到了tomcat-8082，并且session丢失，重新创建了新的session；  
如何让这种情况session不丢失，也有两种方案：Session复制和Session共享；Session共享从扩展性，性能方面都更加好，下面重点介绍一下Session共享如何实现；

## **nginx+tomcat实现Session共享**

Session共享思想就是把session保存到一个公共的地方，用的时候再从里面取出来，具体这个公共的地方可以是：redis，db，memcached等，下面已redis为实例

### 1.redis安装配置

```
yum install redis
```

安装完成以后配置文件/etc/redis.conf  
启动redis服务端

```
redis-server /etc/redis.conf
```

启动客户端

```
redis-cli
```

### 2.Tomcat引入依赖的jar

$TOMCAT_HOME/lib添加如下jar包

```
<dependency>
    <groupId>com.bluejeans</groupId>
    <artifactId>tomcat-redis-session-manager</artifactId>
    <version>2.0.0</version>
</dependency>
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.5.2</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.2</version>
</dependency>
```

### 3.Tomcat修改配置

修改$TOMCAT_HOME/conf目录下的context.xml文件

```
<Valve className="com.orangefunction.tomcat.redissessions.RedisSessionHandlerValve" />
<Manager className="com.orangefunction.tomcat.redissessions.RedisSessionManager"
         host="localhost"
         port="6379"
         database="0"
         maxInactiveInterval="60"/>
```

Tomcat提供了一个开放的session管理和持久化的org.apache.catalina.session.ManagerBase，继承这个抽象类并做一些简单的配置，即可让你的session管理类接管Tomcat的session读取和持久化，这里使用的是[tomcat-redis-session-manager](https://github.com/jcoleman/tomcat-redis-session-manager)来管理session；  
RedisSessionManager继承于org.apache.catalina.session.ManagerBase类，对session的相关操作都在此类中；

### 4.测试

第一次请求[http://ip/](http://ip/)

```
key is null,ready init..... 
sessionID:1131499E5A65DE1591152465E7B24B1F 
sessionCreateTime:1527740273682
tomcat port 8081
```

第二次刷新[http://ip/](http://ip/)

```
key is not null,key=value 
sessionID:1131499E5A65DE1591152465E7B24B1F 
sessionCreateTime:1527740273682
tomcat port 8081
```

将tomcat-8081停掉， 第三次刷新[http://ip/](http://ip/)

```
key is not null,key=value 
sessionID:1131499E5A65DE1591152465E7B24B1F 
sessionCreateTime:1527740273682
tomcat port 8082
```

可以发现此时消息已经转发到tomcat-8082节点了，但是session没有改变，同时key也可以获取到值；

5.查看redis

```
[root@localhost ~]# redis-cli
127.0.0.1:6379> keys *
1) "1131499E5A65DE1591152465E7B24B1F"
127.0.0.1:6379> get 1131499E5A65DE1591152465E7B24B1F
"\xac\xed\x00\x05sr\x00Dcom.orangefunction.tomcat.redissessions.SessionSerializationMetadataB\xd9\xd9\xf7v\xa2\xdbL\x03\x00\x01[\x00\x15sessionAttributesHasht\x00\x02[Bxpw\x14\x00\x00\x00\x10}\xc8\xc9\xcf\xf6\xc3\xb5Y\xc7\x0c\x8eF\xa5\xfaQ\xe8xsr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x01c\xb4j\x94\x12sq\x00~\x00\x03\x00\x00\x01c\xb4j\x94\x12sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexq\x00~\x00\x04\x00\x00\a\bsr\x00\x11java.lang.Boolean\xcd r\x80\xd5\x9c\xfa\xee\x02\x00\x01Z\x00\x05valuexp\x01q\x00~\x00\nsq\x00~\x00\x03\x00\x00\x01c\xb4j\x94*t\x00 1131499E5A65DE1591152465E7B24B1Fsq\x00~\x00\a\x00\x00\x00\x01t\x00\x03keyt\x00\x05valuew\b\x00\x00\x01c\xb4j\x94\x12"
```

可以发现redis里面已经存放了session对象，并且使用sessionId作为key值，存放了session的二进制数据；

## **总结**

本文简单介绍了Nginx整合Tomcat，以及Nginx的负载均衡策略，用实例的方式展示了默认策略和ip_hash策略对session的管理；最后介绍了使用session共享的方式来解决前两种方式对session管理的弊端；后续继续了解Tomcat是如何将session读取和持久化交给其他系统管理的，session更新是否实时，序列化方案，有效期等问题。