## 前言

上文[Mybatis之XML如何映射到方法](https://my.oschina.net/OutOfMemory/blog/3127933)中讲到了类型处理器，分别用在两个地方设置参数到数据库和从结果集中取出数据，根据不同的数据类型从类型注册器里面获取具体的类型处理器，分别进行处理；本文将重点介绍一下类型处理器，注册器，如何处理数据以及如何扩展。

## 类型处理器

类型处理器Mybatis提供了接口TypeHandler，并且Mybatis实现了主流数据库支持类型的处理器实现，具体有哪些类型如下所示：

| 处理器 | java类型 | 数据库类型 |
| --- | --- | --- |
| BooleanTypeHandler | Boolean,boolean | BOOLEAN,BIT |
| ByteTypeHandler | Byte,byte | TINYINT |
| ShortTypeHandler | Short,short | SMALLINT |
| IntegerTypeHandler | Integer,int | INTEGER |
| LongTypeHandler | Long,long | BIGINT |
| FloatTypeHandler | Float,float | FLOAT |
| DoubleTypeHandler | Double,double | DOUBLE |
| StringTypeHandler | String | CHAR,VARCHAR |
| ClobTypeHandler | String | CLOB,LONGVARCHAR |
| NStringTypeHandler | String | NVARCHAR,NCHAR |
| NClobTypeHandler | String | NCLOB |
| ArrayTypeHandler | Object | ARRAY |
| BigDecimalTypeHandler | BigDecimal | REAL,DECIMAL,NUMERIC |
| BlobTypeHandler | byte\[\] | BLOB,LONGVARBINARY |
| UnknownTypeHandler | Object | OTHER或者未指定类型 |
| DateTypeHandler | Date | TIMESTAMP |
| DateOnlyTypeHandler | Date | DATE |
| TimeOnlyTypeHandler | Date | TIME |
| SqlDateTypeHandler | java.sql.Date | DATE |
| SqlTimeTypeHandler | java.sql.Time | TIME |
| SqlTimestampTypeHandler | java.sql.Timestamp | TIMESTAMP |
| EnumTypeHandler | 枚举类 | VARCHAR或兼容的字符串类型 |

上面所有的类型处理器都可以在TypeHandlerRegistry注册器中找到，再来看一下TypeHandler提供的接口方法：

```
public interface TypeHandler<T> {

  void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

  T getResult(ResultSet rs, String columnName) throws SQLException;

  T getResult(ResultSet rs, int columnIndex) throws SQLException;

  T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}

```

四个方法分别表示：setParameter用来设置写入数据库的参数，getResult用来处理结果集和存储过程，可以通过字段名称和下标来处理；以上处理器并没有直接实现TypeHandler，而是继承于公共的BaseTypeHandler，以StringTypeHandler为例只需要实现很简单的操作：

```
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter);
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getString(columnName);
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return rs.getString(columnIndex);
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getString(columnIndex);
  }

```

分别表示往PreparedStatement中设置字符串参数，从ResultSet获取指定字段名称或者下标的字符串数据，以及从存储过程中获取指定下标字符串数据。

## 处理器注册器

所有处理器在启动的时候都注册到了TypeHandlerRegistry中，需要根据javaType或者jdbcType获取处理器时直接在注册器中获取即可，注册器中保存了相关的对应关系如下所示：

```
  private final Map<JdbcType, TypeHandler<?>> JDBC_TYPE_HANDLER_MAP;
  private final Map<Type, Map<JdbcType, TypeHandler<?>>> TYPE_HANDLER_MAP;
  private final TypeHandler<Object> UNKNOWN_TYPE_HANDLER = new UnknownTypeHandler(this);
  private final Map<Class<?>, TypeHandler<?>> ALL_TYPE_HANDLERS_MAP ;
  private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();
  private Class<? extends TypeHandler> defaultEnumTypeHandler = EnumTypeHandler.class;

```

以上定义的几个变量的含义大致如下：  
**JDBC\_TYPE\_HANDLER_MAP**：jdbcType对应的类型处理器Map；  
**TYPE\_HANDLER\_MAP**：一个javaType对应多个jdbcType类型，这也好理解比如String类型可以对象数据库里面的char，varchar，clob等；然后每个jdbcType对应各自的类型处理器；  
**UNKNOWN\_TYPE\_HANDLER**：在没有配置jdbcType的情况下使用默认的UnknownTypeHandler处理器，此类会更新客户端参数来自动匹配相应的处理器；  
**ALL\_TYPE\_HANDLERS_MAP**：所有的类型处理器类名称对应类型处理器；  
**NULL\_TYPE\_HANDLER_MAP**：空Map，用来做判定是否为空的；  
**defaultEnumTypeHandler**：枚举处理器；

有对应关系，这样在设置参数和处理结果集的时候可以直接根据javaType或者jdbcType获取对应的类型处理器，根据具体的处理器处理相关参数。

## 处理数据

用到类型处理器主要在两个地方分别是设置参数的时候和处理结果集的时候，下面分别看一下两种场景；

#### 1.设置参数

在文章Mybatis之XML如何映射到方法中我们将到了BoundSql，ParameterMapping以及处理参数的DefaultParameterHandler，看一段里面的核心代码：

```
 TypeHandler typeHandler = parameterMapping.getTypeHandler();
 JdbcType jdbcType = parameterMapping.getJdbcType();
 if (value == null && jdbcType == null) {
     jdbcType = configuration.getJdbcTypeForNull();
 }
 try {
     typeHandler.setParameter(ps, i + 1, value, jdbcType);
 } catch (TypeException e) {
     ...省略...
 }

```

直接从ParameterMapping中获取具体的类型处理器，这里面的类型处理器是在解析xxMapper.xml根据设置的类型来决定的，下面以一个实例来说明一下：

```
    <select id="selectBlogMap" parameterType="hashmap" resultType="blog">
        select id,title from blog where id = #{id} and author=#{author,javaType=string,jdbcType=VARCHAR}
    </select>

```

如上实例有个参数需要设置分别是id和author，因为id没有指定具体的javaType和jdbcType所以解析的时候id的类型处理器是UnknownTypeHandler，而author指定了具体的类型所以可以解析的时候指定StringTypeHandler类型处理器；那id是未知的类型处理器如何处理Long类型哪，可以看到UnknownTypeHandler内部有一个resolveTypeHandler方法：

```
  private TypeHandler<? extends Object> resolveTypeHandler(Object parameter, JdbcType jdbcType) {
    TypeHandler<? extends Object> handler;
    if (parameter == null) {
      handler = OBJECT_TYPE_HANDLER;
    } else {
      handler = typeHandlerRegistry.getTypeHandler(parameter.getClass(), jdbcType);
      // check if handler is null (issue #270)
      if (handler == null || handler instanceof UnknownTypeHandler) {
        handler = OBJECT_TYPE_HANDLER;
      }
    }
    return handler;
  }

```

可以看到没有配置类型的情况下，直接根据传递过来的参数类型从类型注册器中获取具体的处理器，比如这里的id是Long类型，所以最后获取的实际处理器是LongTypeHandler；

#### 2.处理结果集

结果集我们可以配置resultType和resultMap，在映射结果集的时候首先是需要通过对象工厂创建对象的，这个在上文Mybatis之对象工厂中详细介绍了，创建完对象之后需要往属性中设置值，至于如何获取值就是通过TypeHandler来实现的；如何获取对应的TypeHandler，使用resultType和resultMap有不同的方式：  
**1.resultType**  
根据指定的类型，结果集中的属性映射到类中的属性，然后获取类中属性的类型，可以参考部分代码：

```
final String property = metaObject.findProperty(propertyName, configuration.isMapUnderscoreToCamelCase());
if (property != null && metaObject.hasSetter(property)) {
      if (resultMap.getMappedProperties().contains(property)) {
        continue;
      }
      final Class<?> propertyType = metaObject.getSetterType(property);
      if (typeHandlerRegistry.hasTypeHandler(propertyType, rsw.getJdbcType(columnName))) {
           final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
           autoMapping.add(new UnMappedColumnAutoMapping(columnName, property, typeHandler, propertyType.isPrimitive()));
      } 
      ......
}

```

首先通过结果集中的属性获取到类对象中的属性名称，然后获取属性被定义的类型，通过此类型到注册器TypeHandlerRegistry中获取相应的处理器，最后通过调用处理器中的getResult方法获取对应的值，把值通过反射的方式赋值给通过对象工厂创建的对象。

**2.resultMap**  
resultMap可以直接在xml中指定相关的类型，比如：

```
<resultMap id="blogResultMap" type="blog" autoMapping="true">
        <result property="id" column="id" javaType="long" jdbcType="BIGINT"/>
        <result property="title" column="title" javaType="string" jdbcType="VARCHAR"/>
    </resultMap>

```

直接给属性指定javaType和jdbcType，这样在解析xml的时候直接可以分配相关的类型处理器；当然也可以不指定，如果不指定的话处理方式类似resultType的方式，也需要根据resultMap中指定的type类型，获取相关属性的类型，然后在获取TypeHandler。

## 扩展处理器

扩展处理器有两种情况分别是：已有的类型处理器比如StringTypeHandler，我们创建一个新的去覆盖它；另外就是创建一个之前没有处理类型的处理器，比如常见的枚举类型处理器；下面分别来扩展实现。

**1.覆盖扩展**  
实现自己的MyStringTypeHandler，继承于BaseTypeHandler，如下所示：

```
@MappedTypes({ String.class })
@MappedJdbcTypes(JdbcType.VARCHAR)
public class MyStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        System.out.println("MyStringTypeHandler->setNonNullParameter");
        ps.setString(i, parameter);
    }
；
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        System.out.println("MyStringTypeHandler->getNullableResult");
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        System.out.println("MyStringTypeHandler->getNullableResult");
        return rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        System.out.println("MyStringTypeHandler->getNullableResult");
        return cs.getString(columnIndex);
    }
}

```

需要指定注解MappedTypes和MappedJdbcTypes，分别表示javaType和jdbcType；然后实现BaseTypeHandler中四个抽象方法即可；当然也可以不用注解的方式，可以在configuration中进行类型的配置：

```
<typeHandlers>
        <typeHandler javaType="string" jdbcType="VARCHAR" handler="com.mybatis.myTypeHandler.MyStringTypeHandler" />
    </typeHandlers>

```

指定了javaType="string"以及jdbcType="VARCHAR" 的情况下使用MyStringTypeHandler进行处理；这样配完之后可能有一些问题，比如参数在没有指定jdbcType的情况下，还是会用默认的String类型，这是为什么了那，因为在TypeHandlerRegistry中初始化了String处理器：

```
register(String.class, new StringTypeHandler());

```

没有指定jdbcType的情况下会初始化null->StringTypeHandler；所以要解决这个问题，我们还可以继续添加一行如下所示：

```
<typeHandlers>
        <typeHandler javaType="string" handler="com.mybatis.myTypeHandler.MyStringTypeHandler" />
    </typeHandlers>

```

**2.枚举处理器扩展**  
Mybatis提供了两个默认的枚举类型处理器分别是：EnumTypeHandler和EnumOrdinalTypeHandler；分别表示用枚举字符串名称作为参数，另一个是使用整数下标作为参数传递；使用也很简单，使用如下配置：

```
<typeHandler javaType="com.mybatis.vo.AuthorEnum" handler="org.apache.ibatis.type.EnumTypeHandler" />

```

如果默认的两种方式不能满足，那可以自定义自己的枚举类型处理器，如下通过处理器通过id来获取枚举，代码如下：

```
public class AuthorEnumTypeHandler implements TypeHandler<AuthorEnum> {

    @Override
    public void setParameter(PreparedStatement ps, int i, AuthorEnum parameter, JdbcType jdbcType) throws SQLException {
        System.out.println("AuthorEnumTypeHandler->setParameter");
        ps.setInt(i, parameter.getId());
    }

    @Override
    public AuthorEnum getResult(ResultSet rs, String columnName) throws SQLException {
        int id = Integer.valueOf(rs.getString(columnName));
        System.out.println("AuthorEnumTypeHandler->getResult");
        return AuthorEnum.getAuthor(id);
    }

    @Override
    public AuthorEnum getResult(ResultSet rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        System.out.println("AuthorEnumTypeHandler->getResult");
        return AuthorEnum.getAuthor(id);
    }

    @Override
    public AuthorEnum getResult(CallableStatement cs, int columnIndex) throws SQLException {
        int id = cs.getInt(columnIndex);
        System.out.println("AuthorEnumTypeHandler->getResult");
        return AuthorEnum.getAuthor(id);
    }
}

```

## 总结

本文首先介绍了Mybatis默认有哪些类型处理器，以及每种处理器对应的java类型和jdbc类型；然后介绍了注册器是如何把保存javaType，jdbcType以及TypeHandler对应关系的；接下来介绍TypeHandler主要在两个地方分别是设置参数的时候和处理结果集；最后通过覆盖的方式和新建的方式来扩展处理器，并给出了实例。

## 完整代码

[Github](https://github.com/ksfzhaohui/blog/tree/master/mybatis/mybatis)