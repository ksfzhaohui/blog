## 前言

在上文[Mybatis之XML如何映射到方法](https://my.oschina.net/OutOfMemory/blog/3127933)中讲到结果映射的时候，需要创建好对象，然后再给对象的属性赋值，而创建对象就用到了Mybatis的内置的对象工厂类DefaultObjectFactory，当然Mybatis也提供了扩展机制，用户可以实现自己的对象工厂。

## 对象工厂

上文中介绍了结果映射的相关逻辑在DefaultResultSetHandler处理器中，下面重点看一下创建结果对象的方法：

```
  private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix)
      throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final MetaClass metaType = MetaClass.forClass(resultType, reflectorFactory);
    final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
    if (hasTypeHandlerForResultObject(rsw, resultType)) {
      return createPrimitiveResultObject(rsw, resultMap, columnPrefix);
    } else if (!constructorMappings.isEmpty()) {
      return createParameterizedResultObject(rsw, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix);
    } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
      return objectFactory.create(resultType);
    } else if (shouldApplyAutomaticMappings(resultMap, false)) {
      return createByConstructorSignature(rsw, resultType, constructorArgTypes, constructorArgs, columnPrefix);
    }
    throw new ExecutorException("Do not know how to create an instance of " + resultType);
  }
```

这是创建结果对象的核心方法，创建对象的时候分成了4中情况分别是：  
1.结果对象Mybatis本身提供了处理器，也就是xxTypeHandler，更多实现在**org.apache.ibatis.type**路径下，这种情况可以直接从Resultset中获取结果返回结果值，可以认为都是原生的类型；  
2.在结果映射中指定了构造器，无需使用默认的构造器；  
3.结果对象是接口或者结果对象有默认的构造器；  
4.以上情况都不满足会检查是否配置了自动映射，默认开启；  
以上情况都不满足则直接抛出异常，下面具体分析一下4种类型都在什么情况下执行；

#### 1.原生类型

表示结果集是原生类型，比如string类型，相关xml配置例如：

```
    <select id="selectBlog" parameterType="hashmap" resultType="string">
        select title from blog where id = #{id} and
        author=#{author,javaType=string}
    </select>
```

直接返回了原生类型string，Mybatis提供了StringTypeHandler处理器：

```
private Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final String columnName;
    if (!resultMap.getResultMappings().isEmpty()) {
      final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
      final ResultMapping mapping = resultMappingList.get(0);
      columnName = prependPrefix(mapping.getColumn(), columnPrefix);
    } else {
      columnName = rsw.getColumnNames().get(0);
    }
    final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
    return typeHandler.getResult(rsw.getResultSet(), columnName);
  }
```

首先获取字段名称，然后通过返回类型获取处理器，最后直接从ResultSet中获取结果；在获取字段名称的时候也分两种情况分别是直接配置resultType和配置resultMap两种情况，不管哪种情况如果配置了多个映射字段都只获取第一个，比如：

```
select id,title from blog where id = #{id} and author=#{author,javaType=string}
```

这种情况只会返回id的值，并且被转为string类型；

#### 2.指定构造器

在resultmap中指定了构造器参数，比如如下的例子：

```
    <resultMap id="blogResultMap" type="blog">
        <constructor>
            <idArg column="id" javaType="long"/>
        </constructor>
        <result property="title" column="title" />
    </resultMap>
```

如上所示指定了id为参数的构造器，这种情况在通过对象工厂创建对象的时候不会使用默认的无参构造器，会使用带id参数的构造器，部分代码如下所示：

```
objectFactory.create(resultType, constructorArgTypes, constructorArgs)
```

三个参数分别是：返回的对象类型，构造器参数类型，构造器参数值；

#### 3.默认构造器

无需指定构造器参数类型和构造器参数值，当然前提是类提供了默认的构造器，直接调用create方法：

```
objectFactory.create(resultType)
```

同指定构造器的方式唯一的区别就是构造器参数类型和构造器参数值都为null；下面具体看一下对象工厂的默认实现：

```
@Override
  public <T> T create(Class<T> type) {
    return create(type, null, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    Class<?> classToCreate = resolveInterface(type);
    // we know types are assignable
    return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
  }
  
private  <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    try {
      Constructor<T> constructor;
      if (constructorArgTypes == null || constructorArgs == null) {
        constructor = type.getDeclaredConstructor();
        if (!constructor.isAccessible()) {
          constructor.setAccessible(true);
        }
        return constructor.newInstance();
      }
      constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
      if (!constructor.isAccessible()) {
        constructor.setAccessible(true);
      }
      return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
    } catch (Exception e) {
      ...省略...
    }
  }
```

分别提供了有参数和无参数的构造器，没有参数的时候直接调用默认的构造器；有指定构造器参数类型，则获取有参数类型的构造器，当然对应的类里面需要提供对应参数的构造器，不然会报错；获取构造器之后，直接通过newInstance创建类对象；

#### 4.自动映射

即没有提供默认的构造器也没有提供指定构造参数的构造器，那么Mybatis会进行自动映射；自动映射是有一个开关的，默认开启，可以在configuration里面和resultMap里面配置autoMapping开关；准备如下实例看具体是如何映射的：

```
    <resultMap id="blogResultMap" type="blog" autoMapping="true">
        <result property="id" column="id" />
        <result property="title" column="title" />
        <result property="content" column="content"/>
    </resultMap>
    <select id="selectBlogMap" parameterType="hashmap" resultMap="blogResultMap">
        select id,title from blog where id = #{id} and
        author=#{author,javaType=string}
    </select>
```

blog类里面提供了id为参数的构造器，这样就没有了默认的构造器；这时候Mybatis会找blog里面是否存在id，title类参数的构造器，如果存在则获取此构造器创建对象，不存在则报错，如下所示：

```
Caused by: org.apache.ibatis.executor.ExecutorException: No constructor found in com.mybatis.vo.Blog matching [java.lang.Long, java.lang.String]
```

#### 5.无法创建对象

如果第四种情况也不满足，即可以配置**autoMapping="false"**，则Mybatis直接抛出无法创建对象，具体异常如下所示：

```
Caused by: org.apache.ibatis.executor.ExecutorException: Do not know how to create an instance of class com.mybatis.vo.Blog
```

## 自定义对象工厂

实现自己的对象工厂也很简单，实现接口ObjectFactory或者重载DefaultObjectFactory即可，此处为了方便我们直接重载DefaultObjectFactory，并实现当需要实例化的对象是Blog时，如果没有指定author则给定一个默认值，实现如下：

```
public class MyObjectFactory extends DefaultObjectFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public <T> T create(Class<T> type) {
        System.out.println("create:" + type);
         if (type.equals(Blog.class)){
             Blog blog = (Blog)super.create(type);
             blog.setAuthor("ksfzhaohui");
             return (T) blog;
         }
        return super.create(type);
    }
    
    ...省略...
}
```

实现也很简单，只需要在create方法中判定指定的类型为Blog，然后调用父类的create方法创建对象，然后设置默认值；当然还需要在configuration中配置自定义对象工厂才能生效：

```
    <objectFactory type="com.mybatis.myObjectFactory.MyObjectFactory"> 
            <property name="name" value="MyObjectFactory"/> 
    </objectFactory>
```

## 总结

本文重点介绍了默认的对象工厂和创建对象的五种情况分别是：原生的类型，指定了构造器，有默认构造器的类，使用自动映射的情况以及不满足前面四种情况的处理；最后简单试下了一个自定义的对象工厂，用来给指定的类对象指定默认的属性值。

## 示例代码地址

[Github](https://github.com/ksfzhaohui/blog/tree/master/mybatis)