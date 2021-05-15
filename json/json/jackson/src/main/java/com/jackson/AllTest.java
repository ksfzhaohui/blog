package com.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AllTest {

	public static void main(String[] args) throws JsonProcessingException {
		All all = new All();
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(all);
		System.out.println("toJSONString : " + jsonString);
	}
}
