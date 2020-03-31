package com.spring.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * BeanFactoryPostProcessor接口与 BeanPostProcessor接口类似,可以对bean的定义(配置元数据)进行处理；
 * 也就是spring ioc运行BeanFactoryPostProcessor在容器实例化任何其他的bean之前读取配置元数据,并有可能修改它；
 * 如果业务需要，可以配置多个BeanFactoryPostProcessor的实现类，通过”order”控制执行次序(要实现Ordered接口)
 * 
 * @author hui.zhao.cfs
 *
 */
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("postProcessBeanFactory修改属性name值");
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition("person1");
		beanDefinition.getPropertyValues().add("name", "liSi");
	}
}
