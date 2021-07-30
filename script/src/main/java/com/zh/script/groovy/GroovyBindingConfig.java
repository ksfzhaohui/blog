package com.zh.script.groovy;

import groovy.lang.Binding;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class GroovyBindingConfig implements ApplicationContextAware {
	// 实现ApplicationContextAware接口后的方法类，可以获取Spring中已经实例化的bean
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * 将标注@GroovyFunction注解的类对象绑定到Binding中，并在spring容器中实例化出一个对象
	 * 
	 * @return
	 */
	@Bean("groovyBinding")
	public Binding groovyBinding() {
		Binding groovyBinding = new Binding();
		// 根据注解过滤掉不需要的实例
		Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(GroovyFunction.class);
		for (String beanName : beanMap.keySet()) {
			groovyBinding.setVariable(beanName, beanMap.get(beanName));
		}
		return groovyBinding;
	}
}
