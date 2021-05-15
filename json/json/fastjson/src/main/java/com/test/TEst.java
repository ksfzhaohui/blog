package com.test;

import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class TEst {
	
	public static void main(String[] args) {
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
		ParserConfig.getGlobalInstance().setSafeMode(true);
		
		System.out.println(ParserConfig.getGlobalInstance().isAutoTypeSupport());
		System.out.println(ParserConfig.getGlobalInstance().isSafeMode());
//		
		String jsonString1 = JSON.toJSONString(new B1("11"));
		System.out.println(jsonString1);
		JSON.parseObject(jsonString1);
		String jsonString2 = JSON.toJSONString(new B1("22"), SerializerFeature.WriteClassName);
		System.out.println(jsonString2);
		
//		JSON.parseObject(jsonString2,B1.class);
		System.out.println(JSON.parseObject(jsonString2,B1.class));
//		System.out.println(JSON.parseObject(jsonString2,HashMap.class));
		System.out.println(JSON.parseObject(jsonString2));
		
	}

}
