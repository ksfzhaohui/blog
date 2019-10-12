package zh.maven.interview.threadlocal;

import java.util.Random;

public class ThreadLocalTest {

	private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

	public static void main(String[] args) {

		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					init(Thread.currentThread().getName());
				}
			}, "Thread" + (i + 1)).start();
		}

		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
		}

	}

	private static void init(String value) {
		threadLocal.set(value);
		// 模拟其他操作
		try {
			Thread.sleep(new Random().nextInt(500));
		} catch (InterruptedException e) {
		}

		System.out.println(Thread.currentThread().getName() + "====" + threadLocal.get());
	}

}
