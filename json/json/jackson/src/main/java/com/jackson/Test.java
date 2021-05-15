package com.jackson;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackson.impl.Apple;
import com.jackson.impl.Banana;

public class Test {

	public static void main(String[] args) throws IOException {
		Banana banana = new Banana();
		banana.setName("banana");

		Buy buy = new Buy("online", banana);

		ObjectMapper mapper = new ObjectMapper();
//		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

		// 序列化
		String jsonString = mapper.writeValueAsString(buy);
		System.out.println("toJSONString : " + jsonString);

		// 反序列化
		Buy newBuy = mapper.readValue(jsonString, Buy.class);
		banana = (Banana) newBuy.getFruit();
		System.out.println(banana);
	}
}
