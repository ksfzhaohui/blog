## 前言

上文[Mybatis之方法如何映射到XML](https://my.oschina.net/OutOfMemory/blog/3127729)中介绍了Mybatis是如何将方法进行分拆出方法名映射到statementID，参数如何解析成xml中sql所需要的，以及返回类型的处理；本文将从XML端来看是如何同方法端进行映射的。

## XML映射类

前两篇文章中了解到通过Mapper类路径+方法名映射xxMapper.xml中的namespace+statementID，而namespace+statementID块其实在初始化的时候在Configuration中保存在MappedStatement中，所以我们在增删改查的时候都会看到如下代码：

```
MappedStatement ms = configuration.getMappedStatement(statement);
```

在Configuration中获取指定namespace+statementID的MappedStatement，而在Configuration是通过Map维护了对应关系；已最常见的Select语句块为例，在XML中的配置的结构如下：

```
<select
  id="selectPerson"
  parameterType="int"
  parameterMap="deprecated"
  resultType="hashmap"
  resultMap="personResultMap"
  flushCache="false"
  useCache="true"
  timeout="10"
  fetchSize="256"
  statementType="PREPARED"
  resultSetType="FORWARD_ONLY">
  ...sql语句...
</select>
```

其他增删改除了个别的几个关键字比如：keyProperty，keyColumn等，其他和select标签类似；再来看一下MappedStatement类中相关的属性：

```
public final class MappedStatement {

  private String resource;
  private Configuration configuration;
  private String id;
  private Integer fetchSize;
  private Integer timeout;
  private StatementType statementType;
  private ResultSetType resultSetType;
  private SqlSource sqlSource;
  private Cache cache;
  private ParameterMap parameterMap;
  private List<ResultMap> resultMaps;
  private boolean flushCacheRequired;
  private boolean useCache;
  private boolean resultOrdered;
  private SqlCommandType sqlCommandType;
  private KeyGenerator keyGenerator;
  private String[] keyProperties;
  private String[] keyColumns;
  private boolean hasNestedResultMaps;
  private String databaseId;
  private Log statementLog;
  private LanguageDriver lang;
  private String[] resultSets;
  ...省略...
}
```

select标签里面的关键字基本可以在类MappedStatement中找到对应的属性，关于每个属性代表的含义可以参考官方文档：[mybatis-3](https://mybatis.org/mybatis-3/sqlmap-xml.html)；除了关键字还有sql语句，对应的是MappedStatement中的SqlSource，sql语句有动态和静态的区别，对应的SqlSource也提供了相关的子类：StaticSqlSource和DynamicSqlSource，相关的sql解析类在XMLScriptBuilder中：

```
  public SqlSource parseScriptNode() {
    MixedSqlNode rootSqlNode = parseDynamicTags(context);
    SqlSource sqlSource = null;
    if (isDynamic) {
      sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
    } else {
      sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
    }
    return sqlSource;
  }
```

具体哪种sql是动态的，哪种是静态的，相关逻辑在parseDynamicTags中判断的，此处大致说一下其中的原理：遇到**${}**和动态标签如**<if>，<set>，<foreach>**则为DynamicSqlSource，否则为StaticSqlSource也就是常见的**#{}**；在解析动态sql的时候Mybatis为每个标签专门提供了处理类NodeHandler，初始化信息如下：

```
  private void initNodeHandlerMap() {
    nodeHandlerMap.put("trim", new TrimHandler());
    nodeHandlerMap.put("where", new WhereHandler());
    nodeHandlerMap.put("set", new SetHandler());
    nodeHandlerMap.put("foreach", new ForEachHandler());
    nodeHandlerMap.put("if", new IfHandler());
    nodeHandlerMap.put("choose", new ChooseHandler());
    nodeHandlerMap.put("when", new IfHandler());
    nodeHandlerMap.put("otherwise", new OtherwiseHandler());
    nodeHandlerMap.put("bind", new BindHandler());
  }
```

处理完之后会生成对应的SqlNode如下图所示：  
![xx.jpg](https://user-gold-cdn.xitu.io/2019/11/10/16e530dafaf55c8c?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

不管是动态还是静态的SqlSource，最终都是为了获取BoundSql，如SqlSource接口中定义的：

```
public interface SqlSource {

  BoundSql getBoundSql(Object parameterObject);

}
```

这里的parameterObject就是上文中通过方法参数生成的sql参数对象，这样BoundSql包含了sql语句，客户端传来的参数，以及XML中配置的参数，直接可以进行映射处理；

## 参数映射

上节中将到了BoundSql，本节重点来介绍一下，首先可以看一下其相关属性：

```
public class BoundSql {

  private final String sql;
  private final List<ParameterMapping> parameterMappings;
  private final Object parameterObject;
  private final Map<String, Object> additionalParameters;
  private final MetaObject metaParameters;
  ...省略...
}
```

几个属性大致含义：要执行的sql语句，xml配置的参数映射，客户端传来的参数以及额外参数；已一个常见的查询为例可以看一下大致的内容：

```
    <select id="selectBlog3" parameterType="hashmap" resultType="blog">
        select * from blog where id = #{id} and author=#{author,jdbcType=VARCHAR,javaType=string}
    </select>
```

此时sql对应就是：

```
select * from blog where id = ? and author=?
```

parameterMappings对应的是：

```
ParameterMapping{property='id', mode=IN, javaType=class java.lang.Object, jdbcType=null, numericScale=null, resultMapId='null', jdbcTypeName='null', expression='null'}
ParameterMapping{property='author', mode=IN, javaType=class java.lang.String, jdbcType=VARCHAR, numericScale=null, resultMapId='null', jdbcTypeName='null', expression='null'}
```

parameterObject对应的是：

```
{author=zhaohui, id=158, param1=158, param2=zhaohui}
```

如果知道以上参数，我们就可以直接使用原生的PreparedStatement来操作数据库了：

```
PreparedStatement prestmt = conn.prepareStatement("select * from blog where id = ? and author=?");
prestmt.setLong(1,id);
prestmt.setString(2,author);
```

其实Mybatis本质上和上面的语句没有区别，可以看一下Mybatis是如何处理参数的，具体实现在DefaultParameterHandler中，如下所示：

```
 public void setParameters(PreparedStatement ps) {
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
      for (int i = 0; i < parameterMappings.size(); i++) {
        ParameterMapping parameterMapping = parameterMappings.get(i);
        if (parameterMapping.getMode() != ParameterMode.OUT) {
          Object value;
          String propertyName = parameterMapping.getProperty();
          if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
            value = boundSql.getAdditionalParameter(propertyName);
          } else if (parameterObject == null) {
            value = null;
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            value = parameterObject;
          } else {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            value = metaObject.getValue(propertyName);
          }
          TypeHandler typeHandler = parameterMapping.getTypeHandler();
          JdbcType jdbcType = parameterMapping.getJdbcType();
          if (value == null && jdbcType == null) {
            jdbcType = configuration.getJdbcTypeForNull();
          }
          try {
            typeHandler.setParameter(ps, i + 1, value, jdbcType);
          } catch (TypeException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          } catch (SQLException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          }
        }
      }
    }
  }
```

大致就是遍历parameterMappings，然后通过propertyName到客户端参数parameterObject中获取对应的值，获取到值之后就面临一个问题一个就是客户端参数的类型，另一个就是xml配置的类型，如何进行转换，Mybatis提供了TypeHandler来进行转换，这是一个接口类，其实现包括了常用的基本类型，Map，对象，时间等；具体使用哪种类型的TypeHandler，根据我们在xml中配置的**<javaType=类型>**来决定，如果没有配置则使用UnknownTypeHandler，UnknownTypeHandler内部会根据value的类型来决定使用具体的TypeHandler；Mybatis内部所有的类型都注册在TypeHandlerRegistry中，所以获取的时候直接根据value类型直接去TypeHandlerRegistry获取即可；获取之后直接调用**typeHandler.setParameter(ps, i + 1, value, jdbcType)**，已StringTypeHandler为例，可以看一下具体实现：

```
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter);
  }
```

使用了原生的PreparedStatement，往指定位置设置参数值；设置完参数之后就执行execute方法，返回结果；

## 结果映射

上一节执行execute之后，返回的是ResultSet结果集，如果直接用原生的读取方式，你会看到如下代码：

```
ResultSet resultSet = preparedStatement.executeQuery();
while (resultSet.next()) {
      Long id = resultSet.getLong("id");
      String title = resultSet.getString("title");
      String author = resultSet.getString("author");
      String content = resultSet.getString("content");
      ......
}
```

获取到每个字段的数据之后，然后通过反射的方式生成一个对象；Mybatis内部其实也是这样实现的，封装好通过简单的配置即可获取结果集，常见的结果集配置如resultMap，resultType等；Mybatis内部处理ResultSet是ResultSetHandler，其具体实现是DefaultResultSetHandler类：

```
public interface ResultSetHandler {

  <E> List<E> handleResultSets(Statement stmt) throws SQLException;

  <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

  void handleOutputParameters(CallableStatement cs) throws SQLException;

}
```

处理接口中一共定一个了三个方法分别是：处理普通的结果集，处理游标结果集，以及处理输出参数，可以大致看一下最常用的handleResultSets实现：

```
public List<Object> handleResultSets(Statement stmt) throws SQLException {
    ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

    final List<Object> multipleResults = new ArrayList<Object>();

    int resultSetCount = 0;
    ResultSetWrapper rsw = getFirstResultSet(stmt);

    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount);
    while (rsw != null && resultMapCount > resultSetCount) {
      ResultMap resultMap = resultMaps.get(resultSetCount);
      handleResultSet(rsw, resultMap, multipleResults, null);
      rsw = getNextResultSet(stmt);
      cleanUpAfterHandlingResultSet();
      resultSetCount++;
    }
    ...以下省略...
  }
```

while循环遍历结果集，ResultMap就是在XML中定义的结果集，比如定一个的是一个类型Blog，那么会在处理结果的时候，首先创建一个对象，然后给对象属性分配类型处理器TypeHandler，然后根据实际类型调用处理器的getResult方法：

```
public interface TypeHandler<T> {

  void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

  T getResult(ResultSet rs, String columnName) throws SQLException;

  T getResult(ResultSet rs, int columnIndex) throws SQLException;

  T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
```

可以看到类型处理器分别用来处理设置参数和从结果集获取参数，也就是分别处理了输入和输出；处理完之后其实就生成了XML中配置的结果集，可能是一个对象，列表，hashMap等；另外一个需要注意的地方就是xxMapper接口中定义的返回值需要保证和XML中配的结果集一致，不然当我们通过代理对象返回结果集的时候会出现类型转换异常；

## 总结

XML的映射本文分三块来介绍的，分别从Statement块映射MappedStatement，参数映射ParameterMapping，以及结果集是如何通过DefaultResultSetHandler处理的；当然本文只是介绍了一个大概的映射流程，很多细节没有讲到，比如ResultHandler，RowBounds，缓存等；后面每个细节都会单独的写一篇文章来介绍。

## 示例代码地址

[Github](https://github.com/ksfzhaohui/blog/tree/master/mybatis)