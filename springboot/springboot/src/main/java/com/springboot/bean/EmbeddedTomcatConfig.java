package com.springboot.bean;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

/**
 * 编程方式配置内嵌servlet容器
 * 
 * @author hui.zhao.cfs
 *
 */
@Component
public class EmbeddedTomcatConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		((TomcatServletWebServerFactory) factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
			@Override
			public void customize(Connector connector) {
				Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
				protocol.setMaxConnections(200);
				protocol.setMaxThreads(200);
				protocol.setSelectorTimeout(3000);
				protocol.setSessionTimeout(3000);
				protocol.setConnectionTimeout(3000);

				connector.setPort(8888);
			}
		});
	}
}
