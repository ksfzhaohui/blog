## 前言
在众多的ORM框架中，Mybatis现在越来越多的被互联网公司所使用；主要原因还是因为Mybatis使用简单，操作灵活；本系列准备通过提问的方式来从源码层来更加深入的了解Mybatis。

## 提问
我们最常用的使用Mybatis的方式是获取一个Mapper接口对象，然后通过接口的方法名映射到配置文件中的statement；大致的代码格式如下所示：
```
public class BlogMain {
	public static void main(String[] args) throws IOException {
		String resource = "mybatis-config-sourceCode.xml";
		InputStream inputStream = Resources.getResourceAsStream(resource);
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		SqlSession session = sqlSessionFactory.openSession();
		try {
			BlogMapper mapper = session.getMapper(BlogMapper.class);
			// 常规方法
			System.out.println(mapper.selectBlog(101));
			// Object的方法
			System.out.println(mapper.hashCode());
			// public default方法
			System.out.println(mapper.defaultValue());
			// 父接口中的方法
			System.out.println(mapper.selectParent(101));
		} finally {
			session.close();
		}
	}
```
以上除了使用常规的接口方法selectBlog，还使用了类型完全不同的方法分别是：Object内部方法，接口的默认方法，以及父类中的方法，当然Mybatis都能很好的处理，那我们每次调用的接口的方法时，Mybatis是如何帮我们执行sql的；接下来将进行分析，同时一并看一下是如何处理这些特殊方法的。

## 猜测
通过使用动态代理，生成一个代理类，然后通过Mapper里面的方法名称和配置文件中的statement名称做映射，然后根据statement类型分别执行sql。

## 分析
首先分析getMapper操作，然后再分析执行Mapper中的相关方法是如何调用相关sql的；
#### 1.执行getMapper分析
如上代码中使用openSession创建的一个**DefaultSqlSession**类，此类中包含了执行了sql的增删改查等操作，另外还包含了getMapper方法：
```
  private final Configuration configuration;
  
  @Override
  public <T> T getMapper(Class<T> type) {
    return configuration.<T>getMapper(type, this);
  }
```
此处的**Configuration**是关键，也是Mybatis的一个核心类，可以先简单理解为就是我们的配置文件mybatis-config.xml的一个映射类；继续往下走：
```
  protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
  
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }
```
这里引出了**MapperRegistry**，所有的Mapper都在此类中注册，通过key-value的形式存放，key对应xx.xx.xxMapper，而value存放的是Mapper的代理类，具体如类MapperRegistry代码所示：
```
  private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();
  
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
```
可以看到每次getMapper的时候其实都是去knownMappers获取一个**MapperProxyFactory**类，至于是何时往knownMappers中添加数据的，是在解析mybatis-config.xml配置文件的时候，解析到mappers标签的时候，如下所示：
```
<mappers>
	<mapper resource="mapper/BlogMapper.xml" />
</mappers>
```
继续解析里面的BlogMapper.xml，会把BlogMapper.xml中的namespace作为key，如下所示：
```
<mapper namespace="com.mybatis.mapper.BlogMapper">
	<select id="selectBlog" parameterType="long" resultType="blog">
		select * from blog where id = #{id}
	</select>
</mapper>
```
namespace是必填的，此值作为MapperRegistry中的knownMappers的key，而value就是此Mapper类的一个代理工厂类MapperProxyFactory，每次调用getMapper的时候都会newInstance一个实例，代码如下：
```
  private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();
   
  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }
```
可以发现通过jdk自带的代理类Proxy.newProxyInstance(...)创建了一个代理类，设置MapperProxy作为InvocationHandler，在实例化MapperProxy时同时传入了一个methodCache对象，此对象是一个Map，存放的就是每个Mapper里面的方法，这里定义为MapperMethod；至此我们了解了getMapper的大致流程，下面继续看执行方法；

#### 2.执行方法
由上分析可知，通过getMapper返回的是Mapper的一个动态代理类，并且指定了**MapperProxy**作为InvocationHandler，所以我们每次调用方法时其实调用了MapperProxy中的invoke方法：
```
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }

  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }
```
以上有两个判断分别是：是否是Object类中的方法，以及是否是默认方法；这两种情况也是我在上面实例中展示的原因，是不需要映射xxMapper.xml中的statement的，可以直接执行返回结果；接下来就是非这两种情况的处理，这里使用的缓存做优化，也就是说我如果连续调用同一个Mapper下面同一个方法多次，不会创建多个MapperMethod；为什么需要缓存，主要是因为每次实例化MapperMethod需要初始化很多东西，如下所示：
```
  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    this.command = new SqlCommand(config, mapperInterface, method);
    this.method = new MethodSignature(config, mapperInterface, method);
  }
```
主要是SqlCommand和MethodSignature这两个实例，这两个类大致意思是：SqlCommand保存了方法是何种操作类型包括增删改查，未知，刷新以及对应的xxMapper.xml中的statement的ID；MethodSignature保存了方法的签名包括返回类型等；此处我们大致了解一下就行，后面的文章会继续进行详细介绍；有了上面初始化好的这些参数就可以执行调用**MapperMethod**的execute方法了：
```
  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      case INSERT: {
      Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.insert(command.getName(), param));
        break;
      }
      case UPDATE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.update(command.getName(), param));
        break;
      }
      case DELETE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.delete(command.getName(), param));
        break;
      }
      case SELECT:
        if (method.returnsVoid() && method.hasResultHandler()) {
          executeWithResultHandler(sqlSession, args);
          result = null;
        } else if (method.returnsMany()) {
          result = executeForMany(sqlSession, args);
        } else if (method.returnsMap()) {
          result = executeForMap(sqlSession, args);
        } else if (method.returnsCursor()) {
          result = executeForCursor(sqlSession, args);
        } else {
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
        }
        break;
      case FLUSH:
        result = sqlSession.flushStatements();
        break;
      default:
        throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName() 
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }
```
根据SqlCommand的命令类型分别执行不同的sql；执行时还需要对参数进行处理，执行完之后还需要对结果集进行处理，当然还有缓存结果集的处理；此处我们大致了解一下就行，后面每个点会单独进行提问介绍；好了执行完之后就可以返回结果了；

## 总结
本文大致了解到通过getMapper获取了一个xxMapper接口的动态代理类，并且每次get操作都会获取一个新的对象，Mybatis并没有对此类进行缓存，而是对xxMapper接口中的每个方法(MapperMethod)进行缓存，这里的缓存方法是被每个动态代理类对象所共享的，没有对代理类进行缓存主要是因为每个类可以有自己的sqlSession；另外一点是Mybatis处理了是否是Object类中的方法，以及是否是默认方法两种特殊的方法；最后根据方法名称映射的statement执行相关的sql。

## 示例代码地址
[Github](https://github.com/ksfzhaohui/blog/tree/master/mybatis)