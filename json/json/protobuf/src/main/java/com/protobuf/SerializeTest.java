package com.protobuf;

import com.protobuf.clazz.Person;

public class SerializeTest {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			Person.person.Builder builder = Person.person.newBuilder();
			builder.setId(1);
			builder.setName("zhaohui");
			builder.setEmail("xxxxxxxx@126.com");
			
			Person.person p = builder.build();
			byte[] result = p.toByteArray();
		}
        long endTime = System.currentTimeMillis();
		System.out.println("time:" + (endTime - startTime));
    }
}
