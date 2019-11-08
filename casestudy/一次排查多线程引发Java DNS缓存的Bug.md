**问题描述**  
最近通知应用在近三个月内出现过2次DNS缓存的问题，第一次在重启之后一直没有出现过问题，所以也没有去重视，但是最近又出现过一次，看来很有必要彻底排查一次；具体的错误日志如下：

```java
2018-03-16 18:53:59,501 ERROR [DefaultMessageListenerContainer-1] (com.bill99.asap.service.CryptoClient.seal(CryptoClient.java:34))- null
java.lang.NullPointerException
    at java.net.InetAddress$Cache.put(InetAddress.java:779) ~[?:1.7.0_79]
    at java.net.InetAddress.cacheAddresses(InetAddress.java:858) ~[?:1.7.0_79]
    at java.net.InetAddress.getAddressesFromNameService(InetAddress.java:1334) ~[?:1.7.0_79]
    at java.net.InetAddress.getAllByName0(InetAddress.java:1248) ~[?:1.7.0_79]
    at java.net.InetAddress.getAllByName(InetAddress.java:1164) ~[?:1.7.0_79]
    at java.net.InetAddress.getAllByName(InetAddress.java:1098) ~[?:1.7.0_79]
    at java.net.InetAddress.getByName(InetAddress.java:1048) ~[?:1.7.0_79]
    at java.net.InetSocketAddress.<init>(InetSocketAddress.java:220) ~[?:1.7.0_79]
    at sun.net.NetworkClient.doConnect(NetworkClient.java:180) ~[?:1.7.0_79]
    at sun.net.www.http.HttpClient.openServer(HttpClient.java:432) ~[?:1.7.0_79]
    at sun.net.www.http.HttpClient.openServer(HttpClient.java:527) ~[?:1.7.0_79]
    at sun.net.www.http.HttpClient.<init>(HttpClient.java:211) ~[?:1.7.0_79]
    at sun.net.www.http.HttpClient.New(HttpClient.java:308) ~[?:1.7.0_79]
    at sun.net.www.http.HttpClient.New(HttpClient.java:326) ~[?:1.7.0_79]
    at sun.net.www.protocol.http.HttpURLConnection.getNewHttpClient(HttpURLConnection.java:997) ~[?:1.7.0_79]
    at sun.net.www.protocol.http.HttpURLConnection.plainConnect(HttpURLConnection.java:933) ~[?:1.7.0_79]
    at sun.net.www.protocol.http.HttpURLConnection.connect(HttpURLConnection.java:851) ~[?:1.7.0_79]
    at sun.net.www.protocol.http.HttpURLConnection.getOutputStream(HttpURLConnection.java:1092) ~[?:1.7.0_79]
    at org.springframework.ws.transport.http.HttpUrlConnection.getRequestOutputStream(HttpUrlConnection.java:81) ~[spring-ws-core.jar:1.5.6]
    at org.springframework.ws.transport.AbstractSenderConnection$RequestTransportOutputStream.createOutputStream(AbstractSenderConnection.java:101) ~[spring-ws-core.jar:1.5.6]
    at org.springframework.ws.transport.TransportOutputStream.getOutputStream(TransportOutputStream.java:41) ~[spring-ws-core.jar:1.5.6]
    at org.springframework.ws.transport.TransportOutputStream.write(TransportOutputStream.java:60) ~[spring-ws-core.jar:1.5.6]

```

具体表现就是出现此异常之后连续的出现大量此异常，同时系统节点不可用；

**问题分析**  
1.既然InetAddress$Cache.put报空指针，那就具体看一下源代码：

```java
if (policy != InetAddressCachePolicy.FOREVER) {
     // As we iterate in insertion order we can
     // terminate when a non-expired entry is found.
     LinkedList<String> expired = new LinkedList<>();
     long now = System.currentTimeMillis();
     for (String key :  ) {
            CacheEntry entry = cache.get(key);
 
            if (entry.expiration >= 0 && entry.expiration < now) {
                    expired.add(key);
            } else {
                    break;
            }
     }
 
     for (String key : expired) {
            cache.remove(key);
     }
}
```

报空指针的的地方就是entry.expiration，也就是说从cache取出来的entry为null，可以查看cache写入的地方：

```java
CacheEntry entry = new CacheEntry(addresses, expiration);
cache.put(host, entry);
```

每次都是new一个CacheEntry然后再put到cache中，不会写入null进去；此时猜测是多线程引发的问题，cache.keySet()在遍历的时候同时也进行了remove操作，导致cache.get(key)到一个空值，查看源代码可以发现一共有两次对cache进行remove的地方，分别是put方法和get方法，put方法代码如上，每次在遍历的时候检测是否过期，然后统一进行remove操作；还有一处就是get方法，代码如下：

```java
public CacheEntry get(String host) {
       int policy = getPolicy();
       if (policy == InetAddressCachePolicy.NEVER) {
             return null;
       }
       CacheEntry entry = cache.get(host);
 
       // check if entry has expired
       if (entry != null && policy != InetAddressCachePolicy.FOREVER) {
             if (entry.expiration >= 0 && entry.expiration < System.currentTimeMillis()) {
                    cache.remove(host);
                    entry = null;
              }
       }
       return entry;
 }
```

