## **前言**

数据库锁定机制简单来说就是数据库为了保证数据的一致性而使各种共享资源在被并发访问访问变得有序所设计的一种规则；对于任何一种数据库来说都需要有相应的锁定机制，Mysql也不例外。

## **Mysql几种锁定机制类型**

MySQL 各存储引擎使用了三种类型（级别）的锁定机制：行级锁定，页级锁定和表级锁定。

### 1.行级锁定

锁定对象的颗粒度很小，只对当前行进行锁定，所以发生锁定资源争用的概率也最小，能够给予应用程序尽可能大的并发处理能力；弊端就是获取锁释放锁更加频繁，系统消耗更大，同时行级锁定也最容易发生死锁；  
行级锁定的主要是Innodb存储引擎和NDB Cluster存储引擎；

### 2.页级锁定

锁定颗粒度介于行级锁定与表级锁之间，每页有多行数据，并发处理能力以及获取锁定所需要的资源开销在两者之间；  
页级锁定主要是BerkeleyDB 存储引擎；

### 3.表级锁定

一次会将整张表锁定，该锁定机制最大的特点是实现逻辑非常简单，带来的系统负面影响最小，而且可以避免死锁问题；弊端就是锁定资源争用的概率最高，并发处理能力最低；  
使用表级锁定的主要是MyISAM，Memory，CSV等一些非事务性存储引擎。

本文重点介绍Innodb存储引擎使用的行级锁定；

## **两段锁协议(2PL)**

两段锁协议规定所有的事务应遵守的规则：  
1.在对任何数据进行读、写操作之前，首先要申请并获得对该数据的封锁；  
2.在释放一个封锁之后，事务不再申请和获得其它任何封锁；

即事务的执行分为两个阶段：  
第一阶段是获得封锁的阶段，称为扩展阶段；第二阶段是释放封锁的阶段，称为收缩阶段；

```
begin;
insert ...   加锁1
update ...   加锁2
commit;      事务提交时，释放锁1，锁2
```

如果在加锁2的时候，加锁不成功，则进入等待状态，直到加锁成功才继续执行；  
如果有另外一个事务获取锁的时候顺序刚好相反，是有可能导致死锁的；为此有了一次性封锁法，要求事务必须一次性将所有要使用的数据全部加锁，否则就不能继续执行；

**定理：若所有事务均遵守两段锁协议，则这些事务的所有交叉调度都是可串行化的（串行化很重要，尤其是在数据恢复和备份的时候）；**

## **行级锁定（悲观锁）**

### 1.共享锁和排他锁

Innodb的行级锁定同样分为两种类型：共享锁和排他锁；  
**共享锁**：当一个事务获得共享锁之后，它只可以进行读操作，所以共享锁也叫读锁，多个事务可以同时获得某一行数据的共享锁；  
**排他锁**：而当一个事务获得一行数据的排他锁时，就可以对该行数据进行读和写操作，所以排他锁也叫写锁，排他锁与共享锁和其他的排他锁不兼容；

既然数据库提供了共享锁和排他锁，那具体用在什么地方：  
1.1在数据库操作中，为了有效保证并发读取数据的正确性，提出的事务隔离级别，隔离级别就使用了锁机制；  
1.2提供了相关的SQL，可以方便的在程序中使用；

### 2.事务隔离级别和锁的关系

数据库隔离级别：未提交读(Read uncommitted)，已提交读(Read committed)，可重复读(Repeatable read)和可串行化(Serializable)；  
未提交读(Read uncommitted)：可能读取到其他会话中未提交事务修改的数据，会出现**脏读(Dirty Read)**；  
已提交读(Read committed)：只能读取到已经提交的数据，会出现**不可重复读(NonRepeatable Read)**；  
可重复读(Repeatable read)：InnoDB默认级别，不会出现不可重复读(NonRepeatable Read)，但是会出现**幻读(Phantom Read)**;  
可串行化(Serializable)：强制事务排序，使之不可能相互冲突，从而解决幻读问题，使用表级共享锁，读写相互都会阻塞；

常用的2种隔离级别是：**已提交读(Read committed)**和**可重复读(Repeatable read)**；

### 3.已提交读

#### 3.1准备测试表

```
CREATE TABLE `test_lock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8
 
mysql> insert into test_lock values(null,'zhaohui',1);
mysql> insert into test_lock values(null,'zhaohui2',2);
```

#### 3.2查看和设置隔离级别

```
mysql> SELECT @@tx_isolation;
+-----------------+
| @@tx_isolation  |
+-----------------+
| REPEATABLE-READ |
+-----------------+
1 row in set
 
mysql> set session transaction isolation level read committed;
Query OK, 0 rows affected
 
