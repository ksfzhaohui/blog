package com.zh.script.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class AviatorSimpleExample5 {
	public static void main(String[] args) {
		AviatorEvaluator.addFunction(new MinFunction());
		String expression = "min(a,b)";
		Expression compiledExp = AviatorEvaluator.compile(expression, true);
		Map<String, Object> env = new HashMap<>();
		env.put("a", 100.3);
		env.put("b", 45);
		Double result = (Double) compiledExp.execute(env);
		System.out.println(result);
	}

	static class MinFunction extends AbstractFunction {
		@Override
		public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
			Number left = FunctionUtils.getNumberValue(arg1, env);
			Number right = FunctionUtils.getNumberValue(arg2, env);
			return new AviatorBigInt(Math.min(left.doubleValue(), right.doubleValue()));
		}

		public String getName() {
			return "min";
		}
	}
}
