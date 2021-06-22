package com.zh.timeout;

public class Service {
	
	private ThreadLocal<String> local = new ThreadLocal<String>();
	
	public void test() {
		local.set("aaa");
	}
	
	public void test2() {
		System.out.println(local.get());
	}

}
