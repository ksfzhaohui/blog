package zh.maven.DynamicConf.zk;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * zk方式加载常量
 * 
 * @author hui.zhao
 *
 */
public class ZKPropPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private ZKWatcher zkwatcher;

	@Override
	protected Properties mergeProperties() throws IOException {
		return loadPropFromZK(super.mergeProperties());
	}

	/**
	 * 从zk中加载配置的常量
	 * 
	 * @param result
	 * @return
	 */
	private Properties loadPropFromZK(Properties result) {
		zkwatcher.watcherKeys();
		zkwatcher.fillProperties(result);
		return result;
	}

	public ZKWatcher getZkwatcher() {
		return zkwatcher;
	}

	public void setZkwatcher(ZKWatcher zkwatcher) {
		this.zkwatcher = zkwatcher;
	}

}
