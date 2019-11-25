**系列文章**

[MySql Binlog初识](https://my.oschina.net/OutOfMemory/blog/1571107)

[MySql Binlog事件介绍篇](https://my.oschina.net/OutOfMemory/blog/1572968)

[MySql Binlog事件数据篇](https://my.oschina.net/OutOfMemory/blog/1579454)

[Mysql通讯协议分析](https://my.oschina.net/OutOfMemory/blog/1595684)

[基于Netty模拟解析Binlog](https://my.oschina.net/OutOfMemory/blog/1605201)

**前言**  
前两篇文章[MySql Binlog初识](https://my.oschina.net/OutOfMemory/blog/1571107)和[MySql Binlog事件介绍篇](https://my.oschina.net/OutOfMemory/blog/1572968)分别从Binlog入门和Binlog事件如何产生的两个角度来介绍Binlog，本文将从Binlog事件的数据来更深入的了解Binlog。

**Binlog事件数据**  
1.QUERY_EVENT  
执行更新语句时会生成此事件，包括：create，insert，update，delete；  
Fixed data part，总长度13字节：  
4字节：执行sql的线程id；  
4字节：执行sql的时间；  
1字节：数据库名称的长度；  
2字节：执行sql产生的错误码；  
2字节：状态变量的长度，具体内容在Variable part；

Variable part：  
可变字节：状态变量，每个状态变量key为一个字节，后面跟着value，不同的key对应不同长度的value，但是总长度在Fixed data part中已经定义；  
可变字节：数据库名称  
可变字节：sql语句，通过事件的总长度-header长度-Fixed data-状态变量，剩余的字节数组通过utf-8编码即可获取；

2.STOP_EVENT  
当mysqld停止时生成此事件；  
Fixed data part:空的

Variable part：空的

3.ROTATE_EVENT  
当mysqld切换到新的binlog文件生成此事件；  
Fixed data part，总长度8字节：  
8字节：下一个binlog文件的第一个事件的position，这个值一直是4，因为魔数占用了4字节；

Variable data part:  
可变字节：下一个binlog的名称，它的长度=事件总长度-header长度-Fixed data

4.INTVAR_EVENT  
当sql语句中使用了AUTO\_INCREMENT的字段或者LAST\_INSERT_ID()函数；  
Fixed data part：空的

Variable part：  
1字节：一个变量类型的值：LAST\_INSERT\_ID\_EVENT = 1 或者 INSERT\_ID_EVENT = 2；  
8字节：LAST\_INSERT\_ID()函数调用，或者AUTO_INCREMENT字段生成的一个无符号的整型；

5.RAND_EVENT

```
| bin-log.000003 |  438 | RAND               |         1 |         473 | rand_seed1=223769196,rand_seed2=1013907192
```

执行包含RAND()函数的语句产生此事件，此事件没有被用在binlog_format为ROW模式的情况下；  
Fixed data part：空的

Variable part：  
8字节：第一个种子值（ex:rand_seed1=223769196）  
8字节：第二个种子值（ex:rand_seed2=1013907192）

6.USER\_VAR\_EVENT

```
| bin-log.000003 |  711 | User var           |         1 |         756 | @`age`=50
```

执行包含了用户变量的语句产生此事件，此事件没有被用在binlog_format为ROW模式的情况下；  
Fixed data part：空的

Variable part：  
4字节：用户变量名的大小；  
可变字节：用户变量名，具体长度上一个4字节的数据指定了；  
1字节：如果是变量值是NULL，那么此值是非0的；如果是此值是0，那么才有接下来的其他数据；应该是对有空值情况的一种优化；  
1字节：用户变量类型，包括：(STRING\_RESULT=0, REAL\_RESULT=1, INT\_RESULT=2, ROW\_RESULT=3, DECIMAL_RESULT=4)；  
4字节：用户变量字符的数量；  
4字节：用户变量值的长度；  
可变字节：变量的值，通过变量类型和变量值的长度，可以解析出具体的变量值；

7.FORMAT\_DESCRIPTION\_EVENT

```
| bin-log.000003 |    4 | Format_desc        |         1 |         107 | Server ver: 5.5.29-log, Binlog ver: 4
```

描述事件，被写在每个binlog文件的开始位置，用在MySQL5.0以后的版本中，代替了START\_EVENT\_V3;  
Fixed data part：  
2字节：binlog版本，Mysql5.0以及以上的版本值为：4  
50字节：Mysql Server版本；  
4字节：事件创建的时间戳；  
1字节：header的长度，binlog版本为4的情况下header长度是19；  
可变字节：从START\_EVENT\_V3开始到第27个Event，每个Event的fixed part lengths，每个事件一个字节，总共27个字节；

Variable part：空的

8.XID_EVENT

```
| bin-log.000003 |  315 | Xid                |         1 |         342 | COMMIT /* xid=32 */
```

事务提交产生的XID_EVENT事件；  
Fixed data part：空的

Variable part：  
8字节：事务编号；

9.BEGIN\_LOAD\_QUERY_EVENT

```
| bin-log.000003 |  964 | Begin_load_query   |         1 |        1008 | ;file_id=3;block_len=21
```

执行LOAD DATA INFILE 语句时产生此事件  
Fixed data part：  
4字节：加载Data File的ID，防止加载的Data File内容是相同的；

Variable part：  
加载数据的第一个块，如果文件大小超过某个阀值，后面会有多个APPEND\_BLOCK\_EVENT事件，每一个包含一个数据块；可变字节长度 = 事件的总长度 – header长度 – Fixed data；因为测试数据量比较少（999, 101, ‘zhaohui’）总共就21个字节，所以一个块足够了；

10.EXECUTE\_LOAD\_QUERY_EVENT

```
| bin-log.000003 | 1008 | Execute_load_query |         1 |        1237 | use `test`; LOAD DATA INFILE 'D:/btest.sql' INTO TABLE `btest` FIELDS TERMINATED BY ',' ENCLOSED BY '' ESCAPED BY '\\' LINES TERMINATED BY '\n' (`id`, `age`, `name`) ;file_id=3 |
```

执行LOAD DATA INFILE产生的事件，类似QUERY\_EVENT事件，Fixed data的前13个字节和QUERY\_EVENT类似；  
Fixed data part：  
4字节：执行sql的线程id；  
4字节：执行sql的时间；  
1字节：数据库名称的长度；  
2字节：执行sql产生的错误码；  
2字节：状态变量的长度，具体内容在Variable part；  
4字节：加载Data File的ID；  
4字节：文件名替换语句中的起始位置；  
4字节：文件名替换语句中的结束位置；  
1字节：如何处理重复数据，三个选项：LOAD\_DUP\_ERROR = 0, LOAD\_DUP\_IGNORE = 1, LOAD\_DUP\_REPLACE = 2

Variable part：  
1.状态变量，每个状态变量key为一个字节，后面跟着value，不同的key对应不同长度的value，但是总长度在Fixed data part中已经定义；  
2.sql语句，通过事件的总长度-header长度-Fixed data-状态变量，剩余的字节数组通过utf-8编码即可获取；

11.TABLE\_MAP\_EVENT

```
| bin-log.000004 |  844 | Table_map   |         1 |         892 | table_id: 33 (test.btest)
```

将表的定义映射到一个数字，在行操作事件之前记录（包括：WRITE\_ROWS\_EVENT，UPDATE\_ROWS\_EVENT，DELETE\_ROWS\_EVENT）；  
Fixed data part：  
6字节：表Id；  
2字节：保留字段为将来使用；

Variable part：  
1字节：数据库名字的长度；  
可变字节：数据库名字，根据前一个字节记录的名字长度，获取的字节数组通过utf-8编码即可获取；  
1字节：表名的长度；  
可变字节：表名，根据前一个字节记录的名字长度，获取的字节数组通过utf-8编码即可获取；  
Packed integer：用来记录表中字段的数量；  
注：Packed integer是一个可变字节的类型，根据数据大小字节大小不一样，  
更多详细：[https://dev.mysql.com/doc/internals/en/event-content-writing-conventions.html](https://dev.mysql.com/doc/internals/en/event-content-writing-conventions.html)  
可变字节：表字段类型数组，每个字段一个字节；  
Packed integer：用来记录表元数据的长度；  
可变字节：元数据块，根据前一个字节记录的名字长度，获取的字节数组通过utf-8编码即可获取；  
可变字节：用位域表示每一个字段是否为null，一个字节有8位，所以N个字段需要(N+7)/8个字节；

12.WRITE\_ROWS\_EVENT，UPDATE\_ROWS\_EVENT和DELETE\_ROWS\_EVENT  
binlog_format为ROW模式下，执行insert，update和delete操作产生的事件；  
Fixed data part：  
6字节：表Id；  
2字节：保留字段为将来使用；

Variable part：  
Packed integer：记录表中字段的数量；  
可变字节：用位域表示每个字段是否被使用（比如只有更新、插入的字段才是被使用的），N个字段需要(N+7)/8个字节；  
可变字节：仅用在UPDATE\_ROWS\_EVENT事件中，用位域表示每个字段更新之后是否被使用（值只有真正被更新了才是被使用的），N个字段需要(N+7)/8个字节；  
接下来是记录的每一行的数据：  
可变字节：当前行中的字段值是否为NULL，只有这个字段被标识为被使用，才会出现在这；  
可变字节：当前行所有字段的值，只有这个字段被标识为被使用，并且值不为NULL才会有值；

13.INCIDENT_EVENT  
主服务器发生了不正常的事件，通知从服务器并告知可能会导致数据处于不一致的状态；  
Fixed data part：  
1字节：不正常事件的编号；  
1字节：消息的长度；

Variable part：  
消息的内容，根据Fixed data part中指定的消息长度读取消息的内容；

14.HEARTBEAT\_LOG\_EVENT  
主服务器告诉从服务器，主服务器还活着，不写入到日志文件中；  
Fixed data part：空的

Variable part：空的

更多参考：[https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html](https://dev.mysql.com/doc/internals/en/event-data-for-specific-event-types.html)

**Java读取简单实例**  
1.创建表，并插入数据，产生binlog日志文件；

2.查看binlog中的事件；

```
mysql> show binlog events in 'bin-log.000001';
+----------------+-----+-------------+-----------+-------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Log_name       | Pos | Event_type  | Server_id | End_log_pos | Info                                                                                                                                                                                                           |
+----------------+-----+-------------+-----------+-------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| bin-log.000001 |   4 | Format_desc |         1 |         107 | Server ver: 5.5.29-log, Binlog ver: 4                                                                                                                                                                          |
| bin-log.000001 | 107 | Query       |         1 |         364 | use `test`; CREATE TABLE `btest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `age` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 |
| bin-log.000001 | 364 | Query       |         1 |         432 | BEGIN                                                                                                                                                                                                          |
| bin-log.000001 | 432 | Query       |         1 |         536 | use `test`; insert into btest values(1,100,'zhaohui')                                                                                                                                                          |
| bin-log.000001 | 536 | Xid         |         1 |         563 | COMMIT /* xid=30 */                                                                                                                                                                                            |
| bin-log.000001 | 563 | Stop        |         1 |         582 |                                                                                                                                                                                                                |
+----------------+-----+-------------+-----------+-------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

```

3.通过java代码来读取binlog日志，具体代码如下：

```java
public class BinlogRead {
 
    private static RandomAccessFile file;
    /** 魔数的字节长度 **/
    private static final int MAGIN_LEN = 4;
    /** 事件header长度 **/
    private static final int EVENT_HEADER_LEN = 19;
    /** Query_Event fix data长度 **/
    private static final int QUERY_EVENT_FIX_LEN = 13;
 
    public static void main(String[] args) throws Exception {
        file = new RandomAccessFile(new File("D://bin-log.000001"), "rw");
        FileChannel channel = file.getChannel();
 
        /** 1.魔数4字节 **/
        ByteBuffer magic = ByteBuffer.allocate(MAGIN_LEN);
        channel.read(magic);
 
        /** 2.Format_desc_Event事件 **/
        EventHeader header = getEventHeader(channel);
        channel.position(header.getEventLen() + MAGIN_LEN);
 
        /** 3.Query_Event事件 **/
        header = getEventHeader(channel);
        System.out.println(getQueryEventSql(header.getEventLen(), channel));
 
        /** 4.Query_Event事件 **/
        header = getEventHeader(channel);
        System.out.println(getQueryEventSql(header.getEventLen(), channel));
 
        /** 5.Query_Event事件 **/
        header = getEventHeader(channel);
        System.out.println(getQueryEventSql(header.getEventLen(), channel));
 
        /** 6.Xid_Event事件 **/
        header = getEventHeader(channel);
        ByteBuffer xidNumber = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        channel.read(xidNumber);
        xidNumber.flip();
        System.out.println("xidNumber = " + xidNumber.getLong());
 
        /** 7.Stop_Event事件 **/
        header = getEventHeader(channel);
 
    }
 
    /**
     * 获取事件Header信息
     * 
     * @param channel
     * @return
     * @throws IOException
     */
    private static EventHeader getEventHeader(FileChannel channel) throws IOException {
        ByteBuffer formatDescEventHeader = ByteBuffer.allocate(EVENT_HEADER_LEN).order(ByteOrder.LITTLE_ENDIAN);
        channel.read(formatDescEventHeader);
        formatDescEventHeader.flip();
        EventHeader header = new EventHeader();
        header.setTimestamp(formatDescEventHeader.getInt());
        header.setTypeCode(formatDescEventHeader.get());
        header.setServerId(formatDescEventHeader.getInt());
        header.setEventLen(formatDescEventHeader.getInt());
        header.setNextPosition(formatDescEventHeader.getInt());
        header.setFlags(formatDescEventHeader.getShort());
        System.out.println(header.toString());
        return header;
    }
 
    /**
     * 获取Query Event sql语句
     * 
     * @param queryEventLen
     * @param channel
     * @return
     * @throws IOException
     */
    private static String getQueryEventSql(int queryEventLen, FileChannel channel) throws IOException {
        /** Query_Event fix data **/
        ByteBuffer queryEventFix = ByteBuffer.allocate(QUERY_EVENT_FIX_LEN).order(ByteOrder.LITTLE_ENDIAN);
        channel.read(queryEventFix);
        queryEventFix.flip();
        queryEventFix.position(11);
 
        /** 状态变量的长度 **/
        int statusLen = queryEventFix.getShort();
 
        int queryEventValLen = queryEventLen - EVENT_HEADER_LEN - QUERY_EVENT_FIX_LEN;
        ByteBuffer queryEventVal = ByteBuffer.allocate(queryEventValLen).order(ByteOrder.LITTLE_ENDIAN);
        channel.read(queryEventVal);
        queryEventVal.flip();
        queryEventVal.position(statusLen);
 
        /** 数据库名称 **/
        queryEventVal.mark();
        int length = 0;
        while ('\0' != queryEventVal.get()) {
            length++;
        }
        queryEventVal.reset();
        byte dbName[] = new byte[length];
        queryEventVal.get(dbName);
        System.out.println("db name : " + new String(dbName, "utf-8"));
 
        /** sql语句 **/
        byte sql[] = new byte[queryEventValLen - statusLen - length - 1];
        queryEventVal.get(sql);
        return new String(sql, "utf-8");
    }
}
```

```java
public class EventHeader {
 
    private int timestamp;
    private byte typeCode;
    private int serverId;
    private int eventLen;
    private int nextPosition;
    private int flags;
 
    @Override
    public String toString() {
        return "EventHeader [timestamp=" + timestamp + ", typeCode=" + typeCode + ", serverId=" + serverId
                + ", eventLen=" + eventLen + ", nextPosition=" + nextPosition + ", flags=" + flags + "]";
    }
         
        //...get/set方法省略...
}
```

**总结**  
本文对事件的数据格式做了详细的介绍，因为所有事件的event header部分都是一样的，所以文中主要介绍的event data部分，event data主要包括两个部分：Fixed data part和Variable part；最后通过一个简单实例来大致了解事件数据的读取方式，后续会提供更详细的binlog事件数据读取。