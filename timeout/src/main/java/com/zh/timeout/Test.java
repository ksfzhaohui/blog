package com.zh.timeout;

import java.util.concurrent.TimeUnit;

public class Test {
	
	public static void main(String[] args) throws InterruptedException {
		Service service =new Service();
		service.test();
		
		service.test2();
		
		Thread thread = Thread.currentThread();
		System.gc();
		TimeUnit.SECONDS.sleep(1);
		System.out.println(thread);
	}

}
