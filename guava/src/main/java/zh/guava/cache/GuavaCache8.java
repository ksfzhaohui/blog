package zh.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 统计信息
 * 可以对Cache的命中率、加载数据时间等信息进行统计
 * @author hui.zhao.cfs
 *
 */
public class GuavaCache8 {

	public static void main(String[] args) throws InterruptedException {
		Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(3).recordStats() // 开启统计信息开关
				.build();
		cache.put("key1", "value1");
		cache.put("key2", "value2");
		cache.put("key3", "value3");
		cache.put("key4", "value4");

		cache.getIfPresent("key1");
		cache.getIfPresent("key2");
		cache.getIfPresent("key3");
		cache.getIfPresent("key4");
		cache.getIfPresent("key5");
		cache.getIfPresent("key6");

		System.out.println(cache.stats()); // 获取统计信息
	}
}
