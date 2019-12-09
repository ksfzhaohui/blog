package com.zh.druid.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;

@Configuration
public class DruidConfig {

	/**
	 * 配置绑定
	 * 
	 * @return
	 */
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DruidDataSource druid() {
		return new DruidDataSource();
	}

	/**
	 * Druid的servlet
	 * 
	 * @return
	 */
	@Bean
	public ServletRegistrationBean statViewServlet() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet());
		Map<String, String> initParams = new HashMap<>();
		initParams.put("loginUsername", "root");
		initParams.put("loginPassword", "root");
		initParams.put("allow", "127.0.0.1");
		bean.setInitParameters(initParams);
		bean.setUrlMappings(Arrays.asList("/druid/*"));
		return bean;
	}

	@Bean
	public FilterRegistrationBean webStatFilter() {
		FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
		HashMap<String, String> initParams = new HashMap<>();
		initParams.put("exclusions", "/css,/druid/*");
		bean.setInitParameters(initParams);
		bean.setUrlPatterns(Arrays.asList("/*"));
		return bean;
	}
}
