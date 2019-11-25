**系列文章**

[MySql Binlog初识](https://my.oschina.net/OutOfMemory/blog/1571107)

[MySql Binlog事件介绍篇](https://my.oschina.net/OutOfMemory/blog/1572968)

[MySql Binlog事件数据篇](https://my.oschina.net/OutOfMemory/blog/1579454)

[Mysql通讯协议分析](https://my.oschina.net/OutOfMemory/blog/1595684)

[基于Netty模拟解析Binlog](https://my.oschina.net/OutOfMemory/blog/1605201)

**Mysql日志**  
MySQL 的日志包括错误日志（ErrorLog），更新日志（Update Log），二进制日志（Binlog），查询日志（Query Log），慢查询日志（Slow Query Log）等；  
更新日志是老版本的MySQL 才有的，目前已经被二进制日志替代；在默认情况下，系统仅仅打开错误日志，关闭了其他所有日志，以达到尽可能减少IO损耗提高系统，  
性能的目的，但是在一般稍微重要一点的实际应用场景中，都至少需要打开二进制日志，因为这是MySQL很多存储引擎进行增量备份的基础，也是MySQL实现复制的基本条件；  
下面介绍的就是二进制日志–Binlog

**Binlog开启**  
默认Binlog是关闭的，首先要开启才能记录日志；  
1.查看是否开启log_bin

```
mysql> show variables like 'log_bin';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | OFF   |
+---------------+-------+
```

2.开启log_bin  
在my.ini中添加配置：

```
log_bin=D:/mysql/bin-log.log
```

3.重启mysql，再次查看

```
mysql> show variables like 'log_bin';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | ON    |
+---------------+-------+
```

4.查看Binlog  
4.1显示binlog的名称和大小

```
mysql> show binary logs;
+----------------+-----------+
| Log_name       | File_size |
+----------------+-----------+
| bin-log.000001 |       107 |
+----------------+-----------+
```

4.2查看生成的binlog  
想用查看到binlog可以往数据库中更新数据库(包括：插入，更新和删除操作)，查询sql是无法生成binlog的

```
mysql> show binlog events;
+----------------+------+-------------+-----------+-------------+---------------
------------------------------------------------+
| Log_name       | Pos  | Event_type  | Server_id | End_log_pos | Info
                                                |
+----------------+------+-------------+-----------+-------------+---------------
------------------------------------------------+
| bin-log.000001 |    4 | Format_desc |         1 |         107 | Server ver: 5.
5.29-log, Binlog ver: 4                         |
| bin-log.000001 |  107 | Query       |         1 |         175 | BEGIN
                                                |
| bin-log.000001 |  175 | Intvar      |         1 |         203 | INSERT_ID=9
                                                |
| bin-log.000001 |  203 | Query       |         1 |         315 | use `test`; in
sert into user (age,name) values(100,"zhaohui") |
| bin-log.000001 |  315 | Xid         |         1 |         342 | COMMIT /* xid=
41 */                                           |
+----------------+------+-------------+-----------+-------------+---------------
------------------------------------------------+
```

**Binlog相关参数**  
通过执行如下命令可以获得关于Binlog 的相关参数：

```
mysql> show variables like '%binlog%';
+-----------------------------------------+----------------------+
| Variable_name                           | Value                |
+-----------------------------------------+----------------------+
| binlog_cache_size                       | 32768                |
| binlog_direct_non_transactional_updates | OFF                  |
| binlog_format                           | STATEMENT            |
| binlog_stmt_cache_size                  | 32768                |
| innodb_locks_unsafe_for_binlog          | OFF                  |
| max_binlog_cache_size                   | 18446744073709547520 |
| max_binlog_size                         | 1073741824           |
| max_binlog_stmt_cache_size              | 18446744073709547520 |
| sync_binlog                             | 0                    |
+-----------------------------------------+----------------------+
```

1.binlog\_cache\_size  
在事务过程中容纳binlog SQL语句的缓存大小；binlog缓存是服务器支持事务存储引擎并且服务器启用了二进制日志(—log-bin选项)的前提下为每个Session分配的内存；  
主要是用来提高binlog的写速度；可以通过MySQL的以下个状态变量来判断当前的binlog\_cache\_size的状况：Binlog\_cache\_use和Binlog\_cache\_disk_use

```
mysql> show status like 'Binlog_cache%';
+-----------------------+-------+
| Variable_name         | Value |
+-----------------------+-------+
| Binlog_cache_disk_use | 0     |
| Binlog_cache_use      | 4     |
+-----------------------+-------+
```

Binlog\_cache\_use：使用缓冲区存放binlog的次数  
Binlog\_cache\_disk_use：使用临时文件存放binlog的次数

2.binlog\_stmt\_cache_size  
发生事务时非事务语句的缓存的大小，可以通过MySQL 的以下个状态变量来判断当前的binlog\_stmt\_cache\_size的状况：Binlog\_stmt\_cache\_use和Binlog\_stmt\_cache\_disk\_use

```
mysql> show status like 'binlog_stmt_cache%';
+----------------------------+-------+
| Variable_name              | Value |
+----------------------------+-------+
| Binlog_stmt_cache_disk_use | 0     |
| Binlog_stmt_cache_use      | 1     |
+----------------------------+-------+
```

Binlog\_stmt\_cache_use：缓冲区存放binlog的次数  
Binlog\_stmt\_cache\_disk\_use：临时文件存放binlog的次数

3.max\_binlog\_cache_size  
和binlog\_cache\_size相对应，但是所代表的是binlog能够使用的最大cache内存大小；binlog\_cache\_size对应的每个Session，max\_binlog\_cache_size对应所有Session；  
当我们执行多语句事务的时候，所有Session的使用的内存超过max\_binlog\_cache_size的值时，系统可能会报出”Multi-statement transaction required more than ‘max\_binlog\_cache_size’ bytes of storage”的错误。

4.max\_binlog\_stmt\_cache\_size  
同max\_binlog\_cache_size类似，非事务语句binlog能够使用的最大cache内存大小。

5.max\_binlog\_size  
Binlog日志最大值，默认1G，。该大小并不能非常严格控制Binlog大小，尤其是当到达Binlog比较靠近尾部而又遇到一个较大事务的时候，系统为了保证事务的完整性，不可能做切换日志的动作，只能将该事务的所有SQL都记录进入当前日志，直到该事务结束。

6.sync_binlog  
同步binlog缓存中数据到磁盘的方式：  
sync_binlog=0  
当事务提交之后，MySQL不做fsync之类的磁盘同步指令刷新binlog_cache中的信息到磁盘，而让Filesystem自行决定什么时候来做同步，或者cache满了之后才同步到磁盘；  
sync_binlog=n（区间0-4294967295）  
当每进行n次事务提交之后，MySQL将进行一次fsync之类的磁盘同步指令来将binlog_cache中的数据强制写入磁盘；

系统默认设置为0，这种情况下性能最好，风险最大，可能导致binlog\_cache中的数据丢失；sync\_binlog=1性能最差，风险最小。

7.binlog_format  
设置binlog的格式，可选值：STATEMENT, ROW, or MIXED；默认是：STATEMENT  
7.1 ROW模式  
日志中记录成每一行数据被修改的形式，然后在slave端再对相同的数据进行修改；  
在ROW模式下bin-log中可以不记录执行的SQL语句的上下文相关的信息，只需要记录哪条数据被修改成什么样了，不会因为某些语法复制出现问题（比如function，trigger等）；  
缺点是每行数据的修改都会记录，最明显的就是update语句，导致更新多少条数据就会产生多少事件，使bin-log文件很大，而复制要网络传输，影响性能。

7.2 STATEMENT模式  
每一条修改数据的sql都会被记录到bin-log中，slave端再根据sql语句重现，解决了ROW模式的缺点，不会产生大量是bin-log数据；  
缺点是为了让sql能在slave端正确重现，需要记录sql在执行的上下文信息，另外一个问题就是在复制某些特殊的函数或者功能的时候会出现问题，比如sleep()函数。

7.3 MIXED模式  
前面两种模式的结合，根据不同的情况分别使用ROW模式和STATEMENT模式。

更多参数详见：[https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html](https://dev.mysql.com/doc/refman/5.7/en/replication-options-binary-log.html)

**Binlog结构和内容**  
日志由一组二进制日志文件（Binlog）,加上一个索引文件（index）；Binlog是一个二进制文件集合，每个Binlog以一个4字节的魔数开头，接着是一组Events；  
1.魔数：0xfe62696e对应的是0xfebin；  
2.Event：每个Event包含header和data两个部分；header提供了Event的创建时间，哪个服务器等信息，data部分提供的是针对该Event的具体信息，如具体数据的修改；  
3.第一个Event用于描述binlog文件的格式版本，这个格式就是event写入binlog文件的格式；  
4.其余的Event按照第一个Event的格式版本写入；  
5.最后一个Event用于说明下一个binlog文件；  
6.Binlog的索引文件是一个文本文件，其中内容为当前的binlog文件列表，比如：

```
D:\mysql\bin-log.000001
```

参考：[https://dev.mysql.com/doc/internals/en/binary-log-structure-and-contents.html](https://dev.mysql.com/doc/internals/en/binary-log-structure-and-contents.html)

**Binlog的Event类型**  
官方提供的可能Event类型有36种，具体看下面的枚举：

```
enum Log_event_type { 
  UNKNOWN_EVENT= 0, 
  START_EVENT_V3= 1, 
  QUERY_EVENT= 2, 
  STOP_EVENT= 3, 
  ROTATE_EVENT= 4, 
  INTVAR_EVENT= 5, 
  LOAD_EVENT= 6, 
  SLAVE_EVENT= 7, 
  CREATE_FILE_EVENT= 8, 
  APPEND_BLOCK_EVENT= 9, 
  EXEC_LOAD_EVENT= 10, 
  DELETE_FILE_EVENT= 11, 
  NEW_LOAD_EVENT= 12, 
  RAND_EVENT= 13, 
  USER_VAR_EVENT= 14, 
  FORMAT_DESCRIPTION_EVENT= 15, 
  XID_EVENT= 16, 
  BEGIN_LOAD_QUERY_EVENT= 17, 
  EXECUTE_LOAD_QUERY_EVENT= 18, 
  TABLE_MAP_EVENT = 19, 
  PRE_GA_WRITE_ROWS_EVENT = 20, 
  PRE_GA_UPDATE_ROWS_EVENT = 21, 
  PRE_GA_DELETE_ROWS_EVENT = 22, 
  WRITE_ROWS_EVENT = 23, 
  UPDATE_ROWS_EVENT = 24, 
  DELETE_ROWS_EVENT = 25, 
  INCIDENT_EVENT= 26, 
  HEARTBEAT_LOG_EVENT= 27, 
  IGNORABLE_LOG_EVENT= 28,
  ROWS_QUERY_LOG_EVENT= 29,
  WRITE_ROWS_EVENT = 30,
  UPDATE_ROWS_EVENT = 31,
  DELETE_ROWS_EVENT = 32,
  GTID_LOG_EVENT= 33,
  ANONYMOUS_GTID_LOG_EVENT= 34,
  PREVIOUS_GTIDS_LOG_EVENT= 35, 
  ENUM_END_EVENT 
  /* end marker */
};
```

参考：[https://dev.mysql.com/doc/internals/en/event-classes-and-types.html](https://dev.mysql.com/doc/internals/en/event-classes-and-types.html)

Event结构官网提供了3个版本，分别是v1，v3，v4：  
v1：用在MySQL 3.23  
v3：用在MySQL 4.0.2-4.1  
v4：用在MySQL 5.0之后

现在MySQL的版本基本都使用5.0之后的版本，可以直接看v4，具体如下：

```
+=====================================+
| event  | timestamp         0 : 4    |
| header +----------------------------+
|        | type_code         4 : 1    |
|        +----------------------------+
|        | server_id         5 : 4    |
|        +----------------------------+
|        | event_length      9 : 4    |
|        +----------------------------+
|        | next_position    13 : 4    |
|        +----------------------------+
|        | flags            17 : 2    |
|        +----------------------------+
|        | extra_headers    19 : x-19 |
+=====================================+
| event  | fixed part        x : y    |
| data   +----------------------------+
|        | variable part              |
+=====================================+
```

名字后面的两个数字表示：offset : length即从第几个字节开始，后面多少个字节用来存放数据  
比如：timestamp(0 : 4)表示从第0个字节开始，往后四个字节用来存放timestamp  
目前来说x=19，所有extra_headers是空的，y是fixed part的长度，不同的Event长度不一样。

参考：[https://dev.mysql.com/doc/internals/en/event-structure.html](https://dev.mysql.com/doc/internals/en/event-structure.html)

**Event简要分析**  
1.从一个最简单的实例来分析其中的Event，包括创建表，插入数据，更新数据，删除数据；binlog_format使用的是默认的STATEMENT；

```
CREATE TABLE `btest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `age` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
 
insert into btest values(1,100,'zhaohui');
update btest set name='zhaohui_new' where id=1;
delete from btest where id=1;
```

2.查看所有的Events

```
show binlog events;
```

![](https://static.oschina.net/uploads/space/2017/1110/153425_V7Y6_159239.jpg)

上图中一共出现了3中类型的Event，分别是Format_desc，Query和Xid，下面进行简单的分析

2.1Format\_desc\_Event  
官网格式如下：

```
+=====================================+
| event  | timestamp         0 : 4    |
| header +----------------------------+
|        | type_code         4 : 1    | = FORMAT_DESCRIPTION_EVENT = 15
|        +----------------------------+
|        | server_id         5 : 4    |
|        +----------------------------+
|        | event_length      9 : 4    | >= 91
|        +----------------------------+
|        | next_position    13 : 4    |
|        +----------------------------+
|        | flags            17 : 2    |
+=====================================+
| event  | binlog_version   19 : 2    | = 4
| data   +----------------------------+
|        | server_version   21 : 50   |
|        +----------------------------+
|        | create_timestamp 71 : 4    |
|        +----------------------------+
|        | header_length    75 : 1    |
|        +----------------------------+
|        | post-header      76 : n    | = array of n bytes, one byte per event
|        | lengths for all            |   type that the server knows about
|        | event types                |
+=====================================+
```

使用十六进制方式打开文件bin-log.000001，以下是Format\_desc\_Event的十六进制代码：  
![](https://static.oschina.net/uploads/space/2017/1110/153502_9nep_159239.jpg)

可以先看前面的4+103=107个字节，4字节是binlog的魔数，103字节是Format\_desc\_Event，其中有19字节是header；  
注:Binlog日志是小端字节顺序  
0x5A0504AA四个字节是timestamp；0x0F一个字节表示type\_code；0x00000001四个字节为server\_id；0x00000067四个字节是event_length，对应的十进制就是103；  
0x0000006b四个字节是next_position，即下一个Event的开始位置为107；ox0001两个字节是flags；header总计19字节。  
data总字节数=103-19即84字节，排除掉前面的57个字节，剩余27字节表示post-header lengths for all event types；  
post-header lengths：从START\_EVENT\_V3开始到第27个Event，每个Event的fixed part lengths；  
Format\_desc\_Event位置是15，可以在图中找到15的位置是0x54，对应十进制是84，即fixed part lengths=84，而这个值刚好是57+27=84，所以Format\_desc\_Event不存在variable part；

参考：[https://dev.mysql.com/doc/internals/en/binary-log-versions.html](https://dev.mysql.com/doc/internals/en/binary-log-versions.html)

2.2Query_Event

以下的create table产生的Query_Event的十六进制代码：  
![](https://static.oschina.net/uploads/space/2017/1110/153525_FY8f_159239.png)

header共19字节，0x02一个字节表示type\_code（Query\_Event=2）;0x00000101四个字节是event\_length，对应的十进制就是257（pos=107，End\_log_pos=364）;  
Query_Event在post-header的第二个位置0x0d，所有fix part lengths=13；  
variable part=257-19-13=225字节  
具体fix data和variable data：

```
+==============================================================+
| fix    | The ID of the thread                      19 : 4    | 
| data   +-----------------------------------------------------+
|        | The time in seconds                       23 : 4    |
|        +-----------------------------------------------------+
|        | The length of the name of the database    27 : 1    |
|        +-----------------------------------------------------+
|        | The error code                            28 : 2    |
|        +-----------------------------------------------------+
|        | The length of the status variable block   30 : 2    | 
+==============================================================+
```

在创建表产生一个Query\_Event，insert、update以及delete执行之后分别产生了2个Query\_Event和一个Xid_Event。

更多详细：[https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html](https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html)

2.3Xid_Event

以下的更新数据产生的Xid_Event的十六进制代码：

![](https://static.oschina.net/uploads/space/2017/1110/153556_EYkZ_159239.jpg)

header共19字节，0x10一个字节表示type\_code（XID\_EVENT=16）;0x0000001b四个字节是event\_length，对应的十进制就是27（pos=536，End\_log_pos=563）;  
2Xid_Event在post-header的第十六个位置0x00，所有fix part lengths=0；  
variable part=27-19=8字节  
8字节：The XID transaction number。

insert、update以及delete执行之后分别产生了Xid_Event，事务提交产生的事件。

更多详细：[https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html](https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html)

**总结**  
本文主要对Mysql Binlog做了一个大体的介绍，包括：Binlog的参数，格式以及最重要的事件；事件数量比较多，从最简单的增删改查入手，介绍了几个比较常见的事件；  
后续会继续学习其他事件，对Binlog有更加详细的了解。

**参考：**

[https://dev.mysql.com/doc/internals/en/binary-log.html](https://dev.mysql.com/doc/internals/en/binary-log.html)

**个人博客：[codingo.xyz](http://codingo.xyz/)**