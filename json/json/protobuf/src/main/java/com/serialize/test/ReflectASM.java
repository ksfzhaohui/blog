package com.serialize.test;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.protostuff.Person;

public class ReflectASM {

	public static void main(String[] args) {
		Person person = new Person();
		long startTime = System.currentTimeMillis();
		int times = 100000;

		for (int i = 0; i < times; i++) {
			MethodAccess access = MethodAccess.get(Person.class);
			access.invoke(person, "setName", "zhaohui");
			String name = (String) access.invoke(person, "getName", null);
		}

		long endTime = System.currentTimeMillis();
		System.out.println("time:" + (endTime - startTime));
	}

}