mysql> SELECT @@tx_isolation;
+----------------+
| @@tx_isolation |
+----------------+
| READ-COMMITTED |
+----------------+
```

#### 3.3模拟多个事务交叉执行

Session1执行查询

```
mysql> begin;
Query OK, 0 rows affected
mysql> select * from test_lock where id=1;
+----+---------+------+
| id | name    | type |
+----+---------+------+
|  1 | zhaohui |    1 |
+----+---------+------+
1 row in set
```

Session2更新数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> update test_lock set name='zhaohui_new' where id=1;
  
Query OK, 1 row affected
Rows matched: 1  Changed: 1  Warnings: 0
mysql> commit;
Query OK, 0 rows affected
```

Session1执行查询

```
mysql> select * from test_lock where id=1;
+----+-------------+------+
| id | name        | type |
+----+-------------+------+
|  1 | zhaohui_new |    1 |
+----+-------------+------+
1 row in set
 
mysql> commit;
Query OK, 0 rows affected
```

Session1中出现了不可重复读(NonRepeatable Read)，也就是在查询的时候没有锁住相关的数据，导致出现了不可重复读，但是写入、修改和删除数据还是加锁了，如下所示：

Session1更新数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> update test_lock set name='zhaohui_new2' where id=1;
Query OK, 1 row affected
Rows matched: 1  Changed: 1  Warnings: 0
```

Session2更新数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> update test_lock set name='zhaohui_new3' where id=1;
1205 - Lock wait timeout exceeded; try restarting transaction
```

Session2更新在更新同一条数据的时候超时了，在更新数据的时候添加了排他锁；

### 4.可重复读

#### 4.1查看和设置隔离级别

```
mysql> set session transaction isolation level repeatable read;
Query OK, 0 rows affected
 
mysql> SELECT @@tx_isolation;
+-----------------+
| @@tx_isolation  |
+-----------------+
| REPEATABLE-READ |
+-----------------+
1 row in set
```

#### 4.2模拟多个事务交叉执行

Session1执行查询

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> select * from test_lock where type=2;
+----+----------+------+
| id | name     | type |
+----+----------+------+
|  2 | zhaohui2 |    2 |
+----+----------+------+
1 row in set
```

Session2更新数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> update test_lock set name='zhaohui2_new' where type=2;
Query OK, 1 row affected
Rows matched: 1  Changed: 1  Warnings: 0
 
mysql> commit;
Query OK, 0 rows affected
```

Session1执行查询

```
mysql> select * from test_lock where type=2;
+----+----------+------+
| id | name     | type |
+----+----------+------+
|  2 | zhaohui2 |    2 |
+----+----------+------+
1 row in set
```

可以发现2次查询的数据结果是一样的，实现了可重复读(Repeatable read)，再来看一下是否有幻读(Phantom Read)的问题；

Session3插入数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> insert into test_lock values(null,'zhaohui3',2);
Query OK, 1 row affected
 
mysql> commit;
Query OK, 0 rows affected
```

Session1执行查询

```
mysql> select * from test_lock where type=2;
+----+----------+------+
| id | name     | type |
+----+----------+------+
|  2 | zhaohui2 |    2 |
+----+----------+------+
1 row in set
```

可以发现可重复读(Repeatable read)隔离级别下，也不会出现幻读的现象；

**分析一下原因**：如何通过悲观锁的方式去实现可重复读和不出现幻读的现象，对读取的数据加共享锁，对同样的数据执行更新操作就只能等待，这样就可以保证可重复读，但是对于不出现幻读的现象无法通过锁定行数据来解决；  
最终看到的现象是没有幻读的问题，同时如果对读取的数据加共享锁，更新相同数据应该会等待，上面的实例中并没有出现等待，所以mysql内部应该还有其他锁机制--MVCC机制；

### 5.悲观锁SQL使用

#### 5.1共享锁使用(lock in share mode)

Session1查询数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> select * from test_lock where type=2 lock in share mode;
+----+--------------+------+
| id | name         | type |
+----+--------------+------+
|  2 | zhaohui2_new |    2 |
|  3 | zhaohui3     |    2 |
+----+--------------+------+
2 rows in set
```

Session2查询数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> select * from test_lock where type=2 lock in share mode;
+----+--------------+------+
| id | name         | type |
+----+--------------+------+
|  2 | zhaohui2_new |    2 |
|  3 | zhaohui3     |    2 |
+----+--------------+------+
2 rows in set
```

Session3更新数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> update test_lock set name='zhaohui3_new' where id=3;
1205 - Lock wait timeout exceeded; try restarting transaction
```

