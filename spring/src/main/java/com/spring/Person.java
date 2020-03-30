package com.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.bean.PersonBean;

public class Person {

	public static void main(String[] args) {
		System.out.println("开始初始化容器");
		ApplicationContext ac = new ClassPathXmlApplicationContext("person.xml");
		System.out.println("xml加载完毕");
		PersonBean person1 = (PersonBean) ac.getBean("person1");
		System.out.println(person1);
		System.out.println("关闭容器");
		((ClassPathXmlApplicationContext) ac).close();
	}
}
