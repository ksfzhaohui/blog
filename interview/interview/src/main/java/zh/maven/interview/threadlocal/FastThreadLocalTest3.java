package zh.maven.interview.threadlocal;

import java.util.concurrent.CountDownLatch;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;

public class FastThreadLocalTest3 {

	public static void main(String[] args) throws Exception {

		 test1();
//		test2();
		Thread.sleep(100000);

	}

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

}