Session1和Session2使用了共享锁，所以可以存在多个，并不冲突，但是Session3更新操作需要加上排他锁，和共享锁不能同时存在；

#### 5.2排他锁使用(for update)

Session1查询数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> select * from test_lock where type=2 for update;
+----+--------------+------+
| id | name         | type |
+----+--------------+------+
|  2 | zhaohui2_new |    2 |
|  3 | zhaohui3     |    2 |
+----+--------------+------+
2 rows in set
```

Session2查询数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> select * from test_lock where type=2 for update;
Empty set
```

Session3更新数据

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> update test_lock set name='zhaohui3_new' where id=3;
1205 - Lock wait timeout exceeded; try restarting transaction
```

排他锁只能有一个同时存在，所有Session2和Session3都将等等超时；

## **多版本并发控制MVCC**

**多版本并发控制(Multiversion Concurrency Control)**：每一个写操作都会创建一个新版本的数据，读操作会从有限多个版本的数据中挑选一个最合适的结果直接返回；读写操作之间的冲突就不再需要被关注，而管理和快速挑选数据的版本就成了MVCC需要解决的主要问题。  
为什么要引入此机制，首先通过悲观锁来处理读请求是很耗性能的，其次数据库的事务大都是只读的，读请求是写请求的很多倍，最后如果没有并发控制机制，最坏的情况也是读请求读到了已经写入的数据，这对很多应用完全是可以接受的；

再来看一下可重复读(Repeatable read)现象，通过MVCC机制读操作只读该事务开始前的数据库的快照(snapshot), 这样在读操作不用阻塞写操作，写操作不用阻塞读操作的同时，避免了脏读和不可重复读;

当然并不是说悲观锁就没有用了，在数据更新的时候数据库默认还是使用悲观锁的，所以MVCC是可以整合起来一起使用的(MVCC+2PL)，用来解决读-写冲突的无锁并发控制；  
MVCC使用快照读的方式，解决了不可重复读和幻读的问题，如上面的实例所示：select查询的一直是快照信息，不需要添加任何锁；  
以上实例中使用的select方式把它称为**快照读(snapshot read)**，其实事务的隔离级别的读还有另一层含义：读取数据库当前版本数据–**当前读(current read)**；

## **当前读和Gap锁**

区别普通的select查询，当前读对应的sql包括：

```
select ...for update,
select ...lock in share mode,
insert,update,delete;
```

以上sql本身会加悲观锁，所以不存在不可重复读的问题，剩下的就是幻读的问题；  
Session1执行当前读

```
mysql> select * from test_lock where type=2 for update;
+----+----------------+------+
| id | name           | type |
+----+----------------+------+
|  2 | zhaohui2_new   |    2 |
|  3 | zhaohui3_new_1 |    2 |
+----+----------------+------+
2 rows in set
```

Session2执行插入

```
mysql> begin;
Query OK, 0 rows affected
 
mysql> insert into test_lock values(null,'zhaohui_001',1);
1205 - Lock wait timeout exceeded; try restarting transaction
```

为什么明明锁住的是type=2的数据，当插入type=1也会锁等待，因为InnoDB对于行的查询都是采用了Next-Key锁，锁定的不是单个值，而是一个范围(GAP);  
如果当前type类型包括：1，2，4，6，8，10锁住type=2，那么type=1，2，3会被锁住，后面的不会，锁住的是一个区间；这样也就保证了当前读也不会出现幻读的现象；  
**注：type字段添加了索引，如果没有添加索引，gap锁会锁住整张表；**

## **乐观锁**

乐观锁是一种思想，认为事务间争用没有那么多，和悲观锁是相对的，乐观锁在java的并发包中大量的使用；一般采用以下方式：使用版本号(version)机制来实现，版本号就是为数据添加一个版本标志，一般在表中添加一个version字段；当读取数据的时候把version也取出来，然后version+1，更新数据库的时候对比第一次取出来的version和数据库里面的version是否一致，如果一致则更新成功，否则失败进入重试，具体使用大致如下：

```
begin;
select id,name,version from test_lock where id=1;
....
update test_lock set name='xxx',version=version+1 where id=1 and version=${version};
commit;
```

先查询后更新，需要保证原子性，要么使用悲观锁的方式，对整个事务加锁；要么使用乐观锁的方式，如果在读多写少的系统中，乐观锁性能更好；

## **总结**

本文首先从Mysql的悲观锁出发，然后介绍了悲观锁和事务隔离级别之间的关系，并分析为什么没有使用悲观锁来实现隔离级别；然后从问题出发分别介绍了MVCC和Gap锁是如何解决了不可重复读的问题和幻读的问题；最后介绍了乐观锁经常被用在读数据远大于写数据的系统中。