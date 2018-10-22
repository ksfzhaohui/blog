package com.serialization.fastjson;

import java.math.BigDecimal;

import com.alibaba.fastjson.JSON;
import com.serialization.bean.Bean1;
import com.serialization.bean.Bean2;

public class FJTest {

	public static void main(String[] args) {
		Bean1 bean1 = new Bean1("haha1", new BigDecimal("1.00"));
		Bean2 bean2 = new Bean2("haha2", new BigDecimal("2.00"));

		String jsonString1 = JSON.toJSONString(bean1);
		String jsonString2 = JSON.toJSONString(bean2);

		System.out.println(jsonString1);
		System.out.println(jsonString2);

		Bean1 bean11 = JSON.parseObject(jsonString1, Bean1.class);
		Bean2 bean22 = JSON.parseObject(jsonString2, Bean2.class);

		System.out.println(bean11.toString());
		System.out.println(bean22.toString());

	}

}
