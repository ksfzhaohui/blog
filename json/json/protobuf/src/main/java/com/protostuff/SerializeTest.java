package com.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class SerializeTest {

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			Person person = new Person();
//			person.setId(1);
			person.setName("zhaohui");
//			person.setEmail("xxxxxxxx@126.com");

			Schema<Person> schema = RuntimeSchema.getSchema(Person.class);
			LinkedBuffer buffer = LinkedBuffer.allocate(1024);
			byte[] data = ProtostuffIOUtil.toByteArray(person, schema, buffer);

		}
		long endTime = System.currentTimeMillis();

		System.out.println("time:" + (endTime - startTime));
	}
}
