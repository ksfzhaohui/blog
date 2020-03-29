package com.spring;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

import com.spring.bean.HelloBean;

public class App1 {
	public static void main(String[] args) throws Exception {
		ClassPathResource resource = new ClassPathResource("beans.xml");
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
		reader.loadBeanDefinitions(resource);

		HelloBean helloBean = (HelloBean) factory.getBean("hello");
		System.out.println(helloBean.hello("hello world1"));

	}
}
