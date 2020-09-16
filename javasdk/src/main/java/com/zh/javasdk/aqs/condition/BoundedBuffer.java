package com.zh.javasdk.aqs.condition;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer {

    final Lock lock = new ReentrantLock();
    final Condition notFull = lock.newCondition(); // 不满
    final Condition notEmpty = lock.newCondition(); // 不空

    final Object[] items = new Object[5];
    int putptr, takeptr, count;

    public void put(Object x) throws InterruptedException {
        lock.lock(); // 获取锁
        try {
            // 如果缓冲已满，则等待；直到缓冲不是满的，才将x添加到缓冲中
            while (count == items.length)
                notFull.await();
            items[putptr] = x; // 将x添加到缓冲中
            // 将put统计数putptr+1；如果缓冲已满，则设putptr为0。
            if (++putptr == items.length)
                putptr = 0;
            ++count; // 将缓冲数量+1
            notEmpty.signal(); // 唤醒take线程，因为take线程通过notEmpty.await()等待
            // 打印写入的数据
            System.out.println(Thread.currentThread().getName() + " put  " + (Integer) x);
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    public Object take() throws InterruptedException {
        lock.lock(); // 获取锁
        try {
            while (count == 0) // 如果缓冲为空,则等待;直到缓冲不为空,才将x从缓冲中取出
                notEmpty.await();
            Object x = items[takeptr]; // 将x从缓冲中取出
            // 将take统计数takeptr+1；如果缓冲为空，则设takeptr为0。
            if (++takeptr == items.length)
                takeptr = 0;
            --count; // 将缓冲数量-1
            notFull.signal(); // 唤醒put线程，因为put线程通过notFull.await()等待
            // 打印取出的数据
            System.out.println(Thread.currentThread().getName() + " take " + (Integer) x);
            return x;
        } finally {
            lock.unlock(); // 释放锁
        }
    }

}
