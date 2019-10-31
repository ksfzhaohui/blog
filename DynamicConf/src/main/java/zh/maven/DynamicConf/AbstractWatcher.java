package zh.maven.DynamicConf;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractWatcher {

	protected Map<String, String> keyValueMap = new ConcurrentHashMap<String, String>();
	protected String[] keyPatterns;

	/**
	 * 监听keys
	 */
	public abstract void watcherKeys();

	/**
	 * key-value填充Properties
	 * 
	 * @param result
	 */
	public abstract void fillProperties(Properties result);

	/**
	 * 根据key获取value
	 * 
	 * @param key
	 * @return
	 */
	public abstract String getKeyValue(String key);

	public String[] getKeyPatterns() {
		return keyPatterns;
	}

	public void setKeyPatterns(String[] keyPatterns) {
		if ((null == keyPatterns) || (keyPatterns.length == 0)) {
			throw new IllegalArgumentException("the defaultkey of this app must not be blank");
		}
		this.keyPatterns = keyPatterns;
	}

}
