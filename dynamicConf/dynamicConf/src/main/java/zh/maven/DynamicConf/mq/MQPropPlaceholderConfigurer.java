package zh.maven.DynamicConf.mq;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * MQ方式加载常量
 * 
 * @author hui.zhao
 *
 */
public class MQPropPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private MQWatcher mqwatcher;

	@Override
	protected Properties mergeProperties() throws IOException {
		return loadPropFromMQ(super.mergeProperties());
	}

	/**
	 * 从MQ中加载配置的常量
	 * 
	 * @param result
	 * @return
	 */
	private Properties loadPropFromMQ(Properties result) {
		mqwatcher.watcherKeys();
		mqwatcher.fillProperties(result);
		return result;
	}

	public MQWatcher getMqwatcher() {
		return mqwatcher;
	}

	public void setMqwatcher(MQWatcher mqwatcher) {
		this.mqwatcher = mqwatcher;
	}

}
