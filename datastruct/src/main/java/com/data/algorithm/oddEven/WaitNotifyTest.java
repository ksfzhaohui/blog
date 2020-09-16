package com.data.algorithm.oddEven;

/**
 * 
 * 如果线程调用了对象的 wait()方法，那么线程便会处于该对象的等待池中，等待池中的线程不会去竞争该对象的锁。
 * 
 * 当有线程调用了对象的 notifyAll()方法（唤醒所有 wait 线程）或 notify()方法（只随机唤醒一个 wait 线程），
 * 被唤醒的的线程便会进入该对象的锁池中，锁池中的线程会去竞争该对象锁。
 * 也就是说，调用了notify后只要一个线程会由等待池进入锁池，而notifyAll会将该对象等待池内的所有线程移动到锁池中，等待锁竞争
 * 
 * 优先级高的线程竞争到对象锁的概率大，假若某线程没有竞争到该对象锁，它还会留在锁池中，唯有线程再次调用 wait()方法，它才会重新回到等待池中。
 * 而竞争到对象锁的线程则继续往下执行，直到执行完了 synchronized 代码块，它会释放掉该对象锁，这时锁池中的线程会继续竞争该对象锁。
 * 
 * 所谓唤醒线程，另一种解释可以说是将线程由等待池移动到锁池，notifyAll调用后，会将全部线程由等待池移到锁池，
 * 然后参与锁的竞争，竞争成功则继续执行，如果不成功则留在锁池等待锁被释放后再次参与竞争。而notify只会唤醒一个线程。
 * 
 * @author hui.zhao
 *
 */
public class WaitNotifyTest {

    public static void main(String[] args) throws InterruptedException {
        TestThread tt = new TestThread();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tt.waitT("001");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tt.waitT("002");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tt.waitT("003");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    tt.notifyT();
                    tt.notifyAllT();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        Thread.sleep(2000);
    }

}

class TestThread {

    public synchronized void waitT(String name) throws InterruptedException {
        System.out.println("start wait " + name);
        this.wait();
        System.out.println("end wait " + name);
    }

    public synchronized void notifyT() throws InterruptedException {
        System.out.println("start notify");
        this.notify();
    }

    public synchronized void notifyAllT() throws InterruptedException {
        System.out.println("start notifyAll");
        this.notifyAll();
    }
}
