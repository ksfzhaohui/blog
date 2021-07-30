package com.zh.script.aviator;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;

public class AviatorSimpleExample3 {
	public static void main(String[] args) {
		String str = "小哥哥带你使用Aviator";
		Map<String, Object> env = new HashMap<>();
		env.put("str", str);
		Long length = (Long) AviatorEvaluator.execute("string.length(str)", env);
		System.out.println(length);
	}
}
