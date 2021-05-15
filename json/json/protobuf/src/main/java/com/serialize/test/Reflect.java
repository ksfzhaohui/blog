package com.serialize.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.protostuff.Person;

public class Reflect {

	public static void main(String[] args) throws Exception {
		Person person = new Person();
		long startTime = System.currentTimeMillis();
//		int times = 100000;
//
//		for (int i = 0; i < times; i++) {
//			Method set = Person.class.getMethod("setName", String.class);
//			set.invoke(person, "zhaoui");
//			Method get = Person.class.getMethod("getName");
//			String name = (String) get.invoke(person);
//		}

		Field fields[] = Person.class.getFields();
		for (Field field : fields) {
			field.get(person);
		}

		long endTime = System.currentTimeMillis();
		System.out.println("time:" + (endTime - startTime));
	}
}
