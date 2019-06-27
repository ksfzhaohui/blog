package com.springboot.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.springboot.service.HelloService;

//可以添加 @ComponentScan 注解而不需要任何参数，所有应用组件（ @Component , @Service , @Repository , @Controller 等） 
//都会自动注册成Spring Beans
@Service
@ConfigurationProperties(prefix = "my")
public class HelloServiceImpl implements HelloService {

	@Value("${name}")
	private String ext;

	private List<String> servers = new ArrayList<String>();

	@Value("${random.value}")
	private String random;

	public String hello() {
		return "hello world!!!!666" + ext + ",random=" + random + ",server:" + servers;
	}

	/**
	 * 需要提供set方法
	 * 
	 * @param servers
	 */
	public void setServers(List<String> servers) {
		this.servers = servers;
	}

}
