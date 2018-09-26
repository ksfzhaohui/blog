package com.dubboProvider;

import com.dubboApi.DemoService;

public class DemoServiceImpl implements DemoService {

	@Override
	public String syncSayHello(String name) {
		return "sync Hello " + name;
	}

	@Override
	public String asyncSayHello(String name) {
		return "async Hello " + name;
	}
}