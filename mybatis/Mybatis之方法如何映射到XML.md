## 前言

上文[Mybatis之Mapper接口如何执行SQL](https://my.oschina.net/OutOfMemory/blog/3127584)中了解到，Mapper通过动态代理的方式执行SQL，但是并没有详细的介绍方法是如何做映射的，方法包括：方法名，返回值，参数等；这些都是如何同xxMapper.xml进行关联的。

## 方法名映射

上文中提到缓存MapperMethod的目的是因为需要实例化SqlCommand和MethodSignature两个类，而这两个类实例化需要花费一些时间；而方法名的映射就在实例化SqlCommand的时候，具体可以看构造方法：

```
    private final String name;
    private final SqlCommandType type;

    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
      final String methodName = method.getName();
      final Class<?> declaringClass = method.getDeclaringClass();
      MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
          configuration);
      if (ms == null) {
        if (method.getAnnotation(Flush.class) != null) {
          name = null;
          type = SqlCommandType.FLUSH;
        } else {
          throw new BindingException("Invalid bound statement (not found): "
              + mapperInterface.getName() + "." + methodName);
        }
      } else {
        name = ms.getId();
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) {
          throw new BindingException("Unknown execution method for: " + name);
        }
      }
    }
```

首先获取了方法的名称和定义此方法的类，一般此类都是xxxMapper类；注此处的declaringClass类和mapperInterface是有区别的，主要是因为此方法可以是父类里面的方法，而mapperInterface是子类；所以如果定义了父xxxMapper，同样也能进行映射，所以可以看相关代码：

```
 private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
        Class<?> declaringClass, Configuration configuration) {
      String statementId = mapperInterface.getName() + "." + methodName;
      if (configuration.hasStatement(statementId)) {
        return configuration.getMappedStatement(statementId);
      } else if (mapperInterface.equals(declaringClass)) {
        return null;
      }
      for (Class<?> superInterface : mapperInterface.getInterfaces()) {
        if (declaringClass.isAssignableFrom(superInterface)) {
          MappedStatement ms = resolveMappedStatement(superInterface, methodName,
              declaringClass, configuration);
          if (ms != null) {
            return ms;
          }
        }
      }
      return null;
    }
  }
```

可以看到方法名映射的时候并不是只有名称，同样在前面加了接口名称类似：com  
.xx.mapper.XXMapper+方法名，其对应的就是xxMapper.xml中的namespace+statementID；如果可以找到就直接返回configuration中的一个MappedStatement，暂时可以简单为就是xxMapper.xml的一个标签块；如果找不到说明有可能在父类中，可以发现这里使用递归，直到从所有父类中查找，如果还是找不到返回null；  
接着上段代码往下看，如果找不到对应的MappedStatement，会查看方法是否有@Flush注解，如果有指定命令类型为FLUSH，否则就抛出异常；找到MappedStatement从里面获取命令类型，所有类型包括：

```
public enum SqlCommandType {
  UNKNOWN, INSERT, UPDATE, DELETE, SELECT, FLUSH;
}
```

INSERT, UPDATE, DELETE, SELECT其实也就是我们在xxMapper.xml中定义的标签；

## 方法签名

缓存的另一个对象是MethodSignature，直译就是方法签名，包含了方法的返回值，参数等，可以看其构造函数：

```
    public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
      Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
      if (resolvedReturnType instanceof Class<?>) {
        this.returnType = (Class<?>) resolvedReturnType;
      } else if (resolvedReturnType instanceof ParameterizedType) {
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
      } else {
        this.returnType = method.getReturnType();
      }
      this.returnsVoid = void.class.equals(this.returnType);
      this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
      this.returnsCursor = Cursor.class.equals(this.returnType);
      this.mapKey = getMapKey(method);
      this.returnsMap = this.mapKey != null;
      this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
      this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
      this.paramNameResolver = new ParamNameResolver(configuration, method);
    }
```

首先是获取返回值的类型Type，我们在查询的时候是需要更加具体的类型的，比如是对象类型，基本数据类型，数组类型，列表类型，Map类型，游标类型，void类型；所以这里获取类型的时候需要Type类型的子接口类一共包括：ParameterizedType, TypeVariable<D>, GenericArrayType, WildcardType这四种分别表示：  
**ParameterizedType**:表示一种参数化的类型，比如Collection；  
**TypeVariable**:表示泛型类型如T；  
**GenericArrayType**：类型变量的数组类型；  
**WildcardType**：一种通配符类型表达式，比如?, ? extends Number；  
获取到具体类型之后，根据类型创建了四个标识：returnsMany，returnsMap，returnsVoid，returnsCursor，分别表示：  
returnsMany：返回列表或者数组；  
returnsMap：返回一个Map；  
returnsVoid：没有返回值；  
returnsCursor：返回一个游标；  
除了以上介绍的几个参数，还定义了三个参数分别是：RowBounds在所有参数中的位置，ResultHandler参数在所有参数中的位置，以及实例化了一个参数解析类ParamNameResolver用来作为参数转为sql命令参数；注：RowBounds和ResultHandler是两个特殊的参数，并不映射到xxMapper.xml中的参数，分别用来处理分页和对结果进行再处理；

## 参数映射处理

参数的映射处理主要在ParamNameResolver中处理的，在实例化MethodSignature的同时，初始化了一个ParamNameResolver，构造函数如下：

```
private final SortedMap<Integer, String> names;

private boolean hasParamAnnotation;

public ParamNameResolver(Configuration config, Method method) {
    final Class<?>[] paramTypes = method.getParameterTypes();
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    final SortedMap<Integer, String> map = new TreeMap<Integer, String>();
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
      if (isSpecialParameter(paramTypes[paramIndex])) {
        // skip special parameters
        continue;
      }
      String name = null;
      for (Annotation annotation : paramAnnotations[paramIndex]) {
        if (annotation instanceof Param) {
          hasParamAnnotation = true;
          name = ((Param) annotation).value();
          break;
        }
      }
      if (name == null) {
        // @Param was not specified.
        if (config.isUseActualParamName()) {
          name = getActualParamName(method, paramIndex);
        }
        if (name == null) {
          // use the parameter index as the name ("0", "1", ...)
          // gcode issue #71
          name = String.valueOf(map.size());
        }
      }
      map.put(paramIndex, name);
    }
    names = Collections.unmodifiableSortedMap(map);
  }
```

首先获取了参数的类型，然后获取参数的注解；接下来遍历注解，在遍历的过程中会检查参数类型是否是RowBounds和ResultHandler，这两个类型前文说过，是两个特殊的类型并不用于参数映射，所以这里过滤掉了，然后获取注解中的值如@Param("id")中的value="id"，如果没有注解值，会检查mybatis-config.xml中是否配置了**useActualParamName**，这是一个开关表示是否使用真实的参数名称，默认为开启，如果关闭了开关则使用下标0，1，2...来表示名称；下面已一个例子来说明一下，比如有如下方法：

```
public Blog selectBlog3(@Param("id") long id, @Param("author") String author);
```

那对应的names为:{0=id, 1=author}，如果去掉[@Param](https://my.oschina.net/u/2303379)，如下：

```
public Blog selectBlog3(long id, String author);
```

对应的names为：{0=arg0, 1=arg1}，如果关闭**useActualParamName**开关：

```
<setting name="useActualParamName" value="false"/>
```

对应的names为：{0=0, 1=1}；names这里只是初始化信息，真正和xxMapper.xml中映射的参数还在ParamNameResolver中的另一个方法中做的处理：

```
public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
      return null;
    } else if (!hasParamAnnotation && paramCount == 1) {
      return args[names.firstKey()];
    } else {
      final Map<String, Object> param = new ParamMap<Object>();
      int i = 0;
      for (Map.Entry<Integer, String> entry : names.entrySet()) {
        param.put(entry.getValue(), args[entry.getKey()]);
        // add generic param names (param1, param2, ...)
        final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
        // ensure not to overwrite parameter named with @Param
        if (!names.containsValue(genericParamName)) {
          param.put(genericParamName, args[entry.getKey()]);
        }
        i++;
      }
      return param;
    }
  }
```

此方法会遍历names，在遍历之前会检查是否为空和检查是否只有一个参数，并且没有给这个参数设置注解，那会直接返回这个参数值，并没有key，这种情况xxMapper.xml对应的参数名称其实是不关心的，什么名称都可以直接进行映射；如果不是以上两种情况会遍历names会分别往一个Map中写入两个key值，还是上面的三种情况，经过此方法处理后，值会发生如下变化：

```
{author=zhaohui, id=158, param1=158, param2=zhaohui}
```

以上是有设置注解名称的情况；

```
{arg1=zhaohui, arg0=158, param1=158, param2=zhaohui}
```

以上是没有设置注解名称的情况，但是开启了**useActualParamName**开关；

```
{0=158, 1=zhaohui, param1=158, param2=zhaohui}
```

以上是没有设置注解名称的情况，并且关闭了**useActualParamName**开关；

有了以上三种情况，所以我们在xxMapper.xml也会有不同的配置方式，下面根据以上三种情况看看在xxMapper.xml中如何配置：  
**第一种情况**，xxMapper.xml可以这样配置：

```
    <select id="selectBlog3" parameterType="hashmap" resultType="blog">
        select * from blog where id = #{id} and author=#{author}
    </select>
    <select id="selectBlog3" parameterType="hashmap" resultType="blog">
        select * from blog where id = #{param1} and author=#{param2}
    </select>
```

**第二种情况**，xxMapper.xml可以这样配置：

```
    <select id="selectBlog3" parameterType="hashmap" resultType="blog">
        select * from blog where id = #{arg0} and author=#{arg1}
    </select>
    <select id="selectBlog3" parameterType="hashmap" resultType="blog">
        select * from blog where id = #{param1} and author=#{param2}
    </select>
```

**第三种情况**，xxMapper.xml可以这样配置：

```
    <select id="selectBlog3" parameterType="hashmap" resultType="blog">
        select * from blog where id = #{0} and author=#{1}
    </select>
    <select id="selectBlog3" parameterType="hashmap" resultType="blog">
        select * from blog where id = #{param1} and author=#{param2}
    </select>
```

正是因为Mybatis在初始化参数映射的时候提供了多种key值，更加方便开发者灵活的设置值；虽然提供了多个key值选择，但个人认为还是设置明确的注解更加规范；

## 总结

本文重点介绍了SqlCommand和MethodSignature这两个类的实例化过程，SqlCommand重点介绍了方法名的映射通过接口路径+方法名的方式和xxMapper.xml中的namespace+statementID进行映射，并且将到了递归父类的问题；然后就是方法签名，通过方法的返回值类型创建了四个标识；最后讲了参数的映射问题，Mybatis给开发者提供了多样的映射key。

## 示例代码地址

[Github](https://github.com/ksfzhaohui/blog/tree/master/mybatis)