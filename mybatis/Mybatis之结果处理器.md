## 前言

在上文[Mybatis之方法如何映射到XML](https://my.oschina.net/OutOfMemory/blog/3127729)中讲到需要实例化SqlCommand和MethodSignature两个类，在MethodSignature初始化的时候有一个resultHandlerIndex的参数用来指定是否设置了ResultHandler参数，本文将重点ResultHandler如何使用，分析如何触发的以及如何自定义结果处理器。

## 使用结果处理器

首先看一下Mybatis提供的结果处理器接口类ResultHandler：

```
public interface ResultHandler<T> {

  void handleResult(ResultContext<? extends T> resultContext);

}
```

还是比较简单的，就只有一个handleResult的方法，方法参数是ResultContext里面存放了正常执行sql的结果，这里的结果处理器其实就是对结果进行二次加工处理；Mybatis提供了两个默认的处理器分别是：DefaultResultHandler和DefaultMapResultHandler，分别处理list和map，下面看一下是如何使用这两个处理器的：

```
    public static void selectHandler(SqlSession session, Configuration configuration) {
        BlogMapper mapper = session.getMapper(BlogMapper.class);
        //DefaultResultHandler内置结果处理器
        DefaultResultHandler defaultHandler = new DefaultResultHandler();
        mapper.selectBlogsByHandler("zhaohui", defaultHandler);
        System.out.println(defaultHandler.getResultList());

        //DefaultMapResultHandler内置结果处理器
        DefaultMapResultHandler<Long, Blog> defaultMapResultHandler = new DefaultMapResultHandler<Long, Blog>("id",
                configuration.getObjectFactory(), configuration.getObjectWrapperFactory(),
                configuration.getReflectorFactory());
        mapper.selectBlogsByHandler("zhaohui", defaultMapResultHandler);
        System.out.println(defaultMapResultHandler.getMappedResults());
    }
```

上面的实例分别使用了两个内置处理器，分别处理获取到的相同结果；map处理器指定了id作为key；下面再看一下BlogMapper中的selectBlogsByHandler：

```
public void selectBlogsByHandler(String author, ResultHandler handler);

<select id="selectBlogsByHandler" parameterType="string" resultType="blog">
        select * from blog where author = #{author}
</select>
```

上面唯一要注意的点就是selectBlogsByHandler的返回值void类型，不然结果处理器是不会触发处理结果的，这个后面再分析为什么；看一下输出结果：

```
[Blog [id=159, title=hello java, author=zhaohui, content=hello java666], Blog [id=160, title=hello java, author=zhaohui, content=hello java666]]
{160=Blog [id=160, title=hello java, author=zhaohui, content=hello java666], 159=Blog [id=159, title=hello java, author=zhaohui, content=hello java666]}
```

## 处理流程

首先我们需要了解结果处理器在什么情况下才会被触发，其次再看什么时候被调用的，最后我们在分析一下内置的处理器什么时候被使用的；

#### 1.触发条件

上一节中提到需要指定void返回类型，主要原因可以查看MapperMethod的execute方法，MapperMethod在[Mybatis之方法如何映射到XML](https://my.oschina.net/OutOfMemory/blog/3127729)中是有介绍的，重点讲到了SqlCommand和MethodSignature类，下面看一下execute方法部分代码：

```
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      ...省略...
      case SELECT:
        if (method.returnsVoid() && method.hasResultHandler()) {
          executeWithResultHandler(sqlSession, args);
          result = null;
        } 
      ...省略...
    return result;
  }
```

可以看到什么情况下才会调用结果处理器有**三个要求**：  
1.首先必须是查询命令，其他的增删改是没有结果处理器的；  
2.其次是方法的返回值必须是void，这其实也好理解，如果本身方法就有返回值，结果处理器也有返回值，反而容易搞混；  
3.必须要有结果处理器，这个肯定是必须的，没有也别谈结果处理器了。

#### 2.何时调用

具体何时调用我们自己的结果处理器，相关处理主要在DefaultResultSetHandler中，也就是在处理结果映射的时候，更多可以参考[Mybatis之XML如何映射到方法](https://my.oschina.net/OutOfMemory/blog/3127933)，在主方法handleResultSets中调用的handleResultSet方法：

```
 private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
    try {
      if (parentMapping != null) {
        handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
      } else {
        if (resultHandler == null) {
          DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
          handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
          multipleResults.add(defaultResultHandler.getResultList());
        } else {
          handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
        }
      }
    } finally {
      // issue #228 (close resultsets)
      closeResultSet(rsw.getResultSet());
    }
  }
```

重点看一下没有指定parentMapping的情况下，如果没有指定resultHandler则系统会创建一个默认的DefaultResultHandler，如果有则用用户自己的处理器；然后就是通过[对象工厂](https://my.oschina.net/OutOfMemory/blog/3128145)创建对象，然后通过[类型处理器](https://my.oschina.net/OutOfMemory/blog/3128578)读取ResultSet，最后调用callResultHandler方法来调用处理器：

```
  private void callResultHandler(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue) {
    resultContext.nextResultObject(rowValue);
    ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
  }
```

把封装好数据的对象rowValue放入resultContext中，然后传入处理器中进行处理；所以需要注意的是如果有多条记录其实是一条一条传入结果处理器进行处理的，并不是生成一个list然后交给ResultHandler处理，但是内置的Map结果集却不是这样处理的，接下来重点看一下Mybatis内置的两个处理器；

#### 3.内置结果处理器

3.1 DefaultResultHandler  
内置了一个ArrayList<Object>对象，可以简单的理解为将结果集放入list中；但是上面我们也看到DefaultResultHandler并不是给用户使用的，而是Mybatis自己使用的，在用户没有指定处理器，Mybatis会自己创建一个DefaultResultHandler，而这个处理器是一个局部对象，每次getResultList之后其实还是放在了一个multipleResults中：

```
public List<Object> handleResultSets(Statement stmt) throws SQLException {
    final List<Object> multipleResults = new ArrayList<Object>();
    ...处理ResultSet...
```

这里定义的multipleResults是否可以替换成DefaultResultHandler，感觉会更加合理；

3.2 DefaultMapResultHandler  
内置了一个HashMap对象，此类也是被Mybatis内部使用在处理结果集是Map时使用，具体代码如下：

```
  public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
    final List<? extends V> list = selectList(statement, parameter, rowBounds);
    final DefaultMapResultHandler<K, V> mapResultHandler = new DefaultMapResultHandler<K, V>(mapKey,
        configuration.getObjectFactory(), configuration.getObjectWrapperFactory(), configuration.getReflectorFactory());
    final DefaultResultContext<V> context = new DefaultResultContext<V>();
    for (V o : list) {
      context.nextResultObject(o);
      mapResultHandler.handleResult(context);
    }
    return mapResultHandler.getMappedResults();
  }
```

这里首先获取结果集list，然后遍历结果集通过DefaultMapResultHandler转成Map结果集；这里个人感觉可以直接把DefaultMapResultHandler作为参数传入selectList中，这样最后就无需再次遍历一遍；

## 自定义结果处理器

自定义一个结果处理器也很简单，实现ResultHandler接口即可，比如下面的Map处理器：

```
public class MyResultHandler implements ResultHandler<Blog> {

    Map<Long, Blog> result = new HashMap<Long, Blog>();

    @Override
    public void handleResult(ResultContext<? extends Blog> resultContext) {
        Blog blog = resultContext.getResultObject();
        System.out.println(blog.toString());
        result.put(blog.getId(), blog);
    }

    public Map<Long, Blog> getResult() {
        return result;
    }

}
```

简单测试一下，同样查询Blog，如下所示：

```
    public static void selectMyHandler(SqlSession session) {
        BlogMapper mapper = session.getMapper(BlogMapper.class);
        MyResultHandler handler = new MyResultHandler();
        mapper.selectBlogsByHandler("zhaohui", handler);
        System.out.println(handler.getResult());
    }
```

日志输出如下：

```
Blog [id=159, title=hello java, author=zhaohui, content=hello java666]
Blog [id=160, title=hello java, author=zhaohui, content=hello java666]
{160=Blog [id=160, title=hello java, author=zhaohui, content=hello java666], 159=Blog [id=159, title=hello java, author=zhaohui, content=hello java666]}
```

可以发现分别打印了两次Blog，因为每次生成一个Blog对象都会调用一次handleResult；

## 总结

本文首先介绍了如何使用结果处理器，然后引出什么情况下才能触发处理器需要有三个条件，以及Mybatis内置的两个处理器分别处理list和map，最后自定义了一个简单的结果处理器。

## 示例代码地址

[Github](https://github.com/ksfzhaohui/blog/tree/master/mybatis)