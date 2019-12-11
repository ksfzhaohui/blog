## 前言

分页可以说是非常常见的一个功能，大部分主流的数据库都提供了物理分页方式，比如Mysql的limit关键字，Oracle的ROWNUM关键字等；Mybatis作为一个ORM框架，也提供了分页功能，接下来详细介绍Mybatis的分页功能。

## RowBounds分页

### 1.RowBounds介绍

Mybatis提供了RowBounds类进行分页处理，内部提供了offset和limit两个值，分别用来指定查询数据的开始位置和查询数据量：

```
public class RowBounds {

  public static final int NO_ROW_OFFSET = 0;
  public static final int NO_ROW_LIMIT = Integer.MAX_VALUE;
  public static final RowBounds DEFAULT = new RowBounds();

  private final int offset;
  private final int limit;

  public RowBounds() {
    this.offset = NO_ROW_OFFSET;
    this.limit = NO_ROW_LIMIT;
  }
}
```

默认是从0下标开始，查询数据量为Integer.MAX_VALUE；查询的时候没有指定RowBounds的时候默认RowBounds.DEFAULT:

```
  public <E> List<E> selectList(String statement, Object parameter) {
    return this.selectList(statement, parameter, RowBounds.DEFAULT);
  }
```

### 2.RowBounds使用

使用也很简单，只需要知道总记录数，然后设置好每页需要查询的数量，计算出一共分多少次查询，然后通过RowBounds指定下标，大致代码如下：

```
    public String rowBounds() {
        int pageSize = 10;
        int totalCount = blogRepository.countBlogs();
        int totalPages = (totalCount % pageSize == 0) ? totalCount / pageSize : totalCount / pageSize + 1;
        System.out.println("[pageSize=" + pageSize + ",totalCount=" + totalCount + ",totalPages=" + totalPages + "]");
        for (int currentPage = 0; currentPage < totalPages; currentPage++) {
            List<Blog> blogs = blogRepository.selectBlogs("zhaohui", new RowBounds(currentPage * pageSize, pageSize));
            System.err.println("currentPage=" + (currentPage + 1) + ",current size:" + blogs.size());
        }
        return "ok";
    }
```

pageSize每次查询数量，totalCount总记录数，totalPages总共分多少次查询；

### 3.RowBounds分析

Mybatis处理分页的相关代码在DefaultResultSetHandler中，部分代码如下：

```
private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping)
      throws SQLException {
    DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
    ResultSet resultSet = rsw.getResultSet();
    //跳过指定的下标Offset
    skipRows(resultSet, rowBounds);
    ////判定当前是否读取是否在limit内
    while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed() && resultSet.next()) {
      ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap, null);
      Object rowValue = getRowValue(rsw, discriminatedResultMap, null);
      storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
    }
  }
  
  //跳过指定的下标Offset
  private void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
    if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
      if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
        rs.absolute(rowBounds.getOffset());
      }
    } else {
      for (int i = 0; i < rowBounds.getOffset(); i++) {
        if (!rs.next()) {
          break;
        }
      }
    }
  }
  
  //判定当前是否读取是否在limit内
  private boolean shouldProcessMoreRows(ResultContext<?> context, RowBounds rowBounds) {
    return !context.isStopped() && context.getResultCount() < rowBounds.getLimit();
  }
```

在处理ResultSet首先需要跳过指定的下标Offset，这里跳过方式分成了两种情况：resultSetType为TYPE\_FORWARD\_ONLY和resultSetType为非TYPE\_FORWARD\_ONLY类型，Mybatis也提供了类型配置，可选项包括：

-   FORWARD_ONLY：只能向前滚动；
-   SCROLL_SENSITIVE： 能够实现任意的前后滚动，对修改敏感；
-   SCROLL_INSENSITIVE：能够实现任意的前后滚动，对修不改敏感；
-   DEFAULT：默认值为FORWARD_ONLY；

类型为FORWARD_ONLY的情况下只能遍历到指定的下标，而其他两种类型可以直接通过absolute方法定位到指定下标，可以通过如下方式指定类型：

```
    <select id="selectBlogs" parameterType="string" resultType="blog" resultSetType="SCROLL_INSENSITIVE ">
        select * from blog where author = #{author}
    </select>
```

limit限制，通过ResultContext中记录的resultCount记录当前读取的记录数，然后判定是否已经达到limit限制；

## Pagehelper分页

除了官方提供的RowBounds分页方式，比较常用的有第三方插件Pagehelper；为什么已经有官方提供的分页方式，还出现了Pagehelper这样的第三方插件，主要原因还是RowBounds提供的是逻辑分页，而Pagehelper提供了物理分页；

