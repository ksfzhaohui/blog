## 前言

谈到并行，我们可能最先想到的是线程，多个线程一起运行，来提高我们系统的整体处理速度；为什么使用多个线程就能提高处理速度，因为现在计算机普遍都是多核处理器，我们需要充分利用cpu资源；如果站的更高一点来看，我们每台机器都可以是一个处理节点，多台机器并行处理；并行的处理方式可以说无处不在，本文主要来谈谈Java在并行处理方面的努力。

## 无处不在的并行

Java的垃圾回收器，我们可以看到每一代版本的更新，伴随着GC更短的延迟，从serial到cms再到现在的G1，一直在摘掉Java慢的帽子；消息队列从早期的ActiveMQ到现在的kafka和RocketMQ，引入的分区的概念，提高了消息的并行性；数据库单表数据到一定量级之后，访问速度会很慢，我们会对表进行分表处理，引入数据库中间件；Redis你可能觉得本身处理是单线程的，但是Redis的集群方案中引入了slot(槽)的概念；更普遍的就是我们很多的业务系统，通常会部署多台，通过负载均衡器来进行分发；好了还有其他的一些例子，此处不在一一例举。

## 如何并行

我觉得并行的核心在于"拆分"，把大任务变成小任务，然后利用多核CPU也好，还是多节点也好，同时并行的处理，Java历代版本的更新，都在为我们开发者提供更方便的并行处理，从开始的Thread，到线程池，再到fork/join框架，最后到流处理，下面使用简单的求和例子来看看各种方式是如何并行处理的；

### 单线程处理

首先看一下最简单的单线程处理方式，直接使用主线程进行求和操作；

```
public class SingleThread {

    public static long[] numbers;

    public static void main(String[] args) {
        numbers = LongStream.rangeClosed(1, 10_000_000).toArray();
        long sum = 0;
        for (int i = 0; i < numbers.length; i++) {
            sum += numbers[i];
        }
        System.out.println("sum  = " + sum);
    }

}
```

求和本身是一个计算密集型任务，但是现在已经是多核时代，只用单线程，相当于只使用了其中一个cpu，其他cpu被闲置，资源的浪费；

### Thread方式

我们把任务拆分成多个小任务，然后每个小任务分别启动一个线程，如下所示：

```
public class ThreadTest {

    public static final int THRESHOLD = 10_000;
    public static long[] numbers;
    private static long allSum;

    public static void main(String[] args) throws Exception {
        numbers = LongStream.rangeClosed(1, 10_000_000).toArray();
        int taskSize = (int) (numbers.length / THRESHOLD);
        for (int i = 1; i <= taskSize; i++) {
            final int key = i;
            new Thread(new Runnable() {
                public void run() {
                    sumAll(sum((key - 1) * THRESHOLD, key * THRESHOLD));
                }
            }).start();
        }
        Thread.sleep(100);
        System.out.println("allSum = " + getAllSum());
    }

    private static synchronized long sumAll(long threadSum) {
        return allSum += threadSum;
    }

    public static synchronized long getAllSum() {
        return allSum;
    }

    private static long sum(int start, int end) {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }
}
```

以上指定了一个拆分阀值，计算拆分多少个认为，同时启动多少线程；这种处理就是启动的线程数过多，而CPU数有限，更重要的是求和是一个计算密集型任务，启动过多的线程只会带来更多的线程上下文切换；同时线程处理完一个任务就终止了，也是对资源的浪费；另外可以看到主线程不知道何时子任务已经处理完了，需要做额外的处理；所有Java后续引入了线程池。

### 线程池方式

jdk1.5引入了并发包，其中包括了ThreadPoolExecutor，相关代码如下：

```
public class ExecutorServiceTest {

    public static final int THRESHOLD = 10_000;
    public static long[] numbers;

    public static void main(String[] args) throws Exception {
        numbers = LongStream.rangeClosed(1, 10_000_000).toArray();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        CompletionService<Long> completionService = new ExecutorCompletionService<Long>(executor);
        int taskSize = (int) (numbers.length / THRESHOLD);
        for (int i = 1; i <= taskSize; i++) {
            final int key = i;
            completionService.submit(new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    return sum((key - 1) * THRESHOLD, key * THRESHOLD);
                }
            });
        }
        long sumValue = 0;
        for (int i = 0; i < taskSize; i++) {
            sumValue += completionService.take().get();
        }
        // 所有任务已经完成,关闭线程池
        System.out.println("sumValue = " + sumValue);
        executor.shutdown();
    }

    private static long sum(int start, int end) {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }
}
```

上面已经分析了计算密集型并不是线程越多越好，这里创建了JDK默认的线程数:CPU数+1，这是一个经过大量测试以后给出的一个结果；线程池顾名思义，可以重复利用现有的线程；同时利用CompletionService来对子任务进行汇总；合理的使用线程池已经可以充分的并行处理任务，只是在写法上有点繁琐，此时JDK1.7中引入了fork/join框架；

### fork/join框架

分支/合并框架的目的是以递归的方式将可以并行的认为拆分成更小的任务，然后将每个子任务的结果合并起来生成整体结果；相关代码如下：

```
public class ForkJoinTest extends java.util.concurrent.RecursiveTask<Long> {
    
    private static final long serialVersionUID = 1L;
    private final long[] numbers;
    private final int start;
    private final int end;
    public static final long THRESHOLD = 10_000;

    public ForkJoinTest(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    private ForkJoinTest(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            return computeSequentially();
        }
        ForkJoinTest leftTask = new ForkJoinTest(numbers, start, start + length / 2);
        leftTask.fork();
        ForkJoinTest rightTask = new ForkJoinTest(numbers, start + length / 2, end);
        Long rightResult = rightTask.compute();
        // 注：join方法会阻塞，因此有必要在两个子任务的计算都开始之后才执行join方法
        Long leftResult = leftTask.join();
        return leftResult + rightResult;
    }

    private long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }

    public static void main(String[] args) {
        System.out.println(forkJoinSum(10_000_000));
    }

    public static long forkJoinSum(long n) {
        long[] numbers = LongStream.rangeClosed(1, n).toArray();
        ForkJoinTask<Long> task = new ForkJoinTest(numbers);
        return new ForkJoinPool().invoke(task);
    }
}
```

ForkJoinPool是ExecutorService接口的一个实现，子认为分配给线程池中的工作线程；同时需要把任务提交到此线程池中，需要创建RecursiveTask<R>的一个子类；大体逻辑就是通过fork进行拆分，然后通过join进行结果的合并，JDK为我们提供了一个框架，我们只需要在里面填充即可，更加方便；有没有更简单的方式，连拆分都省了，自动拆分合并，jdk在1.8中引入了流的概念；

### 流方式

Java8引入了stream的概念，可以让我们更好的利用并行，使用流代码如下：

```
public class StreamTest {

    public static void main(String[] args) {
        System.out.println("sum = " + parallelRangedSum(10_000_000));
    }

    public static long parallelRangedSum(long n) {
        return LongStream.rangeClosed(1, n).parallel().reduce(0L, Long::sum);
    }
}
```

以上代码是不是非常简单，对于开发者来说完全不需要手动拆分，使用同步机制等方式，就可以让任务并行处理，只需要对流使用parallel()方法，系统自动会对任务进行拆分，当然前提是没有共享可变状态；其实并行流内部使用的也是fork/join框架；

## 总结

本文使用一个求和的实例，来介绍了jdk为开发者提供并行处理的各种方式，可以看到Java一直在为提供更方便的并行处理而努力。

## 参考

<<java8实战>>