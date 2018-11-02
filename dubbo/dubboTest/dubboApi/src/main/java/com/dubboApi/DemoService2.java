package com.dubboApi;

import com.dubboApi.bean.TestBean;

public interface DemoService2 {
	String syncSayHello(String name);

	String asyncSayHello(String name);
	
	String sayHello(TestBean bean);
}
