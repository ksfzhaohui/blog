## **系列文章**

[Spring整合Quartz分布式调度](https://my.oschina.net/OutOfMemory/blog/1790200)

[Quartz数据库表分析](https://my.oschina.net/OutOfMemory/blog/1799185)

[Quartz调度源码分析](https://my.oschina.net/OutOfMemory/blog/1800560)

**前言**  
上一篇文章[Spring整合Quartz分布式调度](https://my.oschina.net/OutOfMemory/blog/1790200)介绍了Quartz通过数据库的方式来实现分布式调度，通过使用数据库来存储trigger，job等信息，可以在停服重启的时候重新加载上次trigger的状态，保证了完整性；另一方面通过数据库来实现锁机制来实现分布式调度；Quartz默认提供了11张表，本文将对这几张表做简要的分析。

**表信息**

```sql
1.qrtz_blob_triggers
2.qrtz_cron_triggers
3.qrtz_simple_triggers
4.qrtz_simprop_triggers
5.qrtz_fired_triggers
6.qrtz_triggers
7.qrtz_job_details
8.qrtz_calendars
9.qrtz_paused_trigger_grps
10.qrtz_scheduler_state
11.qrtz_locks
```

共11张表，前6张都是关于各种triggers的信息，后面包括job，悲观锁，调度状态等信息；相关表操作在类StdJDBCDelegate中，相关sql语句在StdJDBCConstants中；

1.qrtz\_blob\_triggers  
自定义的triggers使用blog类型进行存储，非自定义的triggers不会存放在此表中，Quartz提供的triggers包括：CronTrigger，CalendarIntervalTrigger，  
DailyTimeIntervalTrigger以及SimpleTrigger，这几个trigger信息会保存在后面的几张表中；

2.qrtz\_cron\_triggers  
存储CronTrigger，这也是我们使用最多的触发器，在配置文件中做如下配置，即可在qrtz\_cron\_triggers生成记录：

```xml
<bean id="firstCronTrigger"
    class="org.springframework.scheduling.quartz.CronTriggerFactoryBean">
    <property name="jobDetail" ref="firstTask" />
    <property name="cronExpression" value="0/6 * * ? * *" />
    <property name="group" value="firstCronGroup"></property>
</bean>
<bean id="firstTask"
    class="org.springframework.scheduling.quartz.JobDetailFactoryBean">
    <property name="jobClass" value="zh.maven.SQuartz.task.FirstTask" />
    <property name="jobDataMap">
        <map>
            <entry key="firstService" value-ref="firstService" />
        </map>
    </property>
</bean>
<bean id="firstService" class="zh.maven.SQuartz.service.FirstService"></bean>
```

表达式指定了每隔6秒执行一次，然后指定了要执行的task，task指定了要执行的业务，运行之后可以查看数据表：

```
mysql> select * from qrtz_cron_triggers;
+-------------+------------------+----------------+-----------------+---------------+
| SCHED_NAME  | TRIGGER_NAME     | TRIGGER_GROUP  | CRON_EXPRESSION | TIME_ZONE_ID  |
+-------------+------------------+----------------+-----------------+---------------+
| myScheduler | firstCronTrigger | firstCronGroup | 0/6 * * ? * *   | Asia/Shanghai |
+-------------+------------------+----------------+-----------------+---------------+

```

myScheduler是在定义SchedulerFactoryBean时指定的名称，其他字段都可以在上面的配置中找到；

3.qrtz\_simple\_triggers  
存储SimpleTrigger，在配置文件中做如下配置，即可在qrtz\_simple\_triggers生成记录：

```xml
<bean id="firstSimpleTrigger"
    class="org.springframework.scheduling.quartz.SimpleTriggerFactoryBean">
    <property name="jobDetail" ref="firstSimpleTask" />
    <property name="startDelay" value="1000" />
    <property name="repeatInterval" value="2000" />
    <property name="repeatCount" value="5"></property>
    <property name="group" value="firstSimpleGroup"></property>
</bean>
<bean id="firstSimpleTask"
    class="org.springframework.scheduling.quartz.JobDetailFactoryBean">
    <property name="jobClass" value="zh.maven.SQuartz.task.SimpleFirstTask" />
    <property name="jobDataMap">
        <map>
            <entry key="firstService" value-ref="simpleFirstService" />
        </map>
    </property>
</bean>
<bean id="simpleFirstService" class="zh.maven.SQuartz.service.SimpleFirstService"></bean>

```

指定了开始延迟时间，重复间隔时间已经重复的次数限制，查看表如下：

```sql
mysql> select * from qrtz_simple_triggers;
+-------------+--------------------+------------------+--------------+-----------------+-----------------+
| SCHED_NAME  | TRIGGER_NAME       | TRIGGER_GROUP    | REPEAT_COUNT | REPEAT_INTERVAL | TIMES_TRIGGERED |
+-------------+--------------------+------------------+--------------+-----------------+-----------------+
| myScheduler | firstSimpleTrigger | firstSimpleGroup |            5 |            2000 |               1 |
+-------------+--------------------+------------------+--------------+-----------------+-----------------+

```

TIMES\_TRIGGERED用来记录执行了多少次了，此值被定义在SimpleTriggerImpl中，每次执行+1，这里定义的REPEAT\_COUNT=5，实际情况会执行6次，具体可以查看SimpleTriggerImpl源码：

```java
public Date getFireTimeAfter(Date afterTime) {
        if (complete) {
            return null;
        }
 
        if ((timesTriggered > repeatCount)
                && (repeatCount != REPEAT_INDEFINITELY)) {
            return null;
        }
        ......
}
```

timesTriggered默认值为0，当timesTriggered > repeatCount停止trigger，所以会执行6次，当执行完毕之后此记录会被删除；

4.qrtz\_simprop\_triggers  
存储CalendarIntervalTrigger和DailyTimeIntervalTrigger两种类型的触发器，使用CalendarIntervalTrigger做如下配置：

```xml
<bean id="firstCalendarTrigger" class="org.quartz.impl.triggers.CalendarIntervalTriggerImpl">
    <property name="jobDataMap">
        <map>
            <entry key="jobDetail" value-ref="firstCalendarTask"></entry>
        </map>
    </property>
    <property name="key" ref="calendarTriggerKey"></property>
    <property name="repeatInterval" value="1" />
    <property name="group" value="firstCalendarGroup"></property>
</bean>
<bean id="firstCalendarTask"
    class="org.springframework.scheduling.quartz.JobDetailFactoryBean">
    <property name="jobClass" value="zh.maven.SQuartz.task.CalendarFirstTask" />
    <property name="jobDataMap">
        <map>
            <entry key="firstService" value-ref="calendarFirstService" />
        </map>
    </property>
</bean>
<bean id="calendarFirstService" class="zh.maven.SQuartz.service.CalendarFirstService"></bean>

```

CalendarIntervalTrigger没有对应的FactoryBean，直接设置实现类CalendarIntervalTriggerImpl；指定的重复周期是1，默认单位是天，也就是每天执行一次，查看表如下：

```
mysql> select * from qrtz_simprop_triggers;
+-------------+--------------------+--------------------+------------+---------------+------------+------------+------------+-------------+-------------+------------+------------+-------------+-------------+
| SCHED_NAME  | TRIGGER_NAME       | TRIGGER_GROUP      | STR_PROP_1 | STR_PROP_2    | STR_PROP_3 | INT_PROP_1 | INT_PROP_2 | LONG_PROP_1 | LONG_PROP_2 | DEC_PROP_1 | DEC_PROP_2 | BOOL_PROP_1 | BOOL_PROP_2 |
+-------------+--------------------+--------------------+------------+---------------+------------+------------+------------+-------------+-------------+------------+------------+-------------+-------------+
| myScheduler | calendarTriggerKey | firstCalendarGroup | DAY        | Asia/Shanghai | NULL       |          1 |          1 |           0 |           0 |       NULL |       NULL | 0           | 0           |
+-------------+--------------------+--------------------+------------+---------------+------------+------------+------------+-------------+-------------+------------+------------+-------------+-------------+

```

提供了3个string类型的参数，2个int类型的参数，2个long类型的参数，2个decimal类型的参数以及2个boolean类型的参数；具体每个参数是什么含义，根据不同的trigger类型存放各自的参数；

5.qrtz\_fired\_triggers  
存储已经触发的trigger相关信息，trigger随着时间的推移状态发生变化，直到最后trigger执行完成，从表中被删除；已SimpleTrigger为例重复3次执行，查询表：

```sql
mysql> select * from qrtz_fired_triggers;
+-------------+----------------------------------------+--------------------+------------------+---------------------------+---------------+---------------+----------+-----------+-----------------+-----------+------------------+-------------------+
| SCHED_NAME  | ENTRY_ID                               | TRIGGER_NAME       | TRIGGER_GROUP    | INSTANCE_NAME             | FIRED_TIME    | SCHED_TIME    | PRIORITY | STATE     | JOB_NAME        | JOB_GROUP | IS_NONCONCURRENT | REQUESTS_RECOVERY |
+-------------+----------------------------------------+--------------------+------------------+---------------------------+---------------+---------------+----------+-----------+-----------------+-----------+------------------+-------------------+
| myScheduler | NJD9YZGJ2-PC15241041777351524104177723 | firstSimpleTrigger | firstSimpleGroup | NJD9YZGJ2-PC1524104177735 | 1524104178499 | 1524104178472 |        0 | EXECUTING | firstSimpleTask | DEFAULT   | 0                | 0                 |
| myScheduler | NJD9YZGJ2-PC15241041777351524104177724 | firstSimpleTrigger | firstSimpleGroup | NJD9YZGJ2-PC1524104177735 | 1524104180477 | 1524104180472 |        0 | EXECUTING | firstSimpleTask | DEFAULT   | 0                | 0                 |
| myScheduler | NJD9YZGJ2-PC15241041777351524104177725 | firstSimpleTrigger | firstSimpleGroup | NJD9YZGJ2-PC1524104177735 | 1524104180563 | 1524104182472 |        0 | ACQUIRED  | NULL            | NULL      | 0                | 0                 |
+-------------+----------------------------------------+--------------------+------------------+---------------------------+---------------+---------------+----------+-----------+-----------------+-----------+------------------+-------------------+

```

相同的trigger和task，每触发一次都会创建一个实例；从刚被创建的ACQUIRED状态，到EXECUTING状态，最后执行完从数据库中删除；

6.qrtz_triggers  
存储定义的trigger，以上定义的三个triggers为例，分别是：firstSimpleTrigger，firstCalendarTrigger和firstCronTrigger，运行之后查看数据库：

```
mysql> select * from qrtz_triggers;
+-------------+--------------------+--------------------+-------------------+-----------+-------------+----------------+----------------+----------+---------------+--------------+---------------+----------+---------------+---------------+----------+
| SCHED_NAME  | TRIGGER_NAME       | TRIGGER_GROUP      | JOB_NAME          | JOB_GROUP | DESCRIPTION | NEXT_FIRE_TIME | PREV_FIRE_TIME | PRIORITY | TRIGGER_STATE | TRIGGER_TYPE | START_TIME    | END_TIME | CALENDAR_NAME | MISFIRE_INSTR | JOB_DATA |
+-------------+--------------------+--------------------+-------------------+-----------+-------------+----------------+----------------+----------+---------------+--------------+---------------+----------+---------------+---------------+----------+
| myScheduler | calendarTriggerKey | firstCalendarGroup | firstCalendarTask | DEFAULT   | NULL        |  1524203884719 |  1524117484719 |        5 | WAITING       | CAL_INT      | 1524117484719 |        0 | NULL          |             0 |          |
| myScheduler | firstCronTrigger   | firstCronGroup     | firstTask         | DEFAULT   | NULL        |  1524117492000 |  1524117486000 |        0 | ACQUIRED      | CRON         | 1524117483000 |        0 | firstCalendar |             0 |          |
| myScheduler | firstSimpleTrigger | firstSimpleGroup   | firstSimpleTask   | DEFAULT   | NULL        |             -1 |  1524117488436 |        0 | COMPLETE      | SIMPLE       | 1524117484436 |        0 | NULL          |             0 |          |
+-------------+--------------------+--------------------+-------------------+-----------+-------------+----------------+----------------+----------+---------------+--------------+---------------+----------+---------------+---------------+----------+

```

和qrtz\_fired\_triggers存放的不一样，不管trigger触发了多少次都只有一条记录，TRIGGER_STATE用来标识当前trigger的状态；firstCalendarTask每天执行一次，执行完之后一直是WAITING状态；firstCronTrigger每6秒执行一次状态是ACQUIRED状态；firstSimpleTrigger重复执行6次后状态为COMPLETE，并且会被删除；

7.qrtz\_job\_details  
存储jobDetails信息，相关信息在定义的时候指定，如上面定义的JobDetailFactoryBean，查询数据库：

```
mysql> select * from qrtz_job_details;
+-------------+-------------------+-----------+-------------+-----------------------------------------+------------+------------------+----------------+-------------------+----------+
| SCHED_NAME  | JOB_NAME          | JOB_GROUP | DESCRIPTION | JOB_CLASS_NAME                          | IS_DURABLE | IS_NONCONCURRENT | IS_UPDATE_DATA | REQUESTS_RECOVERY | JOB_DATA |
+-------------+-------------------+-----------+-------------+-----------------------------------------+------------+------------------+----------------+-------------------+----------+
| myScheduler | firstCalendarTask | DEFAULT   | NULL        | zh.maven.SQuartz.task.CalendarFirstTask | 0          | 0                | 0              | 0                 | |
| myScheduler | firstSimpleTask   | DEFAULT   | NULL        | zh.maven.SQuartz.task.SimpleFirstTask   | 0          | 0                | 0              | 0                 | |
| myScheduler | firstTask         | DEFAULT   | NULL        | zh.maven.SQuartz.task.FirstTask         | 0          | 0                | 0              | 0                 | |
+-------------+-------------------+-----------+-------------+-----------------------------------------+------------+------------------+----------------+-------------------+----------+

```

JOB_DATA存放的就是定义task时指定的jobDataMap属性，所以此属性需要实现Serializable接口，方便持久化到数据库；

8.qrtz_calendars  
Quartz为我们提供了日历的功能，可以自己定义一个时间段，可以控制触发器在这个时间段内触发或者不触发；现在提供6种类型：AnnualCalendar，CronCalendar，DailyCalendar，HolidayCalendar，MonthlyCalendar，WeeklyCalendar；以下使用CronCalendar为例：

```xml
<bean id="firstCalendar" class="org.quartz.impl.calendar.CronCalendar">
    <constructor-arg value="0/5 * * ? * *"></constructor-arg>
</bean>
<bean id="firstCronTrigger"
    class="org.springframework.scheduling.quartz.CronTriggerFactoryBean">
    <property name="jobDetail" ref="firstTask" />
    <property name="cronExpression" value="0/6 * * ? * *" />
    <property name="group" value="firstCronGroup"></property>
    <property name="calendarName" value="firstCalendar"></property>
</bean>
<bean id="scheduler"
    class="org.springframework.scheduling.quartz.SchedulerFactoryBean">
    <property name="schedulerName" value="myScheduler"></property>
    <property name="dataSource" ref="dataSource" />
    <property name="configLocation" value="classpath:quartz.properties" />
    <property name="triggers">
        <list>
            <ref bean="firstCronTrigger" />
        </list>
    </property>
    <property name="calendars">
        <map>
            <entry key="firstCalendar" value-ref="firstCalendar"></entry>
        </map>
    </property>
</bean>
```

定义了一个排除每隔5秒的CronCalendar，然后在firstCronTrigger中指定了calendarName，并且需要在SchedulerFactoryBean中定义calendars；因为firstCronTrigger每6秒执行一次，而CronCalendar排除每隔5秒，所以会出现firstCronTrigger在第5次触发的时候需要等待12秒，结果如下：

```
20180419 15:09:06---start FirstService
20180419 15:09:08---end FirstService
20180419 15:09:12---start FirstService
20180419 15:09:14---end FirstService
20180419 15:09:18---start FirstService
20180419 15:09:20---end FirstService
20180419 15:09:24---start FirstService
20180419 15:09:26---end FirstService
20180419 15:09:36---start FirstService
20180419 15:09:38---end FirstService
```

查询保存在数据中的CronCalendar：

```
mysql> select * from qrtz_calendars;
+-------------+---------------+----------+
| SCHED_NAME  | CALENDAR_NAME | CALENDAR |
+-------------+---------------+----------+
| myScheduler | firstCalendar | |
+-------------+---------------+----------+
```

CALENDAR存放的是CronCalendar序列化之后的数据；

9.qrtz\_paused\_trigger_grps  
存放暂停掉的触发器，测试手动暂停firstCronTrigger，代码如下：

```java
public class App {
    public static void main(String[] args) {
        final AbstractApplicationContext context = new ClassPathXmlApplicationContext("quartz.xml");
        final StdScheduler scheduler = (StdScheduler) context.getBean("scheduler");
        try {
            Thread.sleep(4000);
            scheduler.pauseTriggers(GroupMatcher.triggerGroupEquals("firstCronGroup"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

启动之后延迟4秒后暂停firstCronTrigger，这里传递的参数group，然后查看数据库：

```
mysql> select * from qrtz_paused_trigger_grps;
+-------------+----------------+
| SCHED_NAME  | TRIGGER_GROUP  |
+-------------+----------------+
| myScheduler | firstCronGroup |
+-------------+----------------+
```

因为已经入库，所以重启之后firstCronGroup还是处于暂停状态，firstCronTrigger不会运行；

10.qrtz\_scheduler\_state  
存储所有节点的scheduler，会定期检查scheduler是否失效，启动多个scheduler，查询数据库：

```
mysql> select * from qrtz_scheduler_state;
+-------------+---------------------------+-------------------+------------------+
| SCHED_NAME  | INSTANCE_NAME             | LAST_CHECKIN_TIME | CHECKIN_INTERVAL |
+-------------+---------------------------+-------------------+------------------+
| myScheduler | NJD9YZGJ2-PC1524209095408 |     1524209113973 |             1000 |
| myScheduler | NJD9YZGJ2-PC1524209097649 |     1524209113918 |             1000 |
+-------------+---------------------------+-------------------+------------------+
```

记录了最后最新的检查时间，在quartz.properties中设置了CHECKIN_INTERVAL为1000，也就是每秒检查一次；

11.qrtz_locks  
Quartz提供的锁表，为多个节点调度提供分布式锁，实现分布式调度，默认有2个锁：

```
mysql> select * from qrtz_locks;
+-------------+----------------+
| SCHED_NAME  | LOCK_NAME      |
+-------------+----------------+
| myScheduler | STATE_ACCESS   |
| myScheduler | TRIGGER_ACCESS |
+-------------+----------------+
```

STATE_ACCESS主要用在scheduler定期检查是否失效的时候，保证只有一个节点去处理已经失效的scheduler；  
TRIGGER_ACCESS主要用在TRIGGER被调度的时候，保证只有一个节点去执行调度；

**总结**  
本文对这11张表做了简要的分析，介绍了每张表具体是用来存储什么的，并且给了简单的实例；其实如果要实现一个trigger的管理系统，其实也就是对这几张表的维护。