package zh.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 设置最大存储
 * 
 * @author hui.zhao.cfs
 *
 */
public class GuavaCache2 {

	public static void main(String[] args) {
		Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(2).build();
		cache.put("key1", "value1");
		cache.put("key2", "value2");
		cache.put("key3", "value3");
		System.out.println("第一个值：" + cache.getIfPresent("key1"));
		System.out.println("第二个值：" + cache.getIfPresent("key2"));
		System.out.println("第三个值：" + cache.getIfPresent("key3"));
	}
}
