## 问题描述

近期项目需要从虚拟机环境迁移到容器环境，其中有一个项目在迁移到容器环境之后的两天之内出现了2次“死锁(deadlock)”的问题，部分关键日志如下：

```
Found one Java-level deadlock:
=============================
"DefaultMessageListenerContainer-9":
  waiting to lock monitor 0x00007fde3400bf38 (object 0x00000000dda358d0, a oracle.jdbc.driver.T4CConnection),
  which is held by "DefaultMessageListenerContainer-7"
"DefaultMessageListenerContainer-7":
  waiting to lock monitor 0x00007fdea000b478 (object 0x00000000dda35578, a oracle.jdbc.driver.T4CConnection),
  which is held by "DefaultMessageListenerContainer-9"
Java stack information for the threads listed above:
===================================================
"DefaultMessageListenerContainer-9":
    at oracle.jdbc.oracore.OracleTypeADT.linearize(OracleTypeADT.java:1280)
    - waiting to lock <0x00000000dda358d0> (a oracle.jdbc.driver.T4CConnection)
    at oracle.sql.ArrayDescriptor.toBytes(ArrayDescriptor.java:653)
    at oracle.sql.ARRAY.toBytes(ARRAY.java:711)
    - locked <0x00000000dda35578> (a oracle.jdbc.driver.T4CConnection)
    at oracle.jdbc.driver.OraclePreparedStatement.setArrayCritical(OraclePreparedStatement.java:6049)
    at oracle.jdbc.driver.OraclePreparedStatement.setARRAYInternal(OraclePreparedStatement.java:6008)
    - locked <0x00000000dda35578> (a oracle.jdbc.driver.T4CConnection)
    at oracle.jdbc.driver.OraclePreparedStatement.setArrayInternal(OraclePreparedStatement.java:5963)
    at oracle.jdbc.driver.OracleCallableStatement.setArray(OracleCallableStatement.java:4833)
    at oracle.jdbc.driver.OraclePreparedStatementWrapper.setArray(OraclePreparedStatementWrapper.java:114)

```

```
"DefaultMessageListenerContainer-7":
    at oracle.jdbc.oracore.OracleTypeADT.linearize(OracleTypeADT.java:1280)
    - waiting to lock <0x00000000dda35578> (a oracle.jdbc.driver.T4CConnection)
    at oracle.sql.ArrayDescriptor.toBytes(ArrayDescriptor.java:653)
    at oracle.sql.ARRAY.toBytes(ARRAY.java:711)
    - locked <0x00000000dda358d0> (a oracle.jdbc.driver.T4CConnection)
    at oracle.jdbc.driver.OraclePreparedStatement.setArrayCritical(OraclePreparedStatement.java:6049)
    at oracle.jdbc.driver.OraclePreparedStatement.setARRAYInternal(OraclePreparedStatement.java:6008)
    - locked <0x00000000dda358d0> (a oracle.jdbc.driver.T4CConnection)
    at oracle.jdbc.driver.OraclePreparedStatement.setArrayInternal(OraclePreparedStatement.java:5963)
    at oracle.jdbc.driver.OracleCallableStatement.setArray(OracleCallableStatement.java:4833)
    at oracle.jdbc.driver.OraclePreparedStatementWrapper.setArray(OraclePreparedStatementWrapper.java:114)
    at

```

日志还是挺明显的，线程DefaultMessageListenerContainer-9获得了锁0x00000000dda35578，等待获取0x00000000dda358d0；而DefaultMessageListenerContainer-7正好相反，从而导致死锁；

## 问题分析

以上的错误日志和Oracle的驱动类有关，所以猜测是驱动版本的问题，所以找相关人员分别拉取了虚拟机环境和容器环境的生产Oracle驱动jar包，结果如下：

```
#虚拟机
[19:38:21 oracle@tomcat-384 lib]$ ls -l ojdbc-1.4.jar
-rw-r--r-- 1 oracle oinstall 1378346 Jul  3  2014 ojdbc-1.4.jar
 
#容器
[oracle@7f666c76b7-dx2gq lib]$ ls -l ojdbc6.jar
-rw-r--r-- 1 oracle oinstall 2739670 Aug 11  2015 ojdbc6.jar

```

