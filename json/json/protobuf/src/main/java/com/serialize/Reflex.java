package com.serialize;

import java.lang.reflect.Method;

public class Reflex {

	public static void main(String[] args) {
	}

	public static void test() throws Exception {
		long startTime = System.currentTimeMillis();
		Person person = new Person("10001", "zhaohui");
		Method[] ms = Person.class.getDeclaredMethods();
		for (int i = 0; i < 1_0000_0000; i++) {
			ms[i & ms.length - 1].invoke(person);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Reflex time:" + (endTime - startTime) + "ms");
	}

}
