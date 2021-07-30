package com.zh.script.aviator;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

public class AviatorSimpleExample4 {
	public static void main(String[] args) {
		String expression = "a-(b-c)>100";
		Expression compiledExp = AviatorEvaluator.compile(expression);
		Map<String, Object> env = new HashMap<>();
		env.put("a", 100.3);
		env.put("b", 45);
		env.put("c", -199.100);
		Boolean result = (Boolean) compiledExp.execute(env);
		System.out.println(result);
	}
}
