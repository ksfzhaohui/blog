package com.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;

public class ParserConfigTest {
	
	public static void main(String[] args) {
		System.out.println(ParserConfig.getGlobalInstance().isAutoTypeSupport());
//		System.out.println(ParserConfig.getGlobalInstance().isSafeMode());
	}

}
