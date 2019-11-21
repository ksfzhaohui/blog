package zh.guava.cache;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 显示清除
 * 
 * @author hui.zhao.cfs
 *
 */
public class GuavaCache5 {
	
	public static void main(String[] args) throws InterruptedException {
        Cache<String,String> cache = CacheBuilder.newBuilder().build();
        Object value = new Object();
        cache.put("key1","value1");
        cache.put("key2","value2");
        cache.put("key3","value3");

        List<String> list = new ArrayList<String>();
        list.add("key1");
        list.add("key2");

        cache.invalidateAll(list);//批量清除list中全部key对应的记录
        System.out.println(cache.getIfPresent("key1"));
        System.out.println(cache.getIfPresent("key2"));
        System.out.println(cache.getIfPresent("key3"));
    }
}
