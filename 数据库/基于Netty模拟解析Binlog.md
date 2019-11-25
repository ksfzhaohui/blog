**系列文章**

[MySql Binlog初识](https://my.oschina.net/OutOfMemory/blog/1571107)  
[MySql Binlog事件介绍篇](https://my.oschina.net/OutOfMemory/blog/1572968)  
[MySql Binlog事件数据篇](https://my.oschina.net/OutOfMemory/blog/1579454)  
[Mysql通讯协议分析](https://my.oschina.net/OutOfMemory/blog/1595684)  
[基于Netty模拟解析Binlog](https://my.oschina.net/OutOfMemory/blog/1605201)

**前言**  
最近一段时间一直再看mysql binlog相关的内容，也整理了几篇相关的文章，对mysql的事件以及通讯协议在理论上有了一个大概的了解，但是缺少实战；本文的目的就是从实战出发，了解binlog解析的整个过程。

**解析思路**  
把binlog的解析过程大致分为以下几个步骤：  
1.服务器启动首先获取上一次解析成功的位置（实例中存储在本地文件中）；  
2.和mysql服务器建立连接；  
3.接受mysql发送来的binlog事件；  
4.对不同的binlog事件进行解析；  
5.将数据进行存储（实例中仅在日志中打印）；  
6.存储成功后，定时记录Binaly Log位置。

关于binlog相关的配置可以参考系列文章，里面有详解的介绍，下面对步骤进行详细的介绍；

**1.服务器启动首先获取上一次解析成功的位置（实例中存储在本地文件中）**  
binlog的位置信息存储在文件namePosition，有更新也同样更新到namePosition中，部分代码如下：

```java
public class NamePositionStore {
 
    private static Logger log = LoggerFactory.getLogger(NamePositionStore.class);
 
    public static final String BINLOG_NAME = "binlogName";
    public static final String BINLOG_POSITIION = "binlogPosition";
 
    private static Map<String, String> binlogMap = new HashMap<String, String>();
 
    private static String lineSeparator = (String) System.getProperties().get("line.separator");
    private static String localStoreUrl = "namePosition";
 
    static {
        loadNamePosition();
    }
 
    public static synchronized Map<String, String> loadNamePosition() {
        binlogMap = load();
        return binlogMap;
    }
 
    public static synchronized Map<String, String> getNamePosition() {
        return binlogMap;
    }
 
    public static synchronized void putNamePosition(String binlogName, long binlogPosition) {
        binlogMap.put(BINLOG_NAME, binlogName);
        binlogMap.put(BINLOG_POSITIION, binlogPosition + "");
 
        store(binlogMap);
    }
 
    public static synchronized void putNamePosition(long binlogPosition) {
        binlogMap.put(BINLOG_POSITIION, binlogPosition + "");
        store(binlogMap);
    }
         
    ...以下代码省略，可参考码云完整代码...
}
```

namePosition中存储了两个字段分别是：binlogName和binlogPosition，这两个字段会在客户端请求mysql binlog的时候需要的参数；

**2.和mysql服务器建立连接**  
在文章[Mysql通讯协议分析](https://my.oschina.net/OutOfMemory/blog/1595684)中可以看到和mysql服务器建立连接的步骤：mysql发送握手包，客户端发送认证包，mysql发送认证的结果；

```java
public class HandshakeHandler extends SimpleChannelInboundHandler<DataPackage> {
 
    private Logger logger = LoggerFactory.getLogger(HandshakeHandler.class);
 
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataPackage pk) throws Exception {
        logger.info("Handshake start");
        if (null == pk) {
            return;
        }
        ByteBuf msg = (ByteBuf) pk.getContent();
        int protocolVersion = msg.readByte();
        String serverVersion = ByteUtil.NullTerminatedString(msg);
        int threadId = ByteUtil.readInt(msg, 4);
        logger.info("protocolVersion = " + protocolVersion + ",serverVersion = " + serverVersion + ",threadId = "
                + threadId);
        String randomNumber1 = ByteUtil.NullTerminatedString(msg);
        msg.readBytes(2);
        byte encode = msg.readByte();
        msg.readBytes(2);
        msg.readBytes(13);
        String randomNumber2 = ByteUtil.NullTerminatedString(msg);
        logger.info("Handshake end");
        AuthenticateDataBean dataBean = new AuthenticateDataBean(encode, randomNumber1 + randomNumber2,
                Constants.userName, Constants.password);
        ctx.channel().writeAndFlush(new DataPackage(1, dataBean));
        ctx.pipeline().remove(this);
    }
}
```

接受mysql发送的握手包，进行相关的解析工作，其中比较重要的是两个挑战随机数，客户端在认证的时候需要使用随机数对密码加密；解析完之后客户端发送认证数据包（封装在AuthenticateDataBean），具体类信息如下：

```java
public class AuthenticateDataBean implements IDataBean {
    /** 认证需要的用户名密码 **/
    private String userName;
    private String password;
    /** 编码和挑战随机数 **/
    private byte encode;
    private String randomNumber;
 
    ...以下代码省略，可参考码云完整代码...
 
    @Override
    public byte[] toByteArray() throws Exception {
        int clientPower = PowerType.CLIENT_LONG_FLAG | PowerType.CLIENT_PROTOCOL_41
                | PowerType.CLIENT_SECURE_CONNECTION;
        byte clientPowerBytes[] = ByteUtil.writeInt(clientPower, 4);
        int maxLen = 0;
        byte maxLenBytes[] = ByteUtil.writeInt(maxLen, 4);
        byte encodeBytes[] = ByteUtil.writeInt(encode, 1);
        byte zeroBytes[] = ByteUtil.writeInt(0, 23);
 
        byte[] userNameBytes = (userName + "\0").getBytes();
        byte[] passwordBytes = "".equals(password) ? new byte[0]
                : ByteUtil.passwordCompatibleWithMySQL411(password, randomNumber);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(clientPowerBytes);
        byteBuf.writeBytes(maxLenBytes);
        byteBuf.writeBytes(encodeBytes);
        byteBuf.writeBytes(zeroBytes);
        byteBuf.writeBytes(userNameBytes);
        byteBuf.writeByte((byte) passwordBytes.length);
        byteBuf.writeBytes(passwordBytes);
        return byteBuf.array();
    }
 
}
```

发送的认证包到服务器之后，客户端会收到认证的结果，具体处理在AuthenticateResultHandler中：

```java
public class AuthenticateResultHandler extends SimpleChannelInboundHandler<DataPackage> {
 
    private Logger logger = LoggerFactory.getLogger(AuthenticateResultHandler.class);
 
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataPackage dataPackage) throws Exception {
        ByteBuf msg = (ByteBuf) dataPackage.getContent();
        int mark = msg.readByte();
        if (mark == 0) {
            Map<String, String> binlongMap = NamePositionStore.getNamePosition();
            RequestBinlogDumpDataBean dataBean = new RequestBinlogDumpDataBean(Constants.serverId,
                    binlongMap.get(NamePositionStore.BINLOG_NAME),
                    Long.valueOf(binlongMap.get(NamePositionStore.BINLOG_POSITIION)));
            ctx.channel().writeAndFlush(new DataPackage(0, dataBean));
            logger.info("Authenticate success:" + ByteUtil.bytesToHexString(msg.array()));
        } else {
            logger.info("Authenticate fail:" + ByteUtil.bytesToHexString(msg.array()));
        }
        ctx.pipeline().remove(this);
    }
}
```

如果认证成功，这时候客户端需要发送请求接受binlog的请求，这里面包含两个重要的参数就是binlogName和binlogPosition，具体信息在RequestBinlogDumpDataBean类中，结构类似AuthenticateDataBean，此处省略。

**3.接受mysql发送来的binlog事件**  
服务器收到客户端的binlog请求，这时服务器如果产生了binlog日志，会发送给客户端，客户端需要一个接受binlog事件的类：

```java
public class BinlogEventParseHandler extends SimpleChannelInboundHandler<DataPackage> {
 
    private Logger logger = LoggerFactory.getLogger(BinlogEventParseHandler.class);
 
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataPackage datePackage) throws Exception {
        ByteBuf contentBuf = (ByteBuf) datePackage.getContent();
        contentBuf.skipBytes(1);
        EventHeader header = new EventHeader();
        header.setTimestamp(ByteUtil.readInt(contentBuf, 4));
        header.setTypeCode((byte) ByteUtil.readInt(contentBuf, 1));
        header.setServerId(ByteUtil.readInt(contentBuf, 4));
        header.setEventLen(ByteUtil.readInt(contentBuf, 4));
        header.setNextPosition(ByteUtil.readInt(contentBuf, 4));
        header.setFlags(ByteUtil.readInt(contentBuf, 2));
        logger.info(header.toString());
 
        IEventParser parser = EventParserFactory.getEventParser(header.getTypeCode());
        if (parser == null) {
            logger.error("不支持的binlog事件类型解析;typeCode = " + header.getTypeCode());
        }
        parser.parse(contentBuf, header);
        if (header.getTypeCode() != EventType.ROTATE_EVENT
                && header.getTypeCode() != EventType.FORMAT_DESCRIPTION_EVENT) {
            NamePositionStore.putNamePosition(header.getNextPosition());
        }
    }
}
```

首先解析事件头包括：eventType，eventLen，nextPosition等信息，然后根据事件类型，调用不同的解析器进行解析；

**4.对不同的binlog事件进行解析**  
步骤3中通过不同的事件类型，获取对应的解析器，这些解析器都在EventParserFactory中，下面以FormatDescriptionEventParser为例

```java
public class FormatDescriptionEventParser implements IEventParser {
 
    private Logger logger = LoggerFactory.getLogger(FormatDescriptionEventParser.class);
 
    @Override
    public void parse(ByteBuf msg, EventHeader eventHeader) {
        long binlogVersion = ByteUtil.readInt(msg, 2);
        String serverVersion = ByteUtil.readFixedLenString(msg, 50);
        long timestamp = ByteUtil.readInt(msg, 4);
        byte headerLength = msg.readByte();
        StringBuffer eventTypeFixDataLen = new StringBuffer();
        for (int i = 0; i < 27; i++) {
            eventTypeFixDataLen.append(msg.readByte() + ",");
        }
        logger.info("binlogVersion = " + binlogVersion + ",serverVersion = " + serverVersion + ",timestamp = "
                + timestamp + ",headerLength = " + headerLength + ",eventTypeStr = " + eventTypeFixDataLen);
    }
}
```

根据FormatDescriptionEvent的格式读取ByteBuf里面的数据包括：binlog版本，服务器版本，时间戳，事件头长度以及每个Event的fixed part lengths，本次实战中仅仅将解析后的数据打印到日志中，没有做其他处理。

**5.将数据进行存储（实例中仅在日志中打印）**  
本次使用的binlog模式是：STATEMENT，所有所有的sql语句都会发送给客户端，对应的事件是QueryEvent，包括创建表，增删改等操作：

```java
public class QueryEventParser implements IEventParser {
 
    private Logger logger = LoggerFactory.getLogger(QueryEventParser.class);
 
    private static final int QUERY_EVENT_FIX_LEN = 13;
 
    @Override
    @SuppressWarnings("unused")
    public void parse(ByteBuf msg, EventHeader eventHeader) {
        long threadId = ByteUtil.readInt(msg, 4);
        long time = ByteUtil.readInt(msg, 4);
        int dbNameLen = msg.readByte();
        int errorCode = ByteUtil.readInt(msg, 2);
        int variableLen = ByteUtil.readInt(msg, 2);
 
        msg.skipBytes(variableLen);
 
        String dbName = ByteUtil.NullTerminatedString(msg);
        String sql = ByteUtil.readFixedLenString(msg, (int) (eventHeader.getEventLen() - variableLen
                - EventHeader.EVENT_HEADER_LEN - QUERY_EVENT_FIX_LEN - dbName.getBytes().length - 1));
        logger.info("dbName = " + dbName + ",sql = " + sql);
    }
}
```

以上的QueryEventParser解析执行的更新语句，记录了数据库名称和相关的更新sql语句。

**6.存储成功后，定时记录Binaly Log位置**  
在步骤三中的BinlogEventParseHandler类中，我们在解析玩之后，存储了nextPosition信息到文件中，方便下次启动读取，同时binlog还有一个切换binlog文件的事件，同样也需要记录；

```java
public class RotateEventParser implements IEventParser {
 
    private Logger logger = LoggerFactory.getLogger(RotateEventParser.class);
 
    @Override
    public void parse(ByteBuf msg, EventHeader eventHeader) {
        long binlogPosition = ByteUtil.readLong(msg, 8);
        int variablePartLen = (int) (eventHeader.getEventLen() - EventHeader.EVENT_HEADER_LEN - 8);
        byte variablePart[] = new byte[variablePartLen];
        msg.readBytes(variablePart);
        String binlogName = new String(variablePart);
 
        logger.info("binlogPosition = " + binlogPosition + ",binlogName = " + binlogName);
 
        NamePositionStore.putNamePosition(binlogName, binlogPosition);
    }
}
```

对应的事件是RotateEvent，因为切换成新的binlongName，所有需要同时记录binlongName和binlogPosition。

以上具体代码可以参考：  
码云：[https://gitee.com/OutOfMemory/easy-binlog](https://gitee.com/OutOfMemory/easy-binlog)  
github：[https://github.com/ksfzhaohui/easy-binlog](https://github.com/ksfzhaohui/easy-binlog)

**总结**  
本文旨在让大家更加了解binlog同步的大致过程，所以本文提供的项目没有经过大量的测试，仅供大家学习使用；本项目中参考了一些优秀的开源软件：mysql-binlog-connector-java和MySQL-Binlog

**个人博客：[codingo.xyz](http://codingo.xyz/)**