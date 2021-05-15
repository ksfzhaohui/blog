package com.test;

import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class TEst2 {
	
	public static void main(String[] args) {
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
		ParserConfig.getGlobalInstance().setSafeMode(true);
		
		System.out.println(ParserConfig.getGlobalInstance().isAutoTypeSupport());
		System.out.println(ParserConfig.getGlobalInstance().isSafeMode());
//		
		
		HashMap<String, Object> map =new HashMap<String, Object>();
		map.put("aa", "11");
		map.put("bb", new B1("11"));
		
		String jsonString1 = JSON.toJSONString(map);
		System.out.println(jsonString1);
		JSON.parseObject(jsonString1);
		String jsonString2 = JSON.toJSONString(map, SerializerFeature.WriteClassName);
		System.out.println(jsonString2);
		
		HashMap<String, Object> map2 =new HashMap<String, Object>();
		Boolean xx = new Boolean(true);
		map2.put("@type", "java.lang.Boolean");
//		map2.put("aa", new Boolean(false));
		String jsonString3 = JSON.toJSONString(map2, SerializerFeature.WriteClassName);
		System.out.println(jsonString3);
		
//		JSON.parseObject(jsonString2,B1.class);
		//System.out.println(JSON.parseObject(jsonString2,B1.class));
//		System.out.println(JSON.parseObject(jsonString2,HashMap.class));
		//System.out.println(JSON.parseObject(jsonString2));
		
		System.out.println(JSON.parseObject(jsonString3));
		
		//System.out.println(JSON.parseObject("{\"@type\":\"java.lang.Boolean\",\"xx\":\"11\"}"));
		
	}

}