类似put方法也是每次在get的时候进行有效期检测，然后进行remove操作；  
所以如果出现多线程问题大概就是：1.同时调用put，get方法，2.多个线程都调用put方法；继续查看源码调用put和get的地方，一共有三处分别是：

```java
private static void cacheInitIfNeeded() {
        assert Thread.holdsLock(addressCache);
        if (addressCacheInit) {
            return;
        }
        unknown_array = new InetAddress[1];
        unknown_array[0] = impl.anyLocalAddress();
 
        addressCache.put(impl.anyLocalAddress().getHostName(),
                         unknown_array);
 
        addressCacheInit = true;
    }
 
    /*
     * Cache the given hostname and addresses.
     */
    private static void cacheAddresses(String hostname,
                                       InetAddress[] addresses,
                                       boolean success) {
        hostname = hostname.toLowerCase();
        synchronized (addressCache) {
            cacheInitIfNeeded();
            if (success) {
                addressCache.put(hostname, addresses);
            } else {
                negativeCache.put(hostname, addresses);
            }
        }
    }
 
    /*
     * Lookup hostname in cache (positive & negative cache). If
     * found return addresses, null if not found.
     */
    private static InetAddress[] getCachedAddresses(String hostname) {
        hostname = hostname.toLowerCase();
 
        // search both positive & negative caches
 
        synchronized (addressCache) {
            cacheInitIfNeeded();
 
            CacheEntry entry = addressCache.get(hostname);
            if (entry == null) {
                entry = negativeCache.get(hostname);
            }
 
            if (entry != null) {
                return entry.addresses;
            }
        }
 
        // not found
        return null;
    }
```

cacheInitIfNeeded只在cacheAddresses和getCachedAddresses方法中被调用，用来检测cache是否已经被初始化了；而另外两个方法都加了对象锁addressCache，所以不会多线程问题；

2.猜测外部直接调用了addressCache，没有使用内部提供的方法  
查看源码可以发现addressCache本身是私有属性，也不存在对外的访问方法

```java
private static Cache addressCache = new Cache(Cache.Type.Positive);
```

那业务代码中应该也不能直接使用，除非使用反射的方式，随手搜了一下全局代码查看关键字”addressCache”，搜到了类似如下代码：

```java
static{
      Class clazz = java.net.InetAddress.class;  
      final Field cacheField = clazz.getDeclaredField("addressCache");  
      cacheField.setAccessible(true);  
      final Object o = cacheField.get(clazz);  
      Class clazz2 = o.getClass();  
      final Field cacheMapField = clazz2.getDeclaredField("cache");  
      cacheMapField.setAccessible(true);  
      final Map cacheMap = (Map)cacheMapField.get(o); 
}
```

通过反射的方式获取了addressCache对象，然后又获取了cache对象（cache是一个LinkedHashMap）,同时提供了一个类似如下的方法：

```java
public void remove(String host){
      cacheMap.remove(host);
}
```

对外提供了一个清除缓存的方法，而且没有使用任何加锁，所以就引发了多线程问题，remove的同时又去调用cache.keySet()遍历；  
但是这种情况和现象不是很匹配，因为如果刚好remove的时候调用了cache.keySet()，虽然本次会抛异常，下次调用的时候有很大几率不会出现异常，并不会出现连续抛异常，节点直接不可用；

3.猜测addressCache出现了有key值，但是取出的value为null  
这样的话这个值一直在addressCache中，每次只要获取address必然报空指针，而且不会被清除，可以做一个测试，测试代码如下：

```java
public class TEst {
 
    public static void main(String[] args) throws IOException, InterruptedException {
        final LinkedHashMap<Integer, HH> map = new LinkedHashMap<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 2000; i++) {
                    map.put(new Random().nextInt(1000), new HH(new Random(100).nextInt()));
                }
            }
        }).start();
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
 
                @Override
                public void run() {
                    for (int i = 0; i < 500; i++) {
                        map.remove(new Random().nextInt(1000));
                    }
                }
            }).start();
        }
        Thread.sleep(2000);
        System.out.println("size=" + map.keySet().size() + "," + map.keySet());
        for (Integer s : map.keySet()) {
            System.out.println(map.get(s));
        }
    }
}
 
class HH {
    private int k;
 
    public HH(int k) {
        this.k = k;
    }
 
    public int getK() {
        return k;
    }
 
    public void setK(int k) {
        this.k = k;
    }
}
```

模拟单线程put操作，业务端会有多条线程同时remove操作，执行看输出结果(可以执行多次看结果)：

```
size=0,[121, 517, 208]
null
null
null
```

可以发现会出现猜测的情况，HashMap中的size属性本身不是线程安全的，所以多线程的情况下有可能出现0，这样导致get方法获取都为null，当然HashMap还有很多其他的多线程问题，因为HashMap也不是为多线程准备的，至此大概了解了原因。

**问题解决**  
给反射获取的cache对象加上和cacheAddresses方法同样的锁，或者直接不在业务代码中处理cache对象；  
可以借鉴一下阿里在github开源的操作dns缓存的项目：[https://github.com/alibaba/java-dns-cache-manipulator](https://github.com/alibaba/java-dns-cache-manipulator)

**总结**  
本次排查问题花了一些时间在排查是不是jdk提供的类是不是有bug，这其实是有些浪费时间的；还有就是在排查问题中不要放过任何一种可能往往问题就发生在那些理所当然的地方。