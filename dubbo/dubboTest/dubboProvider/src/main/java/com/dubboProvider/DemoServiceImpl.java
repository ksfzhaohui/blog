package com.dubboProvider;

import com.dubboApi.DemoService;
import com.dubboApi.bean.TestBean;

public class DemoServiceImpl implements DemoService {

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