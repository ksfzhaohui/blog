package zh.maven.DynamicConf.redis;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Redis方式加载常量
 * 
 * @author hui.zhao
 *
 */
public class RedisPropPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private RedisWatcher rediswatcher;

	@Override
	protected Properties mergeProperties() throws IOException {
		return loadPropFromRedis(super.mergeProperties());
	}

	/**
	 * 从Redis中加载配置的常量
	 * 
	 * @param result
	 * @return
	 */
	private Properties loadPropFromRedis(Properties result) {
		rediswatcher.watcherKeys();
		rediswatcher.fillProperties(result);
		return result;
	}

	public RedisWatcher getRediswatcher() {
		return rediswatcher;
	}

	public void setRediswatcher(RedisWatcher rediswatcher) {
		this.rediswatcher = rediswatcher;
	}

}
