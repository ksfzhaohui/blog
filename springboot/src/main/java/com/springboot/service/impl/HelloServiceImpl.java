package com.springboot.service.impl;

import org.springframework.stereotype.Service;

import com.springboot.service.HelloService;

//可以添加 @ComponentScan 注解而不需要任何参数，所有应用组件（ @Component , @Service , @Repository , @Controller 等） 
//都会自动注册成Spring Beans
@Service
public class HelloServiceImpl implements HelloService {

	public String hello() {
		return "hello world!!!!666";
	}
}
