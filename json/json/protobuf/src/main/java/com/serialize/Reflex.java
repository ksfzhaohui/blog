package com.serialize;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.protostuff.Person;

public class Reflex {

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {


		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			Person person = new Person();
			person.setId(1);
			person.setName("zhaohui");
			person.setEmail("xxxxxxxx@126.com");
			ByteBuffer buff = ByteBuffer.allocate(100);
			Class<Person> clazz = Person.class;
			Field fields[] = clazz.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Class fc = field.getType();
				if (fc == int.class) {
					buff.putInt((int) field.get(person));
					// System.out.println("type="+field.getType()+",value="+field.get(person));
				} else if (fc == String.class) {
					buff.put(((String) field.get(person)).getBytes());
					// System.out.println("type="+field.getType()+",value="+field.get(person));
				}
			}
			buff.array();
		}
		long endTime = System.currentTimeMillis();

		System.out.println("time:" + (endTime - startTime));
	}

}
