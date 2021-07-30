package com.zh.script.aviator;

import com.googlecode.aviator.AviatorEvaluator;

public class AviatorSimpleExample1 {
	public static void main(String[] args) {
		Long result = (Long) AviatorEvaluator.execute("1+2+3");
		System.out.println(result);
	}
}
