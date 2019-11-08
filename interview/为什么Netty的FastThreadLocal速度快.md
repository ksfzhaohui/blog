## 前言

最近在看netty源码的时候发现了一个叫FastThreadLocal的类，jdk本身自带了ThreadLocal类，所以可以大致想到此类比jdk自带的类速度更快，主要快在什么地方，以及为什么速度更快，下面做一个简单的分析；

## 性能测试

ThreadLocal主要被用在多线程环境下，方便的获取当前线程的数据，使用者无需关心多线程问题，方便使用；为了能说明问题，分别对两个场景进行测试，分别是：多个线程操作同一个ThreadLocal，单线程下的多个ThreadLocal，下面分别测试：

### 1.多个线程操作同一个ThreadLocal

分别对ThreadLocal和FastThreadLocal使用测试代码，部分代码如下：

```
public static void test2() throws Exception {
        CountDownLatch cdl = new CountDownLatch(10000);
        ThreadLocal<String> threadLocal = new ThreadLocal<String>();
        long starTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    threadLocal.set(Thread.currentThread().getName());
                    for (int k = 0; k < 100000; k++) {
                        threadLocal.get();
                    }
                    cdl.countDown();
                }
            }, "Thread" + (i + 1)).start();
        }
        cdl.await();
        System.out.println(System.currentTimeMillis() - starTime + "ms");
    }
```

以上代码创建了10000个线程，同时往ThreadLocal设置，然后get十万次，然后通过CountDownLatch来计算总的时间消耗，运行结果为：**1000ms左右**；  
下面再对FastThreadLocal进行测试，代码类似：

```
public static void test2() throws Exception {
        CountDownLatch cdl = new CountDownLatch(10000);
        FastThreadLocal<String> threadLocal = new FastThreadLocal<String>();
        long starTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            new FastThreadLocalThread(new Runnable() {

                @Override
                public void run() {
                    threadLocal.set(Thread.currentThread().getName());
                    for (int k = 0; k < 100000; k++) {
                        threadLocal.get();
                    }
                    cdl.countDown();
                }
            }, "Thread" + (i + 1)).start();
        }

        cdl.await();
        System.out.println(System.currentTimeMillis() - starTime);
    }
```

运行之后结果为：**1000ms左右**；可以发现在这种情况下两种类型的ThreadLocal在性能上并没有什么差距，下面对第二种情况进行测试；

### 2.单线程下的多个ThreadLocal

分别对ThreadLocal和FastThreadLocal使用测试代码，部分代码如下：

```
    public static void test1() throws InterruptedException {
        int size = 10000;
        ThreadLocal<String> tls[] = new ThreadLocal[size];
        for (int i = 0; i < size; i++) {
            tls[i] = new ThreadLocal<String>();
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                long starTime = System.currentTimeMillis();
                for (int i = 0; i < size; i++) {
                    tls[i].set("value" + i);
                }
                for (int i = 0; i < size; i++) {
                    for (int k = 0; k < 100000; k++) {
                        tls[i].get();
                    }
                }
                System.out.println(System.currentTimeMillis() - starTime + "ms");
            }
        }).start();
    }
```

以上代码创建了10000个ThreadLocal，然后使用同一个线程对ThreadLocal设值，同时get十万次，运行结果：**2000ms左右**;  
下面再对FastThreadLocal进行测试，代码类似：

```
    public static void test1() {
        int size = 10000;
        FastThreadLocal<String> tls[] = new FastThreadLocal[size];
        for (int i = 0; i < size; i++) {
            tls[i] = new FastThreadLocal<String>();
        }
        
        new FastThreadLocalThread(new Runnable() {

            @Override
            public void run() {
                long starTime = System.currentTimeMillis();
                for (int i = 0; i < size; i++) {
                    tls[i].set("value" + i);
                }
                for (int i = 0; i < size; i++) {
                    for (int k = 0; k < 100000; k++) {
                        tls[i].get();
                    }
                }
                System.out.println(System.currentTimeMillis() - starTime + "ms");
            }
        }).start();
    }
```

运行结果：**30ms左右**；可以发现性能达到两个数量级的差距，当然这是在大量访问次数的情况下才有的效果；下面重点分析一下ThreadLocal的机制，以及FastThreadLocal为什么比ThreadLocal更快；

## ThreadLocal的机制

因为我们常用的就是set和get方法，分别看一下对应的源码：

```
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }
    
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }
```

以上代码大致意思：首先获取当前线程，然后获取当前线程中存储的threadLocals变量，此变量其实就是ThreadLocalMap，最后看此ThreadLocalMap是否为空，为空就创建一个新的Map，不为空则以当前的ThreadLocal为key，存储当前value；可以进一步看一下ThreadLocalMap中的set方法：

