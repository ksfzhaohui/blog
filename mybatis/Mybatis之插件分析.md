## 前言

Mybatis提供了强大的扩展功能，也就是Mybatis的插件（plugins）功能；MyBatis允许你在已映射语句执行过程中的某一点进行拦截调用，拦截之后可以对已有方法添加一些定制化的功能，比如常见的分页功能；试图修改或重写已有方法的行为的时候，你很可能在破坏MyBatis 的核心模块，这些都是更低层的类和方法，所以使用插件的时候要特别当心。

## 如何扩展

### 1.拦截点

拦截的点一共包含四个对象，若干方法，具体如下：

-   Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
-   ParameterHandler (getParameterObject, setParameters)
-   ResultSetHandler (handleResultSets, handleOutputParameters)
-   StatementHandler (prepare, parameterize, batch, update, query)

有了拦截点之后，我们需要告诉Mybatis在哪个类下执行到哪个方法的时候进行扩展；Mybatis提供了简单的配置即可实现；

### 2.如何扩展

使用插件是非常简单的，只需实现Interceptor接口，并指定想要拦截的方法签名即可；如下面自定义的插件MyPlugin：

```
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }) })
public class MyPlugin implements Interceptor {
    private Properties prop;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.err.println("====before===="+invocation.getTarget());
        Object obj = invocation.proceed();
        System.err.println("====after===="+invocation.getTarget());
        return obj;
    }
    
    @Override
    public Object plugin(Object target) {
        System.err.println("====调用生成代理对象====target:" + target);
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        this.prop = properties;
    }
}
```

通过注解的方式指定了方法的签名：类型、方法名、参数列表；如果要拦截多个方法可以配置多个**[@Signature](https://my.oschina.net/u/2393005)**通过逗号分隔；如上配置表示在执行到Executor的update和query方法时进行拦截，也就会执行Interceptor接口中定义的intercept方法，我们可以简单的做一些修改，或者干脆在里面重写对应的方法，当然前提是对源码很熟悉的情况下；当然还需要Mybatis知道我们自定义的MyPlugin，需要提供configuration配置：

```
<plugins>
        <plugin interceptor="com.mybatis.plugin.MyPlugin">
            <property name="dbType" value="mysql" />
        </plugin>
    </plugins>
```

做完以上配置之后，可以简单的做一个测试，执行Mybatis的一个查询功能，日志输出如下：

```
====调用生成代理对象====target:org.apache.ibatis.executor.CachingExecutor@31d7b7bf
====before====org.apache.ibatis.executor.CachingExecutor@31d7b7bf
====调用生成代理对象====target:org.apache.ibatis.scripting.defaults.DefaultParameterHandler@f5ac9e4
====调用生成代理对象====target:org.apache.ibatis.executor.resultset.DefaultResultSetHandler@7334aada
====调用生成代理对象====target:org.apache.ibatis.executor.statement.RoutingStatementHandler@4d9e68d0
====after====org.apache.ibatis.executor.CachingExecutor@31d7b7bf
```

生成的四个代理对象其实就是我们上面介绍的四个拦截点，虽然都生成了代理对象，但是在代理对象执行的时候也会检查是否配置了指定拦截方法，所以执行Mybatis查询功能的时候，检查是否配置了query方法。

## 插件分析

### 1.拦截器注册

我们在configuration中通过标签plugins配置的插件，最后都会注册到一个拦截链**InterceptorChain**中，其中维护了拦截器列表：

```
public class InterceptorChain {

  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }
}
```

Mybatis在解析plugins标签的时候，解析到每个plugin的时候都会调用**addInterceptor()**方法将插件添加到interceptors，多个插件会在**pluginAll()**方法执行的时候依次被调用；

### 2.触发拦截器

过滤器链中提供了**pluginAll()**方法，此方法会在我们上面介绍的四个类被实例化的时候调用，可以在开发工具中简单的使用快捷键查询此方法被调用的地方：  
![Mybatis之插件分析](http://p3.pstatp.com/large/pgc-image/7e87c090d8b3462184fd02c93a4c15f1)  
可以看一个最常见的Executor实例化：

```
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
      executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }
```

以上首先根据不同的executorType类型创建不同的Executor，默认的如CachingExecutor，最后会调用interceptorChain的pluginAll方法，传入参数和返回参数都是Executor，可以简单理解就是通过pluginAll返回了Executor的代理类；

### 3.生成代理类

生成代理类的方式有：JDK自带的Proxy和CGlib方式，Mybatis提供了Plugin类提供了生成相关代理类的功能，所以在上面的实例MyPlugin的plugin方法中直接使用了**Plugin.wrap(target, this)**，两个参数分别是：target对应就是上面的四种类型，this代表当前的自定义插件：

```
  public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }
```

此方法首先获取自定义插件中配置的Signature，然后检查Signature中配置的类是否是当前target类型，如果匹配则通过JDK自带的Proxy创建代理类，否则直接返回target，不做任何代理处理；如上实例这里的Target是CachingExecutor，其对应的接口是Executor，而我们在MyPlugin中的Signature中配置了Executor类，所以可以匹配成功；

### 4.触发执行

等到真正执行具体方法的时候，其实是执行创建代理类时指定的InvocationHandler的invoke方法，可以发现在上节中指定的InvocationHandler是Plugin对象，Plugin本身也是继承于InvocationHandler，提供了invoke方法：

```
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        return interceptor.intercept(new Invocation(target, method, args));
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }
```

首先从signatureMap中获取拦截类对应的方法列表，然后检查当前执行的方法是否在要拦截的方法列表中，如果在则调用自定义的插件interceptor，否则执行默认的invoke操作；interceptor调用intercept方法的时候是传入的Invocation对象，其包含了三个参数分别是：target对应就是上面的四种类型，method当前执行的方法，args当前执行方法的参数；这里的方法名和参数是可以在源码里面查看的，比如Executor的query方法：

```
<E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;
```

### 5.拦截处理

调用自定义拦截器的intercept方法，传入一个Invocation对象，如果只是记录一下日志，如上面的MyPlugin插件，这时候只需要执行proceed方法：

```
public class Invocation {
  private final Object target;
  private final Method method;
  private final Object[] args;

  public Object proceed() throws InvocationTargetException, IllegalAccessException {
    return method.invoke(target, args);
  }
}
```

此方法其实就是默认的处理，和不使用代理类的情况产生的结果其实是一样的，只不过代理类使用反射的方式调用；当然Mybatis的插件不止记录日志这么简单，比如我们常用的插件[PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)用来做物理分页等等；

## 总结

Mybatis提供了四个类，可以被用来拦截，用来进行功能扩展；本文介绍了如何使用插件进行功能扩展，然后从源码层面进行分析如何通过代理类来实现扩展。

## 示例代码

[Github](https://github.com/ksfzhaohui/blog/tree/master/mybatis)