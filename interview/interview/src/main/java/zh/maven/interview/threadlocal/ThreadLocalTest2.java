package zh.maven.interview.threadlocal;

import java.util.concurrent.CountDownLatch;

public class ThreadLocalTest2 {

	public static void main(String[] args) throws Exception {
		test1();
		// test2();
		Thread.sleep(100000);
	}

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

}
