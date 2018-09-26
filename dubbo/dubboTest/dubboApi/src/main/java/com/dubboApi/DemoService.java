package com.dubboApi;

public interface DemoService {
	String syncSayHello(String name);

	String asyncSayHello(String name);
}
