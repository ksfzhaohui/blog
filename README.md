[![license](https://badgen.net/badge/license/MIT/blue)](https://github.com/ksfzhaohui/blog/blob/master/LICENSE)
[![stars](https://badgen.net/github/stars/ksfzhaohui/blog)](https://github.com/ksfzhaohui/blog/stargazers)
[![forks](https://badgen.net/github/forks/ksfzhaohui/blog)](https://github.com/ksfzhaohui/blog/network/members)
[![help-wanted](https://badgen.net/github/label-issues/ksfzhaohui/blog/help%20wanted/open)](https://github.com/ksfzhaohui/blog/labels/help%20wanted)
[![issues](https://badgen.net/github/open-issues/ksfzhaohui/blog)](https://github.com/ksfzhaohui/blog/issues)
[![PRs Welcome](https://badgen.net/badge/PRs/welcome/green)](http://makeapullrequest.com)

# BLOG
个人工作学习记录，主要围绕java基础，数据库，常用Java框架，中间件，遇到的问题以及技术总结等。

## 1.Java基础
Java相关基础包括JVM，线程，并发编程，网络编程，锁，内存模型等；

* [1.Java SPI机制分析](https://github.com/ksfzhaohui/blog/blob/master/SPI/Java%20SPI%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90.md "Java SPI机制分析.md")
* [2.谈谈Java任务的并行处理](https://github.com/ksfzhaohui/blog/blob/master/java8/%E8%B0%88%E8%B0%88Java%E4%BB%BB%E5%8A%A1%E7%9A%84%E5%B9%B6%E8%A1%8C%E5%A4%84%E7%90%86.md "谈谈Java任务的并行处理.md")
* [3.Java CAS操作的ABA问题](https://github.com/ksfzhaohui/blog/blob/master/Java%E5%9F%BA%E7%A1%80/Java%20CAS%E6%93%8D%E4%BD%9C%E7%9A%84ABA%E9%97%AE%E9%A2%98.md "Java CAS操作的ABA问题.md")
* [4.Java压缩算法性能比较.](https://github.com/ksfzhaohui/blog/blob/master/Java%E5%9F%BA%E7%A1%80/Java%E5%8E%8B%E7%BC%A9%E7%AE%97%E6%B3%95%E6%80%A7%E8%83%BD%E6%AF%94%E8%BE%83.md "Java压缩算法性能比较.md")
* [5.对协程的一些理解](https://github.com/ksfzhaohui/blog/blob/master/Java%E5%9F%BA%E7%A1%80/%E5%AF%B9%E5%8D%8F%E7%A8%8B%E7%9A%84%E4%B8%80%E4%BA%9B%E7%90%86%E8%A7%A3.md "对协程的一些理解.md")
* [6.Java调用Lua](https://github.com/ksfzhaohui/blog/blob/master/Java%E5%9F%BA%E7%A1%80/Java%E8%B0%83%E7%94%A8Lua.md "Java调用Lua.md")
* [7.关于零拷贝的一点认识](https://github.com/ksfzhaohui/blog/blob/master/java8/%E5%85%B3%E4%BA%8E%E9%9B%B6%E6%8B%B7%E8%B4%9D%E7%9A%84%E4%B8%80%E7%82%B9%E8%AE%A4%E8%AF%86.md "关于零拷贝的一点认识.md")

## 2.数据库
主要分析Mysql锁，索引，事务，以及binlog等相关技术点；
### 2.1 Mysql锁机制

* [1.Mysql锁机制分析](https://github.com/ksfzhaohui/blog/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/Mysql%E9%94%81%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90.md "Mysql锁机制分析.md")

### 2.2 Mysql Binlog介绍

* [1.MySql Binlog初识](https://github.com/ksfzhaohui/blog/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/MySql%20Binlog%E5%88%9D%E8%AF%86.md "MySql Binlog初识.md")
* [2.MySql Binlog事件介绍篇](https://github.com/ksfzhaohui/blog/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/MySql%20Binlog%E4%BA%8B%E4%BB%B6%E4%BB%8B%E7%BB%8D%E7%AF%87.md "MySql Binlog事件介绍篇.md")
* [3.MySql Binlog事件数据篇](https://github.com/ksfzhaohui/blog/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/MySql%20Binlog%E4%BA%8B%E4%BB%B6%E6%95%B0%E6%8D%AE%E7%AF%87.md "MySql Binlog事件数据篇.md")
* [4.Mysql通讯协议分析](https://github.com/ksfzhaohui/blog/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/Mysql%E9%80%9A%E8%AE%AF%E5%8D%8F%E8%AE%AE%E5%88%86%E6%9E%90.md "Mysql通讯协议分析.md")
* [5.基于Netty模拟解析Binlog](https://github.com/ksfzhaohui/blog/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%BA%E4%BA%8ENetty%E6%A8%A1%E6%8B%9F%E8%A7%A3%E6%9E%90Binlog.md "基于Netty模拟解析Binlog.md")

## 3.常用Java框架
主要介绍Java主流的一些框架：Mybatis，Spring，Spring Boot等，不限于源码和使用；
### 3.1 Mybatis系列

* [1.Mybatis之Mapper接口如何执行SQL](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8BMapper%E6%8E%A5%E5%8F%A3%E5%A6%82%E4%BD%95%E6%89%A7%E8%A1%8CSQL.md "Mybatis之Mapper接口如何执行SQL.md")  
* [2.Mybatis之方法如何映射到XML](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8B%E6%96%B9%E6%B3%95%E5%A6%82%E4%BD%95%E6%98%A0%E5%B0%84%E5%88%B0XML.md "Mybatis之方法如何映射到XML.md")  
* [3.Mybatis之XML如何映射到方法](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8BXML%E5%A6%82%E4%BD%95%E6%98%A0%E5%B0%84%E5%88%B0%E6%96%B9%E6%B3%95.md "Mybatis之XML如何映射到方法.md")  
* [4.Mybatis之对象工厂](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8B%E5%AF%B9%E8%B1%A1%E5%B7%A5%E5%8E%82.md "Mybatis之对象工厂.md")  
* [5.Mybatis之类型处理器](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8B%E7%B1%BB%E5%9E%8B%E5%A4%84%E7%90%86%E5%99%A8.md "Mybatis之类型处理器.md")  
* [6.Mybatis之结果处理器](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8B%E7%BB%93%E6%9E%9C%E5%A4%84%E7%90%86%E5%99%A8.md "Mybatis之结果处理器.md")  
* [7.Mybatis之缓存分析](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8B%E7%BC%93%E5%AD%98%E5%88%86%E6%9E%90.md "Mybatis之缓存分析.md")  
* [8.Mybatis之插件分析](https://github.com/ksfzhaohui/blog/blob/master/mybatis/Mybatis%E4%B9%8B%E6%8F%92%E4%BB%B6%E5%88%86%E6%9E%90.md "Mybatis之插件分析.md")  

### 3.2 SpringBoot系列

* [1.从SpringBoot整合Mybatis分析自动配置](https://github.com/ksfzhaohui/blog/blob/master/springboot/%E4%BB%8ESpringBoot%E6%95%B4%E5%90%88Mybatis%E5%88%86%E6%9E%90%E8%87%AA%E5%8A%A8%E9%85%8D%E7%BD%AE.md "从SpringBoot整合Mybatis分析自动配置.md")

### 3.3 SpringSession系列
* [1.Nginx+Tomcat关于Session的管理](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Nginx%2BTomcat%E5%85%B3%E4%BA%8ESession%E7%9A%84%E7%AE%A1%E7%90%86.md "Nginx+Tomcat关于Session的管理.md")
* [2.Tomcat Session管理分析](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Tomcat%20Session%E7%AE%A1%E7%90%86%E5%88%86%E6%9E%90.md "Tomcat Session管理分析.md")
* [3.Spring-Session基于Redis管理Session](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Spring-Session%E5%9F%BA%E4%BA%8ERedis%E7%AE%A1%E7%90%86Session.md "Spring-Session基于Redis管理Session.md")

## 4.分布式
围绕主流的一些中间件进行介绍：RPC，消息队列，分布式缓存，分布式链路追踪，分布式事务，分布式锁，分布式调度，分布式配置中心等；

### 4.1 分布式基础

* [1.从ACID到CAPBASE](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/%E4%BB%8EACID%E5%88%B0CAPBASE.md "从ACID到CAPBASE.md")
* [2.2PC3PC到底是啥](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/2PC3PC%E5%88%B0%E5%BA%95%E6%98%AF%E5%95%A5.md "2PC3PC到底是啥.md")
* [3.Paxos算法浅析](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Paxos%E7%AE%97%E6%B3%95%E6%B5%85%E6%9E%90.md "Paxos算法浅析.md")
* [4.ZAB协议和Paxos算法](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/ZAB%E5%8D%8F%E8%AE%AE%E5%92%8CPaxos%E7%AE%97%E6%B3%95.md "ZAB协议和Paxos算法.md")
* [5.Raft算法浅析](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Raft%E7%AE%97%E6%B3%95%E6%B5%85%E6%9E%90.md "Raft算法浅析.md")

### 4.2 分布式配置中心
主流的配置中心有很多如：Apollo，Nacos，Spring-Cloud-Config等

#### 4.2.1 Spring-Cloud-Config系列

* [1.Spring-Cloud-Config快速开始](https://github.com/ksfzhaohui/blog/blob/master/spring-cloud-config/1.Spring-Cloud-Config%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B.md "1.Spring-Cloud-Config快速开始.md")
* [2.Spring-Cloud-Config消息总线和高可用](https://github.com/ksfzhaohui/blog/blob/master/spring-cloud-config/2.Spring-Cloud-Config%E6%B6%88%E6%81%AF%E6%80%BB%E7%BA%BF%E5%92%8C%E9%AB%98%E5%8F%AF%E7%94%A8.md "2.Spring-Cloud-Config消息总线和高可用.md")

#### 4.2.2 实战配置中心

* [1.JMS实现参数的集中式管理](https://github.com/ksfzhaohui/blog/blob/master/dynamicConf/JMS%E5%AE%9E%E7%8E%B0%E5%8F%82%E6%95%B0%E7%9A%84%E9%9B%86%E4%B8%AD%E5%BC%8F%E7%AE%A1%E7%90%86.md "JMS实现参数的集中式管理.md")
* [2.Redis实现参数的集中式管理](https://github.com/ksfzhaohui/blog/blob/master/dynamicConf/Redis%E5%AE%9E%E7%8E%B0%E5%8F%82%E6%95%B0%E7%9A%84%E9%9B%86%E4%B8%AD%E5%BC%8F%E7%AE%A1%E7%90%86.md "Redis实现参数的集中式管理.md")
* [3.Zookeeper实现参数的集中式管理](https://github.com/ksfzhaohui/blog/blob/master/dynamicConf/Zookeeper%E5%AE%9E%E7%8E%B0%E5%8F%82%E6%95%B0%E7%9A%84%E9%9B%86%E4%B8%AD%E5%BC%8F%E7%AE%A1%E7%90%86.md "Zookeeper实现参数的集中式管理.md")

### 4.3 消息队列
主流的消息队列有：RocketMQ，Kafka，ActiveMQ，RabbitMQ等；
#### 4.3.1 消息队列基础

* [1.JMS消息确认和事务](https://github.com/ksfzhaohui/blog/blob/master/jms/1.JMS%E6%B6%88%E6%81%AF%E7%A1%AE%E8%AE%A4%E5%92%8C%E4%BA%8B%E5%8A%A1.md "1.JMS消息确认和事务.md")

#### 4.3.2 RocketMQ系列

* [1.RocketMQ入门篇](https://github.com/ksfzhaohui/blog/blob/master/rocketmq/RocketMQ%E5%85%A5%E9%97%A8%E7%AF%87.md "RocketMQ入门篇.md")
* [2.RocketMQ生产者流程篇](https://github.com/ksfzhaohui/blog/blob/master/rocketmq/RocketMQ%E7%94%9F%E4%BA%A7%E8%80%85%E6%B5%81%E7%A8%8B%E7%AF%87.md "RocketMQ生产者流程篇.md")
* [3.RocketMQ生产者消息篇](https://github.com/ksfzhaohui/blog/blob/master/rocketmq/RocketMQ%E7%94%9F%E4%BA%A7%E8%80%85%E6%B6%88%E6%81%AF%E7%AF%87.md "RocketMQ生产者消息篇.md")
* [4.从RocketMQ看长轮询](https://github.com/ksfzhaohui/blog/blob/master/rocketmq/%E4%BB%8ERocketMQ%E7%9C%8B%E9%95%BF%E8%BD%AE%E8%AF%A2.md "从RocketMQ看长轮询.md")

#### 4.3.3 Kafka系列

* [1.Kafka快速开始](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Kafka%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B.md "Kafka快速开始.md")

### 4.4 RPC框架
远程方法调用成熟产品很多常见的有：dubbo，gRpc，Thrift，还有spring cloud那一套；
#### 4.4.1 Dubbo系列

* [1.Dubbo分析之Serialize层](https://github.com/ksfzhaohui/blog/blob/master/dubbo/1.Dubbo%E5%88%86%E6%9E%90%E4%B9%8BSerialize%E5%B1%82.md "1.Dubbo分析之Serialize层.md")
* [2.Dubbo分析之Transport层](https://github.com/ksfzhaohui/blog/blob/master/dubbo/2.Dubbo%E5%88%86%E6%9E%90%E4%B9%8BTransport%E5%B1%82.md "2.Dubbo分析之Transport层.md")
* [3.Dubbo分析之Exchange层](https://github.com/ksfzhaohui/blog/blob/master/dubbo/3.Dubbo%E5%88%86%E6%9E%90%E4%B9%8BExchange%E5%B1%82.md "3.Dubbo分析之Exchange层.md")
* [4.Dubbo分析之Cluster层](https://github.com/ksfzhaohui/blog/blob/master/dubbo/Dubbo%E5%88%86%E6%9E%90%E4%B9%8BCluster%E5%B1%82.md "Dubbo分析之Cluster层.md")
* [5.Dubbo分析之Protocol层](https://github.com/ksfzhaohui/blog/blob/master/dubbo/Dubbo%E5%88%86%E6%9E%90%E4%B9%8BProtocol%E5%B1%82.md "Dubbo分析之Protocol层.md")
* [6.Dubbo分析之Registry层](https://github.com/ksfzhaohui/blog/blob/master/dubbo/Dubbo%E5%88%86%E6%9E%90%E4%B9%8BRegistry%E5%B1%82.md "Dubbo分析之Registry层.md")

### 4.5 分布式数据库
数据库访问层常见：mycat，sharding-JDBC，Atlas等
#### 4.5.1 MyCat系列

* [1.Demo入门Mycat](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Demo%E5%85%A5%E9%97%A8Mycat.md "Demo入门Mycat.md")
* [2.Demo之Mycat读写分离](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Demo%E4%B9%8BMycat%E8%AF%BB%E5%86%99%E5%88%86%E7%A6%BB.md "Demo之Mycat读写分离.md")

### 4.6 分布式调度
最常用的Quartz，但是quartz在分布式调度的时候并不完美，所有出现了一些对其扩展的产品如：XXL-Job,Elastic-Job等；
#### 4.6.1 Quartz分析

* [1.Spring整合Quartz分布式调度](https://github.com/ksfzhaohui/blog/blob/master/quartz/1.Spring%E6%95%B4%E5%90%88Quartz%E5%88%86%E5%B8%83%E5%BC%8F%E8%B0%83%E5%BA%A6.md "1.Spring整合Quartz分布式调度.md")
* [2.Quartz数据库表分析](https://github.com/ksfzhaohui/blog/blob/master/quartz/2.Quartz%E6%95%B0%E6%8D%AE%E5%BA%93%E8%A1%A8%E5%88%86%E6%9E%90.md "2.Quartz数据库表分析.md")
* [3.Quartz调度源码分析](https://github.com/ksfzhaohui/blog/blob/master/quartz/3.Quartz%E8%B0%83%E5%BA%A6%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90.md "3.Quartz调度源码分析.md")
* [4.基于Netty+Zookeeper+Quartz调度分析](https://github.com/ksfzhaohui/blog/blob/master/quartz/4.%E5%9F%BA%E4%BA%8ENetty%2BZookeeper%2BQuartz%E8%B0%83%E5%BA%A6%E5%88%86%E6%9E%90.md "4.基于Netty+Zookeeper+Quartz调度分析.md")

### 4.7 搜索引擎
开源的有ElasticSearch，商业的有Splunk等；

#### 4.7.1 ElasticSearch系列

* [1.基于ELK5.1-ElasticSearch, Logstash, Kibana的一次整合测试](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/%E5%9F%BA%E4%BA%8EELK5.1-ElasticSearch%2C%20Logstash%2C%20Kibana%E7%9A%84%E4%B8%80%E6%AC%A1%E6%95%B4%E5%90%88%E6%B5%8B%E8%AF%95.md "基于ELK5.1-ElasticSearch, Logstash, Kibana的一次整合测试.md")

### 4.8 分布式链路追踪
开源的产品有：zipkin，cat等
#### 4.8.1 Zipkin系列

* [1.Zipkin快速开始](https://github.com/ksfzhaohui/blog/blob/master/%E5%88%86%E5%B8%83%E5%BC%8F/Zipkin%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B.md "Zipkin快速开始.md")

### 4.9 分布式缓存
缓存服务器常用的就是redis，memcached等；

### 4.10 注册中心
常用的有Zookeeper，eruka等；

## 5.遇到的问题
整理工作中遇到的一些典型问题，进行分析汇总；

* [1.Poi读取Excel引发的内存溢出](https://github.com/ksfzhaohui/blog/blob/master/casestudy/Poi%E8%AF%BB%E5%8F%96Excel%E5%BC%95%E5%8F%91%E7%9A%84%E5%86%85%E5%AD%98%E6%BA%A2%E5%87%BA.md "Poi读取Excel引发的内存溢出.md")
* [2.一次排查多线程引发Java DNS缓存的Bug](https://github.com/ksfzhaohui/blog/blob/master/casestudy/%E4%B8%80%E6%AC%A1%E6%8E%92%E6%9F%A5%E5%A4%9A%E7%BA%BF%E7%A8%8B%E5%BC%95%E5%8F%91Java%20DNS%E7%BC%93%E5%AD%98%E7%9A%84Bug.md "一次排查多线程引发Java DNS缓存的Bug.md")
* [3.记一次升级Oracle驱动引发的死锁](https://github.com/ksfzhaohui/blog/blob/master/casestudy/%E8%AE%B0%E4%B8%80%E6%AC%A1%E5%8D%87%E7%BA%A7Oracle%E9%A9%B1%E5%8A%A8%E5%BC%95%E5%8F%91%E7%9A%84%E6%AD%BB%E9%94%81.md "记一次升级Oracle驱动引发的死锁.md")
* [4.关于Jackson默认丢失Bigdecimal精度问题分析](https://github.com/ksfzhaohui/blog/blob/master/serialization/1.%E5%85%B3%E4%BA%8EJackson%E9%BB%98%E8%AE%A4%E4%B8%A2%E5%A4%B1Bigdecimal%E7%B2%BE%E5%BA%A6%E9%97%AE%E9%A2%98%E5%88%86%E6%9E%90.md "1.关于Jackson默认丢失Bigdecimal精度问题分析.md")

## 6.技术总结

* [1.如何设计一个安全的对外接口](https://github.com/ksfzhaohui/blog/blob/master/java8/%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E5%AE%89%E5%85%A8%E7%9A%84%E5%AF%B9%E5%A4%96%E6%8E%A5%E5%8F%A3.md "如何设计一个安全的对外接口.md")
* [2.如何设计一个本地缓存](https://github.com/ksfzhaohui/blog/blob/master/java8/%E5%A6%82%E4%BD%95%E8%AE%BE%E8%AE%A1%E4%B8%80%E4%B8%AA%E6%9C%AC%E5%9C%B0%E7%BC%93%E5%AD%98.md "如何设计一个本地缓存.md")
* [3.如何快速安全的插入千万条数据](https://github.com/ksfzhaohui/blog/blob/master/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%A6%82%E4%BD%95%E5%BF%AB%E9%80%9F%E5%AE%89%E5%85%A8%E7%9A%84%E6%8F%92%E5%85%A5%E5%8D%83%E4%B8%87%E6%9D%A1%E6%95%B0%E6%8D%AE.md "如何快速安全的插入千万条数据.md")

## 7.计算机网络

* [1.HTTPS分析与实战](https://github.com/ksfzhaohui/blog/blob/master/%E8%AE%A1%E7%AE%97%E6%9C%BA%E5%9F%BA%E7%A1%80/HTTPS%E5%88%86%E6%9E%90%E4%B8%8E%E5%AE%9E%E6%88%98.md "HTTPS分析与实战.md")

## 8.常见面试题

* [1.为什么Netty的FastThreadLocal速度快](https://github.com/ksfzhaohui/blog/blob/master/interview/%E4%B8%BA%E4%BB%80%E4%B9%88Netty%E7%9A%84FastThreadLocal%E9%80%9F%E5%BA%A6%E5%BF%AB.md "为什么Netty的FastThreadLocal速度快.md")

## 9.联系我

* [开源中国](https://my.oschina.net/OutOfMemory)  
* [掘金](https://juejin.im/user/5c806d57e51d4526e619cacb)  
* [segmentfault](https://segmentfault.com/u/ksfzhaohui)  