两个环境使用了不同的版本，容器使用了高版本(11.2.0.4.0)，虚拟机使用的是低版本(10.1.0.5.0)；Google查询了和Oracle驱动相关产生死锁的问题，查到了Oracle官方有如下文档：  
[Java-level deadlock with 11.2](https://support.oracle.com/knowledge/Middleware/1271651_1.html)  
提供给我们的方案是“Upgraded the Oracle JDBC driver from 10.2 to 11.2.”，正好和我们遇到的情况相反，我们是高版本有问题，低版本没有问题，所以需要进一步分析；

### 源码分析

首先找到相关的逻辑代码类，此处为了更好的看出问题，使用了如下的模拟类，大致如下：

```
//测试Dao，配置在spring下的单例
public class TestDaoImpl {

    //共享的两个ArrayDescriptor
    private ArrayDescriptor param1Desc;
    private ArrayDescriptor param2Desc;

    private String param1;
    private String param2;
    private DataSource dataSource;

    public void callProc(Object param) {
        // 准备的两个ARRAY参数
        ARRAY param1Array = null;
        ARRAY param2Array = null;

        CallableStatement callable = null;
        Connection conn = null;

        try {
            // 从连接池获取连接
            conn = DataSourceUtils.getConnection(dataSource);
            param1Array = wrapProcParameter1(param, conn);
            param2Array = wrapProcParameter2(param, conn);

            callable = conn.prepareCall("{ call testProc " + "(?,?,?)}");
            callable.setArray(1, param1Array);
            callable.setArray(2, param2Array);

            callable.execute();

        } catch (Exception e) {
            // 异常处理
        } finally {
            // 关闭处理
        }
    }

    private ARRAY wrapProcParameter1(Object param, Connection conn) throws SQLException {
        if (null == this.param1Desc) {
            this.param1Desc = new ArrayDescriptor(this.param1, conn);
        }
        //省略
        ARRAY array1 = new ARRAY(this.param1Desc, conn, param);
        return array1;
    }

    private ARRAY wrapProcParameter2(Object param, Connection conn) throws SQLException {
        if (null == this.param2Desc) {
            this.param2Desc = new ArrayDescriptor(this.param2, conn);
        }
        //省略
        ARRAY array2 = new ARRAY(this.param2Desc, conn, param);
        return array2;
    }
}

```

大致的逻辑是通过从连接池获取的Connection创建了一个存储过程，然后给存储过程设置了两个ARRAY参数，在创建ARRAY时需要指定相应的ArrayDescriptor，最后执行存储过程；  
产生异常分别在两次setArray的地方，线程1在setArray1的地方，线程2在setArray2的地方，所有以此为入口分别查看两个驱动版本相关类：OraclePreparedStatement，ARRAY，ArrayDescriptor以及OracleTypeADT；

#### 驱动11.2.0.4.0版本

首先查看OraclePreparedStatement中调用的setArray，最终会调用如下方法：  
![](https://oscimg.oschina.net/oscnet/b8e67d7728b644c0a83033022cef59426c6.jpg)  
在方法setARRAYInternal中使用了connection作为了对象锁，接下来OraclePreparedStatement会调用ARRAY，然后ARRAY调用ArrayDescriptor，最后ArrayDescriptor在调用OracleTypeADT，为了方便看出问题直接展示OracleTypeADT中使用锁的地方：  
![](https://oscimg.oschina.net/oscnet/8b06332552732bdc27594b5d940f61a1c39.jpg)  
同样使用connection做为锁对象，这样就存在同时需要获取两把锁了，而上面两把锁都是connection对象，应该不会出现死锁，但是深入发现其实OracleTypeADT中的connection对象是从ArrayDescriptor中获取的，而ArrayDescriptor是一个共享的类变量，这样在多线程环境下就会出现被赋值不同的connection，从而导致出现死锁的问题；  
**大致流程如下：**  
1.首先线程1获取conn1，然后线程2获取conn2；  
2.然后线程1创建Array1，同时对共享的ArrayDescriptor1设置connection=conn1；  
3.线程1挂起，线程2创建Array1，同时对共享的ArrayDescriptor1设置connection=conn2，对共享的ArrayDescriptor2设置connection=conn2；  
4.线程2继续占用cpu，执行setArray1，这时候都是Array1和ArrayDescriptor1中的锁都是conn2，所以没有问题，继续执行setArray2，在执行完获取第一把锁conn2之后，线程2挂起；  
5.线程1抢占cpu，对共享的ArrayDescriptor2设置connection=conn1，然后执行setArray1；但此时Array1中的connection是conn1，而ArrayDescriptor1中的connection是conn2，所以出现线程1占用了conn1，等待conn2锁；  
6.此时线程2再次抢到cpu，但是在获取第二把锁时，此时ArrayDescriptor2中的connection已经被设置成了conn1，而conn1已经被线程1占有，所以等待获取conn1；  
7.死锁出现了线程1占有了conn1锁，等待conn2锁；线程2占有了conn2锁，等待conn1锁；从而导致死锁发生；

从上面的分析可以看出主要原因是ArrayDescriptor被设置成了类变量，被多个线程所访问，解决死锁问题可以把ArrayDescriptor改成局部变量；但是如果仅是业务造成的问题，那应该在驱动ojdbc-1.4中存在同样的死锁问题，但是此项目在虚拟机环境中一直没有出现过问题；继续看ojdbc-1.4源码；

#### 驱动10.1.0.5.0版本

同样分析此驱动版本中的相同类，同上首先查看OraclePreparedStatement中调用的setArray，最终会调用如下方法：  
![](https://oscimg.oschina.net/oscnet/fac1cd965f1ddc5f60029dbec892e657d9f.jpg)  
同样使用了connection作为对象锁，再看OracleTypeADT，相关代码如下：  
![](https://oscimg.oschina.net/oscnet/c36f41e23af1e21f7db3fc2bc65b5c6e5a1.jpg)  
可以看到这里并没有使用connection作为锁，而是使用了内置锁，所以就不会出现死锁问题；

## 问题总结

首先就是在迁移环境时一定要保证相关的依赖公共jar保证版本的一致，就算是低版本，高版本也不一样保证向下兼容；其次也是最重要的写业务逻辑时遇到公共变量时一定要谨慎，是否会出现多线程问题；