package zh.guava.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 设置过期时间
 * 
 * @author hui.zhao.cfs
 *
 */
public class GuavaCache4 {

	public static void main(String[] args) throws InterruptedException {
		test1();
		//test2();
	}

	private static void test1() throws InterruptedException {
		Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(2).expireAfterWrite(300, TimeUnit.SECONDS)
				.build();
		cache.put("key1", "value1");
		int time = 1;
		while (true) {
			System.out.println("第" + time++ + "次取到key1的值为：" + cache.getIfPresent("key1"));
			Thread.sleep(1000);
		}
	}

	private static void test2() throws InterruptedException {
		Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(2).expireAfterAccess(3, TimeUnit.SECONDS)
				.build();
		cache.put("key1", "value1");
		int time = 1;
		while (true) {
			Thread.sleep(time * 1000);
			System.out.println("睡眠" + time++ + "秒后取到key1的值为：" + cache.getIfPresent("key1"));
		}
	}
}
