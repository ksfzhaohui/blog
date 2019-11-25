**系列文章**

[Demo入门Mycat](https://my.oschina.net/OutOfMemory/blog/1625554)

[Demo之Mycat读写分离](https://my.oschina.net/OutOfMemory/blog/1631912)

**前言**  
Mycat是一个开源的分布式数据库系统，是一个实现了MySQL协议的的Server，前端用户可以把它看作是一个数据库代理，用MySQL客户端工具和命令行访问，而其后端可以用MySQL原生（Native）协议与多个MySQL服务器通信，也可以用JDBC协议与大多数主流数据库服务器通信，其核心功能是分表分库，即将一个大表水平分割为N个小表，存储在后端MySQL服务器里或者其他数据库里；  
下面将从Mycat提供的demo来简单分析一下：

**准备**  
1.Jdk1.7.0_80  
2.Mysql 5.5  
3.Mycat-server-1.6-release http://dl.mycat.io/1.6-RELEASE/  
4.Mycat配置后端的writeHost和readHost，配置成本机即可，在conf/schema.xml下：

```xml
<dataHost name="localhost1" maxCon="1000" minCon="10" balance="0"
              writeType="0" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">
        <heartbeat>select user()</heartbeat>
        <!-- can have multi write hosts -->
        <writeHost host="hostM1" url="localhost:3306" user="root"
                   password="root">
            <!-- can have multi read hosts -->
            <readHost host="hostS2" url="localhost:3306" user="root" password="root" />
        </writeHost>
</dataHost>
```

5.配置日志等级为debug，在conf/log4j2.xml下：

```xml
<asyncRoot level="debug" includeLocation="true">
     <AppenderRef ref="Console" />
     <AppenderRef ref="RollingFile"/>
</asyncRoot>
```

**启动Mycat**  
1.启动Mycat，运行bin/startup_nowrap.bat可执行文件  
2.连接Mycat服务器，默认端口是8066

```bash
C:\Users\hui.zhao.cfs>mysql -uroot -proot -P8066 -h127.0.0.1
```

3.简单查看Mycat服务器，包括数据库，数据表

```sql
mysql> show databases;
+----------+
| DATABASE |
+----------+
| TESTDB   |
+----------+
1 row in set (0.00 sec)

mysql> use TESTDB;
Database changed
mysql> show tables;
+------------------+
| Tables in TESTDB |
+------------------+
| company          |
| customer         |
| customer_addr    |
| employee         |
| goods            |
| hotnews          |
| orders           |
| order_items      |
| travelrecord     |
+------------------+
9 rows in set (0.01 sec)
```

以上显示的数据库和数据表，都配置在conf/schema.xml中，相关的还有conf/server.xml和conf/rule.xml。  
schema.xml主要定义了逻辑库，逻辑表等相关信息；  
server.xml主要配置了一些系统参数；  
rule.xml主要定义了分库分表的一些规则。  
下面主要以schema.xml中配置的默认的逻辑库和逻辑表，来做一些简单的操作了解Mycat。

**Demo展示**  
schema.xml定义了后台的Mysql数据库db1，db2，db3；所以首先需要在Mysql数据库中创建这三个数据库；

1.表travelrecord(分片规则)，定义如下：

```xml
<table name="travelrecord" dataNode="dn1,dn2,dn3" rule="auto-sharding-long" />
```

name：定义了逻辑表的表名；  
dataNode：定义这个逻辑表所属的dataNode,需要和dataNode标签中name属性的值相互对应，也就是对应的后台的数据库；  
rule：用于指定逻辑表要使用的规则名字，规则名字在rule.xml中定义；

1.1分别在三个数据库中创建表

```sql
CREATE TABLE `travelrecord` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

1.2在rule.xml中定义的规则

```xml
<tableRule name="auto-sharding-long">
    <rule>
        <columns>id</columns>
        <algorithm>rang-long</algorithm>
    </rule>
</tableRule>
<function name="rang-long" class="io.mycat.route.function.AutoPartitionByLong">
    <property name="mapFile">autopartition-long.txt</property>
</function>
```

此分片规则提前规划好分片字段某个范围属于哪个分片，具体定义在conf/autopartition-long.txt文件中；并且指定了id作为分片字段；

1.3模拟id范围插入数据

```sql
insert into travelrecord (id,name) values(1,'hehe');
insert into travelrecord (id,name) values(5000001,'hehe');
insert into travelrecord (id,name) values(10000001,'hehe');
```

1.4查询数据，并观察日志

```sql
select * from travelrecord where id=5000001;
select * from travelrecord;
```

id=5000001应该路由到dn2节点，查看日志：

```sql
route={1 -> dn2{SELECT * FROM travelrecord WHERE id = 5000001 LIMIT 100}
```

无查询条件的应该路由到三个节点，查看日志：

```sql
route={
   1 -> dn1{SELECT * FROM travelrecord LIMIT 100} 
   2 -> dn2{SELECT * FROM travelrecord LIMIT 100}
   3 -> dn3{SELECT * FROM travelrecord LIMIT 100}
}
```

2.表company(全局表)，定义如下：

```sql
<table name="company" primaryKey="ID" type="global" dataNode="dn1,dn2,dn3" />
```

2.1分别在三个数据库中创建表

```sql
CREATE TABLE `company` (
  `ID` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

2.2逻辑表类型  
type定义了逻辑表的类型，目前逻辑表只有“全局表”和”普通表”两种类型，如果是全局表，所有的分片都有一份相同的数据；

```sql
insert into company (ID,name) values(1,'hehe');
```

查看日志如下：

```sql
route={
   1 -> dn1{insert into company (ID,name) values(1,'hehe')}
   2 -> dn2{insert into company (ID,name) values(1,'hehe')}
   3 -> dn3{insert into company (ID,name) values(1,'hehe')}
} rrs
```

2.3查看全局表

```sql
select * from company;
```

多次执行查看全局表，查看日志会发现每次从三个分片中随机取一个执行查询语句；以下三条日志是执行三次的结果：

```sql
route={1 -> dn3{SELECT * FROM company LIMIT 100}} rrs
route={1 -> dn2{SELECT * FROM company LIMIT 100}} rrs
route={1 -> dn1{SELECT * FROM company LIMIT 100}} rrs
```

3.表hotnews(自增主键)，定义如下：

```xml
<table name="hotnews" primaryKey="ID" autoIncrement="true" dataNode="dn1,dn2,dn3" rule="mod-long" />
```

3.1分别在三个数据库中创建表

```sql
CREATE TABLE `hotnews` (
  `id` bigint(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

3.2自增长主键  
在分库分表的情况下，默认的MySQL的自增长主键无法满足主键的唯一性，Mycat提供了全局序列号的功能，来保证表主键的唯一性；  
Mycat提供了多种全局序列号的方式包括：本地文件方式，数据库方式，本地时间戳方式，分布式ZK ID 生成器，Zk 递增方式；  
server.xml默认的的sequnceHandlerType=2，表示本地时间戳方式，具体使用如下：

```sql
insert into hotnews (id,name) values(next value for MYCATSEQ_GLOBAL,'hehe');
```

同时配置了rule=”mod-long”,指定的分片规则为取模，可以查看数据结果，在db1下插入了如下数据：

```
968418212909813760  hehe
```

4.表employee(配置primaryKey)，定义如下：

```xml
<table name="employee" primaryKey="ID" dataNode="dn1,dn2" rule="sharding-by-intfile" />
```

4.1分别在三个数据库中创建表

```sql
CREATE TABLE `employee` (
  `id` int(11) NOT NULL,
  `sharding_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

4.2插入数据  
employee指定的分片规则是sharding-by-intfile，具体conf/rule.xml:

```xml
<tableRule name="sharding-by-intfile">
    <rule>
        <columns>sharding_id</columns>
        <algorithm>hash-int</algorithm>
    </rule>
</tableRule>
<function name="hash-int" class="io.mycat.route.function.PartitionByFileMap">
    <property name="mapFile">partition-hash-int.txt</property>
</function>
```

使用sharding_id作为分片字段，分片规则是“分片枚举”：通过在配置文件中配置可能的枚举 id，自己配置分片，此处配置在conf/partition-hash-int.txt中，准备插入两条数据，分别入库dn1和dn2：

```sql
insert into employee (id,sharding_id,name) values(1,10000,'hehe0');
insert into employee (id,sharding_id,name) values(2,10010,'hehe1');
```

4.3primaryKey属性  
表示该逻辑表对应真实表的主键，当分片规则使用非主键进行分片的，当使用主键查询时，会将查询语句发送到所有的分片节点上，如果配置了该属性，那么Mycat就会缓存主键和具体dataNode的信息；

```sql
select * from employee where id=1;
```

第一次执行上面的查询语句，查看日志可以发现往2个分片节点上都发送了查询语句：

```
route={
   1 -> dn1{select * from employee where id=1}
   2 -> dn2{select * from employee where id=1}
} rrs
```

当第二次执行此查询语句，再查看日志只往一个节点发送了查询语句：

```
route={
   1 -> dn1{select * from employee where id=1}
} rrs
```

5.表customer和orders(父子表关系)，定义如下：

```xml
<table name="customer" primaryKey="ID" dataNode="dn1,dn2" rule="sharding-by-intfile">
    <childTable name="orders" primaryKey="ID" joinKey="customer_id" parentKey="id">
    </childTable>
</table>
```

5.1分别在三个数据库中创建表

```sql
CREATE TABLE `customer` (
  `id` int(11) NOT NULL,
  `sharding_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `orders` (
  `id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

5.2父子表关系  
childTable 标签用于定义 E-R 分片的子表，通过标签上的属性与父表进行关联；  
joinKey：插入子表的时候会使用这个列的值查找父表存储的数据节点；  
parentKey：属性指定的值一般为与父表建立关联关系的列名。程序首先获取joinkey的值，再通过parentKey属性指定的列名产生查询语句，通过执行该语句得到父表存储在哪个分片上，从而确定子表存储的位置；

5.3模拟数据的插入

```sql
insert into customer (id,sharding_id,name) values(1,10000,'hehe0');
insert into customer (id,sharding_id,name) values(2,10010,'hehe1');
```

分别通过分片规则往dn1和dn2上面各自插入了一条数据，下面再往orders插入数据看是否可以插入到关联的节点上；

```sql
insert into orders (id,customer_id,name) values(1,1,'order1');
```

customer_id=1对应的customer表的id，应该插入到dn1节点上，查看日志：

```
route={
   1 -> dn1{insert into orders (id,customer_id,name) values(1,1,'order1')}
} rrs
```

同理指定customer_id=2：

```
insert into orders (id,customer_id,name) values(2,2,'order1');

route={
   1 -> dn2{insert into orders (id,customer_id,name) values(2,2,'order2')}
} rrs
```

**总结**  
本文主要从Mycat自带的demo并结合官方提供的文档大概了解了一下Mycat的相关功能，很多功能并没有深入，算是一个简单的入门；后续准备了解对Mycat有更全面的了解，同时能够深入到源码层面。