package com.serialize.test;

import com.protostuff.Person;

public class Direct {

	public static void main(String[] args) {
		Person person = new Person();
		long startTime = System.currentTimeMillis();
		int times = 100000;

		for (int i = 0; i < times; i++) {
			person.setName("zhaohui");
			String name = person.getName();
		}

		long endTime = System.currentTimeMillis();
		System.out.println("time:" + (endTime - startTime));

	}
}
