package com.serialize;

import com.protostuff.Person;

public class Manual {

	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			Person person = new Person();
			person.setId(1);
			person.setName("zhaohui");
			person.setEmail("xxxxxxxx@126.com");

			
			person.serialize();
		}
		
		long endTime = System.currentTimeMillis();

		System.out.println("time:" + (endTime - startTime));
	}
}
