package com.spring.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import com.spring.bean.PersonBean;

/**
 * 允许在正常的BeanFactoryPostProcessor检测开始之前注册更多的自定义bean。
 * 特别是，BeanDefinitionRegistryPostProcessor可以注册更多的bean定义，然后定义BeanFactoryPostProcessor实例。
 * 也就是说可以借此方法实现自定义的bean。
 *
 */
public class CustomBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("postProcessBeanDefinitionRegistry");
		Class<?> cls = PersonBean.class;
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(cls);
		GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
		definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
		definition.getPropertyValues().add("name", "pepsi02");
		// 注册bean名,一般为类名首字母小写
		registry.registerBeanDefinition("person2", definition);

		BeanDefinition beanDefinition = registry.getBeanDefinition("person1");
		System.out.println("postProcessBeanDefinitionRegistry修改属性name值");
		beanDefinition.getPropertyValues().add("name", "tom");
	}

}
