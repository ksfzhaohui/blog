package com.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Scheduler {

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ac = new ClassPathXmlApplicationContext("scheduler.xml");
		Thread.sleep(10000);
		((ClassPathXmlApplicationContext) ac).close();
	}
}