### 1.Pagehelper使用

Pagehelper主要利用了[Mybatis的插件](https://my.oschina.net/OutOfMemory/blog/3138490)功能，所以在使用的时候首先需要配置插件类PageInterceptor：

```
        <plugin interceptor="com.github.pagehelper.PageInterceptor">
            <property name="helperDialect" value="mysql" />
        </plugin>
```

helperDialect用来指定何种方言，这里使用mysql进行测试；更多详细的参数配置可以参考官方文档：[Mybatis-PageHelper](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md)；具体如何调用，Mybatis-PageHelper也提供了多种方式，这里使用RowBounds方式的调用，具体代码和上面的实例代码完全一样，只不过因为插件的存在，使其实现方式发生改变；

### 2.Pagehelper分析

Pagehelper通过对Executor的query方法进行拦截，具体如下：

```
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class PageInterceptor implements Interceptor {
}
```

在上文[Mybatis之插件分析](https://my.oschina.net/OutOfMemory/blog/3138490)中介绍了插件利用了动态代理技术，在执行Executor的query方法时，会自动触发InvocationHandler的invoke方法，方法内会调用PageInterceptor的intercept方法：

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

可以看到最终query的相关参数args（4个或者6个），都封装到了Invocation中，其中就包括了用于分页的RowBounds类；Pagehelper会将RowBounds中的offset和limit映射到功能更强大的Page类， Page里面包含了很多属性，这里就简单看一下和RowBounds相关的：

```
    public Page(int[] rowBounds, boolean count) {
        super(0);
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
        } else {
            this.pageSize = rowBounds[1];
            this.pageNum = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1])) : 0;
        }
        this.startRow = rowBounds[0];
        this.count = count;
        this.endRow = this.startRow + rowBounds[1];
    }
```

offset映射到了startRow，limit映射到了pageSize；有了相关分页的参数，然后通过配置的数据库方言类型，生成不同的方言生成sql，比如Mysql提供了MySqlRowBoundsDialect类：

```
public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (rowBounds.getOffset() == 0) {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(rowBounds.getLimit());
        } else {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(rowBounds.getOffset());
            sqlBuilder.append(",");
            sqlBuilder.append(rowBounds.getLimit());
            pageKey.update(rowBounds.getOffset());
        }
        pageKey.update(rowBounds.getLimit());
        return sqlBuilder.toString();
    }
```

mysql的物理分页关键字是Limit，提供offset和limit即可实现分页；

## 性能对比

RowBounds利用的是逻辑分页，而Pagehelper利用的物理分页；  
**逻辑分页**：逻辑分页利用游标分页，好处是所有数据库都统一，坏处就是效率低；利用ResultSet的滚动分页，由于ResultSet带有游标，因此可以使用其next()方法来指向下一条记录；当然也可以利用Scrollable ResultSets(可滚动结果集合)来快速定位到某个游标所指定的记录行，所使用的是ResultSet的absolute()方法；  
**物理分页**：数据库本身提供了分页方式，如mysql的limit，好处是效率高，不好的地方就是不同数据库有不同分页方式，需要为每种数据库单独分页处理；

下面分别对逻辑分页向前滚动，逻辑分页前后滚动，以及物理分页三种分页方式查询100条数据进行测试，使用druid进行监控，使用的数据库是mysql；

### 1.逻辑分页向前滚动

因为只能向前滚动，所有越往后面的分页，遍历的数据越多，监控如下：  
![](https://oscimg.oschina.net/oscnet/up-736fb9cdfa5aec405ea66bc4c751363fc20.JPEG)  
虽然只有100条数据，但是读取数据为550行，性能低下；

### 2.逻辑分页前后滚动

这里配置的resultSetType为SCROLL_INSENSITIVE，可以快速定位，监控如下：  
![](https://oscimg.oschina.net/oscnet/up-961478beeec9bb2002bdafd036d66428d36.JPEG)

### 3.物理分页

配置使用Pagehelper插件，监控如下：  
![](https://oscimg.oschina.net/oscnet/up-a120738b9acad771366c0015600bfa81c22.JPEG)

可以看到物理分页在执行时间和读取行数都更占优；

## 总结

本文分别介绍了RowBounds和Pagehelper两种分页方式，分别代表了逻辑分页和物理分页；以及这两种方式是内部是如何实现的；最后文末做了一个简单的性能测试。

## 示例代码

[Github](https://github.com/ksfzhaohui/blog/tree/master/druid)