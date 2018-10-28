## **系列文章**

[Spring整合Quartz分布式调度](https://my.oschina.net/OutOfMemory/blog/1790200)

[Quartz数据库表分析](https://my.oschina.net/OutOfMemory/blog/1799185)

[Quartz调度源码分析](https://my.oschina.net/OutOfMemory/blog/1800560)

**前言**  
为了保证应用的高可用和高并发性，一般都会部署多个节点；对于定时任务，如果每个节点都执行自己的定时任务，一方面耗费了系统资源，另一方面有些任务多次执行，可能引发应用逻辑问题，所以需要一个分布式的调度系统，来协调每个节点执行定时任务。

**Spring整合Quartz**  
Quartz是一个成熟的任务调度系统，Spring对Quartz做了兼容，方便开发，下面看看具体如何整合：  
1.Maven依赖文件

```
<dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>4.3.5.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.quartz-scheduler</groupId>
            <artifactId>quartz</artifactId>
            <version>2.2.3</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.29</version>
        </dependency>
    </dependencies>
```

主要就是Spring相关库、quartz库以及mysql驱动库，注：分布式调度需要用到数据库，这里选用mysql；

2.配置job  
提供了两种方式来配置job，分别是：MethodInvokingJobDetailFactoryBean和JobDetailFactoryBean  
2.1MethodInvokingJobDetailFactoryBean  
要调用特定bean的一个方法的时候使用，具体配置如下：

```xml
<bean id="firstTask" class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean">  
    <property name="targetObject" ref="firstService" />  
    <property name="targetMethod" value="service" />  
</bea>
```

2.2JobDetailFactoryBean  
这种方式更加灵活，可以设置传递参数，具体如下：

```xml
<bean id="firstTask"
        class="org.springframework.scheduling.quartz.JobDetailFactoryBean">
        <property name="jobClass" value="zh.maven.SQuartz.task.FirstTask" />
        <property name="jobDataMap">
            <map>
                <entry key="firstService" value-ref="firstService" />
            </map>
        </property>
</bean>
```

jobClass定义的任务类，继承QuartzJobBean，实现executeInternal方法；jobDataMap用来给job传递数据;

3.配置调度使用的触发器  
同样提供了两种触发器类型：SimpleTriggerFactoryBean和CronTriggerFactoryBean  
重点看CronTriggerFactoryBean，这种类型更加灵活，具体如下：

```xml
<bean id="firstCronTrigger"
    class="org.springframework.scheduling.quartz.CronTriggerFactoryBean">
    <property name="jobDetail" ref="firstTask" />
    <property name="cronExpression" value="0/5 * * ? * *" />
</bean>
```

jobDetail指定的就是在步骤2中配置的job，cronExpression配置了每5秒执行一次job；

4.配置Quartz调度器的SchedulerFactoryBean  
同样提供了两种方式：内存RAMJobStore和数据库方式  
4.1内存RAMJobStore  
job的相关信息存储在内存里，每个节点存储各自的，互相隔离，配置如下：

```xml
<bean class="org.springframework.scheduling.quartz.SchedulerFactoryBean">
    <property name="triggers">
        <list>
            <ref bean="firstCronTrigger" />
        </list>
    </property>
</bean>
```

4.2数据库方式  
job的相关信息存储在数据库中，所有节点共用数据库，每个节点通过数据库来通信，保证一个job同一时间只会在一个节点上执行，并且  
如果某个节点挂掉，job会被分配到其他节点执行，具体配置如下：

```xml
<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource"
        destroy-method="close">
        <property name="driverClass" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/quartz" />
        <property name="user" value="root" />
        <property name="password" value="root" />
    </bean>
    <bean class="org.springframework.scheduling.quartz.SchedulerFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="configLocation" value="classpath:quartz.properties" />
        <property name="triggers">
            <list>
                <ref bean="firstCronTrigger" />
            </list>
        </property>
    </bean>
```

dataSource用来配置数据源，数据表相关信息，可以到官网下载gz包，sql文件在路径：docs\\dbTables下，里面提供了主流数据库的sql文件，总共11张表；  
configLocation配置的quartz.properties文件在quartz.jar的org.quartz包下，里面提供了一些默认的数据，比如org.quartz.jobStore.class

```
org.quartz.jobStore.class: org.quartz.simpl.RAMJobStore
```

这里需要将quartz.properties拷贝出来做一些修改，具体修改如下：

```
org.quartz.scheduler.instanceId: AUTO
org.quartz.jobStore.class: org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.isClustered: true
org.quartz.jobStore.clusterCheckinInterval: 1000
```

5.相关类

```java
public class FirstTask extends QuartzJobBean {

    private FirstService firstService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        firstService.service();
    }

    public void setFirstService(FirstService firstService) {
        this.firstService = firstService;
    }
}
```

FirstTask继承QuartzJobBean，实现executeInternal方法，调用FirstService;

```java
public class FirstService implements Serializable {

    private static final long serialVersionUID = 1L;

    public void service() {
        System.out.println(new SimpleDateFormat("YYYYMMdd HH:mm:ss").format(new Date()) + "---start FirstService");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(new SimpleDateFormat("YYYYMMdd HH:mm:ss").format(new Date()) + "---end FirstService");
    }
}
```

FirstService需要提供序列化接口，因为需要保存在数据库中；

```java
public class App {
    public static void main(String[] args) {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("quartz.xml");
    }
}
```

主类用来加载quartz配置文件；

**测试分布式调度**  
1.同时启动App两次，观察日志：

```
20180405 14:48:10---start FirstService
20180405 14:48:12---end FirstService
20180405 14:48:15---start FirstService
20180405 14:48:17---end FirstService
```

其中A1有日志输出，A2没有；当停掉A1以后，A2有日志输出；

2.添加新的job分别新建：SecondTask和SecondService，同时添加相关配置文件，启动App观察日志：  
A1日志如下：

```
20180405 15:03:15---start FirstService
20180405 15:03:15---start SecondService
20180405 15:03:17---end FirstService
20180405 15:03:17---end SecondService
20180405 15:03:20---start FirstService
20180405 15:03:22---end FirstService
20180405 15:03:25---start FirstService
20180405 15:03:27---end FirstService
```

A2日志如下：

```
20180405 15:03:20---start SecondService
20180405 15:03:22---end SecondService
20180405 15:03:25---start SecondService
20180405 15:03:27---end SecondService
```

可以发现A1和A2都有执行任务，但是同一任务同一时间只会在一个节点执行，并且只有在执行结束后才有可能分配到其他节点；

3.如果间隔时间小于任务执行时间，比如这里改成sleep(6000)  
A1日志如下：

```
20180405 15:14:40---start FirstService
20180405 15:14:45---start FirstService
20180405 15:14:46---end FirstService
20180405 15:14:50---start FirstService
20180405 15:14:50---start SecondService
20180405 15:14:51---end FirstService
```

A2日志如下：

```
20180405 15:14:40---start SecondService
20180405 15:14:45---start SecondService
20180405 15:14:46---end SecondService
20180405 15:14:51---end SecondService
```

间隔时间是5秒，而任务执行需要6秒，观察日志可以发现，任务还没有结束，新的任务已经开始，这种情况可能引发应用的逻辑问题，其实就是任务能不能支持串行的问题；

4.@DisallowConcurrentExecution注解保证任务的串行  
在FirstTask和SecondTask上分别添加@DisallowConcurrentExecution注解，日志结果如下：  
A1日志如下：

```
20180405 15:32:45---start FirstService
20180405 15:32:51---end FirstService
20180405 15:32:51---start FirstService
20180405 15:32:51---start SecondService
20180405 15:32:57---end FirstService
20180405 15:32:57---end SecondService
20180405 15:32:57---start FirstService
20180405 15:32:57---start SecondService
```

A2日志如下：

```
20180405 15:32:45---start SecondService
20180405 15:32:51---end SecondService
```

观察日志可以发现，任务只有在end以后，才会开始新的任务，实现了任务的串行化；

**总结**  
本文旨在对Spring+Quartz分布式调度有一个直观的了解，通过实际的使用来解决问题，当然可能还有很多疑问比如它是如何调度的，数据库如果挂了会怎么样等等，还需要做更加深入的了解。