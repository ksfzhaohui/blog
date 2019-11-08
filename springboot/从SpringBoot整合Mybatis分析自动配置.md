## 前言

SpringBoot凭借"约定大于配置"的理念，已经成为最流行的web开发框架，所以有必须对其进行深入的了解；本文通过整合Mybatis类来分析SpringBoot提供的自动配置(AutoConfigure)功能，在此之前首先看一个整合Mybatis的实例。

## SpringBoot整合Mybatis

提供SpringBoot整合Mybatis的实例，通过Mybatis实现简单的增删改查功能；

### 1.表数据

```
CREATE TABLE `role` (
  `note` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `role_name` varchar(255) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

提供创建role表相关的sql，对表进行增删改查操作；

### 2.整合Mybatis的依赖

主要是mybatis-spring-boot-starter和使用的mysql驱动：

```
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.0.1</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.29</version>
</dependency>
```

### 3.配置application.properties

提供连接mysql相关的信息：url，驱动，用户名，密码；

```
spring.datasource.url=jdbc:mysql://localhost/mybatis
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```

### 4.提供bean和Dao

分别提供表对应的bean类和操作数据库的dao类；

```
public class Role {

    private long id;
    private String roleName;
    private String note;
    //省略get/set方法
}
```

```
@Mapper
public interface RoleDao {

    @Select("SELECT id,role_name as roleName,note FROM role WHERE id = #{id}")
    Role findRoleById(@Param("id") long id);
}
```

### 5.提供Service和Controller

```
public interface RoleService {

    public Role findRoleById(long roleId);

}

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    public Role findRoleById(long roleId) {
        return roleDao.findRoleById(roleId);
    }

}
```

```
@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @RequestMapping("/role")
    public String getRole(long id) {
        return roleService.findRoleById(id).toString();
    }

}
```

启动服务，进行简单的测试：[http://localhost](http://localhost/):8888/role?id=111  
结果如下：

```
Role [id=111, roleName=zhaohui, note=hello]
```

### 6.提出问题

如上实例中，我们使用了很少的配置，就通过mybatis实现了操作数据库；正常使用mybatis需要的SqlSessionFactory和SqlSession没有看到被实例化，同时mybatis依赖的数据源也没有看到被引用，那SpringBoot是如何帮我们自动配置的，下面重点分析一下；

## SpringBoot自动配置

### 1.自动配置注解

要想使用自动配置功能，SpringBoot提供了注解@EnableAutoConfiguration，当然不需要我们配置因为在@SpringBootApplication注解中默认以及启用了；

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
//...省略...
}
```

