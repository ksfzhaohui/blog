package zh.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Cache对象添加一个移除监听器，这样当有记录被删除时可以感知到这个事件
 * 
 * @author hui.zhao.cfs
 *
 */
public class GuavaCache6 {

	public static void main(String[] args) throws InterruptedException {
		RemovalListener<String, String> listener = new RemovalListener<String, String>() {
			public void onRemoval(RemovalNotification<String, String> notification) {
				System.out.println("[" + notification.getKey() + ":" + notification.getValue() + "] is removed!");
			}
		};
		Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(3).removalListener(listener).build();
		Object value = new Object();
		cache.put("key1", "value1");
		cache.put("key2", "value2");
		cache.put("key3", "value3");
		cache.put("key4", "value3");
		cache.put("key5", "value3");
		cache.put("key6", "value3");
		cache.put("key7", "value3");
		cache.put("key8", "value3");
	}
}
