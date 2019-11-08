package zh.maven.interview.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhcacheTest {
	public static void main(String[] args) {
		try {

			// 创建一个CacheManager实例
			CacheManager manager = new CacheManager();
			// 增加一个cache
			manager.addCache("cardCache");
			// 获取所有cache名称
			String[] cacheNamesForManager = manager.getCacheNames();
			int iLen = cacheNamesForManager.length;
			System.out.println("缓存名称列表:----------------------------");
			for (int i = 0; i < iLen; i++) {
				System.out.println(cacheNamesForManager[i].toString());
			}

			// 获取cache对象
			Cache cache = manager.getCache("cardCache");
			// create
			Element element = new Element("username", "howsky");
			cache.put(element);

			// get
			Element element_get = cache.get("username");
			Object value = element_get.getObjectValue();
			System.out.println(value.toString());
			
			Element element_get2 = cache.get("username");
			System.out.println(element_get2==element_get);

			// update
			cache.put(new Element("username", "howsky.net"));
			// get
			Element element_new = cache.get("username");
			
			System.out.println(element_new==element_get);
			
			Object value_new = element_new.getObjectValue();
			System.out.println(value_new.toString());

			cache.remove("username");

			// 移除cache
			manager.removeCache("cardCache");

			// 关闭CacheManager
			manager.shutdown();

		} catch (Exception e) {

			System.out.println(e.getMessage());

		}
	}
}