可以看到@SpringBootApplication注解本身也有注解@EnableAutoConfiguration：

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
//...省略...
}
```

在注解@EnableAutoConfiguration中重点看一下@Import注解中使用的AutoConfigurationImportSelector类，此类是自动注解的核心类，会有条件的加载我们默认指定的配置类；这里有两个概念一个是有条件，一个是配置类，分别简单介绍一下：配置类可以简单理解就是相关组件对接SpringBoot的对接类，此类可以做一些初始化的工作；有条件表示并不是有配置类就能被对接上，是有条件的，SpringBoot默认提供了大量配置类，但并不是所有配置类都能被加载初始化的，是有条件的，比如mybatis在没有数据源的情况下，没有mybatis基础包的情况下是不能被对接的；下面首先看一下SpringBoot提供的哪些条件类；

### 2.条件类

SpringBoot提供了很多条件类，可以在配置中上配置注解条件类，相关条件类可以在spring-boot-autoconfigure包下的org.springframework.boot.autoconfigure.condition下找到，主要包含如下：

-   ConditionalOnBean：当前容器有指定Bean的条件下；
-   ConditionalOnClass：当前类路径下有指定类的条件下；
-   ConditionalOnCloudPlatform：当指定了云平台的时候；
-   ConditionalOnExpression：SpEL表达式作为判断条件；
-   ConditionalOnJava：JVM版本作为判断条件；
-   ConditionalOnJndi：在JNDI存在的条件下查找指定的位置；
-   ConditionalOnMissingBean：当容器里没有指定Bean的情况下；
-   ConditionalOnMissingClass：当类路径下没有指定的类的条件下；
-   ConditionalOnNotWebApplication：当前项目不是WEB项目的条件下；
-   ConditionalOnProperty：当前应用是否配置了指定属性指定的值；
-   ConditionalOnResource：只有当指定的资源位于类路径下；
-   ConditionalOnSingleCandidate：bean工厂中只有一个或者有多个情况下是主要的候选bean；
-   ConditionalOnWebApplication：当前项目是WEB项目的条件下。

以上是注解类，注解本身没有功能，只是提供标记的功能，具体功能在@Conditional中指定的，比如ConditionalOnBean注解如下所示：

```
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnBean {
//...省略...
}
```

相关功能的实现就在OnBeanCondition类中，同样其他注解类的实现类也在包org.springframework.boot.autoconfigure.condition下找到；

### 3.自动配置过程

Springboot应用启动过程中使用ConfigurationClassParser分析配置类，此类中有一个processImports方法，此方法用来处理@Import注解，在@EnableAutoConfiguration注解存在@Import注解，这时候会实例化注解中的AutoConfigurationImportSelector，在其内部有一个AutoConfigurationGroup内部类，内部类有两个核心方法分别是：process和selectImports；

```
        @Override
        public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
            Assert.state(deferredImportSelector instanceof AutoConfigurationImportSelector,
                    () -> String.format("Only %s implementations are supported, got %s",
                            AutoConfigurationImportSelector.class.getSimpleName(),
                            deferredImportSelector.getClass().getName()));
            AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)
                    .getAutoConfigurationEntry(getAutoConfigurationMetadata(), annotationMetadata);
            this.autoConfigurationEntries.add(autoConfigurationEntry);
            for (String importClassName : autoConfigurationEntry.getConfigurations()) {
                this.entries.putIfAbsent(importClassName, annotationMetadata);
            }
        }
```

此方法主要获取经过条件过滤之后可用的自动配置类，主要调用AutoConfigurationImportSelector中的getAutoConfigurationEntry完成的：

```
    protected AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata,
            AnnotationMetadata annotationMetadata) {
        if (!isEnabled(annotationMetadata)) {
            return EMPTY_ENTRY;
        }
        AnnotationAttributes attributes = getAttributes(annotationMetadata);
        List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
        configurations = removeDuplicates(configurations);
        Set<String> exclusions = getExclusions(annotationMetadata, attributes);
        checkExcludedClasses(configurations, exclusions);
        configurations.removeAll(exclusions);
        configurations = filter(configurations, autoConfigurationMetadata);
        fireAutoConfigurationImportEvents(configurations, exclusions);
        return new AutoConfigurationEntry(configurations, exclusions);
    }
```

首先获取了所有备选的自动配置类，然后删除了重复和被排除的类，最后通过条件进行筛选出可用的配置类，下面分别看一下，首先看一下如何获取所有备选的配置类：

```
    protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
                getBeanClassLoader());
        Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
                + "are using a custom packaging, make sure that file is correct.");
        return configurations;
    }
