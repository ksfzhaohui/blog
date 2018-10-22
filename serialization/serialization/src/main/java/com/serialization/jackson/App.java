package com.serialization.jackson;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.serialization.bean.Bean1;
import com.serialization.bean.Bean2;

public class App {

	public static void main(String[] args) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
//		mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

		// SimpleModule desModule = new SimpleModule("testModule");
		// desModule.addDeserializer(Bean2.class, new
		// Bean2Deserializer(Bean2.class));
		// mapper.registerModule(desModule);

		Bean1 bean1 = new Bean1("haha1", new BigDecimal("1.00"));
		Bean2 bean2 = new Bean2("haha2", new BigDecimal("2.00"));

		String bs1 = mapper.writeValueAsString(bean1);
		String bs2 = mapper.writeValueAsString(bean2);

		System.out.println(bs1);
		System.out.println(bs2);

		// Bean1 b1 = mapper.readValue(bs1, Bean1.class);
		// System.out.println(b1.toString());
		// Bean1 b21 = mapper.readValue(bs2, Bean1.class);
		// System.out.println(b21.toString());
		Bean2 b22 = mapper.readValue(bs2, Bean2.class);
		System.out.println(b22.toString());
	}
}
