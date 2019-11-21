package zh.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 弱引用
 * 
 * @author hui.zhao.cfs
 *
 */
public class GuavaCache3 {

	public static void main(String[] args) throws InterruptedException {
		Cache<String, Object> cache = CacheBuilder.newBuilder().maximumSize(2).weakValues().build();
		Object value = new Object();
		cache.put("key1", value);

		value = new Object();// 原对象不再有强引用
		System.gc();
		System.out.println(cache.getIfPresent("key1"));
	}
}
