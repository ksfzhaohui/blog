package com.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.bean.CDBean1;
import com.spring.bean.CDBean2;

/**
 * 循环依赖--Cyclic dependency
 * 
 * @author hui.zhao.cfs
 *
 */
public class CDTest {
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "cd.xml" });
		CDBean1 cdBean1 = (CDBean1) ctx.getBean("cdBean1");
		System.out.println("cdBean1=" + cdBean1);
		System.out.println("cdBean2=" + cdBean1.getCdBean2());
		System.out.println("=================================");
		CDBean2 cdBean2 = (CDBean2) ctx.getBean("cdBean2");
		System.out.println("cdBean2=" + cdBean2);
		System.out.println("cdBean1=" + cdBean2.getCdBean1());
		((ClassPathXmlApplicationContext) ctx).close();
	}
}
