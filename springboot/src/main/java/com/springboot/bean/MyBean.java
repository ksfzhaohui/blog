package com.springboot.bean;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 * 访问应用参数 如果需要获取传递给 SpringApplication.run(…) 的应用参数， 你可以注入一个
 * org.springframework.boot.ApplicationArguments 类型的bean
 * 
 * @author hui.zhao.cfs
 *
 */
@Component
public class MyBean {

	@Autowired
	public MyBean(ApplicationArguments args) {
		boolean debug = args.containsOption("debug");
		System.err.println("myBean<debug>:" + debug);
		List<String> files = args.getNonOptionArgs();
		System.err.println("myBean<files>:" + files);
	}
}
