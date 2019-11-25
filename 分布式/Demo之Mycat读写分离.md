**系列文章**

[Demo入门Mycat](https://my.oschina.net/OutOfMemory/blog/1625554)

[Demo之Mycat读写分离](https://my.oschina.net/OutOfMemory/blog/1631912)

**前言**  
数据库读写分离对于大型系统或者访问量很高的互联网应用来说，是必不可少的一个重要功能；对于MySQL来说，标准的读写分离是主从模式，一个写节点Master后面跟着多个读节点，其中包含两个步骤，其一是数据源的主从同步，其二是sql的读写分发；而Mycat不负责任何数据的同步，具体的数据同步还是依赖Mysql数据库自身的功能。

**Mysql主从复制**  
准备两台主机，安装相同版本的Mysql数据库，下面准备配置Mysql的主从复制配置：  
1.配置Master  
配置my.ini或者my.conf如下：

```
server-id=1
binlog_format=STATEMENT
log_bin=D:/mysql/bin-log.log
```

_server-id_：一般设置为IP,注意要唯一；_binlog_format_：设置binlog的格式；_log_bin_：开启log_bin

2.配置Slave  
配置my.ini或者my.conf如下：

```
server-id=2
binlog_format=STATEMENT
relay_log=mysql-relay-bin
```

_relay_log_：从服务器I/O线程将主服务器的二进制日志读取过来记录到从服务器本地文件，然后SQL线程会读取relay-log日志的内容并应用到从服务器；

更多binlog的配置：[https://my.oschina.net/OutOfMemory/blog/1571107](https://my.oschina.net/OutOfMemory/blog/1571107)

3.分别重启Master和Slave

```bash
service mysqld restart;
```

4.查询Master的状态

```
mysql> show master status;
+----------------+----------+--------------+------------------+
| File           | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+----------------+----------+--------------+------------------+
| bin-log.000001 |      107 |              |                  |
+----------------+----------+--------------+------------------+
```

5.Slave设置Master

```bash
change master to master_host='192.168.110.1', master_user='root', master_password='root', master_port=3306, master_log_file='bin-log.000001', master_log_pos=107, master_connect_retry=10; 
```

_master_host_：MasterIp；_master_user_：用于同步数据的用户；_master_password_：用于同步数据的密码；_master\_log\_file_：Slave从哪个日志文件开始同步数据；_master\_log\_pos_：从什么位置开始同步数据；

6.Slave开始同步

```sql
start slave; 
```

7.查看Slave同步状态

```sql
show slave status;
```

Slave\_IO\_Running和Slave\_SQL\_Running都为Yes表示已经开始工作了；

8.测试主从同步  
在Master上创建数据库db1：

```sql
create database db1;
```

查看Slave是否能同步到数据库；

**Mycat读写分离**  
Mycat读写分离配置在conf/schema.xml文件中：

```xml
<dataHost name="localhost1" maxCon="1000" minCon="10" balance="0" writeType="0" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">
    <heartbeat>select user()</heartbeat>
    <writeHost host="hostM1" url="localhost:3306" user="root" password="root">
            <readHost host="hostS2" url="192.168.237.128:3306" user="root" password="root" />
    </writeHost>
</dataHost>
```

_maxCon_：连接池的最大连接，_minCon_：例连接池的最小连接；_dbType_：指定后端连接的数据库类型；_dbDriver_：指定连接后端数据库使用的Driver，目前可选的值有native和JDBC；  
_balance_：负载均衡类型  
0：不开启读写分离机制，  
1：全部的readHost与stand by writeHost参与select语句的负载均衡，  
2：所有读操作都随机的在writeHost、readhost上分发，  
3：所有读请求随机的分发到wiriterHost对应的readhost执行，writerHost不负担读压力  
_writeType_：负载均衡类型  
0：所有写操作发送到配置的第一个writeHost，第一个挂了切到还生存的第二个writeHost，重新启动后已切换后的为准，切换记录在配置文件中:dnindex.properties  
1：所有写操作都随机的发送到配置的writeHost，1.5以后废弃不推荐  
_switchType_：切换类型  
-1：表示不自动切换，  
1：默认值自动切换，  
2：基于MySQL主从同步的状态决定是否切换（心跳语句为 show slave status），  
3：基于MySQL galary cluster的切换机制（心跳语句为 show status like ‘wsrep%’）  
_slaveThreshold_：Slave数据库延迟阀值  
1.4开始支持MySQL主从复制状态绑定的读写分离机制，让读更加安全可靠；switchType=”2″ 与 slaveThreshold=”100″，此时意味着开启MySQL主从复制状态绑定的读写分离与切换机制，Mycat心跳机制通过检测show slave status中的 “Seconds\_Behind\_Master”, “Slave\_IO\_Running”, “Slave\_SQL\_Running” 三个字段来确定当前主从同步的状态以及Seconds\_Behind\_Master主从复制时延， 当Seconds\_Behind\_Master>slaveThreshold时，读写分离筛选器会过滤掉此Slave机器，防止读到很久之前的旧数据，而当主节点宕机后，切换逻辑会检查Slave上的Seconds\_Behind\_Master是否为0，为0时则表示主从同步，可以安全切换，否则不会切换；  
_heartbeat标签_：明用于和后端数据库进行心跳检查的语句；  
_writeHost标签、readHost标签_：writeHost指定写实例，readHost指定读实例，一个dataHost内可以定义多个writeHost和readHost，如果writeHost指定的后端数据库宕机，那么这个writeHost绑定的所有readHost都将不可用；由于这个writeHost宕机系统会自动的检测到，并切换到备用的writeHost上去。

**Demo展示**  
1.准备测试数据  
执行插入语句：

```sql
insert into travelrecord (id,name) values(1,'hehe');
```

查询日志：

```sql
ServerConnection [id=1, schema=TESTDB, host=127.0.0.1, user=root,txIsolation=3, autocommit=true, schema=TESTDB]insert into travelrecord (id,name) values(1,'hehe'), route={
   1 -> dn1{insert into travelrecord (id,name) values(1,'hehe')}
} rrs 
```

通过分片策略插入到dn1中

2.不开启读写分离机制，设置balance=0  
执行查询：

```sql
SELECT * FROM travelrecord;
```

查询日志：

```sql
select read source hostM1 for dataHost:localhost1
```

可以发现查询的数据还是来自hostM1，没有进行读写分离

3.readHost与stand by writeHost参与select语句的负载均衡，设置balance=1  
执行查询：

```sql
SELECT * FROM travelrecord;
```

查询日志：

```sql
select read source hostS2 for dataHost:localhost1
```

因为此处没有stand by writeHost，所以所有的读操作都进入了hostS2

4.所有读操作都随机的在writeHost、readhost上分发，设置balance=2  
多次执行查询：

```sql
SELECT * FROM travelrecord;
```

查询日志：

```sql
select read source hostS2 for dataHost:localhost1
select read source hostM1 for dataHost:localhost1
```

因为是随机分发，所以查询语句会在hostS2和hostM1上切换

5.分发到wiriterHost对应的readhost执行，设置balance=3  
多次执行查询：

```sql
SELECT * FROM travelrecord;
```

查询日志：

```sql
select read source hostS2 for dataHost:localhost1
```

执行多次可以发现查询语句只在hostS2上

6.自动切换，switchType=1  
停掉hostM1，执行查询语句：

```sql
SELECT * FROM travelrecord;
ERROR 1105 (HY000): backend connect: java.net.ConnectException: Connection refus
ed: no further information
```

如果writeHost指定的后端数据库宕机，那么这个writeHost绑定的所有readHost都将不可用

7.自动切换,设置balance=3，switchType=1  
首先将readHost改成writeHost从库：

```xml
<writeHost host="hostM1" url="localhost:3306" user="root" password="root"/>
<writeHost host="hostM2" url="192.168.237.128:3306" user="root" password="root" />
```

执行查询：

```sql
SELECT * FROM travelrecord;
```

查询日志：

```sql
select read source hostM1 for dataHost:localhost1
```

此时hostM1并不存在从库，所以就算balance=3，也会从hostM1查询

停到hostM1，执行查询和插入语句：

```sql
SELECT * FROM travelrecord;

select read source hostM2 for dataHost:localhost1
```

执行插入：

```sql
insert into travelrecord (id,name) values(3,'hehe');

node=dn1{insert into travelrecord (id,name) values(3,'hehe')}, packetId=1], host=192.168.237.128, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=true

```

查看conf/dnindex.properties被更新了，内容如下：

```
#update
#Thu Mar 08 15:20:31 CST 2018
localhost1=1
```

hostM1是第0个，hostM2为第1个，所以后续的主库都是hostM2

重启Mycat，执行查询和插入语句：

```sql
SELECT * FROM travelrecord;

select read source hostM2 for dataHost:localhost1
```

执行插入：

```sql
insert into travelrecord (id,name) values(4,'hehe');

node=dn1{insert into travelrecord (id,name) values(4,'hehe')}, packetId=1], host=192.168.237.128, port=3306, statusSync=null, writeQueue=0, modifiedSQLExecuted=true
```

可以发现因为dnindex.properties已经记录了主host，所以后面的查询和插入都定位到了hostM2

8.不自动切换，设置balance=1，switchType=-1

注：此处为了方便将conf/dnindex.properties中的localhost1设置为0

```sql
SELECT * FROM travelrecord;

select read source hostM2 for dataHost:localhost1
```

balance=1，所有查询语句在hostM2，停掉hostM1，执行查询：

```sql
SELECT * FROM travelrecord;

select read source hostM2 for dataHost:localhost1
```

执行插入语句：

```sql
insert into travelrecord (id,name) values(5,'hehe');
ERROR 1184 (HY000): Connection refused: no further information
```

可以查询无法插入数据，同时conf/dnindex.properties中的值也没有更新

9.安全切换，设置balance=1，switchType=2  
正常切换，执行插入语句：

```sql
insert into travelrecord (id,name) values(6,'hehe');
```

停掉hostM1，此时dnindex.properties更新，localhost1=1，表示已经切换为hostM2

延迟的切换，为了模拟同步的延迟，将hostM2的表锁住：

```sql
LOCK TABLES travelrecord write;
```

执行插入语句：

```sql
insert into travelrecord (id,name) values(7,'hehe');
```

停掉hostM1，此时dnindex.properties没有更新，localhost1=0，表示没有切换

解锁表

```sql
UNLOCK TABLES;
```

10.避免读延迟，设置balance=3，switchType=2，slaveThreshold=1，同时配置writeHost

```xml
<writeHost host="hostM1" url="localhost:3306" user="root"
    <readHost host="hostS2" url="192.168.237.128:3306" user="root" password="root" />
</writeHost>
```

首先锁住表

```sql
LOCK TABLES travelrecord write;
```

然后执行插入数据：

```sql
insert into travelrecord (id,name) values(8,'hehe');
```

此时执行查询语句：

```sql
SELECT * FROM travelrecord;

select read source hostM1 for dataHost:localhost1
```

解锁表然后再次查询表travelrecord，查询日志如下：

```sql
select read source hostS2 url for dataHost:localhost1
```

在从表同步数据延迟的情况下，查询数据到hostM1，当同步完成后，查询数据又到hostS2

**总结**  
本文主要介绍了Mycat的读写分离以及相关配置，然后已Demo的形式展示Mycat的读写分离机制，其中尽可能将各种配置的情况展示出来，力求全名的了解Mycat读写分离。