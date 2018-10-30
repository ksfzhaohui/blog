package com.dubboProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Provider {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Provider.class);

	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-provider.xml" });
		context.start();
		LOGGER.info("server starting...");
		System.in.read(); // 按任意键退出
	}
}
