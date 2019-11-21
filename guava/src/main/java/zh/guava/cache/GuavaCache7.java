package zh.guava.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 自动加载
 * 
 * Cache的get方法有两个参数，第一个参数是要从Cache中获取记录的key，第二个记录是一个Callable对象。
 * 当缓存中已经存在key对应的记录时，get方法直接返回key对应的记录。
 * 如果缓存中不包含key对应的记录，Guava会启动一个线程执行Callable对象中的call方法，call方法的返回值会作为key对应的值被存储到缓存中，并且被get方法返回
 * 
 * @author hui.zhao.cfs
 *
 */
public class GuavaCache7 {

	private static Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(3).build();

	public static void main(String[] args) throws InterruptedException {

		new Thread(new Runnable() {
			public void run() {
				System.out.println("thread1");
				try {
					String value = cache.get("key", new Callable<String>() {
						public String call() throws Exception {
							System.out.println("load1"); // 加载数据线程执行标志
							Thread.sleep(1000); // 模拟加载时间
							return "auto load by Callable";
						}
					});
					System.out.println("thread1 " + value);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				System.out.println("thread2");
				try {
					String value = cache.get("key", new Callable<String>() {
						public String call() throws Exception {
							System.out.println("load2"); // 加载数据线程执行标志
							Thread.sleep(1000); // 模拟加载时间
							return "auto load by Callable";
						}
					});
					System.out.println("thread2 " + value);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
