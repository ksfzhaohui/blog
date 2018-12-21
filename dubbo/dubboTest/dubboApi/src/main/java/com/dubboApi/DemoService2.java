package com.dubboApi;

import com.dubboApi.bean.TestBean;

public interface DemoService2 {
	String syncSayHello2(String name);

	String asyncSayHello2(String name);
	
	String sayHello2(TestBean bean);
}
