## **系列文章**

[Nginx+Tomcat关于Session的管理](https://my.oschina.net/OutOfMemory/blog/1821751)

[Tomcat Session管理分析](https://my.oschina.net/OutOfMemory/blog/1825123)

[Spring-Session基于Redis管理Session](https://my.oschina.net/OutOfMemory/blog/1837937)

## **前言**

在上文[Tomcat Session管理分析](https://my.oschina.net/OutOfMemory/blog/1825123)介绍了使用tomcat-redis-session-manager来集中式管理session，其中一个局限性就是必须使用tomcat容器；本文介绍的spring-session也能实现session的集中式管理，并且不局限于某种容器；

## **spring-session管理session实战**

### 1.maven依赖的jar

```
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
    <version>1.3.1.RELEASE</version>
    <type>pom</type>
</dependency>
<dependency>
    <groupId>biz.paluch.redis</groupId>
    <artifactId>lettuce</artifactId>
    <version>3.5.0.Final</version>
</dependency>
<dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-web</artifactId>
         <version>4.3.4.RELEASE</version>
</dependency>
```

### 2.准备spring-session.xml配置文件

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
 
    <!--支持注解 -->
    <context:annotation-config />
 
    <bean
        class="org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration" />
 
    <bean
        class="org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory">
        <property name="hostName" value="localhost" />
        <property name="port" value="6379" />
    </bean>
</beans>
```

session同样是使用redis来做集中式存储，为了方便测试使用本地的6379端口redis，LettuceConnectionFactory是redis连接工厂类；  
RedisHttpSessionConfiguration可以简单理解为spring-session使用redis来存储session的功能类，此类本身使用了@Configuration注解，@Configuration注解相当于把该类作为spring的xml配置文件中的，此类中包含了很多bean对象同样也是注解[@Bean](https://my.oschina.net/bean)；

### 3.准备servelt类

```
public class SSessionTest extends HttpServlet {
    private static final long serialVersionUID = 1L;
 
    public SSessionTest() {
        super();
    }
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.getWriter().append("sessionId=" + request.getSession().getId());
    }
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
```

定义了一个简单的servelt，每次请求都在界面打印sessionId；

### 4.配置web.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app>
    <display-name>Archetype Created Web Application</display-name>
 
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath*:spring-session.xml</param-value>
    </context-param>
 
    <filter>
        <filter-name>springSessionRepositoryFilter</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy
        </filter-class>
    </filter>
    <filter-mapping>
        <filter-name>springSessionRepositoryFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener
        </listener-class>
    </listener>
 
    <servlet>
        <servlet-name>SSessionTest</servlet-name>
        <display-name>SSessionTest</display-name>
        <description></description>
        <servlet-class>zh.maven.ssesion.SSessionTest</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>SSessionTest</servlet-name>
        <url-pattern>/SSessionTest</url-pattern>
    </servlet-mapping>
</web-app>
```

首先配置了加载类路径下的spring-session.xml配置文件，然后配置了一个名称为springSessionRepositoryFilter的过滤器；这里定义的class是类DelegatingFilterProxy，此类本身并不是过滤器，是一个代理类，可以通过使用targetBeanName参数来指定具体的过滤器类(如下所示)，如果不指定默认就是filter-name指定的名称；

```
<init-param>
    <param-name>targetBeanName</param-name>
    <param-value>springSessionRepositoryFilter</param-value>
</init-param>
```

### 5.测试

浏览器中访问：[http://localhost](http://localhost/):8080/ssession/SSessionTest，查看结果：

```
sessionId=d520abed-829f-4d0d-9b51-5e9bc9c7e7f2
```

查看redis

```
127.0.0.1:6379> keys *
1) "spring:session:expirations:1530194760000"
2) "spring:session:sessions:expires:d520abed-829f-4d0d-9b51-5e9bc9c7e7f2"
3) "spring:session:sessions:d520abed-829f-4d0d-9b51-5e9bc9c7e7f2"
```

### 6.常见问题

具体异常如下：

```
org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'springSessionRepositoryFilter' available
    at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBeanDefinition(DefaultListableBeanFactory.java:680)
    at org.springframework.beans.factory.support.AbstractBeanFactory.getMergedLocalBeanDefinition(AbstractBeanFactory.java:1183)
    at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:284)
    at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:202)
    at org.springframework.context.support.AbstractApplicationContext.getBean(AbstractApplicationContext.java:1087)
    at org.springframework.web.filter.DelegatingFilterProxy.initDelegate(DelegatingFilterProxy.java:326)
    at org.springframework.web.filter.DelegatingFilterProxy.initFilterBean(DelegatingFilterProxy.java:235)
    at org.springframework.web.filter.GenericFilterBean.init(GenericFilterBean.java:199)
    at org.apache.catalina.core.ApplicationFilterConfig.initFilter(ApplicationFilterConfig.java:285)
    at org.apache.catalina.core.ApplicationFilterConfig.getFilter(ApplicationFilterConfig.java:266)
    at org.apache.catalina.core.ApplicationFilterConfig.<init>(ApplicationFilterConfig.java:108)
    at org.apache.catalina.core.StandardContext.filterStart(StandardContext.java:4981)
    at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5683)
    at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:145)
    at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1702)
    at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1692)
    at java.util.concurrent.FutureTask.run(FutureTask.java:262)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
    at java.lang.Thread.run(Thread.java:745)
```

指定的filter找不到实现类，原因是没有使用配置，此配置可以让系统能够识别相应的注解，而在类RedisHttpSessionConfiguration中使用了大量的注解，其中就有个使用@Bean注解的方法；

## **spring-session管理session分析**

### 1.DelegatingFilterProxy代理类

DelegatingFilterProxy里没有实现过滤器的任何逻辑，具体逻辑在其指定的filter-name过滤器中；

```
@Override
    protected void initFilterBean() throws ServletException {
        synchronized (this.delegateMonitor) {
            if (this.delegate == null) {
                // If no target bean name specified, use filter name.
                if (this.targetBeanName == null) {
                    this.targetBeanName = getFilterName();
                }
                // Fetch Spring root application context and initialize the delegate early,
                // if possible. If the root application context will be started after this
                // filter proxy, we'll have to resort to lazy initialization.
                WebApplicationContext wac = findWebApplicationContext();
                if (wac != null) {
                    this.delegate = initDelegate(wac);
                }
            }
        }
    }
```

初始化过滤器，如果没有配置targetBeanName，则直接使用filter-name，这里指定的是springSessionRepositoryFilter，这个名称是一个固定值此filter在RedisHttpSessionConfiguration中被定义；

### 2.RedisHttpSessionConfiguration配置类

在RedisHttpSessionConfiguration的父类SpringHttpSessionConfiguration中定义了springSessionRepositoryFilter

```
@Bean
    public <S extends ExpiringSession> SessionRepositoryFilter<? extends ExpiringSession> springSessionRepositoryFilter(
            SessionRepository<S> sessionRepository) {
        SessionRepositoryFilter<S> sessionRepositoryFilter = new SessionRepositoryFilter<S>(
                sessionRepository);
        sessionRepositoryFilter.setServletContext(this.servletContext);
        if (this.httpSessionStrategy instanceof MultiHttpSessionStrategy) {
            sessionRepositoryFilter.setHttpSessionStrategy(
                    (MultiHttpSessionStrategy) this.httpSessionStrategy);
        }
        else {
            sessionRepositoryFilter.setHttpSessionStrategy(this.httpSessionStrategy);
        }
        return sessionRepositoryFilter;
    }
```

此方法返回值是SessionRepositoryFilter，这个其实就是真实的过滤器；方法参数sessionRepository同样使用@Bean注解的方式定义；

```
@Bean
    public RedisOperationsSessionRepository sessionRepository(
            @Qualifier("sessionRedisTemplate") RedisOperations<Object, Object> sessionRedisTemplate,
            ApplicationEventPublisher applicationEventPublisher) {
        RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(
                sessionRedisTemplate);
        sessionRepository.setApplicationEventPublisher(applicationEventPublisher);
        sessionRepository
                .setDefaultMaxInactiveInterval(this.maxInactiveIntervalInSeconds);
        if (this.defaultRedisSerializer != null) {
            sessionRepository.setDefaultSerializer(this.defaultRedisSerializer);
        }
 
        String redisNamespace = getRedisNamespace();
        if (StringUtils.hasText(redisNamespace)) {
            sessionRepository.setRedisKeyNamespace(redisNamespace);
        }
 
        sessionRepository.setRedisFlushMode(this.redisFlushMode);
        return sessionRepository;
    }
```

此方法的返回值是RedisOperationsSessionRepository，有关于session持久化到redis的相关操作都在此类中；  
注：持久化到redis只是spring-session的一种方式，也支持持久化到其他数据库中（jdbc，Mongo，Hazelcast等）；

### 3.SessionRepositoryFilter过滤器

所有的请求都会先经过SessionRepositoryFilter过滤器，doFilter方法如下：

```
protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    request.setAttribute(SESSION_REPOSITORY_ATTR, this.sessionRepository);
 
    SessionRepositoryRequestWrapper wrappedRequest = new SessionRepositoryRequestWrapper(
            request, response, this.servletContext);
    SessionRepositoryResponseWrapper wrappedResponse = new SessionRepositoryResponseWrapper(
            wrappedRequest, response);
 
    HttpServletRequest strategyRequest = this.httpSessionStrategy
            .wrapRequest(wrappedRequest, wrappedResponse);
    HttpServletResponse strategyResponse = this.httpSessionStrategy
            .wrapResponse(wrappedRequest, wrappedResponse);
 
    try {
        filterChain.doFilter(strategyRequest, strategyResponse);
    }
    finally {
        wrappedRequest.commitSession();
    }
}
```

request被包装成了SessionRepositoryRequestWrapper对象，response被包装成了SessionRepositoryResponseWrapper对象，SessionRepositoryRequestWrapper中重写了getSession等方法；finally中执行了commitSession方法，将session进行持久化操作；

### 4.SessionRepositoryRequestWrapper包装类

重点看一下重写的getSession方法，代码如下：

```
@Override
        public HttpSessionWrapper getSession(boolean create) {
            HttpSessionWrapper currentSession = getCurrentSession();
            if (currentSession != null) {
                return currentSession;
            }
            String requestedSessionId = getRequestedSessionId();
            if (requestedSessionId != null
                    && getAttribute(INVALID_SESSION_ID_ATTR) == null) {
                S session = getSession(requestedSessionId);
                if (session != null) {
                    this.requestedSessionIdValid = true;
                    currentSession = new HttpSessionWrapper(session, getServletContext());
                    currentSession.setNew(false);
                    setCurrentSession(currentSession);
                    return currentSession;
                }
                else {
                    // This is an invalid session id. No need to ask again if
                    // request.getSession is invoked for the duration of this request
                    if (SESSION_LOGGER.isDebugEnabled()) {
                        SESSION_LOGGER.debug(
                                "No session found by id: Caching result for getSession(false) for this HttpServletRequest.");
                    }
                    setAttribute(INVALID_SESSION_ID_ATTR, "true");
                }
            }
            if (!create) {
                return null;
            }
            if (SESSION_LOGGER.isDebugEnabled()) {
                SESSION_LOGGER.debug(
                        "A new session was created. To help you troubleshoot where the session was created we provided a StackTrace (this is not an error). You can prevent this from appearing by disabling DEBUG logging for "
                                + SESSION_LOGGER_NAME,
                        new RuntimeException(
                                "For debugging purposes only (not an error)"));
            }
            S session = SessionRepositoryFilter.this.sessionRepository.createSession();
            session.setLastAccessedTime(System.currentTimeMillis());
            currentSession = new HttpSessionWrapper(session, getServletContext());
            setCurrentSession(currentSession);
            return currentSession;
        }
 
        private S getSession(String sessionId) {
            S session = SessionRepositoryFilter.this.sessionRepository
                    .getSession(sessionId);
            if (session == null) {
                return null;
            }
            session.setLastAccessedTime(System.currentTimeMillis());
            return session;
        }
```

大致分为三步，首先去本地内存中获取session，如果获取不到去指定的数据库中获取，这里其实就是去redis里面获取，sessionRepository就是上面定义的RedisOperationsSessionRepository对象；如果redis里面也没有则创建一个新的session；

### 5.RedisOperationsSessionRepository类

关于session的保存，更新，删除，获取操作都在此类中；

#### 5.1保存session

每次在消息处理完之后，会执行finally中的commitSession方法，每个session被保存都会创建三组数据，如下所示：

```
127.0.0.1:6379> keys *
1) "spring:session:expirations:1530254160000"
2) "spring:session:sessions:expires:d5e0f376-69d1-4fd4-9802-78eb5a3db144"
3) "spring:session:sessions:d5e0f376-69d1-4fd4-9802-78eb5a3db144"
```

**hash结构记录**  
key格式：spring:session:sessions:\[sessionId\]，对应的value保存session的所有数据包括：creationTime，maxInactiveInterval，lastAccessedTime，attribute；  
**set结构记录**  
key格式：spring:session:expirations:\[过期时间\]，对应的value为expires:\[sessionId\]列表，有效期默认是30分钟，即1800秒；  
**string结构记录**  
key格式：spring:session:sessions:expires:\[sessionId\]，对应的value为空；该数据的TTL表示sessionId过期的剩余时间；

相关代码如下：

```
public void onExpirationUpdated(Long originalExpirationTimeInMilli,
            ExpiringSession session) {
        String keyToExpire = "expires:" + session.getId();
        long toExpire = roundUpToNextMinute(expiresInMillis(session));
 
        if (originalExpirationTimeInMilli != null) {
            long originalRoundedUp = roundUpToNextMinute(originalExpirationTimeInMilli);
            if (toExpire != originalRoundedUp) {
                String expireKey = getExpirationKey(originalRoundedUp);
                this.redis.boundSetOps(expireKey).remove(keyToExpire);
            }
        }
 
        long sessionExpireInSeconds = session.getMaxInactiveIntervalInSeconds();
        String sessionKey = getSessionKey(keyToExpire);
 
        if (sessionExpireInSeconds < 0) {
            this.redis.boundValueOps(sessionKey).append("");
            this.redis.boundValueOps(sessionKey).persist();
            this.redis.boundHashOps(getSessionKey(session.getId())).persist();
            return;
        }
 
        String expireKey = getExpirationKey(toExpire);
        BoundSetOperations<Object, Object> expireOperations = this.redis
                .boundSetOps(expireKey);
        expireOperations.add(keyToExpire);
 
        long fiveMinutesAfterExpires = sessionExpireInSeconds
                + TimeUnit.MINUTES.toSeconds(5);
 
        expireOperations.expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);
        if (sessionExpireInSeconds == 0) {
            this.redis.delete(sessionKey);
        }
        else {
            this.redis.boundValueOps(sessionKey).append("");
            this.redis.boundValueOps(sessionKey).expire(sessionExpireInSeconds,
                    TimeUnit.SECONDS);
        }
        this.redis.boundHashOps(getSessionKey(session.getId()))
                .expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);
    }
 
    static long expiresInMillis(ExpiringSession session) {
        int maxInactiveInSeconds = session.getMaxInactiveIntervalInSeconds();
        long lastAccessedTimeInMillis = session.getLastAccessedTime();
        return lastAccessedTimeInMillis + TimeUnit.SECONDS.toMillis(maxInactiveInSeconds);
    }
 
    static long roundUpToNextMinute(long timeInMs) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMs);
        date.add(Calendar.MINUTE, 1);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        return date.getTimeInMillis();
    }
```

getMaxInactiveIntervalInSeconds默认是1800秒，expiresInMillis返回了一个到期的时间戳；roundUpToNextMinute方法在此基础上添加了1分钟，并且清除了秒和毫秒，返回的long值被用来当做key，用来记录一分钟内应当过期的key列表，也就是上面的set结构记录；  
后面的代码分别为以上三个key值指定了有效期，spring:session:sessions:expires是30分钟，而另外2个都是35分钟；  
理论上只需要为spring:session:sessions:\[sessionId\]指定有效期就行了，为什么还要再保存两个key，官方的说法是依赖redis自身提供的有效期并不能保证及时删除；

#### 5.2定期删除

除了依赖redis本身的有效期机制，spring-session提供了一个定时器，用来定期检查需要被清理的session；

```
@Scheduled(cron = "${spring.session.cleanup.cron.expression:0 * * * * *}")
public void cleanupExpiredSessions() {
    this.expirationPolicy.cleanExpiredSessions();
}
 
public void cleanExpiredSessions() {
    long now = System.currentTimeMillis();
    long prevMin = roundDownMinute(now);
 
    if (logger.isDebugEnabled()) {
        logger.debug("Cleaning up sessions expiring at " + new Date(prevMin));
    }
 
    String expirationKey = getExpirationKey(prevMin);
    Set<Object> sessionsToExpire = this.redis.boundSetOps(expirationKey).members();
    this.redis.delete(expirationKey);
    for (Object session : sessionsToExpire) {
        String sessionKey = getSessionKey((String) session);
        touch(sessionKey);
    }
}
 
/**
 * By trying to access the session we only trigger a deletion if it the TTL is
 * expired. This is done to handle
 * https://github.com/spring-projects/spring-session/issues/93
 *
 * @param key the key
 */
private void touch(String key) {
    this.redis.hasKey(key);
}
```

同样是通过roundDownMinute方法来获取key，获取这一分钟内要被删除的session，此value是set数据结构，里面存放这需要被删除的sessionId；  
（注：这里面存放的的是spring:session:sessions:expires:\[sessionId\]，并不是实际存储session数据的spring:session:sessions:\[sessionId\]）  
首先删除了spring:session:expirations:\[过期时间\]，然后遍历set执行touch方法，并没有直接执行删除操作，看touch方法的注释大致意义就是尝试访问一下key，如果key已经过去则触发删除操作，利用了redis本身的特性；

#### 5.3键空间通知(keyspace notification)

定期删除机制并没有删除实际存储session数据的spring:session:sessions:\[sessionId\]，这里利用了redis的keyspace notification功能，大致就是通过命令产生一个通知，具体什么命令可以配置（包括：删除，过期等）具体可以查看：[http://redisdoc.com/topic/not...](http://redisdoc.com/topic/notification.html)；  
spring-session的keyspace notification配置在ConfigureNotifyKeyspaceEventsAction类中，RedisOperationsSessionRepository负责接收消息通知，具体代码如下：

```
public void onMessage(Message message, byte[] pattern) {
        byte[] messageChannel = message.getChannel();
        byte[] messageBody = message.getBody();
        if (messageChannel == null || messageBody == null) {
            return;
        }
 
        String channel = new String(messageChannel);
 
        if (channel.startsWith(getSessionCreatedChannelPrefix())) {
            // TODO: is this thread safe?
            Map<Object, Object> loaded = (Map<Object, Object>) this.defaultSerializer
                    .deserialize(message.getBody());
            handleCreated(loaded, channel);
            return;
        }
 
        String body = new String(messageBody);
        if (!body.startsWith(getExpiredKeyPrefix())) {
            return;
        }
 
        boolean isDeleted = channel.endsWith(":del");
        if (isDeleted || channel.endsWith(":expired")) {
            int beginIndex = body.lastIndexOf(":") + 1;
            int endIndex = body.length();
            String sessionId = body.substring(beginIndex, endIndex);
 
            RedisSession session = getSession(sessionId, true);
 
            if (logger.isDebugEnabled()) {
                logger.debug("Publishing SessionDestroyedEvent for session " + sessionId);
            }
 
            cleanupPrincipalIndex(session);
 
            if (isDeleted) {
                handleDeleted(sessionId, session);
            }
            else {
                handleExpired(sessionId, session);
            }
 
            return;
        }
    }
```

接收已spring:session:sessions:expires开头的通知，然后截取出sessionId，然后通过sessionId删除实际存储session的数据；  
此处有个疑问就是为什么要引入spring:session:sessions:expires:\[sessionId\]类型key，spring:session:expirations的value直接保存spring:session:sessions:\[sessionId\]不就可以了吗，这里使用此key的目的可能是让有效期和实际的数据分开，如果不这样有地方监听到session过期，而此时session已经被移除，导致获取不到session的内容；并且在上面设置有效期的时候，spring:session:sessions:\[sessionId\]的有效期多了5分钟，应该也是为了这个考虑的；

## **总结**

比起之前介绍的tomcat-redis-session-manager来管理session，spring-session引入了更多的键值，并且还引入了定时器，这无疑增加了复杂性和额外的开销，实际项目具体使用哪种方式还需要权衡一下。