package com.spring.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PersonBean
		implements BeanNameAware, BeanFactoryAware, ApplicationContextAware, InitializingBean, DisposableBean {

	private String name;

	public PersonBean() {
		System.out.println("PersonService类构造方法");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		System.out.println("set方法被调用");
	}

	// 自定义的初始化函数
	public void myInit() {
		System.out.println("myInit被调用");
	}

	// 自定义的销毁方法
	public void myDestroy() {
		System.out.println("myDestroy被调用");
	}

	public void destroy() throws Exception {
		System.out.println("destory被调用");
	}

	public void afterPropertiesSet() throws Exception {
		System.out.println("afterPropertiesSet被调用");
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		System.out.println("setApplicationContext被调用");
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		System.out.println("setBeanFactory被调用,beanFactory");
	}

	public void setBeanName(String beanName) {
		System.out.println("setBeanName被调用,beanName:" + beanName);
	}

	public String toString() {
		return "name is :" + name;
	}

}
