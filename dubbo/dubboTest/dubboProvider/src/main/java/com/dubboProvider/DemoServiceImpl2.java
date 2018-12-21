package com.dubboProvider;

import com.dubboApi.DemoService2;
import com.dubboApi.bean.TestBean;

public class DemoServiceImpl2 implements DemoService2 {

	@Override
	public String syncSayHello2(String name) {
		return "sync Hello2 " + name;
	}

	@Override
	public String asyncSayHello2(String name) {
		return "async Hello2 " + name;
	}

	@Override
	public String sayHello2(TestBean bean) {
		System.out.println("sayHello2=====" + bean.getName());
		return bean.getName();
	}
}