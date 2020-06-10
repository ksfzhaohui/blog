package zh.guava.limiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterExample {

	public static void main(String[] args) throws InterruptedException {
		// qps设置为5，代表一秒钟只允许处理五个并发请求
		RateLimiter rateLimiter = RateLimiter.create(5);
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		int nTasks = 10;
		CountDownLatch countDownLatch = new CountDownLatch(nTasks);
		long start = System.currentTimeMillis();
		for (int i = 0; i < nTasks; i++) {
			final int j = i;
			executorService.submit(() -> {
				rateLimiter.acquire(1);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				System.out.println(Thread.currentThread().getName() + " gets job " + j + " done");
				countDownLatch.countDown();
			});
		}
		executorService.shutdown();
		countDownLatch.await();
		long end = System.currentTimeMillis();
		System.out.println("10 jobs gets done by 5 threads concurrently in " + (end - start) + " milliseconds");
	}
}
