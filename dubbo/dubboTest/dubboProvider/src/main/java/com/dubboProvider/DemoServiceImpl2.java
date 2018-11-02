package com.dubboProvider;

import com.dubboApi.DemoService2;
import com.dubboApi.bean.TestBean;

public class DemoServiceImpl2 implements DemoService2 {

	@Override
	public String syncSayHello(String name) {
		return "sync Hello " + name;
	}

	@Override
	public String asyncSayHello(String name) {
		return "async Hello " + name;
	}

	@Override
	public String sayHello(TestBean bean) {
		System.out.println("sayHello====="+bean.getName());
		return bean.getName();
	}
}