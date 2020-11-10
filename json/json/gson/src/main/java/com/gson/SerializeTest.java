package com.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;

public class SerializeTest {

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		Gson gson = new Gson();
		for (int i = 0; i < 100000; i++) {
			Person person = new Person();
			person.setId(1);
			person.setName("zhaohui");
			person.setEmail("xxxxxxxx@126.com");


			String jsonString = gson.toJson(person);

		}
		long endTime = System.currentTimeMillis();

		System.out.println("time:" + (endTime - startTime));
	}
}
