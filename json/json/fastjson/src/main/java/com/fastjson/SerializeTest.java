package com.fastjson;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializeTest {

	public static void main(String[] args) throws JsonProcessingException {

		long startTime = System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			Person person = new Person();
			person.setId(1);
			person.setName("zhaohui");
			person.setEmail("xxxxxxxx@126.com");
			
			
			String jsonString = JSON.toJSONString(person);
		}
		long endTime = System.currentTimeMillis();

		System.out.println("time:" + (endTime - startTime));
	}
}
