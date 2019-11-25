**CAS介绍**  
比较并交换(compare and swap, CAS)，是原子操作的一种，可用于在多线程编程中实现不被打断的数据交换操作，从而避免多线程同时改写某一数据时由于执行顺序不确定性以及中断的不可预知性产生的数据不一致问题。

CAS操作基于CPU提供的原子操作指令实现，各个编译器根据这个特点实现了各自的原子操作函数。来源维基百科：

C语言：由GNU提供了对应的__sync系列函数完成原子操作。   
Windows：通过WindowsAPI实现了InterLocked Functions。  
C++ 11：STL提供了atomic系列函数。  
JAVA：sun.misc.Unsafe提供了compareAndSwap系列函数。  
C#：通过Interlocked方法实现。  
Go：通过import "sync/atomic"包实现。

java.util.concurrent包完全建立在CAS之上的，借助CAS实现了区别于synchronouse同步锁的一种乐观锁。  
可以看一下AtomicInteger：

```
public final int getAndIncrement() {
     for (;;) {
         int current = get();
         int next = current + 1;
         if (compareAndSet(current, next))
             return current;
     }
}

public final boolean compareAndSet(int expect, int update) {
     return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
}
```

其中牵扯到3个值：current，next以及当前内存中的最新值，当且仅当current和内存中的最新值相同时，才会改变内存值为next。

**CAS的ABA问题**  
ABA问题描述：  
1.进程P1在共享变量中读到值为A  
2.P1被抢占了，进程P2执行  
3.P2把共享变量里的值从A改成了B，再改回到A，此时被P1抢占。  
4.P1回来看到共享变量里的值没有被改变，于是继续执行。

虽然P1以为变量值没有改变，继续执行了，但是这个会引发一些潜在的问题。ABA问题最容易发生在lock free的算法中的，CAS首当其冲，因为CAS判断的是指针的地址。如果这个地址被重用了呢，问题就很大了。（地址被重用是很经常发生的，一个内存分配后释放了，再分配，很有可能还是原来的地址）。

**ABA问题解决方案**  
各种乐观锁的实现中通常都会用**版本戳version**来对记录或对象标记，避免并发操作带来的问题，在Java中，**AtomicStampedReference**也实现了这个作用，它通过包装类**Pair\[E,Integer\]**的元组来对对象标记版本戳stamp，从而避免ABA问题。

下面看一下AtomicInteger和AtomicStampedReference分别执行CAS操作：

```
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

public class ABASingle {

    public static void main(String[] args) {
        AtomicInteger atomicInt = new AtomicInteger(100);
        atomicInt.compareAndSet(100, 101);
        atomicInt.compareAndSet(101, 100);
        System.out.println("new value = " + atomicInt.get());
        boolean result1 = atomicInt.compareAndSet(100, 101);
        System.out.println(result1); // result:true

        AtomicInteger v1 = new AtomicInteger(100);
        AtomicInteger v2 = new AtomicInteger(101);
        AtomicStampedReference<AtomicInteger> stampedRef = new AtomicStampedReference<AtomicInteger>(
                v1, 0);

        int stamp = stampedRef.getStamp();
        stampedRef.compareAndSet(v1, v2, stampedRef.getStamp(),
                stampedRef.getStamp() + 1);
        stampedRef.compareAndSet(v2, v1, stampedRef.getStamp(),
                stampedRef.getStamp() + 1);
        System.out.println("new value = " + stampedRef.getReference());
        boolean result2 = stampedRef.compareAndSet(v1, v2, stamp, stamp + 1);
        System.out.println(result2); // result:false
    }
}
```

AtomicInteger 执行cas操作成功，AtomicStampedReference执行cas操作失败。

这样是不是就是说AtomicInteger存在ABA问题，根本就不能用了；肯定是可以用的，AtomicInteger处理的一个数值，所有就算出现ABA问题问题，也不会有什么影响；但是如果这里是一个地址**（地址被重用是很经常发生的，一个内存分配后释放了，再分配，很有可能还是原来的地址）**，比较地址发现没有问题，但其实这个对象早就变了，这时候就可以使用AtomicStampedReference来解决ABA问题。

**个人博客：[codingo.xyz](http://codingo.xyz/)**