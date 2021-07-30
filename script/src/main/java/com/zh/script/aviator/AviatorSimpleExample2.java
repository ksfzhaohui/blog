package com.zh.script.aviator;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;

public class AviatorSimpleExample2 {
	public static void main(String[] args) {
		String name = "唐简";
		Map<String, Object> env = new HashMap<>();
		env.put("name", name);
		String result = (String) AviatorEvaluator.execute(" 'Hello ' + name ", env);
		System.out.println(result);
	}
}
