package com.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.bean.EmailEvent;
import com.spring.bean.PersonBean;

public class PersonTest {

	public static void main(String[] args) {
		System.out.println("开始初始化容器");
		ApplicationContext ac = new ClassPathXmlApplicationContext("person.xml");
		System.out.println("xml加载完毕");
		PersonBean person1 = (PersonBean) ac.getBean("person1");
		System.out.println(person1);
		PersonBean person2 = (PersonBean) ac.getBean("person2");
		System.out.println(person2);
		System.out.println("关闭容器");

		EmailEvent event = new EmailEvent("hello", "abc@163.com", "This is a test");
		ac.publishEvent(event);

		((ClassPathXmlApplicationContext) ac).close();
	}
}