```
private void set(ThreadLocal<?> key, Object value) {

            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);

            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }
```

大致意思：ThreadLocalMap内部使用一个数组来保存数据，类似HashMap；每个ThreadLocal在初始化的时候会分配一个threadLocalHashCode，然后和数组的长度进行取模操作，所以就会出现hash冲突的情况，在HashMap中处理冲突是使用数组+链表的方式，而在ThreadLocalMap中，可以看到直接使用nextIndex，进行遍历操作，明显性能更差；下面再看一下get方法：

```
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }
```

同样是先获取当前线程，然后获取当前线程中的ThreadLocalMap，然后以当前的ThreadLocal为key，到ThreadLocalMap中获取value：

```
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.get() == key)
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }
        
         private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null)
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }
```

同set方式，通过取模获取数组下标，如果没有冲突直接返回数据，否则同样出现遍历的情况；所以通过分析可以大致知道以下几个问题：  
1.ThreadLocalMap是存放在Thread下面的，ThreadLocal作为key，所以多个线程操作同一个ThreadLocal其实就是在每个线程的ThreadLocalMap中插入的一条记录，不存在任何冲突问题；  
2.ThreadLocalMap在解决冲突时，通过遍历的方式，非常影响性能；  
3.FastThreadLocal通过其他方式解决冲突的问题，达到性能的优化；  
下面继续来看一下FastThreadLocal是通过何种方式达到性能的优化。

## 为什么Netty的FastThreadLocal速度快

Netty中分别提供了FastThreadLocal和FastThreadLocalThread两个类，FastThreadLocalThread继承于Thread，下面同样对常用的set和get方法来进行源码分析：

```
   public final void set(V value) {
        if (value != InternalThreadLocalMap.UNSET) {
            set(InternalThreadLocalMap.get(), value);
        } else {
            remove();
        }
    }

    public final void set(InternalThreadLocalMap threadLocalMap, V value) {
        if (value != InternalThreadLocalMap.UNSET) {
            if (threadLocalMap.setIndexedVariable(index, value)) {
                addToVariablesToRemove(threadLocalMap, this);
            }
        } else {
            remove(threadLocalMap);
        }
    }
```

此处首先对value进行判定是否为InternalThreadLocalMap.UNSET，然后同样使用了一个InternalThreadLocalMap用来存放数据：

```
    public static InternalThreadLocalMap get() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            return fastGet((FastThreadLocalThread) thread);
        } else {
            return slowGet();
        }
    }

    private static InternalThreadLocalMap fastGet(FastThreadLocalThread thread) {
        InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
        if (threadLocalMap == null) {
            thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
        }
        return threadLocalMap;
    }
```

可以发现InternalThreadLocalMap同样存放在FastThreadLocalThread中，不同在于，不是使用ThreadLocal对应的hash值取模获取位置，而是直接使用FastThreadLocal的index属性，index在实例化时被初始化：

```
    private final int index;

    public FastThreadLocal() {
        index = InternalThreadLocalMap.nextVariableIndex();
    }
```

再进入nextVariableIndex方法中：

```
    static final AtomicInteger nextIndex = new AtomicInteger();
     
    public static int nextVariableIndex() {
        int index = nextIndex.getAndIncrement();
        if (index < 0) {
            nextIndex.decrementAndGet();
            throw new IllegalStateException("too many thread-local indexed variables");
        }
        return index;
    }
```

在InternalThreadLocalMap中存在一个静态的nextIndex对象，用来生成数组下标，因为是静态的，所以每个FastThreadLocal生成的index是连续的，再看一下InternalThreadLocalMap中是如何setIndexedVariable的：

```
    public boolean setIndexedVariable(int index, Object value) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object oldValue = lookup[index];
            lookup[index] = value;
            return oldValue == UNSET;
        } else {
            expandIndexedVariableTableAndSet(index, value);
            return true;
        }
    }
```

indexedVariables是一个对象数组，用来存放value；直接使用index作为数组下标进行存放；如果index大于数组长度，进行扩容；get方法直接通过FastThreadLocal中的index进行快速读取：

```
   public final V get(InternalThreadLocalMap threadLocalMap) {
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }

        return initialize(threadLocalMap);
    }
    
    public Object indexedVariable(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length? lookup[index] : UNSET;
    }
```

直接通过下标进行读取，速度非常快；但是这样会有一个问题，可能会造成空间的浪费；

## 总结

通过以上分析我们可以知道在有大量的ThreadLocal进行读写操作的时候，才可能会遇到性能问题；另外FastThreadLocal通过空间换取时间的方式来达到O(1)读取数据；还有一个疑问就是内部为什么不直接使用HashMap(数组+黑红树)来代替ThreadLocalMap。