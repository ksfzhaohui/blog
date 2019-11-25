**系列文章**

[MySql Binlog初识](https://my.oschina.net/OutOfMemory/blog/1571107)

[MySql Binlog事件介绍篇](https://my.oschina.net/OutOfMemory/blog/1572968)

[MySql Binlog事件数据篇](https://my.oschina.net/OutOfMemory/blog/1579454)

[Mysql通讯协议分析](https://my.oschina.net/OutOfMemory/blog/1595684)

[基于Netty模拟解析Binlog](https://my.oschina.net/OutOfMemory/blog/1605201)

**1.Mysql的连接方式**  
要了解Mysql的通讯协议，首先需要知道是以哪种连接方式去连接Mysql服务器的；Mysql的主要连接方式包括：Unix套接字，内存共享，命名管道，TCP/IP套接字等。

1.1Unix套接字  
在Linux和Unix环境下，可以使用Unix套接字进行Mysql服务器的连接；Unix套接字其实不是一个网络协议，只能在客户端和Mysql服务器在同一台电脑上才可以使用，使用方式也很简单：

```bash
root@root ~]# mysql -uroot -proot
mysql> show variables like 'socket';
+---------------+---------------------------+
| Variable_name | Value                     |
+---------------+---------------------------+
| socket        | /var/lib/mysql/mysql.sock |
+---------------+---------------------------+
1 row in set (0.00 sec)
```

以上命令查询Unix套接字文件的位置；

1.2命名管道和内存共享  
在window系统中客户端和Mysql服务器在同一台电脑上，可以使用命名管道和共享内存的方式，  
命名管道开启：–enable-named-pipe=on/off;  
共享内存开启：–shared-memory=on/off；

1.3TCP/IP套接字  
在任何系统下都可以使用的方式，也是使用最多的连接方式，本文要介绍的通讯协议也是基于此连接方式的，下面通过tcpdump对TCP/IP套接字有一个初步的了解：  
服务器端：

```bash
[root@root ~]# tcpdump port 3306
tcpdump: verbose output suppressed, use -v or -vv for full protocol decode
listening on venet0, link-type LINUX_SLL (Linux cooked), capture size 65535 bytes

```

服务器端监听3306端口(也就是Mysql的端口)；

客户端：

```bash
C:\Users\hui.zhao>mysql -h64.xxx.xxx.xxx -uroot -proot
mysql> exit
Bye
```

客户端连接服务器，然后断开连接，这时候观察服务器的监听结果日志：

```bash
[root@root ~]# tcpdump port 3306
tcpdump: verbose output suppressed, use -v or -vv for full protocol decode
listening on venet0, link-type LINUX_SLL (Linux cooked), capture size 65535 bytes
 
02:06:25.442472 IP 153.3.251.202.33876 > root.mysql: Flags [S], seq 27289263, win 8192, options [mss 1460,nop,wscale 8,nop,nop,sackOK], length 0
02:06:25.442763 IP root.mysql > 153.3.251.202.33876: Flags [S.], seq 2014324548, ack 27289264, win 14600, options [mss 1460,nop,nop,sackOK,nop,wscale 7], length 0
02:06:25.617449 IP 153.3.251.202.33876 > root.mysql: Flags [.], ack 1, win 256, length 0
02:06:29.812946 IP root.mysql > 153.3.251.202.33876: Flags [P.], seq 1:57, ack 1, win 115, length 56
02:06:29.992362 IP 153.3.251.202.33876 > root.mysql: Flags [P.], seq 1:63, ack 57, win 256, length 62
02:06:29.992411 IP root.mysql > 153.3.251.202.33876: Flags [.], ack 63, win 115, length 0
02:06:29.992474 IP root.mysql > 153.3.251.202.33876: Flags [P.], seq 57:68, ack 63, win 115, length 11
02:06:30.166992 IP 153.3.251.202.33876 > root.mysql: Flags [P.], seq 63:100, ack 68, win 256, length 37
02:06:30.167109 IP root.mysql > 153.3.251.202.33876: Flags [P.], seq 68:158, ack 100, win 115, length 90
02:06:30.536298 IP 153.3.251.202.33876 > root.mysql: Flags [.], ack 158, win 256, length 0
02:06:34.568611 IP 153.3.251.202.33876 > root.mysql: Flags [P.], seq 100:105, ack 158, win 256, length 5
02:06:34.568620 IP 153.3.251.202.33876 > root.mysql: Flags [F.], seq 105, ack 158, win 256, length 0
02:06:34.568751 IP root.mysql > 153.3.251.202.33876: Flags [F.], seq 158, ack 106, win 115, length 0
02:06:34.743815 IP 153.3.251.202.33876 > root.mysql: Flags [.], ack 159, win 256, length 0

```

\[S\]:SYN发起连接标志,\[P\]:PUSH传送数据标志,\[F\]:FIN关闭连接标志,\[.\]:表示确认包;  
可以大致看出流程：建立tcp连接，客户端和Mysql服务器建立连接通讯，关闭tcp连接；  
\[S\]\[S.\]\[.\]这几个数据包表示tcp连接的三次握手；  
\[F.\]\[F.\]\[.\]这几个数据包表示tcp连接的四次挥手；  
中间的多个\[P.\]\[.\]其实就是客户端和Mysql服务器建立连接发送的协议数据包。

**2.协议分析**  
Mysql协议被用在Mysql Clients和Mysql Server通讯的时候，具体有以下几个场景：客户端和服务器进行连接，Mysql代理以及主从备份；  
MySQL客户端与服务器的交互主要分为两个阶段：Connection Phase（连接阶段或者叫认证阶段）和Command Phase（命令阶段）；  
结合tcpdump的输出，客户端和服务器端通讯的整个流程大致如下：

```bash
1.建立tcp连接三次握手；
2.与Mysql服务器建立连接，即Connection Phase（连接阶段或者叫认证阶段）；
    s->c:发送握手初始化包（a Initial Handshake Packet）
    c->s:发送验证包(authentication response)
    s->c:服务器发送认证结果包
3.认证通过之后，服务器端接受客户端的命令包，发送相应的响应包，即Command Phase（命令阶段）；
4.断开连接请求exit命令；
5.四次挥手tcp断开连接；
```

2.1基本类型  
在整个协议中的基本类型：整数型和字符串型；

2.1.1整数型  
分为两种类型Fixed-Length Integer Types和Length-Encoded Integer Type；  
Fixed-Length Integer Types：  
一个固定长度的无符号整数将其值存储在一系列字节中，具体固定字节数可以是：1，2，3，4，6，8；  
Length-Encoded Integer Type：  
存储需要的字节数取决于数值的大小，具体可参照如下：  
1个字节：0<=X<251；  
2个字节：251<=X<2^16；  
3个字节：2^16<=X<2^24；  
9个字节：2^24<=X<2^64；

2.1.2字符串型  
分为5种类型包括，FixedLengthString，NullTerminatedString，VariableLengthString，LengthEncodedString和RestOfPacketString；  
FixedLengthString：固定长度的字符串具有已知的硬编码长度，一个例子是ERR_Packet的SQL状态，它总是5个字节长；  
NullTerminatedString：以遇到Null(字节为00)结束的字符串；  
VariableLengthString：可变字符串，字符串的长度由另一个字段决定或在运行时计算，比如int+value，int为长度，value为指定长度的字节数；  
LengthEncodedString：以描述字符串长度的长度编码的整数作为前缀的字符串，是VariableLengthString指定的int+value方式；  
RestOfPacketString：如果一个字符串是数据包的最后一个组件，它的长度可以从整个数据包长度减去当前位置来计算；

2.2基本数据包  
如果MySQL客户端或服务器想要发送数据，则：  
每个数据包大小不能超过2^24字节(16MB);  
在每个数据块前面加上一个数据包头；

包格式如下：

```java
int<3>：具体包内容的长度；除去int<3>+int<1>=4字节长度；
int<1>：sequence_id随每个数据包递增，并可能环绕。 它从0开始，在命令阶段开始一个新的命令时重置为0；
string<var>：具体数据内容，也是int<3>指定的长度；
```

例如：  
01 00 00对应int表示具体数据内容的长度为1个字节；  
00对应int表示sequence_id；  
01对应string前面指定的数据内容为1个字节。

2.3报文类型  
可以分成三个大类：登录认证报文，客户端请求报文以及服务器端返回报，基于mysql5.1.73(mysql4.1以后的版本)

2.3.1登录认证报文  
主要在交互的认证阶段，由上文中可以知道一共分为三个阶段：Handshake Packet，authentication response以及结果包，这里主要分析前两个包；

2.3.1.1 Handshake Packet

```java
1字节：协议版本号
NullTerminatedString：数据库版本信息
4字节：连接MySQL Server启动的线程ID
8字节：挑战随机数，用于数据库认证
1字节：填充值(0x00)
2字节：用于与客户端协商通讯方式
1字节：数据库的编码
2字节：服务器状态
13字节：预留字节
12字节：挑战随机数，用于数据库认证
1字节：填充值(0x00)
```

使用tcpdump进行监听，输出十六进制日志如下：

```bash
[root@root ~]# tcpdump port 3306 -X
......
03:20:34.299521 IP root.mysql > 153.3.251.202.44658: Flags [P.], seq 1:57, ack 1, win 115, length 56
    0x0000:  4508 0060 09f1 4000 4006 c666 43da 9190  E..`..@.@..fC...
    0x0010:  9903 fbca 0cea ae72 bb4e 25ba 21e7 27e3  .......r.N%.!.'.
    0x0020:  5018 0073 b1e0 0000 3400 0000 0a35 2e31  P..s....4....5.1
    0x0030:  2e37 3300 4024 0000 5157 4222 252f 5f6f  .73.@$..QWB"%/_o
    0x0040:  00ff f708 0200 0000 0000 0000 0000 0000  ................
    0x0050:  0000 0032 4a5d 7553 7e45 784f 627e 7400  ...2J]uS~ExOb~t.
```

包的总长度是56，减去int<3>+int<1>4字节=52字节，对应的十六进制就是34；int<3>十六进制为3400 00表示包内容长度，int<1>十六进制为00表示sequence_id；后续的内容就是包体内容共52字节，0a对应的十进制是10，所有协议号版本是10；后续的数据库版本信息遇到00结束，35 2e31 2e37 33对应的5.1.73，正是当前使用的数据库版本；4024 0000对应十进制是6436；08表示数据库的编码；0200表示服务器状态；后续的13对00为预留字节；最后的13个字节是挑战随机数和填充值。

2.3.1.2 Authentication Packet

```java
4字节：用于与客户端协商通讯方式
4字节：客户端发送请求报文时所支持的最大消息长度值
1字节：标识通讯过程中使用的字符编码
23字节：保留字节
NullTerminatedString：用户名
LengthEncodedString：加密后的密码
NullTerminatedString：数据库名称（可选）
```

使用tcpdump进行监听，输出十六进制日志如下：

```bash
03:20:34.587416 IP 153.3.251.202.44658 > root.mysql: Flags [P.], seq 1:63, ack 57, win 256, length 62
    0x0000:  4500 0066 29ee 4000 7006 766b 9903 fbca  E..f).@.p.vk....
    0x0010:  43da 9190 ae72 0cea 21e7 27e3 bb4e 25f2  C....r..!.'..N%.
    0x0020:  5018 0100 d8d2 0000 3a00 0001 85a6 0f00  P.......:.......
    0x0030:  0000 0001 2100 0000 0000 0000 0000 0000  ....!...........
    0x0040:  0000 0000 0000 0000 0000 0000 726f 6f74  ............root
    0x0050:  0014 ff58 4bd2 7946 91a0 a233 f2c1 28af  ...XK.yF...3..(.
    0x0060:  d578 0762 c2e8                           .x.b..
```

包的总长度是62，减去int<3>+int<1>4字节=58字节，对应的十六进制就是3a；int<3>十六进制为3a00 00表示包内容长度；int<1>十六进制为01表示sequence_id；726f 6f74 00是用户名解码后是root；后面是加密后的密码类型是LengthEncodedString，14对应的十进制是20，后面20个字节就是加密后的密码；可选的数据库名称不存在。

2.4客户端请求报文

```java
int<1>：执行的命令，比如切换数据库
string<var>：命令相应的参数
```

命令列表：

```java
0x00    COM_SLEEP   （内部线程状态)
0x01    COM_QUIT    关闭连接
0x02    COM_INIT_DB 切换数据库
0x03    COM_QUERY   SQL查询请求
0x04    COM_FIELD_LIST  获取数据表字段信息
0x05    COM_CREATE_DB   创建数据库
0x06    COM_DROP_DB 删除数据库
0x07    COM_REFRESH 清除缓存
0x08    COM_SHUTDOWN    停止服务器
0x09    COM_STATISTICS  获取服务器统计信息
0x0A    COM_PROCESS_INFO    获取当前连接的列表
0x0B    COM_CONNECT （内部线程状态)
0x0C    COM_PROCESS_KILL    中断某个连接
0x0D    COM_DEBUG   保存服务器调试信息
0x0E    COM_PING    测试连通性
0x0F    COM_TIME    （内部线程状态）
0x10    COM_DELAYED_INSERT  （内部线程状态）
0x11    COM_CHANGE_USER 重新登陆（不断连接）
0x12    COM_BINLOG_DUMP 获取二进制日志信息
0x13    COM_TABLE_DUMP  获取数据表结构信息
0x14    COM_CONNECT_OUT （内部线程状态)
0x15    COM_REGISTER_SLAVE  从服务器向主服务器进行注册
0x16    COM_STMT_PREPARE    预处理SQL语句
0x17    COM_STMT_EXECUTE    执行预处理语句
0x18    COM_STMT_SEND_LONG_DATA 发送BLOB类型的数据
0x19    COM_STMT_CLOSE  销毁预处理语句
0x1A    COM_STMT_RESET  清除预处理语句参数缓存
0x1B    COM_SET_OPTION  设置语句选项
0x1C    COM_STMT_FETCH  获取预处理语句的执行结果
```

比如：use test;使用tcpdump进行监听，输出十六进制日志如下：

```bash
22:04:29.379165 IP 153.3.251.202.33826 > root.mysql: Flags [P.], seq 122:131, ack 222, win 64019, length 9
    0x0000:  4500 0031 3f19 4000 7006 6175 9903 fbca  E..1?.@.p.au....
    0x0010:  43da 9190 8422 0cea 42e2 524b 7e18 25c1  C...."..B.RK~.%.
    0x0020:  5018 fa13 a07b 0000 0500 0000 0274 6573  P....{.......tes
    0x0030:  74                                       t
```

包的总长度是9，减去int<3>+int<1>4字节=5字节，对应的十六进制就是05；int<3>十六进制为0500 00表示包内容长度；int<1>十六进制为00表示sequence\_id；02对应COM\_INIT_DB，后面是test的二进制编码；

2.5服务器响应报文  
对于客户端发送给服务器的大多数命令，服务器返回其中一个响应的数据包：OK_Packet，ERR_Packet和EOF_Packet，Result Set；

2.5.1OK_Packet  
表示成功完成一个命令，具体格式如下：

```java
int<1>：0x00或0xFEOK包头
int<lenenc>：受影响行数
int<lenenc>：最后插入的索引ID
int<2>：服务器状态
int<2>：告警计数  注：MySQL 4.1 及之后的版本才有
string<lenenc>：服务器消息(可选)
```

use test;服务器返回的包，使用tcpdump进行监听，输出十六进制日志如下：

```bash
22:04:29.379308 IP root.mysql > 153.3.251.202.33826: Flags [P.], seq 222:233, ack 131, win 14600, length 11
    0x0000:  4508 0033 4a0a 4000 4006 867a 43da 9190  E..3J.@.@..zC...
    0x0010:  9903 fbca 0cea 8422 7e18 25c1 42e2 5254  ......."~.%.B.RT
    0x0020:  5018 3908 3b61 0000 0700 0001 0000 0002  P.9.;a..........
    0x0030:  0000 00                       
```

包的总长度是11，减去int<3>+int<1>4字节=7字节，对应的十六进制就是07；int<3>十六进制为0700 00表示包内容长度；int<1>十六进制为01表示sequence_id；00表示包头；00表示受影响行数；00表示最后插入的索引ID；0200表示服务器状态；

2.5.2ERR_Packet  
表示发生了错误，具体格式如下：

```java
int<1>：0xFF ERR包头
int<2>：错误码
string[1]：Sql状态标识   注：MySQL 4.1 及之后的版本才有
string[5]：Sql状态       注：MySQL 4.1 及之后的版本才有
string<EOF>：错误消息
```

2.5.3EOF_Packet  
以标记查询执行结果的结束：

```java
int<1>：EOF值（0xFE）
int<2>：告警计数    注：MySQL 4.1 及之后的版本才有
int<2>：状态标志位   注：MySQL 4.1 及之后的版本才有
```

2.5.4Result Set  
当客户端发送查询请求后，在没有错误的情况下，服务器会返回结果集（Result Set）给客户端，一共有5个部分：

```
Result Set Header       返回数据的列数量
Field                   返回数据的列信息（多个）
EOF                     列结束
Row Data                行数据（多个）
EOF                     数据结束
```

2.5.4.1Result Set Header

```
Length-Encoded Integer  Field结构的数量
Length-Encoded Integer  额外信息
```

2.5.4.2Field

```
LengthEncodedString     目录名称
LengthEncodedString     数据库名称
LengthEncodedString     数据表名称
LengthEncodedString     数据表原始名称
LengthEncodedString     列（字段）名称
LengthEncodedString     列（字段）原始名称
int<1>                  填充值
int<2>                  字符编码
int<4>                  列（字段）长度
int<1>                  列（字段）类型
int<2>                  列（字段）标志
int<1>                  整型值精度
int<2>                  填充值（0x00）
LengthEncodedString     默认值
```

2.5.4.3EOF  
参考2.5.3EOF_Packet

2.5.4.4Row Data

```
LengthEncodedString     字段值
......                  多个字段值
```

实例分析，表信息如下：

```sql
CREATE TABLE `btest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `age` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8
```

select * from btest;服务器返回的数据如下：

```sql
mysql> select * from btest;
+----+------+---------+
| id | age  | name    |
+----+------+---------+
|  1 |   10 | zhaohui |
|  2 |   11 | zhaohui |
+----+------+---------+
```

服务器返回的包，使用tcpdump进行监听，输出十六进制日志如下：

```bash
01:54:21.522660 IP root.mysql > 153.3.251.202.58587: Flags [P.], seq 1:196, ack 24, win 115, length 195
    0x0000:  4508 00eb 8839 4000 4006 4793 43da 9190  E....9@.@.G.C...
    0x0010:  9903 fbca 0cea e4db 9dd8 0216 eda6 f730  ...............0
    0x0020:  5018 0073 ca34 0000 0100 0001 0328 0000  P..s.4.......(..
    0x0030:  0203 6465 6604 7465 7374 0562 7465 7374  ..def.test.btest
    0x0040:  0562 7465 7374 0269 6402 6964 0c3f 0014  .btest.id.id.?..
    0x0050:  0000 0008 0342 0000 002a 0000 0303 6465  .....B...*....de
    0x0060:  6604 7465 7374 0562 7465 7374 0562 7465  f.test.btest.bte
    0x0070:  7374 0361 6765 0361 6765 0c3f 000b 0000  st.age.age.?....
    0x0080:  0003 0000 0000 002c 0000 0403 6465 6604  .......,....def.
    0x0090:  7465 7374 0562 7465 7374 0562 7465 7374  test.btest.btest
    0x00a0:  046e 616d 6504 6e61 6d65 0c21 00fd 0200  .name.name.!....
    0x00b0:  00fd 0000 0000 0005 0000 05fe 0000 2200  ..............".
    0x00c0:  0d00 0006 0131 0231 3007 7a68 616f 6875  .....1.10.zhaohu
    0x00d0:  690d 0000 0701 3202 3131 077a 6861 6f68  i.....2.11.zhaoh
    0x00e0:  7569 0500 0008 fe00 0022 00              ui.......".
```

0328 0000 02对应的是Result Set Header，03表示3个字段；03 6465 66对应的是目录名称的默认值def，03表示后面的字节数为3；04 7465 7374  
对应的是数据库名称test；0562 7465 7374对应的是数据表名称btest；0562 7465 7374对应的是数据表原始名称btest；0269 64对应字段名称id；02 6964对应列（字段）原始名称id；0c3f 00对应的是填充值和字符编码；14 0000 00对应的十进制是20表示列（字段）长度；08 0342 00分别表示列（字段）类型，标识，整型值精度；00002个字节为填充值；00为默认值表示空的；  
后续的age和name字段同上，不在重复；

0131类型LengthEncodedString对应的字符1就是id的值；0231 30类型LengthEncodedString对应的字符10就是age的值；07 7a68 616f 6875 69类型LengthEncodedString对应的字符zhaohui就是name的值；

**参考**  
[https://dev.mysql.com/doc/dev/mysql-server/latest/PAGE_PROTOCOL.html](https://dev.mysql.com/doc/dev/mysql-server/latest/PAGE_PROTOCOL.html)  
[http://hutaow.com/blog/2013/11/06/mysql-protocol-analysis/](http://hutaow.com/blog/2013/11/06/mysql-protocol-analysis/)

**个人博客：[codingo.xyz](http://codingo.xyz/)**