```

通过SpringFactoriesLoader获取类路径下META-INF/spring.factories文件中key为**org.springframework.boot.autoconfigure.EnableAutoConfiguration**的配置类，可以看一下spring-boot-autoconfigure.jar中的spring.factories内容：

```
# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,\
org.springframework.boot.autoconfigure.cloud.CloudServiceConnectorsAutoConfiguration,\
//...以下省略...
```

当然这里只是截取了其中一个类路径jar下的部分配置，获取所有配置类之后进行去重，去被排除的类，然后进行条件过滤，下面重点看一下：

```
private List<String> filter(List<String> configurations, AutoConfigurationMetadata autoConfigurationMetadata) {
        long startTime = System.nanoTime();
        String[] candidates = StringUtils.toStringArray(configurations);
        boolean[] skip = new boolean[candidates.length];
        boolean skipped = false;
        for (AutoConfigurationImportFilter filter : getAutoConfigurationImportFilters()) {
            invokeAwareMethods(filter);
            boolean[] match = filter.match(candidates, autoConfigurationMetadata);
            for (int i = 0; i < match.length; i++) {
                if (!match[i]) {
                    skip[i] = true;
                    candidates[i] = null;
                    skipped = true;
                }
            }
        }
        if (!skipped) {
            return configurations;
        }
        List<String> result = new ArrayList<>(candidates.length);
        for (int i = 0; i < candidates.length; i++) {
            if (!skip[i]) {
                result.add(candidates[i]);
            }
        }
        if (logger.isTraceEnabled()) {
            int numberFiltered = configurations.size() - result.size();
            logger.trace("Filtered " + numberFiltered + " auto configuration class in "
                    + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
        }
        return new ArrayList<>(result);
    }
```

此方法大致就是首先获取配置的AutoConfigurationImportFilter ，然后对之前获取的所有配置类进行过滤，最后返回过滤之后的配置类；AutoConfigurationImportFilter同样也是通过SpringFactoriesLoader类进行加载类路径下META-INF/spring.factories，只不过当前的key是：**org.springframework.boot.autoconfigure.AutoConfigurationImportFilter**，可以看一下SpringBoot默认配置的filter：

```
# Auto Configuration Import Filters
org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
org.springframework.boot.autoconfigure.condition.OnBeanCondition,\
org.springframework.boot.autoconfigure.condition.OnClassCondition,\
org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition
```

可以看到Filter其实就是上文介绍的条件类，这里默认了OnBeanCondition，OnClassCondition以及OnWebApplicationCondition，已这里使用的Mybatis为例看一下MybatisAutoConfiguration的注解：

```
@org.springframework.context.annotation.Configuration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MybatisAutoConfiguration implements InitializingBean {
//...以下省略...
}
```

可以看到其中有用到@ConditionalOnClass，表示必须提供SqlSessionFactory和SqlSessionFactoryBean类的情况下才加载此配置类，而整两个是正式Mybatis基础包中提供的；有了基础包还不行，还需要DataSource，而且DataSource必须在MybatisAutoConfiguration实例化之前初始化好，SpringBoot是如何实现，继续看另外一个核心方法selectImports():

```
        @Override
        public Iterable<Entry> selectImports() {
            if (this.autoConfigurationEntries.isEmpty()) {
                return Collections.emptyList();
            }
            Set<String> allExclusions = this.autoConfigurationEntries.stream()
                    .map(AutoConfigurationEntry::getExclusions).flatMap(Collection::stream).collect(Collectors.toSet());
            Set<String> processedConfigurations = this.autoConfigurationEntries.stream()
                    .map(AutoConfigurationEntry::getConfigurations).flatMap(Collection::stream)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            processedConfigurations.removeAll(allExclusions);

            return sortAutoConfigurations(processedConfigurations, getAutoConfigurationMetadata()).stream()
                    .map((importClassName) -> new Entry(this.entries.get(importClassName), importClassName))
                    .collect(Collectors.toList());
        }

        private List<String> sortAutoConfigurations(Set<String> configurations,
                AutoConfigurationMetadata autoConfigurationMetadata) {
            return new AutoConfigurationSorter(getMetadataReaderFactory(), autoConfigurationMetadata)
                    .getInPriorityOrder(configurations);
        }
```

首先是对被排除类的一个过滤，然后接下来重点看一下对配置类进行排序的一个方法，具体操作在类AutoConfigurationSorter中进行的，具体方法为getInPriorityOrder():

```
    public List<String> getInPriorityOrder(Collection<String> classNames) {
        AutoConfigurationClasses classes = new AutoConfigurationClasses(this.metadataReaderFactory,
                this.autoConfigurationMetadata, classNames);
        List<String> orderedClassNames = new ArrayList<>(classNames);
        // Initially sort alphabetically
        Collections.sort(orderedClassNames);
        // Then sort by order
        orderedClassNames.sort((o1, o2) -> {
            int i1 = classes.get(o1).getOrder();
            int i2 = classes.get(o2).getOrder();
            return Integer.compare(i1, i2);
        });
        // Then respect @AutoConfigureBefore @AutoConfigureAfter
        orderedClassNames = sortByAnnotation(classes, orderedClassNames);
        return orderedClassNames;
    }
```

首先使用order进行排序，然后使用@AutoConfigureBefore和@AutoConfigureAfter就行排序；order其实就是通过注解@AutoConfigureOrder进行排序的，值是一个整数，结构类似如下：

```
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
```

@AutoConfigureBefore和@AutoConfigureAfter字面意思也很好理解，指定在其他配置类之前和之后，所以可以看到在MybatisAutoConfiguration中有如下配置：

```
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
```

表示在DataSourceAutoConfiguration配置类加载之后才会加载Mybatis配置类，这样就解决了依赖关系；还有上文提到的Mybatis操作数据库依赖的SqlSessionFactory和SqlSession，都在MybatisAutoConfiguration进行了初始化操作；SpringBoot本身其实以及提供了大量常用组件的自动配置类，我们只需要提供满足的特定条件，SpringBoot自动会帮我加载初始化等操作，但是肯定也有自定义配置类的需求，下面用一个简单的实例来看看如何自定义一个自动配置类；

## 自定义配置类

接下来我们用很简单的实例来看一下自定义的流程，一个格式化大写消息的实例；

### 1.pom文件引入依赖

```
<groupId>com.format</groupId>
<artifactId>format-spring-boot-starter</artifactId>
<version>0.0.1-SNAPSHOT</version>
<packaging>jar</packaging>

<name>format-spring-boot-starter</name>
<url>http://maven.apache.org</url>

<properties>
    <java.version>1.8</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-autoconfigure</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <!-- Import dependency management from Spring Boot -->
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>1.5.2.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Spring 官方 Starter通常命名为spring-boot-starter-{name}如 spring-boot-starter-web，Spring官方建议非官方Starter命名应遵循{name}-spring-boot-starter的格式;

### 2.服务类和属性配置类

```
@ConfigurationProperties("format.service")
public class FormatServiceProperties {
    private String type;
    //...get/set省略...
}

public class FormatService {
    private String type;

    public FormatService(String type) {
        this.type = type;
    }

    public String wrap(String word) {
        if(type.equalsIgnoreCase("Upper")){//大写
            return word.toUpperCase();
        }else if(type.equalsIgnoreCase("Lower")){//小写
            return word.toLowerCase();
        }
        return word;
    }
}
```

属性类提供了type参数可以在application.properties中配置，可配置值包括：upper，lower；

### 3.自动配置类和创建spring.factories文件

```
@Configuration
@ConditionalOnClass(FormatService.class)
@EnableConfigurationProperties(FormatServiceProperties.class)
public class FormatAutoConfigure {

    @Autowired
    private FormatServiceProperties properties;

    @Bean
    @ConditionalOnMissingBean
    FormatService formatService() {
        return new FormatService(properties.getType());
    }

}
```

这个就是自定义的自动配置类，SpringBoot启动的时候会根据条件自动初始化；最后在resources/META-INF/下创建spring.factories文件：

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.format.FormatAutoConfigure
```

### 4.测试

在其他SpringBoot中可以引入上面创建的项目，引入方式也很简单：

```
<dependency>
    <groupId>com.format</groupId>
    <artifactId>format-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

同时在application.properties配置格式化类型：

```
format.service.type=upper
```

启动应用，浏览器访问[http://localhost](http://localhost/):8888/format?word=hello，结果为：HELLO

## 总结

本文从使用SpringBoot整合Mybatis开始，然后提出使用中产生的疑问，进而通过分析源码的方式来理解SpringBoot的自动配置机制，最后自定义了一个自动配置类来看看具体如何使用；SpringBoot通过自动配置的方式帮助开发者减少了很大的工作量，达到开箱即用的效果；但是另一方面如果出现问题需要调试可能不是那么好定位。

## 示例代码地址

[https://github.com/ksfzhaohui...](https://github.com/ksfzhaohui/blog.git)

-   \[springboot\]
-   \[format-spring-boot-starter\]