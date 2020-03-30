package com.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.bean.HelloBean;

public class App2 {
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "beans.xml" });
		HelloBean helloBean = (HelloBean) ctx.getBean("hello");
		System.out.println(helloBean.hello("hello world2"));

		((ClassPathXmlApplicationContext) ctx).close();
	}
}
